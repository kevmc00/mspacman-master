package pacman.entries.pacman;

import java.util.ArrayList;

import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.internal.Ghost;

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
		CORNERS,
		DANGER;
	}
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away

	boolean[] section_empty = {false, false, false, false};
	private PillRoutePlanner pillRoute;
	private EscapeRoutePlanner escapeRoutePlanner;

	public CS4096PacMan(){
		this.pillRoute = new PillRoutePlanner(Route.CORNERS);
		this.escapeRoutePlanner = new EscapeRoutePlanner();
	}
	
	public MOVE getMove(Game game,long timeDue)
	{			
		int current=game.getPacmanCurrentNodeIndex();

		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		// TODO : fix to react as "if threat assessment is high, follow escape route"
		if (AssessThreat(game) == 1){
			return escapeRoutePlanner.getNextMove(game);
		}

		//Strategy 2: find the nearest edible ghost and go after them 
		// TODO : find better threshold for ghost edible time
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
		
		if(minGhost!=null)	//we found an edible ghost
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH);
		
		//Strategy 3: go after the pills and power pills
		// TODO : make pill route planner better
		int[] powerPills=game.getPowerPillIndices();		
		
		ArrayList<Integer> targets = pillRoute.getTargetPills(game);
			
		for(int i=0;i<powerPills.length;i++)			//check with power pills are available
		if(game.isPowerPillStillAvailable(powerPills[i]))
			targets.add(powerPills[powerPills[i]]);				
		
		int[] targetsArray=new int[targets.size()];		//convert from ArrayList to array
		
		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);
		
		//return the next direction once the closest target has been identified
		return game.getNextMoveTowardsTarget(current,game.getClosestNodeIndexFromNodeIndex(current,targetsArray,DM.PATH),DM.PATH);

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
	private class PillRoutePlanner{
		int routeIndex;
		Integer[] TOP_LEFT = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 22, 23, 26, 27, 28, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 58, 59, 60, 64, 65, 66, 70, 71, 72, 76, 77, 78, 79, 
			80, 81, 82, 83, 92, 94, 96, 98, 100, 102, 104, 106, 108};
		Integer[] TOP_RIGHT = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 24, 25, 29, 30, 31, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 61, 62, 63, 67, 68, 69, 73, 74, 75, 84, 85, 
			86, 87, 88, 89, 90, 91, 93, 95, 97, 99, 101, 103, 105, 107, 109};
		Integer[] BOTTOM_LEFT = {110, 112, 114, 115, 116, 117, 118, 119, 120, 128, 129, 132, 133, 136, 137, 138,
			139, 140, 141, 142, 143, 144, 145, 146, 147, 160, 161, 162, 166, 167, 168, 172, 173, 174, 175,
			176, 177, 184, 185, 188, 189, 190, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206};
		Integer[] BOTTOM_RIGHT = {111, 113, 121, 122, 123, 124, 125, 126, 127, 130, 131, 134, 135, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 163, 164, 165, 169, 170, 171, 178, 
			179, 180, 181, 182, 183, 186, 187, 191, 192, 193, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219};
		ArrayList<Integer[]> strategy;
		
		private PillRoutePlanner(Route route){
			routeIndex = 0;
			strategy = new ArrayList<Integer[]>();
			switch (route){
				case DANGER:		// TODO : implement this right
				case CORNERS:
				{
					strategy.add(BOTTOM_RIGHT);
					strategy.add(BOTTOM_LEFT);
					strategy.add(TOP_LEFT);
					strategy.add(TOP_RIGHT);
					break;
				}
			}
		}

		// TODO : Fix this method for game over
		private ArrayList<Integer> getTargetPills(Game game){
			int[] pills=game.getPillIndices();
			ArrayList<Integer> targets = new ArrayList<Integer>();

			while (targets.size() == 0){
				Integer[] current_section;
				if(routeIndex == strategy.size()){
					ArrayList<Integer> convert = new ArrayList<Integer>();
					for (int i : pills){
						convert.add((Integer)i);
					}
					current_section = convert.toArray(new Integer[convert.size()]);
				}
				else{
					current_section = strategy.get(routeIndex);
				}
				for(int i=0; i < current_section.length ; i++){					//check which pills are available
					if(game.isPillStillAvailable(current_section[i])){
						targets.add(pills[current_section[i]]);
					}
				}	
				// Area is empty
				if (targets.size() == 0){
					System.out.println("Moving to next area!");
					routeIndex++;
				}
			}
			
			return targets;
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
			int closest_distance = 999999;

			// Check all ghosts
			for(GHOST ghost : GHOST.values())
				// No need to run if ghost is edible or in lair
				if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
					// Check if ghost is closest
					if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<closest_distance){
						closest_ghost = ghost;
						closest_distance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
					}
			// Return move away from closest ghost
			return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(closest_ghost),DM.PATH); 
		}
	}
}























