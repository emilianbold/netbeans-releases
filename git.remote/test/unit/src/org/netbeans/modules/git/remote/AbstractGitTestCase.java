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

package org.netbeans.modules.git.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitRepository;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author ondra
 */
public abstract class AbstractGitTestCase extends NbTestCase {

    protected VCSFileProxy repositoryLocation;
    
    protected static final String NULL_OBJECT_ID = "0000000000000000000000000000000000000000";

    public AbstractGitTestCase (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        VCSFileProxy workDir = VCSFileProxy.createFileProxy(getWorkDir());
        VCSFileProxy userdir = VCSFileProxy.createFileProxy(workDir.getParentFile(), "userdir");
        VCSFileProxySupport.mkdirs(userdir);
        System.setProperty("netbeans.user", userdir.getPath());
        super.setUp();
        repositoryLocation = VCSFileProxy.createFileProxy(workDir, "work");
        clearWorkDir();
        VCSFileProxySupport.mkdirs(repositoryLocation);
        getClient(repositoryLocation).init(GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy repositoryMetadata = VCSFileProxy.createFileProxy(repositoryLocation, ".git");
        assertTrue(repositoryMetadata.exists());
    }
    
    protected VCSFileProxy getRepositoryLocation() {
        return repositoryLocation;
    }
    
    protected VCSFileProxy createFolder(String name) throws IOException {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject folder = wd.createFolder(name);
        return VCSFileProxy.createFileProxy(folder);
    }

    protected VCSFileProxy createFolder(VCSFileProxy parent, String name) throws IOException {
        FileObject parentFO = parent.toFileObject();
        FileObject folder = parentFO.createFolder(name);
        return VCSFileProxy.createFileProxy(folder);
    }

    protected VCSFileProxy createFile(VCSFileProxy parent, String name) throws IOException {
        FileObject parentFO = parent.toFileObject();
        FileObject fo = parentFO.createData(name);
        return VCSFileProxy.createFileProxy(fo);
    }

    protected VCSFileProxy createFile(String name) throws IOException {
        return createFile(VCSFileProxy.createFileProxy(getWorkDir()), name);
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

    protected String read (VCSFileProxy file) throws IOException {
        BufferedReader r = null;
        try {
            StringBuilder sb = new StringBuilder();
            r = new BufferedReader(new InputStreamReader(file.getInputStream(false), "UTF-8"));
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    protected FileStatusCache getCache () {
        return Git.getInstance().getFileStatusCache();
    }

    protected GitClient getClient (VCSFileProxy repositoryLocation) throws GitException {
        return GitRepository.getInstance(repositoryLocation).createClient();
    }

    protected void add (VCSFileProxy... files) throws GitException {
        getClient(repositoryLocation).add(files == null ? new VCSFileProxy[0] : files, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected void commit (VCSFileProxy... files) throws GitException {
        getClient(repositoryLocation).commit(files == null ? new VCSFileProxy[0] : files, "blablabla", null, null, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected void delete (boolean cached, VCSFileProxy... files) throws GitException {
        getClient(repositoryLocation).remove(files == null ? new VCSFileProxy[0] : files, cached, GitUtils.NULL_PROGRESS_MONITOR);
    }

    protected VCSFileProxy initSecondRepository () throws GitException {
        VCSFileProxy secondRepositoryFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "work_2"); //NOI18N
        VCSFileProxySupport.mkdirs(secondRepositoryFolder);
        getClient(secondRepositoryFolder).init(GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(secondRepositoryFolder.isDirectory());
        return secondRepositoryFolder;
    }

    protected class StatusRefreshLogHandler extends Handler {
        private Set<VCSFileProxy> filesToRefresh;
        private boolean filesRefreshed;
        private final HashSet<VCSFileProxy> refreshedFiles = new HashSet<VCSFileProxy>();
        private final VCSFileProxy topFolder;
        private final Set<String> interestingFiles = new HashSet<String>();
        boolean active;

        public StatusRefreshLogHandler (VCSFileProxy topFolder) {
            this.topFolder = topFolder;
        }

        @Override
        public void publish(LogRecord record) {
            if (!active) {
                return;
            }
            if (record.getMessage().contains("refreshAllRoots() roots: finished")) {
                synchronized (this) {
                    if (refreshedFiles.containsAll(filesToRefresh)) {
                        filesRefreshed = true;
                        notifyAll();
                    }
                }
            } else if (record.getMessage().contains("refreshAllRoots() roots: ")) {
                synchronized (this) {
                    for (VCSFileProxy f : (Set<VCSFileProxy>) record.getParameters()[0]) {
                        if (f.getPath().startsWith(topFolder.getPath()))
                        refreshedFiles.add(f);
                    }
                    notifyAll();
                }
            } else if (record.getMessage().equals("refreshAllRoots() file status: {0} {1}")) {
                interestingFiles.add((String) record.getParameters()[0]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public void setFilesToRefresh (Set<VCSFileProxy> files) {
            active = false;
            filesRefreshed = false;
            refreshedFiles.clear();
            filesToRefresh = files;
            interestingFiles.clear();
            active = true;
        }

        public boolean waitForFilesToRefresh () throws InterruptedException {
            for (int i = 0; i < 50; ++i) {
                synchronized (this) {
                    if (filesRefreshed) {
                        return true;
                    }
                    wait(500);
                }
            }
            return false;
        }

        public boolean getFilesRefreshed () {
            return filesRefreshed;
        }

        Set<String> getInterestingFiles () {
            return new HashSet<String>(interestingFiles);
        }

    }

    protected final void runExternally (VCSFileProxy workdir, List<String> command) throws Exception {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        String[] args = command.toArray(new String[command.size()]);
        org.netbeans.api.extexecution.ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(workdir);
        VCSFileProxySupport.mkdirs(workdir);
        ProcessUtils.ExitStatus executeInDir = ProcessUtils.executeInDir(workdir.getPath(), null, false, canceled, processBuilder, "git", args);
        if (!executeInDir.error.isEmpty()) {
            throw new Exception(executeInDir.error);
        }
    }
}
