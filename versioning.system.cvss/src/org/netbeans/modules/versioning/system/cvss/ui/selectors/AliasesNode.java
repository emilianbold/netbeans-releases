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
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.checkout.ModuleListInformation;

import java.util.Collections;
import java.util.List;

/**
 * Represents module aliases subtree.
 *
 * @author Petr Kuzel
 */
final class AliasesNode extends AbstractNode {

    public static AliasesNode create(Client.Factory client, CVSRoot root) {
        AliasesNode node = new AliasesNode(new AliasesChildren(client, root));
        node.setDisplayName("Aliases");
        return node;
    }

    private AliasesNode(Children children) {
        super(children);
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
            AbstractNode waitNode = new AbstractNode(Children.LEAF);
            waitNode.setDisplayName("Loading...");
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
            errorNode.setDisplayName("Error");
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
        }

    }
}
