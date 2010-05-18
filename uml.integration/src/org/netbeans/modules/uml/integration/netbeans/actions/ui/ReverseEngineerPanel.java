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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.integration.netbeans.actions.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.Document;
import org.netbeans.modules.uml.core.support.BundleSupport;

import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI.JavaSourceRootsModel;
import org.openide.util.NbBundle;


/**
 *
 * @author  Craig Conover, craig.conover@sun.com
 */
public class ReverseEngineerPanel extends javax.swing.JPanel
    implements ActionListener, DocumentListener, 
        ChangeListener, TableModelListener
{
    
    /**
     *  Creates a new ReverseEngineerPanel based on 
     *  a selected Java project node
     */
    public ReverseEngineerPanel(Project sourceProject)
    {
        this(sourceProject, null);
    }

    public ReverseEngineerPanel(
        Project sourceProject,
        Project targetProject)
    {
        javaProject = sourceProject;
        umlProject = targetProject;
        showSourceRoots = true;
        
        initComponents();
        initSourceRootsTable();
        initListeners();
        initValues();
        initJavaProjectValues();
    }

    
    public ReverseEngineerPanel(
        Project sourceProject, 
        ArrayList<String> sourceNodes, 
        Project targetProject)
    {
        javaProject = sourceProject;
        selectedNodes = sourceNodes;
        umlProject = targetProject;
        showSourceRoots = false;
        
        initComponents();
        // source roots selection is N/A for non-project node selection
        sourceRootsPanel.setVisible(false);
        initSourceRootsTable();
        initListeners();
        initValues();
        initJavaProjectValues();
        initJavaFilesValues();
    }

    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        projectTypeButtonGroup = new javax.swing.ButtonGroup();
        selNodesPanel = new javax.swing.JPanel();
        selectedNodesLabel = new javax.swing.JLabel();
        selectedNodesText = new javax.swing.JTextField();
        sourceRootsPanel = new javax.swing.JPanel();
        sourceFoldersLabel = new javax.swing.JLabel();
        sourceRootsScrollPane = new javax.swing.JScrollPane();
        sourceRoots = new javax.swing.JTable();
        existingProjectPanel = new javax.swing.JPanel();
        existingProjectRadio = new javax.swing.JRadioButton();
        targetProjectLabel = new javax.swing.JLabel();
        targetProjectCombo = new javax.swing.JComboBox();
        newProjectPanel = new javax.swing.JPanel();
        newProjectRadio = new javax.swing.JRadioButton();
        projectNameLabel = new javax.swing.JLabel();
        projectNameText = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationText = new javax.swing.JTextField();
        projectFolderLabel = new javax.swing.JLabel();
        projectFolderText = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();

        selectedNodesLabel.setLabelFor(selectedNodesText);
        org.openide.awt.Mnemonics.setLocalizedText(selectedNodesLabel, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_SelectedNodes")); // NOI18N
        selectedNodesLabel.getAccessibleContext().setAccessibleName("");
        selectedNodesLabel.getAccessibleContext().setAccessibleDescription("");

        selectedNodesText.setEditable(false);
        selectedNodesText.getAccessibleContext().setAccessibleName("");
        selectedNodesText.getAccessibleContext().setAccessibleDescription("");

        org.jdesktop.layout.GroupLayout selNodesPanelLayout = new org.jdesktop.layout.GroupLayout(selNodesPanel);
        selNodesPanel.setLayout(selNodesPanelLayout);
        selNodesPanelLayout.setHorizontalGroup(
            selNodesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selNodesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selectedNodesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectedNodesText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                .addContainerGap())
        );
        selNodesPanelLayout.setVerticalGroup(
            selNodesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selNodesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selNodesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectedNodesLabel)
                    .add(selectedNodesText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sourceFoldersLabel.setLabelFor(sourceRoots);
        org.openide.awt.Mnemonics.setLocalizedText(sourceFoldersLabel, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_JavaProjectSourcePackageFolders")); // NOI18N
        sourceFoldersLabel.getAccessibleContext().setAccessibleName("");
        sourceFoldersLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_JavaProjectSourcePackageFolders")); // NOI18N

        sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {new Boolean(true), null, null},
                {new Boolean(true), null, null}
            },
            new String []
            {
                "Reverse Engineer", "Package Folder", "Package Folder Label"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean []
            {
                true, false, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        sourceRootsScrollPane.setViewportView(sourceRoots);
        sourceRoots.getAccessibleContext().setAccessibleName("");
        sourceRoots.getAccessibleContext().setAccessibleDescription("");

        org.jdesktop.layout.GroupLayout sourceRootsPanelLayout = new org.jdesktop.layout.GroupLayout(sourceRootsPanel);
        sourceRootsPanel.setLayout(sourceRootsPanelLayout);
        sourceRootsPanelLayout.setHorizontalGroup(
            sourceRootsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceRootsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceRootsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sourceRootsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                    .add(sourceFoldersLabel))
                .addContainerGap())
        );
        sourceRootsPanelLayout.setVerticalGroup(
            sourceRootsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceRootsPanelLayout.createSequentialGroup()
                .add(sourceFoldersLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sourceRootsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        projectTypeButtonGroup.add(existingProjectRadio);
        existingProjectRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(existingProjectRadio, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_UseExistingUMLProject")); // NOI18N
        existingProjectRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        existingProjectRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        existingProjectRadio.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                existingProjectRadioStateChanged(evt);
            }
        });

        existingProjectRadio.getAccessibleContext().setAccessibleName("");
        existingProjectRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_UseExistingUMLProject")); // NOI18N

        targetProjectLabel.setLabelFor(targetProjectCombo);
        org.openide.awt.Mnemonics.setLocalizedText(targetProjectLabel, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_TargetProject")); // NOI18N
        targetProjectLabel.getAccessibleContext().setAccessibleName("");
        targetProjectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_TargetProject")); // NOI18N

        targetProjectCombo.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                targetProjectComboItemStateChanged(evt);
            }
        });

        targetProjectCombo.getAccessibleContext().setAccessibleName("");
        targetProjectCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_TargetProject")); // NOI18N

        org.jdesktop.layout.GroupLayout existingProjectPanelLayout = new org.jdesktop.layout.GroupLayout(existingProjectPanel);
        existingProjectPanel.setLayout(existingProjectPanelLayout);
        existingProjectPanelLayout.setHorizontalGroup(
            existingProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(existingProjectPanelLayout.createSequentialGroup()
                .add(existingProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(existingProjectPanelLayout.createSequentialGroup()
                        .add(27, 27, 27)
                        .add(targetProjectLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(targetProjectCombo, 0, 469, Short.MAX_VALUE))
                    .add(existingProjectPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(existingProjectRadio)))
                .addContainerGap())
        );
        existingProjectPanelLayout.setVerticalGroup(
            existingProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(existingProjectPanelLayout.createSequentialGroup()
                .add(existingProjectRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(existingProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(targetProjectLabel)
                    .add(targetProjectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        projectTypeButtonGroup.add(newProjectRadio);
        org.openide.awt.Mnemonics.setLocalizedText(newProjectRadio, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_CreateNewUMLProject")); // NOI18N
        newProjectRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        newProjectRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        newProjectRadio.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                newProjectRadioStateChanged(evt);
            }
        });

        newProjectRadio.getAccessibleContext().setAccessibleName("");
        newProjectRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_CreateNewUMLProject")); // NOI18N

        projectNameLabel.setLabelFor(projectNameText);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_ProjectName")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleName("");
        projectNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_ProjectName")); // NOI18N

        projectNameText.getAccessibleContext().setAccessibleName("");
        projectNameText.getAccessibleContext().setAccessibleDescription("");

        projectLocationLabel.setLabelFor(projectLocationText);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_ProjectLocation")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_ProjectLocation")); // NOI18N

        projectLocationText.getAccessibleContext().setAccessibleName("");
        projectLocationText.getAccessibleContext().setAccessibleDescription("");

        projectFolderLabel.setLabelFor(projectFolderText);
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_ProjectFolder")); // NOI18N
        projectFolderLabel.getAccessibleContext().setAccessibleName(null);
        projectFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ASCD_ProjectFolder")); // NOI18N

        projectFolderText.setEnabled(false);
        projectFolderText.getAccessibleContext().setAccessibleName("");
        projectFolderText.getAccessibleContext().setAccessibleDescription("");

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "LBL_BrowseButton")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                browseButtonActionPerformed(evt);
            }
        });

        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReverseEngineerPanel.class, "ACSD_BrowseButton")); // NOI18N

        org.jdesktop.layout.GroupLayout newProjectPanelLayout = new org.jdesktop.layout.GroupLayout(newProjectPanel);
        newProjectPanel.setLayout(newProjectPanelLayout);
        newProjectPanelLayout.setHorizontalGroup(
            newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newProjectPanelLayout.createSequentialGroup()
                .add(newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(newProjectPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(newProjectRadio))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, newProjectPanelLayout.createSequentialGroup()
                        .add(27, 27, 27)
                        .add(newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectLocationLabel)
                            .add(projectNameLabel)
                            .add(projectFolderLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectFolderText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                            .add(projectNameText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, projectLocationText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)))
                .addContainerGap())
        );
        newProjectPanelLayout.setVerticalGroup(
            newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newProjectPanelLayout.createSequentialGroup()
                .add(newProjectRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLocationLabel)
                    .add(browseButton)
                    .add(projectLocationText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newProjectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectFolderLabel)
                    .add(projectFolderText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        statusLabel.setForeground(new java.awt.Color(0, 0, 255));
        statusLabel.setText("<field validation status message>");
        statusLabel.getAccessibleContext().setAccessibleName("Dialog field validation message");
        statusLabel.getAccessibleContext().setAccessibleDescription("Reports the validation messages for all of the input fields of this dialog.");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selNodesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(sourceRootsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                .addContainerGap())
            .add(newProjectPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(existingProjectPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(selNodesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sourceRootsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(existingProjectPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newProjectPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void targetProjectComboItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_targetProjectComboItemStateChanged
    {//GEN-HEADEREND:event_targetProjectComboItemStateChanged
        umlProject = (Project)targetProjectCombo.getSelectedItem();
    }//GEN-LAST:event_targetProjectComboItemStateChanged

    private void newProjectRadioStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_newProjectRadioStateChanged
    {//GEN-HEADEREND:event_newProjectRadioStateChanged
        // updateValidStatus();
    }//GEN-LAST:event_newProjectRadioStateChanged

    private void existingProjectRadioStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_existingProjectRadioStateChanged
    {//GEN-HEADEREND:event_existingProjectRadioStateChanged
        // updateValidStatus();
    }//GEN-LAST:event_existingProjectRadioStateChanged

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseButtonActionPerformed
    {//GEN-HEADEREND:event_browseButtonActionPerformed
        javax.swing.JFrame parentFrame = new javax.swing.JFrame();
        parentFrame.setLocation(getLocationOnScreen());
        
        if (chooser == null)
        {
            chooser = new ChooseLocationDialog(
                parentFrame, true, new File(retrieveProjectParentDirectory()),
                NbBundle.getMessage(ReverseEngineerPanel.class, 
                    "LBL_RevEngProjectLocationChooseDialog_Title")); // NOI18N);
        }
        
        else 
            chooser.setFolderLocation(retrieveProjectParentDirectory());
        
        chooser.getLocationChooser().addActionListener(
            new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                    locationChooserActionPerformed(evt);
                }
            });
            
        chooser.setLocation(getLocationOnScreen());
        chooser.setVisible(true);
    }//GEN-LAST:event_browseButtonActionPerformed

    public void locationChooserActionPerformed(ActionEvent evt)
    {
        if (evt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
        {
            projectLocationText.setText(chooser.getFolderLocation().getPath());
        }
    }

    
    public void requestFocus()
    {
        targetProjectCombo.requestFocus();
        updateValidStatus();
    }
    
    private void initListeners()
    {
        projectNameText.getDocument().addDocumentListener(this);
        projectLocationText.getDocument().addDocumentListener(this);
        projectTypeButtonGroup.getSelection().addChangeListener(this);
        sourceRoots.getModel().addTableModelListener(this);
    }

    // DocumentListener interface implementation
    ////////////////////////////////////////////
    
    public void changedUpdate(DocumentEvent event)
    {
        updateTexts(event);
        
        if (projectNameText.getDocument() == event.getDocument())
        {
            firePropertyChange(
                PROP_PROJECT_NAME, null, projectNameText.getText());
       }
    }
    
    public void insertUpdate( DocumentEvent event )
    {
        updateTexts(event);
        
        if (projectNameText.getDocument() == event.getDocument())
        {
            firePropertyChange(
                PROP_PROJECT_NAME, null, projectNameText.getText());
        }
    }
    
    public void removeUpdate(DocumentEvent event)
    {
        updateTexts(event);
        
        if (projectNameText.getDocument() == event.getDocument())
        {
            firePropertyChange(
                PROP_PROJECT_NAME, null, projectNameText.getText());
        }
    }
    
    
    /** 
     * Handles changes in the Project name and project directory
     */
    private void updateTexts(DocumentEvent event)
    {
        Document doc = event.getDocument();
        
        if (doc == projectNameText.getDocument() ||
            doc == projectLocationText.getDocument())
        {
            // Change in the project name
            String projectName = projectNameText.getText();
            String projectFolder = projectLocationText.getText();
            
            projectFolderText.setText(
                projectFolder + File.separatorChar + projectName);

            updateValidStatus();
        }
    }
    

    private void initSourceRootsTable()
    {
        JavaSourceRootsModel model = JavaSourceRootsUI.createModel(javaProject);
        
        if (model != null)
            sourceRoots.setModel(model);

        else
            sourceRoots.setModel(JavaSourceRootsUI.createEmptyModel());
        
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        sourceRoots.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    
    private void initValues()
    {
        populateProjectList();
        enablePanelFields(targetProjectComboHasItems() && umlProject != null);
        populateNewProjectFields();
        statusLabel.setText(" "); // NOI18N
    }
    
    private void initJavaProjectValues()
    {
        selectedNodesText.setText(retrieveJavaProjectName());
    }
    
    private void initJavaFilesValues()
    {
        StringBuffer fileNames = new StringBuffer();
        
        if (selectedNodes == null || selectedNodes.size() == 0)
            fileNames.append("<Error: no nodes selected>");

        else
        {
            for (String fileName: selectedNodes)
                fileNames.append(fileName).append(", ");
            
            fileNames.delete(fileNames.lastIndexOf(", "), fileNames.length()-1);
        }
        
        selectedNodesText.setText(fileNames.toString());
    }

    private void populateProjectList()
    {
        ProjectCellRenderer projectCellRenderer = new ProjectCellRenderer();
        targetProjectCombo.setRenderer(projectCellRenderer);
        
        // Populate the combo box with list of open UML projects
        Project[] openProjects = ProjectUtil.getOpenUMLProjects();
        
        // no UML project open? then don't allow the Use Existing Project 
        //  radio button to be selected at all
        if (openProjects == null || openProjects.length <1)
        {
            enableExistingProjectPanelComponents(false);
            existingProjectRadio.setEnabled(false);
        }

        else
        {
            DefaultComboBoxModel projectsModel = 
                new DefaultComboBoxModel(openProjects);

            targetProjectCombo.setModel(projectsModel);
            selectTargetProject();
        }
    }
    
    
    private void populateNewProjectFields()
    {
        String prjParentDir = retrieveProjectParentDirectory();
        
        projectNameText.setText(ProjectUtil.createUniqueProjectName(
            new File(prjParentDir), 
            retrieveJavaProjectName() + NbBundle.getMessage(
                ReverseEngineerPanel.class, "TXT_RE_PROJECT_SUFFIX"), // NOI18N
            true));
        
        projectLocationText.setText(retrieveProjectParentDirectory());
    }
    

    private String retrieveJavaProjectName()
    {
        return ProjectUtils.getInformation(javaProject).getDisplayName();
    }
    
    private void selectTargetProject()
    {
        if (!targetProjectComboHasItems())
        {
            targetProjectCombo.setEnabled(false);
            return;
        }
        
        if (umlProject != null)
            targetProjectCombo.setSelectedItem(umlProject);

        else
            targetProjectCombo.setSelectedIndex(0);
    }

    // OK or Cancel button clicked
    public void actionPerformed(ActionEvent event)
    {
//        if (event.getActionCommand().equals("OK")) // NOI18N
//        {
//            // TODO: do something ???
//        }
    }

    public void stateChanged(ChangeEvent event)
    {
        enablePanelFields(existingProjectRadio.isSelected());
        updateValidStatus();
    }


    private void enablePanelFields(boolean enable)
    {
        enableExistingProjectPanelComponents(enable);
        enableNewProjectPanelComponents(!enable);
    }

    
    private void enableExistingProjectPanelComponents(boolean enable)
    {
        existingProjectRadio.setSelected(enable);
        targetProjectLabel.setEnabled(enable);
        targetProjectCombo.setEnabled(enable);
    
        if (enable)
            umlProject = (Project)targetProjectCombo.getSelectedItem();
        
        else
            umlProject = null;
    }

    private void enableNewProjectPanelComponents(boolean enable)
    {
        newProjectRadio.setSelected(enable);
        projectNameLabel.setEnabled(enable);
        projectNameText.setEnabled(enable);

        projectLocationLabel.setEnabled(enable);
        projectLocationText.setEnabled(enable);
        browseButton.setEnabled(enable);
        
        projectFolderLabel.setEnabled(enable);
    }

    public String getProjectFolder()
    {
        return projectFolderText.getText();
    }

    public String getProjectName()
    {
        return projectNameText.getText();
    }

    public Project getJavaProject()
    {
        return javaProject;
    }

    public JavaSourceRootsUI.JavaSourceRootsModel getSourceRoots()
    {
        return (JavaSourceRootsUI.JavaSourceRootsModel)sourceRoots.getModel();
    }

    public boolean isUseExistingProject()
    {
        return existingProjectRadio.isSelected();
    }

    public boolean isCreateNewProject()
    {
        return newProjectRadio.isSelected();
    }

    public Project getUMLProject()
    {
        return umlProject;
    }

    public boolean isShowSourceRoots()
    {
        return showSourceRoots;
    }
    
    private String retrieveProjectParentDirectory()
    {
        try
        {
            return FileUtil.toFile(javaProject.getProjectDirectory().getParent())
                .getCanonicalFile().getAbsolutePath();
        }
    
        catch (IOException ex)
        {
            // TODO: conover - provide proper handling
            ex.printStackTrace();
            return "<Java project parent folder IOException>"; // NOI18N
        }
    }

    private boolean targetProjectComboHasItems()
    {
        return targetProjectCombo.getModel() != null &&
            targetProjectCombo.getModel().getSize() > 0 ;
    }

    private boolean updateValidStatus()
    {
        enable = true;
        statusMsg = " "; // NOI18N
        
        if (newProjectRadio.isSelected())
        {
            if (!(new File(projectLocationText.getText()).exists()))
            {
               enable = Boolean.FALSE;
               statusMsg = "MSG_STATUS_ProjectLocationFolderDNE"; // NOI18N
               
               return notifyPropertyListeners();
            }

            else if (new File(projectFolderText.getText()).exists())
            {
               enable = Boolean.FALSE;
               statusMsg = "MSG_STATUS_ProjectExists"; // NOI18N
               
               return notifyPropertyListeners();
            }
        }
        
        if (showSourceRoots)
        {
            boolean atLeastOne = false;
            
            for (int i=0; i < sourceRoots.getModel().getRowCount(); i++)
            {
                if (((Boolean)sourceRoots.getModel()
                    .getValueAt(i, 0)).booleanValue())
                {
                    atLeastOne = true;
                    break;
                }
            }
            
            if (!atLeastOne)
            {
               enable = Boolean.FALSE;
               statusMsg = "MSG_STATUS_NoSourceGroups"; // NOI18N
               
               return notifyPropertyListeners();
            }
        }
        
        return notifyPropertyListeners();
    }
    
    private boolean notifyPropertyListeners()
    {
//        if (enable == valid)
//            return valid;
        
        firePropertyChange(
            NotifyDescriptor.PROP_VALID, valid, enable);
        
        if (!statusMsg.equals(" "))
            statusMsg = NbBundle.getMessage(
                ReverseEngineerPanel.class, statusMsg);
        
        statusLabel.setText(statusMsg);
        valid = enable;
        return valid;
    }

    public void tableChanged(TableModelEvent e)
    {
        updateValidStatus();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel existingProjectPanel;
    private javax.swing.JRadioButton existingProjectRadio;
    private javax.swing.JPanel newProjectPanel;
    private javax.swing.JRadioButton newProjectRadio;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectFolderText;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationText;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameText;
    private javax.swing.ButtonGroup projectTypeButtonGroup;
    private javax.swing.JPanel selNodesPanel;
    private javax.swing.JLabel selectedNodesLabel;
    private javax.swing.JTextField selectedNodesText;
    private javax.swing.JLabel sourceFoldersLabel;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JPanel sourceRootsPanel;
    private javax.swing.JScrollPane sourceRootsScrollPane;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JComboBox targetProjectCombo;
    private javax.swing.JLabel targetProjectLabel;
    // End of variables declaration//GEN-END:variables

    private Project javaProject;
    private Project umlProject;
    private ArrayList<String> selectedNodes;
    private boolean showSourceRoots = true;
    private ChooseLocationDialog chooser;
    private ReverseEngineerDescriptor descriptor;
    private boolean valid = true;
    private String statusMsg = " "; // NOI18N
    private boolean enable = false;
    
    
    private String PROP_PROJECT_NAME = "PROP_PROJECT_NAME"; // NOI18N


    
    private static class ProjectCellRenderer extends JLabel 
        implements ListCellRenderer
    {
        public ProjectCellRenderer()
        {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            
            if (value instanceof Project)
            {
                ProjectInformation pi = 
                    ProjectUtils.getInformation((Project)value);
                
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            }

            else
            {
                setText( value == null ? " " : value.toString() ); // NOI18N
                setIcon( null );
            }

            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                
            }
            
            return this;
        }
    }

}
