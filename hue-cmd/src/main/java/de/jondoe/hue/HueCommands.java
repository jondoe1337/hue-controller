package de.jondoe.hue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

public class HueCommands
{
    public enum CommonColors
    {
     WHITE(255, 255, 255);

        private int r;
        private int g;
        private int b;

        private CommonColors(int r, int g, int b)
        {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int getRed()
        {
            return r;
        }

        public int getGreen()
        {
            return g;
        }

        public int getBlue()
        {
            return b;
        }
    }

    private PHHueSDK phHueSDK = PHHueSDK.getInstance();
    private Map<String, PHAccessPoint> macAddress2bridge = new HashMap<>();

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

    public void setLightState(String lightId, String commonColour, boolean on)
    {
        CommonColors color = CommonColors.valueOf(commonColour.toUpperCase());

        PHLightState lightState = new PHLightState();
        float xy[] = PHUtilities.calculateXYFromRGB(color.getRed(), color.getGreen(), color.getBlue(), "LCT001");
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        lightState.setOn(on);

        phHueSDK.getSelectedBridge().updateLightState(lightId, lightState, null);
    }
}
