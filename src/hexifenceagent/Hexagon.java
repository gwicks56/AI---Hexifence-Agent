package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;

public class Hexagon {

	private ArrayList<Edge> edges;
	private Point position;
	private int sidesTaken;
	
	public Hexagon(){
		sidesTaken = 0;
		position = null;
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
