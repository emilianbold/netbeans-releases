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

package org.netbeans.modules.mobility.project.deployment;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.openide.DialogDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Adam Sotona
 */
public class NewInstanceDialog extends JPanel implements DocumentListener, ActionListener {
    
    private final MobilityDeploymentProperties props;
    private DialogDescriptor dd;
    private Collection<String> invalidNames = Collections.EMPTY_SET;
    
    /** Creates new form NewInstanceDialog */
    public NewInstanceDialog(MobilityDeploymentProperties props, DeploymentPlugin selected) {
        this.props = props;
        initComponents();
        final ListCellRenderer r = jComboBoxType.getRenderer();
        jComboBoxType.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return r.getListCellRendererComponent(list, value instanceof DeploymentPlugin ? ((DeploymentPlugin)value).getDeploymentMethodDisplayName() : value, index, isSelected, cellHasFocus);
            }
        });
        Vector<DeploymentPlugin> v = new Vector();
        for (DeploymentPlugin d : Lookup.getDefault().lookupAll(DeploymentPlugin.class)) {
            if (d.getGlobalPropertyDefaultValues().size() > 0) v.add(d);
        }
        jComboBoxType.setModel(new DefaultComboBoxModel(v));
        if (selected != null) jComboBoxType.setSelectedItem(selected);
        jComboBoxType.addActionListener(this);
        jTextFieldName.getDocument().addDocumentListener(this);
    }
    
    public void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
        actionPerformed(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelType = new javax.swing.JLabel();
        jComboBoxType = new javax.swing.JComboBox();
        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelError = new javax.swing.JLabel();

        jLabelType.setLabelFor(jComboBoxType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelType, NbBundle.getMessage(NewInstanceDialog.class, "NewInstanceDialog.jLabelType.text")); // NOI18N

        jLabelName.setLabelFor(jTextFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelName, NbBundle.getMessage(NewInstanceDialog.class, "NewInstanceDialog.jLabelName.text")); // NOI18N

        jLabelError.setForeground(new java.awt.Color(89, 79, 191));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelError, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabelType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                            .addComponent(jComboBoxType, 0, 256, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelType))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelName)
                    .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelError)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jLabelType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_NAME_jLabelType")); // NOI18N
        jLabelType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_DESCRIPTION_jLabelType")); // NOI18N
        jComboBoxType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_NAME_jComboBoxType")); // NOI18N
        jComboBoxType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_DESCRIPTION_jComboBoxType")); // NOI18N
        jLabelName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_NAME_jLabelName")); // NOI18N
        jLabelName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_DESCRIPTION_jLabelName")); // NOI18N
        jTextFieldName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_NAME_jTextFieldName")); // NOI18N
        jTextFieldName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewInstanceDialog.class, "ACCESSIBLE_DESCRIPTION_jLabelName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    public void actionPerformed(ActionEvent e) {
        DeploymentPlugin dp = getDeploymentPlugin();
        invalidNames = dp == null ? Collections.EMPTY_SET : props.getInstanceList(dp.getDeploymentMethodName());
        changedUpdate(null);
    }

    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        String name = getInstanceName();
        if (invalidNames.contains(name)) {
            jLabelError.setText(NbBundle.getMessage(NewInstanceDialog.class, "ERR_InstanceExists")); // NOI18N
            jLabelError.setVisible(true);
            if (dd != null) dd.setValid(false);
        } else if (!Utilities.isJavaIdentifier(name)) {
            jLabelError.setText(NbBundle.getMessage(NewInstanceDialog.class, "ERR_InvalidName")); // NOI18N
            jLabelError.setVisible(true);
            if (dd != null) dd.setValid(false);
        } else {
            jLabelError.setVisible(false);
            if (dd != null) dd.setValid(true);
        }
    }

    public DeploymentPlugin getDeploymentPlugin() {
        return (DeploymentPlugin)jComboBoxType.getSelectedItem();
    }
    
    public String getInstanceName() {
        return jTextFieldName.getText();
    }

    public void addNotify() {
        super.addNotify();
        jTextFieldName.requestFocusInWindow();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxType;
    private javax.swing.JLabel jLabelError;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelType;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
}
