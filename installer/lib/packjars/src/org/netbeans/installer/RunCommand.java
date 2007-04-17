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

package org.netbeans.installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;


/**
 * RunCommand execute the command text and returns the
 * status of execution and output and error messages
 * associated with the execution
 */
public class RunCommand {
    private Runtime		runTime;
    protected boolean           interrupted;
    protected Process           thisProcess;
    protected String            lineSeparator;
    protected String            cmdString;
    
    private BufferedReader      inputReader = null;
    private BufferedReader      errorReader = null;
    protected OutputStream      out = null;
    private int exitValue;
    private String in;
    private String err;
    
    public RunCommand() {
        thisProcess = null;
        runTime = Runtime.getRuntime();
        interrupted = false;
        lineSeparator = System.getProperty("line.separator", "\n");
    }
    
    /**
     * Execute the command line in a separate thread
     * The command parameter is a fully qualified path
     * of the command
     * @see java.lang.Runtime.exec(String)
     */
    public void execute(String command){
        execute(command, null, null);
    }
    
    /**
     * @see java.lang.Runtime.exec(String,String[],File)
     */
    public void execute(String command, String[] envp, File dir){
        cmdString = command;
        try {
            thisProcess = runTime.exec(command, envp, dir);
            //initIOStreams();
            //System.out.println("process = " + thisProcess);
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * @see java.lang.Runtime.exec(String[])
     */
    public void execute(String[] cmdArray) {
        cmdString = "";
        for (int i = 0; i < cmdArray.length; i++) {
            if (i > 0) {
                cmdString += " ";
            }
            cmdString += cmdArray[i];
        }
        try {
            thisProcess = runTime.exec(cmdArray);
            //initIOStreams();
            //System.out.println("process = " + thisProcess);
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    /* @see java.lang.Runtime.exec(String[],String[],File)
     */
    public void execute(String[] cmdArray, String[] envp, File dir) {
        cmdString = "";
        for (int i = 0; i < cmdArray.length; i++) {
            if (i > 0) {
                cmdString += " ";
            }
            cmdString += cmdArray[i];
        }
        try {
            thisProcess = runTime.exec(cmdArray, envp, dir);
            //initIOStreams();
	} catch (Throwable t) {
            System.out.println(t.getLocalizedMessage());
            t.printStackTrace();
	}
    }
        
    /**
     * Interrupt the command line process
     */
    public void interrupt() {
        interrupted = true;
        //System.out.println("Runcommand interrupted");
        thisProcess.destroy();
        thisProcess = null;
        //System.out.println("Process destroyed thisProcess -> " + thisProcess);
    }
    
    /**
     * Gets the return status of the command line process
     */
    public int getReturnStatus () {
        if (interrupted) {
            return(-1);
        }
        if (thisProcess != null) {
            return exitValue;
        }
        
        return (-3);
    }
    
    /**
     * Gets the return status of the command line process
     */
    public int waitFor () {
        if (interrupted) {
            return(-1);
        }
        try {
            InputStream is = thisProcess.getInputStream();
            StreamAccumulator inAccumulator = new StreamAccumulator(is);
            InputStream es = thisProcess.getErrorStream();
            StreamAccumulator errAccumulator = new StreamAccumulator(es);
            OutputStream os = thisProcess.getOutputStream();
            
	    inAccumulator.start();
	    errAccumulator.start();
            
	    exitValue = thisProcess.waitFor();
	    inAccumulator.join();
	    errAccumulator.join();

	    in = inAccumulator.result();
	    err = errAccumulator.result();
            
            inputReader = new BufferedReader(new StringReader(in));
            errorReader = new BufferedReader(new StringReader(err));
            is.close();
            es.close();
            os.close();
        } catch (Throwable t) {
            System.out.println(t.getLocalizedMessage());
            t.printStackTrace();
            //System.out.println(ex.getMessage());
            return(-2);
        }
        //System.out.println("WaitFor completed: thisProcess -> " + thisProcess);
        if (thisProcess != null) {
            return exitValue;
        }
        
        return (-3);
    }
    
    /**
     * Checks whether the command line process is still running
     */
    public boolean isRunning() {
        try {
            thisProcess.exitValue();
        } catch (IllegalThreadStateException ie) {
            return(true);
        } catch (Exception ie) {
            return(false);
        }
        return(false);
    }
        
    /**
     * Gets a line from the standard output of the command line process
     */
    public String getOutputLine() {
        String ret = null;
        try{
            ret = inputReader.readLine();
            if (ret != null) {
                ret = ret + "\n";
            }
        } catch(IOException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return ret;
    }
        
    /**
     * Gets a line from the standard error of the command line process
     */
    public String getErrorLine(){
        String ret = null;
        try{
            ret = errorReader.readLine();
            if (ret != null) {
                ret = ret + "\n";
            }
        }catch(IOException ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return ret;
    }
    
    /**
     * Returns debug info about the command line process like
     * output,error & return status
     */
    public String print () {
        StringBuffer sb = new StringBuffer(1024);
        sb.append("\n");
        sb.append("------------------------------  Command Print  -----------------------\n");
        sb.append("Command: \n");
        sb.append(cmdString + "\n");
        sb.append("Command Output:\n");
        sb.append(in + "\n");
        sb.append("Command Error:\n");
        sb.append(err + "\n");
        sb.append("Return Status: " + exitValue + "\n");
        sb.append("------------------------------------------------------------------------\n");
        return sb.toString();
    }
    
    private static class StreamAccumulator extends Thread {
	private final InputStream is;
	private final StringBuffer sb = new StringBuffer();
	private Throwable throwable = null;
        
	public String result () throws Throwable {
	    if (throwable != null) {
		throw throwable;
            }
	    return sb.toString();
	}
        
	StreamAccumulator (InputStream is) {
	    this.is = is;
	}
        
	public void run() {
	    try {
		Reader r = new InputStreamReader(is);
		char[] buf = new char[4096];
		int n;
                //Method read blocks till end of stream is encountered so it ends
                //after external process ends.
                while ((n = r.read(buf)) > 0) {
                    sb.append(buf,0,n);
                }
                r.close();
	    } catch (Throwable t) {
		throwable = t;
	    }
	}
    }
}
