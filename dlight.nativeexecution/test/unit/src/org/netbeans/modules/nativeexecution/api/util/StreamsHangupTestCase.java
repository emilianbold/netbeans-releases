/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import static org.netbeans.modules.nativeexecution.api.util.ProcessUtils.getReader;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author vkvashin
 */
public class StreamsHangupTestCase extends NativeExecutionBaseTestCase {

    static {
      System.setProperty("nativeexecution.support.logger.level", "0");
    }

    private String remoteScriptPath = null;
    private static final String scriptName = "err_and_out.sh";
    private final String prefix;
    private String remoteTmpDir = null;

    public StreamsHangupTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
        prefix = "[" + testExecutionEnvironment + "] ";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File dir = getDataDir();
        File scriptFile = new File(dir, scriptName);
        assertTrue(scriptFile.exists());
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env.isLocal()) {
            scriptFile.setExecutable(true);
            remoteScriptPath = scriptFile.getAbsolutePath();
            remoteTmpDir = File.createTempFile(getClass().getSimpleName(), ".dat").getAbsolutePath();
        } else {
            ConnectionManager.getInstance().connectTo(env);
            remoteTmpDir = createRemoteTmpDir();
            int rc = CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
            remoteScriptPath = remoteTmpDir + "/" + scriptFile.getName();
            CommonTasksSupport.UploadStatus res = CommonTasksSupport.uploadFile(scriptFile, env, remoteScriptPath, 0777, true).get();
            assertEquals("Error uploading file " + scriptFile.getAbsolutePath() + " to " + getTestExecutionEnvironment() + ":" + remoteScriptPath, 0, rc);
        }
    }

    private void print(String text) {
        System.err.println(prefix + text);
    }

    @Override
    protected void tearDown() throws Exception {
        if (remoteTmpDir != null) {
            ExecutionEnvironment env = getTestExecutionEnvironment();
            CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
        }
        super.tearDown();
    }

    enum Action {
        NONE,
        CALL_PROCESSUTILS_IGNORE,
        READ_ERR_THEN_OUT,
        READ_OUT_TTHEN_ERR,
    }

    private void dotest(int cycles, int bufsize, int timeout, Action action) throws Exception {
        assertNotNull(remoteScriptPath);
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
        pb.setExecutable(remoteScriptPath);
        pb.setArguments("-c", ""+cycles, "-o", ""+bufsize, "-e", ""+bufsize);
        print("Starting process " + scriptName + " with cycles=" + cycles + ", bufsize=" + bufsize);
        NativeProcess process = pb.call();
        print("Waiting process " + scriptName + " with timeout=" + timeout);
        long time = System.currentTimeMillis();
        switch (action) {
            case NONE:
                break;
            case CALL_PROCESSUTILS_IGNORE:
                ProcessUtils.ignoreProcessOutputAndError(process);
                break;
            case READ_ERR_THEN_OUT:
                readStream(process.getErrorStream());
                readStream(process.getInputStream());
                break;
            case READ_OUT_TTHEN_ERR:
                readStream(process.getInputStream());
                readStream(process.getErrorStream());
                break;
            default:
                throw new AssertionError(action.name());
        }
        boolean exited = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!exited) {
            int pid = process.getPID();
            ProcessUtils.execute(env, "kill", "-9", ""+pid);
            //process.destroy(); does not work on Mac
            throw new TimeoutException("Process jas not finish in " + timeout + " seconds");
        }
        time = System.currentTimeMillis() - time;
        print("Waited " + time + " ms");
        assertEquals("Script exit code", 0, process.exitValue());
        ProcessUtils.ExitStatus res = ProcessUtils.execute(env, "ls", "-l", remoteTmpDir);
        assertEquals(0, res.exitCode);
    }

    private void findBufSize(Action action, int timeout) throws Exception {
        int sz = 32;
        while (true) {
            try {
                dotest(1, sz, timeout, action);
            } catch (TimeoutException ex) {
                String text = "Hung with bufsize=" + sz + "; action=" + action;
                print(text);
                TimeoutException ex2 = new TimeoutException(text);
                ex2.initCause(ex);
                throw ex2;
            }
            if (sz > 1024*16) {
                sz += 1024*4;
            } else if (sz > 1024) {
                sz += 1024;
            } else {
                sz *= 2;
            }
        }
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testHangup() throws Exception {

        //findBufSize(Action.NONE, 10);
        findBufSize(Action.READ_ERR_THEN_OUT, 10);

        // does not hang
        // dotest(1, 1024, 20, Action.READ_OUT_TTHEN_ERR);

        // hangs
        // dotest(1, 1024*1024, 20, Action.READ_OUT_TTHEN_ERR);

//        dotest(1, 1024*1024, 30, Action.NONE);
//        dotest(1, 1024*1024, 30, Action.CALL_PROCESSUTILS_IGNORE);
    }
    
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(StreamsHangupTestCase.class);
    }
}
