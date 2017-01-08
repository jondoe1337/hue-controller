package de.jondoe.hue;

import java.util.List;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;

public class HueListener implements PHSDKListener
{

    private PHHueSDK phHueSDK = PHHueSDK.getInstance();
    private HueCommands cmds;

    public HueListener(HueCommands cmds)
    {
        this.cmds = cmds;
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> accessPointsList)
    {
        System.out.println("Bridges found (MAC): ");
        for (PHAccessPoint phAccessPoint : accessPointsList)
        {
            System.out.println(phAccessPoint.getMacAddress());
        }
        cmds.registerFoundBridges(accessPointsList);
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint accessPoint)
    {
        phHueSDK.startPushlinkAuthentication(accessPoint);
        System.out.println("Press Auth Button on Bridge!");
    }

    @Override
    public void onBridgeConnected(PHBridge bridge, String username)
    {
        phHueSDK.setSelectedBridge(bridge);
        phHueSDK.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);
        String lastIpAddress = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
        HueProperties.storeUsername(username);
        HueProperties.storeLastIPAddress(lastIpAddress);
        HueProperties.saveProperties();

        System.out.println("Connected to Bridge.");
    }

    @Override
    public void onCacheUpdated(List<Integer> arg0, PHBridge arg1)
    {
    }

    @Override
    public void onConnectionLost(PHAccessPoint arg0)
    {
    }

    @Override
    public void onConnectionResumed(PHBridge arg0)
    {
    }

    @Override
    public void onError(int code, final String message)
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
    public void onParsingErrors(List<PHHueParsingError> parsingErrorsList)
    {
        for (PHHueParsingError parsingError : parsingErrorsList)
        {
            System.out.println("ParsingError : " + parsingError.getMessage());
        }
    }
}
