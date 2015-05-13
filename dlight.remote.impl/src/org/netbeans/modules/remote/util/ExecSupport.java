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

package org.netbeans.modules.remote.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.RequestProcessor;

/**
 * This class does pretty much the same as ProcessUtils.execute()
 * It is introduced because ProcessUtils in 6.8 has no this method
 * [appeared in 6.8.1]
 * 
 * @author ak119685
 */
public final class ExecSupport {

    private ExecSupport() {
    }

    public static Status call(final NativeProcessBuilder npb) {
        Status result;

        if (npb == null) {
            throw new NullPointerException("NULL process builder!"); // NOI18N
        }

        RequestProcessor rp = new RequestProcessor("e/o reading", 2); // NOI18N

        try {
            final NativeProcess process = npb.call();

            if (process.getState() == State.ERROR) {
                process.destroy();
                return new Status(-100, Collections.<String>emptyList(), Arrays.asList("Error while starting a process")); // NOI18N
            }

            Callable<List<String>> ereader = new Callable<List<String>>() {

                public List<String> call() throws Exception {
                    return ProcessUtils.readProcessError(process);
                }
            };

            Callable<List<String>> oreader = new Callable<List<String>>() {

                public List<String> call() throws Exception {
                    return ProcessUtils.readProcessOutput(process);
                }
            };


            FutureTask<List<String>> etask = new FutureTask<>(ereader);
            FutureTask<List<String>> otask = new FutureTask<>(oreader);

            rp.post(otask);
            rp.post(etask);

            result = new Status(process.waitFor(), otask.get(), etask.get());
        } catch (Throwable th) {
            result = new Status(-100, Collections.<String>emptyList(), Arrays.asList(th.getMessage()));
        }

        return result;
    }

    public static final class Status {

        public final int exitCode;
        public final List<String> error;
        public final List<String> output;

        private Status(int exitCode, List<String> output, List<String> error) {
            this.exitCode = exitCode;
            this.error = error;
            this.output = output;
        }

        public boolean isOK() {
            return exitCode == 0;
        }
    }
}
