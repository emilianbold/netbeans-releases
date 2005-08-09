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


package org.netbeans.modules.palette;

import java.beans.BeanInfo;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.*;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.*;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.modules.palette.ui.PalettePanel;

import org.openide.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.ExClipboard;


/**
 * Class providing various useful methods for palette classes.
 *
 * @author S Aubrecht
 */
public final class Utils {

    private static FileObject paletteFolder;
    private static DataFolder paletteDataFolder;

    private Utils() {
    }

    // -----------

    public static ResourceBundle getBundle() {
        return NbBundle.getBundle(Utils.class);
    }

    public static String getBundleString(String key) {
        return getBundle().getString(key);
    }
    
    public static Action[] mergeActions( Action[] first, Action[] second ) {
        if( null == first )
            return second;
        if( null == second )
            return first;
        
        Action[] res = new Action[first.length+second.length+1];
        System.arraycopy( first, 0, res, 0, first.length );
        res[first.length] = null;
        System.arraycopy( second, 0, res, first.length+1, second.length );
        return res;
    }
    
    public static boolean isReadonly( Node node ) {
        Object val = node.getValue( PaletteController.ATTR_IS_READONLY );
        if( null == val ) {
            DataObject dobj = (DataObject)node.getCookie( DataObject.class );
            if( null != dobj ) {
                val = dobj.getPrimaryFile().getAttribute( PaletteController.ATTR_IS_READONLY );
            }
        }
        if( null != val ) {
            return Boolean.valueOf( val.toString() ).booleanValue();
        } else {
            return !node.canDestroy();
        }
    }
    
    public static void addCustomizationMenuItems( JPopupMenu popup, PalettePanel panel ) {
        popup.addSeparator();
        popup.add( new ShowNamesAction( panel ) );
        popup.add( new ChangeIconSizeAction( panel ) );
        popup.addSeparator();
        popup.add( new ShowCustomizerAction( panel.getController() ) );
    }
    
    /**
     * Find a Node representing the given category.
     *
     * @param root Palette's root node.
     * @param categoryName Name of the category to search for.
     * @return Category with the given name or null.
     */
    public static Node findCategoryNode( Node root, String categoryName ) {
        Node category = root.getChildren().findChild( categoryName );
        return category;
    }

    /**
     * An action to create a new palette category.
     */
    public static class NewCategoryAction extends AbstractAction {
        private Node paletteNode;
        
        /**
         * @param paletteRootNode Palette's root node.
         */
        public NewCategoryAction( Node paletteRootNode ) {
            putValue(Action.NAME, getBundleString("CTL_CreateCategory")); // NOI18N
            this.paletteNode = paletteRootNode;
        }

        public void actionPerformed(ActionEvent event) {
            NewType[] newTypes = paletteNode.getNewTypes();
            try {
                if( null != newTypes && newTypes.length > 0 ) {
                    newTypes[0].create();
                }
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    /**
     * An action to change the order of categories.
     */
    static class ReorderCategoriesAction extends AbstractAction {
        private Node paletteNode;
        public ReorderCategoriesAction( Node paletteNode ) {
            putValue(Action.NAME, getBundleString("CTL_OrderCategories")); // NOI18N
            this.paletteNode = paletteNode;
        }
        
        public void actionPerformed(ActionEvent event) {
            Index order = (Index)paletteNode.getCookie(Index.class);
            if (order != null) {
                order.reorder();
            }
        }
        
        public boolean isEnabled() {
            return (paletteNode.getCookie(Index.class) != null);
        }
        
    }
    
    /**
     * An action to sort categories alphabetically.
     */
    static class SortCategoriesAction extends AbstractAction {
        private Node paletteNode;
        public SortCategoriesAction( Node paletteNode ) {
            putValue(Action.NAME, getBundleString("CTL_SortCategories")); // NOI18N
            this.paletteNode = paletteNode;
        }
        
        public void actionPerformed(ActionEvent event) {
            Index order = (Index)paletteNode.getCookie(Index.class);
            if (order != null) {
                final Node[] nodes = paletteNode.getChildren().getNodes( DefaultModel.canBlock() );
                Arrays.sort( nodes, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Node n1 = (Node)o1;
                        Node n2 = (Node)o2;
                        return n1.getDisplayName().compareTo( n2.getDisplayName() );
                    }
                } );
                int[] perm = new int[nodes.length];
                for( int i=0; i<perm.length; i++ ) {
                    perm[i] = order.indexOf( nodes[i] );
                }
                order.reorder( perm );
            }
        }
        
        public boolean isEnabled() {
            return (paletteNode.getCookie(Index.class) != null);
        }
    }
    
    /**
     * An action to show/hide palette item names.
     */
    private static class ShowNamesAction extends AbstractAction {
        
        private PalettePanel panel;
        
