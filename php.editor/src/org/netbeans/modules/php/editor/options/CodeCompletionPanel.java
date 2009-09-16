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
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CodeCompletionPanel extends JPanel {
    private static final long serialVersionUID = -24730122182427272L;

    public static enum CodeCompletionType {
        SMART,
        QUALIFIED,
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

    static final String PHP_CODE_COMPLETION_TYPE = "phpCodeCompletionType"; // NOI18N
    static final String PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS = "phpCodeCompletionProvideStaticMethods"; // NOI18N
    static final String PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS = "phpCodeCompletionProvideNonStaticMethods"; // NOI18N

    // default values
    static final boolean PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS_DEFAULT = true;
    static final boolean PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS_DEFAULT = false;

    private final Preferences preferences;

    public CodeCompletionPanel(Preferences preferences) {
        assert preferences != null;

        this.preferences = preferences;

        initComponents();

        initCodeCompletionType();
        initCodeCompletionForMethods();
    }

    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCustomizer(preferences);
            }
        };
    }

    private void initCodeCompletionType() {
        CodeCompletionType type = CodeCompletionType.resolve(preferences.get(PHP_CODE_COMPLETION_TYPE, null));
        switch (type) {
            case SMART:
                smartRadioButton.setSelected(true);
                break;
            case QUALIFIED:
                qualifiedRadioButton.setSelected(true);
                break;
            case UNQUALIFIED:
                unqualifiedRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown code completion type: " + type);
        }

        ItemListener defaultRadioButtonListener = new DefaultRadioButtonListener();
        smartRadioButton.addItemListener(defaultRadioButtonListener);
        qualifiedRadioButton.addItemListener(defaultRadioButtonListener);
        unqualifiedRadioButton.addItemListener(defaultRadioButtonListener);
    }

    private void initCodeCompletionForMethods() {
        boolean provideStaticMethods = preferences.getBoolean(
                PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS,
                PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS_DEFAULT);
        provideStaticMethodsCheckBox.setSelected(provideStaticMethods);

        boolean provideNonStaticMethods = preferences.getBoolean(
                PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS,
                PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS_DEFAULT);
        provideNonStaticMethodsCheckBox.setSelected(provideNonStaticMethods);

        ItemListener defaultCheckBoxListener = new DefaultCheckBoxListener();
        provideStaticMethodsCheckBox.addItemListener(defaultCheckBoxListener);
        provideNonStaticMethodsCheckBox.addItemListener(defaultCheckBoxListener);
    }

    void validateData() {
        CodeCompletionType type = null;
        if (smartRadioButton.isSelected()) {
            type = CodeCompletionType.SMART;
        } else if (qualifiedRadioButton.isSelected()) {
            type = CodeCompletionType.QUALIFIED;
        } else if (unqualifiedRadioButton.isSelected()) {
            type = CodeCompletionType.UNQUALIFIED;
        }
        assert type != null;
        preferences.put(PHP_CODE_COMPLETION_TYPE, type.name());
        preferences.putBoolean(PHP_CODE_COMPLETION_PROVIDE_STATIC_METHODS, provideStaticMethodsCheckBox.isSelected());
        preferences.putBoolean(PHP_CODE_COMPLETION_PROVIDE_NON_STATIC_METHODS, provideNonStaticMethodsCheckBox.isSelected());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codeCompletionButtonGroup = new ButtonGroup();
        codeCompletionTypeLabel = new JLabel();
        smartRadioButton = new JRadioButton();
        smartInfoLabel = new JLabel();
        qualifiedRadioButton = new JRadioButton();
        qualifiedInfoLabel = new JLabel();
        unqualifiedRadioButton = new JRadioButton();
        unqualifiedInfoLabel = new JLabel();
        methodCodeCompletionLabel = new JLabel();
        provideStaticMethodsCheckBox = new JCheckBox();
        provideNonStaticMethodsCheckBox = new JCheckBox();

        codeCompletionTypeLabel.setLabelFor(smartRadioButton);

        codeCompletionTypeLabel.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionTypeLabel.text")); // NOI18N
        codeCompletionButtonGroup.add(smartRadioButton);

        smartRadioButton.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartRadioButton.text")); // NOI18N
        smartInfoLabel.setLabelFor(smartRadioButton);
        smartInfoLabel.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartInfoLabel.text")); // NOI18N
        smartInfoLabel.setEnabled(false);

        codeCompletionButtonGroup.add(qualifiedRadioButton);

        qualifiedRadioButton.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.qualifiedRadioButton.text")); // NOI18N
        qualifiedInfoLabel.setLabelFor(qualifiedRadioButton);
        qualifiedInfoLabel.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.qualifiedInfoLabel.text")); // NOI18N
        qualifiedInfoLabel.setEnabled(false);

        codeCompletionButtonGroup.add(unqualifiedRadioButton);

        unqualifiedRadioButton.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedRadioButton.text")); // NOI18N
        unqualifiedInfoLabel.setLabelFor(unqualifiedRadioButton);
        unqualifiedInfoLabel.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedInfoLabel.text")); // NOI18N
        unqualifiedInfoLabel.setEnabled(false);


        methodCodeCompletionLabel.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.methodCodeCompletionLabel.text")); // NOI18N
        provideStaticMethodsCheckBox.setSelected(true);


        provideStaticMethodsCheckBox.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.provideStaticMethodsCheckBox.text")); // NOI18N
        provideNonStaticMethodsCheckBox.setText(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.provideNonStaticMethodsCheckBox.text")); // NOI18N
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(codeCompletionTypeLabel)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(smartInfoLabel))
                            .add(smartRadioButton)
                            .add(qualifiedRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(qualifiedInfoLabel))
                            .add(unqualifiedRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(unqualifiedInfoLabel))))
                    .add(methodCodeCompletionLabel)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(provideNonStaticMethodsCheckBox)
                            .add(provideStaticMethodsCheckBox))))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(codeCompletionTypeLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(smartRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(smartInfoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(qualifiedRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(qualifiedInfoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(unqualifiedRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(unqualifiedInfoLabel)
                .add(18, 18, 18)
                .add(methodCodeCompletionLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(provideStaticMethodsCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(provideNonStaticMethodsCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup codeCompletionButtonGroup;
    private JLabel codeCompletionTypeLabel;
    private JLabel methodCodeCompletionLabel;
    private JCheckBox provideNonStaticMethodsCheckBox;
    private JCheckBox provideStaticMethodsCheckBox;
    private JLabel qualifiedInfoLabel;
    private JRadioButton qualifiedRadioButton;
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
