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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.clientproject.ui.AppClientLogicalViewProvider;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerLibraries extends JPanel implements HelpCtx.Provider, ListDataListener {
    private static final long serialVersionUID = 1L;
    
    public static final String COMPILE = "COMPILE";  //NOI18N
    public static final String RUN = "RUN";          //NOI18N
    public static final String COMPILE_TESTS = "COMPILE_TESTS"; //NOI18N
    public static final String RUN_TESTS = "RUN_TESTS";  //NOI18N        
    
    private final AppClientProjectProperties uiProperties;    
    
    public CustomizerLibraries( AppClientProjectProperties uiProperties, CustomizerProviderImpl.SubCategoryProvider subcat) {
        this.uiProperties = uiProperties;        
        initComponents();        
        
        this.putClientProperty( "HelpID", "J2SE_CustomizerGeneral" ); // NOI18N

        
        // Hide unused edit buttons
        jButtonEditC.setVisible( false );
        jButtonEditCT.setVisible( false );
        jButtonEditR.setVisible( false );
        jButtonEditRT.setVisible( false );
        
        jTableCpC.setModel( uiProperties.JAVAC_CLASSPATH_MODEL );
        initTableVisualProperties(jTableCpC);
        uiProperties.CLASS_PATH_TABLE_ITEM_RENDERER.setBooleanRenderer(jTableCpC.getDefaultRenderer(Boolean.class));
        jTableCpC.setDefaultRenderer( ClassPathSupport.Item.class, uiProperties.CLASS_PATH_TABLE_ITEM_RENDERER );
        jTableCpC.setDefaultRenderer( Boolean.class, uiProperties.CLASS_PATH_TABLE_ITEM_RENDERER );
        
        AppClientClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               AppClientClassPathUi.EditMediator.createListComponent(jTableCpC, uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel()),
                                               jButtonAddJarC.getModel(), 
                                               jButtonAddLibraryC.getModel(), 
                                               jButtonAddArtifactC.getModel(), 
                                               jButtonRemoveC.getModel(), 
                                               jButtonMoveUpC.getModel(), 
                                               jButtonMoveDownC.getModel(),
                                               true);
        
        jListCpCT.setModel( uiProperties.JAVAC_TEST_CLASSPATH_MODEL);
        jListCpCT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        AppClientClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               AppClientClassPathUi.EditMediator.createListComponent(jListCpCT), 
                                               jButtonAddJarCT.getModel(), 
                                               jButtonAddLibraryCT.getModel(), 
                                               jButtonAddArtifactCT.getModel(), 
                                               jButtonRemoveCT.getModel(), 
                                               jButtonMoveUpCT.getModel(), 
                                               jButtonMoveDownCT.getModel(),
                                               false);
        
        jListCpR.setModel( uiProperties.RUN_CLASSPATH_MODEL );
        jListCpR.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        AppClientClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               AppClientClassPathUi.EditMediator.createListComponent(jListCpR), 
                                               jButtonAddJarR.getModel(), 
                                               jButtonAddLibraryR.getModel(), 
                                               jButtonAddArtifactR.getModel(), 
                                               jButtonRemoveR.getModel(), 
                                               jButtonMoveUpR.getModel(), 
                                               jButtonMoveDownR.getModel(),
                                               false);
        
        jListCpRT.setModel( uiProperties.RUN_TEST_CLASSPATH_MODEL );
        jListCpRT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        AppClientClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               AppClientClassPathUi.EditMediator.createListComponent(jListCpRT), 
                                               jButtonAddJarRT.getModel(), 
                                               jButtonAddLibraryRT.getModel(), 
                                               jButtonAddArtifactRT.getModel(), 
                                               jButtonRemoveRT.getModel(), 
                                               jButtonMoveUpRT.getModel(), 
                                               jButtonMoveDownRT.getModel(),
                                               false);
                
        uiProperties.NO_DEPENDENCIES_MODEL.setMnemonic( jCheckBoxBuildSubprojects.getMnemonic() );
        jCheckBoxBuildSubprojects.setModel( uiProperties.NO_DEPENDENCIES_MODEL );                        
        jComboBoxTarget.setModel(uiProperties.PLATFORM_MODEL);               
        jComboBoxTarget.setRenderer(uiProperties.PLATFORM_LIST_RENDERER);
        jComboBoxTarget.putClientProperty ("JComboBox.isTableCellEditor", Boolean.TRUE);    //NOI18N
        jComboBoxTarget.addItemListener(new java.awt.event.ItemListener(){ 
            public void itemStateChanged(java.awt.event.ItemEvent e){ 
                javax.swing.JComboBox combo = (javax.swing.JComboBox)e.getSource(); 
                combo.setPopupVisible(false); 
            } 
        });
        testBroken();
        if (AppClientCompositePanelProvider.LIBRARIES.equals(subcat.getCategory())) {
            showSubCategory(subcat.getSubcategory());
        }
        
        uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel().addListDataListener( this );
        uiProperties.JAVAC_TEST_CLASSPATH_MODEL.addListDataListener( this );
        uiProperties.RUN_CLASSPATH_MODEL.addListDataListener( this );
        uiProperties.RUN_TEST_CLASSPATH_MODEL.addListDataListener( this );
    }
        
    private void testBroken() {
        
        DefaultListModel[] models = new DefaultListModel[] {
            uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel(),
            uiProperties.JAVAC_TEST_CLASSPATH_MODEL,
            uiProperties.RUN_CLASSPATH_MODEL,
            uiProperties.RUN_TEST_CLASSPATH_MODEL,
        };
        
        boolean broken = false;
        
        for( int i = 0; i < models.length; i++ ) {
            for( Iterator it = ClassPathUiSupport.getIterator( models[i] ); it.hasNext(); ) {
                if ( ((ClassPathSupport.Item)it.next()).isBroken() ) {
                    broken = true;
                    break;
                }
            }
            if ( broken ) {
                break;
            }
        }
        
        if ( broken ) {
            jLabelErrorMessage.setText( NbBundle.getMessage( CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_Error" ) ); // NOI18N            
        }
        else {
            jLabelErrorMessage.setText( " " ); // NOI18N
        }
        AppClientLogicalViewProvider viewProvider = uiProperties.getProject().getLookup().lookup(AppClientLogicalViewProvider.class);
        //Update the state of project's node if needed
        viewProvider.testBroken();
    }
    
    // Implementation of HelpCtx.Provider --------------------------------------
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerLibraries.class );
    }        

    
    // Implementation of ListDataListener --------------------------------------
    
    
    public void intervalRemoved( ListDataEvent e ) {
        testBroken(); 
    }

    public void intervalAdded( ListDataEvent e ) {
        // NOP
    }

    public void contentsChanged( ListDataEvent e ) {
        // NOP
    }
    
    
    private void showSubCategory (String name) {
        if (name.equals(COMPILE)) {
            jTabbedPane1.setSelectedIndex (0);
        }        
        else if (name.equals(COMPILE_TESTS)) {
            jTabbedPane1.setSelectedIndex (2);
        }
        else if (name.equals(RUN)) {
            jTabbedPane1.setSelectedIndex (1);
        }
        else if (name.equals(RUN_TESTS)) {
            jTabbedPane1.setSelectedIndex (3);
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelCompile = new javax.swing.JPanel();
        librariesJLabel1 = new javax.swing.JLabel();
        librariesJScrollPane = new javax.swing.JScrollPane();
        jTableCpC = new javax.swing.JTable();
        jButtonAddArtifactC = new javax.swing.JButton();
        jButtonAddLibraryC = new javax.swing.JButton();
        jButtonAddJarC = new javax.swing.JButton();
        jButtonEditC = new javax.swing.JButton();
        jButtonRemoveC = new javax.swing.JButton();
        jButtonMoveUpC = new javax.swing.JButton();
        jButtonMoveDownC = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
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
        jLabelErrorMessage = new javax.swing.JLabel();

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

        jPanelCompile.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesC_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompile.add(librariesJLabel1, gridBagConstraints);

        jTableCpC.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        librariesJScrollPane.setViewportView(jTableCpC);

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
        jButtonAddArtifactC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonAddLibraryC, gridBagConstraints);
        jButtonAddLibraryC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonAddJarC, gridBagConstraints);
        jButtonAddJarC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar"));

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
        jButtonRemoveC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonMoveUpC, gridBagConstraints);
        jButtonMoveUpC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonMoveDownC, gridBagConstraints);
        jButtonMoveDownC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "MSG_CustomizerLibraries_CompileCpMessage"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanelCompile.add(jLabel1, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesTab"), jPanelCompile);

        jPanelRun.setLayout(new java.awt.GridBagLayout());

        jPanelRun.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        librariesJLabel3.setLabelFor(jListCpR);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel3, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesR_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRun.add(librariesJLabel3, gridBagConstraints);

        librariesJScrollPane2.setViewportView(jListCpR);
        jListCpR.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AN_CustomizerLibraries_jListClasspathR"));
        jListCpR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jLabelClasspathR"));

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
        jButtonAddArtifactR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonAddLibraryR, gridBagConstraints);
        jButtonAddLibraryR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonAddJarR, gridBagConstraints);
        jButtonAddJarR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar"));

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
        jButtonRemoveR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonMoveUpR, gridBagConstraints);
        jButtonMoveUpR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonMoveDownR, gridBagConstraints);
        jButtonMoveDownR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown"));

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Run_Tab"), jPanelRun);

        jPanelCompileTests.setLayout(new java.awt.GridBagLayout());

        jPanelCompileTests.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        librariesJLabel2.setLabelFor(jListCpCT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel2, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesCT_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileTests.add(librariesJLabel2, gridBagConstraints);

        librariesJScrollPane1.setViewportView(jListCpCT);
        jListCpCT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AN_CustomizerLibraries_jListClasspathCT"));
        jListCpCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jLabelClasspathCT"));

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
        jButtonAddArtifactCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonAddLibraryCT, gridBagConstraints);
        jButtonAddLibraryCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonAddJarCT, gridBagConstraints);
        jButtonAddJarCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar"));

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
        jButtonRemoveCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonMoveUpCT, gridBagConstraints);
        jButtonMoveUpCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonMoveDownCT, gridBagConstraints);
        jButtonMoveDownCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown"));

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_TestLibrariesTab"), jPanelCompileTests);

        jPanelRunTests.setLayout(new java.awt.GridBagLayout());

        jPanelRunTests.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        librariesJLabel4.setLabelFor(jListCpRT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel4, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesRT_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunTests.add(librariesJLabel4, gridBagConstraints);

        librariesJScrollPane3.setViewportView(jListCpRT);
        jListCpRT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AN_CustomizerLibraries_jListClasspathRT"));
        jListCpRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jLabelClasspathRT"));

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
        jButtonAddArtifactRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonAddLibraryRT, gridBagConstraints);
        jButtonAddLibraryRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonAddJarRT, gridBagConstraints);
        jButtonAddJarRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar"));

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
        jButtonRemoveRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonMoveUpRT, gridBagConstraints);
        jButtonMoveUpRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonMoveDownRT, gridBagConstraints);
        jButtonMoveDownRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown"));

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_RunTests_Tab"), jPanelRunTests);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jTabbedPane1, gridBagConstraints);
        jTabbedPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSN_CustomizerLibraries_JTabbedPane"));
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_JTabbedPane"));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxBuildSubprojects, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Build_Subprojects"));
        jCheckBoxBuildSubprojects.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jCheckBoxBuildSubprojects, gridBagConstraints);
        jCheckBoxBuildSubprojects.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AD_CheckBoxBuildSubprojects"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabelErrorMessage, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelErrorMessage, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void createNewPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewPlatform
        Object selectedItem = this.jComboBoxTarget.getSelectedItem();
        JavaPlatform jp = (selectedItem == null ? null : PlatformUiSupport.getPlatform(selectedItem));
        PlatformsCustomizer.showCustomizer(jp);        
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelErrorMessage;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JList jListCpCT;
    private javax.swing.JList jListCpR;
    private javax.swing.JList jListCpRT;
    private javax.swing.JPanel jPanelCompile;
    private javax.swing.JPanel jPanelCompileTests;
    private javax.swing.JPanel jPanelRun;
    private javax.swing.JPanel jPanelRunTests;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableCpC;
    private javax.swing.JLabel librariesJLabel1;
    private javax.swing.JLabel librariesJLabel2;
    private javax.swing.JLabel librariesJLabel3;
    private javax.swing.JLabel librariesJLabel4;
    private javax.swing.JScrollPane librariesJScrollPane;
    private javax.swing.JScrollPane librariesJScrollPane1;
    private javax.swing.JScrollPane librariesJScrollPane2;
    private javax.swing.JScrollPane librariesJScrollPane3;
    // End of variables declaration//GEN-END:variables
        
    private void initTableVisualProperties(JTable table) {
        //table.setGridColor(jTableCpC.getBackground());
        table.setRowHeight(jTableCpC.getRowHeight() + 4);        
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);        
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));        
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        
        TableColumn column = table.getColumnModel().getColumn(1);
        JTableHeader header = table.getTableHeader();
        column.setMaxWidth(24 + SwingUtilities.computeStringWidth(header.getFontMetrics(header.getFont()), String.valueOf(column.getHeaderValue())));
        header.setReorderingAllowed(false);
    }
}
