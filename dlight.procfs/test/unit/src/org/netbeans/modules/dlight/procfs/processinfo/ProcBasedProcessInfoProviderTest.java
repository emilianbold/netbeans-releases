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
package org.netbeans.modules.dlight.procfs.processinfo;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.ProcessInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.ProcessInfoProvider;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import static org.junit.Assert.*;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Utilities;

/**
 *
 * @author ak119685
 */
public class ProcBasedProcessInfoProviderTest extends NativeExecutionBaseTestCase {

    public ProcBasedProcessInfoProviderTest(String name) {
        super(name);
    }

    public ProcBasedProcessInfoProviderTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(ProcBasedProcessInfoProviderTest.class);
    }

    public void testGetProcessInfoLocal() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        ProcBasedProcessInfoProviderFactory instance = new ProcBasedProcessInfoProviderFactory();

        if (Utilities.getOperatingSystem() != Utilities.OS_SOLARIS) {
            ProcessInfoProvider pip = instance.getProvider(execEnv, 0);
            assertNull(pip);
            return;
        }

        // Be sure that Factory returns not null value and
        // that provider returns not null info
        long timeBefore = System.nanoTime();
        Process p = new ProcessBuilder("/bin/sh", "-c", "/bin/echo $$ && sleep 100").start();
        try {
            int pid = new Scanner(p.getInputStream()).nextInt();

            ProcessInfoProvider pip = instance.getProvider(execEnv, pid);
            assertNotNull(pip);

            ProcessInfo info = pip.getProcessInfo();
            assertNotNull(info);

            long processStartTime = info.getCreationTimestamp(TimeUnit.NANOSECONDS);
            long timeAfter = System.nanoTime();
            assertTrue(0 < processStartTime - timeBefore);
            assertTrue(0 < timeAfter - processStartTime);
        } finally {
            p.destroy();
        }
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testGetProcessInfo() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ProcBasedProcessInfoProviderFactory instance = new ProcBasedProcessInfoProviderFactory();

        if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() != HostInfo.OSFamily.SUNOS) {
            ProcessInfoProvider pip = instance.getProvider(execEnv, 0);
            assertNull(pip);
            return;
        }

        long[] startTimes = new long[3];

        for (int i = 0; i < 3; i++) {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable("/bin/sh").setArguments("-c", "/bin/echo $$ && sleep 100");
            Process p = npb.call();
            try {
                int pid = new Scanner(p.getInputStream()).nextInt();

                ProcessInfoProvider pip = instance.getProvider(execEnv, pid);
                assertNotNull(pip);

                ProcessInfo info = pip.getProcessInfo();
                assertNotNull(info);

                startTimes[i] = info.getCreationTimestamp(TimeUnit.NANOSECONDS);
            } finally {
                p.destroy();
            }
        }

        assertTrue(0 < startTimes[1] - startTimes[0]);
        assertTrue(0 < startTimes[2] - startTimes[1]);
    }
}
