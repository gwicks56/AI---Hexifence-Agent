package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
// the original movefinder
public class MoveFinderSuper implements IMoveFinder {
    private ArrayList<ArrayList<Hexagon>> OpenChains;
    private ArrayList<ArrayList<Edge>> DoubleDeals; 
    private ArrayList<Hexagon> smallestChain;
    private Random random;
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    
    public MoveFinderSuper(Game game) {
        this.game = game;
        Hexagons = game.getHexagons();
        Edges = game.getEdges();    
        random = new Random();  
        OpenChains = new ArrayList<ArrayList<Hexagon>>();
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
                }
                // Edge with a parent of 4 sides is unsafe as 
                // opponent can capture it
                if (parent.getSidesTaken() == 4) {
                    isSafe = false;
                    unsafeMoves.add(edge);
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

        
        // FIND DOUBLE DEALS (HALF-CLOSED CHAINS OF STRICTLY LENGTH 2 
        // (5-SIDE CAPTURED HEXAGON SHARED WITH 4 SIDE-CAPTURED HEXAGON)
        // THE LATTER HEXAGON IS NOT SHARED WITH A 4-SIDE-CAPTURED HEXAGON
        findDoubleDeals(); 
        System.out.println("DOUBLE DEAL SIZE: " + DoubleDeals.size());
        //printStatus(safeMoves, captureMoves, chain3Count, chain2Count, chain1Count);
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
        smallestChain = null;
        /* If there is one double deal move in the plate, try to see if its good to use it */
        if(willSacrifice(safeMoves, captureMoves)) {
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
            if(current == null) continue;
            
            Hexagon parent = current.getOtherParent(hexagon);

            if(parent == null || parent.getSidesTaken() != 4) continue;
            
            Edge currentNext = null;
            for(Edge edge: parent.getEdges()) {
                if(!edge.isMarked() && edge != current) {
                    currentNext = edge;
                    chain.add(edge);
                }
            }
            if(currentNext == null) continue;
            
            Hexagon parentNext = currentNext.getOtherParent(parent);
            if (parentNext == null  || parentNext.getSidesTaken() < 4) {
                DoubleDeals.add(chain);
                System.out.println("DD FOUND");
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
                OpenChains.add(chain);
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
    
    public void printStatus(int longChainsCount, int triangularChainsCount, int chains2Count, int chains1Count) {
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
        System.out.println("++++++++++++++++++++++++++++++++++++");
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
        System.out.println("TOTAL CHAIN COUNTS: " + OpenChains.size());
        System.out.println("CHAIN L COUNTS: " + longChainsCount);
        System.out.println("CHAIN T COUNTS: " + triangularChainsCount);
        System.out.println("CHAIN 2 COUNTS: " + chains2Count);
        System.out.println("CHAIN 1 COUNTS: " + chains1Count);

        
        for(ArrayList<Hexagon> e : OpenChains) {
           System.out.println("CHAIN SIZE: " +e.size());
           
        }
        System.out.println("++++++++++++++++++++++++++++++++++++");
    }
    
    
    /*
     * This method checks whether a open chain is triangular or
     * not.  
     */
    private boolean IsTriangularChain(ArrayList<Hexagon> chain) {
        /* Only chains of size 3 can be triangular */
        if(chain.size() != 3) return false;
        
        /* Every hexagon in triangular chain shares a free edge with
         * the other two hexagons in the chain.
         */
        for(Hexagon h1 : chain) {
            for(Hexagon h2 : chain) {
                // ignore checking for same hexagons
                if (h2 == h1) continue;
                boolean isShared = false;
                // Look for a free edge that is shared between the pair
                // of hexagons
                for(Edge edge : h1.getEdges()) {                   
                    if (!edge.isMarked() && edge.getOtherParent(h1) == h2) {
                        isShared = true;
                        break;
                    }
                }
                // If a hexagon in h1 does not share a free edge 
                // with a different hexaogn in the chain
                // then its not triangular
                if(!isShared) return false;
            }
        }
        
        return true;
    }
    
    /* This method determines whether the player should offer a
     * double dealing sacrificial move or not. It also finds
     * the smallest chain.
     */
    private boolean willSacrifice(ArrayList<Edge> sm, ArrayList<Edge> cm) {
        /* Count of chains of size 3 or greater which CAN offer double deal sacrifice */
        int longChainsCount = 0;
        /* Count of chains 3 arranged in triangle and can not offer double deal sacrifice */
        int triangularChainsCount = 0;
        /* Count of chains 2 which cannot offer double deal sacrifice */
        int chains2Count = 0;
        /* Count of chains 1 which cannot offer double deal sacrifice */
        int chains1Count = 0;
        
              
        float minSize = Hexagons.size();

        for(ArrayList<Hexagon> chain : OpenChains) {
            float size = chain.size();

            if(IsTriangularChain(chain)) {
                System.out.println("TRIANGULAR FOUND");
                // These chains are considered smaller as double-dealing
                // sacrifice moves cannot be done on them
                size = 2.5f; 
                triangularChainsCount++;
            }
            else if (chain.size() > 2) {
                longChainsCount++;          
            }
            else if (chain.size() == 2) {
                chains2Count++;
            }
            else if(chain.size() == 1) {
                chains1Count++;
            }
            // Find the smallest chain for later use
            if(size < minSize) {
                smallestChain = chain;
                minSize = size;
            }
            
        }
        
        if(DoubleDeals.size() != 1) {
            return false;
        }

        // Scores for double-dealing or not double dealing respectively
        int doubleDealScore = getNetScore(true, longChainsCount, triangularChainsCount, chains2Count, chains1Count);
        int normalScore = getNetScore(false, longChainsCount, triangularChainsCount, chains2Count, chains1Count);
        
        if(doubleDealScore > normalScore) {
            System.out.println("################################SACRIFICE###############################################");
            printStatus(longChainsCount, triangularChainsCount, chains2Count, chains1Count);
            System.out.println("sm: " + sm.size());
            System.out.println("cm: " + cm.size());
            return true;
        }
        printStatus(longChainsCount, triangularChainsCount, chains2Count, chains1Count);
        System.out.println("sm: " + sm.size());
        System.out.println("cm: " + cm.size());
        return false;
    }
    
    public int getNetScore(boolean doubleDeal, int longChainsCount, int triangularChainsCount, int chains2Count, int chains1Count) {
        int score = 0;
        boolean myGain = false;
        
        if(doubleDeal) {
            myGain = true;
            score = -2;
        }
        
        Queue<Integer> queue = new LinkedList<Integer>();
        for(int i = 0; i < chains1Count; i++) {
            queue.add(1);
        }
        for(int i = 0; i < chains2Count; i++) {
            queue.add(2);
        }
        for(int i = 0; i < triangularChainsCount; i++) {
            queue.add(3);
        }
        
        while(!queue.isEmpty()) {
            int points = queue.poll();
            if(myGain) {
                score += points;
            }
            else {
                score -= points;
            }
            myGain = !myGain;
        }
        
        int hexagonsLeft = 0;
        for(Hexagon hexagon: Hexagons.values()) {
            if(hexagon.getSidesTaken() != 6) {
                hexagonsLeft++;
            }
        }
        
        int chainScore = hexagonsLeft
                        - ((triangularChainsCount * 3) + (chains2Count * 2) + (chains1Count * 1)) 
                        - (Math.max((longChainsCount - 1) * 2, 0))
                        - 2;
        
        if (myGain) {
            score += chainScore;
        }
        else {
            score -= chainScore;
        }
        return score;
    }
    
}
