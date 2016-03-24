package hexifenceagent;

import java.util.ArrayList;
import java.util.Scanner;

public class Game {

	ArrayList<Edge> sides;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		String line;
		int n = sc.nextInt();
		int x = 0, y = 0;
		sc.nextLine();
		//int n = sc.nextInt();
		while(sc.hasNextLine()) {
			line = sc.nextLine();
			
			for (char ch : line.toCharArray()){
		        System.out.print(ch);
		    } 
			
			y++;
		}
	}

}
