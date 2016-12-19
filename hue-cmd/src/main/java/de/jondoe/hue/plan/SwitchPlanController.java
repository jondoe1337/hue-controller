package de.jondoe.hue.plan;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.jondoe.hue.HueCommands;

public class SwitchPlanController implements Closeable
{
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> controllerFuture = null;
    private HueCommands cmds;

    public SwitchPlanController(HueCommands cmds)
    {
        this.cmds = cmds;
    }

    @Override
    public void close()
    {
        executor.shutdown();
        try
        {
            executor.awaitTermination(5l, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void scheduleAll(List<SwitchPlan> plans)
    {
        unscheduleAllPlans();
        // TODO: Move this maybe to plan xml declaration?
        SwitchPlanControllerTask task = new DailyRandomSwitchPlanControllerTask(plans, cmds);
        controllerFuture = executor.scheduleAtFixedRate(task, 10, 10, TimeUnit.SECONDS);
        System.out.println("Plans scheduled.");
    }

    public void unscheduleAllPlans()
    {
        if (controllerFuture != null)
        {
            controllerFuture.cancel(true);
        }
    }
}
