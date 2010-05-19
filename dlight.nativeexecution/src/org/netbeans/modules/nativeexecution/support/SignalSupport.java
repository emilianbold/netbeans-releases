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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;

/**
 *
 * @author Andrew
 */
public final class SignalSupport {

    private static WeakHashMap<ExecutionEnvironment, SignalSupport> cache =
            new WeakHashMap<ExecutionEnvironment, SignalSupport>();
    private NativeProcessBuilder npb;
    private String[] args = new String[2];
    private boolean useShell;
    private int last_pid = -1;
    private Signal last_signal = null;

    public static synchronized SignalSupport getSignalSupportFor(ExecutionEnvironment execEnv) throws IOException {
        if (!HostInfoUtils.isHostInfoAvailable(execEnv)) {
            throw new IOException("Host info must be available at this point"); // NOI18N
        }

        SignalSupport result = cache.get(execEnv);
        if (result == null) {
            result = new SignalSupport();
            result.init(execEnv);
            cache.put(execEnv, result);
        }

        return result;
    }

    private SignalSupport() {
    }

    private void init(ExecutionEnvironment execEnv) throws IOException {
        String command = null;
        boolean _useShell = false;

        HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
        String shell = hostInfo.getShell();

        if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
            File killFile = new File(new File(shell).getParentFile(), "kill.exe"); // NOI18N
            if (killFile.exists()) {
                command = killFile.getAbsolutePath();
            } else {
                // Msys has no kill.exe - will use shell
                command = shell;
                args[0] = "-c"; // NOI18N
                _useShell = true;
            }
        } else {
            command = "/bin/kill"; // NOI18N
        }

        useShell = _useShell;

        if (command != null) {
            npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(command);
        } else {
            npb = null;
        }
    }

    public synchronized int kill(Signal signal, int pid) {
        if (npb == null) {
            return -1;
        }

        if (last_pid != pid || last_signal != signal) {
            last_pid = pid;
            last_signal = signal;

            if (useShell) {
                args[1] = "kill -" + (signal == Signal.NULL ? "0" // NOI18N
                        : signal.name().substring(3)) + " " + pid; // NOI18N
            } else {
                args[0] = signal == Signal.NULL ? "-0" // NOI18N
                        : "-" + signal.name().substring(3); // NOI18N
                args[1] = Integer.toString(pid);
            }

            npb.setArguments(args);
        }

        int result = -1;

        try {
            Process p = npb.call();
            result = p.waitFor();
        } catch (Exception ex) {
        }

        return result;
    }
}
