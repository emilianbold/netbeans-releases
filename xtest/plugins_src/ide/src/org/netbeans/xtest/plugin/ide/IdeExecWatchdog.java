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

/*
 * IdeExecWatchdog.java
 *
 * Created on April 18, 2002, 4:46 PM
 */

package org.netbeans.xtest.plugin.ide;

import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.BuildException;
import java.lang.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.util.*;
import java.io.*;

// native kill
import org.netbeans.xtest.util.NativeKill;
// screen shot utility
import org.netbeans.xtest.util.PNGEncoder;

/**
 *
 * @author  mb115822
 */
public class IdeExecWatchdog extends ExecuteWatchdog {


    protected Process ideProcess;
    protected long ideTimeout;
    protected boolean ideWatch = false;
    protected boolean ideKilled = false;
    protected Exception ideCaught = null;
    protected Project antProject;
    
    protected String ideUserdir = null;
    
    /** Creates a new instance of IdeExecWatchdog */
    public IdeExecWatchdog(long timeout, Project project) {
        super(timeout);
        //super(90000);
        //System.out.println("Got timeout "+timeout+", but using 90 seconds for testing purposes");
        if (project == null) throw new IllegalArgumentException("Ant's Project is not set");
        antProject = project;
        ideTimeout = timeout;        
        project.log("IdeExecWatchdog created with timeout "+ideTimeout, Project.MSG_VERBOSE);
    }
    
    // get the netbean's user dir
    public void setIdeUserdir(String userdir) {
        antProject.log("ide watchdog: ide userdir was set to:"+userdir, Project.MSG_VERBOSE);
        this.ideUserdir = userdir;
    }
    
    public synchronized void start(Process process) {                        
        super.start(process);   
        antProject.log("IdeExecWatchdog started", Project.MSG_VERBOSE);
        ideProcess = process;
        ideWatch = true;
        ideKilled = false;       
    }
    
    public void timeoutOccured(Watchdog w) {
        antProject.log("XTest: Hard timeout occured - trying to kill IDE");
        try {
            // Capture screen to IDE userdir. It is copied to results in target 
            // move-ide-userdir of plugins_src/base/lib/basic_results_processor_targets.
            PNGEncoder.captureScreenToIdeUserdir(ideUserdir,"screenshot-kill.png");
        } catch (Throwable captureException) {
            // we have a problem when capturing
            // write it out
            antProject.log("XTest: Exception thrown when capturing IDE screenshot: "+captureException.getMessage());
        }
        // kill ide
        ideKilled = true;
        this.killIde();
        ideProcess.destroy();
        antProject.log("XTest: IDE killed.");
        //System.out.println("process destroyed");
    }
    
    /**
     * Watches the ide process and terminates it, if it runs for to long.
    */
    
/*    public synchronized void run() {
        try {
            //antProject.log("Watchdog running");
            System.out.println("WG running");
            // This isn't a Task, don't have a Project object to log.
            // project.log("ExecuteWatchdog: timeout = "+timeout+" msec",  Project.MSG_VERBOSE);
            final long until = System.currentTimeMillis() + ideTimeout;
            long now;
            while (ideWatch && until > (now = System.currentTimeMillis())) {
                try {
                    //System.out.println("ExecuteWatchdog(): waiting: for "+(until-now));
                    wait(until - now);
                } catch (InterruptedException e) {}
            }

            System.out.println("WG finished waiting");
            // if we are here, either someone stopped the watchdog,
            // we are on timeout and the process must be killed, or
            // we are on timeout and the process has already stopped.
            try {                
                // We must check if the process was not stopped
                // before being here
                //System.out.println("ExecuteWatchdog(): is there any exit value()?");                
                int ev = ideProcess.exitValue();
                //System.out.println("ExecuteWatchdog(): is there any exit value()? - yes:" + ev);
                // IDE was terminated correctly - let's delete the ide.pid file
                boolean result = deletePIDFile();
                if (result==false) {
                    antProject.log("cannot delete file containing PID of running IDE (${xtest.workdir}/ide.pid)");
                }
            } catch (IllegalThreadStateException e){
                // the process is not terminated, if this is really
                // a timeout and not a manual stop then kill it.
                //System.out.println("ExecuteWatchdog(): is there any exit value() - no ITSE!!!");                
                e.printStackTrace();
                //System.out.println("ExecuteWatchdog(): ideWatch?"+ideWatch);                
                if (ideWatch){
                    // capture screen shot
                    antProject.log("XTest: Trying to kill IDE");
                    try {
                        PNGEncoder.captureScreenToIdeUserdir(ideUserdir,"screenshot-kill.png");                        
                    } catch (Exception captureException) {
                        // we have a problem when capturing
                        // write it out
                        antProject.log("Exception thrown when capturing IDE screenshot: "+captureException.getMessage());
                    }
                    // kill ide                    
                    ideKilled = true;
                    this.killIde();
                    ideProcess.destroy();
                    
                    //System.out.println("process destroyed");
                }
            }
        } catch(Exception e) {
            ideCaught = e;
            System.out.println("ExecuteWatchdog: EXCEPTION!!");
            e.printStackTrace();
        } finally {
            cleanUp();
        }
    }
  */  
    
