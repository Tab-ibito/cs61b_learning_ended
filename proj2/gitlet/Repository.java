package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * <p>
 * does at a high level.
 *
 * @author Tab_1bit0
 */
public class Repository {
    /**
     *
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
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addFile(String name) {
        addFile(name, false);
    }

    public static void addFile(String name, boolean removed) {
        if(removed){
            HashMap<String, Stage> sites;
            sites = readObject(INDEX, HashMap.class);
            String hash = getCurrentCommit().getInfo().get(name);
            sites.put(name, new Stage(hash, removed));
            writeObject(INDEX, sites);
            return;
        }
        File stagingFile = new File(CWD, name);
        if (!stagingFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        byte[] inp = readContents(stagingFile);
        Commit commit = getCurrentCommit();
        String hash = sha1(inp);
        HashMap<String, Stage> sites;
        sites = readObject(INDEX, HashMap.class);
        sites.remove(name);
        writeObject(INDEX, sites);
        if(commit.getInfo().containsKey(name) && hash.equals(commit.getInfo().get(name))){
            return;
        }
        sites.put(name, new Stage(hash, removed));
        writeObject(INDEX, sites);
        File added = join(OBJECTS, hash);
        try {
            added.createNewFile();
            writeContents(added, inp);
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
        File branch = getCurrentBranchFile();
        LinkedList<String> history = readObject(branch, LinkedList.class);
        history.addFirst(commit.getId());
        writeObject(branch, history);
        INDEX.delete();
        try {
            INDEX.createNewFile();
            writeObject(INDEX, new HashMap<String, Stage>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printLog() {
        File branch = getCurrentBranchFile();
        LinkedList<String> map = readObject(branch, LinkedList.class);
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
        HashMap<String, Stage> sites = readObject(INDEX, HashMap.class);
        Stage removedItem = sites.remove(fileName);
        if (removedItem != null) {
            removed = true;
            writeObject(INDEX, sites);
        }
        Commit commit = getCurrentCommit();
        if (commit.getInfo().containsKey(fileName)) {
            removed = true;
            addFile(fileName, true);
            restrictedDelete(removedFile);
        }
        if (!removed) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static Commit getCurrentCommit() {
        String commitId = getCurrentCommitId();
        File commitFile = join(OBJECTS, commitId);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }

    private static String getCurrentCommitId() {
        File branch = getCurrentBranchFile();
        LinkedList<String> history = readObject(branch, LinkedList.class);
        String commitId = history.getFirst();
        return commitId;
    }

    private static Commit getCommit(String exceptedId, File branch) {
        LinkedList<String> history = readObject(branch, LinkedList.class);
        Commit commit = null;
        if (exceptedId.length() == UID_LENGTH && history.contains(exceptedId)) {
            commit = getCommitById(exceptedId);
            return commit;
        }
        if (exceptedId.length() < UID_LENGTH) {
            int count = 0;
            for (String historyId : history) {
                if (historyId.indexOf(exceptedId) == 0) {
                    count++;
                    commit = getCommitById(historyId);
                }
            }
            if (count > 1) {
                throw error("ambiguous argument " + exceptedId + ": unknown revision or path not in the working tree.");
            }
        }
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return commit;
    }

    private static Commit getCommit(String exceptedId) {
        return getCommit(exceptedId, getCurrentBranchFile());
    }

    private static Set<String> getTrackingFileNames() {
        Commit commit = getCurrentCommit();
        return commit.getInfo().keySet();
    }

    private static Set<String> getStagingFileNames() {
        HashMap<String, Stage> sites = readObject(INDEX, HashMap.class);
        return sites.keySet();
    }

    private static void printStringList(List<String> printed) {
        for (int i = 0; i < printed.toArray().length; i++) {
            System.out.println(printed.toArray()[i]);
        }
    }

    private static Commit getCommitById(String id) {
        File commitFile = join(OBJECTS, id);
        return readObject(commitFile, Commit.class);
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
            if (branchName.equals(printed.toArray()[i])) {
                System.out.println("*" + printed.toArray()[i]);
                continue;
            }
            System.out.println(printed.toArray()[i]);
        }
        List<String> stagedFiles = new ArrayList<>();
        List<String> removedFiles = new ArrayList<>();
        System.out.println();
        HashMap<String, Stage> sites = readObject(INDEX, HashMap.class);
        for (String fileName : sites.keySet()) {
            if (!sites.get(fileName).removed) {
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
        HashMap<String, Stage> sites = readObject(INDEX, HashMap.class);
        for (String i : sites.keySet()) {
            if (workingFiles.contains(i) && !sites.get(i).removed) {
                File working = join(CWD, i);
                byte[] inp = readContents(working);
                if (!sha1(inp).equals(sites.get(i).value)) {
                    result.add(i);
                }
            } else if (!workingFiles.contains(i) && !sites.get(i).removed) {
                result.add(i);
            }
        }
        return result;
    }

    public static void checkout(String fileName) {
        String commitId = getCurrentCommitId();
        checkout(commitId, fileName, getCurrentBranchFile());
    }

    public static void checkout(String fileName, File branch) {
        String commitId = getCurrentCommitId();
        checkout(commitId, fileName, branch);
    }

    public static void checkout(String commitId, String fileName) {
        checkout(commitId, fileName, getCurrentBranchFile());
    }

    public static void checkout(String commitId, String fileName, File branch) {
        Commit commit = getCommit(commitId, branch);
        if (!commit.getInfo().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String index = commit.getInfo().get(fileName);
        File fileInObjects = join(OBJECTS, index);
        byte[] fileContent = readContents(fileInObjects);
        File file = join(CWD, fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeContents(file, fileContent);
    }

    public static void switchBranch(String target) {
        String branchName = readObject(HEAD, String.class);
        File targetBranch = join(HEADS, target);
        if (branchName.equals(target)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if (!targetBranch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        LinkedList<String> history = readObject(targetBranch, LinkedList.class);
        String targetId = history.getFirst();
        File targetFile = join(OBJECTS, targetId);
        Commit targetCommit = readObject(targetFile, Commit.class);
        Set<String> tracking = getTrackingFileNames();
        for (String i : targetCommit.getInfo().keySet()) {
            if (!tracking.contains(i) && join(CWD, i).exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String fileName : tracking){
            removeFile(fileName);
        }
        for (String fileName : targetCommit.getInfo().keySet()) {
            checkout(targetId ,fileName, targetBranch);
        }
        writeObject(HEAD, target);
        writeObject(INDEX, new HashMap<String, Stage>());
    }

    public static void createBranch(String branchName) {
        File targetBranch = join(HEADS, branchName);
        File branch = getCurrentBranchFile();
        if (targetBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            targetBranch.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedList<String> history = readObject(branch, LinkedList.class);
        writeObject(targetBranch, history);
    }

    public static void removeBranch(String target) {
        File targetBranch = join(HEADS, target);
        if (!targetBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (target.equals(readObject(HEAD, String.class))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        targetBranch.delete();
    }

    public static void reset(String commitId) {
        File branch = getCurrentBranchFile();
        Commit commit = getCommit(commitId);
        Commit currentCommit = getCurrentCommit();
        commit.getInfo();
        for (String i : currentCommit.getInfo().keySet()) {
            removeFile(i);
        }
        LinkedList<String> history = readObject(branch, LinkedList.class);
        while (!history.getFirst().equals(commit.getId())) {
            history.removeFirst();
        }
        for (String i : getCommitById(history.getFirst()).getInfo().keySet()){
            checkout(commitId, i, branch);
        }
        writeObject(branch, history);
        writeObject(INDEX, new HashMap<String, Stage>());
    }

    public static void merge(String givenBranch) {
        String currentBranch = readObject(HEAD, String.class);
        File givenFile = join(HEADS, givenBranch);
        LinkedList<String> currentHistory = readObject(getCurrentBranchFile(), LinkedList.class);
        LinkedList<String> givenHistory = readObject(givenFile, LinkedList.class);
        HashMap<String, String> currentCommitInfo = getCurrentCommit().getInfo();
        HashMap<String, String> givenCommitInfo = getCommitById(givenHistory.getFirst()).getInfo();
        HashMap<String, String> splitCommitInfo = null;
        boolean status = false;
        for (String i : currentHistory) {
            if (givenHistory.contains(i)) {
                splitCommitInfo = getCommitById(i).getInfo();
                break;
            }
        }
        for (String i : splitCommitInfo.keySet()) {
            if (!isModified(i, splitCommitInfo, currentCommitInfo) && isModified(i, splitCommitInfo, givenCommitInfo)) {
                checkout(givenHistory.getFirst(), i, givenFile);
                addFile(i);
            }
            if (!givenCommitInfo.containsKey(i) && !isModified(i, splitCommitInfo, currentCommitInfo)) {
                removeFile(i);
            }
        }
        for (String i : givenCommitInfo.keySet()) {
            if (!splitCommitInfo.containsKey(i) && !currentCommitInfo.containsKey(i)) {
                checkout(givenHistory.getFirst(), i, givenFile);
                addFile(i);
            }
            if (isConflict(i, currentCommitInfo, givenCommitInfo) && isModified(i, splitCommitInfo, currentCommitInfo) && isModified(i, splitCommitInfo, givenCommitInfo)) {
                File conflictFile = join(CWD, i);
                String givenIndex = givenCommitInfo.get(i);
                String currentIndex = currentCommitInfo.get(i);
                File givenContent = join(OBJECTS, givenIndex);
                File currentContent = join(OBJECTS, currentIndex);
                byte[] givenFileContent = readContents(givenContent);
                byte[] currentFileContent = readContents(currentContent);
                try {
                    if (!conflictFile.exists()) {
                        conflictFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                status = true;
                writeContents(conflictFile, "<<<<<<< HEAD\n", currentFileContent, "\n=======\n", givenFileContent, "\n>>>>>>>");
                addFile(i);
            }
        }
        System.out.println("Merged " + givenBranch + " into " + currentBranch + ".");
        if (status) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static boolean isModified(String i, HashMap<String, String> origin, HashMap<String, String> info) {
        return info.containsKey(i) && !origin.get(i).equals(info.get(i));
    }

    public static boolean isConflict(String i, HashMap<String, String> current, HashMap<String, String> given) {
        return given.containsKey(i) && current.containsKey(i) && !given.get(i).equals(current.get(i));
    }

    protected static File getCurrentBranchFile() {
        String branchName = readObject(HEAD, String.class);
        return join(HEADS, branchName);
    }
}
