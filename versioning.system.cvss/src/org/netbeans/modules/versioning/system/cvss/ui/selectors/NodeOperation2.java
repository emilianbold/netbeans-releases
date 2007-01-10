/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * NodeOperations without icons and root.
 *
 * @author Petr Kuzel
 * @see issue #63249
 */
public final class NodeOperation2 extends BeanTreeView implements PropertyChangeListener {

    private final ExplorerManager manager = new ExplorerManager();

    private DialogDescriptor dd;
    private boolean hideIcons;
    private NodeAcceptor acceptor;
    private HelpCtx helpCtx;

    /**
     * Creates new selector with default node renderer.
     */
    public NodeOperation2() {
    }

    public Node[] select(String title, String subtitle, String acsd, Node root, String browserAcsn, String browserAcsd, NodeAcceptor acceptor) throws UserCancelException {
        manager.setRootContext(root);
        manager.addPropertyChangeListener(this);

        if (hideIcons) {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setOpenIcon(null);
            renderer.setClosedIcon(null);
            renderer.setLeafIcon(null);
            tree.setCellRenderer(renderer);
        }

        setPopupAllowed (false);
        setDefaultActionAllowed (false);
        setBorder(UIManager.getBorder("Nb.ScrollPane.border")); // NOI18N

        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, subtitle);
        label.setLabelFor(tree);
        label.setToolTipText(browserAcsd);
        getAccessibleContext().setAccessibleDescription(browserAcsd);
        getAccessibleContext().setAccessibleName(browserAcsn);
        ExplorerParent pane = new ExplorerParent(this);
        pane.add(label, BorderLayout.NORTH);
        pane.setBorder(BorderFactory.createEmptyBorder(12,12,0,12));

        dd = new DialogDescriptor(pane, title);
        dd.setModal(true);
        if (helpCtx  != null) {
            dd.setHelpCtx(helpCtx);
        }
        this.acceptor = acceptor;
        testAccept();

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        dialog.setVisible(true);

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            return manager.getSelectedNodes();
        } else {
            throw new UserCancelException();
        }
    }

    public void setIconsVisible(boolean visible) {
        hideIcons = visible == false;
    }

    public void setHelpCtx(HelpCtx help) {
        helpCtx = help;
    }

    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals (ExplorerManager.PROP_SELECTED_NODES)) {
            testAccept();
        }
    }

    private void testAccept() {
        dd.setValid(acceptor.acceptNodes (manager.getSelectedNodes()));
    }

    /**
     * Provides explorer manager for given client.
     * Workarounds ExprorerManager.find behaviour.
     */
    private class ExplorerParent extends JPanel implements ExplorerManager.Provider {
        public ExplorerParent(Component client) {
            super(new BorderLayout(6, 6));
            add(client, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }
}
