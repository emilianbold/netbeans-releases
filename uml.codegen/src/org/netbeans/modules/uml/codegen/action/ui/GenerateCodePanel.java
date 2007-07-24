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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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

        String folder = prjProps.getCodeGenFolderLocation();
        
        if (folder == null || folder.length() == 0)
            folder = retrieveFolderLocationDefault(prjProps);
        
        locationText.setText(folder);
        locationText.getDocument().addDocumentListener(this);
        
        noSourceFolder = !(locationText.getText() != null && 
            locationText.getText().length() > 0);
        
        backupSourcesCheck.setSelected(prjProps.isCodeGenBackupSources());
        generateMarkersCheck.setSelected(prjProps.isCodeGenUseMarkers());
        addMarkersCheck.setSelected(prjProps.isCodeGenAddMarkers());
        addMarkersCheck.setEnabled(prjProps.isCodeGenUseMarkers());
        showDialogCheckBox.setSelected(prjProps.isCodeGenShowDialog());
        
        scrollPlaceHolder.setVisible(true);
        templatesLabel.setVisible(true);
        statusLabel.setVisible(true);
            
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

                statusLabel.setText(""); // NIO18N
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
            statusLabel.setVisible(false);
        }
    }

    public void requestFocus()
    {
        locationText.requestFocus();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        locationLabel = new javax.swing.JLabel();
        locationText = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        backupSourcesCheck = new javax.swing.JCheckBox();
        generateMarkersCheck = new javax.swing.JCheckBox();
        addMarkersCheck = new javax.swing.JCheckBox();
        showDialogCheckBox = new javax.swing.JCheckBox();
        templatesLabel = new javax.swing.JLabel();
        scrollPlaceHolder = new javax.swing.JScrollPane();
        panelPlaceHolder = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();

        locationLabel.setLabelFor(locationText);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/codegen/action/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, bundle.getString("LBL_ExportLocationLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, bundle.getString("LBL_BrowseButton")); // NOI18N
        browseButton.setActionCommand("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        backupSourcesCheck.setSelected(true);
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, scrollPlaceHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(locationLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(locationText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, backupSourcesCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, generateMarkersCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, showDialogCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, addMarkersCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, templatesLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locationLabel)
                    .add(browseButton)
                    .add(locationText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(backupSourcesCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(generateMarkersCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addMarkersCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showDialogCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(templatesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPlaceHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel)
                .addContainerGap())
        );

        locationLabel.getAccessibleContext().setAccessibleName("");
        locationLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ExportFolder")); // NOI18N
        locationText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ACSN_FolderLocationText")); // NOI18N
        locationText.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ExportFolder")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GenerateCodePanel.class, "ASCN_BrowseButton")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BrowseButton")); // NOI18N
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
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseButtonActionPerformed
    {//GEN-HEADEREND:event_browseButtonActionPerformed
        javax.swing.JFrame parentFrame = new javax.swing.JFrame();
        parentFrame.setLocation(getLocationOnScreen());

        String validFolder = findValidParentFolder(locationText.getText());
        if (validFolder == null)
            locationText.getText();
        
        chooser = new ChooseLocationDialog(
            parentFrame, true, new File(validFolder), 
            NbBundle.getMessage(GenerateCodePanel.class, 
                "LBL_GenCodeSourceFolderChooseDialog_Title")); // NOI18N

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
            locationText.setText(chooser.getFolderLocation().getPath());
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
        return locationText.getText();
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
        
        if (genCodeFolder != null && genCodeFolder.length() > 0)
            return genCodeFolder;

        // if the export source folder hasn't been saved to file yet,
        // provide a suggestion for a place to put the sources

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
        String text = locationText.getText();
        noSourceFolder = !(text != null && text.length() > 0);
        propertyChange(null);
    }
    
    public void removeUpdate(DocumentEvent event)
    {
        String text = locationText.getText();
        noSourceFolder = !(text != null && text.length() > 0);
        propertyChange(null);
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
        firePropertyChange(GenerateCodeDescriptor.PROP_VALID, null, 
            !(noSourceFolder || noTemplatesEnabled));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addMarkersCheck;
    private javax.swing.JCheckBox backupSourcesCheck;
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox generateMarkersCheck;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField locationText;
    private javax.swing.JPanel panelPlaceHolder;
    private javax.swing.JScrollPane scrollPlaceHolder;
    private javax.swing.JCheckBox showDialogCheckBox;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel templatesLabel;
    // End of variables declaration//GEN-END:variables
    
}
