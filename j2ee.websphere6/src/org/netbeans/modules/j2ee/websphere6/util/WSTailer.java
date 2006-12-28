/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.util;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openide.*;
import org.openide.windows.*;

/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans
 *
 * @author Kirill Sorokin
 */
public class WSTailer extends Thread {
    
    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int delay = 1000;
        
    private static Map isIOPanelOpen = Collections.synchronizedMap((Map)new HashMap(2,1));
    
    /**
     * The file for which to track changes
     */
    private File file;
    
    /**
     * The input stream for which to track changes
     */
    private InputStream inputStream;
    
    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;
    
    /**
     * Creates a new instance of WSTailer
     *
     * @param file the file for which to track changes
     * @param ioPanelName the I/O window where to output the changes
     */
    public WSTailer(File file, String ioPanelName) {
        // save the parameters
        this.file = file;
        this.io = IOProvider.getDefault().getIO(ioPanelName, false);        
    }
    
    /**
     * Creates a new instance of WSTailer
     *
     * @param file the input stream for which to track changes
     * @param ioPanelName the I/O window where to output the changes
     */
    public WSTailer(InputStream inputStream, String ioPanelName) {
        // save the parameters
        this.inputStream = inputStream;
        this.io = IOProvider.getDefault().getIO(ioPanelName, false);        
    }
    
    /**
     * Implementation of the Runnable interface. Here all tailing is
     * performed
     */
    public void run() {
        if(isIOPanelOpen.containsKey(io)) {
            return;
        }
        
        isIOPanelOpen.put(io, new Object());
        try {            
            
            if(io.isClosed()) {                
                io.getOut().reset();
            }            
            this.io.select();
            
            // check the source for the tailing, if it is a file we create a
            // new FileInputStream            
            if (file != null) {
                while (inputStream == null) {
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (IOException e) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ex) {
                            continue;
                        }
                    }
                }
            }            
            // create a reader from the input stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            // read from the input stream and put all the changes to the
            // I/O window
            String line;                   
            while (!io.isClosed()) {
                // while there is something in the stream to be read - read that
                while (reader.ready()) {
                    line = reader.readLine();                    
                    if(line!=null) {
                        io.getOut().println(line);
                        io.getOut().flush();                        
                    }
                }
                
                // when the stream is empty - sleep for a while
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }            
        } catch (FileNotFoundException e) {            
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            return;
        } catch (IOException e) {            
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        } finally {
            // close the opened stream
            try {
                isIOPanelOpen.remove(io);
                inputStream.close();         
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }        
    }
    
}
