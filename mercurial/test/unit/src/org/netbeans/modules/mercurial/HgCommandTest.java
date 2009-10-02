/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/**
 *
 * @author ondra
 */
public class HgCommandTest extends AbstractHgTest {

    public HgCommandTest(String arg0) throws IOException {
        super(arg0);
        System.setProperty("netbeans.user", getTempDir().getAbsolutePath());
    }

    public void testDisabledIndexing () throws Exception {
        CommandHandler handler = new CommandHandler();
        Mercurial.LOG.addHandler(handler);
        Mercurial.LOG.setLevel(Level.ALL);
        File newRepo = new File(getTempDir(), "repo");
        List<File> repoAsList = Collections.singletonList(newRepo);
        handler.reset("clone", repoAsList);
        commit(getWorkDir());
        HgCommand.doClone(getWorkDir(), newRepo, NULL_LOGGER);
        handler.assertResults(1);
        
        handler.reset();
        getCache().refreshAll(newRepo);
        handler.assertResults(0);

        handler.reset();
        File fol = new File(newRepo, "folder1");
        fol.mkdirs();
        File file = new File(fol, "file1");
        file.createNewFile();
        HgCommand.doAdd(newRepo, file, NULL_LOGGER);
        HgCommand.doCommit(newRepo, Collections.singletonList(file), "blabla", NULL_LOGGER);
        String revision = HgCommand.doTip(newRepo, NULL_LOGGER).getCSetShortID();
        write(file, "hello");
        HgCommand.doCommit(newRepo, Collections.singletonList(file), "blabla", NULL_LOGGER);
        handler.assertResults(0);

        handler.reset("revert", Collections.singletonList(fol));
        HgCommand.doRevert(newRepo, Collections.singletonList(fol), revision, false, NULL_LOGGER);
        handler.assertResults(1);
        
        handler.reset("revert", Collections.singletonList(fol));
        HgCommand.doRevert(newRepo, Collections.singletonList(fol), null, false, NULL_LOGGER);
        handler.assertResults(1);

        handler.reset("update", repoAsList);
        HgCommand.doUpdateAll(newRepo, true, null);
        handler.assertResults(1);

        handler.reset("revert", Collections.singletonList(fol));
        HgCommand.doRevert(newRepo, Collections.singletonList(fol), null, false, NULL_LOGGER);
        handler.assertResults(1);

        handler.reset("fetch", repoAsList);
        HgCommand.doFetch(newRepo, new HgURL(getWorkDir()), NULL_LOGGER);
        handler.assertResults(1);

        handler.reset();
        fol = createFolder("folder2");
        commit(createFile(fol, "file2"));
        revision = HgCommand.doTip(getWorkDir(), NULL_LOGGER).getCSetShortID();
        handler.assertResults(0);
        handler.reset("pull", repoAsList);
        HgCommand.doPull(newRepo, NULL_LOGGER);
        handler.assertResults(1);

        handler.reset("merge", repoAsList);
        HgCommand.doMerge(newRepo, revision);
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
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            tmp = new File(tmpDir, "gtmt-" + Long.toString(System.currentTimeMillis()));
            tmp.deleteOnExit();
        }
        return tmp;
    }
}
