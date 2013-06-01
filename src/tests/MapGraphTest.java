/**
 * 
 */
package tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import graphenbib.*;

/**
 * @author arno
 *	Test the MapGraph
 */
public class MapGraphTest {

	/**
	 * Definitions
	 */
	private static int[] laengen = new int[9];
	
	private static MapNode[] nodes = new MapNode[12];
	//private static MapEdge[] edges = new MapEdge[9];
	private static GPSCoordinate[] koord = new GPSCoordinate[12];
	
	private static MapGraph testGraph;
	private static GPSCoordinate[] graphKoord = new GPSCoordinate[4];
	
	private static MapGraph failGraph;
	//private static MapGraph failGraphRound;
	
	/*private static GPSCoordinate failKoord1;
	private static GPSCoordinate failKoord2;
	
	private static GPSCoordinate failKoordRound1;
	private static GPSCoordinate failKoordRound2; */
	

	private static float randomFloat(float min, float max) {
		return (float) (Math.random() * (max - min + 1)) + min;
	}
	

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		int i = 0;

		for (i=0;i<11;i++) {

			koord[i] = new GPSCoordinate(randomFloat(-90,89),randomFloat(-180,179));
		}
		
		koord[11] = new GPSCoordinate(randomFloat(-9,9),randomFloat(-10,10));
		
		
		//Nodes
		for (i=0;i<12;i++) {
			nodes[i] = new MapNode(i,koord[i]);
		}
		
		
		
		//Laengen
		for (i=0;i<9;i++) {
			laengen[i] = (int)Math.random();
		}
		/*
		//Edges
		edges[0] = new MapEdge(nodes[0],nodes[5],0,laengen[0],StreetType.LIVING_STREET);
		edges[1] = new MapEdge(nodes[5],nodes[1],1,laengen[1],StreetType.PRIMARY);
		edges[2] = new MapEdge(nodes[6],nodes[2],2,laengen[2],StreetType.MOTORWAY);
		edges[3] = new MapEdge(nodes[2],nodes[6],3,laengen[3],StreetType.RESIDENTIAL);
		edges[4] = new MapEdge(nodes[3],nodes[7],4,laengen[4],StreetType.ROAD);
		edges[5] = new MapEdge(nodes[3],nodes[8],5,laengen[5],StreetType.SECONDARY);
		edges[6] = new MapEdge(nodes[8],nodes[4],6,laengen[6],StreetType.TERTIARY);
		edges[7] = new MapEdge(nodes[9],nodes[4],7,laengen[7],StreetType.TRUNK);
		edges[8] = new MapEdge(nodes[0],nodes[5],8,laengen[8],StreetType.LIVING_STREET);
		*/
	
