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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

/*
 * CustomizerJVM.java
 *
 * Created on 20.07.2010, 15:25:26
 */

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties.Vendor;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;

/**
 *
 * @author den
 */
class CustomizerJVM extends javax.swing.JPanel {

    private static final long serialVersionUID = 3411155308004602121L;
    
    CustomizerJVM(WLDeploymentManager manager) {
        this.manager = manager;
        initComponents();
        
        initValues();
    }

    private void initValues() {
        String beaHome = manager.getInstanceProperties().getProperty(
                WLPluginProperties.BEA_JAVA_HOME);
        String sunHome = manager.getInstanceProperties().getProperty(
                WLPluginProperties.SUN_JAVA_HOME);
        String vendor = manager.getInstanceProperties().getProperty(
                WLPluginProperties.VENDOR);
        boolean beaFound = false;
        boolean sunFound = false;
        if ( beaHome == null && sunHome == null ){
            Properties runtimeProps = WLPluginProperties.getRuntimeProperties( 
                    manager.getInstanceProperties().getProperty( 
                            WLPluginProperties.DOMAIN_ROOT_ATTR));
            beaHome = runtimeProps.getProperty( WLPluginProperties.BEA_JAVA_HOME);
            sunHome = runtimeProps.getProperty( WLPluginProperties.SUN_JAVA_HOME);
        }
        if ( beaHome != null && beaHome.trim().length()>0 ){
            beaFound = true;
            vendor = WLPluginProperties.Vendor.ORACLE.toString();
        }
        if ( sunHome!= null && sunHome.trim().length() >0 ){
            sunFound = true;
            vendor = WLPluginProperties.Vendor.SUN.toString();
        }
        if ( !beaFound){
            oracleVendor.setEnabled( false );
        }
        if ( !sunFound ){
            sunVendor.setEnabled( false );
        }
        oracleVendor.setSelected( Vendor.ORACLE.toString().equals( vendor) );
        sunVendor.setSelected( Vendor.SUN.toString().equals( vendor) );
        javaHome.setText( oracleVendor.isSelected()? beaHome.trim() : sunHome.trim());
        
        ActionListener listener = new ActionListener() {
            
            @Override
            public void actionPerformed( ActionEvent e ) {
                if ( e.getSource().equals( oracleVendor )){
                    manager.getInstanceProperties().setProperty( WLPluginProperties.VENDOR, 
                            Vendor.ORACLE.toString());
                }
                else {
                    manager.getInstanceProperties().setProperty( WLPluginProperties.VENDOR, 
                            Vendor.SUN.toString());
                }
            }
        };
        oracleVendor.addActionListener( listener );
        sunVendor.addActionListener(listener);
        
        String javaOpts = manager.getInstanceProperties().getProperty(
                WLPluginProperties.JAVA_OPTS);
        if ( javaOpts != null ){
            vmOptions.setText( javaOpts.trim());
        }
        vmOptions.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, WLPluginProperties.JAVA_OPTS, vmOptions));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        vmVendorGroup = new javax.swing.ButtonGroup();
        javaHomeLabel = new javax.swing.JLabel();
        javaHome = new javax.swing.JTextField();
        vmOptionsLabel = new javax.swing.JLabel();
        NoteChangesLabel = new javax.swing.JLabel();
        vmOptions = new javax.swing.JTextField();
        vmOptionsSampleLabel = new javax.swing.JLabel();
        javaVendorPanel = new javax.swing.JPanel();
        oracleVendor = new javax.swing.JRadioButton();
        sunVendor = new javax.swing.JRadioButton();

        javaHomeLabel.setLabelFor(javaHome);
        org.openide.awt.Mnemonics.setLocalizedText(javaHomeLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_JavaHome")); // NOI18N

        javaHome.setEditable(false);

        vmOptionsLabel.setLabelFor(vmOptions);
        org.openide.awt.Mnemonics.setLocalizedText(vmOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(NoteChangesLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "CustomizerJVM.NoteChangesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(vmOptionsSampleLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmOptionsSample")); // NOI18N

        javaVendorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_JvmVendor"))); // NOI18N

        vmVendorGroup.add(oracleVendor);
        org.openide.awt.Mnemonics.setLocalizedText(oracleVendor, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_Oracle")); // NOI18N

        vmVendorGroup.add(sunVendor);
        org.openide.awt.Mnemonics.setLocalizedText(sunVendor, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_Sun")); // NOI18N

        javax.swing.GroupLayout javaVendorPanelLayout = new javax.swing.GroupLayout(javaVendorPanel);
        javaVendorPanel.setLayout(javaVendorPanelLayout);
        javaVendorPanelLayout.setHorizontalGroup(
            javaVendorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javaVendorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(javaVendorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javaVendorPanelLayout.createSequentialGroup()
                        .addComponent(oracleVendor)
                        .addContainerGap(349, Short.MAX_VALUE))
                    .addGroup(javaVendorPanelLayout.createSequentialGroup()
                        .addComponent(sunVendor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(363, 363, 363))))
        );
        javaVendorPanelLayout.setVerticalGroup(
            javaVendorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javaVendorPanelLayout.createSequentialGroup()
                .addComponent(oracleVendor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sunVendor))
        );

        oracleVendor.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_Oracle")); // NOI18N
        oracleVendor.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_Oracle")); // NOI18N
        sunVendor.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_Sun")); // NOI18N
        sunVendor.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_Sun")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(NoteChangesLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaHomeLabel)
                            .addComponent(vmOptionsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(vmOptionsSampleLabel)
                            .addComponent(vmOptions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .addComponent(javaHome, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)))
                    .addComponent(javaVendorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(javaVendorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaHomeLabel)
                    .addComponent(javaHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vmOptionsLabel)
                    .addComponent(vmOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vmOptionsSampleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 127, Short.MAX_VALUE)
                .addComponent(NoteChangesLabel)
                .addContainerGap())
        );

        javaHomeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_JavaHome")); // NOI18N
        javaHome.getAccessibleContext().setAccessibleName(javaHomeLabel.getAccessibleContext().getAccessibleName());
        javaHome.getAccessibleContext().setAccessibleDescription(javaHomeLabel.getAccessibleContext().getAccessibleDescription());
        vmOptionsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptions")); // NOI18N
        vmOptionsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptions")); // NOI18N
        NoteChangesLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_Note")); // NOI18N
        NoteChangesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_Note")); // NOI18N
        vmOptions.getAccessibleContext().setAccessibleName(vmOptionsLabel.getAccessibleContext().getAccessibleName());
        vmOptions.getAccessibleContext().setAccessibleDescription(vmOptionsLabel.getAccessibleContext().getAccessibleDescription());
        vmOptionsSampleLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptionsSample")); // NOI18N
        vmOptionsSampleLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_VmOptionsSample")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel NoteChangesLabel;
    private javax.swing.JTextField javaHome;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JPanel javaVendorPanel;
    private javax.swing.JRadioButton oracleVendor;
    private javax.swing.JRadioButton sunVendor;
    private javax.swing.JTextField vmOptions;
    private javax.swing.JLabel vmOptionsLabel;
    private javax.swing.JLabel vmOptionsSampleLabel;
    private javax.swing.ButtonGroup vmVendorGroup;
    // End of variables declaration//GEN-END:variables
    
    private WLDeploymentManager manager;

}
