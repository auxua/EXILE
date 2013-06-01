/**
 * 
 */
package mods;

import graphenbib.GPSCoordinate;
import graphenbib.InvalidGPSCoordinateException;
import graphenbib.MapGraph;
import graphenbib.MapNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import main.BiMap.MyEntry;
import main.Config;
import main.Emulator;
import main.Logger;

/**
 * @author arno
 *	This module implements NNSS, direct/indirect kNNSS and also performes linear/squares approximations on these data.  
 */
public class WiFiFinger extends AbstractMod implements Emulateable {

	/**
	 * This enum defines methods of the Wii-Fingerprinting for estimating the position
	 * 
	 * - NNSS is the standard next Neighbor in Signal Space (return vector of (0..,0,1,0..0)
	 * - direct kNNSS is the direct (fast) k NNSS method that detects the k nearest neighbors and returns them in the probaility vector
	 * - indirect kNNSS is like direct kNNSS but additionally the k best map points are computed matching the fingerprints (See figure in thesis) 
	 * 
	 * @author arno
	 *
	 */
	public static enum WiFiMethod {
		NNSS, directkNNSS, indirectkNNSS, directkNNSSLin, directkNNSSSqu, indirectkNNSSLin, indirectkNNSSSqu
	}
	
	/**
	 * This Mod will use static sensor Set
	 */
	private final Sensor[] sensors = {Sensor.WIFI};
	
	public Sensor[] getSensors() {
		return this.sensors;
	}
	

	/**
	 * To give a reliable estimate on the distance in Signal Space there is need for cnstant dimensions of the signal space.
	 * This is realized by storing all known BSSIDs in this set.
	 */
	private Set<String> signalSpace = new HashSet<String>();
	
	/**
	 * This HashMap stores the complete amount of WiFiFingerprints for the actual map that are available
	 * (K,V) == (NodeID, WIFiInfo-Object)  
	 */
	private HashMap<Integer, WiFiInfo> mapFingers = new HashMap<Integer, WiFiInfo>();
	
	/**
	 * This HashMap stores all the scan Results during the walk. 
	 * (K,V) = (TimeStamp, WiFiInfo-Object)
	 */
	private TreeMap<Long, WiFiInfo> wiFiScans = new TreeMap<Long, WiFiInfo>();
	
	

	/**
	 * Denotes the Method of computing the location (see enum WiFiMethod)
	 */
	private WiFiMethod method;

	/**
	 * Parameter for kNNSS methods
	 */
	private int k;
	
	
	/**
	 * This class represents a scan result for a certain timestamp or Environmental Node.
	 * @author arno
	 *
	 */
	private class WiFiInfo {
		
		//private long iTimestamp;
		private HashMap<String,Integer> scanData = new HashMap<String,Integer>();
		
		
		/**
		 * This constant is used for BSSIDs nt in an actual WiFi Measurement
		 */
		private final int NOSCAN = Integer.MIN_VALUE; 
		
		/**
		 * Creates WiFiInfo-Object for the corresponding Timestamp it
		 */
		public WiFiInfo() {	}
		
		@SuppressWarnings("unused")
		public HashMap<String, Integer> getScanData() {
			return this.scanData;
		}
		
		public void add(String ID, int level) {
			this.scanData.put(ID, level);
		}
		
		public int getScanEntry(String ID) {
			Integer ret = this.scanData.get(ID);
			if (ret == null)
				return this.NOSCAN;
			return ret;
		}
		
		/**
		 * Computes the euclidean distance in signal Space
		 * @param other the WiFiInfo belonging to the other point in signal space 
		 * @return euclidean distance
		 */
		public double distanceTo(WiFiInfo other) {
			double dist = -1; String ID; int myValue, otherValue;
			
			//Iterate over the signal space and compute the euclidean distance 
			Iterator<String> it = signalSpace.iterator();
			while (it.hasNext()) {
				ID = it.next();
				myValue = this.getScanEntry(ID);
				otherValue = other.getScanEntry(ID);
				dist += ((myValue-otherValue)*(myValue-otherValue));
			}
			
			//Now we have the sum of all squared dist. - use sqrt to finish!
			return Math.sqrt(dist);
		}
	}
	
	public WiFiFinger(MapGraph graph,  int k, WiFiMethod m) {
		super(graph);
		this.k = k;
		this.method = m;
	}
	
