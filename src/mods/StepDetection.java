/**
 * 
 */
package mods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;



import main.BiMap.MyEntry;
import main.Config;
import main.Emulator;
import main.Logger;
import main.Matrix;

import graphenbib.MapEdge;
import graphenbib.MapGraph;
import graphenbib.MapNode;

/**
 * @author arno
 *	An Approach of Step detection and matching it to the map and the walking progress.
 *	For more information see chapter 4 in the thesis
 */
public class StepDetection extends AbstractMod implements Emulateable {

	/**
	 * This Mod will use static sensor Set
	 */
	private final Sensor[] sensors = {Sensor.ACC, Sensor.COMPASS}; 
	
	public StepDetection(MapGraph graph) {
		super(graph);
	}
	
	@SuppressWarnings("unused")
	private int startNode = 0;
	
	private TreeMap<Long,Double> steps = new TreeMap<Long,Double>();

	private Matrix matrix;
	

	@Override
	public boolean init() {
		Logger.getInstance().log("StepDetection.init", "Initiating Detected Steps");
		
		File dataFile = new File(Config.DataDir+File.separator+"steps.csv");
		
		/*
		 * Format:
		 * #TIMESTAMP, #Direction
		 */
		
		String myLine="";
		int pos;
		double dir;
		Long timeStamp;
		
		try{
			FileReader fStream = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fStream);
			
			
			while(true) {
				//End of File reached?
				if ((myLine = br.readLine()) == null) break;
				
				//Get TimeStamp and Direcion
				pos = myLine.indexOf(",");
				timeStamp = Long.parseLong(myLine.substring(0, pos));
				dir = Double.parseDouble(myLine.substring(myLine.lastIndexOf(",")+2));
				
				//Store it
				this.steps.put(timeStamp, dir);
			}
			
			//Close File
			br.close();
		
		} catch (Exception ex) {
			Logger.getInstance().log("StepDetection.init", "Failed Initiating: "+ex);
			//ex.printStackTrace();
			return false;
		}
		
		Logger.getInstance().log("StepDetection.init", "Trying to store Matrix)");
		
		this.matrix = Emulator.getMatrix();
		
