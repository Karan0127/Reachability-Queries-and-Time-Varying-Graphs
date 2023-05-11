package org.col362.neo4j;

import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.logging.InternalLog;
import org.neo4j.procedure.Procedure;

public class SCCEdgeMaker
{
    private boolean merge;
    private int merged_id;
    private Set<Integer> split_set = new HashSet<Integer>();
    public Transaction txn;
    public scchandler sh;

    public SCCEdgeMaker(Transaction t)
    {
        txn = t;
    }

    public void reset()
    {
        split_set.clear();
    }
    
    public void addSplitId(int id)
    {
        split_set.add(id);
    }

    public void setMergeId(int id)
    {
        merged_id = id;
    }

    private int getSCC(Node u)
    {
        int x = (int) u.getProperty("scc");
        return x;
    }

    public void handler(boolean m)
    {
        // Node node = txn.createNode(Label.label("SCCDebug"));
        // node.setProperty("index_id", 301);
        // node.setProperty("scc", 301);
        sh = new scchandler(txn);
        merge = m;
        if (merge)
            mergeHandler();
        else
            splitHandler();
    }

    private void mergeHandler()
    {
        // Node node = txn.createNode(Label.label("SCCDebug"));
        // node.setProperty("index_id", 302);
        // node.setProperty("scc", 302);
        ResourceIterator<Relationship> rels = txn.findRelationships(reltypes.connected_to);
        try
        {
            while(rels.hasNext())
            {
                Relationship rel = rels.next();
                Node u = rel.getStartNode();
                Node v = rel.getEndNode();
                int scc_u = getSCC(u);
                int scc_v = getSCC(v);
                if (scc_u != merged_id && scc_v != merged_id)
                    continue;
                if (scc_u == merged_id && scc_v == merged_id)
                    continue;
                sh.createRelationship(scc_u, scc_v, Global.time);
            }
        }
        finally
        {
            rels.close();
        }
    }

    private void splitHandler()
    {
        ResourceIterator<Relationship> rels = txn.findRelationships(reltypes.connected_to);
        try
        {
            // Node n = txn.createNode(Label.label("DelDebug"));
            // n.setProperty("scc_id", 200);
            while(rels.hasNext())
            {
                Relationship rel = rels.next();
                Node u = rel.getStartNode();
                Node v = rel.getEndNode();
                int scc_u = getSCC(u);
                int scc_v = getSCC(v);
                if (scc_u == scc_v)
                    continue;
                if (!split_set.contains(scc_u) && !split_set.contains(scc_v))
                    continue;
                sh.createRelationship(scc_u, scc_v, Global.time);
            }
        }
        finally
        {
            rels.close();
        }
    }
}