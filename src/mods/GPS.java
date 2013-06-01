/**
 * 
 */
package mods;

import graphenbib.GPSCoordinate;
import graphenbib.MapEdge;
import graphenbib.MapGraph;
import graphenbib.MapNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import main.BiMap.MyEntry;
import main.Config;
import main.Emulator;
import main.Logger;

/**
 * @author arno
 *	A GPS-ased module. Tries matching the detected GPS coordinates to the best fitting map Node
 */
public class GPS extends AbstractMod implements Emulateable {

	/**
	 * This Mod will use static sensor Set
	 */
	private final Sensor[] sensors = {Sensor.GPS};
	
	public Sensor[] getSensors() {
		return this.sensors;
	}
	
	public GPS(MapGraph graph) {
		super(graph);
	}

	TreeMap<Long,GPSCoordinate> scanResults = new TreeMap<Long,GPSCoordinate>();
	
	
	@Override
	public boolean init() {
		
		Logger.getInstance().log("GPS.init", "Initiating GPS-Module");
		
		File dataFile = new File(Config.DataDir+File.separator+"gps.csv");
		
		/*
		 * Format:
		 * #TIMESTAMP, #lat, #lon
		 */
		
		String myLine="";
		int pos;
		float lat, lon;
		Long timeStamp;
		GPSCoordinate gps;
		
		try{
			FileReader fStream = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fStream);
			
			
			while(true) {
				//End of File reached?
				if ((myLine = br.readLine()) == null) break;
				
				//Get TimeStamp
				pos = myLine.indexOf(",");
				timeStamp = Long.parseLong(myLine.substring(0, pos));
				lat = Float.parseFloat(myLine.substring(pos+1, myLine.lastIndexOf(",")));
				lon = Float.parseFloat(myLine.substring(myLine.lastIndexOf(",")+1));
				
				//Store it
				gps = new GPSCoordinate(lat,lon);
				this.scanResults.put(timeStamp, gps);
			}
			
			//Close File
			br.close();
		
		} catch (Exception ex) {
			Logger.getInstance().log("GPS.init", "Failed Initiating: "+ex);
			//ex.printStackTrace();
			return false;
		}
		
		Logger.getInstance().log("GPS.init", "Successfully Initiated. Total Entries: "+this.scanResults.size());

		
		return true;
	}
	
	/**
	 * TestMethod so far - partially reused later on
	 */
	public void testStep() {
		Iterator<Entry<Long,GPSCoordinate>> it = this.scanResults.entrySet().iterator();
		Entry<Long,GPSCoordinate> entry;
		//This structure should store all Distances to the estimated virtual Steps
		TreeMap<Integer, MyEntry<Integer>> vSteps = new TreeMap<Integer,MyEntry<Integer>>();
		
		while (it.hasNext()) {
			vSteps.clear();
			entry = it.next();
			try {
				//get first Estimate
				MapNode node = this.graph.getClosestNode(entry.getValue());
				//get refinemant basing on the virtual steps
				MapEdge[] edges = node.getOutgoingEdges();
				Entry<Integer,MyEntry<Integer>> bestPosition = null;
				for (MapEdge edge : edges) {
					if (edge.hasVirtualSteps()) {
						GPSCoordinate[] edgeSteps = edge.getVirtualSteps();
						for (int i = 0; i<edgeSteps.length; i++) {
							vSteps.put(entry.getValue().distanceTo(edgeSteps[i]), new MyEntry<Integer>(edge.getNodeStart().uid,edge.getNodeEnd().uid,i));
						}
						//Now we have all distances -> get the closest one
						bestPosition = vSteps.firstEntry();
					}
				}
				
				
				//Logger.getInstance().log("GPS.testStep", "Nearest node: "+node.uid+" at TimeStamp:" +entry.getKey());
				Logger.getInstance().log("GPS.testStep", "best Position: "+bestPosition.getValue()+" with distance of: "+bestPosition.getKey());
			} catch (Exception e) {
				Logger.getInstance().log("GPS.testStep", "Fail during search for best fitting Node - Graph damaged? "+e);
			}
		}
	}

	@Override
	public LocationEstimationVector getEstimates(long timeStamp1, long timeStamp2) {
		
		//No data for this time
		Entry<Long,GPSCoordinate> entry = scanResults.lowerEntry(timeStamp2);
		if (entry == null) return null;
		
		//Last timestamp is too old
		if (entry.getKey() < timeStamp1) return null;
		
		//TreeMap<Integer, MyEntry<Integer>> vSteps = new TreeMap<Integer, MyEntry<Integer>>();
		HashMap<MyEntry<Integer>,Integer> vSteps = new HashMap<MyEntry<Integer>,Integer>();
		
		Entry<MyEntry<Integer>,Integer> position = null;
		
		MapNode node; int distanceSum = 0;
		try {
			node = this.graph.getClosestNode(entry.getValue());
		
			
			
			//get refinemant basing on the virtual steps
			MapEdge[] edges = node.getOutgoingEdges();
			
			for (MapEdge edge : edges) {
				if (edge.hasVirtualSteps()) {
					GPSCoordinate[] edgeSteps = edge.getVirtualSteps();
					
					for (int i = 0; i<edgeSteps.length; i++) {
						vSteps.put(new MyEntry<Integer>(edge.getNodeStart().uid,edge.getNodeEnd().uid,i),entry.getValue().distanceTo(edgeSteps[i]));
						distanceSum += entry.getValue().distanceTo(edgeSteps[i]);
					}
					//Now we have all distances -> get the closest one
					//position = vSteps.firstEntry();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Strict mode: just return this only one position
		//struture mode: create surrounding estimates...
		
		LocationEstimationVector LEV = new LocationEstimationVector(Emulator.getMatrix().getColumnDimension(),entry.getKey(),this);
		
		
		Iterator<Entry<MyEntry<Integer>,Integer>> it =  vSteps.entrySet().iterator();
		//Entry<MyEntry<Integer>,Integer> mapEntry;
		while (it.hasNext()) {
			//mapEntry = it.next();
			position = it.next();
			LEV.setEntry(Emulator.mapping.getKey(position.getKey()),((double) position.getValue() / (double)distanceSum));
		}
		
		
		return LEV;
	}

}
