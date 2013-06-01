/**
 * 
 */
package mods;

/**
 * @author arno
 * Enum of possible Sensors (Extensible, refinement possible)
 */
public enum Sensor {
	GPS, //The GPS (or Galileo, Glonass.....)
	ACC, //Accelerometer
	COMPASS, //The magnetormeter/Compass
	WIFI, //possible to subdivide to 2.4/5/... GHz Bands
	CAMERA, //The Camera (pictures or video)
	AUDIO, //Audio/Sound-based like Microphone, Ultrasonic...
	RADIO, //Radio-Based like NFC
	UNKNOWN //For unknown Sensors
}
