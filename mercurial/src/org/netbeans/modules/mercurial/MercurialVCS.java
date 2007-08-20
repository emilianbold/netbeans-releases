/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.mercurial;

import java.io.File;
import java.util.Set;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;

/**
 * Extends framework <code>VersioningSystem</code> to Mercurial module functionality.
 * 
 * @author Maros Sandor
 */
public class MercurialVCS extends VersioningSystem implements PropertyChangeListener {

    public MercurialVCS() {
        putProperty(PROP_DISPLAY_NAME, "Mercurial");
        putProperty(PROP_MENU_LABEL, "&Mercurial");
        Mercurial.getInstance().addPropertyChangeListener(this);
        Mercurial.getInstance().getFileStatusCache().addPropertyChangeListener(this);
    }

    /**
     * Tests whether the file is managed by this versioning system. If it is, 
     * the method should return the topmost 
     * ancestor of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its ancestors or null if the 
     *  supplied file is NOT managed by this versioning system
     */
    public File getTopmostManagedAncestor(File file) {
        return Mercurial.getInstance().getTopmostManagedParent(file);
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


    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName() == FileStatusCache.PROP_FILE_STATUS_CHANGED) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
            fireStatusChanged(changedEvent.getFile());
        } else if (event.getPropertyName() == Mercurial.PROP_ANNOTATIONS_CHANGED) {
            fireAnnotationsChanged((Set<File>) event.getNewValue());
        } else if (event.getPropertyName() == Mercurial.PROP_VERSIONED_FILES_CHANGED) {
            fireVersionedFilesChanged();
        }
    }
}
