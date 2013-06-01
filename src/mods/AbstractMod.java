package mods;

import java.util.HashMap;
import java.util.Iterator;


import main.BiMap.MyEntry;
import main.Emulator;

import graphenbib.MapEdge;
import graphenbib.MapGraph;
import graphenbib.MapNode;

/**
 * 
 * @author arno
 *	This is an abstract mod definition providing a constructor and functionalities for linear or squared approximations
 */
public abstract class AbstractMod {

	protected final MapGraph graph;
	
	public AbstractMod(MapGraph graph) {
		this.graph = graph;
	}
	
	public LocationEstimationVector ApproximateLin(LocationEstimationVector LEV) {
		
		if (LEV == null) return null;
		
		HashMap<MapNode,Double> nodes = new HashMap<MapNode,Double>();
		double entry =0;
		//Analyze the LEV for values
		for (int i=0; i<LEV.size(); i++) {
			entry = LEV.getEntry(i);
			if (entry != 0.0) {
				//Do not look at virtual Steps - just look for "real" nodes
				if (Emulator.mapping.get(i).getValueY() != 0) continue;
				nodes.put(graph.getNode(Emulator.mapping.get(i).getValueX()), entry);
			}
		}
		
		//Now let's work on it
		Iterator<MapNode> it = nodes.keySet().iterator();
		MapNode node;
		MapEdge[] edges;
		// the actual steps for an edge, the value of the destinations entry, the index of the destination, a temporary index and value 
		int steps; double destValue;
		//int destIndex;
		int tmpIndex; double tmpValue;
		
		
		while (it.hasNext()) {
			node = it.next();
			edges = node.getOutgoingEdges();
			
			for (MapEdge edge : edges) {
				if (edge.getNodeStart() == node) {
					//forward
					if (edge.hasVirtualSteps() == false) continue;
					//destIndex = Emulator.mapping.getKey(new MyEntry<Integer>(edge.getNodeEnd().uid,0,0));
					if (nodes.containsKey(edge.getNodeEnd())){
						destValue =  nodes.get(edge.getNodeEnd());
					} else {
						destValue = 0.0;
					}
					//destValue =  nodes.get(destIndex);
					steps = edge.getStepCount();
					//Now compute all steps' values
					for (int i=0; i<steps; i++) {
						tmpIndex = Emulator.mapping.getKey(new MyEntry<Integer>(node.uid,edge.getNodeEnd().uid,i));
						//This is the "real" calculation!
						tmpValue = ( (((double) i / steps )*destValue) + (((steps- (double) i) / steps )*nodes.get(node)) ); 
						LEV.setMaxEntry(tmpIndex,tmpValue);
					}
				} else {
					//backward
					if (edge.hasVirtualSteps() == false) continue;
					//destIndex = Emulator.mapping.getKey(new MyEntry<Integer>(edge.getNodeStart().uid,0,0));
					if (nodes.containsKey(edge.getNodeStart())){
						destValue =  nodes.get(edge.getNodeStart());
					} else {
						destValue = 0.0;
					}
					steps = edge.getStepCount();
					//Now compute all steps' values
					for (int i=0; i<steps; i++) {
						tmpIndex = Emulator.mapping.getKey(new MyEntry<Integer>(edge.getNodeStart().uid,node.uid,i));
						//This is the "real" calculation!
						tmpValue = ( (((double) i / steps )*nodes.get(node)) + (((steps- (double) i) / steps )*destValue) ); 
						LEV.setMaxEntry(tmpIndex,tmpValue);
					}
				}
			}
			
		}
		
		return LEV;
	}
	
	public LocationEstimationVector ApproximateSqu(LocationEstimationVector LEV) {
		
		if (LEV == null) return null;
		
		HashMap<MapNode,Double> nodes = new HashMap<MapNode,Double>();
		double entry =0;
		//Analyze the LEV for values
		for (int i=0; i<LEV.size(); i++) {
			entry = LEV.getEntry(i);
			if (entry != 0.0) {
				//Do not look at virtual Steps - just look for "real" nodes
				if (Emulator.mapping.get(i).getValueY() != 0) continue;
				nodes.put(graph.getNode(Emulator.mapping.get(i).getValueX()), entry);
			}
		}
		
		//Now let's work on it
		Iterator<MapNode> it = nodes.keySet().iterator();
		MapNode node;
		MapEdge[] edges;
		// the actual steps for an edge, the value of the destinations entry, the index of the destination, a temporary index and value 
		int steps; double destValue;
		//int destIndex;
		int tmpIndex; double tmpValue;
		
		
		while (it.hasNext()) {
			node = it.next();
			edges = node.getOutgoingEdges();
			
			for (MapEdge edge : edges) {
				if (edge.getNodeStart() == node) {
					//forward
					if (edge.hasVirtualSteps() == false) continue;
					//destIndex = Emulator.mapping.getKey(new MyEntry<Integer>(edge.getNodeEnd().uid,0,0));
					if (nodes.containsKey(edge.getNodeEnd())){
						destValue =  nodes.get(edge.getNodeEnd());
					} else {
						destValue = 0.0;
					}
					//destValue =  nodes.get(destIndex);
					steps = edge.getStepCount();
					//Now compute all steps' values
					for (int i=0; i<steps; i++) {
						tmpIndex = Emulator.mapping.getKey(new MyEntry<Integer>(node.uid,edge.getNodeEnd().uid,i));
						//This is the "real" calculation!
						double d1,d2;
						d1 = ((double) i / steps ) * ((double) i / steps );
						d2 = ((steps- (double) i) / steps ) * ((steps- (double) i) / steps );
						tmpValue = ( ( d1 *destValue) + (d2*nodes.get(node)) ); 
						LEV.setMaxEntry(tmpIndex,tmpValue);
					}
				} else {
					//backward
					if (edge.hasVirtualSteps() == false) continue;
					//destIndex = Emulator.mapping.getKey(new MyEntry<Integer>(edge.getNodeStart().uid,0,0));
					if (nodes.containsKey(edge.getNodeStart())){
						destValue =  nodes.get(edge.getNodeStart());
					} else {
						destValue = 0.0;
					}
					steps = edge.getStepCount();
					//Now compute all steps' values
					for (int i=0; i<steps; i++) {
						tmpIndex = Emulator.mapping.getKey(new MyEntry<Integer>(edge.getNodeStart().uid,node.uid,i));
						//This is the "real" calculation!
						double d1,d2;
						d1 = ((double) i / steps ) * ((double) i / steps );
						d2 = ((steps- (double) i) / steps ) * ((steps- (double) i) / steps );
						tmpValue = ( (d1*nodes.get(node)) + (d2*destValue) ); 
						LEV.setMaxEntry(tmpIndex,tmpValue);
					}
				}
			}
			
		}
		
		return LEV;
	}
}
