package org.col362.neo4j;

import java.util.*;


import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.logging.InternalLog;
import org.neo4j.procedure.Procedure;

/* input = from_node_id, to_node_id, time (in unix format)
*/

// enum reltypes implements RelationshipType
// {
//     connected_to
// }

public class createVersionGraph {
    @Context
    // public Transaction tx;
    public GraphDatabaseService db;

    @Procedure(name = "org.col362.neo4j.createVersionGraph", mode = Mode.WRITE)
    @Description("org.col362.neo4j.createVersionGraph(long node_from_id, long node_to_id, long time), this creates the version graph with this relationship")
    public void createGraph(@Name("node_from_id") long nf_id, @Name("node_to_id") long nt_id, @Name("time") long time) {
        try {
            Transaction tx = db.beginTx();
	    Node n1 = tx.findNode(Label.label("Node"), "id", nf_id);
            Node n2 = tx.findNode(Label.label("Node"), "id", nt_id);
            if(n1 == null || n2 == null){
                if(n1 == null){
                    n1 = tx.createNode(Label.label("Node"));
                    n1.setProperty("id", nf_id);    
                }
                if(n2 == null){
                    n2 = tx.createNode(Label.label("Node"));
                    n2.setProperty("id", nt_id);    
                }
                Relationship r = n1.createRelationshipTo(n2, reltypes.connected_to);
                long[] times = new long[2];
                times[0] = time;
                times[1] = Long.MAX_VALUE;
                r.setProperty("timestamps", times);
            }
            else{
                ResourceIterator<Relationship> ri = n1.getRelationships(Direction.OUTGOING).iterator();
                int broken = 0;
                while(ri.hasNext()) {
                    Relationship r = ri.next();
                    Node n = r.getEndNode();
                    
                    // if(comparator(n, n2)) {
                    if(n.equals(n2)) {
                        long[] times = (long[])r.getProperty("timestamps");
                        r.removeProperty("timestamps");
                        ArrayList<Long> timestamps = new ArrayList<Long>();
                        int ind = Arrays.binarySearch(times,time);
                        // r.setProperty("ind", ind);
                        if(ind >= 0) {
                            if(times.length > 2){
                                for(int i = 0; i < times.length; i++){
                                    if(i != ind){
                                        timestamps.add(times[i]);
                                    }
                                    // else {
                                    //     timestamps.add(time);
                                    // }
                                }
                                long[] finaltimes = new long[timestamps.size()];
                                int i = 0;
                                for(long ele : timestamps){
                                    finaltimes[i++] = ele;
                                }
                                r.setProperty("timestamps", finaltimes);
                            }
                            else{
                                r.delete();
                            }
                        }
                        else{
                            ind = -1*ind - 1;
                            for(int i = 0; i < times.length; i++){
                                if(i == ind){
                                    timestamps.add(time);
                                }
                                timestamps.add(times[i]);
                            }
                            long[] finaltimes = new long[timestamps.size()];
                            int i = 0;
                            for(long ele : timestamps){
                                finaltimes[i++] = ele;
                            }
                            r.setProperty("timestamps", finaltimes);
                        }
                        broken = 1;
                        break;
                    } 
                }
                ri.close();       
                if(broken == 0) {
                    Relationship r = n1.createRelationshipTo(n2, reltypes.connected_to);
                    long[] times = new long[2];
                    times[0] = time;
                    times[1] = Long.MAX_VALUE;
                    r.setProperty("timestamps", times);
                }
            }
            tx.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean comparator(Node n1, Node n2) {
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
