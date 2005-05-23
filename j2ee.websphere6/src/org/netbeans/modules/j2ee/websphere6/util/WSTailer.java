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
package org.netbeans.modules.j2ee.websphere6.util;

import java.io.*;

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
        this.io = IOProvider.getDefault().getIO(ioPanelName, true);
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
        this.io = IOProvider.getDefault().getIO(ioPanelName, true);
    }
    
    /**
     * Implementation of the Runnable interface. Here all tailing is 
     * performed
     */
    public void run() {
        try {
            // check the source for the tailing, if it is a file we create a 
            // new FileInputStream
            if (file != null) {
                inputStream = new FileInputStream(file);
            }
            
            // create a reader from the input stream
            InputStreamReader reader = new InputStreamReader(inputStream);
            
            // read from the input stream and put all the changes to the 
            // I/O window
            char[] chars = new char[1024];
            while (true) {
                // while there is something in the stream to be read - read that
                while (reader.ready()) {
                    io.getOut().println(new String(chars, 0, 
                            reader.read(chars)));
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
                inputStream.close();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
    }
    
}
