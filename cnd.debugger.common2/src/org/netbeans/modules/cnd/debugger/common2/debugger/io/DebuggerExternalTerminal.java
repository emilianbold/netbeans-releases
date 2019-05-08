/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Create, open, and manage an external terminal for a Unix debug session.
 *
 */
class DebuggerExternalTerminal {
    
    private final String tty;
    private final long pid;
    private File gdbHelperLog = null;
    private File gdbHelperScript = null;

    private final ExecutionEnvironment exEnv;

    private static final Logger log = Logger.getLogger(DebuggerExternalTerminal.class.toString());

    private static final int RETRY_LIMIT = 200;

    public DebuggerExternalTerminal(String termPath) {
        initGdbHelpers();
        
        this.exEnv = ExecutionEnvironmentFactory.getLocal();

        String termBaseName = CndPathUtilities.getBaseName(termPath);
        if (ExternalTerminalProvider.getSupportedTerminalIDs().contains(termBaseName)) {
            NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
            ExternalTerminal terminal = ExternalTerminalProvider.getTerminal(exEnv, termBaseName);
            terminal = terminal.setTitle(Catalog.get("Title_Debugger_External_Terminal")); // NOI18N
            npb.useExternalTerminal(terminal);
            npb.setExecutable(gdbHelperScript.getAbsolutePath());
            npb.redirectError();
            try {
                ProcessUtils.ignoreProcessOutput(npb.call());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

//        TerminalProfile termProfile = getTermProfile(termpath);
//        ProcessBuilder pb = new ProcessBuilder(termProfile.options);
//
//        // Set "DISPLAY" environment variable if not already set (Mac OSX only)
//        // Used only localy, so we can use Utilities.getOperatingSystem()
//        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
//            Map<String,String> map = pb.environment();
//            if (map.get("DISPLAY") == null) { // NOI18N
//                String display = ":0.0"; // NOI18N
//                for (int i = 0; i < env.length; i++) {
//                    if (env[i].startsWith("DISPLAY=") && env[i].length() >= 8) { // NOI18N
//                        display = env[i].substring(8);
//                    }
//                }
//                map.put("DISPLAY", display); // NOI18N
//            }
//        }
//
//        pb.redirectErrorStream(true);
//        Process process = pb.start();
        
        int count = 0;
        String tty_line = null;
        String pid_line = null;
        try {
            while (count++ < RETRY_LIMIT) {
                // first check for process termination
                // only if not in KDE

                // TODO: it is not good to wait for the file to be filled with info this way
                // need to find a better way to get pid later
                BufferedReader fromTerm = new BufferedReader(new FileReader(gdbHelperLog));
                tty_line = fromTerm.readLine();
                pid_line = fromTerm.readLine();
                fromTerm.close();
                if (pid_line == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                } else {
                    break;
                }
            }
            if (count >= RETRY_LIMIT) {
                log.warning("Retry limit reached for " + gdbHelperLog + ", giving up");
            }
        } catch (IOException ioe) {
            log.warning("Failed to read external terminal helper");
        }
        tty = tty_line;
        log.finest("ExternalTerminal: tty=" + tty);
        long pidTemp = 0;
        try {
            pidTemp = Long.parseLong(pid_line);
        } catch (Exception ex) {
            log.warning("Error parsing pid: " + pid_line);
        }
        pid = pidTemp;
        log.finest("ExternalTerminal: pid=" + pid);
    }
    
    private void initGdbHelpers() {
        try {
            gdbHelperLog = File.createTempFile("gdb_helper_", ".log"); // NOI18N
            gdbHelperScript = File.createTempFile("gdb_helper_", ".sh"); // NOI18N
        } catch (IOException ex) {
            gdbHelperLog = new File("/tmp/gdb_helper.log"); // NOI18N
            gdbHelperScript = new File("/tmp/gdb_helper.sh"); // NOI18N
        }
        String content = NbBundle.getMessage(DebuggerExternalTerminal.class,
                "GdbHelperScript", gdbHelperLog.getAbsolutePath()); // NOI18N
        
        gdbHelperLog.deleteOnExit();
        gdbHelperScript.deleteOnExit();
        
        try {
            FileWriter fw = new FileWriter(gdbHelperScript);
            fw.write(content);
            fw.close();
        } catch (IOException ioe) {
        }
        try {
            CommonTasksSupport.chmod(ExecutionEnvironmentFactory.getLocal(), gdbHelperScript.getAbsolutePath(), 0755, null).get(30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * Return the tty to the gdb engine. We don't have a timeout here because this is managed
     * by the general gdb startup timeout.
     */
    String getTty() {
        return tty;
    }

    void finish() {
        gdbHelperScript.delete();
        gdbHelperLog.delete();
    }
    
//    public void propertyChange(PropertyChangeEvent ev) {
//        if (ev.getPropertyName().equals(GdbDebugger.PROP_STATE)) {
//            Object state = ev.getNewValue();
//            if (state == GdbDebugger.State.EXITED) {
//                gdbHelperScript.delete();
//                gdbHelperLog.delete();
//                debugger.removePropertyChangeListener(this);
//            }
//	} else if (ev.getPropertyName().equals(GdbDebugger.PROP_KILLTERM)) {
//            if (pid == 0) {
//                log.warning("Killing zero pid detected from log: " + gdbHelperLog);
//            }
//            debugger.kill(Signal.TERM, pid);
//        }
//    }
}
