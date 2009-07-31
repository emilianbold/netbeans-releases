// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>

package org.netbeans.modules.glassfish.common.ui;

import java.util.Map;
import org.netbeans.modules.glassfish.spi.GlassfishModule;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class InstanceCustomizer extends javax.swing.JPanel {

    private GlassfishModule commonSupport;
    private boolean cometEnabledChanged = false;
    private boolean monitorEnabledChanged = false;
    private boolean jdbcDriverDeployEnabledChanged = false;
    private boolean sessionEnabledChanged = false;
    private boolean startDerbyChanged = false;
    
    public InstanceCustomizer(GlassfishModule commonSupport) {
        this.commonSupport = commonSupport;
        
        initComponents();
    }

    private void initFields() {
        Map<String, String> ip = commonSupport.getInstanceProperties();
        textLocation.setText(ip.get(GlassfishModule.HOSTNAME_ATTR) + ":" + 
                ip.get(GlassfishModule.HTTPPORT_ATTR));
        textDomainsFolder.setText(ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR)); // NOI18N
        textDomainName.setText(ip.get(GlassfishModule.DOMAIN_NAME_ATTR)); // NOI18N
        
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
            commonSupport.setEnvironmentProperty(GlassfishModule.COMET_FLAG, cometEnabled, true);
        }
        if (monitorEnabledChanged) {
            String monitorEnabled = Boolean.toString(monitorCheckBox.isSelected());
            commonSupport.setEnvironmentProperty(GlassfishModule.HTTP_MONITOR_FLAG, monitorEnabled, true);
        }
        if (jdbcDriverDeployEnabledChanged) {
            String driverDeployEnabled = Boolean.toString(jdbcDriverDeployCheckBox.isSelected());
            commonSupport.setEnvironmentProperty(GlassfishModule.DRIVER_DEPLOY_FLAG, driverDeployEnabled, true);
        }
        if (sessionEnabledChanged) {
            String sessionsEnabled = Boolean.toString(enableSessionsCheckBox.isSelected());
            commonSupport.setEnvironmentProperty(GlassfishModule.SESSION_PRESERVATION_FLAG, sessionsEnabled, true);
        }
        if (startDerbyChanged) {
            String derbyEnabled = Boolean.toString(startDerby.isSelected());
            commonSupport.setEnvironmentProperty(GlassfishModule.START_DERBY_FLAG, derbyEnabled, true);
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

        setName(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_Common")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_Location")); // NOI18N

        textLocation.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(labelDomainsFolder, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_DomainsFolder")); // NOI18N

        textDomainsFolder.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(labelDomainName, org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_DomainName")); // NOI18N

        textDomainName.setEditable(false);

        cometCheckBox.setText(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_EnableCometSupport")); // NOI18N
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

        enableSessionsCheckBox.setText(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_PreserverSessions")); // NOI18N
        enableSessionsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableSessionsCheckBoxActionPerformed(evt);
            }
        });

        startDerby.setText(org.openide.util.NbBundle.getMessage(InstanceCustomizer.class, "LBL_START_DERBY")); // NOI18N
        startDerby.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startDerby(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(startDerby)
                    .add(jdbcDriverDeployCheckBox)
                    .add(monitorCheckBox)
                    .add(cometCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelLocation)
                            .add(labelDomainsFolder)
                            .add(labelDomainName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(textLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .add(textDomainsFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .add(textDomainName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)))
                    .add(enableSessionsCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelLocation)
                    .add(textLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelDomainsFolder)
                    .add(textDomainsFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelDomainName)
                    .add(textDomainName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cometCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(monitorCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jdbcDriverDeployCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(enableSessionsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(startDerby)
                .addContainerGap(13, Short.MAX_VALUE))
        );
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
    private javax.swing.JCheckBox cometCheckBox;
    private javax.swing.JCheckBox enableSessionsCheckBox;
    private javax.swing.JCheckBox jdbcDriverDeployCheckBox;
    private javax.swing.JLabel labelDomainName;
    private javax.swing.JLabel labelDomainsFolder;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JCheckBox monitorCheckBox;
    private javax.swing.JCheckBox startDerby;
    private javax.swing.JTextField textDomainName;
    private javax.swing.JTextField textDomainsFolder;
    private javax.swing.JTextField textLocation;
    // End of variables declaration//GEN-END:variables

}
