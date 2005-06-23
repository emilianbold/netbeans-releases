/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.io.File;
import org.netbeans.spi.project.ui.support.ProjectChooser;

/**
 * UI related utility methods for the module.
 *
 * @author Martin Krauskopf
 */
public final class UIUtil {
    
    private UIUtil() {}
    
    /**
     * Calls in turn {@link ProjectChooser#setProjectsFolder} if the
     * <code>folder</code> is not <code>null</code> and is a directory.
     */
    public static void setProjectChooserDir(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return;
        }
        ProjectChooser.setProjectsFolder(folder);
    }

    /**
     * Calls {@link #setProjectChooserDir} with the <code>fileOrFolder</code>'s
     * parent if it isn't <code>null</code>. Otherwise fallbacks to
     * <code>fileOrFolder</code> itself if it is a directory.
     */
    public static void setProjectChooserDirParent(File fileOrFolder) {
        if (fileOrFolder == null) {
            return;
        }
        File parent = fileOrFolder.getParentFile();
        setProjectChooserDir(parent != null ? parent : 
            (fileOrFolder.isDirectory() ? fileOrFolder : null));
    }

}
