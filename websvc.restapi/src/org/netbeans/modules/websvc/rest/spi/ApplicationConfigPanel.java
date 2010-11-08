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

import javax.swing.JCheckBox;

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
        useServerLibrary.setVisible( hasServerJerseyLibrary );
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
        useServerLibrary.setVisible( hasServerJerseyLibrary );
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
                Object source = e.getSource();
                if ( source == jCheckBox1 ){
                    useServerLibrary.setSelected( false);
                }
                else {
                    jCheckBox1.setSelected( false );
                }
            }
        };
        jCheckBox1.addActionListener(listener);
        useServerLibrary.addActionListener(listener);
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
        return jCheckBox1.isSelected();
    }
    
    public boolean isServerJerseyLibSelected(){
        return useServerLibrary.isVisible() && useServerLibrary.isSelected();
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
        useServerLibrary = new javax.swing.JCheckBox();
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

        org.openide.awt.Mnemonics.setLocalizedText(useServerLibrary, org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "TXT_UseServerLibrary")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(useServerLibrary)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                    .add(jCheckBox1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE))
                .addContainerGap())
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jRadioButton3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jRadioButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                        .add(jRadioButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(152, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(useServerLibrary)
                .addContainerGap())
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jRadioButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jRadioButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jRadioButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(88, Short.MAX_VALUE)))
        );

        useServerLibrary.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ACSN_UseServerLibrary")); // NOI18N
        useServerLibrary.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ACSD_UseServerLibrary")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jLabel2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel2)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 361, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "TTL_ApplicationConfigPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ApplicationConfigPanel.class, "ApplicationConfigPanel.jLabel2.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JCheckBox useServerLibrary;
    // End of variables declaration//GEN-END:variables

}
