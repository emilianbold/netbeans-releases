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

package org.netbeans.modules.web.project.ui.customizer;


import java.io.IOException;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.J2eePlatformUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.MessageUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Exceptions;

public class CustomizerRun extends JPanel implements HelpCtx.Provider {

    private final ProjectCustomizer.Category category;
    private final WebProjectProperties uiProperties;
    private final String oldServerInstanceId;
    
    /** Creates new form CustomizerRun */
    public CustomizerRun(ProjectCustomizer.Category category, WebProjectProperties uiProperties) {
        initComponents();

        this.category = category;
        this.uiProperties = uiProperties;
        
        this.oldServerInstanceId = uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null
                ? J2eePlatformUiSupport.getServerInstanceID(uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem())
                : null;
        
        uiProperties.JAVAC_CLASSPATH_MODEL.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                setMessages();
            }
        });
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_A11YDesc")); //NOI18N
        
        // disable editing context path if deployment descriptor does not exist
        ProjectWebModule wm = (ProjectWebModule) uiProperties.getProject().getLookup().lookup(ProjectWebModule.class);        
        jTextFieldContextPath.setEnabled(wm.getDeploymentDescriptor() != null);             

        jTextFieldJ2EE.setDocument( uiProperties.J2EE_PLATFORM_MODEL );
        jTextFieldJ2EE.setVisible(false);
        jTextFieldContextPath.setDocument( uiProperties.CONTEXT_PATH_MODEL );
        jTextFieldRelativeURL.setDocument( uiProperties.LAUNCH_URL_RELATIVE_MODEL );
        vmOptions.setDocument(uiProperties.RUNMAIN_JVM_MODEL);
        uiProperties.DISPLAY_BROWSER_MODEL.setMnemonic( jCheckBoxDisplayBrowser.getMnemonic() );
        jCheckBoxDisplayBrowser.setModel( uiProperties.DISPLAY_BROWSER_MODEL ); 
        jCheckBoxDeployOnSave.setModel(uiProperties.DEPLOY_ON_SAVE_MODEL);
        jComboBoxServer.setModel( uiProperties.J2EE_SERVER_INSTANCE_MODEL );
        
        Profile j2eeProfile = Profile.fromPropertiesString(jTextFieldJ2EE.getText().trim());
        if (j2eeProfile != null) {
            jTextFieldJ2EE_Display.setText(j2eeProfile.getDisplayName());
        }

        setDeployOnSaveState();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        jLabelJ2EE = new javax.swing.JLabel();
        jTextFieldJ2EE = new javax.swing.JTextField();
        jTextFieldJ2EE_Display = new javax.swing.JTextField();
        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        jCheckBoxDisplayBrowser = new javax.swing.JCheckBox();
        jLabelContextPathDesc = new javax.swing.JLabel();
        jLabelRelativeURL = new javax.swing.JLabel();
        jTextFieldRelativeURL = new javax.swing.JTextField();
        jLabelURLExample = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();
        jCheckBoxDeployOnSave = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        vmOptions = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        dosDescription = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelServer.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_Server_LabelMnemonic").charAt(0));
        jLabelServer.setLabelFor(jComboBoxServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelServer, gridBagConstraints);

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jComboBoxServer, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle"); // NOI18N
        jComboBoxServer.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeRun_Server_A11YDesc")); // NOI18N

        jLabelJ2EE.setLabelFor(jTextFieldJ2EE);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelJ2EE, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_J2EE_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelJ2EE, gridBagConstraints);
        jLabelJ2EE.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSD_CustomizerRun_jLabelJ2EE")); // NOI18N

        jTextFieldJ2EE.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jTextFieldJ2EE, gridBagConstraints);

        jTextFieldJ2EE_Display.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jTextFieldJ2EE_Display, gridBagConstraints);

        jLabelContextPath.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_ContextPath_LabelMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPath, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPath_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelContextPath, gridBagConstraints);

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 0);
        add(jTextFieldContextPath, gridBagConstraints);
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeRun_ContextPath_A11YDesc")); // NOI18N

        jCheckBoxDisplayBrowser.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDisplayBrowser, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DisplayBrowser_JCheckBox")); // NOI18N
        jCheckBoxDisplayBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayBrowserActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jCheckBoxDisplayBrowser, gridBagConstraints);
        jCheckBoxDisplayBrowser.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeRun_DisplayBrowser_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPathDesc, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPathDesc_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jLabelContextPathDesc, gridBagConstraints);

        jLabelRelativeURL.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_RelativeURL_LabelMnemonic").charAt(0));
        jLabelRelativeURL.setLabelFor(jTextFieldRelativeURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRelativeURL, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RelativeURL_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jLabelRelativeURL, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 5, 0);
        add(jTextFieldRelativeURL, gridBagConstraints);
        jTextFieldRelativeURL.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeRun_RelativeURL_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelURLExample, NbBundle.getMessage(CustomizerRun.class, "LBL_RelativeURLExample")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 0);
        add(jLabelURLExample, gridBagConstraints);
        jLabelURLExample.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_RelativeURLExample_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(errorLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDeployOnSave, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DeployOnSave_JCheckBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jCheckBoxDeployOnSave, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jSeparator1, gridBagConstraints);
        jSeparator1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSN_CustomizerRun_NA")); // NOI18N
        jSeparator1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSN_CustomizerRun_NA")); // NOI18N

        jLabel1.setLabelFor(vmOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "Label_JVM_Argument")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSN_CustomizerRun_NA")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(vmOptions, gridBagConstraints);
        vmOptions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSN_CustomizerRun_NA")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "Label_VM_Hint")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSN_CustomizerRun_NA")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dosDescription, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DeployOnSave_Description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(dosDescription, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        setMessages();//GEN-LAST:event_jTextFieldContextPathKeyReleased
    }                                                 

    private void jCheckBoxDisplayBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayBrowserActionPerformed
        boolean editable = jCheckBoxDisplayBrowser.isSelected();//GEN-LAST:event_jCheckBoxDisplayBrowserActionPerformed
        
        jLabelContextPathDesc.setEnabled(editable);
        jLabelRelativeURL.setEnabled(editable);
        jTextFieldRelativeURL.setEditable(editable);
    }                                                       

