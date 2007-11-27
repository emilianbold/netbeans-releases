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
package org.netbeans.modules.ruby.hints.introduce;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.introduce.IntroduceFieldPanel;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda
 */
public class IntroduceFieldPanel extends javax.swing.JPanel {

    public static final int INIT_METHOD = 1;
    public static final int INIT_FIELD = 2;
    public static final int INIT_CONSTRUCTORS = 4;
    private int[] allowInitMethods;
    private Set<String> takenNames;
    private JButton btnOk;

    public IntroduceFieldPanel(String name, int[] allowInitMethods, int numOccurrences,
            JButton btnOk, Set<String> takenNames) {
        this.btnOk = btnOk;
        this.takenNames = takenNames;
        initComponents();

        this.name.setText(name);
        if (name != null && name.trim().length() > 0) {
            this.name.setCaretPosition(name.length());
            this.name.setSelectionStart(0);
            this.name.setSelectionEnd(name.length());
        }
        this.allowInitMethods = allowInitMethods;
        this.replaceAll.setEnabled(numOccurrences > 1);

        Preferences pref = getPreferences();
        if (numOccurrences == 1) {
            replaceAll.setEnabled(false);
            replaceAll.setSelected(false);
        } else {
            replaceAll.setEnabled(true);
            replaceAll.setText(replaceAll.getText() + " (" + numOccurrences + ")");
            replaceAll.setSelected(pref.getBoolean("replaceAll", true)); //NOI18N
        }

        // Replace All not yet implemented
        replaceAll.setVisible(false);

        boolean allowInitializationLocation = false;
        if (allowInitializationLocation) {
            int init = pref.getInt("initMethod", INIT_METHOD); //NOI18N
            switch (init) {
            case INIT_FIELD:
                initField.setSelected(true);
                break;
            case INIT_CONSTRUCTORS:
                initConstructors.setSelected(true);
                break;
            case INIT_METHOD:
                initMethod.setSelected(true);
                break;
            }

            adjustInitializeIn();
        } else {
            lblInitializeIn.setVisible(false);
            initMethod.setVisible(false);
            initField.setVisible(false);
            initConstructors.setVisible(false);
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(IntroduceFieldPanel.class).node("introduceField"); //NOI18N
    }

    private void adjustInitializeIn() {
        int allowInitMethods = this.allowInitMethods[this.replaceAll.isSelected() ? 1 : 0];

        initMethod.setEnabled((allowInitMethods & INIT_METHOD) != 0);
        initField.setEnabled((allowInitMethods & INIT_FIELD) != 0);
        initConstructors.setEnabled((allowInitMethods & INIT_CONSTRUCTORS) != 0);

        if (!initMethod.isEnabled() && initMethod.isSelected()) {
            if (initField.isEnabled()) {
                initField.setSelected(true);
            } else {
                initConstructors.setSelected(true);
            }
        } else if (!initField.isEnabled() && initField.isSelected()) {
            if (initMethod.isEnabled()) {
                initMethod.setSelected(true);
            } else {
                initConstructors.setSelected(true);
            }
        } else if (!initConstructors.isEnabled() && initConstructors.isSelected()) {
            if (initMethod.isEnabled()) {
                initMethod.setSelected(true);
            } else {
                initField.setSelected(true);
            }
        }
    }

    private JLabel createErrorLabel() {
        ErrorLabel.Validator validator = new ErrorLabel.Validator() {

            public String validate(String text) {
                if (null == text || text.length() == 0) {
                    return "";
                }
                if (!RubyUtils.isValidRubyIdentifier(text)) {
                    return getDefaultErrorMessage(text);
                }

                if (takenNames.contains(text)) {
                    return NbBundle.getMessage(IntroduceFieldPanel.class, "FieldAlreadyExists", text);
                }

                return null;
            }
        };

        final ErrorLabel errorLabel = new ErrorLabel(name.getDocument(), validator);
        errorLabel.addPropertyChangeListener(ErrorLabel.PROP_IS_VALID, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {
                btnOk.setEnabled(errorLabel.isInputTextValid());
            }
        });
        return errorLabel;
    }

