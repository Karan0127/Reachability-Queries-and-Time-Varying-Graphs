// package org.col362.neo4j;

// import org.neo4j.graphdb.*;
// import org.neo4j.logging.Log;
// import org.neo4j.procedure.Context;
// import org.neo4j.procedure.Description;
// import org.neo4j.procedure.Name;
// import org.neo4j.procedure.Procedure;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.stream.Stream;
// import java.util.*;

// public class limitedTarjan
// {
//     // @Context
//     public Transaction txn;
  
//     // @Context
//     // public Log log;

//     static int N = 15;
//     static int lastestSCCId = N + 1;

//     Deque<Node> stack = new ArrayDeque<Node>();
//     int disc[];
//     int lowlink[];
//     boolean onStack[];
//     int scc;

//     private int getSCC(Node u)
//     {
//         int x = (int) u.getProperty("scc");
//         return x;
//     }

//     private void updateSCC(Node u, int scc)
//     {
//         u.setProperty("scc", scc);
//     }

//     // TO EDIT!!!!!!!!!!!!!!
//     private int getNewSCCId()
//     {
//         return lastestSCCId++;
//     }

//     private void initialize(int s)
//     {
//         disc = new int[N];
//         lowlink = new int[N];
//         onStack = new boolean[N];

//         scc = s;
//         int i = 0;
//         ResourceIterator<Node> nodes = txn.findNodes(Label.label("Node"));
//         try {
            
//             while(nodes.hasNext())
//             {
//                 Node n1 = txn.createNode(Label.label("Debug"));
//                 n1.setProperty("index_id", 2000 + i);
//                 n1.setProperty("scc", 2000 + i);
//                 Node node = nodes.next();
//                 int id = (int) node.getProperty("index_id");
//                 if (id >= 15)
//                     continue;
//                 disc[id] = -1;
//                 lowlink[id] = -1;
//                 onStack[id] = false;
//                 i++;
//             }
//         }
//         finally {
//             nodes.close();
//         }
//     }

//     private void tarjan(Node node, int index, Deque<Node> stack)
//     {

//         if (getSCC(node) != scc)
//             return;

//         int id = (int) node.getProperty("index_id");
//         disc[id] = index;
//         lowlink[id] = index;
//         Node n4 = txn.createNode(Label.label("Debug"));
//         n4.setProperty("index_id", 4003);
//         n4.setProperty("scc", 4003);
//         onStack[id] = true;
//         Node n5 = txn.createNode(Label.label("Debug"));
//         n5.setProperty("index_id", 4004);
//         n5.setProperty("scc", 4004);
//         index++;
//         stack.push(node);
//         Node n6 = txn.createNode(Label.label("Debug"));
//         n6.setProperty("index_id", 4005);
//         n6.setProperty("scc", 4005);
//         int i = 0;

//         Node n = txn.createNode(Label.label("Debug"));
//         n.setProperty("index_id", 3000 + (int) node.getProperty("index_id"));
//         n.setProperty("scc", 3000 + getSCC(node));

//         ResourceIterator<Relationship> rels = node.getRelationships(Direction.OUTGOING).iterator();
//         Node n0 = txn.createNode(Label.label("Debug"));
//         n0.setProperty("index_id", 3050+i);
//         n0.setProperty("scc", 3050+i);
//         try {
//             while(rels.hasNext())
//             {
//                 // Node n1 = txn.createNode(Label.label("Debug"));
//                 // n1.setProperty("index_id", 3050+i);
//                 // n1.setProperty("scc", 3050+i);
//                 Relationship rel = rels.next();
//                 Node child = rel.getEndNode();
//                 int childid = (int) child.getProperty("index_id");
//                 if (disc[childid] == -1)
//                 {
//                     tarjan(child, index, stack);
//                     lowlink[id] = Math.min(lowlink[id], lowlink[childid]);
//                 }
//                 else if (onStack[childid])
//                 {
//                     lowlink[id] = Math.min(lowlink[id], disc[childid]);
//                 }
//                 i++;
//             }
//         }
//         finally {
//             rels.close();
//         }
//         Node n1 = txn.createNode(Label.label("Debug"));
//         n1.setProperty("index_id", 3100);
//         n1.setProperty("scc", 3100);

//         if (lowlink[id] == disc[id])
//         {
//             int new_scc_id = getNewSCCId();

//             while ( (int) stack.peek().getProperty("index_id") != id)
//             {
//                 Node v = stack.pop();
//                 onStack[(int)v.getProperty("index_id")] = false;
//                 updateSCC(v, new_scc_id);
//             }
//             Node v = stack.pop();
//             onStack[(int)v.getProperty("index_id")] = false;
//             updateSCC(v, new_scc_id);
//         }
//         Node n2 = txn.createNode(Label.label("Debug"));
//         n2.setProperty("index_id", 3101);
//         n2.setProperty("scc", 3101);
//     }

//     /*
//         Initialises tarjan's algorithm and applies it to the graph
//         The tarjan only works on the scc to be split, other nodes are ignored
//     */
//     public void applyTarjan(int s, Transaction t)
//     {
//         txn = t;
//         initialize(s);
//         int j = 0;
//         ResourceIterator<Node> nodes = txn.findNodes(Label.label("Node"));
//         try {
//             while(nodes.hasNext())
//             {   
//                 Node n = txn.createNode(Label.label("Debug"));
//                 n.setProperty("index_id", 1000 + j);
//                 n.setProperty("scc", 1000 + j);
//                 Node node = nodes.next();
//                 tarjan(node, 0, stack);
//                 j++;
//             }
//         }
//         finally {
//             nodes.close();
//         }
        
//         for (int i = 1; i <= 14; i++)
//         {
//             Node node = txn.findNode(Label.label("Node"), "index_id", i);
//             node.setProperty("disc", disc[i]);
//             node.setProperty("lowlink", lowlink[i]);
//         }
//     }
// }