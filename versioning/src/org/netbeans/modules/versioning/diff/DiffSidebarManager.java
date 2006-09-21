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
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import java.io.File;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.Collection;

/**
 * Central place of diff integration into editor and errorstripe.
 * 
 * @author Maros Sandor
 */
class DiffSidebarManager {

    private static DiffSidebarManager instance;

    static synchronized DiffSidebarManager getInstance() {
        if (instance == null) {
            instance = new DiffSidebarManager();
        }
        return instance;
    }

    private boolean sidebarEnabled = false;

    private Map<JTextComponent, DiffSidebar> sideBars = new WeakHashMap<JTextComponent, DiffSidebar>();

    private DiffSidebarManager() {
    }

    JComponent createSideBar(JTextComponent target) {
        return getSideBar(target);
    }

    private synchronized DiffSidebar getSideBar(JTextComponent target) {
        DiffSidebar sideBar = sideBars.get(target);
        if (sideBar == null) {
            File file = null;
            Document doc = target.getDocument();
            DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                file = FileUtil.toFile(fo);
            }
            if (file == null) return null;

            DiffSidebarProvider.OriginalContent originalContent = null;
            Collection<? extends DiffSidebarProvider> c = Lookup.getDefault().lookupAll(DiffSidebarProvider.class);
            for (DiffSidebarProvider p : c) {
                originalContent = p.getOriginalContent(file);
                if (originalContent != null) {
                    break;
                }
            }
            if (originalContent == null) return null;

            sideBar = new DiffSidebar(target, originalContent);
            sideBars.put(target, sideBar);
            sideBar.setSidebarVisible(sidebarEnabled);
        }
        return sideBar;
    }

    void setSidebarEnabled(boolean enable) {
        for (DiffSidebar sideBar : sideBars.values()) {
            sideBar.setSidebarVisible(enable);
        }
       sidebarEnabled = enable;
    }

    boolean isSidebarEnabled() {
        return sidebarEnabled;
    }

    MarkProvider createMarkProvider(JTextComponent target) {
        DiffSidebar sideBar = getSideBar(target);
        return (sideBar != null) ? sideBar.getMarkProvider() : null;
    }
}