    public synchronized void stop() {
        antProject.log("IdeExecWatchdog stop", Project.MSG_VERBOSE);
        ideWatch = false;
        super.stop();
    }
    
    protected void cleanUp() {
        super.cleanUp();
        ideWatch = false;
        ideProcess = null;        
    }
    
    public void checkException() throws BuildException {
        if (ideCaught != null) {
            throw new BuildException("Exception in IdeExecuteWatchdog.run: "
                                     + ideCaught.getMessage(), ideCaught);
        }
    }
    
    public boolean isWatching(){
        return ideWatch;
    }

    /**
     * Indicates whether the last process run was killed on timeout or not.
     * @return  <tt>true</tt> if the process was killed otherwise <tt>false</tt>.
     */
    public boolean killedProcess(){
        return ideKilled;
    }
    
    public boolean deletePIDFile() {
        String workdir = antProject.getProperty("xtest.workdir");
        if (workdir!=null) {
             File ideRunning = new File(workdir,"ide.pid");
             return ideRunning.delete();
        } else {                    
            return false;
        }
    }
    
    // kill ide 
    public boolean killIde() {
        String workdir = antProject.getProperty("xtest.workdir");
        System.setProperty("xtest.home",antProject.getProperty("xtest.home"));
        
        if (workdir!=null) {
            // create flag file indicating running tests
            File ideRunning = new File(workdir,"ide.pid");
            if (ideRunning.exists()) {
                try {
                    LineNumberReader reader = new LineNumberReader(new FileReader(ideRunning));
                    String line = reader.readLine();
                    if (line != null) {
                        try {
                            long pid = Long.parseLong(line);                            
                            // so  let's do it via external utility
                            antProject.log("requesting thread dump on process with PID="+pid);
                            NativeKill.dumpProcess(pid);                                                        
                            // sleep a bit, so resources can be released
                            Thread.sleep(2000);
                            // so  let's do it via external utility
                            antProject.log("killing process with PID="+pid);
                            boolean result = NativeKill.killProcess(pid);                                                        
                            // sleep a bit, so resources can be released
                            Thread.sleep(2000);
                            return result;
                        } catch (NumberFormatException nfe) {
                            antProject.log("cannot parse PID written in the ide.flag file: "+line+" - not killing");
                        }
                    }
                } catch (IOException ioe) {
                    antProject.log("IOException when reading PID from ide.flag file - cannot kill");
                    antProject.log(ioe.toString());
                } catch (Exception e) {
                    antProject.log("Exception when trying to kill IDE");
                    antProject.log(e.toString());
                }
            } else {
                antProject.log("cannot find file containing PID of running IDE (${xtest.workdir}/ide.pid) - canot kill");
            }
        } else {
            antProject.log("xtest.workdir property is not specified - cannot kill");
        }
        return false;
    }
    
}
