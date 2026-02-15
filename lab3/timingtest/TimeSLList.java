package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        SLList<Integer> a = new SLList<>();
        int N=128000;
        int M=10000;
        int i=0;
        int n=1000;
        AList<Integer> Ns = new AList();
        AList<Double> times = new AList();
        AList<Integer> opCounts = new AList();
        while(i<=N){
            if(i==n){
                int cnt=0;
                n*=2;
                Ns.addLast(i);
                Stopwatch sw = new Stopwatch();
                for(int j=1;j<=M;j++){
                    a.getLast();
                    cnt+=1;
                }
                double timeInSeconds = sw.elapsedTime();
                times.addLast(timeInSeconds);
                opCounts.addLast(cnt);
            }
            a.addLast(i);
            i++;
        }
        printTimingTable(Ns,times,opCounts);
    }
}
