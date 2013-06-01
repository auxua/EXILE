package graphenbib;

/**
 * 
 * @author arno
 *	This Exception can be thrown when targeting an unknown node
 */
public class NodeNotInGraphException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2438197459166466929L;

	public NodeNotInGraphException() {
		super();
	}

	public NodeNotInGraphException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public NodeNotInGraphException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NodeNotInGraphException(String arg0) {
		super(arg0);
	}

	public NodeNotInGraphException(Throwable arg0) {
		super(arg0);
	}

}
