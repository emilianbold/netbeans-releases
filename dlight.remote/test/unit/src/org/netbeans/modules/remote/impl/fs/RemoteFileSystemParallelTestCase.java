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
public class RemoteFileSystemParallelTestCase extends RemoteFileTestBase {

    public RemoteFileSystemParallelTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
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
                String content = readRemoteFile(absPath);
                int currSize = content.length();
                size.compareAndSet(-1, currSize);
                String text2search = "printf";
                assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                        content.indexOf(text2search) >= 0);
                // size reported by file system and size of text in characters differ
                // TODO:rfs think out how to check the size
                //assertEquals("File size for " + absPath + " differ", size.get(), currSize);
            }
        }

        fs.resetStatistic();
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new Worker(absPath, barrier, exceptions));
            threads[i].start();
        }
        System.err.printf("Waiting for threads to finish\n");
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        assertEquals("Dir. sync count differs", 3, fs.getDirSyncCount());
        assertEquals("File transfer count differs", 1, fs.getFileCopyCount());
        if (!exceptions.isEmpty()) {
            System.err.printf("There were %d exceptions; throwing first one.\n", exceptions.size());
            throw exceptions.iterator().next();
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteFileSystemParallelTestCase.class);
    }
}
