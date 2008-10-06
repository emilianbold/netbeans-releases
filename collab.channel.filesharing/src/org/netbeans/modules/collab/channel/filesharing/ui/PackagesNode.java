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
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class PackagesNode extends AbstractNode implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String ICON_BASE = "org/netbeans/modules/collab/ui/resources/group_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {  };
    public static final Image PACKAGE_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/collab/channel/filesharing/resources/packageBadge.gif", true
        ); // NOI18N		

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private static String packageRootPath;
    private Node originalNode;
    private boolean isLocal = false;
    private FilesharingContext context = null;

    /**
     *
     *
     */
    public PackagesNode(
        String displayName, String packageRootPath, Node originalNode, boolean isLocal, FilesharingContext context
    ) {
        super(new PackagesNodeChildren(originalNode, isLocal, context));
        this.isLocal = isLocal;
        this.context = context;
        Debug.out.println("In PackagesNode ");
        setName(originalNode.getName());

        if (displayName != null) {
            setDisplayName(displayName);
        } else {
            setDisplayName(originalNode.getDisplayName());
        }

        //setIcon(originalNode.getIcon(0));
        systemActions = DEFAULT_ACTIONS;
        this.packageRootPath = packageRootPath;
        this.originalNode = originalNode;
    }

    public String getName(int type) {
        Debug.out.println("In PackagesNode getName");

        return originalNode.getName();
    }

    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private Image computeIcon(boolean opened, int type) {
        Image icon = opened ? originalNode.getOpenedIcon(BeanInfo.ICON_COLOR_16x16)
                            : originalNode.getIcon(BeanInfo.ICON_COLOR_16x16);

        return ImageUtilities.mergeImages(icon, PACKAGE_BADGE, 7, 7);
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
    public PackagesNodeChildren getPackagesNodeChildren() {
        return (PackagesNodeChildren) getChildren();
    }

    public static class PackagesNodeChildren extends Children.Keys implements NodeListener, PropertyChangeListener {
        ////////////////////////////////////////////////////////////////////////////
        // Instance variables
        ////////////////////////////////////////////////////////////////////////////
        private Collection keys;
        private Node originalNode;
        private boolean isLocal;
        private FilesharingContext context = null;

        /**
         *
         *
         */
        public PackagesNodeChildren(Node originalNode, boolean isLocal, FilesharingContext context) {
            super();
            Debug.out.println("In PackagesNodeChildren ");
            this.originalNode = originalNode;
            this.isLocal = isLocal;
            this.context = context;
        }

        /**
         *
         *
         */
        public boolean add(Node[] nodes) {
            Debug.out.println("PSN add: " + nodes.length);

            for (int i = 0; i < nodes.length; i++) {
                super.add(createNodes(nodes[i]));
            }

            return true;
        }

        /**
         *
         *
         */
        public boolean add(String name, Node[] nodes) {
            Debug.out.println("PSN add: " + nodes.length);

            for (int i = 0; i < nodes.length; i++) {
                super.add(createNodes(name, nodes[i]));
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
            Node[] result = null;

            try {
                Node node = (Node) key;
                DataObject d = (DataObject) node.getCookie(DataObject.class);
                String name = DEFAULT_PKG;

                if (d != null) {
                    name = d.getPrimaryFile().getPath();
                    Debug.out.println("PSN createNode full: " + name);
                }

                int lastIndex = name.lastIndexOf(FILE_SEPERATOR);

                //uri="src/japp/Main.java" if dragNodePath is "C:/test/javaapp/src/japp/Main.java", 
                //project path is "C:/test/javaapp"
                //"japp"
                Debug.out.println("PSN createNode projectPath: " + packageRootPath);

                if (lastIndex > (packageRootPath.length() + 1)) {
                    name = name.substring(packageRootPath.length() + 1, lastIndex);
                } else {
                    name = DEFAULT_PKG;
                }

                Debug.out.println("PSN createNode short: " + name);
                result = createNodes(name, key);
            } catch (Exception e) {
                Debug.debugNotify(e);
            }

            return result;
        }

        /**
         *
         *
         */
        protected Node[] createNodes(String name, Object key) {
            Node[] result = null;

            try {
                Node node = (Node) key;
                DataObject d = (DataObject) node.getCookie(DataObject.class);

                if ((name == null) || name.equals("")) {
                    name = DEFAULT_PKG;
                }

                Node pkg = new PackageNode(name, isLocal, context);
                pkg.getChildren().add(new Node[] { (Node) key });
                result = new Node[] { pkg };
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
