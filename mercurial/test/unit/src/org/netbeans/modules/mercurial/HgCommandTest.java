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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author ondra
 */
public class HgCommandTest extends AbstractHgTestCase {

    public HgCommandTest(String arg0) throws IOException {
        super(arg0);
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
    }

    public void testDisabledIndexing () throws Exception {
        CommandHandler handler = new CommandHandler();
        Mercurial.LOG.addHandler(handler);
        Mercurial.LOG.setLevel(Level.ALL);
        File newRepo = new File(getTempDir(), "repo");
        List<File> repoAsList = Collections.singletonList(newRepo);
        handler.reset("clone", repoAsList);
        commit(getWorkTreeDir());
        HgCommand.doClone(getWorkTreeDir(), newRepo, NULL_LOGGER);
        handler.assertResults(1);
        
        handler.reset();
        getCache().refresh(newRepo);
        handler.assertResults(0);

        FileObject fileFO;
        handler.reset();
        File fol = new File(newRepo, "folder1");
        fol.mkdirs();
        File file = new File(fol, "file1");
        file.createNewFile();
        fileFO = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        FileObject folderFO = fileFO.getParent();
        FileObject repoFO = folderFO.getParent();
        
        HgCommand.doAdd(newRepo, file, NULL_LOGGER);
        HgCommand.doCommit(newRepo, Collections.singletonList(file), "blabla", NULL_LOGGER);
        String revision = HgCommand.doTip(newRepo, NULL_LOGGER).getCSetShortID();
        write(file, "hello");
        HgCommand.doCommit(newRepo, Collections.singletonList(file), "blabla", NULL_LOGGER);
        handler.assertResults(0);

        // *************** REVERT *************** //
        Thread.sleep(2000); // give some time so modification timestamps differ

        RefreshProbe refreshProbe = new RefreshProbe(fileFO);
        handler.reset("revert", Collections.singletonList(fol));
        refreshProbe.reset();
        HgCommand.doRevert(newRepo, Collections.singletonList(fol), revision, false, NULL_LOGGER);
        refreshProbe.checkRefresh(true);
        handler.assertResults(1);

        // *************** REVERT *************** //
        Thread.sleep(2000); // give some time so modification timestamps differ
        
        handler.reset("revert", Collections.singletonList(fol));
        refreshProbe.reset();
        HgCommand.doRevert(newRepo, Collections.singletonList(fol), null, false, NULL_LOGGER);
        refreshProbe.checkRefresh(true);
        handler.assertResults(1);

        // *************** UPDATE *************** //
        Thread.sleep(2000); // give some time so modification timestamps differ

        handler.reset("update", repoAsList);
        refreshProbe.reset();
        HgCommand.doUpdateAll(newRepo, true, revision);
        refreshProbe.checkRefresh(true);
        handler.assertResults(1);

        // *************** UPDATE *************** //
        Thread.sleep(2000); // give some time so modification timestamps differ

        handler.reset("update", repoAsList);
        refreshProbe.reset();
        HgCommand.doUpdateAll(newRepo, true, "tip");
        refreshProbe.checkRefresh(true);
        handler.assertResults(1);

        // *************** BACKOUT *************** //
        Thread.sleep(2000); // give some time so modification timestamps differ

        handler.reset("backout", repoAsList);
        write(file, "backout test");
        revision = HgCommand.doTip(newRepo, NULL_LOGGER).getCSetShortID();
        HgCommand.doCommit(newRepo, Collections.singletonList(file), "backout test", NULL_LOGGER);
        Thread.sleep(2000); // give some time so modification timestamps differ
        String message = "Backout";
        refreshProbe.reset();
        HgCommand.doBackout(newRepo, "tip", false, message, NULL_LOGGER);
        refreshProbe.checkRefresh(true);
        assertEquals(message, HgCommand.doTip(newRepo, NULL_LOGGER).getMessage());
        handler.assertResults(1);

        // *************** INTER-REPOSITORY-COMMANDS *************** //

        // create a file in original repo
        File mainFol = createFolder("folder2");
        File mainFile;
        commit(mainFile = createFile(mainFol, "file2"));
        // fetch the changes to the clone
        handler.reset("fetch", repoAsList);
        HgCommand.doFetch(newRepo, new HgURL(getWorkTreeDir()), NULL_LOGGER);
        handler.assertResults(1);

        Thread.sleep(2000); // give some time so modification timestamps differ
        folderFO = repoFO.getFileObject(mainFol.getName());
        fileFO = folderFO.getFileObject(mainFile.getName());

        // *************** FETCH *************** //
        // do changes in the default repo
        write(mainFile, "fetch test");
        commit(mainFile);
        // fetch
        handler.reset("fetch", repoAsList);
        refreshProbe = new HgCommandTest.RefreshProbe(fileFO);
        refreshProbe.reset();
        HgCommand.doFetch(newRepo, new HgURL(getWorkTreeDir()), NULL_LOGGER);
        refreshProbe.checkRefresh(true);
        handler.assertResults(1);

        Thread.sleep(2000); // give some time so modification timestamps differ

        // *************** PULL *************** //
        // pull without update - no refresh, refreshed in merge
        handler.reset();
        write(mainFile, "pull test");
        commit(mainFile);
        revision = HgCommand.doTip(getWorkTreeDir(), NULL_LOGGER).getCSetShortID();
        handler.assertResults(0);
        handler.reset("pull", repoAsList);
        HgCommand.doPull(newRepo, NULL_LOGGER);
        handler.assertResults(1);

        // *************** MERGE *************** //
        Thread.sleep(2000); // give some time so modification timestamps differ

        handler.reset("merge", repoAsList);
        refreshProbe.reset();
        HgCommand.doMerge(newRepo, revision);
        refreshProbe.checkRefresh(true);
        HgCommand.doCommit(newRepo, Collections.EMPTY_LIST, "after merge", NULL_LOGGER);
        handler.assertResults(1);

        // *************** IMPORT DIFF *************** //
        Thread.sleep(2000); // give some time so modification timestamps differ

        write(mainFile, "import diff test");
        commit(mainFile);
        revision = HgCommand.doTip(getWorkTreeDir(), NULL_LOGGER).getCSetShortID();
        File diffFile = new File(getTempDir(), "export.patch");
        HgCommand.doExport(getWorkTreeDir(), revision, diffFile.getAbsolutePath(), NULL_LOGGER);
        assertTrue(diffFile.exists());

        handler.reset("import", repoAsList);
        refreshProbe.reset();
        HgCommand.doImport(newRepo, diffFile, NULL_LOGGER);
        refreshProbe.checkRefresh(true);
        handler.assertResults(1);

        // *************** PULL *************** //
        // prepare - sync changes
        HgCommand.doFetch(newRepo, new HgURL(getWorkTreeDir()), NULL_LOGGER);
        HgCommand.doPush(newRepo, new HgURL(getWorkTreeDir()), NULL_LOGGER, false);
        HgCommand.doUpdateAll(getWorkTreeDir(), false, null);
        Thread.sleep(2000); // give some time so modification timestamps differ
        // pull without update - no refresh, refreshed in merge
        write(mainFile, "pull test with refresh");
        commit(mainFile);
        handler.reset("pull", repoAsList);
        refreshProbe.reset();
        revision = HgCommand.doTip(getWorkTreeDir(), NULL_LOGGER).getCSetShortID();
        HgCommand.doPull(newRepo, NULL_LOGGER);
        refreshProbe.checkRefresh(true);
        handler.assertResults(1);
    }

