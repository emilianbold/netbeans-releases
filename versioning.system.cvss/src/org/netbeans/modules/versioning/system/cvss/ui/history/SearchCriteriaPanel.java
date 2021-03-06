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

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;

import javax.swing.*;
import java.io.IOException;
import java.io.File;

/**
 * Packages search criteria in Search History panel.
 *
 * @author Maros Sandor
 */
class SearchCriteriaPanel extends javax.swing.JPanel {
    
    private final File[] roots;

    /** Creates new form SearchCriteriaPanel */
    public SearchCriteriaPanel(File [] roots) {
        this.roots = roots;
        initComponents();
    }

    public String getFrom() {
        String s = tfFrom.getText().trim();
        return s.length() > 0 ? s : null;
    }

    public String getTo() {
        String s = tfTo.getText().trim();
        return s.length() > 0 ? s : null;
    }
    
    public String getCommitMessage() {
        String s = tfCommitMessage.getText().trim();
        return s.length() > 0 ? s : null;
    }

    public String getUsername() {
        String s = tfUsername.getText().trim();
        return s.length() > 0 ? s : null;
    }

    public void setFrom(String from) {
        if (from == null) from = "";  // NOI18N
        tfFrom.setText(from);
    }

    public void setTo(String to) {
        if (to == null) to = "";  // NOI18N
        tfTo.setText(to);
    }
    
    public void setCommitMessage(String message) {
        if (message == null) message = ""; // NOI18N
        tfCommitMessage.setText(message);
    }

    public void setUsername(String username) {
        if (username == null) username = ""; // NOI18N
        tfUsername.setText(username);
    }
    
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tfCommitMessage.requestFocusInWindow();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        tfCommitMessage = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tfUsername = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tfFrom = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        bBrowseFrom = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        tfTo = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        bBrowseTo = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 0, 11));
        jLabel1.setLabelFor(tfCommitMessage);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseCommitMessage"));
        jLabel1.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_CommitMessage"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jLabel1, gridBagConstraints);

        tfCommitMessage.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(tfCommitMessage, gridBagConstraints);

        jLabel2.setLabelFor(tfUsername);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseUsername"));
        jLabel2.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_Username"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jLabel2, gridBagConstraints);

        tfUsername.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(tfUsername, gridBagConstraints);

        jLabel3.setLabelFor(tfFrom);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseFrom"));
        jLabel3.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_From"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jLabel3, gridBagConstraints);

        tfFrom.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(tfFrom, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_FromToHint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 4);
        add(jLabel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bBrowseFrom, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_BrowseFrom"));
        bBrowseFrom.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_BrowseFrom"));
        bBrowseFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onFromBrowse(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(bBrowseFrom, gridBagConstraints);

        jLabel4.setLabelFor(tfTo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseTo"));
        jLabel4.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_To"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(jLabel4, gridBagConstraints);

        tfTo.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(tfTo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_FromToHint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 2, 0, 4);
        add(jLabel6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bBrowseTo, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_BrowseTo"));
        bBrowseTo.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_BrowseTo"));
        bBrowseTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onToBrowse(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(bBrowseTo, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void onToBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onToBrowse
        onBrowse(tfTo);
    }//GEN-LAST:event_onToBrowse

    private void onFromBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onFromBrowse
        onBrowse(tfFrom);
    }//GEN-LAST:event_onFromBrowse

    private void onBrowse(JTextField destination) {
        for (int i = 0; i < roots.length; i++) {
            try {
                CVSRoot.parse(Utils.getCVSRootFor(roots[i]));  // raises exception
                BranchSelector selector = new BranchSelector();
                String tag = selector.selectTag(roots[i]);
                if (tag != null) {
                    destination.setText(tag);
                }
                return;
            } catch (IOException e) {
                // no root for this file, try next
            }
        }        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowseFrom;
    private javax.swing.JButton bBrowseTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField tfCommitMessage;
    private javax.swing.JTextField tfFrom;
    private javax.swing.JTextField tfTo;
    private javax.swing.JTextField tfUsername;
    // End of variables declaration//GEN-END:variables
    
}
