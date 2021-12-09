package pacman.entries.pacman;

import java.util.ArrayList;
import pacman.controllers.Controller;
import pacman.controllers.examples.StarterGhosts;
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
	public enum Route {
		BASIC,
		CLOSEST_AWAY,
		CORNERS,
	}
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	private int GHOST_TOLERANCE;
	private int LOOK_AHEAD;

	private PillRoutePlanner pillRoute;
	private EscapeRoutePlanner escapeRoutePlanner;
	private MOVE lastMove;

	public CS4096PacMan(){
		this.pillRoute = new PillRoutePlanner(Route.CLOSEST_AWAY);
		this.escapeRoutePlanner = new EscapeRoutePlanner();
		this.GHOST_TOLERANCE = 30;
		this.LOOK_AHEAD = 15;
	}

	public CS4096PacMan(int tolerance, int lookAhead){
		this.pillRoute = new PillRoutePlanner(Route.CLOSEST_AWAY);
		this.escapeRoutePlanner = new EscapeRoutePlanner();
		this.GHOST_TOLERANCE = tolerance;
		this.LOOK_AHEAD = lookAhead;
	}
	
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanCurrentNodeIndex();

		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		if (AssessThreat(game) == 1){
			lastMove = escapeRoutePlanner.getNextMove(game);
			return lastMove;
		}

		//Strategy 2: find the nearest edible ghost and go after them 
		//TODO : find better threshold for ghost edible time
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)>5)
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		
		if(minGhost!=null){	//we found an edible ghost
			lastMove = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH);
			return lastMove;
		}
		//Strategy 3: go after the pills and power pills
		lastMove = pillRoute.getMoveTowardsPills(game);
		return lastMove;

	}

	// MOUNIKA
	private double AssessThreat(Game game){
		// Get PacMan's Position
		int current=game.getPacmanCurrentNodeIndex();

		// Check all ghosts
		for(GHOST ghost : GHOST.values())
			// No threat if ghost is edible or in lair
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				// Checks if ghost is closer than the threshold
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
					// If so, PacMan is in danger
					return 1.0;

		// Otherwise, PacMan is safe
		return 0.0;
	}

	// KEVIN
	// TODO : make routes swappable and figure out corners
	private class PillRoutePlanner{
		int routeIndex;
		Route strategy;
		Integer[] TOP_LEFT = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 22, 23, 26, 27, 28, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 58, 59, 60, 64, 65, 66, 70, 71, 72, 76, 77, 78, 79, 
			80, 81, 82, 83, 92, 94, 96, 98, 100, 102, 104, 106, 108};
		Integer[] TOP_RIGHT = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 24, 25, 29, 30, 31, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 61, 62, 63, 67, 68, 69, 73, 74, 75, 84, 85, 
			86, 87, 88, 89, 90, 91, 93, 95, 97, 99, 101, 103, 105, 107, 109};
		Integer[] BOTTOM_LEFT = {110, 112, 114, 115, 116, 117, 118, 119, 120, 128, 129, 132, 133, 136, 137, 138,
			139, 140, 141, 142, 143, 144, 145, 146, 147, 160, 161, 162, 166, 167, 168, 172, 173, 174, 175,
			176, 177, 184, 185, 188, 189, 190, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206};
		Integer[] BOTTOM_RIGHT = {111, 113, 121, 122, 123, 124, 125, 126, 127, 130, 131, 134, 135, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 163, 164, 165, 169, 170, 171, 178, 
			179, 180, 181, 182, 183, 186, 187, 191, 192, 193, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219};
		ArrayList<Integer[]> routeList;
		
		private PillRoutePlanner(Route route){
			routeIndex = 0;
			routeList = new ArrayList<Integer[]>();
			strategy = route;
			switch (route){
				case CORNERS:
				{
					routeList.add(BOTTOM_RIGHT);
					routeList.add(BOTTOM_LEFT);
					routeList.add(TOP_LEFT);
					routeList.add(TOP_RIGHT);
					break;
				}
				default:{
					// Do nothing
				}
			}
		}

		// TODO : Fix this method for game over
		private MOVE getMoveTowardsPills(Game game){
			switch (strategy){
				case BASIC:
				{
					return eatClosestPill(game);
				}
				case CLOSEST_AWAY:
				{
					return eatClosestPillAwayFromGhosts(game);
				}
				case CORNERS:
				default:
				{
					return areaPriority(game);
				}
			}
		}

		private MOVE areaPriority(Game game){
			int[] pills=game.getPillIndices();
			int[] powerPills=game.getPowerPillIndices();		
			int current=game.getPacmanCurrentNodeIndex();
			ArrayList<Integer> targets = new ArrayList<Integer>();

			// Repeat until targets are found
			while (targets.size() == 0){
				Integer[] current_section;
				// If all routes have been cleared, return all pills
				if(routeIndex == routeList.size()){
					ArrayList<Integer> convert = new ArrayList<Integer>();
					for (int i : pills){
						convert.add((Integer)i);
					}
					current_section = convert.toArray(new Integer[convert.size()]);
				}
				// Otherwise, get target area
				else
				{
					current_section = routeList.get(routeIndex);
				}
				
				// Add remaining pills to targets
				for(int i=0; i < current_section.length ; i++){					//check which pills are available
					if(game.isPillStillAvailable(current_section[i])){
						targets.add(pills[current_section[i]]);
					}
				}	
				// If area is empty, move to next route
				if (targets.size() == 0){
					System.out.println("Moving to next area!");
					routeIndex++;
				}
			}
									
			for(int i=0;i<powerPills.length;i++)			//check with power pills are available
			if(game.isPowerPillStillAvailable(powerPills[i]))
				targets.add(powerPills[powerPills[i]]);				
			
			int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
			
			for(int i=0;i<targetsArray.length;i++)
				targetsArray[i]=targets.get(i);
			
			//return the next direction once the closest target has been identified
			return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
		}

		private MOVE eatClosestPill(Game game){
			int[] pills=game.getPillIndices();
			int[] powerPills=game.getPowerPillIndices();
			int current=game.getPacmanCurrentNodeIndex();		
			
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
			
			//return the next direction once the closest target has been identified
			return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
		}

		// Strategy to aim for pills that maximise distance from ghosts
		// TODO : incorporate power pill strategy
		private MOVE eatClosestPillAwayFromGhosts(Game game){
			int current=game.getPacmanCurrentNodeIndex();		

			// Get all pills on board
			int[] targetsArray = getNodesOfAvailablePillsAndPowerPills(game);
			
			// Narrow down to closest pills within tolerance
			// TODO : This could be better
			int[] closestPills = game.getClosestNodeIndexesFromNodeIndexWithTolerance(current,targetsArray,DM.PATH,GHOST_TOLERANCE);
			
			// Use this instead to consider all pills - a bit slower and does more stuttering
			//int[] closestPills = targetsArray;

			StarterGhosts ghostController = new StarterGhosts();
			ArrayList<GHOST> closeGhostList = new ArrayList<GHOST>();

			// TODO : use Mounikas threat assessment here once done
			for(GHOST ghost : GHOST.values())
			{
				// No threat if ghost is edible or in lair
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
					// Checks if ghost is closer than the threshold
					if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE*3)
						closeGhostList.add(ghost);
			}

			// If there is only one closest pill or no close ghosts, eat closest pill
			if ((closestPills.length == 1) || closeGhostList.size() == 0)
			{
				return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);
			}
			// If there are close ghosts, move away from them
			else
			{
				double maxGhostDistance = Double.MIN_VALUE;
				int bestPillIndex = 0;

				// Play out consequences of chasing closest pills
				for(int i = 0; i < closestPills.length; i++)
				{
					boolean caught = false;
					// Get copy of the game state
					Game gameClone = game.copy();
					int newNode = gameClone.getPacmanCurrentNodeIndex();
					
					// Look ahead at consequences for set number of turns
					for(int j = 0; j < LOOK_AHEAD; j++){
						gameClone = gameClone.getNextGameState(gameClone, closestPills[i], ghostController);
						newNode = gameClone.getPacmanCurrentNodeIndex();

						// If PacMan is eaten, raise flag
						if ((game.getPacmanNumberOfLivesRemaining() > gameClone.getPacmanNumberOfLivesRemaining()) || gameClone.gameOver()){
							caught = true;
							break;
						// If pill is eaten, no more computation necessary
						}else if (newNode == closestPills[i]){
							break;
						}
					}
					
					// If PacMan is eaten, do not consider this move
					if(caught){
						continue;
					}	

					// Get average distance from close ghosts
					int distanceSum = 0;
					for(GHOST ghost : closeGhostList)
					{
						distanceSum += gameClone.getShortestPathDistance(newNode,gameClone.getGhostCurrentNodeIndex(ghost));
					}
					double averageGhostDistance = distanceSum/closeGhostList.size();

					// Find index of move which leads to max distance from ghosts
					if (averageGhostDistance > maxGhostDistance)
					{
						maxGhostDistance = averageGhostDistance;
						bestPillIndex = i;
					}
				}

				return game.getNextMoveTowardsTarget(current,closestPills[bestPillIndex],DM.PATH);
			}
		}

		private int[] getNodesOfAvailablePillsAndPowerPills(Game game){

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
	}

	// PRIYAL AND RISHABH
	private class EscapeRoutePlanner{
		private int[] getNodesOfAvailablePowerPills(Game game){
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

		// Returns next move to escape ghosts
		private MOVE getNextMove(Game game){
			// PacMan's current position
			int current=game.getPacmanCurrentNodeIndex();

			// Variables for closest ghost and their distance
			GHOST closest_ghost = GHOST.values()[0];
			int closest_distance = 999999;
			GHOST sec_closest_ghost = GHOST.values()[0];
			int sec_closest_distance = 99999;
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
			for (GHOST ghost : GHOST.values())
				if (ghost != closest_ghost) {
					if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
						// Check if ghost is closest
						if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<sec_closest_distance){
							sec_closest_ghost = ghost;
							sec_closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
						}
				}
			ghostArray[0] = game.getGhostCurrentNodeIndex(closest_ghost);
			ghostArray[1] = game.getGhostCurrentNodeIndex(sec_closest_ghost);
			// Return move away from closest ghost
			return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getClosestNodeIndexFromNodeIndex(current,ghostArray,DM.PATH),DM.PATH);
			
		}
	}
}























