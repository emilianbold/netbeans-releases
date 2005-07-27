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
import java.text.MessageFormat;
import java.util.Collections;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.lookup.ProxyLookup;

/**
 * A node for palette category.
 *
 * @author S. Aubrecht
 */
class CategoryNode extends FilterNode {
    
    static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];
    
    static final String CAT_NAME = "categoryName"; // NOI18N

    private Action[] actions;

    CategoryNode( Node originalNode, Lookup lkp ) {
        super( originalNode, 
               new Children( originalNode, lkp ),
               new ProxyLookup( new Lookup[] { lkp, originalNode.getLookup() } ) );
        
        DataFolder folder = (DataFolder)originalNode.getCookie( DataFolder.class );
        if( null != folder ) {
            FileObject fob = folder.getPrimaryFile();
            Object catName = fob.getAttribute( CAT_NAME );
            if (catName instanceof String)
                setName((String)catName, false);
        }
    }

    // -------

    public void setName(String name) {
        setName(name, true);
    }

    public void setName(String name, boolean rename) {
        if (rename) {
            if( !checkCategoryName( getParentNode(), name, this ) )
                return; // invalid name
            try {
                DataFolder folder = (DataFolder)getCookie( DataFolder.class );
                if( null != folder ) {
                    FileObject fo = folder.getPrimaryFile();
                    String folderName = convertCategoryToFolderName( fo.getParent(), name, fo.getName());
                    fo.setAttribute( CAT_NAME, null );
                    folder.rename(folderName);
                    if (!folderName.equals(name))
                        fo.setAttribute( CAT_NAME, name );
                }
            }
            catch (java.io.IOException ex) {
                RuntimeException e = new IllegalArgumentException();
                org.openide.ErrorManager.getDefault().annotate(e, ex);
                throw e;
            }
        }
        super.setName(name);
    }

    public String getShortDescription() {
        return getDisplayName();
    }

    public Action[] getActions(boolean context) {
        if (actions == null) {
            actions = new Action[] {
                new Utils.NewCategoryAction( getParentNode() ),
                null,
                new Utils.DeleteCategoryAction(this),
                new Utils.RenameCategoryAction(this),
                null,
                new Utils.ShowNamesAction(),
                new Utils.ChangeIconSizeAction(),
//                    null,
//                    new PaletteUtils.PasteBeanAction(this),
                null,
                new Utils.ReorderCategoryAction(this),
                new Utils.ReorderCategoriesAction( getParentNode() ),
                null,
                new Utils.RefreshPaletteAction()
            };
        }
        PaletteActions customActions = (PaletteActions)getParentNode().getLookup().lookup( PaletteActions.class );
        if( null != customActions ) {
            return Utils.mergeActions( actions, customActions.getCustomCategoryActions( getLookup() ) );
        }
        return actions;
    }

    public Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }

    public boolean canDestroy() {
        return !Utils.isReadonly( getOriginal() );
    }
    
    private static class Children extends FilterNode.Children {

        private Lookup lkp;
        private PaletteFilter filter;
        
        public Children(Node original, Lookup lkp) {
            super(original);
            this.lkp = lkp;
            this.filter = (PaletteFilter)lkp.lookup( PaletteFilter.class );
        }

        protected Node copyNode(Node node) {
            return new ItemNode( node );
        }
        
        protected Node[] createNodes(Object key) {
            Node n = (Node) key;
            
            if( null == filter || filter.isValidItem( n.getLookup() ) ) {
                return new Node[] { copyNode(n) };
            }

            return null;
        }
        
        public void resultChanged(LookupEvent ev) {
            Node[] nodes = original.getChildren().getNodes();
            setKeys( Collections.EMPTY_LIST );
            setKeys( nodes );
        }
    }

    /** Checks category name if it is valid and if there's already not
     * a category with the same name.
     * @param name name to be checked
     * @param namedNode node which name is checked or null if it doesn't exist yet
     * @return true if the name is OK
     */
    static boolean checkCategoryName( Node parentNode, String name, Node namedNode) {
        boolean invalid = false;
        if (name == null || "".equals(name)) // NOI18N
            invalid = true;
        else // name should not start with . or contain only spaces
            for (int i=0, n=name.length(); i < n; i++) {
                char ch = name.charAt(i);
                if (ch == '.' || (ch == ' ' && i+1 == n)) {
                    invalid = true;
                    break;
                }
                else if (ch != ' ')
                    break;
            }

        if (invalid) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(MessageFormat.format(
                      Utils.getBundleString("ERR_InvalidName"), // NOI18N
                                         new Object[] { name }),
                      NotifyDescriptor.INFORMATION_MESSAGE));
            return false;
        }

        Node[] nodes = parentNode.getChildren().getNodes();
        for (int i=0; i < nodes.length; i++)
            if (name.equals(nodes[i].getName()) && nodes[i] != namedNode) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(MessageFormat.format(
                          Utils.getBundleString("FMT_CategoryExists"), // NOI18N
                                             new Object[] { name }),
                          NotifyDescriptor.INFORMATION_MESSAGE));
                return false;
            }

        return true;
    }

    /** Converts category name to name that can be used as name of folder
     * for the category (restricted even to package name).
     */ 
    static String convertCategoryToFolderName( FileObject paletteFO, 
                                                       String name,
                                                       String currentName)
    {
        if (name == null || "".equals(name)) // NOI18N
            return null;

        int i;
        int n = name.length();
        StringBuffer nameBuff = new StringBuffer(n);

        char ch = name.charAt(0);
        if (Character.isJavaIdentifierStart(ch)) {
            nameBuff.append(ch);
            i = 1;
        }
        else {
            nameBuff.append('_');
            i = 0;
        }

        while (i < n) {
            ch = name.charAt(i);
            if (Character.isJavaIdentifierPart(ch))
                nameBuff.append(ch);
            i++;
        }

        String fName = nameBuff.toString();
        if ("_".equals(fName)) // NOI18N
            fName = "Category"; // NOI18N
        if (fName.equals(currentName))
            return fName;

        // having the base name, make sure it is not used yet
        String freeName = null;
        boolean nameOK = false;

        for (i=0; !nameOK; i++) {
            freeName = i > 0 ? fName + "_" + i : fName; // NOI18N

            if (Utilities.isWindows()) {
                nameOK = true;
                java.util.Enumeration en = paletteFO.getChildren(false);
                while (en.hasMoreElements()) {
                    FileObject fo = (FileObject)en.nextElement();
                    String fn = fo.getName();
                    String fe = fo.getExt();

                    // case-insensitive on Windows
                    if ((fe == null || "".equals(fe)) && fn.equalsIgnoreCase(freeName)) { // NOI18N
                        nameOK = false;
                        break;
                    }
                }
            }
            else nameOK = paletteFO.getFileObject(freeName) == null;
        }
        return freeName;
    }
}
