/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.checkout.ModuleListInformation;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.awt.*;
import java.beans.BeanInfo;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;

/**
 * Represents module aliases subtree.
 *
 * @author Petr Kuzel
 */
final class AliasesNode extends AbstractNode {

    public static AliasesNode create(Client.Factory client, CVSRoot root) {
        AliasesNode node = new AliasesNode(new AliasesChildren(client, root));
        node.setDisplayName(org.openide.util.NbBundle.getMessage(AliasesNode.class, "BK2005"));
        return node;
    }

    private AliasesNode(Children children) {
        super(children);
        setIconBaseWithExtension("org/netbeans/modules/versioning/system/cvss/ui/selectors/defaultFolder.gif");  // NOI18N
    }

    public Image getIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image)UIManager.get("Nb.Explorer.Folder.icon");  // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    public Image getOpenedIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image)UIManager.get("Nb.Explorer.Folder.openedIcon");  // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    static class AliasesChildren extends Children.Keys implements Runnable {

        private final Client.Factory clientFactory;
        private final CVSRoot root;
        private RequestProcessor.Task task;

        public AliasesChildren(Client.Factory client, CVSRoot root) {
            this.clientFactory = client;
            this.root = root;
        }

        protected void addNotify() {
            super.addNotify();
            AbstractNode waitNode = new WaitNode(org.openide.util.NbBundle.getMessage(AliasesNode.class, "BK2006"));
            setKeys(Collections.singleton(waitNode));
            task = CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(this);
        }

        protected void removeNotify() {
            task.cancel();
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Node) {
                return new Node[] {(Node)key};
            }

            Node alias = AliasNode.create((ModuleListInformation)key);  // NOI18N
            return new Node[] {alias};
        }

        public void run() {
            try {
                List aliases = ModuleSelector.listAliases(clientFactory.createClient(), root);
                setKeys(aliases);
            } catch (CommandException e) {
                setKeys(Collections.singleton(errorNode(e)));
            } catch (AuthenticationException e) {
                setKeys(Collections.singleton(errorNode(e)));
            }
        }

        private Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(AliasesNode.class, "BK2007"));
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }
    }

    static class AliasNode extends AbstractNode {

        public static AliasNode create(ModuleListInformation alias) {
            String name = alias.getModuleName();
            Lookup lookup = Lookups.singleton(name);
            AliasNode node = new AliasNode(Children.LEAF, lookup);
            node.setName(name);
            String paths = alias.getPaths();
            node.setShortDescription(paths);
            return node;
        }

        private AliasNode(Children children, Lookup lookup) {
            super(children, lookup);
            setIconBaseWithExtension("org/netbeans/modules/versioning/system/cvss/ui/selectors/defaultFolder.gif");  // NOI18N
        }

        public Image getIcon(int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                img = (Image)UIManager.get("Nb.Explorer.Folder.icon");  // NOI18N
            }
            if (img == null) {
                img = super.getIcon(type);
            }
            return badge(img);
        }

        public Image getOpenedIcon(int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                img = (Image)UIManager.get("Nb.Explorer.Folder.openedIcon");  // NOI18N
            }
            if (img == null) {
                img = super.getIcon(type);
            }
            return badge(img);
        }

        private Image badge(Image image) {
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/versioning/system/cvss/ui/selectors/link.png", true);  // NOI18N
            return ImageUtilities.mergeImages(image, badge, 0, 8);
        }
    }
}
