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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;

import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties.Vendor;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLJpa2SwitchSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author den
 */
class CustomizerJVM extends javax.swing.JPanel {

    private static final long serialVersionUID = 3411155308004602121L;

    private final WLJpa2SwitchSupport support;
    
    CustomizerJVM(WLDeploymentManager manager) {
        this.manager = manager;
        this.support = new WLJpa2SwitchSupport(manager);
        initComponents();
        
        initValues();
    }

    private void initValues() {
        Object vendor = manager.getInstanceProperties().getProperty(
                WLPluginProperties.VENDOR);
        List<Object> vendors = new LinkedList<Object>();
        Properties runtimeProps = WLPluginProperties
                .getRuntimeProperties(manager.getInstanceProperties()
                        .getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR));
        final String beaHome = runtimeProps.getProperty(WLPluginProperties.
                BEA_JAVA_HOME);
        final String sunHome = runtimeProps.getProperty(WLPluginProperties.
                SUN_JAVA_HOME);
        final Properties javaHomeProps = 
            (Properties)runtimeProps.get(WLPluginProperties.JAVA_HOME);
        if ( beaHome != null && beaHome.trim().length()>0 ){
            vendors.add( WLPluginProperties.Vendor.ORACLE.toString() );
        }
        if ( sunHome!= null && sunHome.trim().length() >0 ){
            vendors.add( WLPluginProperties.Vendor.SUN.toString() );
        }
        if ( vendor == null || vendor.toString().trim().length() == 0 ){
            vendor = DEFAULT_VENDOR;
        }
        vendors.add( DEFAULT_VENDOR );// NOI18N
        
        Enumeration<Object> keys = javaHomeProps.keys();
        while ( keys.hasMoreElements()){
            String key = (String)keys.nextElement();
            if ( key.length() > 0 && !key.equals(Vendor.SUN.toString()) && 
                    !key.equals(Vendor.ORACLE.toString()))
            {
                vendors.add( key );
            }
        }
        
        vendorName.setModel( new DefaultComboBoxModel(vendors.toArray( )));
        vendorName.setSelectedItem(vendor);
        
        if (vendor == DEFAULT_VENDOR) {
            javaHome.setText(javaHomeProps.getProperty(""));
        }
        else if (vendor.equals(Vendor.ORACLE.toString())) {
            javaHome.setText(beaHome);
        }
        else if (vendor.equals(Vendor.SUN.toString())) {
            javaHome.setText(sunHome);
        }
        else {
            javaHome.setText(javaHomeProps.getProperty(vendor.toString()));
        }
        
        vendorName.addItemListener( new ItemListener() {
            
            @Override
            public void itemStateChanged( ItemEvent event ) {
                Object item = event.getItem();
                if ( item == DEFAULT_VENDOR ){
                    javaHome.setText(javaHomeProps.getProperty(""));
                    manager.getInstanceProperties().setProperty(
                            WLPluginProperties.VENDOR, "");
                }
                else {
                    String vendor = event.getItem().toString();
                    if ( vendor.equals( Vendor.ORACLE.toString())){
                        javaHome.setText(beaHome);
                    }
                    else if ( vendor.equals( Vendor.SUN.toString())){
                        javaHome.setText(sunHome);
                    }
                    else {
                        javaHome.setText(javaHomeProps.getProperty(vendor));
                    }
                    manager.getInstanceProperties().setProperty(
                            WLPluginProperties.VENDOR, vendor );
                }
            }
        });
        
        String javaOpts = manager.getInstanceProperties().getProperty(
                WLPluginProperties.JAVA_OPTS);
        if ( javaOpts != null ){
            vmOptions.setText( javaOpts.trim());
        }
        
        String memOpts = manager.getInstanceProperties().getProperty(
                WLPluginProperties.MEM_OPTS);
        if ( memOpts!= null){
            memoryOptions.setText( memOpts.trim());
        }
        
