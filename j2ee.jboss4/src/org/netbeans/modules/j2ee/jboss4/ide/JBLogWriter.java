/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4.ide;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import org.openide.ErrorManager;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import java.util.Hashtable;

/**
 *
 * @author Kirill Sorokin
 */
public class JBLogWriter implements Runnable {
    
    private OutputWriter writer;
    private Reader reader;
    private boolean fromFile;
    private InputOutput io;
    private String server;
    
    private static Hashtable instances = new Hashtable();
    
    
    private final static int delay = 500;
    
    private boolean read;
    
    /** Creates a new instance of JBLogWriter */
//    private JBLogWriter(InputOutput io, InputStream in) {
//        this.writer = io.getOut();
//        this.reader = new InputStreamReader(in);
//    }
    private JBLogWriter(InputOutput io, InputStream in, String server,boolean fromFile) {
        this.writer = io.getOut();
        this.reader = new InputStreamReader(in);
        this.fromFile = fromFile;
        this.io = io;
        this.server = server;
    }
    
    synchronized public static  JBLogWriter createInstance(InputOutput io, InputStream in, String server, boolean fromFile ){
        JBLogWriter instance = (JBLogWriter)instances.get(server);
        if (instances.get(server)==null){
            instance = new JBLogWriter(io,in,server,fromFile);
            instances.put(server, instance);
        }
        return instance;
    }
    
    
     synchronized public static  JBLogWriter updateInstance(InputOutput io, InputStream in, String server ){
        instances.remove(server);
        return createInstance(io, in, server, false );
    }
    
    public static JBLogWriter createInstance(InputOutput io, InputStream in, String server){
       return createInstance(io, in,server, false);
    }
    
    public void run() {
        read = true;
        LineNumberReader reader = new LineNumberReader(this.reader);
        if(fromFile ){
                try {
                    while (reader.ready()) {
                        String line = reader.readLine();
                        if(line.indexOf("INFO")<0)
                            continue;
                        writer.write(line);
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
        }
        while (read && !io.isClosed()) {
            try {
                if (reader.ready()) {
                    String line = reader.readLine();
                    if(fromFile ){
                        if(line.indexOf("INFO")<0)
                            continue;
                    }
                    writer.write(line);
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // do nothing
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        read = false;
        instances.remove(server);
    }
    
    public void stopReading() {
        this.read = false;
    }
    
    public boolean isRunning(){
        return read;
    }
}