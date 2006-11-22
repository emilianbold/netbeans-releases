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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.services;


import java.awt.datatransfer.Transferable;
import java.util.*;
import javax.swing.JSeparator;
import org.netbeans.core.NbPlaces;
import org.openide.NotifyDescriptor;
import org.openide.actions.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.nodes.FilterNode.Children;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.*;

/** The node for the menu folder representation.
* Delegates most of its functionality to the original data folder node.
* Final only for better performance, can be unfinaled.
*
* @author Dafe Simonek
*/
public final class MenuFolderNode extends DataFolder.FolderNode {

    /** Actions which this node supports */
    static SystemAction[] staticActions;
    /** Actions of this node when it is top level menu node */
    static SystemAction[] topStaticActions;

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private DataFolder folder;

    public MenuFolderNode () {
        this (NbPlaces.getDefault().menus ());
    }

    /** Constructs this node with given node to filter.
    */
    MenuFolderNode (DataFolder folder) {
        folder.super(new MenuFolderChildren(folder));
        this.folder = folder;
        //JST: it displays only Menu as name!    super.setDisplayName(NbBundle.getBundle (MenuFolderNode.class).getString("CTL_Menu_name"));
        setShortDescription(NbBundle.getMessage(MenuFolderNode.class, "CTL_Menu_hint"));

        setIconBaseWithExtension ("org/netbeans/core/resources/menu.gif"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (MenuFolderNode.class);
    }

    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        PasteType pType = ActionPasteType.getPasteType((DataFolder)getDataObject() , t);
        if (pType != null) {
            //now we know that the tranferable holds a paste-able Action
            s.add(pType);
        }        
    }
    
    /** Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes () {
        NewType newMenu = new NewType() {
            public String getName () {
                return NbBundle.getMessage(MenuFolderNode.class, "CTL_newMenuName");
            }
            public void create () throws java.io.IOException {
                newMenu();
            }
        };
        
        if(getParentNode() instanceof MenuFolderNode) {
            return new NewType[] {
                newMenu,
                //Fixed bug #5610 Added support for adding new separator through popup menu
                new NewType () {
                    public String getName () {
                        return NbBundle.getMessage(MenuFolderNode.class, "CTL_newMenuSeparator");
                    }
                    public void create () throws java.io.IOException {
                        newSeparator();
                    }
                }
            };
        } else {
            // #14415. Just menu for main menu bar.
            return new NewType[] {newMenu};
        }
    }

    void newMenu () {
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine
                                        (NbBundle.getMessage(MenuFolderNode.class, "CTL_newMenuLabel"),
                                         NbBundle.getMessage(MenuFolderNode.class, "CTL_newMenuDialog"));
        il.setInputText (NbBundle.getMessage(MenuFolderNode.class, "CTL_newMenu"));

        Object ok = org.openide.DialogDisplayer.getDefault ().notify (il);
        if (ok == NotifyDescriptor.OK_OPTION) {
            String s = il.getInputText();
            if (!s.equals ("")) { // NOI18N
                FileObject mnFO = folder.getPrimaryFile();
                try {
                    FileObject newFO = mnFO.getFileObject(s);
                    if (newFO == null) {
                        String lastName = getLastName();
                        
                        newFO = mnFO.createFolder (s);

                        // #13015. Set new item as last one.
                        if(lastName != null) {
                            mnFO.setAttribute(
                                lastName + "/" + newFO.getNameExt(), // NOI18N
                                Boolean.TRUE
                            );
                        }
                    }
                } catch (java.io.IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    //Fixed bug #5610 Added support for adding new separator through popup menu
    void newSeparator () {
        try {
            InstanceDataObject instData = InstanceDataObject.find
            (folder,null,"javax.swing.JSeparator"); // NOI18N
            
            String lastName = getLastName();
            DataObject d; 
            
            if (instData == null) {
                d = InstanceDataObject.create
                (folder,null,"javax.swing.JSeparator"); // NOI18N
            } else {
                d = instData.copy(folder);
            }

            // #13015. Set new item as last one.
            if(lastName != null) {
                folder.getPrimaryFile().setAttribute(
                    lastName + "/" + d.getPrimaryFile().getNameExt(), // NOI18N
                    Boolean.TRUE
                );
            }
        } catch (java.io.IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
    //End

    /** Gets name of last child.
     * @return name of last menu or <code>null</code> if there is no one */
    private String getLastName() {
        String lastName = null;
        Node[] ch = getChildren().getNodes();
        if(ch.length > 0) {
            Node last = ch[ch.length - 1];
            DataObject d = (DataObject)last.getCookie(DataObject.class);
            if(d != null) {
                lastName = d.getPrimaryFile().getNameExt();
            }
        }
        
        return lastName;
    }
    