private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
        setDeployOnSaveState();//GEN-LAST:event_jComboBoxServerActionPerformed
        setMessages();
}                                               
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dosDescription;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox jCheckBoxDeployOnSave;
    private javax.swing.JCheckBox jCheckBoxDisplayBrowser;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelContextPathDesc;
    private javax.swing.JLabel jLabelJ2EE;
    private javax.swing.JLabel jLabelRelativeURL;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JLabel jLabelURLExample;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JTextField jTextFieldJ2EE;
    private javax.swing.JTextField jTextFieldJ2EE_Display;
    private javax.swing.JTextField jTextFieldRelativeURL;
    private javax.swing.JTextField vmOptions;
    // End of variables declaration//GEN-END:variables

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }

    private void setDeployOnSaveState() {
        if (uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            String serverInstanceID = J2eePlatformUiSupport.getServerInstanceID(
                    uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());

            J2eeModule module = uiProperties.getProject().getWebModule().getJ2eeModule();
            ServerInstance instance = Deployment.getDefault().getServerInstance(serverInstanceID);

            try {
                jCheckBoxDeployOnSave.setEnabled(instance.isDeployOnSaveSupported(module));
            } catch (InstanceRemovedException ex) {
                jCheckBoxDeployOnSave.setEnabled(false);
            }
        } else {
            jCheckBoxDeployOnSave.setEnabled(false);
        }
    }

    private void setMessages() {
        MessageUtils.clear(errorLabel);
        category.setValid(true);

        String message = contextPathValidation();
        if (message != null) {
            MessageUtils.setMessage(errorLabel, MessageUtils.MessageType.ERROR,
                    "<html>"+message+"</html>"); // NOI18N
            category.setValid(false);
            return;
        }

        if (uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null &&
            isServerLibraryMessageNeeded(J2eePlatformUiSupport.getServerInstanceID(
                    uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem()), uiProperties))
        {
            MessageUtils.setMessage(errorLabel, MessageUtils.MessageType.WARNING,
                "<html>"+NbBundle.getMessage(CustomizerRun.class, "MSG_CREATING_LIBRARY")+"</html>"); // NOI18N
        }
    }

    private String contextPathValidation() {
        String contextPath = jTextFieldContextPath.getText();
        String message = null;
        if (contextPath.length() > 0) {
            if (!contextPath.startsWith("/")) { // NOI18N
                message = NbBundle.getMessage (CustomizerRun.class, "MSG_INVALID_CP_DOES_NOT_START_WITH_SLASH"); //NOI18N
            } else if (contextPath.indexOf("//") >= 0) { // NOI18N
                message = NbBundle.getMessage (CustomizerRun.class, "MSG_INVALID_CP_CONTAINS_DOUBLE_SLASH"); //NOI18N
            } else if (contextPath.endsWith("/")) { // NOI18N
                message = NbBundle.getMessage (CustomizerRun.class, "MSG_INVALID_CP_ENDS_WITH_SLASH"); //NOI18N
            }
        }
        return message;
    }

    private boolean isServerLibraryMessageNeeded(String serverInstanceId, WebProjectProperties uiProperties) {
        UpdateHelper helper = uiProperties.getProject().getUpdateHelper();

        try {
            if (SharabilityUtility.isLibrarySwitchIntended(serverInstanceId,
                    oldServerInstanceId, ClassPathUiSupport.getList(uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel()), helper)) {

                    AntProjectHelper antHelper = helper.getAntProjectHelper();
                    Library[] libs = SharabilityUtility.getLibraries(antHelper.resolveFile(antHelper.getLibrariesLocation()), serverInstanceId);
                    return libs.length <= 0;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

}
