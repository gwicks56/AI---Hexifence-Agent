package hexifenceagent;

import java.util.ArrayList;

public class Edge {
	private boolean isMarked;
	private String colour;
	private boolean isShared;
	ArrayList<Hexagon> parents;
	
	public Edge() {
		this.parents = new ArrayList<Hexagon>();
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
	
	
	
}
