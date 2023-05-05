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

public class sccUpdater
{
    // @Context
    public Transaction txn;

    // TO EDIT!!!!!!!!!!!!!!
    int N = 15;

    int visited[];
    Set<Integer> set;

    public void initialize(Transaction t)
    {
        visited = new int[N];
        set = new HashSet<Integer>();
        txn = t;
    }

    private void resetVisited()
    {
        for (int i = 0; i < N; i++)
            visited[i] = -1;
    }

    private void resetSet()
    {
        set.clear();
    }

    private int getSCC(Node u)
    {
        int x = (int) u.getProperty("scc");
        return x;
    }

    private void updateSCC(Node u, int scc)
    {
        u.setProperty("scc", scc);
    }

    private boolean sameSCC(Node u, Node v)
    {
        return getSCC(u) == getSCC(v);
    }

    private boolean pathExists(Node u, Node v)
    {
        resetVisited();
        return pathDFS(u, v);
    }

    private boolean pathDFS(Node u, Node v)
    {
        if (sameSCC(u, v))
            return true;
        int id = (int) u.getProperty("index_id");
        visited[id] = 1;
        boolean ans = false;
        for (Relationship rel : u.getRelationships(Direction.OUTGOING))
        {
            Node child = rel.getEndNode();
            int new_id = (int) child.getProperty("index_id");
            if (visited[new_id] == -1)
                ans |= pathDFS(child, v);
        }
        return ans;
    }

    private void mergeSCC(Node u, Node v)
    {
        resetVisited();
        resetSet();
        mergeDFS(u, v);
        updateNodeSCCs(getSCC(u));
    }

    /*
        We go through all the paths from u to an SCC element of v, avoiding repititions
        For all nodes in the path, the SCC becomes common
        So, we add SCCs of the nodes in the path
        To do this, we check if there is a path from this node to v, and if there is, we add it to the set
        Path check is represented by visited[id] = 1
    */
    private void mergeDFS(Node u, Node v)
    {
        if (sameSCC(u, v))
        {
            visited[(int)u.getProperty("index_id")] = 1;
            set.add(getSCC(u));
            return;
        }

        int id = (int) u.getProperty("index_id");
        visited[id] = 0;
        for (Relationship rel : u.getRelationships(Direction.OUTGOING))
        {
            Node child = rel.getEndNode();
            int new_id = (int) child.getProperty("index_id");
            if (visited[new_id] == -1)
                mergeDFS(child, v);
            if (visited[new_id] == 1)
                visited[id] = 1;
        }
        if (visited[id] == 1)
            set.add(getSCC(u));
    }

    private void updateNodeSCCs(int scc)
    {
        for (Node node : txn.getAllNodes())
        {
            if (set.contains(getSCC(node)))
                updateSCC(node, scc);
        }
        // For all numbers in set
        // for (int x : set)
        // {
        //     Node node = txn.createNode(Label.label("Node"));
        //     node.setProperty("index_id", 100 + x);
        //     node.setProperty("scc", 100 + x);
        // }
        // for (int i = 1; i <= 3; i++)
        // {
        //     Node node = txn.createNode(Label.label("Node"));
        //     node.setProperty("index_id", 200 + i);
        //     node.setProperty("scc", 200 + i);
        //     node.setProperty("visited", visited[i]);
        // }
    }

    private void splitSCC(Node u, Node v)
    {
        limitedTarjan lt = new limitedTarjan();
        int scc = getSCC(u);
        lt.applyTarjan(scc, txn);
    }

/*
    1. In Addition, we check if nodes belong to same SCC. In this case, nothing changes
    2. In case SCC isn't same and there isn't a reverse path, nothing changes again
    3. In case SCC isn't same and there is a reverse path, we merge the SCCs
    4. In Deletion, we check if nodes belong different SCC. In this case, nothing changes
    5. In case SCC is same and there is another path, nothing changes again
    6. In case SCC is same and there isn't another path, we split the SCCs
 */
    public int updateSCC(Node u, Node v, boolean addition)
    {
        if (addition)
        {
            if (sameSCC(u, v))
                return 0;
            else if (!pathExists(v, u))
                return 0;
            else
                mergeSCC(v, u);
        }
        else
        {
            if (!sameSCC(u, v))
                return 0;
            else if (pathExists(u, v))
                return 0;
            else
                splitSCC(u, v);
        }
        return 1;
    }
}