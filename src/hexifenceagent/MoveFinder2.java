package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MoveFinder2 implements IMoveFinder {
    private HashMap<ArrayList<Hexagon>, Integer> Chains;
    private Random random;
    
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    private int myColour;
    private int opColour;
    
    public MoveFinder2(Game game, int myColour, int opColour) {
        this.game = game;
        Hexagons = game.getHexagons();
        Edges = game.getEdges();
        
        random = new Random();  
        Chains = new HashMap<ArrayList<Hexagon>, Integer>();
        this.myColour = myColour;
        this.opColour = opColour;
    }
    
    
    public Edge findMove() {
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();
        ArrayList<Edge> unsafeMoves = new ArrayList<Edge>();
        // Tries to find any hexagon which is possible to capture
        for(Hexagon hexagon: game.getHexagons().values()) {
            if(hexagon.getSidesTaken() == 5)
                for(Edge edge: hexagon.getEdges()) {
                    if(!edge.isMarked()) {
                        return edge;
                    }
                }
        }
        // Generates a list of safe moves which won't enable
        // opponent to capture a hexagon

        for (Edge edge: game.getEdges().values()) {
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

}
