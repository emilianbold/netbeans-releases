/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.modules.ModuleInstall;

import org.netbeans.modules.form.palette.BeanInstaller;

import java.beans.*;
import java.io.File;
import java.util.*;
import org.openide.modules.InstalledFileLocator;

/**
 * Module installation class for Form Editor
 *
 * @author Ian Formanek
 */
public class FormEditorModule extends ModuleInstall
{
    private static final long serialVersionUID = 1573432625099425394L;

    private static final String BEANINFO_PATH_AWT = "org.netbeans.modules.form.beaninfo.awt"; // NOI18N
    private static final String BEANINFO_PATH_SWING = "org.netbeans.modules.form.beaninfo.swing"; // NOI18N

    /** Module installed again. */
    
    public void restored() {
        Beans.setDesignTime(true);

        FormPropertyEditorManager.registerEditor(
            javax.swing.KeyStroke.class,
            org.netbeans.modules.form.editors.KeyStrokeEditor.class);

        // Add beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        List paths = new ArrayList(Arrays.asList(sp));
        if (!paths.contains(BEANINFO_PATH_AWT)) {
            paths.add(BEANINFO_PATH_AWT);
        }
        if (!paths.contains(BEANINFO_PATH_SWING)) {
            paths.add(BEANINFO_PATH_SWING);
        }
        Introspector.setBeanInfoSearchPath((String[])paths.toArray(new String[paths.size()]));
    }

    /** Module was uninstalled. */

    public void uninstalled() {
        Repository rep = Repository.getDefault();

        java.util.Enumeration enum = rep.getFileSystems();
        while (enum.hasMoreElements()) {
            FileSystem fs = (FileSystem) enum.nextElement();
            if (fs instanceof GlobalJarFileSystem)
                rep.removeFileSystem(fs);
        }

        // Remove beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        List paths = new ArrayList(Arrays.asList(sp));
        paths.remove(BEANINFO_PATH_AWT);
        paths.remove(BEANINFO_PATH_SWING);
        Introspector.setBeanInfoSearchPath((String[])paths.toArray(new String[paths.size()]));
        
        FormGroupActivator.uninstall();
    }
}
