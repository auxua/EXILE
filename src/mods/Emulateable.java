/**
 * 
 */
package mods;


/**
 * @author arno
 *	This interface defines what methods need to be implemented to provide an emulateable module
 */
public interface Emulateable {

	/**
	 * This function loads the data
	 * @return true if the data was correctly found and formatted (no check for fitting the map!)
	 */
	public boolean init();
	
	//public boolean addEstimates(MapGraph graph);
	
	/**
	 * Returns the Sensors used for the Mod
	 * @return Array of the Sensor enum
	 */
	public Sensor[] getSensors();
	

	
	
	
	
	/**
	 * Returns estimates on the Position of the user/device
	 * 
	 * ! The mod itself has to decide if an entry of its data may fit better to the actual or next timestamp !
	 * For example dataTimeStamp = 80, timeStamp1 = 1, timeStamp2 = 81 -> Should decide that dataTimeStamp is not used in most cases.
	 * @param timeStamp1 the actual concerning TimeStamp (FP-based)
	 * @param timeStamp2 the upcoming TimeStamp (FP-Based)
	 * @return Estimates of the position
	 */
	public LocationEstimationVector getEstimates(long timeStamp1, long timeStamp2);
}
