package tests;

import static org.junit.Assert.*;

import java.io.File;


import org.junit.Test;

import graphenbib.*;
import importer.OSMReader;

/**
 * 
 * @author arno
 *	Test the Importer by using several prepared testFiles.
 */
public class ImporterTest {

	@Test
	public void test() {
		
		MapGraph testGraph = null;
	
		/*
		 * Test 0.1
		 * two nodes, one edge
		 */
	
		//starting
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/test0.1.osm"));
		}
		catch(Exception e) {
			fail("Test 0.1: Fail during reading: "+e.getLocalizedMessage());
		}
		
		//testing
		if (testGraph == null)
			fail("Test 0.1: Null Pointer instead of graph-object");
		
		
		
		//test correctness of nodes.
		
		MapNode node1 = testGraph.getNode(1);
		MapNode node2 = testGraph.getNode(2);
		MapNode node3 = testGraph.getNode(500);
		
		//Test node3 - Hopefully did not mix up ID and UID? 
		if (node3 != null)
			fail("Test 0.1: A node with UID = 500 exists - not in File provided!");
		//Nullpointers?
		if (node1 == null)
			fail("Test 0.1: not all nodes do exist! Missing UID=1");
		if (node2 == null)
			fail("Test 0.1: not all nodes do exist! Missing UID=2");
		
		//Test the lists of edges
		if (node1.getIncomingEdges().length == 0)
			fail("Test 0.1: Node 1 has no incoming edge! (But should have one)");
		if (node2.getOutgoingEdges().length == 0)
			fail("Test 0.1: Node 2 has no outgoing edge! (But should have one");
		
		//Test the edge
		if (!(node1.getOutgoingEdges()[0].equals(node2.getIncomingEdges()[0])))
			fail("Test 0.1: several edges detected where only one should exist!");
			
		//Teste Streettype der Kante
		if (node1.getOutgoingEdges()[0].getType() != StreetType.TRUNK)
			fail("Test 0.1: StreetType was not correctly imported");
			
		
		
		
		/*
		 * Test 0.2
		 * Similar to Test 0.1
		 */
		
		//start Test
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/test0.2.osm"));
		}
		catch(Exception e) {
			fail("Test 0.2: Failes reading File: "+e.getLocalizedMessage());
		}
		
		//Checking
		
		//Null-Pointer
		if (testGraph == null)
			fail("Test 0.2: Null Pointer detected instead of graph object");
		
		//Testing Nodes
		
		node1 = testGraph.getNode(1);
		node2 = testGraph.getNode(2);
		
		//Checking lists of edges
		if (node2.getIncomingEdges().length == 0)
			fail("Test 0.2: Node 2 misses an edge");
		if (node1.getOutgoingEdges().length == 0)
			fail("Test 0.2: Node 1 misses an edge");
		
		//Checking correct/identical Edge
		if (!(node2.getOutgoingEdges()[0].equals(node1.getIncomingEdges()[0])))
			fail("Test 0.2: Several Edges detected, where only one should be!");
		
		
		/*
		 * Test 0.3
		 * Similar to Tet 0.2
		 */
		
		//starting
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/test0.3.osm"));
			
		}
		catch(Exception e) {
			fail("Test 0.3: Failed during reading: "+e.getLocalizedMessage());
		}
		
		//Checking
		
		//Null-Pointer
		if (testGraph == null)
			fail("Test 0.3: Null Pointer detected instead of graph object");
		
		//Testing nodes
		
		node1 = testGraph.getNode(1);
		node2 = testGraph.getNode(2);
			
		//Checking lists
		if (node2.getIncomingEdges().length != 1)
			fail("Test 0.3: Node 2 has wrong number of edges");
		if (node1.getOutgoingEdges().length != 1)
			fail("Test 0.3: Node 1 has wrong number of edges");
		
		//Checking edges
		if (node1.getOutgoingEdges()[0].getNodeEnd() != node2 && node1.getOutgoingEdges()[0].getNodeStart() != node2)
			fail("Test 0.3: outgoing Edge of Node 1 has wrong target");
		if (node2.getOutgoingEdges()[0].getNodeEnd() != node1 && node2.getOutgoingEdges()[0].getNodeStart() != node1)
			fail("Test 0.3: outgoing edge of Node 2 has wrong target");
		if (node1.getIncomingEdges()[0].getNodeStart() != node2 && node1.getIncomingEdges()[0].getNodeEnd() != node2)
			fail("Test 0.3: incoming edge of Node 1 has wrng start");
		if (node2.getIncomingEdges()[0].getNodeStart() != node1 && node2.getIncomingEdges()[0].getNodeEnd() != node1)
			fail("Test 0.3: incoming edge of node 2 has wrong start");
			

		/*
		 * Test 1
		 * Invalid XML Syntax!
		 */
		
		//starte Test - erwarte Fehler
		boolean fehler = false;
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/test1.osm"));

		}
		catch(Exception e) {
			fehler = true;
		}
		
		if (fehler == false)
			fail("Test 1: No Exception on damaged input thrown!");
		
		
		/*
		 * Test 2
		 * Double Node - should break
		 */
		
		//starte Test - erwarte Fehler
		fehler = false;
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/test2.osm"));
			
		}
		catch(Exception e) {
			fehler = true;
		}
		if (fehler == false)
			fail("Test 2: Two nodes with the sasme UID have been read");
		
