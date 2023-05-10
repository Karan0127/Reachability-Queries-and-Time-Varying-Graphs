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

public class pair implements Serializable {
    private static final long serialversionUID = 42L;
    
    long first, second;
   
    // constructor for assigning values
    pair(long first, long second)
    {
        this.first = first;
        this.second = second;
    }

    // long getFirst() {
    //     return first;
    // } 

    // long getSecond() {
    //     return second;
    // }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
 
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
 
        pair p = (pair) o;
 
        // call `equals()` method of the underlying objects
        if (first != p.first) {
            return false;
        }
        return second == p.second;
    }

    public String toString() {
        return "(" + first + ", " + second + ")";
    }    

}