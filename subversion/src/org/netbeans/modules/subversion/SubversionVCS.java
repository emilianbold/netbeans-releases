/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import java.io.File;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * @author Maros Sandor
 */
@ServiceProviders({@ServiceProvider(service=VersioningSystem.class), @ServiceProvider(service=SubversionVCS.class)})
public class SubversionVCS extends VersioningSystem implements VersioningListener, PreferenceChangeListener, PropertyChangeListener {
    
    private final Set<File> unversionedParents = Collections.synchronizedSet(new HashSet<File>(20));
    
    public SubversionVCS() {
        putProperty(PROP_DISPLAY_NAME, NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_DisplayName"));
        putProperty(PROP_MENU_LABEL, NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_MainMenu"));
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
    }

    /**
     * Tests whether the file is managed by this versioning system. If it is, the method should return the topmost 
     * parent of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its parents or null if the supplied file is NOT managed by this versioning system
     */
    @Override
    public File getTopmostManagedAncestor(File file) {
        Subversion.LOG.log(Level.FINE, "looking for managed parent for {0}", new Object[] { file });
        if(unversionedParents.contains(file)) {
            Subversion.LOG.fine(" cached as unversioned");
            return null;
        }
        File metadataRoot = null;
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            Subversion.LOG.fine(" part of metaddata");
            for (;file != null; file = file.getParentFile()) {
                if (SvnUtils.isAdministrative(file)) {
                    Subversion.LOG.log(Level.FINE, " will use parent {0}", new Object[] { file });
                    metadataRoot = file;
                    file = file.getParentFile();
                    break;
                }
            }
        }
        File topmost = null;
        Set<File> done = new HashSet<File>();
        for (; file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                Subversion.LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                break;
            }
            if (org.netbeans.modules.versioning.util.Utils.isScanForbidden(file)) break;
            if (new File(file, SvnUtils.SVN_ENTRIES_DIR).exists()) { // NOI18N
                Subversion.LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                topmost = file;
                done.clear();
            } else {
                Subversion.LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            Subversion.LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        if (topmost == null && metadataRoot != null) {
            // .svn is considered managed, too, see #159453
            Subversion.LOG.log(Level.FINE, "setting metadata root as managed parent {0}", new Object[] { metadataRoot });
            topmost = metadataRoot;
        }
        Subversion.LOG.log(Level.FINE, "returning managed parent {0}", new Object[] { topmost });
        return topmost;
    }

    @Override
    public VCSAnnotator getVCSAnnotator() {
        return Subversion.getInstance().getVCSAnnotator();
    }

    @Override
    public VCSInterceptor getVCSInterceptor() {
        return Subversion.getInstance().getVCSInterceptor();
    }

    @Override
    public void getOriginalFile(File workingCopy, File originalFile) {
        Subversion.getInstance().getOriginalFile(workingCopy, originalFile);
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
            try {
                SVNUrl ra = SvnUtils.getRepositoryRootUrl(a);
                if(ra == null) {
                    // this might happen. there is either no svn client available or
                    // no repository url stored in the metadata (svn < 1.3).
                    // one way or another, can't do anything reasonable at this point
                    Subversion.LOG.log(Level.WARNING, "areCollocated returning false due to missing repository url for {0} {1}", new Object[] {a, b});
                    return false;
                }
                SVNUrl rb = SvnUtils.getRepositoryRootUrl(b);
                SVNUrl rr = SvnUtils.getRepositoryRootUrl(fra);
                return ra.equals(rb) && ra.equals(rr);
            } catch (SVNClientException e) {
                if (!SvnClientExceptionHandler.isTooOldClientForWC(e.getMessage())) {
                    Subversion.LOG.log(Level.INFO, null, e);
                }
                Subversion.LOG.log(Level.WARNING, "areCollocated returning false due to catched exception " + a + " " + b);
                // root not found
                return false;
            }
        }

        public File findRoot(File file) {
            // TODO: we should probably return the closest common ancestor
            return getTopmostManagedAncestor(file);
        }
    };
    
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            fireStatusChanged(file);
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<File>) null);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Subversion.PROP_ANNOTATIONS_CHANGED)) {
            fireAnnotationsChanged((Set<File>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Subversion.PROP_BASE_FILE_CHANGED)) {
            fireStatusChanged((Set<File>) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Subversion.PROP_VERSIONED_FILES_CHANGED)) {
            Subversion.LOG.fine("cleaning unversioned parents cache");
            unversionedParents.clear();
            fireVersionedFilesChanged();
        }
    }
}
