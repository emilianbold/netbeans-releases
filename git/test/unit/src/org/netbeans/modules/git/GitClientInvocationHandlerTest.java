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

package org.netbeans.modules.git;

import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.client.GitClientInvocationHandler;
import org.netbeans.modules.git.client.GitCanceledException;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileProgressMonitor;
import org.netbeans.libs.git.progress.ProgressMonitor.DefaultProgressMonitor;
import org.netbeans.libs.git.progress.StatusProgressMonitor;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
public class GitClientInvocationHandlerTest extends AbstractGitTestCase {
    private Logger indexingLogger;
    private Logger invocationHandlerLogger;

    public GitClientInvocationHandlerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IndexingBridge bridge = IndexingBridge.getInstance();
        Field f = IndexingBridge.class.getDeclaredField("LOG");
        f.setAccessible(true);
        indexingLogger = (Logger) f.get(bridge);
        f = GitClientInvocationHandler.class.getDeclaredField("LOG");
        f.setAccessible(true);
        invocationHandlerLogger = (Logger) f.get(GitClientInvocationHandler.class);
    }

    public void testIndexingBridge () throws Exception {
        indexingLogger.setLevel(Level.ALL);
        LogHandler h = new LogHandler();
        indexingLogger.addHandler(h);

        GitClient client = Git.getInstance().getClient(repositoryLocation);

        File folder = new File(repositoryLocation, "folder");
        File file = new File(folder, "file");
        folder.mkdirs();
        file.createNewFile();
        h.reset();
        client.add(new File[] { file }, FileProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        client.commit(new File[] { file }, "aaa", StatusProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        client.copyAfter(file, new File(folder, "file2"), FileProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        client.getStatus(new File[] { file }, StatusProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        File anotherRepo = new File(repositoryLocation.getParentFile(), "wc2");
        anotherRepo.mkdirs();
        GitClient client2 = Git.getInstance().getClient(anotherRepo);
        client2.init();
        assertFalse(h.bridgeAccessed);

        h.reset();
        h.setExpectedParents(new File[] { file.getParentFile() });
        client.remove(new File[] { file }, false, FileProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(h.bridgeAccessed);
        assertTrue(h.expectedParents.isEmpty());

        h.reset();
        write(file, "aaa");
        client.rename(file, new File(folder, "file2"), false, FileProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);
    }

    public void testExclusiveAccess () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitMonitor m = new InhibitMonitor();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, FileProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testDoNotBlockIndexing () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitMonitor m = new InhibitMonitor();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        invocationHandlerLogger.setLevel(Level.ALL);
        LogHandler handler = new LogHandler();
        invocationHandlerLogger.addHandler(handler);
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.remove(new File[] { file }, false, FileProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        assertFalse(handler.indexingBridgeCalled);
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testPallalelizableCommands () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitMonitor m = new InhibitMonitor();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getStatus(new File[] { file }, StatusProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        t2.join(5000);
        assertEquals(Thread.State.TERMINATED, t2.getState());
        m.cont = true;
        t1.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testCancelCommand () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final InhibitMonitor m = new InhibitMonitor();
        final Exception[] exs = new Exception[2];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file, file2 }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        m.cancel();
        m.cont = true;
        t1.join();
        assertTrue(m.isCanceled());
        assertEquals(1, m.count);
    }

    public void testCancelWaitingOnBlockedRepository () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitMonitor m = new InhibitMonitor();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, FileProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        t2.interrupt();
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertTrue(exs[1] instanceof GitCanceledException);
    }

    public void testCancelSupport () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final InhibitMonitor m = new InhibitMonitor();
        final Exception[] exs = new Exception[2];
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            public void perform () {
                setProgressMonitor(m);
                try {
                    client.add(new File[] { file, file2 }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        };
        Task t = supp.start(Git.getInstance().getRequestProcessor(repositoryLocation), repositoryLocation, "Git Add");
        m.waitAtBarrier();
        supp.cancel();
        m.cont = true;
        t.waitFinished();
        assertTrue(supp.isCanceled());
        assertTrue(m.isCanceled());
        assertEquals(1, m.count);
    }

    public void testSupportDisplayNames () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();
        final File file3 = new File(repositoryLocation, "aaa3");
        file3.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final GitProgressSupport.DefaultProgressMonitor[] ms = new GitProgressSupport.DefaultProgressMonitor[1];
        final Exception[] exs = new Exception[2];
        ProgressLogHandler h = new ProgressLogHandler();
        Logger log = Logger.getLogger(GitProgressSupport.class.getName());
        log.addHandler(h);
        log.setLevel(Level.ALL);
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repositoryLocation);

        final boolean[] flags = new boolean[6];

        Task preceedingTask = rp.post(new Runnable() {
            @Override
            public void run () {
                // barrier
                flags[0] = true;
                // wait for asserts
                while (!flags[1]) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {}
                }
            }
        });

        final InhibitMonitor m = new InhibitMonitor();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file3 }, m);
                } catch (GitException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        m.cont = false;
        thread.start();

        GitProgressSupport supp = new GitProgressSupport() {
            abstract class InternMonitor extends DefaultProgressMonitor implements FileProgressMonitor {
            }

            @Override
            public void perform () {
                InternMonitor m;
                ms[0] = m = new InternMonitor() {
                    @Override
                    public void notifyFile(File file) {
                        // barrier
                        flags[4] = true;
                        // wait for asserts
                        while (!flags[5]) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {}
                        }
                        setProgress(file.getName());
                    }
                };
                setProgressMonitor(ms[0]);
                try {
                    // barrier
                    flags[2] = true;
                    // wait for asserts
                    while (!flags[3]) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {}
                    }
                    getClient().add(new File[] { file, file2 }, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        };
        List<String> expectedMessages = new LinkedList<String>();
        expectedMessages.add("Git Add - Queued");
        Task t = supp.start(rp, repositoryLocation, "Git Add");
        assertEquals(expectedMessages, h.progressMessages);
        flags[1] = true;
        preceedingTask.waitFinished();
        expectedMessages.add("Git Add");
        for (int i = 0; i < 100; ++i) {
            if (flags[2]) break;
            Thread.sleep(100);
        }
        assertTrue(flags[2]);
        assertEquals(expectedMessages, h.progressMessages);
        flags[3] = true;

        expectedMessages.add("Git Add - Queued on " + repositoryLocation.getName());
        for (int i = 0; i < 100; ++i) {
            if (expectedMessages.equals(h.progressMessages)) break;
            Thread.sleep(100);
        }
        assertEquals(expectedMessages, h.progressMessages);
        m.cont = true;
        thread.join();

        for (int i = 0; i < 100; ++i) {
            if (flags[4]) break;
            Thread.sleep(100);
        }
        assertTrue(flags[4]);
        expectedMessages.add("Git Add");
        assertEquals(expectedMessages, h.progressMessages);
        flags[5] = true;

        t.waitFinished();
        expectedMessages.add("Git Add - " + file.getName());
        expectedMessages.add("Git Add - " + file2.getName());
        assertEquals(expectedMessages, h.progressMessages);
    }

    private static class InhibitMonitor extends DefaultProgressMonitor implements FileProgressMonitor {
        private boolean cont;
        private boolean barrierAccessed;
        private int count;
        @Override
        public void notifyFile (File file) {
            barrierAccessed = true;
            ++count;
            while (!cont) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        private void waitAtBarrier() throws InterruptedException {
            for (int i = 0; i < 100; ++i) {
                if (barrierAccessed) {
                    break;
                }
                Thread.sleep(100);
            }
            assertTrue(barrierAccessed);
        }
    }

    private class LogHandler extends Handler {

        boolean bridgeAccessed;
        private HashSet<File> expectedParents;
        private boolean indexingBridgeCalled;

        @Override
        public void publish (LogRecord record) {
            if (record.getLoggerName().equals(indexingLogger.getName())) {
                bridgeAccessed = true;
                for (File f : expectedParents) {
                    if (record.getMessage().equals("scheduling for fs refresh: [" + f + "]")) {
                        expectedParents.remove(f);
                        break;
                    }
                }
            } else if (record.getLoggerName().equals(invocationHandlerLogger.getName())) {
                if (record.getMessage().contains("Running command in indexing bridge")) {
                    indexingBridgeCalled = true;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void reset() {
            bridgeAccessed = false;
            indexingBridgeCalled = false;
        }

        private void setExpectedParents(File[] files) {
            this.expectedParents = new HashSet<File>(Arrays.asList(files));
        }
    }

    private static class ProgressLogHandler extends Handler {

        List<String> progressMessages = new LinkedList<String>();

        @Override
        public void publish (LogRecord record) {
            if (record.getMessage().equals("New status of progress: {0}")) {
                progressMessages.add((String) record.getParameters()[0]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
