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

enum reltypes implements RelationshipType
{
    connected_to
}


public class createIndexForFile
{
    @Context
    // public Transaction txn;
    public GraphDatabaseService db;

    static long time = 0;

    static int N = 15;
    static String label = "Node";

    // @Context
    // public InternalLog log;
    
    @Procedure(name = "org.col362.neo4j.createIndexForFile", mode = Mode.WRITE)
    @Description("org.col362.neo4j.createIndexForFile(string pathToFile, string delimiter, string label1 (optional), string label2 (optional argument)), this creates the version graph from a file")

    public void createGraph(@Name("filePath") String filepath, @Name("delimiter") String delimiter)
    {
        Transaction txn = db.beginTx();
        initialize(txn);
        try
        {
            FileReader f = new FileReader(filepath);
            BufferedReader br = new BufferedReader(f);
            String records;
      
            Node nprevf = null;
            Node nprevt = null;

            sccUpdater updater = new sccUpdater();
            updater.initialize(txn);

            // In each line, we obtain the from and to node along with the time
            // For each time, we add/remove the relationship and update the scc
            while ((records = br.readLine()) != null)
            {
                String[] record = records.split(delimiter,0); 

                long nf = Long.parseLong(record[0]);
                long nt = Long.parseLong(record[1]);
                time = Long.parseLong(record[2].split("\n",0)[0]);

                Node n1 = txn.findNode(Label.label(label), "index_id", nf);
                Node n2 = txn.findNode(Label.label(label), "index_id", nt);

                // Check if edge exists between n1 and n2
                if (checkRelationship(n1, n2) == false)
                {
                    // Create relationship
                    Relationship rel = n1.createRelationshipTo(n2, reltypes.connected_to);
                    updater.updateSCC(n1, n2, true);
                }
                else
                {
                    // Delete relationship
                    // Relationship rel = n1.getSingleRelationship(reltypes.connected_to, Direction.OUTGOING);
                    for (Relationship rel : n1.getRelationships(Direction.OUTGOING))
                    {
                        Node child = rel.getEndNode();
                        if (child.equals(n2))
                        {
                            rel.delete();
                            updater.updateSCC(n1, n2, false);
                        }
                    }
                    // rel.delete();
                    // updater.updateSCC(n1, n2, false);
                }
            }
        }
        catch (Exception e) {
            // txn.close();
            e.printStackTrace();
        }
        txn.commit();
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
        for (int i = 1; i <= N; i++)
        {
            Node node = txn.createNode(Label.label(label));
            node.setProperty("index_id", i);
            node.setProperty("scc", i);
        }
    }
}