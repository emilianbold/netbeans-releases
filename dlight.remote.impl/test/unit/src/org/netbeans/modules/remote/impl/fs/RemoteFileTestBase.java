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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileTestBase extends NativeExecutionBaseTestCase {

    static {
        System.setProperty("jsch.connection.timeout", "30000");
        System.setProperty("socket.connection.timeout", "30000");
        System.setProperty("remote.throw.assertions", "true");
    }
    
    protected RemoteFileSystem fs;
    protected RemoteFileObject rootFO;
    protected final ExecutionEnvironment execEnv;

    protected String sharedLibExt;

    private Level oldLevel = null;
    
    public RemoteFileTestBase(String testName) {
        super(testName);
        fs = null;
        rootFO = null;
        execEnv = null;
    }

    public RemoteFileTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        this.execEnv = execEnv;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setLoggers(true);
        if (execEnv == null) {
            return;
        }
        RemoteFileSystemManager.getInstance().resetFileSystem(execEnv);
        fs = RemoteFileSystemManager.getInstance().getFileSystem(execEnv);
        assertNotNull("Null remote file system", fs);
        File cache = fs.getCache();
        removeDirectoryContent(cache);
        rootFO = fs.getRoot();
        assertNotNull("Null root file object", rootFO);
        assertTrue("Can not create directory " + cache.getAbsolutePath(), cache.exists() || cache.mkdirs());
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() == OSFamily.MACOSX) {
            sharedLibExt = ".dylib";
        } else {
            sharedLibExt = ".so";
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        setLoggers(false);
    }
   
    @org.netbeans.api.annotations.common.SuppressWarnings("LG")
    private void setLoggers(boolean setup) {
        final Logger logger = Logger.getLogger("remote.support.logger");
        if (setup) {
            logger.addHandler(new TestLogHandler(logger));
            if (NativeExecutionTestSupport.getBoolean("remote", "logging.finest")
                 || NativeExecutionTestSupport.getBoolean("remote", getClass().getName() + ".logging.finest")) {
                oldLevel = logger.getLevel();
                logger.setLevel(Level.FINEST);
            }
        } else {
            if (oldLevel != null) {
                logger.setLevel(oldLevel);
            }
        }
    }

    protected String mkTempAndRefreshParent() throws Exception {
        return mkTempAndRefreshParent(false);
    }
    
    protected void removeRemoteDirIfNotNull(String path) throws Exception {
        if (path != null) {
            ProcessUtils.execute(execEnv, "chmod", "-R", "u+w", path);
            CommonTasksSupport.rmDir(execEnv, path, true, new OutputStreamWriter(System.err)).get();
        }
    }

    protected String execute(String command, String... args) {
        return execute(execEnv, command, args);
    }

    protected static String execute(ExecutionEnvironment env, String command, String... args) {
        ProcessUtils.ExitStatus res = ProcessUtils.execute(env, command, args);
        assertEquals(command + ' ' + args + " at " + env.getDisplayName() + " failed: " + res.error, 0, res.exitCode);
        return res.output;
    }

    protected static String executeInDir(String dir, ExecutionEnvironment env, String command, String... args) {
        ProcessUtils.ExitStatus res = ProcessUtils.executeInDir(dir, env, command, args);
        assertEquals(command + ' ' + args + " at " + env.getDisplayName() + " failed: " + res.error, 0, res.exitCode);
        return res.output;
    }
    
    protected String executeInDir(String dir, String command, String... args) {
        return executeInDir(dir, execEnv, command, args);
    }

    protected RemoteFileObject getFileObject(String path) throws Exception {
        RemoteFileObject fo = rootFO.getFileObject(path);
        assertNotNullFileObject(fo, null, path);
        return fo;
    }
    
    private void assertNotNullFileObject(FileObject fo, FileObject parent, String relOrAbsPath) throws Exception    {
        if (fo == null) {
            String absPath;
            StringBuilder message = new StringBuilder();
            message.append("Null file object for ").append(relOrAbsPath);
            if (parent == null) {
                absPath = relOrAbsPath;
                message.append(" in ").append(execEnv);
            } else {
                absPath = parent.getPath() + '/' + relOrAbsPath;
                message.append(" in ").append(parent);
            }
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "ls", "-ld", absPath);
            System.err.printf("Null file object for %s:%s\n", execEnv, absPath);
            System.err.printf("ls -ld %s\nrc=%d\n%s\n%s", absPath, res.exitCode, res.output, res.error);
            String dirName = PathUtilities.getDirName(absPath);
            String baseName = PathUtilities.getBaseName(absPath);
            RemoteFileObject parentFO = rootFO.getFileObject(dirName);
            System.err.printf("parentFO=%s\n", parentFO);
            if (parentFO != null) {                
                File cache = parentFO.getImplementor().getCache();
                if(cache == null) {
                    System.err.printf("Cache file is null\n");
                } else {
                    File storageFile = new File(cache, RemoteFileSystem.CACHE_FILE_NAME);
                    if (storageFile.exists()) {
                        System.err.printf("Parent directory cache (%s) content:\n", storageFile.getAbsolutePath());
                        printFile(storageFile, null, System.err);
                    } else {
                        System.err.printf("Parent directory cache (%s) does not exist\n", storageFile.getAbsolutePath());
                    }
                }
                fo = parentFO.getFileObject(baseName);
                System.err.printf("2-nd attempt %s: %s\n", (fo == null ? "failed" : "succeeded"), fo);
                if (fo == null) {
                    parentFO.refresh();
                    fo = parentFO.getFileObject(baseName);
                    System.err.printf("3-rd attempt %s: %s\n", (fo == null ? "failed" : "succeeded"), fo);
                }
            }
            if (res.isOK()) {
                message.append("; ls reports that file exists:\n").append(res.output);
            } else {
                message.append("; ls reports that file does NOT exist:\n").append(res.error);
            }
            assertTrue(message.toString(), false);
        }
    }

    protected FileObject getFileObject(FileObject base, String path) throws Exception {
        FileObject fo = base.getFileObject(path);
        assertNotNullFileObject(fo, base, path);
        return fo;
    }

    protected void upload(File file, String remotePath) throws Exception {
        Future<UploadStatus> task = CommonTasksSupport.uploadFile(file, execEnv, remotePath, -1);
        UploadStatus res = task.get();
        assertEquals("Failed uploading " + file.getAbsolutePath() + " to " + execEnv + ":" + remotePath
                + ": " + res.getError(), 0, res.getExitCode());
    }

    protected void mkDir(String dir) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Integer> mkDirTask = CommonTasksSupport.mkDir(execEnv, dir, new PrintWriter(System.err));
        //System.out.printf("Mkdir %s\n", dir);
        int rc = mkDirTask.get(30, TimeUnit.SECONDS);
        //System.out.printf("mkdir %s done, rc=%d\n", dir, rc);
        assertEquals(0, rc);
    }

    protected String mkTempAndRefreshParent(boolean directory) throws Exception {
        String path = mkTemp(execEnv, directory);
        refreshParent(path);
        return path;
    }

    protected void refreshParent(String path) throws Exception {
        String parent = PathUtilities.getDirName(path);
        getFileObject(parent).refresh();
    }
    
    protected String readRemoteFile(String absPath) throws Exception {
        FileObject fo = getFileObject(absPath);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        return readFile(fo);
    }

    protected void writeFile(FileObject fo, CharSequence content) throws Exception {
        writeFile(fo, content, 1);
    }

    protected void writeFile(FileObject fo, CharSequence content, int repeatCount) throws Exception {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream()));
            for (int i = 0; i < repeatCount; i++) {
                bw.append(content);
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    protected String readFile(FileObject fo) throws Exception {
        assertTrue("File " +  fo.getPath() + " does not exist", fo.isValid());
        InputStream is = fo.getInputStream();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
        try {
            assertNotNull("Null input stream", is);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rdr.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            rdr.close();
        }
    }

    protected String getFileName(ExecutionEnvironment execEnv, String absPath) {
        return execEnv.toString() + ':' + absPath;
    }
}
