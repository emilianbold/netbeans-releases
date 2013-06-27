/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.ui;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.glassfish.common.EnableComet;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.utils.JavaUtils;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Williams
 */
public class InstanceCustomizer extends javax.swing.JPanel {

    // This is copy-paste from Glassfish cloud module. It should be moved
    // to some common place later.
    // Spource: org.netbeans.modules.glassfish.cloud.wizards.GlassFishWizardComponent
    /**
     * Event listener to validate component field on the fly.
     */
    abstract class ComponentFieldListener implements DocumentListener {
        
        ////////////////////////////////////////////////////////////////////////
        // Abstract methods                                                   //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Process received notification from all notification types.
         */
        abstract void processEvent();

        ////////////////////////////////////////////////////////////////////////
        // Implemented Interface Methods                                      //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Gives notification that there was an insert into component field.
         * <p/>
         * @param event Change event object.
         */
        @Override
        public void insertUpdate(DocumentEvent e) {
            processEvent();
        }

        /**
         * Gives notification that a portion of component field has been removed.
         * <p/>
         * @param event Change event object.
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            processEvent();
        }

        /**
         * Gives notification that an attribute or set of attributes changed.
         * <p/>
         * @param event Change event object.
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
            processEvent();
        }

    }

    /**
     * Action to invoke Java SE platforms customizer.
     */
    private class ButtonPwAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            echoPw = !echoPw;
            passwordField.setEchoChar(echoPw ? '\0' : '*');
            buttonPw.setText(buttonPwLabel());
        }
        
    }

    private boolean cometEnabledChanged = false;
    private boolean monitorEnabledChanged = false;
    private boolean jdbcDriverDeployEnabledChanged = false;
    private boolean sessionEnabledChanged = false;
    private boolean startDerbyChanged = false;

    /** Show password button label. */
    private final String buttonPwShow;

    /** Hide password button label. */
    private final String buttonPwHide;

    /** Echo password text. */
    boolean echoPw;

    /** Password show/hide button action. */
    private final ButtonPwAction buttonPwAction;

    /** GlassFish server instance to be modified. */
    private final GlassfishInstance instance;
    
    public InstanceCustomizer(final GlassfishInstance instance) {
        this.instance = instance;
        this.buttonPwShow = NbBundle.getMessage(
                GlassFishPassword.class, "InstanceCustomizer.buttonPwShow");
        this.buttonPwHide = NbBundle.getMessage(
                GlassFishPassword.class, "InstanceCustomizer.buttonPwHide");
        this.echoPw = false;
        this.buttonPwAction = new ButtonPwAction();
        initComponents();
    }

    /**
     * Get password button text based on echo text trigger.
     * <p/>
     * @return Show label when password text is hidden and hide label when
     *         password text is echoed.
     */
    private String buttonPwLabel() {
        return echoPw ? buttonPwHide : buttonPwShow;
    }

    /**
     * Get target.
     * <p/>
     * @return User name.
     */
    public String getTarget() {
        String text = targetValueField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get user name.
     * <p/>
     * @return User name.
     */
    public String getUserName() {
        String text = userNameField.getText();
        return text != null ? text.trim() : null;
    }
    
    /**
     * Get user password.
     * <p/>
     * Password processing should not remove leading and trailing spaces.
     * <p/>
     * @return User password.
     */
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    /**
     * Create event listener to update target field on the fly.
     * This change will cause server URI to be updated too.
     * <p/>
     * This method is far from being finished because URI change will require
     * few more things to be done. It's not registered in targetValueField now.
     */
    private DocumentListener initTargetUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                instance.putProperty(GlassfishModule.TARGET_ATTR, getTarget());
            }
        };
    }

    /**
     * Create event listener to update user name field on the fly.
     */
    private DocumentListener initUserNameUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                instance.putProperty(
                        GlassfishModule.USERNAME_ATTR, getUserName());
            }
        };
    }

    /**
     * Create event listener to update user password field on the fly.
     */
    private DocumentListener initPasswordUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                 instance.putProperty(
                         GlassfishModule.PASSWORD_ATTR, getPassword());
            }
        };
    }

    private void initFields() {
        Map<String, String> ip = instance.getProperties();
        String host = ip.get(GlassfishModule.HTTPHOST_ATTR);
        if (null == host) {
            host = ip.get(GlassfishModule.HOSTNAME_ATTR);
        }
        textLocation.setText(host + ":" +
                ip.get(GlassfishModule.HTTPPORT_ATTR));
        textDomainsFolder.setText(ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR)); // NOI18N
        textDomainName.setText(ip.get(GlassfishModule.DOMAIN_NAME_ATTR)); // NOI18N
        targetValueField.setText(ip.get(GlassfishModule.TARGET_ATTR));
        userNameField.setText(ip.get(GlassfishModule.USERNAME_ATTR));
        userNameField.getDocument()
                .addDocumentListener(initUserNameUpdateListener());
        passwordField.setText(ip.get(GlassfishModule.PASSWORD_ATTR));
        passwordField.getDocument()
                .addDocumentListener(initPasswordUpdateListener());
        
