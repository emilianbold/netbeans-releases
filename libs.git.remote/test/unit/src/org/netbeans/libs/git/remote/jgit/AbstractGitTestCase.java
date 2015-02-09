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

package org.netbeans.libs.git.remote.jgit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.fail;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.git.remote.ApiUtils;
import org.netbeans.libs.git.remote.GitClient;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitRepository;
import org.netbeans.libs.git.remote.GitStatus;
import org.netbeans.libs.git.remote.GitStatus.Status;
import org.netbeans.libs.git.remote.jgit.utils.TestUtils;
import org.netbeans.libs.git.remote.progress.FileListener;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.libs.git.remote.progress.StatusListener;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author ondra
 */
public class AbstractGitTestCase extends NbTestCase {
    private final VCSFileProxy workDir;
    private final VCSFileProxy wc;
    private Repository repository;
    private final VCSFileProxy repositoryLocation;
    private JGitRepository localRepository;
    protected static final ProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor ();
    
    public AbstractGitTestCase (String testName) throws IOException {
        super(testName);
        System.setProperty("work.dir", getWorkDirPath());
        workDir = VCSFileProxy.createFileProxy(getWorkDir());
        repositoryLocation = VCSFileProxy.createFileProxy(workDir, "repo");
        wc = VCSFileProxy.createFileProxy(workDir, getName() + "_wc");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        VCSFileProxySupport.mkdirs(wc);
        initializeRepository();
    }
    
    protected  boolean createLocalClone () {
        return true;
    }

    protected VCSFileProxy getWorkingDirectory () {
        return wc;
    }

    protected JGitRepository getLocalGitRepository () {
        return localRepository;
    }

    protected Repository getRemoteRepository () {
        return repository;
    }

    protected Repository getRepositoryForWC(VCSFileProxy wc) throws IOException {
        return new FileRepositoryBuilder().setGitDir(Utils.getMetadataFolder(wc).toFile()).readEnvironment().findGitDir().build();
    }

    protected void write(VCSFileProxy file, String str) throws IOException {
        OutputStreamWriter w = null;
        try {
            w = new OutputStreamWriter(VCSFileProxySupport.getOutputStream(file));
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    protected String read(VCSFileProxy file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(file.getInputStream(false)));
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

    protected static void assertStatus (Map<VCSFileProxy, GitStatus> statuses, VCSFileProxy workDir, VCSFileProxy file, boolean tracked, Status headVsIndex, Status indexVsWorking, Status headVsWorking, boolean conflict) {
        GitStatus status = statuses.get(file);
        assertNotNull(file.getPath() + " not in " + statuses.keySet(), status);
        assertEquals(TestUtils.getRelativePath(file, workDir), status.getRelativePath());
        assertEquals(tracked, status.isTracked());
        assertEquals(headVsIndex, status.getStatusHeadIndex());
        assertEquals(indexVsWorking, status.getStatusIndexWC());
        assertEquals(headVsWorking, status.getStatusHeadWC());
        assertEquals(conflict, status.isConflict());
    }

    protected Repository getRepository (JGitRepository gitRepo) {
        return gitRepo.getRepository();
    }

    protected Repository getRepository (GitClient client) throws Exception {
        Field f = GitClient.class.getDeclaredField("gitRepository");
        f.setAccessible(true);
        return ((JGitRepository) f.get(client)).getRepository();
    }

//    protected GitRepository cloneRemoteRepository (File target) throws GitException {
//        return GitRepository.cloneRepository(target, repositoryLocation.getAbsolutePath(), null);
//    }

    private void initializeRepository() throws Exception {
        repository = new FileRepositoryBuilder().setGitDir(Utils.getMetadataFolder(repositoryLocation).toFile()).readEnvironment().findGitDir().build();
        repository.create(true);

        if (createLocalClone()) {
            GitRepository fact = GitRepository.getInstance(wc);
            GitClient client = fact.createClient();
            client.init(NULL_PROGRESS_MONITOR);
            Field f = GitRepository.class.getDeclaredField("gitRepository");
            f.setAccessible(true);
            localRepository = (JGitRepository) f.get(fact);
            client.release();
        }
    }

    protected GitClient getClient (VCSFileProxy repository) throws GitException {
        return GitRepository.getInstance(repository).createClient();
    }

    protected void add (VCSFileProxy... files) throws GitException {
        getClient(wc).add(files, NULL_PROGRESS_MONITOR);
    }

    protected void commit (VCSFileProxy... files) throws GitException {
        getClient(wc).commit(files, "commit", null, null, NULL_PROGRESS_MONITOR);
    }

    protected void remove (boolean cached, VCSFileProxy... files) throws GitException {
        getClient(wc).remove(files, cached, NULL_PROGRESS_MONITOR);
    }

    protected void copyFile(VCSFileProxy source, VCSFileProxy target) throws IOException {
        VCSFileProxySupport.mkdirs(target.getParentFile());
        if (source.isDirectory()) {
            VCSFileProxy[] children = source.listFiles();
            for (VCSFileProxy child : children) {
                copyFile(child, VCSFileProxy.createFileProxy(target, child.getName()));
            }
        } else if (source.isFile()) {
            VCSFileProxySupport.createNew(target);
            String s = read(source);
            if (s != null) {
                write(target, s);
            }
        }
    }
    
    protected void clearRepositoryPool() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        ApiUtils.clearRepositoryPool();
    }

    protected boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }

