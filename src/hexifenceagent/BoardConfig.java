package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;

public class BoardConfig {
	private int rows;
	private int columns;
	private ArrayList<Point> hexagonLocs;
	
	public BoardConfig(int rows, int columns, ArrayList<Point> hexagonLocs) {
		this.rows = rows;
		this.columns = columns;
		this.hexagonLocs = hexagonLocs;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public ArrayList<Point> getHexagonLocs() {
		return hexagonLocs;
	}

	public void setHexagonLocs(ArrayList<Point> hexagonLocs) {
		this.hexagonLocs = hexagonLocs;
	}
	
}
