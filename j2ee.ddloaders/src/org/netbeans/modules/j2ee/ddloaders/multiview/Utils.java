/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.src.ClassElement;

import javax.swing.*;
import java.awt.*;

/**
 * @author pfiala
 */
public class Utils {
    public static final String ICON_BASE_DD_VALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDValidIcon"; // NOI18N
    public static final String ICON_BASE_DD_INVALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDInvalidIcon"; // NOI18N
    public static final String ICON_BASE_EJB_MODULE_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbModuleNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTERPRISE_JAVA_BEANS_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbContainerNodeIcon"; // NOI18N
    public static final String ICON_BASE_SESSION_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/SessionNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTITY_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EntityNodeIcon"; // NOI18N
    public static final String ICON_BASE_MESSAGE_DRIVEN_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MessageNodeIcon"; // NOI18N
    public static final String ICON_BASE_MISC_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MiscNodeIcon"; // NOI18N

    private static BrowseFolders.FileObjectFilter imageFileFilter = new BrowseFolders.FileObjectFilter() {
        public boolean accept(FileObject fileObject) {
            return fileObject.getMIMEType().startsWith("image/");
        }
    };

    public static String browseIcon(EjbJarMultiViewDataObject dataObject) {
        FileObject fileObject = org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders.showDialog(
                dataObject.getSourceGroups(), imageFileFilter);
        String relativePath;
        if (fileObject != null) {
            FileObject projectDirectory = dataObject.getProjectDirectory();
            relativePath = FileUtil.getRelativePath(projectDirectory, fileObject);
        } else {
            relativePath = null;
        }
        return relativePath;
    }

    public static Color getErrorColor() {
        // inspired by org.openide.WizardDescriptor
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        return c == null ? new Color(89, 79, 191) : c;
    }

    public static JTree findTreeComponent(Component component) {
        if (component instanceof JTree) {
            return (JTree) component;
        }
        if (component instanceof Container) {
            Component[] components = ((Container) component).getComponents();
            for (int i = 0; i < components.length; i++) {
                JTree tree = findTreeComponent(components[i]);
                if (tree != null) {
                    return tree;
                }
            }
        }
        return null;
    }

    public static ClassElement getBeanClass(FileObject ejbJarFile, Ejb ejb) {
        return org.netbeans.modules.j2ee.ejbjarproject.Utils.getBeanClass(ejbJarFile, ejb);
    }

    public static void scrollToVisible(JComponent component) {
        org.netbeans.modules.xml.multiview.Utils.scrollToVisible(component);
    }
}
