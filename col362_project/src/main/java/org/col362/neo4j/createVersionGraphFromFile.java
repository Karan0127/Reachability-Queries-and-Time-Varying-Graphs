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
import org.neo4j.logging.Log;
import org.neo4j.logging.InternalLog;
import org.neo4j.procedure.Procedure;

/* input = path to file, delimiter. Make sure file has only 3 columns with int values
 *  the 3 columns should be from_node, to_node and time in unix format
*/

/*enum reltypes implements RelationshipType
{
    connected_to
}*/

public class createVersionGraphFromFile {
    @Context
    // public Transaction tx;
    // public InternalLog log;
    public GraphDatabaseService db;

    @Procedure(name = "org.col362.neo4j.createVersionGraphFromFile", mode = Mode.WRITE)
    @Description("org.col362.neo4j.createVersionGraphFromFile(string pathToFile, string delimiter), this creates the version graph from a file")

    public void createGraph(@Name("filePath") String filepath, @Name("delimiter") String delimiter) {
        try {
            //GraphDatabaseService graphDB = new GraphDatabase
            // 
            Transaction tx = db.beginTx();
            FileReader f = new FileReader(filepath);
            // CSVParser parser = new CSVParserBuilder().withSeparator(delimiter).build();
            // CSVReader csvReader = new CSVReaderBuilder(f).withCSVParser(parser).build();
            BufferedReader br = new BufferedReader(f);

            String records;
      
            ArrayList<Integer> times = new ArrayList<Integer>();
            Node nprevf = null;
            Node nprevt = null;
            while ((records = br.readLine()) != null) {
                String[] record = records.split(delimiter,0); 
                int nf = Integer.parseInt(record[0]);
                int nt = Integer.parseInt(record[1]);
                int time = Integer.parseInt(record[2].split("\n",0)[0]);
                
                Node n1 = tx.findNode(Label.label("Node"), "node_id", nf);
                Node n2 = tx.findNode(Label.label("Node"), "node_id", nt);

                // if (n1 == null || n2 == null || nprevf == null || nprevt == null || !comparator(n1, nprevf) || !comparator(n2, nprevt)) {
                if (n1 == null || n2 == null || nprevf == null || nprevt == null || !n1.equals(nprevf) || !n2.equals(nprevt)) {    
                    if(nprevf != null){
                        ResourceIterator<Relationship> ri = nprevf.getRelationships(Direction.OUTGOING).iterator();
                        while(ri.hasNext()) {
                            Relationship r = ri.next();
                            Node n = r.getEndNode();
                            // if(comparator(n, nprevt)) {
                            if(n.equals(nprevt)) {
                                // times.add(Integer.MAX_VALUE);
                                int[] finaltimes = new int[times.size()];
                                int i = 0;
                                for(int ele : times){
                                    finaltimes[i++] = ele;
                                }
                                r.setProperty("timestamps", finaltimes);
                                break;
                            }
                        }
                        ri.close();
                    }
                    nprevf = n1;
                    nprevt = n2;
                    if(n1 == null){
                        nprevf = tx.createNode(Label.label("Node"));
                        nprevf.setProperty("node_id", nf);
                    }
                    if(n2 == null){
                        nprevt = tx.createNode(Label.label("Node"));
                        nprevt.setProperty("node_id", nt);
                    }
                    nprevf.createRelationshipTo(nprevt, reltypes.connected_to);
                    times = new ArrayList<Integer>();
                    times.add(time);
                }
                else {
                    times.add(time);
                }
            }
            ResourceIterator<Relationship> ri = nprevf.getRelationships(Direction.OUTGOING).iterator();
            while(ri.hasNext()) {
                Relationship r = ri.next();
                Node n = r.getEndNode();
                // if(comparator(n, nprevt)) {
                if(n.equals(nprevt)) {
                    // times.add(Integer.MAX_VALUE);
                    int[] finaltimes = new int[times.size()];
                    int i = 0;
                    for(int ele : times){
                        finaltimes[i++] = ele;
                    }
                    r.setProperty("timestamps", finaltimes);
                    break;
                }
            }
            ri.close();
            tx.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    boolean comparator(Node n1, Node n2) {
        Iterator<Label> n1l = n1.getLabels().iterator();
        Iterator<Label> n2l = n2.getLabels().iterator();
        if(n1l.hasNext() && n2l.hasNext()){
            if(n1l.next().name().equals(n2l.next().name())){
                if(n1.getProperty("id").equals(n2.getProperty("id"))){
                    return true;
                }
            }
        }
        return false;
    }
}