    private class CommandHandler extends Handler {

        private String expectedCommand;
        private int occurrences;
        private boolean commandInvoked;
        List files;
        List<File> expectedFiles;

        public void reset (String expectedCommand, List<File> expectedFiles) {
            reset();
            this.expectedCommand = expectedCommand;
            this.expectedFiles = expectedFiles;
        }

        public void reset () {
            occurrences = 0;
            commandInvoked = false;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().startsWith("Running command with disabled indexing:")) {
                ++occurrences;
                if (record.getParameters() != null && record.getParameters().length > 1
                        && record.getParameters()[0].toString().equals(expectedCommand)) {
                    commandInvoked = true;
                    if (record.getParameters()[1] instanceof List) {
                        files = (List)record.getParameters()[1];
                    }
                }
            }
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }

        public void assertResults (int occurrences) {
            assertTrue(occurrences == this.occurrences);
            assertTrue(occurrences == 0 || this.commandInvoked);
            assertEquals(files, expectedFiles);
        }
    }

    private static File tmp = null;
    private File getTempDir() {
        if(tmp == null) {
            File tmpDir;
            try {
                tmpDir = getWorkDir();
                tmp = new File(tmpDir, "gtmt-" + Long.toString(System.currentTimeMillis()));
                tmp.deleteOnExit();
            } catch (IOException ex) {

            }
        }
        return tmp;
    }

    private class RefreshProbe {
        private final FileObject fo;
        private long lastModified;

        public RefreshProbe(FileObject fo) {
            this.fo = fo;
        }

        void reset () {
            lastModified = fo.lastModified().getTime();
        }

        void checkRefresh (boolean refreshAllowed) throws Exception {
            boolean refreshed = false;
            for (int i = 0; i < 5; ++i) {
                Thread.sleep(1000);
                if (fo.lastModified().getTime() > lastModified) {
                    refreshed = true;
                    break;
                }
            }
            assert refreshed == refreshAllowed;
        }
    }
}
