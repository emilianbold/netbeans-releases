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

package org.netbeans.modules.php.editor.options;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CodeCompletionPanel extends JPanel {
    private static final long serialVersionUID = -24730122182427272L;

    public static enum CodeCompletionType {
        SMART,
        FULLY_QUALIFIED,
        UNQUALIFIED;

        public static CodeCompletionType resolve(String value) {
            if (value != null) {
                try {
                    return valueOf(value);
                } catch (IllegalArgumentException ex) {
                    // ignored
                }
            }
            return SMART;
        }
    }

    public static enum VariablesScope {
        ALL,
        CURRENT_FILE;

        public static VariablesScope resolve(String value) {
            if (value != null) {
                try {
                    return valueOf(value);
                } catch (IllegalArgumentException ex) {
                    // ignored
                }
            }
            return ALL;
        }
    }

    static final String PHP_AUTO_COMPLETION_FULL = "phpAutoCompletionFull"; // NOI18N
    static final String PHP_AUTO_COMPLETION_VARIABLES = "phpAutoCompletionVariables"; // NOI18N
    static final String PHP_AUTO_COMPLETION_TYPES = "phpAutoCompletionTypes"; // NOI18N
    static final String PHP_AUTO_COMPLETION_NAMESPACES = "phpAutoCompletionNamespaces"; // NOI18N
    static final String PHP_CODE_COMPLETION_STATIC_METHODS = "phpCodeCompletionStaticMethods"; // NOI18N
    static final String PHP_CODE_COMPLETION_NON_STATIC_METHODS = "phpCodeCompletionNonStaticMethods"; // NOI18N
    static final String PHP_CODE_COMPLETION_VARIABLES_SCOPE = "phpCodeCompletionVariablesScope"; // NOI18N
    static final String PHP_CODE_COMPLETION_TYPE = "phpCodeCompletionType"; // NOI18N

    // default values
    static final boolean PHP_AUTO_COMPLETION_FULL_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_VARIABLES_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_TYPES_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_NAMESPACES_DEFAULT = true;
    static final boolean PHP_CODE_COMPLETION_STATIC_METHODS_DEFAULT = true;
    static final boolean PHP_CODE_COMPLETION_NON_STATIC_METHODS_DEFAULT = false;

    private final Preferences preferences;
    private final ItemListener defaultCheckBoxListener = new DefaultCheckBoxListener();
    private final ItemListener defaultRadioButtonListener = new DefaultRadioButtonListener();

    public CodeCompletionPanel(Preferences preferences) {
        assert preferences != null;

        this.preferences = preferences;

        initComponents();

        initAutoCompletion();
        initCodeCompletionForMethods();
        initCodeCompletionForVariables();
        initCodeCompletionType();
    }

    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCustomizer(preferences);
            }
        };
    }

    private void initAutoCompletion() {
        // full
        autoCompletionFullRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setAutoCompletionState(false);
                }
            }
        });
        autoCompletionCustomizeRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setAutoCompletionState(true);
                }
            }
        });
        boolean autoCompletionFull = preferences.getBoolean(
                PHP_AUTO_COMPLETION_FULL,
                PHP_AUTO_COMPLETION_FULL_DEFAULT);
        if (autoCompletionFull) {
            autoCompletionFullRadioButton.setSelected(true);
        } else {
            autoCompletionCustomizeRadioButton.setSelected(true);
        }
        autoCompletionFullRadioButton.addItemListener(defaultRadioButtonListener);
        autoCompletionCustomizeRadioButton.addItemListener(defaultRadioButtonListener);

        // specific
        boolean autoCompletionVariables = preferences.getBoolean(
                PHP_AUTO_COMPLETION_VARIABLES,
                PHP_AUTO_COMPLETION_VARIABLES_DEFAULT);
        autoCompletionVariablesCheckBox.setSelected(autoCompletionVariables);
        autoCompletionVariablesCheckBox.addItemListener(defaultCheckBoxListener);

        boolean autoCompletionTypes = preferences.getBoolean(
                PHP_AUTO_COMPLETION_TYPES,
                PHP_AUTO_COMPLETION_TYPES_DEFAULT);
        autoCompletionTypesCheckBox.setSelected(autoCompletionTypes);
        autoCompletionTypesCheckBox.addItemListener(defaultCheckBoxListener);

        boolean autoCompletionNamespaces = preferences.getBoolean(
                PHP_AUTO_COMPLETION_NAMESPACES,
                PHP_AUTO_COMPLETION_NAMESPACES_DEFAULT);
        autoCompletionNamespacesCheckBox.setSelected(autoCompletionNamespaces);
        autoCompletionNamespacesCheckBox.addItemListener(defaultCheckBoxListener);
    }

    private void initCodeCompletionForMethods() {
        boolean codeCompletionStaticMethods = preferences.getBoolean(
                PHP_CODE_COMPLETION_STATIC_METHODS,
                PHP_CODE_COMPLETION_STATIC_METHODS_DEFAULT);
        codeCompletionStaticMethodsCheckBox.setSelected(codeCompletionStaticMethods);
        codeCompletionStaticMethodsCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionNonStaticMethods = preferences.getBoolean(
                PHP_CODE_COMPLETION_NON_STATIC_METHODS,
                PHP_CODE_COMPLETION_NON_STATIC_METHODS_DEFAULT);
        codeCompletionNonStaticMethodsCheckBox.setSelected(codeCompletionNonStaticMethods);
        codeCompletionNonStaticMethodsCheckBox.addItemListener(defaultCheckBoxListener);
    }

    private void initCodeCompletionForVariables() {
        VariablesScope variablesScope = VariablesScope.resolve(preferences.get(PHP_CODE_COMPLETION_VARIABLES_SCOPE, null));
        switch (variablesScope) {
            case ALL:
                allVariablesRadioButton.setSelected(true);
                break;
            case CURRENT_FILE:
                currentFileVariablesRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown variables scope: " + variablesScope);
        }
        allVariablesRadioButton.addItemListener(defaultRadioButtonListener);
        currentFileVariablesRadioButton.addItemListener(defaultRadioButtonListener);
    }

    private void initCodeCompletionType() {
        CodeCompletionType type = CodeCompletionType.resolve(preferences.get(PHP_CODE_COMPLETION_TYPE, null));
        switch (type) {
            case SMART:
                smartRadioButton.setSelected(true);
                break;
            case FULLY_QUALIFIED:
                fullyQualifiedRadioButton.setSelected(true);
                break;
            case UNQUALIFIED:
                unqualifiedRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown code completion type: " + type);
        }
        smartRadioButton.addItemListener(defaultRadioButtonListener);
        fullyQualifiedRadioButton.addItemListener(defaultRadioButtonListener);
        unqualifiedRadioButton.addItemListener(defaultRadioButtonListener);
    }

    void validateData() {
        preferences.putBoolean(PHP_AUTO_COMPLETION_FULL, autoCompletionFullRadioButton.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_VARIABLES, autoCompletionVariablesCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_TYPES, autoCompletionTypesCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_NAMESPACES, autoCompletionNamespacesCheckBox.isSelected());

        preferences.putBoolean(PHP_CODE_COMPLETION_STATIC_METHODS, codeCompletionStaticMethodsCheckBox.isSelected());
        preferences.putBoolean(PHP_CODE_COMPLETION_NON_STATIC_METHODS, codeCompletionNonStaticMethodsCheckBox.isSelected());

        VariablesScope variablesScope = null;
        if (allVariablesRadioButton.isSelected()) {
            variablesScope = VariablesScope.ALL;
        } else if (currentFileVariablesRadioButton.isSelected()) {
            variablesScope = VariablesScope.CURRENT_FILE;
        }
        assert variablesScope != null;
        preferences.put(PHP_CODE_COMPLETION_VARIABLES_SCOPE, variablesScope.name());

        CodeCompletionType type = null;
        if (smartRadioButton.isSelected()) {
            type = CodeCompletionType.SMART;
        } else if (fullyQualifiedRadioButton.isSelected()) {
            type = CodeCompletionType.FULLY_QUALIFIED;
        } else if (unqualifiedRadioButton.isSelected()) {
            type = CodeCompletionType.UNQUALIFIED;
        }
        assert type != null;
        preferences.put(PHP_CODE_COMPLETION_TYPE, type.name());
    }

    void setAutoCompletionState(boolean enabled) {
        autoCompletionVariablesCheckBox.setEnabled(enabled);
        autoCompletionTypesCheckBox.setEnabled(enabled);
        autoCompletionNamespacesCheckBox.setEnabled(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codeCompletionTypeButtonGroup = new ButtonGroup();
        codeCompletionVariablesScopeButtonGroup = new ButtonGroup();
        autoCompletionButtonGroup = new ButtonGroup();
        enableAutocompletionLabel = new JLabel();
        autoCompletionFullRadioButton = new JRadioButton();
        autoCompletionCustomizeRadioButton = new JRadioButton();
        autoCompletionVariablesCheckBox = new JCheckBox();
        autoCompletionTypesCheckBox = new JCheckBox();
        autoCompletionNamespacesCheckBox = new JCheckBox();
        methodCodeCompletionLabel = new JLabel();
        codeCompletionStaticMethodsCheckBox = new JCheckBox();
        codeCompletionNonStaticMethodsCheckBox = new JCheckBox();
        codeCompletionVariablesScopeLabel = new JLabel();
        allVariablesRadioButton = new JRadioButton();
        currentFileVariablesRadioButton = new JRadioButton();
        codeCompletionTypeLabel = new JLabel();
        smartRadioButton = new JRadioButton();
        smartInfoLabel = new JLabel();
        fullyQualifiedRadioButton = new JRadioButton();
        fullyQualifiedInfoLabel = new JLabel();
        unqualifiedRadioButton = new JRadioButton();
        unqualifiedInfoLabel = new JLabel();

        Mnemonics.setLocalizedText(enableAutocompletionLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.enableAutocompletionLabel.text")); // NOI18N

        autoCompletionButtonGroup.add(autoCompletionFullRadioButton);
        Mnemonics.setLocalizedText(autoCompletionFullRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionFullRadioButton.text")); // NOI18N

        autoCompletionButtonGroup.add(autoCompletionCustomizeRadioButton);




        Mnemonics.setLocalizedText(autoCompletionCustomizeRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionCustomizeRadioButton.text")); // NOI18N
        Mnemonics.setLocalizedText(autoCompletionVariablesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionVariablesCheckBox.text"));
        Mnemonics.setLocalizedText(autoCompletionTypesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTypesCheckBox.text"));
        Mnemonics.setLocalizedText(autoCompletionNamespacesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionNamespacesCheckBox.text"));
        Mnemonics.setLocalizedText(methodCodeCompletionLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.methodCodeCompletionLabel.text"));

        codeCompletionStaticMethodsCheckBox.setSelected(true);


        Mnemonics.setLocalizedText(codeCompletionStaticMethodsCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionStaticMethodsCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(codeCompletionNonStaticMethodsCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionNonStaticMethodsCheckBox.text"));
        Mnemonics.setLocalizedText(codeCompletionVariablesScopeLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionVariablesScopeLabel.text"));

        codeCompletionVariablesScopeButtonGroup.add(allVariablesRadioButton);
        Mnemonics.setLocalizedText(allVariablesRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.allVariablesRadioButton.text")); // NOI18N

        codeCompletionVariablesScopeButtonGroup.add(currentFileVariablesRadioButton);
        Mnemonics.setLocalizedText(currentFileVariablesRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.currentFileVariablesRadioButton.text")); // NOI18N

        codeCompletionTypeLabel.setLabelFor(smartRadioButton);
        Mnemonics.setLocalizedText(codeCompletionTypeLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionTypeLabel.text")); // NOI18N

        codeCompletionTypeButtonGroup.add(smartRadioButton);
        Mnemonics.setLocalizedText(smartRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartRadioButton.text")); // NOI18N

        smartInfoLabel.setLabelFor(smartRadioButton);
        Mnemonics.setLocalizedText(smartInfoLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartInfoLabel.text")); // NOI18N

        codeCompletionTypeButtonGroup.add(fullyQualifiedRadioButton);
        Mnemonics.setLocalizedText(fullyQualifiedRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedRadioButton.text")); // NOI18N

        fullyQualifiedInfoLabel.setLabelFor(fullyQualifiedRadioButton);
        Mnemonics.setLocalizedText(fullyQualifiedInfoLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedInfoLabel.text")); // NOI18N

        codeCompletionTypeButtonGroup.add(unqualifiedRadioButton);
        Mnemonics.setLocalizedText(unqualifiedRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedRadioButton.text")); // NOI18N

        unqualifiedInfoLabel.setLabelFor(unqualifiedRadioButton);

        Mnemonics.setLocalizedText(unqualifiedInfoLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedInfoLabel.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(autoCompletionCustomizeRadioButton)
                    .add(autoCompletionFullRadioButton)
                    .add(currentFileVariablesRadioButton)
                    .add(allVariablesRadioButton)
                    .add(methodCodeCompletionLabel)
                    .add(codeCompletionNonStaticMethodsCheckBox)
                    .add(codeCompletionStaticMethodsCheckBox)
                    .add(codeCompletionTypeLabel)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(smartInfoLabel))
                    .add(smartRadioButton)
                    .add(fullyQualifiedRadioButton)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(fullyQualifiedInfoLabel))
                    .add(unqualifiedRadioButton)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(unqualifiedInfoLabel))
                    .add(enableAutocompletionLabel)
                    .add(codeCompletionVariablesScopeLabel)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(autoCompletionTypesCheckBox)
                            .add(autoCompletionVariablesCheckBox)
                            .add(autoCompletionNamespacesCheckBox))))
                .addContainerGap(109, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(enableAutocompletionLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(autoCompletionFullRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(autoCompletionCustomizeRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(autoCompletionVariablesCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(autoCompletionTypesCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(autoCompletionNamespacesCheckBox)
                .add(18, 18, 18)
                .add(methodCodeCompletionLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(codeCompletionStaticMethodsCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(codeCompletionNonStaticMethodsCheckBox)
                .add(18, 18, 18)
                .add(codeCompletionVariablesScopeLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(allVariablesRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(currentFileVariablesRadioButton)
                .add(18, 18, 18)
                .add(codeCompletionTypeLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(smartRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(smartInfoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(fullyQualifiedRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(fullyQualifiedInfoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(unqualifiedRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(unqualifiedInfoLabel)
                .addContainerGap(64, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton allVariablesRadioButton;
    private ButtonGroup autoCompletionButtonGroup;
    private JRadioButton autoCompletionCustomizeRadioButton;
    private JRadioButton autoCompletionFullRadioButton;
    private JCheckBox autoCompletionNamespacesCheckBox;
    private JCheckBox autoCompletionTypesCheckBox;
    private JCheckBox autoCompletionVariablesCheckBox;
    private JCheckBox codeCompletionNonStaticMethodsCheckBox;
    private JCheckBox codeCompletionStaticMethodsCheckBox;
    private ButtonGroup codeCompletionTypeButtonGroup;
    private JLabel codeCompletionTypeLabel;
    private ButtonGroup codeCompletionVariablesScopeButtonGroup;
    private JLabel codeCompletionVariablesScopeLabel;
    private JRadioButton currentFileVariablesRadioButton;
    private JLabel enableAutocompletionLabel;
    private JLabel fullyQualifiedInfoLabel;
    private JRadioButton fullyQualifiedRadioButton;
    private JLabel methodCodeCompletionLabel;
    private JLabel smartInfoLabel;
    private JRadioButton smartRadioButton;
    private JLabel unqualifiedInfoLabel;
    private JRadioButton unqualifiedRadioButton;
    // End of variables declaration//GEN-END:variables

    private final class DefaultRadioButtonListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                validateData();
            }
        }
    }

    private final class DefaultCheckBoxListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            validateData();
        }
    }

    static final class CodeCompletionPreferencesCustomizer implements PreferencesCustomizer {

        private final Preferences preferences;

        private CodeCompletionPreferencesCustomizer(Preferences preferences) {
            this.preferences = preferences;
        }

        public String getId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(CodeCompletionPanel.class);
        }

        public JComponent getComponent() {
            return new CodeCompletionPanel(preferences);
        }
    }
}
