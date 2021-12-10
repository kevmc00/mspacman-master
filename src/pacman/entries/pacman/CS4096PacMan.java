package pacman.entries.pacman;

import java.util.ArrayList;

import pacman.controllers.Controller;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.game.Game;

import static pacman.game.Constants.*;

/*
 * Pac-Man controller as part of the starter package - simply upload this file as a zip called
 * MyPacMan.zip and you will be entered into the rankings - as simple as that! Feel free to modify 
 * it or to start from scratch, using the classes supplied with the original software. Best of luck!
 * 
 * This controller utilises 3 tactics, in order of importance:
 * 1. Get away from any non-edible ghost that is in close proximity
 * 2. Go after the nearest edible ghost
 * 3. Go to the nearest pill/power pill
 */
public class CS4096PacMan extends Controller<MOVE>
{	
	private int PILL_DISTANCE_TOLERANCE;
	private int LOOK_AHEAD;
	private int CLOSE_GHOST_DISTANCE;
	private int MIN_GHOST_EDIBLE_TIME;

	private PillRoutePlanner pillRoute;
	private GhostHunter ghostHunter;
	private EscapeRoutePlanner escapeRoutePlanner;

	public CS4096PacMan(){
		this.pillRoute = new PillRoutePlanner();
		this.escapeRoutePlanner = new EscapeRoutePlanner();
		this.ghostHunter = new GhostHunter();
		this.PILL_DISTANCE_TOLERANCE = 30;
		this.LOOK_AHEAD = 15;
		this.CLOSE_GHOST_DISTANCE = 30;
		this.MIN_GHOST_EDIBLE_TIME = 5;
	}

	public CS4096PacMan(int tolerance, int lookAhead, int closeGhostDistance, int minEdibleGhostTime){
		this.pillRoute = new PillRoutePlanner();
		this.escapeRoutePlanner = new EscapeRoutePlanner();
		this.ghostHunter = new GhostHunter();
		this.PILL_DISTANCE_TOLERANCE = tolerance;
		this.LOOK_AHEAD = lookAhead;
		this.CLOSE_GHOST_DISTANCE = closeGhostDistance;
		this.MIN_GHOST_EDIBLE_TIME = minEdibleGhostTime;
	}
	
	public MOVE getMove(Game game,long timeDue)
	{			
		MOVE nextMove;

		ArrayList<GHOST> edibleGhosts = HELPER.getEdibleGhosts(game, MIN_GHOST_EDIBLE_TIME);
		ArrayList<GHOST> closeGhosts = HELPER.getCloseGhosts(game, CLOSE_GHOST_DISTANCE);

		// PLAN A : EAT GHOSTS
		// Criteria: edible ghosts on board
		if (edibleGhosts.size() > 0){
			nextMove = ghostHunter.hunt(game, edibleGhosts);
			if (nextMove != null){
				return nextMove;
			}
		}

		// PLAN B : EAT A POWER PILL
		// Criteria : 3 close ghosts
		if (closeGhosts.size() >= 3){
			nextMove = pillRoute.getPowerPill(game);
			if (nextMove != null){
				return nextMove;
			}

			// PLAN C : RUN AWAY	
			// Criteria : 3 close ghosts
			return escapeRoutePlanner.getNextMove(game);
		}

		// PLAN D : RUN TO PILLS AWAY FROM GHOSTS
		// Criteria : Close ghosts
		if(closeGhosts.size() > 0){
			return pillRoute.getPillAwayFromGhosts(game, closeGhosts);
		}

		// PLAN E : EAT CLOSEST PILL
		// Criteria : No close ghosts
		return pillRoute.getMoveTowardsClosestPill(game);
	}

	// Plans the best route for eating the pills
	private class PillRoutePlanner{

		public MOVE getPowerPill(Game game){
			int[] powerPills = HELPER.getNodesOfAvailablePowerPills(game);
			AggressiveGhosts ghostController = new AggressiveGhosts();
			int closestAccessiblePowerPill = Integer.MAX_VALUE;
			int minPowerPillDistance = Integer.MAX_VALUE;

			for (int pill : powerPills){
				if((HELPER.canPacManGetHere(game, pill, ghostController) && game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pill) < minPowerPillDistance)){
					closestAccessiblePowerPill = pill;
					minPowerPillDistance = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pill);
				}
			}

