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
package org.netbeans.xtest.plugin.jvm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Calendar;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.util.Watchdog;
import org.netbeans.xtest.util.NativeKill;

/** Watchdog used in JVMTestRunnerTask. If timeout expires, it kills JVM
 * in which tests run. PID of process is passed in file ${xtest.workdir}/jvm.pid
 * from JUnitTestRunnerLauncher class.
 */
public class JVMExecuteWatchdog extends ExecuteWatchdog {
    
    protected long timeout;
    protected Project antProject;
    
    /** Creates a new instance of IdeExecWatchdog */
    public JVMExecuteWatchdog(long timeout, Project project) {
        super(timeout);
        //super(90000); System.out.println("Got timeout "+timeout+", but using 90 seconds for testing purposes");
        if (project == null) {
            throw new IllegalArgumentException("Ant's Project is not set");
        }
        this.antProject = project;
        this.timeout = timeout;
        project.log("JVMExecWatchdog created with timeout "+timeout, Project.MSG_VERBOSE);
    }
    
    public synchronized void timeoutOccured(Watchdog w) {
        antProject.log(" XTest: Hard timeout "+timeout+"ms occured - trying to kill JVM - "+Calendar.getInstance().getTime());
        this.killJVM();
        antProject.log("XTest: JVM killed.");
        super.timeoutOccured(w);
    }
    
    /** Kill JVM process running tests with PID from ${xtest.workdir}/jvm.pid file.
     */
    public boolean killJVM() {
        String workdir = antProject.getProperty("xtest.workdir");
        if (workdir != null) {
            // create flag file indicating running tests
            File jvmPID = new File(workdir, "jvm.pid");
            if (jvmPID.exists()) {
                try {
                    LineNumberReader reader = new LineNumberReader(new FileReader(jvmPID));
                    String line = reader.readLine();
                    if (line != null) {
                        try {
                            long pid = Long.parseLong(line);
                            // xtest.home used in NativeKill class
                            System.setProperty("xtest.home", antProject.getProperty("xtest.home"));
                            antProject.log("Requesting thread dump on process with PID="+pid);
                            NativeKill.dumpProcess(pid);
                            // sleep a bit, so resources can be released
                            Thread.sleep(2000);
                            antProject.log("Killing process with PID="+pid);
                            boolean result = NativeKill.killProcess(pid);
                            // sleep a bit, so resources can be released
                            Thread.sleep(2000);
                            return result;
                        } catch (NumberFormatException nfe) {
                            antProject.log("Cannot parse PID written in the ide.flag file: "+line+" - not killing");
                        }
                    }
                } catch (IOException ioe) {
                    antProject.log("Cannot kill. IOException when reading PID from file: "+jvmPID);
                    antProject.log(ioe.toString());
                } catch (Exception e) {
                    antProject.log("Exception when trying to kill IDE");
                    antProject.log(e.toString());
                }
            } else {
                antProject.log("Cannot kill. Cannot find file containing PID: "+jvmPID);
            }
        } else {
            antProject.log("xtest.workdir property is not specified - cannot kill");
        }
        return false;
    }
}
