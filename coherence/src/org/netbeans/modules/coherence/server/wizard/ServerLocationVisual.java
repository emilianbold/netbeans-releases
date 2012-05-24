/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server.wizard;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import org.netbeans.modules.coherence.server.CoherenceModuleProperties;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.util.ClasspathTable;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Panel for set up base Coherence server properties.
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 * @author Martin Fousek <mafous@netbeans.org>
 */
public class ServerLocationVisual extends javax.swing.JPanel implements ChangeListener {

    private static final String COHERENCE_LIB_PATH = File.separator + CoherenceProperties.PLATFORM_LIB_DIR + File.separator;
    private static final String COHERENCE_JAR_PATH = COHERENCE_LIB_PATH + "coherence.jar"; //NOI18N

    private String classpath = "";

    private static JFileChooser fileChooser;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new form ServerLocationVisual.
     */
    public ServerLocationVisual() {
        initComponents();
        initListeners();
    }

    public String getServerName() {
        return serverLocationTextField.getText();
    }

    private String validCoherenceServerDirectory(File directory) {
        boolean libDir = false, binDir = false;
        for (File file : directory.listFiles()) {
            if (file.getName().equals(CoherenceProperties.PLATFORM_BIN_DIR)) {
                binDir = true;
            } else if (file.getName().equals(CoherenceProperties.PLATFORM_LIB_DIR)) {
                libDir = true;
            }
        }

        // one of mandatory directories wasn't found
        if (!libDir || !binDir) {
            return NbBundle.getMessage(ServerLocationVisual.class, "LBL_NotValidCoherencePlatformDir"); //NOI18N
        }

        // coherence.jar was not found inside library directory
        if (!new File(directory, COHERENCE_JAR_PATH).exists()) {
            return NbBundle.getMessage(ServerLocationVisual.class, "LBL_CoherenceJarNotFoundInPlatform"); //NOI18N
        }
        return null;
    }

