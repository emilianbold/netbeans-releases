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

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.modules.ModuleInstall;

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.palette.*;

import java.beans.*;
import java.io.File;

/**
 * Module installation class for Form Editor
 *
 * @author Ian Formanek
 */
public class FormEditorModule extends ModuleInstall
{
    private static final long serialVersionUID = 1573432625099425394L;
    
    // XXX(-tdt) hack around failure of loading TimerBean caused by package
    // renaming com.netbeans => org.netbeans AND the need to preserve user's
    // system settings
    
    private static void timerBeanHack() {
        TopManager.getDefault().getRepository().addRepositoryListener(
            new RepositoryListener() {
                public void fileSystemRemoved (RepositoryEvent ev) {}
                public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {}

                public void fileSystemAdded (RepositoryEvent ev) {
                    FileSystem fs = ev.getFileSystem();
                    if (! (fs instanceof JarFileSystem))
                        return;
                    JarFileSystem jarfs = (JarFileSystem) fs;

                    try {
                        // XXX this should never happen, but sometimes it kdjf
                        // does. WHY?
                        if (null == jarfs.getJarFile())
                            return;
                        
                        String jarpath = jarfs.getJarFile().getCanonicalPath();
                        if (! jarpath.endsWith(File.separator + "beans"
                                               + File.separator + "TimerBean.jar"))
                            return;
                        File timerbean = new File(
                            System.getProperty("netbeans.home")
                            + File.separator + "beans"
                            + File.separator + "TimerBean.jar");
                        if (jarpath.equals(timerbean.getCanonicalPath()))
                            return;
                        
                        jarfs.setJarFile(timerbean);
                   }
                    catch (java.io.IOException ex) { /* ignore */ }
                    catch (PropertyVetoException ex) { /* ignore */ }
                }
            });
    }

    /** Module installed again. */
    
    public void restored() {
        Beans.setDesignTime(true);
        BeanInstaller.autoLoadBeans();
        timerBeanHack();
        
        // register standard persistence managers
        PersistenceManager.registerManager(new TuborgPersistenceManager());
        PersistenceManager.registerManager(new GandalfPersistenceManager());

        // XXX(-tdt) JDK "forgets" to provide a PropertyEditor for char and Character

        PropertyEditor charEditor;

        charEditor = PropertyEditorManager.findEditor(Character.TYPE);
        if (charEditor == null)
            FormPropertyEditorManager.registerEditor(
                Character.TYPE,
                org.netbeans.modules.form.editors.CharacterEditor.class);

        charEditor = PropertyEditorManager.findEditor(Character.class);
        if (charEditor == null)
            FormPropertyEditorManager.registerEditor(
                Character.class,
                org.netbeans.modules.form.editors.CharacterEditor.class);

        FormPropertyEditorManager.registerEditor(
            javax.swing.KeyStroke.class,
            org.netbeans.modules.form.editors.KeyStrokeEditor.class);
    }

    static String[] getDefaultAWTComponents() {
        return defaultAWTComponents;
    }

    static String[] getDefaultAWTIcons() {
        return defaultAWTIcons;
    }

    /** The default AWT Components */
    private final static String[] defaultAWTComponents = new String[] {
        "java.awt.Label", // NOI18N
        "java.awt.Button", // NOI18N
        "java.awt.TextField", // NOI18N
        "java.awt.TextArea", // NOI18N
        "java.awt.Checkbox", // NOI18N
        "java.awt.Choice", // NOI18N
        "java.awt.List", // NOI18N
        "java.awt.Scrollbar", // NOI18N
        "java.awt.ScrollPane", // NOI18N
        "java.awt.Panel", // NOI18N
        "java.awt.Canvas", // NOI18N
        "java.awt.MenuBar", // NOI18N
        "java.awt.PopupMenu", // NOI18N
    };

    /** The default AWT icons */
    private final static String[] defaultAWTIcons = new String[] {
        "/org/netbeans/beaninfo/awt/label.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/button.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/textfield.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/textarea.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/checkbox.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/choice.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/list.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/scrollbar.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/scrollpane.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/panel.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/canvas.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/menubar.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/popupmenu.gif", // NOI18N
    };
}