	public WiFiFinger(MapGraph graph) {
		super(graph);
		this.k = 3;
		this.method = WiFiMethod.directkNNSS;
		//this.method = WiFiMethod.NNSS;
	}
	
	@Override
	public boolean init() {
		
		Logger.getInstance().log("WiFiFinger.init", "Started to load Data");
		
		//-----------------
		// Read the environment
		//-----------------
		
		File fEnv = new File ("environmental Data"+File.separatorChar+Config.mapName+File.separatorChar+"WiFiFinger"+File.separatorChar);
		//No Data for this map available - abort
		if (fEnv.exists() == false) return false;
		
		File[] listOfFiles = fEnv.listFiles();
		
		//Read every available file in the dir
		for (File wFile : listOfFiles) {
		    if (wFile.isFile()) {
		        readFingerFile(wFile);
		    }
		}
		
		Logger.getInstance().log("WiFiFinger.init", "Read all Fingers of the Map");
		
		//-----------------
		// Read the scans form walk
		//-----------------
		
		File fScans = new File (Config.DataDir+File.separator+"wifi.csv");
		//No Data for this map available - abort
		if (fEnv.exists() == false) return false;
		
		readScanFile(fScans);
		
		Logger.getInstance().log("WiFiFinger.init", "Read all Walk-Scans");
		
		Logger.getInstance().log("WiFiFinger.init", "Signal Space Dimension: "+this.signalSpace.size());
		Logger.getInstance().log("WiFiFinger.init", "Fingers read: "+this.mapFingers.size());
		Logger.getInstance().log("WiFiFinger.init", "Walk Scans read: "+this.wiFiScans.size());
				
		//System.gc();
		
		return true;
	}

	/**
	 * Reads the scanFile of wifi from the dCollector.
	 * Syntax of theFile is:
	 *  
	 *  {
	 * Time passed (ms): #TIMESTAMP
	 * {#SSID: (..)  BSSID: {#BSSID}, (..) level: #LEVEL, (..)} ^*
	 *  } ^*
	 *  
	 *  BSSID, level and Timestamp will be taken for this system
	 * 
	 * @param fScans the File containing the scans in this declared formatting
	 */
	private void readScanFile(File fScans) {
		String myLine="", tmp;
		int pos, level;
		Long timeStamp;
		WiFiInfo wifiInfo  = new WiFiInfo();
		
		try{
			FileReader fStream = new FileReader(fScans);
			BufferedReader br = new BufferedReader(fStream);
			
			//Empty File Return
			if ((myLine = br.readLine()) == null) return;
			//The constant formatting of the TimeStamp-Lines allows this
			timeStamp = Long.parseLong(myLine.substring(18));
			
			while(true) {
				//End of File reached?
				if ((myLine = br.readLine()) == null) break;
				//Start of a new Scan?
				if (myLine.startsWith("Time")) {
					this.wiFiScans.put(timeStamp, wifiInfo);
					timeStamp = Long.parseLong(myLine.substring(18));
					wifiInfo = new WiFiInfo();
					continue;
				}
				
				//Take data of the actual scan 
				pos = myLine.lastIndexOf("BSSID: ");
				tmp = myLine.substring(pos+7,pos+24);
				//if ((myLine = br.readLine()) == null) break;
				//line++;
				pos = myLine.lastIndexOf("level: ");
				level = Integer.parseInt(myLine.substring(pos+7,myLine.lastIndexOf(",")));
				
				//Store this (K,V)-pair
				wifiInfo.add(tmp, level);
			}
			
			//Close and store last Scan
			this.wiFiScans.put(timeStamp, wifiInfo);
			
			//Close File
			br.close();
		
		} catch (Exception ex) {
			Logger.getInstance().log("WiFiFinger.readScanFile", ex);
			ex.printStackTrace();
		}
		
		
	}

	/**
	 * Reads the FingerFile and stores is
	 * @param wFile actual FingerFile
	 */
	private void readFingerFile(File wFile) {
		String myLine, tmp;
		WiFiInfo wifiInfo  = new WiFiInfo();
		
		try{
			FileReader fStream = new FileReader(wFile);
			BufferedReader br = new BufferedReader(fStream);
			
			while(true) {
				//End of File reached?
				if ((myLine = br.readLine()) == null) break;
				//First store BSSID
				this.signalSpace.add(myLine);
				tmp = myLine;
				if ((myLine = br.readLine()) == null) break;
				wifiInfo.add(tmp, Integer.parseInt(myLine));
			}
			
			//Close File
			br.close();
		
		} catch (Exception ex) {
			Logger.getInstance().log("WiFiFinger.readFingerFile", ex);
		}
		
		//Store this Fingerprint
		tmp = wFile.getName();
		this.mapFingers.put(Integer.parseInt(tmp.substring(0,tmp.lastIndexOf("."))),wifiInfo);
	}

