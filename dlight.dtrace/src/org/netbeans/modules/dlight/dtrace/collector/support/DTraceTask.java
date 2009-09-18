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
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 *
 * @author ak119685
 */
class DTraceTask implements Callable<Integer> {

    private final static Logger log = DLightLogger.getLogger(DTraceTask.class);
    private final ExecutionEnvironment execEnv;
    private final String taskCommand;
    private final ProcessLineCallback lineProcessor;

    public DTraceTask(ExecutionEnvironment execEnv, String taskCommand, ProcessLineCallback lineProcessor) {
        this.execEnv = execEnv;
        this.taskCommand = taskCommand;
        this.lineProcessor = lineProcessor;
    }

    public Integer call() throws Exception {
        int result = -1;

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine(taskCommand);

        Process dtraceProcess = null;
        InputStream is = null;

        try {
            dtraceProcess = npb.call();
            is = dtraceProcess.getInputStream();

            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;

                try {
                    while ((line = br.readLine()) != null) {
                        lineProcessor.processLine(line);
                    }
                } catch (InterruptedIOException ex) {
                    // Clean interrupted status
                    Thread.interrupted();
                }
            }
        } catch (Throwable th) {
            log.log(Level.FINE, taskCommand, th.getMessage());
        } finally {
            try {
                lineProcessor.processClose();
            } catch (Throwable th) {
                log.log(Level.FINE, taskCommand, th.getMessage());
            }

            if (dtraceProcess != null) {
                try {
                    result = dtraceProcess.exitValue();
                } catch (Throwable th) {
                    // Not exited yet...
                }

                dtraceProcess.destroy();
            }
        }

        if (result != 0) {
            ProcessUtils.logError(Level.FINE, log, dtraceProcess);
        }

        return result;
    }
}
