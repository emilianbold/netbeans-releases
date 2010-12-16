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

package org.netbeans.libs.git.jgit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.utils.TestUtils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class AbstractGitTestCase extends NbTestCase {
    private final File workDir;
    private final File wc;
    private Repository repository;
    private final File repositoryLocation;
    private JGitRepository localRepository;
    
    public AbstractGitTestCase (String testName) throws IOException {
        super(testName);
        System.setProperty("work.dir", getWorkDirPath());
        workDir = getWorkDir();
        repositoryLocation = new File(workDir, "repo");
        wc = new File(workDir, getName() + "_wc");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        wc.mkdirs();
        initializeRepository();
    }
    
    protected  boolean createLocalClone () {
        return true;
    }

    protected File getWorkingDirectory () {
        return wc;
    }

    protected JGitRepository getLocalGitRepository () {
        return localRepository;
    }

    protected Repository getRemoteRepository () {
        return repository;
    }

    protected Repository getRepositoryForWC(File wc) throws IOException {
        return new FileRepositoryBuilder().setGitDir(Utils.getMetadataFolder(workDir)).readEnvironment().findGitDir().build();
    }

    protected void write(File file, String str) throws IOException {
        FileWriter w = null;
        try {
            w = new FileWriter(file);
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    protected String read(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(file));
            String s = r.readLine();
            if (s != null) {
                while( true ) {
                    sb.append(s);
                    s = r.readLine();
                    if (s == null) break;
                    sb.append('\n');
                }
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return sb.toString();
    }

    protected static void assertStatus (Map<File, GitStatus> statuses, File workDir, File file, boolean tracked, Status headVsIndex, Status indexVsWorking, Status headVsWorking, boolean conflict) {
        GitStatus status = statuses.get(file);
        assertNotNull(status);
        assertEquals(TestUtils.getRelativePath(file, workDir), status.getRelativePath());
        assertEquals(tracked, status.isTracked());
        assertEquals(headVsIndex, status.getStatusHeadIndex());
        assertEquals(indexVsWorking, status.getStatusIndexWC());
        assertEquals(headVsWorking, status.getStatusHeadWC());
        assertEquals(conflict, status.isConflict());
    }

    protected Repository getRepository (JGitRepository gitRepo) throws Exception {
        Field f = JGitRepository.class.getDeclaredField("repository");
        f.setAccessible(true);
        return (Repository) f.get(gitRepo);
    }

//    protected GitRepository cloneRemoteRepository (File target) throws GitException {
//        return GitRepository.cloneRepository(target, repositoryLocation.getAbsolutePath(), null);
//    }

    private void initializeRepository() throws Exception {
        repository = new FileRepositoryBuilder().setGitDir(Utils.getMetadataFolder(repositoryLocation)).readEnvironment().findGitDir().build();
        repository.create(true);

        if (createLocalClone()) {
            JGitClientFactory fact = JGitClientFactory.getInstance();
            fact.getClient(wc).init(ProgressMonitor.NULL_PROGRESS_MONITOR);
            Field f = JGitClientFactory.class.getDeclaredField("repositoryPool");
            f.setAccessible(true);
            localRepository = ((Map<File, JGitRepository>) f.get(fact)).get(wc);
        }
    }

    protected GitClient getClient (File repository) throws GitException {
        return JGitClientFactory.getInstance().getClient(repository);
    }

    protected void add (File... files) throws GitException {
        getClient(wc).add(files, ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    protected void commit (File... files) throws GitException {
        getClient(wc).commit(files, "commit", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    protected void remove (boolean cached, File... files) throws GitException {
        getClient(wc).remove(files, cached, ProgressMonitor.NULL_PROGRESS_MONITOR);
    }

    protected void copyFile(File source, File target) throws IOException {
        target.getParentFile().mkdirs();
        if (source.isDirectory()) {
            File[] children = source.listFiles();
            for (File child : children) {
                copyFile(child, new File(target, child.getName()));
            }
        } else if (source.isFile()) {
            target.createNewFile();
            String s = read(source);
            if (s != null) {
                write(target, s);
            }
        }
    }
    
    protected void clearRepositoryPool() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        JGitClientFactory.getInstance().clearRepositoryPool();
    }

    protected static class Monitor extends ProgressMonitor.DefaultProgressMonitor implements FileListener {
        public final HashSet<File> notifiedFiles = new HashSet<File>();
        public final List<String> notifiedWarnings = new LinkedList<String>();
        private boolean barrierAccessed;
        public int count;
        public volatile boolean cont;

        public Monitor () {
            cont = true;
        }

        @Override
        public void notifyFile (File file, String relativePathToRoot) {
            notifiedFiles.add(file);
            barrierReached();
        }

        @Override
        public void notifyError(String message) {
            fail(message);
        }

        @Override
        public void notifyWarning (String message) {
            notifiedWarnings.add(message);
        }

        private void barrierReached() {
            barrierAccessed = true;
            ++count;
            while (!cont) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        public void waitAtBarrier() throws InterruptedException {
            for (int i = 0; i < 100; ++i) {
                if (barrierAccessed) {
                    break;
                }
                Thread.sleep(100);
            }
            assertTrue(barrierAccessed);
        }
    }
    
    public static JGitClientFactory getJGitClientFactory() {
        return JGitClientFactory.getInstance();
    }
}
