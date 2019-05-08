/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;

/**
 *
 */
public class SuspendableFileChangeListenerTest extends NativeExecutionBaseTestCase {
    private static final boolean TRACE = false;
    
    public SuspendableFileChangeListenerTest(String name) {
        super(name, ExecutionEnvironmentFactory.getLocal());
    }

    private boolean isDisabled() throws IOException, CancellationException {
        return HostInfoUtils.getHostInfo(getTestExecutionEnvironment()).getOSFamily() == HostInfo.OSFamily.WINDOWS;
    }

    private static final class FCL extends DumpingFileChangeListener {
        private final static class EventPair {
            private final String kind;
            private final FileEvent event;

            public EventPair(String kind, FileEvent event) {
                this.kind = kind;
                this.event = event;
            }

            @Override
            public String toString() {
                return "EventPair{" + "kind=" + kind + ", event=" + event + '}';
            }
        }
        
        private final LinkedList<EventPair> events = new LinkedList<EventPair>();
        public FCL(String name, String prefixToStrip, PrintStream out, boolean checkExpected) {
            super(name, prefixToStrip, out, checkExpected);
        }

        @Override
        protected void register(String eventKind, FileEvent fe) {
            events.addLast(new EventPair(eventKind, fe));
            super.register(eventKind, fe);
        }
    }
    
    @Test
    public void testRemoveThenCreateAsChange() throws Throwable {
        if (isDisabled()) {
            return;
        }
        String[] dirStruct = new String[]{
            "d real_dir_1",
            "- real_dir_1/file_1",
        };
        String[] removeFile = new String[]{
            "R real_dir_1/file_1",};
        String[] addFile = new String[]{
            "- real_dir_1/file_1"
        };
        FileObject tempFO = mkTempFO(getName(), "tmp");
        final File tempFile = FileUtil.toFile(tempFO);
        try {
            createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), dirStruct);
            File workDir = getWorkDir();
            File testLog = new File(workDir, "test.dat");
            File referenceLog = new File(workDir, "reference.dat");
            FCL golden = new FCL(getName(), "", new PrintStream(referenceLog), true);
            FCL delegate = new FCL(getName(), "", new PrintStream(testLog), true);
            SuspendableFileChangeListener suspendableListener = new SuspendableFileChangeListener(delegate, false);

            FileSystemProvider.addRecursiveListener(golden, tempFO.getFileSystem(), tempFO.getPath());
            FileSystemProvider.addRecursiveListener(suspendableListener, tempFO.getFileSystem(), tempFO.getPath());