	/**
	 * This method is not finished yet. It just puts the complete track into the Outputstream.
	 * This is good for handy evaluating...
	 * @return true in any case...
	 */
	@Deprecated
	public boolean testStep() {
		
		//Map of the computed distances - (K,V) = (Distance, NodeID)
		TreeMap<Double,Integer> distances = new TreeMap<Double,Integer>();
		long timeStamp;
		Iterator<Long> timeIterator = this.wiFiScans.keySet().iterator();
		
		WiFiInfo scan;
		WiFiInfo point;
		int pos; double dist; 
		
		
		Iterator<Integer> mapIt = this.mapFingers.keySet().iterator();
		
		Logger.getInstance().log("WiFiFinger.testStep", "Starting distance-computations");
		
		//Iterate historically over the Timestamps
		while (timeIterator.hasNext()) {
			
			timeStamp = timeIterator.next();
			scan = this.wiFiScans.get(timeStamp);
			
			//Iterate over all provided Map Fingerprints
			while(mapIt.hasNext()) {
				pos = mapIt.next();
				point = this.mapFingers.get(pos);
				dist = scan.distanceTo(point);
				distances.put(dist, pos);
			}
			
			//This is mainly for interest now...
			Iterator<Double> dIt = distances.keySet().iterator();

			dist = dIt.next();
			
			Logger.getInstance().log("WiFiFinger.testStep","1-NNSS: Node "+distances.get(dist)+" with distance of "+dist+" for Timestamp "+timeStamp);
			dist = dIt.next();
			Logger.getInstance().log("WiFiFinger.testStep","2-NNSS: Node "+distances.get(dist)+" with distance of "+dist+" for Timestamp "+timeStamp);
			dist = dIt.next();
			Logger.getInstance().log("WiFiFinger.testStep","3-NNSS: Node "+distances.get(dist)+" with distance of "+dist+" for Timestamp "+timeStamp);

			
			//Reset iterator for the map
			mapIt = this.mapFingers.keySet().iterator(); 
			distances.clear();

		}
		
		
		return true;
	}

	@Override
	public LocationEstimationVector getEstimates(long timeStamp1,long timeStamp2) {
		
		Long timeStampTMP = wiFiScans.ceilingKey(timeStamp1);
		//Use later Timestamp due to timeslot for getting data!
		if (timeStampTMP == null) return null;
		timeStampTMP = wiFiScans.higherKey(timeStampTMP);
		if (timeStampTMP == null) return null; 
		if (timeStampTMP > timeStamp2) return null;
		
		LocationEstimationVector LEV = new LocationEstimationVector(Emulator.mapping.size(), timeStampTMP, this);
		
		// Decide algorithm based on method
		switch(this.method) 
		{
			case NNSS:
				LEV = getEstimateNNSS(timeStampTMP, LEV);
				break;
				
			case directkNNSS:
				LEV = getEstimateDirectkNNSS(timeStampTMP, LEV);
				break;
				
			case indirectkNNSS:
				LEV = getEstimateIndirectkNNSS(timeStampTMP, LEV);
				break;
				
			case directkNNSSLin:
				LEV = getEstimateDirectkNNSS(timeStampTMP, LEV);
				LEV = this.ApproximateLin(LEV);
				break;
				
			case directkNNSSSqu:
				LEV = getEstimateDirectkNNSS(timeStampTMP, LEV);
				LEV = this.ApproximateSqu(LEV);
				break;
				
			case indirectkNNSSLin:
				LEV = getEstimateIndirectkNNSS(timeStampTMP, LEV);
				LEV = this.ApproximateLin(LEV);
				break;
				
			case indirectkNNSSSqu:
				LEV = getEstimateIndirectkNNSS(timeStampTMP, LEV);
				LEV = this.ApproximateSqu(LEV);
				break;
		}
		
		int scanSize = wiFiScans.get(timeStampTMP).scanData.size();
		LEV.addParamter("scanSize", scanSize);
		
		return LEV;
	}

