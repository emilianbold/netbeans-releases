/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
