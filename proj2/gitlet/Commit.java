package gitlet;

// TODO: any imports you need here

import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static java.lang.String.format;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Tab_1bit0
 */
public class Commit {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String date;
    private List<Object> info = new ArrayList<>();
    private String fatherId;
    private String id;
    /* TODO: fill in the rest of this class. */
    public Commit(String msg){
        message = msg;
        Date time = new Date();
        //Thu Nov 9 20:00:05 2017 -0800
        date = format(Locale.US,"%ta %tb %td %tT %tY %tZ",time,time,time,time,time,time);
        try{
            Iterator<String> iter = readObject(INDEX, HashMap.class).keySet().iterator();
            while (iter.hasNext()){
                info.add(iter.next());
            }
            LinkedHashMap<String, Commit> history = readObject(MASTER, LinkedHashMap.class);
            fatherId = Utils.sha1(history.entrySet().iterator().next());
            info.add(fatherId);
            info.add(message);
            info.add(date);
        } catch (Exception e) {
            info.add(message);
            info.add(date);
        }
        id=sha1(info);
        writeLog();
    }

    public String getId(){
        return id;
    }
    private void writeLog(){
        ArrayList<Object> result = new ArrayList<>();
        result.add("===");
        result.add("commit "+id);
        result.add("Date: "+date);
        result.add(message);
        result.add("");
        try{
            result.addAll(readObject(LOGS, ArrayList.class));
        } catch (Exception ignored) {

        }
        writeObject(LOGS,result);
    }
}