    public boolean valid(WizardDescriptor wizardDescriptor) {
        if (serverLocationTextField.getText().isEmpty()) {
            return false;
        } else {
            File file = new File(serverLocationTextField.getText());
            if (!file.exists()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerLocationVisual.class, "ERR_CoherenceDirNotExists")); //NOI18N
                return false;
            } else if (!file.isDirectory()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerLocationVisual.class, "ERR_CoherenceDirNotDir")); //NOI18N
                return false;
            } else if (!file.canRead()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerLocationVisual.class, "ERR_CoherenceDirNotReadable")); //NOI18N
                return false;
            } else if (validCoherenceServerDirectory(file) != null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        validCoherenceServerDirectory(file));
                return false;
            }
        }

        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }

    private void initListeners() {
        // document listener for serverLocationTextField
        serverLocationTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireChange();
            }

            private void fireChange() {
                fireChangeEvent();
                ((ClasspathTable) additionalCPTable).refreshClasspathEntries(getServerLocation());
            }
        });

        // change listener for classpath items
        ((ClasspathTable) additionalCPTable).addChangeListener(this);

        // action listeners for checkboxes
        createLibraryCheckBox.addItemListener(new CheckBoxItemListener());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    private class CheckBoxItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChangeEvent();
        }
    }

    /**
     * Adds a listener
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes a registered listener
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Shows the filechooser set to currently selected directory or to the
     * default system root if the directory is invalid
     */
    private void showFileChooser() {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        // set the chooser's properties
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.exists() && f.isDirectory()) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(ServerLocationVisual.class, "DESC_ServerLocationFileFilter"); //NOI18N
            }
        });
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // set the current directory
        File currentLocation = new File(serverLocationTextField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            fileChooser.setCurrentDirectory(currentLocation.getParentFile());
            fileChooser.setSelectedFile(currentLocation);
        }

        // wait for the user to choose the directory and if he clicked the OK
        // button store the selected directory in the server location field
        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            serverLocationTextField.setText(fileChooser.getSelectedFile().getPath());
            fireChangeEvent();
        }
    }

    private void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public String getClasspath() {
        return classpath;
    }

    public String getServerLocation() {
        return serverLocationTextField.getText();
    }

    public boolean getCreateCoherenceLibrary() {
        return createLibraryCheckBox.isSelected();
    }

    private ClasspathTable.TableModel getTableModel() {
        return ((ClasspathTable) additionalCPTable).getTableModel();
    }

    public void fillInCoherenceClasspath(boolean isCoherenceValid) {
        if (isCoherenceValid) {
            String location = getServerLocation();
            if (location != null && location.trim().length() > 0) {
                StringBuilder classpathSB = new StringBuilder(location).append(COHERENCE_JAR_PATH);
                for (int i = 0; i < additionalCPTable.getRowCount(); i++) {
                    ClasspathTable.TableModelItem item = getTableModel().getItem(i);
                    if (item.getSelected()) {
                        classpathSB.append(CoherenceModuleProperties.CLASSPATH_SEPARATOR).
                                append(location).append(COHERENCE_LIB_PATH).append(item.getName());
                    }
                }
                classpath = classpathSB.toString();
            }
        } else {
            classpath = ""; //NOI18N
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLocationLabel = new javax.swing.JLabel();
        serverLocationTextField = new javax.swing.JTextField();
        serverPropertiesNoticeLabel = new javax.swing.JLabel();
        additionalClasspathPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        additionalCPTable = new ClasspathTable();
        browseButton = new javax.swing.JButton();
        createLibraryCheckBox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "LBL_CoherenceCommonProperties")); // NOI18N

        serverLocationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.serverLocationLabel.mnemonics").charAt(0));
        serverLocationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        serverLocationLabel.setLabelFor(serverLocationTextField);
        serverLocationLabel.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.serverLocationLabel.text")); // NOI18N
        serverLocationLabel.setName("lServerName"); // NOI18N

        serverLocationTextField.setToolTipText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.serverLocationTextField.toolTipText")); // NOI18N
        serverLocationTextField.setName("serverLocationTextField"); // NOI18N

        serverPropertiesNoticeLabel.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.serverPropertiesNoticeLabel.text")); // NOI18N

        additionalClasspathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.additionalClasspathPanel.border.title"))); // NOI18N
        additionalClasspathPanel.setName(""); // NOI18N

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        additionalCPTable.setModel(getTableModel());
        additionalCPTable.setColumnSelectionAllowed(true);
        additionalCPTable.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(additionalCPTable);

        javax.swing.GroupLayout additionalClasspathPanelLayout = new javax.swing.GroupLayout(additionalClasspathPanel);
        additionalClasspathPanel.setLayout(additionalClasspathPanelLayout);
        additionalClasspathPanelLayout.setHorizontalGroup(
            additionalClasspathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        additionalClasspathPanelLayout.setVerticalGroup(
            additionalClasspathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/coherence/server/wizard/Bundle").getString("ServerLocationVisual.browseButtonLabel.mnemonics").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createLibraryCheckBox.setSelected(true);
        createLibraryCheckBox.setText(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.createLibraryCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serverLocationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverLocationTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(serverPropertiesNoticeLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(additionalClasspathPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(createLibraryCheckBox)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverLocationLabel)
                    .addComponent(browseButton)
                    .addComponent(serverLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(additionalClasspathPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serverPropertiesNoticeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(createLibraryCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        additionalClasspathPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerLocationVisual.class, "ServerLocationVisual.additionalClasspathPanel.border.title")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        showFileChooser();
    }//GEN-LAST:event_browseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable additionalCPTable;
    private javax.swing.JPanel additionalClasspathPanel;
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox createLibraryCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel serverLocationLabel;
    private javax.swing.JTextField serverLocationTextField;
    private javax.swing.JLabel serverPropertiesNoticeLabel;
    // End of variables declaration//GEN-END:variables
}
