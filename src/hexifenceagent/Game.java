/*
 * Geordie Wicks [185828] and Mubashwer Salman Khurshid [601738]
 * Project 1 - Artificial Intelligence
 */

package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;


/*
* This is the entry point of hexifence game
*/	

public class Game {

	/* Collection of the positions of centres of hexagons, mapped by dimension (n = 2 or 3) */
	public static HashMap<Integer, ArrayList<Point>> info = new HashMap<Integer, ArrayList<Point>>();
	/* Collection of all hexagons where its mapped by its position */
	public static HashMap<Point, Hexagon> Hexagons;
	/* Collection of all edges where its mapped by its position  */
	public static HashMap<Point, Edge> Edges;
	
	public static void main(String[] args) {
		Hexagons = new HashMap<Point, Hexagon>();
		Edges = new HashMap<Point, Edge>();
		
		
		/* Save what we know about the positions of the hexagons depending
		 * on whether n = 2 or 3.
		 * Hardcoded the values rather than using a loop, as we know n=2 or n=3 it should offer a 
		 * small performance improvement
		 */
		info.put(2, new ArrayList<Point>(Arrays.asList(new Point(1,1), new Point(3,1), 
				new Point(1,3), new Point(3,3), new Point(5,3), 
				new Point(3,5), new Point(5,5))));
		
		info.put(3, new ArrayList<Point>(Arrays.asList(new Point(1,1), new Point(3,1), new Point(5,1),
				new Point(1,3), new Point(3,3), new Point(5,3), new Point(7,3),
				new Point(1,5), new Point(3,5), new Point(5,5), new Point(7,5), new Point(9,5),
				new Point(3,7), new Point(5,7), new Point(7,7), new Point(9,7),
				new Point(5,9), new Point(7,9), new Point(9,9)))); // points need to be verified
		
		
		Scanner sc = new Scanner(System.in);
		String line;
	
		int x = 0, n = 0;
		
		// If the first line does not contain the dimension number
		try {
			n = sc.nextInt(); // dimension of the board
		} catch(Exception e) {
			System.out.println("Incorrect board dimension.");
			System.exit(1);
		}
		
		
		/* 2d char array for storing the text input map of the game board */
		char[][] board = new char[4*n - 1][4*n - 1];
		
		sc.nextLine();

		// Read input board line by line and store the data in 2d array
		for(int y = 0; y < (4*n)-1; y++) {
			
			// If there are less lines than expected in input
			if(!sc.hasNextLine()) {
				System.out.println("Invalid input: Incorrect number of rows");
				System.exit(1);
			}
			
			line = sc.nextLine();
			
			// If there are wrong number of elements on each line (including in-between spaces)
			if (line.length() != 8*n - 3) {
				System.out.println("Invalid input: Incorrect number of columns");
				System.exit(1);
			}
			char[] lineChars= line.toCharArray();
			x = 0;
			
			for(int i = 0; i < 8*n -3; i += 2) {
				char ch = lineChars[i];
				if(ch != 'R' && ch != 'B' && ch != '+' && ch != '-') {
					System.out.println(ch + " is not a valid character");
					System.exit(1);
				}
				board[x++][y] = lineChars[i];
			}
			
		}
		
		// If there are more lines than expected in input
		if(sc.hasNextLine()) {
			System.out.println("Invalid input: Incorrect number of rows");
			System.exit(1);
		}
		sc.close();
		
		/*
		 * For each hexagon, all six 6 edges with its details are created and added to hexagon data structure.
		 * The hexagons and the edges are added to their respective hash maps.
		 *  Again we hardcode the values rather than using a loop, in the hope of minor performance gains
		 */
		Iterator<Point> hexPointIt = info.get(n).iterator();		
		while(hexPointIt.hasNext()) {
			Point hexPoint = hexPointIt.next();
			Hexagon hexagon = new Hexagon(hexPoint);
			
			Edge edge;
			int index = 0;
			// Moving clockwise from top-right edge
			Point edgePos = new Point(hexPoint.x, hexPoint.y-1);
			edge = AddEdge(hexagon, edgePos, board[edgePos.x][edgePos.y]);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(hexPoint.x+1,hexPoint.y);
			edge = AddEdge(hexagon, edgePos, board[edgePos.x][edgePos.y]);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(hexPoint.x+1,hexPoint.y+1);
			edge = AddEdge(hexagon, edgePos, board[edgePos.x][edgePos.y]);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(hexPoint.x,hexPoint.y+1);
			edge = AddEdge(hexagon, edgePos, board[edgePos.x][edgePos.y]);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(hexPoint.x-1,hexPoint.y);
			edge = AddEdge(hexagon, edgePos, board[edgePos.x][edgePos.y]);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(hexPoint.x-1,hexPoint.y-1);
			edge = AddEdge(hexagon, edgePos, board[edgePos.x][edgePos.y]);
			hexagon.addEdge(index++, edge);
			
			Hexagons.put(hexPoint, hexagon);
			
		}
		
		//BoardDesc();
		GetNumberOfPossibleMoves();
		GetMaximumNumberOfHexagonalCellsThatCanBeCapturedByOneMove();
		GetNumberOfHexagonalCellsAvailableForCapture();
		
		//System.out.println(Arrays.deepToString(board));
		
		
	}
	
