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

package org.netbeans.modules.tomcat5.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.windows.*;





/**
 * Thread which displays Tomcat log files in the output window. The output 
 * window name equals prefix minus trailing dot, if present.
 *
 * Currently only <code>org.apache.catalina.logger.FileLogger</code> logger
 * is supported.
 *
 * @author  Stepan Herold
 */
public class LogViewer extends Thread {   
    private Map/*<String, Link>*/ links = new HashMap();
    private Annotation errAnnot;
    private volatile boolean stop = false;
    private InputOutput inOut;    
    
    private File directory;
    private String prefix;
    private String suffix;
    private boolean isTimestamped;
    private boolean takeFocus;    
    
    /**
     * Creates a new LogViewer thread.
     *
     * @param catalinaDir catalina directory (CATALINA_BASE or CATALINA_HOME)
     * @param className class name of logger implementation
     * @param directory absolute or relative pathname of a directory in which log 
     * files reside, if null catalina default is used
     * @param prefix log file prefix, if null catalina default is used
     * @param suffix log file suffix, if null catalina default is used
     * @param isTimestamped whether logged messages are timestamped
     * @param takeFocus whether output window should get focus after each change
     * 
     * @throws NullPointerException if catalinaDir parameter is null
     * @throws UnsupportedLoggerException logger specified by the className parameter
     * is not supported
     */
    public LogViewer(File catalinaDir, String className, String directory, 
            String prefix, String suffix, boolean isTimestamped, 
            boolean takeFocus) throws UnsupportedLoggerException {        
        super("LogViewer - Thread"); // NOI18N
        if (catalinaDir == null) throw new NullPointerException();        
        if (!"org.apache.catalina.logger.FileLogger".equals(className)) { // NOI18N
            throw new UnsupportedLoggerException(className);
        }
        setDaemon(true);
        if (directory != null) {
            this.directory = new File(directory);
            if (!this.directory.isAbsolute()) {
                this.directory = new File(catalinaDir, directory);
            }
        } else {
            this.directory = new File(catalinaDir, "logs");  // NOI18N
        }
        if (prefix != null) {
            this.prefix = prefix;
        } else {
            this.prefix = "catalina."; // NOI18N
        }
        if (suffix != null) {
            this.suffix = suffix;
        } else {
            this.suffix = ".log";  // NOI18N
        }

        this.isTimestamped = isTimestamped;
        this.takeFocus = takeFocus;
        
        // cut off trailing dot
        String displayName = this.prefix;
        int trailingDot = displayName.lastIndexOf('.');
        if (trailingDot > -1) displayName = displayName.substring(0, trailingDot);
        
        inOut = IOProvider.getDefault().getIO(displayName, false);
    }
    
    /**
     * Stop the LogViewer thread.
     */
    public void close() {
        synchronized(this) {
            stop = true;
            notify();
        }        
    }
    
    /**
     * Tests whether LogViewer thread is still running.
     * @return <code>false</code> if thread was stopped or its output window
     * was closed, <code>true</code> otherwise.
     */
    public boolean isOpen() {
        InputOutput io = inOut;
        return !(io == null || stop || io.isClosed());
    }
    
    /**
     * Make the log tab visible
     */
    public void takeFocus() {
        InputOutput io = inOut;
        if (io != null) io.select();
    }
    
    private File getLogFile(String timestamp) throws IOException {
        File f = new File(directory, prefix + timestamp + suffix);
        f.createNewFile(); // create, if does not exist
        return f;
    }
    
