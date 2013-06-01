package tests;


import mods.FloorColor;
import mods.FloorColor.FloorColorMethod;


public class QuickModTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//File file = new File(main.Config.DataDir+"wifi.csv");
		//String myLine = "1.SSID: halifax_wlan, BSSID: 00:18:e7:d4:38:7e, capabilities: [WPA2-EAP-CCMP][ESS], level: -89, frequency: 2437";
		//int pos = myLine.lastIndexOf("level: ");
		//Integer.parseInt(myLine.substring(pos+7,myLine.lastIndexOf(",")));
		//System.out.println(myLine.substring(pos+7,myLine.lastIndexOf(",")));
		
		//main.Logger.setOutput(main.Output.CONSOLE);
		//WiFiFinger wf = new WiFiFinger();
		
		//wf.init();

		//wf.testStep();
		
		FloorColor fc = new FloorColor(null,FloorColorMethod.STANDARD);
		fc.init();
		//fc.getMeanColorFromFile(new File("testFiles/testpic.jpg"));
		//fc.getMeanColorFromFile(new File("testFiles/env/DarkCyan.jpg"));
		
		//GPS myGPS = new GPS();
		//myGPS.init();
		
		//PosReference ref = new PosReference(null);
		//ref.load();
		
		//StepDetection fp = new StepDetection(null);
		//fp.init();
	}

}
