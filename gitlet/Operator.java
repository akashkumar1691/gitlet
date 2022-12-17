package gitlet;

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class Operator implements Serializable {

    CTree cTree = null;
    File path = new File(".gitlet");
    File objects = new File(path, "objects");
    File files = new File(path, "files");
    File cTreeDr = new File(objects, "CTree");

    public void toDisk() throws IOException {
        FileOutputStream fileout = new FileOutputStream(cTreeDr);
        ObjectOutputStream out = new ObjectOutputStream(fileout);
        out.writeObject(cTree);
        out.close();
        fileout.close();
    }

    public void fromDisk() throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(cTreeDr);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        cTree = (CTree) in.readObject();
        in.close();
        fileIn.close();
    }

    public void init() throws IOException {
        if (path.exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        } else {
            path.mkdir();
            objects.mkdir();
            files.mkdir();
            cTree = new CTree();
            toDisk();
        }
    }

    public void add(String fileName, Boolean notHelper)
            throws IOException, ClassNotFoundException {
        if (notHelper) {
            fromDisk();
        }
        File newfi = new File(fileName);
        if (!newfi.exists()) {
            System.out.println("File does not exist.");
            return;
        } else {
            byte[] fileByte = Utils.readContents(newfi);
            String fileSHA = Utils.sha1(fileByte);
            if (cTree.stageA.remove.containsKey(fileName)) {
                cTree.stageA.remove.remove(fileName);
            }
            if (cTree.head.pointer.names.containsValue(fileSHA)
                    && cTree.head.pointer.names.containsKey(fileName)
                     && cTree.head.pointer.names.get(fileName).equals(fileSHA)) {
                    //bug adding file after checkout w/
                    // same contents not being removed
                    //cTree.stageA._names.remove(fileName);
                toDisk();
                return;
            }
            File file = new File(files, fileName);
            if (!(file.exists())) {
                file.mkdir();
            }
                //Stage already exists.
            if (cTree.stageA._names.containsKey(fileName)) {
                cTree.stageA._names.replace(fileName, fileSHA);
            } else {
                cTree.stageA._names.put(fileName, fileSHA);
            }
            File curr = new File(file, fileSHA);
            if (!(curr.exists())) {
                Utils.writeContents(curr, fileByte);
            }
            if (notHelper) {
                toDisk();
            }
        }
    }

    public void commit(String cmsg, Boolean notHelper) throws IOException, ClassNotFoundException {
        if (notHelper) {
            fromDisk();
        }
        CTree.CNode commit;
        if (cTree.stageA._names.isEmpty()) {
            if (cTree.stageA.remove.isEmpty()) {
                System.out.println("No changes added to the commit.");
                return;
            } else {
                commit = cTree.addNode(cmsg);
            }
        } else {
            commit = cTree.addNode(cmsg);
        }
        cTree.head.pointer = commit;
        if (notHelper) {
            toDisk();
        }
    }

    public void log() throws IOException, ClassNotFoundException {
        fromDisk();
        String active = cTree.head.pointer.commitId;
        while (!(active.equals("done"))) {
            CTree.CNode rtn = cTree.cMap.get(active);
            System.out.println("===");
            System.out.println("Commit " + rtn.commitId);
            System.out.println(rtn.time);
            System.out.println(rtn.cmsg);
            System.out.println();
            if (rtn.parentId.equals("cNode")) {
                active = "done";
            } else {
                active = rtn.parentId;
            }
        }
        toDisk();
        // print first commit
    }

    public void global() throws IOException, ClassNotFoundException {
        fromDisk();
        for (String a : cTree.cMap.keySet()) {
            CTree.CNode aNode = cTree.cMap.get(a);
            System.out.println("===");
            System.out.println("Commit " + aNode.commitId);
            System.out.println(aNode.time);
            System.out.println(aNode.cmsg);
            System.out.println(" ");
        }
        toDisk();
    }

    public void find(String msg) throws IOException, ClassNotFoundException {
        fromDisk();
        boolean notFound = true;
        for (String a : cTree.cMap.keySet()) {
            CTree.CNode aNode = cTree.cMap.get(a);
            if (aNode.cmsg.equals(msg)) {
                System.out.println(aNode.commitId);
                notFound = false;
            }
        }
        if (notFound) {
            System.out.println("Found no commit with that message.");
        }
        toDisk();
    }

    public void remove(String name) throws IOException, ClassNotFoundException {
        fromDisk();
        if (cTree.stageA.tracked.containsKey(name)) {
            String sha = cTree.stageA.tracked.get(name);
            cTree.stageA.tracked.remove(name);
            cTree.stageA.remove.put(name, sha);
            File rmd = new File(name);
            if (cTree.stageA._names.containsKey(name)) {
                cTree.stageA._names.remove(name);
            }
            if (rmd.exists()) {
                rmd.delete();
            }
        } else if (cTree.stageA._names.containsKey(name)) {
            cTree.stageA._names.remove(name);
        } else {
            System.out.println("No reason to remove the file.");
            return;
        }
        toDisk();
    }

    public void status() throws IOException, ClassNotFoundException {
        fromDisk();
        Branch active = cTree.head;
        //Branches
        System.out.println("=== Branches ===");
        for (Branch b : cTree.branches) {
            if (b == active) {
                System.out.println("*" + b.name);
            } else {
                System.out.println(b.name);
            }
        }
        System.out.println();
        //Staged Files
        System.out.println("=== Staged Files ===");
        if (!(cTree.stageA._names.isEmpty())) {
            Object[] keys = cTree.stageA._names.keySet().toArray();
            Arrays.sort(keys);
            for (Object a : keys) {
                System.out.println(a);
            }
        }
        //Removed
        System.out.println();
        System.out.println("=== Removed Files ===");
        Object[] items = cTree.stageA.remove.keySet().toArray();
        Arrays.sort(items);
        for (Object a : items) {
            System.out.println(a);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        toDisk();
    }

    public void branch(String str) throws IOException, ClassNotFoundException {
        fromDisk();
        Branch branch = new Branch(str, cTree.head.pointer);
        if (cTree.branches.contains(branch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        cTree.addBranch(branch);
        toDisk();
    }

    public void removeBranch(String str)
            throws IOException, ClassNotFoundException {
        fromDisk();
        Branch d = null;
        Branch b = new Branch(str, null);
        if (!(cTree.branches.contains(b))) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (cTree.head.equals(b)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else {
            for (Branch c: cTree.branches) {
                if (c.name.equals(str)) {
                    d = c;
                }
            }
            cTree.removeBranch(d);
        }
        toDisk();
    }

    public void checkout1(String fileName)
            throws IOException, ClassNotFoundException {
        fromDisk();
        if (!(cTree.head.pointer.names.containsKey(fileName))) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            String fileSHA = cTree.head.pointer.names.get(fileName);
            File folder = new File(files, fileName);
            File file = new File(folder, fileSHA);
            byte[] contents = Utils.readContents(file);
            Utils.writeContents(new File(fileName), contents);
        }
        toDisk();
    }

    public void checkout2(String id, String name)
            throws IOException, ClassNotFoundException {
        fromDisk();
        if (!(cTree.cMap.containsKey(id)) && id.length() == 40) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            CTree.CNode node = null;
            if (id.length() == 40) {
                node = cTree.cMap.get(id);
            } else {
                int len = id.length();
                for (String ids : cTree.cMap.keySet()) {
                    String sub = ids.substring(0, len);
                    if (id.equals(sub)) {
                        node = cTree.cMap.get(ids);
                    }
                }
                if (node == null) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
            }
            if (!(node.names.containsKey(name))) {
                System.out.println("File does not exist in that commit.");
                return;
            } else {

                String fileSHA = node.names.get(name);
                File folder = new File(files, name);
                File file = new File(folder, fileSHA);
                byte[] contents = Utils.readContents(file);
                Utils.writeContents(new File(name), contents);
            }
        }
        toDisk();
    }

    public void checkout3(String branchName)
            throws IOException, ClassNotFoundException {
        fromDisk();
        Branch d = null;
        Branch b = new Branch(branchName, null);
        for (Branch c: cTree.branches) {
            if (c.name.equals(branchName)) {
                d = c;
            }
        }
        if (d != null) {
            for (String fileName : d.pointer.names.keySet()) {
                File curr = new File(fileName);
                if (curr.exists() && (!(cTree.head.pointer.names.containsKey(fileName)))) {
                    System.out.println("There is an untracked file in the way;"
                            + "delete it or add it first.");
                    return;
                }
            }
        }

        if (!(cTree.branches.contains(b))) {
            System.out.println("No such branch exists.");
            return;
        } else if (cTree.head.equals(b)) {
            System.out.println("No need to checkout the current branch.");
            return;
        } else {
            for (String name : cTree.stageA.tracked.keySet()) {
                if (!(d.pointer.names.containsKey(name))) {
                    File active = new File(name);
                    active.delete();
                }
            }
            for (String a : d.pointer.names.keySet()) {
                String fileSHA = d.pointer.names.get(a);
                File folder = new File(files, a);
                File file = new File(folder, fileSHA);
                byte[] contents = Utils.readContents(file);
                Utils.writeContents(new File(a), contents);
            }
            cTree.head = d;
            cTree.createStage(d.pointer.names);
        }
        toDisk();
    }

    public void merge(String branchname)
            throws IOException, ClassNotFoundException {
        fromDisk();
        HashMap<String, String> h = cTree.head.pointer.names;
        String splitPoint = "done";
        Boolean conflict = false;
        Branch d = null;
        for (Branch c : cTree.branches) {
            if (c.name.equals(branchname)) {
                d = c;
            }
        }
        if (d != null) {
            splitPoint = splitPoint(d);
            if (splitPoint.equals("done")) {
                return;
            }
        } else if (d == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (d.equals(cTree.head)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (((!(cTree.stageA._names.isEmpty())))
                || (!(cTree.stageA.remove.isEmpty()))) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        CTree.CNode splitNode = cTree.cMap.get(splitPoint);
        for (String givenName : d.pointer.names.keySet()) {
            if (splitNode.names.containsKey(givenName)) {
                if (!(cTree.head.pointer.names.containsKey(givenName))) {
                    if (!(splitNode.names.get(givenName).equals
                            (d.pointer.names.get(givenName)))) {
                        mergeConflict(d, givenName);
                        conflict = true;
                    }
                } else if (!(splitNode.names.get(givenName).equals
                        (d.pointer.names.get(givenName)))) {
                    if (h.get(givenName).equals(splitNode.names.get(givenName))) {
                        checkout4(d, givenName);
                    } else if (!(d.pointer.names.get(givenName)).equals(h.get(givenName))) {
                        mergeConflict(d, givenName);
                        conflict = true;
                    }
                }
            } else {
                if (!(cTree.head.pointer.names.containsKey(givenName))) {
                    checkout4(d, givenName);
                } else if (!(h.get(givenName).equals(d.pointer.names.get(givenName)))) {
                    mergeConflict(d, givenName);
                    conflict = true;
                }
            }
        }
        for (String splitName : splitNode.names.keySet()) {
            if (!(d.pointer.names.containsKey(splitName))) {
                if (cTree.head.pointer.names.containsKey(splitName)) {
                    if (h.get(splitName).equals(splitNode.names.get(splitName))) {
                        rmHelper(splitName);
                    } else {
                        mergeConflict(d, splitName);
                        conflict = true;
                    }
                }
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        } else {
            commit("Merged " + cTree.head.name + " with " + branchname + ".", false);
        }
        toDisk();
    }
    private void rmHelper(String splitName) {
        if (cTree.stageA.tracked.containsKey(splitName)) {
            String sha = cTree.stageA.tracked.get(splitName);
            cTree.stageA.tracked.remove(splitName);
            cTree.stageA.remove.put(splitName, sha);
        }
        if (cTree.stageA._names.containsKey(splitName)) {
            cTree.stageA._names.remove(splitName);
        }
        File rmd = new File(splitName);
        if (rmd.exists()) {
            rmd.delete();
        }
    }
    private void checkout4(Branch branch, String givenName)
            throws IOException, ClassNotFoundException {
        String givenSHA = branch.pointer.names.get(givenName);
        File folder = new File(files, givenName);
        File file = new File(folder, givenSHA);
        byte[] contents = Utils.readContents(file);
        Utils.writeContents(new File(givenName), contents);
        add(givenName, false);
    }

    private String splitPoint(Branch branch) throws IOException {
        Boolean fastforward = false;
        Boolean sameBranch = false;
        Boolean ancestor = false;
        String headId = cTree.head.pointer.commitId;
        cTree.given = branch;
        String givenId = cTree.given.pointer.commitId;
        String splitPoint = "done";
        int counter = 0;
        outerloop:
        while (!(splitPoint.equals(headId))) {
            for (CTree.CNode c : cTree) {
                if (c.commitId.equals(headId) && counter == 0 && (!(headId.equals(givenId)))) {
                    fastforward = true;
                    break outerloop;
                } else if (headId.equals(givenId) && counter == 0) {
                    //head and given pointing to same commit.
                    sameBranch = true;
                    break outerloop;
                } else if (headId.equals(givenId) && counter != 0) {
                    ancestor = true;
                    break outerloop;
                } else if (c.commitId.equals(headId)) {
                    splitPoint = c.commitId;
                    break outerloop;
                }
            }
            if (headId.equals("cNode")) {
                break;
            }
            counter += 1;
            headId = cTree.cMap.get(headId).parentId;
        }
        cTree.given = cTree.head;
        if (!(splitPoint.equals("done"))) {
            CTree.CNode splitNode = cTree.cMap.get(splitPoint);
            for (String filos: branch.pointer.names.keySet()) {
                String fileSHA = "";
                File filo = new File(filos);
                if (filo.exists()) {
                    fileSHA = Utils.sha1(Utils.readContents(filo));
                }
                if (!(cTree.head.pointer.names.containsKey(filos)) && filo.exists()
                        && !(fileSHA.equals(branch.pointer.names.get(filos)))
                        && !(splitNode.names.containsKey(filos))
                        && !(cTree.stageA.tracked.containsKey(filos))) {
                    //in removed, in given, in split, unmodified in head.
                    System.out.println("There is an untracked file in the way; "
                            + "delete it or add it first.");
                    return "done";
                }
            }
        } else if (fastforward) {
            cTree.head.pointer = cTree.given.pointer;
            System.out.println("Current branch fast-forwarded.");
            toDisk();
            return "done";
        } else if (sameBranch) {
            System.out.println("Cannot merge a branch with itself.");
            return "done";
        } else if (ancestor) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return "done";
        }
        return splitPoint;
    }

    private void mergeConflict(Branch given, String givenName) throws IOException {
        File folder = new File(files, givenName);
        File headFile = new File(folder, "idk");
        File givenFile = new File(folder, "ok");
        String shaHead = "empty";
        String shaGiven = "empty";
        if ((cTree.head.pointer.names.containsKey(givenName))) {
            shaHead = cTree.head.pointer.names.get(givenName);
            headFile = new File(folder, shaHead);
        } else {
            headFile.createNewFile();
        }
        if ((given.pointer.names.containsKey(givenName))) {
            shaGiven = given.pointer.names.get(givenName);
            givenFile = new File(folder, shaGiven);
        } else {
            givenFile.createNewFile();
        }
        byte[] headByte = Utils.readContents(headFile);
        byte[] givenByte = Utils.readContents(givenFile);
        String headString = new String(headByte);
        String givenString = new String(givenByte);
        String product = "<<<<<<< HEAD\n" + headString + "=======\n" + givenString + ">>>>>>>\n";
        byte[] result  = product.getBytes();
        Utils.writeContents(new File(givenName), result);
        if (shaGiven.equals("empty")) {
            givenFile.delete();
        }
        if (shaHead.equals("empty")) {
            headFile.delete();
        }
    }

    public void reset(String id) throws IOException, ClassNotFoundException {
        fromDisk();
        if (!(cTree.cMap.containsKey(id)) && id.length() > 35) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            CTree.CNode node = null;
            if (id.length() == 40) {
                node = cTree.cMap.get(id);
            } else {
                int len = id.length();
                for (String ids : cTree.cMap.keySet()) {
                    String sub = ids.substring(0, len);
                    if (id.equals(sub)) {
                        node = cTree.cMap.get(ids);
                    }
                }
                if (node == null) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
            }
            for (String name : node.names.keySet()) {
                File untracked = new File(name);
                if ((!(cTree.head.pointer.names.containsKey(name))) && untracked.exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it or add it first.");
                    return;
                }
            }
            for (String fileNames : node.names.keySet()) {
                String fileSHA = node.names.get(fileNames);
                File folder = new File(files, fileNames);
                File file = new File(folder, fileSHA);
                byte[] contents = Utils.readContents(file);
                Utils.writeContents(new File(fileNames), contents);
            }
            for (String name : cTree.stageA.tracked.keySet()) {
                if (!(node.names.containsKey(name))) {
                    File toDelete = new File(name);
                    toDelete.delete();
                }
            }
            cTree.head.pointer = node;
            cTree.createStage(node.names);
        }
        toDisk();
    }
}





