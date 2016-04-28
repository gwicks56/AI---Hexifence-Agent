package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MoveFinder implements IMoveFinder {
    private HashMap<ArrayList<Hexagon>, Integer> Chains;
    private ArrayList<ArrayList<Edge>> HalfClosedChains;
    
    private Random random;
    
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    private int myColour;
    private int opColour;
    
    public MoveFinder(Game game, int myColour, int opColour) {
        this.game = game;
        Hexagons = game.getHexagons();
        Edges = game.getEdges();
        
        random = new Random();  
        Chains = new HashMap<ArrayList<Hexagon>, Integer>();
        HalfClosedChains = new ArrayList<ArrayList<Edge>>();
        this.myColour = myColour;
        this.opColour = opColour;
    }
    
    
    public Edge findMove() {
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();
        ArrayList<Edge> unsafeMoves = new ArrayList<Edge>();
        ArrayList<Edge> captureMoves = new ArrayList<Edge>();

        
        // Generates a list of safe moves which won't enable
        // opponent to capture a hexagon
        for (Edge edge: Edges.values()) {
            if(edge.isMarked()) continue;
            boolean isSafe = true;
            
            // Determine if the move is safe or not
            for(Hexagon parent: edge.getParents()) {
                if(parent.getSidesTaken() == 5) {
                    isSafe = false;
                    captureMoves.add(edge);
                    break;
                }
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
        
        if(!safeMoves.isEmpty() && captureMoves.isEmpty()) {
            // Select a safe move randomly if possible
            int index = random.nextInt(safeMoves.size());
            return safeMoves.get(index);
        }
        
        if(!safeMoves.isEmpty() && !captureMoves.isEmpty()) {
            // Select a capture move randomly if there is at least one capture move and one safe move
            int index = random.nextInt(captureMoves.size());
            return captureMoves.get(index);
        }
        
        findChains();
        findHalfClosedChains();
       
       
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
//        System.out.println("CHAIN 1: " + chain1Count);
//        System.out.println("CHAIN 2: " + chain2Count);
//        System.out.println("CHAIN 3: " + chain3Count);
        
        System.out.println("HC SIZE: " + HalfClosedChains.size());
        
        if(HalfClosedChains.isEmpty() && !captureMoves.isEmpty()) {
            int index = random.nextInt(captureMoves.size());
            return captureMoves.get(index);
        }
        
        // TO:DO have to capture moves greedily when no safe move and there is one capturable move not part of half-chain
        
        /*if(HalfClosedChains.size() >= 2 && !captureMoves.isEmpty()) {
            if(!captureMoves.isEmpty()) {
                // Select a capture move randomly if there is at least one capture move and one safe move
                int index = random.nextInt(captureMoves.size());
                return captureMoves.get(index);
            }
        } */
        
        // Sacrifice
        if(!HalfClosedChains.isEmpty()) {
            System.out.println("##################################################SACRIFICE");
            return HalfClosedChains.get(0).get(1);    
        }

       
        if(!captureMoves.isEmpty()) {
            // Select a capture move randomly if there is at least one capture move and one safe move
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
            // Select a safe move randomly if possible
            int index = random.nextInt(unsafeMoves.size());
            return unsafeMoves.get(index);
        }
        System.out.println("NO MOVES ERROR");
        return null;
    }
    
    
    public void findHalfClosedChains() {
        HalfClosedChains.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            
            System.out.println("check hex");
            if(hexagon.getSidesTaken() != 5) continue;
            ArrayList<Edge> chain = new ArrayList<Edge>();
            Edge current = null;
            for(Edge edge : hexagon.getEdges()) {
                System.out.println("check edge");

                if(!edge.isMarked()) {
                    current = edge;
                    chain.add(edge);
                }
            }
            Hexagon parent = current.getOtherParent(hexagon);
            System.out.println("check parent1");

            if(parent == null || parent.getSidesTaken() != 4) continue;
            
            for(Edge edge: parent.getEdges()) {
                System.out.println("check parent1 edge");

                if(!edge.isMarked() && !edge.getPosition().equals(current.getPosition())) {
                    current = edge;
                    chain.add(edge);
                }
            }
            
            parent = current.getOtherParent(hexagon);
            System.out.println("check parent2");
            if (parent == null  || parent.getSidesTaken() < 4) {
                HalfClosedChains.add(chain);
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
