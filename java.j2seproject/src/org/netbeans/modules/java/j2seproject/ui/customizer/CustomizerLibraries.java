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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.openide.util.HelpCtx;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerLibraries extends JPanel implements HelpCtx.Provider {
    
    public CustomizerLibraries( J2SEProjectProperties uiProperties ) {
        initComponents();

        this.putClientProperty( "HelpID", "J2SE_CustomizerGeneral" ); // NOI18N

        
        // Hide unused edit buttons
        jButtonEditC.setVisible( false );
        jButtonEditCT.setVisible( false );
        jButtonEditR.setVisible( false );
        jButtonEditRT.setVisible( false );
        
        jListCpC.setModel( uiProperties.JAVAC_CLASSPATH_MODEL );
        jListCpC.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        J2SEClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               jListCpC, 
                                               uiProperties.JAVAC_CLASSPATH_MODEL, 
                                               jButtonAddJarC.getModel(), 
                                               jButtonAddLibraryC.getModel(), 
                                               jButtonAddArtifactC.getModel(), 
                                               jButtonRemoveC.getModel(), 
                                               jButtonMoveUpC.getModel(), 
                                               jButtonMoveDownC.getModel() );
        
        jListCpCT.setModel( uiProperties.JAVAC_TEST_CLASSPATH_MODEL);
        jListCpCT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        J2SEClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               jListCpCT, 
                                               uiProperties.JAVAC_CLASSPATH_MODEL, 
                                               jButtonAddJarCT.getModel(), 
                                               jButtonAddLibraryCT.getModel(), 
                                               jButtonAddArtifactCT.getModel(), 
                                               jButtonRemoveCT.getModel(), 
                                               jButtonMoveUpCT.getModel(), 
                                               jButtonMoveDownCT.getModel() );
        
        jListCpR.setModel( uiProperties.RUN_CLASSPATH_MODEL );
        jListCpR.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        J2SEClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               jListCpR, 
                                               uiProperties.JAVAC_CLASSPATH_MODEL, 
                                               jButtonAddJarR.getModel(), 
                                               jButtonAddLibraryR.getModel(), 
                                               jButtonAddArtifactR.getModel(), 
                                               jButtonRemoveR.getModel(), 
                                               jButtonMoveUpR.getModel(), 
                                               jButtonMoveDownR.getModel() );
        
        jListCpRT.setModel( uiProperties.RUN_TEST_CLASSPATH_MODEL );
        jListCpRT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        J2SEClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               jListCpRT, 
                                               uiProperties.JAVAC_CLASSPATH_MODEL, 
                                               jButtonAddJarRT.getModel(), 
                                               jButtonAddLibraryRT.getModel(), 
                                               jButtonAddArtifactRT.getModel(), 
                                               jButtonRemoveRT.getModel(), 
                                               jButtonMoveUpRT.getModel(), 
                                               jButtonMoveDownRT.getModel() );
        
        jCheckBoxBuildSubprojects.setModel( uiProperties.NO_DEPENDENCIES_MODEL );                        
        jComboBoxTarget.setModel(uiProperties.PLATFORM_MODEL);               
    }
        
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerLibraries.class );
    }        
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelCompile = new javax.swing.JPanel();
        librariesJLabel1 = new javax.swing.JLabel();
        librariesJScrollPane = new javax.swing.JScrollPane();
        jListCpC = new javax.swing.JList();
        jButtonAddArtifactC = new javax.swing.JButton();
        jButtonAddLibraryC = new javax.swing.JButton();
        jButtonAddJarC = new javax.swing.JButton();
        jButtonEditC = new javax.swing.JButton();
        jButtonRemoveC = new javax.swing.JButton();
        jButtonMoveUpC = new javax.swing.JButton();
        jButtonMoveDownC = new javax.swing.JButton();
        jPanelCompileTests = new javax.swing.JPanel();
        librariesJLabel2 = new javax.swing.JLabel();
        librariesJScrollPane1 = new javax.swing.JScrollPane();
        jListCpCT = new javax.swing.JList();
        jButtonAddArtifactCT = new javax.swing.JButton();
        jButtonAddLibraryCT = new javax.swing.JButton();
        jButtonAddJarCT = new javax.swing.JButton();
        jButtonEditCT = new javax.swing.JButton();
        jButtonRemoveCT = new javax.swing.JButton();
        jButtonMoveUpCT = new javax.swing.JButton();
        jButtonMoveDownCT = new javax.swing.JButton();
        jPanelRun = new javax.swing.JPanel();
        librariesJLabel3 = new javax.swing.JLabel();
        librariesJScrollPane2 = new javax.swing.JScrollPane();
        jListCpR = new javax.swing.JList();
        jButtonAddArtifactR = new javax.swing.JButton();
        jButtonAddLibraryR = new javax.swing.JButton();
        jButtonAddJarR = new javax.swing.JButton();
        jButtonEditR = new javax.swing.JButton();
        jButtonRemoveR = new javax.swing.JButton();
        jButtonMoveUpR = new javax.swing.JButton();
        jButtonMoveDownR = new javax.swing.JButton();
        jPanelRunTests = new javax.swing.JPanel();
        librariesJLabel4 = new javax.swing.JLabel();
        librariesJScrollPane3 = new javax.swing.JScrollPane();
        jListCpRT = new javax.swing.JList();
        jButtonAddArtifactRT = new javax.swing.JButton();
        jButtonAddLibraryRT = new javax.swing.JButton();
        jButtonAddJarRT = new javax.swing.JButton();
        jButtonEditRT = new javax.swing.JButton();
        jButtonRemoveRT = new javax.swing.JButton();
        jButtonMoveUpRT = new javax.swing.JButton();
        jButtonMoveDownRT = new javax.swing.JButton();
        jCheckBoxBuildSubprojects = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLabelTarget.setLabelFor(jComboBoxTarget);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTarget, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_Platform_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabelTarget, gridBagConstraints);
        jLabelTarget.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerGeneral_jLabelTarget"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jComboBoxTarget, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_Platform_JButton"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewPlatform(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerGeneral_jButton1"));

        jPanelCompile.setLayout(new java.awt.GridBagLayout());

        jPanelCompile.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompile.add(librariesJLabel1, gridBagConstraints);

        librariesJScrollPane.setViewportView(jListCpC);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompile.add(librariesJScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonAddArtifactC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonAddLibraryC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonAddJarC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonEditC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonRemoveC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonMoveUpC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonMoveDownC, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesTab"), jPanelCompile);

        jPanelCompileTests.setLayout(new java.awt.GridBagLayout());

        jPanelCompileTests.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel2, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileTests.add(librariesJLabel2, gridBagConstraints);

        librariesJScrollPane1.setViewportView(jListCpCT);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileTests.add(librariesJScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonAddArtifactCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonAddLibraryCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonAddJarCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonEditCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonRemoveCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonMoveUpCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonMoveDownCT, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_TestLibrariesTab"), jPanelCompileTests);

        jPanelRun.setLayout(new java.awt.GridBagLayout());

        jPanelRun.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel3, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRun.add(librariesJLabel3, gridBagConstraints);

        librariesJScrollPane2.setViewportView(jListCpR);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRun.add(librariesJScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonAddArtifactR, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonAddLibraryR, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonAddJarR, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonEditR, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonRemoveR, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonMoveUpR, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonMoveDownR, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Run_Tab"), jPanelRun);

        jPanelRunTests.setLayout(new java.awt.GridBagLayout());

        jPanelRunTests.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel4, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunTests.add(librariesJLabel4, gridBagConstraints);

        librariesJScrollPane3.setViewportView(jListCpRT);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRunTests.add(librariesJScrollPane3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonAddArtifactRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonAddLibraryRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonAddJarRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonEditRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonRemoveRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonMoveUpRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonMoveDownRT, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_RunTests_Tab"), jPanelRunTests);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jTabbedPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxBuildSubprojects, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Build_Subprojects"));
        jCheckBoxBuildSubprojects.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jCheckBoxBuildSubprojects, gridBagConstraints);

    }//GEN-END:initComponents

    private void createNewPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewPlatform
        PlatformsCustomizer.showCustomizer(null);        
    }//GEN-LAST:event_createNewPlatform
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddArtifactC;
    private javax.swing.JButton jButtonAddArtifactCT;
    private javax.swing.JButton jButtonAddArtifactR;
    private javax.swing.JButton jButtonAddArtifactRT;
    private javax.swing.JButton jButtonAddJarC;
    private javax.swing.JButton jButtonAddJarCT;
    private javax.swing.JButton jButtonAddJarR;
    private javax.swing.JButton jButtonAddJarRT;
    private javax.swing.JButton jButtonAddLibraryC;
    private javax.swing.JButton jButtonAddLibraryCT;
    private javax.swing.JButton jButtonAddLibraryR;
    private javax.swing.JButton jButtonAddLibraryRT;
    private javax.swing.JButton jButtonEditC;
    private javax.swing.JButton jButtonEditCT;
    private javax.swing.JButton jButtonEditR;
    private javax.swing.JButton jButtonEditRT;
    private javax.swing.JButton jButtonMoveDownC;
    private javax.swing.JButton jButtonMoveDownCT;
    private javax.swing.JButton jButtonMoveDownR;
    private javax.swing.JButton jButtonMoveDownRT;
    private javax.swing.JButton jButtonMoveUpC;
    private javax.swing.JButton jButtonMoveUpCT;
    private javax.swing.JButton jButtonMoveUpR;
    private javax.swing.JButton jButtonMoveUpRT;
    private javax.swing.JButton jButtonRemoveC;
    private javax.swing.JButton jButtonRemoveCT;
    private javax.swing.JButton jButtonRemoveR;
    private javax.swing.JButton jButtonRemoveRT;
    private javax.swing.JCheckBox jCheckBoxBuildSubprojects;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JList jListCpC;
    private javax.swing.JList jListCpCT;
    private javax.swing.JList jListCpR;
    private javax.swing.JList jListCpRT;
    private javax.swing.JPanel jPanelCompile;
    private javax.swing.JPanel jPanelCompileTests;
    private javax.swing.JPanel jPanelRun;
    private javax.swing.JPanel jPanelRunTests;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel librariesJLabel1;
    private javax.swing.JLabel librariesJLabel2;
    private javax.swing.JLabel librariesJLabel3;
    private javax.swing.JLabel librariesJLabel4;
    private javax.swing.JScrollPane librariesJScrollPane;
    private javax.swing.JScrollPane librariesJScrollPane1;
    private javax.swing.JScrollPane librariesJScrollPane2;
    private javax.swing.JScrollPane librariesJScrollPane3;
    // End of variables declaration//GEN-END:variables
        
        
}
