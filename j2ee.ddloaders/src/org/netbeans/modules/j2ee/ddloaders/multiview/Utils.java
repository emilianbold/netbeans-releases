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
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.src.ClassElement;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;

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
            return fileObject.getMIMEType().startsWith("image/"); // NOI18N
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

    public static String getBundleMessage(String messageId) {
        return NbBundle.getMessage(Utils.class, messageId);
    }

    public static String getBundleMessage(String messageId, Object param1) {
        return NbBundle.getMessage(Utils.class, messageId, param1);
    }

    public static String getBundleMessage(String messageId, Object param1, Object param2) {
        return NbBundle.getMessage(Utils.class, messageId, param1, param2);
    }

    public static boolean isJavaIdentifier(String id) {
        return Utilities.isJavaIdentifier(id);
    }

    /**
     * Returns true, if the passed string can be used as a qualified identifier.
     * it does not check for semantic, only for syntax.
     * The function returns true for any sequence of identifiers separated by
     * dots.
     */
    public static boolean isValidPackageName(String packageName) {
        if (packageName.length() > 0 && packageName.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageName, "."); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() == 0) {
                return false;
            }
            if (!isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    public static void removeInterface(ClassElement beanClass, String interfaceName) {
        org.netbeans.modules.j2ee.ejbjarproject.Utils.removeInterface(beanClass, interfaceName);
    }

    public static void removeClassFile(FileObject ejbJarFile, String className) {
        org.netbeans.modules.j2ee.ejbjarproject.Utils.removeClassFile(ejbJarFile, className);
    }

    public static FileObject getPackageFile(FileObject ejbJarFile, String packageName) {
        return org.netbeans.modules.j2ee.ejbjarproject.Utils.getPackageFile(ejbJarFile, packageName);
    }

    public static String getPackage(String ejbClass) {
        return org.netbeans.modules.j2ee.ejbjarproject.Utils.getPackage(ejbClass);
    }

    public static void addInterfaces(FileObject ejbJarFile, EntityAndSession ejb, boolean local) {
        org.netbeans.modules.j2ee.ejbjarproject.Utils.addInterfaces(ejbJarFile, ejb, local);
    }
}
