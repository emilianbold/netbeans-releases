/*
 * Copyright ? 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * RunCommand executes the command in a shell 
 * It allows to execute the command line and returns the
 * status of execution and output and error messages
 * associated with the execution
 *
 * @author Winston
 * @author cao
 */
public class RunCommand {
    private Runtime runTime;
    private Process thisProcess;
    private String cmdString;
    
    /** Creates new RunCommand */
    public RunCommand() {
        thisProcess = null;
        runTime = Runtime.getRuntime();
    }
    
    /**
     * Execute the command line in a separate process
     * The command parameter is a fully qualified path
     * of the command
     */
    public void execute(String command) throws java.io.IOException{
        cmdString = command;
        
        thisProcess = runTime.exec(command);
        
        // Gobble up the input and error steam.
        StreamGobbler inputStream = new StreamGobbler("stdin", thisProcess.getInputStream());
        StreamGobbler errorStream = new StreamGobbler("stderr", thisProcess.getErrorStream());
        inputStream.start();
        errorStream.start();
    }
    
    /**
     * Execute the command line in a separate process
     *
     */
    public void execute(String[] cmdArray) throws java.io.IOException{
        for( int i = 0; i < cmdArray.length; i ++ ) {
            cmdString += cmdArray[i] + " ";
        }
        
        thisProcess = runTime.exec(cmdArray);
        
        // Gobble up the input and error steam.
        StreamGobbler inputStream = new StreamGobbler("stdin", thisProcess.getInputStream());
        StreamGobbler errorStream = new StreamGobbler("stderr", thisProcess.getErrorStream());
        inputStream.start();
        errorStream.start();
        
    }
    
    /**
     * Gets the return status of the command line process
     */
    public int getReturnStatus() {
        try {
            thisProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return(-2);
        }
        return(thisProcess.exitValue());
    }
    
    /**
     * Checks whether the command line process is still running
     */
    public boolean isRunning() {
        try {
            int ev = thisProcess.exitValue();
        } catch (IllegalThreadStateException ie) {
            return(true);
        } catch (Exception ie) {
            return(false);
        }
        return(false);
    }
    

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
                    // System.err.println("[" + name + "] " + s);
                }

            } catch (Exception ex) {
                System.err.println("Problem reading stream " + name + "... :" + ex);
                ex.printStackTrace();
            }
            finally {
                try {
                    if (is != null ) is.close() ;
                }
                catch(Exception ee) {
                    // must be closed anyway.
                }
            }
        }
    }

}
