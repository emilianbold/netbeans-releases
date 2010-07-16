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

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.procfs.api.PStatus;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 *
 * @author ak119685
 */
public class ProcessStatusProvider64 implements ProcessStatusProvider {

    private final NativeProcessBuilder npb;
    private final Pattern lwpPattern;
    private final PStatus.PIDInfo pidInfo;

    public ProcessStatusProvider64(ExecutionEnvironment execEnv, int pid) {
        npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable("/bin/pflags").setArguments("" + pid); // NOI18N
        lwpPattern = Pattern.compile("^[\t ]+\\/([0-9]+):.*"); // NOI18N
        pidInfo = new PStatus.PIDInfo(pid) {
        };
    }

    public PStatus getProcessStatus() {
        PStatus status = null;

        try {
            final Process p = npb.call();
            final List<String> lines = ProcessUtils.readProcessOutput(p);
            int count = 0;
            for (String line : lines) {
                Matcher m = lwpPattern.matcher(line);
                if (m.matches()) {
                    count++;
                }
            }

            int rc = -1;

            try {
                rc = p.waitFor();
            } catch (InterruptedException ex) {
            }

            if (rc == 0) {
                final int pr_nlwp = count;
                status = new PStatus() {

                    public ThreadsInfo getThreadInfo() {
                        return new ThreadsInfo(pr_nlwp, 0) {
                        };
                    }

                    public PIDInfo getPIDInfo() {
                        return pidInfo;
                    }
                };
            }
        } catch (IOException ex) {
        }
        return status;
    }
}
