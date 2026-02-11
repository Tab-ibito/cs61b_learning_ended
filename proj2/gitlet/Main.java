package gitlet;

import gitlet.Utils.*;

import static gitlet.Utils.error;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if(args.length==0) {
            throw error("Please enter a command.");
        }
        //TODO: what if args is empty?
        String firstArg = args[0];
        //String firstArg = "log";
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if(args.length!=1){
                    error("Incorrect operands.");
                }
                gitlet.Repository.initialize();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if(args.length!=2){
                    error("Incorrect operands.");
                }
                String fileName = args[1];
                gitlet.Repository.addFile(fileName);
                break;
            case "commit":
                if(args.length==1){
                    error("Please enter a commit message.");
                }
                if(args.length>2){
                    error("Incorrect operands.");
                }
                gitlet.Repository.commitFile(args[1]);
                break;
            case "rm":
                break;
            case "log":
                if(args.length!=1){
                    error("Incorrect operands.");
                }
                Repository.printLog();
                break;
            case "global-log":
                break;
            case "find":
                break;
            case "status":
                break;
            case "checkout":
                break;
            case "branch":
                break;
            case "rm-branch":
                break;
            case "reset":
                break;
            case "merge":
                break;
            default:
                throw error("No command with that name exists.");
        }
    }
}
