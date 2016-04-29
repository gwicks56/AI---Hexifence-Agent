package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/* baseline + lets go of shortest chain */

public class MoveFinder3 implements IMoveFinder {
    private HashMap<ArrayList<Hexagon>, Integer> OpenChains;
    private Random random;
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    
    public MoveFinder3(Game game) {
        this.game = game;
        Hexagons = game.getHexagons();
        Edges = game.getEdges();    
        random = new Random();  
        OpenChains = new HashMap<ArrayList<Hexagon>, Integer>();

    }
    
    
    public Edge findMove() {
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();
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
        
        /* ALL MOVES ARE UNSAFE FROM THIS POINT */
        
        // FIND OPEN-CHAINS (SERIES OF SHARED 4-SIDES CAPTURED HEXAGONS)
        findChains();  
        
        ArrayList<Hexagon> smallestChain = null;
        int minSize = Integer.MAX_VALUE;
        for(Map.Entry<ArrayList<Hexagon>,Integer> e : OpenChains.entrySet()) {
            int size = e.getValue();
            if (size < minSize) {
                minSize = size;
                smallestChain = e.getKey();
            }
        }

        Hexagon hexagon = smallestChain.get(0);
        for(Edge edge : hexagon.getEdges()) {
            if(!edge.isMarked()) {
                return edge;
            }
        }
        
        System.out.println("NO MOVES ERROR");
        return null;
    }
        
            
    public void findChains() {
        OpenChains.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            hexagon.setVisited(false);
        }
        
        for(Hexagon hexagon: Hexagons.values()) {
            ArrayList<Hexagon> chain = new ArrayList<Hexagon>();
            findChains(hexagon, chain);
            int chainSize = chain.size();
            if(chainSize > 0) {
                OpenChains.put(chain, chainSize);
            }
        }
    }
    
    public void findChains(Hexagon current, ArrayList<Hexagon> chain) {
        if(current.isVisited()) return;
        current.setVisited(true);
        if(current.getSidesTaken() == 4) {
            chain.add(current);
            for(Edge edge: current.getEdges()) {
                if(!edge.isMarked() && edge.isShared()) {
                    Hexagon adjacent = edge.getOtherParent(current);
                    findChains(adjacent, chain); //REC STARTS
                }
            }
        }
    }
    
    public Edge selectRandomly(ArrayList<Edge> edges) {
        int index = random.nextInt(edges.size());
        return edges.get(index);
    }
}
