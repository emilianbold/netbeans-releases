/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator.InvalidUrlException;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsLocalWeb extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -53489817846332331L;
    private final PhpProjectProperties properties;
    private final PhpProject project;
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final String displayName;
    final Category category;

    public RunAsLocalWeb(PhpProjectProperties properties, ConfigManager manager, Category category) {
        super(manager);
        this.properties = properties;
        this.category = category;
        project = properties.getProject();
        displayName = NbBundle.getMessage(RunAsLocalWeb.class, "LBL_ConfigLocalWeb");

        initComponents();
        this.labels = new JLabel[] {
            urlLabel,
            indexFileLabel,
            argsLabel
        };
        this.textFields = new JTextField[] {
            urlTextField,
            indexFileTextField,
            argsTextField
        };
        this.propertyNames = new String[] {
            PhpProjectProperties.URL,
            PhpProjectProperties.INDEX_FILE,
            PhpProjectProperties.ARGS
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
    }

    @Override
    protected boolean isDefault() {
        return true;
    }

    @Override
    protected RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.LOCAL;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    public JComboBox getRunAsCombo() {
        return runAsCombo;
    }

    protected void loadFields() {
        for (int i = 0; i < textFields.length; i++) {
            textFields[i].setText(getValue(propertyNames[i]));
        }
    }

    protected void validateFields() {
        String url = urlTextField.getText();
        String indexFile = indexFileTextField.getText();
        String args = argsTextField.getText();

        // #150179 - index file not mandatory
        if (!StringUtils.hasText(indexFile)) {
            indexFile = null;
        }
        String err = RunAsValidator.validateWebFields(url, FileUtil.toFile(getWebRoot()), indexFile, args);
        category.setErrorMessage(err);
        // #148957 always allow to save customizer
        category.setValid(true);
    }

    private FileObject getWebRoot() {
        return ProjectPropertiesSupport.getSourceSubdirectory(project, properties.getWebRoot());
    }

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        protected final String getDefaultValue() {
            return RunAsLocalWeb.this.getDefaultValue(getPropName());
        }

        @Override
        protected void processUpdate() {
            super.processUpdate();
            String hint = ""; // NOI18N
            try {
                hint = RunAsValidator.composeUrlHint(urlTextField.getText(), indexFileTextField.getText(), argsTextField.getText());
            } catch (InvalidUrlException ex) {
                category.setErrorMessage(ex.getMessage());
                category.setValid(false);
            }
            hintLabel.setText(hint);
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

        runAsLabel = new JLabel();
        runAsCombo = new JComboBox();
        urlLabel = new JLabel();
        urlTextField = new JTextField();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        argsLabel = new JLabel();
        argsTextField = new JTextField();
        hintLabel = new JTextArea();
        advancedButton = new JButton();

        setFocusTraversalPolicy(new FocusTraversalPolicy() {



            public Component getDefaultComponent(Container focusCycleRoot){
                return advancedButton;
            }//end getDefaultComponent
            public Component getFirstComponent(Container focusCycleRoot){
                return advancedButton;
            }//end getFirstComponent
            public Component getLastComponent(Container focusCycleRoot){
                return advancedButton;
            }//end getLastComponent
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  runAsCombo){
                    return urlTextField;
                }
                if(aComponent ==  urlTextField){
                    return indexFileTextField;
                }
                if(aComponent ==  indexFileBrowseButton){
                    return argsTextField;
                }
                if(aComponent ==  indexFileTextField){
                    return indexFileBrowseButton;
                }
                if(aComponent ==  argsTextField){
                    return advancedButton;
                }
                return advancedButton;//end getComponentAfter
            }
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  urlTextField){
                    return runAsCombo;
                }
                if(aComponent ==  indexFileTextField){
                    return urlTextField;
                }
                if(aComponent ==  argsTextField){
                    return indexFileBrowseButton;
                }
                if(aComponent ==  indexFileBrowseButton){
                    return indexFileTextField;
                }
                if(aComponent ==  advancedButton){
                    return argsTextField;
                }
                return advancedButton;//end getComponentBefore

            }}
        );

        runAsLabel.setLabelFor(runAsCombo);

        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_RunAs")); // NOI18N
        urlLabel.setLabelFor(urlTextField);

        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_ProjectUrl")); // NOI18N
        indexFileLabel.setLabelFor(indexFileTextField);

        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_IndexFile"));
        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_Browse"));
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        argsLabel.setLabelFor(argsTextField);

        Mnemonics.setLocalizedText(argsLabel, NbBundle.getMessage(RunAsLocalWeb.class, "LBL_Arguments")); // NOI18N
        hintLabel.setEditable(false);
        hintLabel.setLineWrap(true);
        hintLabel.setRows(2);
        hintLabel.setWrapStyleWord(true);
        hintLabel.setBorder(null);
        hintLabel.setDisabledTextColor(UIManager.getDefaults().getColor("Label.disabledForeground"));
        hintLabel.setEnabled(false);
        hintLabel.setOpaque(false);
        Mnemonics.setLocalizedText(advancedButton, NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.advancedButton.text"));
        advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                advancedButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(advancedButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(argsLabel)
                            .add(urlLabel)
                            .add(indexFileLabel)
                            .add(runAsLabel))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(GroupLayout.TRAILING, hintLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(GroupLayout.TRAILING, argsTextField, GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(indexFileTextField, GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(indexFileBrowseButton))
                            .add(GroupLayout.TRAILING, runAsCombo, 0, 220, Short.MAX_VALUE)
                            .add(GroupLayout.TRAILING, urlTextField, GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(runAsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(runAsLabel))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(urlLabel)
                    .add(urlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.CENTER)
                    .add(indexFileBrowseButton)
                    .add(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(indexFileLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.CENTER)
                    .add(argsLabel)
                    .add(argsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(hintLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(advancedButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsCombo.AccessibleContext.accessibleName")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.runAsCombo.AccessibleContext.accessibleDescription")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlTextField.AccessibleContext.accessibleName")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsLabel.AccessibleContext.accessibleName")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsTextField.AccessibleContext.accessibleName")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.argsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.hintLabel.AccessibleContext.accessibleName")); // NOI18N
        hintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.hintLabel.AccessibleContext.accessibleDescription")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.advancedButton.AccessibleContext.accessibleName")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.advancedButton.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsLocalWeb.class, "RunAsLocalWeb.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseFolderFile(PhpVisibilityQuery.forProject(project), getWebRoot(), indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    private void advancedButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_advancedButtonActionPerformed
        RunAsWebAdvanced.Properties props = new RunAsWebAdvanced.Properties(
                getValue(PhpProjectProperties.DEBUG_URL),
                hintLabel.getText(),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL),
                getValue(PhpProjectProperties.DEBUG_PROXY_HOST),
                getValue(PhpProjectProperties.DEBUG_PROXY_PORT));
        RunAsWebAdvanced advanced = new RunAsWebAdvanced(project, props);
        if (advanced.open()) {
            Pair<String, String> pathMapping = advanced.getPathMapping();
            Pair<String, String> debugProxy = advanced.getDebugProxy();
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_URL, advanced.getDebugUrl().name());
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE, pathMapping.first);
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL, pathMapping.second);
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_HOST, debugProxy.first);
            RunAsLocalWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_PORT, debugProxy.second);
        }
    }//GEN-LAST:event_advancedButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton advancedButton;
    private JLabel argsLabel;
    private JTextField argsTextField;
    private JTextArea hintLabel;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JComboBox runAsCombo;
    private JLabel runAsLabel;
    private JLabel urlLabel;
    private JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
