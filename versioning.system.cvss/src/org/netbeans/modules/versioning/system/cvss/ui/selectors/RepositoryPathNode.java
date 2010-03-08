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
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.command.CommandException;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.awt.*;
import java.beans.BeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a path in repository, its children
 * subfolders.
 *
 * <p>Lookup contains a string identifing rpresented path.
 *
 * @author Petr Kuzel
 */
public class RepositoryPathNode extends AbstractNode {

    public static RepositoryPathNode create(Client.Factory clientFactory, CVSRoot root, String path) {

        assert path.startsWith("/") == false : path;  // NOI18N

        RepositoryPathChildren kids = new RepositoryPathChildren(clientFactory, root, path);
        Lookup lookup = Lookups.singleton(path);
        RepositoryPathNode node = new RepositoryPathNode(kids, lookup);

        String name = root.getRepository();
        if (path.equals("") == false) { // NOI18N
            String[] atoms = path.split("/");    // NOI18N
            if (atoms.length > 0) {
                name = atoms[atoms.length -1];
            }
        }
        node.setDisplayName(name);
        return node;
    }

    private RepositoryPathNode(Children children, Lookup lookup) {
        super(children, lookup);
        setIconBaseWithExtension("org/netbeans/modules/versioning/system/cvss/ui/selectors/defaultFolder.gif"); // NOI18N
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

    static class RepositoryPathChildren extends Children.Keys implements Runnable {

        private final Client.Factory clientFactory;
        private final CVSRoot root;
        private final String path;
        private RequestProcessor.Task task;

        public RepositoryPathChildren(Client.Factory client, CVSRoot root, String path) {
            this.clientFactory = client;
            this.root = root;
            this.path = path;
        }

        protected void addNotify() {
            super.addNotify();
            AbstractNode waitNode = new WaitNode(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2024"));
            setKeys(Collections.singleton(waitNode));
            RequestProcessor rp = RequestProcessor.getDefault();
            task = rp.post(this);
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

            String relPath = path.equals("") ? (String) key : path + "/" + key; // NOI18N
            Node pathNode = RepositoryPathNode.create(clientFactory, root, relPath);
            return new Node[] {pathNode};
        }

        public void run() {
            Client client = clientFactory.createClient();
            try {
                List keys = ModuleSelector.listRepositoryPath(client, root, path);
                setKeys(keys);
            } catch (CommandException e) {
                setKeys(Collections.singleton(errorNode(e)));
            } catch (AuthenticationException e) {
                setKeys(Collections.singleton(errorNode(e)));
            } catch (IllegalArgumentException e) {
                setKeys(Collections.singleton(errorNode(e)));
            } finally {
                try {
                    client.getConnection().close();
                } catch (Throwable ex) {
                    Logger.getLogger(BranchSelector.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }

        private Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2025"));
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }
    }

}