	private LocationEstimationVector getEstimateIndirectkNNSS(Long timeStampTMP, LocationEstimationVector LEV) {
		//First, get k NNSS in the normal way

		//Map of the computed distances - (K,V) = (Distance, NodeID)
		TreeMap<Double,Integer> distances = new TreeMap<Double,Integer>();
		
		

		
		WiFiInfo scan;
		WiFiInfo point;
		int pos; double dist; 
		
		
		Iterator<Integer> mapIt = this.mapFingers.keySet().iterator();
		
		//Logger.getInstance().log("WiFiFinger.getEstimatesIndirectNNSS", "Starting distance-computations");
		
			
		scan = this.wiFiScans.get(timeStampTMP);
			
		//Iterate over all provided Map Fingerprints
		while(mapIt.hasNext()) {
			pos = mapIt.next();
			point = this.mapFingers.get(pos);
			dist = scan.distanceTo(point);
			distances.put(dist, pos);
		}
			
		double[] BestDistances = new double[k]; 
				
		BestDistances[0] = distances.firstKey();
		for (int i = 1; i<k; i++) {
			BestDistances[i] = distances.higherKey(BestDistances[i-1]);
		}
		
		//int BestPos = distances.get(BestDist);
		int[] bestPositions = new int[k];
		
		int[] bestIndices = new int[k];
		for (int i=0; i<k; i++) {
			bestPositions[i] = distances.get(BestDistances[i]);
			bestIndices[i] = Emulator.mapping.getKey(new MyEntry<Integer>(bestPositions[i],0,0));
			LEV.setEntry(bestIndices[i], 0.5);
		}
		
		
		//Logger.getInstance().log("WiFiFinger.getEstimateIndirectkNNSS", "Now calculating the mid-point");
		
		//LEV.setEntry(bestIndex, 1);
		double latSum =0;
		double lonSum = 0;
		GPSCoordinate[] gpss = new GPSCoordinate[k];
		for (int i=0; i<k; i++) {
			gpss[i] = graph.getNode(bestPositions[i]).getGPS();
			latSum += gpss[i].getLatitude();
			lonSum += gpss[i].getLongitude();
		}
		
		double midLat = (latSum / (double) k);
		double midLon = (lonSum / (double) k);
		int bestFit =0;
		
		try {
			bestFit = this.graph.getClosestNode(new GPSCoordinate(midLat, midLon)).uid;
		} catch (Exception e) {
			Logger.getInstance().log("WiFiFinger.getEstimateIndirectkNNSS", "Exception when getting the mid-Node: "+e);
			return LEV;
		}
		
		int bestIndex = Emulator.mapping.getKey(new MyEntry<Integer>(bestFit,0,0));
		LEV.setMaxEntry(bestIndex, 1.0);
		
		LEV.setAccuracy(this.getkNNSSAccuracy(distances, new MyEntry<Integer>(bestFit,0,0)));
		
		return LEV;
	}

	private LocationEstimationVector getEstimateDirectkNNSS(Long timeStampTMP, LocationEstimationVector LEV) {

		//Map of the computed distances - (K,V) = (Distance, NodeID)
		TreeMap<Double,Integer> distances = new TreeMap<Double,Integer>();
		WiFiInfo scan;
		WiFiInfo point;
		int pos; double dist; 
		
		
		Iterator<Integer> mapIt = this.mapFingers.keySet().iterator();
		
		//Logger.getInstance().log("WiFiFinger.getEstimatesDirectNNSS", "Starting distance-computations");
		
			
		scan = this.wiFiScans.get(timeStampTMP);
			
		//Iterate over all provided Map Fingerprints
		while(mapIt.hasNext()) {
			pos = mapIt.next();
			point = this.mapFingers.get(pos);
			dist = scan.distanceTo(point);
			distances.put(dist, pos);
		}
			
		double[] BestDistances = new double[k]; 
				
		BestDistances[0] = distances.firstKey();
		for (int i = 1; i<k; i++) {
			BestDistances[i] = distances.higherKey(BestDistances[i-1]);
		}
		
		//int BestPos = distances.get(BestDist);
		int[] bestPositions = new int[k];
		
		int[] bestIndices = new int[k];
		for (int i=0; i<k; i++) {
			bestPositions[i] = distances.get(BestDistances[i]);
			bestIndices[i] = Emulator.mapping.getKey(new MyEntry<Integer>(bestPositions[i],0,0));
			LEV.setEntry(bestIndices[i], 1.0);
		}
		
		LEV.setAccuracy(this.getkNNSSAccuracy(distances, null));
		
		//Logger.getInstance().log("WiFiFinger.getEstimatesNNSS", "BestPos: "+BestPos);
		
		//LEV.setEntry(bestIndex, 1);
		
		return LEV;
	}