//		-> Redefined Graphenbib -> No need for these tests any more		
//		/*
//		 * Test 3 
//		 * Should break because of invalid GPS
//		 */
//			/*
//		
//		fehler = false;
//		try {
//			osm_imp = new OSMImporter("testFiles/test3.osm","tmp");
//			testGraph = osm_imp.getTile(osm_imp.getMapCenter(), 0);
//		}
//		catch(Exception e) {
//			fehler = true;
//		}
//		
//		if (fehler == false)
//			fail("Test 3: Imported Graph with invalid GPS");
//			*/
//		
//		/*
//		 * Test 4
//		 * Should break because of invalid GPS (out of bounds)
//		 */
//			/*
//		fehler = false;
//		try {
//			osm_imp = new OSMImporter("testFiles/test4.osm","tmp");
//			testGraph = osm_imp.getTile(osm_imp.getMapCenter(), 0);
//		}
//		catch(Exception e) {
//			fehler = true;
//		}
//		
//		if (fehler == false)
//			fail("Test 4: Read out-of-bounds Nodes...");
//			*/
		
			
		/*
		 * Test 5
		 * Unknown StreetTypes should not break the import!
		 */
			
		//starte Test
		try {
			OSMReader.readOSMFile(new File("testFiles/test5.osm"));
		}
		catch(Exception e) {
			fail("Test 5: Broke import beacuase of unknown streetTypes...");
		}
			
		
		
		/*
		 * Test 6
		 * Should break because of invalid way (non-existing end- node)
		 */
			
		fehler = false;
		try {
			OSMReader.readOSMFile(new File ("testFiles/test6.osm","tmp"));
		}
		catch(Exception e) {
			fehler = true;
		}
		if (fehler == false)
			fail("Test 6: invalid edge was inserted!");
			
		
		/*
		 * Test 7
		 * Bounds-based GPScoordinates for nodes
		 */
			
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/test7.osm"));
			
		}
		catch(Exception e) {
			fail("Test 7: Fail during import of correct GPS coordinates: "+e.getLocalizedMessage());
		}
		

		
		/*
		 * TestGraph
		 * predefined Graph
		 */
		
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/testGraph.osm"));
			
		}
		catch(Exception e) {
			fail("TestGraph: Failed importing: "+e.getLocalizedMessage());
		}
		
		
		boolean[] korrekt = new boolean[12];
		
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
		
		
		
		korrekt[1] = ((liste1.length == 1) && (liste1[0].getUID() == 8) || (liste1[0].getUID() == 10));
		korrekt[4] = ((liste4.length == 2) && ((liste4[0].getUID() == 4) || (liste4[0].getUID() == 5)) &&  ((liste4[1].getUID() == 4) || (liste4[1].getUID() == 5)));
		korrekt[10] = ((liste10.length == 1) && (liste10[0].getUID() == 7));
		
		
		//Node 2 - only edge 1
		korrekt[2] = ((liste2.length==1) && (liste2[0].getUID() == 1));
		//Node 3 - only edge 23
		korrekt[3] = ((liste3.length==1) && (liste3[0].getUID() == 23));
		//Node 5 - only edges 6,7 
		korrekt[5] = ((liste5.length==2) && ((liste5[0].getUID() == 6) || (liste5[0].getUID() == 7)) &&  ((liste5[1].getUID() == 6) || (liste5[1].getUID() == 7)));
		//Node 6 - only edges 10,8
		korrekt[6] = ((liste6.length==3) && ((liste6[0].getUID() == 10) || (liste6[0].getUID() == 8) || (liste6[0].getUID() == 1)) &&  ((liste6[1].getUID() == 10) || (liste6[1].getUID() == 8) || (liste6[1].getUID() == 1)) && ((liste6[2].getUID() == 10) || (liste6[2].getUID() == 8) || (liste6[2].getUID() == 1)));
		//Node 7 - only edges 23
		korrekt[7] = ((liste7.length==1) && (liste7[0].getUID() == 23));
		//Node 8 - only edge 4
		korrekt[8] = ((liste8.length==1) && (liste8[0].getUID() == 4));
		//Node 9 - only edge 5
		korrekt[9] = ((liste9.length==2) && ((liste9[0].getUID() == 5) || (liste9[0].getUID() == 6)) &&  ((liste9[1].getUID() == 5) || (liste9[1].getUID() == 6)));
		
		//for (int i=1;i<12;i++) {
		for (int i=1;i<11;i++) {
			if (korrekt[i] == false) {
				
				fail("testGraph: Failed IncomingEdges-Test Number "+i);
			}
		}
		
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
		
		
		korrekt[1] = ((liste1.length == 1) && (liste1[0].getUID() == 8) || (liste1[0].getUID() == 10));
		korrekt[4] = ((liste4.length == 2) && ((liste4[0].getUID() == 4) || (liste4[0].getUID() == 5)) &&  ((liste4[1].getUID() == 4) || (liste4[1].getUID() == 5)));
		korrekt[10] = ((liste10.length == 1) && (liste10[0].getUID() == 7));
		
		
		korrekt[2] = ((liste2.length==1) && (liste2[0].getUID() == 1));
		//Node 3 - only 23
		korrekt[3] = ((liste3.length==1) && (liste3[0].getUID() == 23));
		//Node 5 - only 6,7 
		korrekt[5] = ((liste5.length==2) && ((liste5[0].getUID() == 6) || (liste5[0].getUID() == 7)) &&  ((liste5[1].getUID() == 6) || (liste5[1].getUID() == 7)));
		//Node 6 - only 10,8
		korrekt[6] = ((liste6.length==3) && ((liste6[0].getUID() == 10) || (liste6[0].getUID() == 8) || (liste6[0].getUID() == 1)) &&  ((liste6[1].getUID() == 10) || (liste6[1].getUID() == 8) || (liste6[1].getUID() == 1)) && ((liste6[2].getUID() == 10) || (liste6[2].getUID() == 8) || (liste6[2].getUID() == 1)));
		//Node 7 - only 23
		korrekt[7] = ((liste7.length==1) && (liste7[0].getUID() == 23));
		//Node 8 - only 4
		korrekt[8] = ((liste8.length==1) && (liste8[0].getUID() == 4));
		//Node 9 - only 5
		korrekt[9] = ((liste9.length==2) && ((liste9[0].getUID() == 5) || (liste9[0].getUID() == 6)) &&  ((liste9[1].getUID() == 5) || (liste9[1].getUID() == 6)));
		
		//for (int i=1;i<12;i++) {
		for (int i=1;i<11;i++) {
			if (korrekt[i] == false) fail("testGraph: failed OutgingEdges-Test Number "+i);
		}
		
		/*
		 * TestGraph2
		 * predefined graph with complex ways
		 */
		
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/testGraph2.osm"));
			
		}
		catch(Exception e) {
			fail("TestGraph2: failed during import: "+e.getLocalizedMessage());
		}
		
		

		korrekt = new boolean[12];
		
		liste1 = testGraph.getNode(1).getIncomingEdges();
		liste2 = testGraph.getNode(2).getIncomingEdges();
		liste3 = testGraph.getNode(3).getIncomingEdges();
		liste4 = testGraph.getNode(4).getIncomingEdges();
		liste5 = testGraph.getNode(5).getIncomingEdges();
		liste6 = testGraph.getNode(6).getIncomingEdges();
		liste7 = testGraph.getNode(7).getIncomingEdges();
		liste8 = testGraph.getNode(8).getIncomingEdges();
		liste9 = testGraph.getNode(9).getIncomingEdges();
		liste10 = testGraph.getNode(10).getIncomingEdges();

		
		korrekt[1] = ((liste1.length == 2) && ((liste1[0].getUID() == 8) || (liste1[0].getUID() == 10)));
		korrekt[4] = ((liste4.length == 2) && ((liste4[0].getUID() == 4) || (liste4[0].getUID() == 5)) && ((liste4[1].getUID() == 4) || (liste4[1].getUID() == 5)));
		korrekt[10] = ((liste10.length == 1) && (liste10[0].getUID() == 7));
		
		
		korrekt[2] = ((liste2.length==2) && ((liste2[0].getUID() == 10) || (liste2[0].getUID() == 20)) &&  ((liste2[1].getUID() == 10) || (liste2[1].getUID() == 20)));
		//Node 3 - only 20
		korrekt[3] = ((liste3.length==1) && (liste3[0].getUID() == 20));
		//Node 5 - only 5,7 
		korrekt[5] = ((liste5.length==2) && ((liste5[0].getUID() == 5) || (liste5[0].getUID() == 7)) &&  ((liste5[1].getUID() == 5) || (liste5[1].getUID() == 7)));
		//Node 6 - only 10,8
		korrekt[6] = ((liste6.length==3) && ((liste6[0].getUID() == 10) || (liste6[0].getUID() == 8)) &&  ((liste6[1].getUID() == 10) || (liste6[1].getUID() == 8)) && ((liste6[1].getUID() == 10) || (liste6[1].getUID() == 8)));
		//Node 7 - only 20,20
		korrekt[7] = ((liste7.length==2) && ((liste7[0].getUID() == 20)) &&  ((liste7[1].getUID() == 20)));
		//Node 8 - only 4
		korrekt[8] = ((liste8.length==1) && (liste8[0].getUID() == 4));
		//Node 9 - only 5
		korrekt[9] = ((liste9.length==2) && (liste9[0].getUID() == 5) && (liste9[0].getUID() == 5));
		
		//for (int i=1;i<12;i++) {
		for (int i=1;i<11;i++) {
			if (korrekt[i] == false) fail("testGraph2: Failed IncomingEdges-Test Number "+i);
		}
		
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
		
		
		korrekt[1] = ((liste1.length == 2) && ((liste1[0].getUID() == 8) || (liste1[0].getUID() == 10)));
		korrekt[4] = ((liste4.length == 2) && ((liste4[0].getUID() == 4) || (liste4[0].getUID() == 5)) && ((liste4[1].getUID() == 4) || (liste4[1].getUID() == 5)));
		korrekt[10] = ((liste10.length == 1) && (liste10[0].getUID() == 7));
		
		
		korrekt[2] = ((liste2.length==2) && ((liste2[0].getUID() == 10) || (liste2[0].getUID() == 20)) &&  ((liste2[1].getUID() == 10) || (liste2[1].getUID() == 20)));
		//Node 3 - only 20
		korrekt[3] = ((liste3.length==1) && (liste3[0].getUID() == 20));
		//Node 5 - only 5,7 
		korrekt[5] = ((liste5.length==2) && ((liste5[0].getUID() == 5) || (liste5[0].getUID() == 7)) &&  ((liste5[1].getUID() == 5) || (liste5[1].getUID() == 7)));
		//Node 6 - only 10,8
		korrekt[6] = ((liste6.length==3) && ((liste6[0].getUID() == 10) || (liste6[0].getUID() == 8)) &&  ((liste6[1].getUID() == 10) || (liste6[1].getUID() == 8)) && ((liste6[1].getUID() == 10) || (liste6[1].getUID() == 8)));
		//Node 7 - only 20,20
		korrekt[7] = ((liste7.length==2) && ((liste7[0].getUID() == 20)) &&  ((liste7[1].getUID() == 20)));
		//Node 8 - only 4
		korrekt[8] = ((liste8.length==1) && (liste8[0].getUID() == 4));
		//Node 9 - only 5
		korrekt[9] = ((liste9.length==2) && (liste9[0].getUID() == 5) && (liste9[0].getUID() == 5));
		//for (int i=1;i<12;i++) {
		for (int i=1;i<11;i++) {
			if (korrekt[i] == false) fail("testGraph2: Failed OutgingEdges-Test Number "+i);
		}
		
		/*
		 * TestGraph3
		 * predefined graph. More complex, testing correct lengths.
		 */
		
		try {
			testGraph = OSMReader.readOSMFile(new File("testFiles/testGraph3.osm"));

		}
		catch(Exception e) {
			fail("TestGraph3: Failed Importing: "+e.getLocalizedMessage());
		}
		

		int i = 0; int eps = 1;
		MapNode node = null;
		MapEdge[] edge = new MapEdge[12];
		int[] Ist = new int[112];

		node = testGraph.getNode(1);
		i = 0;
		edge = node.getIncomingEdges();


		while(i<edge.length && i<12) {
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		
		
		//if (!((Ist[8] >= 6027.781f) && (Ist[8] <= 6027.782f)))
		if (((6027.782f - Ist[8]) > eps) && ((Ist[8] - 6027.782f) > 0))
			fail("testGraph3: wrong length at Edge 8. expected min: 6027.781, max: 6027.782, but is: "+Ist[8]);
		
		//if (!((Ist[4] >= 7610.388f) && (Ist[4] <= 7610.389f)))
		if (((7610.389f - Ist[4]) > eps) && ((Ist[4] - 7610.389f) > 0))
			fail("testGraph3:  wrong length at Edge 4. expected min: 7610.388, max: 7610.389, but is: "+Ist[4]);
		
		//if (!((Ist[5] >= 5990.883f) && (Ist[5] <= 5990.884f)))
		if (((5990.884f - Ist[5]) > eps) && ((Ist[5] - 5990.884f) > 0))
			fail("testGraph3:  wrong length at Edge 5. expected min: 5990.884, max: 5990.884, but is: "+Ist[5]);
		

		node = testGraph.getNode(2);
		edge = node.getIncomingEdges();
		while(i<edge.length && i<12) {
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		if (((14032.333f - Ist[111]) > eps) && ((Ist[111] - 14032.333f) > 0))
			fail("testGraph3: wrong length at Edge 111. expected min: 14032.332, max: 14032.333, but is: "+Ist[111]);
		

		node = testGraph.getNode(3);
		edge = node.getIncomingEdges();
		while(i<edge.length && i<12) {
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		
		if (((9174.900f - Ist[7]) > eps)  && ((Ist[7] - 9174.900f) > 0))
			fail("testGraph3: wrong length at Edge 7. expected min: 9174.899, max: 9174.900, but is: "+Ist[7]);
		

		node = testGraph.getNode(4);
		edge = node.getIncomingEdges();
		while(i<edge.length && i<12) {
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		if (((4187.851f - Ist[20]) > eps) && ((Ist[20] - 4187.851f) > 0))
			fail("testGraph3: wrong length at Edge 20. expected min: 4187.850, max: 4187.851, but is: "+Ist[20]);
		
		if (((2085.281f - Ist[21]) > eps) && ((Ist[21] - 2085.281f) > 0))
			fail("testGraph3: wrong length at Edge 21. expected min: 2085.280, max: 2085.281, but is: "+Ist[21]);
		

		node = testGraph.getNode(5);
		edge = node.getIncomingEdges();
		while(i<edge.length && i<12) {
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		if (((3553.719f - Ist[22]) > eps) && ((Ist[22] - 3553.719f) > 0))
			fail("testGraph3: wrong length at Edge 20. expected min: 3553.718, max: 3553.719, but is: "+Ist[22]);
		
		if (((8618.230f - Ist[33]) > eps) && ((Ist[33] - 8618.230f) > 0))
			fail("testGraph3: wrong length at Edge 33. expected min: 8618.229, max: 8618.230, but is: "+Ist[33]);
		

		node = testGraph.getNode(6);
		edge = node.getIncomingEdges();
		while(i<edge.length && i<12) {
			
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		if (((0.005f - Ist[50]) > eps) && ((Ist[50] - 0.005f) > 0))
			fail("testGraph3: wrong length at Edge 50. expected min: 0.004, max: 0.005, but is: "+Ist[50]);
		
		if (((5064.695f - Ist[55]) > eps) && ((Ist[55] - 5064.695f) > 0))
			fail("testGraph3: wrong length at Edge 55. expected min: 5064.694, max: 5064.695, but is: "+Ist[55]);
		
		

		node = testGraph.getNode(7);
		edge = node.getIncomingEdges();
		while(i<edge.length && i<12) {
			
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		if (((5416.757f - Ist[5]) > eps) && ((Ist[5] - 5416.757f) > 0))
			fail("testGraph3: wrong length at Edge 5(l). expected min: 5416.756, max: 5416.757, but is: "+Ist[5]);
		

		node = testGraph.getNode(8);
		edge = node.getIncomingEdges();
		while(i<edge.length && i<12) {
			
			Ist[edge[i].getUID()] = edge[i].getLength();
			i++;
		}
		i =0;
		
		if (((4786.739f - Ist[10]) > eps) && ((Ist[10] - 4786.739f) > 0))
			fail("testGraph3: wrong length at Edge 10. expected min: 4786.738, max: 4786.739, but is: "+Ist[5]);
		
		
		
		
	}


}
