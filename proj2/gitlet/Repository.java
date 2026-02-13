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
    static final File CONFIG = join(GITLET_DIR, "config");

    static class Stage implements Serializable {
        String value;
        boolean removed;

        Stage(String val, boolean rm) {
            value = val;
            removed = rm;
        }
    }

    static class Remote {
        File dir;
        File head;
        File index;
        File objects;
        File refs;
        File remotes;
        File heads;

        Remote(String path){
            try {
                dir = join(CWD, path).getCanonicalFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            head = join(dir, "HEAD");
            index = join(dir, "index");
            objects = join(dir, "objects");
            refs = join(dir, "refs");
            remotes = join(refs, "remotes");
            heads = join(refs, "heads");
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
                CONFIG.createNewFile();
                writeObject(HEAD, "master");
                writeObject(INDEX, new HashMap<String, Stage>());
                writeObject(CONFIG, new HashMap<String, String>());
                Commit initialCommit = new Commit("initial commit", false, null);
                LinkedList<String> history = new LinkedList<>();
                history.addFirst(initialCommit.getId());
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
        if (removed) {
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
        if (commit.getInfo().containsKey(name) && hash.equals(commit.getInfo().get(name))) {
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
        commitFile(msg, false, null);
    }

    public static void commitFile(String msg, boolean merging, String mergingBranch) {
        Commit commit = new Commit(msg, merging, mergingBranch);
        if (!commit.isChanged()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        File branch = getCurrentBranchFile();
        LinkedList<String> history = readObject(branch, LinkedList.class);
        history.addFirst(commit.getId());
        writeObject(branch, history);
        writeObject(INDEX, new HashMap<String, Stage>());
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
        if (commit.isMerging()) {
            System.out.println("Merge: " + commit.getFatherId().substring(0, 7) + " " + commit.getSecondParentId().substring(0, 7));
        }
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

    private static Commit getCommit(String expectedId, File branch) {
        LinkedList<String> history = readObject(branch, LinkedList.class);
        String full = getFullUid(expectedId);
        if (history.contains(full)) {
            return getCommitById(full);
        } else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return null;
    }

    private static String getFullUid(String shortened) {
        String full = null;
        int count = 0;
        for (String objectId : plainFilenamesIn(OBJECTS)) {
            if (objectId.indexOf(shortened) == 0) {
                full = objectId;
                count++;
            }
        }
        if (count > 1) {
            throw error("ambiguous argument " + shortened + ": unknown revision or path not in the working tree.");
        }
        if (full == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return full;
    }

    private static Commit getCommit(String expectedId) {
        return getCommit(expectedId, getCurrentBranchFile());
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
        if (printed != null) {
            Collections.sort(printed);
        }
        for (int i = 0; i < printed.toArray().length; i++) {
            if (branchName.equals(printed.toArray()[i])) {
                System.out.println("*" + printed.toArray()[i]);
                continue;
            }
            System.out.println(printed.toArray()[i]);
        }
        List<String> stagedFiles = new ArrayList<>();
        List<String> removedFiles = new ArrayList<>();
        HashMap<String, Stage> sites = readObject(INDEX, HashMap.class);
        for (String fileName : sites.keySet()) {
            if (!sites.get(fileName).removed) {
                stagedFiles.add(fileName);
            } else {
                removedFiles.add(fileName);
            }
        }
        System.out.println();
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
        if(workingFiles!=null){
            Collections.sort(workingFiles);
        }
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
                    result.add(i+" (modified)");
                }
            } else if (!workingFiles.contains(i) && !getStagingFileNames().contains(i)) {
                result.add(i+" (deleted)");
            }
        }
        HashMap<String, Stage> sites = readObject(INDEX, HashMap.class);
        for (String i : sites.keySet()) {
            if (workingFiles.contains(i) && !sites.get(i).removed) {
                File working = join(CWD, i);
                byte[] inp = readContents(working);
                if (!sha1(inp).equals(sites.get(i).value)) {
                    result.add(i+" (modified)");
                }
            } else if (!workingFiles.contains(i) && !sites.get(i).removed) {
                result.add(i+" (deleted)");
            }
        }
        return result;
    }

    public static void checkout(String fileName) {
        String commitId = getCurrentCommitId();
        checkout(commitId, fileName);
    }

    public static void checkout(String commitId, String fileName) {
        Commit commit = getCommitById(getFullUid(commitId));
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
        for (String fileName : tracking) {
            File hiddenFile = join(CWD, fileName);
            hiddenFile.delete();
        }
        for (String fileName : targetCommit.getInfo().keySet()) {
            checkout(targetId, fileName);
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
        Commit currentCommit = getCurrentCommit();
        for (String i : currentCommit.getInfo().keySet()) {
            removeFile(i);
        }
        Commit target = getCommitById(getFullUid(commitId));
        LinkedList<String> history = new LinkedList<>();
        String pointer = target.getFatherId();
        history.addLast(target.getId());
        while (pointer != null) {
            history.addLast(pointer);
            Commit next = getCommitById(getFullUid(pointer));
            pointer = next.getFatherId();
        }
        Set<String> tracking = getTrackingFileNames();
        for (String i : target.getInfo().keySet()) {
            if (!tracking.contains(i) && join(CWD, i).exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String i : target.getInfo().keySet()) {
            checkout(commitId, i);
        }
        writeObject(branch, history);
        writeObject(INDEX, new HashMap<String, Stage>());
    }

    private static HashMap<String, Integer> getAllAncestors(String id) {
        HashMap<String, Integer> res = new HashMap<>();
        Queue<String> bfs = new ArrayDeque<>();
        Queue<Integer> depth = new ArrayDeque<>();
        bfs.add(id);
        depth.add(0);
        while (!bfs.isEmpty()) {
            String pointer = bfs.remove();
            Integer level = depth.remove();
            res.put(pointer, level);
            if (getCommitById(pointer).getFatherId() != null) {
                bfs.add(getCommitById(pointer).getFatherId());
                depth.add(level + 1);
            }
            if (getCommitById(pointer).getSecondParentId() != null) {
                bfs.add(getCommitById(pointer).getSecondParentId());
                depth.add(level + 1);
            }
        }
        return res;
    }

    private static String getSplitCommit(String givenId, String currentId) {
        String solution = null;
        Integer level = null;
        HashMap<String, Integer> currentAncestors = getAllAncestors(currentId);
        Queue<String> bfs = new ArrayDeque<>();
        bfs.add(givenId);
        while (!bfs.isEmpty()) {
            String pointer = bfs.remove();
            String newFather = getCommitById(pointer).getFatherId();
            String newSecondParent = getCommitById(pointer).getSecondParentId();
            if (newFather != null) {
                bfs.add(newFather);
            }
            if (newSecondParent != null) {
                bfs.add(newSecondParent);
            }
            if (currentAncestors.containsKey(pointer) && (level == null || currentAncestors.get(pointer) < level)) {
                solution = pointer;
                level = currentAncestors.get(pointer);
            }
        }
        return solution;
    }

    public static void merge(String givenBranch) {
        String currentBranch = readObject(HEAD, String.class);
        if (currentBranch.equals(givenBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        File givenFile = join(HEADS, givenBranch);
        if (!givenFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        LinkedList<String> currentHistory = readObject(getCurrentBranchFile(), LinkedList.class);
        LinkedList<String> givenHistory = readObject(givenFile, LinkedList.class);
        HashMap<String, String> currentCommitInfo = getCurrentCommit().getInfo();
        HashMap<String, String> givenCommitInfo = getCommitById(givenHistory.getFirst()).getInfo();
        Commit splitCommit = getCommitById(getSplitCommit(givenHistory.getFirst(), getCurrentCommitId()));
        HashMap<String, String> splitCommitInfo = splitCommit.getInfo();
        HashMap<String, String> pool = new HashMap<>();
        boolean status = false;
        pool.putAll(currentCommitInfo);
        pool.putAll(givenCommitInfo);
        pool.putAll(splitCommitInfo);
        Set<String> tracking = getTrackingFileNames();
        HashMap<String, Stage> sites = readObject(INDEX, HashMap.class);
        if (!sites.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        for (String i : givenCommitInfo.keySet()) {
            if (!tracking.contains(i) && join(CWD, i).exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        if (splitCommit.getId().equals(getCommitById(givenHistory.getFirst()).getId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitCommit.getId().equals(getCurrentCommitId())) {
            reset(givenHistory.getFirst());
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        for (String i : pool.keySet()) {
            boolean inSplit = in(i, splitCommitInfo);
            boolean inCurrent = in(i, currentCommitInfo);
            boolean inGiven = in(i, givenCommitInfo);
            boolean modifiedGiven = isModified(i, splitCommitInfo, givenCommitInfo);
            boolean modifiedCurrent = isModified(i, splitCommitInfo, currentCommitInfo);
            boolean conflict = isConflict(i, givenCommitInfo, currentCommitInfo);
            if (inSplit && inCurrent && inGiven && !modifiedCurrent && modifiedGiven) {
                checkout(givenHistory.getFirst(), i);
                addFile(i);
            }
            if (inSplit && !inGiven && inCurrent && !modifiedCurrent) {
                removeFile(i);
            }
            if (inGiven && !inSplit && !inCurrent) {
                checkout(givenHistory.getFirst(), i);
                addFile(i);
            }
            if (!inSplit && conflict || modifiedGiven && modifiedCurrent && conflict || modifiedGiven && !inCurrent || modifiedCurrent && !inGiven) {
                File conflictFile = join(CWD, i);
                String givenIndex = givenCommitInfo.get(i);
                String currentIndex = currentCommitInfo.get(i);
                byte[] givenFileContent = new byte[0];
                byte[] currentFileContent = new byte[0];
                if (givenIndex != null) {
                    File givenContent = join(OBJECTS, givenIndex);
                    givenFileContent = readContents(givenContent);
                }
                if (currentIndex != null) {
                    File currentContent = join(OBJECTS, currentIndex);
                    currentFileContent = readContents(currentContent);
                }
                try {
                    if (!conflictFile.exists()) {
                        conflictFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                status = true;
                writeContents(conflictFile, "<<<<<<< HEAD\n", currentFileContent, "=======\n", givenFileContent, ">>>>>>>\n");
                addFile(i);
            }
        }
        String msg = "Merged " + givenBranch + " into " + currentBranch + ".";
        commitFile(msg, true, givenBranch);
        if (status) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void checkEnvironment() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static boolean in(String i, HashMap<String, String> range) {
        return range.containsKey(i);
    }

    private static boolean isModified(String i, HashMap<String, String> origin, HashMap<String, String> info) {
        return origin.containsKey(i) && info.containsKey(i) && !origin.get(i).equals(info.get(i));
    }

    private static boolean isConflict(String i, HashMap<String, String> current, HashMap<String, String> given) {
        return given.containsKey(i) && current.containsKey(i) && !given.get(i).equals(current.get(i));
    }

    protected static File getCurrentBranchFile() {
        String branchName = readObject(HEAD, String.class);
        return join(HEADS, branchName);
    }

    public static void addRemote(String name, String location) {
        HashMap<String, String> info = readObject(CONFIG, HashMap.class);
        if(info.containsKey(name)){
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        info.put(name, location);
        writeObject(CONFIG, info);
    }
    public static void removeRemote(String name) {
        HashMap<String, String> info = readObject(CONFIG, HashMap.class);
        String removed = info.remove(name);
        if(removed==null){
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        writeObject(CONFIG, info);
    }

    private static Remote getRemoteEnv(String name){
        HashMap<String, String> info = readObject(CONFIG, HashMap.class);
        if(info.get(name)==null){
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        return new Remote(info.get(name));
    }

    public static String getRemoteId(Remote env, String branch){
        if(!env.dir.exists()){
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        File branchFile = join(env.heads, branch);
        if(!branchFile.exists()){
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
        LinkedList<String> remoteHistory = readObject(branchFile, LinkedList.class);
        return remoteHistory.getFirst();
    }

    public static void pushRemote(String remoteName, String remoteBranchName){
        Remote env = getRemoteEnv(remoteName);
        File remoteBranchFile = join(HEADS, remoteName, remoteBranchName);
        String remoteId = getRemoteId(env, remoteBranchName);
        if(!remoteBranchFile.exists()){
            System.out.println("Please pull down remote changes before pushing.");
            System.exit(0);
        }
        LinkedList<String> result = readObject(getCurrentBranchFile(), LinkedList.class);
        HashMap<String, Integer> ancestors = getAllAncestors(getCurrentCommitId());
        if(ancestors.containsKey(getRemoteId(env, remoteBranchName))){
            for (String i : ancestors.keySet()){
                uploadFile(env, i);
                for (String j : getCommitById(i).getInfo().values()){
                    uploadFile(env, j);
                }
            }
        }
        writeObject(join(env.heads, remoteBranchName), result);
    }

    private static void copyRemoteFile(Remote env, String id){
        File originalFile = join(env.objects, id);
        File copyFile = join(OBJECTS, id);
        byte[] content = readContents(originalFile);
        try {
            copyFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeObject(copyFile, content);
    }

    private static void uploadFile(Remote env, String id){
        File originalFile = join(OBJECTS, id);
        File copyFile = join(env.objects, id);
        byte[] content = readContents(originalFile);
        try {
            copyFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeObject(copyFile, content);
    }

    private static Commit getRemoteCommitById(String id, Remote env) {
        return readObject(join(env.objects, id), Commit.class);
    }

    public static void fetchRemote(String remoteName, String remoteBranchName){
        Remote env = getRemoteEnv(remoteName);
        String remoteId = getRemoteId(env, remoteBranchName);
        File remoteBranchDir = join(HEADS, remoteName);
        remoteBranchDir.mkdirs();
        File remoteBranchFile = join(HEADS, remoteName, remoteBranchName);
        LinkedList<String> remoteHistory = readObject(join(env.heads, remoteBranchName), LinkedList.class);
        try {
            remoteBranchFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeObject(remoteBranchFile, remoteHistory);
        Queue<String> bfs = new ArrayDeque<>();
        bfs.add(remoteId);
        while (!bfs.isEmpty()) {
            String pointer = bfs.remove();
            Commit commit = getRemoteCommitById(pointer, env);
            if (commit.getFatherId() != null) {
                bfs.add(commit.getFatherId());
            }
            if (commit.getSecondParentId() != null) {
                bfs.add(commit.getSecondParentId());
            }
            copyRemoteFile(env, pointer);
            for(String i : commit.getInfo().values()){
                copyRemoteFile(env, i);
            }
        }
    }

    public static void pullRemote(String remoteName, String remoteBranchName){
        fetchRemote(remoteName, remoteBranchName);
        merge(remoteBranchName);
    }
}
