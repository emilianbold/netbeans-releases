/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author  vstejskal
 */
public class JUnitCfgOfCreate extends JPanel {
    
    /**
     * whether the dialog should support the case that tests for whole folders
     * are about to be created
     */
    private final boolean forFolders;

    private class Pair {
        public String  name;
        public Object  item;
        public Pair(String name, Object item) {
            this.name = name;
            this.item = item;
        }
        public String toString() {
            return name.toString();
        }
    }
    
    /**
     * Creates a JUnit configuration panel.
     *
     * @param  forFolders  whether the options should support the case that
     *                     tests for whole folders are about to be created
     */
    private JUnitCfgOfCreate(final boolean forFolders) {
        this.forFolders = forFolders;
        
        initBundle();
        try {
            initComponents();
            addAccessibleDescriptions();
            setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));
        } finally {
            unlinkBundle();
        }
    }
    
    private void addAccessibleDescriptions() {
        
        // window
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.AD"));
        
        // check boxes
        this.chkPublic.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPublic.toolTip"));
        this.chkPublic.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPublic.AD"));        
        
        this.chkProtected.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkProtected.toolTip"));
        this.chkProtected.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkProtected.AD"));        
        
        this.chkPackage.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPackage.toolTip"));
        this.chkPackage.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPackage.AD"));
        
        this.chkComments.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkComments.toolTip"));        
        this.chkComments.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkComments.AD"));
        
        this.chkContent.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkContent.toolTip"));
        this.chkContent.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkContent.AD"));
        
        this.chkJavaDoc.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.toolTip"));
        this.chkJavaDoc.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkJavaDoc.AD"));
        
        if (forFolders) {
            this.chkExceptions.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkExceptions.toolTip"));
            this.chkExceptions.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkExceptions.AD"));
        
            this.chkAbstractImpl.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.toolTip"));
            this.chkAbstractImpl.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkAbstractImpl.AD"));
        
            this.chkPackagePrivateClasses.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkPackagePrivateClasses.toolTip"));
            this.chkPackagePrivateClasses.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkPackagePrivateClasses.AD"));
        
            this.chkGenerateSuites.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkGenerateSuites.toolTip"));
            this.chkGenerateSuites.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkGenerateSuites.AD"));
        }
        
        this.chkEnabled.setToolTipText(bundle.getString("JUnitCfgOfCreate.chkEnabled.toolTip"));
        this.chkEnabled.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.chkEnabled.AD"));
        
        // labels
        
        if (forFolders) {
            this.cboSuiteClass.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.cboSuiteClass.AD"));
            this.cboSuiteClass.getAccessibleContext().setAccessibleName(bundle.getString("JUnitCfgOfCreate.cboSuiteClass.AN"));
        }
        
        this.cboTestClass.getAccessibleContext().setAccessibleDescription(bundle.getString("JUnitCfgOfCreate.cboTestClass.AD"));
        this.cboTestClass.getAccessibleContext().setAccessibleName(bundle.getString("JUnitCfgOfCreate.cboTestClass.AN"));
        
    }
    
    private void fillTemplates() {
        Pair        item;
        FileObject  foJUnitTmpl;
        FileObject  foTemplates[];
        
        foJUnitTmpl = Repository.getDefault().getDefaultFileSystem().findResource("Templates/JUnit");
        if (null == foJUnitTmpl) return;
        
        foTemplates = foJUnitTmpl.getChildren();
        for(int i = 0; i < foTemplates.length; i++) {
            if (!foTemplates[i].getExt().equals("java"))
                continue;
            
            item = new Pair(foTemplates[i].getName(), foTemplates[i]);
            if (forFolders) {
                // add template to Suite templates list
                cboSuiteClass.addItem(item);
                if (foTemplates[i].getPath().equals(JUnitSettings.getDefault().getSuiteTemplate()))
                    cboSuiteClass.setSelectedItem(item);
            }
    
            // add template to Class templates list
            cboTestClass.addItem(item);
            if (foTemplates[i].getPath().equals(JUnitSettings.getDefault().getClassTemplate()))
                cboTestClass.setSelectedItem(item);
        }
    }
    
    /**
     * Displays a configuration dialog and updates JUnit options according
     * to the user's settings.
     *
     * @param  forFolders  whether tests for folders are about to be created
     */
    public static boolean configure(final boolean forFolders) {
        // check if the dialog can be displayed
        if (!JUnitSettings.getDefault().isCfgCreateEnabled())
            return true;
        
        // create panel
        JUnitCfgOfCreate cfg = new JUnitCfgOfCreate(forFolders);
        
        // setup the panel
        cfg.fillTemplates();
        cfg.chkPublic.setSelected(JUnitSettings.getDefault().isMembersPublic());
        cfg.chkProtected.setSelected(JUnitSettings.getDefault().isMembersProtected());
        cfg.chkPackage.setSelected(JUnitSettings.getDefault().isMembersPackage());
        cfg.chkComments.setSelected(JUnitSettings.getDefault().isBodyComments());
        cfg.chkContent.setSelected(JUnitSettings.getDefault().isBodyContent());
        cfg.chkJavaDoc.setSelected(JUnitSettings.getDefault().isJavaDoc());        
        if (forFolders) {
            cfg.chkGenerateSuites.setSelected(JUnitSettings.getDefault().isGenerateSuiteClasses());        
            cfg.chkPackagePrivateClasses.setSelected(JUnitSettings.getDefault().isIncludePackagePrivateClasses());
            cfg.chkAbstractImpl.setSelected(JUnitSettings.getDefault().isGenerateAbstractImpl());
            cfg.chkExceptions.setSelected(JUnitSettings.getDefault().isGenerateExceptionClasses());
        }
        cfg.chkEnabled.setSelected(JUnitSettings.getDefault().isCfgCreateEnabled());
        cfg.chkSetUp.setSelected(JUnitSettings.getDefault().isGenerateSetUp());
        cfg.chkTearDown.setSelected(JUnitSettings.getDefault().isGenerateTearDown());
        
        // display dialog
        DialogDescriptor descriptor = new DialogDescriptor (
            cfg,
            NbBundle.getMessage(JUnitCfgOfCreate.class,
                                "JUnitCfgOfCreate.Title")               //NOI18N
        );
        descriptor.setHelpCtx(new HelpCtx(JUnitCfgOfCreate.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.show();
        dialog.dispose();
        
        // save panel settings
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            FileObject  foTemplate;
            
            if (forFolders) {
                // store Suite class template
                foTemplate = (FileObject)((Pair)cfg.cboSuiteClass.getSelectedItem()).item;
                JUnitSettings.getDefault().setSuiteTemplate(foTemplate.getPath());
            }
            
            // store Test class template
            foTemplate = (FileObject)((Pair)cfg.cboTestClass.getSelectedItem()).item;
            JUnitSettings.getDefault().setClassTemplate(foTemplate.getPath());
            
            // store code generation options
            JUnitSettings.getDefault().setMembersPublic(cfg.chkPublic.isSelected());
            JUnitSettings.getDefault().setMembersProtected(cfg.chkProtected.isSelected());
            JUnitSettings.getDefault().setMembersPackage(cfg.chkPackage.isSelected());
            JUnitSettings.getDefault().setBodyComments(cfg.chkComments.isSelected());
            JUnitSettings.getDefault().setBodyContent(cfg.chkContent.isSelected());
            JUnitSettings.getDefault().setJavaDoc(cfg.chkJavaDoc.isSelected());
            JUnitSettings.getDefault().setCfgCreateEnabled(cfg.chkEnabled.isSelected());
            if (forFolders) {
                JUnitSettings.getDefault().setGenerateSuiteClasses(cfg.chkGenerateSuites.isSelected());
                JUnitSettings.getDefault().setIncludePackagePrivateClasses(cfg.chkPackagePrivateClasses.isSelected());
                JUnitSettings.getDefault().setGenerateAbstractImpl(cfg.chkAbstractImpl.isSelected());
                JUnitSettings.getDefault().setGenerateExceptionClasses(cfg.chkExceptions.isSelected());
            }
            JUnitSettings.getDefault().setGenerateSetUp(cfg.chkSetUp.isSelected());
            JUnitSettings.getDefault().setGenerateTearDown(cfg.chkTearDown.isSelected());
            
            return true;
        }
        
        return false;
    }
    
    /** resource bundle used during initialization of this panel */
    public ResourceBundle bundle;
    
    /**
     * Loads a resource bundle so that it can be used during intialization
     * of this panel.
     *
     * @see  #unlinkBundle
     */
    private void initBundle() {
        bundle = NbBundle.getBundle(JUnitCfgOfCreate.class);
    }
    
    /**
     * Nulls the resource bundle so that it is not held in memory when it is
     * not used.
     *
     * @see  #initBundle
     */
    private void unlinkBundle() {
        bundle = null;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        createTemplatesPanel();
        createOptionsPanel();
        composeWholePanel();
    }
    
    /**
     * Creates a panel for choosing templates.
     * The created panel is stored in variable {@link #jpTemplates jpTemplates}.
     */
    private void createTemplatesPanel() {
        
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagLayout gbl = new GridBagLayout();
        jpTemplates = new SizeRestrictedPanel(gbl, false, true);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        
        if (forFolders) {
            /* create label and combo-box for the test suite template chooser */
            JLabel lblSuiteClass = new JLabel();
            Mnemonics.setLocalizedText(
               lblSuiteClass,
               bundle.getString("JUnitCfgOfCreate.lblSuiteClass.text"));//NOI18N
            cboSuiteClass = new JComboBox();
            lblSuiteClass.setLabelFor(cboSuiteClass);
            
            /* set layout: */
            gbc.gridx = 0;
            gbc.weightx = 0.0d;
            gbc.insets = new Insets(0, 0, 5, 12);
            jpTemplates.add(lblSuiteClass, gbc);
        
            gbc.gridx = 1;
            gbc.weightx = 1.0d;
            gbc.insets = new Insets(0, 0, 5, 0);
            jpTemplates.add(cboSuiteClass, gbc);
            
            gbc.gridy++;
        }
        {
            /* create label and combo-box for the test case template chooser */
            JLabel lblTestClass = new JLabel();
            Mnemonics.setLocalizedText(
                lblTestClass,
                bundle.getString("JUnitCfgOfCreate.lblTestClass.text"));//NOI18N
            cboTestClass = new JComboBox();
            lblTestClass.setLabelFor(cboTestClass);
        
            /* set layout: */
            gbc.gridx = 0;
            gbc.weightx = 0.0d;
            gbc.insets = new Insets(0, 0, 0, 12);
            jpTemplates.add(lblTestClass, gbc);
        
            gbc.gridx = 1;
            gbc.weightx = 1.0d;
            gbc.insets = new Insets(0, 0, 0, 0);
            jpTemplates.add(cboTestClass, gbc);
        }
    }
    
    /**
     * Creates a panel containing controls for settings code generation options.
     * The created panel is stored in variable {@link #jpCodeGen jpCodeGen}.
     */
    private void createOptionsPanel() {
        
        /* create the components: */
        chkPublic = new JCheckBox();
        chkProtected = new JCheckBox();
        chkPackage = new JCheckBox();
        if (forFolders) {
            chkPackagePrivateClasses = new JCheckBox();
            chkAbstractImpl = new JCheckBox();
            chkExceptions = new JCheckBox();
            chkGenerateSuites = new JCheckBox();
        }
        chkSetUp = new JCheckBox();
        chkTearDown = new JCheckBox();
        chkContent = new JCheckBox();
        chkJavaDoc = new JCheckBox();
        chkComments = new JCheckBox();
        
        /* set texts: */
        Mnemonics.setLocalizedText(
                chkPublic,
                bundle.getString("JUnitCfgOfCreate.chkPublic.text"));   //NOI18N
        Mnemonics.setLocalizedText(
                chkProtected,
                bundle.getString("JUnitCfgOfCreate.chkProtected.text"));//NOI18N
        Mnemonics.setLocalizedText(
                chkPackage,
                bundle.getString("JUnitCfgOfCreate.chkPackage.text"));  //NOI18N
        if (forFolders) {
            Mnemonics.setLocalizedText(
                chkPackagePrivateClasses,
                bundle.getString(
                    "JUnitCfgOfCreate.chkPackagePrivateClasses.text")); //NOI18N
            Mnemonics.setLocalizedText(
                chkAbstractImpl,
                bundle.getString(
                    "JUnitCfgOfCreate.chkAbstractImpl.text"));          //NOI18N
            Mnemonics.setLocalizedText(
                chkExceptions,
                bundle.getString(
                    "JUnitCfgOfCreate.chkExceptions.text"));            //NOI18N
            Mnemonics.setLocalizedText(
                chkGenerateSuites,
                bundle.getString(
                    "JUnitCfgOfCreate.chkGenerateSuites.text"));        //NOI18N
        }
        Mnemonics.setLocalizedText(
                chkSetUp,
                bundle.getString("JUnitCfgOfCreate.chkSetUp.text"));    //NOI18N
        Mnemonics.setLocalizedText(
                chkTearDown,
                bundle.getString("JUnitCfgOfCreate.chkTearDown.text")); //NOI18N
        Mnemonics.setLocalizedText(
                chkContent,
                bundle.getString("JUnitCfgOfCreate.chkContent.text"));  //NOI18N
        Mnemonics.setLocalizedText(
                chkJavaDoc,
                bundle.getString("JUnitCfgOfCreate.chkJavaDoc.text"));  //NOI18N
        Mnemonics.setLocalizedText(
                chkComments,
                bundle.getString("JUnitCfgOfCreate.chkComments.text")); //NOI18N
        
        /* create groups of checkboxes: */
        JComponent methodAccessLevels = createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupAccessLevels"), //NOI18N
                new JCheckBox[] {chkPublic, chkProtected, chkPackage});
        JComponent classTypes = null;
        JComponent optionalClasses = null;
        if (forFolders) {
            classTypes = createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupClassTypes"),   //NOI18N
                new JCheckBox[] {chkPackagePrivateClasses,
                                 chkAbstractImpl, chkExceptions});
            optionalClasses = createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupOptClasses"),   //NOI18N
                new JCheckBox[] {chkGenerateSuites});
        }
        JComponent optionalCode = createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupOptCode"),      //NOI18N
                new JCheckBox[] {chkSetUp, chkTearDown, chkContent});
        JComponent optionalComments = createChkBoxGroup(
                bundle.getString("JUnitCfgOfCreate.groupOptComments"),  //NOI18N
                new JCheckBox[] {chkJavaDoc, chkComments});
        
        /* create the left column of options: */
        Box leftColumn = Box.createVerticalBox();
        leftColumn.add(methodAccessLevels);
        if (forFolders) {
            leftColumn.add(Box.createVerticalStrut(11));
            leftColumn.add(classTypes);
        }
        leftColumn.add(Box.createVerticalGlue());
        
        /* set alignments for BoxLayout: */
        methodAccessLevels.setAlignmentX(0.0f);
        if (forFolders) {
            classTypes.setAlignmentX(0.0f);
        }
        
        /* create the right column of options: */
        Box rightColumn = Box.createVerticalBox();
        if (forFolders) {
            rightColumn.add(optionalClasses);
            rightColumn.add(Box.createVerticalStrut(11));
        }
        rightColumn.add(optionalCode);
        rightColumn.add(Box.createVerticalStrut(11));
        rightColumn.add(optionalComments);
        rightColumn.add(Box.createVerticalGlue());
        
        /* set alignments for BoxLayout: */
        if (forFolders) {
            optionalClasses.setAlignmentX(0.0f);
        }
        optionalCode.setAlignmentX(0.0f);
        optionalComments.setAlignmentX(0.0f);
        
        /* compose the final panel: */
        jpCodeGen = new SizeRestrictedPanel(false, true);
        jpCodeGen.setLayout(new BoxLayout(jpCodeGen, BoxLayout.X_AXIS));
        jpCodeGen.add(leftColumn);
        jpCodeGen.add(Box.createHorizontalStrut(12));
        jpCodeGen.add(rightColumn);
    }
    
    /**
     * Creates a labelled group of checkboxes.
     *
     * @param  title  title for the group of checkboxes
     * @param  elements  checkboxes - members of the group
     * @return  visual component representing the group
     */
    private static JComponent createChkBoxGroup(String title,
                                                JCheckBox[] elements) {
        
        /* create a component representing the group without title: */
        JComponent content;
        if (elements.length == 1) {
            content = elements[0];
        } else {
            content = new JPanel(new GridLayout(0, 1, 0, 5));
            for (int i = 0; i < elements.length; i++) {
                content.add(elements[i]);
            }
        }
        
        /* add the title and insets to the group: */
        JPanel result = new JPanel(new BorderLayout());
        result.add(new JLabel(title), BorderLayout.NORTH);
        content.setBorder(BorderFactory.createEmptyBorder(6, 12, 0, 0));
        result.add(content, BorderLayout.CENTER);
        
        /*
         * restrict the size so that the component is not resized when put
         * into a container layed out by BoxLayout:
         */
        result.setMaximumSize(result.getPreferredSize());
        
        return result;
    }
    
    /**
     * Adds a border and a title around a given component.
     *
     * @param  component  component the border and title should be added to
     * @param  insets  insets between the component and the titled border
     * @param  title  text of the title
     */
    private static void addTitledBorder(JComponent component,
                                        Insets insets,
                                        String title) {
        Border insideBorder = BorderFactory.createEmptyBorder(
                insets.top, insets.left, insets.bottom, insets.right);
        Border outsideBorder = new TitledBorder(
                BorderFactory.createEtchedBorder(), title);
        component.setBorder(new CompoundBorder(outsideBorder, insideBorder));
    }
    
    /**
     * Composes the whole panel from the templates panel, code generation
     * options panel and the &quot;Show this configuration dialog&quot;
     * checkbox.
     */
    private void composeWholePanel() {
        
        /* create the "Show this configuration dialog again" checkbox: */
        chkEnabled = new JCheckBox();
        Mnemonics.setLocalizedText(
                chkEnabled,
                bundle.getString("JUnitCfgOfCreate.chkEnabled.text"));  //NOI18N
        
        /* decorate the option panels: */
        addTitledBorder(jpTemplates,
                  new Insets(12, 12, 11, 11),
                  bundle.getString("JUnitCfgOfCreate.jpTemplates.title"));//NOI18N
        addTitledBorder(jpCodeGen,
                  new Insets(12, 12, 11, 12),
                  bundle.getString("JUnitCfgOfCreate.jpCodeGen.title"));//NOI18N
        
        /* compose the main panel: */
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(jpTemplates);
        add(Box.createVerticalStrut(11));
        add(jpCodeGen);
        add(Box.createVerticalStrut(11));
        add(chkEnabled);
        
        /* tune the layout: */
        jpTemplates.setAlignmentX(0.0f);
        jpCodeGen.setAlignmentX(0.0f);
        //chkEnabled.setAlignmentX(0.0f);   //not necessary - its the default
    }

    private JComboBox cboSuiteClass;
    private JComboBox cboTestClass;
    private JCheckBox chkAbstractImpl;
    private JCheckBox chkComments;
    private JCheckBox chkContent;
    private JCheckBox chkEnabled;
    private JCheckBox chkExceptions;
    private JCheckBox chkGenerateSuites;
    private JCheckBox chkJavaDoc;
    private JCheckBox chkPackage;
    private JCheckBox chkPackagePrivateClasses;
    private JCheckBox chkProtected;
    private JCheckBox chkPublic;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JComponent jpCodeGen;
    private JComponent jpTemplates;

}
