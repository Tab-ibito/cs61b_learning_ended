package deque;

public class ArrayDeque<T> implements Deque<T> {
    protected T[] items;
    private int size=0;
    protected int first;
    protected int last;
    public ArrayDeque(){
        items =(T[]) new Object[8];
        size=0;
        first=4;
        last=4;
    }
    private int[] resize(int capacity, boolean direction){
        T[] a=(T[]) new Object[capacity+items.length];
        if (!direction){
            for(int i = first;i<last;i++){
                a[i+capacity]=items[i];
            }
            items=a;
            return new int[]{first+capacity, last + capacity};
        }else{
            for(int i = first;i<last;i++){
                a[i]=items[i];
            }
            items=a;
            return new int[]{first, last};
        }
    }
    @Override
    public void addFirst(T item){
        first--;
        items[first]=item;
        size++;
        if(first==0){
            int[] res=resize(size,false);
            first=res[0];
            last=res[1];
        }
    }
    @Override
    public void addLast(T item){
        items[last]=item;
        last++;
        size++;
        if(last==items.length){
            int[] res=resize(size,true);
            first=res[0];
            last=res[1];
        }
    }
    @Override
    public int size(){
        return size;
    }
    @Override
    public void printDeque(){
        for(int i=first;i<last;i++){
            System.out.println(items[i]+" ");
        }
        System.out.println("\n");
    }
    @Override
    public T removeFirst(){
        if(size==0){
            return null;
        }
        T removed=items[first];
        first++;
        size--;
        if(first>(int) (1.4*size)){
            int[] res=resize((int) (-0.4*size),false);
            first=res[0];
            last=res[1];
        }
        return removed;
    }
    @Override
    public T removeLast(){
        if(size==0){
            return null;
        }
        last--;
        size--;
        T removed=items[last];
        if(items.length-last>(int) (1.4*size)){
            int[] res=resize((int) (-0.4*size),true);
            first=res[0];
            last=res[1];
        }
        return removed;
    }
    @Override
    public T get(int index){
        if (index+first>=last){
            return null;
        }
        return items[index+first];
    }
}
