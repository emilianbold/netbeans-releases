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

/*
 * NewParameterPanel.java
 *
 * Created on August 22, 2005, 11:01 AM
 */

package org.netbeans.modules.uml.propertysupport.customizers;
import java.awt.Color;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

public class NewParameterPanel extends javax.swing.JPanel {
    private ResourceBundle bundle = NbBundle.getBundle(ParameterCustomizerPanel.class);
    private DialogDescriptor dd;
    private DefaultComboBoxModel typeModel = new DefaultComboBoxModel();
    private Object[] paramList;


    /** Creates new NewParameterPanel */
    public NewParameterPanel(DefaultComboBoxModel comboBoxModel, Object[] parameterList) {
        initComponents();
        paramList = parameterList;
        setTypeList(comboBoxModel);
        name.setColumns(25);
        name.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }
            
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }
            
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }
            
        });

        // set message color
        Color c = javax.swing.UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            c = new Color(89,79,191); 
        }
        messageArea.setForeground(c);
        messageArea.setBackground(this.getBackground());
    }
    
     public void setTypeList(DefaultComboBoxModel comboBoxModel) {
        if (comboBoxModel != null) {
            typeModel = comboBoxModel;
            type.setModel(typeModel);
        }
    }
          
    public void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
    }
     
    
    public String getParamType() {
        return (String) type.getSelectedItem();
    }
    
    public String getParamName() {
        return name.getText();
    }
    
    
     private void nameChanged() {
         String paramName = getParamName().trim();
         boolean valid = true;
         String message = "";
         if (paramName == null || paramName.length() == 0) {
             message = NbBundle.getMessage(NewParameterPanel.class,"MSG_EMTPY_NAME");
             valid = false;
         } else {
             // validate the name to make sure it is unique
             ElementData elem = null;
             if (paramList != null ) {
                 for (int i = 0; i < paramList.length; i++) {              
                     elem = (ElementData) paramList[i];
                     if (elem.getName().equals(paramName)) {
                         valid = false;
                         message = NbBundle.getMessage(NewParameterPanel.class,"MSG_DUPLICATE", paramName);
                         break;
                     }                  
                 }
             }
         }
         
         // enable/disable the OK button
         if (dd != null) {
             dd.setValid(valid);
         }
         messageArea.setText(message);
     }
     
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        type = new javax.swing.JComboBox();
        messageArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getBundle(NewParameterPanel.class).getString("LBL_NAME"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 0);
        add(nameLabel, gridBagConstraints);
        nameLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_NAME"));
        nameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NAME"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        add(name, gridBagConstraints);
        name.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_NAME"));
        name.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_NAME"));

        typeLabel.setLabelFor(type);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getBundle(NewParameterPanel.class).getString("LBL_TYPE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        add(typeLabel, gridBagConstraints);
        typeLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_TYPE"));
        typeLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_TYPE"));

        type.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(type, gridBagConstraints);
        type.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TYPE"));
        type.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_TYPE"));

        messageArea.setBackground(new java.awt.Color(212, 208, 200));
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setRows(1);
        messageArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(messageArea, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea messageArea;
    private javax.swing.JTextField name;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox type;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
    
}
