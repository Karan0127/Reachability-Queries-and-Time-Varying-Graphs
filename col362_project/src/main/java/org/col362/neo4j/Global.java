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

class Global
{
    static int N = 105;
    static int T = 105;
    static int time = 0;
    static int temp = 20;
    static LinkedHashSet<Integer> availableSCCs = new LinkedHashSet<Integer>();
    static int offset1 = 0;

    static int getNewSCCId()
    {
        // Remove and the first element
        // Iterator<Integer> iterator = availableSCCs.iterator();
        // int scc = -1;
        // if (iterator.hasNext())
        //     scc = iterator.next();
        // availableSCCs.remove(scc);
        // // return temp++;
        // return scc;
        return temp++;
    }

    static void addSCCToFreeList(int scc)
    {
        // availableSCCs.add(scc);
        return;
    }
}