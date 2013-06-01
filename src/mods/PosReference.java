/**
 * 
 */
package mods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.TreeMap;

import main.Config;
import main.Logger;

import graphenbib.MapGraph;

/**
 * @author arno
 *
 *	This class stores the reference data of a walk for comparison reasons.
 */
public class PosReference extends AbstractMod {

	public PosReference(MapGraph graph) {
		super(graph);
	}
	
	/**
	 * Map to store the reference Data 
	 * (K,V) = (Timestamp, NodeID)
	 */
	private TreeMap<Long,Integer> refPoints = new TreeMap<Long,Integer>();
	
	/**
	 * Loads the Reference Data File
	 * @return true if data was successfully loaded
	 */
	public boolean load() {
		Logger.getInstance().log("Reference.load", "Initiating Reference Data");
		
		File dataFile = new File(Config.DataDir+File.separator+"pos.csv");
		
		/*
		 * Format:
		 * #TIMESTAMP, #lat, #lon, #Node
		 * 
		 * Because of walking to a position and then clicking this data is only reliable when
		 * changing position (=Clicking at Position)
		 * Therefore only store first entry of same-position-row
		 */
		
		String myLine="";
		int stringPos; int nodeID = 1; int newNode;
		//float lat, lon;
		Long timeStamp;
		//GPSCoordinate gps;
		
		try{
			FileReader fStream = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fStream);
			
			
			while(true) {
				//End of File reached?
				if ((myLine = br.readLine()) == null) break;
				
				//Get TimeStamp and NodeID
				stringPos = myLine.indexOf(",");
				timeStamp = Long.parseLong(myLine.substring(0, stringPos));
				newNode = Integer.parseInt(myLine.substring(myLine.lastIndexOf(",")+2));
				
				//Store it, if new NodeID
				if (newNode != nodeID) {
					this.refPoints.put(timeStamp, newNode);
					nodeID = newNode;
				}
			}
			
			//Close File
			br.close();
		
		} catch (Exception ex) {
			Logger.getInstance().log("Reference.load", "Failed Loading: "+ex);
			//ex.printStackTrace();
			return false;
		}
		
		Logger.getInstance().log("Reference.load", "Successfully Loaded. Total Entries: "+this.refPoints.size());
		
		return true;
	}
	
	public Integer getStrictRef(long timeStamp) {
		if (refPoints.get(timeStamp) != null) return refPoints.get(timeStamp);
		return 0;
	}
	
	public String getEstRef(long timeStamp) {
		Long before = refPoints.floorKey(timeStamp);
		Long after = refPoints.ceilingKey(timeStamp);
		if (after == null || before == null) return "";
		return ("Between "+refPoints.get(before)+" and "+refPoints.get(after));
	}

}
