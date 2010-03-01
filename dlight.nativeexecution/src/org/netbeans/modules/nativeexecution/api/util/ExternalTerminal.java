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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.nativeexecution.ExternalTerminalAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.support.TerminalProfile;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * NativeProcessBuilder can create a process that is executed in an external
 * terminal identified by ExternalTerminal object. The object can be obtained
 * with {@link ExternalTerminalProvider#getTerminal(java.lang.String)}.
 *
 * @see ExternalTerminalProvider
 */
public final class ExternalTerminal {

    private final static ConcurrentHashMap<TermEnvPair, String> execCache =
            new ConcurrentHashMap<TermEnvPair, String>();
    private final TerminalProfile profile;
    private String title = null;
    private static final boolean CLOSE_TERMINAL = Boolean.getBoolean("org.netbeans.modules.nativeexecution.api.util.CloseTerminal"); // NOI18N
    private String prompt = CLOSE_TERMINAL ? "NO" : loc("Terminal.DefaultPrompt.text"); // NOI18N

    static {
        ExternalTerminalAccessor.setDefault(new ExternalTerminalAccessorImpl());
    }

    ExternalTerminal(TerminalProfile info) throws IllegalArgumentException {
        this.profile = info;
    }

    private ExternalTerminal(ExternalTerminal terminal) {
        profile = terminal.profile;
        title = terminal.title;
        prompt = terminal.prompt;
    }

    public boolean isAvailable(ExecutionEnvironment executionEnvironment) {
        return getExecutable(executionEnvironment) != null;
    }

    /**
     * Returnes an ExternalTerminal with configured prompt message that
     * appears in terminal after command execution is finished.
     *
     * @param prompt prompt to be used in external terminal
     * @return ExternalTerminal with configured prompt message
     */
    public ExternalTerminal setPrompt(String prompt) {
        ExternalTerminal result = new ExternalTerminal(this);
        result.prompt = prompt;
        return result;
    }

    /**
     * Returnes an ExternalTerminal with configured title that
     * appears in terminal that executes a native process.
     *
     * @param title String to be displayed in a title of a terminal (if a
     *        terminal has capabilities to set a title)
     * 
     * @return ExternalTerminal with configured title
     */
    public ExternalTerminal setTitle(String title) {
        ExternalTerminal result = new ExternalTerminal(this);
        result.title = title;
        return result;
    }

    private static class ExternalTerminalAccessorImpl
            extends ExternalTerminalAccessor {

        @Override
        public TerminalProfile getTerminalProfile(ExternalTerminal terminal) {
            return terminal.profile;
        }

        @Override
        public List<String> wrapCommand(ExecutionEnvironment execEnv,
                ExternalTerminal terminal, List<String> args) {
            String exec = terminal.getExecutable(execEnv);

            if (exec == null) {
                return args;
            }

            ArrayList<String> result = new ArrayList<String>();

            result.add(exec);

            for (String arg : terminal.profile.getArguments()) {
                if ("$@".equals(arg)) { // NOI18N
                    result.addAll(args);
                    continue;
                }

                if ("$shell".equals(arg)) { // NOI18N
                    try {
                        HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                        result.add(hostInfo.getShell());
                        continue;
                    } catch (IOException ex) {
                    } catch (CancellationException ex) {
                    }
                }

                if (arg.contains("$title")) { // NOI18N
                    arg = arg.replace("$title", terminal.title); // NOI18N
                }

                result.add(arg);
            }

            return result;
        }

        @Override
        public String getPrompt(ExternalTerminal terminal) {
            return terminal.prompt;
        }

        @Override
        public String getTitle(ExternalTerminal terminal) {
            return terminal.title;
        }

        @Override
        public String getExecutable(ExternalTerminal terminal, ExecutionEnvironment execEnv) {
            return terminal.getExecutable(execEnv);
        }
    }

    private String getExecutable(ExecutionEnvironment execEnv) {
        TermEnvPair key = new TermEnvPair(execEnv, profile.getCommand());

        String exec = execCache.get(key);

        if (exec == null) {
            if (execEnv.isLocal() && Utilities.isWindows()) {
                exec = profile.getCommand();
            } else {
                exec = HostInfoUtils.searchFile(execEnv,
                        profile.getSearchPaths(),
                        profile.getCommand(), true);
            }
            if (exec != null) {
                String execPath = execCache.putIfAbsent(key, exec);

                if (execPath != null) {
                    exec = execPath;
                }
            }
        }

        return exec;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(ExternalTerminal.class, key, params);
    }

    /*package*/
    final static class TermEnvPair {

        public final ExecutionEnvironment env;
        public final String termexec;

        public TermEnvPair(ExecutionEnvironment env, String termexec) {
            this.env = env;
            this.termexec = termexec;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TermEnvPair)) {
                throw new IllegalArgumentException();
            }
            TermEnvPair that = (TermEnvPair) obj;

            return env.equals(that.env) && termexec.equals(that.termexec);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.env != null ? this.env.hashCode() : 0);
            hash = 79 * hash + (this.termexec != null ? this.termexec.hashCode() : 0);
            return hash;
        }
    }
}
