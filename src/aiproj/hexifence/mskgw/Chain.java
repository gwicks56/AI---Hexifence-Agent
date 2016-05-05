package aiproj.hexifence.mskgw;

import java.util.ArrayList;

public class Chain {

    public static int size = 0;    
    public static int plusSize = 0;  
    public static int triangleSize = 0;  
    public static int singleSize = 0;
    public static int longSize = 0;
    public static int longWinPoints = 0;
    public static Chain smallest;
    public static Chain triangular;
    public static Chain plus;
    
    private ArrayList<Hexagon> chainUnits;
    private boolean isPlus;  
    private boolean isTriangle;
    
    public Chain(ArrayList<Hexagon> chainUnits) {
        size++;
        isTriangle = false;
        isPlus = false;
        this.chainUnits = chainUnits;
        
        if(isTriangularChain(chainUnits)) {
            triangleSize++;
            isTriangle = true;
            triangular = this;
        }
        else if(isPlusChain(chainUnits)) {
            plusSize++;
            isPlus = true;
            plus = this;
        }
        else if(chainUnits.size() > 1) {
            longSize++;
            longWinPoints = size();
            if(longSize != 1) {
                longWinPoints -= 2;
            }
        }
        else if(chainUnits.size() == 1) {
            singleSize++;
        }
        else {
            System.out.println("Empty Chain Error");
        }
   
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

    public int size() {
        return chainUnits.size();
    }
    
    public ArrayList<Hexagon> getChainUnits() {
        return chainUnits;
    }

    public boolean isPlus() {
        return isPlus;
    }

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
     
        // Only 2 hexagons share only 1 marked edge which are referred as horizontals    
        int sharedMarkedCount = 0;
        for(Hexagon h1 : chainUnits) {
            for(Hexagon h2: chainUnits) {
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
