/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.OriginalContent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;

import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * CVS registration class.
 * 
 * @author Maros Sandor
 */
public class CVS extends VersioningSystem implements VersioningListener, PreferenceChangeListener {

    public CVS() {
        CvsVersioningSystem.getInstance().addVersioningListener(this);
        CvsVersioningSystem.getInstance().getStatusCache().addVersioningListener(this);
        CvsModuleConfig.getPreferences().addPreferenceChangeListener(this);
    }
    
    /**
     * Returns the topmost parent folder of the given file that is managed by this versioning system.
     * 
     * @param file
     * @return null if this file is not managed by this versioning system or a topmpost File (folder) that is still versioned but its parent is not
     */
    public File getTopmostManagedParent(File file) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) == 0) return null;
        for (;;) {
            File parent = file.getParentFile();
            if (parent == null || (cache.getStatus(parent).getStatus() & FileInformation.STATUS_MANAGED) == 0) return file;
            file = parent;
        }
    }

    public VCSAnnotator getVCSAnnotator() {
        return CvsVersioningSystem.getInstance().getVCSAnnotator();
    }

    public VCSInterceptor getVCSInterceptor() {
        return CvsVersioningSystem.getInstance().getVCSInterceptor();
    }

    public OriginalContent getVCSOriginalContent(File workingCopy) {
        return CvsVersioningSystem.getInstance().getVCSOriginalContent(workingCopy);
    }

    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            fireStatusChanged(file);
        } else if (event.getId() == CvsVersioningSystem.EVENT_REFRESH_ANNOTATIONS) {
            fireStatusChanged((Set<File>) null);
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(CvsModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            fireStatusChanged((Set<File>) null);
        }
    }
}
