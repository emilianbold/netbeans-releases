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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;

public final class FetchPrivilegesTask implements Computable<ExecutionEnvironment, List<String>> {

    private static final java.util.logging.Logger log = Logger.getInstance();

    public List<String> compute(ExecutionEnvironment execEnv) {
        /*
         * To find out actual privileges that tasks will have use
         * > ppriv -v $$ | grep [IL]
         *
         * and return intersection of list of I (inherit) and L (limit)
         * privileges...
         */

        NativeProcess ppriv = null;
        try {
            String shell = HostInfoUtils.getHostInfo(execEnv).getShell();
            String command = "ppriv -v $$ | grep [IL]"; // NOI18N

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(shell).setArguments("-c", command); // NOI18N

            ppriv = npb.call();
            int result = ppriv.waitFor();

            if (result != 0) {
                throw new IOException("Unable to get current privileges. Command " + // NOI18N
                        command + " failed with code " + result); // NOI18N
            }

            List<String> iprivs = new ArrayList<String>();
            List<String> lprivs = new ArrayList<String>();

            List<String> out = ProcessUtils.readProcessOutput(ppriv);

            for (String str : out) {
                if (str.contains("I:")) { // NOI18N
                    String[] privs = str.substring(
                            str.indexOf(": ") + 2).split(","); // NOI18N
                    iprivs = Arrays.asList(privs);
                } else if (str.contains("L:")) { // NOI18N
                    String[] privs = str.substring(
                            str.indexOf(": ") + 2).split(","); // NOI18N
                    lprivs = Arrays.asList(privs);
                }
            }

            if (iprivs == null || lprivs == null) {
                return Collections.emptyList();
            }

            List<String> real_privs = new ArrayList<String>();

            for (String ipriv : iprivs) {
                if (lprivs.contains(ipriv)) {
                    real_privs.add(ipriv);
                }
            }

            return real_privs;
        } catch (ConnectException ex) {
            return Collections.emptyList();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (IOException ex) {
            log.fine(ex.getMessage());
            try {
                ProcessUtils.logError(Level.FINE, log, ppriv);
            } catch (IOException ioex) {
            }
        }

        return Collections.emptyList();
    }
}
