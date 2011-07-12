/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * RestSupportPanel.java
 *
 * Created on 26-Oct-2009, 13:14:19
 */

package org.netbeans.modules.websvc.rest.spi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JCheckBox;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class ApplicationConfigPanel extends javax.swing.JPanel {

    /** Creates new form RestSupportPanel */

    public ApplicationConfigPanel(String configType, String resourcesPath, 
            boolean isJerseyLib, boolean annotationConfigAvailable, boolean
            hasServerJerseyLibrary) 
    {
        initComponents();
        if (!annotationConfigAvailable) {
            jRadioButton1.setVisible(false);
        }
        if (WebRestSupport.CONFIG_TYPE_IDE.equals(configType)) {
            if (annotationConfigAvailable) jRadioButton1.setSelected(true);
            else jRadioButton3.setSelected(true);
            if (!isJerseyLib) {
                jCheckBox1.setSelected(false);
            }
        } else if (WebRestSupport.CONFIG_TYPE_USER.equals(configType)) {
            jRadioButton2.setSelected(true);
            jTextField1.setEnabled(false);
            if (!isJerseyLib) {
                jCheckBox1.setSelected(false);
            }
        } else {
            jRadioButton3.setSelected(true);
        }
        jTextField1.setText(resourcesPath);
        jLabel3.setVisible(hasServerJerseyLibrary);
        jComboBox1.setVisible(hasServerJerseyLibrary);
        addListeners();
    }

    public ApplicationConfigPanel(boolean annotationConfigAvailable,
            boolean hasServerJerseyLibrary) 
    {
        initComponents();
        if (!annotationConfigAvailable) {
            jRadioButton1.setVisible(false);
            jRadioButton3.setSelected(true);
        }
        jLabel3.setVisible(hasServerJerseyLibrary);
        jComboBox1.setVisible(hasServerJerseyLibrary);
        addListeners();
    }

    private void addListeners() {
        ItemListener l = new MyItemListener();
        jRadioButton1.addItemListener(l);
        jRadioButton2.addItemListener(l);
        jRadioButton3.addItemListener(l);
        
        ActionListener listener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = jCheckBox1.isSelected();
                jLabel3.setEnabled(enable);
                jComboBox1.setEnabled(enable);
            }
        };
        if ( jComboBox1.isVisible() ){
            jCheckBox1.addActionListener(listener);
        }
    }
    
    private ComboBoxModel getSourceModel(){
        String serverLibrary = NbBundle.getMessage(ApplicationConfigPanel.class, 
                "TXT_UseServerLibrary");                    // NO18N
        String nbLibrary = NbBundle.getMessage(ApplicationConfigPanel.class, 
                "TXT_UseNbLibrary");                        // NO18N
        Object items[] = new Object[]{serverLibrary,nbLibrary };
        return new DefaultComboBoxModel(items);
    }

    private class MyItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (jRadioButton1.isSelected()) {
                jLabel1.setEnabled(true);
                jTextField1.setEnabled(true);
                jCheckBox1.setEnabled(true);
            } else if (jRadioButton2.isSelected()) {
                jLabel1.setEnabled(false);
                jTextField1.setEnabled(false);
                jCheckBox1.setEnabled(true);
            } else if (jRadioButton3.isSelected()){
                jLabel1.setEnabled(true);
                jTextField1.setEnabled(true);
                jCheckBox1.setSelected(true);
                jCheckBox1.setEnabled(false);
            }
        }

    }

    public String getConfigType() {
        if (jRadioButton1.isSelected()) return WebRestSupport.CONFIG_TYPE_IDE;
        else if (jRadioButton2.isSelected()) return WebRestSupport.CONFIG_TYPE_USER;
        else return WebRestSupport.CONFIG_TYPE_DD;
    }

    public boolean isJerseyLibSelected() {
        if ( !jCheckBox1.isSelected() ){
            return false;
        }
        if ( !jComboBox1.isVisible() ){
            return true;
        }
        return jComboBox1.getSelectedIndex() ==1;
    }
    
    public boolean isServerJerseyLibSelected(){
        boolean visible = jComboBox1.isVisible();
        if ( !visible || !jCheckBox1.isSelected() ){
            return false;
        }
        return jComboBox1.getSelectedIndex() ==0;
    }

    public String getApplicationPath() {
        return jTextField1.getText().trim();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jLabel1.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jLabel1.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jTextField1.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        buttonGroup1.add(jRadioButton3);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton3, org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jRadioButton3.text")); // NOI18N
        jRadioButton3.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        buttonGroup1.add(jRadioButton2);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton2, org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jRadioButton2.text")); // NOI18N
        jRadioButton2.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton1, org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jRadioButton1.text")); // NOI18N
        jRadioButton1.setAutoscrolls(true);
        jRadioButton1.setVerifyInputWhenFocusTarget(false);
        jRadioButton1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jCheckBox1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jCheckBox1.text")); // NOI18N

        jComboBox1.setModel(getSourceModel());

        jLabel3.setLabelFor(jComboBox1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "LBL_Source")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jCheckBox1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGap(134, 134, 134)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jRadioButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                        .addComponent(jRadioButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                        .addComponent(jRadioButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(152, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jRadioButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(92, Short.MAX_VALUE)))
        );

        jComboBox1.getAccessibleContext().setAccessibleName(jLabel3.getAccessibleContext().getAccessibleName());
        jComboBox1.getAccessibleContext().setAccessibleDescription(jLabel3.getAccessibleContext().getAccessibleDescription());
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ACSN_Source")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ACSD_Source")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "TTL_ApplicationConfigPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jLabel2.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}
