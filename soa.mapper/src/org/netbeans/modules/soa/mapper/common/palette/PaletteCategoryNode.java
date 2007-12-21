/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mapper.common.palette;

import java.util.ArrayList;

import javax.swing.Action;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;

/**
 * The PaletteCategoryNode is a Node representing the Palette content
 * in the tree under Environment.
 *
 * @author Tientien Li
 */
public class PaletteCategoryNode
        extends org.openide.loaders.DataFolder.FolderNode
        implements IPaletteCategory {

    /** The icons for PaletteCategory */
    private static String mIconURL =
        "com/stc/collabeditor/common/palette/resources/paletteCategory.gif";

    /** Field icon32URL           */
    private static String mIcon32URL =
       "com/stc/collabeditor/common/palette/resources/paletteCategory32.gif";

    /** Field mNodeProperties           */
    private static final org.openide.nodes.Node.PropertySet[] NODE_PROPERTIES =
        new org.openide.nodes.Node.PropertySet[0];

    /** Field mStaticActions           */
    private static SystemAction[] mStaticActions;

    /** Field mFolder           */
    private DataFolder mFolder;

    /**
     * Constructor PaletteCategoryNode
     *
     *
     * @param folder the data folder for the palette category
     *
     */
    public PaletteCategoryNode(DataFolder folder) {

        folder.super(new PaletteCategoryNodeChildren(folder));

        this.mFolder = folder;

        Object catName = folder.getPrimaryFile().getAttribute("categoryName");

        if ((catName != null) && (catName instanceof String)) {
            setName((String) catName, false);
        } else {
            String name        = getName();
            String displayName = getDisplayName();

            if (!name.equals(displayName)) {
                setName(displayName, false);
            }
        }

    }

    /**
     * get a requested palette Category Attribute
     *
     *
     * @param attr the request palette category attribute
     *
     * @return the attibute value
     *
     */
    public Object getCategoryAttribute(String attr) {
        return mFolder.getPrimaryFile().getAttribute(attr);    // NOI18N
    }

    /**
     * set the Palette category Name
     *
     *
     * @param name the category name
     *
     */
    public void setName(String name) {
        setName(name, true);
    }

    /**
     * set the Palette category Name
     *
     *
     * @param name the palette category name
     * @param rename is this a rename request
     *
     */
    public void setName(String name, boolean rename) {

        if (rename) {
            if (!checkCategoryName(name, this)) {
                return;
            }

            try {
                DataObject dobj       = getDataObject();
                org.openide.filesystems.FileObject fobj = dobj.getPrimaryFile();
                String     folderName = convertToFolderName(name,
                                                            fobj.getName());

                fobj.setAttribute("categoryName", null);        // NOI18N
                dobj.rename(folderName);

                if (!folderName.equals(name)) {
                    fobj.setAttribute("categoryName", name);    // NOI18N
                }
            } catch (java.io.IOException ex) {
                RuntimeException e = new IllegalArgumentException();
                ErrorManager.getDefault().annotate(e, ex);
                throw e;
            }
        }

        super.setName(name, false);
    }

    /**
     * get the NetBeans Help Context
     *
     *
     * @return the help context
     *
     */
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("gui.options.component-palette");
    }

    //------------------------------------------------------

    /**
     * Get the tool tip of this node
     *
     *
     * @return the tool tip text
     *
     */
    public String getToolTip() {
        return getShortDescription().replace('-', '.');
    }
    
    public Node[] getItemNodes() {
        DataObject[] items = mFolder.getChildren();
        ArrayList list = new ArrayList();
        
        for (int i = 0; i < items.length; i++) {
            DataObject dObj = items[i];

            if (dObj != null) {
                PaletteItemNode itemNode = new PaletteItemNode(dObj.getNodeDelegate());
                list.add(itemNode);
            }

           
        }

        return (Node[]) list.toArray(new PaletteItemNode[list.size()]);
    }
    
    //------------------------------------------------------

    /**
     * get the valid item nodes of this palette category
     *
     *
     * @return the list of valid item nodes
     *
     */
    public Node[] getValidItemNodes() {

//        Node[]         nodes      = getChildren().getNodes();
//        java.util.List validNodes = new java.util.ArrayList(nodes.length);
        Node[]         nodes      = getItemNodes();
        return nodes;
    }

    /**
     * Supports index cookie in addition to standard support.
     * @param type the class to look for
     * @return instance of that class or null if this class of cookie is not
     * supported
     */
    public org.openide.nodes.Node.Cookie getCookie(Class type) {

        if (org.openide.nodes.Index.class.isAssignableFrom(type)) {

            // search for data object
            DataFolder dataObj =
                (DataFolder) super.getCookie(DataFolder.class);

            if (dataObj != null) {
                return new PaletteCategoryIndex(
                    dataObj, this, (PaletteCategoryNodeChildren) getChildren());
            }
        }

        return super.getCookie(type);
    }

    /**
     * get the palette category icon
     *
     *
     * @param type the type of category
     *
     * @return the icon image
     *
     */
    public java.awt.Image getIcon(int type) {

        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16)
                || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return org.openide.util.Utilities.loadImage(mIconURL);
        } else {
            return org.openide.util.Utilities.loadImage(mIcon32URL);
        }
    }

    /**
     * get the Opened Icon of this palette category
     *
     *
     * @param type the type of category
     *
     * @return the icon image
     *
     */
    public java.awt.Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     * Support for new types that can be created in this node.
     * @return array of new type operations that are allowed
     */
    public org.openide.util.datatransfer.NewType[] getNewTypes() {
        return new org.openide.util.datatransfer.NewType[0];
    }

    /** Set up the array of associated node actions.
     *
     * @return array of actions for this node
     */
    public Action[] getActions(boolean context) {

        if (mStaticActions == null) {
            mStaticActions = new SystemAction[] {
                SystemAction.get(org.openide.actions.FileSystemAction.class),
                null,
                SystemAction.get(org.openide.actions.MoveUpAction.class),
                SystemAction.get(org.openide.actions.MoveDownAction.class),
                SystemAction.get(org.openide.actions.ReorderAction.class),
                null,
                SystemAction.get(org.openide.actions.PasteAction.class),
                null,
                SystemAction.get(org.openide.actions.DeleteAction.class),
                SystemAction.get(org.openide.actions.RenameAction.class),
            };
        }

        return mStaticActions;
    }

    /**
     * Creates properties for this node
     *
     * @return the node properties
     */
    public org.openide.nodes.Node.PropertySet[] getPropertySets() {
        return NODE_PROPERTIES;
    }

    /**
     * create Paste transferable data types
     *
     *
     * @param t the trasnsferable data
     * @param s the source list
     *
     */
    protected void createPasteTypes(java.awt.datatransfer.Transferable t,
                                    java.util.List s) {

        Node[]  nodes;
        boolean cut;

        nodes = org.openide.nodes.NodeTransfer.nodes(t,
                org.openide.nodes.NodeTransfer.MOVE);

        if (nodes == null) {
            cut   = false;
            nodes = org.openide.nodes.NodeTransfer.nodes(t,
                    org.openide.nodes.NodeTransfer.CLIPBOARD_COPY);

            if (nodes == null) {
                return;
            }
        } else {
            cut = true;
        }

        java.util.ArrayList list = new java.util.ArrayList();

        for (int i = 0; i < nodes.length; i++) {
            Node           node = nodes[i];
            DataObject     dobj =
                (DataObject) node.getCookie(DataObject.class);
            InstanceCookie ic   =
                (InstanceCookie) node.getCookie(InstanceCookie.class);
            org.openide.filesystems.FileObject     fo   = (dobj != null)
                                  ? dobj.getPrimaryFile()
                                  : null;

            if ((getChildren().findChild(node.getName()) == null)
                    && (dobj != null) && dobj.isValid() && (ic != null)
                    && (!cut || "instance".equals(fo.getExt())    // NOI18N
                    || "shadow".equals(fo.getExt())))  {
                list.add(dobj);
            }
        }

        if (!list.isEmpty()) {
            s.add(new BeanPaste(list, cut, false));

            if (!cut) {
                s.add(new BeanPaste(list, cut, true));
            }
        }
    }

    /** Checks category name if it is valid and if there's already not
     * a category with the same name.
     *
     * @param name name to be checked
     * @param namedNode node which name is checked or null if it doesn't exist
     * @return true if the name is OK
     */
    static boolean checkCategoryName(String name, Node namedNode) {

        boolean invalid = false;

        if ((name == null) || "".equals(name)) {
            invalid = true;
        } else {    // name should not start with . or contain only spaces
            for (int i = 0, n = name.length(); i < n; i++) {
                char ch = name.charAt(i);

                if ((ch == '.') || ((ch == ' ') && (i + 1 == n))) {
                    invalid = true;

                    break;
                } else if (ch != ' ') {
                    break;
                }
            }
        }

        if (invalid) {
            NotifyDescriptor desc =
                new org.openide.NotifyDescriptor.Message(
                    java.text.MessageFormat.format(
                        PaletteManager.getBundle().getString(
                            "ERR_InvalidName"), new Object[]{ name }),
                            org.openide.NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return false;
        }

        /*
         * Can't not call PaletteNode static method now... 08/20/02
        Node[] nodes = PaletteNode.getPaletteNode().getChildren().getNodes();
        for (int i=0; i < nodes.length; i++)
            if (name.equals(nodes[i].getName()) && nodes[i] != namedNode) {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(MessageFormat.format(
                    PaletteManager.getBundle().getString("FMT_CategoryExists"),
                                             new Object[] { name }),
                          NotifyDescriptor.INFORMATION_MESSAGE));
                return false;
            }
        */
        return true;
    }

    /** Converts category name to name that can be used as name of folder
     * for the category (restricted even to package name).
     *
     * @param name the new name
     * @param currentName the current category name
     *
     * @return the updated name
     */
    static String convertToFolderName(String name, String currentName) {

        if ((name == null) || "".equals(name)) {
            return null;
        }

        int          i;
        int          n        = name.length();
        StringBuffer nameBuff = new StringBuffer(n);
        char         ch       = name.charAt(0);

        if (Character.isJavaIdentifierStart(ch)) {
            nameBuff.append(ch);

            i = 1;
        } else {
            nameBuff.append('_');

            i = 0;
        }

        while (i < n) {
            ch = name.charAt(i);

            if (Character.isJavaIdentifierPart(ch)) {
                nameBuff.append(ch);
            }

            i++;
        }

        String fName = nameBuff.toString();

        if ("_".equals(fName)) {
            fName = "Category";    // NOI18N
        }

        if (fName.equals(currentName)) {
            return fName;
        }

        /*
         * Can't not call PaletteNode static method now... 08/20/02
        FileObject paletteFO = PaletteNode.getPaletteFolder().getPrimaryFile();
        String freeName = null;
        boolean nameOK = false;
        for (i=0; !nameOK; i++) {
            freeName = i > 0 ? fName + "_" + i : fName;
            if (Utilities.isWindows()) {
                nameOK = true;
                Enumeration en = paletteFO.getChildren(false);
                while (en.hasMoreElements()) {
                    FileObject fo = (FileObject)en.nextElement();
                    String fn = fo.getName();
                    String fe = fo.getExt();
                    // case-insensitive on Windows
                    if ((fe == null || "".equals(fe))
                            && fn.equalsIgnoreCase(freeName)) {
                        nameOK = false;
                        break;
                    }
                }
            }
            else nameOK = paletteFO.getFileObject(freeName) == null;
        }
        return freeName;
        */
        return fName;
    }

    /**
     * Children for the PaletteCategoryNode. Creates PaletteCategoryNodes as
     * filter subnodes...
     *
     */
    private static class PaletteCategoryNodeChildren
            extends org.openide.nodes.FilterNode.Children {

        /**
         * create the node children list taking from the original node
         *
         * @param folder the original node to take children from
         */
        public PaletteCategoryNodeChildren(DataFolder folder) {
            super(folder.getNodeDelegate());
        }

        /**
         * Overriden, returns PaletteCategoryNode filters for folders and copies
         * of other nodes.
         * @param node node to create copy of
         * @return PaletteCategoryNode filter of the original node or Node's
         * clone if it is not a DataFolder
         */
        protected Node copyNode(Node node) {

            if (node.getCookie(DataObject.class) != null) {
           /* && node.getCookie(InstanceCookie.class) != null */
                return new PaletteItemNode(node);
            }

            return node.cloneNode();
        }
    }

    /**
     * This class serves as index cookie implementation for the
     * PaletteCategoryNode object. Allows reordering of palette items.
     */
    static final class PaletteCategoryIndex
            extends org.openide.loaders.DataFolder.Index {

        /** The children we are working with */
        private PaletteCategoryNodeChildren children;

        /**
         * Constructor PaletteCategoryIndex
         *
         *
         * @param df the data folder
         * @param children the list of palette category children nodes
         *
         */
        PaletteCategoryIndex(DataFolder df,
                             Node node,
                             PaletteCategoryNodeChildren children) {

            super(df, node);

            this.children = children;
        }

        /**
         * Overrides DataFolder.Index.getNodesCount().
         *
         * @return count of the nodes from the asociated chidren.
         */
        public int getNodesCount() {
            return children.getNodesCount();
        }

        /**
         * Overrides DataFolder.Index.getNodes().
         * Returns array of subnodes from asociated children.
         * @return array of subnodes
         */
        public Node[] getNodes() {
            return children.getNodes();
        }
    }

    /**
     * Class BeanPaste
     *
     *
     * @author
     */
    private class BeanPaste extends org.openide.util.datatransfer.PasteType {

        /** Field dataObjs           */
            private java.util.List dataObjs;

        /** Field cut           */
        private boolean cut;

        /** Field createLink           */
        private boolean createLink;

        /**
         * Constructor BeanPaste
         *
         *
         * @param dataObjs
         * @param cut
         * @param link
         *
         */
        public BeanPaste(java.util.List dataObjs, boolean cut, boolean link) {

            this.dataObjs   = dataObjs;
            this.cut        = cut;
            this.createLink = link;
        }

        /**
         * Method getName
         *
         *
         * @return
         *
         */
        public String getName() {

            return createLink
                   ? PaletteManager.getBundle().getString("CTL_PasteLink")
                   :    // NOI18N
                       PaletteManager.getBundle().getString("CTL_PasteBean");
        }

        /**
         * Method getHelpCtx
         *
         *
         * @return
         *
         */
        public org.openide.util.HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx(BeanPaste.class.getName());
        }

        /** Paste.
        */
        public final java.awt.datatransfer.Transferable paste()
                throws java.io.IOException {

            DataFolder df = PaletteCategoryNode.this.mFolder;
            java.util.Iterator   it = dataObjs.iterator();

            while (it.hasNext()) {
                DataObject dobj = (DataObject) it.next();
                org.openide.filesystems.FileObject fo   = dobj.getPrimaryFile();

                if (!createLink && ("instance".equals(fo.getExt())
                                    || "shadow".equals(fo.getExt()))) {
                    // copy or cut data objects directly (can be done with
                    // instance or shadow objects only)
                    Object iconAttr     =
                        fo.getAttribute("SystemFileSystem.icon");
                    Object beaninfoAttr = fo.getAttribute("beaninfo");

                    if (cut) {
                        if (dobj.isMoveAllowed()) {
                            dobj.move(df);
                        } else {
                            dobj = null;
                        }
                    } else {
                        dobj = dobj.copy(df);
                    }

                    if (dobj != null) {
                        if (iconAttr != null) {
                            dobj.getPrimaryFile()
                                .setAttribute("SystemFileSystem.icon",
                                              iconAttr);
                        }

                        if (beaninfoAttr != null) {
                            dobj.getPrimaryFile().setAttribute("beaninfo",
                                                               beaninfoAttr);
                        }
                    }
                } else if (!cut) {  // other objects can't be moved
                    if (!createLink && ("java".equals(fo.getExt())
                                        || "class".equals(fo.getExt()))) {
                        // instead of copying java data object create an
                        // instance data object
                        InstanceCookie ic =
                            (InstanceCookie) dobj
                                .getCookie(InstanceCookie.class);
                    } else {  // create link to the original data object
                        dobj.createShadow(df);
                    }
                }
            }

            return null;    // clear clipboard
        }
    }
}
