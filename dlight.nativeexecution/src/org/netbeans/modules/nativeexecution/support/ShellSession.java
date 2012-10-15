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
package org.netbeans.modules.nativeexecution.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * This class holds a single shell session per environment to run small and
 * quick tasks.
 *
 * It is synchronized. Use with care! Failed/closed session will be restored
 * automatically when needed.
 *
 * UTF-8 charset is used for I/O.
 *
 * @author ak119685
 */
public final class ShellSession {

    private static final RequestProcessor RP = new RequestProcessor("ShellSession I/O", 10); // NOI18N
    private static final HashMap<ExecutionEnvironment, NativeProcess> processes =
            new HashMap<ExecutionEnvironment, NativeProcess>();
    private static final String csName = "UTF-8"; // NOI18N
    private static final String eop = "ShellSession.CMDDONE"; // NOI18N

    private ShellSession() {
    }

    public static void shutdown(final ExecutionEnvironment env) {
        NativeProcess process;

        synchronized (processes) {
            process = processes.put(env, null);
        }

        if (process != null) {
            ProcessUtils.destroy(process);
        }
    }

    private static NativeProcess startProcessIfNeeded(final ExecutionEnvironment env) throws IOException, CancellationException {
        NativeProcess process;

        synchronized (processes) {
            process = processes.get(env);
            if (process != null) {
                return process;
            }

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.setExecutable("/bin/sh").setArguments("-s"); // NOI18N
            npb.getEnvironment().put("LC_ALL", "C"); // NOI18N
            process = npb.call();
            if (process.getState() == State.RUNNING) {
                processes.put(env, process);
            } else {
                process = null;
            }
        }

        if (process == null) {
            throw new IOException("Failed to start shell session on " + env.getDisplayName()); // NOI18N
        }

        return process;
    }

    public static ExitStatus execute(final ExecutionEnvironment env, final String command) throws IOException, CancellationException {
        final NativeProcess process = startProcessIfNeeded(env);
        synchronized (process) {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(command).append(')'); // NOI18N
            sb.append("; echo ").append(eop).append("$?; echo ").append(eop).append(" 1>&2\n"); // NOI18N
            process.getOutputStream().write(sb.toString().getBytes(csName));
            process.getOutputStream().flush();
            final AtomicInteger rc = new AtomicInteger(-1);

            Future<String> out = RP.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    StringBuilder result = new StringBuilder();
                    ReadableByteChannel channel = Channels.newChannel(process.getInputStream());
                    BufferedReader br = new BufferedReader(Channels.newReader(channel, csName));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith(eop)) {
                            rc.set(Integer.parseInt(line.substring(eop.length())));
                            break;
                        } else {
                            result.append(line).append('\n');
                        }
                    }
                    return result.toString();
                }
            });

            Future<String> err = RP.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    StringBuilder result = new StringBuilder();
                    ReadableByteChannel channel = Channels.newChannel(process.getErrorStream());
                    BufferedReader br = new BufferedReader(Channels.newReader(channel, csName));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith(eop)) {
                            break;
                        } else {
                            result.append(line).append('\n');
                        }
                    }
                    return result.toString();
                }
            });

            String output = "", error = ""; // NOI18N

            try {
                output = out.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            try {
                error = err.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                err.cancel(true);
            } catch (CancellationException ex) {
                // ignore
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }


            return new ExitStatus(rc.get(), output, error);
        }
    }
}
