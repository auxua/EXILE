package tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.BeforeClass;
import graphenbib.*;

/**
 * 
 * @author arno
 *	Test the MapNode-interfaces 
 */
public class MapNodeTest {

	/**
	 * Definitions
	 */
	
	private static MapNode[] nodes = new MapNode[11];
	private static MapEdge[] edges = new MapEdge[9];
	//private static String[] namen = new String[11];
	private static GPSCoordinate[] koord = new GPSCoordinate[11];
	
	private static int randomint(int min, int max) {
		return (int) (Math.random() * (max - min + 1)) + min;
	}
	

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		int i = 0;

		for (i=0;i<11;i++) {
			try {
				koord[i] = new GPSCoordinate(randomint(-90,90),randomint(-180,180));
			} catch (Exception e) {
				fail("Fehler bei ANlegen von GPS-Koordinaten (zufall)"+e.getLocalizedMessage());
			}
		}
		//Nodes
		for (i=0;i<11;i++) {
			nodes[i] = new MapNode(i,koord[i]);
		}
		//Edges
		edges[0] = new MapEdge(nodes[0],nodes[5],0,(int)Math.random(),StreetType.LIVING_STREET);
		edges[1] = new MapEdge(nodes[5],nodes[1],1,(int)Math.random(),StreetType.PRIMARY);
		edges[2] = new MapEdge(nodes[6],nodes[2],2,(int)Math.random(),StreetType.MOTORWAY);
		edges[3] = new MapEdge(nodes[2],nodes[6],3,(int)Math.random(),StreetType.RESIDENTIAL);
		edges[4] = new MapEdge(nodes[3],nodes[7],4,(int)Math.random(),StreetType.ROAD);
		edges[5] = new MapEdge(nodes[3],nodes[8],5,(int)Math.random(),StreetType.SECONDARY);
		edges[6] = new MapEdge(nodes[8],nodes[4],6,(int)Math.random(),StreetType.TERTIARY);
		edges[7] = new MapEdge(nodes[9],nodes[4],7,(int)Math.random(),StreetType.TRUNK);
		edges[8] = new MapEdge(nodes[0],nodes[5],8,(int)Math.random(),StreetType.LIVING_STREET);
		//Fuege diese hinzu
		nodes[0].addOutgoingEdge(edges[0]);
		nodes[5].addOutgoingEdge(edges[1]);
		nodes[6].addOutgoingEdge(edges[2]);
		nodes[2].addOutgoingEdge(edges[3]);
		nodes[3].addOutgoingEdge(edges[4]);
		nodes[3].addOutgoingEdge(edges[5]);
		nodes[8].addOutgoingEdge(edges[6]);
		nodes[9].addOutgoingEdge(edges[7]);
		nodes[0].addOutgoingEdge(edges[8]);
				
