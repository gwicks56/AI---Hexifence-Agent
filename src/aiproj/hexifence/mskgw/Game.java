/*
 * Geordie Wicks [185828] and Mubashwer Salman Khurshid [601738]
 * Project 1 - Artificial Intelligence
 */

package aiproj.hexifence.mskgw;

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;


/*
* This is the entry point of hexifence game
*/	

public class Game {

	/* Text representation of game board */
    private char[][] board;
    /* Board dimension */
    private int dimension;
    /* Collection of the positions of centres of hexagons, mapped by dimension (n = 2 or 3) 
	private HashMap<Integer, ArrayList<Point>> info = new HashMap<Integer, ArrayList<Point>>();*/
	/* Collection of all hexagons where its mapped by its position */
	private HashMap<Point, Hexagon> Hexagons;
	/* Collection of all edges where its mapped by its position  */
	private HashMap<Point, Edge> Edges;
    /* The number of hexagons captured mapped by its colour */
    private HashMap<Integer, Integer> Score;
    /* The number of times one move captured two hexagons. */
    private int doubleCrossedCount;
			
	public Game(int n)  {
		Hexagons = new HashMap<Point, Hexagon>();
		Edges = new HashMap<Point, Edge>();
		Score = new HashMap<Integer, Integer>();
		setDoubleCrossedCount(0);
		/* Scores are 0 initially */
		Score.put(Piece.BLUE, 0);
		Score.put(Piece.RED, 0);
		
		/* Save what we know about the positions of the hexagons depending
		 * on whether n = 2 or 3.
		 * Hard-coded the values rather than using a loop, as we know n=2 or n=3
		 
		 
		info.put(2, new ArrayList<Point>(Arrays.asList(new Point(1,1), new Point(3,1), 
				new Point(1,3), new Point(3,3), new Point(5,3), 
				new Point(3,5), new Point(5,5))));
		
		info.put(3, new ArrayList<Point>(Arrays.asList(new Point(1,1), new Point(3,1), new Point(5,1),
				new Point(1,3), new Point(3,3), new Point(5,3), new Point(7,3),
				new Point(1,5), new Point(3,5), new Point(5,5), new Point(7,5), new Point(9,5),
				new Point(3,7), new Point(5,7), new Point(7,7), new Point(9,7),
				new Point(5,9), new Point(7,9), new Point(9,9)))); // points need to be verified
		*/
		/* 2d char array for storing the text map of the game board */
		dimension = n;
		int size = 4 * dimension - 1;
		board = new char[size][size];
		for(int i = 0; i < size; i++) {
		    for(int j = 0; j < size; j++) {
		        board[i][j] = '-';
		    }
		}

		/*
		 * Each hexagon is created from the generated list of the centres of hexagons.
		 * All six 6 edges with its details are created and added to hexagon data structure.
		 * The hexagons and the edges are added to their respective hash maps.
		 */
		
		ArrayList<Point> centres = GenerateHexagonCentres(dimension);		
		for(Point centre : centres) {
			Hexagon hexagon = new Hexagon(centre);
			
			Edge edge;
			int index = 0;
			// Moving clockwise from top-right edge
			Point edgePos = new Point(centre.x, centre.y-1);
			edge = AddEdge(hexagon, edgePos);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(centre.x+1,centre.y);
			edge = AddEdge(hexagon, edgePos);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(centre.x+1,centre.y+1);
			edge = AddEdge(hexagon, edgePos);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(centre.x,centre.y+1);
			edge = AddEdge(hexagon, edgePos);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(centre.x-1,centre.y);
			edge = AddEdge(hexagon, edgePos);
			hexagon.addEdge(index++, edge);
			
			edgePos = new Point(centre.x-1,centre.y-1);
			edge = AddEdge(hexagon, edgePos);
			hexagon.addEdge(index++, edge);
			
			Hexagons.put(centre, hexagon);
			
		}
		
		/*
		System.out.println(GetNumberOfPossibleMoves());
		System.out.println(GetMaximumNumberOfHexagonalCellsThatCanBeCapturedByOneMove());
		System.out.println(GetNumberOfHexagonalCellsAvailableForCapture());
		*/

		
		
	}
	
