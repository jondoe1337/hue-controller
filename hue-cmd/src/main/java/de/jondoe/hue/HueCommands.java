package de.jondoe.hue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.philips.lighting.hue.listener.PHScheduleListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.model.PHSchedule;

public class HueCommands
{
    public enum ScheduleAttributes
    {
        RANDOM_TIME("randomTime", "Random delta in seconds. Params: (int)");

        private ScheduleAttributes(String name, String desc)
        {
            this.name = name;
            this.desc = desc;
        }

        private String name;
        private String desc;

        public String getName()
        {
            return name;
        }

        public String getDesc()
        {
            return desc;
        }

        public static ScheduleAttributes from(String s)
        {
            for (ScheduleAttributes attr : ScheduleAttributes.values())
            {
                if (attr.name.equals(s))
                {
                    return attr;
                }
            }
            throw new IllegalArgumentException("Unknwon attribute: " + s);
        }
    }

    private PHHueSDK phHueSDK = PHHueSDK.getInstance();
    private Map<String, PHAccessPoint> macAddress2bridge = new HashMap<>();
    private PHScheduleListener scheduleListener = new HueScheduleListener();

    public void discoverBridges()
    {
        PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);
    }

    public boolean connectToMostRecentBridge()
    {
        String username = HueProperties.getUsername();
        String lastIpAddress = HueProperties.getLastConnectedIP();

        if (username == null || lastIpAddress == null)
        {
            return false;
        }
        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setIpAddress(lastIpAddress);
        accessPoint.setUsername(username);
        phHueSDK.connect(accessPoint);
        return true;
    }

    public void connectToNewBridge(String macAddress)
    {
        PHAccessPoint accessPoint = macAddress2bridge.get(macAddress);
        if (accessPoint == null)
        {
            System.out.println("The bridge with the following MAC-address was not discovered: " + macAddress);
        }
        else
        {
            phHueSDK.connect(accessPoint);
        }
    }

    public void registerFoundBridges(List<PHAccessPoint> accessPointsList)
    {
        accessPointsList.forEach(ap -> macAddress2bridge.put(ap.getMacAddress(), ap));
    }

    public List<PHLight> getLights()
    {
        PHBridge bridge = getSelectedBridge();
        return bridge.getResourceCache().getAllLights();
    }

    private PHBridge getSelectedBridge()
    {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge == null)
        {
            throw new IllegalStateException("No bridge selected!");
        }
        return bridge;
    }

    public void setLightState(String lightId, CommonColors color, LightState lState)
    {
        PHLight phLight = getLightIdFor(lightId);

        PHLightState lightState = new PHLightState();
        float xy[] = PHUtilities.calculateXYFromRGB(color.getRed(), color.getGreen(), color.getBlue(), phLight.getModelNumber());
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        switch (lState.getCommonState())
        {
            case ON:
                lightState.setOn(true);
                lightState.setBrightness(LightState.MAX_ALLOWED_BRIGHTNESS);
                break;
            case DIMMED:
                lightState.setOn(true);
                lightState.setBrightness(lState.getBrightness());
                break;
            case OFF:
                lightState.setOn(false);
                break;
        }

        String validateState = lightState.validateState();
        if (validateState != null)
        {
            throw new IllegalStateException("Light state was not valid: " + validateState);
        }

        phHueSDK.getSelectedBridge().updateLightState(lightId, lightState, null);
    }

    private PHLight getLightIdFor(String lightId)
    {
        Optional<PHLight> opLight = getLights().stream().filter(light -> light.getIdentifier().equals(lightId)).findFirst();
        if (!opLight.isPresent())
        {
            throw new IllegalStateException("LightId unknown: " + lightId);
        }

        PHLight phLight = opLight.get();
        return phLight;
    }

    public void setLightState(String lightId, String color, String state)
    {
        CommonColors commonColour = CommonColors.valueOf(color.toUpperCase());
        LightState lState = LightState.from(state);

        setLightState(lightId, commonColour, lState);
    }

    public void close()
    {
        phHueSDK.destroySDK();
    }

    public PHLightState getLightState(String lightId)
    {
        PHLight light = getLightIdFor(lightId);
        PHLightState lastKnownLightState = light.getLastKnownLightState();
        return lastKnownLightState;
    }

    public List<PHSchedule> getSchedules(boolean recurring)
    {
        return getSelectedBridge().getResourceCache().getAllSchedules(recurring);
    }

    public void updateSchedule(boolean recurring, String scheduleId, Map<ScheduleAttributes, Object> key2value)
    {
        Optional<PHSchedule> opSchedule = getSchedules(recurring).stream().filter(schedule -> schedule.getIdentifier().equals(scheduleId))
                                                                 .findFirst();
        if (!opSchedule.isPresent())
        {
            throw new IllegalStateException("Schedule unknown: " + scheduleId);
        }

        PHSchedule schedule = opSchedule.get();

        for (ScheduleAttributes attr : key2value.keySet())
        {
            switch (attr)
            {
                case RANDOM_TIME:
                    Object randomTimeObject = key2value.get(attr);
                    schedule.setRandomTime((int) randomTimeObject);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attr);
            }
        }

        getSelectedBridge().updateSchedule(schedule, scheduleListener);
    }
}
