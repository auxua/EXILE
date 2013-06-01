/**
 * 
 */
package mods;

import graphenbib.MapGraph;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import main.BiMap.MyEntry;
import main.Config;
import main.Emulator;
import main.Logger;

/**
 * @author arno
 *	A Floor Color module. Can compute mean color of an Image and comparing it to reference colors/points
 */
public class FloorColor extends AbstractMod implements Emulateable {

	/**
	 * This Mod will use static sensor Set
	 */
	private final Sensor[] sensors = {Sensor.CAMERA}; 
	
	private HashMap<Integer,MyEntry<Integer>> refPoints = new HashMap<Integer,MyEntry<Integer>>();
	
	private TreeMap<Long, MyEntry<Integer>> pictures = new TreeMap<Long,MyEntry<Integer>>();
	
	public FloorColor(MapGraph graph, FloorColorMethod m) {
		super(graph);
		this.method = m;
	}
	
	private final FloorColorMethod method; 
	
	public static enum FloorColorMethod{
		STANDARD, PENALTY
	}

	@Override
	public boolean init() {
		//First Check for reference Points 
		if (false == loadRef()) {
			return false;
		}
		
		//Now lets check for "real" pics
		if (false == loadPics()) {
			return false;
		}
		
		return true;
	}
	
	private boolean loadPics() {
		Logger.getInstance().log("FloorColor.loadPics", "Started to load Data");
		
		//-----------------
		// Read the pictures
		//-----------------
		
		File fEnv = new File (Config.DataDir+File.separator+"Pictures"+File.separatorChar);
		//No Data for this run available - abort
		if (fEnv.exists() == false) return false;
		
		File[] listOfFiles = fEnv.listFiles();
		MyEntry<Integer> entry;
		String name;
		Long timeStamp  = null;
		
		
		//Read every available file in the dir
		for (File wFile : listOfFiles) {
		    if (wFile.isFile()) {
		        //readFingerFile(wFile);
		    	name = wFile.getName();
		    	name = name.substring(0, name.indexOf("."));
		    	try {
					timeStamp = Long.parseLong(name);
				} catch (NumberFormatException e) {
					timeStamp = null;
				}
		    	//Did this work?
		    	if (timeStamp == null) continue;
		    	//Compute the mean color
		    	entry = this.getMeanColorFromFile(wFile);
		    	if (entry == null) continue;
		    	//Lets store these data
		    	this.pictures.put(timeStamp, entry);
		    	
		    }
		}
		
		Logger.getInstance().log("FloorColor.loadPics", "Read all Pictures and successfully computed their mean Colors. Pictures read: "+this.pictures.size());
		//No Pictures? -> No need for me
		if (this.pictures.size() == 0) return false;
		
		return true;
	}

	private boolean loadRef() {
		Logger.getInstance().log("FloorColor.loadRef", "Initiating Reference Data");
		
		File dataFile = new File("environmental Data"+File.separatorChar+Config.mapName+File.separatorChar+"FloorColor.csv");
		
		/*
		 * Format:
		 * #NODEID, #rrr, #ggg, #bbb
		 * 
		 * All rgb-Values must have exactly 3 digits
		 */
		
		String myLine="";
		int stringPos; int nodeID = 1;		
		int red, green, blue;

		
		try{
			FileReader fStream = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fStream);
			
			
			while(true) {
				//End of File reached?
				if ((myLine = br.readLine()) == null) break;
				
				//Get NodeID and RGB data
				stringPos = myLine.indexOf(",");
				
				nodeID = Integer.parseInt(myLine.substring(0,stringPos));
				
				red = Integer.parseInt(myLine.substring(stringPos+2,stringPos+5));
				green = Integer.parseInt(myLine.substring(stringPos+7,stringPos+10));
				blue = Integer.parseInt(myLine.substring(stringPos+12,stringPos+15));
				
				
				//Store it
				this.refPoints.put(nodeID, new MyEntry<Integer>(red,green,blue));
				
			}
			
			//Close File
			br.close();
		
		} catch (Exception ex) {
			Logger.getInstance().log("FloorColor.loadRef", "Failed Loading: "+ex);
			//ex.printStackTrace();
			return false;
		}
		
		Logger.getInstance().log("FloorColor.loadRef", "Successfully Loaded. Total Entries: "+this.refPoints.size());
		