	/* This method adds an edge to the hexagon */
	public static Edge AddEdge(Hexagon parent, Point edgePos, char status) {
		Edge outputEdge;
		
		// If the Edges hashmap already has an edge at this position, this is a shared edge
		// the parent is added to the edge and the parent's captured edge count is incremented
		if (Edges.containsKey(edgePos)) {
			outputEdge = Edges.get(edgePos);
			outputEdge.addParent(parent);
			outputEdge.setShared(true);
			
			if(status == 'B' || status == 'R') {
				parent.captureSide();
			}
		}
		// Otherwise, a new edge is created, its details are added to it and the parent's captured edge count
		// is incremented
		else {
			outputEdge = new Edge(edgePos);
			outputEdge.addParent(parent);
			
			if (status == 'B'){
				outputEdge.setColour("Blue");
				outputEdge.setMarked(true);
				parent.captureSide();
			}
			if (status == 'R'){
				outputEdge.setColour("Red");
				outputEdge.setMarked(true);
				parent.captureSide();
			}
			Edges.put(edgePos, outputEdge);
		}
		return outputEdge;
	}
	
	/*
	** Helper function which may be needed later so left in **


	public static void BoardDesc() {
		for(Hexagon hexagon : Hexagons.values()) {
			System.out.println("Hexagon Position: " + hexagon.getPosition());
			System.out.println("Sides taken: " + hexagon.getSidesTaken());
			
			for(int i = 0; i < 6; i++){
				System.out.println("Edge " + i + " position: " + hexagon.getEdges().get(i).getPosition());
				System.out.println("Edge " + i + " colour: " + hexagon.getEdges().get(i).getColour() );
				System.out.println("Edge " + i + " shared: " + hexagon.getEdges().get(i).isShared() );
				if(hexagon.getEdges().get(i).isShared()) {
					Point sharedParent = hexagon.getEdges().get(i).getParents().get(0).getPosition();
					if(sharedParent.equals(hexagon.getPosition())) {
						sharedParent = hexagon.getEdges().get(i).getParents().get(1).getPosition();
					}
					System.out.println("Edge " + i + " shared parent: " + sharedParent);
				}
					
				System.out.println();
				
			}
			System.out.println();
		}
		
	}
	*/
	
	
	/*
	 * This method looks for the number of unmarked edges to find possible moves
	 */
	public static void GetNumberOfPossibleMoves() {
		int moveCount = 0;
		for(Edge edge : Edges.values()) {
			if(!edge.isMarked()) {
				moveCount++;
			}
		}
		System.out.println(moveCount);
	}
	
	
	/*
	 * This method looks for hexagons with 5 edges captured to find the answer
	 */
	public static void GetNumberOfHexagonalCellsAvailableForCapture(){
		int hexagonCount = 0;
		for(Hexagon hexagon : Hexagons.values()) {
			if(hexagon.getSidesTaken() == 5) {
				hexagonCount++;
			}
		}
		System.out.println(hexagonCount);
	}

	
	public static void GetMaximumNumberOfHexagonalCellsThatCanBeCapturedByOneMove() {
		int hexagonCount = 0;
		for(Hexagon hexagon : Hexagons.values()) {
			// If a hexagon with 5 edges captured is found, then at least 1 cell can be captured by a move
			if(hexagon.getSidesTaken() == 5) {
				if(hexagonCount == 0) {
					hexagonCount = 1;	
				}
				// Then we look at all the edges of that hexagon to find any other hexagon which shares the lone unmarked edge
				// with the former. If the latter hexagon also has 5 edges captured, then we can confirm that 2 hexagons can be
				// captured by one move.
				for (Edge edge : hexagon.getEdges()) {
					if(!edge.isMarked()) {
						for (Hexagon parent : edge.getParents()) {
							if(!parent.getPosition().equals(hexagon.getPosition()) && parent.getSidesTaken() == 5 ) {
								hexagonCount = 2;
								System.out.println(hexagonCount);
								return;
							}
						}
					}
				}
				
			}
		}
		System.out.println(hexagonCount);
	}
	
}
