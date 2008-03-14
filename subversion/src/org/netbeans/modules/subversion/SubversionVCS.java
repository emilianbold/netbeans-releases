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

/**
 * @author Maros Sandor
 */
public class SubversionVCS extends VersioningSystem implements VersioningListener, PreferenceChangeListener, PropertyChangeListener, CollocationQueryImplementation {

    public SubversionVCS() {
        putProperty(PROP_DISPLAY_NAME, NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_DisplayName"));
        putProperty(PROP_MENU_LABEL, NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_MainMenu"));
        Subversion.getInstance().getStatusCache().addVersioningListener(this);
        Subversion.getInstance().addPropertyChangeListener(this);
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
    }

    public File getTopmostManagedAncestor(File file) {
        return Subversion.getInstance().getTopmostManagedParent(file);
    }

    public VCSAnnotator getVCSAnnotator() {
        return Subversion.getInstance().getVCSAnnotator();
    }

    public VCSInterceptor getVCSInterceptor() {
        return Subversion.getInstance().getVCSInterceptor();
    }

    public void getOriginalFile(File workingCopy, File originalFile) {
        Subversion.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    public boolean areCollocated(File a, File b) {
        File fra = getTopmostManagedAncestor(a);
        File frb = getTopmostManagedAncestor(b);
        if (fra == null || !fra.equals(frb)) return false;
        try {
            SVNUrl ra = SvnUtils.getRepositoryRootUrl(a);
            SVNUrl rb = SvnUtils.getRepositoryRootUrl(b);
            SVNUrl rr = SvnUtils.getRepositoryRootUrl(fra);
            return ra.equals(rb) && ra.equals(rr);
        } catch (SVNClientException e) {
            // root not found
            return false;
        }
    }

    public File findRoot(File file) {
        return getTopmostManagedAncestor(file);
    }
    
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
        } else if (evt.getPropertyName().equals(Subversion.PROP_VERSIONED_FILES_CHANGED)) {
            fireVersionedFilesChanged();
        }
    }
}
