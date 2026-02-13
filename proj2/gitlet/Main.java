package gitlet;

import gitlet.Utils.*;

import static gitlet.Utils.error;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Tab_1bit0
 */
public class Main {
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        //args = new String[]{"rm","a.txt"};
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        //String firstArg = "init";
        switch (firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.initialize();
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String fileName = args[1];
                gitlet.Repository.checkEnvironment();
                gitlet.Repository.addFile(fileName);
                break;
            case "commit":
                if (args.length == 1 || args.length == 2 && args[1].isEmpty()) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                gitlet.Repository.commitFile(args[1]);
                break;
            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                gitlet.Repository.removeFile(args[1]);
                break;
            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.printLog();
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.printGlobalLog();
                break;
            case "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.findCommit(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.printStatus();
                break;
            case "checkout":
                if (args.length == 3 && args[1].equals("--")) {
                    gitlet.Repository.checkEnvironment();
                    Repository.checkout(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    gitlet.Repository.checkEnvironment();
                    Repository.checkout(args[1], args[3]);
                } else if (args.length == 2) {
                    gitlet.Repository.checkEnvironment();
                    Repository.switchBranch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.reset(args[1]);
                break;
            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.merge(args[1]);
                break;
            case "add-remote":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.addRemote(args[1],args[2]);
                break;
            case "rm-remote":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.removeRemote(args[1],args[2]);
                break;
            case "push":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.pushRemote(args[1],args[2]);
                break;
            case "fetch":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.fetchRemote(args[1],args[2]);
                break;
            case "pull":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                gitlet.Repository.checkEnvironment();
                Repository.pullRemote(args[1],args[2]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
