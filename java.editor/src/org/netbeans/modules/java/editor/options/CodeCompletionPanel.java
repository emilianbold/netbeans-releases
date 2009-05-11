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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.editor.options;

import java.awt.Component;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.HelpCtx;

/**
 *
 * @author Dusan Balek
 * @author Sam Halliday
 */
public class CodeCompletionPanel extends javax.swing.JPanel implements DocumentListener {

    public static final String JAVA_AUTO_POPUP_ON_IDENTIFIER_PART = "javaAutoPopupOnIdentifierPart"; //NOI18N
    public static final boolean JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT = false;
    public static final String JAVA_AUTO_COMPLETION_TRIGGERS = "javaAutoCompletionTriggers"; //NOI18N
    public static final String JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT = "."; //NOI18N
    public static final String JAVA_COMPLETION_SELECTORS = "javaCompletionSelectors"; //NOI18N
    public static final String JAVA_COMPLETION_SELECTORS_DEFAULT = ".,;:([+-="; //NOI18N
    public static final String JAVADOC_AUTO_COMPLETION_TRIGGERS = "javadocAutoCompletionTriggers"; //NOI18N
    public static final String JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT = ".#@"; //NOI18N
    public static final String GUESS_METHOD_ARGUMENTS = "guessMethodArguments"; //NOI18N
    public static final boolean GUESS_METHOD_ARGUMENTS_DEFAULT = true;
    public static final String JAVA_COMPLETION_WHITELIST = "javaCompletionWhitelist"; //NOI18N
    public static final String JAVA_COMPLETION_WHITELIST_DEFAULT = ""; //NOI18N
    public static final String JAVA_COMPLETION_BLACKLIST = "javaCompletionBlacklist"; //NOI18N
    public static final String JAVA_COMPLETION_BLACKLIST_DEFAULT = ""; //NOI18N
    public static final String JAVA_COMPLETION_EXCLUDER_METHODS = "javaCompletionExcluderMethods"; //NOI18N
    public static final boolean JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT = false;

    private static final String JAVA_FQN_REGEX = "[$\\w._]*";

    private final Preferences preferences;

    // null if a new entry is to be created, otherwise the entry to be replaced
    private volatile String javaExcluderEditing;

    /** Creates new form FmtTabsIndents */
    public CodeCompletionPanel(Preferences p) {
        initComponents();
        preferences = p;
        guessMethodArguments.setSelected(preferences.getBoolean(GUESS_METHOD_ARGUMENTS, GUESS_METHOD_ARGUMENTS_DEFAULT));
        javaAutoPopupOnIdentifierPart.setSelected(preferences.getBoolean(JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT));
        javaAutoCompletionTriggersField.setText(preferences.get(JAVA_AUTO_COMPLETION_TRIGGERS, JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT));
        javaCompletionSelectorsField.setText(preferences.get(JAVA_COMPLETION_SELECTORS, JAVA_COMPLETION_SELECTORS_DEFAULT));
        javadocAutoCompletionTriggersField.setText(preferences.get(JAVADOC_AUTO_COMPLETION_TRIGGERS, JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT));        
        String blacklist = preferences.get(JAVA_COMPLETION_BLACKLIST, JAVA_COMPLETION_BLACKLIST_DEFAULT);
        initExcluderList(javaCompletionExcludeJlist, blacklist);
        String whitelist = preferences.get(JAVA_COMPLETION_WHITELIST, JAVA_COMPLETION_WHITELIST_DEFAULT);
        initExcluderList(javaCompletionIncludeJlist, whitelist);
        javaCompletionExcluderMethodsCheckBox.setSelected(preferences.getBoolean(JAVA_COMPLETION_EXCLUDER_METHODS, JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT));
        javaCompletionExcluderDialog2.getRootPane().setDefaultButton(javaCompletionExcluderDialogOkButton);

        javaCompletionExcluderDialog2.pack();
        javaCompletionExcluderDialog2.setLocationRelativeTo(this);

        javaAutoCompletionTriggersField.getDocument().addDocumentListener(this);
        javaCompletionSelectorsField.getDocument().addDocumentListener(this);
        javadocAutoCompletionTriggersField.getDocument().addDocumentListener(this);
    }

