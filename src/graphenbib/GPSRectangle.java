/**
 * 
 */
package graphenbib;

/**
 * @author arno
 *
 *	This class represents a pseudo-rectangle spanned by two GPS Coordinates.
 */
public class GPSRectangle {

	private GPSCoordinate upperLeft;
	private GPSCoordinate lowerRight;
	
	
	/**
	 * Construct a GPSRectangle basing on two GPSCoordinates
	 */
	public GPSRectangle(GPSCoordinate upperLeft, GPSCoordinate lowerRight) throws EmptyInputException, InvalidInputException {
		//Chec the Edges
		if (upperLeft == null || lowerRight == null) {
			throw new EmptyInputException("At least on eCoordinate was invalid");
		}
		
		//Check if the edges are spanning a rectangle
		if ((upperLeft.getLatitude() <= lowerRight.getLatitude()) || (upperLeft.getLongitude() >= lowerRight.getLongitude())) {
			throw new InvalidInputException("The Coordinates do not span a rectangle");
		}
		this.upperLeft = upperLeft;
		this.lowerRight = lowerRight;
	}
	
	/**
	 * Constructing a Rectangle - parameters are provided clockwise
	 */
	public GPSRectangle(double top, double right, double bottom, double left) throws EmptyInputException, InvalidInputException, InvalidGPSCoordinateException {
		GPSCoordinate upLeft = new GPSCoordinate(top,left);
		GPSCoordinate lowRight = new GPSCoordinate(bottom,right);
		
		//Check i the edges are spanning a rectangle
		if ((upLeft.getLatitude() <= lowRight.getLatitude()) || (upLeft.getLongitude() >= lowRight.getLongitude())) {
			throw new InvalidInputException("The Coordinates do not span a rectangle");
		}
		this.upperLeft = upLeft;
		this.lowerRight = lowRight;
	}
	
	/**
	 * Getter for the top left Corner
	 */
	public GPSCoordinate getUpperLeft() {
		return this.upperLeft;
	}
	
	/**
	 * Getter for the lower right Corner 
	 */
	public GPSCoordinate getLowerRight() {
		return this.lowerRight;
	}
	
	/**
	 * Checks if the Coordinate is inside this Rectangle
	 */
	public boolean GPSInside(GPSCoordinate gps) {
		return ((gps.getLongitude() >= this.upperLeft.getLongitude()) &&
				(gps.getLatitude() <= this.upperLeft.getLatitude()) &&
				(gps.getLongitude() <= this.lowerRight.getLongitude()) &&
				(gps.getLatitude() >= this.lowerRight.getLatitude()));
	}
	
	/**
	 * Getter for minimal Latitude
	 */
	public double getMinLat() {
		return lowerRight.getLatitude();
	}
	
	/**
	 * Getter for maximal Latitude
	 */
	public double getMaxLat() {
		return upperLeft.getLatitude();
	}
	
	/**
	 * Getter fo minimal Longitude
	 */
	public double getMinLon() {
		return upperLeft.getLongitude();
	}
	
	/**
	 * Getter for maximal Longitude
	 */
	public double getMaxLon() {
		return lowerRight.getLongitude();
	}
}
