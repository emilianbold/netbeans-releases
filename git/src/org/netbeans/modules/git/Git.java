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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClientFactory;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.RootsToFile;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public final class Git {

    private static Git instance;
    private Annotator annotator;
    private FilesystemInterceptor interceptor;
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.git"); //NOI18N
    public static final Logger STATUS_LOG = Logger.getLogger("org.netbeans.modules.git.status"); //NOI18N;
    private GitVCS gitVCS;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private FileStatusCache fileStatusCache;
    private HashMap<File, RequestProcessor> processorsToUrl;
    public static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged"; // NOI18N
    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged"; // NOI18N

    private RootsToFile rootsToFile;
    
    public Git () {
    }

    public static synchronized Git getInstance () {
        if (instance == null) {
            instance = new Git();
            instance.init();
        }
        return instance;
    }

    private void init() {
        fileStatusCache = new FileStatusCache();
        annotator = new Annotator();
        interceptor = new FilesystemInterceptor();
        gitVCS = Lookup.getDefault().lookup(GitVCS.class);
        fileStatusCache.addPropertyChangeListener(gitVCS);
        addPropertyChangeListener(gitVCS);

        int statisticsFrequency;
        String s = System.getProperty("git.root.stat.frequency", "0"); //NOI18N
        try {
            statisticsFrequency = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            statisticsFrequency = 0;
        }
        rootsToFile = new RootsToFile(new RootsToFile.Callback() {
            @Override
            public boolean repositoryExistsFor (File file) {
                return GitUtils.repositoryExistsFor(file);
            }

            @Override
            public File getTopmostManagedAncestor (File file) {
                return gitVCS.getTopmostManagedAncestor(file, false);
            }
        }, Logger.getLogger("org.netbeans.modules.git.RootsToFile"), statisticsFrequency); //NOI18N
    }

    public VCSAnnotator getVCSAnnotator() {
        return annotator;
    }

    FilesystemInterceptor getVCSInterceptor() {
        return interceptor;
    }

    void getOriginalFile (File workingCopy, File originalFile) {
        File repository = getRepositoryRoot(workingCopy);
        if (repository != null) {
            try {
                GitClient client = getClient(repository);
                if (!client.catFile(workingCopy, GitUtils.HEAD, new FileOutputStream(originalFile), ProgressMonitor.NULL_PROGRESS_MONITOR)) {
                    originalFile.delete();
                }
            } catch (java.io.FileNotFoundException ex) {
                LOG.log(Level.SEVERE, "Parent folder [{0}] does not exist", originalFile.getParentFile()); //NOI18N
                LOG.log(Level.SEVERE, null, ex);
            } catch (GitException.MissingObjectException ex) {
                LOG.log(Level.FINE, null, ex); //NOI18N
                originalFile.delete();
            } catch (GitException ex) {
                LOG.log(Level.INFO, "Error retrieving file", ex); //NOI18N
                originalFile.delete();
            }
        }
    }

    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status.

     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */
    public boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof GitVCS && !GitUtils.isPartOfGitMetadata(file);
    }

    public FileStatusCache getFileStatusCache() {
        return fileStatusCache;
    }

    public File getRepositoryRoot (File file) {
        return rootsToFile.getRepositoryRoot(file);
    }

    public GitClient getClient (File repository) throws GitException {
        return getClient(repository, null);
    }

    public GitClient getClient (File repository, GitProgressSupport progressSupport) throws GitException {
        // get the only instance for the repository folder, so we can synchronize on it
        File repositoryFolder = getRepositoryRoot(repository);
        if (repositoryFolder != null) {
            repository = repositoryFolder;
        }
        GitClient client = GitClientFactory.getInstance(null).getClient(repository);
        GitClientInvocationHandler handler = new GitClientInvocationHandler(client, repository);
        handler.setProgressSupport(progressSupport);
        return (GitClient) Proxy.newProxyInstance(GitClient.class.getClassLoader(), new Class[] { GitClient.class }, handler);
    }

    public RequestProcessor getRequestProcessor() {
        return getRequestProcessor(null);
    }

    /**
     * @param  repositoryRoot  repository root or {@code null}
     */
    public RequestProcessor getRequestProcessor (File repositoryRoot) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<File, RequestProcessor>();
        }

        RequestProcessor rp = processorsToUrl.get(repositoryRoot);
        if (rp == null) {
            if(repositoryRoot == null) {
                String rpName = "Git - ANY_KEY";//NOI18N
                rp = new RequestProcessor(rpName, 50, true);                
            } else {    
                String rpName = "Git - " + repositoryRoot.toString();//NOI18N
                rp = new RequestProcessor(rpName, 1, true);
            }
            processorsToUrl.put(repositoryRoot, rp);
        }
        return rp;
    }

    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void headChanged (Set<File> files) {
        gitVCS.refreshStatus(files);
    }

    public void versionedFilesChanged () {
        rootsToFile.clear();
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    /**
     * Refreshes cached modification timestamp of the repository's metadata
     * @param repository owner of the metadata to refresh
     */
    public void refreshWorkingCopyTimestamp (File repository) {
        getVCSInterceptor().refreshMetadataTimestamp(repository);
    }

    public void connectRepository (File repository) {
        gitVCS.connectRepository(repository);
        versionedFilesChanged();
    }

    public void disconnectRepository (File repository) {
        gitVCS.disconnectRepository(repository);
        versionedFilesChanged();
    }

    public boolean isDisconnected (File repository) {
        return gitVCS.isDisconnected(repository);
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    public Set<File> getSeenRoots (File repositoryRoot) {
        return getVCSInterceptor().getSeenRoots(repositoryRoot);
    }
}
