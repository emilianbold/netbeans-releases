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

import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Hides folders that have 'Localy removed' status.
 * 
 * @author Maros Sandor
 */
public class CvsVisibilityQuery implements VisibilityQueryImplementation, VersioningListener {

    private static CvsVisibilityQuery instance;
    private static final String MARKER_CVS_REMOVED = "CVS/.nb-removed";

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final RequestProcessor.Task refreshVisibilityTask;

    public CvsVisibilityQuery() {
        CvsVersioningSystem.getInstance().getStatusCache().addVersioningListener(this);
        instance = this;
        refreshVisibilityTask = Utils.createTask(new Runnable() {
            public void run() {
                fireVisibilityChanged();
            }
        });        
    }

    public boolean isVisible(FileObject fileObject) {
        if (fileObject.isData()) return true;
        File file = FileUtil.toFile(fileObject);
        return file == null || !isHiddenFolder(file);
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
            if (file == null) return;
            File parent = file.getParentFile();
            if (parent == null) return;
            File marker = new File(parent, MARKER_CVS_REMOVED);
            if (marker.exists()) {
                if (file.lastModified() > marker.lastModified() && !CvsVersioningSystem.FILENAME_CVS.equals(file.getName())) {
                    makeVisible(parent);
                }
                fireVisibilityChanged();
            }
        }
    }

    private static void makeVisible(File file) {
        if (file == null) return;
        new File(file, MARKER_CVS_REMOVED).delete();
        makeVisible(file.getParentFile());
    }

    static boolean isHiddenFolder(File file) {
        File marker = new File(file, MARKER_CVS_REMOVED);
        if (marker.exists()) {
            File [] files = file.listFiles();
            for (File child : files) {
                if (child.lastModified() > marker.lastModified() && !CvsVersioningSystem.FILENAME_CVS.equals(child.getName())) {
                    makeVisible(file);
                    refreshVisibility();
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    private synchronized void fireVisibilityChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);          
        }          
    }

    private static void refreshVisibility() {
        if (instance != null) {
            instance.refreshVisibilityTask.schedule(100);
        }
   }

    static void hideFolder(File file) throws IOException {
        new File(file, MARKER_CVS_REMOVED).createNewFile();
        refreshVisibility();
    }
}
