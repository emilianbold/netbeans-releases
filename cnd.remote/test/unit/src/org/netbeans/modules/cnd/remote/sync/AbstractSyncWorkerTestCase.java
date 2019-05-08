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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Base test for different RemoteSyncWorker implementations
 */
public abstract class AbstractSyncWorkerTestCase extends RemoteTestBase {

    private RemoteSyncFactory oldSyncFactory;

    abstract RemoteSyncFactory getSyncFactory();

    abstract BaseSyncWorker createWorker(File src, ExecutionEnvironment execEnv, 
            PrintWriter out, PrintWriter err, FileObject privProjectStorageDir);

    protected abstract String getTestNamePostfix();

    public AbstractSyncWorkerTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    public String getName() {
        String name = super.getName();
        int pos = name.indexOf('[');
        if (pos > 1) {
            if (name.charAt(pos - 1) == ' ') {
                pos--;
            }
            name = name.substring(0, pos) + "_" + getTestNamePostfix() + name.substring(pos);
        }
        return name;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createRemoteTmpDir();
        RemoteServerRecord record = RemoteServerList.getInstance().get(getTestExecutionEnvironment());
        oldSyncFactory = record.getSyncFactory();
        record.setSyncFactory(getSyncFactory());
    }

    @Override
    protected void tearDown() throws Exception {
        clearRemoteTmpDir(); // before disconnection!
        super.tearDown();
        if (oldSyncFactory != null) {
            RemoteServerList.getInstance().get(getTestExecutionEnvironment()).setSyncFactory(oldSyncFactory);
        }
    }


    @ForAllEnvironments
    public void testSyncWorker_simple() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assertNotNull(execEnv);
        File src = createTestDir();
        doTest(src, execEnv, getDestDir(execEnv));
    }

    @ForAllEnvironments
    public void testSyncWorker_nb_platform_lib() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assertNotNull(execEnv);
        File netBeansDir = new File(getNetBeansPlatformDir(), "lib");
        doTest(netBeansDir, execEnv, getDestDir(execEnv));
    }

    @ForAllEnvironments
    public void testSyncWorker_nb_platform() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assertNotNull(execEnv);
        File netBeansDir = getNetBeansPlatformDir();
        doTest(netBeansDir, execEnv, getDestDir(execEnv));
    }

    private String getDestDir(ExecutionEnvironment execEnv) {
        return  getRemoteTmpDir() + "/sync-worker-test/" + Math.random() + "/";
    }

    private void doTest(File src, ExecutionEnvironment execEnv, String dst) throws Exception {
        PrintWriter out = new PrintWriter(System.out);
        PrintWriter err = new PrintWriter(System.err);
        System.err.printf("testUploadFile: %s to %s:%s\n", src.getAbsolutePath(), execEnv.getDisplayName(), dst);
        File privProjectStorageDir = createTempFile(src.getName() + "-nbproject-private-", "", true);
        BaseSyncWorker worker = createWorker(src, execEnv, out, err, FileUtil.toFileObject( privProjectStorageDir));
        boolean ok = worker.startup(Collections.<String, String>emptyMap());
        assertTrue(worker.getClass().getSimpleName() + ".startup failed", ok);
        worker.shutdown();
        CommonTasksSupport.rmDir(execEnv, dst, true, err).get();
        removeDirectory(privProjectStorageDir);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
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
        return FileUtil.normalizeFile(src);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(AbstractSyncWorkerTestCase.class);
    }
}
