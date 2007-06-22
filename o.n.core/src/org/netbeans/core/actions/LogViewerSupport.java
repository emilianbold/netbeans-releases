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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Po-Ting Wu: Copy from studio-plugin/src/org/netbeans/modules/j2ee/sun/ide/j2ee/LogViewerSupport.java
 */

package org.netbeans.core.actions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.*;

/** Connects the output stream of a file to the IDE output window.
 *
 * @author ludo
 */
public class LogViewerSupport implements Runnable {
    boolean shouldStop = false;
    FileInputStream  filestream=null;
    BufferedReader    ins;
    InputOutput io;
    File fileName;
    String ioName;
    int lines;
    Ring ring;
    private final RequestProcessor.Task task = RequestProcessor.getDefault().create(this);
    
    /** Connects a given process to the output window. Returns immediately, but threads are started that
     * copy streams of the process to/from the output window.
     * @param process process whose streams to connect to the output window
     * @param ioName name of the output window tab to use
     */
    public LogViewerSupport(final File fileName, final String ioName) {

        this.fileName=fileName;
        this.ioName = ioName;
    }


    private void init() {
        final int LINES = 2000;
        final int OLD_LINES = 2000;
        ring = new Ring(OLD_LINES);
        String line;

        // Read the log file without
        // displaying everything
        try {
            while ((line = ins.readLine()) != null) {
                ring.add(line);
            } // end of while ((line = ins.readLine()) != null)
        } catch (IOException e) {
            Logger.getLogger(LogViewerSupport.class.getName()).log(Level.INFO, null, e);
        } // end of try-catch

                                // Now show the last OLD_LINES
        lines = ring.output();
        ring.setMaxCount(LINES);
    }

    public void run() {
        final int MAX_LINES = 10000;
        String line;

        ////System.out.println("io close or not"+io.isClosed());
        if (io.isClosed()){//tab is closed by the user
            shouldStop =true;
        }
        else{
                        // it is possilbe in the case of only
                        // 1 tab, that the tab is hidden, not
                        // closed. In this case we need to
                        // detect that and close our stream
                        // anyway to unlock the log file
            shouldStop =true; //assume the tab is hidden
            TopComponent.Registry rr= TopComponent.getRegistry();
            for (TopComponent tc: rr.getOpened()) {
                if (tc.toString().startsWith("org.netbeans.core.output2.OutputWindow")){
                    // the tab is not hidden so we should not stopped!!!
                    shouldStop =false;
                    break;
                }
            }
        }
        
        if (!shouldStop) {
            try {
                if (lines >= MAX_LINES) {
                    io.getOut().reset();
                    lines = ring.output();
                } // end of if (lines >= MAX_LINES)

                while ((line = ins.readLine()) != null) {
                    if ((line = ring.add(line)) != null) {
                        io.getOut().println(line);
                        lines++;
                    } // end of if ((line = ring.add(line)) != null)
                }

            }catch (IOException e) {
                Logger.getLogger(LogViewerSupport.class.getName()).log(Level.INFO, null, e);
            }
            task.schedule(10000);
        }
        else {
            ///System.out.println("end of infinite loop for log viewer\n\n\n\n");
            stopUpdatingLogViewer();
        }
    }
    /* display the log viewer dialog
     *
     **/
    
    public void showLogViewer() throws IOException{
        shouldStop = false;
        io = IOProvider.getDefault().getIO(ioName, false);
        io.getOut().reset();
        io.select();
        filestream = new FileInputStream(fileName);
        ins = new BufferedReader(new InputStreamReader(filestream));
        
        init();
        task.schedule(0);
    }
    
    /* stop to update  the log viewer dialog
     *
     **/
    
    public void stopUpdatingLogViewer()   {
        try{
            ins.close();
            filestream.close();
            io.closeInputOutput();
            io.setOutputVisible(false);
        }
        catch (IOException e){
            Logger.getLogger(LogViewerSupport.class.getName()).log(Level.INFO, null, e);
        }
    }
    

    private class Ring {
        private int maxCount;
        private int count;
        private LinkedList<String> anchor;
        
        public Ring(int max) {
            maxCount = max;
            count = 0;
            anchor = new LinkedList<String>();
        }
    
        public String add(String line) {
            if (line == null || line.equals("")) { // NOI18N
                return null;
            } // end of if (line == null || line.equals(""))
            
            while (count >= maxCount) {
                anchor.removeFirst();
                count--;
            } // end of while (count >= maxCount)
            
            anchor.addLast(line);
            count++;
            
            return line;
        }

        public void setMaxCount(int newMax) {
            maxCount = newMax;
        }
        
        public int output() {
            int i = 0;
            for (String s: anchor) {
                io.getOut().println(s);
                i++;
            }

            return i;
        }
        
        public void reset() {
            anchor = new LinkedList<String>();
        }
    }
}

