/**
 * 
 */
package main;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

/**
 * @author arno
 *	This Logger is a Singleton and can output logging data to Console, Files (auto-assigned) or a visual MessageBox.
 *	The logger automatically closes all Files when being destroyed!
 */
public class Logger {

	/**
	 * Saving Instance
	 */
	private static Logger instance = null; 
	
	private static boolean timeOutput = false;
	
	private static String newline = System.getProperty("line.separator");
	
	public static void setTimeOutput(boolean timeOutput) {
		Logger.timeOutput = timeOutput;
	}

	/**
	 * Mute the Logger - No Nesting!
	 */
	public static void mute() {
		oldOutput = output;
		setOutput(Output.SILENT);
	}
	
	/**
	 * Unmute the Logger 
	 */
	public static void unmute() {
		setOutput(oldOutput);
	}
	
	/**
	 * Singleton instance-getter 
	 * implements the Singleton-Pattern
	 */
	public static Logger getInstance() {
		if (instance == null) 
			instance = new Logger();
		return instance;
	}
	
	/**
	 * This field describes the Outoput method 
	 */
	private static Output output = Output.CONSOLE;
	private static Output oldOutput;
	
	/**
	 * The reserved filename, when logging into a file
	 */
	private String fileName = "";
	
	/**
	 * LogFile path
	 */
	private String filePath = "logs";
	

	private File file = null;
	private FileWriter writer = null;
	
	
	private Logger() {
		 //Constructor is private
	}
	
	/**
	 * Setter for the Output Decision
	 * This only switched the method - Files are not assigned by that
	 */
	public static void setOutput(Output op) {
		output = op;
	}
	
	/**
	 * Getter for the Output
	 */
	public static Output getOutput() {
		return output;
	}
	
	/**
	 * Getter for theLogFileName
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Getter for the LogFilePath
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Getter for the complete LogFilePath
	 */
	public String getFullFileName() {
		return filePath+File.separatorChar+fileName;
	}
	
	/**
	 * Assigns a LogFile. In Case of an Error there is a Console-Fallback
	 * @return true, iff the File was assigned successfully
	 */
	private boolean assignFile() {
		//Filename: logxxxxxxx.log - where xxxxxxx is the timeStamp
		//Assumption: OS is capable of more than 8+3 Filenames
		
		Date date = new Date();
		SimpleDateFormat format;
		
		format = new SimpleDateFormat("yyMMdd_HHmmss_S");
		
		String suffix = format.format(date);
		
		fileName = "log"+suffix+".log";
		

		try {
			file = new File(filePath+File.separatorChar+fileName);
			//Allow the writer to overwrite a LogFile
			writer = new FileWriter(file);
			writer.write("Log-File von TimeStamp: "+suffix);
			writer.write(newline);
			writer.write(newline);
			writer.flush();
		} catch (Exception e) {
			output = Output.CONSOLE;
			log("Logger", "Failed assigning a File: "+e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Log sth.
	 * @param sender Sender of the Log data
	 * @param o Object to Log
	 */
	public void log(String sender, Object o) {
		this.log(sender, o.toString());
	}
	
	/**
	 * Log sth.
	 * @param sender Sender of the Log data
	 * @param message String message to log
	 */
	public void log(String sender, String message) {
		//Output with timeStamp?
		if (timeOutput) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			sender = sender+"@ "+sdf.format(new Date());
		}
		//Now Log
		switch(output) {
			case SILENT: //No Output
				break;
			case CONSOLE: //No Console
				System.out.println("["+sender+"]: "+message);
				break;
			case MESSAGE: //Use a MesageBox
				JOptionPane.showMessageDialog(null,message,sender, JOptionPane.CANCEL_OPTION);
				break;
			case FILE: //Use a LogFile
				if (writer == null) {
					boolean erfolg = assignFile();
					if (erfolg == false) {
						log(sender,message);
						break;
					}
				}	
				try {
					writer.write("["+sender+"]: "+message+newline);
					writer.flush();
				} catch (Exception e) {
					output = Output.CONSOLE;
					log("Logger", "Failed assigning LogFile: "+e.getLocalizedMessage());
				}
				break;
		}
	}
	
	/**
	 * Close all Files when shutting down! (Do not let open handles back in FileSystem/OS)
	 */
	protected void finalize() throws Throwable {
		try {
			if (writer != null)
				writer.close();
		} catch (Exception e) {
			System.out.println("Fail during Finalizing: "+e.getLocalizedMessage());
		} finally {
			super.finalize();
		}
	}

}
