package pacman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.NearestPillPacMan;
import pacman.controllers.examples.NearestPillPacManVS;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomNonRevPacMan;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.pacman.*;
import pacman.game.Game;
import pacman.game.GameView;
import static pacman.game.Constants.*;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 */
@SuppressWarnings("unused")
public class Executor
{	
	/**
	 * The main method. Several options are listed - simply remove comments to use the option you want.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		int delay=10;
		boolean visual=true;
		int numTrials=100;
		
		Executor exec=new Executor();
		
		/* run a game in synchronous mode: game waits until controllers respond. */
		// System.out.println("STARTER PACMAN vs starter GHOSTS");
		int PILL_DISTANCE_TOLERANCE = 30;
		int LOOK_AHEAD = 15;
		int CLOSE_GHOST_DISTANCE = 30;
		int MIN_GHOST_EDIBLE_TIME = 5;
		exec.runGame(new CS4096PacMan(PILL_DISTANCE_TOLERANCE, LOOK_AHEAD, CLOSE_GHOST_DISTANCE, MIN_GHOST_EDIBLE_TIME), new StarterGhosts(), visual,delay);
		exec.runGame(new StarterPacMan(), new StarterGhosts(), visual,delay);

		/* run multiple games in batch mode - good for testing. */
		
		// System.out.println("STARTER PACMAN vs LEGACY2THERECONING");
		// exec.runExperiment(new StarterPacMan(), new Legacy2TheReckoning(),numTrials);
		// System.out.println("RANDOM PACMAN vs LEGACY2THERECONING");
		// exec.runExperiment(new RandomPacMan(), new Legacy2TheReckoning(),numTrials);
		// System.out.println("NEAREST PILL PACMAN vs LEGACY2THERECONING");
		// exec.runExperiment(new NearestPillPacMan(), new Legacy2TheReckoning(),numTrials);
//		
//		
		System.out.println("CS4096 PACMAN vs Starter GHOSTS");
		List<Integer> cs4096Scores = exec.runExperiment(new CS4096PacMan(),  new StarterGhosts(),numTrials);
		System.out.println("STARTER PACMAN vs starter GHOSTS");
		List<Integer> starterScores = exec.runExperiment(new StarterPacMan(), new StarterGhosts(),numTrials);
		exec.writeScoresToFile(cs4096Scores, starterScores);

//		System.out.println("NEAREST PILL PACMAN vs RANDOM GHOSTS");
//		exec.runExperiment(new NearestPillPacMan(), new StarterGhosts(),numTrials);
		
		// Tolerance Test
		// for(int i = 5; i <= 50; i+=5){
		// 	System.out.println("Tolerance: " + i);
		// 	exec.runLevelExperiment(new CS4096PacMan(i, 10),  new StarterGhosts(), numTrials, false);
		// }
  		 
		// // Look Ahead Test
		// for(int i = 1; i <= 30; i+=1){
		// 	System.out.println("Look Ahead: " + i);
		// 	exec.runLevelExperiment(new CS4096PacMan(30, i),  new StarterGhosts(), numTrials, false);
		// }
		/* run the game in asynchronous mode. */
		
//		exec.runGameTimed(new MyPacMan(),new AggressiveGhosts(),visual);
//		exec.runGameTimed(new RandomPacMan(), new AvengersEvolution(evolutionFile),visual);
		// exec.runGameTimed(new HumanController(new KeyBoardInput()),new StarterGhosts(),visual);	
		
		
		/* run the game in asynchronous mode but advance as soon as both controllers are ready  - this is the mode of the competition.
		time limit of DELAY ms still applies.*/
		
//		boolean visual=true;
//		boolean fixedTime=false;
//		exec.runGameTimedSpeedOptimised(new MyMCTSPacMan(new AggressiveGhosts()),new AggressiveGhosts(),fixedTime,visual);
	
		
		/* run game in asynchronous mode and record it to file for replay at a later stage. */
		

