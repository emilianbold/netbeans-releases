/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import jdk.jshell.EnhancedJShell;
import jdk.jshell.JDIRemoteAgent;
import jdk.jshell.RemoteJShellService;
import jdk.jshell.JShell;
import jdk.jshell.JShellAccessor;
import jdk.jshell.NbExecutionControl;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class JShellLauncher extends InternalJShell {

    private String prefix = "";
    
    private RemoteJShellService execEnv = null;

    /**
     * 
     * @param cmdout command output
     * @param cmderr command error
     * @param userin user input to the JShell to the JShell VM
     * @param userout user output from the JShell VM
     * @param usererr  user error from the JShell VM
     */
    public JShellLauncher(PrintStream cmdout, PrintStream cmderr, InputStream userin, PrintStream userout, PrintStream usererr, RemoteJShellService execEnv) {
        super(cmdout, cmderr, userin, userout, usererr);
        this.execEnv = execEnv;
    }

    
    protected String prompt(boolean continuation) {
        int index = state.snippets().size() + 1;
        if (continuation) {
            return ">> "; // NOI18N 
        } else if (feedback() == Feedback.Concise) {
            return "[" + index + "] -> "; // NOI18N 
        } else {
            return "\n[" + index + "] -> "; // NOI18N 
        }
    }

    public void start() {
        fluff("Welcome to the JShell NetBeans integration"); // NOI18N 
        fluff("Type /help for help"); // NOI18N 
        ensureLive();
        cmdout.append(prompt(false));
    }
    
    public void stop() {
        closeState();
    }
    
    public void evaluate(String command) throws IOException {
        ensureLive();
        String trimmed = trimEnd(command);
        if (!trimmed.isEmpty()) {
            prefix = process(prefix, command);
        }
//        cmdout.append(prompt(!prefix.isEmpty()));
    }
    
    public List<String> completion(String command) {
        return completions(prefix, command);
    }

    private void ensureLive() {
        if (!live) {
            resetState();
            live = true;
        }
    }

    public JShell getJShell() {
        ensureLive();
        return state;
    }

    @Override
    protected void setupState() {
        printSystemInfo();
    }
    
    

    @Override
    protected JShell createJShellInstance() {
        if (execEnv == null) {
            execEnv = new JDIRemoteAgent(this::decorateLaunchArgs);
        }
        ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            JShell ret = new EnhancedJShell(createJShell(), execEnv);
            return ret;
        } finally {
            Thread.currentThread().setContextClassLoader(ctxLoader);
        }
    }
    
    private String decorateLaunchArgs(String s) {
        return "-classpath " + classpath; // NOI18N
    }
    
    @NbBundle.Messages({
        "MSG_SystemInformation=System Information:",
        "# {0} - java machine name",
        "# {1} - java version",
        "MSG_JavaVersion=    Java version: {0}, version {1}",
        "MSG_Classpath=    Classpath:",
        "MSG_VersionUnknown=<unknown>",
        "MSG_MachineUnknown=<unknown>",
    })
    private void printSystemInfo() {
        NbExecutionControl ctrl = JShellAccessor.getNbExecControl(state);
        Map<String, String> versionInfo = ctrl.commandVersionInfo();
        
        if (versionInfo.isEmpty()) {
            // some error ?
            return;
        }
        fluff(""); // newline
        fluff(Bundle.MSG_SystemInformation());
        String javaName = versionInfo.getOrDefault("java.vm.name", Bundle.MSG_MachineUnknown()); // NOI18N
        String javaVersion = versionInfo.getOrDefault("java.vm.version", Bundle.MSG_VersionUnknown()); // NOI18N
        fluff(Bundle.MSG_JavaVersion(javaName, javaVersion));
        
        String cpString = versionInfo.get("nb.class.path"); // NOI18N
        String[] cpItems = cpString.split(":"); // NOI18N
        if (cpItems.length > 0) {
            fluff("Classpath:");
            for (String item : cpItems) {
                if (item.isEmpty()) {
                    continue;
                }
                fluff("\t%s", item);
            }
        }
        fluff(""); // newline
    }

    private String classpath;

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }
}
