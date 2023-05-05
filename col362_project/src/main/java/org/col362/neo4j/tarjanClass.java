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

public class tarjanClass
{

    @Context
    public Transaction txn;
  
    // @Context
    // public Log log;

    Deque<Node> stack = new ArrayDeque<Node>();
  
  
    // @Procedure(name = "org.col362.neo4j.applyTarjan")
    // @Description("org.col362.neo4j.applyTarjan(String condition, long numViews)")

    private void initialize()
    {
        for (Node node : txn.getAllNodes())
        {
            node.setProperty("lowlink", -1);
            node.setProperty("index", -1);
            node.setProperty("onStack", false);
        }
    }

    private void tarjan(Node node, int index, Deque<Node> stack)
    {
        node.setProperty("index", index);
        node.setProperty("lowlink", index);
        node.setProperty("onStack", true);
        index++;
        stack.push(node);

        for (Relationship rel : node.getRelationships(Direction.OUTGOING))
        {
            Node child = rel.getEndNode();
            if (child.getProperty("index").equals(-1))
            {
                tarjan(child, index, stack);
                node.setProperty("lowlink", Math.min((int)node.getProperty("lowlink"), (int)child.getProperty("lowlink")));
            }
            else if (child.getProperty("onStack").equals(true))
            {
                node.setProperty("lowlink", Math.min((int)node.getProperty("lowlink"), (int)child.getProperty("index")));
            }
        }

        if (node.getProperty("lowlink").equals(node.getProperty("index")))
        {
            Node w;
            do
            {
                w = stack.pop();
                w.setProperty("onStack", false);
            } while (!w.equals(node));
        }
    }

    public void applyTarjan()
    {
        initialize();
        int index = 0;
        for (Node node : txn.getAllNodes())
        {
            if (node.getProperty("index").equals(-1))
            {
                tarjan(node, index, stack);
            }
        }
        txn.commit();
    }
}  