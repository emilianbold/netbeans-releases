/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.util.*;
import java.io.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.ErrorManager;
import org.openide.windows.InputOutput;
import org.openide.windows.*;
/** Connects the output stream of a file to the IDE output window.
 *
 * @author ludo
 */
public class LogViewerSupport extends Thread {
    boolean shouldStop = false;
    FileInputStream  filestream=null;
    BufferedReader    ins;
    InputOutput io;
    File fileName;
    String url;
    /** Connects a given process to the output window. Returns immediately, but threads are started that
     * copy streams of the process to/from the output window.
     * @param process process whose streams to connect to the output window
     * @param url deployment manager URL
     */
    public LogViewerSupport(final File fileName, final String url) {
        
        this.fileName=fileName;
        this.url = url;
    }
    
    
    public void run() {
        int MAX_LINES = 15000;
        int LINES = 2000;
        int OLD_LINES = 600;
        int lines;
        Ring ring = new Ring(OLD_LINES);
        int c;
        String line;
        
                                // Read the log file without
                                // displaying everything
        try {
            while ((line = ins.readLine()) != null) {
                ring.add(line);
            } // end of while ((line = ins.readLine()) != null)
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } // end of try-catch
        
                                // Now show the last OLD_LINES
        lines = ring.output();
        ring.setMaxCount(LINES);
        
        while (shouldStop ==false) {
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            try {
                sleep(10000);
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
                    Set ss = rr.getOpened();
                    Iterator ttt = ss.iterator();
                    while (ttt.hasNext()){
                        Object o = ttt.next();
                        String sss=""+o;
                        if (sss.startsWith("org.netbeans.core.output2.OutputWindow")){
                            // the tab is not hidden so we should not stopped!!!
                            shouldStop =false;
                        }
                    }
                }
            }catch (Exception e){
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            }
        }
        ///System.out.println("end of infinite loop for log viewer\n\n\n\n");
        stopUpdatingLogViewer();
        
    }
    /* display the log viewer dialog
     *
     **/
    
    public void showLogViewer() throws IOException{
        shouldStop = false;
        io = UISupport.getServerIO(url);
        io.getOut().reset();
        io.select();
        filestream = new FileInputStream(fileName);
        // RAVE ins = new BufferedReader(new InputStreamReader(filestream,"UTF-8"));//NOI18N
                                // Use the default charset!
        ins = new BufferedReader(new InputStreamReader(filestream));
        
        start();
    }
    
    /* stop to update  the log viewer dialog
     *
     **/
    
    public void stopUpdatingLogViewer()   {
        shouldStop = true;
        
        try{
            ins.close();
            filestream.close();
            io.closeInputOutput();
            io.setOutputVisible(false);
        }
        catch (IOException e){
            
        }
    }
    

    private class Ring {
        private int maxCount;
        private int count;
        private LinkedList anchor;
        
        public Ring(int max) {
            maxCount = max;
            count = 0;
            anchor = new LinkedList();
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
            Iterator it = anchor.iterator();
            
            while (it.hasNext()) {
                io.getOut().println((String)it.next());
                i++;
            } // end of while (it.hasNext())

            return i;
        }
        
        public void reset() {
            anchor = new LinkedList();
        }
    }
}

