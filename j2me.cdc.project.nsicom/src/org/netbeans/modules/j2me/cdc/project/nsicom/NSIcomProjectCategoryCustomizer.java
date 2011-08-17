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
 * NSIcomProjectCategoryCustomizer.java
 *
 * Created on January 21, 2007, 6:27 PM
 */

package org.netbeans.modules.j2me.cdc.project.nsicom;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.mobility.activesync.ActiveSyncOps;
import org.netbeans.mobility.activesync.DeviceConnectedListener;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.j2me.cdc.project.nsicom.NSIcomPropertiesDescriptor.*;
import org.openide.util.NbBundle;

/**
 *
 * @author  suchys
 */
public class NSIcomProjectCategoryCustomizer extends JPanel implements CustomizerPanel, VisualPropertyGroup  {
    
    private static String[] PROPERTY_NAMES = new String[] {
        NSIcomPropertiesDescriptor.PROP_MONITOR_HOST, 
        NSIcomPropertiesDescriptor.PROP_VERBOSE, 
        NSIcomPropertiesDescriptor.PROP_RUN_REMOTE, 
        NSIcomPropertiesDescriptor.PROP_REMOTE_VM, 
        NSIcomPropertiesDescriptor.PROP_REMOTE_APP
    };    
    
    private VisualPropertySupport vps;

    private ConnectionListener listener = new ConnectionListener();
    private Color nbErrorForeground;
    
