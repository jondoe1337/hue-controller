package de.jondoe.hue;

import com.philips.lighting.hue.sdk.PHHueSDK;

public class Main {
	public static void main(String[] args) {
		PHHueSDK phHueSDK = PHHueSDK.create();

		HueProperties.loadProperties();

		HueCommands hueCommands = new HueCommands();

		HueListener listener = new HueListener(hueCommands);
		phHueSDK.getNotificationManager().registerSDKListener(listener);

		CommandLineInterface cmds = new CommandLineInterface(hueCommands);
		cmds.interact();
	}
}
