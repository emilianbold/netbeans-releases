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

import java.beans.BeanInfo;
import java.util.ArrayList;
import java.text.MessageFormat;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.*;
import javax.swing.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExClipboard;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.form.project.ClassSource;

/**
 * Class providing various useful methods for palette classes.
 *
 * @author Tomas Pavek, Jan Stola
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
    
    static class NewCategoryAction extends AbstractAction {
        
        public NewCategoryAction() {
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_CreateCategory")); // NOI18N
        }

        public void actionPerformed(ActionEvent event) {
            try {
                PaletteNode.getPaletteNode().createNewCategory();
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
    }
    
    static class ReorderCategoriesAction extends AbstractAction {
        
        public ReorderCategoriesAction() {
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_OrderCategories")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            Index order = (Index)PaletteNode.getPaletteNode().getCookie(Index.class);
            if (order != null) {
                order.reorder();
            }
        }
        
        public boolean isEnabled() {
            return (PaletteNode.getPaletteNode().getCookie(Index.class) != null);
        }
        
    }
    
    static class ShowNamesAction extends AbstractAction {
        
        public void actionPerformed(ActionEvent event) {
            CPManager manager = CPManager.getDefault();
            manager.setShowComponentsNames(!manager.getShowComponentsNames());
        }
        
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                boolean showNames = CPManager.getDefault().getShowComponentsNames();
                String name = CPManager.getBundle().getString(showNames ? "CTL_HideNames" : "CTL_ShowNames"); // NOI18N
                return name;
            } else {
                return super.getValue(key);
            }
        }
        
    }
    
    static class ChangeIconSizeAction extends AbstractAction {
        
        public void actionPerformed(ActionEvent event) {
            CPManager manager = CPManager.getDefault();
            int oldSize = manager.getPaletteIconSize();
            int newSize = (oldSize == BeanInfo.ICON_COLOR_16x16) ?
                BeanInfo.ICON_COLOR_32x32 : BeanInfo.ICON_COLOR_16x16;
            manager.setPaletteIconSize(newSize);
        }
        
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                String namePattern = CPManager.getBundle().getString("CTL_IconSize"); // NOI18N
                String name = MessageFormat.format(namePattern,
                new Object[] {new Integer(CPManager.getDefault().getPaletteIconSize())});
                return name;
            } else {
                return super.getValue(key);
            }
        }
        
    }
    
    static class RefreshPaletteAction extends AbstractAction {
        
        public RefreshPaletteAction() {
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_RefreshPalette")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            CPManager.getDefault().resetPalette();
        }
        
    }
    
    static class DeleteCategoryAction extends AbstractAction {
        private Node categoryNode;
        
        public DeleteCategoryAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_DeleteCategory")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            // first user confirmation...
            String message = MessageFormat.format(
                PaletteUtils.getBundleString("FMT_ConfirmCategoryDelete"), // NOI18N
                new Object [] { categoryNode.getName() });

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message,
                PaletteUtils.getBundleString("CTL_ConfirmCategoryTitle"), // NOI18N
                NotifyDescriptor.YES_NO_OPTION);

            if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
                try {
                    categoryNode.destroy();
                } catch (java.io.IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
    }
    
    static class RenameCategoryAction extends AbstractAction {
        private Node categoryNode;
        
        public RenameCategoryAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_RenameCategory")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(
                PaletteUtils.getBundleString("CTL_NewName"), // NOI18N
                PaletteUtils.getBundleString("CTL_Rename")); // NOI18N
            desc.setInputText(categoryNode.getName());

            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
                String newName = null;
                try {
                    newName = desc.getInputText();
                    if (!"".equals(newName)) // NOI18N
                    categoryNode.setName(newName);
                } catch (IllegalArgumentException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
    }

    static class ReorderCategoryAction extends AbstractAction {
        private Node categoryNode;
        
        public ReorderCategoryAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_OrderItems")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            Index order = (Index)categoryNode.getCookie(Index.class);
            if (order != null) {
                order.reorder();
            }
        }
        
        public boolean isEnabled() {
            return (categoryNode.getCookie(Index.class) != null);
        }

    }
    
    static class PasteBeanAction extends AbstractAction {
        private Node categoryNode;
        
        public PasteBeanAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_Paste")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            PasteType type = getPasteType();
            if (type != null) {
                try {
                    Transferable trans = type.paste();
                    if (trans != null) {
                        ClipboardOwner owner = trans instanceof ClipboardOwner ?
                            (ClipboardOwner)trans : new StringSelection(""); // NOI18N
                        Clipboard clipboard = (Clipboard)Lookup.getDefault().lookup(ExClipboard.class);
                        clipboard.setContents(trans, owner);
                    }
                } catch (java.io.IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
        public boolean isEnabled() {
            return (getPasteType() != null);
        }

        private PasteType getPasteType() {
            Clipboard clipboard = (Clipboard) Lookup.getDefault().lookup(ExClipboard.class);
            Transferable trans = clipboard.getContents(this);
            if (trans != null) {
                PasteType[] pasteTypes = categoryNode.getPasteTypes(trans);
                if (pasteTypes != null && pasteTypes.length != 0)
                    return pasteTypes[0];
            }
            return null;
        }

    }
    
    static class CutBeanAction extends AbstractAction {
        private Node beanNode;
        
        public CutBeanAction(Node beanNode) {
            this.beanNode = beanNode;
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_Cut")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            try {
                Transferable trans = beanNode.clipboardCut();
                if (trans != null) {
                    Clipboard clipboard = (Clipboard)
                        Lookup.getDefault().lookup(ExClipboard.class);
                    clipboard.setContents(trans, new StringSelection("")); // NOI18N
                }
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
    }
    
    static class CopyBeanAction extends AbstractAction {
        private Node beanNode;
        
        public CopyBeanAction(Node beanNode) {
            this.beanNode = beanNode;
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_Copy")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            try {
                Transferable trans = beanNode.clipboardCopy();
                if (trans != null) {
                    Clipboard clipboard = (Clipboard)
                        Lookup.getDefault().lookup(ExClipboard.class);
                    clipboard.setContents(trans, new StringSelection("")); // NOI18N
                }
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
    }
    
    static class RemoveBeanAction extends AbstractAction {
        private Node beanNode;
        
        public RemoveBeanAction(Node beanNode) {
            this.beanNode = beanNode;
            putValue(Action.NAME, CPManager.getBundle().getString("CTL_Delete")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            // first user confirmation...
            String message = MessageFormat.format(
                PaletteUtils.getBundleString("FMT_ConfirmBeanDelete"), // NOI18N
                new Object[] { beanNode.getDisplayName() });

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message,
                PaletteUtils.getBundleString("CTL_ConfirmBeanTitle"), // NOI18N
                NotifyDescriptor.YES_NO_OPTION);

            if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
                try {
                    beanNode.destroy();
                } catch (java.io.IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
    }

}
