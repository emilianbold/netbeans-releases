/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.glassfish.common.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.ServerDetails;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  vbk
 */
public class AddDomainLocationVisualPanel extends javax.swing.JPanel {

    private transient final List<ChangeListener> listeners; 

    /** Creates new form AddDomainLocationVisualPanel */
    public AddDomainLocationVisualPanel() {
        listeners = new CopyOnWriteArrayList<ChangeListener>();
        initComponents();
        registerLocalRB.setSelected(true);
        registerRemoteRB.setSelected(false);
        hostNameField.setEnabled(false);
        portValueField.setEnabled(false);
        setName(NbBundle.getMessage(AddDomainLocationVisualPanel.class, "TITLE_DomainLocation")); // NOI18N
    }
    
    void initModels(String gfRoot) {
        // Put the choices into the combo box...
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        File domainsDir = new File(gfRoot, GlassfishInstance.DEFAULT_DOMAINS_FOLDER); // NOI18N
        File candidates[] = domainsDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File dir) {
                File logsDir = new File(dir, "logs"); // NOI18N
                return Utils.canWrite(logsDir);
            }
            
        });
        if (null != candidates) {
            for (File f : candidates) {
                model.addElement(f.getName());
            }
        } 
        if (model.getSize() == 0) {
            FileObject userHome = FileUtil.toFileObject(
                    FileUtil.normalizeFile(new File(System.getProperty("user.home"))));
            String defaultItem = FileUtil.findFreeFolderName(userHome, "personal_domain");
            model.addElement(System.getProperty("user.home")+File.separator+defaultItem);
        }
        domainField.setModel(model);
        KeyListener kl = new MyKeyListener();
        domainField.getEditor().getEditorComponent().addKeyListener(kl);
        domainField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                domainField.getEditor().setItem(domainField.getSelectedItem());
                fireChangeEvent();
            }
        });
        useDefaultPortsCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireChangeEvent();
            }
        });
        boolean defaultPortsAreOpen = true;
        ServerSocket adminPort = null;
        ServerSocket httpPort = null;
        try {
            adminPort = new ServerSocket(4848);
            httpPort = new ServerSocket(8080);
        } catch (IOException ex) {
            defaultPortsAreOpen = false;
            useDefaultPortsCB.setText(NbBundle.getMessage(AddDomainLocationVisualPanel.class,
                    "AddDomainLocationVisualPanel.useDefaultPortsCB.text2"));
            if (null == adminPort) {
                useDefaultPortsCB.setToolTipText(
                        NbBundle.getMessage(AddDomainLocationVisualPanel.class, "TIP_ADMIN_IN_USE"));  // NOI18N
            } else {
                useDefaultPortsCB.setToolTipText(
                        NbBundle.getMessage(AddDomainLocationVisualPanel.class, "TIP_HTTP_IN_USE"));  // NOI18N
            }
        } finally {
            if (null != adminPort) { try { adminPort.close(); } catch (IOException ioe) {}}
            if (null != httpPort) { try { httpPort.close(); } catch (IOException ioe) {}}
        }
        useDefaultPortsCB.setEnabled(defaultPortsAreOpen);
        useDefaultPortsCB.setSelected(defaultPortsAreOpen);
        registerLocalRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostNameField.setEnabled(!registerLocalRB.isSelected());
                portValueField.setEnabled(!registerLocalRB.isSelected());
                domainField.setEnabled(!registerRemoteRB.isSelected());
                useDefaultPortsCB.setEnabled(!registerRemoteRB.isSelected());
                fireChangeEvent();
            }
        });
        registerRemoteRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostNameField.setEnabled(!registerLocalRB.isSelected());
                portValueField.setEnabled(!registerLocalRB.isSelected());
                domainField.setEnabled(!registerRemoteRB.isSelected());
                useDefaultPortsCB.setEnabled(!registerRemoteRB.isSelected());
                fireChangeEvent();
            }
        });
        hostNameField.addKeyListener(kl);
        portValueField.addKeyListener(kl);
        // make sure the target field is ok...
        //
        if (ServerDetails.getVersionFromInstallDirectory(new File(gfRoot)) < 
                ServerDetails.GLASSFISH_SERVER_3_1.getVersion()) {
            targetValueField.setText(""); // NOI18N
            targetValueField.setEnabled(false);
        } else {
            targetValueField.setText(""); // NOI18N
            targetValueField.setEnabled(true);
        }
        targetValueField.addKeyListener(kl);
        userNameField.addKeyListener(kl);
        passwordField.addKeyListener(kl);
    }
    
    String getDomainField() {
        return (String) domainField.getEditor().getItem();  //getSelectedItem();
    }

    String getHostName() {
        return hostNameField.getText().trim();
    }

    String getPortValue() {
        return portValueField.getText().trim();
    }
    
    String getTargetValue() {
        return targetValueField.getText().trim();
    }

    /**
     * Return administrator's user name value from text field.
     * <p/>
     * @return Administrator's user name value from text field.
     */
    String getUserNameValue() {
        return userNameField.getText().trim();
    }

    /**
     * Return administrator's password value from text field.
     * <p/>
     * @return Administrator's password value from text field.
     */
    String getPasswordValue() {
        return new String(passwordField.getPassword());
    }

    boolean getUseDefaultPorts() {
        return useDefaultPortsCB.isEnabled() && useDefaultPortsCB.isSelected();
    }

    /**
     * Initialize server port with default value.
     * <p/>
     * @return Default administrator's user name value.
     */
    private String initPortValue() {
        return Integer.toString(GlassfishInstance.DEFAULT_ADMIN_PORT);
    }

    /**
     * Initialize administrator's user name with default value.
     * <p/>
     * @return Default administrator's user name value.
     */
    private String initUserNameValue() {
        //return GlassfishInstance.DEFAULT_ADMIN_NAME;
        return "";
    }

    /**
     * Initialize administrator's password with default value.
     * <p/>
     * @return Default administrator's password value.
     */
    private String initPasswordValue() {
        //return GlassfishInstance.DEFAULT_ADMIN_PASSWORD;
        return "";
    }

    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l ) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }

    boolean registerLocalDomain() {
        return registerLocalRB.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        registerLocalRB = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        domainFieldLabel = new javax.swing.JLabel();
        domainField = new javax.swing.JComboBox();
        useDefaultPortsCB = new javax.swing.JCheckBox();
        targetValueLabel = new javax.swing.JLabel();
        targetValueField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        registerRemoteRB = new javax.swing.JRadioButton();
        hostNameLabel = new javax.swing.JLabel();
        hostNameField = new javax.swing.JTextField();
        portValueLabel = new javax.swing.JLabel();
        portValueField = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(438, 353));

        buttonGroup1.add(registerLocalRB);
        org.openide.awt.Mnemonics.setLocalizedText(registerLocalRB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerLocalRB.text")); // NOI18N

        domainFieldLabel.setLabelFor(domainField);
        org.openide.awt.Mnemonics.setLocalizedText(domainFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainFieldLabel.text")); // NOI18N

        domainField.setEditable(true);
        domainField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultPortsCB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.text")); // NOI18N
        useDefaultPortsCB.setToolTipText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(useDefaultPortsCB)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(domainFieldLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(domainField, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(domainFieldLabel)
                    .addComponent(domainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useDefaultPortsCB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        domainField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainField.AccessibleContext.accessibleDescription")); // NOI18N
        useDefaultPortsCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.AccessibleContext.accessibleDescription")); // NOI18N

        targetValueLabel.setLabelFor(portValueField);
        org.openide.awt.Mnemonics.setLocalizedText(targetValueLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueLabel.text")); // NOI18N

        targetValueField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.userNameLabel.text")); // NOI18N

        userNameField.setText(initUserNameValue());

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.passwordLabel.text")); // NOI18N

        passwordField.setText(initPasswordValue());

        buttonGroup1.add(registerRemoteRB);
        org.openide.awt.Mnemonics.setLocalizedText(registerRemoteRB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerRemoteRB.text")); // NOI18N

        hostNameLabel.setLabelFor(hostNameField);
        org.openide.awt.Mnemonics.setLocalizedText(hostNameLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostNameLabel.text")); // NOI18N

        hostNameField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostNameField.text")); // NOI18N
        hostNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostNameFieldActionPerformed(evt);
            }
        });

        portValueLabel.setLabelFor(portValueField);
        org.openide.awt.Mnemonics.setLocalizedText(portValueLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.portValueLabel.text")); // NOI18N

        portValueField.setText(initPortValue());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordLabel)
                    .addComponent(targetValueLabel)
                    .addComponent(userNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(portValueLabel)
                            .addComponent(hostNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(portValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 157, Short.MAX_VALUE))
                            .addComponent(hostNameField)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(registerRemoteRB)
                            .addComponent(registerLocalRB))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(registerLocalRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(registerRemoteRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hostNameLabel)
                    .addComponent(hostNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(portValueLabel)
                    .addComponent(portValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(targetValueLabel)
                    .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(userNameLabel)
                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(123, Short.MAX_VALUE))
        );

        registerLocalRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerLocalRB.AccessibleContext.accessibleDescription")); // NOI18N
        registerRemoteRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerRemoteRB.AccessibleContext.accessibleDescription")); // NOI18N
        hostNameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostNameField.AccessibleContext.accessibleDescription")); // NOI18N
        portValueField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.portValueField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void hostNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hostNameFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox domainField;
    private javax.swing.JLabel domainFieldLabel;
    private javax.swing.JTextField hostNameField;
    private javax.swing.JLabel hostNameLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portValueField;
    private javax.swing.JLabel portValueLabel;
    private javax.swing.JRadioButton registerLocalRB;
    private javax.swing.JRadioButton registerRemoteRB;
    private javax.swing.JTextField targetValueField;
    private javax.swing.JLabel targetValueLabel;
    private javax.swing.JCheckBox useDefaultPortsCB;
    private javax.swing.JTextField userNameField;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables

    class MyKeyListener implements KeyListener {
        @Override
            public void keyTyped(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        @Override
            public void keyPressed(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        @Override
            public void keyReleased(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
                fireChangeEvent();
            }

    }
}
