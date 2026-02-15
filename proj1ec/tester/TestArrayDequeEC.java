package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void test(){
        String message= "";
        StudentArrayDeque<Integer> ad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ad2 = new ArrayDequeSolution<>();
        for(int i=1;i<=10000;i++){
            int op = StdRandom.uniform(0,4);
            if(op==0){
                ad1.addFirst(i);
                ad2.addFirst(i);
                message=message.concat("addFirst("+i+")\n");
            }else if(op==1){
                ad1.addLast(i);
                ad2.addLast(i);
                message=message.concat("addLast("+i+")\n");
            }else if(op==2 && !ad1.isEmpty() && !ad2.isEmpty()){
                Integer actual=ad1.removeFirst();
                Integer expected=ad2.removeFirst();
                message=message.concat("removeFirst()\n");
                assertEquals(message ,expected, actual);
            }else if(op==3 && !ad1.isEmpty() && !ad2.isEmpty()){
                Integer actual=ad1.removeLast();
                Integer expected=ad2.removeLast();
                message=message.concat("removeLast()\n");
                assertEquals(message ,expected, actual);
            }
        }
    }
}
