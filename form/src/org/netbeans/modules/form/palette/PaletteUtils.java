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

package org.netbeans.modules.form.palette;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.form.project.ClassSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/**
 * Class providing various useful methods for palette classes.
 *
 * @author Tomas Pavek
 */
public final class PaletteUtils {

    private static FileObject paletteFolder;
    private static DataFolder paletteDataFolder;

    private PaletteUtils() {
    }

    // -----------

    static Node[] getItemNodes(Node categoryNode, boolean mustBeValid) {
        Node[] nodes = categoryNode.getChildren().getNodes(true);
        if (mustBeValid) {
            java.util.List list = null; // don't create until needed
            for (int i=0; i < nodes.length; i++) {
                if (nodes[i].getCookie(PaletteItem.class) != null) {
                    if (list != null)
                        list.add(nodes[i]);
                }
                else if (list == null) {
                    list = new ArrayList(nodes.length);
                    for (int j=0; j < i; j++)
                        list.add(nodes[j]);
                }
            }
            if (list != null) {
                nodes = new Node[list.size()];
                list.toArray(nodes);
            }
        }
        return nodes;
    }

    static Node[] getCategoryNodes(Node paletteNode, boolean mustBeVisible) {
        Node[] nodes = paletteNode.getChildren().getNodes(true);
        if (mustBeVisible) {
            java.util.List list = null; // don't create until needed
            for (int i=0; i < nodes.length; i++) {
                if (isValidCategoryNode(nodes[i], mustBeVisible)) {
                    if (list != null)
                        list.add(nodes[i]);
                }
                else if (list == null) {
                    list = new ArrayList(nodes.length);
                    for (int j=0; j < i; j++)
                        list.add(nodes[j]);
                }
            }
            if (list != null) {
                nodes = new Node[list.size()];
                list.toArray(nodes);
            }
        }
        return nodes;
    }

    static boolean isValidCategoryNode(Node node, boolean visible) {
        DataFolder df = (DataFolder) node.getCookie(DataFolder.class);
        return df != null
               && (!visible || !Boolean.TRUE.equals(df.getPrimaryFile()
                                       .getAttribute(PaletteNode.CAT_HIDDEN))); // NOI18N
    }

    static String getItemComponentDescription(PaletteItem item) {
        ClassSource classSource = item.getComponentClassSource();

        if (classSource == null || classSource.getCPRootCount() == 0) {
            String className = classSource.getClassName();
            if (className != null) {
                if (className.startsWith("javax.") // NOI18N
                        || className.startsWith("java.")) // NOI18N
                    return getBundleString("MSG_StandardJDKComponent"); // NOI18N
                if (className.startsWith("org.netbeans.")) // NOI18N
                    return getBundleString("MSG_NetBeansComponent"); // NOI18N
            }
        }
        else {
            String type = classSource.getCPRootType(0);
            String name = classSource.getCPRootName(0);

            if (ClassSource.JAR_SOURCE.equals(type)) {
                return MessageFormat.format(
                    getBundleString("FMT_ComponentFromJar"), // NOI18N
                    new Object[] { name });
            }
            else if (ClassSource.LIBRARY_SOURCE.equals(type)) {
                Library lib = LibraryManager.getDefault().getLibrary(name);
                return MessageFormat.format(
                    getBundleString("FMT_ComponentFromLibrary"), // NOI18N
                    new Object[] { lib != null ? lib.getDisplayName() : name });
            }
            else if (ClassSource.PROJECT_SOURCE.equals(type)) {
                try {
                    Project project = FileOwnerQuery.getOwner(new File(name).toURI());
                    return MessageFormat.format(
                          getBundleString("FMT_ComponentFromProject"), // NOI18N
                          new Object[] { project == null ? name :
                                         FileUtil.getFileDisplayName(project.getProjectDirectory()) });
                } catch (Exception ex) {
                    // XXX must catch specific exceptions and notify them or explain why they are ignored!
                }
            }
        }

        return getBundleString("MSG_UnspecifiedComponent"); // NOI18N
    }

    public static FileObject getPaletteFolder() {
        if (paletteFolder != null)
            return paletteFolder;

        try {
            paletteFolder = Repository.getDefault().getDefaultFileSystem()
                                                     .findResource("Palette"); // NOI18N
            if (paletteFolder == null) // not found, create new folder
                paletteFolder = Repository.getDefault().getDefaultFileSystem()
                                  .getRoot().createFolder("Palette"); // NOI18N
        }
        catch (java.io.IOException ex) {
            throw new IllegalStateException("Palette folder not found and cannot be created."); // NOI18N
        }
        return paletteFolder;
    }

    static DataFolder getPaletteDataFolder() {
        if (paletteDataFolder == null)
            paletteDataFolder = DataFolder.findFolder(getPaletteFolder());
        return paletteDataFolder;
    }

    static String getBundleString(String key) {
        return NbBundle.getBundle(PaletteUtils.class).getString(key);
    }

}
