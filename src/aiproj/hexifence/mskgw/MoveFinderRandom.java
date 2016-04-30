package aiproj.hexifence.mskgw;

import java.util.ArrayList;
import java.util.Random;

public class MoveFinderRandom implements IMoveFinder {

    private Random random;    
    private Game game;

    public MoveFinderRandom(Game game) {
        this.game = game;     
        random = new Random();  

    }
    
    
    public Edge findMove() {
        ArrayList<Edge> moveList = new ArrayList<Edge>();
        for (Edge edge: game.getEdges().values()) {
            if(!edge.isMarked()) {
                moveList.add(edge);
            }
             
        }
        if(!moveList.isEmpty()) {
            // Select a safe move randomly if possible
            int index = random.nextInt(moveList.size());
            return moveList.get(index);
        }

        return null;
    }

}
