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

import junit.framework.Test;
import org.netbeans.modules.dlight.procfs.api.PStatus;
import org.netbeans.modules.dlight.procfs.api.PStatus.ThreadsInfo;
import org.netbeans.modules.dlight.procfs.api.PUsage;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author ak119685
 */
public class ProcReaderTest extends NativeExecutionBaseTestCase {

    public ProcReaderTest(String name) {
        super(name);
    }

    public ProcReaderTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(ProcReaderTest.class);
    }

    public void testGetProcessStatusLocal() throws Exception {
        doTestGetProcessStatus(ExecutionEnvironmentFactory.getLocal());
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testGetProcessStatus() throws Exception {
        doTestGetProcessStatus(getTestExecutionEnvironment());
    }

    private void doTestGetProcessStatus(ExecutionEnvironment execEnv) throws Exception {
        HostInfo hostinfo = HostInfoUtils.getHostInfo(execEnv);
        if (hostinfo.getOSFamily() != HostInfo.OSFamily.SUNOS) {
            // this test is valid only on Solaris
            return;
        }

        String pwait = HostInfoUtils.fileExists(execEnv, "/usr/bin/amd64/pwait")
                ? "/usr/bin/amd64/pwait" : "/usr/bin/pwait";
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        NativeProcess p = npb.setExecutable(pwait).setArguments("1").call();
        try {
            ProcReader preader = ProcReaderFactory.getReader(execEnv, p.getPID());

            PStatus pstatus = preader.getProcessStatus();
            assertNotNull(pstatus);
            assertEquals("actual pid should be the same as provider returns", p.getPID(), pstatus.getPIDInfo().pr_pid);

            ThreadsInfo threadInfo = pstatus.getThreadInfo();
            assertNotNull(threadInfo);
            assertEquals("pwait is a single-thread appl", 1, threadInfo.pr_nlwp);

            PUsage pusage1 = preader.getProcessUsage();
            assertNotNull(pusage1);
            assertTrue(0 < pusage1.getUsageInfo().pr_tstamp - pusage1.getUsageInfo().pr_create);

            PUsage pusage2 = preader.getProcessUsage();
            assertNotNull(pusage2);
            assertTrue(0 < pusage2.getUsageInfo().pr_tstamp - pusage2.getUsageInfo().pr_create);
            assertTrue(0 < pusage2.getUsageInfo().pr_tstamp - pusage1.getUsageInfo().pr_tstamp);
        } finally {
            p.destroy();
        }
    }
}
