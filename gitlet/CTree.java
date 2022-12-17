package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.Iterator;


public class CTree implements Serializable, Iterable<CTree.CNode> {

    Branch head;
    ArrayList<Branch> branches;
    HashMap<String, CNode> cMap;
    Stage stageA;
    Branch given;


    public CTree() {
        CNode first = new CNode();
        Branch master = new Branch("master", first);
        this.cMap = new HashMap<>();
        this.cMap.put(first.commitId, first);
        this.head = master;
        this.branches = new ArrayList<>();
        //head.pointer should change with current branch
        addBranch(master);
        this.given = this.head;
    }

    public CTreeIterator iterator() {
        return new CTreeIterator();
    }

    private class CTreeIterator implements Iterator<CTree.CNode> {
        String curr;

        public CTreeIterator() {
            this.curr = given.pointer.commitId;
        }
        @Override
        public boolean hasNext() {
            return (!(curr.equals("done")));
        }

        @Override
        public CNode next() {
            CNode rtn = cMap.get(curr);
            if (rtn.parentId.equals("cNode")) {
                curr = "done";
            } else {
                curr = rtn.parentId;
            }
            return rtn;
        }
    }

    public void addBranch(Branch newBranch) {
        branches.add(newBranch);
        branches.sort(Comparator.naturalOrder());
    }

    public void removeBranch(Branch b) {
        branches.remove(b);
    }

    public CNode addNode(String msg) {
        CNode commit = new CNode(msg);
        this.cMap.put(commit.commitId, commit);
        return commit;
    }

    public Stage createStage(HashMap<String, String> tracked) {
        Stage S = new Stage(tracked);
        this.stageA = S;
        return S;
    }


    public class CNode implements Serializable {
        String commitId;
        String parentId;
        String cmsg;
        HashMap<String, String> names;
        String time;


        public CNode() {
            this.cmsg = "initial commit";
            this.parentId = "cNode";
            this.names = new HashMap<>();
            String[] times = LocalTime.now().toString().split("\\.");
            this.time = LocalDate.now() + " " + times[0];
            this.commitId = this.commitId(this);
            stageA = createStage(this.names);
        }

        public CNode(String cmsg) {
            this.cmsg = cmsg;
            this.parentId = head.pointer.commitId;
            this.names = new HashMap<>(stageA.tracked);
            String[] times = LocalTime.now().toString().split("\\.");
            this.time = LocalDate.now() + " " + times[0];
            this.commitId = this.commitId(this);
            for (String me : stageA._names.keySet()) {
                if (this.names.containsKey(me)) {
                    this.replaceName(me, stageA._names.get(me));
                } else {
                    this.addName(me, stageA._names.get(me));
                }
            }
            stageA = createStage(this.names);
        }

        @Override
        public String toString() {
            return this.cmsg;
        }

        private String commitId(CNode c) {
            String sha = Utils.sha1(c.cmsg + c.names.toString()
                    + c.parentId + c.time);
            return sha;
        }

        public void addName(String key, String val) {
            cMap.remove(this.commitId);
            this.names.put(key, val);
            this.commitId = commitId(this);
            cMap.put(this.commitId, this);
        }

        public void replaceName(String key, String val) {
            cMap.remove(this.commitId);
            this.names.replace(key, val);
            this.commitId = commitId(this);
            cMap.put(this.commitId, this);
        }

    }

    public class Stage implements Serializable {
        //file name, SHA-1
        HashMap<String, String> _names;
        //any files called from rm, filename, SHA-1
        HashMap<String, String> remove;
        HashMap<String, String> tracked;

        public Stage(HashMap<String, String> tracked) {
            this._names = new HashMap<>();
            this.remove = new HashMap<>();
            this.tracked = new HashMap<>(tracked);
        }




    }

}
