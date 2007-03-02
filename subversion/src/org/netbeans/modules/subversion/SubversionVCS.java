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
package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.OriginalContent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.openide.util.NbBundle;

import java.io.File;
import java.util.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author Maros Sandor
 */
public class SubversionVCS extends VersioningSystem implements VersioningListener, PreferenceChangeListener, PropertyChangeListener {

    public SubversionVCS() {
        Subversion.getInstance().getStatusCache().addVersioningListener(this);
        Subversion.getInstance().addPropertyChangeListener(this);
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SubversionVCS.class, "CTL_Subversion_MainMenu");
    }

    public File getTopmostManagedParent(File file) {
        return Subversion.getInstance().getTopmostManagedParent(file);
    }

    public VCSAnnotator getVCSAnnotator() {
        return Subversion.getInstance().getVCSAnnotator();
    }

    public VCSInterceptor getVCSInterceptor() {
        return Subversion.getInstance().getVCSInterceptor();
    }

    public OriginalContent getVCSOriginalContent(File file) {
        return Subversion.getInstance().getVCSOriginalContent(file);
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
        } else if (evt.getKey().startsWith(SvnModuleConfig.PROP_TEXT_ANNOTATIONS_FORMAT)) {
            fireAnnotationsChanged((Set<File>) null);
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
