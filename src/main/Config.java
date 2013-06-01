/**
 *
 */
package main;

import java.io.File;

import mods.FloorColor.FloorColorMethod;
import mods.WiFiFinger;
import mods.WiFiFinger.WiFiMethod;

/**
 *This Class is a Configuration Calls storing all Data that may be influenced/changed by the user
 * @author arno
 *
 *
 */
public class Config {

	/////////////////////////////////////////////////////////
	//	Configuration of Map and Data Collection
	//
	//Change this all together! (Map and Start!)
	/////////////////////////////////////////////////////////
	//Start Node or the Walk
	//public static  int startNode = -8;
	/**
	 * Defines the startNode of a walk
	 */
	//public static final int startNode = -8;
	public static final int startNode = -88;

	/**
	 * Directory of the actual walk data (must contain wifi, pos, etc.)
	 */
	public static final File DataDir = new File("collected Data"+File.separatorChar+"1362324839118_d1Cor"+File.separatorChar);
	//public static  File DataDir = new File("collected Data"+File.separatorChar+"1362326733889_dPark1"+File.separatorChar);

	/**
	 * Map Name
	 */
	public static final String mapName = "hoern_eg_triv.osm";
	//public static  String mapName = "hoern_park.osm";


	/////////////////////////////////////////////////////////
	//	 Weightening Function
	/////////////////////////////////////////////////////////
	/**
	 * Enables or disables the dynamic Weighting
	 */
	public static final boolean useDynamicWeighting = true;


	/////////////////////////////////////////////////////////
	//	 Step Detection
	/////////////////////////////////////////////////////////
	/**
	 * Defines the initial StepLength used for the StepDetection System
	 */
	public static final int initialStepLength = 130;
	//public static final int initialStepLength = 180;

	/**
	 * This constant denotes the distance threshold under which the sorrounding edges will not need to be computed for their virtual steps
	 */
	//public static final double APPROXTHRESHOLD = 0.2;

	/**
	 * Below this value the recursive computing of probabilities in StepDetection will break
	 */
	public static final double FP_THRESHOLD = 0.0002;


	/////////////////////////////////////////////////////////
	//	 WifiFingerPrinting
	/////////////////////////////////////////////////////////
	/**
	 * Enables or disables the WifiFingerprinting Module
	 */
	public static final boolean useWiFi = true;

	/**
	 * Defines the "k" for kNNSS (has to be positive!)
	 */
	public static final int WIFIK = 3;

	/**
	 * Defines the accuracy Threshold (in cm) that is needed to take the kNNSS Estimation into account
	 */
	public static final int WIFI_ACC_THRESHOLD = 3000;

	/**
	 * The penalty when being outdoors. If the penalties are higher than the usual weight, the weight is set to 0
	 */
	public static final double OUTDOORPENALTY = 0.2;

	/**
	 * This value denotes the minimal number of detected Wifis in a scan that is needed to take it into account
	 */
	public static final int WIFI_NUMSCANS_THRESHOLD = 5;

	/**
	 * The mods.WiFiFinger.WiFiMethod that should be used
	 */
	public static final WiFiMethod WiFiMethod = WiFiFinger.WiFiMethod.NNSS;


	/////////////////////////////////////////////////////////
	//	 FloorColor
	/////////////////////////////////////////////////////////
	/**
	 * Defines the methods used for storing the estimates of FloorColor (Additive or penalty)
	 */
	public static final FloorColorMethod FColorMethod = FloorColorMethod.STANDARD;

	/**
	 * Enables or disables the FloorColor Module
	 */
	public static final boolean useFloorColor = false;

	/////////////////////////////////////////////////////////
	//	 GPS
	/////////////////////////////////////////////////////////
	/**
	 * Enalbes or disables the GPS module
	 */
	public static final boolean useGPS = false;

	/////////////////////////////////////////////////////////
	//	 QR
	/////////////////////////////////////////////////////////
	/**
	 * Enables or disables the QR Module
	 */
	public static final boolean useQR = false;

	/////////////////////////////////////////////////////////
	//	 Output
	/////////////////////////////////////////////////////////
	/**
	 * Print the route?
	 */
	public static final boolean printRoute = true;

	/**
	 * Print the refernce Points compared to the detected Position?
	 */
	public static final boolean printReference = true;

	/**
	 * Print the Matrix?
	 * (Beware! Printing huge matrices will take long time!)
	 */
	public static final boolean printMatrix = false;

	/**
	 * Log the Matrix into a File using the Logger?
	 * (Beware! Logging huge matrices will take long time!)
	 */
	public static final boolean LogMatrix = false;



	/////////////////////////////////////////////////////////
	//	 Internal Configuration
	/////////////////////////////////////////////////////////


	/**
	 * Non-GPS Nodes with Edges get that length (Fallback for damaged OSM-Files)
	 */
	public static final int initialTemporaryLengthForEdges = Integer.MAX_VALUE;


	// Espilon for inside GPS-Recatnlge Algorithms
	//public static int gpsEpsilon = 1;
	/**
	 * Value for marking invalid/damaged GPS-Coordinates (Fallback)
	 */
	public static final float INVALID_GPS = 500.0f;
}
