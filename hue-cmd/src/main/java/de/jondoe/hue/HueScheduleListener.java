package de.jondoe.hue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHSchedule;

public class HueScheduleListener implements PHScheduleListener
{

    @Override
    public void onError(int code, String message)
    {
        if (code == PHHueError.BRIDGE_NOT_RESPONDING)
        {
            System.out.println("Bridge not responding: " + message);
        }
        else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED)
        {
            System.out.println("Auth Button not pressed: " + message);
        }
        else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED)
        {
            System.out.println("Auth failed: " + message);
        }
        else if (code == PHMessageType.BRIDGE_NOT_FOUND)
        {
            System.out.println("Bridge not found: " + message);
        }
        else
        {
            System.out.println("Unknown Error: code=" + code + " message=" + message);
        }
    }

    @Override
    public void onStateUpdate(Map<String, String> successAttribute, List<PHHueError> errorAttribute)
    {
        System.out.println("Schedule updated.\nSuccess for attributes:\n"
                + successAttribute.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining("\n"))
                + "\nUnsuccessful:\n"
                + errorAttribute.stream().map(error -> error.getCode() + ":" + error.getMessage()).collect(Collectors.joining("\n")));

    }

    @Override
    public void onSuccess()
    {
        System.out.println("Schedule action succeeded");
    }

    @Override
    public void onCreated(PHSchedule schedule)
    {
        System.out.println("Schedule created: " + schedule.getName());
    }
}
