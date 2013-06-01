/**
 * 
 */
package graphenbib;

/**
 * @author arno
 *	A simple Exception for invalid Inputs (e.g. null Pointers or values not in the right domain) 
 */
public class InvalidInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4478480620974589710L;

	public InvalidInputException()
	{
	}

	public InvalidInputException(String arg0)
	{
		super(arg0);
	}

	public InvalidInputException(Throwable arg0)
	{
		super(arg0);
	}

	public InvalidInputException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}
}
