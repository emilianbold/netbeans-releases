/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.openide.util.NbPreferences;

/**
 * based on org.netbeans.modules.java.hints.introduce.IntroduceVariablePanel
 * @author Jan Lahoda
 * @author Vladimir Voskresensky
 */
public class IntroduceVariablePanel extends javax.swing.JPanel {

    private static final int ACCESS_PUBLIC = 1;
    private static final int ACCESS_PROTECTED = 2;
    private static final int ACCESS_PRIVATE = 4;
    private boolean introduceConstant;
    private JButton btnOk;

    public IntroduceVariablePanel(int numDuplicates, String defaultName, boolean introduceConstant, JButton btnOk) {
        this.btnOk = btnOk;

        initComponents();

        this.introduceConstant = introduceConstant;

        lblAccess.setVisible(introduceConstant);
        accessPublic.setVisible(introduceConstant);
        accessProtected.setVisible(introduceConstant);
        accessPrivate.setVisible(introduceConstant);

        Preferences pref = getPreferences(introduceConstant);
        if (numDuplicates == 1) {
            replaceAll.setEnabled(false);
            replaceAll.setSelected(false);
        } else {
            replaceAll.setEnabled(true);
            replaceAll.setText(replaceAll.getText() + " (" + numDuplicates + ")"); // NOI18N
            replaceAll.setSelected(pref.getBoolean("replaceAll", true)); //NOI18N
        }

        declareFinal.setEnabled(!introduceConstant);
        declareFinal.setSelected(introduceConstant ? true : pref.getBoolean("declareFinal", true)); //NOI18N

        if (!introduceConstant) {
            int accessModifier = pref.getInt("accessModifier", ACCESS_PUBLIC); //NOI18N
            switch (accessModifier) {
                case ACCESS_PUBLIC:
                    accessPublic.setSelected(true);
                    break;
                case ACCESS_PROTECTED:
                    accessProtected.setSelected(true);
                    break;
                case ACCESS_PRIVATE:
                    accessPrivate.setSelected(true);
                    break;
            }
        }
        if (introduceConstant) {
            name.setText(defaultName.toUpperCase());
        } else {
            name.setText(defaultName);
        }
        if (name != null && defaultName.trim().length() > 0) {
            this.name.setCaretPosition(defaultName.length());
            this.name.setSelectionStart(0);
            this.name.setSelectionEnd(defaultName.length());
        }
    }

    private Preferences getPreferences(boolean introduceConstant) {
        return NbPreferences.forModule(IntroduceVariablePanel.class).node(introduceConstant ? "introduceConstant" : "introduceVariable"); //NOI18N
    }

    private JLabel createErrorLabel() {
        ErrorLabel.Validator validator = new ErrorLabel.Validator() {

            public String validate(String text) {
                if (null == text || text.length() == 0) {
                    return ""; // NOI18N
                }
                if (!CndLexerUtilities.isCppIdentifier(text)) {
                    return getDefaultErrorMessage(text);
                }
                return null;
            }
        };

        final ErrorLabel label = new ErrorLabel(name.getDocument(), validator);
        label.addPropertyChangeListener(ErrorLabel.PROP_IS_VALID, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {
                btnOk.setEnabled(label.isInputTextValid());
            }
        });
        return label;
    }

    String getDefaultErrorMessage(String inputText) {
        return "'" + inputText + "' is not a valid identifier"; // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        accessGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        replaceAll = new javax.swing.JCheckBox();
        declareFinal = new javax.swing.JCheckBox();
        lblAccess = new javax.swing.JLabel();
        accessPublic = new javax.swing.JRadioButton();
        accessProtected = new javax.swing.JRadioButton();
        accessPrivate = new javax.swing.JRadioButton();
        errorLabel = createErrorLabel();

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceVariablePanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(replaceAll, org.openide.util.NbBundle.getBundle(IntroduceVariablePanel.class).getString("LBL_ReplaceAll")); // NOI18N
        replaceAll.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(declareFinal, org.openide.util.NbBundle.getBundle(IntroduceVariablePanel.class).getString("LBL_DeclareFinal")); // NOI18N
        declareFinal.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(lblAccess, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_Access")); // NOI18N

        accessGroup.add(accessPublic);
        accessPublic.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accessPublic, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_public")); // NOI18N
        accessPublic.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessProtected);
        org.openide.awt.Mnemonics.setLocalizedText(accessProtected, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_protected")); // NOI18N
        accessProtected.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessPrivate);
        org.openide.awt.Mnemonics.setLocalizedText(accessPrivate, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "LBL_private")); // NOI18N
        accessPrivate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "IntroduceVariablePanel.errorLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                    .add(replaceAll)
                    .add(declareFinal)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblAccess)
                            .add(lblName))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(accessPublic)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(accessProtected)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(accessPrivate)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblAccess)
                    .add(accessPublic)
                    .add(accessProtected)
                    .add(accessPrivate))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(declareFinal)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(replaceAll)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 32, Short.MAX_VALUE)
                .add(errorLabel)
                .addContainerGap())
        );

        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Name")); // NOI18N
        replaceAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_ReplaceAllOccurences")); // NOI18N
        declareFinal.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_DeclareFinal")); // NOI18N
        accessPublic.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Public")); // NOI18N
        accessProtected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Protected")); // NOI18N
        accessPrivate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Private")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceVariablePanel.class, "AD_IntrVar_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JRadioButton accessPrivate;
    private javax.swing.JRadioButton accessProtected;
    private javax.swing.JRadioButton accessPublic;
    private javax.swing.JCheckBox declareFinal;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel lblAccess;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    private javax.swing.JCheckBox replaceAll;
    // End of variables declaration//GEN-END:variables
    private CsmVisibility testAccess;

    public String getVariableName() {
        return name.getText();
    }

    public boolean isReplaceAll() {
        boolean ret = replaceAll.isSelected();
        getPreferences(introduceConstant).putBoolean("replaceAll", ret); //NOI18N
        return ret;
    }

    public boolean isDeclareFinal() {
        boolean ret = declareFinal.isSelected();
        getPreferences(introduceConstant).putBoolean("declareFinal", ret); //NOI18N
        return ret;
    }

    public CsmVisibility getAccess() {
        if (testAccess != null) {
            return testAccess;
        }

        CsmVisibility set;
        int val;
        if (accessPublic.isSelected()) {
            val = ACCESS_PUBLIC;
            set = CsmVisibility.PUBLIC;
        } else if (accessProtected.isSelected()) {
            val = ACCESS_PROTECTED;
            set = CsmVisibility.PROTECTED;
        } else {
            val = ACCESS_PRIVATE;
            set = CsmVisibility.PRIVATE;
        }
        getPreferences(introduceConstant).putInt("accessModifier", val); //NOI18N
        return set;
    }

    //for tests only:
    void setVariableName(String name) {
        this.name.setText(name);
    }

    void setDeclareFinal(boolean declareFinal) {
        this.declareFinal.setSelected(declareFinal);
    }

    void setReplaceAll(boolean replaceAll) {
        this.replaceAll.setSelected(replaceAll);
    }

    void setAccess(CsmVisibility access) {
        testAccess = access;
    }
}
