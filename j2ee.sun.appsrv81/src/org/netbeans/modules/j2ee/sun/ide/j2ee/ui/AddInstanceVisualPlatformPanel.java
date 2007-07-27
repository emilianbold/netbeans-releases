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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

class AddInstanceVisualPlatformPanel extends javax.swing.JPanel  {
    
    private Object type;
    
    AddInstanceVisualPlatformPanel(File defaultLoc) {
        initComponents();
        platformField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            public void insertUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            public void removeUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
        });
        platformField.setText(defaultLoc.getAbsolutePath());
//        if (defaultLoc.canWrite()) {
//            type = AddDomainWizardIterator.DEFAULT;
//            registerDefault.setSelected(true);
//        } else {
//            type = AddDomainWizardIterator.PERSONAL;
//            createPersonal.setSelected(true);
//        }
        instanceSelector.setModel(new ComboBoxModel() {
            public void addListDataListener(ListDataListener listDataListener) {
            }
            public Object getElementAt(int i) {
                return null;
            }
            public Object getSelectedItem() {
                return null;
            }
            public int getSize() {
                return 0;
            }
            public void removeListDataListener(ListDataListener listDataListener) {
            }
            public void setSelectedItem(Object object) {
            }
        });
        instanceSelector.setPrototypeDisplayValue("WWWWWWWWWWWWWWWW");
    }
    
    Object getSelectedType() {
        return type;
    }
    
    String getInstallLocation() {
        return platformField.getText();
    }
        
    String getProfileConstant() {
        return "";
    }
    
    void setDomainsList(Object[] domainsList, boolean react) {
        if (domainsList != null) {
            instanceSelector.setModel(new javax.swing.DefaultComboBoxModel(domainsList));
            if (react && !registerLocal.isSelected() && !registerRemote.isSelected()) {
                if (domainsList.length < 1) {
                    createPersonal.setSelected(true);
                } else {
                    registerDefault.setSelected(true);
                }
            }
        } else {
            instanceSelector.setModel(new javax.swing.DefaultComboBoxModel());
        }
    }
    
    void setProfilesList(Profile[] profiles, boolean react) {
        profileSelector.setModel(new javax.swing.DefaultComboBoxModel(profiles));
    }
    
    String getDomainDir() {
        String retVal = null;
        boolean okay = true;
        String tmp = (String) instanceSelector.getSelectedItem();
        if (null != tmp) {
            int firstParen = tmp.lastIndexOf('(');
            int lastParen = tmp.lastIndexOf(')');
            if (firstParen < 0) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        NbBundle.getMessage(AddInstanceVisualPlatformPanel.class,
                        "ERRMSG_PARSE_DOMAIN_DIR", tmp));
                okay = false;
            }
            if (okay && lastParen < 0) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        NbBundle.getMessage(AddInstanceVisualPlatformPanel.class,
                        "ERRMSG_PARSE_DOMAIN_DIR", tmp));
                okay = false;
            }
            if (okay && lastParen < firstParen) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        NbBundle.getMessage(AddInstanceVisualPlatformPanel.class,
                        "ERRMSG_PARSE_DOMAIN_DIR", tmp));
                okay = false;
            }
            if (okay) {
                retVal = tmp.substring(firstParen+1,lastParen);
            }
        }
        return retVal;
    }
    
    Profile getProfile() {
        return (Profile) profileSelector.getSelectedItem();
    }
    
    public String getName() {
        return NbBundle.getMessage(AddInstanceVisualPlatformPanel.class,
                "StepName_EnterPlatformDirectory");                                // NOI18N
    }
    
    // Event Handling
    //
    final private Set/*<ChangeListener.*/ listenrs = new HashSet/*<Changelisteners.*/();
    
    void addChangeListener(ChangeListener l) {
        synchronized (listenrs) {
            listenrs.add(l);
        }
    }
    
    void removeChangeListener(ChangeListener l ) {
        synchronized (listenrs) {
            listenrs.remove(l);
        }
    }
    
    transient RequestProcessor.Task changeEvent = null;
    
    private void fireChangeEvent() {
        // don't go so fast here, since this can get called a lot from the
        // document listener
        if (changeEvent == null) {
            changeEvent = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Iterator it;
                            synchronized (listenrs) {
                                it = new HashSet(listenrs).iterator();
                            }
                            ChangeEvent ev = new ChangeEvent(this);
                            while (it.hasNext()) {
                                ((ChangeListener)it.next()).stateChanged(ev);
                            }
                        }
                    });
                    
                }
            }, 100);
        } else {
            changeEvent.schedule(100);
        }
    }
    
    void setSelectedType(Object t, java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == evt.SELECTED) {
            type = t;
            fireChangeEvent();
        }
    }
    
    private String browseInstallLocation(){
        String insLocation = null;
        JFileChooser chooser = new PlatformInstChooser();
        String fname = platformField.getText();
        Util.decorateChooser(chooser,fname,
                NbBundle.getMessage(AddInstanceVisualPlatformPanel.class,
                "LBL_Choose_Install")); //NOI18M
        int returnValue = chooser.showDialog(this,
                NbBundle.getMessage(AddInstanceVisualPlatformPanel.class,
                "LBL_Choose_Button"));                                          //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }
    
    static private class PlatformInstChooser extends JFileChooser {
        public void approveSelection() {
            File dir = FileUtil.normalizeFile(getSelectedFile());
            
            if ( ServerLocationManager.isGoodAppServerLocation(dir) ) {
                super.approveSelection();
            } else {
                setCurrentDirectory( dir );
            }
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        instanceTypeButtonGroup = new javax.swing.ButtonGroup();
        description = new javax.swing.JLabel();
        platformFieldLabel = new javax.swing.JLabel();
        platformField = new javax.swing.JTextField();
        openDirectoryCooser = new javax.swing.JButton();
        registerDefault = new javax.swing.JRadioButton();
        instanceSelector = new javax.swing.JComboBox();
        registerLocal = new javax.swing.JRadioButton();
        registerRemote = new javax.swing.JRadioButton();
        createPersonal = new javax.swing.JRadioButton();
        spacingHack = new javax.swing.JLabel();
        instanceSelectorLabel = new javax.swing.JLabel();
        profileSelectorLabel = new javax.swing.JLabel();
        profileSelector = new javax.swing.JComboBox();

        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle"); // NOI18N
        description.setText(bundle.getString("TXT_platformPanelDescription")); // NOI18N
        description.setFocusable(false);
        description.setMaximumSize(null);
        description.setMinimumSize(null);
        description.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(description, gridBagConstraints);

        platformFieldLabel.setLabelFor(platformField);
        org.openide.awt.Mnemonics.setLocalizedText(platformFieldLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "platformFieldLabel")); // NOI18N
        platformFieldLabel.setMaximumSize(null);
        platformFieldLabel.setMinimumSize(null);
        platformFieldLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 6);
        add(platformFieldLabel, gridBagConstraints);
        platformFieldLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "ACSD_InstallRoot")); // NOI18N

        platformField.setMaximumSize(null);
        platformField.setMinimumSize(null);
        platformField.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 6);
        add(platformField, gridBagConstraints);
        platformField.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_platformField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(openDirectoryCooser, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_openDirectoryChooser")); // NOI18N
        openDirectoryCooser.setMaximumSize(null);
        openDirectoryCooser.setMinimumSize(null);
        openDirectoryCooser.setPreferredSize(null);
        openDirectoryCooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDirectoryCooserActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 5, 0);
        add(openDirectoryCooser, gridBagConstraints);
        openDirectoryCooser.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_openDirectoryChooser")); // NOI18N

        instanceTypeButtonGroup.add(registerDefault);
        org.openide.awt.Mnemonics.setLocalizedText(registerDefault, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_registerDeafult")); // NOI18N
        registerDefault.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        registerDefault.setMargin(new java.awt.Insets(0, 0, 0, 0));
        registerDefault.setMaximumSize(null);
        registerDefault.setMinimumSize(null);
        registerDefault.setPreferredSize(null);
        registerDefault.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                registerDefaultItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 1, 5, 1);
        add(registerDefault, gridBagConstraints);
        registerDefault.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_registerDefault")); // NOI18N

        instanceSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        instanceSelector.setMaximumSize(null);
        instanceSelector.setMinimumSize(null);
        instanceSelector.setPreferredSize(null);
        instanceSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instanceSelectorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 5, 0);
        add(instanceSelector, gridBagConstraints);
        instanceSelector.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "Domain_A11Y_DESC")); // NOI18N

        instanceTypeButtonGroup.add(registerLocal);
        org.openide.awt.Mnemonics.setLocalizedText(registerLocal, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_registerLocal")); // NOI18N
        registerLocal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        registerLocal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        registerLocal.setMaximumSize(null);
        registerLocal.setMinimumSize(null);
        registerLocal.setPreferredSize(null);
        registerLocal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                registerLocalItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 0);
        add(registerLocal, gridBagConstraints);
        registerLocal.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_registerLocal")); // NOI18N

        instanceTypeButtonGroup.add(registerRemote);
        org.openide.awt.Mnemonics.setLocalizedText(registerRemote, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_registerRemote")); // NOI18N
        registerRemote.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        registerRemote.setMargin(new java.awt.Insets(0, 0, 0, 0));
        registerRemote.setMaximumSize(null);
        registerRemote.setMinimumSize(null);
        registerRemote.setPreferredSize(null);
        registerRemote.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                registerRemoteItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 0);
        add(registerRemote, gridBagConstraints);
        registerRemote.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_registerRemote")); // NOI18N

        instanceTypeButtonGroup.add(createPersonal);
        org.openide.awt.Mnemonics.setLocalizedText(createPersonal, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_createPersonal")); // NOI18N
        createPersonal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        createPersonal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        createPersonal.setMaximumSize(null);
        createPersonal.setMinimumSize(null);
        createPersonal.setPreferredSize(null);
        createPersonal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createPersonalItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 0);
        add(createPersonal, gridBagConstraints);
        createPersonal.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_createPersonal")); // NOI18N

        spacingHack.setEnabled(false);
        spacingHack.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.weighty = 1.0;
        add(spacingHack, gridBagConstraints);

        instanceSelectorLabel.setLabelFor(instanceSelector);
        org.openide.awt.Mnemonics.setLocalizedText(instanceSelectorLabel, bundle.getString("LBL_instanceSelectorLabel2")); // NOI18N
        instanceSelectorLabel.setMaximumSize(null);
        instanceSelectorLabel.setMinimumSize(null);
        instanceSelectorLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 23, 5, 6);
        add(instanceSelectorLabel, gridBagConstraints);
        instanceSelectorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "Domain_A11Y_DESC")); // NOI18N

        profileSelectorLabel.setLabelFor(profileSelector);
        org.openide.awt.Mnemonics.setLocalizedText(profileSelectorLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_PROFILE")); // NOI18N
        profileSelectorLabel.setMaximumSize(null);
        profileSelectorLabel.setMinimumSize(null);
        profileSelectorLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 23, 5, 6);
        add(profileSelectorLabel, gridBagConstraints);
        profileSelectorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "ACSD_LBL_Profile")); // NOI18N

        profileSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        profileSelector.setMaximumSize(null);
        profileSelector.setMinimumSize(null);
        profileSelector.setPreferredSize(null);
        profileSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileSelectorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 5, 0);
        add(profileSelector, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void profileSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileSelectorActionPerformed
    fireChangeEvent();
}//GEN-LAST:event_profileSelectorActionPerformed
    
    private void instanceSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instanceSelectorActionPerformed
        fireChangeEvent();
    }//GEN-LAST:event_instanceSelectorActionPerformed
    
    private void createPersonalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createPersonalItemStateChanged
        setSelectedType(AddDomainWizardIterator.PERSONAL,evt);
    }//GEN-LAST:event_createPersonalItemStateChanged
    
    private void registerRemoteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_registerRemoteItemStateChanged
        setSelectedType(AddDomainWizardIterator.REMOTE,evt);
    }//GEN-LAST:event_registerRemoteItemStateChanged
    
    private void registerLocalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_registerLocalItemStateChanged
        setSelectedType(AddDomainWizardIterator.LOCAL,evt);
    }//GEN-LAST:event_registerLocalItemStateChanged
    
    private void registerDefaultItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_registerDefaultItemStateChanged
        setSelectedType(AddDomainWizardIterator.DEFAULT,evt);
    }//GEN-LAST:event_registerDefaultItemStateChanged
    
    private void openDirectoryCooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDirectoryCooserActionPerformed
        String val = browseInstallLocation();
        if (null != val && val.length() >=1) {
            platformField.setText(val);
        }
    }//GEN-LAST:event_openDirectoryCooserActionPerformed
    
    ComboBoxModel getDomainsListModel() {
        return instanceSelector.getModel();
    }

    ComboBoxModel getProfilesListModel() {
        return profileSelector.getModel();
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton createPersonal;
    private javax.swing.JLabel description;
    private javax.swing.JComboBox instanceSelector;
    private javax.swing.JLabel instanceSelectorLabel;
    private javax.swing.ButtonGroup instanceTypeButtonGroup;
    private javax.swing.JButton openDirectoryCooser;
    private javax.swing.JTextField platformField;
    private javax.swing.JLabel platformFieldLabel;
    private javax.swing.JComboBox profileSelector;
    private javax.swing.JLabel profileSelectorLabel;
    private javax.swing.JRadioButton registerDefault;
    private javax.swing.JRadioButton registerLocal;
    private javax.swing.JRadioButton registerRemote;
    private javax.swing.JLabel spacingHack;
    // End of variables declaration//GEN-END:variables
    
}
