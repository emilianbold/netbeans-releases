/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.ui;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import javax.swing.JFileChooser;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.netbeans.modules.glassfish.common.Util;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.JrePicker;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class VmCustomizer extends javax.swing.JPanel  {
    GlassfishModule gm;
    JrePicker picker = null;
    /** Creates new form VmCustomizer */
    public VmCustomizer(GlassfishModule commonSupport) {
        gm = commonSupport;
        initComponents();
        // hook for future customization
        //picker = org.openide.util.Lookup.getDefault().lookup(JrePicker.class);
        // put the picker component into the pickerPanel
        // left as an exercise for the reader at this point...
    }

    private void initFields() {
        if (null == picker) {
            javaExecutableField.setText(gm.getInstanceProperties().get(GlassfishModule.JAVA_PLATFORM_ATTR));
        } else {
            throw new UnsupportedOperationException("not implemented yet");
            // if there is a picker
            // picker.initFromJava(gm.getInstanceProperties().get(GlassfishModule.JAVA_PLATFORM_ATTR));
        }
        String address = gm.getInstanceProperties().get(GlassfishModule.DEBUG_PORT);
        if (null == address || "".equals(address)) {
            useUserDefinedAddress.setSelected(false);
            addressValue.setEditable(false);
        } else {
            useUserDefinedAddress.setSelected(true);
            addressValue.setEditable(true);
            addressValue.setText(address);

        }
        if (!"/".equals(File.separator)) {
            useSharedMemRB.setSelected("true".equals(gm.getInstanceProperties().get(GlassfishModule.USE_SHARED_MEM_ATTR)));
            useSocketRB.setSelected(!("true".equals(gm.getInstanceProperties().get(GlassfishModule.USE_SHARED_MEM_ATTR))));
        } else {
            // not windows -- disable shared mem and correct it if it was set...
            useSharedMemRB.setEnabled(false);
            useSharedMemRB.setSelected(false);
            useSocketRB.setSelected(true);
        }
        useIDEProxyInfo.setSelected("true".equals(gm.getInstanceProperties().get(GlassfishModule.USE_IDE_PROXY_FLAG)));
        boolean isLocalDomain = gm.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR) != null;
        this.javaExecutableField.setEnabled(isLocalDomain);
        this.useIDEProxyInfo.setEnabled(isLocalDomain);
        this.useSharedMemRB.setEnabled(isLocalDomain);
    }

    private void persistFields() {
        if (null == picker) {
            gm.setEnvironmentProperty(GlassfishModule.JAVA_PLATFORM_ATTR, javaExecutableField.getText(), true);
            RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
            if (null != db) {
                File f = new File(javaExecutableField.getText().trim());
                if (f.exists() && f.canRead()) {
                    File dir = f.getParentFile().getParentFile();
                    File dbdir = new File(dir,"db"); // NOI18N
                    if (dbdir.exists() && dbdir.isDirectory() && dbdir.canRead()) {
                        db.initialize(dbdir.getAbsolutePath());
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("not implemented yet");
            // get data out of the picker
            //gm.setEnvironmentProperty(GlassfishModule.JAVA_PLATFORM_ATTR, picker.getJava(), true);
        }
        gm.setEnvironmentProperty(GlassfishModule.USE_SHARED_MEM_ATTR, Boolean.toString(useSharedMemRB.isSelected()),true);
        gm.setEnvironmentProperty(GlassfishModule.USE_IDE_PROXY_FLAG, Boolean.toString(useIDEProxyInfo.isSelected()),true);
        gm.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, addressValue.getText().trim(),true);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        debugSettingsPanel = new javax.swing.JPanel();
        useSocketRB = new javax.swing.JRadioButton();
        useSharedMemRB = new javax.swing.JRadioButton();
        useUserDefinedAddress = new javax.swing.JCheckBox();
        addressValue = new javax.swing.JTextField();
        pickerPanel = new javax.swing.JPanel();
        javaInstallLabel = new javax.swing.JLabel();
        openDirectoryBrowser = new javax.swing.JButton();
        javaExecutableField = new javax.swing.JTextField();
        useIDEProxyInfo = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.name")); // NOI18N

        debugSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.debugSettingsPanel.border.title.text"))); // NOI18N

        buttonGroup1.add(useSocketRB);
        org.openide.awt.Mnemonics.setLocalizedText(useSocketRB, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useSocketRB.text")); // NOI18N

        buttonGroup1.add(useSharedMemRB);
        org.openide.awt.Mnemonics.setLocalizedText(useSharedMemRB, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useSharedMemRB.text")); // NOI18N

        useUserDefinedAddress.setText(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useUserDefinedAddress.text", new Object[] {})); // NOI18N
        useUserDefinedAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleAddressUsage(evt);
            }
        });

        addressValue.setText(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.addressValue.text", new Object[] {})); // NOI18N

        org.jdesktop.layout.GroupLayout debugSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(debugSettingsPanel);
        debugSettingsPanel.setLayout(debugSettingsPanelLayout);
        debugSettingsPanelLayout.setHorizontalGroup(
            debugSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(debugSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(debugSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(useSharedMemRB)
                    .add(useSocketRB)
                    .add(debugSettingsPanelLayout.createSequentialGroup()
                        .add(useUserDefinedAddress)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addressValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)))
                .addContainerGap())
        );
        debugSettingsPanelLayout.setVerticalGroup(
            debugSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(debugSettingsPanelLayout.createSequentialGroup()
                .add(useSharedMemRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(useSocketRB)
                .add(8, 8, 8)
                .add(debugSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(useUserDefinedAddress)
                    .add(addressValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javaInstallLabel.setLabelFor(javaExecutableField);
        org.openide.awt.Mnemonics.setLocalizedText(javaInstallLabel, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.javaInstallLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(openDirectoryBrowser, org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.openDirectoryBrowser.text")); // NOI18N
        openDirectoryBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInstallChooser(evt);
            }
        });

        javaExecutableField.setText(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.javaExecutableField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout pickerPanelLayout = new org.jdesktop.layout.GroupLayout(pickerPanel);
        pickerPanel.setLayout(pickerPanelLayout);
        pickerPanelLayout.setHorizontalGroup(
            pickerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pickerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(javaInstallLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(javaExecutableField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 308, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(openDirectoryBrowser)
                .addContainerGap())
        );
        pickerPanelLayout.setVerticalGroup(
            pickerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pickerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(pickerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(javaInstallLabel)
                    .add(javaExecutableField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(openDirectoryBrowser))
                .addContainerGap())
        );

        useIDEProxyInfo.setText(org.openide.util.NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.useIDEProxyInfo.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pickerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(useIDEProxyInfo))
                    .add(debugSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pickerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(9, 9, 9)
                .add(debugSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(useIDEProxyInfo)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void openInstallChooser(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInstallChooser
        JFileChooser f = new JFileChooser();
        f.setSelectedFile(new File(javaExecutableField.getText()));
        f.setFileSelectionMode(JFileChooser.FILES_ONLY);
        f.setMultiSelectionEnabled(false);
        final String TESTNAME = File.separatorChar == '/' ? "java" : "java.exe";
        f.setFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File arg0) {
                if (arg0.isDirectory()) {
                    return true;
                }
                if (arg0.getName().equalsIgnoreCase(TESTNAME)) {
                    if (gm.getInstanceProvider().equals(GlassfishInstanceProvider.getEe6())) {
                            return Util.appearsToBeJdk6OrBetter(arg0);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(VmCustomizer.class, "VmCustomizer.filechooser.description");
            }

        });
        int retVal = f.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            javaExecutableField.setText(f.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_openInstallChooser

    private void toggleAddressUsage(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleAddressUsage
        if (useUserDefinedAddress.isSelected()) {
            // enable the edit field and fill it in
            addressValue.setEnabled(true);
            int debugPort = 8787;
            try {
                    ServerSocket t = new ServerSocket(0);
                    debugPort = t.getLocalPort();
                    t.close();
            } catch (IOException ioe) {
                // I will ignore this nor now.
            }
            addressValue.setText(Integer.toString(debugPort));
            addressValue.setEditable(true);
        } else {
            // clear the field and disable it
             addressValue.setEditable(false);
             addressValue.setText("");
             addressValue.setEnabled(false);
        }
    }//GEN-LAST:event_toggleAddressUsage


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressValue;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel debugSettingsPanel;
    private javax.swing.JTextField javaExecutableField;
    private javax.swing.JLabel javaInstallLabel;
    private javax.swing.JButton openDirectoryBrowser;
    private javax.swing.JPanel pickerPanel;
    private javax.swing.JCheckBox useIDEProxyInfo;
    private javax.swing.JRadioButton useSharedMemRB;
    private javax.swing.JRadioButton useSocketRB;
    private javax.swing.JCheckBox useUserDefinedAddress;
    // End of variables declaration//GEN-END:variables


}
