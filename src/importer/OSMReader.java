/**
 * 
 */
package importer;

import graphenbib.EmptyInputException;
import graphenbib.GPSCoordinate;
import graphenbib.InvalidGPSCoordinateException;
import graphenbib.InvalidInputException;
import graphenbib.MapGraph;
import graphenbib.MapNode;
import graphenbib.NodeNotInGraphException;
import graphenbib.StreetType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import main.Config;
import main.Logger;

/**
 * @author arno,Marc Gerasch
 *	This is a OSM/XML-parser converting a OSM-File into a MapGraph. By adjusting the filters the imported ways can easily changed.  
 */
public class OSMReader {
	
	
	public static MapGraph readOSMFile(File f) throws IOException, XMLStreamException, EmptyInputException, InvalidInputException, InvalidGPSCoordinateException, NodeNotInGraphException {
		return readOSMSource(new StreamSource(f));
		
	}
	
	/**
	 * Reads an OSM File and construct a MapGraph
	 * 
	 * @param source StreamSource of the OSM File.
	 * @return MapGraph representing the Map stored in the OSM File.
	 */
	public static MapGraph readOSMSource(StreamSource source) throws IOException,
			XMLStreamException, EmptyInputException, InvalidInputException,
			InvalidGPSCoordinateException, NodeNotInGraphException {
		double minlat = 0, minlon = 0, maxlat = 0.1f, maxlon = 0.1f;
		long timeStarted = System.currentTimeMillis();
		int event;
		MapGraph graph = null;
		
		//HashMap<Integer, GPSCoordinate> nodes = new HashMap<Integer, GPSCoordinate>();

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(source);
		//XMLStreamReader parser = factory.createXMLStreamReader(new FileReader(source)); //Test for OpenJDK?

		int ID = 0, ref = 0, refSafe = 0, index = 0;
		int nrNodes = 0, nrWays = 0;
		double lat = 0;
		double lon = 0;
		boolean wayFlag = false, highway = false, firstFlag = true;// oneway = false
		//boolean allowcar = true, allowaccess = true;
		String name = "";

		ArrayList<ImportEdge> aList = new ArrayList<ImportEdge>();

		StreetType streetType = StreetType.UNKNOWN;

		
		//
		// Bounds (Extraction or Calculation)
		//
		while (true) {
			event = parser.next();
			if (event == XMLStreamConstants.END_DOCUMENT) {
				parser.close();
				graph = new MapGraph(new GPSCoordinate(maxlat, minlon),
						new GPSCoordinate(minlat, maxlon));
				parser = factory.createXMLStreamReader(source);
				break;
			}
			if (event == XMLStreamConstants.START_ELEMENT) {
				
				if (parser.getLocalName().equalsIgnoreCase("bounds")) {
					//Found the Bounds in OSM-File
					minlat = Double.parseDouble(parser.getAttributeValue(
							null, "minlat"));
					maxlat = Double.parseDouble(parser.getAttributeValue(
							null, "maxlat"));
					minlon = Double.parseDouble(parser.getAttributeValue(
							null, "minlon"));
					maxlon = Double.parseDouble(parser.getAttributeValue(
							null, "maxlon"));
					graph = new MapGraph(new GPSCoordinate(maxlat, minlon),
							new GPSCoordinate(minlat, maxlon));
					break;
				} else {
					//No Bounds found -> parse all nodes
					if (parser.getLocalName().equalsIgnoreCase("node")) {
						lat = Double.parseDouble(parser.getAttributeValue(null,
								"lat"));
						lon = Double.parseDouble(parser.getAttributeValue(null,
								"lon"));
						if (firstFlag) {
							firstFlag = false;
							minlat = maxlat = lat;
							minlon = maxlon = lon;
						}

						if (lat > maxlat)
							maxlat = lat;
						if (lat < minlat)
							minlat = lat;
						if (lon > maxlon)
							maxlon = lon;
						if (lon < minlon)
							minlon = lon;

					}
				}
			}
		}
		
		//
		// Import Nodes
		//
		//boolean nodeflag = false;
		
		while (true) {
			event = parser.next();
			if (event == XMLStreamConstants.END_DOCUMENT) {
				parser.close();
				break;
			}

			if (event == XMLStreamConstants.START_ELEMENT) {
				// Node
				if (parser.getLocalName().equalsIgnoreCase("node")) {
					ID = Integer.parseInt(parser.getAttributeValue(null, "id"));
					lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
					lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
					nrNodes++;
					graph.insertNode(ID, new GPSCoordinate(lat, lon));
					
					//nodes.put(ID, new GPSCoordinate(lat, lon));
				}
				
				if (parser.getLocalName().equals("tag")) {
					if (parser.getAttributeValue(null, "k").equalsIgnoreCase("indoor")) {
						if (parser.getAttributeValue(null, "v").equals("yes")) {
							//This is an indoor Node
							graph.getNode(ID).setIndoor(true);
						}
					}
				}

				if (parser.getLocalName().equalsIgnoreCase("way")) {
					break;
				}
			}
		}
		
		//
		// Import Ways
		//		
		while (true) {
			if (event == XMLStreamConstants.END_DOCUMENT) {
				parser.close();
				break;
			}

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:

				// This is a Way
				if (parser.getLocalName().equalsIgnoreCase("way")) {
					ID = Integer.parseInt(parser.getAttributeValue(null,"id"));
					wayFlag = true; 
					index = 0;
					//refSafe = 0;
				}

				if (wayFlag && parser.getLocalName().equalsIgnoreCase("nd")) {
					//I'm a node inside a way
					refSafe = ref;
					ref = Integer.parseInt(parser.getAttributeValue(null,"ref"));

					if (index > 0) {
						aList.add(new ImportEdge(ID, refSafe, ref, 0, null));
					}
					index++;
				}
				if (wayFlag	&& parser.getLocalName().equalsIgnoreCase("tag")) {
					//Concerning the way tags
					
					//"highway" means we can use it
					if (parser.getAttributeValue(null, "k").equalsIgnoreCase("highway")) {
						highway = true;
						streetType = getStreetType(parser.getAttributeValue(null, "v"));
					}
					if (parser.getAttributeValue(null, "k").equalsIgnoreCase("indoor")) {
						highway = true;
						streetType = StreetType.UNKNOWN;
					}

					if (parser.getAttributeValue(null, "k").equalsIgnoreCase("name")) {
						name = parser.getAttributeValue(null, "v");
					}
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				// Way in Graphenbib
				if (wayFlag	&& parser.getLocalName().equalsIgnoreCase("way")) {
					wayFlag = false;
					
					//
					// This is the filter for ways special ways
					//
					//if (!highway || streetType == StreetType.UNKNOWN || !allowcar || !allowaccess) { // Nicht
					if (!highway) {
						//oneway = false;
						//allowcar = true;
						//allowaccess = true;
						streetType = StreetType.UNKNOWN;
						aList.clear();
						name = "";
						break;
					}
					
					//Insert those ways into the graph
					Iterator<ImportEdge> itr = aList.iterator();
					MapNode node;
					ImportEdge edge;
					int length;
					while (itr.hasNext()) {
						edge = itr.next();
						node = graph.getNode(edge.getStartNode());
						if (node != null && node.getGPS() != null) {
							node = graph.getNode(edge.getEndNode());
							if (node != null) {
								if (node.getGPS() != null) {
									length = graph.getNode(edge.getStartNode()).getGPS().distanceTo(graph.getNode(edge.getEndNode()).getGPS());
									// graph.insertEdge(edge.getStartNode(),
									// edge.getEndNode(),
									// edge.getWayID(),length, streetType);
									//if (oneway) {
									//	graph.insertOneWay(edge.getStartNode(),edge.getEndNode(),edge.getWayID(), length,streetType, name);
										//graph.insertEdgeBothDirections(edge.getStartNode(),edge.getEndNode(),edge.getWayID(), length,streetType, name);
									//	nrWays++;
									//} else {
										// graph.insertEdge(edge.getEndNode(),edge.getStartNode()
										// , edge.getWayID(),length,
										// streetType);
										graph.insertEdgeBothDirections(edge.getStartNode(),edge.getEndNode(),edge.getWayID(), length,streetType, name);
										nrWays += 2;
									//}
								} else {
									//if (oneway) {
									//	nrWays++; 
									//	graph.insertOneWay(edge.getStartNode(),edge.getEndNode(),edge.getWayID(),	Config.initialTemporaryLengthForEdges,streetType, name);
									//} else {
										nrWays++; nrWays++;
										graph.insertEdgeBothDirections(edge.getStartNode(),edge.getEndNode(),edge.getWayID(),	Config.initialTemporaryLengthForEdges,streetType, name);
									//}
								}
							} else {
								nrNodes++;
								Logger.getInstance().log("OSMReader", "NoGPSNode: "+edge.getEndNode());
								graph.insertNodeWithoutGPS(edge.getEndNode());
								
								//if (oneway) {
								//	nrWays++;
								//	graph.insertOneWay(edge.getStartNode(),edge.getEndNode(),edge.getWayID(),Config.initialTemporaryLengthForEdges,streetType, name);
								//} else {
									nrWays++; nrWays++;
									graph.insertEdgeBothDirections(edge.getStartNode(),edge.getEndNode(),edge.getWayID(),Config.initialTemporaryLengthForEdges,streetType, name);
								//}

							}
						}
					}
					//oneway = false;
					highway = false;
					streetType = StreetType.UNKNOWN;
					aList.clear();
					name = "";
					//allowcar = true;
					//allowaccess = true;
				}
			}

			event = parser.next();
		}
		
		graph.deleteIsolatedNodes();
		long timeNeeded = System.currentTimeMillis() - timeStarted;
		Logger.getInstance().log(
				"OSMImporter",
				"Done! Imported Data: 	Nodes: " + nrNodes
						+ "	        Nodes after deleting isolated Nodes: "
						+ graph.getSize() + "        	Ways: " + nrWays
						+ "	  Total Time: " + timeNeeded
						+ " msec.");
		
		return graph;
	}
	
	
	

