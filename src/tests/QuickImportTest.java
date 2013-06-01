package tests;

import java.io.File;
import java.io.IOException;

import graphenbib.*;
import importer.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

public class QuickImportTest {

	
	public static void main(String[] args) throws IOException, XMLStreamException, EmptyInputException, InvalidInputException, InvalidGPSCoordinateException, NodeNotInGraphException {
		//Just test for fails when importing/reading the OSM
		
		File file = new File("testFiles/parkinglot.osm");
		StreamSource source = new StreamSource(file);
		MapGraph graph = OSMReader.readOSMSource(source);
		System.out.println(graph.getLowerRight());
		System.out.println(graph.getUpperLeft());
		System.out.println(graph);

	}

}
