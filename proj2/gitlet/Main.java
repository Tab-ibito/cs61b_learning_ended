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
        //args = new String[]{"rm","a.txt"};
        if(args.length==0) {
            throw error("Please enter a command.");
        }
        //TODO: what if args is empty?
        String firstArg = args[0];
        //String firstArg = "init";
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
                    throw error("Incorrect operands.");
                }
                String fileName = args[1];
                gitlet.Repository.addFile(fileName);
                break;
            case "commit":
                if(args.length==1){
                    throw error("Please enter a commit message.");
                }
                if(args.length>2){
                    throw error("Incorrect operands.");
                }
                gitlet.Repository.commitFile(args[1]);
                break;
            case "rm":
                if(args.length!=2){
                    throw error("Incorrect operands.");
                }
                gitlet.Repository.removeFile(args[1]);
                break;
            case "log":
                if(args.length!=1){
                    throw error("Incorrect operands.");
                }
                Repository.printLog();
                break;
            case "global-log":
                if(args.length!=1){
                    throw error("Incorrect operands.");
                }
                Repository.printGlobalLog();
                break;
            case "find":
                if(args.length!=2){
                    throw error("Incorrect operands.");
                }
                Repository.findCommit(args[1]);
                break;
            case "status":
                if(args.length!=1){
                    throw error("Incorrect operands.");
                }
                Repository.printStatus();
                break;
            case "checkout":
                if(args.length==3 && args[1].equals("--")){
                    Repository.checkout(args[2]);
                }else if (args.length==4 && args[2].equals("--")){
                    Repository.checkout(args[1], args[3]);
                }else if (args.length==2){
                    Repository.switchBranch(args[1]);
                }
                break;
            case "branch":
                if(args.length!=2){
                    throw error("Incorrect operands.");
                }
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                if(args.length!=2){
                    throw error("Incorrect operands.");
                }
                Repository.removeBranch(args[1]);
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
