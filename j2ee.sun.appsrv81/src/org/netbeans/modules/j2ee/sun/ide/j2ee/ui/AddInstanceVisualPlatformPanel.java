/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
                boolean hasWritableDomains = (domainsList.length >= 1);
                registerDefault.setEnabled(hasWritableDomains);
                instanceSelector.setEnabled(hasWritableDomains);
                instanceSelectorLabel.setEnabled(hasWritableDomains);
                if (!hasWritableDomains) {
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
    
    File getDomainDir() {
        String retVal = null;
        boolean okay = true;
        DomainListEntry tmp = (DomainListEntry) instanceSelector.getSelectedItem();
        if (null != tmp) {
            return tmp.getDomainDir();
        }
        return null;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        instanceSelectorLabel = new javax.swing.JLabel();
        profileSelectorLabel = new javax.swing.JLabel();
        profileSelector = new javax.swing.JComboBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle"); // NOI18N
        description.setText(bundle.getString("TXT_platformPanelDescription")); // NOI18N
        description.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        description.setFocusable(false);

        platformFieldLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        platformFieldLabel.setLabelFor(platformField);
        org.openide.awt.Mnemonics.setLocalizedText(platformFieldLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "platformFieldLabel")); // NOI18N

        platformField.setAlignmentX(0.3F);

        org.openide.awt.Mnemonics.setLocalizedText(openDirectoryCooser, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_openDirectoryChooser")); // NOI18N
        openDirectoryCooser.setAlignmentX(0.9F);
        openDirectoryCooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDirectoryCooserActionPerformed(evt);
            }
        });

        instanceTypeButtonGroup.add(registerDefault);
        org.openide.awt.Mnemonics.setLocalizedText(registerDefault, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_registerDeafult")); // NOI18N
        registerDefault.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        registerDefault.setMargin(new java.awt.Insets(0, 0, 0, 0));
        registerDefault.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                registerDefaultItemStateChanged(evt);
            }
        });

        instanceSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        instanceSelector.setAlignmentX(0.3F);
        instanceSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instanceSelectorActionPerformed(evt);
            }
        });

        instanceTypeButtonGroup.add(registerLocal);
        org.openide.awt.Mnemonics.setLocalizedText(registerLocal, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_registerLocal")); // NOI18N
        registerLocal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        registerLocal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        registerLocal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                registerLocalItemStateChanged(evt);
            }
        });

        instanceTypeButtonGroup.add(registerRemote);
        org.openide.awt.Mnemonics.setLocalizedText(registerRemote, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_registerRemote")); // NOI18N
        registerRemote.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        registerRemote.setMargin(new java.awt.Insets(0, 0, 0, 0));
        registerRemote.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                registerRemoteItemStateChanged(evt);
            }
        });

        instanceTypeButtonGroup.add(createPersonal);
        org.openide.awt.Mnemonics.setLocalizedText(createPersonal, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_createPersonal")); // NOI18N
        createPersonal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        createPersonal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        createPersonal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createPersonalItemStateChanged(evt);
            }
        });

        instanceSelectorLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        instanceSelectorLabel.setLabelFor(instanceSelector);
        org.openide.awt.Mnemonics.setLocalizedText(instanceSelectorLabel, bundle.getString("LBL_instanceSelectorLabel2")); // NOI18N

        profileSelectorLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        profileSelectorLabel.setLabelFor(profileSelector);
        org.openide.awt.Mnemonics.setLocalizedText(profileSelectorLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "LBL_PROFILE")); // NOI18N

        profileSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        profileSelector.setAlignmentX(0.3F);
        profileSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileSelectorActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(platformFieldLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(platformField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(openDirectoryCooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(instanceSelectorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(instanceSelector, 0, 305, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(registerLocal, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .add(247, 247, 247))
            .add(layout.createSequentialGroup()
                .add(registerRemote, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .add(247, 247, 247))
            .add(layout.createSequentialGroup()
                .add(createPersonal, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .add(247, 247, 247))
            .add(layout.createSequentialGroup()
                .add(1, 1, 1)
                .add(registerDefault, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .add(246, 246, 246))
            .add(description, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(profileSelectorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(profileSelector, 0, 305, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(description)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(platformFieldLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(platformField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(11, 11, 11)
                        .add(registerDefault)
                        .add(11, 11, 11)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(instanceSelectorLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(instanceSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(11, 11, 11)
                        .add(registerLocal)
                        .add(11, 11, 11)
                        .add(registerRemote)
                        .add(11, 11, 11)
                        .add(createPersonal)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(profileSelectorLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(profileSelector, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(openDirectoryCooser))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {openDirectoryCooser, platformField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        platformFieldLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "ACSD_InstallRoot")); // NOI18N
        platformField.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_platformField")); // NOI18N
        openDirectoryCooser.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_openDirectoryChooser")); // NOI18N
        registerDefault.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_registerDefault")); // NOI18N
        instanceSelector.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "Domain_A11Y_DESC")); // NOI18N
        registerLocal.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_registerLocal")); // NOI18N
        registerRemote.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_registerRemote")); // NOI18N
        createPersonal.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_createPersonal")); // NOI18N
        instanceSelectorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "Domain_A11Y_DESC")); // NOI18N
        profileSelectorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualPlatformPanel.class, "ACSD_LBL_Profile")); // NOI18N
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
    // End of variables declaration//GEN-END:variables
    
}
