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

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileProgressMonitor;
import org.netbeans.libs.git.progress.StatusProgressMonitor;
import org.netbeans.modules.versioning.util.IndexingBridge;

/**
 *
 * @author ondra
 */
public class GitClientInvocationHandlerTest extends AbstractGitTestCase {

    public GitClientInvocationHandlerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testIndexingBridge () throws Exception {
        IndexingBridge bridge = IndexingBridge.getInstance();
        Field f = IndexingBridge.class.getDeclaredField("LOG");
        f.setAccessible(true);
        Logger LOG = (Logger) f.get(bridge);
        LOG.setLevel(Level.ALL);
        IndexingBridgeLogHandler h = new IndexingBridgeLogHandler();
        LOG.addHandler(h);

        GitClient client = Git.getInstance().getClient(repositoryLocation);

        File folder = new File(repositoryLocation, "folder");
        File file = new File(folder, "file");
        folder.mkdirs();
        file.createNewFile();
        h.reset();
        client.add(new File[] { file }, FileProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        client.commit(new File[] { file }, "aaa", FileProgressMonitor.NULL_PROGRESS_MONITOR);
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

    private static class InhibitMonitor extends FileProgressMonitor {
        private boolean cont;
        private boolean barrierAccessed;
        @Override
        public void notifyFile (File file) {
            barrierAccessed = true;
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

    private static class IndexingBridgeLogHandler extends Handler {

        boolean bridgeAccessed;
        private HashSet<File> expectedParents;

        @Override
        public void publish (LogRecord record) {
            bridgeAccessed = true;
            for (File f : expectedParents) {
                if (record.getMessage().equals("scheduling for fs refresh: [" + f + "]")) {
                    expectedParents.remove(f);
                    break;
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
        }

        private void setExpectedParents(File[] files) {
            this.expectedParents = new HashSet<File>(Arrays.asList(files));
        }
    }
}
