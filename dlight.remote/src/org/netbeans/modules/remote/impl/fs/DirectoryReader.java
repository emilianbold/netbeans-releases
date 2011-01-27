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

package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.impl.fs.DirectoryStorage.Entry;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.util.RequestProcessor;

/**
 * Responsible for reading a directory.
 * @author Vladimir Kvashin
 */
public class DirectoryReader {

    private final ExecutionEnvironment execEnv;
    private final String remoteDirectory;
    private final List<DirectoryStorage.Entry> entries = new ArrayList<DirectoryStorage.Entry>();
    private final AtomicReference<IOException> criticalException = new AtomicReference<IOException>();
    private final AtomicReference<IOException> nonCriticalException = new AtomicReference<IOException>();
    private final StringBuilder errorOutput = new StringBuilder();
    private LsLineParser lineParser;

    private final CountDownLatch latch = new CountDownLatch(2);

    private static final RequestProcessor RP = new RequestProcessor("Reading remote directory", 32); //NOI18N

    private static class LsLineParser {

        private final int timestampWordCount;
        private final String remoteDir;

        public LsLineParser(int timestampWordCount, String remoteDir) {
            this.timestampWordCount = timestampWordCount;
            this.remoteDir = remoteDir;
        }

        public DirectoryStorage.Entry parseLine(String line) {
//            StringBuilder curr = new StringBuilder();
//            for (int i = 0; i < line.length(); i++) {
//                char c = line.charAt(i);
//            }
            String[] words = line.split(" +"); //NOI18N
            if (words.length < 6 + timestampWordCount) {
                RemoteLogger.getInstance().log(Level.INFO, "Unexpected ls output format for {0}: {0}", new Object[] { remoteDir, line });
                return null;
            }
            StringBuilder timestamp = new StringBuilder();
            for (int i = 5; i < 5 + timestampWordCount; i++) {
                if (timestamp.length() > 0) {
                    timestamp.append(' ');
                }
                timestamp.append(words[i]);

            }
            long size;
            try {
                if (words[4].endsWith(",")) { //NOI18N
                    words[4] = words[4].substring(0, words[4].length()-1); // in /dev size ends with comma
                }
                size = Long.parseLong(words[4]);
            } catch (NumberFormatException e) {
                RemoteLogger.getInstance().log(Level.INFO, "Unexpected ls output format (size) for {0}: {0}", new Object[] { remoteDir, line });
                size = 1024;
            }
            StringBuilder name = new StringBuilder();
            StringBuilder link = new StringBuilder();
            StringBuilder curr = name;
            for (int i = 5 + timestampWordCount; i < words.length; i++) {
                String word = words[i];
                if ("->".equals(word)) { // NOI18N
                    curr = link;
                } else {
                    if (curr.length() > 0) {
                        curr.append(' '); // NOI18N
                    }
                    curr.append(word);
                }
            }

            DirectoryStorage.Entry result = new DirectoryStorage.Entry(
                    name.toString(), null, words[0], words[2],
                    words[3], size, timestamp.toString(),
                    (link.length() == 0) ? null : link.toString());

            return result;
        }
    }

    private class StdOutReader implements Runnable {

        private final BufferedReader reader;

        public StdOutReader(InputStream stream, boolean isRemote) {
            reader = ProcessUtils.getReader(stream, isRemote);
        }

        @Override
        public void run() {
            try {
                boolean first = true;
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line != null) {
                            if (first) {
                                first = false;
                            } else { // 1-st line is "Total NNN"
                                DirectoryStorage.Entry entry = lineParser.parseLine(line);
                                if (entry != null) {
                                    entries.add(entry);
                                }
                            }
                        } else {
                            break;
                        }
                    } catch (IOException ex) {
                        criticalException.set(ex);
                    }

                }
            } finally {
                latch.countDown();
            }
        }
    }

    private class StdErrReader implements Runnable {

        private final BufferedReader reader;

        public StdErrReader(InputStream stream, boolean isRemote) {
            reader = ProcessUtils.getReader(stream, isRemote);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line != null) {
                            errorOutput.append(line).append('\n');
                        } else {
                            break;
                        }
                    } catch (IOException ex) {
                        nonCriticalException.set(ex);
                    }

                }
            } finally {
                latch.countDown();
            }
        }
    }


    public DirectoryReader(ExecutionEnvironment execEnv, String remoteDirectory) {
        this.execEnv = execEnv;
        this.remoteDirectory = remoteDirectory.endsWith("/") ? remoteDirectory : remoteDirectory + "/"; //NOI18N
    }

    public final void readDirectory() throws IOException, InterruptedException, CancellationException {
        RemoteLogger.assertTrue(ConnectionManager.getInstance().isConnectedTo(execEnv), execEnv.getDisplayName() + " shoud"); //NOI18N
        HostInfo.OSFamily oSFamily = HostInfoUtils.getHostInfo(execEnv).getOSFamily();
        lineParser = createLsLineParser(oSFamily, remoteDirectory);
        String option = getFullTimeLsOption(oSFamily);
        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(execEnv);
        pb.setExecutable("/bin/ls"); //NOI18N
        pb.setArguments(option, "-A", remoteDirectory); //NOI18N
        NativeProcess process = pb.call();
        RP.post(new StdOutReader(process.getInputStream(), execEnv.isRemote()));
        RP.post(new StdErrReader(process.getErrorStream(), execEnv.isRemote()));
        int rc = process.waitFor();
        latch.await();
        if (rc != 0) {
            if (process.getState() == NativeProcess.State.CANCELLED) {
                throw new InterruptedException("ls has been cancelled"); //NOI18N
            } else if (rc == 2) {
                throw new FileNotFoundException(errorOutput.toString());
            } else {
                throw new IOException(errorOutput.toString());
            }
        }
        if (criticalException.get() != null) {
            throw criticalException.get();
        }
    }

    public List<DirectoryStorage.Entry> getEntries() {
        return entries;
    }

    private String getFullTimeLsOption(HostInfo.OSFamily oSFamily) {
        switch (oSFamily) {
            case LINUX:
                return "--full-time"; // NOI18N
            case MACOSX:
                return "-lT"; // NOI18N
            case SUNOS:
                return "-lE"; // NOI18N
            case WINDOWS:
                throw new IllegalStateException("Windows in unsupported"); //NOI18N
            case UNKNOWN:
            default:
                return "-l"; // NOI18N
        }
    }

    private static LsLineParser createLsLineParser(HostInfo.OSFamily oSFamily, String remoteDir) {
        switch (oSFamily) {
            case LINUX:
                return new LsLineParser(3, remoteDir); // LinuxLsLineParser();
            case MACOSX:
                return new LsLineParser(4, remoteDir); // MacosLsLineParser();
            case SUNOS:
                return new LsLineParser(3, remoteDir); // SolarisLsLineParser();
            case WINDOWS:
                throw new IllegalStateException("Windows in unsupported"); //NOI18N
            case UNKNOWN:
            default:
                return new LsLineParser(4, remoteDir); // OtherLsLineParser();
        }
    }

    /*package*/ static List<DirectoryStorage.Entry> testLsLineParser(HostInfo.OSFamily oSFamily, String[] lines) {
        LsLineParser lp = createLsLineParser(oSFamily, "/dummy"); // NOI18N
        List<DirectoryStorage.Entry> result = new ArrayList<Entry>();
        for (String line : lines) {
            DirectoryStorage.Entry entry = lp.parseLine(line);
            result.add(entry);
        }
        return result;
    }

}
