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

public class scchandler {
    
    Transaction txn;

    public scchandler(Transaction t) {
        txn = t;
    }

    public boolean sccexists(int scc_id) {
        Node scc = txn.findNode(Label.label("SCC"), "scc_id", scc_id);
        if(scc != null) {
            return true;
        }
        return false;
    }

    public Relationship findrel(Node nscc1, Node nscc2) {
        ResourceIterator<Relationship> rels = nscc1.getRelationships(Direction.OUTGOING).iterator();
        try {
            while(rels.hasNext()) {
                Relationship rel = rels.next();
                Node child = rel.getEndNode();
                if(child.equals(nscc2)) {
                    return rel;
                }
            }
        }
        finally {
            rels.close();
        }
        return null;
    }

    public Node createSCC(int scc_id, int time) {
        Node scc = txn.findNode(Label.label("SCC"), "scc_id", scc_id);
        if(scc == null) {
            scc = txn.createNode(Label.label("SCC"));
            scc.setProperty("scc_id", scc_id);
            int[] timestamps = new int[]{time};
            scc.setProperty("timestamps", timestamps);
            return scc;
        }
        int[] timestamps = (int[]) scc.getProperty("timestamps");
        int[] finaltimestamps = new int[timestamps.length + 1];
        int i = 0;
        for(int ele : timestamps){
            finaltimestamps[i++] = ele;
        }
        finaltimestamps[i] = time;
        scc.setProperty("timestamps", finaltimestamps);
        return scc;
    }

    public Relationship createRelationship(int scc1, int scc2, int time) {
        Node nscc1;
        Node nscc2;
        nscc1 = txn.findNode(Label.label("SCC"), "scc_id", scc1);
        nscc2 = txn.findNode(Label.label("SCC"), "scc_id", scc2);
        if(nscc1 == null)
            nscc1 = createSCC(scc1, time);
        if(nscc2 == null)
            nscc2 = createSCC(scc2, time);
        Relationship rel = findrel(nscc1, nscc2);
        if(rel == null){
            rel = nscc1.createRelationshipTo(nscc2, reltypes.scc_connected);
            int[] timestamps = new int[]{time};
            rel.setProperty("timestamps", timestamps);
            rel.setProperty("weight", 1);
            return rel;
        }
        int weight = (int) rel.getProperty("weight");
        if(weight == 0){
            int[] timestamps = (int[]) rel.getProperty("timestamps");
            int[] finaltimestamps = new int[timestamps.length + 1];
            int i = 0;
            for(int ele : timestamps){
                finaltimestamps[i++] = ele;
            }
            finaltimestamps[i] = time;
            rel.setProperty("timestamps", finaltimestamps);
        }
        weight++;
        rel.setProperty("weight", weight);
        // int[] timestamps = (int[]) rel.getProperty("timestamps");
        // int[] finaltimestamps = new int[timestamps.length + 1];
        // int i = 0;
        // for(int ele : timestamps){
        //     finaltimestamps[i++] = ele;
        // }
        // finaltimestamps[i] = time;
        // rel.setProperty("timestamps", finaltimestamps);
        return rel;
    }

    public void deleteRelationship(int scc1, int scc2, int time) {
        Node nscc1;
        Node nscc2;
        nscc1 = txn.findNode(Label.label("SCC"), "scc_id", scc1);
        nscc2 = txn.findNode(Label.label("SCC"), "scc_id", scc2);
        if(nscc1 == null || nscc2 == null)
            return;
        Relationship rel = findrel(nscc1, nscc2);
        if(rel == null)
            return;
        int weight = (int) rel.getProperty("weight");
        weight--;
        if(weight == 0) {
            int[] timestamps = (int[]) rel.getProperty("timestamps");
            int[] finaltimestamps = new int[timestamps.length + 1];
            int i = 0;
            for(int ele : timestamps){
                finaltimestamps[i++] = ele;
            }
            finaltimestamps[i] = time;
            rel.setProperty("timestamps", finaltimestamps);
            rel.setProperty("weight", weight);
        }
        else {
            rel.setProperty("weight", weight);
        }
    }

    public Node deleteSCC(int scc_id, int time) {
        Node scc = txn.findNode(Label.label("SCC"), "scc_id", scc_id);
        if(scc == null) {
            return null;
        }
        int[] timestamps = (int[]) scc.getProperty("timestamps");
        int len = timestamps.length;
        int finalele = timestamps[len-1];
        int[] finaltimestamps;
        // Node n2 = txn.createNode(Label.label("SCCDebug"));
        // n2.setProperty("index_id", 210);
        if(finalele == time && len%2 == 1) {
            finaltimestamps = new int[len-1];
            for(int i = 0; i < len-1; i++){
                finaltimestamps[i] = timestamps[i];
            }

        }
        else if(finalele < time && len%2 == 1) {
            finaltimestamps = new int[len + 1];
            int i = 0;
            for(int ele : timestamps){
                finaltimestamps[i++] = ele;
            }
            finaltimestamps[i] = time;
        }
        else {
            return null;
        }
        // Node n = txn.createNode(Label.label("SCCDebug"));
        // n.setProperty("index_id", 220);
        scc.setProperty("timestamps", finaltimestamps);
        return scc;
    }
    // public Relationship deleteRelationship(int scc1, int scc2, int time) {
    //     Node nscc1;
    //     Node nscc2;
    //     nscc1 = txn.findNode(Label.label("SCC"), "scc_id", scc1);
    //     nscc2 = txn.findNode(Label.label("SCC"), "scc_id", scc2);
    //     if(nscc1 == null || nscc2 == null)
    //         return null;
    //     ResourceIterator<Relationship> rels = nscc1.getRelationships(Direction.OUTGOING).iterator();
    //     try {
    //         while(rels.hasNext()) {
    //             Relationship rel = rels.next();
    //             Node child = rel.getEndNode();
    //             if(child.equals(nscc2)) {
                    
    //                 break;
    //             }
    //         }
    //     }
    //     finally {
    //         rels.close();
    //     }
    // }
} 
