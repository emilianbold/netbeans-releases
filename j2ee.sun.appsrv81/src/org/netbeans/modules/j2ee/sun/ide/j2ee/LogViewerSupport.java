/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.ErrorManager;
import org.openide.windows.InputOutput;




public final class LogViewerSupport extends Thread {
    
    
    private InputOutput io;
    
    private String url;
    private static final HashMap logsMap = new HashMap();
    // known open logs
    // private static Set openLogs = new HashSet();
    /**
     * frequency to check file changes
     */
    private long sampleInterval = 10000;
    
    /**
     * The log file to tail
     */
    private File logfile;
    
    /**
     * Defines whether the log file tailer should include the entire contents
     * of the exising log file or tail from the end of the file when the tailer starts
     */
    private boolean startAtBeginning = false;
    
    /**
     * are we working, reading the log file?
     */
    private boolean working = false;
    private LogHyperLinkSupport.AppServerLogSupport logSupport;
    private BufferedReader reader = null;
    
    private boolean initRingerDone=false;
    /* ring buffer constants:
     */
    static private  int OLD_LINES = 600;
    static private  int MAX_LINES = 25000;
    static private  int LINES = 2000;    

    /**
     * Creates a new log file tailer
     *
     * @param file         The file to tail
     * @param sampleInterval    How often to check for updates to the log file (default = 10s)
     * @param startAtBeginning   Should the tailer simply tail or should it process the entire
     *               file and continue tailing (true) or simply start tailing from the
     *               end of the file
     */

