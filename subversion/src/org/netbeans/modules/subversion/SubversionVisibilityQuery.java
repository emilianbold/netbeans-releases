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

package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

import java.io.*;
import java.util.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * Hides folders that have 'Localy removed' status.
 * 
 * @author Maros Sandor
 */
public class SubversionVisibilityQuery implements VisibilityQueryImplementation, VersioningListener {

    private List<ChangeListener>  listeners = new ArrayList<ChangeListener>();
    private FileStatusCache       cache;

    public SubversionVisibilityQuery() {
        cache = Subversion.getInstance().getStatusCache();
        cache.addVersioningListener(this);
    }

    public boolean isVisible(FileObject file) {
        if (file.isData()) return true;
        return cache.getStatus(FileUtil.toFile(file)).getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY; 
    }

    public synchronized void addChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.add(l);
        listeners = newList;
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.remove(l);
        listeners = newList;
    }

    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            if (file.isDirectory()) {
                FileInformation old = (FileInformation) event.getParams()[1];
                FileInformation cur = (FileInformation) event.getParams()[2];
                if (old != null && old.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY || cur.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                    fireVisibilityChanged();
                }
            }
        }
    }

    static boolean isHiddenFolder(FileInformation info, File file) {
        return file.isDirectory() && info != null && info.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY;
    }
    
    private void fireVisibilityChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);          
        }          
    }
}
