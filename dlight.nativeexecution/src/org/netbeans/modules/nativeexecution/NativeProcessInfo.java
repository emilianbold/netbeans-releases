/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.support.CaseInsensitiveMacroMap;
import org.netbeans.modules.nativeexecution.support.MacroMap;
import org.openide.util.Utilities;

/**
 *
 */
// @NotThreadSafe
// This class is always used in a thread-safe manner ...
public final class NativeProcessInfo {

    public final MacroExpander macroExpander;
    private final ExecutionEnvironment execEnv;
    private final boolean isWindows;
    private final MacroMap envVariables;
    private final List<String> arguments = new ArrayList<String>();
    private String executable;
    private String commandLine;
    private String workingDirectory;
    private boolean unbuffer;
    private boolean redirectError;
    private boolean x11forwarding;
    private Collection<ChangeListener> listeners = null;

    public NativeProcessInfo(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        this.executable = null;
        this.unbuffer = false;
        this.workingDirectory = null;
        this.macroExpander = MacroExpanderFactory.getExpander(execEnv);

        if (execEnv.isLocal() && Utilities.isWindows()) {
            envVariables = new CaseInsensitiveMacroMap(macroExpander);
            isWindows = true;
        } else {
            envVariables = new MacroMap(macroExpander);
            isWindows = false;
        }

        redirectError = false;
    }

    public void addNativeProcessListener(ChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ChangeListener>();
        }

        listeners.add(listener);
    }

    public void redirectError(boolean redirectError) {
        this.redirectError = redirectError;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setUnbuffer(boolean unbuffer) {
        this.unbuffer = unbuffer;
    }

    public boolean isUnbuffer() {
        return unbuffer;
    }

    public void setX11Forwarding(boolean x11forwarding) {
        this.x11forwarding = x11forwarding;
    }

    public boolean getX11Forwarding() {
        return x11forwarding;
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addEnvironmentVariable(final String name, final String value) {
        envVariables.put(name, value);
    }

    public void addEnvironmentVariables(Map<String, String> envs) {
        for (String key : envs.keySet()) {
            addEnvironmentVariable(key, envs.get(key));
        }
    }

    public void setArguments(String... arguments) {
        if (commandLine != null) {
            throw new IllegalStateException("commandLine is already defined. No additional parameters can be set"); // NOI18N
        }

        this.arguments.clear();
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public List<String> getCommand() {
        String cmd;

        try {
            cmd = macroExpander.expandPredefinedMacros(executable);
        } catch (ParseException ex) {
            cmd = executable;
        }

        List<String> result = new ArrayList<String>();
        result.add(cmd);

        if (!arguments.isEmpty()) {
            result.addAll(arguments);
        }

        return result;
    }

    public List<String> getCommandListForShell() {
        List<String> result = new ArrayList<String>();
        List<String> cmd = getCommand();

        if (isWindows) {
            String exec = WindowsSupport.getInstance().convertToShellPath(cmd.get(0));
            cmd.set(0, exec);
        }

        boolean first = true;

        for (String s : cmd) {
            if (first) {
                result.add(quoteExecutable(s));
                first = false;
            } else {
                result.add(quote(s));
            }
        }

        return result;
    }

    private String quoteExecutable(String orig) {
        StringBuilder sb = new StringBuilder();
        String escapeChars = (isWindows) ? " \"'()" : " \"'()!"; // NOI18N

        for (char c : orig.toCharArray()) {
            if (escapeChars.indexOf(c) >= 0) { // NOI18N
                sb.append('\\');
            }
            sb.append(c);
        }

        return sb.toString();
    }

    private String quote(String orig) {
        String quote = "'"; // NOI18N

        if (isWindows) {
            // On Windows when ExternalTerminal is used and we have "$" in
            // parameters we get it expanded by shell (which is not the same
            // behavior as we have in case of use of OutputWindow or when we
            // are not on Windows)... So do the following replacement..
            orig = orig.replaceAll("\\$", "\\\\\\$"); // NOI18N
        }

        if (orig.indexOf('\'') >= 0) {
            quote = (isWindows) ? "\\\"" : "\""; // NOI18N
        }

        return quote + orig + quote;
    }

    public String getCommandLineForShell() {
        if (commandLine != null) {
            return commandLine;
        }

        StringBuilder sb = new StringBuilder();

        for (String s : getCommandListForShell()) {
            sb.append(s).append(' ');
        }

        if (redirectError) {
            sb.append("2>&1"); // NOI18N
        }

        return sb.toString().trim();
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    public Collection<ChangeListener> getListeners() {
        return listeners;
    }

    public String getWorkingDirectory(boolean expandMacros) {
        String result;

        if (expandMacros && macroExpander != null) {
            try {
                result = macroExpander.expandPredefinedMacros(workingDirectory);
            } catch (ParseException ex) {
                result = workingDirectory;
            }
        }

        result = workingDirectory;

        return result;
    }

    public MacroMap getEnvVariables() {
        return getEnvVariables(null);
    }

    public MacroMap getEnvVariables(Map<String, String> prependMap) {
        // TODO: is there a need of prepending prependMap ?
        // there was some implementation in one of previous version...
        return envVariables;
    }
}
