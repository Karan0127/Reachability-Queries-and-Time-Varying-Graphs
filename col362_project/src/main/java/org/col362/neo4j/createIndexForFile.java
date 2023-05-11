package org.col362.neo4j;

import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import javax.lang.model.util.ElementScanner14;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.logging.InternalLog;
import org.neo4j.procedure.Procedure;

enum reltypes implements RelationshipType
{
    connected_to, scc_connected
}


public class createIndexForFile
{
    @Context
    // public Transaction txn;
    public GraphDatabaseService db;

    // static long time = 0;
    static int time = 0;
    static int T = Global.T;
    static int N = Global.N;
    // static int T;
    // static int N;
    static String label = "Node";
    static scchandler sh;

    // @Context
    // public InternalLog log;
    
    @Procedure(name = "org.col362.neo4j.createIndexForFile", mode = Mode.WRITE)
    @Description("org.col362.neo4j.createIndexForFile(string pathToFile, string delimiter, string label1 (optional), string label2 (optional argument)), this creates the version graph from a file")
    public void createGraph(@Name("numNodes") String num_nodes, @Name("numTimes") String times, @Name("filePath") String filepath, @Name("delimiter") String delimiter)
    {
        Transaction txn = db.beginTx();
        Global.N = Integer.parseInt(num_nodes);
        Global.T = Integer.parseInt(times);
        N = Global.N;
        T = Global.T;
        initialize(txn);
        sh = new scchandler(txn);
        txn.commit();
        try
        {
            txn = db.beginTx();
            FileReader f = new FileReader(filepath);
            BufferedReader br = new BufferedReader(f);
            String records;

            sccUpdater updater = new sccUpdater();
            updater.initialize(txn);
            postingsList pl = new postingsList(N,T,"/Users/bhavukb/Desktop/Courses/Sem8/DBMS/project/col362_project/indices/index");
            pl.createPostingsList();
            Global.offset1 = pl.offset1;
            // In each line, we obtain the from and to node along with the time
            // For each time, we add/remove the relationship and update the scc
            while ((records = br.readLine()) != null)
            {
                // txn = db.beginTx();
                updater.initialize(txn);
                String[] record = records.split(delimiter,0); 

                int nf = Integer.parseInt(record[0]);
                int nt = Integer.parseInt(record[1]);
                Global.time = time = Integer.parseInt(record[2].split("\n",0)[0]);

                Node n1 = txn.findNode(Label.label(label), "index_id", nf);
                Node n2 = txn.findNode(Label.label(label), "index_id", nt);

                SCCEdgeMaker sem = new SCCEdgeMaker(txn);

                // Node nttt = txn.createNode(Label.label("TimeCheck"));
                // nttt.setProperty("time", time);

                // Call the updater
                int changed = processUpdate(n1, n2, updater, sem, txn);

                // Update the postings list
                if (changed > 0)
                {
                    updatePostingList(txn, pl);
                }

                // Add or remove nodes for new SCCs

                if (changed == 1)
                    sem.handler(true);
                else if (changed == 2)
                {
                    Node n = txn.createNode(Label.label("DelDebug"));
                    n.setProperty("scc_id", 100);
                    sem.handler(false);
                }
            }
        }
        catch (Exception e) {
            // txn.close();
            e.printStackTrace();
        }
        txn.commit();
    }

    private int processUpdate(Node n1, Node n2, sccUpdater updater, SCCEdgeMaker sem, Transaction txn)
    {
        // Node n11 = txn.createNode(Label.label("TimeCheck"));
        // n11.setProperty("scc_id", 1000);
        int returnValue = 0;
        // Check if edge exists between n1 and n2
        if (checkRelationship(n1, n2) == false)
        {
            // Create relationship
            Relationship rel = n1.createRelationshipTo(n2, reltypes.connected_to);
            returnValue = updater.updateSCC(n1, n2, true, sem);
        }
        else
        {
            // Delete relationship
            for (Relationship rel : n1.getRelationships(Direction.OUTGOING))
            {
                Node child = rel.getEndNode();
                if (child.equals(n2))
                {
                    // Node n111 = txn.createNode(Label.label("DelDebug"));
                    // n111.setProperty("scc_id", 50);
                    rel.delete();
                    // n111 = txn.createNode(Label.label("DelDebug"));
                    // n111.setProperty("scc_id", 100);
                    returnValue = updater.updateSCC(n1, n2, false, sem);
                    // n111 = txn.createNode(Label.label("DelDebug"));
                    // n111.setProperty("scc_id", 200);
                    if (returnValue == 1)
                        returnValue = 2;
                }
            }
        }
        return returnValue;
    }

