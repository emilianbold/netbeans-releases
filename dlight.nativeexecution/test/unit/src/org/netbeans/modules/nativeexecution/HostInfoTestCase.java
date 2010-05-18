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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;

public class HostInfoTestCase extends NativeExecutionBaseTestCase {

    public HostInfoTestCase(String name) {
        super(name);
    }

    public HostInfoTestCase(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(HostInfoTestCase.class);
    }

    @org.junit.Test
    public void testGetHostInfoLocal() throws Exception {
        HostInfo hi = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
        assertNotNull(hi);
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetHostInfo() throws Exception {
        HostInfo hi = HostInfoUtils.getHostInfo(getTestExecutionEnvironment());
        assertNotNull(hi);
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testGetOS() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        String mspec = NativeExecutionTestSupport.getMspec(execEnv);

        HostInfo hi = HostInfoUtils.getHostInfo(execEnv);
        assertNotNull(hi);

        if (mspec.endsWith("-S2")) {
            assertEquals("SunOS", hi.getOS().getName());
            assertEquals(HostInfo.OSFamily.SUNOS, hi.getOSFamily());
        } else if (mspec.endsWith("-Linux")) {
            assertTrue(hi.getOS().getName().startsWith("Linux"));
            assertEquals(HostInfo.OSFamily.LINUX, hi.getOSFamily());
        } else if (mspec.endsWith("-MacOSX")) {
            assertTrue(hi.getOS().getName().startsWith("MacOSX"));
            assertEquals(HostInfo.OSFamily.MACOSX, hi.getOSFamily());
        } else {
            fail("Could not guess OS from mspec " + mspec);
        }

        if (mspec.startsWith("intel-")) {
            assertEquals(HostInfo.CpuFamily.X86, hi.getCpuFamily());
        } else if (mspec.startsWith("sparc-")) {
            assertEquals(HostInfo.CpuFamily.SPARC, hi.getCpuFamily());
        } else {
            fail("Could not guess OS from mspec " + mspec);
        }
    }

    @org.junit.Test
    public void testFileExistsLocal() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        HostInfo hi = HostInfoUtils.getHostInfo(execEnv);
        String existentFile;
        String nonexistentFile;
        if (hi.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
            existentFile = "C:\\AUTOEXEC.BAT";
            nonexistentFile = "C:\\MANUALEXEC.BAT";
        } else {
            existentFile = "/etc/passwd";
            nonexistentFile = "/etc/passwdx";
        }
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testFileExists() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        String existentFile = "/etc/passwd";
        String nonexistentFile = "/etc/passwdx";
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
        assertTrue(HostInfoUtils.fileExists(execEnv, existentFile));
        assertFalse(HostInfoUtils.fileExists(execEnv, nonexistentFile));
    }

    @org.junit.Test
    public void testIsLocalhost() {
        assertTrue(HostInfoUtils.isLocalhost("localhost"));
        assertTrue(HostInfoUtils.isLocalhost("127.0.0.1"));
        assertFalse(HostInfoUtils.isLocalhost("localhost1"));
        assertFalse(HostInfoUtils.isLocalhost("localhst"));
        assertFalse(HostInfoUtils.isLocalhost("localhost:22"));
    }
}
