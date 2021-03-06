/*
 * Geordie Wicks [185828] and Mubashwer Salman Khurshid [601738]
 * Project 1 - Artificial Intelligence
 */

package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.util.ArrayList;

/*
 * Edge data structure - A side of a hexagon
 * Each edge has a location (point), a boolean to know if it has been captured 
    yet,
 * a color to know which player captured it, and a boolean to know if the edge 
   is shared with
 * more than one hexagon
 */
public class Edge {
    // the position of the hexagon on map
    private Point position; 
    // tells us whether the edge is marked by a colour or not
    private boolean isMarked; 
    // the colour of the edge if it is marked
    private int colour; 
    // tells us whether the edge is shared with another hexagon or not
    private boolean isShared; 
    
    ArrayList<Hexagon> parents; 
    
    public Edge(Point position) {
        this.parents = new ArrayList<Hexagon>();
        this.position = position;
        isMarked = false;
        isShared = false;
    }

    /*
    * Return boolean whether edge is captured or not
    */  

    public boolean isMarked() {
        return isMarked;
    }

    /*
    * Method to set edge as captured
    */  

    public void setMarked(boolean isMarked) {
        this.isMarked = isMarked;
    }

    /*
    * Return string showing which player captured edge
    */  

    public int getColour() {
        return colour;
    }

    /*
    * Method to set color of captured edge
    */  

    public void setColour(int colour) {
        this.colour = colour;
    }

    /*
    * Return boolean whether edge is shared with two hexagons
    */  

    public boolean isShared() {
        return isShared;
    }

    /*
    * Method to set shared edge property
    */  

    public void setShared(boolean isShared) {
        this.isShared = isShared;
    }

    /*
    * return the id of hexagons edge is part of
    */  

    public ArrayList<Hexagon> getParents() {
        return parents;
    }

    /*
    * Method to set the parents of edge
    */  

    public void addParent(Hexagon parent) {
        parents.add(parent);
    }

    /*
    * Return position of hexagon
    */  

    public Point getPosition() {
        return position;
    }

    /*
    * Set position of hexagon
    */  

    public void setPosition(Point position) {
        this.position = position;
    }
    
    public Hexagon getOtherParent(Hexagon parent) {
        for (Hexagon p : parents) {
            if(p != parent) {
                return p;
            }
        }
        return null;
    }
    
    
}
