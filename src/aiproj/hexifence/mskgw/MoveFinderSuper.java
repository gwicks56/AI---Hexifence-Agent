/*
 * Geordie Wicks [185828] and Mubashwer Salman Khurshid [601738]
 * Project 1 - Artificial Intelligence
 */


package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


/*
 * This class implements our AI behaviour
 * Primarily concerned with looking for chains of hexagons
 * Chains are classified into their diffent types including
 * chains which can be used for double dealing
 */

public class MoveFinderSuper implements IMoveFinder {
    
    private ArrayList<Chain> Chains;                 // Array of chains
    private ArrayList<ArrayList<Edge>> DoubleDeals;  // Edges for double dealing
    private Chain offerChain;              // chain to be offered to opponent
    private Random random;
    private Game game;
    private HashMap<Point, Hexagon> Hexagons;
    private HashMap<Point, Edge> Edges;
    
    
    public MoveFinderSuper(Game game) {
        this.game = game;
        Hexagons = game.getHexagons();                  // get all the hexagons
        Edges = game.getEdges();                        // get all the edges
        random = new Random();  
        Chains = new ArrayList<Chain>();
        DoubleDeals = new ArrayList<ArrayList<Edge>>();

    }
    
    
    public Edge findMove() {
        // Array of moves that will not give opponent a capture
        ArrayList<Edge> safeMoves = new ArrayList<Edge>();      
        // All valid moves
        ArrayList<Edge> validMoves = new ArrayList<Edge>();  
        // moves which capture hexagon 
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
        
        // If there are 0 or 2 or more double dealing moves and a capture move, 
        // then capture it as we will still have a double dealer to make use of 
        // later
        if((DoubleDeals.isEmpty() || DoubleDeals.size() >= 2)
         && !captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
        }
        
        // If there is a capture move that is not part of the single double deal
        // chain of moves, then capture it as it won't affect the double 
        // deal chain
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
        
        /* If there is one double deal move in the plate, try to see if its 
        good to use it */
        if(willSacrifice()) {
            return DoubleDeals.get(0).get(1);    
        }

        // If we decided not to sacrifice the double deal move then 
        // we capture them
        if(!captureMoves.isEmpty()) {
            return selectRandomly(captureMoves);
        }

        // If there is no other option, open up the best chain to offer       
        FindOfferChain();
        if(offerChain != null) {
            Hexagon hexagon = offerChain.getChainUnits().get(0);
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
    private void findDoubleDeals() {
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
    private void findChains() {
        Chains.clear();
        Chain.initialize();
        for(Hexagon hexagon: Hexagons.values()) {
            hexagon.setVisited(false);
        }
        
        // For each hexagon with 4 sides taken
        // the search for chain is done recursively
        for(Hexagon hexagon: Hexagons.values()) {
            ArrayList<Hexagon> chainUnits = new ArrayList<Hexagon>();
            if(hexagon.getSidesTaken() == 4) {
                findChains(hexagon, chainUnits);
            }
            int chainSize = chainUnits.size();
            if(chainSize > 0 && !chainUnits.contains(null)) {
                Chains.add(new Chain(chainUnits));
            }
        }
    }
    
    /* This recursively finds a chain of hexagons with size 4 from
     * one starting point
     */
    private void findChains(Hexagon current, ArrayList<Hexagon> chain) {
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
    
    //randomly select edge which does not provide capture move for opponent
    private Edge selectRandomly(ArrayList<Edge> edges) {
        int index = random.nextInt(edges.size());
        return edges.get(index);
    }
    
    
    private void FindOfferChain() {
        offerChain = Chain.smallest;
        
        // If the smallest chain is of size 2 or 3 (that can be used as double-dealing)
        // Then if there is a non-double dealing chain (plus or triangular)
        // than we offer the latter if we can win enough points in long chains
        if(offerChain.size() == 2) {
            if(Chain.triangleSize > 0 && Chain.longWinPoints >= 3) {
                offerChain = Chain.triangular;
            }
            else if(Chain.plusSize > 0 && Chain.longWinPoints >= 4) {
                offerChain = Chain.plus;
            }
        }
        else if(offerChain.size() == 3) {
            if(Chain.plusSize > 0 && Chain.longWinPoints >= 4) {
                offerChain = Chain.plus;
            }
        }             
    }
    
    
    /* This method determines whether the player should offer a
     * double dealing sacrificial move or not. 
     */
    private boolean willSacrifice() {  
        if(DoubleDeals.size() != 1) {
            return false;
        }
        
        // Scores for double-dealing or not double dealing respectively
        int doubleDealScore = getNetScore(true);
        int normalScore = getNetScore(false);
        
        game.setDoubleCrossedCount(game.getDoubleCrossedCount()+1);
        
        if(doubleDealScore > normalScore) {
            
            return true;
        }
        printStatus();
        return false;
    }
    
    
    
    private int getNetScore(boolean doubleDeal) {
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
        for(int i = 0; i < Chain.singleSize; i++) {
            queue.add(1);
        }
        for(int i = 0; i < Chain.triangleSize; i++) {
            queue.add(3);
        }
        for(int i = 0; i < Chain.plusSize; i++) {
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
        
        int trappedHexagons = 0;
        for(Hexagon hexagon: Hexagons.values()) {
            if(hexagon.getSidesTaken() < 4) {
                trappedHexagons++;
            }
        }
        
        // The number of hexagons one can win from long chains
        int chainScore = Chain.longWinPoints + trappedHexagons;
        
        
        if (myGain) {
            score += chainScore;
        }
        else {
            score -= chainScore;
        }

        return score;
    }
    
    
    
    private void printStatus() {
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
       
    }
    
}
