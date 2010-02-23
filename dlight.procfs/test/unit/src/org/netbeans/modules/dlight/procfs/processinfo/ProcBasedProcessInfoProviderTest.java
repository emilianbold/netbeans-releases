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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.openide.util.Exceptions;
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

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of getProcessInfo method, of class ProcBasedProcessInfoProvider.
     */
    @org.junit.Test
    public void testGetLocalProcessInfo() {
        System.out.println("getProcessInfo (local test)");
        ProcBasedProcessInfoProviderFactory instance = new ProcBasedProcessInfoProviderFactory();

        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        boolean isSolaris = Utilities.getOperatingSystem() == Utilities.OS_SOLARIS;

        if (isSolaris) {
            // On solaris proc provider is enabled
            // Be sure that Factory returns not null value and
            // that provider returns not null info
            try {
                ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "/bin/echo $$ && sleep 4");
                Process p = pb.start();
                long time = System.nanoTime() / 1000 / 1000 / 1000;
                String pid = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
                ProcessInfoProvider pip = instance.getProvider(execEnv, Integer.parseInt(pid));
                assertNotNull(pip);
                ProcessInfo info = pip.getProcessInfo();
                assertNotNull(info);
                long processStartTime = info.getCreationTimestamp(TimeUnit.SECONDS);
                assertTrue(Math.abs(processStartTime - time) < 2);

                p.waitFor();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                fail();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                fail();
            }
        } else {
            ProcessInfoProvider pip = instance.getProvider(execEnv, 0);
            assertNull(pip);
        }
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetProcessInfo() {
        try {
            System.out.println("getProcessInfo");
            ProcBasedProcessInfoProviderFactory instance = new ProcBasedProcessInfoProviderFactory();
            ExecutionEnvironment execEnv = getTestExecutionEnvironment();
            HostInfo hinfo = HostInfoUtils.getHostInfo(execEnv);
            boolean isSolaris = hinfo.getOSFamily() == HostInfo.OSFamily.SUNOS;

            if (!isSolaris) {
                ProcessInfoProvider pip = instance.getProvider(execEnv, 0);
                assertNull(pip);
                return;
            }

            long lastTS = -1;

            Process[] processes = new Process[3];

            for (int i = 0; i < 3; i++) {
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                npb.setExecutable("/bin/sh").setArguments("-c", "/bin/echo $$ && sleep 5");
                Process p = npb.call();
                processes[i] = p;
                String pid = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
                ProcessInfoProvider pip = instance.getProvider(execEnv, Integer.parseInt(pid));
                assertNotNull(pip);
                ProcessInfo info = pip.getProcessInfo();
                assertNotNull(info);
                long processStartTime = info.getCreationTimestamp(TimeUnit.MILLISECONDS);

                if (lastTS > 0) {
                    assertTrue(processStartTime - lastTS < 2000);
                }

                lastTS = processStartTime;
            }

            for (int i = 0; i < 3; i++) {
                if (processes[i] != null) {
                    try {
                        processes[i].waitFor();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail();
        } catch (CancellationException ex) {
            Exceptions.printStackTrace(ex);
            fail();
        }
    }
}
