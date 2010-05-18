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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.xhtml;

//XXX <POST_MIGRATION>
// This file does not belong here. Should be part of Property Editors
// Revisit post 5.x migration - Winston

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.util.Collection;
import java.util.Iterator;
import javax.swing.event.*;
import org.netbeans.api.project.Project;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/**
 * Panel used for entering the name of a web form to be created
 */
public class FormNamePanel extends javax.swing.JPanel implements DocumentListener {

    private DialogDescriptor descriptor;

    private Project project;
    private String basename;

    public FormNamePanel(Project project2, String basename) {
        this.project = project2;
        this.basename = basename;
        initComponents();
        // Pick Default Name
        String name = getDefaultFormName();
        nameField.setText(name);
        nameField.selectAll();

        nameField.getDocument().addDocumentListener(this);
    }

    String getDefaultFormName() {
        int num = 0;
        while (true) {
            num++;
            String name = basename + Integer.toString(num);
            if (!isUsedName(name)) {
                return name;
            }
        }
    }

    private boolean isUsedName(String name) {
        FileObject wfolder = null;
        FileObject bfolder = null;

        assert project != null;
        wfolder = JsfProjectUtils.getDocumentRoot(project);
        bfolder = JsfProjectUtils.getPageBeanRoot(project);

        // Does the file exist as a jsp?
        if (wfolder.getFileObject(name, "jsp") != null) {
            return true;
        }
        if (wfolder.getFileObject(name, "jspf") != null) {
            return true;
        }
        if (bfolder.getFileObject(name, "java") != null) {
            return true;
        }
        return false;
    }

    private void validateName() {
        String name = nameField.getText().trim();

        boolean validName;
        validName = JsfProjectUtils.isValidJavaFileName(name);
        if (!validName) {
            String errorMsg = NbBundle.getMessage(FormNamePanel.class,
                    "NotValidName"); // NOI18N
            errorLabel.setText(errorMsg);
            if ((descriptor != null) && descriptor.isValid()) {
                descriptor.setValid(false);
            }
        } else if (isUsedName(name)) {
            String errorMsg = NbBundle.getMessage(FormNamePanel.class,
                    "UsedName"); // NOI18N
            errorLabel.setText(errorMsg);
            if ((descriptor != null) && descriptor.isValid()) {
                descriptor.setValid(false);
            }
        } else {
            errorLabel.setText("");
            if ((descriptor != null) && !descriptor.isValid()) {
                descriptor.setValid(true);
            }
        }
    }

   // Implements DocumentListener

    public void changedUpdate(DocumentEvent e) {
        validateName();
    }

    public void insertUpdate(DocumentEvent e) {
        validateName();
    }

    public void removeUpdate(DocumentEvent e) {
        validateName();
    }

    public String getFragmentName() {
        return nameField.getText().trim();
    }

    public void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 200));
        setLayout(new java.awt.GridBagLayout());

        nameLabel.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(FormNamePanel.class, "NewFormLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 0);
        add(nameLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/xhtml/Bundle"); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleName(bundle.getString("NewFormLabelAccessibleName")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("NewFormLabelAccessibleDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 11);
        add(nameField, gridBagConstraints);
        nameField.getAccessibleContext().setAccessibleName(bundle.getString("NewFormTextFieldAccessibleName")); // NOI18N
        nameField.getAccessibleContext().setAccessibleDescription(bundle.getString("NewFormTextFieldAccessibleDesc")); // NOI18N

        errorLabel.setForeground(java.awt.Color.red);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(errorLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    // End of variables declaration//GEN-END:variables
    
}
