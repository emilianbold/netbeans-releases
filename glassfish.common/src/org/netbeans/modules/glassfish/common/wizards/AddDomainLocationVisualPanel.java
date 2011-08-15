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
    }
    
    String getDomainField() {
        return (String) domainField.getEditor().getItem();  //getSelectedItem();
    }

    String getHostName() {
        return hostNameField.getText();
    }

    String getPortValue() {
        return portValueField.getText();
    }
    
    String getTargetValue() {
        return targetValueField.getText().trim();
    }

    boolean getUseDefaultPorts() {
        return useDefaultPortsCB.isEnabled() && useDefaultPortsCB.isSelected();
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
        explanationLabel = new javax.swing.JLabel();
        useDefaultPortsCB = new javax.swing.JCheckBox();
        registerRemoteRB = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        hostNameLabel = new javax.swing.JLabel();
        hostNameField = new javax.swing.JTextField();
        portValueLabel = new javax.swing.JLabel();
        portValueField = new javax.swing.JTextField();
        targetValueLabel = new javax.swing.JLabel();
        targetValueField = new javax.swing.JTextField();

        buttonGroup1.add(registerLocalRB);
        org.openide.awt.Mnemonics.setLocalizedText(registerLocalRB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerLocalRB.text")); // NOI18N

        domainFieldLabel.setLabelFor(domainField);
        org.openide.awt.Mnemonics.setLocalizedText(domainFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainFieldLabel.text")); // NOI18N

        domainField.setEditable(true);
        domainField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        explanationLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.explanationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultPortsCB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.text")); // NOI18N
        useDefaultPortsCB.setToolTipText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(domainFieldLabel)
                        .addGap(4, 4, 4)
                        .addComponent(domainField, 0, 565, Short.MAX_VALUE))
                    .addComponent(explanationLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
                    .addComponent(useDefaultPortsCB))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainFieldLabel)
                    .addComponent(domainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(explanationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useDefaultPortsCB)
                .addGap(6, 6, 6))
        );

        domainField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainField.AccessibleContext.accessibleDescription")); // NOI18N
        explanationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.explanationLabel.AccessibleContext.accessibleDescription")); // NOI18N
        useDefaultPortsCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.AccessibleContext.accessibleDescription")); // NOI18N

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

        portValueField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.portValueField.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostNameLabel)
                    .addComponent(hostNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portValueLabel)
                    .addComponent(portValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        hostNameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostNameField.AccessibleContext.accessibleDescription")); // NOI18N
        portValueField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.portValueField.AccessibleContext.accessibleDescription")); // NOI18N

        targetValueLabel.setLabelFor(portValueField);
        org.openide.awt.Mnemonics.setLocalizedText(targetValueLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueLabel.text")); // NOI18N

        targetValueField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(registerLocalRB)
                .addContainerGap(510, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(registerRemoteRB)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(targetValueLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(550, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 641, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(registerLocalRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(registerRemoteRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetValueLabel)
                    .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        registerLocalRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerLocalRB.AccessibleContext.accessibleDescription")); // NOI18N
        registerRemoteRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerRemoteRB.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void hostNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hostNameFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox domainField;
    private javax.swing.JLabel domainFieldLabel;
    private javax.swing.JLabel explanationLabel;
    private javax.swing.JTextField hostNameField;
    private javax.swing.JLabel hostNameLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField portValueField;
    private javax.swing.JLabel portValueLabel;
    private javax.swing.JRadioButton registerLocalRB;
    private javax.swing.JRadioButton registerRemoteRB;
    private javax.swing.JTextField targetValueField;
    private javax.swing.JLabel targetValueLabel;
    private javax.swing.JCheckBox useDefaultPortsCB;
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
