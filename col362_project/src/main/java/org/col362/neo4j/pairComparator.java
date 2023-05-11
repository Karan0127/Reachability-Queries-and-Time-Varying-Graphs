package org.col362.neo4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.io.FileReader;
import java.util.stream.Stream;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ByteArrayOutputStream;

public class pairComparator implements Comparator<pair> {
    public int compare(pair p1, pair p2) {
        if(p1.first > p2.first)
            return 1;
        else if(p1.first < p2.first)
            return -1;
        if(p1.second > p2.second)
            return 1;
        else if(p1.second < p2.second)
            return -1;
        return 0;
    }
}
