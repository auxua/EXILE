/**
 * 
 */
package graphenbib;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * @author arno
 *	This is a MapNode having a unique ID. A Node stores it's incoming and outgoing edges as well as it's coordinates.
 *	
 */
public class MapNode implements Comparable<MapNode> {

	private MapEdge[] incomingEdges = new MapEdge[0];
	private MapEdge[] outgoingEdges = new MapEdge[0];
	
	private GPSCoordinate GPS;
	
	public final int uid;
	
	private boolean indoor;
	
	/**
	 * @return the indoor
	 */
	public boolean isIndoor() {
		return indoor;
	}

	/**
	 * @param indoor the indoor to set
	 */
	public void setIndoor(boolean indoor) {
		this.indoor = indoor;
	}

	/**
	 * @return the incomingEdges
	 */
	public final MapEdge[] getIncomingEdges() {
		return incomingEdges;
	}

	/**
	 * @return the outgoingEdges
	 */
	public final MapEdge[] getOutgoingEdges() {
		return outgoingEdges;
	}
	
	public final MapEdge getEdgeTo(int nodeID) {
		for (MapEdge e : outgoingEdges)
			if (e.getNodeEnd().uid == nodeID) return e;
		//Nothing found - maybe other direction?
		for (MapEdge e : outgoingEdges)
			if (e.getNodeStart().uid == nodeID) return e;
		return null;
	}
	
	public final GPSCoordinate getGPS() {
		return this.GPS;
	}
	
	public void setGPS(GPSCoordinate GPS) {
		this.GPS = GPS;
	}

	public MapNode(int uid, GPSCoordinate gps) {
		this.uid = uid;
		this.GPS = gps;
		
		//this.incomingEdges[0] = null;
		//this.outgoingEdges[0] = null;
	}
	
	protected MapNode(int uid)
	{
		this.uid = uid;
	}
	
	
	public TreeSet<MapNode> getNeighbours() {
		MapEdge outgoingEdges[]=this.getOutgoingEdges();
		TreeSet<MapNode> neighbours = new TreeSet<MapNode>();
		for(int i=0; i<outgoingEdges.length; i++) {
			//neighbours.add(outgoingEdges[i].getNodeEnd());
			//Check for the storing direction
			if (outgoingEdges[i].getNodeEnd() == this)
				neighbours.add(outgoingEdges[i].getNodeStart());
			else
				neighbours.add(outgoingEdges[i].getNodeEnd());
		}
		return neighbours;
	}
	
	/**
	 * Checks if the Node with the provided UID is a neighbor
	 * @param uid Die The UID of the neighbor to look for
	 * @return true, iff the neighbor was found
	 */
	public boolean isNeighbour(int uid)
	{
		MapEdge incomingEdges[]=this.getIncomingEdges();
		MapEdge outgoingEdges[]=this.getOutgoingEdges();
		for (int i = 0; i < incomingEdges.length; i++)
        {
	        if (incomingEdges[i].getNodeStart().getUID() == uid || incomingEdges[i].getNodeEnd().getUID() == uid)
	        	return true;
        }
		for (int i = 0; i < outgoingEdges.length; i++)
        {
	        if (outgoingEdges[i].getNodeEnd().getUID() == uid || outgoingEdges[i].getNodeStart().getUID() == uid)
	        	return true;
        }
		return false;
	}
	

	public int getUID() {
		return this.uid;
	}

	public boolean hasGPS() {
		return (this.GPS != null);
	}

	/**
	 * Naive adding - no Checking for correct Edge
	 * @param e
	 */
	public void addOutgoingEdge(MapEdge e) {
		outgoingEdges=Arrays.copyOf(outgoingEdges,outgoingEdges.length+1);
		outgoingEdges[outgoingEdges.length-1]=e;
	}
	
	/**
	 * Naive adding - no Checking for correct Edge
	 * @param e
	 */
	public void addIncomingEdge(MapEdge e) {
		incomingEdges=Arrays.copyOf(incomingEdges,incomingEdges.length+1);
		incomingEdges[incomingEdges.length-1]=e;
		
	}

	public int getNumberOfIncomingEdges() {
		return this.incomingEdges.length;
	}
	
	public int getNumberOfOutgoingEdges() {
		return this.outgoingEdges.length;
	}
	
	
	
	public String toString() {
		String nodeString;
		if (this.hasGPS())
			nodeString ="UID "+this.getUID()+", indoor: "+indoor+", GPS: Longitude: "+this.GPS.getLongitude()+" " + "Latitude "+this.GPS.getLatitude()+"\n";
		else
			nodeString ="UID "+this.getUID()+", indoor: "+indoor+", no GPS \n";
		nodeString+="Outgoing Edges: \n";
		for(MapEdge e: this.getOutgoingEdges()) {
			nodeString=nodeString+e.toString();
		}
		nodeString+="Incoming Edges: \n";
		for(MapEdge e: this.getIncomingEdges()) {
			nodeString=nodeString+e.toString();
		}
		nodeString+="\n";
		return nodeString;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MapNode) {
			return (((MapNode) obj).uid == this.uid);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.uid;
	}

	@Override
	public int compareTo(MapNode o) {
		return Integer.compare(this.uid, o.uid);
	}
	
	void remove() {
		while (incomingEdges.length > 0) {
			incomingEdges[0].remove();
		}
		while (outgoingEdges.length > 0) {
			outgoingEdges[0].remove();
		}
	}
	
	void removeEdge(MapEdge edge) {
		//for security reasons!
		boolean foundin = false, foundout = false;
		for (MapEdge myEdge : incomingEdges) {
			if (myEdge == edge) foundin = true;
		}
		for (MapEdge myEdge : outgoingEdges) {
			if (myEdge == edge) foundout = true;
		}
		if ((foundin || foundout) == false) return;
		int pos = 0;
		
		if (foundin) {
			MapEdge[] newIn = new MapEdge[incomingEdges.length-1];
			for (int i=0; i<incomingEdges.length; i++) {
				if (incomingEdges[i] != edge) 
					newIn[pos++] = incomingEdges[i];
			}
			incomingEdges = newIn;
		}
		pos = 0;
		if (foundout) {
			MapEdge[] newOut = new MapEdge[outgoingEdges.length-1];
			for (int i=0; i<outgoingEdges.length; i++) {
				if (outgoingEdges[i] != edge) 
					newOut[pos++] = outgoingEdges[i];
			}
			outgoingEdges = newOut;
		}
		
		
	}
}
