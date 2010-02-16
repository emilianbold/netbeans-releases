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
package org.netbeans.modules.dlight.procfs.processinfo;

import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.procfs.reader.api.ProcReader;
import org.netbeans.modules.dlight.procfs.reader.api.ProcReaderFactory;
import org.netbeans.modules.dlight.util.Computable;
import org.netbeans.modules.dlight.util.TasksCachedProcessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.ProcessInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.ProcessInfoProvider;

/**
 *
 * @author ak119685
 */
class ProcBasedProcessInfoProvider implements ProcessInfoProvider {

    private final static TasksCachedProcessor<FetchParams, ProcessInfo> cachedProcessor =
            new TasksCachedProcessor<FetchParams, ProcessInfo>(new ProcessInfoFetcher(), false);
    private final FetchParams params;

    ProcBasedProcessInfoProvider(ExecutionEnvironment env, int pid) {
        params = new FetchParams(env, pid);
    }

    public ProcessInfo getProcessInfo() {
        ProcessInfo result = null;

        try {
            result = cachedProcessor.compute(params);
        } catch (InterruptedException ex) {
        }

        return result;
    }

    private static class FetchParams {

        private final ExecutionEnvironment env;
        private final int pid;

        private FetchParams(ExecutionEnvironment env, int pid) {
            this.env = env;
            this.pid = pid;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof FetchParams)) {
                return false;
            }

            FetchParams that = (FetchParams) obj;

            return this.pid == that.pid && this.env.equals(that.env);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + (this.env != null ? this.env.hashCode() : 0);
            hash = 71 * hash + this.pid;
            return hash;
        }
    }

    private static class ProcessInfoFetcher implements Computable<FetchParams, ProcessInfo> {

        public ProcessInfo compute(final FetchParams taskArguments) throws InterruptedException {
            ProcessInfo result = null;

            try {
                HostInfo info = HostInfoUtils.getHostInfo(taskArguments.env);
                if (info.getOSFamily() != HostInfo.OSFamily.SUNOS) {
                    return null;
                }

                ProcReader reader = ProcReaderFactory.getReader(taskArguments.env, taskArguments.pid);
                final long creation_ts = reader.getProcessUsage().getUsageInfo().pr_create;

                if (creation_ts > 0) {
                    result = new ProcessInfo() {

                        public long getCreationTimestamp(TimeUnit unit) {
                            return unit.convert(creation_ts, TimeUnit.NANOSECONDS);
                        }
                    };
                }

            } catch (Throwable ex) {
            }

            return result;
        }
    }
}
