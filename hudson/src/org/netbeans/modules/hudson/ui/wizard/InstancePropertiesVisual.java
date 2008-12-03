/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.openide.util.NbBundle;

/**
 * Instance properties wizard visual panel
 *
 * @author  Michal Mocnak
 */
public class InstancePropertiesVisual extends javax.swing.JPanel {
    
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    /** Creates new form InstancePropertiesVisual */
    public InstancePropertiesVisual() {
        initComponents();
        
        // Setting of the spinner model
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setMinimum(0);
        autoSyncSpinner.setModel(model);
        
        nameTxt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            
            public void removeUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            
            public void changedUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
        });
        
        urlTxt.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                fireChangeEvent();
            }
        });
        
        autoSyncCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireChangeEvent();
            }
        });
        
        autoSyncSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fireChangeEvent();
            }
        });
        
        // Generate default name
        nameTxt.setText(generateDisplayName());
        
        // Set default sync time
        autoSyncSpinner.setValue(5);
    }
    
    public String getName() {
        return NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Properties");
    }
    
    public String getDisplayName() {
        return nameTxt.getText().trim();
    }
    
    public String getUrl() {
        return urlTxt.getText().trim();
    }
    
    public String getSyncTime() {
        return String.valueOf(autoSyncSpinner.getValue());
    }
    
    public boolean isSync() {
        return autoSyncCheckBox.isSelected();
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public void setChecking(boolean b) {
        urlTxt.setEnabled(!b);
    }
    
    private void fireChangeEvent() {
        ArrayList<ChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<ChangeListener>(listeners);
        }
        
        ChangeEvent event = new ChangeEvent(this);
        
        for (ChangeListener l : tempList) {
            l.stateChanged(event);
        }
    }
    
    private String generateDisplayName() {
        String name;
        int count = 0;
        
        do {
            name = NbBundle.getMessage(InstancePropertiesVisual.class, "TXT_Name");
            if (count != 0)
                name += " (" + String.valueOf(count) + ")";
            
            count++;
        } while (HudsonManagerImpl.getInstance().getInstanceByName(name) != null);
        
        return name;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        urlTxt = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        proxyButton = new javax.swing.JButton();
        autoSyncCheckBox = new javax.swing.JCheckBox();
        autoSyncSpinner = new javax.swing.JSpinner();
        jSeparator2 = new javax.swing.JSeparator();
        autoSyncLabel = new javax.swing.JLabel();

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/hudson/ui/wizard/Bundle").getString("MNE_Name").charAt(0));
        nameLabel.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Name")); // NOI18N

        urlLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/hudson/ui/wizard/Bundle").getString("MNE_Url").charAt(0));
        urlLabel.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Url")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Desc")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel2.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Help")); // NOI18N

        proxyButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/hudson/ui/wizard/Bundle").getString("MNE_Proxy").charAt(0));
        proxyButton.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Proxy")); // NOI18N
        proxyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxyButtonActionPerformed(evt);
            }
        });

        autoSyncCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/hudson/ui/wizard/Bundle").getString("MNE_AutoSync").charAt(0));
        autoSyncCheckBox.setSelected(true);
        autoSyncCheckBox.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_AutoSync")); // NOI18N
        autoSyncCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoSyncCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        autoSyncLabel.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_AutoSyncMinutes")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(urlLabel))
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                            .add(urlTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                            .add(jLabel2)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(autoSyncCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoSyncSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoSyncLabel))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                    .add(proxyButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .add(14, 14, 14)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(autoSyncCheckBox)
                    .add(autoSyncSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(autoSyncLabel))
                .add(14, 14, 14)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(proxyButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void proxyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyButtonActionPerformed
    OptionsDisplayer.getDefault().open("General");
}//GEN-LAST:event_proxyButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSyncCheckBox;
    private javax.swing.JLabel autoSyncLabel;
    private javax.swing.JSpinner autoSyncSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JButton proxyButton;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTxt;
    // End of variables declaration//GEN-END:variables
    
}
