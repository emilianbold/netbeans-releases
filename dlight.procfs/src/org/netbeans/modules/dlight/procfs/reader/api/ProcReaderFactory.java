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
package org.netbeans.modules.dlight.procfs.reader.api;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.procfs.api.LWPUsage;
import org.netbeans.modules.dlight.procfs.api.PStatus;
import org.netbeans.modules.dlight.procfs.api.PUsage;
import org.netbeans.modules.dlight.procfs.reader.impl.DataModel;
import org.netbeans.modules.dlight.procfs.reader.impl.LocalProcReader;
import org.netbeans.modules.dlight.procfs.reader.impl.ProcessStatusProvider;
import org.netbeans.modules.dlight.procfs.reader.impl.ProcessStatusProvider64;
import org.netbeans.modules.dlight.procfs.reader.impl.ProcessUsageProvider;
import org.netbeans.modules.dlight.procfs.reader.impl.RemoteProcReader;
import org.netbeans.modules.dlight.procfs.reader.impl.ThreadsInfoProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

public final class ProcReaderFactory {

    private ProcReaderFactory() {
    }

    public static ProcReader getReader(final ExecutionEnvironment execEnv, final int pid) {
        ProcReader reader = null;

        try {
            HostInfo info = HostInfoUtils.getHostInfo(execEnv);
            ByteOrder byteOrder = info.getCpuFamily() == HostInfo.CpuFamily.SPARC
                    ? ByteOrder.BIG_ENDIAN
                    : ByteOrder.LITTLE_ENDIAN;

            DataModel dataModel = getDataModel(execEnv, pid);

            ProcessStatusProvider _statusProvider = null;
            ProcessUsageProvider _usageProvider = null;
            ThreadsInfoProvider _threadsInfoProvider = null;


            ProcReader _reader = null;

            if (execEnv.isLocal()) {
                _reader = new LocalProcReader(pid, byteOrder, dataModel);

            } else {
                _reader = new RemoteProcReader(execEnv, pid, byteOrder, dataModel);
            }

            _usageProvider = (ProcessUsageProvider) _reader;
            _threadsInfoProvider = (ThreadsInfoProvider) _reader;

            // @see IZ#171783 - MSA does not work for 64-bit application
            if (dataModel == DataModel._LP64) {
                _statusProvider = new ProcessStatusProvider64(execEnv, pid);
            } else {
                _statusProvider = (ProcessStatusProvider) _reader;
            }

            final ProcessStatusProvider statusProvider = _statusProvider;
            final ProcessUsageProvider usageProvider = _usageProvider;
            final ThreadsInfoProvider threadsInfoProvider = _threadsInfoProvider;

            reader = new ProcReader() {

                public PUsage getProcessUsage() throws IOException {
                    return usageProvider.getProcessUsage();
                }

                public List<LWPUsage> getThreadsInfo() throws IOException {
                    return threadsInfoProvider.getThreadsInfo();
                }

                public PStatus getProcessStatus() throws IOException {
                    return statusProvider.getProcessStatus();
                }
            };

        } catch (Throwable th) {
        }

        return reader;
    }

    private static DataModel getDataModel(ExecutionEnvironment execEnv, int pid) {
        DataModel dataModel = DataModel._LP64; // safer ???

        try {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable("/bin/pflags").setArguments("" + pid); // NOI18N
            Process p = npb.call();
            List<String> lines = ProcessUtils.readProcessOutput(p);
            int rc = p.waitFor();
            Pattern pattern = Pattern.compile("[ \t]+data model = ([^ ]+).*"); // NOI18N
            if (rc == 0) {
                for (String line : lines) {
                    Matcher m = pattern.matcher(line);
                    if (m.matches()) {
                        String model = m.group(1);
                        dataModel = DataModel.valueOf(model);
                        break;
                    }
                }
            }
        } catch (InterruptedException ex) {
        } catch (IOException ex) {
        }

        return dataModel;
    }
}
