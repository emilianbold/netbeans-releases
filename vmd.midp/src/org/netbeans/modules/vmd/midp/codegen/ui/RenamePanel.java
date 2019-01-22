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
package org.netbeans.modules.vmd.midp.codegen.ui;
import java.awt.Component;
import java.awt.event.ItemEvent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.vmd.api.model.DesignComponent;


/**
 * Rename refactoring parameters panel
 *
 * 
 */
public class RenamePanel extends JPanel implements CustomRefactoringPanel {

    private final transient String oldName;
    private final transient ChangeListener parent;
    private final transient DesignComponent myComponent;
    private boolean initialized = false;
    
    /** Creates new form RenamePanelName */
    public RenamePanel(String oldName, ChangeListener parent, String name, 
            DesignComponent component , boolean useAccessor )
    {
        setName(name);
        this.oldName = oldName;
        this.parent = parent;
        myComponent = component;
        initComponents();
        textCheckBox.setVisible( false );

        getterLabel.setVisible( useAccessor);
        myAccessor.setVisible( useAccessor );
        //textCheckBox.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.rename", Boolean.FALSE)).booleanValue());

        //nameField.setEnabled(editable);
        //parent.setPreviewEnabled(false);
        nameField.requestFocus();
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent event) {
                nameChanged();
            }
            public void insertUpdate(DocumentEvent event) {
                nameChanged();
            }
            public void removeUpdate(DocumentEvent event) {
                nameChanged();
            }
        });
    }
    
    public void initialize() {
        if (initialized)
            return ;
        //put initialization code here
        initialized = true;
    }
    
    @Override
    public void requestFocus() {
        nameField.requestFocus();
    }

        public String getNameValue() {
        return nameField.getText();
    }

    public boolean searchJavadoc() {
        return textCheckBox.isSelected();
    }

    public Component getComponent() {
        return this;
    }

    String getGetter() {
        return InstanceRenameAction.getGetterName( myComponent,
                nameField.getText());
    }

    private void nameChanged(){
        if ( myAccessor.isVisible()  ){
            myAccessor.setText( getGetter() );
        }
        parent.stateChanged(null);
    }

    private String getAccessorText(){
       if ( myAccessor.isVisible()  ){
            return getGetter();
       }
       return "";
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        textCheckBox = new javax.swing.JCheckBox();
        getterLabel = new javax.swing.JLabel();
        myAccessor = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(RenamePanel.class, "LBL_NewName")); // NOI18N
        add(label, new java.awt.GridBagConstraints());
        label.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSN_NewName")); // NOI18N
        label.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSD_NewName")); // NOI18N

        nameField.setText(oldName);
        nameField.selectAll();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(nameField, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/codegen/ui/Bundle"); // NOI18N
        nameField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_nameField")); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(textCheckBox, org.openide.util.NbBundle.getBundle(RenamePanel.class).getString("LBL_RenameComments")); // NOI18N
        textCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                textCheckBoxItemStateChanged(evt);
            }
        });
        textCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(textCheckBox, gridBagConstraints);
        textCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSN_RenameComments")); // NOI18N
        textCheckBox.getAccessibleContext().setAccessibleDescription(textCheckBox.getText());

        org.openide.awt.Mnemonics.setLocalizedText(getterLabel, org.openide.util.NbBundle.getMessage(RenamePanel.class, "LBL_Accessor")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        add(getterLabel, gridBagConstraints);
        getterLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSN_Accessor")); // NOI18N
        getterLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSD_Accessor")); // NOI18N

        myAccessor.setEditable(false);
        myAccessor.setText(getAccessorText());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        add(myAccessor, gridBagConstraints);
        myAccessor.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSN_Accessor")); // NOI18N
        myAccessor.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RenamePanel.class, "ACSD_Accessor")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void textCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_textCheckBoxItemStateChanged
        // used for change default value for searchInComments check-box.                                                  
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
    }//GEN-LAST:event_textCheckBoxItemStateChanged

    private void textCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textCheckBoxActionPerformed
                                                             
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel getterLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel label;
    private javax.swing.JTextField myAccessor;
    private javax.swing.JTextField nameField;
    private javax.swing.JCheckBox textCheckBox;
    // End of variables declaration//GEN-END:variables

}
