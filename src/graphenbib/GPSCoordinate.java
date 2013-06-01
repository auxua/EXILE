/**
 * 
 */
package graphenbib;

/**
 * @author arno
 *
 *	The class can represent a GPS coordinate and compute a distance to another GPS coordinate. 
 */
public class GPSCoordinate {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 5563090485913454340L;
	private double longitude;
	private double latitude;
	
	public final static int erdRadius = 637813700; //Radius is cm
	
//	private double getInitialBearingTo(double lat_2, double lon_2) {
//		// source: http://www.movable-type.co.uk/scripts/latlong.html
//		double dLon = lon_2 - this.longitude;
//		double lat_1 = Math.toRadians(this.latitude);
//		double lon_1 = Math.toRadians(this.longitude);
//		lat_2 = Math.toRadians(lat_2);
//		lon_2 = Math.toRadians(lon_2);
//		dLon = Math.toRadians(dLon);
//		double y = Math.sin(dLon) * Math.cos(lat_2);
//		double x = Math.cos(lat_1) * Math.sin(lat_2) - Math.sin(lat_1)
//				* Math.cos(lat_2) * Math.cos(dLon);
//		double b = Math.atan2(y, x);
//		b = Math.toDegrees(b);
//		return (b < 0) ? b + 360.0 : b;
//	}

	/**
	 * Constructor for a GPS Coordinate.
	 * @param latitude The Coordinate's Latitude
	 * @param longitude The Coordinate's Longitude
	 * @throws InvalidGPSCoordinateException if the arguments are outside the GPS-Domain (-180 - 180)
	 */
	public GPSCoordinate(double latitude, double longitude) throws InvalidGPSCoordinateException
	{
		if ((longitude<=180) && (longitude>=-180) && (latitude >=-90) && (latitude<=90)) {
			this.longitude = longitude;
			this.latitude = latitude;
		} else {
			throw(new InvalidGPSCoordinateException("GPSCoordinate not in the GPS-Space. ("+longitude+","+latitude+")"));
		}
	}
	

	public double getLongitude()
	{
		return longitude;
	}

	public double getLatitude()
	{
		return latitude;
	}
	
	
	public String toString(){
		return("GPS: Longitude "+longitude+" Latitude "+latitude);
	}
	
	/**
	 * Computed the Distance to another GPS Coordinate. (in cm)
	 * @param b GPSCoordinate to which the distance should be computed
	 * @return The distance between this Coordinate and b in cm
	 * @throws InvalidGPSCoordinateException if the GPS-Coordinate-parameter was null
	 */
	public int distanceTo(GPSCoordinate b) throws InvalidGPSCoordinateException{
		if(b==null) {
			throw new InvalidGPSCoordinateException("Fail - GPSCoordinate is null pointer");
		}
		GPSCoordinate a =this;
		return (int) (Math.acos(Math.sin(b.getLatitude()/180*Math.PI)*Math.sin(a.getLatitude()/180*Math.PI) + Math.cos(b.getLatitude()/180*Math.PI)*Math.cos(a.getLatitude()/180*Math.PI)*Math.cos(b.getLongitude()/180*Math.PI-a.getLongitude()/180*Math.PI) ) * GPSCoordinate.erdRadius);
	}

}