		nodes[5].addIncomingEdge(edges[0]);
		nodes[1].addIncomingEdge(edges[1]);
		nodes[2].addIncomingEdge(edges[2]);
		nodes[6].addIncomingEdge(edges[3]);
		nodes[7].addIncomingEdge(edges[4]);
		nodes[8].addIncomingEdge(edges[5]);
		nodes[4].addIncomingEdge(edges[6]);
		nodes[4].addIncomingEdge(edges[7]);
		nodes[5].addIncomingEdge(edges[8]);
		
		
	}

	/**
	 * Test method for {@link MapNode#getIncomingEdges()}.
	 */
	@Test
	public void testGetIncomingEdges() {


		boolean[] korrekt = new boolean[11];

		MapEdge[] liste0 = nodes[0].getIncomingEdges();
		MapEdge[] liste1 = nodes[1].getIncomingEdges();
		MapEdge[] liste2 = nodes[2].getIncomingEdges();
		MapEdge[] liste3 = nodes[3].getIncomingEdges();
		MapEdge[] liste4 = nodes[4].getIncomingEdges();
		MapEdge[] liste5 = nodes[5].getIncomingEdges();
		MapEdge[] liste6 = nodes[6].getIncomingEdges();
		MapEdge[] liste7 = nodes[7].getIncomingEdges();
		MapEdge[] liste8 = nodes[8].getIncomingEdges();
		MapEdge[] liste9 = nodes[9].getIncomingEdges();
		MapEdge[] liste10 = nodes[10].getIncomingEdges();
		

		korrekt[0] = (liste0.length == 0);
		korrekt[3] = (liste3.length == 0);
		korrekt[9] = (liste9.length == 0);
		korrekt[10] = (liste10.length == 0);

		//Node 1 - only 1
		korrekt[1] = ((liste1.length==1) && (liste1[0] == edges[1]));
		//Node 2 - only 2
		korrekt[2] = ((liste2.length==1) && (liste2[0] == edges[2]));
		//Node 4 - only6,7 
		korrekt[4] = ((liste4.length==2) && ((liste4[0] == edges[6]) || (liste4[0] == edges[7])) && ((liste4[1] == edges[6]) || (liste4[1] == edges[7])));
		//Node 5 - only 0,8
		korrekt[5] = ((liste4.length==2) && ((liste5[0] == edges[0]) || (liste5[0] == edges[8])) && ((liste5[1] == edges[0]) || (liste5[1] == edges[8])));
		//Node 6 - only 3
		korrekt[6] = ((liste6.length==1) && (liste6[0] == edges[3]));
		//Node 7 - only 4
		korrekt[7] = ((liste7.length==1) && (liste7[0] == edges[4]));
		//Node 8 - only 5
		korrekt[8] = ((liste8.length==1) && (liste8[0] == edges[5]));
		
		for (int i=0;i<9;i++) {
			if (korrekt[i] == false) fail("Fail during IncomingEdges-Test "+i);
		}
	}

	/**
	 * Test method for {@link MapNode#getOutgoingEdges()}.
	 */
	@Test
	public void testGetOutgoingEdges() {

		boolean[] korrekt = new boolean[11];

		MapEdge[] liste0 = nodes[0].getOutgoingEdges();
		MapEdge[] liste1 = nodes[1].getOutgoingEdges();
		MapEdge[] liste2 = nodes[2].getOutgoingEdges();
		MapEdge[] liste3 = nodes[3].getOutgoingEdges();
		MapEdge[] liste4 = nodes[4].getOutgoingEdges();
		MapEdge[] liste5 = nodes[5].getOutgoingEdges();
		MapEdge[] liste6 = nodes[6].getOutgoingEdges();
		MapEdge[] liste7 = nodes[7].getOutgoingEdges();
		MapEdge[] liste8 = nodes[8].getOutgoingEdges();
		MapEdge[] liste9 = nodes[9].getOutgoingEdges();
		MapEdge[] liste10 = nodes[10].getOutgoingEdges();
		
		/*
		for(int i=0; i<=10; i++) 
		{
			logger.log("MapNodeTest",nodes[i]);
		}
		*/

		korrekt[1] = (liste1.length == 0);
		korrekt[4] = (liste4.length == 0);
		korrekt[7] = (liste7.length == 0);
		korrekt[10] = (liste10.length == 0);
		

		//Node 0 - only 0,8
		korrekt[0] = ((liste0.length==2) && ((liste0[0] == edges[0]) || (liste0[0] == edges[8])) && ((liste0[1] == edges[0]) || (liste0[1] == edges[8])));
		//Node 2 - only 3
		korrekt[2] = ((liste2.length==1) && (liste2[0] == edges[3]));
		//Node 3 - only 4,5
		korrekt[3] = ((liste3.length==2) && ((liste3[0] == edges[4]) || (liste3[0] == edges[5])) && ((liste3[1] == edges[4]) || (liste3[1] == edges[5])));
		//Node 5 - only 1
		korrekt[5] = ((liste5.length==1) && (liste5[0] == edges[1]));
		//Node 6 - only 2
		korrekt[6] = ((liste6.length==1) && (liste6[0] == edges[2]));
		//Node 8 - only 6
		korrekt[8] = ((liste8.length==1) && (liste8[0] == edges[6]));
		//Node 9 - only 7
		korrekt[9] = ((liste9.length==1) && (liste9[0] == edges[7]));
		
		for (int i=0;i<9;i++) {
			if (korrekt[i] == false) fail("Fail during OutgingEdges-Test "+i);
		}
		
	}
	
}
