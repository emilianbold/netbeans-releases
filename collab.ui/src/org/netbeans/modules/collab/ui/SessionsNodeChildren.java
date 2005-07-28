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
package org.netbeans.modules.collab.ui;

import com.sun.collablet.Account;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.explorer.*;
import org.openide.explorer.view.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class SessionsNodeChildren extends Children.Keys implements NodeListener, PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private Collection keys;
    private CollabExplorerPanel explorerPanel;

    /**
     *
     *
     */
    public SessionsNodeChildren(RootNode rootNode, CollabExplorerPanel explorerPanel) {
        super();
        this.explorerPanel = explorerPanel;

        rootNode.addNodeListener(this);
    }

    /**
     *
     *
     */
    protected void addNotify() {
        refreshCollabManagerListener();
        refreshChildren();
    }

    /**
     *
     *
     */
    protected void removeNotify() {
        _setKeys(Collections.EMPTY_SET);

        if (CollabManager.getDefault() != null) {
            CollabManager.getDefault().removePropertyChangeListener(this);
        }
    }

    /**
     * This method is part of an attempted workaround for bug 5071137:
     * force a refresh of the listener relationship between the node and
     * the collab manager.
     *
     */
    protected void refreshCollabManagerListener() {
        if (CollabManager.getDefault() != null) {
            CollabManager.getDefault().removePropertyChangeListener(this);
            CollabManager.getDefault().addPropertyChangeListener(this);
        } else {
            Debug.debugNotify(new Exception("CollabManager was null; " + "node cannot listen for sessions"));
        }
    }

    /**
     *
     *
     */
    protected Node[] createNodes(Object key) {
        Node[] result = null;

        try {
            if (key instanceof Node) {
                result = new Node[] { (Node) key };
            } else {
                result = new Node[] { new SessionNode((CollabSession) key) };
            }
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
        List keys = new ArrayList();

        try {
            //			Set datasourceNames=new TreeSet(Arrays.asList(
            //				getJatoWebContextCookie().getJDBCDatasourceNames()));
            //			for (Iterator i=datasourceNames.iterator(); i.hasNext(); )
            //			{
            //				nodes.add(getJatoWebContextCookie().getJDBCDatasource(
            //					(String)i.next()));
            //			}
            // TODO: Sort contacts
            CollabSession[] sessions = (CollabManager.getDefault() != null) ? CollabManager.getDefault().getSessions()
                                                                            : new CollabSession[0];

            if (sessions.length == 0) {
                keys.add(
                    new MessageNode(
                        NbBundle.getMessage(SessionsNodeChildren.class, "LBL_SessionsNodeChildren_NoSessions")
                    )
                );
            } else {
                Account defaultAccount = CollabManager.getDefault().getUserInterface().getDefaultAccount();

                Arrays.sort(sessions, new SessionsComparator(defaultAccount));
                keys.addAll(Arrays.asList(sessions));
            }

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
        if (event.getSource() instanceof CollabManager) {
            if (CollabManager.PROP_SESSIONS.equals(event.getPropertyName())) {
                refreshChildren();
            }
        }
    }

    /**
     *
     *
     */
    public void childrenAdded(NodeMemberEvent event) {
        final Node[] nodes = event.getDelta();
        final JTree tree = explorerPanel.getTreeViewJTree();
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        // For each added session node...
                        for (int i = 0; i < nodes.length; i++) {
                            // Expand all its children
                            for (int j = 0; j < tree.getRowCount(); j++) {
                                TreePath path = tree.getPathForRow(j);

                                if (Visualizer.findNode(path.getPath()[1]) == nodes[i]) {
                                    tree.expandPath(path);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Debug.debugNotify(e);
                    }
                }

                private void dumpPath(TreePath path) {
                    for (int i = 0; i < path.getPathCount(); i++)
                        System.out.print(path.getPathComponent(i) + " / ");

                    System.out.println();
                }
            }
        );
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

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected static class SessionsComparator extends Object implements Comparator {
        private Account defaultAccount;

        /**
         *
         *
         */
        public SessionsComparator(Account defaultAccount) {
            super();
            this.defaultAccount = defaultAccount;
        }

        /**
         *
         *
         */
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }

            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return 1;
            }

            Account a1 = ((CollabSession) o1).getAccount();
            Account a2 = ((CollabSession) o2).getAccount();

            // Unilaterally sort the default account first
            if (a1 == defaultAccount) {
                return -1;
            }

            if (a2 == defaultAccount) {
                return 1;
            }

            String s1 = a1.getDisplayName();

            if (s1 == null) {
                s1 = "";
            }

            String s2 = a2.getDisplayName();

            if (s2 == null) {
                s2 = "";
            }

            return s1.compareTo(s2);
        }
    }
}
