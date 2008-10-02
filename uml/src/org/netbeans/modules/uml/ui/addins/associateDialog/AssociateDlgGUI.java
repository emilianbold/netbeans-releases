///*
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// *
// * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
// *
// * The contents of this file are subject to the terms of either the GNU
// * General Public License Version 2 only ("GPL") or the Common
// * Development and Distribution License("CDDL") (collectively, the
// * "License"). You may not use this file except in compliance with the
// * License. You can obtain a copy of the License at
// * http://www.netbeans.org/cddl-gplv2.html
// * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
// * specific language governing permissions and limitations under the
// * License.  When distributing the software, include this License Header
// * Notice in each file and include the License file at
// * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
// * particular file as subject to the "Classpath" exception as provided
// * by Sun in the GPL Version 2 section of the License file that
// * accompanied this code. If applicable, add the following below the
// * License Header, with the fields enclosed by brackets [] replaced by
// * your own identifying information:
// * "Portions Copyrighted [year] [name of copyright owner]"
// *
// * Contributor(s):
// *
// * The Original Software is NetBeans. The Initial Developer of the Original
// * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
// * Microsystems, Inc. All Rights Reserved.
// *
// * If you wish your version of this file to be governed by only the CDDL
// * or only the GPL Version 2, indicate your decision by adding
// * "[Contributor] elects to include this software in this distribution
// * under the [CDDL or GPL Version 2] license." If you do not indicate a
// * single choice of license, a recipient has the option to distribute
// * your version of this file under either the CDDL, the GPL Version 2 or
// * to extend the choice of license to its licensees as provided above.
// * However, if you add GPL Version 2 code and therefore, elected the GPL
// * Version 2 license, then the option applies only if the new code is
// * made subject to such option by the copyright holder.
// */
//
///*
// * Created on Mar 5, 2004
// *
// */
//package org.netbeans.modules.uml.ui.addins.associateDialog;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JRadioButton;
//import javax.swing.JScrollPane;
//import java.util.ResourceBundle;
//import java.util.prefs.Preferences;
//import javax.swing.JTextField;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
//import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
//import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
//import org.netbeans.modules.uml.ui.support.finddialog.FindController;
//import org.netbeans.modules.uml.ui.support.finddialog.FindResults;
//import org.netbeans.modules.uml.ui.support.finddialog.FindUtilities;
//import org.netbeans.modules.uml.ui.support.finddialog.IFindResults;
//import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
//import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
//import org.netbeans.modules.uml.ui.swing.finddialog.FindTableModel;
//import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
//import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
//import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
//import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
//import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
//import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
//import org.netbeans.modules.uml.core.metamodel.structure.IProject;
//import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.ui.support.ProductHelper;
//import org.netbeans.modules.uml.util.DummyCorePreference;
//import org.openide.awt.Mnemonics;
//import org.openide.util.NbBundle;
//import org.openide.util.NbPreferences;
//
///**
// * @author jingmingm
// * @author Craig Conover
// *
// */
//public class AssociateDlgGUI extends JCenterDialog implements IAssociateDlgGUI
//{
//    private boolean m_LockIsChecked = false;
//    static ResourceBundle bundle = NbBundle.getBundle(AssociateDlgGUI.class);
//    
//    /** Creates new form finddialog */
//    public AssociateDlgGUI(java.awt.Frame parent, boolean modal)
//    {
//        super(parent, modal);
//        m_Controller = new FindController();
//        initComponents();
//        initTextFieldListeners();
//        selectionListener = new SelectionListener();
//        m_ResultsTable.getSelectionModel().addListSelectionListener(selectionListener);
//        initDialog();
//        pack();  // pack() should come before center(parent)
//        center(parent);
//    }
//    
//    /* Rewriting the initComponent() to properly layout all the compenents
//     * to fix CR 6274568. All the component are now, by default, resized to fit
//     * the larger font texts. No need to reset the font for each label and no
//     * need to manually recalculate the size of the container.
//     */
//    private void initComponents()
//    {
//        java.awt.GridBagConstraints gridBagConstraints;
//        
//        buttonGroup1 = new javax.swing.ButtonGroup();
//        mainPanel = new javax.swing.JPanel();
//        mainSearchPanel = new javax.swing.JPanel();
//        findPanel = new javax.swing.JPanel();
//        findLabel = new javax.swing.JLabel();
//        m_FindCombo = new javax.swing.JComboBox();
//        checkBoxesPanel = new javax.swing.JPanel();
//        m_MatchCaseCheck = new javax.swing.JCheckBox();
//        xPathCheck = new javax.swing.JCheckBox();
//        m_WholeWordCheck = new javax.swing.JCheckBox();
//        m_SearchAliasCheck = new javax.swing.JCheckBox();
//        radioPanel = new javax.swing.JPanel();
//        m_SearchElementsRadio = new javax.swing.JRadioButton();
//        m_SearchDescriptionsRadio = new javax.swing.JRadioButton();
//        buttonPanel = new javax.swing.JPanel();
//        m_FindButton = new javax.swing.JButton();
//        m_CloseButton = new javax.swing.JButton();
//        jPanel11 = new javax.swing.JPanel();
//        jScrollPane2 = new javax.swing.JScrollPane();
//        bottomPanel = new javax.swing.JPanel();
//        m_NavigateCheck = new javax.swing.JCheckBox();
//        m_Status = new javax.swing.JLabel();
//        associateButtonPanel = new javax.swing.JPanel();
//        m_AssociateButton = new javax.swing.JButton();
//        m_AssociateAllButton = new javax.swing.JButton();
//        spacerPanel = new javax.swing.JPanel();
//        
//        setTitle( bundle.getString("IDS_PROJNAME2")); // NOI18N
//        addWindowListener(new java.awt.event.WindowAdapter()
//        {
//            public void windowClosing(java.awt.event.WindowEvent evt)
//            {
//                closeDialog(evt);
//            }
//        });
//        
//        mainPanel.setLayout(new java.awt.GridBagLayout());
//        
//        mainSearchPanel.setLayout(new java.awt.GridBagLayout());
//        
//        findPanel.setLayout(new java.awt.GridBagLayout());
//        org.openide.awt.Mnemonics.setLocalizedText(findLabel, bundle.getString("IDS_FINDWHAT"));
//        findLabel.setLabelFor(m_FindCombo);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
//        findPanel.add(findLabel, gridBagConstraints);
//        
//        m_FindCombo.setEditable(true);
//        m_FindCombo.setMaximumRowCount(10);
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
//        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.7;
//        findPanel.add(m_FindCombo, gridBagConstraints);
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
//        gridBagConstraints.weightx = 1.0;
//        mainSearchPanel.add(findPanel, gridBagConstraints);
//        
//        checkBoxesPanel.setLayout(new java.awt.GridBagLayout());
//        
//        org.openide.awt.Mnemonics.setLocalizedText(m_MatchCaseCheck, bundle.getString("IDS_MATCHCASE"));
//        m_MatchCaseCheck.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_MatchCaseCheck"));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.5;
//        checkBoxesPanel.add(m_MatchCaseCheck, gridBagConstraints);
//        
//        m_MatchCaseCheck.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onMatchCaseCheck(evt);
//            }
//        });
//        m_MatchCaseCheck.setSelected(isMatchCase());
//        m_Controller.setCaseSensitive(m_MatchCaseCheck.isSelected());
//        
//        org.openide.awt.Mnemonics.setLocalizedText(xPathCheck, bundle.getString("IDS_XPATHEXPRESSION"));
//        xPathCheck.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_XpathCheck"));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.5;
//        checkBoxesPanel.add(xPathCheck, gridBagConstraints);
//        xPathCheck.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onXPathCheck(evt);
//            }
//        });
//        
//        org.openide.awt.Mnemonics.setLocalizedText(m_WholeWordCheck, bundle.getString("IDS_MATCHWHOLE"));
//        m_WholeWordCheck.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_WholeWordCheck"));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.5;
//        checkBoxesPanel.add(m_WholeWordCheck, gridBagConstraints);
//        m_WholeWordCheck.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onWholeWordCheck(evt);
//            }
//        });
//        
//        org.openide.awt.Mnemonics.setLocalizedText(m_SearchAliasCheck, bundle.getString("IDS_SEARCHALIAS"));
//        m_SearchAliasCheck.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_SearchAliasCheck"));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.5;
//        checkBoxesPanel.add(m_SearchAliasCheck, gridBagConstraints);
//        m_SearchAliasCheck.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onAliasCheck(evt);
//            }
//        });
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.6;
//        mainSearchPanel.add(checkBoxesPanel, gridBagConstraints);
//        
//        radioPanel.setLayout(new java.awt.GridLayout());
//        radioPanel.setBorder(new javax.swing.border.TitledBorder(bundle.getString("IDS_SEARCHIN")));
//        
//        buttonGroup1.add(m_SearchElementsRadio);
//        m_SearchElementsRadio.setSelected(true);
//        org.openide.awt.Mnemonics.setLocalizedText(m_SearchElementsRadio, bundle.getString("IDS_ELEMENTS"));
//        m_SearchElementsRadio.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_Search_Element"));
//        radioPanel.add(m_SearchElementsRadio);
//        m_SearchElementsRadio.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onSearchElementsRadio(evt);
//            }
//        });
//        
//        buttonGroup1.add(m_SearchDescriptionsRadio);
//        org.openide.awt.Mnemonics.setLocalizedText(m_SearchDescriptionsRadio, bundle.getString("IDS_DESCRIPTIONS"));
//        m_SearchDescriptionsRadio.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_Search_Description"));
//        radioPanel.add(m_SearchDescriptionsRadio);
//        m_SearchDescriptionsRadio.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onSearchDescriptionsRadio(evt);
//            }
//        });
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.4;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
//        mainSearchPanel.add(radioPanel, gridBagConstraints);
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
//        mainPanel.add(mainSearchPanel, gridBagConstraints);
//        
//        buttonPanel.setLayout(new java.awt.GridBagLayout());
//        
//        org.openide.awt.Mnemonics.setLocalizedText(m_FindButton, bundle.getString("IDS_FIND"));
//        m_FindButton.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("IDS_FIND"));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
//        buttonPanel.add(m_FindButton, gridBagConstraints);
//        m_FindButton.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onFindButton(evt);
//            }
//        });
//        getRootPane().setDefaultButton(m_FindButton);
//        
//        org.openide.awt.Mnemonics.setLocalizedText(m_CloseButton, bundle.getString("IDS_CLOSE"));
//        m_CloseButton.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("IDS_CLOSE"));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
//        buttonPanel.add(m_CloseButton, gridBagConstraints);
//        m_CloseButton.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                setVisible(false);
//                dispose();
//            }
//        });
//        
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridheight = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
//        mainPanel.add(buttonPanel, gridBagConstraints);
//        
//        m_ResultsLabel = new javax.swing.JLabel();
//        Mnemonics.setLocalizedText(m_ResultsLabel, bundle.getString("LBL_SearchResult"));
//        
//        m_ResultsLabel.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("LBL_SearchResult"));
//        jPanel11.setLayout(new java.awt.GridBagLayout());
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
//        jPanel11.add(m_ResultsLabel, gridBagConstraints);
//        
//        AssociateTableModel model = new AssociateTableModel(this);
//        m_ResultsTable = new JAssociateTable(model, this);
//        m_ResultsLabel.setLabelFor(m_ResultsTable);
//        m_ResultsTable.getSelectionModel().addListSelectionListener(selectionListener);
//        m_ResultsTable.setBackground(Color.WHITE);
//        jScrollPane2 = new JScrollPane(m_ResultsTable);
//        
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//        jPanel11.add(jScrollPane2, gridBagConstraints);
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx=1;
//        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
//        mainPanel.add(jPanel11, gridBagConstraints);
//        
//        bottomPanel.setLayout(new java.awt.GridBagLayout());
//        
//        m_NavigateCheck.setEnabled(false);
//        org.openide.awt.Mnemonics.setLocalizedText(m_NavigateCheck, bundle.getString("IDS_NAVIGATE"));
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
//        bottomPanel.add(m_NavigateCheck, gridBagConstraints);
//        m_NavigateCheck.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onNavigateCheck(evt);
//            }
//        });
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 1.0;
//        bottomPanel.add(m_Status, gridBagConstraints);
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
//        mainPanel.add(bottomPanel, gridBagConstraints);
//        
//        associateButtonPanel.setLayout(new java.awt.GridBagLayout());
//        
//        org.openide.awt.Mnemonics.setLocalizedText(m_AssociateButton, bundle.getString("IDS_ASSOCIATE"));
//        m_AssociateButton.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_Associate"));
//        m_AssociateButton.setEnabled(false);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
//        associateButtonPanel.add(m_AssociateButton, gridBagConstraints);
//        m_AssociateButton.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onAssociateButton(evt);
//            }
//        });
//        
//        org.openide.awt.Mnemonics.setLocalizedText(m_AssociateAllButton, bundle.getString("IDS_ASSOCIATEALL"));
//        m_AssociateAllButton.getAccessibleContext().setAccessibleDescription(
//                bundle.getString("ACSD_AssociateAll"));
//        m_AssociateAllButton.setEnabled(false);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        associateButtonPanel.add(m_AssociateAllButton, gridBagConstraints);
//        m_AssociateAllButton.addActionListener(new java.awt.event.ActionListener()
//        {
//            public void actionPerformed(java.awt.event.ActionEvent evt)
//            {
//                onAssociateAllButton(evt);
//            }
//        });
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
//        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
//        mainPanel.add(associateButtonPanel, gridBagConstraints);
//        
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 0.3;
//        mainPanel.add(spacerPanel, gridBagConstraints);
//        
//        Dimension buttonSize = getMaxButtonWidth();
//        m_FindButton.setPreferredSize(buttonSize);
//        m_CloseButton.setPreferredSize(buttonSize);
//        m_AssociateButton.setPreferredSize(buttonSize);
//        m_AssociateAllButton.setPreferredSize(buttonSize);
//        
//        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
//        this.getAccessibleContext().setAccessibleDescription("IDS_ADDIN_FRIENDLY_NAME");
//    }
//    
//    
//    private void initTextFieldListeners()
//    {
//        class TextChangeListener implements DocumentListener
//        {
//            private JTextField textField;
//            TextChangeListener(JTextField textField)
//            {
//                this.textField = textField;
//            }
//            public void changedUpdate(DocumentEvent e)
//            {
//                documentChanged();
//            }
//            public void insertUpdate(DocumentEvent e)
//            {
//                documentChanged();
//            }
//            public void removeUpdate(DocumentEvent e)
//            {
//                documentChanged();
//            }
//            private void documentChanged()
//            {
//                updateState(textField);
//            }
//        }
//        ((JTextField)m_FindCombo.getEditor().getEditorComponent()).getDocument().
//                addDocumentListener(new TextChangeListener(
//                (JTextField)m_FindCombo.getEditor().getEditorComponent()));
//    }
//    
//    private void updateState(JTextField textField)
//    {
//        if (update)
//            m_FindButton.setEnabled(!"".equals(textField.getText().trim()));
//    }
//    
//    private Dimension getMaxButtonWidth()
//    {
//        Dimension ret = null;
//        Dimension d = m_FindButton.getPreferredSize();
//        double max  = d.width;
//        
//        d = m_CloseButton.getPreferredSize();
//        if(d.width > max)
//        {
//            max = d.width;
//            ret = d;
//        }
//        d = m_AssociateButton.getPreferredSize();
//        if(d.width > max)
//        {
//            max = d.width;
//            ret = d;
//        }
//        d = m_AssociateAllButton.getPreferredSize();
//        if(d.width > max)
//        {
//            max = d.width;
//            ret = d;
//        }
//        return ret;
//    }
//    
//    private void onLoadExternalCheck(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JCheckBox)
//        {
//            JCheckBox box = (JCheckBox)obj;
//            boolean checkboxState = box.isSelected();
//            if (checkboxState)
//            {
//                m_Controller.setExternalLoad(true);
//            }
//            else
//            {
//                m_Controller.setExternalLoad(false);
//            }
//        }
//    }
//    
//    private void onXPathCheck(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JCheckBox)
//        {
//            JCheckBox box = (JCheckBox)obj;
//            boolean checkboxState = box.isSelected();
//            if (checkboxState)
//            {
//                m_Controller.setKind(1);
//                m_Controller.setCaseSensitive(true);
//                m_MatchCaseCheck.setEnabled(false);
//                m_SearchDescriptionsRadio.setEnabled(false);
//                m_SearchElementsRadio.setEnabled(false);
//                m_SearchAliasCheck.setEnabled(false);
//                m_WholeWordCheck.setEnabled(false);
//            }
//            else
//            {
//                m_Controller.setKind(0);
//                m_Controller.setCaseSensitive(m_MatchCaseCheck.isSelected());
//                m_MatchCaseCheck.setEnabled(true);
//                m_SearchDescriptionsRadio.setEnabled(true);
//                m_SearchElementsRadio.setEnabled(true);
//                m_SearchAliasCheck.setEnabled(true);
//                m_WholeWordCheck.setEnabled(true);
//            }
//        }
//    }
//    
//    private void onAliasCheck(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JCheckBox)
//        {
//            JCheckBox box = (JCheckBox)obj;
//            boolean checkboxState = box.isSelected();
//            if (checkboxState)
//            {
//                m_Controller.setSearchAlias(true);
//                m_SearchElementsRadio.setSelected(true);
//                m_SearchDescriptionsRadio.setSelected(false);
//                m_SearchDescriptionsRadio.setEnabled(false);
//            }
//            else
//            {
//                m_Controller.setSearchAlias(false);
//                m_SearchDescriptionsRadio.setEnabled(true);
//            }
//        }
//    }
//    
//    private void onWholeWordCheck(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JCheckBox)
//        {
//            JCheckBox box = (JCheckBox)obj;
//            boolean checkboxState = box.isSelected();
//            if (checkboxState)
//            {
//                m_Controller.setWholeWordSearch(true);
//            }
//            else
//            {
//                m_Controller.setWholeWordSearch(false);
//                
//            }
//        }
//    }
//    
//    private void onMatchCaseCheck(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JCheckBox)
//        {
//            Preferences prefs = NbPreferences.forModule (DummyCorePreference.class) ;
//            JCheckBox box = (JCheckBox)obj;
//            boolean checkboxState = box.isSelected();
//            if (checkboxState)
//            {
//                m_Controller.setCaseSensitive(true);
//                prefs.put ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_NEVER") ;
//            }
//            else
//            {
//                m_Controller.setCaseSensitive(false);
//                String find = prefs.get ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ASK");
//                if (find.equals("PSK_NEVER"))
//                    prefs.put("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ALWAYS");
//            }
//        }
//    }
//    
//    private void onSearchElementsRadio(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JRadioButton)
//        {
//            m_Controller.setResultType(0);
//            m_SearchElementsRadio.setSelected(true);
//            m_SearchDescriptionsRadio.setSelected(false);
//            m_SearchAliasCheck.setEnabled(true);
//        }
//    }
//    
//    private void onSearchDescriptionsRadio(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JRadioButton)
//        {
//            m_Controller.setResultType(1);
//            m_SearchDescriptionsRadio.setSelected(true);
//            m_SearchElementsRadio.setSelected(false);
//            m_SearchAliasCheck.setSelected(false);
//            m_SearchAliasCheck.setEnabled(false);
//        }
//    }
//    
//    private void onNavigateCheck(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JCheckBox)
//        {
//            JCheckBox box = (JCheckBox)obj;
//            boolean checkboxState = box.isSelected();
//            if (checkboxState)
//            {
//                m_LockIsChecked = true;
//            }
//            else
//            {
//                m_LockIsChecked = false;
//            }
//        }
//    }
//    
//        /* (non-Javadoc)
//         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#display()
//         */
//    public void display()
//    {
//    }
//    
//        /* (non-Javadoc)
//         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#getResults()
//         */
//    public IFindResults getResults()
//    {
//        return m_Results;
//    }
//    
//        /* (non-Javadoc)
//         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#setResults(org.netbeans.modules.uml.ui.support.finddialog.IFindResults)
//         */
//    public void setResults(IFindResults newVal)
//    {
//        m_Results = newVal;
//        
//    }
//    
//        /* (non-Javadoc)
//         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#getProject()
//         */
//    public IProject getProject()
//    {
//        return m_Project;
//    }
//    
//        /* (non-Javadoc)
//         * @see org.netbeans.modules.uml.ui.addins.associateDialog.IAssociateDlgGUI#setProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
//         */
//    public void setProject(IProject newVal)
//    {
//        m_Project = newVal;
//    }
//    private void onFindButton(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JButton)
//        {
//            onFindButton();
//        }
//    }
//    
//    
//    private void onFindButton()
//    {
//        m_Status.setText("");
//        update = false;
//        clearGrid();
//        // get the string that the user typed in
//        String searchStr = (String) m_FindCombo.getSelectedItem();
//        
//        // Save the values of the search combo
//        FindUtilities.saveSearchString("LastAssociateStrings", m_FindCombo); // NOI18N
//        // reset what is in the search combo
//        FindUtilities.populateComboBoxes("LastAssociateStrings", m_FindCombo); // NOI18N
//        FindUtilities.startWaitCursor(getContentPane());
//        // do the search
//        m_Controller.setSearchString(searchStr);
//        FindResults pResults = new FindResults();
//        try
//        {
//            m_Controller.search2(m_Project, pResults);
//            if (pResults != null)
//            {
//                ETList < IElement > pElements = pResults.getElements();
//                ETList < IProxyDiagram > pDiagrams = pResults.getDiagrams();
//                if ((pElements != null) && (pDiagrams != null))
//                {
//                    int count = pElements.size();
//                    int countD = pDiagrams.size();
//                    if (count > 0 || countD > 0)
//                    {
//                        // show the results
//                        ETList < Object > findResults =
//                                FindUtilities.loadResultsIntoArray(pResults);
//                        AssociateTableModel model =
//                                new AssociateTableModel(this, findResults);
//                        m_ResultsTable.setModel(model);
//                        m_AssociateAllButton.setEnabled(true);
//                        
//                        long totalC = count + countD;
//                        String strMsg = totalC + " ";
//                        strMsg += FindUtilities.translateString("IDS_NUMFOUND"); // NOI18N
//                        m_Status.setText(strMsg);
//                        enableLockCheck();
//                        //
//                        // This is special code to aid in the automating testing.  We had no way to access
//                        // the information in the grid from the automated scripts and/or VisualTest, so
//                        // if a flag is set in the registry, we will dump the results of the grid to a
//                        // specified file
//                        //
//                                                /* TODO
//                                                if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
//                                                {
//                                                        CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
//                                                                if (file.Length())
//                                                                {
//                                                                        m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
//                                                                }
//                                                        }
//                                                 */
//                    }
//                    else
//                    {
//                        String noneStr =
//                                FindUtilities.translateString("IDS_NONEFOUND"); // NOI18N
//                        m_Status.setText(noneStr);
//                    }
//                }
//                else
//                {
//                    String canStr =
//                            FindUtilities.translateString("IDS_CANCELLED"); // NOI18N
//                    m_Status.setText(canStr);
//                }
//            }
//            else
//            {
//                String str2 = FindUtilities.translateString("IDS_NONEFOUND2"); // NOI18N
//                m_Status.setText(str2);
//            }
//        }
//        catch (Exception e)
//        {
//            String msg;
//            
//            if (xPathCheck.isSelected())
//                msg = FindUtilities.translateString("IDS_ERROR1");
//            else
//                msg = FindUtilities.translateString("IDS_NONEFOUND");
//            
//            m_Status.setText(msg);
//        }
//        m_FindCombo.setSelectedItem(searchStr);
//        FindUtilities.endWaitCursor(getContentPane());
//        
//        update = true;
//        m_FindCombo.getEditor().selectAll();
//    }
//    
//    
//    private void onAssociateButton(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JButton)
//        {
//            m_Status.setText("");
//            FindResults pResults = new FindResults();
//            if (pResults != null)
//            {
//                loadResultsFromGrid(pResults, true);
//                ETList<IElement> pElements = pResults.getElements();
//                ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
//                if ( (pElements != null) && (pDiagrams != null))
//                {
//                    int count = pElements.size();
//                    int countD = pDiagrams.size();
//                    if (count > 0 || countD > 0)
//                    {
//                        associate(pResults);
//                        clearGrid();
//                    }
//                    else
//                    {
//                        // no items selected in the grid
//                        String noneStr = FindUtilities.translateString("IDS_NOITEMSSELECTED"); // NOI18N
//                        String str2 = FindUtilities.translateString("IDS_PROJNAME2"); // NOI18N
//                        IErrorDialog pTemp = new SwingErrorDialog(this);
//                        if (pTemp != null)
//                        {
//                            pTemp.display(noneStr, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
//                        }
//                    }
//                }
//            }
//        }
//    }
//    
//    
//    private void onAssociateAllButton(java.awt.event.ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//        if (obj instanceof JButton)
//        {
//            m_Status.setText("");
//            FindResults pResults = new FindResults();
//            if (pResults != null)
//            {
//                loadResultsFromGrid(pResults, false);
//                ETList<IElement> pElements = pResults.getElements();
//                ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
//                if ( (pElements != null) && (pDiagrams != null))
//                {
//                    int count = pElements.size();
//                    int countD = pDiagrams.size();
//                    if (count > 0 || countD > 0)
//                    {
//                        // disable replace buttons in case the search fails
//                        m_AssociateButton.setEnabled(false);
//                        m_AssociateAllButton.setEnabled(false);
//                        m_NavigateCheck.setEnabled(false);
//                        associate(pResults);
//                        clearGrid();
//                    }
//                    else
//                    {
//                        // no items selected in the grid
//                        String noneStr = FindUtilities.translateString("IDS_NOITEMSSELECTED"); // NOI18N
//                        String str2 = FindUtilities.translateString("IDS_PROJNAME2"); // NOI18N
//                        IErrorDialog pTemp = new SwingErrorDialog(this);
//                        if (pTemp != null)
//                        {
//                            pTemp.display(noneStr, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
//                        }
//                    }
//                }
//            }
//        }
//    }
//    
//    /**
//     *
//     * Determines whether or not the edit lock check box should be enabled or not.
//     * We are only going to allow it if what was selected in the tree or draw area to begin
//     * the associate are elements (no diagrams).
//     *
//     *
//     * @return
//     *
//     */
//    private void enableLockCheck()
//    {
//        if (m_Results != null)
//        {
//            ETList < IProxyDiagram > pDiagrams = m_Results.getDiagrams();
//            if (pDiagrams != null)
//            {
//                int count = pDiagrams.size();
//                if (count == 0)
//                {
//                    m_NavigateCheck.setEnabled(true);
//                }
//            }
//        }
//    }
//    
//    
//    private void loadResultsFromGrid(FindResults pResults, boolean bSelect)
//    {
//        if (pResults != null)
//        {
//            // get the elements array from the results object
//            ETList<IElement> pElements = pResults.getElements();
//            // get the diagrams array from the results object
//            ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
//            if ( (pElements != null) && (pDiagrams != null))
//            {
//                if (bSelect)
//                {
//                    // loop through the information in the table
//                    int[] selRows = m_ResultsTable.getSelectedRows();
//                    for (int x = 0; x < selRows.length; x++)
//                    {
//                        int selRow = selRows[x];
//                        AssociateTableModel model = (AssociateTableModel)m_ResultsTable.getModel();
////						FindTableModel model = (FindTableModel)m_ResultsTable.getModel();
//                        if (model != null)
//                        {
//                            IElement pElement = model.getElementAtRow(selRow);
//                            if (pElement != null)
//                            {
//                                pElements.add(pElement);
//                            }
//                            else
//                            {
//                                IProxyDiagram pDiagram = model.getDiagramAtRow(selRow);
//                                if (pDiagram != null)
//                                {
//                                    pDiagrams.add(pDiagram);
//                                }
//                            }
//                        }
//                    }
//                }
//                else
//                {
//                    int rows = m_ResultsTable.getRowCount();
//                    for (int x = 0; x < rows; x++)
//                    {
//                        AssociateTableModel model = (AssociateTableModel)m_ResultsTable.getModel();
//                        if (model != null)
//                        {
//                            IElement pElement = model.getElementAtRow(x);
//                            if (pElement != null)
//                            {
//                                pElements.add(pElement);
//                            }
//                            else
//                            {
//                                IProxyDiagram pDiagram = model.getDiagramAtRow(x);
//                                if (pDiagram != null)
//                                {
//                                    pDiagrams.add(pDiagram);
//                                }
//                            }
//                        }
//                    }
//                    
//                }
//            }
//        }
//    }
//    
//    /**
//     * Associate the member elements of this dialog to those passed into this routine
//     *
//     * @param[in] results   The array of elements that should be associated
//     *
//     * @return HRESULT
//     *
//     */
//    private void associate(IFindResults pResults)
//    {
//        if (m_Results != null && pResults != null)
//        {
//            // the relation factory is going to handle the creation of the
//            // Referencing/Referred relationships
//            IRelationFactory relFactory = new RelationFactory();
//            if (relFactory != null)
//            {
//                // first we will "associate" any symbol to symbol relationships
//                ETList <IElement> pElements = m_Results.getElements();
//                if (pElements != null)
//                {
//                    int count = pElements.size();
//                    for (int x = 0; x < count; x++)
//                    {
//                        IElement pRefEle = pElements.get(x);
//                        if (pRefEle != null)
//                        {
//                            // symbol to symbol
//                            ETList < IElement > pElements2 = pResults.getElements();
//                            if (pElements2 != null)
//                            {
//                                int count2 = pElements2.size();
//                                for (int y = 0; y < count2; y++)
//                                {
//                                    IElement pEle = pElements2.get(y);
//                                    if (pEle != null)
//                                    {
//                                        IReference pRef = relFactory.createReference(pRefEle, pEle);
//                                    }
//                                }
//                            }
//                            // symbol to diagram
//                            ETList < IProxyDiagram > pDiagrams = pResults.getDiagrams();
//                            if (pDiagrams != null)
//                            {
//                                int count2 = pDiagrams.size();
//                                for (int y = 0; y < count2; y++)
//                                {
//                                    IProxyDiagram pDiagram = pDiagrams.get(y);
//                                    if (pDiagram != null)
//                                    {
//                                        pDiagram.addAssociatedElement(pRefEle);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                // Now we will process the diagrams
//                // first we will "associate" any symbol to symbol relationships
//                ETList < IProxyDiagram > pDiagrams = m_Results.getDiagrams();
//                if (pDiagrams != null)
//                {
//                    int count = pDiagrams.size();
//                    for (int x = 0; x < count; x++)
//                    {
//                        IProxyDiagram pRef = pDiagrams.get(x);
//                        if (pRef != null)
//                        {
//                            // diagram to symbol
//                            ETList <IElement> pElements2 = pResults.getElements();
//                            if (pElements2 != null)
//                            {
//                                int count2 = pElements2.size();
//                                for (int y = 0; y < count2; y++)
//                                {
//                                    IElement pEle = pElements2.get(y);
//                                    if (pEle != null)
//                                    {
//                                        pRef.addAssociatedElement(pEle);
//                                    }
//                                }
//                            }
//                            // diagram to diagram
//                            ETList <IProxyDiagram> pDiagrams3 = pResults.getDiagrams();
//                            if (pDiagrams3 != null)
//                            {
//                                int count2 = pDiagrams3.size();
//                                for (int y = 0; y < count2; y++)
//                                {
//                                    IProxyDiagram pDiagram = pDiagrams3.get(y);
//                                    if (pDiagram != null)
//                                    {
//                                        pRef.addDualAssociatedDiagrams(pRef, pDiagram);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                // now that we are done associating elements, check to see if the user wants
//                // to lock the edit mode of the element(s) that are the start of the associated process
//                processLockEdit();
//            }
//        }
//    }
//    
//    
//    /**
//     *
//     * If the user has told us to lock the edit mode of the selected associated element(s),
//     * then we will go through all of its presentation elements and set the lock edit flag
//     * on them.
//     *
//     *
//     * @return
//     *
//     */
//    private void processLockEdit()
//    {
//        if (m_LockIsChecked)
//        {
//            if (m_Results != null)
//            {
//                // get the elements that started the associate process
//                ETList < IElement > pElements = m_Results.getElements();
//                if (pElements != null)
//                {
//                    // loop through
//                    int count = pElements.size();
//                    for (int x = 0; x < count; x++)
//                    {
//                        // got one
//                        IElement pRefEle = pElements.get(x);
//                        if (pRefEle != null)
//                        {
//                            // now get its presentation elements
//                            ETList < IPresentationElement > pPresEles = pRefEle.getPresentationElements();
//                            if (pPresEles != null)
//                            {
//                                // loop through
//                                int presCnt = pPresEles.size();
//                                for (int y = 0; y < presCnt; y++)
//                                {
//                                    IPresentationElement pPresEle = pPresEles.get(y);
//                                    if (pPresEle != null)
//                                    {
//                                        // the lock edit flag is only available for nodes
//                                        // TODO: meteora
////                                        if (pPresEle instanceof INodePresentation)
////                                        {
////                                            INodePresentation pNode = (INodePresentation)pPresEle;
////                                            if (pNode != null)
////                                            {
////                                                pNode.setLockEdit(true);
////                                            }
////                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//    
//    
//    private void clearGrid()
//    {
//        m_ResultsTable.setModel(new FindTableModel());
//        m_AssociateAllButton.setEnabled(false);
//    }
//    
//    private void initDialog()
//    {
//        m_Status.setText("");
//        FindUtilities.populateComboBoxes("LastAssociateStrings", m_FindCombo); // NOI18N
//        m_FindCombo.getEditor().selectAll();
//    }
//    
//    /** Closes the dialog */
//    private void closeDialog(java.awt.event.WindowEvent evt)
//    {
//        setVisible(false);
//        dispose();
//    }
//    
//    
//    private boolean isMatchCase()
//    {
//        return !"PSK_ALWAYS".equals(ProductHelper.getPreferenceManager().getPreferenceValue("FindDialog", "LongSearch"));
//    }
//    
//    private class SelectionListener implements ListSelectionListener
//    {
//        public void valueChanged(ListSelectionEvent e)
//        {
//            m_AssociateButton.setEnabled(m_ResultsTable.getSelectedRows().length>0);
//        }
//    }
//    
//    private javax.swing.ButtonGroup buttonGroup1;
//    private javax.swing.JPanel mainPanel;
//    private javax.swing.JPanel mainSearchPanel;
//    private javax.swing.JPanel buttonPanel;
//    private javax.swing.JPanel findPanel;
//    private javax.swing.JPanel checkBoxesPanel;
//    private javax.swing.JPanel radioPanel;
//    private javax.swing.JPanel jPanel11;
//    private javax.swing.JPanel spacerPanel;
//    private javax.swing.JPanel bottomPanel;
//    private javax.swing.JPanel associateButtonPanel;
//    
//    private javax.swing.JLabel findLabel;
//    private javax.swing.JComboBox m_FindCombo;
//    
//    private javax.swing.JButton m_FindButton;
//    private javax.swing.JButton m_CloseButton;
//    
//    private javax.swing.JCheckBox m_MatchCaseCheck;
//    private javax.swing.JCheckBox xPathCheck;
//    private javax.swing.JCheckBox m_WholeWordCheck;
//    private javax.swing.JCheckBox m_SearchAliasCheck;
//    
//    private javax.swing.JRadioButton m_SearchElementsRadio;
//    private javax.swing.JRadioButton m_SearchDescriptionsRadio;
//    
//    private javax.swing.JButton m_AssociateAllButton;
//    private javax.swing.JButton m_AssociateButton;
//    private javax.swing.JCheckBox m_NavigateCheck;
//    private javax.swing.JLabel m_Status;
//    
//    private javax.swing.JLabel m_ResultsLabel;
//    private javax.swing.JTable m_ResultsTable;
//    private javax.swing.JScrollPane jScrollPane2;
//    
//    private FindController m_Controller = null;
//    private IFindResults m_Results = null;
//    private IProject m_Project = null;
//    private SelectionListener selectionListener;
//    private boolean update = true;
//}
//
//
//
