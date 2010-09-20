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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author ondra
 */
public class FilesystemInterceptorTest extends AbstractGitTestCase {

    public FilesystemInterceptorTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Git.STATUS_LOG.setLevel(Level.ALL);
    }

    public void testSeenRootsLogin () throws Exception {
        File folderA = new File(repositoryLocation, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        File fileB1 = new File(folderB, "file1");
        File fileB2 = new File(folderB, "file2");
        folderB.mkdirs();
        fileB1.createNewFile();
        fileB2.createNewFile();
        File folderC = new File(repositoryLocation, "folderC");
        File fileC1 = new File(folderC, "file1");
        File fileC2 = new File(folderC, "file2");
        folderC.mkdirs();
        fileC1.createNewFile();
        fileC2.createNewFile();

        FilesystemInterceptor interceptor = Git.getInstance().getVCSInterceptor();
        Field f = FilesystemInterceptor.class.getDeclaredField("gitFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<File, Set<File>> map = (HashMap) f.get(hgFolderEventsHandler);

        LogHandler handler = new LogHandler();
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToInitializeRoots(folderA);
        interceptor.pingRepositoryRootFor(folderA);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Set<File> files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));
        handler.setFilesToInitializeRoots(fileA1);
        interceptor.pingRepositoryRootFor(fileA1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));

        handler.setFilesToInitializeRoots(fileB1);
        interceptor.pingRepositoryRootFor(fileB1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(2, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        handler.setFilesToInitializeRoots(fileB2);
        interceptor.pingRepositoryRootFor(fileB2);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));

        handler.setFilesToInitializeRoots(folderC);
        interceptor.pingRepositoryRootFor(folderC);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(4, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(folderB);
        interceptor.pingRepositoryRootFor(folderB);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(repositoryLocation);
        interceptor.pingRepositoryRootFor(repositoryLocation);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Git.STATUS_LOG.removeHandler(handler);
        assertEquals(1, files.size());
        assertTrue(files.contains(repositoryLocation));
    }

    private class LogHandler extends Handler {
        private File fileToInitialize;
        private boolean filesInitialized;
        private final HashSet<File> initializedFiles = new HashSet<File>();

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: finished")) {
                synchronized (this) {
                    filesInitialized = true;
                    notifyAll();
                }
            } else if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: ")) {
                if (record.getParameters()[0].equals(fileToInitialize.getAbsolutePath())) {
                    synchronized (this) {
                        initializedFiles.add(fileToInitialize);
                        notifyAll();
                    }
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void setFilesToInitializeRoots (File file) {
            fileToInitialize = file;
            initializedFiles.clear();
            filesInitialized = false;
        }

        private boolean waitForFilesToInitializeRoots() throws InterruptedException {
            for (int i = 0; i < 20; ++i) {
                synchronized (this) {
                    if (filesInitialized && initializedFiles.contains(fileToInitialize)) {
                        return true;
                    }
                    wait(500);
                }
            }
            return false;
        }

    }
}
