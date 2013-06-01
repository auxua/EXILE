/**
 * 
 */
package main;

import graphenbib.MapEdge;
import graphenbib.MapGraph;
import graphenbib.MapNode;

import importer.OSMReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import main.BiMap.MyEntry;
import mods.Emulateable;
import mods.FloorColor;
import mods.StepDetection;
import mods.GPS;
import mods.LocationEstimationVector;
import mods.PosReference;
import mods.QR;
import mods.WiFiFinger;

/**
 * @author arno
 *	This is the Emulator. It automatically gets all data on it's own from other classes and Objects. Logs what is doing.
 */
public class Emulator {

	public static Long[] timeStamps;
	
	public static Matrix matrix;
	
	//private HashMap<String,Emulateable> mods;
	private static ArrayList<Emulateable> mods;
	
	public static MapGraph graph;
	
	
	/**
	 * This Map is structured as follows:
	 * 
	 * (K,(V,V',V''))
	 * 
	 * we can easily Map  K <-> (V,V',V'')
	 * 
	 * Therefore it should store:
	 * 
	 * Index of matrix-vector (column) <-> (NodeID,NodeTarget,progress)
	 * 
	 * where NodeTarget,progress == 0,0 for "real" nodes
	 *  
	 */
	public static BiMap<Integer,MyEntry<Integer>> mapping;

	private static WeightingFunction weightingFunction;
	
	
	
	
	
	/**
	 * This is the basic Emulator class.
	 * This class controls and starts all needed modules and also stores the Transitionmatrix
	 * @param args Not needed
	 */
	public static void main(String[] args) {

		Logger.getInstance().log("Emulator.main", "Starting now");
		Logger.getInstance().log("Emulator.main", "Setting up Matrix");
		
		matrix = new Matrix();
		try {
			matrix.setEntry(0, 0, 1.0);
		} catch (Exception e) {
			Logger.getInstance().log("Emulator.main", "Fail during creation of matrix and filling: "+e);
		}
		
		Logger.getInstance().log("Emulator.main", "Importing map Data (graph)");
		
		try {
			graph = OSMReader.readOSMFile(new File("testFiles"+File.separatorChar+Config.mapName));
		} catch (Exception e) {
			Logger.getInstance().log("Emulator.main", "Unable to Import map/graph. Exception: "+e);
		}
		
		Logger.getInstance().log("Emulator.main", "Creating Virtual Steps in the graph");
		
		try {
			graph.createVirtualSteps(Config.initialStepLength);
		} catch (Exception e) {
			Logger.getInstance().log("Emulator.main", "Fail during creation of virtual Steps: "+e);
		}
		
		Logger.getInstance().log("Emulator.main", "Registering modules");
		
		mods = new ArrayList<Emulateable>();
		
		ArrayList<Emulateable> premods = new ArrayList<Emulateable>();
		
		
		Emulateable em;
		StepDetection fp;
		
		
		WiFiFinger wem = new WiFiFinger(graph,Config.WIFIK,Config.WiFiMethod);
		if (Config.useWiFi) {
			premods.add(wem);
		}
		
		em = new FloorColor(graph,Config.FColorMethod);
				
		if (Config.useFloorColor) {
			premods.add(em);		
		}
		
		fp = new StepDetection(graph);
		fp.setStartNode(Config.startNode);
		premods.add(fp);

		GPS gps = new GPS(graph);
		if (Config.useGPS) {
			premods.add(gps);
		}
		

		QR qr = new QR(graph);
		if (Config.useQR) {
			premods.add(qr);
		}
		
		graph.deleteNode(-100);
		graph.deleteNode(-84);
		
		//More mods
		
		Logger.getInstance().log("Emulator.main", "Initiating Mods");
		
		//Init all Modules but only keep succ. initiated mods
		for (Emulateable mod : premods) {
			if (mod.init())
				mods.add(mod);
		}
		
		premods.clear(); premods = null;
		
		Logger.getInstance().log("Emulator.main", "Create BiMap");
		
		// Create the BiMap for matching Node IDs to matrices column positions and vice versa.
		
		createMap();
		
		
		Logger.getInstance().log("Emulator.main", "Init Matrix Dimensions");
		//Just for debugging
		boolean mappingAll = false;
		
		try {
			//for (int i=0; i<mapping.size(); i++)
			for (int i=0; i<mapping.size(); i++)
				matrix.setEntry(i, 0, 0);
			
			mappingAll = true;
			matrix.setEntry(mapping.getKey(new MyEntry<Integer>(Config.startNode,0,0)), 0, 1.0);
		} catch (Exception e) {
			Logger.getInstance().log("Emulator.main", "Init Matrix Dimensions failed ("+mappingAll+") : "+e);
		}
		
		Logger.getInstance().log("Emulator.main", "Getting TimeStamps for FP-Reference");
		
		timeStamps = fp.getTimeStamps();
		
		Logger.getInstance().log("Emulator.main", "Creating Weightening-Function");
		
		weightingFunction = new WeightingFunction(mapping.size(),matrix);
		
		Logger.getInstance().log("Emulator.main", "Getting LEVs");
		
		
		LocationEstimationVector LEV = null;
		ArrayList<LocationEstimationVector> LEVs = new ArrayList<LocationEstimationVector>();
		
		
		for (int j = 0; j < timeStamps.length-1; j++) {
			LEV = null;
			LEVs.clear();
			for (int i = 0; i < mods.size(); i++) {
				LEV = mods.get(i).getEstimates(timeStamps[j], timeStamps[j+1]);
				if (LEV == null)
					continue;
				LEV.setProvider(mods.get(i));
				LEVs.add(LEV);
			}
			
			LEV = weightingFunction.getWeightedLEV((LEVs),j+1);
			insertLEV(j+1,LEV);
		}
		
		Logger.getInstance().log("Emulator.main", "Completed the transition matrix");
		
		
		try {
			if (Config.printRoute) {
				Logger.getInstance().log("Emulator.main", "Printing Route");
				printRoute();
			}
			if (Config.printReference) {
				Logger.getInstance().log("Emulator.main", "Printing Position and Reference (incl. distance)");
				printRef();
			}
			if (Config.printMatrix) {
				Logger.getInstance().log("Emulator.main", "Printing Matrix:");
				Logger.getInstance().log("Emulator.main", matrix);
			}
			if (Config.LogMatrix) {
				Logger.getInstance().log("Emulator.main", "Storing Matrix");
				Output back = Logger.getOutput();
				Logger.setOutput(Output.FILE);
				Logger.getInstance().log("Emulator.main", matrix);
				Logger.setOutput(back);
			}			
		} catch (Exception e) {
			Logger.getInstance().log("Emulator.main", "Exception during Printing/Storing: "+e);
		}
		
		
		
		

	}
	
