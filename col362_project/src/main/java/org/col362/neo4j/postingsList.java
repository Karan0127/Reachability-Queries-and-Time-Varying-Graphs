package org.col362.neo4j;

import java.util.ArrayList;
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

public class postingsList {
    private long num_nodes = 0;
    private int T = 0;
    private String filepath;
    int offset1 = 0;
    private int offset2 = 0;

    public postingsList(long num_nodes, int T, String filepath) {
        this.num_nodes = num_nodes;
        this.T = T;
        // System.out.println(T);
        this.filepath = filepath;
    }
    public byte[] serializer(java.lang.Object obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            byte[] listBytes= bos.toByteArray();
            return listBytes;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public void createPostingsList() {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.filepath, "rw");
            System.out.println("pointer at " + raf.getFilePointer());
            ArrayList<pair> al = new ArrayList<pair>();
            for(int i = 0; i < T; i++){
                al.add(new pair(Long.MAX_VALUE,Long.MAX_VALUE));
            }
            // Long[] arr = new Long[2];
            // arr[0] = 1L;
            // arr[1] = 2L;
            this.offset2 = serializer(al.get(0)).length;
            // System.out.println(this.offset2);
            // System.out.println(serializer(new Object()).length);
            for(int i = 0; i < this.num_nodes; i++) {
                // ByteArrayOutputStream bos = new ByteArrayOutputStream();
                // ObjectOutputStream oos = new ObjectOutputStream(bos);
                // oos.writeObject(al);
                // oos.flush();
                // byte[] listBytes= bos.toByteArray();
                byte[] listBytes = serializer(al);
                raf.write(listBytes);
                // System.out.println(i + ": bytearray size is " + listBytes.length);
                // System.out.println(listBytes);
                // System.out.println(i + ": pointer at " + raf.getFilePointer());
                this.offset1 = listBytes.length;
            }
            raf.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ArrayList<pair> readPostingsList(long index) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.filepath, "r");
            // System.out.println("pointer at " + raf.getFilePointer());
            
            ArrayList<pair> al; 
            byte[] listBytes = new byte[this.offset1];
            raf.seek(index*offset1);
            raf.read(listBytes);
            ByteArrayInputStream bos = new ByteArrayInputStream(listBytes);
            ObjectInputStream ois = new ObjectInputStream(bos);
            al = (ArrayList<pair>) ois.readObject();
            // ois.flush();
                // System.out.println(i + ": bytearray size at " + listBytes.length);
                // System.out.println(i + ": pointer at " + raf.getFilePointer());
            
            raf.close();
            return al;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeAtIndex(long index, ArrayList<pair> writelist) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.filepath, "rw");
            raf.seek(index*offset1);
            // ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // ObjectOutputStream oos = new ObjectOutputStream(bos);
            // oos.writeObject(writelist);
            // oos.flush();
            // byte[] listBytes= bos.toByteArray();
            byte[] listBytes = serializer(writelist);
            raf.write(listBytes);
            // System.out.println("bytearray size at " + listBytes.length);
            // System.out.println("pointer at " + raf.getFilePointer());
            raf.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // public void addToList(long index, ArrayList<Long> writelist) {
    //     try {
    //         RandomAccessFile raf = new RandomAccessFile(this.filepath, "rw");
    //         raf.seek(index*offset1);
    //         ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //         ObjectOutputStream oos = new ObjectOutputStream(bos);
    //         oos.writeObject(writelist);
    //         oos.flush();
    //         byte[] listBytes= bos.toByteArray();
    //         raf.write(listBytes);
    //         System.out.println("bytearray size at " + listBytes.length);
    //         System.out.println("pointer at " + raf.getFilePointer());
    //         raf.close();
    //     }
    //     catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}