			if (closestAccessiblePowerPill != Integer.MAX_VALUE){
				return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),closestAccessiblePowerPill,DM.PATH);
			}else{
				return null;
			}
		}

		public MOVE getPillAwayFromGhosts(Game game, ArrayList<GHOST> closeGhostList){
			// Get all pills on board
			int[] targetsArray = HELPER.getNodesOfAvailablePillsAndPowerPills(game);

			// Narrow down to closest pills within tolerance
			int[] closestPills = HELPER.getClosestNodeIndexesFromNodeIndexWithTolerance(game, game.getPacmanCurrentNodeIndex(),targetsArray,DM.PATH, PILL_DISTANCE_TOLERANCE);
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), HELPER.getSafestTargetNode(game, closestPills, closeGhostList, LOOK_AHEAD),DM.PATH);
		}

		public MOVE getMoveTowardsClosestPill(Game game){
			int[] targetsArray = HELPER.getNodesOfAvailablePillsAndPowerPills(game);
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetsArray,DM.PATH),DM.PATH);
		}
	}

	// Plans route for hunting edible ghosts
	private class GhostHunter{
		public MOVE hunt(Game game, ArrayList<GHOST> edibleGhostList){
			GHOST closestAccessibleEdibleGhost = null;
			AggressiveGhosts ghostController = new AggressiveGhosts();
			int minMovesToGhost = Integer.MAX_VALUE;
			int moves;

			// Find the edible ghost which can be caught in the fewest moves
			for(GHOST ghost : edibleGhostList){
				moves = HELPER.movesToCatchEdibleGhost(game, ghost, ghostController);
				if ((moves != -1) && (moves < minMovesToGhost)){
					closestAccessibleEdibleGhost = ghost;
					minMovesToGhost = moves;
				}
			}

			// If one is found, move towards it
			if(closestAccessibleEdibleGhost != null){
				return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(closestAccessibleEdibleGhost), DM.PATH);
			}else{
				return null;
			}
		}
	}

	// Plans route for escaping when danger is high
	private class EscapeRoutePlanner{
		private EscapeRoutePlanner(){}

		// Returns next move to escape ghosts
		private MOVE getNextMove(Game game){
			// PacMan's current position
			int current=game.getPacmanCurrentNodeIndex();

			// Variables for closest ghost and their distance
			GHOST closest_ghost = GHOST.values()[0];

			int closest_distance = Integer.MAX_VALUE;
			GHOST sec_closest_ghost = GHOST.values()[0];
			int sec_closest_distance = Integer.MAX_VALUE;
			int[] ghostArray=new int[2];	
			


			// Check all ghosts
			for(GHOST ghost : GHOST.values())
				// No need to run if ghost is edible or in lair
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
					// Check if ghost is closest
					if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<closest_distance){
						closest_ghost = ghost;
						closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
					}

			ghostArray[0] = game.getGhostCurrentNodeIndex(closest_ghost);
			//check for closest pill and confirm that the pill is not in the direction of the ghost
			int[] powerPills = HELPER.getNodesOfAvailablePowerPills(game);
			int closestAccessiblePowerPill = Integer.MAX_VALUE;
			int minPowerPillDistance = Integer.MAX_VALUE;
			AggressiveGhosts ghostController = new AggressiveGhosts();
			for (int pill : powerPills){
				if((HELPER.canPacManGetHere(game, pill, ghostController) && game.getShortestPathDistance(current, pill) < minPowerPillDistance)){
					closestAccessiblePowerPill = pill;
					minPowerPillDistance = game.getShortestPathDistance(current, pill);
				}
			}

			if ((closestAccessiblePowerPill != Integer.MAX_VALUE) && (game.getShortestPathDistance(closestAccessiblePowerPill, ghostArray[0])<closest_distance)) {
				return game.getNextMoveTowardsTarget(current,closestAccessiblePowerPill,DM.PATH);
			}
					
			for (GHOST ghost : GHOST.values())
				if (ghost != closest_ghost) {
					if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
						// Check if ghost is closest
						if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<sec_closest_distance){
							sec_closest_ghost = ghost;
							sec_closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
						}
				}
			
			ghostArray[1] = game.getGhostCurrentNodeIndex(sec_closest_ghost);
			// Return move away from closest ghost
			return game.getNextMoveAwayFromTarget(current,game.getClosestNodeIndexFromNodeIndex(current,ghostArray,DM.PATH),DM.PATH);
			

			
		}
	}

	public static class HELPER{
		// Returns a list of available power pills
		public static int[] getNodesOfAvailablePowerPills(Game game){
			// Get all power pill indices
			int[] powerPills=game.getPowerPillIndices();
			
			ArrayList<Integer> targets=new ArrayList<Integer>();

			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(i))
				targets.add(powerPills[i]);		

			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);

			return targetsArray;
		}

		// Returns a list of all available pills and power pills
		public static int[] getNodesOfAvailablePillsAndPowerPills(Game game){

			int[] pills=game.getPillIndices();
			int[] powerPills=game.getPowerPillIndices();
			
			ArrayList<Integer> targets=new ArrayList<Integer>();
			
			for(int i=0;i<pills.length;i++)					//check which pills are available			
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);
			
			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
				if(game.isPowerPillStillAvailable(i))
					targets.add(powerPills[i]);				
			
			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
			
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);

			return targetsArray;
		}

		// Returns the number of moves required to catch an edible ghost
		// Returns -1 if ghost becomes inedible before can be caught or pacman gets eaten
		public static int movesToCatchEdibleGhost(Game game, GHOST ghost, AggressiveGhosts ghostController){
			// Create a copy of the game state to simulate chase
			Game gameClone = game.copy();
			int moveCount = 0;

			// Iterate until a result is reached
			while(true){
				// Move towards ghost and increment move count
				int ghostNode = gameClone.getGhostCurrentNodeIndex(ghost);
				gameClone = HELPER.getNextGameState(gameClone, ghostNode, ghostController);
				moveCount++;

				// If PacMan is eaten, raise flag
				if (gameClone.wasPacManEaten()){
					return -1;
				// If ghost is eaten, no more computation necessary
				}else if (gameClone.wasGhostEaten(ghost)){
					return moveCount;
				}
				// If ghost is no longer edible
				else if (gameClone.getGhostEdibleTime(ghost) == 0){
					return -1;
				}
			}
		}

		// Returns the node with the furthest distance from ghosts from a specified list of nodes
		public static int getSafestTargetNode(Game game, int[] targetNodes, ArrayList<GHOST> closeGhostList, int lookAhead){
			double maxGhostDistance = Double.MIN_VALUE;
			AggressiveGhosts ghostController = new AggressiveGhosts();
			int bestNodeIndex = 0;

			// Play out consequences of going to each node
			for(int i = 0; i < targetNodes.length; i++)
			{
				boolean caught = false;
				// Get copy of the game state
				Game gameClone = game.copy();
				int newNode = gameClone.getPacmanCurrentNodeIndex();
				
				// Look ahead at consequences for set number of turns
				for(int j = 0; j < lookAhead; j++){
					gameClone = HELPER.getNextGameState(gameClone, targetNodes[i], ghostController);
					newNode = gameClone.getPacmanCurrentNodeIndex();

					// If PacMan is eaten, raise flag
					if (gameClone.wasPacManEaten()){
						caught = true;
						break;
					// If pill is eaten, no more computation necessary
					}else if (newNode == targetNodes[i]){
						break;
					}
				}
				
				// If PacMan is eaten, do not consider this move
				if(caught){
					continue;
				}	

				// Get average distance from close ghosts
				int distanceSum = 0;
				int ghostCount = 0;

				for(GHOST ghost : GHOST.values())
				{
					if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0){
						int distance = gameClone.getShortestPathDistance(newNode,gameClone.getGhostCurrentNodeIndex(ghost));
						if(distance < 45){
							distanceSum += gameClone.getShortestPathDistance(newNode,gameClone.getGhostCurrentNodeIndex(ghost));
							ghostCount++;
						}
					}
					distanceSum += gameClone.getShortestPathDistance(newNode,gameClone.getGhostCurrentNodeIndex(ghost));
				}

				double averageGhostDistance;
				
				if(ghostCount != 0)
					averageGhostDistance = distanceSum/ghostCount;
				else
					averageGhostDistance = Double.MAX_VALUE;

				// Find index of move which leads to max distance from ghosts
				if ((averageGhostDistance > maxGhostDistance) 
				|| ((averageGhostDistance == maxGhostDistance) && (game.getPacmanLastMoveMade() == game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), targetNodes[i], DM.PATH))))
				{
					maxGhostDistance = averageGhostDistance;
					bestNodeIndex = i;
				}

			}
			return targetNodes[bestNodeIndex];
		}

		public static ArrayList<GHOST> getCloseGhosts(Game game, int distance){
			ArrayList<GHOST> closeGhosts = new ArrayList<GHOST>();
			for(GHOST ghost : GHOST.values())
			{
				// No threat if ghost is edible or in lair
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0){
					// Checks if ghost is closer than the threshold
					if(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost))<distance)
						closeGhosts.add(ghost);
				}
			}
			return closeGhosts;
		}

		public static ArrayList<GHOST> getEdibleGhosts(Game game, int minTime){
			ArrayList<GHOST> edibleGhosts = new ArrayList<GHOST>();
			for(GHOST ghost : GHOST.values()){
				if (game.getGhostEdibleTime(ghost) > minTime){
					edibleGhosts.add(ghost);
				}
			}
			return edibleGhosts;
		}

		// NOTE: clone the game before using this function
		public static Game getNextGameState(Game game, int targetNode, AggressiveGhosts ghostController){
			game.advanceGame(game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), targetNode, DM.PATH), ghostController.getMove());
			return game;
		}

		// Method returns a boolean to say whether or not PacMan can get to a specific node without being eaten
		public static boolean canPacManGetHere(Game game, int targetNode, AggressiveGhosts ghostController){
			Game gameClone = game.copy();
			while(true){
				gameClone = getNextGameState(gameClone, targetNode, ghostController);
				// If PacMan is eaten, raise flag
				if (gameClone.wasPacManEaten()){
					return false;
				// If pill is eaten, no more computation necessary
				}else if (gameClone.getPacmanCurrentNodeIndex() == targetNode){
					return true;
				}
			}
		}

		public static int[] getClosestNodeIndexesFromNodeIndex(Game game, int fromNodeIndex, int[] targetNodeIndices,
		DM distanceMeasure) 
		{
			double minDistance = Integer.MAX_VALUE;
			ArrayList<Integer> targetsList = new ArrayList<Integer>();
	
			for (int i = 0; i < targetNodeIndices.length; i++) {
				double distance = 0;
	
				distance = game.getDistance(targetNodeIndices[i], fromNodeIndex, distanceMeasure);
	
				if (distance < minDistance) {
					minDistance = distance;
					targetsList.clear();
					targetsList.add(targetNodeIndices[i]);
				}
				else if (distance == minDistance)
				{
					targetsList.add(targetNodeIndices[i]);
				}
			}
	
			int[] targets = new int[targetsList.size()];
			for(int i=0;i<targets.length;i++)
				targets[i]=targetsList.get(i);
	
			return targets;
		}
	
		public static int[] getClosestNodeIndexesFromNodeIndexWithTolerance(Game game, int fromNodeIndex, int[] targetNodeIndices,
		DM distanceMeasure, int tolerance) 
		{
			double minDistance = Integer.MAX_VALUE;
			ArrayList<Integer> targetsList = new ArrayList<Integer>();
	
			for (int i = 0; i < targetNodeIndices.length; i++) {
				double distance = 0;
	
				distance = game.getDistance(targetNodeIndices[i], fromNodeIndex, distanceMeasure);
	
				if (distance < minDistance) {
					minDistance = distance;
					for(int j = 0; j < targetsList.size(); j++){
						if(game.getDistance(targetsList.get(j), fromNodeIndex, distanceMeasure) > minDistance + tolerance){
							targetsList.remove(j);
						}
					}
					targetsList.add(targetNodeIndices[i]);
				}
				else if (distance <= minDistance + tolerance)
				{
					targetsList.add(targetNodeIndices[i]);
				}
			}
	
			int[] targets = new int[targetsList.size()];
			for(int i=0;i<targets.length;i++)
				targets[i]=targetsList.get(i);
	
			return targets;
		}
	}
}























