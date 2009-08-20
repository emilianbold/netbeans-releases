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

package org.netbeans.modules.php.symfony.ui.wizards;

import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.symfony.SymfonyScript;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public class NewProjectConfigurationPanel extends JPanel {
    private static final long serialVersionUID = -17850189546528594L;
    private static final String DEFAULT_SECRET = "UniqueSecret"; // NOI18N
    private static final String DEFAULT_PARAMS = "--escaping-strategy=on --csrf-secret=" + DEFAULT_SECRET; // NOI18N
    private static final String APP_FRONTEND = "frontend"; // NOI18N
    private static final String APP_BACKEND = "backend"; // NOI18N
    private static final Pattern APP_NAME_PATTERN = Pattern.compile("\\S+"); // NOI18N
    private static final Pattern SECRET_PATTERN = Pattern.compile("\\b" + DEFAULT_SECRET + "\\b"); // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public NewProjectConfigurationPanel() {
        initComponents();
        // work around - keep the label on the right side
        optionsLabel.setMaximumSize(optionsLabel.getPreferredSize());

        frontendParamsTextField.setText(DEFAULT_PARAMS);
        backendParamsTextField.setText(DEFAULT_PARAMS);
        otherParamsTextField.setText(DEFAULT_PARAMS);

        initApp(frontendCheckBox, frontendParamsLabel, frontendParamsTextField, null);
        initApp(backendCheckBox, backendParamsLabel, backendParamsTextField, null);
        initApp(otherCheckBox, otherParamsLabel, otherParamsTextField, otherNameTextField);

        ItemListener defaultItemListener = new DefaultItemListener();
        frontendCheckBox.addItemListener(defaultItemListener);
        backendCheckBox.addItemListener(defaultItemListener);
        otherCheckBox.addItemListener(defaultItemListener);

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        frontendParamsTextField.getDocument().addDocumentListener(defaultDocumentListener);
        backendParamsTextField.getDocument().addDocumentListener(defaultDocumentListener);
        otherNameTextField.getDocument().addDocumentListener(defaultDocumentListener);
        otherParamsTextField.getDocument().addDocumentListener(defaultDocumentListener);

        generateAppLabel.addPropertyChangeListener("enabled", new PropertyChangeListener() { // NOI18N
            public void propertyChange(PropertyChangeEvent evt) {
                enableOptionsLabel();
            }
        });
        enableOptionsLabel();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    // < app name , params >
    public List<Pair<String, String[]>> getApps() {
        List<Pair<String, String[]>> apps = new LinkedList<Pair<String, String[]>>();
        if (frontendCheckBox.isSelected()) {
            apps.add(Pair.of(APP_FRONTEND, Utilities.parseParameters(frontendParamsTextField.getText().trim()))); // NOI18N
        }
        if (backendCheckBox.isSelected()) {
            apps.add(Pair.of(APP_BACKEND, Utilities.parseParameters(backendParamsTextField.getText().trim()))); // NOI18N
        }
        if (otherCheckBox.isSelected()) {
            apps.add(Pair.of(getOtherAppName(), Utilities.parseParameters(otherParamsTextField.getText().trim())));
        }
        return apps;
    }

    public String getErrorMessage() {
        if (otherCheckBox.isSelected()) {
            String otherAppName = getOtherAppName();
            if (!StringUtils.hasText(otherAppName)) {
                return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_NoAppName");
            } else if (!APP_NAME_PATTERN.matcher(otherAppName).matches()) {
                return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_InvalidAppName", otherAppName);
            }
        }
        return null;
    }

    public String getWarningMessage() {
        String warn = null;
        if (frontendCheckBox.isSelected()) {
            warn = validateParams(APP_FRONTEND, frontendParamsTextField);
            if (warn != null) {
                return warn;
            }
        }
        if (backendCheckBox.isSelected()) {
            warn = validateParams(APP_BACKEND, backendParamsTextField);
            if (warn != null) {
                return warn;
            }
        }
        if (otherCheckBox.isSelected()) {
            String otherAppName = getOtherAppName();
            warn = validateParams(otherAppName, otherParamsTextField);
            if (warn != null) {
                return warn;
            }
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void visibleApp(boolean visible, JLabel paramsLabel, JTextField paramsTextField, JTextField nameTextField) {
        paramsLabel.setVisible(visible);
        paramsTextField.setVisible(visible);
        if (nameTextField != null) {
            nameTextField.setVisible(visible);
        }
    }

    void enableOptionsLabel() {
        optionsLabel.setVisible(generateAppLabel.isEnabled());
    }

    private String getOtherAppName() {
        return otherNameTextField.getText().trim();
    }

    private String validateParams(String appName, JTextField paramsTextField) {
        if (SECRET_PATTERN.matcher(paramsTextField.getText()).find()) {
            return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_DefaultParamUsed", DEFAULT_SECRET, appName);
        }
        return null;
    }

    private void initApp(JCheckBox nameCheckBox, final JLabel paramsLabel, final JTextField paramsTextField, final JTextField nameTextField) {
        nameCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                visibleApp(e.getStateChange() == ItemEvent.SELECTED, paramsLabel, paramsTextField, nameTextField);
            }
        });
        visibleApp(nameCheckBox.isSelected(), paramsLabel, paramsTextField, nameTextField);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generateAppLabel = new JLabel();
        optionsLabel = new JLabel();
        frontendCheckBox = new JCheckBox();
        frontendParamsLabel = new JLabel();
        frontendParamsTextField = new JTextField();
        backendCheckBox = new JCheckBox();
        backendParamsLabel = new JLabel();
        backendParamsTextField = new JTextField();
        otherCheckBox = new JCheckBox();
        otherNameTextField = new JTextField();
        otherParamsLabel = new JLabel();
        otherParamsTextField = new JTextField();

        generateAppLabel.setLabelFor(frontendCheckBox);

        Mnemonics.setLocalizedText(generateAppLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateAppLabel.text"));
        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.optionsLabel.text"));
        optionsLabel.setToolTipText(NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.optionsLabel.toolTipText")); // NOI18N
        optionsLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                optionsLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                optionsLabelMousePressed(evt);
            }
        });

        frontendCheckBox.setSelected(true);

        Mnemonics.setLocalizedText(frontendCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendCheckBox.text")); // NOI18N
        frontendParamsLabel.setLabelFor(frontendParamsTextField);


        Mnemonics.setLocalizedText(frontendParamsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendParamsLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(backendCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendCheckBox.text"));
        backendParamsLabel.setLabelFor(backendParamsTextField);


        Mnemonics.setLocalizedText(backendParamsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendParamsLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(otherCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherCheckBox.text"));
        otherParamsLabel.setLabelFor(otherParamsTextField);

        Mnemonics.setLocalizedText(otherParamsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherParamsLabel.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(backendParamsLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(backendParamsTextField, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                    .add(backendCheckBox))
                .add(0, 0, 0))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(otherParamsLabel))
                    .add(otherCheckBox))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(otherParamsTextField, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .add(otherNameTextField, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                .add(0, 0, 0))
            .add(layout.createSequentialGroup()
                .add(generateAppLabel)
                .addPreferredGap(LayoutStyle.RELATED, 132, Short.MAX_VALUE)
                .add(optionsLabel))
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(frontendParamsLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(frontendParamsTextField, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                    .add(frontendCheckBox)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(generateAppLabel)
                    .add(optionsLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(frontendCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(frontendParamsLabel)
                    .add(frontendParamsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(backendCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(backendParamsLabel)
                    .add(backendParamsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(otherCheckBox)
                    .add(otherNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(otherParamsLabel)
                    .add(otherParamsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void optionsLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_optionsLabelMouseEntered

    private void optionsLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMousePressed
        OptionsDisplayer.getDefault().open(SymfonyScript.getOptionsPath());
    }//GEN-LAST:event_optionsLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox backendCheckBox;
    private JLabel backendParamsLabel;
    private JTextField backendParamsTextField;
    private JCheckBox frontendCheckBox;
    private JLabel frontendParamsLabel;
    private JTextField frontendParamsTextField;
    private JLabel generateAppLabel;
    private JLabel optionsLabel;
    private JCheckBox otherCheckBox;
    private JTextField otherNameTextField;
    private JLabel otherParamsLabel;
    private JTextField otherParamsTextField;
    // End of variables declaration//GEN-END:variables

    private final class DefaultItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }
    }

    private final class DefaultDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }
        private void processUpdate() {
            fireChange();
        }
    }
}
