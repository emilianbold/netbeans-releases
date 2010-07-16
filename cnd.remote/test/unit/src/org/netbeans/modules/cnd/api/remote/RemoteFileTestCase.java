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

package org.netbeans.modules.cnd.api.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;

/**
 *
 * @author Sergey Grinev
 */
public class RemoteFileTestCase extends RemoteTestBase {

    @org.netbeans.api.annotations.common.SuppressWarnings("LG")
    public RemoteFileTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
        if (NativeExecutionTestSupport.getBoolean(RemoteDevelopmentTest.DEFAULT_SECTION, "logging.finest")) {
            Logger.getLogger("cnd.remote.logger").setLevel(Level.ALL);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createRemoteTmpDir();
    }

    @Override
    protected void tearDown() throws Exception {
        clearRemoteTmpDir(); // before disconnection!
        super.tearDown();
    }

    @ForAllEnvironments(section="remote.platforms")
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void testPlainFile() throws Exception {
        final String tmpFile = getTempName();
        ProcessUtils.execute(getTestExecutionEnvironment(), "rm", tmpFile);
        File remoteFile = RemoteFile.create(getTestExecutionEnvironment(), tmpFile);
        assert remoteFile instanceof RemoteFile;
        assert !remoteFile.exists();
        ProcessUtils.execute(getTestExecutionEnvironment(), "touch", tmpFile);
        assert remoteFile.exists();
        assert remoteFile.isFile();
        assert !remoteFile.isDirectory();
        assert remoteFile.canRead();
        remoteFile.delete();
        assert ! remoteFile.exists();
    }

    private final String getTempName() {
        return getRemoteTmpDir() + "/RemoteFileTest" + Math.random() + ".bak";
    }

    @ForAllEnvironments(section="remote.platforms")
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void testDirectoryStructure() throws Exception {
        final String tempDir = getTempName();
        String script = String.format("rm -rf %1$s; mkdir %1$s; touch %1$s/1; touch %1$s/2; touch %1$s/3", tempDir);
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        assertEquals("The return code of a script " + script, 0, ProcessUtils.execute(env, "sh", "-c", script).exitCode);
        File remoteFile = RemoteFile.create(env, tempDir);
        assert remoteFile instanceof RemoteFile;
        assert !remoteFile.isFile();
        assert remoteFile.isDirectory();
        assert remoteFile.canRead();
        File[] list = remoteFile.listFiles();
        assert list.length == 3;
        for(File file : list) {
            assert file instanceof RemoteFile;
            assert file.exists();
            assert file.canRead();
            assert file.isFile();
        }
        remoteFile.delete();
        assert ! remoteFile.exists();
    }

    @ForAllEnvironments(section="remote.platforms")
    public void testReader() throws FileNotFoundException, IOException {
        final String tempFile = getTempName();
        String script = String.format("rm -rf %1$s; echo 1 > %1$s; echo 2 >> %1$s; echo 3 >> %1$s; echo 4 >> %1$s", tempFile);
        ProcessUtils.ExitStatus es = ProcessUtils.execute(
                getTestExecutionEnvironment(),
                "sh", "-c",
                script);
        assertEquals("The return code of a script " + script, 0, es.exitCode);
        BufferedReader in = new BufferedReader(RemoteFile.createReader( RemoteFile.create(getTestExecutionEnvironment(), tempFile) ));
        int i = 1;
        try {
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                assert (line.equals("" + i));
                i++;
            }
        } finally {
            in.close();
        }
        assert i == 5;
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(RemoteFileTestCase.class);
    }
}