	private static StreetType getStreetType(String v) {
		// MOTORWAY,TRUNK,PRIMARY,SECONDARY,TERTIARY,RESIDENTIAL,LIVING_STREET,ROAD
		if (v.equalsIgnoreCase("motorway"))
			return StreetType.MOTORWAY;
		if (v.equalsIgnoreCase("motorway_link"))
			return StreetType.MOTORWAY;
		if (v.equalsIgnoreCase("trunk"))
			return StreetType.TRUNK;
		if (v.equalsIgnoreCase("trunk_link"))
			return StreetType.TRUNK;
		if (v.equalsIgnoreCase("primary"))
			return StreetType.PRIMARY;
		if (v.equalsIgnoreCase("primary_link"))
			return StreetType.PRIMARY;
		if (v.equalsIgnoreCase("secondary"))
			return StreetType.SECONDARY;
		if (v.equalsIgnoreCase("secondary_link"))
			return StreetType.SECONDARY;
		if (v.equalsIgnoreCase("tertiary"))
			return StreetType.TERTIARY;
		if (v.equalsIgnoreCase("tertiary_link"))
			return StreetType.TERTIARY;
		if (v.equalsIgnoreCase("residential"))
			return StreetType.RESIDENTIAL;
		if (v.equalsIgnoreCase("living_street"))
			return StreetType.LIVING_STREET;
		if (v.equalsIgnoreCase("road") || v.equalsIgnoreCase("unclassified"))
			return StreetType.ROAD;
		return StreetType.UNKNOWN;

	}
}
