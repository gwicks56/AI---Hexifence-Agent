/*
 * Geordie Wicks [185828] and Mubashwer Salman Khurshid [601738]
 * Project 1 - Artificial Intelligence
 */


package aiproj.hexifence.mskgw;

import java.util.ArrayList;

// Chain data structure
// This structure is essential for our method of playing
// Here we store all the information we need about each chain
// including it's size, shape etc


public class Chain {

    public static int size = 0;             // size of chain
    public static int plusSize = 0;         // size of plus shaped chain
    public static int triangleSize = 0;     // size of triangle shaped chain
    public static int singleSize = 0;   // number of single hexagons for capture
    public static int longSize = 0;         // length of normal long chain
    public static int longWinPoints = 0;    // points available with sacrifice
    public static Chain smallest;           //smallest chain
    public static Chain triangular;         // is triangular
    public static Chain plus;               // is plus
    
    private ArrayList<Hexagon> chainUnits;  // array of hexagons in a chain
    private boolean isPlus;  
    private boolean isTriangle;
    
    public Chain(ArrayList<Hexagon> chainUnits) {
        size++;
        isTriangle = false;
        isPlus = false;
        this.chainUnits = chainUnits;

        // check if triangle shape
        
        if(isTriangularChain(chainUnits)) {
            triangleSize++;
            isTriangle = true;
            triangular = this;
        }

        //check if plus shaped chain

        else if(isPlusChain(chainUnits)) {
            plusSize++;
            isPlus = true;
            plus = this;
        }

        // else normal chain of length > 1

        else if(chainUnits.size() > 1) {
            longSize++;
            longWinPoints = size();
            if(longSize != 1) {
                longWinPoints -= 2;
            }
        }

        //single hexagon chain
        else if(chainUnits.size() == 1) {
            singleSize++;
        }
        else {
            System.out.println("Empty Chain Error");
        }


        //check if smallest chain
        if(smallest == null || size() < smallest.size()) {
            smallest = this;
        }
        else if(chainUnits.size() == smallest.size()) {
            if(isPlus || isTriangle) {
                smallest = this;
            }
        }
    }
    
    public static void initialize() {
        size = 0;
        plusSize = 0;
        triangleSize = 0;
        singleSize = 0;
        longSize = 0;
        longWinPoints = 0;
        smallest = null; 
        triangular = null;
        plus = null;
    }
    //get size of chain
    public int size() {
        return chainUnits.size();
    }
    // get the array of hexagons in chain
    public ArrayList<Hexagon> getChainUnits() {
        return chainUnits;
    }
    // return true if plus shaped
    public boolean isPlus() {
        return isPlus;
    }
    // return true if triangle shaped
    public boolean isTriangle() {
        return isTriangle;
    }


    /*
     * This method checks whether a open chain is actually a
     *  a closed circular plus-shaped (4) loop which cannot offer
     *  double dealing sacrificial.
     */
    public boolean isPlusChain(ArrayList<Hexagon> chainUnits) { 
        /* Only chains of size 4 can be plus-shaped */
        if(chainUnits.size() != 4) return false;
        
        ArrayList<Hexagon> horizontals = new ArrayList<Hexagon>();
        ArrayList<Hexagon> verticals = new ArrayList<Hexagon>();
     
        // Only 2 hexagons share only 1 marked edge which are referred 
        //as horizontals    
        int sharedMarkedCount = 0;
        for(Hexagon h1 : chainUnits) {
            for(Hexagon h2: chainUnits) {
                if(h1 == h2) continue;          
                for(Edge edge: h1.getEdges()) {
                    if(edge.isMarked() && edge.isShared() && 
                        edge.getOtherParent(h1) == h2) {
                        sharedMarkedCount++;
                        if (!horizontals.contains(h1)) horizontals.add(h1);
                        if (!horizontals.contains(h2)) horizontals.add(h2);
                    }
                }
 
            }
        }
        // Two hexagons must share 1 edge (count is 2 here as it 
        //is done twice in loop)
        if(sharedMarkedCount != 2 || horizontals.size() !=2) return false;
        
        // The other two are verticals
        for(Hexagon h: chainUnits) {
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
    private boolean isTriangularChain(ArrayList<Hexagon> chainUnits) {
        /* Only chains of size 3 can be triangular */
        if(chainUnits.size() != 3) return false;
        
        /* Every hexagon in triangular chain shares a free edge with
         * the other two hexagons in the chain.
         */
        for(Hexagon h1 : chainUnits) {
            for(Hexagon h2 : chainUnits) {
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
    
}
