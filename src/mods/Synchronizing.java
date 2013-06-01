/**
 * 
 */
package mods;

/**
 * @author arno
 *	This interface only instances synchronizing modules that should be handled regarding to this by the weightening function
 */
public interface Synchronizing extends Emulateable {

	/**
	 * After having the matrix for transitions completely created the resetPoition will correct
	 * the position by reliability of Synchronization methods
	 * @return true if it worked, false otherwise
	 */
	//public boolean resetPositions();
	
}
