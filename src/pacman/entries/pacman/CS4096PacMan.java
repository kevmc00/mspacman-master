package pacman.entries.pacman;

import java.util.ArrayList;
import pacman.controllers.Controller;
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
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanCurrentNodeIndex();
		
		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		for(GHOST ghost : GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
					return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH);
		
		//Strategy 2: find the nearest edible ghost and go after them 
		int minDistance=Integer.MAX_VALUE;
		GHOST minGhost=null;		
		
		for(GHOST ghost : GHOST.values())
<<<<<<< Updated upstream
			if(game.getGhostEdibleTime(ghost)>0)
=======
			if(game.getGhostEdibleTime(ghost)>10) //changing value to 10 gave better results
>>>>>>> Stashed changes
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
				
				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}
		
		if(minGhost!=null)	//we found an edible ghost
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH);
		
		//Strategy 3: go after the pills and power pills
		int[] pills=game.getPillIndices();
		int[] powerPills=game.getPowerPillIndices();		
		
<<<<<<< Updated upstream
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
=======
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

		public MOVE eatClosestPill(Game game){
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

		// Strategy to aim for pills that maximize distance from ghosts
		// TODO : incorporate power pill strategy
		public MOVE eatClosestPillAwayFromGhosts(Game game){
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
			if ((closestPills.length == 3) || closeGhostList.size() == 0)
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
		private EscapeRoutePlanner(){}

		// Returns next move to escape ghosts
		private MOVE getNextMove(Game game){
			// PacMan's current position
			int current=game.getPacmanCurrentNodeIndex();

			// Variables for closest ghost and their distance
			GHOST closest_ghost = GHOST.values()[0];
			GHOST sec_closest_ghost = GHOST.values()[0];
			int closest_distance = 999999;
			int sec_closest_distance=999999;
<<<<<<< Updated upstream
			int[] targetsArray=new int[2];
			

			// Check all ghosts
			for(GHOST ghost : GHOST.values())
<<<<<<< Updated upstream
				// No need to run if ghost is edible or in lair and Check if ghost is closest
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0 && game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<closest_distance)
=======

			// Check all ghosts
			for(GHOST ghost : GHOST.values())
			{
				// No need to run if ghost is edible or in lair
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
>>>>>>> Stashed changes
				{
					// Check for ghost is closest
					if (game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<closest_distance)
					{
						closest_ghost = ghost;
						closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
						//check if there is a power pill nearby to eat
						//if the ghost is moving in the opposite direction, no need to run away from it
						
					}
									
				}
			}
			for(GHOST ghost : GHOST.values())
			{
				if (ghost != closest_ghost)
				{
					if (game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<sec_closest_distance)
					{
						sec_closest_ghost = ghost;
						sec_closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
					}
				}
			}
			if (game.getShortestPathDistance(game.getGhostCurrentNodeIndex(closest_ghost),game.getGhostCurrentNodeIndex(closest_ghost))<closest_ghost)
			{
				return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(closest_ghost),DM.PATH);
			}
			else
			{
				return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(sec_closest_ghost),DM.PATH);
			}
			// check how many ghost are nearby if more than 1 change direction 
			// check for ghost distances around the new place and if suitable then move 
			// can use game.getApproximateNextMoveAwayFromTarget 
			// Return move away from closest ghost 
		}
	}
}				//check if there is a power pill nearby to eat
					//if the ghost is moving in the opposite direction, no need to run away from it
					
				}
			// check how many ghost are nearby if more than 1 change direction 
			// check for ghost distances around the new place and if suitable then move 
			// can use game.getApproximateNextMoveAwayFromTarget 
			// Return move away from closest ghost 
			return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(closest_ghost),DM.PATH);
			//not sure if this checks for corners. if it doesn't need to chagne route
=======
			{
				// No need to run if ghost is edible or in lair
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				{
					// Check for ghost is closest
					if (game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<closest_distance)
					{
						closest_ghost = ghost;
						closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
						//check if there is a power pill nearby to eat
						//if the ghost is moving in the opposite direction, no need to run away from it
						
					}
									
				}
			}
			for(GHOST ghost : GHOST.values())
			{
				if (ghost != closest_ghost)
				{
					if (game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<sec_closest_distance)
					{
						sec_closest_ghost = ghost;
						sec_closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
					}
				}
			}
			targetsArray[0]=game.getGhostCurrentNodeIndex(closest_ghost);
			targetsArray[1]=game.getGhostCurrentNodeIndex(sec_closest_ghost);
			// check how many ghost are nearby if more than 1 get away from both 
			if (game.getShortestPathDistance(game.getGhostCurrentNodeIndex(closest_ghost),game.getGhostCurrentNodeIndex(closest_ghost))<closest_distance)
			{
				return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(closest_ghost),DM.EUCLID);
			}
			else
			{			
				return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);

			}
			 
			// check for ghost distances around the new place and if suitable then move 
			// Return move away from closest ghost 
>>>>>>> Stashed changes
		}
	}
}






















