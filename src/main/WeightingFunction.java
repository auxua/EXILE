/**
 * 
 */
package main;

import main.BiMap.MyEntry;
import mods.Emulateable;
import mods.LocationEstimationVector;
import java.util.Collection;
import java.util.HashSet;

import mods.*;

/**
 * @author arno
 *	This is the weighting function. It combines several mods.LocationEstimationVector  and uses static or dynamic weighting.
 */
public class WeightingFunction {

	public final boolean dynamic;
	
	private final int length;
	
	private Matrix matrix;
	
	private HashSet<Sensor> disabledSensors = new HashSet<Sensor>();
	
	public WeightingFunction(int length, Matrix matrix) {
		this.dynamic = Config.useDynamicWeighting;
		this.length = length;
		this.matrix = matrix;
	}
	
	public void reportDisabledSensor(Sensor sensor) {
		this.disabledSensors.add(sensor);
	}
	
	public void reportEnabledSensor(Sensor sensor) {
		this.disabledSensors.remove(sensor);
	}

	public LocationEstimationVector getWeightedLEV(Collection<LocationEstimationVector> LEVs, int columnIndex) {
		LocationEstimationVector LEV = new LocationEstimationVector(length);
		
		if (dynamic)
			return weightDynamically(LEV, LEVs);
		
		return weightStatic(LEV, LEVs);
	}

	private LocationEstimationVector weightStatic(LocationEstimationVector LEV,	Collection<LocationEstimationVector> LEVs) {
		for (LocationEstimationVector tempVect : LEVs) {
			//get Weightening for Mod and multiply

			double factor = this.getBaseWeighting(tempVect.getProvider());

			//Synchronizing LEV?
			if (factor == -1)  {
				return tempVect;
			}
			if (factor == 0.0) continue;
			//otherwise multadd
			tempVect.multiply(factor);
			//Adding to LEV
			
			LEV.add(tempVect);			
		}
		
		
		return LEV;
	}

	private LocationEstimationVector weightDynamically(LocationEstimationVector LEV, Collection<LocationEstimationVector> LEVs) {
		for (LocationEstimationVector tempVect : LEVs) {
			//get Weightening for Mod and multiply
			double factor = this.getBaseWeighting(tempVect.getProvider());

			//Synchronizing LEV?
			if (factor == -1)  {
				return tempVect;
			}
			
			for (Sensor sensor : tempVect.getProvider().getSensors()) {
				//Ignore Systems with disabled Sensors
				if (this.disabledSensors.contains(sensor)) factor = 0.0;
			}
			
			if (factor == 0.0) continue;

			//Now use dynamic Weightening!
			Emulateable em = tempVect.getProvider();
			if (em instanceof WiFiFinger) {
				tempVect = weightWIFI(tempVect);
			} else if (em instanceof StepDetection) {
				tempVect = weightStepDetection(tempVect);
			} else if (em instanceof GPS) {
				tempVect = weightGPS(tempVect);
			} else if (em instanceof FloorColor) {
				tempVect = weightFloorColor(tempVect);
			}
			
			//otherwise multadd and wei
			//tempVect.multiply(factor);
			//Adding to LEV
			
			LEV.add(tempVect);			
		}
		
		
		return LEV;
	}
	
	private LocationEstimationVector weightFloorColor(LocationEstimationVector tempVect) {
		/********************************
		 * 1st Step: Check for color distribution
		 *******************************/
		double colorFract = 0;
		Double d = (Double) tempVect.getParameter("ColorFraction");
		if (d != null) {
			colorFract = 1-d;
		}
		
				
		/********************************
		 * 2nd Step: Apply
		 *******************************/
		double factor = this.getBaseDynWeighting(tempVect.getProvider());
		
		factor = factor * colorFract;
		
		tempVect.multiply(factor);
		
		return tempVect;
	}

	private LocationEstimationVector lastGPS = null;
	
	private LocationEstimationVector weightGPS(LocationEstimationVector tempVect) {
		/********************************
		 * 1st Step: Check for indoor environment (Based on literature: Not reliable indoor at all)
		 *******************************/
		int lastColumn = matrix.getColNum();
		int lastIndex = 0;
		boolean indoor = false;
		try {
			lastIndex = matrix.getColumnMaxPosition(lastColumn-1);
		} catch (Exception e) {
			Logger.getInstance().log("WeightingFunction.weightGPS", "Failed getting the last Position n the matrix: "+e);
		}
		MyEntry<Integer> lastPos = Emulator.mapping.get(lastIndex);
		if (lastPos == null) {
			Logger.getInstance().log("WeightingFunction.weightGPS", "Could not check for inddor/outdoor position");
		} else {
			//Only do this, if we have the last Position
			if (Emulator.graph.getNode(lastPos.getValueX()).isIndoor()) indoor = true;
			if (lastPos.getValueY() != 0) {
				if (Emulator.graph.getNode(lastPos.getValueY()).isIndoor())
					indoor = true;
			}
		}

		/********************************
		 * 2nd Step: Check GPS-History (Several times the same LEV is not a good locating...)
		 *******************************/
		boolean unchanged = false;
		//Only check if there is a histroy...
		if (this.lastGPS != null) {
			if (lastGPS.equals(tempVect)) {
				unchanged = true;
			}
		}
		
		/********************************
		 * 3rd Step: Apply
		 *******************************/
		double factor = this.getBaseDynWeighting(tempVect.getProvider());
		if (unchanged) factor = (factor/4);
		if (indoor) factor = 0;
		
		tempVect.multiply(factor);
		
		return tempVect;
	}

