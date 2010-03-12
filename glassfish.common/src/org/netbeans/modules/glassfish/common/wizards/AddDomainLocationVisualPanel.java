/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
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
        setName(NbBundle.getMessage(AddDomainLocationVisualPanel.class, "TITLE_DomainLocation"));
        //domainField.getEditor().getEditorComponent().addKeyListener(arg0);
    }
    
    void initModels(String gfRoot) {
        // Put the choices into the combo box...
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        File domainsDir = new File(gfRoot, GlassfishInstance.DEFAULT_DOMAINS_FOLDER); // NOI18N
        File candidates[] = domainsDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File dir) {
                File logsDir = new File(dir, "logs");
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
        registerLocalRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostNameField.setEnabled(!registerLocalRB.isSelected());
                portValueField.setEnabled(!registerLocalRB.isSelected());
                domainField.setEnabled(!registerRemoteRB.isSelected());
                fireChangeEvent();
            }
        });
        registerRemoteRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostNameField.setEnabled(!registerLocalRB.isSelected());
                portValueField.setEnabled(!registerLocalRB.isSelected());
                domainField.setEnabled(!registerRemoteRB.isSelected());
                fireChangeEvent();
            }
        });
        hostNameField.addKeyListener(kl);
        portValueField.addKeyListener(kl);
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        registerLocalRB = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        domainFieldLabel = new javax.swing.JLabel();
        domainField = new javax.swing.JComboBox();
        explanationLabel = new javax.swing.JLabel();
        registerRemoteRB = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        hostNameLabel = new javax.swing.JLabel();
        hostNameField = new javax.swing.JTextField();
        portValueLabel = new javax.swing.JLabel();
        portValueField = new javax.swing.JTextField();

        buttonGroup1.add(registerLocalRB);
        registerLocalRB.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerLocalRB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(domainFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainFieldLabel.text")); // NOI18N

        domainField.setEditable(true);
        domainField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        explanationLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.explanationLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, explanationLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(domainFieldLabel)
                        .add(4, 4, 4)
                        .add(domainField, 0, 342, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(domainFieldLabel)
                    .add(domainField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(explanationLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        buttonGroup1.add(registerRemoteRB);
        registerRemoteRB.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.registerRemoteRB.text")); // NOI18N

        hostNameLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostNameLabel.text")); // NOI18N

        hostNameField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostNameField.text")); // NOI18N

        portValueLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.portValueLabel.text")); // NOI18N

        portValueField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.portValueField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .add(hostNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(hostNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(portValueLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(portValueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(hostNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portValueLabel)
                    .add(portValueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(registerLocalRB)
                .addContainerGap(263, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(registerRemoteRB)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(registerLocalRB)
                .add(2, 2, 2)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(registerRemoteRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


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
