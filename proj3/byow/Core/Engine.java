package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private List<Room> rooms = new ArrayList<>();
    TETile[][] world = initialize();
    Random random;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        TETile[][] finalWorldFrame = null;
        String seed = getSeed(input);
        random = new Random(Long.parseLong(seed));
        roomGeneration();
        hallwayGeneration();
        buildWalls();
        finalWorldFrame = world;
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(world);
        return finalWorldFrame;
    }

    static class Node{
        int x;
        int y;
        Node(int x, int y){
            this.x = x;
            this.y = y;
        }
        public Node right(){
            return new Node(x+1, y);
        }
        public Node left(){
            return new Node(x-1, y);
        }
        public Node up(){
            return new Node(x, y+1);
        }
        public Node down(){
            return new Node(x, y-1);
        }
        public Node[] directions(){
            return new Node[]{up(), down(), left(), right()};
        }
        public Node[] cube(){
            return new Node[]{up(), down(), left(), right(), up().right(),up().left(),down().right(),down().left()};
        }
    }

    static class Room{
        Node benchmark;
        Node end;
        int width;
        int height;
        Room(Node benchmark, int width, int height){
            this.benchmark = benchmark;
            this.width = width;
            this.height = height;
            this.end = new Node(benchmark.x+width, benchmark.y+height);
        }
    }

    private boolean checkPath(Node target, boolean[][] painted){
        return checkRange(target) && world[target.x][target.y] != Tileset.NOTHING && world[target.x][target.y] != Tileset.WALL && !painted[target.x][target.y];
    }

    private boolean isConnected(Node start, Node end){
        Queue<Node> bfs = new ArrayDeque<>();
        bfs.add(start);
        boolean[][] painted = new boolean[WIDTH][HEIGHT];
        painted[start.x][start.y]=true;
        while (!bfs.isEmpty()){
            Node position = bfs.remove();
            if(position.x==end.x && position.y == end.y){
                return true;
            }
            for(Node adjacent: position.directions()){
                if(checkPath(adjacent,painted)){
                    painted[adjacent.x][adjacent.y]=true;
                    bfs.add(adjacent);
                }
            }
        }
        return false;
    }

    private Node randomNode(Random random){
        int newX = random.nextInt(WIDTH-2)+1;
        int newY = random.nextInt(HEIGHT-2)+1;
        return new Node(newX, newY);
    }

    private Room randomRoom(Random random){
        Node benchmark = randomNode(random);
        int w = min(random.nextInt(5)+5,max(0, WIDTH - benchmark.x - 2));
        int h = min(random.nextInt(5)+5,max(0, HEIGHT - benchmark.y - 2));
        return new Room(benchmark, w, h);
    }

    private String getSeed(String input){
        Pattern pattern = Pattern.compile("[Nn]([0-9]+)[Ss]");
        Matcher matcher = pattern.matcher(input);
        matcher.matches();
        String result = matcher.group(1);
        return result.substring(1, result.length()-1);
    }

    private static TETile[][] initialize(){
        TETile[][] newWorld = new TETile[WIDTH][HEIGHT];
        for(int x = 0; x<WIDTH; x++){
            for (int y = 0; y<HEIGHT; y++){
                newWorld[x][y] = Tileset.NOTHING;
            }
        }
        return newWorld;
    }
    private void paint(Node point, TETile pattern){
        paint(point, point, pattern);
    }

    private void paint(Node start, Node end, TETile pattern){
        for(int x = start.x; x<=end.x; x++){
            for(int y = start.y; y<=end.y;y++){
                world[x][y] = pattern;
            }
        }
    }

    private void pen(Node start, Node end, TETile pattern){
        boolean style = random.nextBoolean();
        if(style){
            for(int i = min(start.x, end.x); i<=max(start.x, end.x); i++){
                paint(new Node(i, start.y),pattern);
            }
            for(int j = min(start.y,end.y); j<=max(start.y, end.y); j++){
                paint(new Node(end.x, j), pattern);
            }
        }else{
            for(int i = min(start.y, end.y); i<=max(start.y, end.y); i++){
                paint(new Node(start.x, i), pattern);
            }
            for(int j = min(start.x,end.x); j<=max(start.x, end.x); j++){
                paint(new Node(j, end.y), pattern);
            }
        }
    }

    private boolean checkRange(Node node){
        return node.x>=0 && node.x<WIDTH && node.y>=0 && node.y<HEIGHT;
    }

    private void roomGeneration(){
        int roomNum = random.nextInt(20)+15;
        for (int i=0;i<roomNum;i++){
            Room room = randomRoom(random);
            rooms.add(room);
            paint(room.benchmark, room.end, Tileset.FLOWER);
        }
    }

    private void buildWalls(){
        Queue<Node> bfs = new ArrayDeque<>();
        Node start = rooms.get(0).benchmark;
        bfs.add(start);
        boolean[][] painted = new boolean[WIDTH][HEIGHT];
        while (!bfs.isEmpty()){
            Node position = bfs.remove();
            for(Node adjacent: position.cube()){
                if(checkRange(adjacent) && world[adjacent.x][adjacent.y].equals(Tileset.NOTHING)){
                    world[adjacent.x][adjacent.y] = Tileset.WALL;
                }
                if(checkPath(adjacent,painted)){
                    painted[adjacent.x][adjacent.y]=true;
                    bfs.add(adjacent);
                }
            }
        }
    }

    private void randomHallway(Room start, Room end){
        Node startNode = new Node(start.benchmark.x+random.nextInt(start.width+1),start.benchmark.y + random.nextInt(start.height+1));
        Node endNode = new Node(end.benchmark.x+random.nextInt(end.width+1),end.benchmark.y + random.nextInt(end.height+1));
        pen(startNode, endNode, Tileset.FLOWER);
    }

    private void hallwayGeneration(){
        Node[] access = new Node[rooms.size()];
        for (Room room2 : rooms){
            randomHallway(rooms.get(0), room2);
        }
    }
}
