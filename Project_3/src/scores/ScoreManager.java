package scores;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Collections;

/**
 *
 * @author John M. Lasheski
 *
 * Based upon a demo found at http://forum.codecall.net/topic/50071-making-a-simple-high-score-system/
 */
public class ScoreManager {
   private static int highScore = 0;
   private static List<Scores> scores = new ArrayList<Scores>();
   public static Scores playerScore;

  //Initialising an in and outputStream for working with the file
  private static ObjectOutputStream outputStream = null;
  private static ObjectInputStream inputStream = null;

  // The name of the file where the highscores will be saved
  private static final String SCORES_FILE = "scores.dat";
   
  


  public static void initScoreManager() {
    loadHighScores();
    highScore = scores.get(0).getPlayerScore(); // set the highScore to beat
    initPlayerScore();
  }


  public static void initPlayerScore() {
      playerScore = new Scores("John", 0);
  }


  // Read in the high scores and store them in the Scores List   
  public static void loadHighScores() {
    try {
      inputStream = new ObjectInputStream(new FileInputStream(SCORES_FILE));
      scores = (ArrayList<Scores>) inputStream.readObject();
    } catch (FileNotFoundException e) {
      System.out.println("File Not Found Error: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("IO Error: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      System.out.println("Class Not Found Error: " + e.getMessage());
    }

    // After reading in the scores, sort them
    ScoresComparator comparator = new ScoresComparator();
    Collections.sort(scores, comparator);
  }

  // Write the high scores file
  public static void writeHighScores() {
    try {
      outputStream = new ObjectOutputStream(new FileOutputStream(SCORES_FILE));
      outputStream.writeObject(scores);
    } catch (FileNotFoundException e) {
      System.out.println("File Not Found Error: " + e.getMessage() + ",the program will try and make a new file");
    } catch (IOException e) {
      System.out.println("IO Error: " + e.getMessage());
    } finally {
      try {
        if (outputStream != null) {
          outputStream.flush();
          outputStream.close();
        }
      } catch (IOException e) {
        System.out.println("IO Error: " + e.getMessage());
      }
    }
  }

  // Return the single highest score as an integer only   
  public static int getHighScore() {
    return highScore;
  }


  public static String getHighScores() {
    String highScores = new String();
    int max = 10;

    for(int i = 0; i < max; i++) {
      highScores += (i + 1) + " : " + scores.get(i).getPlayerName() + "  : " + scores.get(i).getPlayerScore() + "\n";
    }

    return highScores;
  }

  // return true if the player has set a new highScore
  public static boolean newHighScore() {
    return (playerScore.getPlayerScore() > highScore);
  }





  // Enum used for calculating points to apply to score
  public static enum Points {
    BOULDER, BUGGY, BULLET, CRATE, ELEPHANT, EVILMONKEY
  }

  // Add points to the playerScore  
  public static void updateScore(Points point) {
    switch(point) {
    case BOULDER: playerScore.playerScore += 2; break;
    case BUGGY: playerScore.playerScore += 5; break;
    case BULLET: playerScore.playerScore --; break;
    case CRATE: playerScore.playerScore += 3; break;
    case ELEPHANT: playerScore.playerScore += 2; break;
    case EVILMONKEY: playerScore.playerScore += 10; break;
    }
  }

  

  

  
  
}