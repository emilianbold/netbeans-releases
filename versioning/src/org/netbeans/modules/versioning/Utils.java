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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning;

import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.awt.Actions;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSystem;

import javax.swing.*;
import java.io.File;

/**
 * Utilities for Versioning SPI classes. 
 * 
 * @author Maros Sandor
 */
public class Utils {
    
    /**
     * Request processor for long running tasks.
     */
    private static final RequestProcessor vcsBlockingRequestProcessor = new RequestProcessor("Versioning long tasks", 1);

    /**
     * Tests for parent/child file relationsip.
     * 
     * @param parent supposed parent of the file
     * @param file a file
     * @return true if parent is a parent folder of file OR both parameters are equal, false otherwise
     */
    public static boolean isParentOrEqual(File parent, File file) {
        if (VCSContext.isFlat(parent)) {
            return parent.equals(file) || parent.equals(file.getParentFile()) && !file.isDirectory();
        }
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(parent)) return true;
        }
        return false;
    }

    /**
     * Creates a menu item from an action.
     * 
     * @param action an action
     * @return JMenuItem
     */
    public static JMenuItem toMenuItem(Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Menu) {
            item = ((Presenter.Menu) action).getMenuPresenter();
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, false);
        }
        return item;
    }

    public static File getTempFolder() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));   // NOI18N
        for (;;) {
            File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
            if (!dir.exists() && dir.mkdirs()) {
                dir.deleteOnExit();
                return FileUtil.normalizeFile(dir);
            }
        }
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    public static boolean isLocalHistory(VersioningSystem system) {
        return system.getProperty(VersioningSystem.PROP_LOCALHISTORY_VCS) != null;
    }
    
    /**
     * Creates a task that will run in the Versioning RequestProcessor (with has throughput of 1). The runnable may take long
     * to execute (connet through network, etc).
     * 
     * @param runnable Runnable to run
     * @return RequestProcessor.Task created task
     */
    public static RequestProcessor.Task createTask(Runnable runnable) {
        return vcsBlockingRequestProcessor.create(runnable);
    }

    public static String getDisplayName(VersioningSystem system) {
        return (String) system.getProperty(VersioningSystem.PROP_DISPLAY_NAME);
    }

    public static String getMenuLabel(VersioningSystem system) {
        return (String) system.getProperty(VersioningSystem.PROP_MENU_LABEL);
    }
}
