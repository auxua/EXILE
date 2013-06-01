package importer;

import graphenbib.StreetType;

/**
 * Temporary structure during Import
 * @author arno
 *
 */
class ImportEdge {
	
	private int startNode = 0;
	private int endNode = 0;
	private int wayID = 0;
	private int length;
	private boolean oneway;
	private String name;
	private StreetType streetType;

	
	/**
	 * Constructor
	 */
	public ImportEdge(int wayID, int startNode, int endNode, int distance,
			StreetType streetType) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.wayID = wayID;
		this.length = distance;
		this.streetType = streetType;
	}

	/**
	 * Another Constructor
	 */
	public ImportEdge(int wayID, int startNode, int endNode, int distance,
			boolean oneway, String name, StreetType streetType) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.wayID = wayID;
		this.length = distance;
		this.oneway = oneway;
		this.name = name;
		this.streetType = streetType;
	}

	/**
	 * Set the Start Node of the Edge
	 */
	public void setStartNode(int node) {
		this.startNode = node;
	}

	/**
	 * Set the Endv Node of the edge
	 */
	public void setEndNode(int node) {
		this.endNode = node;
	}

	/**
	 * Get the Start Node ID 
	 */
	public int getStartNode() {
		return startNode;
	}

	/**
	 * Get the End Node ID
	 */
	public int getEndNode() {
		return endNode;
	}

	/**
	 * Get the Edge ID
	 */
	public int getWayID() {
		return wayID;
	}

	/**
	 * Set the ID 
	 */
	public void setWayID(int wayID) {
		this.wayID = wayID;
	}

	/**
	 * Get the StreetType
	 */
	public StreetType getStreetType() {
		return streetType;
	}

	public void setStreetType(StreetType streetType) {
		this.streetType = streetType;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isOneway() {
		return oneway;
	}

	public void setOneway(boolean oneway) {
		this.oneway = oneway;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return ("WayID: " + wayID + "	StartNodeID: " + startNode
				+ "	EndNodeID: " + endNode + " Name: " + name + " Oneway: "
				+ oneway + " length: " + length);
	}

	/**
	 * Convert to a String that might be easily and simply stored in a text File
	 */
	public String toFileString() {
		return ("Edge;" + wayID + ";" + startNode + ";" + endNode);
	}

}