            suspendableListener.suspendRemoves();
            createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), removeFile, false);
            FileUtil.refreshFor(tempFile);
            createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), addFile, false);
            FileUtil.refreshFor(tempFile);
            suspendableListener.resumeRemoves();
            suspendableListener.flush();

            if (TRACE) {
                printFile(referenceLog, "Casual ", System.out);
                printFile(testLog, "Suspend", System.out);
            }
            assertEquals("golden: ", 2, golden.events.size());
            assertEquals("golden 1:", DumpingFileChangeListener.FILE_DELETED, golden.events.get(0).kind);
            assertEquals("golden 2:", DumpingFileChangeListener.FILE_DATA_CREATED, golden.events.get(1).kind);
            assertEquals("delegated: ", 1, delegate.events.size());
            assertEquals("delegate 1:", DumpingFileChangeListener.FILE_CHANGED, delegate.events.get(0).kind);
            assertEquals("golden vs delegate ", golden.events.get(1).event.getFile(), delegate.events.get(0).event.getFile());
            assertEquals("golden vs delegate ", golden.events.get(1).event.getTime(), delegate.events.get(0).event.getTime());
        } finally {
            removeDirectory(tempFile);
        }
    }
        
    @Test
    public void testRenameAsRemoveAddNotSuspended() throws Throwable {
        doTestRenameAsRemoveAdd(false);
    }
    
    @Test
    public void testRenameAsRemoveAddSuspended() throws Throwable {
        doTestRenameAsRemoveAdd(true);
    }
    
    private void doTestRenameAsRemoveAdd(boolean suspend) throws Throwable {
        FileObject tempFO = mkTempFO(getName(), "tmp");
        final File tempFile = FileUtil.toFile(tempFO);
        try {
            File workDir = getWorkDir();
            File testLog = new File(workDir, "test.dat");
            File referenceLog = new File(workDir, "reference.dat");
            FCL golden = new FCL(getName(), "", new PrintStream(referenceLog), true);
            FCL delegate = new FCL(getName(), "", new PrintStream(testLog), true);
            SuspendableFileChangeListener suspendableListener = new SuspendableFileChangeListener(delegate, false);
            
            FileObject fo = tempFO.createData("toMove", "txt");
            String oldPath = fo.getPath();
            FileSystemProvider.addRecursiveListener(golden, tempFO.getFileSystem(), tempFO.getPath());
            FileSystemProvider.addRecursiveListener(suspendableListener, tempFO.getFileSystem(), tempFO.getPath());

            if (suspend) {
                suspendableListener.suspendRemoves();
            }
            FileLock lock = fo.lock();
            try {
                fo.rename(lock, "newName", "newExt");
            } finally {
                lock.releaseLock();
            }
            if (suspend) {
                suspendableListener.resumeRemoves();
            }
            suspendableListener.flush();

            if (TRACE) {
                printFile(referenceLog, "Casual ", System.out);
                printFile(testLog, "Suspend", System.out);
            }
            assertEquals("golden: ", 1, golden.events.size());
            FCL.EventPair renameEvent = golden.events.get(0);
            assertEquals("golden 1:", DumpingFileChangeListener.FILE_RENAMED, renameEvent.kind);
            assertEquals("delegated: ", 2, delegate.events.size());
            FCL.EventPair createEvent, deleteEvent;
            // in suspend we don't know exact order, find it
            if (suspend && DumpingFileChangeListener.FILE_DATA_CREATED.equals(delegate.events.get(0).kind)) {
                deleteEvent = delegate.events.get(1);
                createEvent = delegate.events.get(0);
            } else {
                deleteEvent = delegate.events.get(0);
                createEvent = delegate.events.get(1);
            }
            assertEquals("create :" + createEvent, DumpingFileChangeListener.FILE_DATA_CREATED, createEvent.kind);
            assertEquals("golden vs delegate file " + createEvent, renameEvent.event.getFile(), createEvent.event.getFile());
            assertEquals("golden vs delegate time " + createEvent, renameEvent.event.getTime(), createEvent.event.getTime());
            assertEquals("delete :" + deleteEvent, DumpingFileChangeListener.FILE_DELETED, deleteEvent.kind);
            assertEquals("delete :" + deleteEvent, oldPath, deleteEvent.event.getFile().getPath());
            assertEquals("golden vs delegate source " + createEvent, renameEvent.event.getSource(), createEvent.event.getSource());
        } finally {
            removeDirectory(tempFile);
        }
    }
    
    @Test
    public void testParityNotSuspended() throws Throwable {
        doTestParity(false);
    }

    @Test
    public void testParitySuspended() throws Throwable {
        doTestParity(true);
    }

    @Test
    public void testCreateThenRemoveAsTwoEvents() throws Throwable {
        if (isDisabled()) {
            return;
        }
        String[] dirStruct = new String[]{"d real_dir_1"};
        String[] removeFile = new String[]{
            "R real_dir_1/file_1",};
        String[] addFile = new String[]{
            "- real_dir_1/file_1"
        };
        doTestParityImpl(true, dirStruct, removeFile, addFile);
    }

    private void doTestParity(boolean suspend) throws Throwable {
        if (isDisabled()) {
            return;
        }
        String[] init = new String[]{
            "d real_dir_1",
            "d real_dir_1/subdir_1",
            "d real_dir_1/subdir_1/subsub_a",};
        String[] filesStruct = new String[]{
            "- real_dir_1/file_1",
            "- real_dir_1/file_2"
        };
        doTestParityImpl(suspend, init, filesStruct);
    }

    private void doTestParityImpl(boolean suspend, String[] init, String[]... actions) throws Throwable {
        FileObject tempFO = mkTempFO(getName(), "tmp");
        final File tempFile = FileUtil.toFile(tempFO);
        try {
            createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), init);
            File workDir = getWorkDir();
            File testLog = new File(workDir, "test.dat");
            File referenceLog = new File(workDir, "reference.dat");
            FileChangeListener golden = new DumpingFileChangeListener(getName(), "", new PrintStream(referenceLog), true);
            SuspendableFileChangeListener suspendableListener = new SuspendableFileChangeListener(new DumpingFileChangeListener(getName(), "", new PrintStream(testLog), true), false);
            if (suspend) {
                suspendableListener.suspendRemoves();
            }
            FileSystemProvider.addRecursiveListener(golden, tempFO.getFileSystem(), tempFO.getPath());
            FileSystemProvider.addRecursiveListener(suspendableListener, tempFO.getFileSystem(), tempFO.getPath());

            for (String[] action : actions) {
                createDirStructure(getTestExecutionEnvironment(), tempFO.getPath(), action, false);
                FileUtil.refreshFor(tempFile);
            }
            if (suspend) {
                suspendableListener.resumeRemoves();
            }
            suspendableListener.flush();

            if (TRACE) {
                printFile(referenceLog, "Gold", System.out);
                printFile(testLog, "Test", System.out);
            }
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Wrapped and Golden events differ, see diff " + testLog.getAbsolutePath() + " " + referenceLog.getAbsolutePath(), testLog, referenceLog, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }
        } finally {
            removeDirectory(tempFile);
        }
    }
    
    protected FileObject mkTempFO(String prefix, String suffix) throws Exception {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.delete();
        tempFile.mkdirs();
        FileObject parentFO = FileUtil.toFileObject(tempFile.getParentFile());
        parentFO.refresh();
        return FileUtil.toFileObject(tempFile);
    }

}
