/**
 * 
 */
package main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author arno
 *	A simple Matrix implementation. The entries are stored column-wise due to usually handle whole columns at once in EXILE (better performance)
 *	The matrix is dynamically adapting it's size but throws Exceptions on Dimension errors
 */
public class Matrix {

	//For performance reasons the matrix will be stored columnwise.
	//private HashMap<Integer, HashMap<Integer, Double>> matrix = new HashMap<Integer, HashMap<Integer, Double>>();
	private HashMap<Integer, TreeMap<Integer, Double>> matrix = new HashMap<Integer, TreeMap<Integer, Double>>();
	
	/**
	 * Gets an Entry from the matrix (x,y)
	 * @param x The Line index
	 * @param y The column index
	 * @return The entry stored at that position
	 * @throws Exception if (x,y) is outside the matrix dimensions or the entry isn't initialized
	 */
	public double getEntry(int x, int y) throws Exception {
		//Entry is available
		if (y < matrix.size())
			if (x < matrix.get(y).size())
				return matrix.get(y).get(x);
		//Matrix has too small dimensions
		throw new Exception("Dimension Error");
	}
	
	/**
	 * Gets a complete column of the matrix
	 * @param y the column index
	 * @return The column y of the matrix
	 * @throws Exception if y outside the matrices dimensions
	 */
	//public HashMap<Integer, Double> getColumn(int y) throws Exception {
	public TreeMap<Integer, Double> getColumn(int y) throws Exception {
		//Check Dimension
		if (y < matrix.size())
			return matrix.get(y);
		//Error
		throw new Exception("Dimension Error");
	}
	
	/**
	 * Puts an Entry into the matrix - Only Accessible to the packet toavoid manipulative mods
	 * @param x the row index
	 * @param y the column index
	 * @param d the new value to be stored
	 * @throws Exception if invalid dimensions are used (e.g. negative indices) or epmpty/uninitilaized columns are in between
	 */
	protected void setEntry(int x, int y, double d) throws Exception {
		//Non-existing Column (last one)
		if (y == matrix.size())
			matrix.put(y, new TreeMap<Integer, Double>());
		
		//trivial case - throw native exception for bad dimensioning!
		matrix.get(y).put(x, d);
		
		
	}
	
	/**
	 * Returns the actual number of stored columns of the matrix
	 * @return the size of the columnspace of the matrix
	 */
	public int getColNum() {
		return this.matrix.size();
	}
	
	/**
	 * Return the index of the maximum valued entry of a column
	 * @param y the column index
	 * @return the row index
	 * @throws Exception if dimensions are wrong
	 */
	public int getColumnMaxPosition(int y) throws Exception {
		if (y >= matrix.size())
			throw new Exception("Dimension Error");
		
		double max=-1; double  act; int pos = -1; Entry<Integer,Double> entry;
		Iterator<Entry<Integer,Double>> it = matrix.get(y).entrySet().iterator();
		while (it.hasNext()) {
			entry = it.next();
			act =  entry.getValue();
			if (max < act) {
				max = act;
				pos = entry.getKey();
			}
		}
		
		return pos;
			
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String string = "======= Matrix ====="+System.getProperty("line.separator");

		int maxSizeEstimate = matrix.size();
		
		for (int i=0; i<matrix.get(0).size(); i++) {
			for (int j=0; j<maxSizeEstimate; j++) {
				try {
					string += " [ "+this.getEntry(i, j)+" ] ";
				} catch (Exception e) {
					return string+e.toString()+" at (i,j): "+i+","+j;
				}
			}
			string += System.getProperty("line.separator");
		}
		
		return string;
		
	}
	
	/**
	 * Returns the Dimension of the columns.
	 * !Beware This function do not check for inconsistency errors!
	 * @return the size of the column dimension
	 */
	public int getColumnDimension() {
		return this.matrix.get(0).size();
	}
	
	/**
	 * inserts a complete column into the matrix 
	 * @param column the index of the column to be placed
	 * @param table the column containing data
	 */
	protected void setColumn(int column, TreeMap<Integer, Double> table) {
		this.matrix.put(column, table);
		
	}
	
	
}
