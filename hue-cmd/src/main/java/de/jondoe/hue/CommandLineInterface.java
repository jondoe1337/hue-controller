package de.jondoe.hue;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.philips.lighting.model.PHLight;

public class CommandLineInterface
{

    private static final String EXIT = "exit";
    private static final String HELP = "help";
    private static final String DISCOVER = "discover";
    private static final String CONNECT_LAST = "last";
    private static final String CONNECT = "connect";
    private static final String SHOW_LIGHTS = "showLights";
    private static final String SET_LIGHT_STATE = "setLightState";
    private static final String PATTERN_MAC = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    private HueCommands cmds;

    public CommandLineInterface(HueCommands hueCommands)
    {
        cmds = hueCommands;
    }

    public void interact()
    {
        try (Scanner scanner = new Scanner(System.in))
        {
            Splitter splitter = Splitter.on(' ').trimResults().omitEmptyStrings();
            while (true)
            {
                String inputString = scanner.next();
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
                    case EXIT:
                        return;
                    default:
                        defaultCmd(cmd);
                }
            }
        }
    }

    private void setLightState(ArrayList<String> cmdList)
    {
        if (cmdList.size() != 3)
        {
            throw new IllegalArgumentException("Light-ID, CommonColor and State must be given!");
        }
        cmds.setLightState(cmdList.get(0), cmdList.get(1), Boolean.parseBoolean(cmdList.get(2)));
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
        if (cmdList.size() != 1)
        {
            System.out.println("The Bridge-ID must be given!");
        }
        else
        {
            String macAddress = cmdList.get(0);
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
        System.out.println("Follwowing commands are given:");
        System.out.println(EXIT + "\t- exit");
        System.out.println(HELP + "\t- prints this help");
        System.out.println(DISCOVER + "\t- starts discovery mode to find Hue bridges");
        System.out.println(CONNECT_LAST + "\t- connects to most recent Hue bridge");
        System.out.println("The Hue system is a registered trademark of Philips.");
    }

    private void defaultCmd(String cmd)
    {
        System.out.println("Unknown command:" + cmd);
    }
}
