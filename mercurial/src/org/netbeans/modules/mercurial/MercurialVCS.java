/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial;

import java.io.File;
import java.util.Set;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Extends framework <code>VersioningSystem</code> to Mercurial module functionality.
 * 
 * @author Maros Sandor
 */
@ServiceProviders({@ServiceProvider(service=VersioningSystem.class), @ServiceProvider(service=MercurialVCS.class)})
public class MercurialVCS extends VersioningSystem implements PropertyChangeListener, PreferenceChangeListener {

    private Set<File> knownRoots = Collections.synchronizedSet(new HashSet<File>());
    private final Set<File> unversionedParents = Collections.synchronizedSet(new HashSet<File>(20));
    private final static String PROP_PRIORITY = "Integer VCS.Priority"; //NOI18N
    private final static Integer priority = Utils.getPriority("mercurial"); //NOI18N
    
    public MercurialVCS() {
        putProperty(PROP_DISPLAY_NAME, org.openide.util.NbBundle.getMessage(MercurialVCS.class, "CTL_Mercurial_DisplayName")); // NOI18N
        putProperty(PROP_MENU_LABEL, org.openide.util.NbBundle.getMessage(MercurialVCS.class, "CTL_Mercurial_MainMenu")); // NOI18N
        putProperty(PROP_PRIORITY, priority);
        HgModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
    }

    @Override
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    private final CollocationQueryImplementation collocationQueryImplementation = new CollocationQueryImplementation() {
        public boolean areCollocated(File a, File b) {
            File fra = getTopmostManagedAncestor(a);
            File frb = getTopmostManagedAncestor(b);

            if (fra == null || !fra.equals(frb)) return false;

            return true;
        }

        public File findRoot(File file) {
            // TODO: we should probably return the closest common ancestor
            return getTopmostManagedAncestor(file);
        }
    };
            
    /**
     * Tests whether the file is managed by this versioning system. If it is, 
     * the method should return the topmost 
     * ancestor of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its ancestors or null if the 
     *  supplied file is NOT managed by this versioning system
     */
    @Override
    public File getTopmostManagedAncestor(File file) {
        long t = System.currentTimeMillis();
        Mercurial.LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        if(unversionedParents.contains(file)) {
            Mercurial.LOG.fine(" cached as unversioned");
            return null;
        }
        Mercurial.LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        File parent = getKnownParent(file);
        if(parent != null) {
            Mercurial.LOG.log(Level.FINE, "  getTopmostManagedParent returning known parent " + parent);
            return parent;
        }

        if (HgUtils.isPartOfMercurialMetadata(file)) {
            for (;file != null; file = file.getParentFile()) {
                if (HgUtils.isAdministrative(file)) {
                    file = file.getParentFile();
                    break;
                }
            }
        }
        Set<File> done = new HashSet<File>();
        File topmost = null;
        for (;file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                Mercurial.LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                break;
            }
            if (org.netbeans.modules.versioning.util.Utils.isScanForbidden(file)) break;
            if (HgUtils.hgExistsFor(file)){
                Mercurial.LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                done.clear();   // all folders added before must be removed, they ARE in fact managed by hg
                topmost =  file;
            } else {
                Mercurial.LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            Mercurial.LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        if(Mercurial.LOG.isLoggable(Level.FINE)) {
            Mercurial.LOG.log(Level.FINE, " getTopmostManagedParent returns {0} after {1} millis", new Object[] { topmost, System.currentTimeMillis() - t });
        }
        if(topmost != null) {
            knownRoots.add(topmost);
        }

        return topmost;
    }

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
    
    /**
     * Coloring label, modifying icons, providing action on file
     */
    public VCSAnnotator getVCSAnnotator() {
        return Mercurial.getInstance().getMercurialAnnotator();
    }
    
    /**
     * Handle file system events such as delete, create, remove etc.
     */
    public VCSInterceptor getVCSInterceptor() {
        return Mercurial.getInstance().getMercurialInterceptor();
    }

    public void getOriginalFile(File workingCopy, File originalFile) {
        Mercurial.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @SuppressWarnings("unchecked") // Property Change event.getNewValue returning Object
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(FileStatusCache.PROP_FILE_STATUS_CHANGED)) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
            fireStatusChanged(changedEvent.getFile());
        } else if (event.getPropertyName().equals(Mercurial.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<File>) event.getNewValue());
        } else if (event.getPropertyName().equals(Mercurial.PROP_VERSIONED_FILES_CHANGED)) {
            Mercurial.LOG.fine("cleaning unversioned parents cache");   //NOI18N
            unversionedParents.clear();
            fireVersionedFilesChanged();
        } else if (event.getPropertyName().equals(MercurialAnnotator.PROP_ICON_BADGE_CHANGED)) {
            fireStatusChanged((Set<File>) event.getNewValue());
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<File>) null);
        }
    }
}