        vmOptions.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, WLPluginProperties.JAVA_OPTS, 
                        vmOptions));
        
        memoryOptions.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, WLPluginProperties.MEM_OPTS, 
                        memoryOptions));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javaHomeLabel = new javax.swing.JLabel();
        javaHome = new javax.swing.JTextField();
        vmOptionsLabel = new javax.swing.JLabel();
        noteChangesLabel = new javax.swing.JLabel();
        vmOptions = new javax.swing.JTextField();
        vmOptionsSampleLabel = new javax.swing.JLabel();
        vendorLabel = new javax.swing.JLabel();
        vendorName = new javax.swing.JComboBox();
        memoryOptions = new javax.swing.JTextField();
        memoryOptionsLabel = new javax.swing.JLabel();
        memoryOptionsCommentLabel = new javax.swing.JLabel();

        javaHomeLabel.setLabelFor(javaHome);
        org.openide.awt.Mnemonics.setLocalizedText(javaHomeLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_JavaHome")); // NOI18N

        javaHome.setEditable(false);

        vmOptionsLabel.setLabelFor(vmOptions);
        org.openide.awt.Mnemonics.setLocalizedText(vmOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(noteChangesLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "CustomizerJVM.noteChangesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(vmOptionsSampleLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmOptionsSample")); // NOI18N

        vendorLabel.setLabelFor(vendorName);
        org.openide.awt.Mnemonics.setLocalizedText(vendorLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_JvmVendor")); // NOI18N

        memoryOptionsLabel.setLabelFor(memoryOptions);
        org.openide.awt.Mnemonics.setLocalizedText(memoryOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmMemoryOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(memoryOptionsCommentLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmMemoryOptionsComment")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noteChangesLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(vendorLabel)
                            .addComponent(vmOptionsLabel)
                            .addComponent(javaHomeLabel)
                            .addComponent(memoryOptionsLabel))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(vendorName, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(javaHome, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                            .addComponent(vmOptions, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                            .addComponent(memoryOptions, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                            .addComponent(vmOptionsSampleLabel)
                            .addComponent(memoryOptionsCommentLabel))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vendorLabel)
                    .addComponent(vendorName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memoryOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memoryOptionsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(memoryOptionsCommentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 164, Short.MAX_VALUE)
                .addComponent(noteChangesLabel)
                .addContainerGap())
        );

        javaHomeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_JavaHome")); // NOI18N
        javaHome.getAccessibleContext().setAccessibleName(javaHomeLabel.getAccessibleContext().getAccessibleName());
        javaHome.getAccessibleContext().setAccessibleDescription(javaHomeLabel.getAccessibleContext().getAccessibleDescription());
        vmOptionsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptions")); // NOI18N
        vmOptionsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptions")); // NOI18N
        noteChangesLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_Note")); // NOI18N
        noteChangesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_Note")); // NOI18N
        vmOptions.getAccessibleContext().setAccessibleName(vmOptionsLabel.getAccessibleContext().getAccessibleName());
        vmOptions.getAccessibleContext().setAccessibleDescription(vmOptionsLabel.getAccessibleContext().getAccessibleDescription());
        vmOptionsSampleLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptionsSample")); // NOI18N
        vmOptionsSampleLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_VmOptionsSample")); // NOI18N
        vendorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_Vendor")); // NOI18N
        vendorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_Vendor")); // NOI18N
        vendorName.getAccessibleContext().setAccessibleName(vendorLabel.getAccessibleContext().getAccessibleName());
        vendorName.getAccessibleContext().setAccessibleDescription(vendorLabel.getAccessibleContext().getAccessibleDescription());
        memoryOptions.getAccessibleContext().setAccessibleName(memoryOptionsLabel.getAccessibleContext().getAccessibleName());
        memoryOptions.getAccessibleContext().setAccessibleDescription(memoryOptionsLabel.getAccessibleContext().getAccessibleDescription());
        memoryOptionsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmMemoryOptions")); // NOI18N
        memoryOptionsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_VmMemoryOptions")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private static final class DefaultVendor{
        private DefaultVendor(){
            
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return NbBundle.getMessage(CustomizerJVM.class, "TXT_VendorDefaultItem");   // NOI18N
        }
        
    }

    private static final DefaultVendor DEFAULT_VENDOR = new DefaultVendor();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField javaHome;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JTextField memoryOptions;
    private javax.swing.JLabel memoryOptionsCommentLabel;
    private javax.swing.JLabel memoryOptionsLabel;
    private javax.swing.JLabel noteChangesLabel;
    private javax.swing.JLabel vendorLabel;
    private javax.swing.JComboBox vendorName;
    private javax.swing.JTextField vmOptions;
    private javax.swing.JLabel vmOptionsLabel;
    private javax.swing.JLabel vmOptionsSampleLabel;
    // End of variables declaration//GEN-END:variables
    
    private WLDeploymentManager manager;

}
