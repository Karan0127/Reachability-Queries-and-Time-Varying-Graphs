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

// public class indexQuery
// {
//     @Context
//     public Transaction txn;

//     int visited[];

//     public void initialize()
//     {
//         visited = new int[N];
//     }

//     private void resetVisited()
//     {
//         for (int i = 0; i < N; i++)
//             visited[i] = -1;
//     }

//     private bool sccPathExists(Node u, Node v, int t)
//     {
//         if (getScc(node1, t) == getScc(node2, t))
//             return true;

//         if (visited[u] != -1)
//             return false;

//         visited[u] = 1;

//         for (Relationship rel : u.getRelationships(Direction.OUTGOING))
//         {
//             Node node = rel.getEndNode();
//             if (sccPathExists(node, v, t))
//                 return true;
//         }

//         return false;
//     }

//     public bool query(int u, int v, int t)
//     {
//         Node node1 = txn.findNode(Label.label("Node"), "index_id", u);
//         Node node2 = txn.findNode(Label.label("Node"), "index_id", v);

//         if (node1 == null || node2 == null)
//         {
//             System.out.println("No such nodes exist");
//             return;
//         }

//         int scc1 = getScc(node1, t);
//         int scc2 = getScc(node2, t);

//         if (scc1 == scc2)
//             return true;

//         return sccPathExists(node1, node2, t);
//     }
// }