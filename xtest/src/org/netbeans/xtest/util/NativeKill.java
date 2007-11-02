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

package org.netbeans.xtest.util;

import java.io.File;
import java.io.IOException;

/**
 * NativeKill.java
 *
 * methods in this class are able to run and execute native kill utility on all platforms
 * (on windows only with supplied exe utility)
 * Created on April 16, 2002, 6:10 PM
 * 
 * @author  mb115822
 */
public class NativeKill {

    /** Creates a new instance of NativeKill */
    public NativeKill() {
    }

    private static final int SIGKILL = 9;
    private static final int SIGQUIT = 3;
    
    /** Execute given kill command.
     * @param killCommand command to execute
     * @return true if it succeeds, false otherwise
     * @throws java.io.IOException
     */
    private static boolean executeKillCommand(String killCommand) throws IOException {
        try {
            Process kill = Runtime.getRuntime().exec(killCommand);
            kill.waitFor();
            int exitValue = kill.exitValue();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ie) {
            System.out.println("InterruptedException when killing:"+ie);
        }
        return false;
    }
    
    /** Execute kill command given as array. It is needed to execute shell 
     * command with pipes on unix.
     * @param killCommand command to execute
     * @return true if it succeeds, false otherwise
     * @throws java.io.IOException
     */
    private static boolean executeKillCommand(String[] killCommand) throws IOException {
        try {
            Process kill = Runtime.getRuntime().exec(killCommand);
            kill.waitFor();
            int exitValue = kill.exitValue();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ie) {
            System.out.println("InterruptedException when killing:"+ie);
        }
        return false;
    }
    
    /** Call kill command on Unix.
     * @param pid pid of process to send signal to
     * @param signal signal number
     * @return true if it succeeds, false otherwise
     * @throws java.io.IOException
     */
    private static boolean killOnUnix(long pid, int signal) throws IOException {
        /* Workaround for issue 6624838. When fixed it is enough to have just
         * String killCommand = "kill -"+signal+" "+pid;
         * return executeKillCommand(killCommand);
         */
        String killCommand = "kill -"+signal+" "+pid;
        if(signal == SIGKILL) {
            return executeKillCommand(killCommand);
        }
        String killTreeCommand = "kill -"+signal+" `pstree -pA "+pid+" | head -n 1 | tr -d '[:alpha:]' | tr -d '+-' | tr '()' '  '`";
        String[] shellCommand = {"/bin/sh", "-c", killTreeCommand};
        if(!executeKillCommand(shellCommand)) {
            // if killtree failed do just regular kill
            System.out.println("killtree command failed: \n"+killTreeCommand);
            return executeKillCommand(killCommand);
        } else {
            return true;
        }
    }
    
    // call kill.exe pid utilitity supplied with xtest on windows
    private static boolean killOnWindows(long pid, int signal) throws IOException {
        // need to do !!!
        String xtestHome = System.getProperty("xtest.home");
        if (xtestHome != null) {
            File killFile = new File(xtestHome,"lib/kill.exe");
            //if (killFile.isFile()) {
                String killPath = killFile.getAbsolutePath();
                String killCommand = killPath+" -"+signal+" "+pid;
                return executeKillCommand(killCommand);
            //}
        } else {
            throw new IOException("xtest.home system property not set - cannot find kill distributed with XTest on windows");
        }
    }
    
    /*
     * kills process with given pid 
     */
    private static boolean killProcess(long pid, int signal) {
        try {
            if(System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
                return killOnWindows(pid, signal);
            } else {
                return killOnUnix(pid, signal);
            }
        } catch (IOException ioe) {
            System.out.println("Kill command not found on your computer");
            System.out.println(ioe);
        }
        // not supported platform - let's return just false
        // but I should evaluate throwing an Exception !!!
        return false;
        
    }   
    /*
     * kills process with given pid 
     */
    public static boolean killProcess(long pid) {
        return killProcess(pid, SIGKILL);
    }    
    
    /*
     * thread dump process with given pid 
     */
    public static boolean dumpProcess(long pid) {
        return killProcess(pid, SIGQUIT);
    }   

}