		Logger.getInstance().log("StepDetection.init", "Successfully Initiated. Total Detected Steps: "+this.steps.size());
		
		
		return true;
	}

	@Override
	public LocationEstimationVector getEstimates(long timeStamp1, long timeStamp2) {
		//In case of FP this function is adapted internally for only take into account timeStamp1
		LocationEstimationVector LEV = new LocationEstimationVector(Emulator.mapping.size(),timeStamp1,this);
		//get Last Position vector index (matrix column)
		int lastColumnIndex = matrix.getColNum()-1;
		int lastIndex =0;
		try {
			lastIndex = matrix.getColumnMaxPosition(lastColumnIndex);
		} catch (Exception e) {
			Logger.getInstance().log("StepDetection.getEstimates", "Fail during getting the last position in the matrix - probably matrix inconsistent.: "+e);
		}
		
		
		//get last position (correspnding node)
		MyEntry<Integer> lastPos = Emulator.mapping.get(lastIndex);
		
		//Check if this is a virtual step
		
		boolean virtual = ((lastPos.getValueY() != 0));
		
		TreeSet<MapNode> neighbors;
		neighbors = new TreeSet<MapNode>();
		//get Directions possible to walk
		
		//For parameters: Look for poissible directions and detected one
		HashSet<Double> directions = new HashSet<Double>();
		if (virtual) {
			//Virtual Steps lead to the assumption, that we have only two directions!
			
			MapEdge edge;
			edge = this.graph.getNode(lastPos.getValueX()).getEdgeTo(lastPos.getValueY());			
			this.scoreInitialVirtualEdge(edge, lastPos, LEV, timeStamp1);
			//Now having a virtual edge with fully computed transitioon proabilities
			ScoreAll(neighbors,1, lastPos,LEV,timeStamp1);
			
			//Store possible Directions
			directions.add(edge.getCompDir());
			directions.add(edge.getInvCompDir());
		} else {
		
		
		
		//Zero information but standing on a node?
		MapEdge[] edges = this.graph.getNode(lastPos.getValueX()).getOutgoingEdges();
		HashMap<MapEdge,Double> scoredEdges = new HashMap<MapEdge,Double>();
		double scoreSum = 0;
		//first put in the initial score
		for (MapEdge edge : edges) {
			if (edge.getNodeStart().uid == lastPos.getValueX()) {
				//Edge heading forward from actual position
				scoredEdges.put(edge, (double) this.scoreDir(steps.get(timeStamp1), edge.getCompDir()));
				scoreSum += this.scoreDir(steps.get(timeStamp1), edge.getCompDir());
				//Store Direction
				directions.add(edge.getCompDir());
				
			} else {
				scoredEdges.put(edge, (double) this.scoreDir(steps.get(timeStamp1), edge.getInvCompDir()));
				scoreSum += this.scoreDir(steps.get(timeStamp1), edge.getInvCompDir());
				//Store Direction
				directions.add(edge.getInvCompDir());
			}
		}
		//Now compute the probabilities
		for (MapEdge edge : edges) {
			scoredEdges.put(edge,(scoredEdges.get(edge) / scoreSum));
			scoreEdge(edge, this.graph.getNode(lastPos.getValueX()), LEV, 1, scoredEdges.get(edge));
		}
		
		neighbors = this.graph.getNode(lastPos.getValueX()).getNeighbours();
		
		ScoreAll(neighbors,1, lastPos,LEV,timeStamp1);	
		
		}
		
		LEV.addParamter("Direction", steps.get(timeStamp1));
		Integer minScore = Integer.MAX_VALUE;
		for (Double direction : directions) {
			if (scoreDir(steps.get(timeStamp1),direction) < minScore) {
				minScore = scoreDir(steps.get(timeStamp1),direction);
			}
		}
		LEV.addParamter("minScore", minScore);
		
		return LEV;
	}
	
	/**
	 * scoring all relevant ndoes and edges. (Basing on Breadth-First-Search) 
	 * @param neighbors A Set of the neighbors that have been recently scored
	 * @param LEV 
	 * @param stepCount the Steps done so far from startPosition (excluding the virtual steps towards the edges in the set)
	 * @param lastPos the last Position
	 * @param timeStamp1 the corresponding timeStamp
	 */
	private void ScoreAll(TreeSet<MapNode> neighbors, int stepCount,MyEntry<Integer> lastPos, LocationEstimationVector LEV, long timeStamp1) {
		//Set for storing visited Nodes  (they are marked)
		HashSet<MapNode> marked = new HashSet<MapNode>();
		//The Queue for BFS
		LinkedList<MapNode> queue = new LinkedList<MapNode>();
		//Distances have to be stored
		TreeMap<MapNode,Integer> distances = new TreeMap<MapNode,Integer>();
		
		
		int lastNode = lastPos.getValueX();
		int initNeighbor = 0;
		if (lastPos.getValueY() != 0) {
			initNeighbor = lastPos.getValueY();
			MapEdge edge = graph.getNode(lastPos.getValueX()).getEdgeTo(initNeighbor);
			int edgeSteps = edge.getStepCount();
			if (edge.getNodeEnd().uid == initNeighbor) {
				distances.put(edge.getNodeEnd(), edgeSteps-lastPos.getValueZ());
				distances.put(edge.getNodeStart(), lastPos.getValueZ()+1);
			} else {
				distances.put(edge.getNodeStart(), edgeSteps-lastPos.getValueZ());
				distances.put(edge.getNodeEnd(), lastPos.getValueZ()+1);
			}
			//stepCountForward = 
		}
		
		
		for (MapNode node : neighbors) {
			queue.add(node);
			distances.put(node, node.getEdgeTo(lastNode).getStepCount()+1);
		}
		MapNode lastMapNode = this.graph.getNode(lastNode);
		marked.add(lastMapNode);
		
		MapNode node;
		
		HashMap<MapEdge,Double> scoredEdges = new HashMap<MapEdge,Double>();
		double scoreSum = 0;
		
		while (queue.isEmpty() == false) {
			//get next node
			scoreSum =0;
			scoredEdges.clear();
			node = queue.poll();
			if (marked.contains(node))
				continue;

			//Add unmarked Neighbors
			for (MapNode neighbor : node.getNeighbours()) {
				if (marked.contains(neighbor) == false)
					queue.add(neighbor);
			}
			//mark this node
			marked.add(node);

			
			//get edges from this node
			for (MapEdge edge : node.getOutgoingEdges()) {
				if (edge.getNodeStart().uid == node.uid) {
					//Edge heading forward from actual position
					scoredEdges.put(edge, (double) this.scoreDir(steps.get(timeStamp1), edge.getCompDir()));
					scoreSum += this.scoreDir(steps.get(timeStamp1), edge.getCompDir());
				} else {
					scoredEdges.put(edge, (double) this.scoreDir(steps.get(timeStamp1), edge.getInvCompDir()));
					scoreSum += this.scoreDir(steps.get(timeStamp1), edge.getInvCompDir());
				}
			}
			
			
			//Now compute the probabilities
			for (MapEdge edge : node.getOutgoingEdges()) {
				scoredEdges.put(edge,(scoredEdges.get(edge) / scoreSum));
				scoreEdge(edge, node, LEV, distances.get(node), scoredEdges.get(edge));
				//Get new distances for the new nodes to be visited
				if (edge.getNodeEnd() == node) {
					distances.put(edge.getNodeStart(), distances.get(node)+1+edge.getStepCount());
				} else {
					distances.put(edge.getNodeEnd(), distances.get(node)+1+edge.getStepCount());
				}
			}
			
		}
		

		//manually set value for no movement.
		LEV.setEntry(Emulator.mapping.getKey(lastPos), 0.0);
				
		
	}

	private void scoreInitialVirtualEdge(MapEdge edge, MyEntry<Integer> lastPos, LocationEstimationVector LEV, long timeStamp) {

		edge = this.graph.getNode(lastPos.getValueX()).getEdgeTo(lastPos.getValueY());
		int progress = lastPos.getValueZ();
		double edgeDirection = edge.getCompDir();
		int maxSteps = edge.getStepCount(); //Number of virtual steps on the edge - the steps to the next node are not counted here!
		
		double score = this.scoreDir(edgeDirection, this.steps.get(timeStamp));
		double invscore = this.scoreDir(edge.getInvCompDir(), this.steps.get(timeStamp));
		//Normize it
		double scoreSum = score+invscore;
		score = score / scoreSum;
		invscore = invscore / scoreSum;
		
		//These flags denote what directions of this edge are still to be computed
		boolean forward = true, backward = true;
		//Number of steps needed to there
		int stepDistance = 1;
		//double tempScore = invscore;
		
		int key;
		//One Step backward
		

		
		while (backward) {
			if (progress > 0) {
				//Being not the first step - just decrease progress

				LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(lastPos.getValueX(), lastPos.getValueY(), lastPos.getValueZ() - stepDistance)), (1.0/stepDistance++)*invscore);
				progress--;

			} else {
				//Hadbeen the first virtual step - now look at the startnode itself
				LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(lastPos.getValueX(), 0, 0)), (1.0/stepDistance++)*invscore);
				backward = false;

			}
		}
		stepDistance = 1;
		progress = lastPos.getValueZ();
		while (forward) {
			// And one step forawrd!
			if (progress < maxSteps-1) {
				//Being not the last step - just increase progress
				key = Emulator.mapping.getKey(new MyEntry<Integer>(lastPos.getValueX(), lastPos.getValueY(), lastPos.getValueZ() + stepDistance));

				LEV.setMaxEntry(key, (1.0/stepDistance++)*score);
				progress++;
			} else {
				//Had been the last virtual step - now look at the endnode itself

				LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(lastPos.getValueY(), 0, 0)), (1.0/stepDistance++)*score);
				forward = false;
			}
		}
		
		//manually set value for no movement.
		LEV.setEntry(Emulator.mapping.getKey(lastPos), 0.0);
		
		
	}
	
	/**
	 * Scoring an Edge with base Score and inserting it into the LEV
	 * @param edge the edge to be scored
	 * @param startPos the StartNode the Edge is starting from
	 * @param LEV the actual Location EstimationVector
	 * @param stepCount the number of steps taht is needed basing on the map to get to the first step of the edge
	 * @param baseProb the base Probability denotes the calculated probability for stepping to the first step on this edge. 
	 */
	private void scoreEdge(MapEdge edge, MapNode startPos, LocationEstimationVector LEV, int stepCount, double baseProb) {
		
		//Before starting: How about the Threshold?
		if (baseProb < Config.FP_THRESHOLD)
			return;
		
		MapNode endNode;
		boolean direction;
		if (edge.getNodeEnd() != startPos) {
			endNode = edge.getNodeEnd();
			direction = true;
		}
		else {
			endNode = edge.getNodeStart();
			direction = false;
		}
		
		//First Case: No virtual Steps on this edge
		if (edge.hasVirtualSteps() == false) {
			//No virtual steps? -> directly put the probability into the next node
			LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(endNode.uid,0,0)), baseProb);
			//Nothng more to do - return
			return;
		}
		
	
		//Second: the edge is heading from start to end -> use increasing progress
		if (direction) {
			int progress = 0;
			while (progress < edge.getStepCount())
				LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(startPos.uid,endNode.uid,progress++)), (1.0/stepCount++)*baseProb);
			//Now the EndNode
			LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(endNode.uid,0,0)), (1.0/stepCount++)*baseProb);
		} else {
			//Third: the edge is heading to the starting point
			int progress = edge.getStepCount()-1;
			while (progress >= 0) {
				LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(endNode.uid,startPos.uid,progress--)), (1.0/stepCount++)*baseProb);
			}
			//Now the EndNode

			LEV.setMaxEntry(Emulator.mapping.getKey(new MyEntry<Integer>(endNode.uid,0,0)), (1.0/stepCount++)*baseProb);
		}
		


	}
	
	public Long[] getTimeStamps() {
		//return (Long[]) this.steps.navigableKeySet().toArray();

		
		Set<Long> set =  this.steps.keySet();
		return set.toArray(new Long[steps.size()]);
		
		
	}
	
	/**
	 * Sets the StartNode for the Module (Needed for computations)
	 * @param UID UID of the StartNode
	 */
	public void setStartNode(int UID) {
		this.startNode = UID;
	}

	@Override
	public Sensor[] getSensors() {
		return this.sensors;
	}
	
	/**
	 * Implementation of the pure scoring function concerning the direction 
	 * @param b1 The first Bearing
	 * @param b2 The second Bearing
	 * @return the score of the bearing difference
	 */
	private int scoreDir(double b1, double b2) {
		int abs = (int) Math.abs(b1-b2);
		if (abs < 42)
			return 20;
		if (abs < 90)
			return 10;
		if (abs < 120)
			return 5;
		
		return 1;
	}

}
