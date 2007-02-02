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
package org.netbeans.modules.j2ee.jboss4.ide;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Libor Kotouc
 */
public final class JBLogWriter {
    
    public static final boolean VERBOSE = 
        System.getProperty ("netbeans.serverplugins.jboss4.logging") != null;
    
    private final static int DELAY = 500;
    private static final int START_TIMEOUT = 900000;
    
    /**
     * Lock used to avoid a reader switching while the reader is used
     */
    private final Object READER_LOCK = new Object();

    /**
     * Lock used for synchronizing a server startup thread and 
     * a logger thread checking for the server to start
     */
    private final Object START_LOCK = new Object();
    
    //enumeration of the source types the reader can read from
    private static enum LOGGER_TYPE { PROCESS, FILE };
    
    //indicates the type of source the reader is reading from
    private LOGGER_TYPE type;

    private static final String THREAD_NAME = "JBoss Log Writer"; // NOI18N
    private static final String STOPPER_THREAD_NAME = "JBoss Log Writer Stopper"; //NOI18N
    
    private JBStartServer.ACTION_STATUS actionStatus = JBStartServer.ACTION_STATUS.UNKNOWN;

    JBStartServer startServer;
            
    //output pane's writer
    private final OutputWriter out;
    //server output reader
    volatile private BufferedReader reader;
    //server instance name
    private final String instanceName;
    //server process
    private Process process;
    //server log file
    private File logFile;
    
    //the thread currently reading from the server output and writing into the output pane.
    //there is at most one thread for one JBLogWriter instance (i.e. for each server instance running)
    Thread logWriterThread;
    
    //stores the JBLogWriter instance for each server instance for which the server output has been shown
    private static HashMap<String, JBLogWriter> instances = new HashMap<String, JBLogWriter>();
    
    //the log writer sets the value to true to indicate that it is running
    //it can be used to stop the log writer thread when it is set to false
    private boolean read;
    
    //used to remember the last part of the output read from the server process
    //the part is used in the subsequent reading as the beginning of the line, see the issue #81951
    private String trailingLine = "";    
    
    
    private JBLogWriter(InputOutput io, String instanceName) {
        this.out = (io != null ? io.getOut() : null);
        this.instanceName = instanceName;
    }
    
    synchronized public static JBLogWriter createInstance(InputOutput io, String instanceName) {
        JBLogWriter instance = getInstance(instanceName);
        if (instance == null) {
            instance = new JBLogWriter(io, instanceName);
            instances.put(instanceName, instance);
        }
        return instance;
    }
    
    synchronized public static JBLogWriter getInstance(String instanceName) {
        return instances.get(instanceName);
    }