    private String getTimestamp() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //NOI18N
        return df.format(new Date());
    }
    
    private Link getLink(String errorMsg) {
        if (links.containsKey(errorMsg)) {
            return (Link)links.get(errorMsg);
        } else {
            Link ln = new Link(errorMsg);
            links.put(errorMsg, ln);
            return ln;
        }
    }
    
    public void run() {
        OutputWriter writer = inOut.getOut();
        OutputWriter errorWriter = inOut.getErr();
        BufferedReader reader = null;
        String timestamp = getTimestamp();
        String oldTimestamp = timestamp;
        try {
            File logFile = getLogFile(timestamp);
            reader = new BufferedReader(new FileReader(logFile));
            // do not show older output
            reader.skip(logFile.length());
            // remember error msg
            String errorMsg = "";
            while (!stop && !inOut.isClosed()) {
                // check whether a log file has rotated
                timestamp = getTimestamp();
                if (!timestamp.equals(oldTimestamp)) {
                    oldTimestamp = timestamp;
                    reader.close();
                    logFile = getLogFile(timestamp);
                    reader = new BufferedReader(new FileReader(logFile));
                }
                int count = 0;
                // take a nap after 1024 read cycles, this should ensure responsiveness
                // even if log file is growing fast
                while (reader.ready() && count++ < 1024) {
                    String line = reader.readLine();
                    if (line.trim().startsWith("at ")) { //NOI18N
                        // stacktrace msg
                        errorWriter.println(line, getLink(errorMsg));
                    } else if (isTimestamped && line.length() > 0 
                            && !Character.isDigit(line.charAt(0)))  {
                        // if log msgs are timestamped try to filter exception msg
                        errorMsg = line;
                        errorWriter.println(line);
                    } else {
                        // other msg
                        
                        // if msgs are not timestamped, exceptions cannot be recognized,
                        // consider then every msg as a possible exception and remember it
                        errorMsg = line;
                        writer.println(line);
                    }
                    errorWriter.flush();
                    writer.flush();
                    if (takeFocus) inOut.select();
                }
                // wait for the next attempt
                try {
                    synchronized(this) {
                        if (!stop &&  !inOut.isClosed()) {
                            wait(2000);
                        }
                    }
                } catch(InterruptedException ex) {
                    // ok to ignore
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            writer.close();
            try {
                reader.close();
            } catch(IOException ioe) {
                // ok to ignore
            }
        }
        if (errAnnot != null) {
            errAnnot.detach();
        }
    }
    
    private static class ErrorAnnotation extends Annotation {
        private String shortDesc = null;
        
        public ErrorAnnotation(String desc) {
            shortDesc = desc;
        }
        
        public String getAnnotationType() {
            return "org-netbeans-modules-tomcat5-error"; // NOI18N
        }
        
        public String getShortDescription() {
            return shortDesc;
        }
        
    }
    
    private class Link implements OutputListener {
        private String errMsg = null;
        
        public Link(String errMsg) {
            this.errMsg = errMsg;
        }
        
        public void outputLineAction(OutputEvent ev) {
            String line = ev.getLine().trim();
            String classWithMethod = line.substring(line.indexOf(' ') + 1, line.indexOf('('));
            String className = classWithMethod.substring(0,classWithMethod.lastIndexOf('.'));
            String sourceFileName = className.replace('.','/') + ".java";
            String lineNumber = line.substring(line.lastIndexOf(':') + 1, line.lastIndexOf(')'));
            
            FileObject sourceFile = GlobalPathRegistry.getDefault().findResource(sourceFileName);
            DataObject dataObject = null;
            if (sourceFile != null) {
                try {
                    dataObject = DataObject.find(sourceFile);
                } catch(DataObjectNotFoundException ex) {
                    // it's ok to ignore, msg will be shown by the if-else statement below
                }
            }
            if (dataObject != null) {
                EditorCookie editorCookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
                if (editorCookie == null) return;
                editorCookie.open();              
                int errLineNum = 0;
                Line errorLine = null;
                try {
                    errLineNum= Integer.parseInt(lineNumber) - 1;
                    errorLine = editorCookie.getLineSet().getCurrent(errLineNum);
                } catch (IndexOutOfBoundsException iobe) {
                    return;
                } catch (NumberFormatException nfe) {
                    return;
                }
                if (errAnnot != null) {
                    errAnnot.detach();
                }
                if (errMsg == null || errMsg.equals("")) { //NOI18N
                    errMsg = NbBundle.getMessage(Link.class, "MSG_ExceptionOccurred");
                }
                errAnnot = new ErrorAnnotation(errMsg);
                errAnnot.attach(errorLine);
                errAnnot.moveToFront();
                errorLine.show(Line.SHOW_TRY_SHOW);
            } else {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Link.class, "MSG_SrcFileNotFound", className));
            }
        }
        
        public void outputLineCleared(OutputEvent ev) {}
        public void outputLineSelected(OutputEvent ev) {}
    }
}