    public static LogViewerSupport getLogViewerSupport(final File file, final String url, final long sampleInterval, final boolean startAtBeginning ) {
        synchronized (logsMap) {
            LogViewerSupport logViewer = (LogViewerSupport)logsMap.get(url);
            if (logViewer==null) {
                logViewer = new LogViewerSupport( file, url,  sampleInterval, startAtBeginning );
                logsMap.put(url,logViewer);
            }

            logViewer.sampleInterval = sampleInterval;
            logViewer.startAtBeginning = startAtBeginning;
            Deployment.getDefault().addInstanceListener(new InstanceListener() {

                @Override
                public void instanceAdded(String serverInstanceID) {
                }

                @Override
                public void instanceRemoved(String serverInstanceID) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                    if (serverInstanceID != null &&
                            serverInstanceID.equals(url)) {
                        removeLogViewerSupport(url);
                        Deployment.getDefault().removeInstanceListener(this);
                    }
                    
                }
            
            });
            return logViewer;
        }
    }

    /**
     * stop and remove the log viewer thread for the given server (url)
     *  @param url: the server url
     *  nop is there is no thread for this server
     *  otherwise, stop the thread and close the log file, so that there is no lock on it anymore
     */
    public static void removeLogViewerSupport(final String url) {
        synchronized (logsMap) {
            LogViewerSupport logViewer = (LogViewerSupport)logsMap.get(url);
            if (logViewer!=null) {
                logsMap.remove(url);
                logViewer.working =false;
            }
        }
    }
    
    private LogViewerSupport(final File file, final String url, final long sampleInterval, final boolean startAtBeginning) {
        this.logfile = file;
        this.url = url;
        this.sampleInterval = sampleInterval;
        this.startAtBeginning = startAtBeginning;
        io = UISupport.getServerIO(url);
        
        logSupport = new LogHyperLinkSupport.AppServerLogSupport("", "/");
        // let  process know that it doesn't have to wait for me.
        setDaemon(true);
        start();
    }
    
    
    
    protected void printLine( String line ) {
        String s = filterLine(line);
        if ((!s.equals(""))&&((!s.equals(" ")))){//NOI18N
            
            LogHyperLinkSupport.AppServerLogSupport.LineInfo lineInfo = logSupport.analyzeLine(s);
            if (lineInfo.isError()) {
                if (lineInfo.isAccessible()) {
                    try {
                        io.getOut().println(s, logSupport.getLink(lineInfo.message(), lineInfo.path(), lineInfo.line()));
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                } else {
                    io.getOut().println(s);
                }
            } else {
                io.getOut().println(s);
            }
            
        }
        
    }
    
    public void stopTailing() {
        this.working = false;
    }
    
    
    private void initRingBuffer(Ring ring){

        try {            
            // Start tailing
            if (reader!=null){
                try {
                    reader.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                reader = null;
            }
            
            if(logfile.exists()) {
                reader = new BufferedReader(new FileReader(logfile));
                int c;
                String line;

                // Read the log file without
                // displaying everything
                try {
                    while ((line = reader.readLine()) != null) {
                        ring.add(line);
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                // Now show the last OLD_LINES
                if (startAtBeginning){
                    ring.output(null);
                }
                ring.setMaxCount(LINES);
            }
        } catch (Exception e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
    }

    @Override
    public void run() {
        // The file pointer keeps track of where we are in the file
        long currentIndex = 0;
        FilenameFilter logsDirectoryFilter = new FilenameFilter() {
            
            @Override
            public boolean accept(File arg0, String arg1) {
                return arg1.startsWith(logfile.getName());
            }
            
        };
        boolean needTosleep=true;
        boolean needToRotate=false;
        boolean alreadyRotated=false;
        working = true;
        String line;
        int lines =0;

        Ring ring = new Ring(OLD_LINES);        
        try{
            long currentNbofFileLogs = 0;
            if (null == logfile || !logfile.exists()) {
                working = false;
            } else {
                currentNbofFileLogs = logfile.getParentFile().list(logsDirectoryFilter).length;
                currentIndex = logfile.length();
            }
            while( working  /*&& !io.isClosed()*/) {
                needTosleep=true;
                try {
                    if (initRingerDone==false){
                        //this init is initializing the reader
                        initRingBuffer(ring);
                        currentIndex = logfile.length();
                        initRingerDone=true;
                    }
                    if (lines >= MAX_LINES) {
                        io.getOut().reset();
                        lines = ring.output(null);
                    }
                    // Compare the length of the file to the file pointer
                    long fileLength = logfile.length();
                    long newNbofFileLogs = logfile.getParentFile().list(logsDirectoryFilter).length;
                    
                    if( fileLength < currentIndex ) {
                        needToRotate =true;
                    }
                    
                    if( currentNbofFileLogs < newNbofFileLogs ) {
                        needToRotate =true;
                        currentNbofFileLogs = newNbofFileLogs;
                    }
                    
                    if (needToRotate ){
                        // Log file must have been rotated or deleted;
                        // reopen the file and reset the file pointer
                        
                        //flush where we are.
                        try {
                            line = (reader != null) ? reader.readLine() : null;
                            if(line != null) {
                                alreadyRotated = false;
                            }
                            while( line != null ) {
                                printLine( line );
                                ring.add(line);
                                
                                line = reader.readLine();
                                lines++;
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            needToRotate =true;
                        }
                        
                        //reopen the reader on the new log file:
                        if(!alreadyRotated) {
                            // only print this once until we get more messages.
                            printLine("----Log File Rotated---");
                            alreadyRotated = true;
                        }
                        
                        try {
                            if(reader != null) {
                                reader.close();
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                        
                        if(logfile.exists()) {
                            reader = new BufferedReader(new FileReader(logfile));
                            currentIndex = 0;
                            needToRotate = false;
                        } else {
                            reader = null;
                        }
                    }
                    
                    
                    try {
                        line = (reader != null) ? reader.readLine() : null;
                        if(line != null) {
                            alreadyRotated = false;
                        }
                        while( line != null ) {
                            printLine( line );
                            ring.add(line);
                            line = reader.readLine();
                            lines++;
                        }
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        needTosleep=false; //try to catch up
                        needToRotate =true;
                    }
                    //calculate the current log situation
                    currentIndex = logfile.length();
                    currentNbofFileLogs = logfile.getParentFile().list().length;
                    
                    
                } catch(Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    working = true;//we continue
                } finally{
                    try {
                        synchronized(this) {                            
                            if (needTosleep){
                                wait(100);
                            }
                        }
                    } catch(InterruptedException ex) {
                        // PMD no op - the thread was interrupted
                    }
                }
            }
        } catch( Exception e ) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            working =false;//we continue
        } finally{
            //we close the reader
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    /*
     * return the 6th element of a log line entry, or the line if this element does
     * not exist, or if the line cannot be parsed correctly.
     *
     */
    private String filterLine(String line){
        if (!line.startsWith("[#")){ //NOI18N
            if (line.endsWith("|#]")){//NOI18N
                line = line.substring(0,line.length()-3); //remove the last 3 chars.
            }
            return line;
        }
        
        String s[] = line.split("\\|");//NOI18N
        if (s==null){
            return line;
        }
        
        
        if (s.length<=6){
            return "";//NOI18N
        }
        
        return s[6];
    }

    /**
     * display the log viewer dialog
     * @param forced reset view if true, otherwise refresh
     * @return The output window
     * @throws java.io.IOException encountered issue while doing a reset on the stdout
     */
    public InputOutput showLogViewer(boolean forced) throws IOException{
        io = UISupport.getServerIO(url);
        
        working = true;
        if (forced &&(io.isClosed())){
            initRingerDone=false;
            io.getOut().reset();
        }
        
        io.select();
        
        return io;
        
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
            }
            
            while (count >= maxCount) {
                anchor.removeFirst();
                count--;
            }
            
            anchor.addLast(line);
            count++;
            
            return line;
        }
        
        public void setMaxCount(int newMax) {
            maxCount = newMax;
        }
        
        public int output(String bracket) {
            int i = 0;
            Iterator it = anchor.iterator();
            
            if (null != bracket) {
                printLine("<<<<<<<<<<<<<<<<<<<<<<<<<<<< "+bracket);
            }
            while (it.hasNext()) {
                printLine((String)it.next());
                i++;
            }
            if (null != bracket) {
                printLine(">>>>>>>>>>>>>>>>>>>>>>>>>>>> "+bracket);
            }
            return i;
        }
        
        public void reset() {
            anchor = new LinkedList();
        }
    }
    
}
