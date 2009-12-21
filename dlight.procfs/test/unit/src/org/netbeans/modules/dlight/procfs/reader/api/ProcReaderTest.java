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

package org.netbeans.modules.dlight.procfs.reader.api;

import java.io.IOException;
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
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

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

    public static Test suite() {
        NativeExecutionBaseTestSuite suite = new NativeExecutionBaseTestSuite();
        suite.addTestSuite(ProcReaderTest.class);
        return suite;
    }

    /**
     * Test of getProcessStatus method, of class ProcReader.
     */
    @org.junit.Test
    public void testGetProcessStatus() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        HostInfo hostinfo = HostInfoUtils.getHostInfo(execEnv);
        if (hostinfo.getOSFamily() != HostInfo.OSFamily.SUNOS) {
            // this test is valid only on Solaris
            return;
        }

        NativeProcess p = null;
        try {
            System.out.println("getProcessInfo");
            NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
            npb.setExecutable("/usr/bin/amd64/pwait").setArguments("1");
            p = npb.call();

            System.out.println("Process with PID " + p.getPID() + " started");

            ProcReader preader = ProcReaderFactory.getReader(execEnv, p.getPID());

            PStatus pstatus = preader.getProcessStatus();
            assertNotNull(pstatus);
            ThreadsInfo threadInfo = pstatus.getThreadInfo();
            assertNotNull(threadInfo);

            assertEquals("pwait is a single-thread appl", 1, threadInfo.pr_nlwp);
            assertEquals("actual pid should be the same as provider returns", p.getPID(), pstatus.getPIDInfo().pr_pid);

            PUsage usage = preader.getProcessUsage();
            assertNotNull(usage);

            long sysNanoTime = System.nanoTime();
            assertTrue("Time of process creation is not more than 2 seconds ago", sysNanoTime - usage.getUsageInfo().pr_create < 2 * 1000 * 1000 * 1000);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail(ex.getMessage());
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }
}