    String getDefaultErrorMessage(String inputText) {
        return NbBundle.getMessage(IntroduceFieldPanel.class, "NotValidIdentifier", inputText);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        initilizeIn = new javax.swing.ButtonGroup();
        accessGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        replaceAll = new javax.swing.JCheckBox();
        lblInitializeIn = new javax.swing.JLabel();
        initMethod = new javax.swing.JRadioButton();
        initField = new javax.swing.JRadioButton();
        initConstructors = new javax.swing.JRadioButton();
        errLabel = createErrorLabel();

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(replaceAll, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_ReplaceAll")); // NOI18N
        replaceAll.setMargin(new java.awt.Insets(0, 0, 0, 0));
        replaceAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblInitializeIn, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "IntroduceFieldPanel.lblInitializeIn.text")); // NOI18N

        initilizeIn.add(initMethod);
        initMethod.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(initMethod, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_CurrentMethod")); // NOI18N
        initMethod.setMargin(new java.awt.Insets(0, 0, 0, 0));

        initilizeIn.add(initField);
        org.openide.awt.Mnemonics.setLocalizedText(initField, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Field")); // NOI18N
        initField.setMargin(new java.awt.Insets(0, 0, 0, 0));

        initilizeIn.add(initConstructors);
        org.openide.awt.Mnemonics.setLocalizedText(initConstructors, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Constructors")); // NOI18N
        initConstructors.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(errLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblName)
                        .add(29, 29, 29)
                        .add(name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE))
                    .add(replaceAll)
                    .add(layout.createSequentialGroup()
                        .add(lblInitializeIn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(initField)
                            .add(initMethod)
                            .add(initConstructors))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(replaceAll)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblInitializeIn)
                    .add(layout.createSequentialGroup()
                        .add(initMethod)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(initField)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(initConstructors)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 21, Short.MAX_VALUE)
                .add(errLabel)
                .addContainerGap())
        );

        lblName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Name")); // NOI18N
        replaceAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_ReplaceAllOccurences")); // NOI18N
        initMethod.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_CurrentMethod")); // NOI18N
        initField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Field")); // NOI18N
        initConstructors.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Constructors")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "AD_IntrFld_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void replaceAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllActionPerformed
        adjustInitializeIn();
}//GEN-LAST:event_replaceAllActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JLabel errLabel;
    private javax.swing.JRadioButton initConstructors;
    private javax.swing.JRadioButton initField;
    private javax.swing.JRadioButton initMethod;
    private javax.swing.ButtonGroup initilizeIn;
    private javax.swing.JLabel lblInitializeIn;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    private javax.swing.JCheckBox replaceAll;
    // End of variables declaration//GEN-END:variables
    
    public String getFieldName() {
        if (fieldNameTest != null) return fieldNameTest;
        return this.name.getText();
    }
    
    public int getInitializeIn() {
        if (initializeInTest != null) return initializeInTest;
        int ret;
        if (initMethod.isSelected())
            ret = INIT_METHOD;
        else if (initField.isSelected())
            ret = INIT_FIELD;
        else if (initConstructors.isSelected())
            ret = INIT_CONSTRUCTORS;
        else
            throw new IllegalStateException();
        getPreferences().putInt( "initMethod", ret ); //NOI18N
        return ret;
    }
    
    public boolean isReplaceAll() {
        if (replaceAllTest != null) return replaceAllTest;
        boolean ret = replaceAll.isSelected();
        getPreferences().putBoolean( "replaceAll", ret ); //NOI18N
        return ret;
    }
    
    //For tests:
    private String fieldNameTest;
    private Integer initializeInTest;
    private Boolean replaceAllTest;
    
    void setFieldName(String fieldName) {
        this.fieldNameTest = fieldName;
    }

    void setInitializeIn(Integer initializeIn) {
        this.initializeInTest = initializeIn;
    }

    void setReplaceAll(Boolean replaceAll) {
        this.replaceAllTest = replaceAll;
    }
}
