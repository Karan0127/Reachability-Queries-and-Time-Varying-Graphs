package org.col362.neo4j;

import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.logging.Log;
import org.neo4j.logging.InternalLog;
import org.neo4j.procedure.Procedure;


public class baselineQuery {
    static int visited[];
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "org.col362.neo4j.baselineQuery", mode = Mode.WRITE)
    @Description("org.col362.neo4j.baselineQuery(int node_from_id, int node_to_id, int time), this computes reachability from u to v at time t")

    public Stream<booleanresult> reachabilityQuery(@Name("node_from_id") String nf_id, @Name("node_to_id") String nt_id, @Name("time") String time) {
        booleanresult br;
        try {
            int nf = Integer.parseInt(nf_id);
            int nt = Integer.parseInt(nt_id);
            int t = Integer.parseInt(time);
            initialize();
            Transaction tx = db.beginTx();
            Node n1 = tx.findNode(Label.label("Node"), "node_id", nf);
            Node n2 = tx.findNode(Label.label("Node"), "node_id", nt);
            if (n1 == null || n2 == null) {
                br = new booleanresult(false);
                return Stream.of(br);
            }
            // ResourceIterator<Relationship> ri = n1.getRelationships(Direction.OUTGOING).iterator();
            br = new booleanresult(dfs(n1, n2, t));
            return Stream.of(br);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        br = new booleanresult(false);
        return Stream.of(br);
    }

    private boolean dfs(Node u, Node v, int time) {
        if (u.equals(v))
            return true;
        visited[(int)u.getProperty("node_id")-1] = 1;
        ResourceIterator<Relationship> ri = u.getRelationships(Direction.OUTGOING).iterator();
        while(ri.hasNext()) {
            Relationship r = ri.next();
            Node n = r.getEndNode();
            if (edgeExists(r, time) && visited[(int)n.getProperty("node_id")-1] == 0) {
                if (dfs(n, v, time))
                    return true;
            }
        }
        return false;
    }

    private boolean edgeExists(Relationship r, int time) {
        int[] timestamps = (int[])r.getProperty("timestamps");
        int ind = Arrays.binarySearch(timestamps, time);
        if (ind >= 0){
            if(ind%2 == 0)
                return true;
            else
                return false;
        }
        else{
            ind = -ind - 1;
            if(ind%2 == 0)
                return false;
            else
                return true;
        }
        // return false;
    }

    // private boolean dfs(Node u, Node v, int time) {
    //     if (u.equals(v))
    //         return true;
    //     visited[(int)u.getProperty("node_id")-1] = 1;
    //     ResourceIterator<Relationship> ri = u.getRelationships(Direction.OUTGOING).iterator();
    //     while(ri.hasNext()) {
    //         Relationship r = ri.next();
    //         Node n = r.getEndNode();
    //         if (edgeExists(r, time) && visited[(int)n.getProperty("node_id")-1] == 0) {
    //             if (dfs(n, v, time))
    //                 return true;
    //         }
    //     }
    //     return false;
    // }

    // private boolean entityExists(Entity r, int time) {
    //     int[] timestamps = (int[])r.getProperty("timestamps");
    //     int ind = Arrays.binarySearch(timestamps, time);
    //     if (ind >= 0){
    //         if(ind%2 == 0)
    //             return true;
    //         else
    //             return false;
    //     }
    //     else{
    //         ind = -ind - 1;
    //         if(ind%2 == 0)
    //             return false;
    //         else
    //             return true;
    //     }
    //     // return false;
    // }

    private void initialize(){
        visited = new int[Global.N];
        for (int j = 0; j < Global.N; j++) {
            for (int i = 0; i < Global.N; i++)
                visited[i] = 0;
        }
    }
}