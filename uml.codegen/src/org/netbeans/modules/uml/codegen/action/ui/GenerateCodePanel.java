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

package org.netbeans.modules.uml.codegen.action.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;

import org.netbeans.modules.uml.codegen.CodeGenUtil;
import org.netbeans.modules.uml.codegen.action.GenerateCodeAction;
import org.netbeans.modules.uml.codegen.action.GenerateCodeDescriptor;
import org.netbeans.modules.uml.codegen.ui.customizer.TabbedPanel;
import org.netbeans.modules.uml.codegen.ui.customizer.TemplateModel;
import org.netbeans.modules.uml.codegen.ui.customizer.VerticalTabbedPanel;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;


/**
 *
 * @author  Craig Conover, craig.conover@sun.com
 */
public class GenerateCodePanel extends javax.swing.JPanel
    implements ActionListener, PropertyChangeListener, DocumentListener
{
    public final static String PROP_TARGET_PROJECT = "TARGET_PROJECT"; // NOI18N
    public final static String PROP_NO_TARGET_PROJECT = "NO_TARGET_PROJECT"; // NOI18N
    public final static String PROP_SOURCE_FOLDER = "SOURCE_FOLDER"; // NOI18N
    public final static String PROP_NO_SOURCE_FOLDER = "NO_SOURCE_FOLDER"; // NOI18N
    
    private VerticalTabbedPanel templateFamilies = null;
    private TemplateModel model = null;
    private UMLProject umlProject = null;

    private Project targetPrj = null;
    private Project origPrj = null;
    private SourceGroup srcGroup = null;
    private boolean noTargetProject = false;
    private boolean noOpenTargets = false;
    private boolean noTemplatesEnabled = false;
    
    public GenerateCodePanel(
        boolean isCollapsable, 
        UMLProjectProperties prjProps, 
        UMLProject umlProject)
    {
        initComponents();
        propertyChangeSupport = new PropertyChangeSupport(this);
        this.umlProject = umlProject;
        statusLabel.setText(""); // NIO18N
        
        configureTargetProject(prjProps);

//        if (targetFolderName == null || targetFolderName.length() == 0)
//            targetFolderName = retrieveFolderLocationDefault(prjProps);
//        
//        // added target project comps
//        if (setTargetElementsFromFolder(targetFolderName))
//            targetPrj = origPrj;

        populateExistingProjectElementGroup();
        // end added target project comps
                        
        backupSourcesCheck.setSelected(prjProps.isCodeGenBackupSources());
        generateMarkersCheck.setSelected(prjProps.isCodeGenUseMarkers());
        addMarkersCheck.setSelected(prjProps.isCodeGenAddMarkers());
        addMarkersCheck.setEnabled(prjProps.isCodeGenUseMarkers());
        showDialogCheckBox.setSelected(prjProps.isCodeGenShowDialog());
        
        scrollPlaceHolder.setVisible(true);
        templatesLabel.setVisible(true);
        //statusLabel.setVisible(true);
            
        prjProps.setCodeGenTemplates(CodeGenUtil.cleanProjectTemplatesList(
            (ArrayList<String>)prjProps.getCodeGenTemplatesArray()));
        prjProps.save();
        
        model = new TemplateModel(prjProps);
        model.getPropertyChangeSupport().addPropertyChangeListener(this);
        getPropertyChangeSupport().addPropertyChangeListener(this);
        
        templateFamilies = new VerticalTabbedPanel(model, TabbedPanel.EXPAND_ALL);
        panelPlaceHolder.add(templateFamilies, BorderLayout.CENTER);
        
        noTemplatesEnabled = !CodeGenUtil.areTemplatesEnabled(
            prjProps.getCodeGenTemplatesArray());
        
        if (isCollapsable)
        {
            if (!noTemplatesEnabled)
            {
                setSize(getWidth(), getHeight() - 200);
                scrollPlaceHolder.setVisible(false);
                templatesLabel.setVisible(false);
            }

            else
            {
                statusLabel.setText(NbBundle.getMessage(
                    GenerateCodePanel.class, "MSG_AtLeastOneTemplate")); // NIO18N
            }
        }
        
        statusLabel.setVisible(true);
    }

    @Override
    public void requestFocus()
    {
        propertyChange(null);
    }
    
    
    private void configureTargetProject(UMLProjectProperties prjProps)
    {
        String targetFolderName = prjProps.getCodeGenFolderLocation();
        
        if (targetFolderName != null && targetFolderName.length() > 0)
        {
            File normalizedFile = FileUtil.normalizeFile(new File(targetFolderName));
            FileObject targetSrcFolderFO = 
                FileUtil.toFileObject(normalizedFile);

            if (targetSrcFolderFO == null || !targetSrcFolderFO.isValid())
            {
                targetFolderName = retrieveFolderLocationDefault(prjProps);
            }
        }
        else 
        {
            targetFolderName = retrieveFolderLocationDefault(prjProps);
        }

        if (targetFolderName != null && targetFolderName.length() > 0)
        {
            setTargetElementsFromFolder(targetFolderName);
            targetPrj = origPrj;
        }
        else
        {
            noTargetProject = true;            
        }
        
        if (noTargetProject)
        {
            statusLabel.setText(NbBundle.getMessage(
                GenerateCodePanel.class, "MSG_NoTargetJavaProject")); // NIO18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        targetGroup = new javax.swing.ButtonGroup();
        backupSourcesCheck = new javax.swing.JCheckBox();
        generateMarkersCheck = new javax.swing.JCheckBox();
        addMarkersCheck = new javax.swing.JCheckBox();
        showDialogCheckBox = new javax.swing.JCheckBox();
        templatesLabel = new javax.swing.JLabel();
        scrollPlaceHolder = new javax.swing.JScrollPane();
        panelPlaceHolder = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        targetProjectCombo = new javax.swing.JComboBox();
        srcFolderCombo = new javax.swing.JComboBox();
        targetProject = new javax.swing.JLabel();
        srcFolder = new javax.swing.JLabel();

        backupSourcesCheck.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/codegen/action/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(backupSourcesCheck, bundle.getString("LBL_BackupSourcesCheckBox")); // NOI18N
        backupSourcesCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        generateMarkersCheck.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateMarkersCheck, bundle.getString("LBL_GenerateMarkersCheckBox")); // NOI18N
        generateMarkersCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        generateMarkersCheck.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                generateMarkersActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addMarkersCheck, org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_AddMergeMarkers")); // NOI18N
        addMarkersCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        showDialogCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(showDialogCheckBox, org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_GenCodeShowDialog")); // NOI18N
        showDialogCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        templatesLabel.setLabelFor(panelPlaceHolder);
        org.openide.awt.Mnemonics.setLocalizedText(templatesLabel, org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_TemplatesLabel")); // NOI18N

        panelPlaceHolder.setLayout(new java.awt.BorderLayout());
        scrollPlaceHolder.setViewportView(panelPlaceHolder);
        panelPlaceHolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_TemplatesPanel")); // NOI18N
        panelPlaceHolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSD_TemplatesPanel")); // NOI18N

        statusLabel.setForeground(new java.awt.Color(255, 0, 0));
        statusLabel.setText("<status message>");

        targetProjectCombo.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                targetProjectComboItemStateChanged(evt);
            }
        });

        srcFolderCombo.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                srcFolderComboItemStateChanged(evt);
            }
        });

        targetProject.setLabelFor(targetProjectCombo);
        org.openide.awt.Mnemonics.setLocalizedText(targetProject, org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_TargetProject")); // NOI18N

        srcFolder.setLabelFor(srcFolderCombo);
        org.openide.awt.Mnemonics.setLocalizedText(srcFolder, org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_SourceFolder")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollPlaceHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .add(templatesLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, showDialogCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, addMarkersCheck, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 321, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, generateMarkersCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, backupSourcesCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, srcFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, targetProject, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(srcFolderCombo, 0, 219, Short.MAX_VALUE)
                                    .add(targetProjectCombo, 0, 219, Short.MAX_VALUE))))
                        .add(83, 83, 83)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(targetProject)
                    .add(targetProjectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(srcFolder)
                    .add(srcFolderCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(7, 7, 7)
                .add(backupSourcesCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(generateMarkersCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addMarkersCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showDialogCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(templatesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPlaceHolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 226, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        backupSourcesCheck.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_BackupFiles")); // NOI18N
        backupSourcesCheck.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BackupSourcesCheckBox")); // NOI18N
        generateMarkersCheck.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_GenMarkersMerge")); // NOI18N
        generateMarkersCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSD_GenMakersMerge")); // NOI18N
        addMarkersCheck.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ASCN_AddMergeMarkers")); // NOI18N
        addMarkersCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSD_AddMergeMarkers")); // NOI18N
        showDialogCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_PromptDialog")); // NOI18N
        showDialogCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSD_PromptDialog")); // NOI18N
        statusLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_StatusMessage")); // NOI18N
        statusLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSD_StatusMessage")); // NOI18N
        targetProjectCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSD_TargetProject_Desc")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_GenCodeDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ASCD_ExportCodePanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void generateMarkersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateMarkersActionPerformed
        if (addMarkersCheck != null)
        {
            if (!generateMarkersCheck.isSelected())
                addMarkersCheck.setSelected(false);

            addMarkersCheck.setEnabled(generateMarkersCheck.isSelected());
        }
    }//GEN-LAST:event_generateMarkersActionPerformed

    private void targetProjectComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_targetProjectComboItemStateChanged
        // TODO add your handling code here:
        targetPrj = (Project)targetProjectCombo.getSelectedItem();
        populateSourceFolderCombo(targetPrj);
        
        String prop = targetPrj == null 
            ? PROP_NO_TARGET_PROJECT : PROP_TARGET_PROJECT;
        
        getPropertyChangeSupport().firePropertyChange(prop , null, evt);
    }//GEN-LAST:event_targetProjectComboItemStateChanged

    private void srcFolderComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_srcFolderComboItemStateChanged
        // TODO add your handling code here:
        srcGroup = (SourceGroup)srcFolderCombo.getSelectedItem();
    }//GEN-LAST:event_srcFolderComboItemStateChanged
    
    public void actionPerformed(ActionEvent actionEvent)
    {
        if (actionEvent.getActionCommand().equals("OK")) // NOI18N
            storeProjectProperties();
        
        else if (actionEvent.getActionCommand().equals("TEMPLATES")) // NOI18N
        {
            JButton button = (JButton)actionEvent.getSource();
            Container topCont = getTopLevelAncestor();
            scrollPlaceHolder.setVisible(!scrollPlaceHolder.isVisible());
            templatesLabel.setVisible(scrollPlaceHolder.isVisible());

            if (scrollPlaceHolder.isVisible())
            {
                button.setText(NbBundle.getMessage(
                    GenerateCodeAction.class, "LBL_TemplatesHideButton")); // NOI18N

                Mnemonics.setLocalizedText(button, NbBundle.getMessage(
                    GenerateCodeAction.class, "LBL_TemplatesHideButton")); // NOI18N

                setSize(getWidth(), getHeight() + 200);
                topCont.setSize(topCont.getWidth(), topCont.getHeight() + 250);
            }

            else
            {
                button.setText(NbBundle.getMessage(
                    GenerateCodeAction.class, "LBL_TemplatesShowButton")); // NOI18N

                Mnemonics.setLocalizedText(button, NbBundle.getMessage(
                    GenerateCodeAction.class, "LBL_TemplatesShowButton")); // NOI18N

                setSize(getWidth(), getHeight() - 200);
                topCont.setSize(topCont.getWidth(), topCont.getHeight() - 250);
            }

            topCont.doLayout();
        }

    }

    public void storeProjectProperties() 
    { 
        UMLProjectProperties props = model.getUMLProjectProperties();
        props.setCodeGenFolderLocation(getSelectedFolderName());
        props.setCodeGenBackupSources(isBackupSources());
        props.setCodeGenUseMarkers(isGenerateMarkers());
        props.setCodeGenShowDialog(isShowDialog());
        props.setCodeGenAddMarkers(isAddMarkers());
        props.save();
    }
    
    public String getSelectedFolderName()
    {
        try
        {
            if (srcGroup != null && srcGroup.getRootFolder() != null)
                return FileUtil.toFile(srcGroup.getRootFolder()).getCanonicalPath();
        }
        
        catch (IOException e)
        {}
        
        return null;
    }

    public boolean isBackupSources()
    {
        return backupSourcesCheck != null ? backupSourcesCheck.isSelected() : true;
    }
    
    public boolean isGenerateMarkers()
    {
        return generateMarkersCheck != null ? generateMarkersCheck.isSelected() : true;
    }
    
    public boolean isAddMarkers()
    {
        return addMarkersCheck != null ? addMarkersCheck.isSelected() : false;
    }
    
    public boolean isShowDialog()
    {
        return showDialogCheckBox != null ? showDialogCheckBox.isSelected() : true;
    }
    

    private String retrieveFolderLocationDefault(UMLProjectProperties prjProps)
    {
        File javaSrcRootFolder = prjProps.getJavaSourceRootFolder();
        
        if (javaSrcRootFolder != null)
            return javaSrcRootFolder.getPath();
        
        return prjProps.getCodeGenFolderLocation();
    }

    
    public void changedUpdate(DocumentEvent event)
    {}
    
    public void insertUpdate(DocumentEvent event)
    {
        //String text = locationText.getText();
        //noSourceFolder = !(text != null && text.length() > 0);
        //propertyChange(null);
    }
    
    public void removeUpdate(DocumentEvent event)
    {
        //String text = locationText.getText();
        //noSourceFolder = !(text != null && text.length() > 0);
        //propertyChange(null);
    }
    
    public void propertyChange(PropertyChangeEvent event)
    {
        String propName = "";
        
        if (event != null)
            propName = event.getPropertyName();
        
        if (propName.equals(TemplateModel.PROP_NO_TEMPLATES_ENABLED))
            noTemplatesEnabled = true;
        
        else if (propName.equals(TemplateModel.PROP_ONE_TEMPLATE_ENABLED))
            noTemplatesEnabled = false;

        else if (propName.equals(PROP_TARGET_PROJECT))
            noTargetProject = false;

        else if (propName.equals(PROP_NO_TARGET_PROJECT))
            noTargetProject = true;

        String msg = "";

        
        if (noOpenTargets)
        {
            msg = NbBundle.getMessage(
                GenerateCodePanel.class, "MSG_NoOpenTargets"); // NIO18N
        }
        
        else if (noTargetProject)
        {
            msg = NbBundle.getMessage(
                GenerateCodePanel.class, "MSG_NoTargetJavaProject"); // NIO18N
        }
        
        else if (noTemplatesEnabled)
        {
            msg = NbBundle.getMessage(
                GenerateCodePanel.class, "MSG_AtLeastOneTemplate"); // NIO18N
        }

        statusLabel.setText(msg);
        
        statusLabel.setVisible(
            noTargetProject || noOpenTargets || noTemplatesEnabled);
        
        firePropertyChange(GenerateCodeDescriptor.PROP_VALID, null, 
            !(noTargetProject || noOpenTargets || noTemplatesEnabled));
    }

    
    //
    // target project added elements processing
    //

    private void enableExistingProjectElementGroup(boolean enable)
    {
        targetProjectCombo.setEnabled(enable);
        srcFolderCombo.setEnabled(enable);
    }


    private void populateExistingProjectElementGroup()
    {
        ProjectCellRenderer projectCellRenderer 
            = new ProjectCellRenderer(targetProjectCombo.getRenderer());
        targetProjectCombo.setRenderer(projectCellRenderer);
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        ArrayList<Project> list = new ArrayList<Project>();
        
        if (openProjects != null)
        {
            for (Project prj : openProjects)
            {
                Sources sources = ProjectUtils.getSources(prj);
                if (sources == null)
                    continue;
                
                SourceGroup[] srcGrps = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
                
                if (srcGrps != null && srcGrps.length > 0)
                    list.add(prj);
            }
        }

        if (origPrj != null && !list.contains(origPrj))
            list.add(origPrj);
        
        if (list == null || list.size() == 0)
        {
            noOpenTargets = true;
            enableExistingProjectElementGroup(false);
        }
        
        else
        {
            //list.add(0, null);
            
            DefaultComboBoxModel projectsModel = 
                new DefaultComboBoxModel(list.toArray());
        
            targetProjectCombo.setModel(projectsModel);
            targetProjectCombo.setSelectedIndex(-1);

            // When the selected index was set to -1 it reset the targetPrj
            // value.  Since the targetPrj was simply initialized with the
            // origPrj value, just set it again.
            targetPrj = origPrj;
            selectTargetProject();
            noOpenTargets = false;
            // enableExistingProjectElementGroup(true);
        }

        propertyChange(null);
    }

    private void selectTargetProject()
    {
        if (targetPrj == null)
        {
            if (targetProjectCombo.getSelectedItem() != null)
            {
                targetPrj = (Project) targetProjectCombo.getSelectedItem();
                srcFolderCombo.setEnabled(true);
            }
            
            else
            {
                srcFolderCombo.setEnabled(false);
            }
        }
        
        else
        {
            targetProjectCombo.setSelectedItem(targetPrj);
            srcFolderCombo.setEnabled(true);
        }
        
        if (targetProjectCombo.getSelectedItem() != null)
        {
            Project prj = (Project)targetProjectCombo.getSelectedItem();
            populateSourceFolderCombo(prj);
            srcFolderCombo.setEnabled(true);
        }
    }

    private void populateSourceFolderCombo(Project prj)
    {
        SourceRootCellRenderer srcCellRenderer 
            = new SourceRootCellRenderer(srcFolderCombo.getRenderer());
        srcFolderCombo.setRenderer(srcCellRenderer);
        ArrayList<SourceGroup> srcRoots = new ArrayList<SourceGroup>();
        int index = 0;
        FileObject sfo = null;
        
        if (srcGroup != null)
        {
            sfo = srcGroup.getRootFolder();
        }
        
        if (prj != null)
        {
            Sources sources = ProjectUtils.getSources(prj);
            if (sources != null)
            {
                SourceGroup[] srcGrps = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
                
                if (srcGrps != null)
                {
                    for (SourceGroup g : srcGrps)
                    {
                        if (g != null)
                        {
                            srcRoots.add(g);
                            
                            if (g.getRootFolder() != null && 
                                g.getRootFolder().equals(sfo))
                            {
                                index = srcRoots.size() - 1;
                            }
                        }
                    }
                }
            }
        }
        
        DefaultComboBoxModel rootsModel = 
            new DefaultComboBoxModel(srcRoots.toArray());
        
        srcFolderCombo.setModel(rootsModel);
        
        if (srcRoots.size() > 0)
        {
            srcFolderCombo.setSelectedIndex(index);
            srcGroup = srcRoots.get(index);
            srcFolderCombo.setEnabled(true);
        }
        
        else
        {
            srcFolderCombo.setEnabled(false);
        }
    }

    private boolean setTargetElementsFromFolder(String path)
    {
        if (path == null)
            return false;
        
        FileObject fo = null;
        
        try
        {
            File normalizedFile = FileUtil.normalizeFile(new File(path));
            fo = FileUtil.toFileObject(new File(normalizedFile.getCanonicalPath()));
        }
        
        catch (IOException ex)
        {
            return false;
        }
        catch (Exception e)
        {
            return false;
        }
        if (fo == null)
            return false;
        
        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null)
            return false;
        
        Sources sources = ProjectUtils.getSources(prj);
        if (sources == null)
            return false;
        
        SourceGroup[] srcGrps = sources.getSourceGroups(
            JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        if (srcGrps != null && srcGrps.length > 0)
        {
            for (SourceGroup g : srcGrps)
            {
                FileObject root = g.getRootFolder();

                if (!fo.equals(root))
                    continue;

                else
                {
                    origPrj = prj;
                    srcGroup = g;
                    return true;
                }
            }
        }

        return false;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addMarkersCheck;
    private javax.swing.JCheckBox backupSourcesCheck;
    private javax.swing.JCheckBox generateMarkersCheck;
    private javax.swing.JPanel panelPlaceHolder;
    private javax.swing.JScrollPane scrollPlaceHolder;
    private javax.swing.JCheckBox showDialogCheckBox;
    private javax.swing.JLabel srcFolder;
    private javax.swing.JComboBox srcFolderCombo;
    private javax.swing.JLabel statusLabel;
    private javax.swing.ButtonGroup targetGroup;
    private javax.swing.JLabel targetProject;
    private javax.swing.JComboBox targetProjectCombo;
    private javax.swing.JLabel templatesLabel;
    // End of variables declaration//GEN-END:variables
 


    /**
     * copy from ReverseEngineerPanel.java
     */
    private static class ProjectCellRenderer extends JLabel 
        implements ListCellRenderer
    {
        ListCellRenderer renderer;

        public ProjectCellRenderer(ListCellRenderer hostRenderer)
        {
            renderer = hostRenderer;
        }
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            Component comp = null;

            if (renderer != null) 
            {
                comp = renderer.getListCellRendererComponent(list, value, index, 
                                                             isSelected, cellHasFocus);
            }
            
            JLabel label = null;

            if (comp instanceof JLabel) 
            {                
                label = (JLabel) comp;
            }
            else 
            {
                label = this;
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
                setOpaque(true);
            }

            if (value instanceof Project)
            {
                ProjectInformation pi = 
                    ProjectUtils.getInformation((Project)value);
                
                label.setText(pi.getDisplayName());
                label.setIcon(pi.getIcon());
            }
            else
            {
                label.setText( value == null ? " " : value.toString() ); // NOI18N
                label.setIcon( null );
            }
            return label;                  
        }
    }


    private static class SourceRootCellRenderer extends JLabel 
        implements ListCellRenderer
    {

        ListCellRenderer renderer;

        public SourceRootCellRenderer(ListCellRenderer hostRenderer)
        {
            renderer = hostRenderer;
        }

        public Component getListCellRendererComponent(
            JList list, 
            Object value, 
            int index, 
            boolean isSelected, 
            boolean cellHasFocus)
        {

            Component comp = null;

            if (renderer != null) 
            {
                comp = renderer.getListCellRendererComponent(list, value, index, 
                                                             isSelected, cellHasFocus);
            }
            
            JLabel label = null;

            if (comp instanceof JLabel) 
            {                
                label = (JLabel) comp;
            }
            else 
            {
                label = this;
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
                setOpaque(true);
            }

            if (value instanceof SourceGroup)
            {
                SourceGroup sg = (SourceGroup) value;
                String desc = sg.getDisplayName();
                if (desc == null || desc.length() == 0)
                {
                    FileObject fo = sg.getRootFolder();
                    desc = fo.getPath();
                }
                label.setText(desc);
            }
            else
            {
                label.setText(value == null ? " " : value.toString()); // NOI18N
            }            
            return label;
        }
    }
    
    private PropertyChangeSupport propertyChangeSupport = null;
    
    public PropertyChangeSupport getPropertyChangeSupport()
    {
        return propertyChangeSupport;
    }

    
//    private String findValidParentFolder(String startFolder)
//    {
//        File file = new File(startFolder);
//        
//        if (startFolder == null || startFolder.length() == 0)
//            return null;
//        
//        if (file.exists())
//            return file.getPath();
//        
//        else
//            return findValidParentFolder(new File(file.getParent()).getParent());
//    }

//    private String suggestedGenCodeFolder(
//        UMLProjectProperties projectProps) 
//    {
//        // if the export source folder hasn't been saved to file yet,
//        // provide a suggestion for a place to put the sources
//
//        String genCodeFolder = null;
//        if (umlProject != null)
//            genCodeFolder = umlProject.getProjectDirectory().getParent().getPath();
//        
//        if (genCodeFolder == null || genCodeFolder.length() == 0)
//        {
//            // defensive code: if all else fails use user's home dir as the base dir
//            genCodeFolder = System.getProperty("user.home"); // NOI18N
//        }
//        
//        // append suggested Java project name + "src" dir
//        return genCodeFolder + File.separatorChar + projectProps.getProject().getName() + 
//            PROJECT_SOURCES_SUFFIX + File.separatorChar + PROJECT_SRC_FOLDER;
//    }
}