    private void updatePostingList(Transaction txn, postingsList pl)
    {
        ResourceIterator<Node> nodes = txn.findNodes(Label.label("Node"));
        while(nodes.hasNext()) {
            Node node = nodes.next();
            int scc = (int) node.getProperty("scc");
            int index_id = (int) node.getProperty("index_id");
            ArrayList<pair> al = pl.readPostingsList(index_id-1);
            pair p = new pair(time, scc);
            pair p1 = new pair(time, Integer.MIN_VALUE);
            //binary search the location of pair p in al 
            int index = Collections.binarySearch(al, p1, new pairComparator());
            if(index < 0) {
                index = -index - 1;
            }
            // if(index == 0)
            //     al.set(index, p);
            // else if(al.get(index-1).first == time)
            //     al.set(index-1, p);
            // else
            //     al.set(index, p);
            al.set(index, p);
            pl.writeAtIndex(index_id-1, al);
        }
    }

    private void createSCCNode(Transaction txn, Node n1, Node n2)
    {
        // If the SCC_id does not have a corresponding scc node, create one
        Node ns1 = txn.findNode(Label.label("SCC"), "scc_id", (int) n1.getProperty("scc"));
        Node ns2 = txn.findNode(Label.label("SCC"), "scc_id", (int) n2.getProperty("scc"));

        if (ns1 == null)
        {
            ns1 = txn.createNode(Label.label("SCC"));
            ns1.setProperty("scc_id", (int) n1.getProperty("scc"));
        }

        if (ns2 == null)
        {
            ns2 = txn.createNode(Label.label("SCC"));
            ns2.setProperty("scc_id", (int) n2.getProperty("scc"));
        }
    }

    private boolean checkRelationship(Node n1, Node n2)
    {
        // Check if edge exists between n1 and n2
        if (n1.hasRelationship(Direction.OUTGOING))
        {
            Iterable<Relationship> rels = n1.getRelationships(Direction.OUTGOING);
            for (Relationship rel : rels)
            {
                if (rel.getEndNode().equals(n2))
                {
                    // close the iterable
                    return true;
                }
            }
        }
        return false;
    }

    public void printSCC(Transaction txn)
    {
        for (Node node : txn.getAllNodes())
        {
            System.out.println(node.getProperty("index_id") + " " + node.getProperty("scc"));
        }
        System.out.println();
    }

    private void initialize(Transaction txn)
    {
        Global.availableSCCs.add(N+1);
        for (int i = 1; i <= N; i++)
        {
            Node node = txn.createNode(Label.label(label));
            node.setProperty("index_id", i);
            node.setProperty("scc", i);
            // Node sccNode = txn.createNode(Label.label("SCCNode"));
            // sccNode.setProperty("scc_id", i);
        }
        // Node sccNode = txn.createNode(Label.label("SCCNode"));
        // sccNode.setProperty("scc_id", N+1);
    }

    private void initializer1(Transaction txn, String filepath, String Delimiter) {
        try {
            Global.T = 0;
            Global.N = 0;
            int counter = 0;
            long pt = -1;
            FileReader f = new FileReader(filepath);
            BufferedReader br = new BufferedReader(f);
            String records;
            while ((records = br.readLine()) != null) {
                String[] record = records.split(Delimiter, 0);
                long nf = Long.parseLong(record[0]);
                long nt = Long.parseLong(record[1]);
                time = Integer.parseInt(record[2].split("\n", 0)[0]);
                Node n1 = txn.findNode(Label.label(label), "real_id", nf);
                Node n2 = txn.findNode(Label.label(label), "real_id", nt);
                if (n1 == null) {
                    n1 = txn.createNode(Label.label(label));
                    n1.setProperty("real_id", nf);
                    n1.setProperty("index_id", ++counter);
                    n1.setProperty("scc", counter);
                }
                if (n2 == null) {
                    n2 = txn.createNode(Label.label(label));
                    n2.setProperty("real_id", nt);
                    n2.setProperty("index_id", ++counter);
                    n2.setProperty("scc", counter);
                }
                if (pt != time) {
                    pt = time;
                    Global.T++;
                }
                // Relationship rel = n1.createRelationshipTo(n2, reltypes.connected_to);
            }
            Global.N = counter+1;
            N = Global.N;
            T = Global.T;
            Node n = txn.createNode(Label.label("Debug"));
            n.setProperty("num_nodes", N);
            n.setProperty("num_times", T);
            Global.availableSCCs.add(N+1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}