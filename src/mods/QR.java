/**
 * 
 */
package mods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.TreeMap;


import main.BiMap.MyEntry;
import main.Config;
import main.Emulator;
import main.Logger;


import graphenbib.MapGraph;

/**
 * @author arno
 *	A QR-Module. In fact the codes have been decrypted and analyzed before. This module only synchronizes at the concerned points in time and space.
 */
public class QR extends AbstractMod implements Synchronizing {

	/**
	 * This Mod will use static sensor Set
	 */
	private final Sensor[] sensors = {Sensor.CAMERA};
	private TreeMap<Long,Integer> refPoints = new TreeMap<Long,Integer>();
	
	public Sensor[] getSensors() {
		return this.sensors;
	}
	
	public QR(MapGraph graph) {
		super(graph);
	}

	/* (non-Javadoc)
	 * @see mods.Emulateable#init()
	 */
	@Override
	public boolean init() {
		Logger.getInstance().log("QR.load", "Initiating Reference Data");
		
		File dataFile = new File(Config.DataDir+File.separator+"qr.csv");
		
		//Is there a QR-data-File?
		if (dataFile.exists() == false) return false;
		
		/*
		 * Format:
		 * #TIMESTAMP, #lat, #lon, #Node
		 * 
		 * 
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
			Logger.getInstance().log("QR.load", "Failed Loading: "+ex);
			//ex.printStackTrace();
			return false;
		}
		
		Logger.getInstance().log("QR.load", "Successfully Loaded. Total Entries: "+this.refPoints.size());
		
		return true;

	}


	@Override
	public LocationEstimationVector getEstimates(long timeStamp1, long timeStamp2) {

		Long timeStampTMP = refPoints.floorKey(timeStamp2);
		if (timeStampTMP == null) return null;
		if (timeStampTMP < timeStamp1) return null;
		
		LocationEstimationVector LEV = new LocationEstimationVector(Emulator.mapping.size(),timeStampTMP,this);
		LEV.setEntry(Emulator.mapping.getKey(new MyEntry<Integer>(refPoints.get(timeStampTMP),0,0)), 1.0);
		return LEV;
	}

}
