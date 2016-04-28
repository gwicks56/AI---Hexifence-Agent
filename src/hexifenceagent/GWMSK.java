package hexifenceagent;

import aiproj.hexifence.*;

import java.awt.Point;
import java.io.PrintStream;


public class GWMSK implements Player, Piece {

    private Game game;
    private int myColour;
    private int opColour;
    private int maxScore;
    private IMoveFinder moveFinder;


    
    @Override
    public int init(int n, int p) {
        // Check for invalid board dimension or colour
        if(n != 2 && n != 3 && p != BLUE && p != RED) {
            return -1;
        }
        // Initialise the internal game configuration
        try {
            game = new Game(n);
        } catch (Exception e) {
            return -1;
        }
        
        // Set the colour of the player and the opponent
        myColour = p;
        if(myColour == RED) {
            opColour = BLUE;
        }
        else {
            opColour = RED;
        }
        maxScore = game.getHexagons().size();
        moveFinder = new MoveFinder(game, myColour, opColour);
        return 0;
    }

    @Override
    public Move makeMove() {
        // Find a move
        Edge edge = moveFinder.findMove();
        // Make the move internally
        game.makeMove(edge, myColour);
        // Get position of the move
        Point pos = edge.getPosition();     
        // Return the move
        Move move = new Move();
        move.Col = pos.x;
        move.Row = pos.y;
        move.P = myColour;
        return move;
    }

    @Override
    public int opponentMove(Move m) {
        // Find position of opponent move
        Point pos = new Point(m.Col, m.Row);
        // Make the move internally
        int status = game.makeMove(game.getEdges().get(pos), opColour);
        // Return the status of the move
        return status;
    }

    @Override
    public int getWinner() {
        
        int myScore = game.getScore(myColour);
        int opScore = game.getScore(opColour);
        int totalScore = myScore + opScore;
        
        if(totalScore == maxScore) {
            System.out.println("PLAYER SCORE: " + myScore + "  OPPONENT SCORE: " + opScore);
            if(myScore > opScore) {
                return myColour;
            }
            else if(myScore < opScore) {
                return opColour;
            }
            else {
                return DEAD;
            }
        }
        if(totalScore > maxScore) {
            return INVALID;
        }
       
        return EMPTY;
    }

    @Override
    public void printBoard(PrintStream output) {
        game.printBoard(output);
    }
    
    
}
