package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MoveFinderNew implements IMoveFinder {
    private HashMap<ArrayList<Hexagon>, Integer> OpenChains;
    private ArrayList<ArrayList<Edge>> DoubleDeals;  
    private Random random;
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;

    
    public MoveFinderNew(Game game) {
        this.game = game;
        Hexagons = game.getHexagons();
        Edges = game.getEdges();    
        random = new Random();  
        OpenChains = new HashMap<ArrayList<Hexagon>, Integer>();
        DoubleDeals = new ArrayList<ArrayList<Edge>>();

    }
    
    
    public Edge findMove() {
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();
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
                    break;
                }
            }
            // If neither unsafe nor can it be capture, its safe
            if(isSafe) {
                safeMoves.add(edge);
            }
        }

        // When there are safe moves and moves you can capture
        // Then capture them as you can land a safe move to end turn
        // These are OPPONENT MISTAKES: SO CAPITALISE
        if(!safeMoves.isEmpty() && !captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
        }
        
        
        // FIND DOUBLE DEALS (HALF-CLOSED CHAINS OF STRICTLY LENGTH 2 
        // (5-SIDE CAPTURED HEXAGON SHARED WITH 4 SIDE-CAPTURED HEXAGON)
        // THE LATTER HEXAGON IS NOT SHARED WITH A 4-SIDE-CAPTURED HEXAGON
        //findDoubleDeals();
        findDoubleDeals();
        
        // Offer a double-deal sacrifice move to opponent to keep
        // control
        if(!DoubleDeals.isEmpty()) {
            
            int notCaptured = 0;
            for(Hexagon hexagon: Hexagons.values()) {
                if(hexagon.getSidesTaken() != 6) {
                    notCaptured++;
                }
            }
            // sacrifice except the last chain
            System.out.println("##################################################SACRIFICE");
            if(notCaptured > 3) return DoubleDeals.get(0).get(1);
        }
        
        // When there are safe moves and but no capture moves
        if(!safeMoves.isEmpty() && captureMoves.isEmpty()) {
            // Select a safe move randomly
            return selectRandomly(safeMoves);
        }

                
        // If there is a capture move that is not part of the single double deal
        // chain of moves, then capture it as it won't affect the double deal chain
        ArrayList<Edge> captureMovesNonDD = new ArrayList<Edge>();
        if(!DoubleDeals.isEmpty() && !captureMoves.isEmpty()) {
            for(ArrayList<Edge> chain: DoubleDeals) {
                for(Edge edge : captureMoves) {
                    boolean isDD = false;
                    for(Edge chainEdge : chain) {
                        if(chainEdge == edge) {
                            isDD = true;
                            break;
                        }
                    }
                    if(!isDD) {
                        captureMovesNonDD.add(edge);
                    }
                }
            }
            if(!captureMovesNonDD.isEmpty()) {
                return selectRandomly(captureMovesNonDD);
            }
        }
    
        
        // If we decided not to sacrifice the double deal move then we capture them
        if(!captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
        }
        
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

        
        // If there is no other option, open up the smallest chain
        Hexagon hexagon = smallestChain.get(0);
        for(Edge edge : hexagon.getEdges()) {
            if(!edge.isMarked()) {
                return edge;
            }
        }
  
        System.out.println("ERROR: NO MOVE");
        return null;
    }
    
    public void findDoubleDeals() {
        DoubleDeals.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            hexagon.setVisited(false);
        }
        for(Hexagon hexagon: Hexagons.values()) {
            if(hexagon.getSidesTaken() != 5 || hexagon.isVisited()) continue;
            hexagon.setVisited(true);
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
            if (parent == null  || parent.getSidesTaken() < 4 || parent.getSidesTaken() == 5) {
                DoubleDeals.add(chain);
                System.out.println("FOUND");
            } 
        }
        
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