    private void initExcluderList(JList jList, String list) {
        DefaultListModel model = new DefaultListModel();
        String [] entries = list.split(","); //NOI18N
        for (String entry : entries){
            if (entry.length() != 0)
                model.addElement(entry);
        }
        jList.setModel(model);
    }

    private void openExcluderEditor() {
        assert !javaCompletionExcluderDialog2.isVisible();
        javaCompletionExcluderDialogTextField.setText(javaExcluderEditing);
        javaCompletionExcluderDialog2.setVisible(true);
        javaCompletionExcluderDialogTextField.requestFocus();
    }
    
    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCustomizer(preferences);
            }
        };
    }
    
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javaCompletionExcluderDialog2 = new javax.swing.JDialog();
        javaCompletionExcluderDialogTextField = new javax.swing.JTextField();
        javaCompletionExcluderDialogOkButton = new javax.swing.JButton();
        javaCompletionExcluderDialogLabel = new javax.swing.JLabel();
        javaCompletionExcluderDialogCancelButton = new javax.swing.JButton();
        javaCompletionPane = new javax.swing.JPanel();
        guessMethodArguments = new javax.swing.JCheckBox();
        javaAutoPopupOnIdentifierPart = new javax.swing.JCheckBox();
        javaAutoCompletionTriggersLabel = new javax.swing.JLabel();
        javaAutoCompletionTriggersField = new javax.swing.JTextField();
        javaCompletionSelectorsLabel = new javax.swing.JLabel();
        javaCompletionSelectorsField = new javax.swing.JTextField();
        javadocAutoCompletionTriggersLabel = new javax.swing.JLabel();
        javadocAutoCompletionTriggersField = new javax.swing.JTextField();
        javaCompletionExcluderTab = new javax.swing.JTabbedPane();
        javaCompletionExcludeScrollPane = new javax.swing.JScrollPane();
        javaCompletionExcludeJlist = new javax.swing.JList();
        javaCompletionIncludeScrollPane = new javax.swing.JScrollPane();
        javaCompletionIncludeJlist = new javax.swing.JList();
        javaCompletionExcluderMethodsCheckBox = new javax.swing.JCheckBox();
        javaCompletionExcluderAddButton = new javax.swing.JButton();
        javaCompletionExcluderRemoveButton = new javax.swing.JButton();
        javaCompletionExcluderEditButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        javaCompletionExcluderLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();

        javaCompletionExcluderDialog2.setTitle(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ExcluderDialogTitle")); // NOI18N
        javaCompletionExcluderDialog2.setModal(true);

        javaCompletionExcluderDialogTextField.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogTextField.text")); // NOI18N
        javaCompletionExcluderDialogTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                javaCompletionExcluderDialogTextFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderDialogOkButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogOkButton.text")); // NOI18N
        javaCompletionExcluderDialogOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderDialogOkButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderDialogLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderDialogCancelButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogCancelButton.text")); // NOI18N
        javaCompletionExcluderDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderDialogCancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout javaCompletionExcluderDialog2Layout = new org.jdesktop.layout.GroupLayout(javaCompletionExcluderDialog2.getContentPane());
        javaCompletionExcluderDialog2.getContentPane().setLayout(javaCompletionExcluderDialog2Layout);
        javaCompletionExcluderDialog2Layout.setHorizontalGroup(
            javaCompletionExcluderDialog2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionExcluderDialog2Layout.createSequentialGroup()
                .addContainerGap()
                .add(javaCompletionExcluderDialog2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, javaCompletionExcluderDialogLabel, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, javaCompletionExcluderDialogTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(javaCompletionExcluderDialog2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(javaCompletionExcluderDialogOkButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(javaCompletionExcluderDialogCancelButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        javaCompletionExcluderDialog2Layout.setVerticalGroup(
            javaCompletionExcluderDialog2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, javaCompletionExcluderDialog2Layout.createSequentialGroup()
                .add(javaCompletionExcluderDialog2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(javaCompletionExcluderDialog2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(javaCompletionExcluderDialogLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(javaCompletionExcluderDialog2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(javaCompletionExcluderDialogCancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(javaCompletionExcluderDialog2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(javaCompletionExcluderDialogTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(javaCompletionExcluderDialogOkButton))
                .add(61, 61, 61))
        );

        javaCompletionExcluderDialogTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogTextField.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderDialogTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogTextField.AccessibleContext.accessibleDescription")); // NOI18N
        javaCompletionExcluderDialogOkButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_OKButton")); // NOI18N
        javaCompletionExcluderDialogLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialogLabel.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderDialogCancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_CancelButton")); // NOI18N

        javaCompletionExcluderDialog2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderDialog2.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderDialog2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_PopupDialog")); // NOI18N

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(guessMethodArguments, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_GuessMethodArgs")); // NOI18N
        guessMethodArguments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guessMethodArgumentsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaAutoPopupOnIdentifierPart, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_AutoPopupOnIdentifierPartBox")); // NOI18N
        javaAutoPopupOnIdentifierPart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaAutoPopupOnIdentifierPartActionPerformed(evt);
            }
        });

        javaAutoCompletionTriggersLabel.setLabelFor(javaAutoCompletionTriggersField);
        org.openide.awt.Mnemonics.setLocalizedText(javaAutoCompletionTriggersLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_JavaAutoCompletionTriggers")); // NOI18N

        javaAutoCompletionTriggersField.setAlignmentX(1.0F);

        javaCompletionSelectorsLabel.setLabelFor(javaCompletionSelectorsField);
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionSelectorsLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_JavaCompletionSelectors")); // NOI18N

        javadocAutoCompletionTriggersLabel.setLabelFor(javadocAutoCompletionTriggersField);
        org.openide.awt.Mnemonics.setLocalizedText(javadocAutoCompletionTriggersLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "LBL_JavadocAutoCompletionTriggers")); // NOI18N

        javadocAutoCompletionTriggersField.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javadocAutoCompletionTriggersField.text")); // NOI18N

        javaCompletionExcludeScrollPane.setViewportView(javaCompletionExcludeJlist);
        javaCompletionExcludeJlist.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeJlist.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcludeJlist.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_ExcludeList")); // NOI18N

        javaCompletionExcluderTab.addTab(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeScrollPane.TabConstraints.tabTitle"), javaCompletionExcludeScrollPane); // NOI18N
        javaCompletionExcludeScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeScrollPane.AccessibleContext.accessibleName")); // NOI18N

        javaCompletionIncludeScrollPane.setViewportView(javaCompletionIncludeJlist);
        javaCompletionIncludeJlist.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionIncludeJlist.AccessibleContext.accessibleName")); // NOI18N

        javaCompletionExcluderTab.addTab(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionIncludeScrollPane.TabConstraints.tabTitle"), javaCompletionIncludeScrollPane); // NOI18N
        javaCompletionIncludeScrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionIncludeScrollPane.AccessibleContext.accessibleName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderMethodsCheckBox, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderMethodsCheckBox.text")); // NOI18N
        javaCompletionExcluderMethodsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderMethodsCheckBoxActionPerformed(evt);
            }
        });

        javaCompletionExcluderAddButton.setMnemonic('A');
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderAddButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderAddButton.text")); // NOI18N
        javaCompletionExcluderAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderAddButtonActionPerformed(evt);
            }
        });

        javaCompletionExcluderRemoveButton.setMnemonic('R');
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderRemoveButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderRemoveButton.text")); // NOI18N
        javaCompletionExcluderRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderRemoveButtonActionPerformed(evt);
            }
        });

        javaCompletionExcluderEditButton.setMnemonic('E');
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderEditButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderEditButton.text")); // NOI18N
        javaCompletionExcluderEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderEditButtonActionPerformed(evt);
            }
        });

        javaCompletionExcluderLabel.setLabelFor(javaCompletionExcluderTab);
        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout javaCompletionPaneLayout = new org.jdesktop.layout.GroupLayout(javaCompletionPane);
        javaCompletionPane.setLayout(javaCompletionPaneLayout);
        javaCompletionPaneLayout.setHorizontalGroup(
            javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, javaCompletionExcluderLabel)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator2)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, javaCompletionPaneLayout.createSequentialGroup()
                            .add(6, 6, 6)
                            .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(javaCompletionExcluderMethodsCheckBox)
                                .add(javaCompletionPaneLayout.createSequentialGroup()
                                    .add(javaCompletionExcluderTab, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 298, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(10, 10, 10)
                                    .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(javaCompletionExcluderAddButton)
                                        .add(javaCompletionExcluderRemoveButton)
                                        .add(javaCompletionExcluderEditButton))))))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 427, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(javaCompletionPaneLayout.createSequentialGroup()
                        .add(javadocAutoCompletionTriggersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(javadocAutoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(javaCompletionPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(guessMethodArguments)
                        .add(javaCompletionPaneLayout.createSequentialGroup()
                            .add(javaAutoCompletionTriggersLabel)
                            .add(34, 34, 34)
                            .add(javaAutoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(javaAutoPopupOnIdentifierPart)
                        .add(javaCompletionPaneLayout.createSequentialGroup()
                            .add(javaCompletionSelectorsLabel)
                            .add(30, 30, 30)
                            .add(javaCompletionSelectorsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(213, 213, 213)))
        );

        javaCompletionPaneLayout.linkSize(new java.awt.Component[] {javaCompletionExcluderAddButton, javaCompletionExcluderEditButton, javaCompletionExcluderRemoveButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        javaCompletionPaneLayout.setVerticalGroup(
            javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionPaneLayout.createSequentialGroup()
                .add(132, 132, 132)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(javaCompletionPaneLayout.createSequentialGroup()
                        .add(javaCompletionExcluderLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(javaCompletionExcluderTab, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(javaCompletionExcluderMethodsCheckBox))
                    .add(javaCompletionPaneLayout.createSequentialGroup()
                        .add(35, 35, 35)
                        .add(javaCompletionExcluderAddButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(javaCompletionExcluderRemoveButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(javaCompletionExcluderEditButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(javadocAutoCompletionTriggersLabel)
                    .add(javadocAutoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(javaCompletionPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(guessMethodArguments, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(javaAutoCompletionTriggersLabel)
                        .add(javaAutoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(javaAutoPopupOnIdentifierPart)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(javaCompletionSelectorsLabel)
                        .add(javaCompletionSelectorsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(255, Short.MAX_VALUE)))
        );

        javadocAutoCompletionTriggersField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSN_JavadocTriggers")); // NOI18N
        javadocAutoCompletionTriggersField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_JavadocTrigger")); // NOI18N
        javaCompletionExcluderTab.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderTab.AccessibleContext.accessibleName")); // NOI18N
        javaCompletionExcluderTab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Table")); // NOI18N
        javaCompletionExcluderMethodsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_MethodsCB")); // NOI18N
        javaCompletionExcluderAddButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Add")); // NOI18N
        javaCompletionExcluderRemoveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Remove")); // NOI18N
        javaCompletionExcluderEditButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "ACSD_Edit")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(javaCompletionPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void javaAutoPopupOnIdentifierPartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaAutoPopupOnIdentifierPartActionPerformed
        preferences.putBoolean(JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, javaAutoPopupOnIdentifierPart.isSelected());
    }//GEN-LAST:event_javaAutoPopupOnIdentifierPartActionPerformed

    private void guessMethodArgumentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guessMethodArgumentsActionPerformed
        preferences.putBoolean(GUESS_METHOD_ARGUMENTS, guessMethodArguments.isSelected());
}//GEN-LAST:event_guessMethodArgumentsActionPerformed

	private void javaCompletionExcluderAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderAddButtonActionPerformed
        openExcluderEditor();
}//GEN-LAST:event_javaCompletionExcluderAddButtonActionPerformed

    private void javaCompletionExcluderRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderRemoveButtonActionPerformed
        JList list = getSelectedExcluderList();
        int[] rows = list.getSelectedIndices();
        DefaultListModel model = (DefaultListModel) list.getModel();
        // remove rows in descending order: row numbers change when a row is removed
        for (int row = rows.length - 1; row >= 0; row--) {
            model.remove(rows[row]);
        }
        updateExcluder(list);
}//GEN-LAST:event_javaCompletionExcluderRemoveButtonActionPerformed

    private void javaCompletionExcluderEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderEditButtonActionPerformed
        JList list = getSelectedExcluderList();
        int index = list.getSelectedIndex();
        if (index == -1)
            return;
        DefaultListModel model = (DefaultListModel) list.getModel();
        javaExcluderEditing = (String) model.getElementAt(index);
        openExcluderEditor();
}//GEN-LAST:event_javaCompletionExcluderEditButtonActionPerformed

    private void javaCompletionExcluderMethodsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderMethodsCheckBoxActionPerformed
        preferences.putBoolean(JAVA_COMPLETION_EXCLUDER_METHODS, javaCompletionExcluderMethodsCheckBox.isSelected());
}//GEN-LAST:event_javaCompletionExcluderMethodsCheckBoxActionPerformed

    private void javaCompletionExcluderDialogOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderDialogOkButtonActionPerformed
        JList list = getSelectedExcluderList();
        String text = javaCompletionExcluderDialogTextField.getText();
        DefaultListModel model = (DefaultListModel) list.getModel();
        int index = model.size();
        if (javaExcluderEditing != null){
            // if this was an "edit" rather than "add", then remove the old entry first
            index = model.indexOf(javaExcluderEditing);
            model.remove(index);
            javaExcluderEditing = null;
        }
        String[] entries = text.split(","); // NOI18N
        for (String entry : entries) {
            // strip wildcards
            if (entry.contains("*"))  { // NOI18N
                entry = entry.replaceAll("\\*", "");  // NOI18N
            }
            entry = entry.trim();
            if (entry.length() != 0 && entry.matches(JAVA_FQN_REGEX)){
                model.insertElementAt(entry, index);
                index++;
            }
        }
        updateExcluder(list);
        javaCompletionExcluderDialog2.setVisible(false);
        javaCompletionExcluderDialogTextField.setText(null);
    }//GEN-LAST:event_javaCompletionExcluderDialogOkButtonActionPerformed

    private void javaCompletionExcluderDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderDialogCancelButtonActionPerformed
        javaCompletionExcluderDialog2.setVisible(false);
        javaCompletionExcluderDialogTextField.setText(null);
        javaExcluderEditing = null;
    }//GEN-LAST:event_javaCompletionExcluderDialogCancelButtonActionPerformed

    private void javaCompletionExcluderDialogTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_javaCompletionExcluderDialogTextFieldKeyTyped
        char c = evt.getKeyChar();
        // could use javax.lang.model.SourceVersion.isIdentifier if we had Java 6
        if (c != ' ' && c != ',' && c != '*' && !String.valueOf(c).matches(JAVA_FQN_REGEX)) { //NOI18N
            getToolkit().beep();
            evt.consume();
        }
    }//GEN-LAST:event_javaCompletionExcluderDialogTextFieldKeyTyped

    private void update(DocumentEvent e) {
        if (e.getDocument() == javaAutoCompletionTriggersField.getDocument())
            preferences.put(JAVA_AUTO_COMPLETION_TRIGGERS, javaAutoCompletionTriggersField.getText());
        else if (e.getDocument() == javaCompletionSelectorsField.getDocument())
            preferences.put(JAVA_COMPLETION_SELECTORS, javaCompletionSelectorsField.getText());
        else if (e.getDocument() == javadocAutoCompletionTriggersField.getDocument())
            preferences.put(JAVADOC_AUTO_COMPLETION_TRIGGERS, javadocAutoCompletionTriggersField.getText());
    }

    private void updateExcluder(JList list) {
        DefaultListModel model = (DefaultListModel) list.getModel();
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < model.size() ; i++) {
            String entry = (String) model.getElementAt(i);
            if (builder.length() > 0) {
                builder.append(","); //NOI18N
            }
            builder.append(entry);
        }
        String pref;
        if (list == javaCompletionExcludeJlist)
            pref = JAVA_COMPLETION_BLACKLIST;
        else if (list == javaCompletionIncludeJlist)
            pref = JAVA_COMPLETION_WHITELIST;
        else
            throw new RuntimeException(list.getName());

        preferences.put(pref, builder.toString());
    }

    // allows common excluder buttons to know which table to act on
    private JList getSelectedExcluderList() {
        Component selected = javaCompletionExcluderTab.getSelectedComponent();
        if (selected == javaCompletionExcludeScrollPane) {
            return javaCompletionExcludeJlist;
        } else if (selected == javaCompletionIncludeScrollPane) {
            return javaCompletionIncludeJlist;
        } else {
            throw new RuntimeException(selected.getName());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox guessMethodArguments;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField javaAutoCompletionTriggersField;
    private javax.swing.JLabel javaAutoCompletionTriggersLabel;
    private javax.swing.JCheckBox javaAutoPopupOnIdentifierPart;
    private javax.swing.JList javaCompletionExcludeJlist;
    private javax.swing.JScrollPane javaCompletionExcludeScrollPane;
    private javax.swing.JButton javaCompletionExcluderAddButton;
    private javax.swing.JDialog javaCompletionExcluderDialog2;
    private javax.swing.JButton javaCompletionExcluderDialogCancelButton;
    private javax.swing.JLabel javaCompletionExcluderDialogLabel;
    private javax.swing.JButton javaCompletionExcluderDialogOkButton;
    private javax.swing.JTextField javaCompletionExcluderDialogTextField;
    private javax.swing.JButton javaCompletionExcluderEditButton;
    private javax.swing.JLabel javaCompletionExcluderLabel;
    private javax.swing.JCheckBox javaCompletionExcluderMethodsCheckBox;
    private javax.swing.JButton javaCompletionExcluderRemoveButton;
    private javax.swing.JTabbedPane javaCompletionExcluderTab;
    private javax.swing.JList javaCompletionIncludeJlist;
    private javax.swing.JScrollPane javaCompletionIncludeScrollPane;
    private javax.swing.JPanel javaCompletionPane;
    private javax.swing.JTextField javaCompletionSelectorsField;
    private javax.swing.JLabel javaCompletionSelectorsLabel;
    private javax.swing.JTextField javadocAutoCompletionTriggersField;
    private javax.swing.JLabel javadocAutoCompletionTriggersLabel;
    // End of variables declaration//GEN-END:variables
    
    private static class CodeCompletionPreferencesCustomizer implements PreferencesCustomizer {

        private final Preferences preferences;

        private CodeCompletionPreferencesCustomizer(Preferences p) {
            preferences = p;
        }

        public String getId() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx("netbeans.optionsDialog.editor.codeCompletion.java"); //NOI18N
        }

        public JComponent getComponent() {
            return new CodeCompletionPanel(preferences);
        }        
    }
}
