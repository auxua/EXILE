/**
 * 
 */
package mods;

import java.util.HashMap;


/**
 * @author arno
 *	Describes a Location Estimation Vector
 * Additionally Stores Information about its data TimeStamp and provider
 */
public class LocationEstimationVector {
	
	/**
	 * Stores the estimation data
	 * ((K,V),(K',V'))
	 * 
	 * ((NodeStart,score),(NodeEnd,progress))
	 * 
	 * Case 1: P.Value for one Node
	 * 		((NodeUID,score),null)
	 * 
	 * Case 2: Score concerning a virtual Step between A and B (step number num)
	 * 		((NodeStart,score),(NodeEnd,progress))
	 */
	//HashMap<Entry<Integer,Double>, Entry<Integer,Integer>> values;
	
	/**
	 * Stores the estimation data by Index->Value
	 */
	double[] values;
	
	private long timeStamp;

	private Emulateable provider;
	
	/**
	 * A generic data structure enabling the mod to provide further information about an estimation
	 */
	private HashMap<String,Object> parameters = new HashMap<String,Object>();
	
	public void addParamter(String key, Object value) {
		parameters.put(key, value);
	}
	
	public Object getParameter(String key) {
		return parameters.get(key);
	}
	
	/**
	 * Mods can give information about the estimate's accuracy for better weightening.
	 */
	private int accuracy;
	
	//public final <Provider>
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public int size() {
		return values.length;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	private void init(int size) {
		this.values = new double[size];
		for (int i=0; i<size; i++)
			values[i] = 0.0;
	}

	public LocationEstimationVector(int size) {
		//this.values = new double[size];
		this.init(size);
	}
	
	public LocationEstimationVector(int size, long timeStamp) {
		this.setTimeStamp(timeStamp);
		//values = new double[size];
		this.init(size);
	}
	
	public LocationEstimationVector(int size, long timeStamp, Emulateable em) {
		this.setTimeStamp(timeStamp);
		//values = new double[size];
		this.init(size);
		this.provider = em;
	}
	
	public Emulateable getProvider() {
		return this.provider;
	}
	
	public void setProvider(Emulateable em) {
		this.provider = em;
	}
	
	public void setEntry(int index, double value) {
		this.values[index] = value;
	}
	
	public void multiply(double factor) {
		for (int i=0; i<values.length; i++)
			values[i] *= factor;
	}
	
	public void add(LocationEstimationVector LEV) {
		for(int i=0; i<values.length; i++)
			values[i] += LEV.getEntry(i);
	}

	public double getEntry(int i) {
		return values[i];
	}

	public void setMaxEntry(Integer key, double d) {
		this.setEntry(key, Math.max(this.getEntry(key), d));
		
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}
	
	public int getAccuracy() {
		return this.accuracy;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String string = "===LEV==="+System.getProperty("line.separator");
		for (int i=0; i<values.length; i++) {
			string += i+": [ "+values[i]+" ]"+System.getProperty("line.separator");
		}
		return string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LocationEstimationVector) {
			for (int i=0; i<this.size(); i++) {
				if (this.values[i] != ((LocationEstimationVector) obj).values[i]) return false;
			}
			return true;
		}
		return false;
	}
	
}
