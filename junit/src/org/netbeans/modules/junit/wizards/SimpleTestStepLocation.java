/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.SizeRestrictedPanel;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
public class SimpleTestStepLocation implements WizardDescriptor.Panel {
    
    private Component visualComp;
    private List changeListeners;
    private JTextField tfClassToTest;
    private JButton btnBrowse;
    private JTextField tfTestClass;
    private JTextField tfProjectName;
    private JTextField tfCreatedFile;
    
    private JCheckBox chkPublic;
    private JCheckBox chkProtected;
    private JCheckBox chkPackagePrivate;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkMethodBodies;
    private JCheckBox chkJavadoc;
    private JCheckBox chkHints;
    
    private Project project;
        
    public SimpleTestStepLocation() {
        super();
        visualComp = createVisualComp();
    }
    
    private Component createVisualComp() {
        JLabel lblClassToTest = new JLabel();
        JLabel lblCreatedTestClass = new JLabel();
        JLabel lblProject = new JLabel();
        JLabel lblFile = new JLabel();
        tfClassToTest = new JTextField();
        btnBrowse = new JButton();
        tfTestClass = new JTextField();
        tfProjectName = new JTextField();
        tfCreatedFile = new JTextField();
        
        ResourceBundle bundle
                = NbBundle.getBundle(SimpleTestStepLocation.class);
        
        Mnemonics.setLocalizedText(lblClassToTest,
                                   bundle.getString("LBL_ClassToTest"));//NOI18N
        Mnemonics.setLocalizedText(lblCreatedTestClass,
                                   bundle.getString("LBL_TestClass"));  //NOI18N
        Mnemonics.setLocalizedText(lblProject,
                                   bundle.getString("LBL_Project"));    //NOI18N
        Mnemonics.setLocalizedText(lblFile,
                                   bundle.getString("LBL_CreatedFile"));//NOI18N
        Mnemonics.setLocalizedText(btnBrowse,
                                   bundle.getString("LBL_Browse"));     //NOI18N
        
        lblClassToTest.setLabelFor(tfClassToTest);
        lblCreatedTestClass.setLabelFor(tfTestClass);
        lblProject.setLabelFor(tfProjectName);
        lblFile.setLabelFor(tfCreatedFile);
        
        tfTestClass.setEditable(false);
        tfProjectName.setEditable(false);
        tfCreatedFile.setEditable(false);
        
        tfTestClass.setFocusable(false);
        tfProjectName.setFocusable(false);
        tfCreatedFile.setFocusable(false);
        
        JCheckBox[] chkBoxes;
        
        JComponent accessLevels = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupAccessLevels"),          //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_PUBLIC,
                        GuiUtils.CHK_PROTECTED,
                        GuiUtils.CHK_PACKAGE}));
        chkPublic = chkBoxes[0];
        chkProtected = chkBoxes[1];
        chkPackagePrivate = chkBoxes[2];
        
        JComponent optCode = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupOptCode"),               //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_SETUP,
                        GuiUtils.CHK_TEARDOWN,
                        GuiUtils.CHK_METHOD_BODIES}));
        chkSetUp = chkBoxes[0];
        chkTearDown = chkBoxes[1];
        chkMethodBodies = chkBoxes[2];
        
        JComponent optComments = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupOptComments"),           //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_JAVADOC,
                        GuiUtils.CHK_HINTS}));
        chkJavadoc = chkBoxes[0];
        chkHints = chkBoxes[1];
                        
        /* set layout of the components: */
        JPanel targetPanel
                = new SizeRestrictedPanel(new GridBagLayout(), false, true);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridy = 0;
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 6, 12);
        targetPanel.add(lblClassToTest, gbc);
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 6, 0);
        targetPanel.add(tfClassToTest, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 11, 6, 0);
        targetPanel.add(btnBrowse, gbc);
        
        gbc.gridy++;
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 24, 12);
        targetPanel.add(lblCreatedTestClass, gbc);
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 24, 0);
        targetPanel.add(tfTestClass, gbc);
        
        gbc.gridy++;
        
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 12, 12);
        targetPanel.add(lblProject, gbc);
        
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 12, 0);
        targetPanel.add(tfProjectName, gbc);
        
        gbc.gridy++;
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0d;
        gbc.insets = new Insets(0, 0, 0, 12);
        targetPanel.add(lblFile, gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 0, 0);
        targetPanel.add(tfCreatedFile, gbc);
        
        JComponent accessLevelsBox = new SizeRestrictedPanel(true, false);
        accessLevelsBox.setLayout(
                new BoxLayout(accessLevelsBox, BoxLayout.Y_AXIS));
        accessLevelsBox.add(accessLevels);
        accessLevelsBox.add(Box.createVerticalGlue());
        
        JComponent optionalCodeBox = new SizeRestrictedPanel(true, false);
        optionalCodeBox.setLayout(
                new BoxLayout(optionalCodeBox, BoxLayout.Y_AXIS));
        optionalCodeBox.add(optCode);
        optionalCodeBox.add(Box.createVerticalStrut(11));
        optionalCodeBox.add(optComments);
        optionalCodeBox.add(Box.createVerticalGlue());
        
        JComponent optionsBox = new SizeRestrictedPanel(false, true);
        optionsBox.setLayout(
                new BoxLayout(optionsBox, BoxLayout.X_AXIS));
        optionsBox.add(accessLevelsBox);
        optionsBox.add(Box.createHorizontalStrut(18));
        optionsBox.add(optionalCodeBox);
        optionsBox.add(Box.createHorizontalGlue());
        
        Box result = Box.createVerticalBox();
        result.add(targetPanel);
        result.add(Box.createVerticalStrut(24));
        result.add(optionsBox);
        //result.add(Box.createVerticalGlue());  //not necessary
        
        /* tune layout of the components within the box: */
        targetPanel.setAlignmentX(0.0f);
        optionsBox.setAlignmentX(0.0f);

        return result;
    }
    
    public Component getComponent() {
        return visualComp;
    }
    
    public boolean isValid() {
        //PENDING:
        return true;
    }
    
    public HelpCtx getHelp() {
        //PENDINGg
        return null;
    }
    
    public void readSettings(Object settings) {
        //PENDING
    }
    
    public void storeSettings(Object settings) {
        TemplateWizard wizard = (TemplateWizard) settings;
        
        wizard.putProperty(SimpleTestCaseWizard.PROP_CLASS_TO_TEST,
                           tfClassToTest.getText());
        
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkSetUp.isSelected()));
        wizard.putProperty(GuiUtils.CHK_PACKAGE,
                           Boolean.valueOf(chkPackagePrivate.isSelected()));
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkTearDown.isSelected()));
        wizard.putProperty(GuiUtils.CHK_METHOD_BODIES,
                           Boolean.valueOf(chkMethodBodies.isSelected()));
        wizard.putProperty(GuiUtils.CHK_JAVADOC,
                           Boolean.valueOf(chkJavadoc.isSelected()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(chkHints.isSelected()));
    }
    
    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(4);
        }
        changeListeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                changeListeners = null;
            }
        }
    }
    
    private void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }
    
    void setProject(Project project) {
        if (project == this.project) {
            return;
        }
        if (project == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        this.project = project;
        tfProjectName.setText(
                ProjectUtils.getInformation(project).getDisplayName());
        //PENDING - not yet finished
    }
    
}
