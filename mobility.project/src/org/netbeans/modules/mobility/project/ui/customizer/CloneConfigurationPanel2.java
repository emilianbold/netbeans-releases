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

/*
 * NewConfigurationPanel.java
 *
 * Created on February 11, 2004, 2:44 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.util.Collection;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;

/**
 * CloneConfigurationPanel2*/
public class CloneConfigurationPanel2 extends JPanel implements DocumentListener {
    
    private DialogDescriptor dialogDescriptor;
    final private Collection<String> allNames;
    private Collection<String> namesToClone;
    
    /** Creates CloneConfigurationPanel2 */
    public CloneConfigurationPanel2(Collection<String> allNames, Collection<String> namesToClone) {
        this.allNames = allNames;
        this.namesToClone = namesToClone;
        initComponents();
        initAccessibility();
    }
    
    public String getPrefix() {
        return jTextFieldName.getText();
    }
    
    public String getSuffix() {
        return jTextField1.getText();
    }
    
    public void setDialogDescriptor(final DialogDescriptor dd) {
        assert dialogDescriptor == null : "Set the dialog descriptor only once!"; //NOI18N
        dialogDescriptor = dd;
        dd.setHelpCtx(new HelpCtx(NewConfigurationPanel.class));
        jTextFieldName.getDocument().addDocumentListener(this);
        jTextField1.getDocument().addDocumentListener(this);
        changedUpdate(null);
    }
    
    public boolean isStateValid() {
        for (String name : namesToClone) {
            name = jTextFieldName.getText() + name + jTextField1.getText();
            if (J2MEProjectUtils.ILEGAL_CONFIGURATION_NAMES.contains(name)) {
                errorPanel.setErrorBundleMessage("ERR_AddCfg_ReservedWord"); //NOI18N
                return false;
            }
            if (!Utilities.isJavaIdentifier(name)) {
                errorPanel.setErrorBundleMessage("ERR_AddCfg_MustBeJavaIdentifier"); //NOI18N
                return false;
            }
            if (allNames.contains(name)) {
                errorPanel.setErrorBundleMessage("ERR_AddCfg_NameExists"); //NOI18N
                return false;
            }
        }
        errorPanel.setErrorBundleMessage(null);
        return true;
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
	final DocumentEvent e) {
        if (dialogDescriptor != null) {
            dialogDescriptor.setValid(isStateValid());
        }
    }
    
    public void insertUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void removeUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setPreferredSize(new java.awt.Dimension(400, 100));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(jTextFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(CloneConfigurationPanel2.class, "LBL_NewConfigPanel_ConfigurationPrefix")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(jTextFieldName, gridBagConstraints);
        jTextFieldName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CloneConfigurationPanel2.class, "ACSD_CloneCfg_CfgName")); // NOI18N

        jLabel2.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(CloneConfigurationPanel2.class, "LBL_NewConfigPanel_ConfigurationSuffix")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(jTextField1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConfigurationPanel.class, "ACSN_CloneConfigPanel"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConfigurationPanel.class, "ACSD_CloneConfigPanel"));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
}
