package gitlet;

import java.io.IOException;
import java.util.Arrays;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException, ClassNotFoundException {
        Operator o = new Operator();
        if (args[0].equals("init") && args.length == 1) {
            o.init();
        }
        if (args[0].equals("add")) {
            o.add(args[1], true);
        }
        if (args[0].equals("commit")) {
            if (args.length == 1) {
                System.out.println("Please enter a commit message.");
                return;
            } else if ((args[1].equals(""))) {
                System.out.println("Please enter a commit message.");
                return;
            } else if (!(args[1].charAt(0) == '"')) {
                o.commit(args[1], true);
            } else {
                String[] msg = Arrays.copyOfRange(args, 1, args.length);
                o.commit(msg.toString().replaceAll("^\"|\"$", ""), true);
            }
        }
        if (args[0].equals("rm")) {
            o.remove(args[1]);
        }
        if (args[0].equals("status")) {
            o.status();
        }
        if (args[0].equals("log")) {
            o.log();
        }
        if (args[0].equals("global-log")) {
            o.global();
        }
        if (args[0].equals("branch")) {
            o.branch(args[1]);
        }
        if (args[0].equals("find")) {
            if (!(args[1].charAt(0) == '"')) {
                o.find(args[1]);
            } else {
                String[] msg = Arrays.copyOfRange(args, 1, args.length);
                o.find(msg.toString().replaceAll("^\"|\"$", ""));
            }
        }
        if (args[0].equals("rm-branch")) {
            o.removeBranch(args[1]);
        }

        if (args[0].equals("checkout")) {
            if (args[1].equals("--")) {
                o.checkout1(args[2]);
            } else if (args.length > 2 && args[2].equals("--")) {
                o.checkout2(args[1], args[3]);
            } else if (args.length == 2) {
                o.checkout3(args[1]);
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
        }

        if (args[0].equals("merge")) {
            o.merge(args[1]);
        }
        if (args[0].equals("reset")) {
            o.reset(args[1]);
        }
    }
}
