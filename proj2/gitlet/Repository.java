package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
    static final File HEAD = join(GITLET_DIR, "HEAD");
    static final File HEADS = join(REFS, "heads");
    static final File REMOTES = join(REFS, "remotes");
    static final File MASTER = join(HEADS, "master");

    static class Stage implements Serializable {
        String value;
        boolean removed;
        Stage(String val, boolean rm) {
            value = val;
            removed = rm;
        }
    }

    public static void initialize() {
        GITLET_DIR.mkdirs();
        REFS.mkdirs();
        HEADS.mkdirs();
        REMOTES.mkdirs();
        OBJECTS.mkdirs();
        try {
            if (!MASTER.exists()) {
                HEAD.createNewFile();
                INDEX.createNewFile();
                MASTER.createNewFile();
                Commit initialCommit = new Commit("initial commit");
                LinkedList<String> history = new LinkedList<>();
                history.addFirst(initialCommit.getId());
                writeObject(HEAD, "master");
                writeObject(INDEX, new HashMap<String, Stage>());
                writeObject(MASTER, history);
            } else {
                throw error("A Gitlet version-control system already exists in the current directory.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addFile(String name) {
        addFile(name, false);
    }

    public static void addFile(String name, boolean removed) {
        File stagingFile = new File(CWD, name);
        if (!stagingFile.exists()) {
            throw error("File does not exist.");
        }
        byte[] inp = readContents(stagingFile);
        String hash = sha1(inp);
        HashMap<String, Stage> Sites;
        Sites = readObject(INDEX, HashMap.class);
        Sites.put(name, new Stage(hash, removed));
        writeObject(INDEX, Sites);
        File ADDED = join(OBJECTS, hash);
        try {
            ADDED.createNewFile();
            writeContents(ADDED, inp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void commitFile(String msg) {
        Commit commit = new Commit(msg);
        if (!commit.isChanged()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        File BRANCH = getCurrentBranchFile();
        LinkedList<String> history = readObject(BRANCH, LinkedList.class);
        history.addFirst(commit.getId());
        writeObject(BRANCH, history);
        INDEX.delete();
        try {
            INDEX.createNewFile();
            writeObject(INDEX, new HashMap<String, Stage>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printLog() {
        File BRANCH = getCurrentBranchFile();
        LinkedList<String> map = readObject(BRANCH, LinkedList.class);
        for (String key : map) {
            File commitFile = join(OBJECTS, key);
            Commit gotten = readObject(commitFile, Commit.class);
            printSingleCommit(gotten);
        }
    }

    private static void printSingleCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getId());
        System.out.println("Date: " + commit.getDate());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void printGlobalLog() {
        Iterator<String> iter = plainFilenamesIn(OBJECTS).iterator();
        File commitFile;
        while (iter.hasNext()) {
            commitFile = join(OBJECTS, iter.next());
            try {
                Commit commit = readObject(commitFile, Commit.class);
                printSingleCommit(commit);
            } catch (Exception ignored) {
            }
        }
    }

    public static void removeFile(String fileName) {
        boolean removed = false;
        File removedFile = join(CWD, fileName);
        HashMap<String, Stage> Sites = readObject(INDEX, HashMap.class);
        String removedId = Sites.remove(fileName).value;
        if (removedId != null) {
            removed = true;
        }
        writeObject(INDEX, Sites);
        Commit commit = getCurrentCommit();
        if (commit.getInfo().containsKey(fileName)) {
            removed = true;
            addFile(fileName, true);
            restrictedDelete(removedFile);
        }
        if (!removed) {
            throw error("No reason to remove the file.");
        }
    }

    private static Commit getCurrentCommit() {
        String commitId = getCurrentCommitId();
        File commitFile = join(OBJECTS, commitId);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }

    private static String getCurrentCommitId(){
        File BRANCH = getCurrentBranchFile();
        LinkedList<String> history = readObject(BRANCH, LinkedList.class);
        String commitId = history.getFirst();
        return commitId;
    }
    private static Commit getCommit(String exceptedId){
        File BRANCH = getCurrentBranchFile();
        LinkedList<String> history = readObject(BRANCH, LinkedList.class);
        File commitFile;
        Commit commit = null;
        if(exceptedId.length()==UID_LENGTH && history.contains(exceptedId)){
            commitFile = join(OBJECTS, exceptedId);
            commit = readObject(commitFile, Commit.class);
            return commit;
        }
        if(exceptedId.length()<UID_LENGTH){
            int count = 0;
            for(String historyId : history){
                if (historyId.indexOf(exceptedId)==0){
                    count++;
                    commitFile = join(OBJECTS, historyId);
                    commit = readObject(commitFile, Commit.class);
                }
            }
            if(count > 1){
                throw error("ambiguous argument "+exceptedId+": unknown revision or path not in the working tree.");
            }
        }
        return commit;
    }

    private static Set<String> getTrackingFileNames() {
        Commit commit = getCurrentCommit();
        return commit.getInfo().keySet();
    }

    private static Set<String> getStagingFileNames() {
        HashMap<String, Stage> Sites = readObject(INDEX, HashMap.class);
        return Sites.keySet();
    }

    private static void printStringList(List<String> printed){
        for (int i = 0; i < printed.toArray().length; i++) {
            System.out.println(printed.toArray()[i]);
        }
    }

    public static void findCommit(String msg) {
        Iterator<String> iter = plainFilenamesIn(OBJECTS).iterator();
        File commitFile;
        boolean printed = false;
        while (iter.hasNext()) {
            commitFile = join(OBJECTS, iter.next());
            try {
                Commit commit = readObject(commitFile, Commit.class);
                if (commit.getMessage().equals(msg)) {
                    System.out.println(commit.getId());
                    printed = true;
                }
            } catch (Exception ignored) {
            }
        }
        if (!printed) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void printStatus() {
        System.out.println("=== Branches ===");
        String branchName = readObject(HEAD, String.class);
        List<String> printed = plainFilenamesIn(HEADS);
        for (int i = 0; i < printed.toArray().length; i++) {
            if(branchName.equals(printed.toArray()[i])){
                System.out.println("*"+printed.toArray()[i]);
                continue;
            }
            System.out.println(printed.toArray()[i]);
        }
        List<String> stagedFiles = new ArrayList<>();
        List<String> removedFiles = new ArrayList<>();
        System.out.println();
        HashMap<String, Stage> Sites = readObject(INDEX, HashMap.class);
        for (String fileName : Sites.keySet()) {
            if (!Sites.get(fileName).removed) {
                stagedFiles.add(fileName);
            } else {
                removedFiles.add(fileName);
            }
        }
        System.out.println("=== Staged Files ===");
        printStringList(stagedFiles);
        System.out.println();
        System.out.println("=== Removed Files ===");
        printStringList(removedFiles);
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> specialResult = specialCheck();
        printStringList(specialResult);
        System.out.println();
        System.out.println("=== Untracked Files ===");
        List<String> workingFiles = plainFilenamesIn(CWD);
        for (String i : workingFiles) {
            File working = join(CWD, i);
            if (!working.isDirectory() && !getStagingFileNames().contains(i) && !getTrackingFileNames().contains(i)) {
                System.out.println(i);
            }
        }
    }

    private static List<String> specialCheck() {
        List<String> result = new ArrayList<>();
        List<String> workingFiles = plainFilenamesIn(CWD);
        Commit commit = getCurrentCommit();
        for (String i : commit.getInfo().keySet()) {
            if (workingFiles.contains(i)) {
                File working = join(CWD, i);
                byte[] inp = readContents(working);
                if (!sha1(inp).equals(commit.getInfo().get(i))) {
                    result.add(i);
                }
            } else if (!workingFiles.contains(i) && !getStagingFileNames().contains(i)) {
                result.add(i);
            }
        }
        HashMap<String, Stage> Sites = readObject(INDEX, HashMap.class);
        for (String i : Sites.keySet()) {
            if (workingFiles.contains(i) && !Sites.get(i).removed) {
                File working = join(CWD, i);
                byte[] inp = readContents(working);
                if (!sha1(inp).equals(Sites.get(i).value)) {
                    result.add(i);
                }
            } else if (!workingFiles.contains(i) && !Sites.get(i).removed) {
                result.add(i);
            }
        }
        return result;
    }

    public static void checkout(String fileName){
        String commitId = getCurrentCommitId();
        checkout(commitId, fileName);
    }

    public static void checkout(String commitId, String fileName){
        Commit commit = getCommit(commitId);
        if (commit==null){
            throw error("No commit with that id exists.");
        }
        if(!commit.getInfo().containsKey(fileName)){
            throw error("File does not exist in that commit.");
        }
        String index = commit.getInfo().get(fileName);
        File fileInObjects = join(OBJECTS, index);
        byte[] fileContent = readContents(fileInObjects);
        File file = join(CWD, fileName);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        writeContents(file, fileContent);
    }

    public static void switchBranch(String target){
        String branchName = readObject(HEAD, String.class);
        File TARGET = join(HEADS, target);
        LinkedList<String> history = readObject(TARGET, LinkedList.class);
        String targetId = history.getFirst();
        File targetFile = join(OBJECTS, targetId);
        Commit targetCommit = readObject(targetFile, Commit.class);
        Set<String> tracking = getTrackingFileNames();
        if (branchName.equals(target)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if(!TARGET.exists()){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        for(String i : targetCommit.getInfo().keySet()){
            if(!tracking.contains(i)){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for(String fileName : targetCommit.getInfo().keySet()){
            checkout(fileName);
        }
        writeObject(HEAD, target);
    }

    public static void createBranch(String branchName){
        File TARGET = join(HEADS, branchName);
        File BRANCH = getCurrentBranchFile();
        if(TARGET.exists()){
            throw error("A branch with that name already exists.");
        }
        try {
            TARGET.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedList<String> history = readObject(BRANCH, LinkedList.class);
        writeObject(TARGET, history);
    }

    public static void removeBranch(String target){
        File TARGET = join(HEADS, target);
        if(!TARGET.exists()){
            throw error("A branch with that name does not exist.");
        }
        if (target.equals(readObject(HEAD, String.class))){
            throw error("Cannot remove the current branch.");
        }
        TARGET.delete();
    }

    protected static File getCurrentBranchFile(){
        String branchName = readObject(HEAD, String.class);
        return join(HEADS, branchName);
    }
    /* TODO: fill in the rest of this class. */
}
