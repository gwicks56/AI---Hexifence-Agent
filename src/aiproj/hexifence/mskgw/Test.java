package aiproj.hexifence.mskgw;

import aiproj.hexifence.Referee;

public class Test {
    public static int lossCountsWhileInControl = 0;
    public static int winsCountsWhileInControl = 0;
    public static void main(String[] args) {
        
        
        int P1count = 0;
        int P2count = 0;
        int error = 0;

        int count = 1000;
        while(count-- != 0) {
            int win = Referee.result(args[0], args[1], args[2]);
            if(win == 1) {
                P1count++;
            }
            else if (win == 2) {
                P2count++;
            }
            else {
                error++;
            }
        }
        
        System.out.println("P1 WIN: " + P1count);
        System.out.println("P2 WIN: " + P2count);
        System.out.println("win counts in control: " + winsCountsWhileInControl);
        System.out.println("loss counts in control: " + lossCountsWhileInControl);
        System.out.println("win in control%: " + (winsCountsWhileInControl/((double)(winsCountsWhileInControl+lossCountsWhileInControl))) * 100.0);
        System.out.println("P1 WIN%: " + (P1count/((double)(P1count+P2count))) * 100.0);
        System.out.println("P2 WIN%: " + (P2count/((double)(P1count+P2count))) * 100.0);
        System.out.println("ERROR : " + error);
    }

}
