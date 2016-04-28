package hexifenceagent;

import aiproj.hexifence.*;

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class GWMSK implements Player, Piece {

    private Game game;
    private int myColour;
    private int opColour;
    private int maxScore;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    private HashMap<ArrayList<Hexagon>, Integer> Chains;
    private Random random;
    
    @Override
    public int init(int n, int p) {
        // Check for invalid board dimension or colour
        if(n != 2 && n != 3 && p != BLUE && p != RED) {
            return -1;
        }
        // Initialise the internal game configuration
        try {
            game = new Game(n);
            Hexagons = game.getHexagons();
            Edges = game.getEdges();
        } catch (Exception e) {
            return -1;
        }
        
        random = new Random();  
        Chains = new HashMap<ArrayList<Hexagon>, Integer>();
        
        
        
        // Set the colour of the player and the opponent
        myColour = p;
        if(myColour == RED) {
            opColour = BLUE;
        }
        else {
            opColour = RED;
        }
        maxScore = Hexagons.size();
        
        return 0;
    }

    @Override
    public Move makeMove() {
        // Find a move
        Edge edge = findMove();
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
        int status = game.makeMove(Edges.get(pos), opColour);
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
    
    
    public Edge findMove() {
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();
        ArrayList<Edge> unsafeMoves = new ArrayList<Edge>();
        // Tries to find any hexagon which is possible to capture
        for(Hexagon hexagon: Hexagons.values()) {
            if(hexagon.getSidesTaken() == 5)
                for(Edge edge: hexagon.getEdges()) {
                    if(!edge.isMarked()) {
                        return edge;
                    }
                }
        }
        // Generates a list of safe moves which won't enable
        // opponent to capture a hexagon

        for (Edge edge: Edges.values()) {
            if(edge.isMarked()) continue;
            boolean isSafe = true;
            
            // Determine if the move is safe or not
            for(Hexagon parent: edge.getParents()) {
                if (parent.getSidesTaken() == 4) {
                    isSafe = false;
                    unsafeMoves.add(edge);
                    break;
                }
            }
            if(isSafe) {
                safeMoves.add(edge);
            }
        }
        if(!safeMoves.isEmpty()) {
            // Select a safe move randomly if possible
            int index = random.nextInt(safeMoves.size());
            return safeMoves.get(index);
        }
        if(!unsafeMoves.isEmpty()) {
            // Select a safe move randomly if possible
            int index = random.nextInt(unsafeMoves.size());
            return unsafeMoves.get(index);
        }
        System.out.println("NO MOVES ERROR");
        return null;
    }
    
    
    public void findChains() {
        Chains.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            hexagon.setVisited(false);
        }
        
        for(Hexagon hexagon: Hexagons.values()) {
            ArrayList<Hexagon> chain = new ArrayList<Hexagon>();
            FindChains(hexagon, chain);
            int chainSize = chain.size();
            if(chainSize > 0) {
                Chains.put(chain, chainSize);
            }
        }
    }
    
    public void FindChains(Hexagon current, ArrayList<Hexagon> chain) {
        if(!current.isVisited()) return;
        current.setVisited(true);
        if(current.getSidesTaken() == 4) {
            chain.add(current);
            for(Edge edge: current.getEdges()) {
                if(!edge.isMarked() && edge.isShared()) {
                    Hexagon adjacent = edge.getOtherParent(current);
                    if(adjacent.getSidesTaken() == 4) {
                        chain.add(adjacent);
                        FindChains(current, chain); //REC STARTS
                    }
                }
            }
        }
    }
    
}
