/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.codegen.action.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.uml.codegen.CodeGenUtil;
import org.netbeans.modules.uml.codegen.action.GenerateCodeAction;
import org.netbeans.modules.uml.codegen.action.GenerateCodeDescriptor;
import org.netbeans.modules.uml.codegen.ui.customizer.TabbedPanel;
import org.netbeans.modules.uml.codegen.ui.customizer.TemplateModel;
import org.netbeans.modules.uml.codegen.ui.customizer.VerticalTabbedPanel;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.awt.Mnemonics;

import org.openide.util.NbBundle;


import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListCellRenderer;


/**
 *
 * @author  Craig Conover, craig.conover@sun.com
 */
public class GenerateCodePanel extends javax.swing.JPanel
    implements ActionListener, PropertyChangeListener, DocumentListener
{
    public final static String PROP_SOURCE_FOLDER = "SOURCE_FOLDER"; // NOI18N
    public final static String PROP_NO_SOURCE_FOLDER = "NO_SOURCE_FOLDER"; // NOI18N
    
    private VerticalTabbedPanel templateFamilies = null;
    private TemplateModel model = null;
    private UMLProject umlProject = null;

    private Project targetPrj = null;
    private Project origPrj = null;
    private SourceGroup srcGroup = null;
    
    private final static String PROJECT_SOURCES_SUFFIX = 
        NbBundle.getMessage(GenerateCodePanel.class, 
        "TXT_Project_sources_suffix"); // NOI18N
    
    private final static String PROJECT_SRC_FOLDER = 
        NbBundle.getMessage(GenerateCodePanel.class, 
        "TXT_Project_src_folder"); // NOI18N
    
    public GenerateCodePanel(
        boolean isCollapsable, 
        UMLProjectProperties prjProps, 
        UMLProject umlProject)
    {
        initComponents();
        this.umlProject = umlProject;

	statusLabel.setText(""); // NIO18N

        String folder = prjProps.getCodeGenFolderLocation();

        if (folder == null || folder.length() == 0)
            folder = retrieveFolderLocationDefault(prjProps);
        
	// added target project comps
	if (setTargetElementsFromFolder(folder)) 
	{
	    targetPrj = origPrj;
	}
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
        
        templateFamilies = new VerticalTabbedPanel(model, TabbedPanel.EXPAND_ALL);
        panelPlaceHolder.add(templateFamilies, BorderLayout.CENTER);
        
        noTemplatesEnabled = !CodeGenUtil.areTemplatesEnabled(
            prjProps.getCodeGenTemplatesArray());
        
        if (isCollapsable)
        {
            if (!noTemplatesEnabled)
            {
//                advancedBtn.setText(NbBundle.getMessage(
//                    GenerateCodePanel.class, "LBL_Button_More")); // NOI18N
//                 org.openide.awt.Mnemonics.setLocalizedText(advancedBtn, 
//                     NbBundle.getMessage(GenerateCodePanel.class, "LBL_Button_More")); // NOI18N

                setSize(getWidth(), getHeight() - 200);
                scrollPlaceHolder.setVisible(false);
                templatesLabel.setVisible(false);
            }

            else
            {
//                advancedBtn.setText(NbBundle.getMessage(
//                    GenerateCodePanel.class, "LBL_Button_Less")); // NOI18N
//                 org.openide.awt.Mnemonics.setLocalizedText(advancedBtn, 
//                     NbBundle.getMessage(GenerateCodePanel.class, "LBL_Button_Less")); // NOI18N

                statusLabel.setText(NbBundle.getMessage(
                    GenerateCodePanel.class, "MSG_AtLeastOneTemplate")); // NIO18N
            }
        }
        
        else
        {
//            advancedBtn.setVisible(false);
        }
	statusLabel.setVisible(true);
    }

    public void requestFocus()
    {
        //locationText.requestFocus();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        backupSourcesCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        backupSourcesCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        generateMarkersCheck.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateMarkersCheck, bundle.getString("LBL_GenerateMarkersCheckBox")); // NOI18N
        generateMarkersCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        generateMarkersCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        generateMarkersCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateMarkersActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addMarkersCheck, org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_AddMergeMarkers")); // NOI18N
        addMarkersCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addMarkersCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        showDialogCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(showDialogCheckBox, org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_GenCodeShowDialog")); // NOI18N
        showDialogCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showDialogCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        templatesLabel.setLabelFor(panelPlaceHolder);
        templatesLabel.setText(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_TemplatesLabel")); // NOI18N

        panelPlaceHolder.setLayout(new java.awt.BorderLayout());
        scrollPlaceHolder.setViewportView(panelPlaceHolder);
        panelPlaceHolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_TemplatesPanel")); // NOI18N
        panelPlaceHolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSD_TemplatesPanel")); // NOI18N

        statusLabel.setForeground(new java.awt.Color(255, 0, 0));
        statusLabel.setText("<status message>");

        targetProjectCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                targetProjectComboItemStateChanged(evt);
            }
        });

        srcFolderCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                srcFolderComboItemStateChanged(evt);
            }
        });

        targetProject.setText(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_TargetProject")); // NOI18N

        srcFolder.setText(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "LBL_SourceFolder")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(targetProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(srcFolder))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(srcFolderCombo, 0, 523, Short.MAX_VALUE)
                            .add(targetProjectCombo, 0, 523, Short.MAX_VALUE)))
                    .add(scrollPlaceHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                    .add(showDialogCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                    .add(addMarkersCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                    .add(templatesLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, generateMarkersCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                    .add(backupSourcesCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
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
                .addContainerGap(20, Short.MAX_VALUE))
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

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_GenCodeDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ASCD_ExportCodePanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private ChooseLocationDialog chooser = null;
    
    private void generateMarkersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateMarkersActionPerformed
	if (addMarkersCheck != null) 
	{	    
	    if (! generateMarkersCheck.isSelected()) 
	    {
		addMarkersCheck.setSelected(false);
	    }
	    addMarkersCheck.setEnabled(generateMarkersCheck.isSelected());
	}
    }//GEN-LAST:event_generateMarkersActionPerformed

    private void targetProjectComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_targetProjectComboItemStateChanged
        // TODO add your handling code here:
	Project targetPrj = (Project)targetProjectCombo.getSelectedItem();
	populateSourceFolderCombo(targetPrj);
    }//GEN-LAST:event_targetProjectComboItemStateChanged

    private void srcFolderComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_srcFolderComboItemStateChanged
        // TODO add your handling code here:
	srcGroup = (SourceGroup)srcFolderCombo.getSelectedItem();
    }//GEN-LAST:event_srcFolderComboItemStateChanged

    
    private String findValidParentFolder(String startFolder)
    {
        File file = new File(startFolder);
        
        if (startFolder == null || startFolder.length() == 0)
            return null;
        
        if (file.exists())
            return file.getPath();
        
        else
            return findValidParentFolder(new File(file.getParent()).getParent());
    }
    
    
    public void locationChooserActionPerformed(ActionEvent evt)
    {
        if (evt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
        {
            //locationText.setText(chooser.getFolderLocation().getPath());
        }
    }
    
    public void actionPerformed(ActionEvent actionEvent)
    {
        if (actionEvent.getActionCommand().equals("OK")) // NOI18N
        {
            UMLProjectProperties props = model.getUMLProjectProperties();
            props.setCodeGenFolderLocation(getSelectedFolderName());
            props.setCodeGenBackupSources(isBackupSources());
            props.setCodeGenUseMarkers(isGenerateMarkers());
            props.setCodeGenShowDialog(isShowDialog());
            props.setCodeGenAddMarkers(isAddMarkers());
            props.save();
        }
        
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
    
    public String getSelectedFolderName()
    {
	if (srcGroup != null && srcGroup.getRootFolder() != null) 
	{
	    return srcGroup.getRootFolder().getPath();
	}
	return null;
    }

    public boolean isBackupSources()
    {
        return backupSourcesCheck.isSelected();
    }
    
    public boolean isGenerateMarkers()
    {
        return generateMarkersCheck.isSelected();
    }
    
    public boolean isAddMarkers()
    {
        return addMarkersCheck.isSelected();
    }
    
    public boolean isShowDialog()
    {
        return showDialogCheckBox.isSelected();
    }
    

    private String retrieveFolderLocationDefault(
        UMLProjectProperties projectProps)
    {
        // get target source folder location in private.properties,
        // if it has been set
        AntProjectHelper antHlp = 
            ProjectUtil.getAntProjectHelper(projectProps.getProject());
        
        // save target source folder location in private.properties
        EditableProperties edProps = antHlp.getProperties(
            AntProjectHelper.PRIVATE_PROPERTIES_PATH);

        File javaSrcRootFolder = projectProps.getJavaSourceRootFolder();
        
//        String javaPrjSrc = (String)edProps.get(
//            UMLProjectProperties.REFERENCED_JAVA_PROJECT_SRC);

        if (javaSrcRootFolder != null)
            return javaSrcRootFolder.getPath();
        
        String genCodeFolder = 
            edProps.getProperty(UMLProjectProperties.CODE_GEN_FOLDER_LOCATION);
        
	//if (genCodeFolder != null && genCodeFolder.length() > 0)
            return genCodeFolder;

    }

    private String suggestedGenCodeFolder(
        UMLProjectProperties projectProps) 
    {
        // if the export source folder hasn't been saved to file yet,
        // provide a suggestion for a place to put the sources

	String genCodeFolder = null;
        if (umlProject != null)
            genCodeFolder = umlProject.getProjectDirectory().getParent().getPath();
        
        if (genCodeFolder == null || genCodeFolder.length() == 0)
        {
            // defensive code: if all else fails use user's home dir as the base dir
            genCodeFolder = System.getProperty("user.home"); // NOI18N
        }
        // append suggested Java project name + "src" dir
        return genCodeFolder + File.separatorChar + projectProps.getProject().getName() + 
            PROJECT_SOURCES_SUFFIX + File.separatorChar + PROJECT_SRC_FOLDER;
    }

    
    private boolean noSourceFolder = false;
    private boolean noTemplatesEnabled = false;
    
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

        
        String msg = "";

        if (noSourceFolder)
            msg = NbBundle.getMessage(GenerateCodePanel.class, "MSG_NoSourceFolder"); // NIO18N
        
        else if (noTemplatesEnabled)
            msg = NbBundle.getMessage(GenerateCodePanel.class, "MSG_AtLeastOneTemplate"); // NIO18N
        

        statusLabel.setText(msg);
	statusLabel.setVisible(noSourceFolder || noTemplatesEnabled);
        firePropertyChange(GenerateCodeDescriptor.PROP_VALID, null, 
            !(noSourceFolder || noTemplatesEnabled));
    }

    
    //
    // target project added elements processing
    //

    private void enableExistingProjectElementGroup(boolean enable) 
    {
	targetProjectCombo.setEnabled(enable);
	srcFolderCombo.setEnabled(enable);
    }


    private void enablePlainFolderElementGroup(boolean enable) 
    {
	//locationText.setEnabled(enable);
    }


    private void populateExistingProjectElementGroup() 
    {
	
        ProjectCellRenderer projectCellRenderer = new ProjectCellRenderer();
        targetProjectCombo.setRenderer(projectCellRenderer);

	Project openProjects[] = OpenProjects.getDefault().getOpenProjects();
	ArrayList<Project> list = new ArrayList<Project>();
	if (openProjects != null) 
	{
	    for(Project prj : openProjects) 
	    {
		Sources sources = ProjectUtils.getSources(prj);
		if (sources == null)
		    continue;
            
		SourceGroup[] srcGrps = sources.getSourceGroups(
		    JavaProjectConstants.SOURCES_TYPE_JAVA);
		if (srcGrps != null && srcGrps.length > 0) 
		{
		    list.add(prj);
		}
	    }
	}
	if (origPrj != null && ! list.contains(origPrj)) 
	{
	    list.add(origPrj);
	}
 	DefaultComboBoxModel projectsModel = 
	    new DefaultComboBoxModel(list.toArray());
	targetProjectCombo.setModel(projectsModel);
	if (list.size() > 0) 
	{
	    selectTargetProject();
	}
	else 
	{
	    noSourceFolder = true;
	}
	propertyChange(null);
    }

    
    private void selectTargetProject()
    {	    
	if (targetPrj == null) 
	{
	    targetProjectCombo.setSelectedIndex(0);
	}
	else 
	{
	    targetProjectCombo.setSelectedItem(targetPrj);
	}
	Project prj = (Project)targetProjectCombo.getSelectedItem();
	populateSourceFolderCombo(prj);
    }

    private void populateSourceFolderCombo(Project prj)
    {
        SourceRootCellRenderer srcCellRenderer = new SourceRootCellRenderer();
        srcFolderCombo.setRenderer(srcCellRenderer);
	ArrayList<SourceGroup> srcRoots = new ArrayList<SourceGroup>(); 
	int index = 0;
	FileObject sfo = null;
	if (srcGroup != null) 
	{
	    sfo =  srcGroup.getRootFolder();
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
		    int i = 0;
		    for (SourceGroup g: srcGrps)
		    {			
			if (g != null) 
			{
			    srcRoots.add(g);
			    if (g.getRootFolder() != null 
				&& g.getRootFolder().equals(sfo))
			    {
				index = i;
			    }
			    i++;
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
	}
    }

    private boolean setTargetElementsFromFolder(String path) 
    {
	if (path == null) 
	{
	    return false;
	}
	FileObject fo = null;
	try 
	{
	    fo = FileUtil.toFileObject(new File(new File(path).getCanonicalPath()));
	}
	catch (IOException ex) 	
	{
	    return false;
	}
	if (fo == null) 
	{
	    return false;
	}
	Project prj = FileOwnerQuery.getOwner(fo);
	if (prj == null) 
	{
	    return false;
	}

	Sources sources = ProjectUtils.getSources(prj);
	if (sources == null)
	    return false;
	
	SourceGroup[] srcGrps = sources.getSourceGroups(
	    JavaProjectConstants.SOURCES_TYPE_JAVA);
	if (srcGrps != null && srcGrps.length > 0) 
	{
	    for (SourceGroup g: srcGrps)
	    {
		FileObject root = g.getRootFolder();;
		if (! fo.equals(root)) 
		{
		    continue;
		}
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


    private static class SourceRootCellRenderer extends JLabel 
        implements ListCellRenderer
    {
        public SourceRootCellRenderer()
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
            
            if (value instanceof SourceGroup)		
            {
		SourceGroup sg = (SourceGroup)value;
		String desc = sg.getDisplayName();
		if (desc == null || desc.length() == 0) 
		{
		    FileObject fo = sg.getRootFolder();
		    desc = fo.getPath();
		}
                setText(desc);
            }
            else
            {
                setText( value == null ? " " : value.toString() ); // NOI18N
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
