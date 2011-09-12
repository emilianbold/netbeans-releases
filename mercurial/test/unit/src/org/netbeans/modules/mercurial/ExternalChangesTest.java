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

package org.netbeans.modules.mercurial;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.modules.versioning.VersioningManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

/**
 * Testing cache refresh after external changes - IZ #126156
 * @author ondra
 */
public class ExternalChangesTest extends AbstractHgTestCase {

    FileObject workdirFO;
    File workdir;
    FileObject modifiedFO;
    File modifiedFile;

    public ExternalChangesTest (String arg0) {
        super(arg0);
    }

    @Override
    public void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
        MockLookup.setLayersAndInstances();

        // create
        workdirFO = FileUtil.toFileObject(workdir = getWorkTreeDir());
        File folder = new File(new File(workdir, "folder1"), "folder2");
        folder.mkdirs();
        modifiedFile = new File(folder, "file");
        VersioningManager.getInstance();
        write(modifiedFile, "");
        commit(modifiedFile);
        modifiedFO = FileUtil.toFileObject(modifiedFile);
        System.setProperty("mercurial.handleDirstateEvents", "true");
        Mercurial.STATUS_LOG.setLevel(Level.FINE);


    }

    // simple test if cache refreshes correctly
    public void testRefreshAfterFSChange () throws Exception {
        Thread.sleep(5000); // some time for initial scans to finish
        write(modifiedFile, "testRefreshAfterFSChange");
        commit(modifiedFile);
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);
    }

    // testing if dirstate events are disabled for internal commit action
    public void testInternalCommitNoEvents () throws Exception {
        Thread.sleep(5000); // some time for initial scans to finish
        write(modifiedFile, "testInternalCommitNoDirstate");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        failIfRefreshed(new HgProgressSupport() {
            @Override
            protected void perform() {
                try {
                    HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "testInternalCommitNoDirstate", NULL_LOGGER);
                } catch (HgException ex) {
                    fail(ex.getMessage());
                }
                FileUtil.refreshFor(workdir);
            }
        });
    }

    public void testExternalCommit () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        write(modifiedFile, "testExternalCommitDirstate");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("commit");
        HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "", NULL_LOGGER);
        removeNoTSRefreshCommand("commit");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);
    }

    // testing if dirstate events can be disabled with the commandline switch
    public void testNoExternalEvents () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events disabled
        System.setProperty("mercurial.handleDirstateEvents", "false");
        write(modifiedFile, "testNoExternalEvents");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("commit");
        HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "testNoExternalEvents", null);
        removeNoTSRefreshCommand("commit");
        failIfRefreshed();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        System.setProperty("mercurial.handleDirstateEvents", "true");
    }

    // change of modif TS of the .hg folder must be ignored
    public void testNoEventsOnHgFolderChange () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events disabled
        write(modifiedFile, "testNoEventsOnHgFolderChange");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        new File(workdir, ".hg").setLastModified(System.currentTimeMillis());
        failIfRefreshed();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
    }

    public void testExternalRollback () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events enabled
        write(modifiedFile, "testExternalRollback");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("commit");
        HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "testExternalRollback", null);
        removeNoTSRefreshCommand("commit");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);

        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("rollback");
        HgCommand.doRollback(workdir, null);
        removeNoTSRefreshCommand("rollback");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
    }

    public void testExternalRevert () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events enabled
        write(modifiedFile, "testExternalRevert");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("revert");
        HgCommand.doRevert(workdir, Collections.singletonList(workdir), null, false, NULL_LOGGER);
        removeNoTSRefreshCommand("revert");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);
    }

    private void waitForRefresh () throws Exception {
        InterceptorRefreshHandler handler = new InterceptorRefreshHandler();
        Mercurial.STATUS_LOG.addHandler(handler);
        FileUtil.refreshFor(workdir);
        for (int i=0; i<20; ++i) {
            Thread.sleep(1000);
            if (handler.refreshed) {
                break;
            }
        }
        if (!handler.refreshed) {
            fail("cache not refresh");
        }
        Mercurial.STATUS_LOG.removeHandler(handler);
    }

    private void failIfRefreshed () throws Exception {
        failIfRefreshed(null);
    }

    private void failIfRefreshed (HgProgressSupport supp) throws Exception {
        InterceptorRefreshHandler handler = new InterceptorRefreshHandler();
        Mercurial.STATUS_LOG.addHandler(handler);
        FileUtil.refreshFor(workdir);
        RequestProcessor.Task task = supp == null ? null : supp.start(RequestProcessor.getDefault());
        for (int i = 0; i < 25; ++i) {
            Thread.sleep(1000);
            if (handler.refreshed) {
                fail("cache refresh started: " + handler.refreshString);
            }
        }
        if (task != null) {
            task.waitFinished();
        }
        Mercurial.STATUS_LOG.removeHandler(handler);
    }

    private void addNoTSRefreshCommand (String command) throws Exception {
        Field f = HgCommand.class.getDeclaredField("REPOSITORY_NOMODIFICATION_COMMANDS");
        f.setAccessible(true);
        Set set = (Set) f.get(HgCommand.class);
        set.add(command);
    }

    private void removeNoTSRefreshCommand (String command) throws Exception {
        Field f = HgCommand.class.getDeclaredField("REPOSITORY_NOMODIFICATION_COMMANDS");
        f.setAccessible(true);
        Set set = (Set) f.get(HgCommand.class);
        set.remove(command);
    }

    private class InterceptorRefreshHandler extends Handler {
        private boolean refreshed;
        private boolean refreshStarted;
        private String refreshString;

        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            if (message.startsWith("refreshAll: starting status scan for ") && (
                    message.contains(workdirFO.getPath() + ",")
                    || message.contains(workdirFO.getPath() + "]")
                    || message.contains(modifiedFile.getParentFile().getParentFile().getAbsolutePath()))) {
                refreshStarted = true;
                refreshString = message;
            } else if (refreshStarted && message.startsWith("refreshAll: finishes status scan after ")) {
                refreshed = true;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    @org.openide.util.lookup.ServiceProvider(service=OpenProjectsTrampoline.class)
    public static class OpenProjectsTrampolineImpl implements OpenProjectsTrampoline {
        public OpenProjectsTrampolineImpl() {
        }

        public Project[] getOpenProjectsAPI() {
            return new Project[0];
        }

        public void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void closeAPI(Project[] projects) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {

        }

        public Future<Project[]> openProjectsAPI() {
            return new Future<Project[]>() {

                public boolean cancel(boolean mayInterruptIfRunning) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                public boolean isCancelled() {
                    return false;
                }

                public boolean isDone() {
                    return true;
                }

                public Project[] get() throws InterruptedException, ExecutionException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                public Project[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }

        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {

        }

        public Project getMainProject() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setMainProject(Project project) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}