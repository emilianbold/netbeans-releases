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

import java.util.ArrayList;

import org.openide.nodes.Node;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.*;
import org.openide.ErrorManager;
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
