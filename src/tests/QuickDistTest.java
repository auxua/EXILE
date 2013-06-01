package tests;

import graphenbib.GPSCoordinate;
import graphenbib.InvalidGPSCoordinateException;

public class QuickDistTest {

	/**
	 * @param args
	 * @throws InvalidGPSCoordinateException 
	 */
	public static void main(String[] args) throws InvalidGPSCoordinateException {
		GPSCoordinate gpa = new GPSCoordinate(50.7791257,6.0589091);
		GPSCoordinate gpb = new GPSCoordinate(50.7790592,6.0589234);
		GPSCoordinate gpc = new GPSCoordinate(50.7791257,6.0589090);
		GPSCoordinate gpd = new GPSCoordinate(50.7791258,6.0589091);
		GPSCoordinate gpe = new GPSCoordinate(50.7791261,6.0589095);
		
		System.out.println("a->b: "+gpa.distanceTo(gpb));
		System.out.println("b->a: "+gpb.distanceTo(gpa));
		System.out.println("a->c: "+gpa.distanceTo(gpc));
		System.out.println("a->d: "+gpa.distanceTo(gpd));
		System.out.println("a->e: "+gpa.distanceTo(gpe));
		
		//System.out.println(((250.0f/ 40))*0.11f);
		
	}
}
