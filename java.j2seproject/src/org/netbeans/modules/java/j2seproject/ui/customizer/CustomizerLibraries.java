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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerLibraries extends JPanel implements HelpCtx.Provider, ListDataListener {
    
    public static final String COMPILE_MODULE = "COMPILE_MODULE";  //NOI18N
    public static final String COMPILE = "COMPILE";  //NOI18N
    public static final String PROCESSOR_MODULE = "PROCESSOR_MODULE";  //NOI18N
    public static final String PROCESSOR = "PROCESSOR";  //NOI18N
    public static final String RUN_MODULE = "RUN_MODULE";          //NOI18N
    public static final String RUN = "RUN";          //NOI18N
    public static final String COMPILE_TESTS_MODULE = "COMPILE_TESTS_MODULE"; //NOI18N
    public static final String COMPILE_TESTS = "COMPILE_TESTS"; //NOI18N
    public static final String RUN_TESTS_MODULE = "RUN_TESTS_MODULE";  //NOI18N        
    public static final String RUN_TESTS = "RUN_TESTS";  //NOI18N        
    
    private final J2SEProjectProperties uiProperties;
    private boolean isSharable;
    private final ProjectCustomizer.Category category;
    
    CustomizerLibraries(J2SEProjectProperties uiProps, CustomizerProviderImpl.SubCategoryProvider subcat, ProjectCustomizer.Category category) {
        this.uiProperties = uiProps;
        this.category = category;
        initComponents();        
        
        this.putClientProperty( "HelpID", "J2SE_CustomizerGeneral" ); // NOI18N

        jListCpM.setModel( uiProperties.JAVAC_MODULEPATH_MODEL );
        jListCpM.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register(uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpM),
                jButtonAddJarM.getModel(),
                jButtonAddLibraryM.getModel(),
                jButtonAddArtifactM.getModel(),
                jButtonRemoveM.getModel(),
                jButtonMoveUpM.getModel(),
                jButtonMoveDownM.getModel(),
                jButtonEditM.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        jListCpC.setModel( uiProperties.JAVAC_CLASSPATH_MODEL );
        jListCpC.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register(uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpC),
                jButtonAddJarC.getModel(),
                jButtonAddLibraryC.getModel(),
                jButtonAddArtifactC.getModel(),
                jButtonRemoveC.getModel(),
                jButtonMoveUpC.getModel(),
                jButtonMoveDownC.getModel(),
                jButtonEditC.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        jListCpPM.setModel( uiProperties.JAVAC_PROCESSORMODULEPATH_MODEL );
        jListCpPM.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register(uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpPM),
                jButtonAddJarPM.getModel(),
                jButtonAddLibraryPM.getModel(),
                jButtonAddArtifactPM.getModel(),
                jButtonRemovePM.getModel(),
                jButtonMoveUpPM.getModel(),
                jButtonMoveDownPM.getModel(),
                jButtonEditPM.getModel(),
                true,
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);

        jListCpP.setModel( uiProperties.JAVAC_PROCESSORPATH_MODEL );
        jListCpP.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register(uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpP),
                jButtonAddJarP.getModel(),
                jButtonAddLibraryP.getModel(),
                jButtonAddArtifactP.getModel(),
                jButtonRemoveP.getModel(),
                jButtonMoveUpP.getModel(),
                jButtonMoveDownP.getModel(),
                jButtonEditP.getModel(),
                true,
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);

        jListCpCTM.setModel( uiProperties.JAVAC_TEST_MODULEPATH_MODEL);
        jListCpCTM.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpCTM),
                jButtonAddJarCTM.getModel(),
                jButtonAddLibraryCTM.getModel(),
                jButtonAddArtifactCTM.getModel(),
                jButtonRemoveCTM.getModel(),
                jButtonMoveUpCTM.getModel(),
                jButtonMoveDownCTM.getModel(),
                jButtonEditCTM.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        jListCpCT.setModel( uiProperties.JAVAC_TEST_CLASSPATH_MODEL);
        jListCpCT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpCT),
                jButtonAddJarCT.getModel(),
                jButtonAddLibraryCT.getModel(),
                jButtonAddArtifactCT.getModel(),
                jButtonRemoveCT.getModel(),
                jButtonMoveUpCT.getModel(),
                jButtonMoveDownCT.getModel(),
                jButtonEditCT.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        jListCpRM.setModel( uiProperties.RUN_MODULEPATH_MODEL );
        jListCpRM.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpRM), 
                jButtonAddJarRM.getModel(),
                jButtonAddLibraryRM.getModel(),
                jButtonAddArtifactRM.getModel(),
                jButtonRemoveRM.getModel(),
                jButtonMoveUpRM.getModel(),
                jButtonMoveDownRM.getModel(),
                jButtonEditRM.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        jListCpR.setModel( uiProperties.RUN_CLASSPATH_MODEL );
        jListCpR.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpR), 
                jButtonAddJarR.getModel(),
                jButtonAddLibraryR.getModel(),
                jButtonAddArtifactR.getModel(),
                jButtonRemoveR.getModel(),
                jButtonMoveUpR.getModel(),
                jButtonMoveDownR.getModel(),
                jButtonEditR.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        jListCpRTM.setModel( uiProperties.RUN_TEST_MODULEPATH_MODEL );
        jListCpRTM.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpRTM),
                jButtonAddJarRTM.getModel(),
                jButtonAddLibraryRTM.getModel(),
                jButtonAddArtifactRTM.getModel(),
                jButtonRemoveRTM.getModel(),
                jButtonMoveUpRTM.getModel(),
                jButtonMoveDownRTM.getModel(),
                jButtonEditRTM.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        jListCpRT.setModel( uiProperties.RUN_TEST_CLASSPATH_MODEL );
        jListCpRT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register( uiProperties.getProject(),
                uiProperties.getProject().getAntProjectHelper(),
                uiProperties.getProject().getReferenceHelper(),
                EditMediator.createListComponent(jListCpRT),
                jButtonAddJarRT.getModel(),
                jButtonAddLibraryRT.getModel(),
                jButtonAddArtifactRT.getModel(),
                jButtonRemoveRT.getModel(),
                jButtonMoveUpRT.getModel(),
                jButtonMoveDownRT.getModel(),
                jButtonEditRT.getModel(),
                uiProperties.SHARED_LIBRARIES_MODEL,
                null);
        
        uiProperties.NO_DEPENDENCIES_MODEL.setMnemonic( jCheckBoxBuildSubprojects.getMnemonic() );
        jCheckBoxBuildSubprojects.setModel( uiProperties.NO_DEPENDENCIES_MODEL );                        
        librariesLocation.setDocument(uiProperties.SHARED_LIBRARIES_MODEL);
        jComboBoxTarget.setModel(uiProperties.PLATFORM_MODEL);               
        jComboBoxTarget.setRenderer(uiProperties.PLATFORM_LIST_RENDERER);
        if (!UIManager.getLookAndFeel().getClass().getName().toUpperCase().contains("AQUA")) {  //NOI18N
            //Not needed on Mac AQUA L&F also this causes an appearance problem on it
            jComboBoxTarget.putClientProperty ("JComboBox.isTableCellEditor", Boolean.TRUE);    //NOI18N
            jComboBoxTarget.addItemListener(new java.awt.event.ItemListener(){ 
                @Override
                public void itemStateChanged(java.awt.event.ItemEvent e){ 
                    javax.swing.JComboBox combo = (javax.swing.JComboBox)e.getSource(); 
                    combo.setPopupVisible(false); 
                } 
            });
        }
        testBroken();
        if (J2SECompositePanelProvider.LIBRARIES.equals(subcat.getCategory())) {
            showSubCategory(subcat.getSubcategory());
        }
        
        uiProperties.JAVAC_MODULEPATH_MODEL.addListDataListener( this );
        uiProperties.JAVAC_CLASSPATH_MODEL.addListDataListener( this );
        uiProperties.JAVAC_PROCESSORMODULEPATH_MODEL.addListDataListener( this );
        uiProperties.JAVAC_PROCESSORPATH_MODEL.addListDataListener( this );
        uiProperties.JAVAC_TEST_MODULEPATH_MODEL.addListDataListener( this );
        uiProperties.JAVAC_TEST_CLASSPATH_MODEL.addListDataListener( this );
        uiProperties.RUN_MODULEPATH_MODEL.addListDataListener( this );
        uiProperties.RUN_CLASSPATH_MODEL.addListDataListener( this );
        uiProperties.RUN_TEST_MODULEPATH_MODEL.addListDataListener( this );
        uiProperties.RUN_TEST_CLASSPATH_MODEL.addListDataListener( this );
        
        //check the sharability status of the project.
        isSharable = uiProperties.getProject().getAntProjectHelper().isSharableProject();
        if (!isSharable) {
            sharedLibrariesLabel.setEnabled(false);
            librariesLocation.setEnabled(false);
            Mnemonics.setLocalizedText(librariesBrowse, NbBundle.getMessage(CustomizerLibraries.class, "LBL_MakeSharable")); // NOI18N
            librariesBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerLibraries.class, "ACSD_MakeSharable"));
        } else {
            librariesLocation.setText(uiProperties.getProject().getAntProjectHelper().getLibrariesLocation());
        }
        
        enableModules();
    }

    /** split file name into folder and name */
    private static String[] splitPath(String s) {
        int i = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (i == -1) {
            return new String[]{s, null};
        } else {
            return new String[]{s.substring(0, i), s.substring(i+1)};
        }
    }

    private void switchLibrary() {
        String loc = librariesLocation.getText();
        LibraryManager man;
        if (loc.trim().length() > -1) {
            try {
                File base = FileUtil.toFile(uiProperties.getProject().getProjectDirectory());
                File location = FileUtil.normalizeFile(PropertyUtils.resolveFile(base, loc));
                URL url = Utilities.toURI(location).toURL();
                man = LibraryManager.forLocation(url);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                //TODO show as error in UI
                man = LibraryManager.getDefault();
            }
        } else {
            man = LibraryManager.getDefault();
        }
        
        
        DefaultListModel[] models = new DefaultListModel[]{
            uiProperties.JAVAC_MODULEPATH_MODEL,
            uiProperties.JAVAC_CLASSPATH_MODEL,
            uiProperties.JAVAC_PROCESSORMODULEPATH_MODEL,
            uiProperties.JAVAC_PROCESSORPATH_MODEL,
            uiProperties.JAVAC_TEST_MODULEPATH_MODEL,
            uiProperties.JAVAC_TEST_CLASSPATH_MODEL,
            uiProperties.RUN_MODULEPATH_MODEL,
            uiProperties.RUN_CLASSPATH_MODEL,
            uiProperties.ENDORSED_CLASSPATH_MODEL,
            uiProperties.RUN_TEST_MODULEPATH_MODEL,
            uiProperties.RUN_TEST_CLASSPATH_MODEL
           };
        for (int i = 0; i < models.length; i++) {
            for (Iterator it = ClassPathUiSupport.getIterator(models[i]); it.hasNext();) {
                ClassPathSupport.Item itm = (ClassPathSupport.Item) it.next();
                if (itm.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                    itm.reassignLibraryManager(man);
                }
            }
        }
        jTabbedPane.repaint();
        testBroken();
        
    }
        
    private void testBroken() {
        
        DefaultListModel[] models = new DefaultListModel[] {
            uiProperties.JAVAC_MODULEPATH_MODEL,
            uiProperties.JAVAC_CLASSPATH_MODEL,
            uiProperties.JAVAC_PROCESSORPATH_MODEL,
            uiProperties.JAVAC_TEST_CLASSPATH_MODEL,
            uiProperties.RUN_CLASSPATH_MODEL,
            uiProperties.ENDORSED_CLASSPATH_MODEL,
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
            category.setErrorMessage(NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_Error"));
            // do not call category.setValid(false) as this would prevent OK from being clicked, even if the error existed before
        }
        else {
            category.setErrorMessage(null);
        }
        LogicalViewProvider2 viewProvider = uiProperties.getProject().getLookup().lookup(LogicalViewProvider2.class);
        //Update the state of project's node if needed
        viewProvider.testBroken();
    }
    
    private void enableModules() {
        Object selectedItem = this.jComboBoxTarget.getSelectedItem();
        JavaPlatform jp = (selectedItem == null ? null : PlatformUiSupport.getPlatform(selectedItem));
        SpecificationVersion sv = jp != null ? jp.getSpecification().getVersion() : null;
        boolean modulesEnabled = sv != null && new SpecificationVersion("1.9").compareTo(sv) <= 0;
        jTabbedPaneCompile.setEnabledAt(0, modulesEnabled);
        jTabbedPaneCompile.setSelectedIndex(modulesEnabled ? 0 : 1);
        jTabbedPaneProcessor.setEnabledAt(0, modulesEnabled);
        jTabbedPaneProcessor.setSelectedIndex(modulesEnabled ? 0 : 1);
        jTabbedPaneRun.setEnabledAt(0, modulesEnabled);
        jTabbedPaneRun.setSelectedIndex(modulesEnabled ? 0 : 1);
        jTabbedPaneCompileTests.setEnabledAt(0, modulesEnabled);
        jTabbedPaneCompileTests.setSelectedIndex(modulesEnabled ? 0 : 1);
        jTabbedPaneRunTests.setEnabledAt(0, modulesEnabled);        
        jTabbedPaneRunTests.setSelectedIndex(modulesEnabled ? 0 : 1);
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
        if (name.equals(COMPILE_MODULE)) {
            jTabbedPane.setSelectedIndex (0);
            jTabbedPaneCompile.setSelectedIndex (0);
        }        
        if (name.equals(COMPILE)) {
            jTabbedPane.setSelectedIndex (0);
            jTabbedPaneCompile.setSelectedIndex (1);
        }        
        if (name.equals(PROCESSOR_MODULE)) {
            jTabbedPane.setSelectedIndex (1);
            jTabbedPaneCompile.setSelectedIndex (0);
        }
        if (name.equals(PROCESSOR)) {
            jTabbedPane.setSelectedIndex (1);
            jTabbedPaneCompile.setSelectedIndex (1);
        }
        else if (name.equals(COMPILE_TESTS_MODULE)) {
            jTabbedPane.setSelectedIndex (2);
            jTabbedPaneCompile.setSelectedIndex (0);
        }
        else if (name.equals(COMPILE_TESTS)) {
            jTabbedPane.setSelectedIndex (2);
            jTabbedPaneCompile.setSelectedIndex (1);
        }
        else if (name.equals(RUN_MODULE)) {
            jTabbedPane.setSelectedIndex (3);
            jTabbedPaneCompile.setSelectedIndex (0);
        }
        else if (name.equals(RUN)) {
            jTabbedPane.setSelectedIndex (3);
            jTabbedPaneCompile.setSelectedIndex (1);
        }
        else if (name.equals(RUN_TESTS_MODULE)) {
            jTabbedPane.setSelectedIndex (4);
            jTabbedPaneCompile.setSelectedIndex (0);
        }
        else if (name.equals(RUN_TESTS)) {
            jTabbedPane.setSelectedIndex (4);
            jTabbedPaneCompile.setSelectedIndex (1);
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jTabbedPane = new javax.swing.JTabbedPane();
        jTabbedPaneCompile = new javax.swing.JTabbedPane();
        jPanelCompileModulepath = new javax.swing.JPanel();
        librariesJLabel6 = new javax.swing.JLabel();
        librariesJScrollPane5 = new javax.swing.JScrollPane();
        jListCpM = new javax.swing.JList();
        jButtonAddArtifactM = new javax.swing.JButton();
        jButtonAddLibraryM = new javax.swing.JButton();
        jButtonAddJarM = new javax.swing.JButton();
        jButtonEditM = new javax.swing.JButton();
        jButtonRemoveM = new javax.swing.JButton();
        jButtonMoveUpM = new javax.swing.JButton();
        jButtonMoveDownM = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanelCompileClasspath = new javax.swing.JPanel();
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
        jLabel1 = new javax.swing.JLabel();
        jTabbedPaneProcessor = new javax.swing.JTabbedPane();
        jPanelProcessorModulepath = new javax.swing.JPanel();
        librariesJLabel7 = new javax.swing.JLabel();
        librariesJScrollPane6 = new javax.swing.JScrollPane();
        jListCpPM = new javax.swing.JList();
        jButtonAddArtifactPM = new javax.swing.JButton();
        jButtonAddLibraryPM = new javax.swing.JButton();
        jButtonAddJarPM = new javax.swing.JButton();
        jButtonEditPM = new javax.swing.JButton();
        jButtonRemovePM = new javax.swing.JButton();
        jButtonMoveUpPM = new javax.swing.JButton();
        jButtonMoveDownPM = new javax.swing.JButton();
        jPanelProcessorClasspath = new javax.swing.JPanel();
        librariesJLabel5 = new javax.swing.JLabel();
        librariesJScrollPane4 = new javax.swing.JScrollPane();
        jListCpP = new javax.swing.JList();
        jButtonAddArtifactP = new javax.swing.JButton();
        jButtonAddLibraryP = new javax.swing.JButton();
        jButtonAddJarP = new javax.swing.JButton();
        jButtonEditP = new javax.swing.JButton();
        jButtonRemoveP = new javax.swing.JButton();
        jButtonMoveUpP = new javax.swing.JButton();
        jButtonMoveDownP = new javax.swing.JButton();
        jTabbedPaneRun = new javax.swing.JTabbedPane();
        jPanelRunModulepath = new javax.swing.JPanel();
        librariesJLabel8 = new javax.swing.JLabel();
        librariesJScrollPane7 = new javax.swing.JScrollPane();
        jListCpRM = new javax.swing.JList();
        jButtonAddArtifactRM = new javax.swing.JButton();
        jButtonAddLibraryRM = new javax.swing.JButton();
        jButtonAddJarRM = new javax.swing.JButton();
        jButtonEditRM = new javax.swing.JButton();
        jButtonRemoveRM = new javax.swing.JButton();
        jButtonMoveUpRM = new javax.swing.JButton();
        jButtonMoveDownRM = new javax.swing.JButton();
        jPanelRunClasspath = new javax.swing.JPanel();
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
        jTabbedPaneCompileTests = new javax.swing.JTabbedPane();
        jPanelCompileTestsModulepath = new javax.swing.JPanel();
        librariesJLabel9 = new javax.swing.JLabel();
        librariesJScrollPane8 = new javax.swing.JScrollPane();
        jListCpCTM = new javax.swing.JList();
        jButtonAddArtifactCTM = new javax.swing.JButton();
        jButtonAddLibraryCTM = new javax.swing.JButton();
        jButtonAddJarCTM = new javax.swing.JButton();
        jButtonEditCTM = new javax.swing.JButton();
        jButtonRemoveCTM = new javax.swing.JButton();
        jButtonMoveUpCTM = new javax.swing.JButton();
        jButtonMoveDownCTM = new javax.swing.JButton();
        jPanelCompileTestsClasspath = new javax.swing.JPanel();
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
        jTabbedPaneRunTests = new javax.swing.JTabbedPane();
        jPanelRunTestsModulepath = new javax.swing.JPanel();
        librariesJLabel10 = new javax.swing.JLabel();
        librariesJScrollPane9 = new javax.swing.JScrollPane();
        jListCpRTM = new javax.swing.JList();
        jButtonAddArtifactRTM = new javax.swing.JButton();
        jButtonAddLibraryRTM = new javax.swing.JButton();
        jButtonAddJarRTM = new javax.swing.JButton();
        jButtonEditRTM = new javax.swing.JButton();
        jButtonRemoveRTM = new javax.swing.JButton();
        jButtonMoveUpRTM = new javax.swing.JButton();
        jButtonMoveDownRTM = new javax.swing.JButton();
        jPanelRunTestsClasspath = new javax.swing.JPanel();
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
        sharedLibrariesLabel = new javax.swing.JLabel();
        librariesLocation = new javax.swing.JTextField();
        librariesBrowse = new javax.swing.JButton();

        jLabelTarget.setLabelFor(jComboBoxTarget);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTarget, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_Platform_JLabel")); // NOI18N

        jComboBoxTarget.setMinimumSize(this.jComboBoxTarget.getPreferredSize());
        jComboBoxTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxTargetActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_Platform_JButton")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewPlatform(evt);
            }
        });

        jPanelCompileModulepath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelCompileModulepath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel6.setLabelFor(jListCpM);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel6, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesM_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileModulepath.add(librariesJLabel6, gridBagConstraints);

        librariesJScrollPane5.setViewportView(jListCpM);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileModulepath.add(librariesJScrollPane5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileModulepath.add(jButtonAddArtifactM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileModulepath.add(jButtonAddLibraryM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileModulepath.add(jButtonAddJarM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileModulepath.add(jButtonEditM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileModulepath.add(jButtonRemoveM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileModulepath.add(jButtonMoveUpM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileModulepath.add(jButtonMoveDownM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "MSG_CustomizerLibraries_ModuleCpMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanelCompileModulepath.add(jLabel2, gridBagConstraints);

        jTabbedPaneCompile.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Modulepath_Tab"), jPanelCompileModulepath); // NOI18N

        jPanelCompileClasspath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelCompileClasspath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelCompileClasspath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel1.setLabelFor(jListCpC);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesC_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileClasspath.add(librariesJLabel1, gridBagConstraints);

        jListCpC.setVisibleRowCount(5);
        librariesJScrollPane.setViewportView(jListCpC);
        jListCpC.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AN_CustomizerLibraries_jListClasspathC")); // NOI18N
        jListCpC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jLabelClasspathC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileClasspath.add(librariesJScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileClasspath.add(jButtonAddArtifactC, gridBagConstraints);
        jButtonAddArtifactC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileClasspath.add(jButtonAddLibraryC, gridBagConstraints);
        jButtonAddLibraryC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileClasspath.add(jButtonAddJarC, gridBagConstraints);
        jButtonAddJarC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileClasspath.add(jButtonEditC, gridBagConstraints);
        jButtonEditC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileClasspath.add(jButtonRemoveC, gridBagConstraints);
        jButtonRemoveC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileClasspath.add(jButtonMoveUpC, gridBagConstraints);
        jButtonMoveUpC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileClasspath.add(jButtonMoveDownC, gridBagConstraints);
        jButtonMoveDownC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jLabel1.setLabelFor(jListCpC);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "MSG_CustomizerLibraries_CompileCpMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanelCompileClasspath.add(jLabel1, gridBagConstraints);

        jTabbedPaneCompile.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Classpath_Tab"), jPanelCompileClasspath); // NOI18N

        jTabbedPane.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesTab"), jTabbedPaneCompile); // NOI18N
        jTabbedPaneCompile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSN_CustomizerLibraries_JTabbedPane")); // NOI18N
        jTabbedPaneCompile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_JTabbedPane")); // NOI18N

        jPanelProcessorModulepath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelProcessorModulepath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelProcessorModulepath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel7.setLabelFor(jListCpP);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel7, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesMP_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelProcessorModulepath.add(librariesJLabel7, gridBagConstraints);

        jListCpPM.setVisibleRowCount(5);
        librariesJScrollPane6.setViewportView(jListCpPM);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelProcessorModulepath.add(librariesJScrollPane6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactPM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelProcessorModulepath.add(jButtonAddArtifactPM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryPM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelProcessorModulepath.add(jButtonAddLibraryPM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarPM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorModulepath.add(jButtonAddJarPM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditPM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorModulepath.add(jButtonEditPM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemovePM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorModulepath.add(jButtonRemovePM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpPM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelProcessorModulepath.add(jButtonMoveUpPM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownPM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorModulepath.add(jButtonMoveDownPM, gridBagConstraints);

        jTabbedPaneProcessor.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Modulepath_Tab"), jPanelProcessorModulepath); // NOI18N

        jPanelProcessorClasspath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelProcessorClasspath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelProcessorClasspath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel5.setLabelFor(jListCpP);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel5, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesP_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelProcessorClasspath.add(librariesJLabel5, gridBagConstraints);

        jListCpP.setVisibleRowCount(5);
        librariesJScrollPane4.setViewportView(jListCpP);
        jListCpP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "L_ACSN_ProcesserLibraries")); // NOI18N
        jListCpP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "L_ACSD_ProcesserLibraries")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelProcessorClasspath.add(librariesJScrollPane4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactP, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelProcessorClasspath.add(jButtonAddArtifactP, gridBagConstraints);
        jButtonAddArtifactP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSN_Add_Project")); // NOI18N
        jButtonAddArtifactP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSD_Add_Project")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryP, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelProcessorClasspath.add(jButtonAddLibraryP, gridBagConstraints);
        jButtonAddLibraryP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSN_Add_Library")); // NOI18N
        jButtonAddLibraryP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSD_Add_Library")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarP, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorClasspath.add(jButtonAddJarP, gridBagConstraints);
        jButtonAddJarP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSN_Add_Jar")); // NOI18N
        jButtonAddJarP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSD_Add_Jar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditP, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorClasspath.add(jButtonEditP, gridBagConstraints);
        jButtonEditP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSN_Edit")); // NOI18N
        jButtonEditP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSD_Edit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveP, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorClasspath.add(jButtonRemoveP, gridBagConstraints);
        jButtonRemoveP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSN_Remove")); // NOI18N
        jButtonRemoveP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSD_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpP, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelProcessorClasspath.add(jButtonMoveUpP, gridBagConstraints);
        jButtonMoveUpP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSN_MoveUp")); // NOI18N
        jButtonMoveUpP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSD_MoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownP, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelProcessorClasspath.add(jButtonMoveDownP, gridBagConstraints);
        jButtonMoveDownP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSN_MoveDown")); // NOI18N
        jButtonMoveDownP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "BTN_ACSD_MoveDown")); // NOI18N

        jTabbedPaneProcessor.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Classpath_Tab"), jPanelProcessorClasspath); // NOI18N

        jTabbedPane.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Processors_Tab"), jTabbedPaneProcessor); // NOI18N

        jPanelRunModulepath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelRunModulepath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelRunModulepath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel8.setLabelFor(jListCpR);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel8, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesMR_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunModulepath.add(librariesJLabel8, gridBagConstraints);

        jListCpRM.setVisibleRowCount(5);
        librariesJScrollPane7.setViewportView(jListCpRM);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRunModulepath.add(librariesJScrollPane7, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactRM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunModulepath.add(jButtonAddArtifactRM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryRM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunModulepath.add(jButtonAddLibraryRM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarRM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunModulepath.add(jButtonAddJarRM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditRM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunModulepath.add(jButtonEditRM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveRM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunModulepath.add(jButtonRemoveRM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpRM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunModulepath.add(jButtonMoveUpRM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownRM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunModulepath.add(jButtonMoveDownRM, gridBagConstraints);

        jTabbedPaneRun.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Modulepath_Tab"), jPanelRunModulepath); // NOI18N

        jPanelRunClasspath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelRunClasspath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelRunClasspath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel3.setLabelFor(jListCpR);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel3, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesR_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunClasspath.add(librariesJLabel3, gridBagConstraints);

        jListCpR.setVisibleRowCount(5);
        librariesJScrollPane2.setViewportView(jListCpR);
        jListCpR.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AN_CustomizerLibraries_jListClasspathR")); // NOI18N
        jListCpR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jLabelClasspathR")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRunClasspath.add(librariesJScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunClasspath.add(jButtonAddArtifactR, gridBagConstraints);
        jButtonAddArtifactR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunClasspath.add(jButtonAddLibraryR, gridBagConstraints);
        jButtonAddLibraryR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunClasspath.add(jButtonAddJarR, gridBagConstraints);
        jButtonAddJarR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunClasspath.add(jButtonEditR, gridBagConstraints);
        jButtonEditR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunClasspath.add(jButtonRemoveR, gridBagConstraints);
        jButtonRemoveR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunClasspath.add(jButtonMoveUpR, gridBagConstraints);
        jButtonMoveUpR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownR, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunClasspath.add(jButtonMoveDownR, gridBagConstraints);
        jButtonMoveDownR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jTabbedPaneRun.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Classpath_Tab"), jPanelRunClasspath); // NOI18N

        jTabbedPane.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Run_Tab"), jTabbedPaneRun); // NOI18N

        jPanelCompileTestsModulepath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelCompileTestsModulepath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelCompileTestsModulepath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel9.setLabelFor(jListCpCT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel9, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesMT_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileTestsModulepath.add(librariesJLabel9, gridBagConstraints);

        jListCpCTM.setVisibleRowCount(5);
        librariesJScrollPane8.setViewportView(jListCpCTM);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileTestsModulepath.add(librariesJScrollPane8, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactCTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTestsModulepath.add(jButtonAddArtifactCTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryCTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTestsModulepath.add(jButtonAddLibraryCTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarCTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsModulepath.add(jButtonAddJarCTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditCTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsModulepath.add(jButtonEditCTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveCTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsModulepath.add(jButtonRemoveCTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpCTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTestsModulepath.add(jButtonMoveUpCTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownCTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsModulepath.add(jButtonMoveDownCTM, gridBagConstraints);

        jTabbedPaneCompileTests.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Modulepath_Tab"), jPanelCompileTestsModulepath); // NOI18N

        jPanelCompileTestsClasspath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelCompileTestsClasspath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelCompileTestsClasspath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel2.setLabelFor(jListCpCT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel2, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesCT_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileTestsClasspath.add(librariesJLabel2, gridBagConstraints);

        jListCpCT.setVisibleRowCount(5);
        librariesJScrollPane1.setViewportView(jListCpCT);
        jListCpCT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AN_CustomizerLibraries_jListClasspathCT")); // NOI18N
        jListCpCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jLabelClasspathCT")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileTestsClasspath.add(librariesJScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTestsClasspath.add(jButtonAddArtifactCT, gridBagConstraints);
        jButtonAddArtifactCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTestsClasspath.add(jButtonAddLibraryCT, gridBagConstraints);
        jButtonAddLibraryCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsClasspath.add(jButtonAddJarCT, gridBagConstraints);
        jButtonAddJarCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsClasspath.add(jButtonEditCT, gridBagConstraints);
        jButtonEditCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsClasspath.add(jButtonRemoveCT, gridBagConstraints);
        jButtonRemoveCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTestsClasspath.add(jButtonMoveUpCT, gridBagConstraints);
        jButtonMoveUpCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTestsClasspath.add(jButtonMoveDownCT, gridBagConstraints);
        jButtonMoveDownCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jTabbedPaneCompileTests.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Classpath_Tab"), jPanelCompileTestsClasspath); // NOI18N

        jTabbedPane.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_TestLibrariesTab"), jTabbedPaneCompileTests); // NOI18N

        jPanelRunTestsModulepath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelRunTestsModulepath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelRunTestsModulepath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel10.setLabelFor(jListCpRT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel10, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesMRT_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunTestsModulepath.add(librariesJLabel10, gridBagConstraints);

        jListCpRTM.setVisibleRowCount(5);
        librariesJScrollPane9.setViewportView(jListCpRTM);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRunTestsModulepath.add(librariesJScrollPane9, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactRTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTestsModulepath.add(jButtonAddArtifactRTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryRTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTestsModulepath.add(jButtonAddLibraryRTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarRTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsModulepath.add(jButtonAddJarRTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditRTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsModulepath.add(jButtonEditRTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveRTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsModulepath.add(jButtonRemoveRTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpRTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTestsModulepath.add(jButtonMoveUpRTM, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownRTM, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsModulepath.add(jButtonMoveDownRTM, gridBagConstraints);

        jTabbedPaneRunTests.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Modulepath_Tab"), jPanelRunTestsModulepath); // NOI18N

        jPanelRunTestsClasspath.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelRunTestsClasspath.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelRunTestsClasspath.setLayout(new java.awt.GridBagLayout());

        librariesJLabel4.setLabelFor(jListCpRT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel4, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesRT_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunTestsClasspath.add(librariesJLabel4, gridBagConstraints);

        jListCpRT.setVisibleRowCount(5);
        librariesJScrollPane3.setViewportView(jListCpRT);
        jListCpRT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AN_CustomizerLibraries_jListClasspathRT")); // NOI18N
        jListCpRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jLabelClasspathRT")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRunTestsClasspath.add(librariesJScrollPane3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTestsClasspath.add(jButtonAddArtifactRT, gridBagConstraints);
        jButtonAddArtifactRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTestsClasspath.add(jButtonAddLibraryRT, gridBagConstraints);
        jButtonAddLibraryRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsClasspath.add(jButtonAddJarRT, gridBagConstraints);
        jButtonAddJarRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsClasspath.add(jButtonEditRT, gridBagConstraints);
        jButtonEditRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsClasspath.add(jButtonRemoveRT, gridBagConstraints);
        jButtonRemoveRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTestsClasspath.add(jButtonMoveUpRT, gridBagConstraints);
        jButtonMoveUpRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTestsClasspath.add(jButtonMoveDownRT, gridBagConstraints);
        jButtonMoveDownRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jTabbedPaneRunTests.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Classpath_Tab"), jPanelRunTestsClasspath); // NOI18N

        jTabbedPane.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_RunTests_Tab"), jTabbedPaneRunTests); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxBuildSubprojects, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Build_Subprojects")); // NOI18N

        sharedLibrariesLabel.setLabelFor(librariesLocation);
        org.openide.awt.Mnemonics.setLocalizedText(sharedLibrariesLabel, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_SharedLibraries")); // NOI18N

        librariesLocation.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(librariesBrowse, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizerLibraries_Browse_JButton")); // NOI18N
        librariesBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                librariesBrowseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sharedLibrariesLabel)
                    .addComponent(jLabelTarget))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(librariesLocation)
                    .addComponent(jComboBoxTarget, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(librariesBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE)
            .addComponent(jCheckBoxBuildSubprojects, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTarget)
                    .addComponent(jButton1)
                    .addComponent(jComboBoxTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sharedLibrariesLabel)
                    .addComponent(librariesBrowse)
                    .addComponent(librariesLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxBuildSubprojects))
        );

        jLabelTarget.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerGeneral_jLabelTarget")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerGeneral_jButton1")); // NOI18N
        jCheckBoxBuildSubprojects.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "AD_CheckBoxBuildSubprojects")); // NOI18N
        librariesLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_librariesLocation")); // NOI18N
        librariesBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_librariesBrowse")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void createNewPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewPlatform
        Object selectedItem = this.jComboBoxTarget.getSelectedItem();
        JavaPlatform jp = (selectedItem == null ? null : PlatformUiSupport.getPlatform(selectedItem));
        PlatformsCustomizer.showCustomizer(jp);        
    }//GEN-LAST:event_createNewPlatform

    private void librariesBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_librariesBrowseActionPerformed
        if (!isSharable) {
            if (uiProperties.makeSharable()) {
                isSharable = true;
                sharedLibrariesLabel.setEnabled(true);
                librariesLocation.setEnabled(true);
                librariesLocation.setText(uiProperties.getProject().getAntProjectHelper().getLibrariesLocation());
                Mnemonics.setLocalizedText(librariesBrowse, NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizerLibraries_Browse_JButton")); // NOI18N
                updateJars(uiProperties.JAVAC_MODULEPATH_MODEL);
                updateJars(uiProperties.JAVAC_CLASSPATH_MODEL);
                updateJars(uiProperties.JAVAC_PROCESSORMODULEPATH_MODEL);
                updateJars(uiProperties.JAVAC_PROCESSORPATH_MODEL);
                updateJars(uiProperties.JAVAC_TEST_MODULEPATH_MODEL);
                updateJars(uiProperties.JAVAC_TEST_CLASSPATH_MODEL);
                updateJars(uiProperties.RUN_MODULEPATH_MODEL);
                updateJars(uiProperties.RUN_CLASSPATH_MODEL);
                updateJars(uiProperties.RUN_TEST_MODULEPATH_MODEL);
                updateJars(uiProperties.RUN_TEST_CLASSPATH_MODEL);
                updateJars(uiProperties.ENDORSED_CLASSPATH_MODEL);
                switchLibrary();
            }
        } else {
            File prjLoc = FileUtil.toFile(uiProperties.getProject().getProjectDirectory());
            String s[] = splitPath(librariesLocation.getText().trim());
            String loc = SharableLibrariesUtils.browseForLibraryLocation(s[0], this, prjLoc);
            if (loc != null) {
                final String path = s[1] != null ? loc + File.separator + s[1] :
                    loc + File.separator + SharableLibrariesUtils.DEFAULT_LIBRARIES_FILENAME;
                final File base = FileUtil.toFile(uiProperties.getProject().getProjectDirectory());
                final File location = FileUtil.normalizeFile(PropertyUtils.resolveFile(base, path));
                if (location.exists()) {
                    librariesLocation.setText(path);
                    switchLibrary();
                } else {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor(
                            NbBundle.getMessage(CustomizerLibraries.class, "ERR_InvalidProjectLibrariesFolder"),
                            NbBundle.getMessage(CustomizerLibraries.class, "TITLE_InvalidProjectLibrariesFolder"),
                            NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object[] {NotifyDescriptor.OK_OPTION},
                            NotifyDescriptor.OK_OPTION));
                }
            }
        }
}//GEN-LAST:event_librariesBrowseActionPerformed

    private void jComboBoxTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxTargetActionPerformed
        enableModules();
    }//GEN-LAST:event_jComboBoxTargetActionPerformed
   
    private void updateJars(DefaultListModel model) {
        for (int i = 0; i < model.size(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) model.get(i);
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                if (item.getReference() != null) {
                    item.updateJarReference(uiProperties.getProject().getAntProjectHelper());
                }
            }
        }
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddArtifactC;
    private javax.swing.JButton jButtonAddArtifactCT;
    private javax.swing.JButton jButtonAddArtifactCTM;
    private javax.swing.JButton jButtonAddArtifactM;
    private javax.swing.JButton jButtonAddArtifactP;
    private javax.swing.JButton jButtonAddArtifactPM;
    private javax.swing.JButton jButtonAddArtifactR;
    private javax.swing.JButton jButtonAddArtifactRM;
    private javax.swing.JButton jButtonAddArtifactRT;
    private javax.swing.JButton jButtonAddArtifactRTM;
    private javax.swing.JButton jButtonAddJarC;
    private javax.swing.JButton jButtonAddJarCT;
    private javax.swing.JButton jButtonAddJarCTM;
    private javax.swing.JButton jButtonAddJarM;
    private javax.swing.JButton jButtonAddJarP;
    private javax.swing.JButton jButtonAddJarPM;
    private javax.swing.JButton jButtonAddJarR;
    private javax.swing.JButton jButtonAddJarRM;
    private javax.swing.JButton jButtonAddJarRT;
    private javax.swing.JButton jButtonAddJarRTM;
    private javax.swing.JButton jButtonAddLibraryC;
    private javax.swing.JButton jButtonAddLibraryCT;
    private javax.swing.JButton jButtonAddLibraryCTM;
    private javax.swing.JButton jButtonAddLibraryM;
    private javax.swing.JButton jButtonAddLibraryP;
    private javax.swing.JButton jButtonAddLibraryPM;
    private javax.swing.JButton jButtonAddLibraryR;
    private javax.swing.JButton jButtonAddLibraryRM;
    private javax.swing.JButton jButtonAddLibraryRT;
    private javax.swing.JButton jButtonAddLibraryRTM;
    private javax.swing.JButton jButtonEditC;
    private javax.swing.JButton jButtonEditCT;
    private javax.swing.JButton jButtonEditCTM;
    private javax.swing.JButton jButtonEditM;
    private javax.swing.JButton jButtonEditP;
    private javax.swing.JButton jButtonEditPM;
    private javax.swing.JButton jButtonEditR;
    private javax.swing.JButton jButtonEditRM;
    private javax.swing.JButton jButtonEditRT;
    private javax.swing.JButton jButtonEditRTM;
    private javax.swing.JButton jButtonMoveDownC;
    private javax.swing.JButton jButtonMoveDownCT;
    private javax.swing.JButton jButtonMoveDownCTM;
    private javax.swing.JButton jButtonMoveDownM;
    private javax.swing.JButton jButtonMoveDownP;
    private javax.swing.JButton jButtonMoveDownPM;
    private javax.swing.JButton jButtonMoveDownR;
    private javax.swing.JButton jButtonMoveDownRM;
    private javax.swing.JButton jButtonMoveDownRT;
    private javax.swing.JButton jButtonMoveDownRTM;
    private javax.swing.JButton jButtonMoveUpC;
    private javax.swing.JButton jButtonMoveUpCT;
    private javax.swing.JButton jButtonMoveUpCTM;
    private javax.swing.JButton jButtonMoveUpM;
    private javax.swing.JButton jButtonMoveUpP;
    private javax.swing.JButton jButtonMoveUpPM;
    private javax.swing.JButton jButtonMoveUpR;
    private javax.swing.JButton jButtonMoveUpRM;
    private javax.swing.JButton jButtonMoveUpRT;
    private javax.swing.JButton jButtonMoveUpRTM;
    private javax.swing.JButton jButtonRemoveC;
    private javax.swing.JButton jButtonRemoveCT;
    private javax.swing.JButton jButtonRemoveCTM;
    private javax.swing.JButton jButtonRemoveM;
    private javax.swing.JButton jButtonRemoveP;
    private javax.swing.JButton jButtonRemovePM;
    private javax.swing.JButton jButtonRemoveR;
    private javax.swing.JButton jButtonRemoveRM;
    private javax.swing.JButton jButtonRemoveRT;
    private javax.swing.JButton jButtonRemoveRTM;
    private javax.swing.JCheckBox jCheckBoxBuildSubprojects;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JList jListCpC;
    private javax.swing.JList jListCpCT;
    private javax.swing.JList jListCpCTM;
    private javax.swing.JList jListCpM;
    private javax.swing.JList jListCpP;
    private javax.swing.JList jListCpPM;
    private javax.swing.JList jListCpR;
    private javax.swing.JList jListCpRM;
    private javax.swing.JList jListCpRT;
    private javax.swing.JList jListCpRTM;
    private javax.swing.JPanel jPanelCompileClasspath;
    private javax.swing.JPanel jPanelCompileModulepath;
    private javax.swing.JPanel jPanelCompileTestsClasspath;
    private javax.swing.JPanel jPanelCompileTestsModulepath;
    private javax.swing.JPanel jPanelProcessorClasspath;
    private javax.swing.JPanel jPanelProcessorModulepath;
    private javax.swing.JPanel jPanelRunClasspath;
    private javax.swing.JPanel jPanelRunModulepath;
    private javax.swing.JPanel jPanelRunTestsClasspath;
    private javax.swing.JPanel jPanelRunTestsModulepath;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTabbedPane jTabbedPaneCompile;
    private javax.swing.JTabbedPane jTabbedPaneCompileTests;
    private javax.swing.JTabbedPane jTabbedPaneProcessor;
    private javax.swing.JTabbedPane jTabbedPaneRun;
    private javax.swing.JTabbedPane jTabbedPaneRunTests;
    private javax.swing.JButton librariesBrowse;
    private javax.swing.JLabel librariesJLabel1;
    private javax.swing.JLabel librariesJLabel10;
    private javax.swing.JLabel librariesJLabel2;
    private javax.swing.JLabel librariesJLabel3;
    private javax.swing.JLabel librariesJLabel4;
    private javax.swing.JLabel librariesJLabel5;
    private javax.swing.JLabel librariesJLabel6;
    private javax.swing.JLabel librariesJLabel7;
    private javax.swing.JLabel librariesJLabel8;
    private javax.swing.JLabel librariesJLabel9;
    private javax.swing.JScrollPane librariesJScrollPane;
    private javax.swing.JScrollPane librariesJScrollPane1;
    private javax.swing.JScrollPane librariesJScrollPane2;
    private javax.swing.JScrollPane librariesJScrollPane3;
    private javax.swing.JScrollPane librariesJScrollPane4;
    private javax.swing.JScrollPane librariesJScrollPane5;
    private javax.swing.JScrollPane librariesJScrollPane6;
    private javax.swing.JScrollPane librariesJScrollPane7;
    private javax.swing.JScrollPane librariesJScrollPane8;
    private javax.swing.JScrollPane librariesJScrollPane9;
    private javax.swing.JTextField librariesLocation;
    private javax.swing.JLabel sharedLibrariesLabel;
    // End of variables declaration//GEN-END:variables
        
}
