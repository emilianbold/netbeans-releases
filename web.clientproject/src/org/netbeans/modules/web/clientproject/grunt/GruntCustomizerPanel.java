/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.grunt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.indirect.AntProjectHelper;
import org.netbeans.modules.web.clientproject.indirect.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class GruntCustomizerPanel extends javax.swing.JPanel {

    private static final String PROP_CLEAN_ACTION = "grunt.action.clean";
    private static final String PROP_BUILD_ACTION = "grunt.action.build";
    private static final String PROP_REBUILD_ACTION = "grunt.action.rebuild";
    
    GruntCustomizerPanel(final ClientSideProject project, ProjectCustomizer.Category category) {
        initComponents();
        loadFromProperties(project, category);
        category.setStoreListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                storeToProperties(project);
            }

        });
    }
    
    private void setPanelEnabled(boolean enabled) {
        buildCheckBox.setEnabled(enabled);
        rebuildCheckBox.setEnabled(enabled);
        cleanCheckBox.setEnabled(enabled);
        buildTextField.setEnabled(enabled);
        rebuildTextField.setEnabled(enabled);
        cleanTextField.setEnabled(enabled);
        label.setEnabled(enabled);
    }

    private void loadFromProperties(ClientSideProject project, ProjectCustomizer.Category category) {
        PropertyEvaluator eval = project.getEvaluator();
        Preferences prefs = NbPreferences.forModule(GruntCustomizerPanel.class);
        
        String pref = eval.getProperty(PROP_BUILD_ACTION);
        buildCheckBox.setSelected(pref!=null);
        buildTextField.setEnabled(pref!=null);
        buildTextField.setText(pref!=null?pref:prefs.get(PROP_BUILD_ACTION, "build"));//NOI18N
        
        pref = eval.getProperty(PROP_CLEAN_ACTION);
        cleanCheckBox.setSelected(pref!=null);
        cleanTextField.setEnabled(pref!=null);
        cleanTextField.setText(pref!=null?pref:prefs.get(PROP_CLEAN_ACTION, "clean"));//NOI18N

        pref = eval.getProperty(PROP_REBUILD_ACTION);
        rebuildCheckBox.setSelected(pref!=null);
        rebuildTextField.setEnabled(pref!=null);
        rebuildTextField.setText(pref!=null?pref:prefs.get(PROP_REBUILD_ACTION, "clean build"));//NOI18N
        boolean gruntFound = project.getProjectDirectory().getFileObject("Gruntfile.js")!=null;//NOI18N
        if (!gruntFound) {
            setPanelEnabled(false);
        }
        if (!gruntFound) {
            category.setErrorMessage(NbBundle.getMessage(GruntCustomizerPanel.class, "ERR_NoGruntfile"));
        } else {
            category.setErrorMessage(null);
        }
    }

    private void storeToProperties(ClientSideProject project) {
        try {
            EditableProperties projectProperties = project.getProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            
            
            Preferences prefs = NbPreferences.forModule(GruntCustomizerPanel.class);
            
            if (buildCheckBox.isSelected()) {
                projectProperties.put(PROP_BUILD_ACTION, buildTextField.getText());
            } else {
                projectProperties.remove(PROP_BUILD_ACTION);
                prefs.put(PROP_BUILD_ACTION, buildTextField.getText());
            }
            
            if (cleanCheckBox.isSelected()) {
                projectProperties.put(PROP_CLEAN_ACTION, cleanTextField.getText());
            } else {
                projectProperties.remove(PROP_CLEAN_ACTION);
                prefs.put(PROP_CLEAN_ACTION, cleanTextField.getText());
            }
            
            if (rebuildCheckBox.isSelected()) {
                projectProperties.put(PROP_REBUILD_ACTION, rebuildTextField.getText());
            } else {
                projectProperties.remove(PROP_REBUILD_ACTION);
                prefs.put(PROP_REBUILD_ACTION, rebuildTextField.getText());
            }
            
            project.getProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
            prefs.sync();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buildCheckBox = new javax.swing.JCheckBox();
        cleanCheckBox = new javax.swing.JCheckBox();
        rebuildCheckBox = new javax.swing.JCheckBox();
        buildTextField = new javax.swing.JTextField();
        cleanTextField = new javax.swing.JTextField();
        rebuildTextField = new javax.swing.JTextField();
        label = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(buildCheckBox, org.openide.util.NbBundle.getMessage(GruntCustomizerPanel.class, "GruntCustomizerPanel.buildCheckBox.text")); // NOI18N
        buildCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cleanCheckBox, org.openide.util.NbBundle.getMessage(GruntCustomizerPanel.class, "GruntCustomizerPanel.cleanCheckBox.text")); // NOI18N
        cleanCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cleanCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(rebuildCheckBox, org.openide.util.NbBundle.getMessage(GruntCustomizerPanel.class, "GruntCustomizerPanel.rebuildCheckBox.text")); // NOI18N
        rebuildCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rebuildCheckBoxActionPerformed(evt);
            }
        });

        buildTextField.setText(org.openide.util.NbBundle.getMessage(GruntCustomizerPanel.class, "GruntCustomizerPanel.buildTextField.text")); // NOI18N
        buildTextField.setEnabled(false);

        cleanTextField.setText(org.openide.util.NbBundle.getMessage(GruntCustomizerPanel.class, "GruntCustomizerPanel.cleanTextField.text")); // NOI18N
        cleanTextField.setEnabled(false);

        rebuildTextField.setText(org.openide.util.NbBundle.getMessage(GruntCustomizerPanel.class, "GruntCustomizerPanel.rebuildTextField.text")); // NOI18N
        rebuildTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(GruntCustomizerPanel.class, "GruntCustomizerPanel.label.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buildCheckBox)
                    .addComponent(cleanCheckBox)
                    .addComponent(rebuildCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rebuildTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                    .addComponent(buildTextField)
                    .addComponent(cleanTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(label)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buildCheckBox)
                    .addComponent(buildTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cleanCheckBox)
                    .addComponent(cleanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rebuildCheckBox)
                    .addComponent(rebuildTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cleanCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cleanCheckBoxActionPerformed
        cleanTextField.setEnabled(cleanCheckBox.isSelected());
    }//GEN-LAST:event_cleanCheckBoxActionPerformed

    private void buildCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildCheckBoxActionPerformed
        buildTextField.setEnabled(buildCheckBox.isSelected());
    }//GEN-LAST:event_buildCheckBoxActionPerformed

    private void rebuildCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rebuildCheckBoxActionPerformed
        rebuildTextField.setEnabled(rebuildCheckBox.isSelected());
    }//GEN-LAST:event_rebuildCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox buildCheckBox;
    private javax.swing.JTextField buildTextField;
    private javax.swing.JCheckBox cleanCheckBox;
    private javax.swing.JTextField cleanTextField;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox rebuildCheckBox;
    private javax.swing.JTextField rebuildTextField;
    // End of variables declaration//GEN-END:variables
}
