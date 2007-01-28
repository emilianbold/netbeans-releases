/*
 * StreamGobbler.java
 *
 * Created on November 16, 2004, 9:52 AM
 */

package org.netbeans.modules.visualweb.ejb.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The StreamGobbler class manages a thread that reads an input stream
 *
 * @author  cao
 */
public class StreamGobbler implements Runnable {
    private String name;
    private InputStream is;
    private Thread thread;
    
    public StreamGobbler(String name, InputStream is) {
        this.name = name;
        this.is = is;
    }
    
    public void start() {
        thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            while (true) {
                String s = br.readLine();
                if (s == null) break;
                System.err.println("[" + name + "] " + s);
            }
            
            is.close();
            
        } catch (Exception ex) {
            System.out.println("Problem reading stream " + name + "... :" + ex);
            ex.printStackTrace();
        }
    }
}
