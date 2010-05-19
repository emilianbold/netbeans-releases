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
package org.netbeans.modules.dlight.procfs.reader.impl;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.procfs.api.LWPUsage;
import org.netbeans.modules.dlight.procfs.api.PStatus;
import org.netbeans.modules.dlight.procfs.api.PUsage;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

public class RemoteProcReader extends ProcReaderImpl {

    private final static Logger log = DLightLogger.getLogger(RemoteProcReader.class);
    private final String usageFile;
    private final String lwpsUsageCmd;
    private final String statusFile;
    private final String lwpDir;
    private final NativeProcessBuilder npb;

    public RemoteProcReader(ExecutionEnvironment execEnv, int pid, ByteOrder byteOrder, DataModel dataModel) {
        super(byteOrder, dataModel);
        usageFile = "/proc/" + pid + "/usage"; // NOI18N

        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("cat"); // NOI18N

        statusFile = "/proc/" + pid + "/status"; // NOI18N
        lwpDir = "/proc/" + pid + "/lwp"; // NOI18N

        lwpsUsageCmd = "cat " + lwpDir + "/*/lwpusage"; // NOI18N
    }

    public PStatus getProcessStatus() {
        PStatus result = null;
        try {
            Process p = npb.setArguments(statusFile).call();
            result = getProcessStatus(p.getInputStream());
        } catch (IOException ex) {
        }
        return result;
    }

    public PUsage getProcessUsage() throws IOException {
        Process p = npb.setArguments(usageFile).call();
        return getProcessUsage(p.getInputStream());
    }

    public List<LWPUsage> getThreadsInfo() {
        List<LWPUsage> result = new ArrayList<LWPUsage>();

        // cannot use setArguments as it will enclose * in quotes and will
        // prevent from interpreting it by shell

        try {
            Process p = npb.setCommandLine(lwpsUsageCmd).call(); // NOI18N
            int exitCode = -1;

            try {
                exitCode = p.waitFor();
            } catch (InterruptedException ex) {
            }

            if (exitCode != 0) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("cannot exec " + lwpsUsageCmd); // NOI18N
                    InputStream es = p.getErrorStream();
                    if (es.available() > 0) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(es));
                        while (br.ready()) {
                            log.fine("\t" + br.readLine()); // NOI18N
                        }
                    }
                }

                return result;
            }

            InputStream is = p.getInputStream();
            MultyFilesStream ss = new MultyFilesStream(is, LWPUsage.FILESIZE);

            try {
                while (ss.available() > 0) {
                    result.add(getProcessUsage(ss));
                }
            } catch (IOException ex) {
                // ignore...
            } finally {
                ss.doClose();
            }

        } catch (IOException ex) {
        } finally {
            // To allow others use setArguments...
            npb.setCommandLine(null);
        }

        return result;
    }

    private static class MultyFilesStream extends FilterInputStream {

        private final int chunkSize;

        private MultyFilesStream(InputStream stream, int chunkSize) {
            super(stream);
            this.chunkSize = chunkSize;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, Math.min(len, chunkSize));
        }

        @Override
        public void close() throws IOException {
        }

        public void doClose() throws IOException {
            super.close();
        }
    }
}
