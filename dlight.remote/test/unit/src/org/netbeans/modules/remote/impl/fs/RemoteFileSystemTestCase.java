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
package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.Test;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileSystemTestCase extends RemoteFileTestBase {

    public RemoteFileSystemTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    // Disabled, see IZ 190453
    @RandomlyFails
    public void testSyncDirStruct() throws Exception {
        sleep(200); // FIXUP: a workaround for a very instable test failure
        String dirName = "/usr/include";
        // set up local test directory
        File rfsCache = fs.getCache();
        File localDir = File.createTempFile("usr-include", null, rfsCache);
        if (localDir.exists()) {
            boolean result = localDir.isFile() ? localDir.delete() : removeDirectory(localDir);
            assertTrue("Can't remove file or directory " + localDir, result);
        } else {
            boolean result = localDir.mkdirs();
            assertTrue("Can't create directory " + localDir, result);
        }

        // set up test file
        File stdioFile = new File(localDir,"stdio.h");
        if (stdioFile.exists()) {
            assertTrue("Can't delete file " + stdioFile, stdioFile.delete());
        }

        long time;
        
        RemoteFileSupport remoteFileSupport = fs.getRemoteFileSupport();
        time = System.currentTimeMillis();
        remoteFileSupport.ensureDirSync(localDir, dirName);
        time = System.currentTimeMillis() - time;
        assertTrue("File " + stdioFile + " should exist", stdioFile.exists());
        System.err.printf("Synchronizing %s took %d ms\n", dirName, time);

        // check that ensureDirSync does not take too long
        long maxTime = time / 10;
        time = System.currentTimeMillis();
        remoteFileSupport.ensureDirSync(localDir, dirName);
        time = System.currentTimeMillis() - time;
        System.err.printf("Checking sync for %s took %d ms\n", dirName, time);
        assertTrue("ensureDirSync worked too long", time < maxTime);

        removeDirectory(localDir);
    }

    @ForAllEnvironments
    // Disabled, see IZ 190453
    @RandomlyFails
    public void testRemoteStdioH() throws Exception {
        String absPath = "/usr/include/stdio.h";
        FileObject fo = rootFO.getFileObject(absPath);
        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        String content = readFile(absPath);
        String text2search = "printf";
        assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                content.indexOf(text2search) >= 0);
    }

    @ForAllEnvironments
    // Disabled, see IZ 190453
    @RandomlyFails
    public void testMultipleRead() throws Exception {
        removeDirectory(fs.getCache());
        final String absPath = "/usr/include/errno.h";
        long firstTime = -1;
        for (int i = 0; i < 5; i++) {
            long time = System.currentTimeMillis();
            FileObject fo = rootFO.getFileObject(absPath);
            assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
            assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
            InputStream is = fo.getInputStream();
            assertNotNull("Got null input stream for " + getFileName(execEnv, absPath), is);
            is.close();
            time = System.currentTimeMillis() - time;
            System.err.printf("Pass %d; getting input stream for %s took %d ms\n", i, getFileName(execEnv, absPath), time);
            if (i == 0) {
                firstTime = time;
            } else if (time > 0) {
                assertTrue("Getting input stream for "+ getFileName(execEnv, absPath) + " took too long (" + time + ") ms",
                        time < firstTime / 8);
            }
        }
    }

    private abstract class ThreadWorker implements Runnable {
        private final String name;
        private final CyclicBarrier barrier;
        final List<Exception> exceptions;
        ThreadWorker(String name, CyclicBarrier barrier, List<Exception> exceptions) {
            this.name = name;
            this.barrier = barrier;
            this.exceptions = exceptions;
        }
        public void run() {
            Thread.currentThread().setName(name);
            try {
                System.err.printf("%s waiting on barrier\n", name);
                barrier.await();
                System.err.printf("%s working\n", name);
                work();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                exceptions.add(ex);
            } catch (BrokenBarrierException ex) {
                ex.printStackTrace();
                exceptions.add(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                exceptions.add(ex);
            } finally {
                System.err.printf("%s done\n", name);
            }
        }
        protected abstract void work() throws Exception;
    }


    @ForAllEnvironments
    public void testParallelRead() throws Exception {

        removeDirectory(fs.getCache());
        final String absPath = "/usr/include/stdio.h";

        int threadCount = 10;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<Exception>());
        final AtomicLong size = new AtomicLong(-1);

        class Worker extends ThreadWorker {
            public Worker(String name, CyclicBarrier barrier, List<Exception> exceptions) {
                super(name, barrier, exceptions);
            }
            protected void work() throws Exception {
                String content = readFile(absPath);
                int currSize = content.length();
                size.compareAndSet(-1, currSize);
                String text2search = "printf";
                assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                        content.indexOf(text2search) >= 0);
                assertEquals("File size for " + absPath + " differ", size.get(), currSize);
            }
        }

        fs.getRemoteFileSupport().resetStatistic();
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new Worker(absPath, barrier, exceptions));
            threads[i].start();
        }
        System.err.printf("Waiting for threads to finish\n");
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        assertEquals("Dir. sync count differs", 1, fs.getRemoteFileSupport().getDirSyncCount());
        assertEquals("File transfer count differs", 1, fs.getRemoteFileSupport().getFileCopyCount());
        if (!exceptions.isEmpty()) {
            System.err.printf("There were %d exceptions; throwing first one.\n", exceptions.size());
            throw exceptions.iterator().next();
        }
    }

    @ForAllEnvironments
    public void testInexistance() throws Exception {
        String path = "/dev/qwe/asd/zxc";
        FileObject fo = rootFO.getFileObject(path);
        assertTrue("File " + getFileName(execEnv, path) + " does not exist, but is reported as existent",
                fo == null || !fo.isValid());
    }

    @ForAllEnvironments
    public void testWrite() throws Exception {
        String tempFile = null;
        try {
            FileObject fo;
            String stdio_h = "/usr/include/stdio.h";
            fo = rootFO.getFileObject(stdio_h);
            assertNotNull("null file object for " + stdio_h, fo);
            assertFalse("FileObject should NOT be writable: " + fo.getPath(), fo.canWrite());
            ExitStatus res = ProcessUtils.execute(execEnv, "mktemp");
            assertEquals("mktemp failed", 0, res.exitCode);
            tempFile = res.output;
            fo = rootFO.getFileObject(tempFile);
            assertTrue("FileObject should be writable: " + fo.getPath(), fo.canWrite());
            String content = "a quick brown fox...";
            writeFile(fo, content);
            CharSequence readContent = readFile(fo);
            assertEquals("File content differ", content.toString(), readContent.toString());
            WritingQueue.getInstance(execEnv).waitFinished(null);
            readContent = ProcessUtils.execute(execEnv, "cat", tempFile).output;
            assertEquals("File content differ", content.toString(), readContent.toString());
        } finally {
            if (tempFile != null) {
                CommonTasksSupport.rmFile(execEnv, tempFile, new OutputStreamWriter(System.err));
            }
        }
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteFileSystemTestCase.class);
    }
}
