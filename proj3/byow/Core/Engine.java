package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.*;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private List<Room> rooms = new ArrayList<>();
    TETile[][] world = initialize();
    boolean[][] reachable = new boolean[WIDTH][HEIGHT];
    Random random;
    Node player;

    static class Frame implements Serializable {
        TETile[][] world;
        Node player;
        boolean[][] reachable;
        Random random;

        Frame(TETile[][] world, Node player, boolean[][] reachable, Random random) {
            this.world = world;
            this.player = player;
            this.reachable = reachable;
            this.random = random;
        }
    }

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
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
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
        //ter.initialize(WIDTH, HEIGHT);
        readInput(input);
        finalWorldFrame = world;
        //ter.renderFrame(world);
        return finalWorldFrame;
    }

    static class Node implements Serializable {
        int x;
        int y;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Node right() {
            return new Node(x + 1, y);
        }

        public Node left() {
            return new Node(x - 1, y);
        }

        public Node up() {
            return new Node(x, y + 1);
        }

        public Node down() {
            return new Node(x, y - 1);
        }

        public Node[] directions() {
            return new Node[]{up(), down(), left(), right()};
        }

        public Node[] cube() {
            return new Node[]{up(), down(), left(), right(),
                    up().right(), up().left(), down().right(), down().left()};
        }

        public boolean equals(Node node) {
            return x == node.x && y == node.y;
        }
    }

    static class Room {
        Node benchmark;
        Node end;
        int width;
        int height;
        Node[] corners;

        Room(Node benchmark, int width, int height) {
            this.benchmark = benchmark;
            this.width = width;
            this.height = height;
            this.end = new Node(benchmark.x + width, benchmark.y + height);
            this.corners = new Node[]{
                    benchmark,
                    new Node(benchmark.x + width, benchmark.y),
                    end,
                    new Node(benchmark.x, benchmark.y + height)
            };
        }

        public boolean equals(Room room) {
            return benchmark.equals(room.benchmark) && end.equals(room.end);
        }

        public boolean contain(Room room) {
            for (Node corner : room.corners) {
                if (in(corner)) {
                    for (Node adjacent : corner.directions()) {
                        if (checkRange(adjacent) && in(adjacent)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean in(Node node) {
            return benchmark.x <= node.x && node.x <= end.x
                    && benchmark.y <= node.y && node.y <= end.y;
        }
    }

    private void worldGeneration() {
        roomGeneration();
        hallwayGeneration();
        buildWalls();
        do {
            player = randomNode();
        } while (!checkPath(player));
        world[player.x][player.y] = Tileset.AVATAR;
    }

    private boolean checkPath(Node target) {
        return checkRange(target)
                && !world[target.x][target.y].equals(Tileset.NOTHING)
                && !world[target.x][target.y].equals(Tileset.WALL)
                && reachable[target.x][target.y];
    }

    private boolean checkPath(Node target, boolean[][] painted) {
        return checkPath(target) && !painted[target.x][target.y];
    }

    private Node randomNode() {
        int newX = random.nextInt(WIDTH - 2) + 1;
        int newY = random.nextInt(HEIGHT - 2) + 1;
        return new Node(newX, newY);
    }

    private Room randomRoom() {
        Node benchmark = randomNode();
        int w = min(random.nextInt(5) + 5, max(0, WIDTH - benchmark.x - 2));
        int h = min(random.nextInt(5) + 5, max(0, HEIGHT - benchmark.y - 2));
        return new Room(benchmark, w, h);
    }

    private void readInput(String input) {
        Pattern load = Pattern.compile("^l");
        Matcher loadMatcher = load.matcher(input);
        Pattern seed = Pattern.compile("[Nn]([0-9]+)[Ss]");
        Matcher seedMatcher = seed.matcher(input);
        if (loadMatcher.find()) {
            loadFile();
            input = input.substring(loadMatcher.end());
        }
        if (seedMatcher.find()) {
            String result = seedMatcher.group(1);
            random = new Random(Long.parseLong(result.substring(1, result.length() - 1)));
            worldGeneration();
            input = input.substring(seedMatcher.end());
        }
        Pattern operation = Pattern.compile("([WASDwasd]+)");
        Matcher operationMatcher = operation.matcher(input);
        if (operationMatcher.find()) {
            String result = operationMatcher.group(1).toLowerCase();
            for (int i = 0; i < result.length(); i++) {
                move(result.charAt(i));
            }
            input = input.substring(operationMatcher.end());
        }
        if (input.equalsIgnoreCase(":q")) {
            savefile();
        }
    }

    private void loadFile() {
        try {
            FileInputStream fileIn = new FileInputStream("savefile.txt");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Frame frame = (Frame) in.readObject();
            world = frame.world;
            player = frame.player;
            reachable = frame.reachable;
            random = frame.random;
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void move(char op) {
        switch (op) {
            case 'w':
                if (checkPath(player.up())) {
                    player.y += 1;
                }
                break;
            case 'a':
                if (checkPath(player.left())) {
                    player.x -= 1;
                }
                break;
            case 's':
                if (checkPath(player.down())) {
                    player.y -= 1;
                }
                break;
            case 'd':
                if (checkPath(player.right())) {
                    player.x += 1;
                }
                break;
            default:
                break;
        }
        paint(player, Tileset.AVATAR);
    }

    private void savefile() {
        File save = new File("savefile.txt");
        FileOutputStream fileOut;
        try {
            save.createNewFile();
            fileOut = new FileOutputStream("savefile.txt");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(new Frame(world, player, reachable, random));
            out.close();
            fileOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static TETile[][] initialize() {
        TETile[][] newWorld = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                newWorld[x][y] = Tileset.NOTHING;
            }
        }
        return newWorld;
    }

    private void paint(Node point, TETile pattern) {
        paint(point, point, pattern);
    }

    private void paint(Node start, Node end, TETile pattern) {
        for (int x = start.x; x <= end.x; x++) {
            for (int y = start.y; y <= end.y; y++) {
                reachable[x][y] = true;
                world[x][y] = pattern;
            }
        }
    }

    private void pen(Node start, Node end, TETile pattern) {
        boolean style = random.nextBoolean();
        if (style) {
            for (int i = min(start.x, end.x); i <= max(start.x, end.x); i++) {
                paint(new Node(i, start.y), pattern);
            }
            for (int j = min(start.y, end.y); j <= max(start.y, end.y); j++) {
                paint(new Node(end.x, j), pattern);
            }
        } else {
            for (int i = min(start.y, end.y); i <= max(start.y, end.y); i++) {
                paint(new Node(start.x, i), pattern);
            }
            for (int j = min(start.x, end.x); j <= max(start.x, end.x); j++) {
                paint(new Node(j, end.y), pattern);
            }
        }
    }

    private static boolean checkRange(Node node) {
        return node.x >= 0 && node.x < WIDTH && node.y >= 0 && node.y < HEIGHT;
    }

    private void roomGeneration() {
        int roomNum = random.nextInt(20) + 15;
        for (int i = 0; i < roomNum; i++) {
            Room room = randomRoom();
            rooms.add(room);
            paint(room.benchmark, room.end, Tileset.FLOWER);
        }
    }

    private void buildWalls() {
        Queue<Node> bfs = new ArrayDeque<>();
        Node start = rooms.get(0).benchmark;
        bfs.add(start);
        boolean[][] painted = new boolean[WIDTH][HEIGHT];
        while (!bfs.isEmpty()) {
            Node position = bfs.remove();
            for (Node adjacent : position.cube()) {
                if (checkRange(adjacent) && world[adjacent.x][adjacent.y].equals(Tileset.NOTHING)) {
                    world[adjacent.x][adjacent.y] = Tileset.WALL;
                }
                if (checkPath(adjacent, painted)) {
                    painted[adjacent.x][adjacent.y] = true;
                    bfs.add(adjacent);
                }
            }
        }
    }

    private void randomHallway(Room start, Room end) {
        Node startNode = new Node(start.benchmark.x + random.nextInt(start.width + 1),
                start.benchmark.y + random.nextInt(start.height + 1));
        Node endNode = new Node(end.benchmark.x + random.nextInt(end.width + 1),
                end.benchmark.y + random.nextInt(end.height + 1));
        pen(startNode, endNode, Tileset.FLOWER);
    }

    private void hallwayGeneration() {
//        HashMap<Room, Room> access = new HashMap<>();
//        for (Room room : rooms) {
//            access.put(room, room);
//        }
//        for (Room parent : rooms) {
//            for (Room child : rooms) {
//                if (parent.contain(child)) {
//                    Room pointer = access.get(parent);
//                    access.put(child, pointer);
//                }
//            }
//        }
//        List<Room> result = new ArrayList<>();
//        for (Room room : access.values()) {
//            if (!result.contains(room)) {
//                result.add(room);
//            }
//        }
//        while (result.size() > 1) {
//            Room start = result.remove(0);
//            Room end = result.get(0);
//            randomHallway(start, end);
//        }
//    }
        for (Room room2 : rooms) {
            randomHallway(rooms.get(0), room2);
        }
    }
}
