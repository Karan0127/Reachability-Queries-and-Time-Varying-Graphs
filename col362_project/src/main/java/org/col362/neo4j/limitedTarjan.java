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

    static int N = 15;
    static int lastestSCCId = N + 1;

    Deque<Node> stack = new ArrayDeque<Node>();
    int disc[];
    int lowlink[];
    boolean onStack[];
    int scc;

    private int getSCC(Node u)
    {
        int x = (int) u.getProperty("scc");
        return x;
    }

    private void updateSCC(Node u, int scc)
    {
        u.setProperty("scc", scc);
    }

    // TO EDIT!!!!!!!!!!!!!!
    private int getNewSCCId()
    {
        return lastestSCCId++;
    }

    private void initialize(int s)
    {
        disc = new int[N];
        lowlink = new int[N];
        
        scc = s;
        for (Node node : txn.getAllNodes())
        {
            if (getSCC(node) == s)
            {
                int id = (int) node.getProperty("index_id");
                disc[id] = -1;
                lowlink[id] = -1;
                onStack[id] = false;
            }
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

        for (Relationship rel : node.getRelationships(Direction.OUTGOING))
        {
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
        }

        if (lowlink[id] == disc[id])
        {
            int new_scc_id = getNewSCCId();

            while ( (int) stack.peek().getProperty("index_id") != id)
            {
                Node v = stack.pop();
                onStack[(int)v.getProperty("index_id")] = false;
                updateSCC(v, new_scc_id);
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
    public void applyTarjan(int s, Transaction t)
    {
        txn = t;
        initialize(s);
        for (Node node : txn.getAllNodes())
        {
            tarjan(node, 0, stack);
        }
    }
}