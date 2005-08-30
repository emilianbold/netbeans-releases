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
    }

    public Image getIcon(int type) {
        return (Image) UIManager.get("Nb.Explorer.Folder.icon");  // NOI18N
    }

    public Image getOpenedIcon(int type) {
        return (Image) UIManager.get("Nb.Explorer.Folder.openedIcon"); // NOI18N
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
            AbstractNode waitNode = new AbstractNode(Children.LEAF);
            waitNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2024"));
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
            try {
                List keys = ModuleSelector.listRepositoryPath(clientFactory.createClient(), root, path);
                setKeys(keys);
            } catch (CommandException e) {
                setKeys(Collections.singleton(errorNode(e)));
            } catch (AuthenticationException e) {
                setKeys(Collections.singleton(errorNode(e)));
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
