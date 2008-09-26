/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.openide.loaders.DataShadow;
import org.openide.nodes.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.*;

import java.awt.Image;
import java.awt.datatransfer.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.UnShareFileAction;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SharedProjectNode extends FilterNode implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Node originalNode;
    private boolean isLocal = false;
    private boolean isFolder = true;
    private FilesharingContext context = null;
    private String displayName = null;
    private Image badge;

    /**
     *
     *
     */
    public SharedProjectNode(Node node, boolean isLocal, FilesharingContext context) {
        this(node.getName(), node, isLocal, context);
    }

    /**
     *
     *
     */
    public SharedProjectNode(String displayName, Node node, boolean isLocal, FilesharingContext context) {
        super(node, new SharedProjectNodeChildren(node, isLocal, context));
        this.originalNode = node;
        this.isLocal = isLocal;
        this.context = context;
        init(displayName, node);
    }

    /**
     *
     *
     */
    public SharedProjectNode(Node node, boolean isLocal, FilesharingContext context, boolean canHaveChild) {
        super(node);
        this.originalNode = node;
        this.isLocal = isLocal;
        this.context = context;
        init(node.getName(), node);
    }

    private void init(String displayName, Node node) {
         DataObject d = (DataObject)node.getCookie(DataObject.class);
         if(d!=null && d.getPrimaryFile()!=null && d.getPrimaryFile().isData())
                 this.isFolder=false;
         setDisplayName(displayName, node);
    } 

    private void setDisplayName(String name, Node node) {
        this.displayName = name;

        if (displayName.equals(WEB_FOLDER_NAME)) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/web/project/ui/resources/webPagesBadge.gif"); //NOI18N
        }

        DataObject d = (DataObject) node.getCookie(DataObject.class);

        if (!this.isFolder) {
            displayName = d.getPrimaryFile().getNameExt();
        }

        try { //if lnk change displayName

            DataShadow lnk = (DataShadow) node.getCookie(DataShadow.class);

            if (lnk != null) {
                Debug.out.println("SPN Original node path: " + lnk.getOriginal().getPrimaryFile().getPath());
                displayName = lnk.getOriginal().getPrimaryFile().getNameExt();
            }
        } catch (Throwable th) {
            th.printStackTrace(Debug.out);
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

    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private Image computeIcon(boolean opened, int type) {
        Image image = opened ? originalNode.getOpenedIcon(type) : originalNode.getIcon(type);

        if (this.badge != null) {
            image = ImageUtilities.mergeImages(image, this.badge, 7, 7);
        }

        return image;
    }

    /**
     *
     *
     */
    public PasteType getDropType(Transferable transferable, int action, int index) {
        // We need to filter out "move" as an option, lest the user 
        // accidentally move their original file to the conversation

        /*if (action!=DnDConstants.ACTION_COPY)
        {
                return null;
        }
        return super.getDropType(transferable,action,index);*/
        return null;
    }

    public PasteType[] getPasteTypes(Transferable t) {
        return new PasteType[] {  };
    }

    public NewType[] getNewTypes() {
        return new NewType[] {  };
    }

    public Action[] getActions(boolean ctx) {
        Debug.out.println("SPN getActions: ");

        SystemAction[] actions = super.getActions();
        Debug.out.println("SPN getActions: " + actions);

        List newActions = new ArrayList();

        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];

            if (action != null) {
                String displayName = (String) action.getValue(Action.NAME);
                Debug.out.println("action displayName: " + displayName);

                if (displayName.equals("Edit")) //NoI18n
                 {
                    newActions.add(actions[i]);
                }
            }
        }

        if (isLocal && !isFolder) {
            newActions.add(new UnShareFileAction(this.context, this));
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
        return false;
    }

    public boolean canRename() {
        return false;
    }

    public Transferable clipboardCut() {
        return null;
    }

    /** Represents children of SharedProjectNode
         *
     */
    private static class SharedProjectNodeChildren extends FilterNode.Children {
        private boolean isLocal;
        private FilesharingContext context = null;

        /**
         *
         *
         */
        public SharedProjectNodeChildren(Node node, boolean isLocal, FilesharingContext context) {
            super(node);
            this.isLocal = isLocal;
            this.context = context;
        }

        /**
         *
         *
         */
        public boolean add(Node[] nodes) {
            Debug.out.println("SPN add: " + nodes.length);

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
            Debug.out.println("In SPN: createNodes");

            Node[] result = null;

            try {
                DataObject d = (DataObject) node.getCookie(DataObject.class);

                if (d != null) {
                    if (d.getPrimaryFile().isData()) {
                        String fileName = d.getPrimaryFile().getNameExt();

                        if (fileName.equals(".nbattrs")) {
                            return new Node[] {  };
                        }

                        result = new Node[] { new SharedProjectNode(d.getNodeDelegate(), isLocal, context, false) };
                    } else {
                        result = new Node[] { new SharedProjectNode(d.getNodeDelegate(), isLocal, context) };
                    }
                }
            } catch (Exception e) {
                Debug.debugNotify(e);
            }

            return result;
        }
    }
}
