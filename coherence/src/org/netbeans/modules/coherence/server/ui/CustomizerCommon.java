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

/*
 * CustomizerCommon.java
 *
 * Created on Jul 27, 2011, 11:55:54 AM
 */
package org.netbeans.modules.coherence.server.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Panel for setup base (common) Coherence instance properties.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CustomizerCommon extends javax.swing.JPanel implements ChangeListener {

    private DefaultListModel listModel;
    private InstanceProperties instanceProperties;
    private ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new {@code CustomizerCommon} panel.
     *
     * @param instanceProperties properties for which will be form initialized
     */
    public CustomizerCommon(InstanceProperties instanceProperties) {
        initComponents();
        this.instanceProperties = instanceProperties;

        init();
    }

    /**
     * Initialization of the panel values.
     */
    private void init(){
        coherenceLocationTextField.setText(instanceProperties.getString(CoherenceProperties.PROP_COHERENCE_LOCATION, ""));
        coherenceClasspathTextField.setText(instanceProperties.getString(CoherenceProperties.PROP_COHERENCE_CLASSPATH, ""));
        javaFlagsTextField.setText(instanceProperties.getString(CoherenceProperties.PROP_JAVA_FLAGS, ""));
        customPropertiesTextField.setText(instanceProperties.getString(CoherenceProperties.PROP_CUSTOM_PROPERTIES, ""));

        listModel = new DefaultListModel();
        for (String cp : additionalClasspathFromStringToArray(instanceProperties.getString(CoherenceProperties.PROP_ADDITIONAL_CLASSPATH, ""))) {
            listModel.addElement(cp);
        }
        additionalClasspathList.setModel(listModel);

        changeSupport.addChangeListener(this);
        coherenceLocationTextField.getDocument().addDocumentListener(new SaveDocumentListener());
        coherenceClasspathTextField.getDocument().addDocumentListener(new SaveDocumentListener());
        javaFlagsTextField.getDocument().addDocumentListener(new SaveDocumentListener());
        customPropertiesTextField.getDocument().addDocumentListener(new SaveDocumentListener());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        savePanel();
    }

    private class SaveDocumentListener implements DocumentListener {

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
            changeSupport.fireChange();
        }
    }

    /**
     * Storing values from this panel into {@link InstanceProperties}.
     */
    private void savePanel() {
        instanceProperties.putString(CoherenceProperties.PROP_COHERENCE_CLASSPATH, coherenceClasspathTextField.getText());
        instanceProperties.putString(CoherenceProperties.PROP_JAVA_FLAGS, javaFlagsTextField.getText());
        instanceProperties.putString(CoherenceProperties.PROP_CUSTOM_PROPERTIES, customPropertiesTextField.getText());

        List<String> cpEntries = new ArrayList<String>();
        for (int i = 0; i < additionalClasspathList.getModel().getSize(); i++) {
            cpEntries.add((String)additionalClasspathList.getModel().getElementAt(i));
        }

        instanceProperties.putString(CoherenceProperties.PROP_ADDITIONAL_CLASSPATH, additionalClasspathFromListToString(cpEntries));
    }

    /**
     * Converts one long string which represents additional classpath into {@code
     * String[]}.
     *
     * @param classpath {@code String} consists from all classpaths
     * @return resulting {@code String[]}
     */
    private static String[] additionalClasspathFromStringToArray(String classpath) {
        return classpath.split(CoherenceProperties.CLASSPATH_SEPARATOR);
    }

    /**
     * Converts {@code List} of {@code Strings} into one long {@code String} for
     * storing that into {@link InstanceProperties}.
     *
     * @param classpaths {@code List} of all classpath entries
     * @return resulting {@code String}
     */
    private static String additionalClasspathFromListToString(List<String> classpaths) {
        StringBuilder sb = new StringBuilder();
        for (String cp : classpaths) {
            sb.append(cp).append(CoherenceProperties.CLASSPATH_SEPARATOR);
        }
        String resultString = sb.toString();
        if (resultString.length() == 0) {
            return ""; //NOI18N
        }
        return resultString.substring(0, resultString.length() - CoherenceProperties.CLASSPATH_SEPARATOR.length());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coherenceClasspathLabel = new javax.swing.JLabel();
        coherenceClasspathTextField = new javax.swing.JTextField();
        additionalClasspathLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        additionalClasspathList = new javax.swing.JList();
        addClasspathButton = new javax.swing.JButton();
        removeClasspathButton = new javax.swing.JButton();
        javaFlagsLabel = new javax.swing.JLabel();
        javaFlagsTextField = new javax.swing.JTextField();
        customPropertiesLabel = new javax.swing.JLabel();
        customPropertiesTextField = new javax.swing.JTextField();
        coherenceLocationTextField = new javax.swing.JTextField();
        coherenceLocationLabel = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "TITLE_Common")); // NOI18N

        coherenceClasspathLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.coherenceClasspathLabel.text")); // NOI18N
        coherenceClasspathLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.coherenceClasspathLabel.desc")); // NOI18N

        coherenceClasspathTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.coherenceClasspathTextField.text")); // NOI18N

        additionalClasspathLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.additionalClasspathLabel.text")); // NOI18N
        additionalClasspathLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.additionalClasspathLabel.desc")); // NOI18N

        additionalClasspathList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(additionalClasspathList);

        addClasspathButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.addClasspathButton.text")); // NOI18N
        addClasspathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClasspathButtonActionPerformed(evt);
            }
        });

        removeClasspathButton.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.removeClasspathButton.text")); // NOI18N
        removeClasspathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClasspathButtonActionPerformed(evt);
            }
        });

        javaFlagsLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.javaFlagsLabel.text")); // NOI18N
        javaFlagsLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.javaFlagsLabel.desc")); // NOI18N

        javaFlagsTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.javaFlagsTextField.text")); // NOI18N

        customPropertiesLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.customPropertiesLabel.text")); // NOI18N
        customPropertiesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CoherenceCommonTab.customPropertiesLabel.desc")); // NOI18N

        customPropertiesTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.customPropertiesTextField.text")); // NOI18N

        coherenceLocationTextField.setEditable(false);

        coherenceLocationLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommon.class, "CustomizerCommon.coherenceLocationLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaFlagsLabel)
                            .addComponent(customPropertiesLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaFlagsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                            .addComponent(customPropertiesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(addClasspathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeClasspathButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(additionalClasspathLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 394, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(coherenceLocationLabel)
                                    .addComponent(coherenceClasspathLabel))
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(coherenceLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                                    .addComponent(coherenceClasspathTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coherenceLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(coherenceLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coherenceClasspathLabel)
                    .addComponent(coherenceClasspathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(additionalClasspathLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addClasspathButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeClasspathButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaFlagsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javaFlagsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customPropertiesLabel)
                    .addComponent(customPropertiesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addClasspathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClasspathButtonActionPerformed
        DialogDescriptor.InputLine inputLine = new InputLine(
                NbBundle.getMessage(CustomizerCommon.class, "LBL_AdditionalClasspathEntry"), //NOI18N
                NbBundle.getMessage(CustomizerCommon.class, "TITLE_AddAdditionalClasspathDialog")); //NOI18N
        DialogDisplayer.getDefault().notify(inputLine);
        if (inputLine.getValue() == DialogDescriptor.OK_OPTION) {
            if (!inputLine.getInputText().isEmpty()) {
                listModel.addElement(inputLine.getInputText());
            }
        }
        changeSupport.fireChange();
    }//GEN-LAST:event_addClasspathButtonActionPerformed

    private void removeClasspathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClasspathButtonActionPerformed
        if (additionalClasspathList.getSelectedIndex() == -1) {
            return;
        }
        listModel.remove(additionalClasspathList.getSelectedIndex());
        changeSupport.fireChange();
    }//GEN-LAST:event_removeClasspathButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClasspathButton;
    private javax.swing.JLabel additionalClasspathLabel;
    private javax.swing.JList additionalClasspathList;
    private javax.swing.JLabel coherenceClasspathLabel;
    private javax.swing.JTextField coherenceClasspathTextField;
    private javax.swing.JLabel coherenceLocationLabel;
    private javax.swing.JTextField coherenceLocationTextField;
    private javax.swing.JLabel customPropertiesLabel;
    private javax.swing.JTextField customPropertiesTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel javaFlagsLabel;
    private javax.swing.JTextField javaFlagsTextField;
    private javax.swing.JButton removeClasspathButton;
    // End of variables declaration//GEN-END:variables
}