		//Koordinaten fuer die Maps
		graphKoord[0] = new GPSCoordinate(90,-180);
		graphKoord[1] = new GPSCoordinate(-90,180);
		graphKoord[2] = new GPSCoordinate(50,-150);
		graphKoord[3] = new GPSCoordinate(-50,150);
		
		
	}

	/**
	 * Test method for {@link graphenbib.MapGraph#MapGraph(graphenbib.GPSCoordinate, graphenbib.GPSCoordinate)}.
	 */
	@Test
	public void testMapGraph() {
		//shpould work
		try {
			testGraph = new MapGraph(graphKoord[0],graphKoord[1]);
		} catch (Exception e) {
			fail("Failed creating Graph: "+e.getLocalizedMessage());
		}
		
		//Should throw an exceptiom
		boolean fehler = false;
		try {
			@SuppressWarnings("unused") 
			MapGraph graphFailTest = new MapGraph(graphKoord[1],graphKoord[0]);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler == false) {
			fail("Failtest1: No exception thrown..");
		}
		
		//Should throw exception
		fehler = false;
		try {
			/*GPSCoordinate failKoord1 = new GPSCoordinate(-200,100);
			GPSCoordinate failKoord2 = new GPSCoordinate(200,-100);*/
			@SuppressWarnings("unused")
			MapGraph graphFailTest = new MapGraph(null,null);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler == false) {
			fail("FailTest2: No exceptiopn thrown");
		}
		
		//construct failgraph
		try {
			failGraph = new MapGraph(graphKoord[2],graphKoord[3]);
		} catch (Exception e) {
			fail("Failed in construcitng: "+e.getLocalizedMessage());
		}
		
		//Execute Tests manually because guarenteed ordering of tests needed
		this.testInsertNode();
		this.testInsertEdgeIntIntIntFloatStreetType();
		this.testGetNode();
		this.testDeleteIsolatedNodes();
		
	}

	/**
	 * Test method for {@link graphenbib.MapGraph#insertNode(int,graphenbib.GPSCoordinate)}.
	 */
	//@Test
	public void testInsertNode() {
		//Test inserting valid data
		try {
			for (int i=0;i<11;i++) {
				testGraph.insertNode(i, koord[i]);
			}
		} catch (Exception e) {
			fail("Failed inserting valid nodes: "+e.getLocalizedMessage());
		}


		
		boolean fehler;
		//Test invalid data
		fehler = false;
		try {
			failGraph.insertNode(0,null);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler == false) {
			fail("invalid node inserted");
		}
		

		fehler = false;
		GPSCoordinate failGPS = null;
		try {
			failGPS = new GPSCoordinate(90,180);
		} catch (Exception e) {
			fail("Failed creating GPS");
		}
		
		try {
			failGraph.insertNode(5,failGPS);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler) {
			fail("Node not inserted");
		}
		
		
		try {
			failGraph.insertNode(11,koord[11]);
		} catch (Exception e) {
			fail("Failed inserting node: "+e.getLocalizedMessage());
		}
		

		fehler = false;
		try {
			failGraph.insertNode(11,koord[11]);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler == false)
			fail("Inserted a node twoo times");
		
	}



	/**
	 * Test method for {@link graphenbib.MapGraph#insertEdge(int, int, int, int, graphenbib.StreetType)}.
	 */
	//@Test
	public void testInsertEdgeIntIntIntFloatStreetType() {
		try {
			testGraph.insertEdge(0, 5, 0, laengen[0], StreetType.LIVING_STREET);
			testGraph.insertEdge(5, 1, 1, laengen[1], StreetType.PRIMARY);
			testGraph.insertEdge(6, 2, 2, laengen[2], StreetType.MOTORWAY);
			testGraph.insertEdge(2, 6, 3, laengen[3], StreetType.RESIDENTIAL);
			testGraph.insertEdge(3, 7, 4, laengen[4], StreetType.ROAD);
			testGraph.insertEdge(3, 8, 5, laengen[5], StreetType.SECONDARY);
			testGraph.insertEdge(8, 4, 6, laengen[6], StreetType.TERTIARY);
			testGraph.insertEdge(9, 4, 7, laengen[7], StreetType.TRUNK);
			testGraph.insertEdge(0, 5, 8, laengen[8], StreetType.LIVING_STREET);
		} catch (Exception e) {
			fail("inserting (Param: int,int,int,float, StreetType) not working: "+e.getLocalizedMessage());
		}
		
		boolean fehler;

		fehler = false;
		try {
			failGraph.insertEdge(11, 1, 9, laengen[0], StreetType.LIVING_STREET);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler == false) {
			fail("wrong node inserted");
		}
		

		fehler = false;
		try {
			failGraph.insertEdge(11, 1, 4, laengen[0], StreetType.LIVING_STREET);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler == false) {
			fail("wrong edge inserted");
		}
		

		fehler = false;
		try {
			failGraph.insertEdge(11, 11, 10, -5, StreetType.LIVING_STREET);
		} catch (Exception e) {
			fehler = true;
		}
		if (fehler == false) {
			fail("invalid edge inserted");
		}
	}

	/**
	 * Test method for {@link graphenbib.MapGraph#getNode(int)}.
	 */
	//@Test
	public void testGetNode() {
		//boolean[] korrekt1 = new boolean[11];
		boolean[] korrekt2 = new boolean[11];
		
		try {
			for (int i=0;i<10;i++) {
				korrekt2[i] = (testGraph.getNode(i).getUID() == i);
				if (korrekt2[i] == false) {
					fail("Data not correct :"+i);
				}
			}
		} catch (NullPointerException e) {
			fail("did'nt get Node via getNode: "+e.getLocalizedMessage());
		}
				
		boolean[] korrekt = new boolean[11];
		
		MapEdge[] liste0 = testGraph.getNode(0).getIncomingEdges();
		MapEdge[] liste1 = testGraph.getNode(1).getIncomingEdges();
		MapEdge[] liste2 = testGraph.getNode(2).getIncomingEdges();
		MapEdge[] liste3 = testGraph.getNode(3).getIncomingEdges();
		MapEdge[] liste4 = testGraph.getNode(4).getIncomingEdges();
		MapEdge[] liste5 = testGraph.getNode(5).getIncomingEdges();
		MapEdge[] liste6 = testGraph.getNode(6).getIncomingEdges();
		MapEdge[] liste7 = testGraph.getNode(7).getIncomingEdges();
		MapEdge[] liste8 = testGraph.getNode(8).getIncomingEdges();
		MapEdge[] liste9 = testGraph.getNode(9).getIncomingEdges();
		MapEdge[] liste10 = testGraph.getNode(10).getIncomingEdges();
		
		
		korrekt[0] = (liste0.length == 0);
		korrekt[3] = (liste3.length == 0);
		korrekt[9] = (liste9.length == 0);
		korrekt[10] = (liste10.length == 0);
		
		
		korrekt[1] = ((liste1.length==1) && (liste1[0].getUID() == 1));
		
		korrekt[2] = ((liste2.length==1) && (liste2[0].getUID() == 2));
		 
		korrekt[4] = ((liste4.length==2) && ((liste4[0].getUID() == 6) || (liste4[0].getUID() == 7)) &&  ((liste4[1].getUID() == 6) || (liste4[1].getUID() == 7)));
		
		korrekt[5] = ((liste5.length==2) && ((liste5[0].getUID() == 0) || (liste5[0].getUID() == 8)) &&  ((liste5[1].getUID() == 0) || (liste5[1].getUID() == 8)));
		
		korrekt[6] = ((liste6.length==1) && (liste6[0].getUID() == 3));
		korrekt[7] = ((liste7.length==1) && (liste7[0].getUID() == 4));
		korrekt[8] = ((liste8.length==1) && (liste8[0].getUID() == 5));
		
		for (int i=0;i<11;i++) {
			if (korrekt[i] == false) fail("Failed during IncomingEdges-Test (2) Number "+i);
		}
		
		liste4 = null;
		liste4 = testGraph.getNode(4).getIncomingEdges();
		if (liste4[0] == null) { fail("Incominglists are inconsistent"); }
		
		
		liste0 = testGraph.getNode(0).getOutgoingEdges();
		liste1 = testGraph.getNode(1).getOutgoingEdges();
		liste2 = testGraph.getNode(2).getOutgoingEdges();
		liste3 = testGraph.getNode(3).getOutgoingEdges();
		liste4 = testGraph.getNode(4).getOutgoingEdges();
		liste5 = testGraph.getNode(5).getOutgoingEdges();
		liste6 = testGraph.getNode(6).getOutgoingEdges();
		liste7 = testGraph.getNode(7).getOutgoingEdges();
		liste8 = testGraph.getNode(8).getOutgoingEdges();
		liste9 = testGraph.getNode(9).getOutgoingEdges();
		liste10 = testGraph.getNode(10).getOutgoingEdges();
		
		korrekt[1] = (liste1.length == 0);
		korrekt[4] = (liste4.length == 0);
		korrekt[7] = (liste7.length == 0);
		korrekt[10] = (liste10.length == 0);
			
		korrekt[0] = ((liste0.length==2) && ((liste0[0].getUID() == 0) || (liste0[0].getUID() == 8)) &&  ((liste0[1].getUID() == 0) || (liste0[1].getUID() == 8)));
		korrekt[2] = ((liste2.length==1) &&  (liste2[0].getUID() == 3));
		korrekt[3] = ((liste3.length==2) && ((liste3[0].getUID() == 4) || (liste3[0].getUID() == 5)) &&  ((liste3[1].getUID() == 4) || (liste3[1].getUID() == 5)));
		korrekt[5] = ((liste5.length==1) &&  (liste5[0].getUID() == 1));
		korrekt[6] = ((liste6.length==1) &&  (liste6[0].getUID() == 2));
		korrekt[8] = ((liste8.length==1) &&  (liste8[0].getUID() == 6));
		korrekt[9] = ((liste9.length==1) &&  (liste9[0].getUID() == 7));
		
		for (int i=0;i<11;i++) {
			if (korrekt[i] == false) fail("Failed during OutgingEdges-Test (2) "+i);
		}
		
		liste3 = null;
		liste3 = testGraph.getNode(3).getOutgoingEdges();
		if (liste3[0] == null) { fail("Outgoinglist incosistent!"); }
		
	}
	
	/**
	 * Test method for {@link graphenbib.MapGraph#deleteIsolatedNodes()}. 
	 */
	//@Test
	public void testDeleteIsolatedNodes() {
		try {
			testGraph.insertNode(11, new GPSCoordinate(5,5));
		} catch (Exception e) {
			fail("Fehler beim Hinzufuegen einer Node: "+e.getLocalizedMessage());
		}
		
		//try {
			testGraph.deleteIsolatedNodes();
		//} catch (Exception e) {
		//}
		
		if (testGraph.getNode(0) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(1) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(2) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(3) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(4) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(5) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(6) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(7) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(8) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(9) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(10) != null)
			fail("Node 10 wasn't delted");
		if (testGraph.getNode(11) != null)
			fail("Node 11 wasn't deleted");
		

		try {
			testGraph.deleteIsolatedNodes();
		} catch (Exception e) {
			fail("Fail during DIN-method (second call): "+e.getLocalizedMessage());
		}
		


		if (testGraph.getNode(0) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(1) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(2) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(3) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(4) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(5) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(6) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(7) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(8) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(9) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(10) != null)
			fail("Node 10 wasn't deleted");
		if (testGraph.getNode(11) != null)
			fail("Node 11 wasn't deleted");

		

		try {
			testGraph.insertNode(100, new GPSCoordinate(5,5));
		} catch (Exception e) {
			fail("Failed inserting node: "+e.getLocalizedMessage());
		}
		try {
			testGraph.deleteIsolatedNodes();
		} catch (Exception e) {
			fail("Failed during DIN-method: "+e.getLocalizedMessage());
		}
		
		try {
			testGraph.deleteIsolatedNodes();
		} catch (Exception e) {
			fail("Failed during DIN-method: "+e.getLocalizedMessage());
		}

		
		if (testGraph.getNode(0) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(1) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(2) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(3) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(4) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(5) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(6) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(7) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(8) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(9) == null)
			fail("deleted wrong Node");
		if (testGraph.getNode(10) != null)
			fail("Node 10 wasn't deleted");
		if (testGraph.getNode(11) != null)
			fail("Node 11 wasn't deleted");
		if (testGraph.getNode(100) != null)
			fail("Node 100 wasn't deleted");
		
	}
	
}