	@SuppressWarnings("unused")
	private static LocationEstimationVector columnToLEV(HashMap<Integer,Double> column) {
		LocationEstimationVector LEV = new LocationEstimationVector(column.size());
		
		for (int i=0; i<column.size(); i++) {
			LEV.setEntry(i, column.get(i));
		}
		
		
		return LEV;
	}
	
	//private static HashMap<Integer,Double> LEVToColumn(LocationEstimationVector LEV) {
	private static TreeMap<Integer,Double> LEVToColumn(LocationEstimationVector LEV) {
		TreeMap<Integer, Double> table = new TreeMap<Integer,Double>();
		
		for (int i=0; i<matrix.getColumnDimension(); i++) {
			table.put(i, LEV.getEntry(i));
		}
		
		return table;
	}
	
	private static void insertLEV(int column, LocationEstimationVector LEV) {
		//HashMap<Integer,Double> table = LEVToColumn(LEV);
		TreeMap<Integer,Double> table = LEVToColumn(LEV);
		matrix.setColumn(column, table);
	}
	
	
	private static void createMap() {
		mapping = new BiMap<Integer,MyEntry<Integer>>();
		
		Iterator<MapNode> it = graph.getNodeIt();
		MapNode node;
		//This is the index which will denote the columnuindex based on the Iterators
		int index = 0;
		//Set to store all edges temporarily
		Set<MapEdge> edges = new HashSet<MapEdge>();
		
		//just visit all nodes, store them and collect all edges
		while (it.hasNext()) {
			node = it.next();
			//Store this node
			mapping.put(index++, new MyEntry<Integer>(node.uid,0,0));
			for (MapEdge edge : node.getOutgoingEdges())
				edges.add(edge);
		}
		
		Logger.getInstance().log("Emulator.createMap", "Stored all Nodes");
		
		//Now store the edges' virtual steps
		
		for (MapEdge edge : edges) {
			for (int i=0; i<edge.getStepCount(); i++) {
				mapping.put(index++, new MyEntry<Integer>(edge.getNodeStart().uid,edge.getNodeEnd().uid,i));
			}
		}
		
		Logger.getInstance().log("Emulator.createMap", "Stored all Virtual Steps");
		
		
	}


