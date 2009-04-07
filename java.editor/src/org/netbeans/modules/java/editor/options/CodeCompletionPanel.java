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
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.HelpCtx;

/**
 *
 * @author Dusan Balek
 * @author Sam Halliday
 */
public class CodeCompletionPanel extends javax.swing.JPanel implements DocumentListener, TableModelListener {

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

    private final Preferences preferences;

    private void initExcluderTable(JTable table, String pref) {
        String[] entries = pref.split(","); //NOI18N
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (String entry : entries) {
            if (entry.length() > 0) {
                model.addRow(new String[]{entry});
            }
        }
        model.addTableModelListener(this);
    }

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
        initExcluderTable(javaCompletionExcludeTable, blacklist);
        String whitelist = preferences.get(JAVA_COMPLETION_WHITELIST, JAVA_COMPLETION_WHITELIST_DEFAULT);
        initExcluderTable(javaCompletionIncludeTable, whitelist);
        javaAutoCompletionTriggersField.getDocument().addDocumentListener(this);
        javaCompletionSelectorsField.getDocument().addDocumentListener(this);
        javadocAutoCompletionTriggersField.getDocument().addDocumentListener(this);
    }
    
    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCusromizer(preferences);
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

    // allows common excluder buttons to know which table to act on
    private JTable getSelectedExcluderTable() {
        Component selected = javaCompletionExcluderTab.getSelectedComponent();
        if (selected == javaCompletionExcludeScrollPane) {
            return javaCompletionExcludeTable;
        } else if (selected == javaCompletionIncludeScrollPane) {
            return javaCompletionIncludeTable;
        } else {
            throw new RuntimeException(selected.getName());
        }
    }

    // listen to changes in the excluder lists, and do sanity checking on input
    public void tableChanged(TableModelEvent e) {
        DefaultTableModel model = (DefaultTableModel) e.getSource();
        String pref;
        if (model == javaCompletionExcludeTable.getModel()) {
            pref = JAVA_COMPLETION_BLACKLIST;
        } else if (model == javaCompletionIncludeTable.getModel()) {
            pref = JAVA_COMPLETION_WHITELIST;
        } else {
            throw new RuntimeException();
        }
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> data = model.getDataVector();
        Collection<String> entries = new TreeSet<String>();
        for (Vector<String> row : data) {
            String entry = row.elementAt(0);
            if (entry == null) {
                continue;
            }
            // users can enter wildcards, which is the same as the raw prefix
            if (entry.contains("*"))
                entry = entry.replaceAll("\\*", "");
            entry = entry.trim();
            if (entry.length() == 0) {
                continue;
            }
            // this could be checked by a custom editor on input
            if (!entry.matches("[$\\w._]*")) { //NOI18N
                continue;
            }
            entries.add(entry);
        }
        StringBuilder builder = new StringBuilder();
        for (String entry : entries) {
            if (builder.length() > 0) {
                builder.append(","); //NOI18N
            }
            builder.append(entry);
        }
        preferences.put(pref, builder.toString());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javaCompletionExcluderFrame = new javax.swing.JFrame();
        javaCompletionExcluderTab = new javax.swing.JTabbedPane();
        javaCompletionExcludeScrollPane = new javax.swing.JScrollPane();
        javaCompletionExcludeTable = new javax.swing.JTable();
        javaCompletionIncludeScrollPane = new javax.swing.JScrollPane();
        javaCompletionIncludeTable = new javax.swing.JTable();
        javaCompletionExcluderAddButton = new javax.swing.JButton();
        javaCompletionExcluderRemoveButton = new javax.swing.JButton();
        javaCompletionExcluderCloseButton = new javax.swing.JButton();
        javaCompletionPane = new javax.swing.JPanel();
        guessMethodArguments = new javax.swing.JCheckBox();
        javaAutoPopupOnIdentifierPart = new javax.swing.JCheckBox();
        javaAutoCompletionTriggersLabel = new javax.swing.JLabel();
        javaAutoCompletionTriggersField = new javax.swing.JTextField();
        javaCompletionSelectorsLabel = new javax.swing.JLabel();
        javaCompletionSelectorsField = new javax.swing.JTextField();
        javadocAutoCompletionTriggersLabel = new javax.swing.JLabel();
        javadocAutoCompletionTriggersField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        javaCompletionExcluderLabel = new javax.swing.JLabel();
        javaCompletionExcluderButton = new javax.swing.JButton();

        javaCompletionExcluderFrame.setTitle(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderFrame.title")); // NOI18N
        javaCompletionExcluderFrame.setAlwaysOnTop(true);
        javaCompletionExcluderFrame.setMinimumSize(new java.awt.Dimension(409, 233));
        javaCompletionExcluderFrame.setUndecorated(true);

        javaCompletionExcludeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fully Qualified Name prefix"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        javaCompletionExcludeTable.getTableHeader().setReorderingAllowed(false);
        javaCompletionExcludeScrollPane.setViewportView(javaCompletionExcludeTable);
        javaCompletionExcludeTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeTable.columnModel.title0")); // NOI18N

        javaCompletionExcluderTab.addTab(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeScrollPane.TabConstraints.tabTitle"), javaCompletionExcludeScrollPane); // NOI18N

        javaCompletionIncludeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fully Qualified Name prefix"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        javaCompletionIncludeTable.getTableHeader().setReorderingAllowed(false);
        javaCompletionIncludeScrollPane.setViewportView(javaCompletionIncludeTable);
        javaCompletionIncludeTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcludeTable.columnModel.title0")); // NOI18N

        javaCompletionExcluderTab.addTab(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionIncludeScrollPane.TabConstraints.tabTitle"), javaCompletionIncludeScrollPane); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderAddButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderAddButton.text")); // NOI18N
        javaCompletionExcluderAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderAddButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderRemoveButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderRemoveButton.text")); // NOI18N
        javaCompletionExcluderRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderRemoveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderCloseButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderCloseButton.text")); // NOI18N
        javaCompletionExcluderCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderCloseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout javaCompletionExcluderFrameLayout = new org.jdesktop.layout.GroupLayout(javaCompletionExcluderFrame.getContentPane());
        javaCompletionExcluderFrame.getContentPane().setLayout(javaCompletionExcluderFrameLayout);
        javaCompletionExcluderFrameLayout.setHorizontalGroup(
            javaCompletionExcluderFrameLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, javaCompletionExcluderFrameLayout.createSequentialGroup()
                .addContainerGap()
                .add(javaCompletionExcluderTab, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 298, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(javaCompletionExcluderFrameLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(javaCompletionExcluderCloseButton, 0, 0, Short.MAX_VALUE)
                    .add(javaCompletionExcluderRemoveButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(javaCompletionExcluderAddButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(10, Short.MAX_VALUE))
        );
        javaCompletionExcluderFrameLayout.setVerticalGroup(
            javaCompletionExcluderFrameLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionExcluderFrameLayout.createSequentialGroup()
                .add(javaCompletionExcluderFrameLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(javaCompletionExcluderFrameLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(javaCompletionExcluderTab, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(javaCompletionExcluderFrameLayout.createSequentialGroup()
                        .add(59, 59, 59)
                        .add(javaCompletionExcluderAddButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(javaCompletionExcluderRemoveButton)
                        .add(63, 63, 63)
                        .add(javaCompletionExcluderCloseButton)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(javaCompletionExcluderButton, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.javaCompletionExcluderButton.text")); // NOI18N
        javaCompletionExcluderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaCompletionExcluderButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout javaCompletionPaneLayout = new org.jdesktop.layout.GroupLayout(javaCompletionPane);
        javaCompletionPane.setLayout(javaCompletionPaneLayout);
        javaCompletionPaneLayout.setHorizontalGroup(
            javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .add(javaCompletionPaneLayout.createSequentialGroup()
                        .add(javadocAutoCompletionTriggersLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(javadocAutoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
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
                            .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(javaCompletionExcluderLabel)
                                .add(javaCompletionSelectorsLabel))
                            .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(javaCompletionPaneLayout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(javaCompletionSelectorsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, javaCompletionPaneLayout.createSequentialGroup()
                                    .add(23, 23, 23)
                                    .add(javaCompletionExcluderButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .addContainerGap(25, Short.MAX_VALUE)))
        );
        javaCompletionPaneLayout.setVerticalGroup(
            javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionPaneLayout.createSequentialGroup()
                .add(171, 171, 171)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(javadocAutoCompletionTriggersLabel)
                    .add(javadocAutoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
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
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(javaCompletionPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(javaCompletionExcluderButton)
                        .add(javaCompletionExcluderLabel))
                    .addContainerGap(91, Short.MAX_VALUE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javaCompletionPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void javaAutoPopupOnIdentifierPartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaAutoPopupOnIdentifierPartActionPerformed
        preferences.putBoolean(JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, javaAutoPopupOnIdentifierPart.isSelected());
    }//GEN-LAST:event_javaAutoPopupOnIdentifierPartActionPerformed

    private void guessMethodArgumentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guessMethodArgumentsActionPerformed
        preferences.putBoolean(GUESS_METHOD_ARGUMENTS, guessMethodArguments.isSelected());
}//GEN-LAST:event_guessMethodArgumentsActionPerformed

	private void javaCompletionExcluderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderButtonActionPerformed
        if (javaCompletionExcluderFrame.isVisible())
            return;
        javaCompletionExcluderFrame.pack();
        javaCompletionExcluderFrame.setLocationRelativeTo(this);
        javaCompletionExcluderFrame.setVisible(true);
}//GEN-LAST:event_javaCompletionExcluderButtonActionPerformed

	private void javaCompletionExcluderAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderAddButtonActionPerformed
        JTable table = getSelectedExcluderTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rows = model.getRowCount();
        model.setRowCount(rows + 1);
        table.editCellAt(rows, 0);
        table.requestFocus();
}//GEN-LAST:event_javaCompletionExcluderAddButtonActionPerformed

    private void javaCompletionExcluderRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderRemoveButtonActionPerformed
        JTable table = getSelectedExcluderTable();
        int[] rows = table.getSelectedRows();
        if (rows.length == 0)
            return;
        // remove rows in descending order: row numbers change when a row is removed
        Arrays.sort(rows);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int row = rows.length - 1; row >= 0; row--) {
            model.removeRow(rows[row]);
        }
}//GEN-LAST:event_javaCompletionExcluderRemoveButtonActionPerformed

    private void javaCompletionExcluderCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaCompletionExcluderCloseButtonActionPerformed
        javaCompletionExcluderFrame.setVisible(false);
    }//GEN-LAST:event_javaCompletionExcluderCloseButtonActionPerformed

    private void update(DocumentEvent e) {
        if (e.getDocument() == javaAutoCompletionTriggersField.getDocument())
            preferences.put(JAVA_AUTO_COMPLETION_TRIGGERS, javaAutoCompletionTriggersField.getText());
        else if (e.getDocument() == javaCompletionSelectorsField.getDocument())
            preferences.put(JAVA_COMPLETION_SELECTORS, javaCompletionSelectorsField.getText());
        else if (e.getDocument() == javadocAutoCompletionTriggersField.getDocument())
            preferences.put(JAVADOC_AUTO_COMPLETION_TRIGGERS, javadocAutoCompletionTriggersField.getText());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox guessMethodArguments;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField javaAutoCompletionTriggersField;
    private javax.swing.JLabel javaAutoCompletionTriggersLabel;
    private javax.swing.JCheckBox javaAutoPopupOnIdentifierPart;
    private javax.swing.JScrollPane javaCompletionExcludeScrollPane;
    private javax.swing.JTable javaCompletionExcludeTable;
    private javax.swing.JButton javaCompletionExcluderAddButton;
    private javax.swing.JButton javaCompletionExcluderButton;
    private javax.swing.JButton javaCompletionExcluderCloseButton;
    private javax.swing.JFrame javaCompletionExcluderFrame;
    private javax.swing.JLabel javaCompletionExcluderLabel;
    private javax.swing.JButton javaCompletionExcluderRemoveButton;
    private javax.swing.JTabbedPane javaCompletionExcluderTab;
    private javax.swing.JScrollPane javaCompletionIncludeScrollPane;
    private javax.swing.JTable javaCompletionIncludeTable;
    private javax.swing.JPanel javaCompletionPane;
    private javax.swing.JTextField javaCompletionSelectorsField;
    private javax.swing.JLabel javaCompletionSelectorsLabel;
    private javax.swing.JTextField javadocAutoCompletionTriggersField;
    private javax.swing.JLabel javadocAutoCompletionTriggersLabel;
    // End of variables declaration//GEN-END:variables
    
    private static class CodeCompletionPreferencesCusromizer implements PreferencesCustomizer {

        private final Preferences preferences;

        private CodeCompletionPreferencesCusromizer(Preferences p) {
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
