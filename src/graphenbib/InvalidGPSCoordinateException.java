/**
 * 
 */
package graphenbib;

/**
 * @author arno
 *	A simple Exception for marking invalid GPS Coordinates
 */
public class InvalidGPSCoordinateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5825709700564905971L;
	
	public InvalidGPSCoordinateException()
	{
	}

	public InvalidGPSCoordinateException(String arg0)
	{
		super(arg0);
	}

	public InvalidGPSCoordinateException(Throwable arg0)
	{
		super(arg0);
	}

	public InvalidGPSCoordinateException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

}