	private LocationEstimationVector weightStepDetection(LocationEstimationVector tempVect) {
		/********************************
		 * 1st Step: Check for the minScore of this Step
		 *******************************/
		
		Integer minScore = (Integer)tempVect.getParameter("minScore");
		double factor = this.getBaseDynWeighting(tempVect.getProvider());
		double penalty = 0.0;
		
		// Increase penalty for every Scoring-Step that is lower than the actual minscore
		// -> give penalties for too far away directions
		if (minScore > 42) penalty += 0.2;
		if (minScore > 90) penalty += 0.2;
		if (minScore > 120) penalty += 0.2;
		
		/*******************************
		 * 2nd Step: Check for sth. I just forgot -.-
		 *******************************/
		
		
		/*******************************
		 * 3rd Step: Apply
		 ******************************/
		
		factor = factor - penalty;
		if (factor < 0) factor = 0;
		tempVect.multiply(factor);
		
		return tempVect;
	}

	private LocationEstimationVector weightWIFI(LocationEstimationVector tempVect) {
		/********************************
		 * 1st step: Checking indoor/outdoor
		 * (General Assumption: Indoor WiFi-Locating is better than the outdoor counterpart)
		 *******************************/
		int lastColumn = matrix.getColNum();
		int lastIndex = 0;
		double penalty = 0;
		try {
			lastIndex = matrix.getColumnMaxPosition(lastColumn-1);
		} catch (Exception e) {
			Logger.getInstance().log("WeightingFunction.wightWIFI", "Failed getting the last Position n the matrix: "+e);
		}
		MyEntry<Integer> lastPos = Emulator.mapping.get(lastIndex);
		if (lastPos == null) {
			Logger.getInstance().log("WeightingFunction.weightWIFI", "Could not check for inddor/outdoor position");
		} else {
			//Only do this, if we have the last Position
			if (Emulator.graph.getNode(lastPos.getValueX()).isIndoor()) penalty = Config.OUTDOORPENALTY;
			if (lastPos.getValueY() != 0) {
				if (Emulator.graph.getNode(lastPos.getValueY()).isIndoor())
					penalty = Config.OUTDOORPENALTY;
			}
		}
		
		/********************************
		 * 2nd Step: compute the accuracy-dependent factor for weigthing
		 *******************************/
		double factor = ((double) Config.WIFI_ACC_THRESHOLD / (double) tempVect.getAccuracy());
		if (factor > 1) factor = 1;
		factor = factor * this.getBaseDynWeighting(tempVect.getProvider());
		
		
		/*******************************
		 * 3rd Step: Check for Scanszize
		 ******************************/
		Integer scanSize = (Integer) tempVect.getParameter("scanSize");
		if (scanSize == null || scanSize < Config.WIFI_NUMSCANS_THRESHOLD)
			factor = 0;
		
		/*******************************
		 * 4th Step: Apply
		 ******************************/
		factor = factor - penalty;
		if (factor < 0) factor = 0;
		tempVect.multiply(factor);
		
		
		return tempVect;
	}

	/**
	 * These are fixed values for Base Weighting - where -1 denotes a synchronization point of exclusively priority
	 * @param mod the emulator providing data
	 * @return the factor used for the weighting base
	 */
	private double getBaseWeighting(Emulateable mod) {
		if (mod instanceof Synchronizing) {

			return -1.0; 
		}
		if (mod instanceof FloorColor)
			return 0.2;
		if (mod instanceof GPS)
			return 0.7;
		if (mod instanceof WiFiFinger)
			return 0.7;
		if (mod instanceof StepDetection)
			return 1.0;
			//return 0.1;
		
		//strict handling of unregistered Modules
		return 0.0;
	}
	
	private double getBaseDynWeighting(Emulateable mod) {
		if (mod instanceof Synchronizing) {
			return -1.0; 
		}
		if (mod instanceof FloorColor)
			return 0.3;
		if (mod instanceof GPS)
			return 0.7;
		if (mod instanceof WiFiFinger)
			return 0.9;
		if (mod instanceof StepDetection)
			return 1.0;
			//return 0.1;
		
		//strict handling of unregistered Modules
		return 0.0;
	}
}
