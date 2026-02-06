package deque;

public class LinkedListDeque<T> {
    .first;;

    class node{
        private T val;
        private node next=null;
        private node pre=null;
        public node(T item){
            val=item;
        }
        public T val(){
            return val;
        }
        public node next(){
            return next;
        }
        public node pre(){
            return pre;
        }
    }
    private int size;
    private node first;
    private node last;
    public LinkedListDeque(){
        first=new node(null);
        last=first;
        size=0;
    }
    public void addFirst(T item){
        if(isEmpty()){
            first.val=item;
        }else{
            node added=new node(item);
            added.next=first;
            first.pre=added;
            first=added;
        }
        size+=1;
    }
    public void addLast(T item){
        if(isEmpty()){
            last.val=item;
        }else{
            node added=new node(item);
            added.pre=last;
            last.next=added;
            last=added;
        }
        size+=1;
    }
    public boolean isEmpty(){
        return size==0;
    }
    public int size(){
        return size;
    }
    public void printDeque(){
        node p=first;
        while (p!=null){
            System.out.println(p.val()+" ");
            p=p.next;
        }
        System.out.println("\n");
    }
    public T removeFirst(){
        if(size>0){
            T removed=first.val();
            first=first.next;
            if(first!=null){
                first.pre=null;
            }
            size-=1;
            return removed;
        }
        return null;
    }
    public T removeLast(){
        if(size>0){
            T removed=last.val();
            last=last.pre;
            if(last!=null){
                last.next=null;
            }
            size-=1;
            return removed;
        }
        return null;
    }
    public T get(int index){
        node p=first;
        int i=0;
        while (i<index){
            p=p.next;
            i++;
        }
        return p.val();
    }
    private T runner(node f,int n){
        if (n==0){
            return f.val();
        }
        return runner(f.next,n-1);
    }
    public T getRecursive(int index){
        return runner(first,index);
    }
}