//        boolean cometEnabled = Boolean.parseBoolean(ip.get(GlassfishModule.COMET_FLAG));
        String cometFlag = ip.get(GlassfishModule.COMET_FLAG);
        if(cometFlag == null) {
            cometFlag = System.getProperty(GlassfishModule.COMET_FLAG);
        }
        boolean cometEnabled = Boolean.parseBoolean(cometFlag);
        cometCheckBox.setSelected(cometEnabled);
        String monitorFlag = ip.get(GlassfishModule.HTTP_MONITOR_FLAG);
        boolean monitorEnabled = Boolean.parseBoolean(monitorFlag);
        monitorCheckBox.setSelected(monitorEnabled);
        String driverDeployFlag = ip.get(GlassfishModule.DRIVER_DEPLOY_FLAG);
        boolean driverDeployEnabled = Boolean.parseBoolean(driverDeployFlag);
        jdbcDriverDeployCheckBox.setSelected(driverDeployEnabled);

        String sessionFlag = ip.get(GlassfishModule.SESSION_PRESERVATION_FLAG);
        boolean sessionEnabled = Boolean.parseBoolean(sessionFlag);
        enableSessionsCheckBox.setSelected(sessionEnabled);
        String derbyFlag = ip.get(GlassfishModule.START_DERBY_FLAG);
        boolean derbyEnabled = Boolean.parseBoolean(derbyFlag);
        startDerby.setSelected(derbyEnabled);
        boolean isLocalDomain = ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR) != null;
        cometCheckBox.setEnabled(isLocalDomain);
        monitorCheckBox.setEnabled(isLocalDomain);
        this.jdbcDriverDeployCheckBox.setEnabled(isLocalDomain);
        this.startDerby.setEnabled(isLocalDomain);
}
    
    private void persistFields() {
        if(cometEnabledChanged) {
            String cometEnabled = Boolean.toString(cometCheckBox.isSelected());
            instance.putProperty(GlassfishModule.COMET_FLAG, cometEnabled);
            RequestProcessor.getDefault().post(new EnableComet(instance));
        }
        if (monitorEnabledChanged) {
            String monitorEnabled = Boolean.toString(monitorCheckBox.isSelected());
            instance.putProperty(
                    GlassfishModule.HTTP_MONITOR_FLAG, monitorEnabled);
        }
        if (jdbcDriverDeployEnabledChanged) {
            String driverDeployEnabled = Boolean.toString(jdbcDriverDeployCheckBox.isSelected());
            instance.putProperty(
                    GlassfishModule.DRIVER_DEPLOY_FLAG, driverDeployEnabled);
        }
        if (sessionEnabledChanged) {
            String sessionsEnabled = Boolean.toString(enableSessionsCheckBox.isSelected());
            instance.putProperty(
                    GlassfishModule.SESSION_PRESERVATION_FLAG, sessionsEnabled);
        }
        if (startDerbyChanged) {
            String derbyEnabled = Boolean.toString(startDerby.isSelected());
            instance.putProperty(
                    GlassfishModule.START_DERBY_FLAG, derbyEnabled);
        }
        if ((cometEnabledChanged || monitorEnabledChanged
                || jdbcDriverDeployEnabledChanged || sessionEnabledChanged
                || startDerbyChanged) && instance.getCommonSupport().isWritable()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(getClass(), "WRN_CouldNotWrite"),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            Logger.getLogger("glassfish").warning("Could not write changed property");
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        initFields();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        persistFields();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelLocation = new javax.swing.JLabel();
        textLocation = new javax.swing.JTextField();
        labelDomainsFolder = new javax.swing.JLabel();
        textDomainsFolder = new javax.swing.JTextField();
        labelDomainName = new javax.swing.JLabel();
        textDomainName = new javax.swing.JTextField();
        cometCheckBox = new javax.swing.JCheckBox();
        monitorCheckBox = new javax.swing.JCheckBox();
        jdbcDriverDeployCheckBox = new javax.swing.JCheckBox();
        enableSessionsCheckBox = new javax.swing.JCheckBox();
        startDerby = new javax.swing.JCheckBox();
        targetValueLabel = new javax.swing.JLabel();
        targetValueField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        buttonPw = new javax.swing.JButton(buttonPwAction);

        setName(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_Common")); // NOI18N

        labelLocation.setLabelFor(textLocation);
        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_Location")); // NOI18N

        textLocation.setEditable(false);

        labelDomainsFolder.setLabelFor(textDomainsFolder);
        org.openide.awt.Mnemonics.setLocalizedText(labelDomainsFolder, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_DomainsFolder")); // NOI18N

        textDomainsFolder.setEditable(false);

        labelDomainName.setLabelFor(textDomainName);
        org.openide.awt.Mnemonics.setLocalizedText(labelDomainName, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_DomainName")); // NOI18N

        textDomainName.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(cometCheckBox, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_EnableCometSupport")); // NOI18N
        cometCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cometCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(monitorCheckBox, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_EnableHttpMonitor")); // NOI18N
        monitorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monitorCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jdbcDriverDeployCheckBox, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_EnableJDBCDiverDeployment")); // NOI18N
        jdbcDriverDeployCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jdbcDriverDeployCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(enableSessionsCheckBox, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_PreserverSessions")); // NOI18N
        enableSessionsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableSessionsCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(startDerby, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_START_DERBY")); // NOI18N
        startDerby.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startDerby(evt);
            }
        });

        targetValueLabel.setLabelFor(targetValueField);
        org.openide.awt.Mnemonics.setLocalizedText(targetValueLabel, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "InstanceCustomizer.targetValueLabel")); // NOI18N

        targetValueField.setEditable(false);

        userNameLabel.setLabelFor(userNameField);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "InstanceCustomizer.userNameLabel")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "InstanceCustomizer.passwordLabel")); // NOI18N

        buttonPw.setText(buttonPwLabel());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(cometCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(327, 327, 327))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(monitorCheckBox)
                    .addComponent(labelDomainName)
                    .addComponent(jdbcDriverDeployCheckBox))
                .addGap(361, 361, 361))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(targetValueLabel)
                            .addComponent(userNameLabel)
                            .addComponent(passwordLabel)
                            .addComponent(labelDomainsFolder)
                            .addComponent(labelLocation))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textDomainsFolder)
                            .addComponent(textDomainName)
                            .addComponent(textLocation)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonPw)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enableSessionsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(startDerby, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLocation)
                    .addComponent(textLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDomainsFolder)
                    .addComponent(textDomainsFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDomainName)
                    .addComponent(textDomainName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetValueLabel)
                    .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPw, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cometCheckBox)
                    .addComponent(enableSessionsCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(monitorCheckBox)
                    .addComponent(startDerby))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jdbcDriverDeployCheckBox)
                .addGap(17, 17, 17))
        );

        textLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_InstanceLocation")); // NOI18N
        textDomainsFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_DomainFolder")); // NOI18N
        textDomainName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_DomainName")); // NOI18N
        cometCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_CometSupport")); // NOI18N
        monitorCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_HttpMonitor")); // NOI18N
        jdbcDriverDeployCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_DriverDeployment")); // NOI18N
        enableSessionsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_SessionPreservation")); // NOI18N
        startDerby.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_StartDerby")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "A11Y_DESC_CommonPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cometCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cometCheckBoxActionPerformed
    cometEnabledChanged = true;
}//GEN-LAST:event_cometCheckBoxActionPerformed

