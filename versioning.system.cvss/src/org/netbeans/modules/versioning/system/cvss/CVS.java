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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.spi.VCSVisibilityQuery;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.versioning.util.Utils;

/**
 * CVS registration class.
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.spi.VersioningSystem.class)
public class CVS extends VersioningSystem implements VersioningListener, PreferenceChangeListener {

    private VCSVisibilityQuery visibilityQuery;
    private final static String PROP_PRIORITY = "Integer VCS.Priority"; //NOI18N
    private final static Integer priority = Utils.getPriority("cvs"); //NOI18N

    public CVS() {
        putProperty(PROP_DISPLAY_NAME, NbBundle.getMessage(CVS.class, "CTL_CVS_DisplayName"));
        putProperty(PROP_MENU_LABEL, NbBundle.getMessage(CVS.class, "CTL_CVS_MainMenu"));
        putProperty(PROP_PRIORITY, priority);
        CvsVersioningSystem.getInstance().addVersioningListener(this);
        CvsVersioningSystem.getInstance().getStatusCache().addVersioningListener(this);
        CvsModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
    }
    
    /**
     * Returns the topmost ancestor folder of the given file that is managed by this versioning system.
     * 
     * @param file
     * @return null if this file is not managed by this versioning system or a topmpost File (folder) that is still versioned but its parent is not
     */
    public File getTopmostManagedAncestor(File file) {
        return CvsVersioningSystem.getInstance().getTopmostManagedParent(file);
    }

    public VCSAnnotator getVCSAnnotator() {
        return CvsVersioningSystem.getInstance().getVCSAnnotator();
    }

    public VCSInterceptor getVCSInterceptor() {
        return CvsVersioningSystem.getInstance().getVCSInterceptor();
    }

    public void getOriginalFile(File workingCopy, File originalFile) {
        CvsVersioningSystem.getInstance().getOriginalFile(workingCopy, originalFile);
    }

    @Override
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return collocationQueryImplementation;
    }

    @Override
    public VCSVisibilityQuery getVisibilityQuery() {
        if(visibilityQuery == null) {
            visibilityQuery = new CvsVisibilityQuery();
        }
        return visibilityQuery;
    }

    private final CollocationQueryImplementation collocationQueryImplementation = new CollocationQueryImplementation() {
        public boolean areCollocated(File a, File b) {
            File fra = getTopmostManagedAncestor(a);
            File frb = getTopmostManagedAncestor(b);
            if (fra == null || !fra.equals(frb)) return false;
            try {
                String ra = org.netbeans.modules.versioning.system.cvss.util.Utils.getCVSRootFor(a);
                String rb = org.netbeans.modules.versioning.system.cvss.util.Utils.getCVSRootFor(b);
                String rr = org.netbeans.modules.versioning.system.cvss.util.Utils.getCVSRootFor(fra);
                return ra.equals(rb) && ra.equals(rr);
            } catch (IOException e) {
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
        } else if (event.getId() == CvsVersioningSystem.EVENT_REFRESH_ANNOTATIONS) {
            fireStatusChanged((Set<File>) null);
        } else if (event.getId() == CvsVersioningSystem.EVENT_VERSIONED_FILES_CHANGED) {
            fireVersionedFilesChanged();
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(CvsModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<File>) null);
        } else if (evt.getKey().startsWith(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT)) {
            fireAnnotationsChanged(null);
        }
    }
}
