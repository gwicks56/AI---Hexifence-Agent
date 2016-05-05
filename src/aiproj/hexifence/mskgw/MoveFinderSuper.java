package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class MoveFinderSuper implements IMoveFinder {
    
    private ArrayList<ArrayList<Hexagon>> Chains;
    private ArrayList<ArrayList<Edge>> DoubleDeals; 
    private ArrayList<Hexagon> offerChain;
    private Random random;
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    
    public MoveFinderSuper(Game game) {
        this.game = game;
        Hexagons = game.getHexagons();
        Edges = game.getEdges();    
        random = new Random();  
        Chains = new ArrayList<ArrayList<Hexagon>>();
        DoubleDeals = new ArrayList<ArrayList<Edge>>();

    }
    
    
    public Edge findMove() {
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();
        ArrayList<Edge> validMoves = new ArrayList<Edge>();
        ArrayList<Edge> captureMoves = new ArrayList<Edge>();
        
        // Generates safe(non-capturable), unsafe and capture moves
        for (Edge edge: Edges.values()) {
            if(edge.isMarked()) continue;
            validMoves.add(edge);
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
        offerChain = null;
        /* If there is one double deal move in the plate, try to see if its good to use it */
        /* This method also finds the best chain to offer when we are forced to */
        if(willSacrifice()) {
            return DoubleDeals.get(0).get(1);    
        }

        // If we decided not to sacrifice the double deal move then we capture them
        if(!captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
        }

        // If there is no other option, open up the best chain to offer    
        if(offerChain != null) {
            Hexagon hexagon = offerChain.get(0);
            for(Edge edge : hexagon.getEdges()) {
                if(!edge.isMarked()) {
                    return edge;
                }
            }
        }
        
        // Just to make it more error-proof
        if(!validMoves.isEmpty()) {
            return selectRandomly(validMoves);
        }
        return null;
    }
    
    /* This method finds the double-deal half closed chains
     * where we have the option of sacrificing two hexagons
     * in order to give the opponent the next turn
     */
    public void findDoubleDeals() {
        DoubleDeals.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            if(hexagon.getSidesTaken() != 5) continue;
            // Starting point is a hexagon with only 1 side free
            
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
            // That hexagon must be adjacent to a hexagon with 4 sides free
            if(parent == null || parent.getSidesTaken() != 4) continue;
            
            Edge currentNext = null;
            for(Edge edge: parent.getEdges()) {
                if(!edge.isMarked() && edge != current) {
                    currentNext = edge;
                    chain.add(edge);
                }
            }
            if(currentNext == null) continue;
            
            // The third hexagon cannot have 5 nor 6 sides captured
            Hexagon parentNext = currentNext.getOtherParent(parent);
            if (parentNext == null  || parentNext.getSidesTaken() < 4) {
                DoubleDeals.add(chain);
            } 
        }
        
    }
    
    /*
     * This method finds chains of hexagons with 4 sides captured
     */     
    public void findChains() {
        Chains.clear();
        for(Hexagon hexagon: Hexagons.values()) {
            hexagon.setVisited(false);
        }
        
        // For each hexagon with 4 sides taken
        // the search for chain is done recursively
        for(Hexagon hexagon: Hexagons.values()) {
            ArrayList<Hexagon> chain = new ArrayList<Hexagon>();
            if(hexagon.getSidesTaken() == 4) {
                findChains(hexagon, chain);
            }
            int chainSize = chain.size();
            if(chainSize > 0 && !chain.contains(null)) {
                Chains.add(chain);
            }
        }
    }
    
    /* This recursively finds a chain of hexagons with size 4 from
     * one starting point
     */
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
    
    /* This method determines whether the player should offer a
     * double dealing sacrificial move or not. It also finds
     * the best chain to offer when we are forced to.
     */
    private boolean willSacrifice() {
        /* Count of chains of size 2 or greater which CAN offer double deal sacrifice */
        int longChainsCount = 0;
        /* Count of chains of size 4 arranged in circular plus shape and CAN NOT offer double deal sacrifice */
        int plusChainsCount = 0;
        /* Count of chains of size 3 arranged in circular triangle and CAN NOT offer double deal sacrifice */
        int triangularChainsCount = 0;
        /* Count of chains 1 which CAN NOT offer double deal sacrifice */
        int chains1Count = 0;
        
        ArrayList<Hexagon> triangularChain = null;
        ArrayList<Hexagon> plusChain = null;
        int longChainsHexCount = 0;
        
        float minSize = Hexagons.size();
        
        boolean canDoubleDeal = false;
        for(ArrayList<Hexagon> chain : Chains) {
            float size = chain.size();
            
            if(IsPlusChain(chain)) {
                // These chains are considered smaller than usual
                // 4-length chains as double-dealing
                // sacrifice moves cannot be done on them
                plusChainsCount++;
                plusChain = chain;
                size = 3.5f;
            }
            
            else if(IsTriangularChain(chain)) {
                // These chains are considered smaller than usual
                // 3-length chains as double-dealing
                // sacrifice moves cannot be done on them
                size = 2.5f; 
                triangularChain = chain;
                triangularChainsCount++;
            }
            else if (chain.size() > 2) {
                longChainsCount++;     
                longChainsHexCount += chain.size();
            }
            else if(chain.size() == 1) {
                chains1Count++;
            }
            // Find the smallest chain for later use
            if(size < minSize) {
                offerChain = chain;
                canDoubleDeal = false;
                
                // If the offer chain is not triangular nor plus then it can't
                // be used for double dealing
                if((float)chain.size() == size) {
                    canDoubleDeal = true;
                }
                minSize = size;
            }
        }
        
        // The number of hexagons which can be won from long chains
        int longChainsHexWinCount = longChainsHexCount - Math.max((longChainsCount - 1) * 2, 0); 
        
        // If the smallest chain is of size 2 or 3 (that can be used as double-dealing)
        // Then if there is a non-double dealing chain (plus or triangular)
        // than we offer the latter if we can win enough points in long chains
        if(canDoubleDeal && offerChain.size() == 2) { //

            if(triangularChainsCount > 0) {
                if(longChainsHexWinCount >= 3) {
                    offerChain = triangularChain;
                }
            }
            else if(plusChainsCount > 0) {
                if(longChainsHexWinCount >= 4) {
                    offerChain = plusChain;
                }
            }
        } 
        else if(canDoubleDeal && offerChain.size() == 3) {
            if(plusChainsCount > 0) {
                if(longChainsHexWinCount >= 4) {
                    offerChain = plusChain;
                }
            }
        }
        
        if(DoubleDeals.size() != 1) {
            return false;
        }
        
        // Scores for double-dealing or not double dealing respectively
        int doubleDealScore = getNetScore(true, longChainsCount,plusChainsCount,
                triangularChainsCount, chains1Count);
        int normalScore = getNetScore(false, longChainsCount, plusChainsCount,
                triangularChainsCount,  chains1Count);
        
        game.setDoubleCrossedCount(game.getDoubleCrossedCount()+1);
        
        if(doubleDealScore > normalScore) {
            System.out.println("###############################################################################");
            System.out.println("SACRIFICE");
            printStatus(longChainsCount, triangularChainsCount, plusChainsCount, chains1Count);
            return true;
        }
        printStatus(longChainsCount, triangularChainsCount, plusChainsCount, chains1Count);
        return false;
    }
    
    public int getNetScore(boolean doubleDeal, int longChainsCount, int plusChainsCount,
            int triangularChainsCount, int chains1Count) {
        int score = 0;
        boolean myGain = false;
        
        // If I double-deal, I lose two points but I get the next chain
        if(doubleDeal) {
            myGain = true;
            score = -2;
        }
        
        // Adding small (non double-dealing chains) to a queue
        // Assuming opponent gives up the shortest non double-dealing chain
        Queue<Integer> queue = new LinkedList<Integer>();
        for(int i = 0; i < chains1Count; i++) {
            queue.add(1);
        }
        for(int i = 0; i < triangularChainsCount; i++) {
            queue.add(3);
        }
        for(int i = 0; i < plusChainsCount; i++) {
            queue.add(4);
        }
        
        // Whoever is in control, offers the opponent the next smallest
        // non double-dealing chain and so points are added and subtracted
        // in turns
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
        
        // The number of hexagons one can win from long chains
        int chainScore = hexagonsLeft - (plusChainsCount * 4)
                        - ((triangularChainsCount * 3) + (chains1Count * 1)) 
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
    
    
    /*
     * This method checks whether a open chain is actually a
     *  a closed circular plus-shaped (4) loop which cannot offer
     *  double dealing sacrificial.
     */
    public boolean IsPlusChain(ArrayList<Hexagon> chain) { 
        /* Only chains of size 4 can be plus-shaped */
        if(chain.size() != 4) return false;
        
        ArrayList<Hexagon> horizontals = new ArrayList<Hexagon>();
        ArrayList<Hexagon> verticals = new ArrayList<Hexagon>();
     
        // Only 2 hexagons share only 1 marked edge which are referred as horizontals    
        int sharedMarkedCount = 0;
        for(Hexagon h1 : chain) {
            for(Hexagon h2: chain) {
                if(h1 == h2) continue;          
                for(Edge edge: h1.getEdges()) {
                    if(edge.isMarked() && edge.isShared() && edge.getOtherParent(h1) == h2) {
                        sharedMarkedCount++;
                        if (!horizontals.contains(h1)) horizontals.add(h1);
                        if (!horizontals.contains(h2)) horizontals.add(h2);
                    }
                }
 
            }
        }
        // Two hexagons must share 1 edge (count is 2 here as it is done twice in loop)
        if(sharedMarkedCount != 2 || horizontals.size() !=2) return false;
        
        // The other two are verticals
        for(Hexagon h: chain) {
            if(!horizontals.contains(h)) {
                verticals.add(h);
            }
        }

        // Each of the two verticals must share a free edge with both horizontals
        for(Hexagon h : horizontals) {
            for(Hexagon v : verticals) {
                if(h == v) return false;
                boolean isShared = false;
                // Look for a free edge that is shared between the pair
                // of hexagons
                for(Edge edge : h.getEdges()) {                   
                    if (!edge.isMarked() && edge.getOtherParent(h) == v) {
                        isShared = true;
                        break;
                    }
                }
                // If a hexagon in h1 does not share a free edge 
                // with a different hexagon in the chain
                // then its not plus shaed
                if(!isShared) return false;
                
            }
        }  
        return true;
    }
    
    /*
     * This method checks whether a open chain is actually a
     *  a closed circular triangular (3) loop which cannot offer
     *  double dealing sacrificial.
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
    
    
    
    
    public void printStatus(int longChainsCount, int triangularChainsCount, int plusChainsCount, int chains1Count) {
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
        System.out.println("TOTAL CHAIN COUNTS: " + Chains.size());
        System.out.println("CHAIN L COUNTS: " + longChainsCount);
        System.out.println("CHAIN P COUNTS: " + plusChainsCount);
        System.out.println("CHAIN T COUNTS: " + triangularChainsCount);
        System.out.println("CHAIN 1 COUNTS: " + chains1Count);

        
        for(ArrayList<Hexagon> e : Chains) {
           System.out.println("CHAIN SIZE: " +e.size());
           
        }
        System.out.println("++++++++++++++++++++++++++++++++++++");
    }
    
}
