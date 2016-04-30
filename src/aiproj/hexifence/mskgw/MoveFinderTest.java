package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
// the original movefinder
public class MoveFinderTest implements IMoveFinder {
    private HashMap<ArrayList<Hexagon>, Integer> OpenChains;
    private ArrayList<ArrayList<Edge>> DoubleDeals;  
    private Random random;
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    
    public MoveFinderTest(Game game) {
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
                }
                // Edge with a parent of 4 sides is unsafe as 
                // opponent can capture it
                if (parent.getSidesTaken() == 4) {
                    isSafe = false;
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
            return selectRandomly(safeMoves);
        }
        
        // When there are safe moves and moves you can capture
        // Then capture them as you can land a safe move to end turn
        if(!safeMoves.isEmpty() && !captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
        }
        
        /* ALL MOVES ARE NON-SAFE FROM THIS POINT (UNSAFE OR CAPTURABLE) */
        
        

        
        // FIND OPEN-CHAINS (SERIES OF SHARED 4-SIDES CAPTURED HEXAGONS)
        findChains();  
        // Find the lengths of OPEN chains of different sizes and
        // Find the smallest chain
        int chain1Count = 0; /* count of open-chains of size 1 (just 1 hexagon) */
        int chain2Count = 0; /* count of open-chains of size 2 */
        int chain3Count = 0; /* count of long open-chains of size 3 or more */
        
        ArrayList<Hexagon> smallestChain = null;
        int minSize = Integer.MAX_VALUE;
        for(Map.Entry<ArrayList<Hexagon>,Integer> e : OpenChains.entrySet()) {
            int size = e.getValue();
            if (size < minSize) {
                minSize = size;
                smallestChain = e.getKey();
            }
            if(size == 1) chain1Count++;
            if(size == 2) chain2Count++;
            if(size >= 3) chain3Count++;
        }

        // Offer a double-deal (sacrifice) when there is a long chain 
        // and there are even number of short chains (size 1 or 2)
        // so that opponent opens up long chain(s) for you to capture later
        
        // FIND DOUBLE DEALS (HALF-CLOSED CHAINS OF STRICTLY LENGTH 2 
        // (5-SIDE CAPTURED HEXAGON SHARED WITH 4 SIDE-CAPTURED HEXAGON)
        // THE LATTER HEXAGON IS NOT SHARED WITH A 4-SIDE-CAPTURED HEXAGON
        findDoubleDeals(); 
        printStatus(safeMoves, captureMoves, chain3Count, chain2Count, chain1Count);
        // If there are 0 or 2 or more double dealing moves and a capture move, then
        // capture it as we will still have a double dealer to make use of later
        if((DoubleDeals.isEmpty() || DoubleDeals.size() >= 2) && !captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
        }
        
        // If there is a capture move that is not part of the single double deal
        // chain of moves, then capture it as it won't affect the double deal chain
        ArrayList<Edge> captureMovesNonDD = new ArrayList<Edge>();
        if(DoubleDeals.size() == 1 && !captureMoves.isEmpty()) {
            ArrayList<Edge> chain = DoubleDeals.get(0);
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
            if(!captureMovesNonDD.isEmpty()) {
                return selectRandomly(captureMovesNonDD);
            }
        }
        int otherChainCount = chain1Count + chain2Count;
        
        if(DoubleDeals.size() == 1 && chain3Count > 0 && otherChainCount % 2 == 0) {
            System.out.println("##################################################SACRIFICE");
            return DoubleDeals.get(0).get(1);    
        }

        // If we decided not to sacrifice the double deal move then we capture them
        if(!captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
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
        OpenChains.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            hexagon.setVisited(false);
        }
        
        for(Hexagon hexagon: Hexagons.values()) {
            ArrayList<Hexagon> chain = new ArrayList<Hexagon>();
            if(hexagon.getSidesTaken() == 4) {
                findChains(hexagon, chain);
            }
            int chainSize = chain.size();
            if(chainSize > 0 && !chain.contains(null)) {
                OpenChains.put(chain, chainSize);
            }
        }
    }
    
    public void findChains(Hexagon current, ArrayList<Hexagon> chain) {
        if(current.isVisited()) return;
        current.setVisited(true);
        if(current.getSidesTaken() > 4) {
            // then the chain is closed, so its not an open chain
            // add null so that we know
            chain.add(null);
        }
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
    
    public void printStatus(ArrayList<Edge>safeMoves, ArrayList<Edge>captureMoves, int chain3Count, int chain2Count, int chain1Count) {
        int notCaptured = 0;
        int size3 = 0, size2 = 0, size1 = 0, size0 = 0, size4 = 0, size5 = 0;
        for(Hexagon hexagon: Hexagons.values()) {
            if(hexagon.getSidesTaken() != 6) {
                notCaptured++;
            }
            if(hexagon.getSidesTaken() == 5) {
                size5++;
            }
            if(hexagon.getSidesTaken() == 4) {
                size4++;
            }
            if(hexagon.getSidesTaken() == 3) {
                size3++;
            }
            if(hexagon.getSidesTaken() == 2) {
                size2++;
            }
            if(hexagon.getSidesTaken() == 1) {
                size1++;
            }
            if(hexagon.getSidesTaken() == 0) {
                size0++;
            }
        }
        System.out.println("DOUBLE DEAL SIZE: " + DoubleDeals.size());
        System.out.println("HEXAGONS LEFT: " + notCaptured);
        System.out.println("HEXAGONS WITH SIZE 5: " + size5);
        System.out.println("HEXAGONS WITH SIZE 4: " + size4);
        System.out.println("HEXAGONS WITH SIZE 3: " + size3);
        System.out.println("HEXAGONS WITH SIZE 2: " + size2);
        System.out.println("HEXAGONS WITH SIZE 1: " + size1);
        System.out.println("HEXAGONS WITH SIZE 0: " + size0);
        int enc = 0;
        for(Edge edge: Edges.values()) {
            if(!edge.isMarked()) {
                enc++;
            }
        }
        System.out.println("EDGES LEFT: " + enc);
        System.out.println("LONG CHAIN COUNTS: " + chain3Count);
        System.out.println("EDGES LEFT: " + enc);
        System.out.println("SAFE MOVE COUNT: " + safeMoves.size());
        System.out.println("CHAIN 2 COUNTS: " + chain2Count);
        System.out.println("CHAIN 1 COUNTS: " + chain1Count);
        System.out.println("CAPTURE MOVES: " + captureMoves.size());
        
        for(Map.Entry<ArrayList<Hexagon>,Integer> e : OpenChains.entrySet()) {
           ArrayList<Hexagon> chain = e.getKey();
           System.out.println("CHAIN SIZE: " +e.getValue());
           
        }
    }
}