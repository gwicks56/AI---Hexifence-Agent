package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;

/*
 * Edge data structure - A side of a hexagon
 */
public class Edge {
	private Point position; // the position of the hexagon on map
	private boolean isMarked; // tells us whether the edge is marked by a colour or not
	private String colour; // the colour of the edge if it is marked
	private boolean isShared; // tells us whether the edge is shared with another hexagon or not
	ArrayList<Hexagon> parents; // list of hexagons which is composed of this edge (maximum 2)
	
	public Edge(Point position) {
		this.parents = new ArrayList<Hexagon>();
		this.position = position;
		isMarked = false;
	}

	public boolean isMarked() {
		return isMarked;
	}

	public void setMarked(boolean isMarked) {
		this.isMarked = isMarked;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public boolean isShared() {
		return isShared;
	}

	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}

	public ArrayList<Hexagon> getParents() {
		return parents;
	}

	public void addParent(Hexagon parent) {
		parents.add(parent);
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}
	
	
	
}