    protected static void assertDirCacheEntry (Repository repository, VCSFileProxy workDir, Collection<VCSFileProxy> files) throws IOException {
        DirCache cache = repository.lockDirCache();
        for (VCSFileProxy f : files) {
            String relativePath = Utils.getRelativePath(workDir, f);
            DirCacheEntry e = cache.getEntry(relativePath);
            assertNotNull(e);
            assertEquals(relativePath, e.getPathString());
            if (f.lastModified() != e.getLastModified()) {
                assertEquals((f.lastModified() / 1000) * 1000, (e.getLastModified() / 1000) * 1000);
            }
            try (InputStream in = f.getInputStream(false)) {
                assertEquals(e.getObjectId(), repository.newObjectInserter().idFor(Constants.OBJ_BLOB, VCSFileProxySupport.length(f), in));
            }
            if (e.getLength() == 0 && VCSFileProxySupport.length(f) != 0) {
                assertTrue(e.isSmudged());
            } else {
                assertEquals(VCSFileProxySupport.length(f), e.getLength());
            }
        }
        cache.unlock();
    }
    
    protected long getLinkLastModified(VCSFileProxy link) throws IOException {
        File javaFile = link.toFile();
        return Files.readAttributes(Paths.get(javaFile.getAbsolutePath()), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).lastModifiedTime().toMillis();
    }
    
    protected void assertFile(VCSFileProxy test, String pass) throws IOException {
        String message = "Difference between " + test + " and golden string";
        assertFile(message, test, pass);
    }

    private void assertFile(String message, VCSFileProxy test, String pass) throws IOException {
        String testString = read(test);
        assertTrue(message, testString.equals(pass));
    }

    protected static class Monitor extends ProgressMonitor.DefaultProgressMonitor implements FileListener {
        public final HashSet<VCSFileProxy> notifiedFiles = new HashSet<VCSFileProxy>();
        public final List<String> notifiedWarnings = new LinkedList<String>();
        private boolean barrierAccessed;
        public int count;
        public volatile boolean cont;

        public Monitor () {
            cont = true;
        }

        @Override
        public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
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
    
    private static class NullProgressMonitor extends ProgressMonitor {

        @Override
        public boolean isCanceled () {
            return false;
        }

        @Override
        public void started (String command) {
        }

        @Override
        public void finished () {
        }

        @Override
        public void preparationsFailed (String message) {
        }

        @Override
        public void notifyError (String message) {
        }

        @Override
        public void notifyWarning (String message) {
        }

    }
    
    protected final List<String> runExternally (VCSFileProxy workdir, List<String> command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(System.getenv());
        pb.directory(workdir.toFile());
        Process p = pb.start();
        final BufferedReader outReader = new BufferedReader(Channels.newReader(Channels.newChannel(p.getInputStream()), "UTF-8"));
        final BufferedReader errReader = new BufferedReader(Channels.newReader(Channels.newChannel(p.getErrorStream()), "UTF-8"));
        final List<String> output = new LinkedList<String>();
        final List<String> err = new LinkedList<String>();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run () {
                try {
                    for (String line = outReader.readLine(); line != null; line = outReader.readLine()) {
                        output.add(line);
                    }
                } catch (IOException ex) {
                    
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run () {
                try {
                    for (String line = errReader.readLine(); line != null; line = errReader.readLine()) {
                        err.add(line);
                    }
                } catch (IOException ex) {
                    
                }
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        p.waitFor();
        outReader.close();
        errReader.close();
        if (!err.isEmpty()) {
            throw new Exception(err.toString());
        }
        return output;
    }
    
    protected void assertStatus(Map<VCSFileProxy, GitStatus> statuses, VCSFileProxy repository, VCSFileProxy file, boolean tracked, Status headVsIndex, Status indexVsWorking, Status headVsWorking, boolean conflict, TestStatusListener monitor) {
        assertStatus(statuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
        assertStatus(monitor.notifiedStatuses, repository, file, tracked, headVsIndex, indexVsWorking, headVsWorking, conflict);
    }

    protected static class TestStatusListener implements StatusListener {
        private final Map<VCSFileProxy, GitStatus> notifiedStatuses;

        public TestStatusListener() {
            notifiedStatuses = new HashMap<VCSFileProxy, GitStatus>();
        }

        @Override
        public void notifyStatus(GitStatus status) {
            notifiedStatuses.put(status.getFile(), status);
        }
    }
}
