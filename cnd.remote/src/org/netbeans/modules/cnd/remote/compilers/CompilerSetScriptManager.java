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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.compilers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.remote.support.RemoteConnectionSupport;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainScriptGenerator;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;

/**
 * Manage the getCompilerSets script.
 *
 */
/*package-local*/ class CompilerSetScriptManager extends RemoteConnectionSupport {

    private final List<String> compilerSets;
    private int nextSet;
    private String platform;
    private Process process;

    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N

    public CompilerSetScriptManager(ExecutionEnvironment env) {
        super(env);
        compilerSets = new ArrayList<>();
    }

    public boolean cancel() {
        Process aProcess = process;
        if (aProcess != null) {
            aProcess.destroy();
            return true;
        }
        return false;
    }

    public void runScript() {
        if (isConnected() && !isFailedOrCancelled()) {
            nextSet = 0;
            compilerSets.clear();
            try {
                NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
                HostInfo hinfo = HostInfoUtils.getHostInfo(executionEnvironment);
                pb.setExecutable(hinfo.getShell()).setArguments("-s"); // NOI18N
                process = pb.call();

                long time = System.currentTimeMillis();
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[{0}] #HostSetup: CompilerSetManager generating script started", //NOI18N
                            new Object[]{System.currentTimeMillis()});
                }
                final String script = ToolchainScriptGenerator.generateScript(null, hinfo);
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[{0}] #HostSetup: CompilerSetManager generating script finished in {1} ms", //NOI18N
                            new Object[]{System.currentTimeMillis(), System.currentTimeMillis() - time});
                }

                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[{0}] #HostSetup: CompilerSetManager feeding script started", //NOI18N
                            new Object[]{System.currentTimeMillis()});
                }
                time = System.currentTimeMillis();
                process.getOutputStream().write(script.getBytes("UTF-8")); //NOI18N
                process.getOutputStream().close();
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[{0}] #HostSetup: CompilerSetManager feeding script finished in {1} ms", //NOI18N
                            new Object[]{System.currentTimeMillis(), System.currentTimeMillis() - time});
                }

                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[{0}] #HostSetup: CompilerSetManager reading response started", //NOI18N
                            new Object[]{System.currentTimeMillis()});
                }
                time = System.currentTimeMillis();
                Future<List<String>> err = ProcessUtils.readProcessErrorAsync(process);
                List<String> lines = ProcessUtils.readProcessOutput(process);
                int status = -1;
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "[{0}] #HostSetup: CompilerSetManager reading response finished in {1} ms", //NOI18N
                            new Object[]{System.currentTimeMillis(), System.currentTimeMillis() - time});
                }

                try {
                    status = process.waitFor();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (status != 0) {
                    RemoteUtil.LOGGER.log(Level.WARNING, "CSSM.runScript: FAILURE {0}", status); // NOI18N
                    try {
                        for (String e : err.get()) {
                            RemoteUtil.LOGGER.log(Level.ALL, "ERROR: {0}", e); // NOI18N
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                    }
                } else {
                    int i = 0;
                    for (String s: lines) {
                        RemoteUtil.LOGGER.log(Level.FINE, "CSSM.runScript line: {0}", s); // NOI18N
                        if (i == 0) {
                            platform = s;
                            RemoteUtil.LOGGER.log(Level.FINE, "    platform [{0}]", platform); // NOI18N
                        } else {
                            RemoteUtil.LOGGER.log(Level.FINE, "    line [{0}]", s); // NOI18N
                            compilerSets.add(s);
                        }
                        i++;
                    }
                }
            } catch (CancellationException  ex) {
                setFailed(ex.getMessage()); // TODO:CancellationException error processing
            } catch (IOException ex) {
                RemoteUtil.LOGGER.log(Level.WARNING, "CSSM.runScript: IOException [{0}]", ex.getMessage()); // NOI18N
                setFailed(ex.getMessage());
            } finally {
                process = null;
            }
        }
    }

    public String getPlatform() {
        return platform;
    }

    public boolean hasMoreCompilerSets() {
        return nextSet < compilerSets.size();
    }

    public String getNextCompilerSetData() {
        return compilerSets.get(nextSet++);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String set : compilerSets) {
            buf.append(set).append('\n'); // NOI18N
        }
        return buf.toString();
    }

}
