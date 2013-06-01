/**
 * 
 */
package graphenbib;

/**
 * @author arno
 *
 *	This is an Edge for a graph. It contains information about the bearing,virtual steps and can be used for several filtered weightening (e.g. max speed or sth.)
 *	The ID of the edge does not need to be unique!
 */
public class MapEdge {

	private String name;

	boolean oneWay;
	boolean draw;
	
	private int wayID;
	private int length;
	private StreetType streetType;
	private MapNode start, end;
	//FP-extra
	private double bearing;

	private boolean containsVirtualSteps = false;

	public boolean hasVirtualSteps() {
		return containsVirtualSteps;
	}

	public int getStepCount() {
		return stepCount;
	}

	public GPSCoordinate[] getVirtualSteps() {
		return virtualSteps;
	}
	
	/**
	 * Computes the Directions from its virtual Step locations to a GPS-position
	 * @param gps the gpscoordinate heading towards
	 * @return an Array of the bearings (in the same order as the steps along the edge)
	 */
	public double[] getInitialBearingTo(GPSCoordinate gps) {
		double[] bearings = new double[this.stepCount];
		int i = 0;
		for (GPSCoordinate vgps : this.virtualSteps) {
			bearings[i++] = this.getInitialBearing(vgps.getLatitude(), vgps.getLongitude(), gps.getLatitude(), gps.getLongitude());
		}
		return bearings;
	}

	private int stepCount = 0;

	private GPSCoordinate[] virtualSteps;
	
	/**
	 * returns the bearing of the edge from start to end
	 */
	public double getCompDir() {
		return this.bearing;
	}
	
	/**
	 * Returns the bearing of the edge from end to start
	 */
	public double getInvCompDir() {
		return (this.bearing-180 < 0) ? this.bearing + 180.0 : this.bearing-180;
	}

	/**
	 * 
	 * @param start startNode of the edge.
	 * @param end endNode of the edge.
	 * @param wayID The (non-unique) wayID - basing on OSM
	 * @param length Length of the edge
	 * @param streetType The StreetType depending on OSM/Filters
	 */
	public MapEdge(MapNode start, MapNode end, int wayID, int length, StreetType streetType) {
		this.start=start;
		this.end=end;
		this.wayID = wayID;
		this.length = length;
		this.streetType = streetType;
		this.bearing = getInitialBearing(start.getGPS().getLatitude(), start.getGPS().getLongitude(), end.getGPS().getLatitude(), end.getGPS().getLongitude());
	}
	
	/**
	 * @param lat_1
	 * @param lon_1
	 * @param lat_2
	 * @param lon_2
	 * @return bearing
	 */
	private double getInitialBearing(double lat_1, double lon_1, double lat_2, double lon_2) {
		// source: http://www.movable-type.co.uk/scripts/latlong.html
		double dLon = lon_2 - lon_1;
		lat_1 = Math.toRadians(lat_1);
		lon_1 = Math.toRadians(lon_1);
		lat_2 = Math.toRadians(lat_2);
		lon_2 = Math.toRadians(lon_2);
		dLon = Math.toRadians(dLon);
		double y = Math.sin(dLon) * Math.cos(lat_2);
		double x = Math.cos(lat_1) * Math.sin(lat_2) - Math.sin(lat_1)
				* Math.cos(lat_2) * Math.cos(dLon);
		double b = Math.atan2(y, x);
		b = Math.toDegrees(b);
		return (b < 0) ? b + 360.0 : b;
	}

	/**
	 * Getter-Methode for the StreetType
	 * @return StreetType for the StreetType
	 */
	public StreetType getType()
	{
		return streetType;
	}
	
	/**
	 * Getter for the Edge ID
	 * @return The ID of the edge
	 */
	public int getUID()
	{
		return wayID;
	}
	
	/**
	 * Getter for the length
	 * @return The Length o the edge in cm
	 */
	public int getLength()
	{
		return length;
	}
	
	
	/**
	 * Setter for the length
	 * @param length length of the edge
	 */
	protected void setLength(int length) {
		this.length=length;
	}
	
	/**
	 * Get Weigth of an Edge - By default this is the length, but can be adapted to filters, speed limits, etc.
	 * @return Weight of the edge as long
	 */
	public long getWeight()
	{
		return this.getLength();
	}
	
	/**
	 * Getter for start Node
	 * @return The startNode
	 */
	public MapNode getNodeStart()
	{
		return start;
	}

	/**
	 * Getter for the End Node
	 * @return The EndNode
	 */
	public MapNode getNodeEnd()
	{
		return end;
	}
	
	/**
	 * get the SStreerName, if this is stored
	 * @return StreetName.
	 */
	public String getName() {
		return name;
	}
	
	public MapEdge(MapNode start, MapNode end, int wayID, int length, StreetType streetType, String name)
    {
		this(start,end,wayID,length,streetType);
		this.name=name;
    }
	
	public MapEdge(MapNode start, MapNode end, int wayID, int length, StreetType streetType, String name, boolean draw, boolean oneWay)
    {
		this(start,end,wayID,length,streetType,name);
		this.draw=draw;
		this.oneWay=oneWay;
    }
	

	public String toString(){
		return("Edge: startUID "+this.getNodeStart().getUID()+", endUID "+this.getNodeEnd().getUID()+", " +
				"wayID "+this.getUID()+", Length "+this.getLength()+", CompDir: "+this.getCompDir()+" invComp: "+this.getInvCompDir()+", Gewicht: "+this.getWeight()+
				" #virtual Steps: "+this.virtualSteps.length+" .\n");
	}
	
	public boolean isOneWay() {
		return this.oneWay;
	}
	
//	public int getNeededSteps(int stepSize) {
//		return (1+(this.length/stepSize));
//	}
	
	/**
	 * Creates Virtual Steps based on the given steplength argument. These virtual steps will be stored in this edge 
	 * @param steplength the initial steplength in cm
	 * @throws InvalidGPSCoordinateException 
	 */
	public void createVirtualSteps(int steplength) throws InvalidGPSCoordinateException {
		//Small diffs in GPS (in range of <60cm) will float to 0 in coputations -> make one step out of it.
		if (this.length <10) this.length = steplength;
		//How many Steps wolud be needed? (Take some cm to avoid too little steps)
		int steps = (int) Math.floor((this.length-10) / steplength);
		if (steps < 0) {
			this.containsVirtualSteps = false;
			return;
		}
		
		this.containsVirtualSteps = true;
		//Store number of Steps
		this.stepCount = steps;
		
		this.virtualSteps = new GPSCoordinate[steps];
		
		//Estimate the GPS-Coordinates
		//simplified linear version - for exact version use Vincenty's method for the direct problem
		//Should work because of small steps allowing assumption of linear geometrics
		double startLat = this.start.getGPS().getLatitude();
		double startLon = this.start.getGPS().getLongitude();
		double endLat = this.end.getGPS().getLatitude();
		double endLon = this.end.getGPS().getLongitude();
		
		double latDist = endLat-startLat;
		double lonDist = endLon - startLon;
		
		double nlat,nlon;
		GPSCoordinate gtmp;
		
		for(int i = 0; i<steps; i++) {
			nlat = ((latDist / steps)*(i+1)) + startLat; 
			nlon = ((lonDist / steps)*(i+1)) + startLon;
			gtmp = new GPSCoordinate(nlat,nlon);
			//Store the GPS-Estimate into the array for later wrk
			this.virtualSteps[i] = gtmp;
		}
	}
	
	void remove() {
		start.removeEdge(this);
		end.removeEdge(this);
	}
}
