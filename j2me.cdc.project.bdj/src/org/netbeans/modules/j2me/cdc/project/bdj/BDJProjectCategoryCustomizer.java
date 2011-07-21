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

package org.netbeans.modules.j2me.cdc.project.bdj;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  suchys
 */
public class BDJProjectCategoryCustomizer extends JPanel implements CustomizerPanel, VisualPropertyGroup  {
    
    private static String[] PROPERTY_NAMES = new String[] {
        BDJPropertiesDescriptor.PROP_ORGANIZATION_ID
    };
    
    private VisualPropertySupport vps;
    private String projectDir;
    
    /** Creates new form SavaJeProjectCategoryCustomizer */
    public BDJProjectCategoryCustomizer() {
        initComponents();

        applicationId.setDocument( new NumericDocument(16) );
        organizationId.setDocument( new NumericDocument(32) );
    }

    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(jCheckBox1, configuration, this);
        projectDir = FileUtil.toFile(props.getProjectDirectory()).getAbsolutePath();
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }
    
    public void initGroupValues(boolean useDefault) {
        vps.register(applicationId, BDJPropertiesDescriptor.PROP_APPLICATION_ID, useDefault);
        vps.register(organizationId, BDJPropertiesDescriptor.PROP_ORGANIZATION_ID, useDefault);
        vps.register(fileAccess, BDJPropertiesDescriptor.PROP_FILE_ACCESS, useDefault);
        vps.register(lifecycleControl, BDJPropertiesDescriptor.PROP_LIFECYCLE, useDefault);
        vps.register(serviceSelection, BDJPropertiesDescriptor.PROP_SERVICE_SELECT, useDefault);
        vps.register(readAccess, BDJPropertiesDescriptor.PROP_USER_PREFERENCES_READ, useDefault);
        vps.register(writeAccess, BDJPropertiesDescriptor.PROP_USER_PREFERENCES_WRITE, useDefault);
        vps.register(networkPermissions, BDJPropertiesDescriptor.PROP_NETWORK_PERMISSIONS, useDefault);
        vps.register(deploymentDirField, BDJPropertiesDescriptor.PROP_DEPLOYMENT_DIR, useDefault);
        
        jLabel1.setEnabled(!useDefault);
        jLabel2.setEnabled(!useDefault);
        jLabel3.setEnabled(!useDefault);
        //applicationId.setEnabled(!useDefault);
        //organizationId.setEnabled(!useDefault);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        organizationId = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        applicationId = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        fileAccess = new javax.swing.JCheckBox();
        lifecycleControl = new javax.swing.JCheckBox();
        serviceSelection = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        readAccess = new javax.swing.JCheckBox();
        writeAccess = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        networkPermissions = new javax.swing.JTextField();
        deploymentDirField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        browseDeploymentDir = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        jLabel3.setLabelFor(organizationId);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class,"LBL_OrganizationId")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_UseDefault")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setLabelFor(applicationId);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_ApplicationId")); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "TITLE_Access"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fileAccess, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_FileAccess")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lifecycleControl, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_Lifecycle_Control")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(serviceSelection, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_ServiceSelection")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileAccess)
                    .addComponent(lifecycleControl)
                    .addComponent(serviceSelection))
                .addContainerGap(376, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(fileAccess)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lifecycleControl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serviceSelection)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_UserSettingsTitle"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(readAccess, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_Settings_Read")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(writeAccess, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_Settings_Write")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(readAccess)
                    .addComponent(writeAccess))
                .addContainerGap(408, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(readAccess)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(writeAccess)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "TITLE_NetPermissions"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_NetworkPermissions")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(networkPermissions, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel2)
                .addComponent(networkPermissions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_DeploymentDir")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseDeploymentDir, org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "LBL_BrowseWorkDir")); // NOI18N
        browseDeploymentDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseDeploymentDirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 131, Short.MAX_VALUE)
                        .addComponent(applicationId, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 117, Short.MAX_VALUE)
                        .addComponent(organizationId, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deploymentDirField, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseDeploymentDir)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {applicationId, organizationId});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(applicationId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(organizationId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(deploymentDirField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseDeploymentDir))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        organizationId.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class,"ASCN_CustomizerNokia_AppIcon")); // NOI18N
        organizationId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class,"ASCD_CustomizerNokia_AppIcon")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class,"ASCN_CustomizerNokia")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BDJProjectCategoryCustomizer.class,"ASCD_CustomizerNokia")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void browseDeploymentDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseDeploymentDirActionPerformed
    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);

    String workDir = deploymentDirField.getText();
    if (workDir.trim().length() == 0) workDir = projectDir; 
    chooser.setSelectedFile(new File(workDir));
    chooser.setDialogTitle(NbBundle.getMessage(BDJProjectCategoryCustomizer.class, "TITLE_BrowseDeploymentDir"));
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        deploymentDirField.setText(file.getAbsolutePath());
    }
}//GEN-LAST:event_browseDeploymentDirActionPerformed


   private class NumericDocument extends PlainDocument {

       NumericDocument( int bits ){
           myBits = bits;
           max = 1l<<bits;
       }

        @Override
        public void insertString(int offs, String str, AttributeSet a) 
                throws BadLocationException
        {
            if ( str == null ){
                return;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(getText(0, getLength()));
            builder.insert(offs, str);

            String number ;
            if ( builder.length() >1 && builder.charAt( 0 ) =='0'
                    && builder.charAt( 1 ) =='x')
            {
                // Hex number
                number = builder.substring(2);
                try {
                    if ( number.length()!= 0 ){
                        if ( myBits >15 ){
                            long l = Long.parseLong( number , 16 );
                            if ( l <0 || l >max ){
                                return;
                            }
                        }
                        else {
                            int i = Integer.parseInt( number , 16 );
                            if ( i <0 || i >max ){
                                return;
                            }
                        }
                    }
                }
                catch (NumberFormatException e){
                    return;
                }
            }
            else {
                number = builder.toString();
                try {
                    if ( number.length()!= 0 ){
                        if ( myBits >15 ){
                            long l = Long.parseLong( number );
                            if ( l <0 || l >max ){
                                return;
                            }
                        }
                        else {
                            int i = Integer.parseInt( number );
                            if ( i <0 || i >max ){
                                return;
                            }
                        }
                    }
                }
                catch (NumberFormatException e){
                    return;
                }
            }
            super.insertString(offs, str, a);
        }

        private final int myBits;
        private final long max;
   }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField applicationId;
    private javax.swing.JButton browseDeploymentDir;
    private javax.swing.JTextField deploymentDirField;
    private javax.swing.JCheckBox fileAccess;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox lifecycleControl;
    private javax.swing.JTextField networkPermissions;
    private javax.swing.JTextField organizationId;
    private javax.swing.JCheckBox readAccess;
    private javax.swing.JCheckBox serviceSelection;
    private javax.swing.JCheckBox writeAccess;
    // End of variables declaration//GEN-END:variables

}
