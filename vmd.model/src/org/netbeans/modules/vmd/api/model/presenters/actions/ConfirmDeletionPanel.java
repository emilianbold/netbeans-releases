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
package org.netbeans.modules.vmd.api.model.presenters.actions;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * 
 */
class ConfirmDeletionPanel extends javax.swing.JPanel {

    public ConfirmDeletionPanel() {
        initComponents();

        initAccessibility();
    }

    public static boolean show (Collection<DesignComponent> selectedComponents, Collection<DesignComponent> gatheredComponents) {
        ConfirmDeletionPanel panel = new ConfirmDeletionPanel ();
        ArrayList<String> names;
        DefaultListModel model;

        names = new ArrayList<String> ();
        for (DesignComponent component : selectedComponents) {
            String name = InfoPresenter.getHtmlDisplayName (component);
            if (name != null)
                names.add (name);
        }
        Collections.sort (names);
        model = new DefaultListModel ();
        for (String name : names)
            model.addElement ("<html>" + name); // NOI18N
        panel.selected.setModel (model);

        names = new ArrayList<String> ();
        for (DesignComponent component : gatheredComponents) {
            if (selectedComponents.contains (component))
                continue;
            if (DeleteSupport.isSilent (component))
                continue;
            String name = InfoPresenter.getHtmlDisplayName (component);
            if (name != null)
                names.add (name);
        }
        Collections.sort (names);
        model = new DefaultListModel ();
        for (String name : names)
            model.addElement ("<html>" + name); // NOI18N
        panel.dependent.setModel (model);

        panel.jLabel3.setVisible (model.size () > 0);
        panel.jScrollPane2.setVisible (model.size () > 0);

        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation (panel, NotifyDescriptor.YES_NO_OPTION);
        descriptor.setTitle (NbBundle.getMessage (ConfirmDeletionPanel.class, "TITLE_DeleteDialog")); // NOI18N

        DialogDisplayer.getDefault ().notify (descriptor);
        return NotifyDescriptor.YES_OPTION.equals (descriptor.getValue ());
    }

    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage (
                ConfirmDeletionPanel.class, "ACSN_DeleteDialog")); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (
                ConfirmDeletionPanel.class, "ACSD_DeleteDialog")); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        selected = new javax.swing.JList();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dependent = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "ConfirmDeletionPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "jLabel1.AccessibleContext.accessibleName")); // NOI18N

        jScrollPane1.setViewportView(selected);
        selected.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "ACSN_Selected")); // NOI18N
        selected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "ACSD_Selected")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jLabel2.setLabelFor(selected);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "ConfirmDeletionPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        jLabel3.setLabelFor(dependent);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "ConfirmDeletionPanel.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel3, gridBagConstraints);

        jScrollPane2.setViewportView(dependent);
        dependent.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "ACSN_Dependent")); // NOI18N
        dependent.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfirmDeletionPanel.class, "ACSD_Dependent")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList dependent;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList selected;
    // End of variables declaration//GEN-END:variables
    private static boolean isSilent (DesignComponent component) {
        for (DeletePresenter presenter : component.getPresenters (DeletePresenter.class))
            if (presenter.isSilent ())
                return true;
        return false;
    }
}
