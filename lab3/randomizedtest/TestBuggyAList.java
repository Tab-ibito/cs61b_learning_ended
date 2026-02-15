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
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = correct.size();
                int size2 = broken.size();
                System.out.println("size: " + size);
                System.out.println("size2: " + size2);
            } else if (operationNumber == 2 && correct.size()!=0) {
                System.out.println(correct.removeLast());
                System.out.println(broken.removeLast());
            } else if (operationNumber == 3 && correct.size()!=0){
                int size = correct.size();
                int randVal = StdRandom.uniform(0, size);
                System.out.println(correct.get(randVal));
                System.out.println(broken.get(randVal));
                System.out.println("get(" + randVal + ")");
            }
        }
    }
}
