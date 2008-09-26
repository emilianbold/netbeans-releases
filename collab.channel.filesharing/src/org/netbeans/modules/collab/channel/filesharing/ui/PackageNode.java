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

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.Image;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author        Ayub Khan, ayub.khan@sun.com
 */
public class PackageNode extends AbstractNode implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final Image PACKAGE_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/collab/channel/filesharing/resources/package.gif", true
        ); // NOI18N	
    public static final Image EMPTY_PACKAGE_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/collab/channel/filesharing/resources/packageEmpty.gif", true
        ); // NOI18N	

    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public final String ICON_BASE = "org/netbeans/modules/collab/channel/filesharing/resources/package"; // NOI18N
    private final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {  };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private boolean isLocal = false;
    private FilesharingContext context = null;

    /**
     *
     *
     */
    public PackageNode(String name, boolean isLocal, FilesharingContext context) {
        super(new PackageNodeChildren(isLocal, context));
        this.isLocal = isLocal;
        this.context = context;
        setName(name);
        setDisplayName(name.replaceAll(FILE_SEPERATOR, "."));
        setIconBase(ICON_BASE);
        systemActions = DEFAULT_ACTIONS;
    }

    public Image getIcon(int type) {
        Image icon = PACKAGE_BADGE;

        if (getChildren().getNodesCount() == 0) {
            icon = EMPTY_PACKAGE_BADGE;
        }

        return icon;
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PackageNode.class);
    }

    /**
     *
     *
     */
    public boolean canCut() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canCopy() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canDestroy() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canRename() {
        return false;
    }

    /**
     *
     *
     */
    public void destroy() throws IOException {
        super.destroy();
    }

    /**
     *
     *
     */
    public PackageNodeChildren getPackageNodeChildren() {
        return (PackageNodeChildren) getChildren();
    }

    public static class PackageNodeChildren extends Children.Keys implements NodeListener, PropertyChangeListener {
        ////////////////////////////////////////////////////////////////////////////
        // Instance variables
        ////////////////////////////////////////////////////////////////////////////
        private Collection keys;
        private String displayName;
        private boolean isLocal;
        private FilesharingContext context = null;

        /**
         *
         *
         */
        public PackageNodeChildren(boolean isLocal, FilesharingContext context) {
            super();
            this.isLocal = isLocal;
            this.context = context;
            Debug.out.println("In PackageNodeChildren ");
        }

        /**
         *
         *
         */
        public boolean add(Node[] nodes) {
            Debug.out.println("PN add: " + nodes.length);

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
        protected void addNotify() {
            refreshChildren();
        }

        /**
         *
         *
         */
        protected void removeNotify() {
            _setKeys(Collections.EMPTY_SET);
        }

        /**
         *
         *
         */
        protected Node[] createNodes(Object key) {
            Debug.out.println("In PackageNodeChildren createNodes");

            Node[] result = null;

            try {
                result = new Node[] { new SharedProjectNode((Node) key, isLocal, context, false) };
            } catch (Exception e) {
                Debug.debugNotify(e);
            }

            return result;
        }

        /**
         *
         *
         */
        public Collection getKeys() {
            return keys;
        }

        /**
         *
         *
         */
        public void _setKeys(Collection value) {
            keys = value;
            super.setKeys(value);
        }

        /**
         *
         *
         */
        public void refreshChildren() {
            java.util.List keys = new ArrayList();

            try {
                _setKeys(keys);
            } catch (Exception e) {
                Debug.errorManager.notify(e);
            }
        }

        /**
         *
         *
         */
        public void propertyChange(PropertyChangeEvent event) {
        }

        /**
         *
         *
         */
        public void childrenAdded(NodeMemberEvent ev) {
            // Ignore
        }

        /**
         *
         *
         */
        public void childrenRemoved(NodeMemberEvent ev) {
            // Ignore
        }

        /**
         *
         *
         */
        public void childrenReordered(NodeReorderEvent ev) {
            // Ignore
        }

        /**
         *
         *
         */
        public void nodeDestroyed(NodeEvent ev) {
            refreshChildren();
        }
    }
}
