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
package org.netbeans.modules.collab.ui;

import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.TreePath;

import org.openide.explorer.view.Visualizer;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;
import org.openide.util.RequestProcessor;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class SessionsNodeChildren extends Children.Keys implements /*NodeListener,*/ PropertyChangeListener {
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
            expandChildren();
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

    private void expandChildren() {
        Runnable r = new Runnable() {
            Node[] nodes;
            public void run() {
                if (nodes == null) {
                    nodes = getNodes(true);
                    SwingUtilities.invokeLater(this);
                } else {
                    JTree tree = explorerPanel.getTreeViewJTree();
                    for (int i = 0; i < nodes.length; i++) {
                        // Expand all its children
                        for (int j = 0; j < tree.getRowCount(); j++) {
                            TreePath path = tree.getPathForRow(j);
                            if (Visualizer.findNode(path.getPath()[1]) == nodes[i]) {
                                tree.expandPath(path);
                            }
                        }
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
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
