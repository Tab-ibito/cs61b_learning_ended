package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    static final File REFS = join(GITLET_DIR, "refs");
    static final File INDEX = join(GITLET_DIR, "index");
    static final File OBJECTS = join(GITLET_DIR, "objects");
    static final File LOGS = join(GITLET_DIR, "logs");
    static final File HEADS = join(REFS, "heads");
    static final File REMOTES = join(REFS, "remotes");
    static final File MASTER = join(HEADS, "master");

    public static void initialize() {
        GITLET_DIR.mkdirs();
        REFS.mkdirs();
        HEADS.mkdirs();
        REMOTES.mkdirs();
        OBJECTS.mkdirs();
        try {
            if (!MASTER.exists()) {
                LOGS.createNewFile();
                INDEX.createNewFile();
                MASTER.createNewFile();
                Commit initialCommit = new Commit("initial commit");
                LinkedHashMap<String, Commit> history = new LinkedHashMap<>();
                putNew(history, initialCommit.getId(),initialCommit);
                writeObject(MASTER, history);
            } else {
                throw error("A Gitlet version-control system already exists in the current directory.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void addFile(String name){
        File stagingFile = new File(CWD, name);
        if(!stagingFile.exists()){
            throw error("File does not exist.");
        }
        byte[] inp = Utils.readContents(stagingFile);
        String hash = Utils.sha1(inp);
        HashMap<String, String> Sites;
        try{
            Sites = readObject(INDEX, HashMap.class);
        } catch (Exception e) {
            Sites = new HashMap<>();
        }
        Sites.put(name, hash);
        writeObject(INDEX,Sites);
        File ADDED = join(OBJECTS, hash);
        try {
            ADDED.createNewFile();
            writeObject(ADDED, inp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void commitFile(String msg){
        Commit commit = new Commit(msg);
        LinkedHashMap<String, Commit> history = readObject(MASTER, LinkedHashMap.class);
        putNew(history, commit.getId(), commit);
        writeObject(MASTER, history);
    }
    public static void printLog(){
        ArrayList<Object> info = readObject(LOGS, ArrayList.class);
        Iterator<Object> iter = info.iterator();
        while(iter.hasNext()){
            System.out.println(iter.next());
        }
    }
    public static LinkedHashMap<String, Commit> putNew(LinkedHashMap<String, Commit> map, String key, Commit val){
        LinkedHashMap<String, Commit> newMap = new LinkedHashMap<>();
        newMap.put(key, val);
        newMap.putAll(map);
        return newMap;
    }
    // public static void commitFile
    /* TODO: fill in the rest of this class. */
}
