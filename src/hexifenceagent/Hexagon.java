package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;

/*
 * Hexagon data structure
 */
public class Hexagon {

	private ArrayList<Edge> edges; // List of edges from top-right edge going clockwise
	private Point position; // the position of the centre of the hexagon
	private int sidesTaken; // the number of edges captured by player
	
	public Hexagon(Point position){
		sidesTaken = 0;
		this.position = position;
		edges = new ArrayList<Edge>(6);
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public void addEdge(int index, Edge edge) {
		edges.add(index, edge);
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public int getSidesTaken() {
		return sidesTaken;
	}

	public void captureSide() {
		this.sidesTaken++;
	}
	
	
}