	protected static void insertLEV(LocationEstimationVector LEV, int columnIndex) {
		try {
			for (int i = 0; i<LEV.size();  i++) {
				matrix.setEntry(i, columnIndex, LEV.getEntry(i));
			}
		} catch (Exception e) {
			Logger.getInstance().log("Emulator.insertLEV", "Fail while inserting a LEV into the Matrix: "+e);
		}
	}
	
	/**
	 * Allows to request the matrix Object (e.g. Mods needing History or sth.)
	 * @return the matrix Object - outside this package only read access rights are given!
	 */
	public static Matrix getMatrix() {
		return matrix;
	}
	
	private static void printRoute() throws Exception {
		int key;
		
		PosReference pos = new PosReference(graph);
		pos.load();
		
		
		for (int i=0; i<matrix.getColNum(); i++) {
			key = matrix.getColumnMaxPosition(i);
			System.out.println(Emulator.mapping.get(key)+" | "+pos.getEstRef(timeStamps[i])+" after TS:"+timeStamps[i]);
		}
	}
	
	private static int getDistance(MyEntry<Integer> startPos, MyEntry<Integer> endPos) {
		
		//Marked Nodes
		HashSet<MapNode> marked = new HashSet<MapNode>();
		//Mapping UID to distance 
		HashMap<Integer,Integer> distances = new HashMap<Integer,Integer>();
		//The Queue for Breadth-First Search
		LinkedList<MapNode> queue = new LinkedList<MapNode>();
		
		//Store initial Information
		if (startPos.getValueY() != 0) {
			// It is a virtual Step
			//First store starting of the edge
			queue.add(graph.getNode(startPos.getValueX()));
			distances.put(startPos.getValueX(), startPos.getValueZ()+1);
			//Now store the target of the edge
			queue.add(graph.getNode(startPos.getValueY()));
			int maxSteps = graph.getNode(startPos.getValueX()).getEdgeTo(startPos.getValueY()).getStepCount();
			distances.put(startPos.getValueY(), maxSteps-startPos.getValueZ());
		} else {
			distances.put(startPos.getValueX(), 0);
			queue.add(graph.getNode(startPos.getValueX()));
		}
		
		MapNode node;
		MapEdge edge;
		TreeSet<MapNode> neighbors;
		int tmpDistance = 0;
		
		//Now search everything!
		while (queue.isEmpty() == false) {
			node = queue.poll();
			marked.add(node);
			neighbors = node.getNeighbours();
			for (MapNode neighbor : neighbors) {
				if (marked.contains(neighbor)) continue;
				queue.add(neighbor);
				edge = node.getEdgeTo(neighbor.uid);
				tmpDistance = edge.getStepCount()+1+distances.get(node.uid);
				if ((distances.get(neighbor.uid) == null) || (distances.get(neighbor.uid)>tmpDistance))
					distances.put(neighbor.uid, tmpDistance);
			}
		}
		
		//Now we have a full tree.
		int forward;
		forward = distances.get(endPos.getValueX()) + endPos.getValueZ();
		//When we are targetting a node itself, we can use this
		if (endPos.getValueY() == 0) return forward;
		
		//Fail on it...
		return 0;
	}

	private static void printRef() throws Exception {
		int key;
		int target;
		int dist = 0;
		
		PosReference pos = new PosReference(graph);
		pos.load();
		
		System.out.println("----------------Detected Route---------------");
		
		for (int i=0; i<matrix.getColNum(); i++) {
			key = matrix.getColumnMaxPosition(i);
			target = pos.getStrictRef(timeStamps[i]);
			if (target != 0) {
				dist = getDistance(Emulator.mapping.get(key),new MyEntry<Integer>(target,0,0));
				System.out.println("Position: "+Emulator.mapping.get(key)+" , Target: "+target+" , Diff: "+dist);
			}
			
		}
	}
	
}