        public ShowNamesAction( PalettePanel panel ) {
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent event) {
            panel.setShowItemNames( !panel.getShowItemNames() );
        }
        
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                boolean showNames = panel.getShowItemNames();
                String name = getBundleString(showNames ? "CTL_HideNames" : "CTL_ShowNames"); // NOI18N
                return name;
            } else {
                return super.getValue(key);
            }
        }
        
    }
    
    /**
     * An action to change the size of palette icons.
     */
    private static class ChangeIconSizeAction extends AbstractAction {
        
        private PalettePanel panel;
        
        public ChangeIconSizeAction( PalettePanel panel ) {
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent event) {
            int oldSize = panel.getIconSize();
            int newSize = (oldSize == BeanInfo.ICON_COLOR_16x16) ?
                BeanInfo.ICON_COLOR_32x32 : BeanInfo.ICON_COLOR_16x16;
            panel.setIconSize( newSize );
        }
        
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                String namePattern = getBundleString("CTL_IconSize"); // NOI18N
                String name = MessageFormat.format(namePattern,
                new Object[] {new Integer(panel.getIconSize())});
                return name;
            } else {
                return super.getValue(key);
            }
        }
        
    }
    
    /**
     * An action to restore palette's default state.
     */
    static class RefreshPaletteAction extends AbstractAction {
        
        public RefreshPaletteAction() {
            putValue(Action.NAME, getBundleString("CTL_RefreshPalette")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            PalettePanel.getDefault().doRefresh();
        }
        
    }
    
    /**
     * An action to remove a category and all items in it.
     */
    static class DeleteCategoryAction extends AbstractAction {
        private Node categoryNode;
        
        public DeleteCategoryAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, getBundleString("CTL_DeleteCategory")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            // first user confirmation...
            String message = MessageFormat.format(
                getBundleString("FMT_ConfirmCategoryDelete"), // NOI18N
                new Object [] { categoryNode.getName() });

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message,
                getBundleString("CTL_ConfirmCategoryTitle"), // NOI18N
                NotifyDescriptor.YES_NO_OPTION);

            if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
                try {
                    categoryNode.destroy();
                } catch (java.io.IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
        public boolean isEnabled() {
            return categoryNode.canDestroy();
        }
    }
    
    /**
     * An action to rename a category.
     */
    static class RenameCategoryAction extends AbstractAction {
        private Node categoryNode;
        
        public RenameCategoryAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, getBundleString("CTL_RenameCategory")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(
                getBundleString("CTL_NewName"), // NOI18N
                getBundleString("CTL_Rename")); // NOI18N
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
        
        public boolean isEnabled() {
            return categoryNode.canRename();
        }
    }

    /**
     * An action to change the order of items in a category.
     */
    static class ReorderItemsAction extends AbstractAction {
        private Node categoryNode;
        
        public ReorderItemsAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, getBundleString("CTL_OrderItems")); // NOI18N
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
    
    
    /**
     * An action to sort categories alphabetically.
     */
    static class SortItemsAction extends AbstractAction {
        private Node categoryNode;
        public SortItemsAction( Node categoryNode ) {
            putValue(Action.NAME, getBundleString("CTL_SortItems")); // NOI18N
            this.categoryNode = categoryNode;
        }
        
        public void actionPerformed(ActionEvent event) {
            Index order = (Index)categoryNode.getCookie(Index.class);
            if (order != null) {
                final Node[] nodes = categoryNode.getChildren().getNodes( DefaultModel.canBlock() );
                Arrays.sort( nodes, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Node n1 = (Node)o1;
                        Node n2 = (Node)o2;
                        return n1.getDisplayName().compareTo( n2.getDisplayName() );
                    }
                } );
                int[] perm = new int[nodes.length];
                for( int i=0; i<perm.length; i++ ) {
                    perm[i] = order.indexOf( nodes[i] );
                }
                order.reorder( perm );
            }
        }
        
        public boolean isEnabled() {
            return (categoryNode.getCookie(Index.class) != null);
        }
    }
    
    /**
     * An action to create a new palette item from clipboard contents.
     */
    static class PasteItemAction extends AbstractAction {
        private Node categoryNode;
        
        public PasteItemAction(Node categoryNode) {
            this.categoryNode = categoryNode;
            putValue(Action.NAME, getBundleString("CTL_Paste")); // NOI18N
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
    
    /**
     * An action to cut a palette item to clipboard.
     */
    static class CutItemAction extends AbstractAction {
        private Node itemNode;
        
        public CutItemAction(Node itemNode) {
            this.itemNode = itemNode;
            putValue(Action.NAME, getBundleString("CTL_Cut")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            try {
                Transferable trans = itemNode.clipboardCut();
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
    
    /**
     * An action to copy palette item to clipboard.
     */
    static class CopyItemAction extends AbstractAction {
        private Node itemNode;
        
        public CopyItemAction(Node itemNode) {
            this.itemNode = itemNode;
            putValue(Action.NAME, getBundleString("CTL_Copy")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            try {
                Transferable trans = itemNode.clipboardCopy();
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
    
    /**
     * An action to remove an item from palette.
     */
    static class RemoveItemAction extends AbstractAction {
        private Node itemNode;
        
        public RemoveItemAction(Node itemNode) {
            this.itemNode = itemNode;
            putValue(Action.NAME, getBundleString("CTL_Delete")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            // first user confirmation...
            String message = MessageFormat.format(
                getBundleString("FMT_ConfirmBeanDelete"), // NOI18N
                new Object[] { itemNode.getDisplayName() });

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message,
                getBundleString("CTL_ConfirmBeanTitle"), // NOI18N
                NotifyDescriptor.YES_NO_OPTION);

            if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
                try {
                    itemNode.destroy();
                } catch (java.io.IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
        public boolean isEnabled() {
            return itemNode.canDestroy();
        }
    }
    
    /**
     * An action to remove an item from palette.
     */
    private static class ShowCustomizerAction extends AbstractAction {
        private PaletteController palette;
        
        public ShowCustomizerAction( PaletteController palette ) {
            this.palette = palette;
            putValue(Action.NAME, getBundleString("CTL_ShowCustomizer")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            palette.showCustomizer();
        }
    }
}
