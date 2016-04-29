/*
 * Geordie Wicks [185828] and Mubashwer Salman Khurshid [601738]
 * Project 1 - Artificial Intelligence
 */


 package aiproj.hexifence.mskgw;


import java.awt.Point;
import java.util.ArrayList;

/*
 * Hexagon data structure
 * Each hexagon has a position, and array of Edges and a counter tracking how many sides have been taken
 */
public class Hexagon {

	private ArrayList<Edge> edges; // List of edges from top-right edge going clockwise
	private Point position; // the position of the centre of the hexagon
	private int sidesTaken; // the number of edges captured by player
	private int colour; // colour after being captured
	private boolean visited; //
	
	public Hexagon(Point position){
		sidesTaken = 0;
		this.position = position;
		edges = new ArrayList<Edge>(6);
		setVisited(false);
	}

	/*
	* Return arrayList of hexagons Edges
	*/	

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	/*
	* Add an Edge to the hexagon
	*/	

	public void addEdge(int index, Edge edge) {
		edges.add(index, edge);
	}

	/*
	* Return point position of hexagon
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

	/*
	* Return number of sides of hexagon currently captured
	*/	

	public int getSidesTaken() {
		return sidesTaken;
	}

	/*
	* Capture an Edge and update number of Edges captured
	*/	

	public void captureSide() {
		this.sidesTaken++;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

    public int getColour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }
	
	
}
