package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MoveFinder implements IMoveFinder {
    private HashMap<ArrayList<Hexagon>, Integer> Chains;
    private ArrayList<ArrayList<Edge>> DoubleDeals;  
    private Random random;
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    
    public MoveFinder(Game game) {
        this.game = game;
        Hexagons = game.getHexagons();
        Edges = game.getEdges();    
        random = new Random();  
        Chains = new HashMap<ArrayList<Hexagon>, Integer>();
        DoubleDeals = new ArrayList<ArrayList<Edge>>();

    }
    
    
    public Edge findMove() {
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();
        ArrayList<Edge> unsafeMoves = new ArrayList<Edge>();
        ArrayList<Edge> captureMoves = new ArrayList<Edge>();

        
        // Generates safe(non-capturable), unsafe and capture moves
        for (Edge edge: Edges.values()) {
            if(edge.isMarked()) continue;
            boolean isSafe = true;
            
            // Determine if the move is safe or not
            for(Hexagon parent: edge.getParents()) {
                // Edge with a parent of 5 sides can be captured
                if(parent.getSidesTaken() == 5) {
                    isSafe = false;
                    captureMoves.add(edge);
                    break;
                }
                // Edge with a parent of 4 sides is unsafe as 
                // opponent can capture it
                if (parent.getSidesTaken() == 4) {
                    isSafe = false;
                    unsafeMoves.add(edge);
                    break;
                }
            }
            // If neither unsafe nor can it be capture, its safe
            if(isSafe) {
                safeMoves.add(edge);
            }
        }
        
        
        // When there are safe moves and but no capture moves
        if(!safeMoves.isEmpty() && captureMoves.isEmpty()) {
            // Select a safe move randomly
            int index = random.nextInt(safeMoves.size());
            return safeMoves.get(index);
        }
        
        // When there are safe moves and moves you can capture
        // Then capture them as you can land a safe move to end turn
        if(!safeMoves.isEmpty() && !captureMoves.isEmpty()) {
            // Select a capture move randomly
            int index = random.nextInt(captureMoves.size());
            return captureMoves.get(index);
        }
        
        /* ALL MOVES ARE UNSAFE FROM THIS POINT */
        
        // FIND OPEN-CHAINS (SERIES OF SHARED 4-SIDES CAPTURED HEXAGONS)
        findChains();  
        // FIND DOUBLE DEALS (HALF-CLOSED CHAINS OF STRICTLY LENGTH 2 
        // (5-SIDE CAPTURED HEXAGON SHARED WITH 4 SIDE-CAPTURED HEXAGON)
        // THE LATTER HEXAGON IS NOT SHARED WITH A 4-SIDE-CAPTURED HEXAGON
        findDoubleDeals(); 
        
        System.out.println("HC SIZE: " + DoubleDeals.size());
       
        // If there are no double dealing moves but there are moved that can 
        // be captured, then capture it.
        if(DoubleDeals.isEmpty() && !captureMoves.isEmpty()) {
            int index = random.nextInt(captureMoves.size());
            return captureMoves.get(index);
        }
        
        // If there are 2 or more double dealing moves and a capture move, then
        // capture it as we will still have a double dealer to make use of later
        if(DoubleDeals.size() >= 2 && !captureMoves.isEmpty()) {
                int index = random.nextInt(captureMoves.size());
                return captureMoves.get(index);
        }
        
        // If there is a capture move that is not part of the single double deal
        // chain of moves, then capture it as it won't affect the double deal chain
        ArrayList<Edge> captureMovesNonDD = new ArrayList<Edge>();
        if(DoubleDeals.size() == 1 && !captureMoves.isEmpty()) {
            ArrayList<Edge> chain = DoubleDeals.get(0);
            for(Edge edge : captureMoves) {
                boolean notDD = true;
                for(Edge chainEdge : chain) {
                    if(chainEdge != edge) {
                        notDD = false;
                        break;
                    }
                }
                if(notDD) {
                    captureMovesNonDD.add(edge);
                }
            }
            if(!captureMovesNonDD.isEmpty()) {
                int index = random.nextInt(captureMovesNonDD.size());
                return captureMovesNonDD.get(index);
            }
        }
        
        // Find the lengths of OPEN chains of different sizes and
        // Find the smallest chain
        int chain1Count = 0;
        int chain2Count = 0;
        int chain3Count = 0; /* chain of size 3 or more */
        ArrayList<Hexagon> smallestChain = null;
        int minSize = Integer.MAX_VALUE;
        for(Map.Entry<ArrayList<Hexagon>,Integer> e : Chains.entrySet()) {
           int size = e.getValue();
           System.out.println("CHAIN SIZE: " + size);
            if (size < minSize) {
                minSize = size;
                smallestChain = e.getKey();
            }
            if(size == 1) chain1Count++;
            if(size == 2) chain2Count++;
            if(size >= 3) chain3Count++;
        }

        // Offer a double-deal (sacrifice) when there is a long chain 
        // and there are odd number or 0 number of shorter chains (size 1 or 2)
        // so that opponent opens up a long chain for you to capture
        int otherChainCount = chain1Count + chain2Count;
        if(!DoubleDeals.isEmpty() && chain3Count > 0 && ((otherChainCount == 0) || (otherChainCount % 2 != 0)) ) {
            System.out.println("##################################################SACRIFICE");
            return DoubleDeals.get(0).get(1);    
        }

        // If we still couldn't find a move but we have moves we can capture, then capture it
        if(!captureMoves.isEmpty()) {
            int index = random.nextInt(captureMoves.size());
            return captureMoves.get(index);
        }
       
        if(!unsafeMoves.isEmpty()) {          
            if(smallestChain != null) {
                Hexagon hexagon = smallestChain.get(0);
                for(Edge edge : hexagon.getEdges()) {
                    if(!edge.isMarked()) {
                        return edge;
                    }
                }
                
            }
            // Select unsafe move randomly if possible (if there is no shortest chain to open)
            // This won't happen as all unsafe moves has to be a chain length of 1 at least
            // But just putting it there in case there is an error in finding smallest chain
            int index = random.nextInt(unsafeMoves.size());
            return unsafeMoves.get(index);
        }
        System.out.println("NO MOVES ERROR");
        return null;
    }
    
    
    public void findDoubleDeals() {
        DoubleDeals.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            if(hexagon.getSidesTaken() != 5) continue;
            ArrayList<Edge> chain = new ArrayList<Edge>();
            Edge current = null;
            for(Edge edge : hexagon.getEdges()) {
                if(!edge.isMarked()) {
                    current = edge;
                    chain.add(edge);
                }
            }
            Hexagon parent = current.getOtherParent(hexagon);

            if(parent == null || parent.getSidesTaken() != 4) continue;
            
            for(Edge edge: parent.getEdges()) {
                if(!edge.isMarked() && edge != current) {
                    current = edge;
                    chain.add(edge);
                }
            }
            
            parent = current.getOtherParent(parent);
            if (parent == null  || parent.getSidesTaken() < 4) {
                DoubleDeals.add(chain);
                System.out.println("FOUND");
            } 
        }
        
    }

        
    public void findChains() {
        Chains.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            hexagon.setVisited(false);
        }
        
        for(Hexagon hexagon: Hexagons.values()) {
            ArrayList<Hexagon> chain = new ArrayList<Hexagon>();
            findChains(hexagon, chain);
            int chainSize = chain.size();
            if(chainSize > 0) {
                Chains.put(chain, chainSize);
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
                    if(adjacent.getSidesTaken() == 4) {
                        chain.add(adjacent);
                        findChains(current, chain); //REC STARTS
                    }
                }
            }
        }
    }
}
