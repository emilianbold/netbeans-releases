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
package org.netbeans.modules.versioning.diff;

import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.versioning.spi.OriginalContent;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.VersioningManager;
import org.netbeans.modules.versioning.VersioningConfig;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import java.io.File;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Central place of diff integration into editor and errorstripe.
 * 
 * @author Maros Sandor
 */
class DiffSidebarManager implements PreferenceChangeListener {

    static final String SIDEBAR_ENABLED = "diff.sidebarEnabled"; // NOI18N

    private static DiffSidebarManager instance;

    static synchronized DiffSidebarManager getInstance() {
        if (instance == null) {
            instance = new DiffSidebarManager();
        }
        return instance;
    }

    private boolean sidebarEnabled;

    /**
     * Holds created sidebars (to be able to show/hide them all at once). It is just a set, values are always null.
     */
    private final Map<DiffSidebar, Object> sideBars = new WeakHashMap<DiffSidebar, Object>();

    private DiffSidebarManager() {
        sidebarEnabled = VersioningConfig.getDefault().getPreferences().getBoolean(SIDEBAR_ENABLED, true); 
        VersioningConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
    }

    /**
     * Creates a new task needed by a diff sidebar to update its structures (compute diff). 
     * 
     * @param runnable a runnable task
     * @return RP task
     */
    RequestProcessor.Task createDiffSidebarTask(Runnable runnable) {
        return Utils.createTask(runnable);
    }

    JComponent createSideBar(JTextComponent target) {
        return getSideBar(target);
    }

    private DiffSidebar getSideBar(JTextComponent target) {
        synchronized(sideBars) {
            DiffSidebar sideBar = null;
            for (DiffSidebar bar : sideBars.keySet()) {
                if (bar.getTextComponent() == target) {
                    sideBar = bar;
                    break;
                }
            }
            if (sideBar == null) {
                File file = null;
                Document doc = target.getDocument();
                DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
                if (dobj != null) {
                    FileObject fo = dobj.getPrimaryFile();
                    file = FileUtil.toFile(fo);
                }
                if (file == null) return null;
    
                OriginalContent originalContent = null;
                VersioningSystem vs = VersioningManager.getInstance().getOwner(file);
                if (vs != null) {
                    originalContent = vs.getVCSOriginalContent(file);
                }
                if (originalContent == null) return null;
    
                sideBar = new DiffSidebar(target, originalContent);
                sideBars.put(sideBar, null);
                sideBar.setSidebarVisible(sidebarEnabled);
            }
            return sideBar;
        }
    }

    private void setSidebarEnabled(boolean enable) {
        synchronized(sideBars) {
            for (DiffSidebar sideBar : sideBars.keySet()) {
                sideBar.setSidebarVisible(enable);
            }
            sidebarEnabled = enable;
        }
    }

    MarkProvider createMarkProvider(JTextComponent target) {
        DiffSidebar sideBar = getSideBar(target);
        return (sideBar != null) ? sideBar.getMarkProvider() : null;
    }

    DiffSidebar t9y_getSidebar() {
        return sideBars.keySet().iterator().next();
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals(SIDEBAR_ENABLED)) {
            setSidebarEnabled(Boolean.valueOf(evt.getNewValue()));
        }
    }
}