    /** Actions.
    * @return array of actions for this node
    */
    protected SystemAction[] createActions () {
        if (isTopLevel()) {
            if (topStaticActions == null)
                topStaticActions = new SystemAction [] {
                                       SystemAction.get (FileSystemAction.class),
                                       null,
                                       SystemAction.get(ReorderAction.class),
                                       null,
                                       SystemAction.get(PasteAction.class),
                                       null,
                                       SystemAction.get(NewAction.class),
                                       null,
                                       SystemAction.get(ToolsAction.class),
                                       SystemAction.get(PropertiesAction.class),
                                   };
            return topStaticActions;
        } else {
            if (staticActions == null)
                staticActions = new SystemAction [] {
                                    SystemAction.get (FileSystemAction.class),
                                    null,
                                    SystemAction.get(MoveUpAction.class),
                                    SystemAction.get(MoveDownAction.class),
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(CutAction.class),
                                    SystemAction.get(CopyAction.class),
                                    SystemAction.get(PasteAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    SystemAction.get(RenameAction.class),
                                    null,
                                    SystemAction.get(NewAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class),
                                };
            return staticActions;
        }
    }

    /** Creates properties for this node */
    public Node.PropertySet[] getPropertySets () {
        if (isTopLevel()) {
            return NO_PROPERTIES;
        } else {
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.Name(
                    this,
                    NbBundle.getMessage(MenuFolderNode.class, "PROP_MenuName"),
                    NbBundle.getMessage(MenuFolderNode.class, "HINT_MenuName")
                )
            );
            return sheet.toArray();
        }
    }

    /** Supports index cookie in addition to standard support.
    *
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    *
    public Node.Cookie getCookie (Class type) {
        if (Index.class.isAssignableFrom(type)) {
            // search for data object
            DataFolder dataObj = (DataFolder)super.getCookie(DataFolder.class);
            if (dataObj != null) {
                return new DataFolder.Index (dataObj, this);
            }
        }
        return super.getCookie(type);
    }
    */

    /** Utility - is this top level menu node? */
    boolean isTopLevel () {
        final Node n = getParentNode();
        return (n == null) || !(n instanceof MenuFolderNode);
    }

    public boolean canDestroy () {
        if (isTopLevel ()) return false;
        return super.canDestroy ();
    }

    public boolean canCut () {
        if (isTopLevel ()) return false;
        return super.canCut ();
    }

    public boolean canRename () {
        if (isTopLevel ()) return false;
        return super.canRename ();
    }

    /** Children for the MenuFolderNode. Creates MenuFolderNodes or
    * MenuItemNodes as filter subnodes...
    */
    static final class MenuFolderChildren extends FilterNode.Children {

        /** @param or original node to take children from */
        public MenuFolderChildren (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overriden, returns MenuFolderNode filters of original nodes.
        *
        * @param node node to create copy of
        * @return MenuFolderNode filter of the original node
        */
        protected Node copyNode (Node node) {
            DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
            if (df != null) {
                return new MenuFolderNode(df);
            }
            return new MenuItemNode(node);
        }

    }

    static final class MenuItemNode extends FilterNode {

        /** Actions which this node supports */
        static SystemAction[] staticActions;
        /** Actions which this node supports (when representing a menu separator) */
        static SystemAction[] separatorStaticActions;

        /** Constructs new filter node for menu item */
        MenuItemNode (Node filter) {
            super(filter, Children.LEAF);
        }

        /** Make Move Up and Move Down actions be enabled. */
        public boolean equals (Object o) {
            if (o == null) return false;
            return this == o || getOriginal ().equals (o) || o.equals (getOriginal ());
        }

        /** Actions.
        * @return array of actions for this node
        */
        public SystemAction[] getActions () {
            InstanceCookie.Of ic = (InstanceCookie.Of)getCookie(InstanceCookie.Of.class);
            if (ic != null && ic.instanceOf(JSeparator.class)) {
                //do not allow copy&paste for menu separators
                if( null == separatorStaticActions ) {
                    separatorStaticActions = new SystemAction [] {
                                    SystemAction.get(MoveUpAction.class),
                                    SystemAction.get(MoveDownAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class),
                                };
                }
                return separatorStaticActions;
            }
            
            if (staticActions == null) {
                staticActions = new SystemAction [] {
                                    SystemAction.get(MoveUpAction.class),
                                    SystemAction.get(MoveDownAction.class),
                                    null,
                                    SystemAction.get(CutAction.class),
                                    SystemAction.get(CopyAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class),
                                };
            }
            return staticActions;
        }

        /** Disallows renaming.
        */
        public boolean canRename () {
            return false;
        }

        /** Creates properties for this node */
        public Node.PropertySet[] getPropertySets () {
            /*
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.Name(
                    this,
                    bundle.getString("PROP_MenuItemName"),
                    bundle.getString("HINT_MenuItemName")
                )
            );
            return sheet.toArray();
             */
            return new Node.PropertySet[] { };
        }

    } // end of MenuItemNode

}