		return true;
	}

	/**
	 * Computes the mean color of the Image. Area Averaging/JPEG Optimizations of DC-Blocks make this a simple approach. 
	 * @param file The Image to be worked with
	 */
	public MyEntry<Integer> getMeanColorFromFile(File file) {
		try {
			BufferedImage bImage = ImageIO.read(file);
			 
            //Image scaledImage = bImage.getScaledInstance(1, 1,Image.SCALE_AREA_AVERAGING);
			Image scaledImage = bImage.getScaledInstance(1, 1,Image.SCALE_FAST);
 
            bImage = new BufferedImage(1, 1,BufferedImage.TYPE_INT_RGB);
            Graphics g = bImage.getGraphics();
            g.drawImage(scaledImage, 0, 0, null);
            g.dispose();
            
			int color = bImage.getRGB(0, 0);
			//int alpha = (color >> 24) & 0xFF;
			int red =   (color >> 16) & 0xFF;
			int green = (color >>  8) & 0xFF;
			int blue =  (color      ) & 0xFF;
			
			//Logger.getInstance().log("FloorColor.getMeanColorFromFile","alpha: "+alpha+" red: "+red+" green: "+green+" blue: "+blue);
			
			return new MyEntry<Integer>(red,green,blue);
		} catch (IOException e) {
			Logger.getInstance().log("FloorColor.getMeanColorFromFile", "ail during computations: "+e);
		}
		
		//Should never come here
		return null;
	}

	@Override
	public LocationEstimationVector getEstimates(long timeStamp1, long timeStamp2) {
		//Get the best fitting timeStamp
		Long timeStampTMP = this.pictures.ceilingKey(timeStamp1);
		if (timeStampTMP == null) return null; 
		Long tbc = this.pictures.higherKey(timeStampTMP);
		if ((tbc != null) && (tbc < timeStamp2)) timeStampTMP = tbc;
		
		double distance;
		double maxDist = 0;
		double minDist = Double.MAX_VALUE;
		//int bestNode = 0;
		//compute all distances to the reference points
		TreeMap<Integer, Double> distances = new TreeMap<Integer, Double>();
		for (Integer node : refPoints.keySet()) {
			distance = this.distanceOf(this.pictures.get(timeStampTMP),this.refPoints.get(node));
			if (maxDist < distance) maxDist = distance;
			if (minDist > distance) {
				minDist = distance;
				//bestNode = node;
			}
			distances.put(node, distance);
		}
		
		LocationEstimationVector LEV = new LocationEstimationVector(Emulator.mapping.size(),timeStampTMP,this);
		Integer pos = 0;
		if (this.method == FloorColorMethod.STANDARD) {
			for (Integer node : distances.keySet()) {
				//Store the data in comparison to the min Distance
				pos = Emulator.mapping.getKey(new MyEntry<Integer>(node,0,0));
				LEV.setEntry(pos, (minDist / distances.get(node)));
			}
		} else {
			for (Integer node : distances.keySet()) {
				//Store the data in comparison to the max Distance
				pos = Emulator.mapping.getKey(new MyEntry<Integer>(node,0,0));
				LEV.setEntry(pos, (distances.get(node) / maxDist));
				//Now make penalties out of these
				LEV.multiply(-1);
			}
		}
		 
		//By deault use linear Approximation for FloorColor

		LEV = this.ApproximateLin(LEV);

		
		//Now look for the best color to fit for later checking of Quality - How often is this color detected in the environment?
		int count = 0;
		for (Integer node : distances.keySet()) {
			if (distances.get(node) == minDist) count++;
		}
		
		//Store this information
		Double d = ((double) count / (double) refPoints.size() );
		LEV.addParamter("ColorFraction", d);
		return LEV;
	}

	/**
	 * Computes the euclidiean distance between two images (in fact the distance of the 3-dimensional RGB-Domain)
	 * @param myEntry One RGB-representation of an image
	 * @param myEntry2 Another RGB-representation of an image
	 * @return the euclidean distance between those representationt in the RGB Space
	 */
	private Double distanceOf(MyEntry<Integer> myEntry,	MyEntry<Integer> myEntry2) {
		
		double distance = 0;
		
		distance = ( (myEntry.getValueX()-myEntry2.getValueX())*(myEntry.getValueX()-myEntry2.getValueX()) );
		distance += ( (myEntry.getValueY()-myEntry2.getValueY())*(myEntry.getValueY()-myEntry2.getValueY()) );
		distance += ( (myEntry.getValueZ()-myEntry2.getValueZ())*(myEntry.getValueZ()-myEntry2.getValueZ()) );
		
		distance = Math.sqrt(distance);

		return distance;
	}

	@Override
	public Sensor[] getSensors() {
		return this.sensors;
	}

}
