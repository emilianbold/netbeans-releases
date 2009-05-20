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

package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;

/**
 * Test for ScpSyncWorker
 * @author Vladimir Kvashin
 */
public class ScpSyncWorkerTestCase extends RemoteTestBase {

    static {
        System.setProperty("cnd.remote.logger.level", "0");
        System.setProperty("nativeexecution.support.logger.level", "0");
    }

    public ScpSyncWorkerTestCase(String testName) {
        super(testName);
    }

    public void testSyncWorker() throws Exception {
        if (!canTestRemote()) {
            return;
        }
        ExecutionEnvironment execEnv = getRemoteExecutionEnvironment();
        assertNotNull(execEnv);

        File src = createTestDir();
        String dst = "/tmp/" + execEnv.getUser() + "/sync-worker-test";
        doTest(src, execEnv, dst);
    }

//    public void testSyncWorker_2() throws Exception {
//        if (!canTestRemote()) {
//            return;
//        }
//        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.createNew("vk155633", "endif.russia");
//        assertNotNull(execEnv);
//
//        ConnectionManager.getInstance().connectTo(execEnv);
//
//        File src = new File("/export/home/vk155633/NetBeansProjects/ScpTest");
//        String dst = "/tmp/vk155633/test-sync-worker/ScpTest";
//        doTest(src, execEnv, dst);
//    }

    private void doTest(File src, ExecutionEnvironment execEnv, String dst) throws Exception {
        PrintWriter out = new PrintWriter(System.out);
        PrintWriter err = new PrintWriter(System.err);
        System.err.printf("testUploadFile: %s to %s:%s\n", src.getAbsolutePath(), execEnv.getDisplayName(), dst);
        ScpSyncWorker worker = new ScpSyncWorker(src, execEnv, out, err);
        worker.synchronizeImpl(dst);
        CommonTasksSupport.rmDir(execEnv, dst, true, err);
    }

    private File createTestDir() throws IOException {
        File src = createTempFile("test-sync-worker-dir", null, true);
        File subdir1 = new File(src, "dir1");
        subdir1.mkdirs();
        subdir1.deleteOnExit();

        File subdir2 = new File(src, "dir2");
        subdir2.mkdirs();
        subdir2.deleteOnExit();

        File deeper = new File(subdir2, "deeper");
        deeper.mkdirs();
        deeper.deleteOnExit();

        File file1 = new File(subdir1, "file1");
        writeFile(file1, "this is file1\n");
        file1.deleteOnExit();

        File file2 = new File(subdir2, "file2");
        writeFile(file2, "this is file2\n");
        file2.deleteOnExit();

        File file3 = new File(deeper, "file3");
        writeFile(file3, "this is file3\n");
        file3.deleteOnExit();
        return src;
    }

    private CharSequence runCommand(ExecutionEnvironment execEnv, String command, String... args) throws Exception {
        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(execEnv);
        pb.setExecutable(command);

        if (args != null) {
            pb.setArguments(args);
        }

        NativeProcess lsProcess = pb.call();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(lsProcess.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        int count = 0;
        while ((line = rdr.readLine()) != null) {
            sb.append(line + '\n');
            count++;
        }
        return sb;
    }
}
