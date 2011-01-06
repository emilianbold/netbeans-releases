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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.git.utils.GitUtils;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author ondra
 */
@ServiceProviders({@ServiceProvider(service=VersioningSystem.class), @ServiceProvider(service=GitVCS.class)})
public class GitVCS extends VersioningSystem implements PropertyChangeListener {

    private Set<File> knownRoots = Collections.synchronizedSet(new HashSet<File>());
    private final Set<File> unversionedParents = Collections.synchronizedSet(new HashSet<File>(20));
    private final static String PROP_PRIORITY = "Integer VCS.Priority"; //NOI18N
    private final static Integer priority = Utils.getPriority("git"); //NOI18N
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.git.GitVCS"); //NOI18N
    private final Set<File> disconnectedRepositories;

    public GitVCS() {
        putProperty(PROP_DISPLAY_NAME, org.openide.util.NbBundle.getMessage(GitVCS.class, "CTL_Git_DisplayName")); // NOI18N
        putProperty(PROP_MENU_LABEL, org.openide.util.NbBundle.getMessage(GitVCS.class, "CTL_Git_MainMenu")); // NOI18N
        putProperty(PROP_PRIORITY, priority);
        this.disconnectedRepositories = initializeDisconnectedRepositories();
    }

    @Override
    public File getTopmostManagedAncestor(File file) {
        return getTopmostManagedAncestor(file, true);
    }

    public File getTopmostManagedAncestor (File file, boolean skipDisconnectedRepositories) {
        long t = System.currentTimeMillis();
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        if(unversionedParents.contains(file)) {
            LOG.fine(" cached as unversioned");
            return null;
        }
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        File parent = getKnownParent(file);
        if(parent != null) {
            if (skipDisconnectedRepositories && isDisconnected(parent)) {
                LOG.log(Level.FINE, "  getTopmostManagedParent returning null, disconnected {0}", parent);
                return null;
            } else {
                LOG.log(Level.FINE, "  getTopmostManagedParent returning known parent {0}", parent);
                return parent;
            }
        }

        if (GitUtils.isPartOfGitMetadata(file)) {
            for (;file != null; file = file.getParentFile()) {
                if (GitUtils.isAdministrative(file)) {
                    file = file.getParentFile();
                    break;
                }
            }
        }
        Set<File> done = new HashSet<File>();
        File topmost = null;
        for (;file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                break;
            }
            if (org.netbeans.modules.versioning.util.Utils.isScanForbidden(file)) break;
            if (GitUtils.repositoryExistsFor(file)){
                LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                done.clear();   // all folders added before must be removed, they ARE in fact managed by git
                topmost =  file;
            } else {
                LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, " getTopmostManagedParent returns {0} after {1} millis", new Object[] { topmost, System.currentTimeMillis() - t });
        }
        if(topmost != null) {
            knownRoots.add(topmost);
        }

        return topmost == null || skipDisconnectedRepositories && isDisconnected(topmost) ? null : topmost;
    }

    @Override
    public VCSAnnotator getVCSAnnotator () {
        return Git.getInstance().getVCSAnnotator();
    }

    @Override
    public VCSInterceptor getVCSInterceptor () {
        return Git.getInstance().getVCSInterceptor();
    }

    @Override
    public void getOriginalFile (File workingCopy, File originalFile) {
        Git.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @Override
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    private final CollocationQueryImplementation collocationQueryImplementation = new CollocationQueryImplementation() {
        @Override
        public boolean areCollocated(File a, File b) {
            File fra = getTopmostManagedAncestor(a);
            File frb = getTopmostManagedAncestor(b);

            if (fra == null || !fra.equals(frb)) return false;

            return true;
        }

        @Override
        public File findRoot(File file) {
            return getTopmostManagedAncestor(file);
        }
    };

    private File getKnownParent(File file) {
        File[] roots = knownRoots.toArray(new File[knownRoots.size()]);
        File knownParent = null;
        for (File r : roots) {
            if(Utils.isAncestorOrEqual(r, file) && (knownParent == null || Utils.isAncestorOrEqual(knownParent, r))) {
                knownParent = r;
            }
        }
        return knownParent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(FileStatusCache.PROP_FILE_STATUS_CHANGED)) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
            fireStatusChanged(changedEvent.getFile());
        } else if (event.getPropertyName().equals(Git.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<File>) event.getNewValue());
        } else if (event.getPropertyName().equals(Git.PROP_VERSIONED_FILES_CHANGED)) {
            LOG.fine("cleaning unversioned parents cache"); //NOI18N
            unversionedParents.clear();
            knownRoots.clear();
            fireVersionedFilesChanged();
        }
    }

    void refreshStatus (Set<File> files) {
        fireStatusChanged(files == null || files.isEmpty() ? null : files);
    }

    boolean isDisconnected (File topmost) {
        boolean disconnected = false;
        synchronized (disconnectedRepositories) {
            for (File disconnectedRepository : disconnectedRepositories) {
                if (Utils.isAncestorOrEqual(disconnectedRepository, topmost)) {
                    disconnected = true;
                    LOG.log(Level.FINE, "isDisconnected: Folder is disconnected: {0}, disconnected root: {1}", new Object[] { topmost, disconnectedRepository }); //NOI18N
                    break;
                }
            }
        }
        return disconnected;
    }

    private Set<File> initializeDisconnectedRepositories () {
        Set<File> disconnected = new HashSet<File>();
        try {
            String uf = NbPreferences.forModule(GitVCS.class).get("disconnectedFolders", null); //NOI18N
            if (uf != null && !uf.isEmpty()) {
                String [] paths = uf.split("\\;"); //NOI18N
                for (String path : paths) {
                    if (!path.isEmpty()) {
                        disconnected.add(new File(path));
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.INFO, e.getMessage(), e);
        }
        return disconnected;
    }

    void connectRepository (File repository) {
        synchronized (disconnectedRepositories) {
            boolean changed = false;
            for (Iterator<File> it = disconnectedRepositories.iterator(); it.hasNext(); ) {
                File disconnectedRepository = it.next();
                if (disconnectedRepository.equals(repository)) {
                    LOG.log(Level.FINE, "connectRepository: Connecting repository: {0}", new Object[] { repository }); //NOI18N
                    it.remove();
                    changed = true;
                    break;
                }
            }
            if (changed) {
                saveDisconnectedRepositories();
            }
        }
    }

    void disconnectRepository (File repository) {
        synchronized (disconnectedRepositories) {
            boolean add = true;
            for (File disconnectedRepository : disconnectedRepositories) {
                if (disconnectedRepository.equals(repository)) {
                    LOG.log(Level.FINE, "disconnectRepository: Repository already disconnected: {0}", new Object[] { repository }); //NOI18N
                    add = false;
                    break;
                }
            }
            if (add) {
                disconnectedRepositories.add(repository);
                saveDisconnectedRepositories();
            }
        }
    }

    private void saveDisconnectedRepositories () {
        StringBuilder packed = new StringBuilder();
        for (File repository : disconnectedRepositories) {
            packed.append(repository.getAbsolutePath()).append(";"); //NOI18N
        }
        if (packed.length() > 0) {
            NbPreferences.forModule(GitVCS.class).put("disconnectedFolders", packed.toString()); //NOI18N
        } else {
            NbPreferences.forModule(GitVCS.class).remove("disconnectedFolders"); //NOI18N
        }
    }
}
