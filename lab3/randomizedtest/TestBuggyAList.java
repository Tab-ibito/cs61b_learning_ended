package randomizedtest;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> a = new AListNoResizing();
        BuggyAList<Integer> b = new BuggyAList();
        for (int i=1;i<=3;i++){
            a.addLast(i);
            b.addLast(i);
        }
        for (int i=1;i<=3;i++){
            a.removeLast();
            b.removeLast();
        }
    }
    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> M = new BuggyAList<>();
        int N = 1000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 2);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                M.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int size2 = M.size();
                System.out.println("size: " + size);
                System.out.println("size2: " + size2);
            }
        }
    }
}
