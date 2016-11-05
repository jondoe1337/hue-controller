package de.jondoe.hue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;

public class HueCommands {

	private PHHueSDK phHueSDK = PHHueSDK.getInstance();
	private Map<String, PHAccessPoint> macAddress2bridge = new HashMap<>();

	public void discoverBridges() {
		PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.search(true, true);
	}

	public boolean connectToMostRecentBridge() {
		String username = HueProperties.getUsername();
		String lastIpAddress = HueProperties.getLastConnectedIP();

		if (username == null || lastIpAddress == null) {
			return false;
		}
		PHAccessPoint accessPoint = new PHAccessPoint();
		accessPoint.setIpAddress(lastIpAddress);
		accessPoint.setUsername(username);
		phHueSDK.connect(accessPoint);
		return true;
	}

	public void connectToNewBridge(String macAddress) {
		PHAccessPoint accessPoint = macAddress2bridge.get(macAddress);
		if (accessPoint == null) {
			System.out.println("The bridge with the following MAC-address was not discovered: " + macAddress);
		} else {
			phHueSDK.connect(accessPoint);
		}
	}

	public void registerFoundBridges(List<PHAccessPoint> accessPointsList) {
		accessPointsList.forEach(ap -> macAddress2bridge.put(ap.getMacAddress(), ap));
	}
}