	/* This method adds an edge to the hexagon */
	public Edge AddEdge(Hexagon parent, Point edgePos) {
		Edge outputEdge;		
		// If the Edges hashmap already has an edge at this position, this is a shared edge
		if (Edges.containsKey(edgePos)) {
			outputEdge = Edges.get(edgePos);
			outputEdge.addParent(parent);
			outputEdge.setShared(true);
		}
		// Otherwise, a new edge is created, its details are added t
		else {
			outputEdge = new Edge(edgePos);
			outputEdge.addParent(parent);
			Edges.put(edgePos, outputEdge);
			board[edgePos.x][edgePos.y] = '+';
		}
		return outputEdge;
	}
	
		
	public int makeMove(Edge edge, int colour) {
	    int status = -1;
	    // Invalid move
	    if(edge == null || edge.isMarked()) {
	        return status;
	    }
	    
	    // Update text representation of map for marking edge
        Point pos = edge.getPosition();
        if(colour == Piece.RED) {
            board[pos.x][pos.y] = 'R';
        }
        else if(colour == Piece.BLUE) {
            board[pos.x][pos.y] = 'B';
        }
        // Update internal configuration
        edge.setColour(colour);     
        edge.setMarked(true);
        
	    status = 0;
	    for(Hexagon parent : edge.getParents()) {
	        parent.captureSide();
	        if(parent.getSidesTaken() == 6) {
	           // Hexagon captured, set colour and update score
	           parent.setColour(colour);
               Score.put(colour, Score.get(colour)+1);
               
               // Update text representation of map for capturing
               // hexagon
               Point hexPos = parent.getPosition();
               if(colour == Piece.RED) {
                   board[hexPos.x][hexPos.y] = 'r';
               }
               else if(colour == Piece.BLUE) {
                   board[hexPos.x][hexPos.y] = 'b';
               }
               status = 1;
            }       
	    }
	    return status;
	}
	

    public void printBoard(PrintStream output) {
        int size = 4 * dimension - 1;
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < size; x++) {
                output.print(board[x][y] + " ");
            }
            output.println();
        }
        
    }
    
    
    public int getScore(int colour) {
        return Score.get(colour);
    }

    public HashMap<Point, Hexagon> getHexagons() {
        return Hexagons;
    }
    
    public HashMap<Point, Edge> getEdges() {
        return Edges;
    }

    /* This generates a list of coordinates of the hexagon
     * centres in the board of a given dimension.
     */
	public static ArrayList<Point> GenerateHexagonCentres(int n) {
	    int perRow = n;
	    int currentPerRow = n;
	    ArrayList<Point> centres = new ArrayList<Point>();
	    
	    // First n rows moving left and down from top-right corner
	    for(int y = 1; y < 2*n; y += 2) {
	        currentPerRow = perRow;
	        for(int x = 1; x < 4*n - 1; x += 2) {
	            if(currentPerRow > 0) {
	                centres.add(new Point(x, y));
	                //System.out.println("(" + x + ", " + y + ")");
	                currentPerRow--;
	            }
	        }
	        perRow++;
	    }
	   
	   // Last n-1 rows moving right and up from top-left corner
	   perRow = n; 
       for(int y = 4*n - 3; y > 2*n; y -= 2) {
            currentPerRow = perRow;
            for(int x = 4*n - 3; x > 0; x -= 2) {
                if(currentPerRow > 0) {
                    centres.add(new Point(x, y));
                    //System.out.println("(" + x + ", " + y + ")");
                    currentPerRow--;
                }
            }
            perRow++;
        }
       return centres;
	}

    public int getDoubleCrossedCount() {
        return doubleCrossedCount;
    }

    public void setDoubleCrossedCount(int doubleCrossedCount) {
        this.doubleCrossedCount = doubleCrossedCount;
    }
	
	/*
	
	 * This method looks for the number of unmarked edges to find possible moves
	 
	public static int GetNumberOfPossibleMoves() {
		int moveCount = 0;
		for(Edge edge : Edges.values()) {
			if(!edge.isMarked()) {
				moveCount++;
			}
		}
		return moveCount;
	}
	
	
	 * This method looks for hexagons with 5 edges captured to find the answer
	 
	public static int GetNumberOfHexagonalCellsAvailableForCapture(){
		int hexagonCount = 0;
		for(Hexagon hexagon : Hexagons.values()) {
			if(hexagon.getSidesTaken() == 5) {
				hexagonCount++;
			}
		}
		return hexagonCount;
	}

	
	public static int GetMaximumNumberOfHexagonalCellsThatCanBeCapturedByOneMove() {
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
							if(!parent.getPosition().equals(hexagon.getPosition()) && parent.getSidesTaken() == 5) {
								hexagonCount = 2;
								return hexagonCount;
							}
						}
					}
				}
				
			}
		}
		return hexagonCount;
	}*/
	
}
