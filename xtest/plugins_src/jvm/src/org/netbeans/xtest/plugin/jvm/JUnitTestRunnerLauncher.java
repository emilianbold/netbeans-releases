/*
 *
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


/*
 * JUnitTestRunnerLauncher.java
 *
 * Created on October 20, 2003, 6:28 PM
 */

package org.netbeans.xtest.plugin.jvm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import org.netbeans.xtest.testrunner.JUnitTestRunner;
import org.netbeans.xtest.util.JNIKill;

/**
 *
 * @author  mb115822
 */
public class JUnitTestRunnerLauncher {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // just start the tests
        System.out.println("VM started - "+Calendar.getInstance().getTime());
        try {
            Thread hook = new Thread(new Runnable() {
                public void run() {
                    System.out.println("ERROR: JVM exiting unexpectedly!");
                    // only 1.5 - can be added when XTest drop support of 1.4
                    //threadDump();
                }
            });
            Runtime.getRuntime().addShutdownHook(hook);
            initKill();
            JUnitTestRunner testRunner = new JUnitTestRunner(null,System.out);
            testRunner.runTests();
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (Throwable t) {
            System.out.println("Error - during test run caught exception: "+t.getMessage());
            t.printStackTrace();
            System.exit(-1);
        }
        System.out.println("VM finished");
        System.exit(0);
    }
    
    /* only 1.5
    static void threadDump() {
        System.out.println("Thread dump:");
        Map<Thread,StackTraceElement[]> all = Thread.getAllStackTraces();
        for (Map.Entry<Thread,StackTraceElement[]> elem : all.entrySet()) {
            StringBuffer sb = new StringBuffer();
            sb.append("Thread: ");
            sb.append(elem.getKey().getName()).append('\n');
            for (StackTraceElement e : elem.getValue()) {
                sb.append("    ").append(e.getClassName()).append('.').append(e.getMethodName())
                    .append(':').append(e.getLineNumber()).append('\n');
            }
            System.out.println(sb);
        }
    }
     */
    
    /** Initialize JNIKill library, starts dump thread and store PID of 
     * current process into ${xtest.workdir}/jvm.pid file. If timeout expires,
     * process is killed by JVMExecuteWatchdog.
     */
    static void initKill() {
        String workdir = System.getProperty("xtest.workdir");
        if (workdir!=null) {
            File jvmPID = new File(workdir,"jvm.pid");
            PrintWriter writer = null;
            try {
                jvmPID.createNewFile();
                writer = new PrintWriter(new FileOutputStream(jvmPID));
                // get my pid
                JNIKill kill = new JNIKill();
                if (kill.startDumpThread()) {
                    System.out.println("JVM dump thread succesfully started.");
                }
                long myPID = kill.getMyPID();
                // write it out to a file
                System.out.println("JVM is running under PID:"+myPID);
                writer.println(myPID);
            } catch (Throwable e) {
                System.out.println("There was a problem: "+e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } else {
            System.out.println("Cannot get xtest.workdir property - it has to be set to a valid working directory.");
        }
    }
}