private void monitorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monitorCheckBoxActionPerformed
    monitorEnabledChanged = true;
    if (monitorCheckBox.isSelected()) {
        // open a message about the scary effects of HTTP monitoring
        NotifyDescriptor dd = new NotifyDescriptor(NbBundle.getMessage(this.getClass(), "TXT_WARNING_HTTP_MONITOR_ON"), // NOI18N
                NbBundle.getMessage(this.getClass(), "TITLE_WARNING_HTTP_MONITOR_ON"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.WARNING_MESSAGE, null, null);
        if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.CANCEL_OPTION)) {
            monitorCheckBox.setSelected(false);
            monitorEnabledChanged = false;
        }
    } else {
        // open a message about the scary effects of HTTP monitoring
        NotifyDescriptor dd = new NotifyDescriptor(NbBundle.getMessage(this.getClass(), "TXT_WARNING_HTTP_MONITOR_OFF"), // NOI18N
                NbBundle.getMessage(this.getClass(), "TITLE_WARNING_HTTP_MONITOR_OFF"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.WARNING_MESSAGE, null, null);
        if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.CANCEL_OPTION)) {
            monitorCheckBox.setSelected(true);
            monitorEnabledChanged = false;
        }
    }
}//GEN-LAST:event_monitorCheckBoxActionPerformed

private void jdbcDriverDeployCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jdbcDriverDeployCheckBoxActionPerformed
    jdbcDriverDeployEnabledChanged = true;
}//GEN-LAST:event_jdbcDriverDeployCheckBoxActionPerformed

private void enableSessionsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableSessionsCheckBoxActionPerformed
    sessionEnabledChanged = true;
}//GEN-LAST:event_enableSessionsCheckBoxActionPerformed

private void startDerby(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startDerby
    startDerbyChanged = true;
}//GEN-LAST:event_startDerby


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonPw;
    private javax.swing.JCheckBox cometCheckBox;
    private javax.swing.JCheckBox enableSessionsCheckBox;
    private javax.swing.JCheckBox jdbcDriverDeployCheckBox;
    private javax.swing.JLabel labelDomainName;
    private javax.swing.JLabel labelDomainsFolder;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JCheckBox monitorCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox startDerby;
    private javax.swing.JTextField targetValueField;
    private javax.swing.JLabel targetValueLabel;
    private javax.swing.JTextField textDomainName;
    private javax.swing.JTextField textDomainsFolder;
    private javax.swing.JTextField textLocation;
    private javax.swing.JTextField userNameField;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables

}
