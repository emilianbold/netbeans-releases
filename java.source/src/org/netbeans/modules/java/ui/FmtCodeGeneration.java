/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.java.ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.Preferences;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;

import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import static org.netbeans.modules.java.ui.FmtOptions.*;
import org.netbeans.modules.java.ui.FmtOptions.CategorySupport;
import static org.netbeans.modules.java.ui.FmtOptions.CategorySupport.OPTION_ID;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/**
 *
 * @author Petr Hrebejk, Dusan Balek
 */
public class FmtCodeGeneration extends javax.swing.JPanel implements Runnable, ListSelectionListener {
    
    /** Creates new form FmtCodeGeneration */
    public FmtCodeGeneration() {
        initComponents();
        
        preferLongerNamesCheckBox.putClientProperty(OPTION_ID, preferLongerNames);
        isForBooleanGettersCheckBox.putClientProperty(OPTION_ID, useIsForBooleanGetters);
        fieldPrefixField.putClientProperty(OPTION_ID, fieldNamePrefix);
        fieldSuffixField.putClientProperty(OPTION_ID, fieldNameSuffix);
        staticFieldPrefixField.putClientProperty(OPTION_ID, staticFieldNamePrefix);
        staticFieldSuffixField.putClientProperty(OPTION_ID, staticFieldNameSuffix);
        parameterPrefixField.putClientProperty(OPTION_ID, parameterNamePrefix);
        parameterSuffixField.putClientProperty(OPTION_ID, parameterNameSuffix);
        localVarPrefixField.putClientProperty(OPTION_ID, localVarNamePrefix);
        localVarSuffixField.putClientProperty(OPTION_ID, localVarNameSuffix);
        qualifyFieldAccessCheckBox.putClientProperty(OPTION_ID, qualifyFieldAccess);
        addOverrideAnnortationCheckBox.putClientProperty(OPTION_ID, addOverrideAnnotation);
        parametersFinalCheckBox.putClientProperty(OPTION_ID, makeParametersFinal);
        localVarsFinalCheckBox.putClientProperty(OPTION_ID, makeLocalVarsFinal);
        membersOrderList.putClientProperty(OPTION_ID, classMembersOrder);
        sortByVisibilityCheckBox.putClientProperty(OPTION_ID, sortMembersByVisibility);
        visibilityOrderList.putClientProperty(OPTION_ID, visibilityOrder);
        insertionPointComboBox.putClientProperty(OPTION_ID, classMemberInsertionPoint);
    }
    
    public static PreferencesCustomizer.Factory getController() {
        return new PreferencesCustomizer.Factory() {
            public PreferencesCustomizer create(Preferences preferences) {
                CodeGenCategorySupport support = new CodeGenCategorySupport(preferences, new FmtCodeGeneration());
                ((Runnable)support.panel).run();
                return support;
            }
        };
    }
    