    /**
     * Starts reading of the server log file and writing its content into the output console
     */
    public void start(File logFile) {
        try {
            this.logFile = logFile;
            this.reader = new BufferedReader(new FileReader(logFile));
        } catch (FileNotFoundException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        
        //start the logging thread
        startWriter(new LineProcessor() {
            //only lines with the 'INFO' level severity are written into the output pane
            public void processLine(String line) {
                if (out != null) {
                    if (line != null && line.indexOf(" INFO ") != -1) {
                        out.println(line);
                    }
                }
            }
        }, LOGGER_TYPE.FILE);
    }
    
    /**
     * Starts reading data piped from standard output stream of the server process.
     * It is expected that the this method is called during the server startup. The startup progress
     * is currently done by checking the outgoing messages for some keywords occurence.
     *
     * The calling thread is blocked waiting until it is notified when the server startup has finished.
     *
     * @return true when the server startup was succesfull, false otherwise
     */
    JBStartServer.ACTION_STATUS start(Process process, final JBStartServer startServer) {
        this.process = process;
        this.startServer = startServer;
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        //start the logging thread
        startWriter(new LineProcessor() {
            
            //flag saying whether the the startup progress must be checked or not
            //it is set to false after the server is started or if the server startup is not successfull
            private boolean checkStartProgress = true;

            public void processLine(String line) {
                //all lines are written to the output pane
                if (line != null) {
                    if (out != null) {
                        out.println(line);
                    }
                    if (checkStartProgress) {
                        checkStartProgress(line);
                    }
                }
            }
            
            /**
             * Fires the progress events when the server startup process begins and finishes
             * (either sucessfully or not)
             */
            private void checkStartProgress(String line) {

                if (line.indexOf("Starting JBoss (MX MicroKernel)") > -1 || // JBoss 4.x message // NOI18N
                    line.indexOf("Starting JBoss (Microcontainer)") > -1)   // JBoss 5.0 message // NOI18N
                {
                    if (VERBOSE) {
                        System.out.println("STARTING message fired"); // NOI18N
                    }
                    fireStartProgressEvent(StateType.RUNNING, createProgressMessage("MSG_START_SERVER_IN_PROGRESS")); // NOI18N
                }
                else 
                if ((line.indexOf("JBoss (MX MicroKernel)") > -1 ||     // JBoss 4.x message    // NOI18N
                     line.indexOf("JBoss (Microcontainer)") > -1) &&    // JBoss 5.0 message    // NOI18N
                     line.indexOf("Started in") > -1)                                           // NOI18N
                {
                    if (VERBOSE) {
                        System.out.println("STARTED message fired"); // NOI18N
                    }
                    checkStartProgress = false;
                    actionStatus = JBStartServer.ACTION_STATUS.SUCCESS;
                    notifyStartupThread();
                }
                else 
                if (line.indexOf("Shutdown complete") > -1) { // NOI18N
                    checkStartProgress = false;
                    actionStatus = JBStartServer.ACTION_STATUS.FAILURE;
                    notifyStartupThread();
                }
            }
            
        }, LOGGER_TYPE.PROCESS);
        
        try {
            synchronized (START_LOCK) {
                long start = System.currentTimeMillis();
                //the calling thread is blocked until the server startup has finished or START_TIMEOUT millis has elapsed
                START_LOCK.wait(START_TIMEOUT);
                if (System.currentTimeMillis() - start >= START_TIMEOUT) {
                    if (VERBOSE) {
                        System.out.println("Startup thread TIMEOUT EXPIRED");            
                    }
                    actionStatus = JBStartServer.ACTION_STATUS.UNKNOWN;
                }
                else {
                    if (VERBOSE) {
                        System.out.println("Startup thread NOTIFIED");            
                    }
                }
            }
        } catch (InterruptedException ex) {
            //noop
        }
        
        return actionStatus;
    }

    private void notifyStartupThread() {
        synchronized (START_LOCK) {
            START_LOCK.notify();
        }
    }

    private void fireStartProgressEvent(StateType stateType, String msg) {
        startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, stateType, msg));
    }
    
    private String createProgressMessage(final String resName) {
        return NbBundle.getMessage(JBLogWriter.class, resName, instanceName);
    }
    
    /**
     * Common interface for processing the line read from the server output
     */
    private interface LineProcessor {
        void processLine(String line);
    }
    
    /**
     * Starts the log writer thread. If the old thread is still running then the old thread is interrupted.
     * The old thread is running when is of LOGGER_TYPE.FILE type and the server is stopped outside of the IDE
     * because there is no way how to check the server process status when we don't have access to the server process.
     *
     * The thread reading from the server process (LOGGER_TYPE.PROCESS type) is periodically checking 
     * the server process exit value and finishes when the server process has exited.
     *
     * The thread reading from the server log file periodically checks the server log file size to ensure that 
     * the input stream is valid, i.e. reading from the same file as the server process log file. 
     * JBoss seems to be deleting the log file upon each start thus the file reader is never ready for the reading
     * although the server process is running and logging. Resetting the reader doesn't help thus closing the old one
     * and creating of a new one is needed.
     *
     * The method is also responsible for the correct switching between the threads with different LOGGER_TYPE
     */
    private void startWriter(final LineProcessor lineProcessor, final LOGGER_TYPE type) {
        
        if (isRunning()) {
            //logger reading the log file is not stopped when the server is stopped outside of the IDE
            logWriterThread.interrupt();
            if (VERBOSE) {
                System.out.println("************INTERRUPT thread " + logWriterThread.getId());            
            }
        }
        
        this.type = type;
        
        logWriterThread = new Thread(THREAD_NAME) {
            public void run() {
                if (VERBOSE) {
                    System.out.println("************START thread " + Thread.currentThread().getId());
                }
                read = true;
                boolean interrupted = false;
                long lastFileSize = -1;
                boolean checkProfiler = (startServer != null && startServer.getMode() == JBStartServer.MODE.PROFILE);
                while (read) {
                    // if in profiling mode, server startup (incl. blocked for Profiler direct attach)
                    // is checked by determining Profiler agent status using ProfilerSupport.getState()
                    // STATE_INACTIVE means that Profiler agent failed to start, which also breaks server VM
                    if (checkProfiler) {
                        int state = ProfilerSupport.getState();
                        if (state == ProfilerSupport.STATE_BLOCKING || 
                            state == ProfilerSupport.STATE_RUNNING  ||
                            state == ProfilerSupport.STATE_PROFILING) {
                            fireStartProgressEvent(StateType.COMPLETED, createProgressMessage("MSG_PROFILED_SERVER_STARTED"));
                            checkProfiler = false;
                            notifyStartupThread();
                        } 
                        else 
                        if (state == ProfilerSupport.STATE_INACTIVE) {
                            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_PROFILED_SERVER_FAILED"));
                            process.destroy();
                            notifyStartupThread();
                            break;
                        }
                    }

                    boolean ready = processInput(lineProcessor, type);
                    if (type == LOGGER_TYPE.FILE) { 
                        if (ready) { // some input was read, remember the file size
                            lastFileSize = logFile.length();
                        }
                        // nothing was read, compare the current file size with the remembered one
                        else if (lastFileSize != logFile.length()) {
                            // file size has changed nevertheless there is nothing to read -> refresh needed
                            if (VERBOSE) {
                                System.out.println("!!!!!!!!!DIFFERENCE found");
                            }
                            refresh();
                        }
                    }
                    else {
                        try {
                            process.exitValue();
                            //reaching this line means that the process already exited
                            break;
                        }
                        catch (IllegalThreadStateException itse) {
                            //noop process has not exited yet
                        }
                    }
                    try {
                        Thread.sleep(DELAY); // give the server some time to write the output
                    } catch (InterruptedException e) {
                        interrupted = true;
                        break;
                    }
                }
                
                //print the remaining message from the server process after it has stopped, see the issue #81951
                lineProcessor.processLine(trailingLine);
                
                if (VERBOSE) {
                    System.out.println("************FINISH thread " + Thread.currentThread().getId());
                }
                if (!interrupted) {
                    //reset the read flag and remove instance from the map when the thread exiting is 'natural',
                    //i.e. caused by a server process exiting or by calling stop() on the instance.
                    //the thread interruption means that another thread is going to start execution
                    read = false;
                    instances.remove(instanceName);
                }
            }
        };
        logWriterThread.start();
        
    }
    
    /**
     * Sets the read flag to false to announce running thread that is must stop running.
     */
    void stop() {
        read = false;
    }
    
    boolean isRunning(){
        return read;
    }

    /**
     * If the logger is of type FILE then closes the current reader, resets the output pane
     * and creates new input reader.
     */
    public void refresh() {
        if (type == LOGGER_TYPE.PROCESS || logFile == null) {
            return;
        }

        synchronized (READER_LOCK) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            try {
                if (out != null) {
                    out.reset();
                }
                if (VERBOSE) {
                    System.out.println("REFRESHING the output pane");            
                }
                reader = new BufferedReader(new FileReader(logFile));
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    /**
     * The method is reading the lines from the reader until no input is avalable.
     * @return true if at least one line was available on the input, false otherwise
     */
    private boolean processInput(LineProcessor lineProcessor, final LOGGER_TYPE type) {
        synchronized (READER_LOCK) {
            boolean ready = false;
            try {
                if (type == LOGGER_TYPE.PROCESS) {
                    while (reader.ready()) {
                        //reader.readLine() was hanging on Windows, thus replaced by own readLine() method
                        //see issue #81951
                        ready = readLine(lineProcessor);
                    }
                }
                else {
                    while (reader.ready()) {
                        String line = reader.readLine();
                        lineProcessor.processLine(line);
                        ready = true;
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            return ready;
        }
    }
    
    /**
     * According to the issue #81951, the BefferedReader.read() method must be used 
     * instead of the BefferedReader.readLine() method, otherwise it hangs on Windows 
     * in the underlying native method call after the server process has been stopped. 
     * On Linux the BefferedReader.readLine() method correctly returns.
     *
     * Parsing is done manually to simulate behavior of the BefferedReader.readLine() method. 
     * According to this, a line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a line feed.
     *
     * The method is processing each line read using the given LineProcessor. The remaining text
     * (following the last line) is remembered for the next reading in the instance variable.
     *
     * The method is not able to discover whether there will be some additional reading or not, thus
     * theoretically there are several events when the remaining text might not be processed.
     * The first kind of event is when the server process has been started or some other event has occured
     * (e.g. deploying) which causes the server process to log but not to stop.
     * It is not possible to discover that no other input will be read and if the last logged line 
     * is not ended by the 'new-line' character(s) then the remaing text is printed out not until
     * the next input is read.
     * The second kind of event is when the server process has been stopped. The log writer thread 
     * is finished in this case and has opportunity to write out the remaing text.
     * Actually it seems that the JBoss server is logging the whole lines only so the reading is working well
     * in all cases.
     */
    private boolean readLine(LineProcessor lineProcessor) throws IOException {
        char[] cbuf = new char[128];
        int size = -1;
        if ((size = reader.read(cbuf)) != -1) {
            //prepend the text from the last reading to the text actually read
            String lines = (trailingLine != null ? trailingLine : "");
            lines += new String(cbuf, 0, size);
            int tlLength = (trailingLine != null ? trailingLine.length() : 0);
            int start = 0;
            for (int i = 0; i < size; i++) {//going through the text read and searching for the new line
                //we see '\n' or '\r', *not* '\r\n'
                if (cbuf[i] == '\r' && (i+1 == size || cbuf[i+1] != '\n') || cbuf[i] == '\n') {
                    String line = lines.substring(start, tlLength + i);
                    //move start to the character right after the new line
                    start = tlLength + (i + 1);
                    lineProcessor.processLine(line);
                }
                else //we see '\r\n'
                if (cbuf[i] == '\r' && (i+1 < size) && cbuf[i+1] == '\n') {
                    String line = lines.substring(start, tlLength + i);
                    //skip the '\n' character
                    i += 1;
                    //move start to the character right after the new line
                    start = tlLength + (i + 1);
                    lineProcessor.processLine(line);
                }
            }
            if (start < lines.length()) {
                //new line was not found at the end of the input, the remaing text is stored for the next reading
                trailingLine = lines.substring(start);
            }
            else {
                //null and not empty string to indicate that there is no valid input to write out;
                //an empty string means that a new line character may be written out according 
                //to the LineProcessor implementation
                trailingLine = null; 
            }
            return true;
        }
        return false;
    }
    
    /**
     * The method is used to either for blocking a caller until the server process has exited
     * (when the logger is of PROCESS type) or sleeps some piece of time to allow the logging thread
     * to finish its work.
     * After returning from this call the caller may expect that no other input is going to be read
     * and is safe to stop the logger by calling stop() method.
     *
     * @param milliseconds the time the caller is blocked waiting for the server process to exit, 
     *        otherwise the waiting is terminated. It may help when the process has not exited exists
     *        in some non-defined state
     */
    void waitForServerProcessFinished(long milliseconds) {
        Task t = new RequestProcessor(STOPPER_THREAD_NAME, 1, true).post(new Runnable() {
            public void run() {
                try {
                    if (VERBOSE) {
                        System.out.println(STOPPER_THREAD_NAME + ": WAITING for the server process to stop");
                    }
                    if (type == LOGGER_TYPE.PROCESS) {
                        process.waitFor();
                    }
                    else {
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException ex) {
                    //noop
                }
            }
        });
        try {
            t.waitFinished(milliseconds);
        } catch (InterruptedException ex) {
            //noop
        }
        finally {
            t.cancel();
        }
    }
 
}
