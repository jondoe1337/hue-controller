package de.jondoe.hue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.philips.lighting.model.PHLight;

import de.jondoe.hue.plan.SwitchPlan;
import de.jondoe.hue.plan.SwitchPlanController;
import de.jondoe.hue.plan.SwitchPlanFactory;

public class CommandLineInterface
{
    private static final String PATTERN_MAC = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    private static final String EXIT = "exit";
    private static final String HELP = "help";
    private static final String DISCOVER = "discover";
    private static final String CONNECT_LAST = "last";
    private static final String CONNECT = "connect";
    private static final String SHOW_LIGHTS = "showLights";
    private static final String SET_LIGHT_STATE = "setLightState";
    private static final String LIST_COLORS = "listColors";
    private static final String LIST_PLANS = "listPlans";
    private static final String STOP_PLAN = "stopPlan";
    private static final String SCHEDULE_PLAN = "startPlan";
    private HueCommands cmds;
    private SwitchPlanController controller;

    public CommandLineInterface(HueCommands hueCommands)
    {
        cmds = hueCommands;
        controller = new SwitchPlanController(cmds);
    }

    public void interact()
    {
        try (Scanner scanner = new Scanner(System.in))
        {
            Splitter splitter = Splitter.on(' ').trimResults().omitEmptyStrings();
            while (true)
            {
                System.out.print("~");
                String inputString = scanner.nextLine();
                ArrayList<String> cmdList = Lists.newArrayList(splitter.split(inputString));

                if (cmdList.isEmpty())
                {
                    continue;
                }
                String cmd = cmdList.get(0);
                switch (cmd)
                {
                    case HELP:
                        printHelp();
                        break;
                    case DISCOVER:
                        cmds.discoverBridges();
                        break;
                    case CONNECT_LAST:
                        connectToMostRecentBridge();
                        break;
                    case CONNECT:
                        connectToNewBridge(cmdList);
                        break;
                    case SHOW_LIGHTS:
                        printLights();
                        break;
                    case SET_LIGHT_STATE:
                        setLightState(cmdList);
                        break;
                    case LIST_COLORS:
                        System.out.println(Arrays.asList(CommonColors.values()));
                        break;
                    case SCHEDULE_PLAN:
                        schedulePlan();
                        break;
                    case STOP_PLAN:
                        stopPlan();
                        break;
                    case LIST_PLANS:
                        listPlans();
                        break;
                    case EXIT:
                        close();
                        return;
                    default:
                        defaultCmd(cmd);
                }
            }
        }
    }

    private void listPlans()
    {
        List<SwitchPlan> switchPlansFromXml = SwitchPlanFactory.readSwitchPlansFromXml();
        switchPlansFromXml.forEach(plan -> System.out.print(plan.toString()));
    }

    private void close()
    {
        cmds.close();
        controller.close();
    }

    private void schedulePlan()
    {
        List<SwitchPlan> switchPlansFromXml = SwitchPlanFactory.readSwitchPlansFromXml();
        if (switchPlansFromXml.isEmpty())
        {
            throw new IllegalStateException("The conf/plans.xml is empty");
        }
        controller.scheduleAll(switchPlansFromXml);
    }

    private void stopPlan()
    {
        controller.unscheduleAllPlans();
    }

    private void setLightState(ArrayList<String> cmdList)
    {
        if (cmdList.size() != 4)
        {
            throw new IllegalArgumentException("Light-ID, CommonColor and LightState must be given!");
        }
        cmds.setLightState(cmdList.get(1), cmdList.get(2), cmdList.get(3));
    }

    private void printLights()
    {
        List<PHLight> allLights = cmds.getLights();
        for (PHLight light : allLights)
        {
            System.out.println("Light: " + light.getIdentifier() + "  " + light.getName());
        }
    }

    private void connectToNewBridge(ArrayList<String> cmdList)
    {
        if (cmdList.size() != 2)
        {
            System.out.println("The Bridge-ID must be given!");
        }
        else
        {
            String macAddress = cmdList.get(1);
            if (!macAddress.matches(PATTERN_MAC))
            {
                throw new IllegalArgumentException("Given ID is not a MAC-Address!");
            }
            cmds.connectToNewBridge(macAddress);
        }
    }

    private void connectToMostRecentBridge()
    {
        boolean connected = cmds.connectToMostRecentBridge();
        if (connected)
        {
            System.out.println("Successful connected to most recent Hue bridge.");
        }
        else
        {
            System.out.println("No brdige configured recently - try 'connect'.");
        }
    }

    private void printHelp()
    {
        System.out.println("- Philips Hue cmdline client -");
        System.out.println();
        System.out.println("Follwowing commands are given:");
        System.out.println(format(EXIT, "- exit"));
        System.out.println(format(HELP, "- prints this help"));
        System.out.println(format(DISCOVER, "- starts discovery mode to find Hue bridges"));
        System.out.println(format(CONNECT_LAST, "- connects to most recent Hue bridge"));
        System.out.println(format(CONNECT, "- connects to the Hue bridge with the given [MAC-Address]"));
        System.out.println(format(SHOW_LIGHTS, "- lists all the Hue lights, that are registered with the current connected Hue bridge"));
        System.out.println(format(LIST_COLORS, "- lists the available colors"));
        System.out.println(format(LIST_PLANS, "- lists the available plans"));
        System.out.println(format(SET_LIGHT_STATE, "- sets the state of the given Hue [lightId], [color] and the state [integer 0-255]"));
        System.out.println(format(SCHEDULE_PLAN, "- starts to schedule all plans defined in conf/plans.xml"));
        System.out.println(format(STOP_PLAN, "- unschedule the plans, if running"));
        System.out.println();
        System.out.println("The Hue system is a registered trademark of Philips.");
    }

    private String format(String cmd, String desc)
    {
        return String.format("%-30s %s", cmd, desc);
    }

    private void defaultCmd(String cmd)
    {
        System.out.println("Unknown command:" + cmd);
    }
}
