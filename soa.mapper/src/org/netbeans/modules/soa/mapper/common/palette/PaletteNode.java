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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;


/**
 * The PaletteNode is a Node representing the Palette content in the
 * tree under Environment.
 *
 * @author Tientien Li
 */
public class PaletteNode extends org.openide.loaders.DataFolder.FolderNode {

    /** icons for the PaletteNode */
    private static String iconURL =
        "com/stc/collabeditor/common/palette/resources/palette.gif";

    /** Field icon32URL           */
    private static String icon32URL =
        "com/stc/collabeditor/common/palette/resources/palette32.gif";

    /** Field NO_PROPERTIES           */
    private static final org.openide.nodes.Node.PropertySet[] NO_PROPERTIES =
        new org.openide.nodes.Node.PropertySet[0];

    /** Field staticActions           */
    private static SystemAction[] staticActions;

    /** Field paletteFolder           */
    private DataFolder paletteFolder = null;


    /**
     * Creates a new palette node
     *
     * @param folderName the name of palette folder
     */
    public PaletteNode(String folderName) {
        this(getThisPaletteFolder(folderName));
        initizalShortDesc();
    }

    /**
     * Constructor for creating a new PaletteNode
     *
     *
     * @param folder the palette folder
     *
     */
    PaletteNode(DataFolder folder) {

        folder.super(new PaletteNodeChildren(folder));

        paletteFolder = folder;

        setDisplayName(PaletteManager.getBundle()
            .getString("CTL_Component_palette"));    // NOI18N
        initizalShortDesc();
    }

    /**
     * get the Help context
     *
     *
     * @return the help context
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.options.component-palette");    // NOI18N
    }

    /**
     * get the palette Category Nodes
     *
     *
     * @return the list of palette categories
     *
     */
    public Node[] getCategoryNodes() {
    	DataObject[] categories = paletteFolder.getChildren();
    	ArrayList list = new ArrayList();
    	
        for (int i = 0; i < categories.length; i++) {
        	DataFolder df = (DataFolder) categories[i].getCookie(DataFolder.class);

            if (df != null) {
            	PaletteCategoryNode categoryNode = new PaletteCategoryNode(df);
                list.add(categoryNode);
            }

           
        }

        return (Node[]) list.toArray(new PaletteCategoryNode[list.size()]);
    }

    /**
     * get the Palette Folder
     *
     *
     * @return the palette folder
     *
     */
    DataFolder getPaletteFolder() {
        return paletteFolder;
    }

    private void initizalShortDesc() {
        String tooltip = (String) getNodeAttribute("Tooltip");
        if (tooltip != null) {
            this.setShortDescription(tooltip);
        } else {
            this.setShortDescription("");
        }
    }

    /**
     * get a Palette Folder by name
     *
     *
     * @param folderName the platte folder name
     *
     * @return the palette data folder
     *
     */
    static DataFolder getThisPaletteFolder(String folderName) {

        try {
            org.openide.filesystems.FileObject fo =
                FileUtil.getConfigFile(folderName);

            if (fo == null) {

                // resource not found, try to create new folder
                fo = FileUtil.getConfigRoot()
                    .createFolder(folderName);
            }

            return DataFolder.findFolder(fo);
        } catch (java.io.IOException ex) {
            throw new InternalError("Folder not found and cannot be created: "
                                    + folderName);
        }
    }

