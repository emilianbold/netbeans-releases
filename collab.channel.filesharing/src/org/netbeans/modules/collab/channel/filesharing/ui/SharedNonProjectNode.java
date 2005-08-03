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
package org.netbeans.modules.collab.channel.filesharing.ui;

import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CreateNonProjectAction;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SharedNonProjectNode extends FilterNode {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String displayName;
    private boolean isLocal = false;
    private boolean isFolder = true;
    private FilesharingContext context = null;

    /**
     *
     *
     */
    public SharedNonProjectNode(Node node, boolean isLocal, FilesharingContext context) {
        this(node.getName(), node, isLocal, context);
    }

    /**
     *
     *
     */
    public SharedNonProjectNode(String displayName, Node node, boolean isLocal, FilesharingContext context) {
        super(node, new SharedNonProjectNodeChildren(node, isLocal, context));
        this.displayName = displayName;
        this.isLocal = isLocal;
        this.context = context;

        DataObject d = (DataObject) this.getCookie(DataObject.class);

        if ((d != null) && (d.getPrimaryFile() != null) && d.getPrimaryFile().isData()) {
            this.isFolder = false;
        }
    }

    /**
     *
     *
     */
    public SharedNonProjectNode(Node node, boolean isLocal, FilesharingContext context, boolean canHaveChild) {
        super(node);
        this.isLocal = isLocal;
        this.context = context;

        DataObject d = (DataObject) this.getCookie(DataObject.class);

        if ((d != null) && (d.getPrimaryFile() != null) && d.getPrimaryFile().isData()) {
            this.isFolder = false;
        }
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return NbBundle.getMessage(
            SharedProjectNode.class, "LBL_SharedFileNode_FileAnnotation",
            (this.displayName != null) ? this.displayName : super.getDisplayName()
        );
    }

    public Action[] getActions(boolean context) {
        Debug.out.println("SNPN getActions: context");

        SystemAction[] actions = super.getActions();
        Debug.out.println("SNPN getActions: " + actions);

        List newActions = new ArrayList();

        if (isLocal && isFolder) {
            newActions.add(new CreateNonProjectAction(this.context, this));
        }

        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];

            if (action != null) {
                String actionName = (String) action.getValue(Action.NAME);
                Debug.out.println("actionName: " + actionName);
                Debug.out.println("isLocal: " + isLocal);

                DataObject d = (DataObject) this.getCookie(DataObject.class);

                if (d != null) {
                    if ((d.getPrimaryFile() != null) && d.getPrimaryFile().isData()) {
                        if (isLocal || strContains(actionName, "Edit")) { //NoI18n
                            newActions.add(actions[i]);
                        }
                    } else {
                        if (isLocal && !strContains(actionName, "Rename")) { //NoI18n
                            newActions.add(actions[i]);
                        }
                    }
                }
            }
        }

        return (SystemAction[]) newActions.toArray(new SystemAction[0]);
    }

    public boolean canCopy() {
        return true;
    }

    public boolean canCut() {
        return false;
    }

    public boolean canDestroy() {
        return isLocal;
    }

    public boolean canRename() {
        return isLocal && !isFolder;
    }

    private static boolean strContains(String str, String pattern) {
        return str.indexOf(pattern) != -1;
    }

    /** Represents children of SharedNonProjectNode
         *
     */
    private static class SharedNonProjectNodeChildren extends FilterNode.Children {
        private boolean isLocal;
        private FilesharingContext context = null;

        /**
         *
         *
         */
        public SharedNonProjectNodeChildren(Node node, boolean isLocal, FilesharingContext context) {
            super(node);
            this.isLocal = isLocal;
            this.context = context;
        }

        /**
         *
         *
         */
        public boolean add(Node[] nodes) {
            Debug.out.println("SNPN add: " + nodes.length);

            for (int i = 0; i < nodes.length; i++) {
                if (super.findChild(nodes[i].getName()) == null) {
                    super.add(createNodes(nodes[i]));
                }
            }

            return true;
        }

        /**
         *
         *
         */
        protected Node[] createNodes(Object key) {
            Debug.out.println("In SNPN createNodes: ");

            Node[] result = new Node[] {  };

            try {
                if (key instanceof Node) {
                    Node node = (Node) key;
                    DataObject d = (DataObject) node.getCookie(DataObject.class);

                    if (d != null) {
                        if (d.getPrimaryFile().isData()) {
                            String fileName = d.getPrimaryFile().getNameExt();

                            if (fileName.equals(".nbattrs") || fileName.startsWith(".LCK")) {
                                return new Node[] {  };
                            }

                            result = new Node[] {
                                    new SharedNonProjectNode(d.getNodeDelegate(), this.isLocal, this.context, false)
                                };
                        } else {
                            result = new Node[] {
                                    new SharedNonProjectNode(d.getNodeDelegate(), this.isLocal, this.context)
                                };
                        }
                    }
                }
            } catch (Exception e) {
                Debug.debugNotify(e);
            }

            return result;
        }
    }
}
