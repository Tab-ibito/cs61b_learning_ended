package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static java.lang.String.format;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Tab_1bit0
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;
    private String date;
    private HashMap<String, String> info = new HashMap<>();
    private String fatherId;
    private String id;
    private boolean changed = false;

    /* TODO: fill in the rest of this class. */
    public Commit(String msg) {
        message = msg;
        Date time = new Date();
        //Thu Nov 9 20:00:05 2017 -0800
        date = format(Locale.US, "%ta %tb %td %tT %tY %tz", time, time, time, time, time, time);
        List<Object> material = new ArrayList<>();
        List<String> blacklist = new ArrayList<>();
        try {
            HashMap<String, Stage> Sites = readObject(INDEX, HashMap.class);
            Iterator<String> iter = Sites.keySet().iterator();
            while (iter.hasNext()) {
                String i = iter.next();
                if (!Sites.get(i).removed) {
                    info.put(i, Sites.get(i).value);
                } else {
                    blacklist.add(i);
                }
            }
            changed = true;
        } catch (Exception ignored) {
        } finally {
            try {
                LinkedList<String> history = readObject(Repository.getCurrentBranchFile(), LinkedList.class);
                fatherId = history.getFirst();
                File commitFile = join(OBJECTS, fatherId);
                Commit commit = readObject(commitFile, Commit.class);
                Iterator<String> iter = commit.getInfo().keySet().iterator();
                while (iter.hasNext()) {
                    String i = iter.next();
                    if (!blacklist.contains(i) && info.get(i) == null) {
                        info.put(i, commit.getInfo().get(i));
                    }
                }
                material.add(fatherId);
                material.addAll(info.values());
            } catch (Exception e) {
                material.add(message);
                material.add(date);
            }
        }
        id = sha1(material);
        File commitFile = join(OBJECTS, id);
        try {
            commitFile.createNewFile();
            writeObject(commitFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public boolean isChanged() {
        return changed;
    }

    public HashMap<String, String> getInfo() {
        return info;
    }
}