    /** Creates new form NSIcomProjectCategoryCustomizer */
    public NSIcomProjectCategoryCustomizer() {
        initComponents();
        
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        noteLabel.setForeground(nbErrorForeground);
        //todo
        //after first open, the values will updated. I have no other way to do it.
//        if (evaluator.getProperty(PROP_REMOTE_VM) == null){
//            remoteVMLocation.setText("\\Windows\\creme\\bin\\CrEme.exe"); //NOI18N            
//        }
//        if (evaluator.getProperty(PROP_REMOTE_APP) == null){
//            String appName = evaluator.getProperty("application.name"); //NOI18N
//            applicationLocation.setText("\\My Documents\\NetBeans Applications\\" + appName); //NOI18N            
//        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        monitorHost = new javax.swing.JTextField();
        verboseCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        applicationLocationLabel = new javax.swing.JLabel();
        remoteVMLocation = new javax.swing.JTextField();
        applicationLocation = new javax.swing.JTextField();
        browseRemoteVM = new javax.swing.JButton();
        browseRemoteApp = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        noteLabel = new javax.swing.JLabel();
        remoteVMCheckBox = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();

        jLabel1.setLabelFor(monitorHost);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("LBL_Host_ip")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(verboseCheckBox, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("LBL_VM_verbose")); // NOI18N
        verboseCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        verboseCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel2.setLabelFor(remoteVMLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("LBL_RemoteVMLocation")); // NOI18N

        applicationLocationLabel.setLabelFor(applicationLocation);
        org.openide.awt.Mnemonics.setLocalizedText(applicationLocationLabel, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("LBL_RemoteLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseRemoteVM, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("LBL_BrowseVM")); // NOI18N
        browseRemoteVM.setEnabled(false);
        browseRemoteVM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseRemoteVMActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browseRemoteApp, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("LBL_BrowseAppFolder")); // NOI18N
        browseRemoteApp.setEnabled(false);
        browseRemoteApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseRemoteAppActionPerformed(evt);
            }
        });

        noteLabel.setText("  ");

        org.openide.awt.Mnemonics.setLocalizedText(remoteVMCheckBox, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("LBL_RunRemote")); // NOI18N
        remoteVMCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        remoteVMCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage(NSIcomProjectCategoryCustomizer.class, "LBL_UseDefault")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monitorHost, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(noteLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                            .addComponent(jCheckBox1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(remoteVMCheckBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(applicationLocationLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(applicationLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                                    .addComponent(remoteVMLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(browseRemoteVM)
                            .addComponent(browseRemoteApp)))
                    .addComponent(verboseCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remoteVMCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(remoteVMLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseRemoteVM))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applicationLocationLabel)
                    .addComponent(browseRemoteApp)
                    .addComponent(applicationLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monitorHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(9, 9, 9)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verboseCheckBox)
                .addContainerGap(102, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSN_Host_ip")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSD_Host_ip")); // NOI18N
        verboseCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSN_VM_verbose")); // NOI18N
        verboseCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSD_VM_verbose")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSN_RemoteVMLocation")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSD_RemoteVMLocation")); // NOI18N
        applicationLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSN_RemoteLocation")); // NOI18N
        applicationLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSD_RemoteLocation")); // NOI18N
        browseRemoteVM.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSN_BrowseVM")); // NOI18N
        browseRemoteVM.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSD_BrowseVM")); // NOI18N
        browseRemoteApp.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSN_BrowseAppFolder")); // NOI18N
        browseRemoteApp.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSD_BrowseAppFolder")); // NOI18N
        remoteVMCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSN_RunRemote")); // NOI18N
        remoteVMCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ACSD_RunRemote")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseRemoteAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseRemoteAppActionPerformed
        RemoteFilesystemChooser rfc = new RemoteFilesystemChooser(applicationLocation.getText(), true);
        final DialogDescriptor dd = new DialogDescriptor(rfc, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("TITLE_BrowseRemoteFolder"));
        dd.setValid(false);
        rfc.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (NotifyDescriptor.PROP_VALID.equals(evt.getPropertyName())){
                    boolean valid = (Boolean)evt.getNewValue();
                    dd.setValid(valid);
                }
            }
        });
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (NotifyDescriptor.OK_OPTION == dd.getValue()){
            applicationLocation.setText(rfc.getSelectedFile());
        }

    }//GEN-LAST:event_browseRemoteAppActionPerformed

    private void browseRemoteVMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseRemoteVMActionPerformed
        RemoteFilesystemChooser rfc = new RemoteFilesystemChooser(remoteVMLocation.getText(), false);
        final DialogDescriptor dd = new DialogDescriptor(rfc, org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("TITLE_BrowseRemoteVM"));
        dd.setValid(false);
        rfc.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (NotifyDescriptor.PROP_VALID.equals(evt.getPropertyName())){
                    boolean valid = (Boolean)evt.getNewValue();
                    dd.setValid(valid);
                }
            }
        });
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (NotifyDescriptor.OK_OPTION == dd.getValue()){
            remoteVMLocation.setText(rfc.getSelectedFile());
        }
        
    }//GEN-LAST:event_browseRemoteVMActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField applicationLocation;
    private javax.swing.JLabel applicationLocationLabel;
    private javax.swing.JButton browseRemoteApp;
    private javax.swing.JButton browseRemoteVM;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField monitorHost;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JCheckBox remoteVMCheckBox;
    private javax.swing.JTextField remoteVMLocation;
    private javax.swing.JCheckBox verboseCheckBox;
    // End of variables declaration//GEN-END:variables

    public void addNotify(){
        super.addNotify();
        try {
             if (!ActiveSyncOps.getDefault().isAvailable()){
                noteLabel.setText(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ERR_ActiveSyncError"));//NOI18N
                browseRemoteApp.setEnabled(false);
                browseRemoteVM.setEnabled(false);
             } else {
                 if (!ActiveSyncOps.getDefault().isDeviceConnected()){
                    noteLabel.setText(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ERR_ActiveSyncDeviceDisconnected"));//NOI18N
                    browseRemoteApp.setEnabled(false);
                    browseRemoteVM.setEnabled(false);                     
                 } else {
                    browseRemoteApp.setEnabled(true);
                    browseRemoteVM.setEnabled(true);                     
                 }
             }
        } catch (Exception e){
            noteLabel.setText(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ERR_ActiveSyncError"));//NOI18N
            browseRemoteApp.setEnabled(false);
            browseRemoteVM.setEnabled(false);
        }

        try {
            ActiveSyncOps.getDefault().addConnectionListener(listener);
        } catch (Exception ex) {
            noteLabel.setText(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ERR_ActiveSyncError"));//NOI18N
        }
    }
    
    public void removeNotify(){
        try {
            ActiveSyncOps.getDefault().removeConnectionListener(listener);
        } catch (Exception ex) {
            //ignore
        }
        super.removeNotify();
    }
    
    private class ConnectionListener implements DeviceConnectedListener {
        public void onDeviceConnected(boolean connected) {
            browseRemoteApp.setEnabled(connected);
            browseRemoteVM.setEnabled(connected);
            if (connected){
                noteLabel.setText(" ");  //NOI18N
            } else {
                noteLabel.setText(org.openide.util.NbBundle.getBundle(NSIcomProjectCategoryCustomizer.class).getString("ERR_ActiveSyncDeviceDisconnected"));//NOI18N
            }
        }        
    }

    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(jCheckBox1, configuration, this);
    }

    public void initGroupValues(boolean useDefault) {
        vps.register(applicationLocation, NSIcomPropertiesDescriptor.PROP_REMOTE_APP, useDefault);
        vps.register(remoteVMLocation, NSIcomPropertiesDescriptor.PROP_REMOTE_VM, useDefault);
        vps.register(monitorHost, NSIcomPropertiesDescriptor.PROP_MONITOR_HOST, useDefault);
        vps.register(remoteVMCheckBox, NSIcomPropertiesDescriptor.PROP_RUN_REMOTE, useDefault);
        vps.register(verboseCheckBox, NSIcomPropertiesDescriptor.PROP_VERBOSE, useDefault);
        
        remoteVMCheckBox.setEnabled(!useDefault);
        remoteVMLocation.setEnabled(!useDefault);
        monitorHost.setEnabled(!useDefault);
        applicationLocation.setEnabled(!useDefault);
        verboseCheckBox.setEnabled(!useDefault);
        browseRemoteApp.setEnabled(!useDefault);
        browseRemoteVM.setEnabled(!useDefault);                
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }
            
}
