package de.jondoe.hue.plan;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import de.jondoe.hue.HueCommands;

public class DailyRandomSwitchPlanControllerTask implements SwitchPlanControllerTask
{
    private class SwitchPlanMarker
    {
        private SwitchPlan plan;
        private Instant executionTime;

        public SwitchPlanMarker(SwitchPlan plan)
        {
            this.plan = plan;

            int startHour = plan.getStartHour();
            int endHour = plan.getEndHour();
            int startMinute = plan.getStartMinute();
            int endMinute = plan.getEndMinute();
            int hour = 0;
            int minute = 0;
            int second = random.nextInt(60);
            if (startHour == endHour)
            {
                hour = startHour;
            }
            else
            {
                hour = nextIntInRange(endHour, startHour);
            }
            if (startMinute == endMinute)
            {
                minute = startMinute;
            }
            else
            {
                minute = nextIntInRange(endMinute, startMinute);
            }

            executionTime = Instant.now().with(ChronoField.HOUR_OF_DAY, hour).with(ChronoField.MINUTE_OF_HOUR, minute)
                                   .with(ChronoField.SECOND_OF_MINUTE, second);
        }

        private int nextIntInRange(int max, int min)
        {
            return random.nextInt((max - min) + 1) + min;
        }

        private void softAction()
        {
            if (lastExecutionTime.isBefore(executionTime))
            {
                try
                {
                    hueCommands.setLightState(plan.getLightId(), plan.getColor(), plan.getState());
                    System.out.println("Plan executed: " + this);
                }
                catch (Exception e)
                {
                    System.out.println("Plan failed: " + this);
                    e.printStackTrace();
                }
            }
        }
    }

    private Random random;
    private Instant lastExecutionTime = Instant.now();
    private List<SwitchPlan> plans;
    private List<SwitchPlanMarker> plansOfTheDays;
    private HueCommands hueCommands;

    public DailyRandomSwitchPlanControllerTask(List<SwitchPlan> plans, HueCommands hueCommands)
    {
        this.plans = plans;
        long seed = plans.stream().map(Object::hashCode).reduce((a, b) -> a + b).get();
        random = new Random(seed);
        resetPlans();
    }

    private void resetPlans()
    {
        plansOfTheDays = plans.stream().map(plan -> new SwitchPlanMarker(plan)).collect(Collectors.toList());
        System.out.println("Plans resetted.");
    }

    private void printPlansNextExecutionTime()
    {
        plansOfTheDays.forEach(plan -> System.out.println(plan));
    }

    @Override
    public void run()
    {
        Instant now = Instant.now();
        if (dayChanged(now))
        {
            resetPlans();
        }
        if (hourChanged(now))
        {
            printPlansNextExecutionTime();
        }
        executePlans();

        lastExecutionTime = now;
    }

    private void executePlans()
    {
        plansOfTheDays.forEach(SwitchPlanMarker::softAction);
    }

    private boolean temporalUnitChanged(Instant now, ChronoField unit)
    {
        return (now.get(unit) - lastExecutionTime.get(unit)) != 0;
    }

    private boolean hourChanged(Instant now)
    {
        return temporalUnitChanged(now, ChronoField.DAY_OF_MONTH);
    }

    private boolean dayChanged(Instant now)
    {
        return temporalUnitChanged(now, ChronoField.HOUR_OF_DAY);
    }

}
