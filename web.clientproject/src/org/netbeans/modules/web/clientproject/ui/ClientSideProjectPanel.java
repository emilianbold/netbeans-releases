/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.ClientSideConfigurationProvider;
import org.netbeans.modules.web.clientproject.spi.ClientProjectConfiguration;
import org.netbeans.modules.web.clientproject.spi.ConfigUtils;
import org.netbeans.modules.web.clientproject.spi.ProjectConfigurationCustomizer;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class ClientSideProjectPanel extends javax.swing.JPanel {
    
    private Project p;

    private void updateCustomizerPanel(final ClientSideConfigurationProvider configProvider) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                customizerArea.removeAll();
                final ClientProjectConfiguration activeConfiguration = configProvider.getActiveConfiguration();
                if (activeConfiguration != null) {
                    String type = activeConfiguration.getType();
                    Lookup lookup = LookupProviderSupport.createCompositeLookup(Lookups.fixed(p), "Projects/" + ProjectConfigurationCustomizer.PATH + "/"+ type + "/Lookup");
                    ProjectConfigurationCustomizer customizerPanel = lookup.lookup(ProjectConfigurationCustomizer.class);
                    if (customizerPanel != null) {
                        customizerArea.add(customizerPanel.createPanel(activeConfiguration), BorderLayout.CENTER);
                    }
                }
                customizerArea.validate();
                customizerArea.repaint();
            }
        });
    }

    private static class ConfigRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ProjectConfiguration) {
                setText(((ProjectConfiguration) value).getDisplayName());
            }
            return this;
        }
        
    }

    /**
     * Creates new form ClientSideProjectPanel
     */
    public ClientSideProjectPanel(Project p) {
        this.p = p;
        initComponents();
        final ClientSideConfigurationProvider configProvider = p.getLookup().lookup(ClientSideConfigurationProvider.class);
        configCombo.setModel((ComboBoxModel) configProvider);
        configCombo.setRenderer(new ConfigRenderer());
        configProvider.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateCustomizerPanel(configProvider);
            }
        });
        updateCustomizerPanel(configProvider);
    }
    
    private String[] getTypes() {
        FileObject configFile = FileUtil.getConfigFile("Projects/"+ProjectConfigurationCustomizer.PATH);
        if (configFile==null) {
            return new String[0];
        }
        final FileObject[] children = configFile.getChildren();
        final String[] result = new String[children.length];
        for (int i=0; i<children.length; i++) {
            result[i] = children[i].getName();
        }
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        customizerArea = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        seperator = new javax.swing.JSeparator();

        configLabel.setLabelFor(configCombo);
        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(ClientSideProjectPanel.class, "ClientSideProjectPanel.configLabel.text")); // NOI18N

        customizerArea.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(ClientSideProjectPanel.class, "ClientSideProjectPanel.newButton.text")); // NOI18N
        newButton.setEnabled(false);
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(ClientSideProjectPanel.class, "ClientSideProjectPanel.deleteButton.text")); // NOI18N
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(customizerArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(configLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configCombo, 0, 176, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(deleteButton))
            .add(seperator)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(configLabel)
                    .add(configCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(newButton)
                    .add(deleteButton))
                .add(0, 0, 0)
                .add(seperator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(customizerArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        NewConfigurationPanel newPanel = new NewConfigurationPanel("Configuration", getTypes());
        DialogDescriptor dd = new DialogDescriptor(newPanel, "New Configuration");
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (DialogDescriptor.OK_OPTION == result) {
            EditableProperties props = new EditableProperties(true);
            props.put("type", newPanel.getType());
            props.put("display.name", newPanel.getNewName());
            try {
                FileObject conf = ConfigUtils.createConfigFile(p.getProjectDirectory(), newPanel.getType(), props);
                for (ClientProjectConfiguration config : p.getLookup().lookup(ClientSideConfigurationProvider.class).getConfigurations()) {
                    if (conf.getName().equals(config.getName())) {
                        configCombo.setSelectedItem(config);
                        break;
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        try {
            final String dname = ((ClientProjectConfiguration) configCombo.getSelectedItem()).getDisplayName();
            NotifyDescriptor yesNo = new NotifyDescriptor("Are You Sure You Want to Delete " + dname,
                    "Confirm Object Deletion",
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null,
                    null);

            if (DialogDisplayer.getDefault().notify(yesNo) == NotifyDescriptor.YES_OPTION) {
                int i = configCombo.getSelectedIndex();
                final String name = ((ClientProjectConfiguration) configCombo.getSelectedItem()).getName();
                p.getProjectDirectory().getFileObject("nbproject/configs/" + name + ".properties").delete();
                configCombo.setSelectedIndex(i - 1);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    
    }//GEN-LAST:event_deleteButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox configCombo;
    private javax.swing.JLabel configLabel;
    private javax.swing.JPanel customizerArea;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton newButton;
    private javax.swing.JSeparator seperator;
    // End of variables declaration//GEN-END:variables
}
