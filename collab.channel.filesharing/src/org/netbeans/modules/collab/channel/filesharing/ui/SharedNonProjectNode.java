/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        protected Node[] createNodes(Node node) {
            Debug.out.println("In SNPN createNodes: ");

            Node[] result = new Node[] {  };

            try {
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
            } catch (Exception e) {
                Debug.debugNotify(e);
            }

            return result;
        }
    }
}
