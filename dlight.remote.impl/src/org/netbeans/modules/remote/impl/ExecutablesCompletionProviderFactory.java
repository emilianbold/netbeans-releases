/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl;

import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.remote.api.ui.AutocompletionProvider;
import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Provides auto-completion options based on $PATH environment variable.
 * Searches for all executables in paths and matches their names for
 * autocompletion options.
 * 
 */

// DISABLED - FindBasedExecutablesCompletionProviderFactory is used instead

//@ServiceProvider(service = AutocompletionProviderFactory.class)
public class ExecutablesCompletionProviderFactory implements AutocompletionProviderFactory {

    public AutocompletionProvider newInstance(ExecutionEnvironment env) {
        try {
            return new Provider(env);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // don't report cancellatoin exception
        }

        return null;
    }

    public boolean supports(final ExecutionEnvironment env) {
        return ConnectionManager.getInstance().isConnectedTo(env) && HostInfoUtils.isHostInfoAvailable(env);
    }

    private final static class Provider implements AutocompletionProvider {

        private final static int numOfScanThreads = 2;
        private final Scanner scanner;
        private Task[] scanningTasks;

        public Provider(final ExecutionEnvironment env) throws IOException, CancellationException {
            List<String> paths = new ArrayList<>();
            HostInfo info = HostInfoUtils.getHostInfo(env);
            String pathList = info.getEnvironment().get("PATH"); // NOI18N

            for (String path : pathList.split(":")) { // NOI18N
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }

            scanner = new Scanner(env, paths);
            startScan();
        }

        public List<String> autocomplete(final String str) {
            if ("".equals(str)) { // NOI18N
                return Collections.<String>emptyList();
            }

            List<String> result = new ArrayList<>();

            for (String exec : scanner.getExecutables()) {
                if (exec.startsWith(str)) {
                    result.add(exec);
                }
            }

            return result;
        }

        public void startScan() {
            synchronized (this) {
                if (scanner != null && scanningTasks == null) {
                    scanningTasks = new Task[numOfScanThreads];
                    for (int i = 0; i < numOfScanThreads; i++) {
                        scanningTasks[i] = RequestProcessor.getDefault().post(scanner);
                    }
                }
            }
        }
    }

    private final static class Scanner implements Runnable {

        private final Iterator<String> pathsIterator;
        private final Set<String> executables = new HashSet<>();
        private final ExecutionEnvironment env;
        private volatile boolean isInterrupted;

        public Scanner(final ExecutionEnvironment env, final List<String> paths) {
            List<String> pathsCopy = new ArrayList<>(paths);
            pathsIterator = pathsCopy.iterator();
            this.env = env;
        }

        public void stop() {
            isInterrupted = true;
        }

        public void run() {
            isInterrupted = false;
            while (true) {
                if (isInterrupted()) {
                    break;
                }

                String path = null;

                synchronized (pathsIterator) {
                    if (pathsIterator.hasNext()) {
                        path = pathsIterator.next();
                    }
                }

                if (path == null) {
                    break;
                }

                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                npb.setExecutable("/bin/ls").setArguments("-1FL", path); // NOI18N

                ProcessUtils.ExitStatus result = ProcessUtils.execute(npb);
                if (result.isOK()) {
                    for (String s : result.getOutputLines()) {
                        if (s.endsWith("*")) { // NOI18N
                            synchronized (executables) {
                                executables.add(s.substring(0, s.length() - 1));
                            }
                        }
                    }
                }
            }
        }

        private boolean isInterrupted() {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ex) {
                isInterrupted = true;
                Thread.currentThread().interrupt();
            }

            isInterrupted |= Thread.currentThread().isInterrupted();
            return isInterrupted;
        }

        private String[] getExecutables() {
            synchronized (executables) {
                return executables.toArray(new String[0]);
            }
        }
    }
}
