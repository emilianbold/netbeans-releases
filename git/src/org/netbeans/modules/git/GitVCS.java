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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.spi.queries.CollocationQueryImplementation;
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

    public GitVCS() {
        putProperty(PROP_DISPLAY_NAME, org.openide.util.NbBundle.getMessage(GitVCS.class, "CTL_Git_DisplayName")); // NOI18N
        putProperty(PROP_MENU_LABEL, org.openide.util.NbBundle.getMessage(GitVCS.class, "CTL_Git_MainMenu")); // NOI18N
        putProperty(PROP_PRIORITY, priority);
    }

    @Override
    public File getTopmostManagedAncestor(File file) {
        long t = System.currentTimeMillis();
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        if(unversionedParents.contains(file)) {
            LOG.fine(" cached as unversioned");
            return null;
        }
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        File parent = getKnownParent(file);
        if(parent != null) {
            LOG.log(Level.FINE, "  getTopmostManagedParent returning known parent {0}", parent);
            return parent;
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

        return topmost;
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
            fireVersionedFilesChanged();
        }
    }

    void refreshStatus (Set<File> files) {
        fireStatusChanged(files == null || files.isEmpty() ? null : files);
    }
}
