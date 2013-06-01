/**
 * 
 */
package graphenbib;

/**
 * @author arno
 *
 *	A simple Exception for Empty Inputs
 */
public class EmptyInputException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8035188365634758155L;

	public EmptyInputException()
	{
	}

	public EmptyInputException(String arg0)
	{
		super(arg0);
	}

	public EmptyInputException(Throwable arg0)
	{
		super(arg0);
	}

	public EmptyInputException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

}
