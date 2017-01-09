package de.jondoe.hue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.philips.lighting.hue.sdk.utilities.PHDateTimePattern.RecurringDay;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

import de.jondoe.hue.HueCommands.ScheduleAttributes;
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
    private static final String LIST_RECURRING_SCHEDULES = "listRecurringSchedules";
    private static final String GET_LIGHTS = "getLights";
    private static final String GET_LIGHT_STATE = "getLightState";
    private static final String SET_LIGHT_STATE = "setLightState";
    private static final String LIST_COLORS = "listColors";
    private static final String LIST_PLANS = "listPlans";
    private static final String STOP_PLAN = "stopPlan";
    private static final String SCHEDULE_PLAN = "startPlan";
    private static final String UPDATE_SCHEDULE = "updateSchedule";
    private static final String SUB_LIST_ATTRIBUTES = "listAttributes";
    private static final String SUB_UPDATE = "update";
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
                try
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
                        case GET_LIGHTS:
                            printLights();
                            break;
                        case GET_LIGHT_STATE:
                            printLightState(cmdList);
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
                        case LIST_RECURRING_SCHEDULES:
                            printRecurringSchedules();
                            break;
                        case UPDATE_SCHEDULE:
                            updateSchedule(cmdList);
                            break;
                        case EXIT:
                            close();
                            return;
                        default:
                            defaultCmd(cmd);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateSchedule(ArrayList<String> cmdList)
    {
        if (cmdList.size() < 2)
        {
            System.out.println("Available subcommands: " + SUB_LIST_ATTRIBUTES + " " + SUB_UPDATE);
        }
        switch (cmdList.get(1))
        {
            case SUB_LIST_ATTRIBUTES:
                System.out.println("Available attributes:\n"
                        + Arrays.stream(HueCommands.ScheduleAttributes.values()).map(attr -> attr.getName() + " = " + attr.getDesc())
                                .collect(Collectors.joining("\n")));
                break;
            case SUB_UPDATE:
                String scheduleId = cmdList.get(2);
                Map<ScheduleAttributes, Object> key2value = createAttributeMap(cmdList.subList(3, cmdList.size()));
                cmds.updateSchedule(true, scheduleId, key2value);
                break;
            default:
                System.out.println("Unknown subcommand: " + cmdList.get(1));
        }
    }

    private Map<ScheduleAttributes, Object> createAttributeMap(List<String> subList)
    {
        Map<ScheduleAttributes, Object> retVal = new HashMap<>();
        for (Iterator<String> iterator = subList.iterator(); iterator.hasNext();)
        {
            String string = iterator.next();
            ScheduleAttributes attr = ScheduleAttributes.from(string);
            switch (attr)
            {
                case RANDOM_TIME:
                    int randTime = Integer.valueOf(iterator.next());
                    retVal.put(attr, randTime);
                    break;
                default:
                    throw new IllegalArgumentException("ScheduleAttribute not mapped yet: " + attr);
            }

        }
        return retVal;
    }

    private void printRecurringSchedules()
    {
        for (PHSchedule phSchedule : cmds.getSchedules(true))
        {
            System.out.println(String.format("Identifier: %s", phSchedule.getIdentifier()));
            System.out.println(String.format("Date: %s", phSchedule.getDate().toString()));
            System.out.println(String.format("LightID: %s", phSchedule.getLightIdentifier()));
            System.out.println(String.format("LightState ---\n%s", getLightStateAsString(phSchedule.getLightState())));
            System.out.println(String.format("Name: %s", phSchedule.getName()));
            System.out.println(String.format("RecurringDays: %s", RecurringDay.fromValue(phSchedule.getRecurringDays()).name()));
            System.out.println(String.format("RandomTime: %d", phSchedule.getRandomTime()));
            System.out.println(String.format("LocalTime: %b", phSchedule.getLocalTime()));
            System.out.println("-------");
        }
    }

    private void printLightState(ArrayList<String> cmdList)
    {
        if (cmdList.size() != 2)
        {
            throw new IllegalArgumentException("Light-ID must be given!");
        }
        PHLightState lightState = cmds.getLightState(cmdList.get(1));
        if (lightState == null)
        {
            System.out.println("Light-State unknown.");
        }
        else
        {
            System.out.println(getLightStateAsString(lightState));
        }
    }

    private String getLightStateAsString(PHLightState lightState)
    {
        if (lightState == null)
        {
            return "unknown";
        }
        String retVal = String.format("Hue: %d", lightState.getHue()) //
                + String.format("Brightness: %d", lightState.getBrightness()) //
                + String.format("X: %f", lightState.getX()) //
                + String.format("Y: %f", lightState.getY()) //
                + String.format("On: %b", lightState.isOn());
        return retVal;
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
        System.out.println(format(GET_LIGHTS, "- lists all the Hue lights, that are registered with the current connected Hue bridge"));
        System.out.println(format(LIST_COLORS, "- lists the available colors"));
        System.out.println(format(LIST_PLANS, "- lists the available plans"));
        System.out.println(format(GET_LIGHT_STATE, "- gets the las known state of the given Hue [lightId]"));
        System.out.println(format(SET_LIGHT_STATE, "- sets the state of the given Hue [lightId], [color] and the state [integer 0-255]"));
        System.out.println(format(SCHEDULE_PLAN, "- starts to schedule all plans defined in conf/plans.xml"));
        System.out.println(format(STOP_PLAN, "- unschedule the plans, if running"));
        System.out.println(format(LIST_RECURRING_SCHEDULES, "- lists the available schedules"));
        System.out.println(format(UPDATE_SCHEDULE,
                                  "- updates the available schedules. Subcommands: [" + SUB_LIST_ATTRIBUTES + ", " + SUB_UPDATE + "]"));
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