    @Override
    public void run() {
        membersOrderList.setSelectedIndex(0);
        membersOrderList.addListSelectionListener(this);
        enableMembersOrderButtons();
        visibilityOrderList.setSelectedIndex(0);
        visibilityOrderList.addListSelectionListener(this);
        enableVisibilityOrder();
        namingConventionsLabel.setVisible(false);
        preferLongerNamesCheckBox.setVisible(false);
        isForBooleanGettersCheckBox.setVisible(false);
        prefixLabel.setVisible(false);
        suffixLabel.setVisible(false);
        fieldLabel.setVisible(false);
        fieldPrefixField.setVisible(false);
        fieldSuffixField.setVisible(false);
        staticFieldLabel.setVisible(false);
        staticFieldPrefixField.setVisible(false);
        staticFieldSuffixField.setVisible(false);
        parameterLabel.setVisible(false);
        parameterPrefixField.setVisible(false);
        parameterSuffixField.setVisible(false);
        localVarLabel.setVisible(false);
        localVarPrefixField.setVisible(false);
        localVarSuffixField.setVisible(false);
        jSeparator1.setVisible(false);
        otherLabel.setVisible(false);
        qualifyFieldAccessCheckBox.setVisible(false);
        addOverrideAnnortationCheckBox.setVisible(false);
        parametersFinalCheckBox.setVisible(false);
        localVarsFinalCheckBox.setVisible(false);
        jSeparator3.setVisible(false);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == membersOrderList)
            enableMembersOrderButtons();
        else
            enableVisibilityOrder();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        namingConventionsLabel = new javax.swing.JLabel();
        preferLongerNamesCheckBox = new javax.swing.JCheckBox();
        isForBooleanGettersCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        prefixLabel = new javax.swing.JLabel();
        suffixLabel = new javax.swing.JLabel();
        fieldLabel = new javax.swing.JLabel();
        fieldPrefixField = new javax.swing.JTextField();
        fieldSuffixField = new javax.swing.JTextField();
        staticFieldLabel = new javax.swing.JLabel();
        staticFieldPrefixField = new javax.swing.JTextField();
        staticFieldSuffixField = new javax.swing.JTextField();
        parameterLabel = new javax.swing.JLabel();
        parameterPrefixField = new javax.swing.JTextField();
        parameterSuffixField = new javax.swing.JTextField();
        localVarLabel = new javax.swing.JLabel();
        localVarSuffixField = new javax.swing.JTextField();
        localVarPrefixField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        otherLabel = new javax.swing.JLabel();
        qualifyFieldAccessCheckBox = new javax.swing.JCheckBox();
        addOverrideAnnortationCheckBox = new javax.swing.JCheckBox();
        parametersFinalCheckBox = new javax.swing.JCheckBox();
        localVarsFinalCheckBox = new javax.swing.JCheckBox();
        memberOrderLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        membersOrderList = new javax.swing.JList();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        sortByVisibilityCheckBox = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        visibilityOrderList = new javax.swing.JList();
        visUpButton = new javax.swing.JButton();
        visDownButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        insertionPointLabel = new javax.swing.JLabel();
        insertionPointComboBox = new javax.swing.JComboBox();

