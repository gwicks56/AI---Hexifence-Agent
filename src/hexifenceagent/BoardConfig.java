package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;

/*
 * A data structure to store some information regarding the board that we can assume
 * depending on the dimension of the board
 */
public class BoardConfig {
	private int rows; // number of rows of input board map
	private int columns; // number of columns of input board map
	private ArrayList<Point> hexagonLocs; // locations of centre of hexagons.. 
	
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
