/**
 * 
 */
package graphenbib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import main.Config;
import main.Logger;

/**
 * @author arno
 *	This is the representation of a graph. It contains MapNodes and MapEdges. The graph is stored as Map of nodes (using their unique IDs as keys).
 *	This class provides some functions that are recursively called in the whole structure - may be slow on huge graphs!
 */
public class MapGraph {

	private HashMap<Integer,MapNode> nodes;
	//private GPSCoordinate upperLeft;
	//private GPSCoordinate lowerRight;
	private GPSRectangle MapRectangle;
	private String Filename;
	
	//private int debugCounter =0;

	public String getFilename() {
		return Filename;
	}

	public void setFilename(String filename) {
		Filename = filename;
	}


	/**
	 * Constructor using two GPSCoordinates
	 * @param upperLeft The upper left Corner
	 * @param lowerRight The lower right corner
	 */
	public MapGraph(GPSCoordinate upperLeft, GPSCoordinate lowerRight) throws EmptyInputException, InvalidInputException, InvalidGPSCoordinateException
    {
	    GPSRectangle MapRect = new GPSRectangle(upperLeft,lowerRight);
	    //new MapGraph(MapRect);
		this.MapRectangle = MapRect;
		nodes=new HashMap<Integer,MapNode>();
	    
    }
	

	/**
	 * 
	 * Constructor using a predefined Rectangle 
	 */
	public MapGraph(GPSRectangle MapRectangle) throws EmptyInputException
	{
		if (MapRectangle == null) {
			throw new EmptyInputException("NullPointer in Constructor detected");
		}
		
		this.MapRectangle = MapRectangle;
		nodes=new HashMap<Integer,MapNode>();
	}
	
	/**
	 * Constructor for using closckwise GPS values 
	 */
	public MapGraph(double top, double right, double bottom, double left) throws EmptyInputException, InvalidInputException, InvalidGPSCoordinateException
	{
		GPSRectangle MapRect = new GPSRectangle(top,right,bottom,left);
		this.MapRectangle = MapRect;
		nodes = new HashMap<Integer,MapNode>();
	}
	
	/**
	 * Gets the size of the Graph in terms of number of Nodes
	 * @return Number of Nodes
	 */
	public int getSize() {
		return nodes.size();
	}
	
	/**
	 * Gets an Iterator over all Nodes
	 * @return Iterator over all MapNodes
	 */
	public Iterator<MapNode> getNodeIt(){
		return nodes.values().iterator();
	}
	
	private int outOfGPSNodes = 0;
	
	public void insertNode(int uid, GPSCoordinate gps) throws EmptyInputException, InvalidInputException 
	{
		if (MapRectangle.GPSInside(gps))
		{
			MapNode test = nodes.get(uid);
			if (test != null)
				throw new InvalidInputException("insertNode in MapGraph: Duplicate detected. UID: "+uid);
			MapNode a= new MapNode(uid,gps);
			nodes.put(a.getUID(),a);
		}
		else
		{
			Logger.getInstance().log("MapGraph.inserNode", "Found Out-Of-Bounds Node Nummer"+ (++outOfGPSNodes));
			
			MapNode test = nodes.get(uid);
			if (test != null)
				throw new InvalidInputException("insertNode in MapGraph: detected Duplicate UID: "+uid);
			MapNode a= new MapNode(uid,gps);
			nodes.put(a.getUID(),a);
		}
	}
	
	/**
	 * Allows to input a Node with (temp) no GPS information
	 * @param uid UID of the Node to Input
	 */
	
	public void insertNodeWithoutGPS(int uid) {
		MapNode a=new MapNode(uid);
		nodes.put(a.getUID(), a);
	}
	
	public void insertOneWay(int startNodeUID, int endNodeUID, int wayID, int length, StreetType streetType, String name) 
			throws InvalidInputException, NodeNotInGraphException {
		this.insertEdge(startNodeUID, endNodeUID, wayID, length, streetType,name,true);
	}
	
	public void insertEdgeBothDirections(int startNodeUID, int endNodeUID, int wayID, int length, StreetType streetType, String name) 
			throws InvalidInputException, NodeNotInGraphException {
		this.insertEdge(startNodeUID, endNodeUID, wayID, length, streetType,name,false);
	}
	
	//@Deprecated
	public void insertEdge(int startNodeUID, int endNodeUID, int wayID, int length, StreetType streetType) throws InvalidInputException, NodeNotInGraphException {
		this.insertEdge(startNodeUID, endNodeUID, wayID, length, streetType,"", true);
	}
	
	//@Deprecated
	public void insertEdge(int startNodeUID, int endNodeUID, int wayID, int length, StreetType streetType, String name) 
			throws InvalidInputException, NodeNotInGraphException {
		this.insertEdge(startNodeUID, endNodeUID, wayID, length, streetType, name,true);
	}
			
	private void insertEdge(int startNodeUID, int endNodeUID, int wayID, int length, StreetType streetType, String name, boolean oneWay) 
					throws InvalidInputException, NodeNotInGraphException
	{
		if(name==null)
			name="";
		if (length < 0)
			throw new InvalidInputException("insertEdge in MapGraph: not-positive edge weight - wayID: "+wayID);
		MapNode start=getNode(startNodeUID);
		MapNode end=getNode(endNodeUID);
		
		if(start==null || end==null) 
			throw new NodeNotInGraphException("insertEdge in MapGraph:"+
					"At least One of the Nodes are not in the graph. \n" +
					"startNodeUID: "+startNodeUID+", endNodeUID "+endNodeUID+", wayID"+wayID);
		if(oneWay) {
			MapEdge e=new MapEdge(start,end,wayID,length,streetType,name,true,oneWay);
			start.addOutgoingEdge(e);
			end.addIncomingEdge(e);
		} else {
			MapEdge e1=new MapEdge(start,end,wayID,length,streetType,name,true,false);
			//MapEdge e2=new MapEdge(end,start,wayID,length,streetType,name,false,false);
			start.addOutgoingEdge(e1);
			end.addIncomingEdge(e1);
			end.addOutgoingEdge(e1);
			start.addIncomingEdge(e1);
		}

	}
	
