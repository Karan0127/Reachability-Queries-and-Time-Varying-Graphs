package org.col362.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import java.util.*;

public class limitedTarjan
{
    // @Context
    public Transaction txn;
  
    // @Context
    // public Log log;

    static int N = Global.N;
    // static int lastestSCCId = N + 1;

    Deque<Node> stack = new ArrayDeque<Node>();
    int disc[];
    int lowlink[];
    boolean onStack[];
    int scc;
    scchandler sh;
    SCCEdgeMaker sem;

    private int getSCC(Node u)
    {
        int x = (int) u.getProperty("scc");
        return x;
    }

    private void updateSCC(Node u, int scc)
    {
        u.setProperty("scc", scc);
    }

    private void initialize(int s, SCCEdgeMaker s1)
    {
        disc = new int[N+1];
        lowlink = new int[N+1];
        onStack = new boolean[N+1];
        
        scc = s;
        sem = s1;
        ResourceIterator<Node> nodes = txn.findNodes(Label.label("Node"));
        try {
            while(nodes.hasNext())
            {   
                Node node = nodes.next();
                // Node n1 = txn.createNode(Label.label("Debug"));
                // n1.setProperty("index_id", 2000 + (int) node.getProperty("index_id"));
                // n1.setProperty("scc", 2000 + (int) node.getProperty("scc"));
                if (getSCC(node) != scc)
                    continue;
                int id = (int) node.getProperty("index_id");
                disc[id] = -1;
                lowlink[id] = -1;
                onStack[id] = false;
            }
        }
        finally {
            nodes.close();
        }
    }

    private void tarjan(Node node, int index, Deque<Node> stack)
    {

        if (getSCC(node) != scc)
            return;

        int id = (int) node.getProperty("index_id");
        disc[id] = index;
        lowlink[id] = index;
        onStack[id] = true;
        index++;
        stack.push(node);
        // Node n6 = txn.createNode(Label.label("StackDebug"));
        // n6.setProperty("index_id", 1000 + (int) node.getProperty("index_id"));
        // n6.setProperty("scc", 1000 + scc);
        int i = 0;

        ResourceIterator<Relationship> rels = node.getRelationships(Direction.OUTGOING).iterator();
        try {
            while(rels.hasNext())
            {
                Relationship rel = rels.next();
                Node child = rel.getEndNode();
                int childid = (int) child.getProperty("index_id");
                if (disc[childid] == -1)
                {
                    tarjan(child, index, stack);
                    lowlink[id] = Math.min(lowlink[id], lowlink[childid]);
                }
                else if (onStack[childid])
                {
                    lowlink[id] = Math.min(lowlink[id], disc[childid]);
                }
                i++;
            }
        }
        finally {
            rels.close();
        }

        if (lowlink[id] == disc[id])
        {
            int new_scc_id = Global.getNewSCCId();
            sh.createSCC(new_scc_id, Global.time);
            sem.addSplitId(new_scc_id);

            while ( (int) stack.peek().getProperty("index_id") != id)
            {
                Node v = stack.pop();
                onStack[(int)v.getProperty("index_id")] = false;
                updateSCC(v, new_scc_id);
                // n6 = txn.createNode(Label.label("StackDebug"));
                // n6.setProperty("index_id", 2000 + (int) node.getProperty("index_id"));
                // n6.setProperty("scc", 2000 + scc);
            }
            Node v = stack.pop();
            onStack[(int)v.getProperty("index_id")] = false;
            updateSCC(v, new_scc_id);
        }
    }

    /*
        Initialises tarjan's algorithm and applies it to the graph
        The tarjan only works on the scc to be split, other nodes are ignored
    */
    public void applyTarjan(int s, Transaction t, SCCEdgeMaker s1)
    {
        txn = t;
        // Node n111 = txn.createNode(Label.label("DelDebug"));
        // n111.setProperty("scc_id", 2000);
        sh = new scchandler(txn);
        initialize(s, s1);
        int j = 0;
        ResourceIterator<Node> nodes = txn.findNodes(Label.label("Node"));
        try {
            while(nodes.hasNext())
            {   
                Node node = nodes.next();
                // Node n = txn.createNode(Label.label("Debug"));
                // n.setProperty("index_id", 1000 + (int) node.getProperty("index_id"));
                // n.setProperty("scc", 1000 + scc);
                tarjan(node, 0, stack);
                j++;
            }
        }
        finally {
            nodes.close();
        }
        // n111 = txn.createNode(Label.label("DelDebug"));
        // n111.setProperty("scc_id", 3000);
        Global.addSCCToFreeList(s);
        sh.deleteSCC(s, Global.time);
        // n111 = txn.createNode(Label.label("DelDebug"));
        // n111.setProperty("scc_id", 4000);
        // for (int i = 1; i <= Global.N; i++)
        // {
        //     Node node = txn.findNode(Label.label("Node"), "index_id", i);
        //     node.setProperty("disc", disc[i]);
        //     node.setProperty("lowlink", lowlink[i]);
        // }
        // n111 = txn.createNode(Label.label("DelDebug"));
        // n111.setProperty("scc_id", 5000);
    }
}