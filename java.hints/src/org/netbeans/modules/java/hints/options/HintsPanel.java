/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.modules.java.hints.options;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.impl.refactoring.Configuration;
import org.netbeans.modules.java.hints.jackpot.impl.refactoring.ConfigurationRenderer;
import org.netbeans.modules.java.hints.jackpot.impl.refactoring.ConfigurationsComboModel;
import org.netbeans.modules.java.hints.jackpot.impl.refactoring.Utilities;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.options.HintsPanelLogic.HintCategory;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.modules.options.editor.spi.OptionsFilter.Acceptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;


public final class HintsPanel extends javax.swing.JPanel implements TreeCellRenderer  {

    private final static RequestProcessor WORKER = new RequestProcessor(HintsPanel.class.getName(), 1, false, false);

    private DefaultTreeCellRenderer dr = new DefaultTreeCellRenderer();
    private JCheckBox renderer = new JCheckBox();
    private HintsPanelLogic logic;
    private DefaultTreeModel errorTreeModel;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    //AWT only:
    private HintMetadata toSelect = null;
    
    private Document oldDescription;
      
    DefaultMutableTreeNode extraNode = new DefaultMutableTreeNode(NbBundle.getMessage(HintsPanel.class, "CTL_DepScanning")); //NOI18N

    @Messages("LBL_Loading=Loading...")
    HintsPanel(@NullAllowed final OptionsFilter filter) {
        WORKER.post(new Runnable() {

            @Override
            public void run() {
                RulesManager.getInstance();
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        HintsPanel.this.removeAll();
                        HintsPanel.this.init(filter, true);
                        buttonsPanel.setVisible(false);
                        searchPanel.setVisible(false);
                        configurationsPanel.setVisible(false);
                    }
                });
            }
        });

        setLayout(new GridBagLayout());
        add(new JLabel(Bundle.LBL_Loading()), new GridBagConstraints());
    }

    public HintsPanel(Configuration preselected) {
        init(null, false);
        configCombo.setSelectedItem(preselected);
    }
    public HintsPanel(HintMetadata preselected) {
        init(null, false);
        select(preselected);
    }
    

    private void init(@NullAllowed OptionsFilter filter, boolean allHints) {
        initComponents();
        org.netbeans.modules.java.hints.jackpot.impl.refactoring.OptionsFilter f = null;
        if (!allHints && filter==null) {
            f = new org.netbeans.modules.java.hints.jackpot.impl.refactoring.OptionsFilter(
                    searchTextField.getDocument(), new Runnable() {
        
                @Override
                public void run() {
                }

            }
            ); 
        }
        configCombo.setModel(new ConfigurationsComboModel(true));
        configCombo.setRenderer(new ConfigurationRenderer());
        if (allHints) {
            configCombo.setSelectedItem(null);
        }
        
        descriptionTextArea.setContentType("text/html"); // NOI18N

//        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
//            setOpaque( false );
        
        errorTree.setCellRenderer( this );
        errorTree.setRootVisible( false );
        errorTree.setShowsRootHandles( true );
        errorTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        
        errorTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleClick(e);
            }
            
            private void handleClick(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = e.getPoint();
                    TreePath path = errorTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                    if (path != null) {
                        DefaultMutableTreeNode o = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (o.getUserObject() instanceof HintMetadata) {
                            HintMetadata hint = (HintMetadata) o.getUserObject();
                            if (hint.category.equals("custom")) {
                                JPopupMenu popup = new JPopupMenu();
                                popup.add(new JMenuItem(new RenameHint(o, hint, path)));
                                popup.add(new JMenuItem(new RemoveHint(o, hint)));
                                popup.show(errorTree, e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        });
        
        toProblemCheckBox.setVisible(false);
        
        errorTreeModel = constructTM(allHints?RulesManager.getInstance().allHints.keySet():Utilities.getBatchSupportedHints(), allHints);

        if (filter != null) {
             ((OptionsFilter) filter).installFilteringModel(errorTree, errorTreeModel, new AcceptorImpl());
        } else if (f!=null) {
                ((org.netbeans.modules.java.hints.jackpot.impl.refactoring.OptionsFilter) f).installFilteringModel(errorTree, errorTreeModel, new AcceptorImpl());
        } else {
            errorTree.setModel(errorTreeModel);
        }

        initialized.set(true);
        update();
        
        if (toSelect != null) {
            select(toSelect);
            
            toSelect = null;
        }
        
        boolean editEnabled = !allHints && FileUtil.getConfigFile("org-netbeans-modules-java-hints/templates/HintSample.hint")!=null;
        newButton.setVisible(editEnabled);
        importButton.setVisible(false);
        exportButton.setVisible(false);
        editScriptButton.setVisible(editEnabled);
        editingButtons.setVisible(false);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        treePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorTree = new EditableJTree();
        detailsPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        severityLabel = new javax.swing.JLabel();
        severityComboBox = new javax.swing.JComboBox();
        toProblemCheckBox = new javax.swing.JCheckBox();
        customizerPanel = new javax.swing.JPanel();
        descriptionPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JEditorPane();
        descriptionLabel = new javax.swing.JLabel();
        editingButtons = new javax.swing.JPanel();
        saveButton = new javax.swing.JButton();
        cancelEdit = new javax.swing.JButton();
        openInEditor = new javax.swing.JButton();
        buttonsPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        editScriptButton = new javax.swing.JButton();
        configurationsPanel = new javax.swing.JPanel();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        searchPanel = new javax.swing.JPanel();
        refactoringsLabel = new javax.swing.JLabel();
        searchLabel = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(320);
        jSplitPane1.setOpaque(false);

        treePanel.setOpaque(false);
        treePanel.setLayout(new java.awt.BorderLayout());

        errorTree.setEditable(true);
        jScrollPane1.setViewportView(errorTree);
        errorTree.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.errorTree.AccessibleContext.accessibleName")); // NOI18N
        errorTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.errorTree.AccessibleContext.accessibleDescription")); // NOI18N

        treePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(treePanel);

        detailsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.setLayout(new java.awt.GridBagLayout());

        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new java.awt.GridBagLayout());

        severityLabel.setLabelFor(severityComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        optionsPanel.add(severityLabel, gridBagConstraints);
        severityLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.severityLabel.AccessibleContext.accessibleDescription")); // NOI18N

        severityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        optionsPanel.add(severityComboBox, new java.awt.GridBagConstraints());
        severityComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "AN_Show_As_Combo")); // NOI18N
        severityComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "AD_Show_As_Combo")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(toProblemCheckBox, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_InTasklist_CheckBox")); // NOI18N
        toProblemCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        toProblemCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        optionsPanel.add(toProblemCheckBox, gridBagConstraints);

        customizerPanel.setOpaque(false);
        customizerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        optionsPanel.add(customizerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        detailsPanel.add(optionsPanel, gridBagConstraints);

        descriptionPanel.setOpaque(false);
        descriptionPanel.setLayout(new java.awt.GridBagLayout());

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setPreferredSize(new java.awt.Dimension(100, 50));
        jScrollPane2.setViewportView(descriptionTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        descriptionPanel.add(jScrollPane2, gridBagConstraints);

        descriptionLabel.setLabelFor(descriptionTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        descriptionPanel.add(descriptionLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelEdit, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.cancelEdit.text")); // NOI18N
        cancelEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelEditActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(openInEditor, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.openInEditor.text")); // NOI18N
        openInEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInEditorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout editingButtonsLayout = new javax.swing.GroupLayout(editingButtons);
        editingButtons.setLayout(editingButtonsLayout);
        editingButtonsLayout.setHorizontalGroup(
            editingButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editingButtonsLayout.createSequentialGroup()
                .addComponent(openInEditor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 131, Short.MAX_VALUE)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cancelEdit))
        );
        editingButtonsLayout.setVerticalGroup(
            editingButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editingButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(openInEditor)
                .addComponent(cancelEdit)
                .addComponent(saveButton))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        descriptionPanel.add(editingButtons, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.7;
        detailsPanel.add(descriptionPanel, gridBagConstraints);

        jSplitPane1.setRightComponent(detailsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.newButton.text")); // NOI18N
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(importButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.importButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(exportButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.exportButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editScriptButton, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.editScriptButton.text")); // NOI18N
        editScriptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editScriptButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addComponent(newButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(exportButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 272, Short.MAX_VALUE)
                .addComponent(editScriptButton)
                .addGap(18, 18, 18)
                .addComponent(cancelButton))
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(newButton)
                    .addComponent(importButton)
                    .addComponent(exportButton)
                    .addComponent(editScriptButton)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(buttonsPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.configLabel.text")); // NOI18N

        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout configurationsPanelLayout = new javax.swing.GroupLayout(configurationsPanel);
        configurationsPanel.setLayout(configurationsPanelLayout);
        configurationsPanelLayout.setHorizontalGroup(
            configurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationsPanelLayout.createSequentialGroup()
                .addComponent(configLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configCombo, 0, 658, Short.MAX_VALUE))
        );
        configurationsPanelLayout.setVerticalGroup(
            configurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(configLabel)
                .addComponent(configCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(configurationsPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(refactoringsLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.refactoringsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.searchLabel.text")); // NOI18N

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addComponent(refactoringsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 508, Short.MAX_VALUE)
                .addComponent(searchLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel)
                    .addComponent(refactoringsLabel)))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(searchPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        applyChanges();
        getRootPane().getParent().setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed
        
    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        if (configCombo.getSelectedItem() instanceof ActionListener) {
            ((ActionListener) configCombo.getSelectedItem()).actionPerformed(evt);
        }
    }//GEN-LAST:event_configComboActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        try {
            FileObject tempFO = FileUtil.getConfigFile("org-netbeans-modules-java-hints/templates/HintSample.hint"); // NOI18N
            FileObject folderFO = FileUtil.getConfigFile("rules");
            if (folderFO == null) {
                folderFO = FileUtil.getConfigRoot().createFolder("rules");
            }
            DataFolder folder = (DataFolder) DataObject.find(folderFO);
            DataObject template = DataObject.find(tempFO);
            DataObject newIfcDO = template.createFromTemplate(folder);
            RulesManager.getInstance().reload();
            errorTreeModel = constructTM(Utilities.getBatchSupportedHints(), false);
            errorTree.setModel(errorTreeModel);
            logic.errorTreeModel = errorTreeModel;
            select(getHintByName(newIfcDO.getPrimaryFile().getNameExt()));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
}//GEN-LAST:event_newButtonActionPerformed

    private void editScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editScriptButtonActionPerformed
        descriptionTextArea.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-javahints"));
        descriptionTextArea.setEditable(true);
        editScriptButton.setVisible(false);
        editingButtons.setVisible(true);
        optionsPanel.setVisible(false);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Script_Border"));        
        DataObject dob = getDataObject(getSelectedHint());
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        try {
            oldDescription = descriptionTextArea.getDocument();
            descriptionTextArea.setDocument(ec.openDocument());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        newButton.setEnabled(false);
        searchTextField.setEnabled(false);
        configCombo.setEnabled(false);
        errorTree.setEnabled(false);
        validate();
}//GEN-LAST:event_editScriptButtonActionPerformed

    private void cancelEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelEditActionPerformed
        descriptionTextArea.setDocument(oldDescription);
        descriptionTextArea.setContentType("text/html");
        optionsPanel.setVisible(true);
        editingButtons.setVisible(false);
        editScriptButton.setVisible(true);
        descriptionTextArea.setEditable(false);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border"));

        newButton.setEnabled(true);
        searchTextField.setEnabled(true);
        configCombo.setEnabled(true);
        errorTree.setEnabled(true);
        logic.valueChanged(null);
    }//GEN-LAST:event_cancelEditActionPerformed

    private void openInEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInEditorActionPerformed
        cancelEditActionPerformed(evt);
        cancelButtonActionPerformed(evt);
        getRootPane().getParent().getParent().setVisible(false);
        DataObject dob = getDataObject(getSelectedHint());
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        ec.open();
    }//GEN-LAST:event_openInEditorActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        DataObject dob = getDataObject(getSelectedHint());
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        try {
            ec.saveDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        cancelEditActionPerformed(evt);
    }//GEN-LAST:event_saveButtonActionPerformed
    
    public static HintMetadata getHintByName(String name) {
        for (HintMetadata meta:Utilities.getBatchSupportedHints()) {
            if (meta.id.startsWith(name)) {
                return meta;
            }
        }
        return null;
    }    
    synchronized void update() {
        if (!initialized.get()) return;
        if ( logic != null ) {
            logic.disconnect();
        }
        logic = new HintsPanelLogic();
        logic.connect(errorTree, errorTreeModel, severityLabel, severityComboBox, toProblemCheckBox, customizerPanel, descriptionTextArea, configCombo, editScriptButton);
    }
    
    void cancel() {
        if (!initialized.get()) return;
        logic.disconnect();
        logic = null;
    }
    
    boolean isChanged() {
        return logic != null ? logic.isChanged() : false;
    }
    
    void applyChanges() {
        if (!initialized.get()) return;
        logic.applyChanges();
        logic.disconnect();
        logic = null;
    }
           
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        renderer.setBackground( selected ? dr.getBackgroundSelectionColor() : dr.getBackgroundNonSelectionColor() );
        renderer.setForeground( selected ? dr.getTextSelectionColor() : dr.getTextNonSelectionColor() );
        renderer.setEnabled( true );

        Object data = ((DefaultMutableTreeNode)value).getUserObject();
        if ( data instanceof HintCategory ) {
            HintCategory cat = ((HintCategory)data);
            renderer.setText(cat.displayName);
            if (logic!=null)
                renderer.setSelected( logic.isSelected((DefaultMutableTreeNode)value));
        }
        else if ( data instanceof HintMetadata ) {
            HintMetadata treeRule = (HintMetadata)data;
            renderer.setText( treeRule.displayName );

            if (logic != null) {
                Preferences node = logic.getCurrentPrefernces(treeRule.id);
                renderer.setSelected(HintsSettings.isEnabled(treeRule, node));
            }
        }
        else {
            renderer.setText( value.toString() );
            if (value == extraNode && logic != null) {
                renderer.setSelected(logic.getCurrentDependencyTracking() != DepScanningSettings.DependencyTracking.DISABLED);
            }
        }

        return renderer;
    }
    
    static String getFileObjectLocalizedName( FileObject fo ) {
        Object o = fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
        if ( o instanceof String ) {
            String bundleName = (String)o;
            try {
                ResourceBundle rb = NbBundle.getBundle(bundleName);            
                String localizedName = rb.getString(fo.getPath());                
                return localizedName;
            }
            catch(MissingResourceException ex ) {
                // Do nothing return file path;
            }
        }
        return fo.getPath();
    } 
        
    // Variables declaration - do not modify                     
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton cancelEdit;
    private javax.swing.JComboBox configCombo;
    private javax.swing.JLabel configLabel;
    private javax.swing.JPanel configurationsPanel;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JEditorPane descriptionTextArea;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JButton editScriptButton;
    private javax.swing.JPanel editingButtons;
    private javax.swing.JTree errorTree;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton importButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton newButton;
    private javax.swing.JButton openInEditor;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel refactoringsLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JComboBox severityComboBox;
    private javax.swing.JLabel severityLabel;
    private javax.swing.JCheckBox toProblemCheckBox;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables

    private final Map<HintMetadata, TreePath> hint2Path =  new HashMap<HintMetadata, TreePath>();

    private DefaultTreeModel constructTM(Collection<? extends HintMetadata> metadata, boolean allHints) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Map<HintCategory, Collection<HintMetadata>> cat2Hints = new TreeMap<HintCategory, Collection<HintMetadata>>(new Comparator<HintCategory>() {
            public int compare(HintCategory o1, HintCategory o2) {
                return HintsPanel.compare(o1.displayName, o2.displayName);
            }
        });
        Map<String, HintCategory> cat2CatDesc =  new HashMap<String, HintCategory>();

        for (HintMetadata m : metadata) {
            if (m.options.contains(Options.NON_GUI)) continue;

            HintCategory cat = cat2CatDesc.get(m.category);

            if (cat == null) {
                cat2CatDesc.put(m.category, cat = new HintCategory(m.category));
            }
            
            Collection<HintMetadata> catNode = cat2Hints.get(cat);

            if (catNode == null) {
                cat2Hints.put(cat, catNode = new TreeSet<HintMetadata>(new Comparator<HintMetadata>() {
                    public int compare(HintMetadata o1, HintMetadata o2) {
                        return o1.displayName.compareToIgnoreCase(o2.displayName);
                    }
                }));
            }

            catNode.add(m);
        }

        for (Entry<HintCategory, Collection<HintMetadata>> e : cat2Hints.entrySet()) {
            DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(e.getKey());

            for (HintMetadata hm : e.getValue()) {
                DefaultMutableTreeNode hmNode = new DefaultMutableTreeNode(hm);

                catNode.add(hmNode);
                hint2Path.put(hm, new TreePath(new Object[] {root, catNode, hmNode}));
            }

            root.add(catNode);
        }

        if (allHints)
        root.add(extraNode);
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root) {

            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
                DefaultMutableTreeNode o = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (o.getUserObject() instanceof HintMetadata) {
                    HintMetadata hint = (HintMetadata) o.getUserObject();
                    throw new UnsupportedOperationException("Not implemented yet");
                    
                }
            }
        };
        
        return defaultTreeModel;
    }

    void select(HintMetadata hm) {
        if (errorTree == null) {
            //lazy init:
            toSelect = hm;
            return;
        }
        
	TreePath path = hint2Path.get(hm);
	
        errorTree.setSelectionPath(path);
	errorTree.scrollPathToVisible(path);
    }

    private static int compare(String s1, String s2) {
        return clearNonAlpha(s1).compareToIgnoreCase(clearNonAlpha(s2));
    }

    private static String clearNonAlpha(String str) {
        StringBuilder sb = new StringBuilder(str.length());

        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private DataObject getDataObject(HintMetadata selectedHint) {
        String fileName = selectedHint.id.indexOf('-') != (-1) ? selectedHint.id.substring(0,selectedHint.id.lastIndexOf('-')) : selectedHint.id; //XXX
        FileObject fo = FileUtil.getConfigFile("rules/" + fileName);
        try {
            return DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static final class AcceptorImpl implements Acceptor, org.netbeans.modules.java.hints.jackpot.impl.refactoring.OptionsFilter.Acceptor {

        public boolean accept(Object originalTreeNode, String filterText) {
            if (filterText.isEmpty()) return true;
            
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) originalTreeNode;
            HintMetadata hm = (HintMetadata) n.getUserObject();

            filterText = filterText.toLowerCase();

            if (hm.displayName.toLowerCase().contains(filterText)) {
                return true;
            }

            if (hm.description.toLowerCase().contains(filterText)) {
                return true;
            }

            for (String sw : hm.suppressWarnings) {
                if (sw.toLowerCase().contains(filterText)) {
                    return true;
                }
            }

            return false;
        }
    }

    public Configuration getSelectedConfiguration() {
        return (Configuration) configCombo.getSelectedItem();
    }
    
    public HintMetadata getSelectedHint() {
        TreePath selectionPath = errorTree.getSelectionModel().getSelectionPath();
        if (selectionPath==null) {
            return null;
}
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) (MutableTreeNode) (TreeNode) selectionPath.getLastPathComponent();
        if (lastPathComponent!= null && lastPathComponent.getUserObject() instanceof HintMetadata)
            return (HintMetadata) lastPathComponent.getUserObject();
        return null;
    }
    
    private class RemoveHint extends AbstractAction {

        HintMetadata hint;
        DefaultMutableTreeNode node;

        public RemoveHint(DefaultMutableTreeNode node, HintMetadata hint) {
            super(NbBundle.getMessage(RemoveHint.class, "CTL_Delete"));
            this.hint = hint;
            this.node = node;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                getDataObject(hint).delete();
                RulesManager.getInstance().allHints.remove(hint);
                //errorTreeModel.removeNodeFromParent(node);
                errorTreeModel = constructTM(Utilities.getBatchSupportedHints(), false);
                errorTree.setModel(errorTreeModel);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }
    
    private class RenameHint extends AbstractAction {

        HintMetadata hint;
        DefaultMutableTreeNode node;
        TreePath path;

        public RenameHint(DefaultMutableTreeNode node, HintMetadata hint, TreePath path) {
            super(NbBundle.getMessage(RemoveHint.class, "CTL_Rename"));
            this.hint = hint;
            this.node = node;
            this.path = path;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            errorTree.startEditingAtPath(path);
        }
    }
 
    private static class EditableJTree extends JTree {

        public EditableJTree() {
        }

        @Override
        public boolean isPathEditable(TreePath path) {

            DefaultMutableTreeNode o = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (o.getUserObject() instanceof HintMetadata) {
                HintMetadata hint = (HintMetadata) o.getUserObject();
                if (hint.category.equals("custom")) {
                    return true;
                }
            }
            return false;
        }
    }
}
