package hexifenceagent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Game {

	public static HashMap<Integer, BoardConfig> info = new HashMap<Integer, BoardConfig>();
	public static HashMap<Point, Hexagon> Hexagons;
	public static HashMap<Point, Edge> Edges;
	
	
	public static void main(String[] args) {
		Hexagons = new HashMap<Point, Hexagon>();
		Edges = new HashMap<Point, Edge>();
		
		
		
		info.put(2, new BoardConfig(7,7, new ArrayList<Point>(Arrays.asList(new Point(1,1), new Point(3,1), 
				new Point(1,3), new Point(3,3), new Point(5,3), 
				new Point(3,5), new Point(5,5)))));
		
		info.put(3, new BoardConfig(11,11, new ArrayList<Point>(Arrays.asList(new Point(1,1), new Point(3,1), new Point(1,3),
				new Point(3,3), new Point(5,3), new Point(3,5), new Point(5,5),
				new Point(3,3), new Point(5,3), new Point(3,5), new Point(5,5), new Point(5,5),
				new Point(3,3), new Point(5,3), new Point(3,5), new Point(5,5),
				new Point(3,3), new Point(5,3), new Point(3,5))))); // points need to be corrected
		
		
		char[][] board = new char[info.get(2).getRows()][info.get(2).getColumns()];
		
		
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		String line;
	
		int x = 0, y = 0;
		
		
		int n = sc.nextInt();
		sc.nextLine();

		while(sc.hasNextLine()) {
			line = sc.nextLine();		
			x = 0;
			for (char ch : line.toCharArray()){
		        if (ch == ' ' || ch == '\n') continue;
				board[x][y] = ch;
				//System.out.print(ch);
				x++;
		    } 
			y++;	
		}
		
		/*for(y = 0; y < info.get(2).getRows(); y++) {
			for(x = 0; x < info.get(2).getColumns(); x++) {
				System.out.print(board[x][y]);
			}
			System.out.println();
		}*/
		
		
		Iterator<Point> hexPointIt = info.get(2).getHexagonLocs().iterator();
		while(hexPointIt.hasNext()) {
			Point hexPoint = hexPointIt.next();
			Hexagon hexagon = new Hexagon(hexPoint);
			
			Edge edge;
			int index = 0;
			// Moving clockwise from top-right
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
		
		
	}
	
	public static Edge AddEdge(Hexagon parent, Point edgePos, char status) {
		Edge outputEdge;
		if (Edges.containsKey(edgePos)) {
			outputEdge = Edges.get(edgePos);
			outputEdge.addParent(parent);
			outputEdge.setShared(true);
			
			if(status == 'B' || status == 'R') {
				parent.captureSide();
			}
		}
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
	
	public static void GetNumberOfPossibleMoves() {
		int moveCount = 0;
		for(Edge edge : Edges.values()) {
			if(!edge.isMarked()) {
				moveCount++;
			}
		}
		System.out.println(moveCount);
	}
	
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
			if(hexagon.getSidesTaken() == 5) {
				if(hexagonCount == 0) {
					hexagonCount = 1;	
				}
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
