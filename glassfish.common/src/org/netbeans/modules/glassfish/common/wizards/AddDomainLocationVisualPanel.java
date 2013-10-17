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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.glassfish.tools.ide.utils.NetUtils;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.ServerDetails;
import org.netbeans.modules.glassfish.common.ui.IpComboBox;
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

    /** IP addresses selection content. */
    Set<? extends InetAddress> ips;

    /** Creates new form AddDomainLocationVisualPanel */
    public AddDomainLocationVisualPanel() {
        listeners = new CopyOnWriteArrayList<ChangeListener>();
        ips = NetUtils.getHostIP4s();
        initComponents();
        registerLocalRB.setSelected(true);
        registerRemoteRB.setSelected(false);
        hostRemoteField.setEnabled(false);
        dasPortField.setEnabled(false);
        httpPortField.setEnabled(false);
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
                updatePortsFields();
                fireChangeEvent();
            }
        });
        useLocalIpCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLocalIpsCombobox();
                fireChangeEvent();
            }
        });
        updateLocalIpsCombobox();
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
                updateLocalRemoteFields();
                fireChangeEvent();
            }
        });
        registerRemoteRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLocalRemoteFields();
                fireChangeEvent();
            }
        });
        hostRemoteField.addKeyListener(kl);
        dasPortField.addKeyListener(kl);
        httpPortField.addKeyListener(kl);
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
        return (String)domainField.getEditor().getItem();
    }

    String getHostName() {
        return hostRemoteField.getText().trim();
    }

    /**
     * Retrieve DAS port value stored in form.
     * <p/>
     * @return DAS port value stored in form.
     */
    String getAdminPortValue() {
        return dasPortField.getText().trim();
    }
    
    /**
     * Set DAS port value of corresponding form field.
     * <p/>
     * @param port DAS port value to be set.
     */
    void setAdminPortValue(String port) {
        dasPortField.setText(port);
    }

    /**
     * Retrieve HTTP port value stored in form.
     * <p/>
     * @return HTTP port value stored in form.
     */
    String getHttpPortValue() {
        return httpPortField.getText().trim();
    }

    /**
     * Set HTTP port value of corresponding form field.
     * <p/>
     * @param port HTTP port value to be set.
     */
    void setHttpPortValue(String port) {
        httpPortField.setText(port);
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
     * Get local host IP selected.
     * <p/>
     * @return Raw local host IP selected object.
     */
    Object getLocalHostIp() {
        return hostLocalField.getEditor().getItem();
    }

    /**
     * Initialize server port with default value.
     * <p/>
     * @return Default administrator's user name value.
     */
    private String initAdminPortValue() {
        return Integer.toString(GlassfishInstance.DEFAULT_ADMIN_PORT);
    }

    /**
     * Initialize server port with default value.
     * <p/>
     * @return Default administrator's user name value.
     */
    private String initHttpPortValue() {
        return Integer.toString(GlassfishInstance.DEFAULT_HTTP_PORT);
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

    /**
     * Update content of host IPs combo box depending
     * on <code>Looopback</code> check box status.
     * <p/>
     * @param e A semantic event which indicates that a component-defined
     *          action occurred.
     */
    private void updateLocalIpsCombobox() {
        ((IpComboBox)hostLocalField).updateModel(ips, useLocalIpCB.isSelected());
    }

    /**
     * Update content of port fields values depending
     * on <code>Default</code> check box status.
     */
    private void updatePortsFields() {
        if (useDefaultPortsCB.isSelected()) {
            dasPortField.setText(initAdminPortValue());
            httpPortField.setText(initHttpPortValue());
            dasPortField.setEnabled(false);
            httpPortField.setEnabled(false);
        } else {
            dasPortField.setEnabled(true);
            httpPortField.setEnabled(true);
        }
    }

    /**
     * Update local and remote fields availability depending on Local/Remote
     * radio button value.
     */
    private void updateLocalRemoteFields() {
        domainField.setEnabled(registerLocalRB.isSelected());
        hostLocalField.setEnabled(registerLocalRB.isSelected());
        useLocalIpCB.setEnabled(registerLocalRB.isSelected());
        hostRemoteField.setEnabled(registerRemoteRB.isSelected());
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
        localPanel = new javax.swing.JPanel();
        domainFieldLabel = new javax.swing.JLabel();
        domainField = new javax.swing.JComboBox();
        useLocalIpCB = new javax.swing.JCheckBox();
        hostLocalField = new IpComboBox(ips, useLocalIpCB.isSelected());
        hostLocalFieldLabel = new javax.swing.JLabel();
        targetValueLabel = new javax.swing.JLabel();
        targetValueField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        registerRemoteRB = new javax.swing.JRadioButton();
        dasPortFieldLabel = new javax.swing.JLabel();
        dasPortField = new javax.swing.JTextField();
        httpPortFieldLabel = new javax.swing.JLabel();
        httpPortField = new javax.swing.JTextField();
        useDefaultPortsCB = new javax.swing.JCheckBox();
        remotePanel = new javax.swing.JPanel();
        hostRemoteFieldLabel = new javax.swing.JLabel();
        hostRemoteField = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(438, 353));

        buttonGroup1.add(registerLocalRB);
        org.openide.awt.Mnemonics.setLocalizedText(registerLocalRB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerLocalRB.text")); // NOI18N

        domainFieldLabel.setLabelFor(domainField);
        org.openide.awt.Mnemonics.setLocalizedText(domainFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainFieldLabel.text")); // NOI18N

        domainField.setEditable(true);
        domainField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        useLocalIpCB.setSelected(true);
        useLocalIpCB.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useLocalIpCB.text")); // NOI18N

        hostLocalField.setEditable(true);
        hostLocalField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        hostLocalFieldLabel.setLabelFor(hostLocalField);
        hostLocalFieldLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostLocalFieldLabel.text")); // NOI18N
        hostLocalFieldLabel.setMaximumSize(new java.awt.Dimension(62, 16));
        hostLocalFieldLabel.setMinimumSize(new java.awt.Dimension(62, 16));
        hostLocalFieldLabel.setPreferredSize(new java.awt.Dimension(62, 16));

        javax.swing.GroupLayout localPanelLayout = new javax.swing.GroupLayout(localPanel);
        localPanel.setLayout(localPanelLayout);
        localPanelLayout.setHorizontalGroup(
            localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, localPanelLayout.createSequentialGroup()
                .addGroup(localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(domainFieldLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(hostLocalFieldLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(localPanelLayout.createSequentialGroup()
                        .addComponent(hostLocalField, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useLocalIpCB))
                    .addComponent(domainField, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        localPanelLayout.setVerticalGroup(
            localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localPanelLayout.createSequentialGroup()
                .addGroup(localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(domainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(localPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(domainFieldLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLocalFieldLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostLocalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useLocalIpCB)))
        );

        domainField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainField.AccessibleContext.accessibleDescription")); // NOI18N

        targetValueLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(targetValueLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueLabel.text")); // NOI18N

        targetValueField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.userNameLabel.text")); // NOI18N

        userNameField.setText(initUserNameValue());

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.passwordLabel.text")); // NOI18N

        passwordField.setText(initPasswordValue());

        buttonGroup1.add(registerRemoteRB);
        org.openide.awt.Mnemonics.setLocalizedText(registerRemoteRB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerRemoteRB.text")); // NOI18N

        dasPortFieldLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(dasPortFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.dasPortFieldLabel.text")); // NOI18N

        dasPortField.setText(initAdminPortValue());

        httpPortFieldLabel.setLabelFor(httpPortField);
        httpPortFieldLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.httpPortFieldLabel.text")); // NOI18N

        httpPortField.setText(initHttpPortValue());

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultPortsCB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.text")); // NOI18N
        useDefaultPortsCB.setToolTipText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.toolTipText")); // NOI18N

        hostRemoteFieldLabel.setLabelFor(hostRemoteField);
        org.openide.awt.Mnemonics.setLocalizedText(hostRemoteFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostRemoteFieldLabel.text")); // NOI18N
        hostRemoteFieldLabel.setMaximumSize(new java.awt.Dimension(62, 16));
        hostRemoteFieldLabel.setMinimumSize(new java.awt.Dimension(62, 16));
        hostRemoteFieldLabel.setPreferredSize(new java.awt.Dimension(62, 16));

        hostRemoteField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostRemoteField.text")); // NOI18N
        hostRemoteField.setMaximumSize(new java.awt.Dimension(32767, 32767));
        hostRemoteField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostRemoteFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout remotePanelLayout = new javax.swing.GroupLayout(remotePanel);
        remotePanel.setLayout(remotePanelLayout);
        remotePanelLayout.setHorizontalGroup(
            remotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(remotePanelLayout.createSequentialGroup()
                .addComponent(hostRemoteFieldLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hostRemoteField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        remotePanelLayout.setVerticalGroup(
            remotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, remotePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(remotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostRemoteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostRemoteFieldLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        hostRemoteField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostRemoteField.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(remotePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(localPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(registerRemoteRB)
                            .addComponent(registerLocalRB)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(passwordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(userNameLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                                            .addComponent(httpPortFieldLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(dasPortFieldLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(targetValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(dasPortField)
                                            .addComponent(httpPortField, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(targetValueField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                                            .addComponent(userNameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useDefaultPortsCB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(registerLocalRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(registerRemoteRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remotePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dasPortFieldLabel)
                    .addComponent(dasPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useDefaultPortsCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(httpPortFieldLabel)
                    .addComponent(httpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        registerLocalRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerLocalRB.AccessibleContext.accessibleDescription")); // NOI18N
        registerRemoteRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerRemoteRB.AccessibleContext.accessibleDescription")); // NOI18N
        dasPortField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.dasPortField.AccessibleContext.accessibleDescription")); // NOI18N
        useDefaultPortsCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void hostRemoteFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostRemoteFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hostRemoteFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField dasPortField;
    private javax.swing.JLabel dasPortFieldLabel;
    private javax.swing.JComboBox domainField;
    private javax.swing.JLabel domainFieldLabel;
    private javax.swing.JComboBox hostLocalField;
    private javax.swing.JLabel hostLocalFieldLabel;
    private javax.swing.JTextField hostRemoteField;
    private javax.swing.JLabel hostRemoteFieldLabel;
    private javax.swing.JTextField httpPortField;
    private javax.swing.JLabel httpPortFieldLabel;
    private javax.swing.JPanel localPanel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JRadioButton registerLocalRB;
    private javax.swing.JRadioButton registerRemoteRB;
    private javax.swing.JPanel remotePanel;
    private javax.swing.JTextField targetValueField;
    private javax.swing.JLabel targetValueLabel;
    private javax.swing.JCheckBox useDefaultPortsCB;
    private javax.swing.JCheckBox useLocalIpCB;
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