		//String fileName="replay.txt";
		//exec.runGameTimedRecorded(new HumanController(new KeyBoardInput()),new RandomGhosts(),visual,fileName);
		//exec.replayGame(fileName,visual);
	} 

 // stats methods
 
	public double mean(int[] scoreslist) 
	{
		double sumOfScores = 0.0;    
		int n = scoreslist.length;
        for (int i = 0; i < n; i++) {
            sumOfScores = sumOfScores + scoreslist[i];
        }	
        double mean = sumOfScores/n;
        return mean;
     }
	public double median(int[] scoreslist) 
	{
    	double median =0.0;
		Arrays.sort(scoreslist);

    	int n = scoreslist.length;
    	for (int i = 0; i < n; i++) {
    		if(n%2==1)
			{median=scoreslist[((n+1)/2)-1];
			}
    		else
			{median=(double)(scoreslist[(n/2)-1]+scoreslist[n/2])/2;
			}		
		}
    	return median;
    }

	public double variance(int[] scoreslist) 
	{
    	double variance = 0.0;
    	double mean = mean(scoreslist);
    	int n = scoreslist.length;
    	for (int i = 0; i < n; i++) {
			variance = variance + Math.pow(scoreslist[i] - mean, 2);
		}
	    variance /= n;
    	return variance;
    }
    
	public double stdDeviation(int[] scoreslist) 
	{
    	double variance = variance(scoreslist);
    	double stdev = Math.sqrt(variance); 
    	return stdev;
    }
 
	
    /**
     * For running multiple games without visuals. This is useful to get a good idea of how well a controller plays
     * against a chosen opponent: the random nature of the game means that performance can vary from game to game. 
     * Running many games and looking at the average score (and standard deviation/error) helps to get a better
     * idea of how well the controller is likely to do in the competition.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param trials The number of trials to be executed
     */
    public List<Integer> runExperiment(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials)
    {
		List<Integer> scoreslist = new ArrayList<Integer>();//(declare on top for accessing it as global variable)
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
			}
			
			scoreslist.add(game.getScore());
		}

		int[] list = new int[scoreslist.size()];
		for(int i = 0; i < scoreslist.size(); i++){
			list[i] = scoreslist.get(i);
		}

		// calling the methods defined to find stats
		double mean = mean(list);
		System.out.println("mean of the scores is :"+mean);
		double median = median(list);
		System.out.println("median of the scores is :"+median);
		double variance = variance(list);
		System.out.println("variance of the scores is :"+ variance);
		double stdDev = stdDeviation(list);
		System.out.println("stdDev of the scores is :"+ stdDev);
		System.out.println();

		return scoreslist;
    }

	public void writeScoresToFile(List<Integer> test, List<Integer> control){

		PrintWriter writer = null;
		try{
			writer = new PrintWriter(new File("test.csv"));
			StringBuilder sb = new StringBuilder();
			for (int j=0;j<test.size();j++)
			{
				sb.append(test.get(j));
				sb.append(',');
				sb.append(control.get(j));
				sb.append('\n');
			}
			writer.write(sb.toString());
	        writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if (writer != null){
				try{
					writer.close();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public double runScoreExperiment(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials)
    {
    	double avgScore=0;
		double avgLevel=0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
			}
			
			avgScore+=game.getScore();
			avgLevel+=game.getCurrentLevel();
			//System.out.println(i+"\t"+game.getScore()+"\t Lvl " + game.getCurrentLevel());
		}
		
		System.out.println("Average Score: " + avgScore/trials);
		System.out.println("Average Level: " + avgLevel/trials);

		return avgScore;
    }

	public double runLevelExperiment(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,int trials, boolean verbose)
    {
    	double avgScore=0;
		double avgLevel=0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
			}
			
			avgScore+=game.getScore();
			avgLevel+=game.getCurrentLevel();
			if(verbose)
				System.out.println(i+"\t"+game.getScore()+"\t Lvl " + game.getCurrentLevel());
		}
		
		System.out.println("Average Score: " + avgScore/trials);
		System.out.println("Average Level: " + avgLevel/trials);

		return avgLevel;
    }
	
	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
	 * @param delay The delay between time-steps
	 */
	public void runGame(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int delay)
	{
		Game game=new Game(0);

		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getMove(game.copy(),-1),ghostController.getMove(game.copy(),-1));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
	}
	
	/**
     * Run the game with time limit (asynchronous mode). This is how it will be done in the competition. 
     * Can be played with and without visual display of game states.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual)
	{
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   
	        
	        if(visual)
	        	gv.repaint();
		}
		
		pacManController.terminate();
		ghostController.terminate();
	}
	
    /**
     * Run the game in asynchronous mode but proceed as soon as both controllers replied. The time limit still applies so 
     * so the game will proceed after 40ms regardless of whether the controllers managed to calculate a turn.
     *     
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param fixedTime Whether or not to wait until 40ms are up even if both controllers already responded
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimedSpeedOptimised(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean fixedTime,boolean visual)
 	{
 		Game game=new Game(0);
 		
 		GameView gv=null;
 		
 		if(visual)
 			gv=new GameView(game).showGame();
 		
 		if(pacManController instanceof HumanController)
 			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
 				
 		new Thread(pacManController).start();
 		new Thread(ghostController).start();
 		
 		while(!game.gameOver())
 		{
 			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
 			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

 			try
			{
				int waited=DELAY/INTERVAL_WAIT;
				
				for(int j=0;j<DELAY/INTERVAL_WAIT;j++)
				{
					Thread.sleep(INTERVAL_WAIT);
					
					if(pacManController.hasComputed() && ghostController.hasComputed())
					{
						waited=j;
						break;
					}
				}
				
				if(fixedTime)
					Thread.sleep(((DELAY/INTERVAL_WAIT)-waited)*INTERVAL_WAIT);
				
				game.advanceGame(pacManController.getMove(),ghostController.getMove());	
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
 	        
 	        if(visual)
 	        	gv.repaint();
 		}
 		
 		pacManController.terminate();
 		ghostController.terminate();
 	}
    
	/**
	 * Run a game in asynchronous mode and recorded.
	 *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param visual Whether to run the game with visuals
	 * @param fileName The file name of the file that saves the replay
	 */
	public void runGameTimedRecorded(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,String fileName)
	{
		StringBuilder replay=new StringBuilder();
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof HumanController)
				gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
		}		
		
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	        
	        
	        if(visual)
	        	gv.repaint();
	        
	        replay.append(game.getGameState()+"\n");
		}
		
		pacManController.terminate();
		ghostController.terminate();
		
		saveToFile(replay.toString(),fileName,false);
	}
	
	/**
	 * Replay a previously saved game.
	 *
	 * @param fileName The file name of the game to be played
	 * @param visual Indicates whether or not to use visuals
	 */
	public void replayGame(String fileName,boolean visual)
	{
		ArrayList<String> timeSteps=loadReplay(fileName);
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		for(int j=0;j<timeSteps.size();j++)
		{			
			game.setGameState(timeSteps.get(j));

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
	        if(visual)
	        	gv.repaint();
		}
	}
	
	//save file for replays
    public static void saveToFile(String data,String name,boolean append)
    {
        try 
        {
            FileOutputStream outS=new FileOutputStream(name,append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            System.out.println("Could not save data!");	
        }
    }  

    //load a replay
    private static ArrayList<String> loadReplay(String fileName)
	{
    	ArrayList<String> replay=new ArrayList<String>();
		
        try
        {         	
        	BufferedReader br =new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));	 
            String input=br.readLine();		
            
            while(input!=null)
            {
            	if(!input.equals(""))
            		replay.add(input);

            	input=br.readLine();	
            }
			br.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        return replay;
	}
}