        setName(org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_CodeGeneration")); // NOI18N
        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(namingConventionsLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_Naming")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(preferLongerNamesCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_PreferLongerNames")); // NOI18N
        preferLongerNamesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        preferLongerNamesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        preferLongerNamesCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(isForBooleanGettersCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_UseIsForBooleanGetters")); // NOI18N
        isForBooleanGettersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        isForBooleanGettersCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        isForBooleanGettersCheckBox.setOpaque(false);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(prefixLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_Prefix")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 4, 0);
        jPanel1.add(prefixLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(suffixLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_Suffix")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 4, 0);
        jPanel1.add(suffixLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fieldLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_Field")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(fieldLabel, gridBagConstraints);

        fieldPrefixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel1.add(fieldPrefixField, gridBagConstraints);

        fieldSuffixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel1.add(fieldSuffixField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(staticFieldLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_StaticField")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(staticFieldLabel, gridBagConstraints);

        staticFieldPrefixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel1.add(staticFieldPrefixField, gridBagConstraints);

        staticFieldSuffixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel1.add(staticFieldSuffixField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(parameterLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_Parameter")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(parameterLabel, gridBagConstraints);

        parameterPrefixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel1.add(parameterPrefixField, gridBagConstraints);

        parameterSuffixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel1.add(parameterSuffixField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(localVarLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_LocalVariable")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel1.add(localVarLabel, gridBagConstraints);

        localVarSuffixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 0);
        jPanel1.add(localVarSuffixField, gridBagConstraints);

        localVarPrefixField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 0);
        jPanel1.add(localVarPrefixField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(otherLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_Other")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(qualifyFieldAccessCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_QualifyFieldAccess")); // NOI18N
        qualifyFieldAccessCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        qualifyFieldAccessCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        qualifyFieldAccessCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(addOverrideAnnortationCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_AddOverrideAnnotation")); // NOI18N
        addOverrideAnnortationCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addOverrideAnnortationCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addOverrideAnnortationCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(parametersFinalCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_ParametersFinal")); // NOI18N
        parametersFinalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        parametersFinalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        parametersFinalCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(localVarsFinalCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_LocalVariablesFinal")); // NOI18N
        localVarsFinalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localVarsFinalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        localVarsFinalCheckBox.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(memberOrderLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOreder")); // NOI18N

        membersOrderList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        membersOrderList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(membersOrderList);

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederUp")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederDown")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sortByVisibilityCheckBox, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_SortByVisibility")); // NOI18N
        sortByVisibilityCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByVisibilityCheckBoxActionPerformed(evt);
            }
        });

        visibilityOrderList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        visibilityOrderList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(visibilityOrderList);

        org.openide.awt.Mnemonics.setLocalizedText(visUpButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederUp")); // NOI18N
        visUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visUpButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(visDownButton, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_MembersOrederDown")); // NOI18N
        visDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visDownButtonActionPerformed(evt);
            }
        });

        insertionPointLabel.setLabelFor(insertionPointComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(insertionPointLabel, org.openide.util.NbBundle.getMessage(FmtCodeGeneration.class, "LBL_gen_InsertionPoint")); // NOI18N

        insertionPointComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(otherLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(qualifyFieldAccessCheckBox)
                            .addComponent(addOverrideAnnortationCheckBox)
                            .addComponent(parametersFinalCheckBox)
                            .addComponent(localVarsFinalCheckBox))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jSeparator3)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(namingConventionsLabel)
                    .addComponent(memberOrderLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(isForBooleanGettersCheckBox)
                            .addComponent(preferLongerNamesCheckBox)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(downButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(visDownButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(visUpButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addComponent(sortByVisibilityCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(insertionPointLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insertionPointComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(namingConventionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(preferLongerNamesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(isForBooleanGettersCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(otherLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qualifyFieldAccessCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addOverrideAnnortationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parametersFinalCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localVarsFinalCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(memberOrderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sortByVisibilityCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(visUpButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(visDownButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertionPointLabel)
                    .addComponent(insertionPointComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int idx = membersOrderList.getSelectedIndex();
        if (idx > 0) {
            Object val = membersOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).insertElementAt(val, idx - 1);
            membersOrderList.setSelectedIndex(idx - 1);
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int idx = membersOrderList.getSelectedIndex();
        if (idx >= 0 && idx < membersOrderList.getModel().getSize() - 1) {
            Object val = membersOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)membersOrderList.getModel()).insertElementAt(val, idx + 1);
            membersOrderList.setSelectedIndex(idx + 1);
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void visUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visUpButtonActionPerformed
        int idx = visibilityOrderList.getSelectedIndex();
        if (idx > 0) {
            Object val = visibilityOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).insertElementAt(val, idx - 1);
            visibilityOrderList.setSelectedIndex(idx - 1);
        }
    }//GEN-LAST:event_visUpButtonActionPerformed

    private void visDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visDownButtonActionPerformed
        int idx = visibilityOrderList.getSelectedIndex();
        if (idx >= 0 && idx < visibilityOrderList.getModel().getSize() - 1) {
            Object val = visibilityOrderList.getModel().getElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).removeElementAt(idx);
            ((DefaultListModel)visibilityOrderList.getModel()).insertElementAt(val, idx + 1);
            visibilityOrderList.setSelectedIndex(idx + 1);
        }
    }//GEN-LAST:event_visDownButtonActionPerformed

    private void sortByVisibilityCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByVisibilityCheckBoxActionPerformed
        enableVisibilityOrder();
    }//GEN-LAST:event_sortByVisibilityCheckBoxActionPerformed
    
    private void enableMembersOrderButtons() {
        int idx = membersOrderList.getSelectedIndex();                
        upButton.setEnabled(idx > 0);
        downButton.setEnabled(idx >= 0 && idx < membersOrderList.getModel().getSize() - 1);
    }
    
    private void enableVisibilityOrder() {
        int idx = visibilityOrderList.getSelectedIndex();
        boolean b = sortByVisibilityCheckBox.isSelected();
        visibilityOrderList.setEnabled(b);
        visUpButton.setEnabled(b && idx > 0);
        visDownButton.setEnabled(b && idx >= 0 && idx < visibilityOrderList.getModel().getSize() - 1);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addOverrideAnnortationCheckBox;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel fieldLabel;
    private javax.swing.JTextField fieldPrefixField;
    private javax.swing.JTextField fieldSuffixField;
    private javax.swing.JComboBox insertionPointComboBox;
    private javax.swing.JLabel insertionPointLabel;
    private javax.swing.JCheckBox isForBooleanGettersCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel localVarLabel;
    private javax.swing.JTextField localVarPrefixField;
    private javax.swing.JTextField localVarSuffixField;
    private javax.swing.JCheckBox localVarsFinalCheckBox;
    private javax.swing.JLabel memberOrderLabel;
    private javax.swing.JList membersOrderList;
    private javax.swing.JLabel namingConventionsLabel;
    private javax.swing.JLabel otherLabel;
    private javax.swing.JLabel parameterLabel;
    private javax.swing.JTextField parameterPrefixField;
    private javax.swing.JTextField parameterSuffixField;
    private javax.swing.JCheckBox parametersFinalCheckBox;
    private javax.swing.JCheckBox preferLongerNamesCheckBox;
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JCheckBox qualifyFieldAccessCheckBox;
    private javax.swing.JCheckBox sortByVisibilityCheckBox;
    private javax.swing.JLabel staticFieldLabel;
    private javax.swing.JTextField staticFieldPrefixField;
    private javax.swing.JTextField staticFieldSuffixField;
    private javax.swing.JLabel suffixLabel;
    private javax.swing.JButton upButton;
    private javax.swing.JButton visDownButton;
    private javax.swing.JButton visUpButton;
    private javax.swing.JList visibilityOrderList;
    // End of variables declaration//GEN-END:variables

        private static final class CodeGenCategorySupport extends CategorySupport {
        
        private Source source = null;

        private CodeGenCategorySupport(Preferences preferences, JPanel panel) {
            super(preferences, "code-generation", panel, NbBundle.getMessage(FmtCodeGeneration.class, "SAMPLE_CodeGen"), //NOI18N
                    new String[] { FmtOptions.blankLinesBeforeFields, "1" }); //NOI18N
        }
    
        @Override
        protected void loadListData(JList list, String optionID, Preferences node) {
            DefaultListModel model = new DefaultListModel();
            String value = node.get(optionID, getDefaultAsString(optionID));
            for (String s : value.trim().split("\\s*[,;]\\s*")) { //NOI18N
                if (classMembersOrder.equals(optionID)) {
                    Element e = new Element();
                    if (s.startsWith("STATIC ")) { //NOI18N
                        e.isStatic = true;
                        s = s.substring(7);
                    }
                    e.kind = ElementKind.valueOf(s);
                    model.addElement(e);
                } else {
                    Visibility v = new Visibility();
                    v.kind = s;
                    model.addElement(v);
                }
            }
            list.setModel(model);
        }
        
        @Override
        protected void storeListData(final JList list, final String optionID, final Preferences node) {
            StringBuilder sb = null;
            for (int i = 0; i < list.getModel().getSize(); i++) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(';');
                }
                if (classMembersOrder.equals(optionID)) {
                    Element e = (Element) list.getModel().getElementAt(i);
                    if (e.isStatic)
                        sb.append("STATIC "); //NOI18N
                    sb.append(e.kind.name());
                } else {
                    Visibility v = (Visibility) list.getModel().getElementAt(i);
                    sb.append(v.kind);
                }
            }
            String value = sb != null ? sb.toString() : ""; //NOI18N
            if (getDefaultAsString(optionID).equals(value))
                node.remove(optionID);
            else
                node.put(optionID, value);            
        }

        @Override
        public void refreshPreview() {
            final JEditorPane jep = (JEditorPane) getPreviewComponent();
            try {
                Class.forName(CodeStyle.class.getName(), true, CodeStyle.class.getClassLoader());
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }

            final CodeStyle codeStyle = codeStyleProducer.create(previewPrefs);
            jep.setIgnoreRepaint(true);
            try {
                if (source == null) {
                    FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("org.netbeans.samples.ClassA", "java"); //NOI18N
                    source = Source.create(fo);
                }
                final Document doc = source.getDocument(true);
                if (doc.getLength() > 0) {
                    doc.remove(0, doc.getLength());
                }
                doc.insertString(0, previewText, null);
                doc.putProperty(CodeStyle.class, codeStyle);
                jep.setDocument(doc);
                ModificationResult result = ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                        copy.toPhase(Phase.RESOLVED);
                        TreeMaker tm = copy.getTreeMaker();
                        GeneratorUtilities gu = GeneratorUtilities.get(copy);
                        CompilationUnitTree cut = copy.getCompilationUnit();
                        ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                        VariableTree field = (VariableTree)ct.getMembers().get(1);
                        List<Tree> members = new ArrayList<Tree>();
                        AssignmentTree stat = tm.Assignment(tm.Identifier("name"), tm.Literal("Name")); //NOI18N
                        BlockTree init = tm.Block(Collections.singletonList(tm.ExpressionStatement(stat)), false);
                        members.add(init);
                        members.add(gu.createConstructor(ct, Collections.<VariableTree>emptyList()));
                        members.add(gu.createGetter(field));
                        ModifiersTree mods = tm.Modifiers(EnumSet.of(Modifier.PRIVATE));
                        ClassTree inner = tm.Class(mods, "Inner", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>emptyList()); //NOI18N
                        members.add(inner);
                        mods = tm.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC));
                        ClassTree nested = tm.Class(mods, "Nested", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>emptyList()); //NOI18N
                        members.add(nested);
                        IdentifierTree nestedId = tm.Identifier("Nested"); //NOI18N
                        VariableTree staticField = tm.Variable(mods, "instance", nestedId, null); //NOI18N
                        members.add(staticField);
                        NewClassTree nct = tm.NewClass(null, Collections.<ExpressionTree>emptyList(), nestedId, Collections.<ExpressionTree>emptyList(), null);
                        stat = tm.Assignment(tm.Identifier("instance"), nct); //NOI18N
                        BlockTree staticInit = tm.Block(Collections.singletonList(tm.ExpressionStatement(stat)), true);
                        members.add(staticInit);
                        members.add(gu.createGetter(staticField));
                        ClassTree newCT = gu.insertClassMembers(ct, members);
                        copy.rewrite(ct, newCT);
                    }
                });
                result.commit();
                final Reformat reformat = Reformat.get(doc);
                reformat.lock();
                try {
                    if (doc instanceof BaseDocument) {
                        ((BaseDocument) doc).runAtomicAsUser(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    reformat.reformat(0, doc.getLength());
                                } catch (BadLocationException ble) {}
                            }
                        });
                    } else {
                        reformat.reformat(0, doc.getLength());
                    }
                } finally {
                    reformat.unlock();
                }
                DataObject dataObject = DataObject.find(source.getFileObject());
                SaveCookie sc = dataObject.getLookup().lookup(SaveCookie.class);
                if (sc != null)
                    sc.save();
            } catch (Exception ex) {}
            jep.setIgnoreRepaint(false);
            jep.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
            jep.repaint(100);
        }
        
        private static class Element {
            
            private boolean isStatic;
            private ElementKind kind;

            @Override
            public String toString() {
                return (isStatic ? NbBundle.getMessage(FmtCodeGeneration.class, "VAL_gen_STATIC") + " " : "") //NOI18N
                        + NbBundle.getMessage(FmtCodeGeneration.class, "VAL_gen_" + kind.name()); //NOI18N
            }
        }

        private static class Visibility {
            
            private String kind;

            @Override
            public String toString() {
                return NbBundle.getMessage(FmtCodeGeneration.class, "VAL_gen_" + kind); //NOI18N
            }
        }
    }
}