    /**
     * Supports index cookie in addition to standard support.
     *
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
                return new PaletteIndex(dataObj,
                                        this,
                                        (PaletteNodeChildren) getChildren());
            }
        }

        return super.getCookie(type);
    }

    /**
     * get the node image Icon
     *
     *
     * @param type the icon type
     *
     * @return the node image icon
     *
     */
    public java.awt.Image getIcon(int type) {

        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16)
                || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return ImageUtilities.loadImage(iconURL);
        } else {
            return ImageUtilities.loadImage(icon32URL);
        }
    }

    /**
     * get the node Opened Icon
     *
     *
     * @param type the icon type
     *
     * @return the node opened icon
     *
     */
    public java.awt.Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     * Support for new types that can be created in this node.
     *
     * @return array of new type operations that are allowed
     */
    public org.openide.util.datatransfer.NewType[] getNewTypes() {
        return new org.openide.util.datatransfer.NewType[]{
            new NewCategory()
        };
    }

    /**
     * create a list of assocated node actions.
     *
     * @return array of actions for this node
     */
    public Action[] getActions(boolean context) {

        if (staticActions == null) {
            staticActions = new SystemAction[] {
                SystemAction.get(org.openide.actions.FileSystemAction.class),
                null,
                SystemAction.get(org.openide.actions.MoveUpAction.class),
                SystemAction.get(org.openide.actions.MoveDownAction.class),
                SystemAction.get(org.openide.actions.ReorderAction.class),
                null,
                // PasteAction yes or not?
                //                SystemAction.get(PasteAction.class),
                //                null,
                SystemAction.get(org.openide.actions.NewAction.class),
                //                null,
                //XXX SystemAction.get(ToolsAction.class),
                //                SystemAction.get(PropertiesAction.class),
            };
        }

        return staticActions;
    }

    /**
     * Creates properties for this node
     *
     * @return the list of properties for this node
     */
    public org.openide.nodes.Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }


    /**
     * get the value of an item Attribute
     *
     *
     * @param attr the requested item attribute
     *
     * @return the attribute value
     *
     */
    public Object getNodeAttribute(String attr) {
        return paletteFolder.getPrimaryFile().getAttribute(attr);
    }

    // ------------------

    /**
     * create a palette Category
     *
     *
     * @throws java.io.IOException if encountered file IO errors
     *
     */
    void createNewCategory()
        throws java.io.IOException {

        java.util.ResourceBundle bundle = PaletteManager.getBundle();
        org.openide.NotifyDescriptor.InputLine input  =
            new org.openide.NotifyDescriptor.InputLine
                    .InputLine(bundle.getString("CTL_NewCategoryName"),
                         bundle.getString("CTL_NewCategoryTitle"));

        input.setInputText(bundle.getString("CTL_NewCategoryValue"));

        while (DialogDisplayer.getDefault().notify(input)
                == org.openide.NotifyDescriptor.OK_OPTION) {
            String categoryName = input.getInputText();

            if (PaletteCategoryNode.checkCategoryName(categoryName, null)) {
                String folderName = PaletteCategoryNode
                        .convertToFolderName(categoryName, null);
                FileObject folder = getPaletteFolder().getPrimaryFile()
                        .createFolder(folderName);

                if (!folderName.equals(categoryName)) {
                    folder.setAttribute("categoryName", categoryName);
                }

                break;
            }
        }
    }

    // -------------------

    /**
     * Children for the PaletteNode. Creates PaletteCategoryNodes as filter
     * subnodes.
     */
    private static final class PaletteNodeChildren
            extends org.openide.nodes.FilterNode.Children {

        /**
         *
         * Create a class for the palette node children
         *
         * @param folder the original node to take children from
         */
        public PaletteNodeChildren(DataFolder folder) {
            super(folder.getNodeDelegate());
        }

        /**
         * Overriden, returns PaletteCategoryNode filters for folders and
         * copies of other nodes.
         * @param node node to create copy of
         * @return PaletteNode filter of the original node or Node's clone if
         * it is not a DataFolder
         */
        protected Node copyNode(Node node) {

            DataFolder df = (DataFolder) node.getCookie(DataFolder.class);

            if (df != null) {
                return new PaletteCategoryNode(df);
            }

            return node.cloneNode();
        }
    }

    /**
     * This class serves as index cookie implementation for the PaletteNode
     * object. Allows reordering of palette categories.
     */
    private static final class PaletteIndex
            extends org.openide.loaders.DataFolder.Index {

        /** The children we are working with */
        private PaletteNodeChildren children;

        /**
         * Constructor for creating the Palette Index
         *
         *
         * @param df the palette data folder
         * @param children the palette node children
         *
         */
        PaletteIndex(final DataFolder df,
                     final Node node,
                     final PaletteNodeChildren children) {

            super(df, node);

            this.children = children;
        }

        /** Overrides DataFolder.Index.getNodesCount() to return
         *  count of the nodes from the asociated chidren.
         *
         * @return the count of the nodes from the asociated chidren.
         */
        public int getNodesCount() {
            return children.getNodesCount();
        }

        /** Overrides DataFolder.Index.getNodes().
         * Returns array of subnodes from asociated children.
         * @return array of subnodes
         */
        public Node[] getNodes() {
            return children.getNodes();
        }
    }

    /**
     * New type for creation of new palette category.
     */
    private final class NewCategory
            extends org.openide.util.datatransfer.NewType {

        /**
         * Display name for the creation action. This should be
         * presented as an item in a menu.
         * @return the name of the action
         */
        public String getName() {
            return PaletteManager.getBundle().getString("CTL_NewCategory");
        }

        /**
         * Help context for the creation action.
         * @return the help context
         */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(NewCategory.class);
        }

        /**
         * Create the object.
         * @throws java.io.IOException if something fails
         */
        public void create()
            throws java.io.IOException {
            PaletteNode.this.createNewCategory();
        }
    }
}