	/**
	 * Search the Node being next to a GPS Coordinate (Euclidean Distance)
	 * @param gps the GPS Coordinate to be searched
	 * @return UID of the nearest Node
	 * @throws Exception if null pointer was detected
	 */
	public MapNode getClosestNode(GPSCoordinate gps) throws Exception {
		if(gps==null) {
			throw new InvalidGPSCoordinateException("Null Pointer detected");
		}
		Iterator<MapNode> nodeIt=this.getNodeIt();
		int minDistance=Integer.MAX_VALUE;
		MapNode closestNode =null;
		while (nodeIt.hasNext()) {
			MapNode currentNode=nodeIt.next();
			if(currentNode.hasGPS()) {
				if(gps.distanceTo(currentNode.getGPS())<minDistance) {
					minDistance=gps.distanceTo(currentNode.getGPS());
					closestNode=currentNode;
				}
			}
		}
		if(closestNode==null && minDistance==Integer.MAX_VALUE) {
			throw new Exception("MapGraph has no GPS Coordinates");
		}
		return closestNode;
	}
	/**
	 * Delete all isolated Nodes in the graph (We do not need them, do we?)
	 */
	public void deleteIsolatedNodes() {
		ArrayList<Integer> lliste = new ArrayList<Integer>(); 
		Iterator<MapNode> iterator = this.getNodeIt();
		MapNode currentNode;
		while(iterator.hasNext()) {
			currentNode = iterator.next();
			if((currentNode.getNumberOfIncomingEdges()==0) && (currentNode.getNumberOfOutgoingEdges()==0)) {
				lliste.add(currentNode.getUID());
			}
		}	
		//delete:
		for (int i=0; i<lliste.size(); i++) {
			nodes.remove(lliste.get(i));
		}
	}
	
		
	/**
	 * Gets a Node specified by it's UID
	 * @param uid of the requested node
	 * @return the MapNode or null, if the Node was not found
	 */
	public MapNode getNode(int uid)
	{
		return nodes.get(uid);
	}
	
	/**
	 * @return The Rectangle denoting the Area of the Graph
	 */
	public GPSRectangle getRect()
	{
		return this.MapRectangle;
	}

	public GPSCoordinate getUpperLeft()
	{
		return MapRectangle.getUpperLeft();
	}
	
	public GPSCoordinate getLowerRight()
	{
		return MapRectangle.getLowerRight();
	}
	
	/**
	 * If the Import did not compute all length correctly, do it now!
	 */
	public void correctLength() {
		Iterator<MapNode> nodeIt=this.getNodeIt();
		while(nodeIt.hasNext()) {
			MapEdge outEdges[]=nodeIt.next().getOutgoingEdges();
			for(MapEdge e:outEdges) {
				if(e.getLength()==Config.initialTemporaryLengthForEdges) {
					if(e.getNodeStart().hasGPS() && e.getNodeEnd().hasGPS()) {
						try {
							e.setLength(e.getNodeStart().getGPS().distanceTo(e.getNodeEnd().getGPS()));
						} catch (InvalidGPSCoordinateException e1) {
							//Should never happen
							e1.printStackTrace();
						}
					} else {
						Logger.getInstance().log("MapGraph.correctLength", "Warning: invalid data:" +e);
					}
				}
			}
		}
	}
	
	public String toString() {
		String tempString="";
		Iterator<MapNode> iterator = this.getNodeIt();
		while(iterator.hasNext()) {
			tempString +=iterator.next().toString();
		}
		return tempString;
	}
	
	/**
	 * Creates the virtual Nodes for given Stepsize in the whole Graph
	 * @param steplength in cm.
	 * @throws InvalidGPSCoordinateException 
	 */
	public void createVirtualSteps(int steplength) throws InvalidGPSCoordinateException {
		Iterator<MapNode> it = this.getNodeIt();
		MapNode node;
		MapEdge[] edges;
		
		//Iterate all Edges to create all virtual Nodes/Steps inside 
		while (it.hasNext()) {
			node = it.next();
			edges = node.getOutgoingEdges();
			for (MapEdge edge : edges) {
				//if (false == edge.hasVirtualSteps()) {
					edge.createVirtualSteps(steplength);
				//}
			}
		}
		
	}
	
	/**
	 * Return only the Amount of all virtual Nodes o the Graph
	 * @return amount of virtual nodes
	 */
	public int getVirtualNodesCount() {
		Iterator<MapNode> it = this.getNodeIt();
		MapNode node;
		MapEdge[] edges;
		int count = 0;
		
		//Iterate all Edges to create all virtual Nodes/Steps inside 
		while (it.hasNext()) {
			node = it.next();
			edges = node.getOutgoingEdges();
			for (MapEdge edge : edges) {
				//if (false == edge.hasVirtualSteps()) {
					count += edge.getStepCount();
				//}
			}
		}
		return count;
	}
	
	/**
	 * Gets all Nodes - the real nodes an the virtual Nodes
	 * @return all Nodes in the Graph
	 */
	public int getFullSize() {
		return (this.getSize() + this.getVirtualNodesCount());
	}
	
	public void deleteNode(int uid) {
		MapNode node = this.getNode(uid);
		if (node == null) return;
		node.remove();
		
	}
}
