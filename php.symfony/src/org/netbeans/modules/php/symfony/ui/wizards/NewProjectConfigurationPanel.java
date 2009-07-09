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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import org.netbeans.modules.php.api.util.Pair;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public class NewProjectConfigurationPanel extends JPanel {
    private static final long serialVersionUID = -178501081182418594L;
    private static final String DEFAULT_SECRET = "UniqueSecret"; // NOI18N
    private static final String DEFAULT_PARAMS = "--escaping-strategy=on --csrf-secret=" + DEFAULT_SECRET; // NOI18N
    private static final String APP_FRONTEND = "frontend"; // NOI18N
    private static final String APP_BACKEND = "backend"; // NOI18N
    private static final Pattern APP_NAME_PATTERN = Pattern.compile("\\S+"); // NOI18N
    private static final Pattern SECRET_PATTERN = Pattern.compile("\\b" + DEFAULT_SECRET + "\\b"); // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public NewProjectConfigurationPanel() {
        initComponents();

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

    public String validateData() {
        String err = null;
        if (frontendCheckBox.isSelected()) {
            err = validateParams(APP_FRONTEND, frontendParamsTextField);
            if (err != null) {
                return err;
            }
        }
        if (backendCheckBox.isSelected()) {
            err = validateParams(APP_BACKEND, backendParamsTextField);
            if (err != null) {
                return err;
            }
        }
        if (otherCheckBox.isSelected()) {
            String otherAppName = getOtherAppName();
            if (!APP_NAME_PATTERN.matcher(otherAppName).matches()) {
                return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_InvalidAppName", otherAppName);
            }
            err = validateParams(otherAppName, otherParamsTextField);
            if (err != null) {
                return err;
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

        Mnemonics.setLocalizedText(generateAppLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateAppLabel.text")); // NOI18N
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
            .add(generateAppLabel)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(frontendParamsLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(frontendParamsTextField, GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                    .add(frontendCheckBox))
                .add(0, 0, 0))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(backendParamsLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(backendParamsTextField, GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
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
                    .add(otherParamsTextField, GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                    .add(otherNameTextField, GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(generateAppLabel)
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox backendCheckBox;
    private JLabel backendParamsLabel;
    private JTextField backendParamsTextField;
    private JCheckBox frontendCheckBox;
    private JLabel frontendParamsLabel;
    private JTextField frontendParamsTextField;
    private JLabel generateAppLabel;
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