	private LocationEstimationVector getEstimateNNSS(Long timeStampTMP,	LocationEstimationVector LEV) {
		

		//Map of the computed distances - (K,V) = (Distance, NodeID)
		TreeMap<Double,Integer> distances = new TreeMap<Double,Integer>();
		WiFiInfo scan;
		WiFiInfo point;
		int pos; double dist; 
		
		
		Iterator<Integer> mapIt = this.mapFingers.keySet().iterator();
		
		//Logger.getInstance().log("WiFiFinger.getEstimatesNNSS", "Starting distance-computations");
		
			
		scan = this.wiFiScans.get(timeStampTMP);
			
		//Iterate over all provided Map Fingerprints
		while(mapIt.hasNext()) {
			pos = mapIt.next();
			point = this.mapFingers.get(pos);
			dist = scan.distanceTo(point);
			distances.put(dist, pos);
		}
			
		double BestDist = distances.firstKey();
		int BestPos = distances.get(BestDist);
		
		
		int bestIndex = Emulator.mapping.getKey(new MyEntry<Integer>(BestPos,0,0));
		
		//Logger.getInstance().log("WiFiFinger.getEstimatesNNSS", "BestPos: "+BestPos);
		
		LEV.setEntry(bestIndex, 1);
		
		
		
		return LEV;
	}

	/**
	 * Computes the Accuracy when using kNNSS
	 * @param distances A Map of sorted distances<->NodeIDs
	 * @param targetPosition in case of indirectkNNSS provide the computed GPS-Coordinate
	 * @return the accuracy of this estimate (in terms of mean cm-distance to a neighbor)
	 */
	private int getkNNSSAccuracy(TreeMap<Double,Integer> distances, MyEntry<Integer> targetPosition) {
		int distance = 0;
		double actKey = distances.firstKey();
		
		if (targetPosition == null) {
			//Concerning directkNNSS
			int[] neighbors = new int[k];
			MapNode[] nodes = new MapNode[k];
			for (int i = 0; i<k; i++) {
				neighbors[i] = distances.get(actKey);
				actKey = distances.higherKey(actKey);
				nodes[i] = graph.getNode(neighbors[i]);
			}
			
			for (int i=0; i<k; i++) {
				for (int j = i+1; j<k; j++) {
					try {
						distance += nodes[i].getGPS().distanceTo(nodes[j].getGPS());
					} catch (InvalidGPSCoordinateException e) {
						// Should never happen
						Logger.getInstance().log("WiFiFinger.getkNNSSAccuracy", "Fail: "+e);
					}
				}
			}
			distance = (distance / ((k*(k-1)) / 2 ));
			
			
		} else {
			//Concerning indirectkNNSS
			
			int[] neighbors = new int[k];
			MapNode[] nodes = new MapNode[k];
			for (int i = 0; i<k; i++) {
				neighbors[i] = distances.get(actKey);
				actKey = distances.higherKey(actKey);
				nodes[i] = graph.getNode(neighbors[i]);
			}
			
			//Get the first (maybe only) Node's Position
			MapNode node = graph.getNode(targetPosition.getValueX());
			
			for (int i=0; i<k; i++) {
				try {
					distance += node.getGPS().distanceTo(nodes[i].getGPS());
				} catch (InvalidGPSCoordinateException e) {
					Logger.getInstance().log("WiFiFinger.getkNNSSAccuracy", "Fail: "+e);
				}
			}
			
			if (targetPosition.getValueY() != 0) {
				//The target is a virtual step
				int distance2 = 0;
				
				node = graph.getNode(targetPosition.getValueY());
				
				for (int i=0; i<k; i++) {
					try {
						distance += node.getGPS().distanceTo(nodes[i].getGPS());
					} catch (InvalidGPSCoordinateException e) {
						Logger.getInstance().log("WiFiFinger.getkNNSSAccuracy", "Fail: "+e);
					}
				}
				
				//Correct the Distance Approximation
				if (distance2 < distance) distance = distance2;
				
			}
			
			//distance = (distance / ((k*(k-1)) / 2 ));
			distance = (distance / (k-1));
		}
		
		//Logger.getInstance().log("WiFiFinger.getkNNSSAccuracy", "Computed Distance: "+distance);
		return distance;
		
	}
}
