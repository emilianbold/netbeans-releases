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
package org.netbeans.modules.nativeexecution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.PtyUtility;

/**
 *
 * @author ak119685
 */
public final class PtyNativeProcess extends AbstractNativeProcess {

    private static final Boolean fixEraseKeyInTerminal = Boolean.valueOf(System.getProperty("fixEraseKeyInTerminal", "true")); // NOI18N;
    private String tty;
    private AbstractNativeProcess delegate = null;

    public PtyNativeProcess(final NativeProcessInfo info) {
        super(new NativeProcessInfo(info, true));
    }

    public String getTTY() {
        return tty;
    }

    @Override
    protected void create() throws Throwable {
        ExecutionEnvironment env = info.getExecutionEnvironment();
        Pty pty = info.getPty();

        List<String> newArgs = new ArrayList<String>();

        if (pty != null) {
            newArgs.add("-p"); // NOI18N
            newArgs.add(pty.getSlaveName());
        }

        if (fixEraseKeyInTerminal) {
            newArgs.add("--set-erase-key"); // NOI18N
        }

        final MacroMap envMap = info.getEnvironment();

        // We don't want pty to be affected by passed environment.
        // So at least defend ourselfs from LD_PRELOAD
        final Map<String, String> removedEntries = new HashMap<String, String>();
        removedEntries.put("LD_PRELOAD", envMap.remove("LD_PRELOAD")); // NOI18N
        removedEntries.put("LD_PRELOAD_32", envMap.remove("LD_PRELOAD_32")); // NOI18N
        removedEntries.put("LD_PRELOAD_64", envMap.remove("LD_PRELOAD_64")); // NOI18N
        removedEntries.put("DYLD_INSERT_LIBRARIES", envMap.remove("DYLD_INSERT_LIBRARIES")); // NOI18N

        Iterator<Entry<String, String>> it = removedEntries.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            if (entry.getValue() == null) {
                it.remove();
                continue;
            }

            if (!entry.getValue().isEmpty()) {
                newArgs.add("--env"); // NOI18N
                newArgs.add(entry.getKey().trim() + "=" + entry.getValue().trim()); // NOI18N
            }
        }

        String origCommand = info.getCommandLineForShell();

        if (origCommand != null) {
            newArgs.add(hostInfo.getShell());
            newArgs.add("-c"); // NOI18N
            newArgs.add("exec " + origCommand); // NOI18N
        } else {
            // this means that there is no shell available
            String processExecutable = info.getExecutable();

            if (hostInfo.getOSFamily() == OSFamily.WINDOWS) {
                // pty requires Unix style executable path
                processExecutable = WindowsSupport.getInstance().convertToShellPath(processExecutable);
            }

            newArgs.add(processExecutable);
            newArgs.addAll(info.getArguments());
        }

        info.setCommandLine(null);
        info.setExecutable(PtyUtility.getInstance().getPath(env));
        info.setArguments(newArgs.toArray(new String[newArgs.size()]));

        // no need to preload unbuffer in case of running in internal terminal
        info.setUnbuffer(false);

        NativeProcessInfo delegateInfo = new NativeProcessInfo(info, false);

        if (env.isLocal()) {
            delegate = new LocalNativeProcess(delegateInfo);
        } else {
            delegate = new RemoteNativeProcess(delegateInfo);
        }

        delegate.createAndStart();
        
        InputStream inputStream = delegate.getInputStream();

        if (pty != null) {
            setInputStream(pty.getInputStream());
            setOutputStream(pty.getOutputStream());
        } else {
            setInputStream(inputStream);
            setOutputStream(delegate.getOutputStream());
        }

        setErrorStream(delegate.getErrorStream());

        String pidLine = null;
        String ttyLine = null;
        String line;

        while ((line = readLine(inputStream)) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                break;
            }

            if (line.startsWith("PID=")) { // NOI18N
                pidLine = line.substring(4);
            } else if (line.startsWith("TTY=")) { // NOI18N
                ttyLine = line.substring(4);
            }
        }

        if (pidLine == null || ttyLine == null) {
            String error = ProcessUtils.readProcessErrorLine(this);
            throw new IOException("Unable to start pty process: " + error); // NOI18N
        }

        tty = ttyLine;

        ByteArrayInputStream bis = new ByteArrayInputStream(pidLine.getBytes());
        readPID(bis);
    }

    @Override
    protected int waitResult() throws InterruptedException {
        if (delegate == null) {
            return 1;
        }

        int result = delegate.waitResult();

        return result;
    }

    private String readLine(final InputStream is) throws IOException {
        int c;
        StringBuilder sb = new StringBuilder(20);

        while (!isInterrupted()) {
            c = is.read();

            if (c < 0 || c == '\n') {
                break;
            }

            sb.append((char) c);
        }

        return sb.toString().trim();
    }
}
