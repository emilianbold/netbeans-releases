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
import java.io.StringWriter;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileTestBase extends NativeExecutionBaseTestCase {

    protected RemoteFileSystem fs;
    protected FileObject rootFO;
    protected final ExecutionEnvironment execEnv;

    protected String sharedLibExt;
    private String[] mkTempArgsPlain;
    private String[] mkTempArgsDir;

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
            mkTempArgsPlain = new String[] { "-t", "/tmp" };
            mkTempArgsDir = new String[] { "-t", "/tmp", "-d" };
        } else {
            sharedLibExt = ".so";
            mkTempArgsPlain = new String[0];
            mkTempArgsDir = new String[] { "-d" };
        }
    }

    protected String mkTemp() throws Exception {
        return mkTemp(false);
    }

    protected String execute(String command, String... args) {
        ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, command, args);
        assertEquals(command + ' ' + args + " failed: " + res.error, 0, res.exitCode);
        return res.output;
    }

    protected FileObject getFileObject(String path) throws Exception {
        FileObject fo = rootFO.getFileObject(path);
        assertNotNull("Null file object for " + path, fo);
        return fo;
    }

    protected FileObject getFileObject(FileObject base, String path) throws Exception {
        FileObject fo = base.getFileObject(path);
        assertNotNull("Null file object for " + path + " in " + base.getPath(), fo);
        return fo;
    }

    protected void upload(File file, String remotePath) throws Exception {
        StringWriter errWriter = new StringWriter();
        Future<Integer> task = CommonTasksSupport.uploadFile(file, execEnv, remotePath, -1, errWriter);
        assertEquals("Failed uploading " + file.getAbsolutePath() + " to " + execEnv + ":" + remotePath
                + ": " + errWriter.toString(),
                0, task.get().intValue());
    }

    protected String mkTemp(boolean directory) throws Exception {
        ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "mktemp",
                directory ? mkTempArgsDir : mkTempArgsPlain);
        assertEquals("mktemp failed: " + res.error, 0, res.exitCode);
        return res.output;
    }

    protected String readRemoteFile(String absPath) throws Exception {
        FileObject fo = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        return readFile(fo);
    }

    protected void writeFile(FileObject fo, CharSequence content) throws Exception {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream()));
            bw.append(content);
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

//    protected void checkFileExistance(String absPath) throws Exception {
//        FileObject fo = rootFO.getFileObject(absPath);
//        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
//        assertFalse("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isVirtual());
//    }

    protected String runCommand(String command, String... args) throws Exception {
        ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, command, args);
        assertTrue("Command failed:" + command + ' ' + stringArrayToString(args), res.isOK());
        return res.output;
    }

    protected String runCommandInDir(String dir, String command, String... args) throws Exception {
        ProcessUtils.ExitStatus res = ProcessUtils.executeInDir(dir, execEnv, command, args);
        assertTrue("Command \"" + command + ' ' + stringArrayToString(args) +
                "\" in dir " + dir + " failed", res.isOK());
        return res.output;
    }

    private String stringArrayToString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(' ').append(arg);
        }
        return sb.toString();
    }

    protected String runScript(String script) throws Exception {
        final StringBuilder output = new StringBuilder();
        ShellScriptRunner scriptRunner = new ShellScriptRunner(execEnv, script, new LineProcessor() {
            public void processLine(String line) {
                output.append(line).append('\n');
                System.err.println(line);
            }
            public void reset() {}
            public void close() {}
        });
        int rc = scriptRunner.execute();
        assertEquals("Error running script", 0, rc);
        return output.toString();
    }
}
