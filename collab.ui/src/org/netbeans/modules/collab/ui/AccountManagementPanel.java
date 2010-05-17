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

import org.openide.*;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;

import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.options.AccountNode;
import org.netbeans.modules.collab.ui.options.AccountsNode;

public class AccountManagementPanel extends ExplorerPanel {
    static final long serialVersionUID = 1L;
    private int width_components = 0;
    private int width_leftcomponent = 0;


    /**
     *
     *
     */
    public AccountManagementPanel() {
        initialize();
    }

    public AccountManagementPanel(Account account) {
        initialize();
        setAccount(account);
    }

    private void initialize() {
        update();
        initComponents();

        handleDividerLocation();
        listView1.getAccessibleContext().setAccessibleDescription(
            org.openide.util.NbBundle.getBundle(AccountManagementPanel.class).getString("ACSD_AccountList")
        );
        getAccessibleContext().setAccessibleDescription(
            org.openide.util.NbBundle.getBundle(AccountManagementPanel.class).getString("ACSD_AccountManagementPanel")
        );

        getExplorerManager().addPropertyChangeListener(
            new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent ev) {
                    if (ev.getPropertyName() == ExplorerManager.PROP_SELECTED_NODES) {
                        firePropertyChange(DialogDescriptor.PROP_HELP_CTX, null, null);
                    }

                    firePropertyChange();
                }
            }
        );

        AccountManager.getDefault().addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (ev.getPropertyName().equals(AccountManager.PROP_NEW_ACCOUNT)) {
                        Object newAccount = ev.getNewValue();

                        if (newAccount != null) {
                            setAccount(((Account) newAccount));
                        }
                    }
                }
            }
        );
    }

    private void handleDividerLocation() {
        int listWidth = listView1.getPreferredSize().width;
        int propWidth = propertySheetView1.getPreferredSize().width;
        int splitWidth = jSplitPane1.getPreferredSize().width;
        int location = (int) ((float) listWidth / (listWidth + propWidth) * splitWidth);

        if (location > 0) {
            jSplitPane1.setDividerLocation(location);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("collab_creating_accounts"); //NOI18n
    }

    /** Sets the selected account
    */
    public void setAccount(Account account) {
        update();

        if (account == null) {
            return;
        }

        Account[] accounts = AccountManager.getDefault().getAccounts();

        // Sort the account list
        Arrays.sort(accounts, new AccountManager.AccountComparator());

        Node[] nodes = getExplorerManager().getRootContext().getChildren().getNodes();
        int selected = 0;

        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i] == account) {
                selected = i;

                break;
            }
        }

        try {
            getExplorerManager().setSelectedNodes(new Node[] { nodes[selected] });
        } catch (java.beans.PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        firePropertyChange();
    }

    /** Fires property change.
    */
    private void firePropertyChange() {
        //        firePropertyChange ("account", null, null); // NOI18N
    }

    /** Updates the current state of the explorer manager.
    */
    private void update() {
        Children ch = new Children.Array();
        AccountsNode rootNode = new AccountsNode();

        getExplorerManager().setRootContext(rootNode);
    }

    /**
     *
     *
     */
    public Account[] getSelectedAccounts() {
        List result = new LinkedList();
        Node[] nodes = getExplorerManager().getSelectedNodes();

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof AccountNode) {
                Account account = ((AccountNode) nodes[i]).getAccount();
                result.add(account);
                Debug.out.println(" selected account" + account);
            }
        }

        return (Account[]) result.toArray(new Account[result.size()]);
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        listView1 = new org.openide.explorer.view.ListView();
        propertySheetView1 = new org.openide.explorer.propertysheet.PropertySheetView();
        label = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(0, 2));

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        jSplitPane1.addComponentListener(
            new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    jSplitPane1ComponentResized(evt);
                }
            }
        );

        listView1.setDefaultProcessor(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            }
        );
        listView1.setPopupAllowed(false);
        listView1.setSelectionMode(0);
        listView1.setTraversalAllowed(false);
        listView1.addComponentListener(
            new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    listView1ComponentResized(evt);
                }
            }
        );

        jSplitPane1.setLeftComponent(listView1);

        jSplitPane1.setRightComponent(propertySheetView1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        label.setLabelFor(listView1);
        label.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/Bundle").getString(
                "LBL_AccountManagementPanel_Title"
            )
        );
        add(label, java.awt.BorderLayout.NORTH);
    }

    // </editor-fold>//GEN-END:initComponents
    private void listView1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_listView1ComponentResized
        width_leftcomponent = listView1.getWidth();
    }//GEN-LAST:event_listView1ComponentResized

    private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jSplitPane1ComponentResized

        int width;
        int locator;

        if ((width_components > 0) && (width_leftcomponent > 0)) {
            width = listView1.getWidth() + propertySheetView1.getWidth();
            locator = (width * width_leftcomponent) / width_components;
            jSplitPane1.setDividerLocation(locator);
            width_leftcomponent = locator;
            width_components = width;
        } else {
            width_leftcomponent = listView1.getWidth();
            width_components = width_leftcomponent + propertySheetView1.getWidth();
        }
    }//GEN-LAST:event_jSplitPane1ComponentResized

    private void removeButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonPressed
    }//GEN-LAST:event_removeButtonPressed

    private void addButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonPressed
    }//GEN-LAST:event_addButtonPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel label;
    private org.openide.explorer.view.ListView listView1;
    private org.openide.explorer.propertysheet.PropertySheetView propertySheetView1;
    // End of variables declaration//GEN-END:variables
}
