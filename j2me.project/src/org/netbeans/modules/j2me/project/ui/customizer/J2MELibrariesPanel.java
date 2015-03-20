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

package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.CardLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2me.project.J2MEProjectUtils;
import org.netbeans.modules.j2me.project.ui.LibletListCellRenderer;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Customizer for general project attributes.
 *
 * @author  Theofanis Oikonomou
 */
public class J2MELibrariesPanel extends JPanel implements HelpCtx.Provider, ListDataListener {
    
    public static final String COMPILE = "COMPILE";  //NOI18N
    public static final String PROCESSOR = "PROCESSOR";  //NOI18N
    public static final String RUN = "RUN";          //NOI18N
    public static final String COMPILE_TESTS = "COMPILE_TESTS"; //NOI18N
    public final String RUN_TESTS = "RUN_TESTS";  //NOI18N        
    
    private final J2MEProjectProperties uiProperties;
    private boolean isSharable;
    private final ProjectCustomizer.Category category;
    
    J2MELibrariesPanel(J2MEProjectProperties uiProps, CustomizerProviderImpl.SubCategoryProvider subcat, ProjectCustomizer.Category category) {
        this.uiProperties = uiProps;
        this.category = category;
        initComponents();        
        
        this.putClientProperty( "HelpID", "J2SE_CustomizerGeneral" ); // NOI18N

        jListCpC.setModel( uiProperties.JAVAC_CLASSPATH_MODEL );
        jListCpC.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register(uiProperties.getProject(),
                uiProperties.getProject().getHelper(),
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
        
        jListCpP.setModel( uiProperties.JAVAC_PROCESSORPATH_MODEL );
        jListCpP.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        EditMediator.register(uiProperties.getProject(),
                uiProperties.getProject().getHelper(),
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

//        jListCpCT.setModel( uiProperties.JAVAC_TEST_CLASSPATH_MODEL);
//        jListCpCT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
//        EditMediator.register( uiProperties.getProject(),
//                uiProperties.getProject().getHelper(),
//                uiProperties.getProject().getReferenceHelper(),
//                EditMediator.createListComponent(jListCpCT),
//                jButtonAddJarCT.getModel(),
//                jButtonAddLibraryCT.getModel(),
//                jButtonAddArtifactCT.getModel(),
//                jButtonRemoveCT.getModel(),
//                jButtonMoveUpCT.getModel(),
//                jButtonMoveDownCT.getModel(),
//                jButtonEditCT.getModel(),
//                uiProperties.SHARED_LIBRARIES_MODEL,
//                null);
//        
//        jListCpR.setModel( uiProperties.RUN_CLASSPATH_MODEL );
//        jListCpR.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
//        EditMediator.register( uiProperties.getProject(),
//                uiProperties.getProject().getHelper(),
//                uiProperties.getProject().getReferenceHelper(),
//                EditMediator.createListComponent(jListCpR), 
//                jButtonAddJarR.getModel(),
//                jButtonAddLibraryR.getModel(),
//                jButtonAddArtifactR.getModel(),
//                jButtonRemoveR.getModel(),
//                jButtonMoveUpR.getModel(),
//                jButtonMoveDownR.getModel(),
//                jButtonEditR.getModel(),
//                uiProperties.SHARED_LIBRARIES_MODEL,
//                null);
//        
//        jListCpRT.setModel( uiProperties.RUN_TEST_CLASSPATH_MODEL );
//        jListCpRT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
//        EditMediator.register( uiProperties.getProject(),
//                uiProperties.getProject().getHelper(),
//                uiProperties.getProject().getReferenceHelper(),
//                EditMediator.createListComponent(jListCpRT),
//                jButtonAddJarRT.getModel(),
//                jButtonAddLibraryRT.getModel(),
//                jButtonAddArtifactRT.getModel(),
//                jButtonRemoveRT.getModel(),
//                jButtonMoveUpRT.getModel(),
//                jButtonMoveDownRT.getModel(),
//                jButtonEditRT.getModel(),
//                uiProperties.SHARED_LIBRARIES_MODEL,
//                null);
        
        uiProperties.NO_DEPENDENCIES_MODEL.setMnemonic( jCheckBoxBuildSubprojects.getMnemonic() );
        jCheckBoxBuildSubprojects.setModel( uiProperties.NO_DEPENDENCIES_MODEL );                        
        librariesLocation.setDocument(uiProperties.SHARED_LIBRARIES_MODEL);
        testBroken();
        if (J2MECompositeCategoryProvider.LIBRARIES.equals(subcat.getCategory())) {
            showSubCategory(subcat.getSubcategory());
        }
        
        uiProperties.JAVAC_CLASSPATH_MODEL.addListDataListener( this );
        uiProperties.JAVAC_PROCESSORPATH_MODEL.addListDataListener( this );
//        uiProperties.JAVAC_TEST_CLASSPATH_MODEL.addListDataListener( this );
//        uiProperties.RUN_CLASSPATH_MODEL.addListDataListener( this );
//        uiProperties.RUN_TEST_CLASSPATH_MODEL.addListDataListener( this );
        
        //check the sharability status of the project.
        isSharable = uiProperties.getProject().getHelper().isSharableProject();
        if (!isSharable) {
            sharedLibrariesLabel.setEnabled(false);
            librariesLocation.setEnabled(false);
            Mnemonics.setLocalizedText(librariesBrowse, NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_MakeSharable")); // NOI18N
            librariesBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_MakeSharable"));
        } else {
            librariesLocation.setText(uiProperties.getProject().getHelper().getLibrariesLocation());
        }

        //TODO: remove unused tabs completely from this class
        jTabbedPane1.remove(jPanelRun);
        jTabbedPane1.remove(jPanelCompileTests);
        jTabbedPane1.remove(jPanelRunTests);

        //sync compile classpath libs and liblets from project properties
        updateLiblets();
        jComboBoxLiblet.setModel(uiProperties.LIBLETS_MODEL);
        jComboBoxLiblet.setRenderer(new LibletListCellRenderer());
        updateLibletFormValues();
        jTextFieldLibletUrl.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateUrl();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateUrl();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateUrl();
            }
            
            private void updateUrl() {
                LibletInfo li = (LibletInfo) jComboBoxLiblet.getSelectedItem();
                if (li != null) {
                    li.setUrl(jTextFieldLibletUrl.getText());
                }
            }
        });
        
        DocumentListener documentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDocument(e.getDocument());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDocument(e.getDocument());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDocument(e.getDocument());
            }
            
            private void updateDocument(Document doc) {
                LibletInfo li = (LibletInfo) jComboBoxLiblet.getSelectedItem();
                if (li == null) {
                    return;
                }
                if (doc.equals(jTextFieldLibletNameValue.getDocument())) {
                    li.setName(jTextFieldLibletNameValue.getText());
                } else if (doc.equals(jTextFieldLibletVendorValue.getDocument())) {
                    li.setVendor(jTextFieldLibletVendorValue.getText());
                } else if (doc.equals(jTextFieldLibletVersionValue.getDocument())) {
                    li.setVersion(jTextFieldLibletVersionValue.getText());
                }
                jComboBoxLiblet.repaint();
            }
        };
        jTextFieldLibletNameValue.getDocument().addDocumentListener(documentListener);
        jTextFieldLibletVendorValue.getDocument().addDocumentListener(documentListener);
        jTextFieldLibletVersionValue.getDocument().addDocumentListener(documentListener);
        
        ChangeListener radioLevelListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JRadioButton button = (JRadioButton) e.getSource();
                if (button.isSelected()) {
                    LibletInfo li = (LibletInfo) jComboBoxLiblet.getSelectedItem();
                    if (li != null) {
                        li.setRequirement(LibletInfo.Requirement.valueOf(button.getActionCommand()));
                    }
                }
            }
        };
        jRadioButtonOptional.addChangeListener(radioLevelListener);
        jRadioButtonRequired.addChangeListener(radioLevelListener);
        
        ChangeListener radioTypeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JRadioButton button = (JRadioButton) e.getSource();
                if (button.isSelected()) {
                    LibletInfo li = (LibletInfo) jComboBoxLiblet.getSelectedItem();
                    if (li != null) {
                        li.setType(LibletInfo.LibletType.valueOf(button.getActionCommand()));
                        jTextFieldLibletVendorValue.setText(li.getType() != LibletInfo.LibletType.SERVICE ? li.getVendor() : "");
                        jTextFieldLibletVersionValue.setText(li.getType() != LibletInfo.LibletType.SERVICE ? li.getVersion() : "");
                        jTextFieldLibletVendorValue.setEnabled(li.getType() != LibletInfo.LibletType.LIBLET);
                        jTextFieldLibletVendorValue.setVisible(li.getType() != LibletInfo.LibletType.SERVICE);
                        labelLibletVendor.setVisible(li.getType() != LibletInfo.LibletType.SERVICE);
                        jTextFieldLibletVersionValue.setVisible(li.getType() != LibletInfo.LibletType.SERVICE);
                        labelLibletVersion.setVisible(li.getType() != LibletInfo.LibletType.SERVICE);
                        jComboBoxLiblet.repaint();
                    }
                }
            }
        };
        jRadioButtonLiblet.addChangeListener(radioTypeListener);
        jRadioButtonStandard.addChangeListener(radioTypeListener);
        jRadioButtonService.addChangeListener(radioTypeListener);
        jRadioButtonProprietary.addChangeListener(radioTypeListener);
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
            uiProperties.JAVAC_CLASSPATH_MODEL,
            uiProperties.JAVAC_PROCESSORPATH_MODEL,
            uiProperties.JAVAC_TEST_CLASSPATH_MODEL,
            uiProperties.RUN_CLASSPATH_MODEL,
            uiProperties.ENDORSED_CLASSPATH_MODEL,
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
        jTabbedPane1.repaint();
        testBroken();
        
    }
        
    private void testBroken() {
        
        DefaultListModel[] models = new DefaultListModel[] {
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
            category.setErrorMessage(NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Libraries_Error"));
            // do not call category.setValid(false) as this would prevent OK from being clicked, even if the error existed before
        }
        else {
            category.setErrorMessage(null);
        }
        LogicalViewProvider2 viewProvider = uiProperties.getProject().getLookup().lookup(LogicalViewProvider2.class);
        //Update the state of project's node if needed
        viewProvider.testBroken();
    }
    
    // Implementation of HelpCtx.Provider --------------------------------------
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.j2me.project.ui.customizer.J2MELibrariesPanel"); //NOI18N
    }        

    
    // Implementation of ListDataListener --------------------------------------
    
    
    public void intervalRemoved( ListDataEvent e ) {
        testBroken();
        updateLiblets();
    }

    @Override
    public void intervalAdded( ListDataEvent e ) {
        updateLiblets();
    }

    public void contentsChanged( ListDataEvent e ) {
        // NOP
    }

    private void updateLiblets() {
        DefaultComboBoxModel<LibletInfo> liblets = uiProperties.LIBLETS_MODEL;
        if (liblets == null) {
            uiProperties.LIBLETS_MODEL = new DefaultComboBoxModel();
            liblets = uiProperties.LIBLETS_MODEL;
        }

        LibletInfo li = null;
        List<ClassPathSupport.Item> allLibs = ClassPathUiSupport.getList(uiProperties.JAVAC_CLASSPATH_MODEL);
        for (ClassPathSupport.Item item : allLibs) {
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) { //JAR file added as lib
                boolean liblet = J2MEProjectUtils.isLibraryLiblet(item.getResolvedFile());
                if (liblet) {
                    li = LibletInfo.createLibletInfoForJar(item);
                }
            } else if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) { // Project added as lib
                AntArtifact artifact = item.getArtifact();
                if (artifact != null) {
                    Project project = artifact.getProject();
                    boolean liblet = J2MEProjectUtils.isLibraryLiblet(project);
                    if (liblet) {
                        li = LibletInfo.createLibletInfoForProject(item);
                    }
                }
            }

            if (li != null) {
                if (liblets.getIndexOf(li) == -1) {
                    liblets.addElement(li);
                } else if (liblets.getIndexOf(li) != -1) {
                    LibletInfo libletInfo = liblets.getElementAt(liblets.getIndexOf(li));
                    if (libletInfo.getItem() == null) {
                        libletInfo.setItem(li.getItem());
                    }
                }
            }
        }

        //remove liblets that have been removed from compile classpath
        List<LibletInfo> removed = new ArrayList<>();
        for (int i = 0; i < liblets.getSize(); i++) {
            LibletInfo lInfo = (LibletInfo) liblets.getElementAt(i);
            if (!allLibs.contains(lInfo.getItem()) && lInfo.getType() == LibletInfo.LibletType.LIBLET) {
                removed.add(lInfo);
            }
        }
        for (LibletInfo lInfo : removed) {
            liblets.removeElement(lInfo);
        }
        
        //show or hide customizer for liblets
        if (liblets.getSize() == 0) {
            ((CardLayout)jPanelLibletsCards.getLayout()).show(jPanelLibletsCards, "panelNoLiblets");
        } else {
            ((CardLayout)jPanelLibletsCards.getLayout()).show(jPanelLibletsCards, "panelLibletSettings");
        }
    }

    private void showSubCategory (String name) {
        if (name.equals(COMPILE)) {
            jTabbedPane1.setSelectedIndex (0);
        }        
        if (name.equals(PROCESSOR)) {
            jTabbedPane1.setSelectedIndex (1);
        }
        else if (name.equals(COMPILE_TESTS)) {
            jTabbedPane1.setSelectedIndex (3);
        }
        else if (name.equals(RUN)) {
            jTabbedPane1.setSelectedIndex (2);
        }
        else if (name.equals(RUN_TESTS)) {
            jTabbedPane1.setSelectedIndex (4);
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

        buttonGroupLibletLevel = new javax.swing.ButtonGroup();
        buttonGroupLibletType = new javax.swing.ButtonGroup();
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
        jLabel1 = new javax.swing.JLabel();
        jPanelCompileProcessor = new javax.swing.JPanel();
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
        jPanelLiblets = new javax.swing.JPanel();
        labelLibletsNote = new javax.swing.JLabel();
        jPanelLibletsCards = new javax.swing.JPanel();
        jPanelLibletSettings = new javax.swing.JPanel();
        jTextFieldLibletUrl = new javax.swing.JTextField();
        jRadioButtonRequired = new javax.swing.JRadioButton();
        jComboBoxLiblet = new javax.swing.JComboBox();
        labelLibletVersion = new javax.swing.JLabel();
        labelLibletName = new javax.swing.JLabel();
        labelLibletLevel = new javax.swing.JLabel();
        jRadioButtonOptional = new javax.swing.JRadioButton();
        libletLabel = new javax.swing.JLabel();
        labelLibletUrl = new javax.swing.JLabel();
        labelLibletVendor = new javax.swing.JLabel();
        labelLibletInfo = new javax.swing.JLabel();
        buttonBrowseJad = new javax.swing.JButton();
        labelLibletLocalJad = new javax.swing.JLabel();
        labelLibletType = new javax.swing.JLabel();
        jRadioButtonLiblet = new javax.swing.JRadioButton();
        jRadioButtonStandard = new javax.swing.JRadioButton();
        jRadioButtonService = new javax.swing.JRadioButton();
        jRadioButtonProprietary = new javax.swing.JRadioButton();
        buttonRemoveLiblet = new javax.swing.JButton();
        buttonAddLiblet = new javax.swing.JButton();
        jTextFieldLibletVersionValue = new javax.swing.JTextField();
        jTextFieldLibletNameValue = new javax.swing.JTextField();
        jTextFieldLibletVendorValue = new javax.swing.JTextField();
        labelLibletUsage = new javax.swing.JLabel();
        checkBoxLibletUsage = new javax.swing.JCheckBox();
        jPanelNoLiblets = new javax.swing.JPanel();
        labelNoLiblets = new javax.swing.JLabel();
        bAddFirstLiblet = new javax.swing.JButton();
        jCheckBoxBuildSubprojects = new javax.swing.JCheckBox();
        sharedLibrariesLabel = new javax.swing.JLabel();
        librariesLocation = new javax.swing.JTextField();
        librariesBrowse = new javax.swing.JButton();

        jPanelCompile.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelCompile.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelCompile.setLayout(new java.awt.GridBagLayout());

        librariesJLabel1.setLabelFor(jListCpC);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel1, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_LibrariesC_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompile.add(librariesJLabel1, gridBagConstraints);

        jListCpC.setVisibleRowCount(5);
        librariesJScrollPane.setViewportView(jListCpC);
        jListCpC.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "AN_CustomizerLibraries_jListClasspathC")); // NOI18N
        jListCpC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jLabelClasspathC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompile.add(librariesJScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactC, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonAddArtifactC, gridBagConstraints);
        jButtonAddArtifactC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryC, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonAddLibraryC, gridBagConstraints);
        jButtonAddLibraryC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonAddJarC, gridBagConstraints);
        jButtonAddJarC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditC, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonEditC, gridBagConstraints);
        jButtonEditC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveC, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonRemoveC, gridBagConstraints);
        jButtonRemoveC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonMoveUpC, gridBagConstraints);
        jButtonMoveUpC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonMoveDownC, gridBagConstraints);
        jButtonMoveDownC.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jLabel1.setLabelFor(jListCpC);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "MSG_CustomizerLibraries_CompileCpMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanelCompile.add(jLabel1, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_LibrariesTab"), jPanelCompile); // NOI18N

        jPanelCompileProcessor.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelCompileProcessor.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelCompileProcessor.setLayout(new java.awt.GridBagLayout());

        librariesJLabel5.setLabelFor(jListCpP);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel5, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_LibrariesP_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileProcessor.add(librariesJLabel5, gridBagConstraints);

        jListCpP.setVisibleRowCount(5);
        librariesJScrollPane4.setViewportView(jListCpP);
        jListCpP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "L_ACSN_ProcesserLibraries")); // NOI18N
        jListCpP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "L_ACSD_ProcesserLibraries")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileProcessor.add(librariesJScrollPane4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactP, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileProcessor.add(jButtonAddArtifactP, gridBagConstraints);
        jButtonAddArtifactP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSN_Add_Project")); // NOI18N
        jButtonAddArtifactP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSD_Add_Project")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryP, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileProcessor.add(jButtonAddLibraryP, gridBagConstraints);
        jButtonAddLibraryP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSN_Add_Library")); // NOI18N
        jButtonAddLibraryP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSD_Add_Library")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarP, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileProcessor.add(jButtonAddJarP, gridBagConstraints);
        jButtonAddJarP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSN_Add_Jar")); // NOI18N
        jButtonAddJarP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSD_Add_Jar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditP, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileProcessor.add(jButtonEditP, gridBagConstraints);
        jButtonEditP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSN_Edit")); // NOI18N
        jButtonEditP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSD_Edit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveP, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileProcessor.add(jButtonRemoveP, gridBagConstraints);
        jButtonRemoveP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSN_Remove")); // NOI18N
        jButtonRemoveP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSD_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpP, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileProcessor.add(jButtonMoveUpP, gridBagConstraints);
        jButtonMoveUpP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSN_MoveUp")); // NOI18N
        jButtonMoveUpP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSD_MoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownP, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileProcessor.add(jButtonMoveDownP, gridBagConstraints);
        jButtonMoveDownP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSN_MoveDown")); // NOI18N
        jButtonMoveDownP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "BTN_ACSD_MoveDown")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Processors_Tab"), jPanelCompileProcessor); // NOI18N

        jPanelRun.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelRun.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelRun.setLayout(new java.awt.GridBagLayout());

        librariesJLabel3.setLabelFor(jListCpR);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel3, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_LibrariesR_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRun.add(librariesJLabel3, gridBagConstraints);

        jListCpR.setVisibleRowCount(5);
        librariesJScrollPane2.setViewportView(jListCpR);
        jListCpR.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "AN_CustomizerLibraries_jListClasspathR")); // NOI18N
        jListCpR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jLabelClasspathR")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRun.add(librariesJScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactR, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonAddArtifactR, gridBagConstraints);
        jButtonAddArtifactR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryR, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonAddLibraryR, gridBagConstraints);
        jButtonAddLibraryR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarR, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonAddJarR, gridBagConstraints);
        jButtonAddJarR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditR, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonEditR, gridBagConstraints);
        jButtonEditR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveR, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonRemoveR, gridBagConstraints);
        jButtonRemoveR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpR, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRun.add(jButtonMoveUpR, gridBagConstraints);
        jButtonMoveUpR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownR, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRun.add(jButtonMoveDownR, gridBagConstraints);
        jButtonMoveDownR.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Run_Tab"), jPanelRun); // NOI18N

        jPanelCompileTests.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelCompileTests.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelCompileTests.setLayout(new java.awt.GridBagLayout());

        librariesJLabel2.setLabelFor(jListCpCT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel2, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_LibrariesCT_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileTests.add(librariesJLabel2, gridBagConstraints);

        jListCpCT.setVisibleRowCount(5);
        librariesJScrollPane1.setViewportView(jListCpCT);
        jListCpCT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "AN_CustomizerLibraries_jListClasspathCT")); // NOI18N
        jListCpCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jLabelClasspathCT")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileTests.add(librariesJScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactCT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonAddArtifactCT, gridBagConstraints);
        jButtonAddArtifactCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryCT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonAddLibraryCT, gridBagConstraints);
        jButtonAddLibraryCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarCT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonAddJarCT, gridBagConstraints);
        jButtonAddJarCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditCT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonEditCT, gridBagConstraints);
        jButtonEditCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveCT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonRemoveCT, gridBagConstraints);
        jButtonRemoveCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpCT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonMoveUpCT, gridBagConstraints);
        jButtonMoveUpCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownCT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonMoveDownCT, gridBagConstraints);
        jButtonMoveDownCT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_TestLibrariesTab"), jPanelCompileTests); // NOI18N

        jPanelRunTests.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        jPanelRunTests.setMinimumSize(new java.awt.Dimension(410, 250));
        jPanelRunTests.setLayout(new java.awt.GridBagLayout());

        librariesJLabel4.setLabelFor(jListCpRT);
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel4, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_LibrariesRT_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunTests.add(librariesJLabel4, gridBagConstraints);

        jListCpRT.setVisibleRowCount(5);
        librariesJScrollPane3.setViewportView(jListCpRT);
        jListCpRT.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "AN_CustomizerLibraries_jListClasspathRT")); // NOI18N
        jListCpRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jLabelClasspathRT")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRunTests.add(librariesJScrollPane3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactRT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonAddArtifactRT, gridBagConstraints);
        jButtonAddArtifactRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddArtifact")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryRT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddLibary_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonAddLibraryRT, gridBagConstraints);
        jButtonAddLibraryRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarRT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonAddJarRT, gridBagConstraints);
        jButtonAddJarRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonAddJar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditRT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Edit_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonEditRT, gridBagConstraints);
        jButtonEditRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_jButtonEdit")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveRT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonRemoveRT, gridBagConstraints);
        jButtonRemoveRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpRT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveUp_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonMoveUpRT, gridBagConstraints);
        jButtonMoveUpRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownRT, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_MoveDown_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonMoveDownRT, gridBagConstraints);
        jButtonMoveDownRT.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_jButtonMoveDown")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_RunTests_Tab"), jPanelRunTests); // NOI18N

        labelLibletsNote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/j2me/project/ui/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(labelLibletsNote, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("TXT_CustomizeLibraries_labelLibletsNote"), new Object[] {})); // NOI18N

        jPanelLibletsCards.setLayout(new java.awt.CardLayout());

        buttonGroupLibletLevel.add(jRadioButtonRequired);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonRequired, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_jRadioButtonRequired"), new Object[] {})); // NOI18N
        jRadioButtonRequired.setActionCommand("REQUIRED");

        jComboBoxLiblet.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxLiblet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxLibletActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletVersion, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletVersion"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletName, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletName"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletLevel, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletLevel"), new Object[] {})); // NOI18N

        buttonGroupLibletLevel.add(jRadioButtonOptional);
        jRadioButtonOptional.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonOptional, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_jRadioButtonOptional"), new Object[] {})); // NOI18N
        jRadioButtonOptional.setActionCommand("OPTIONAL");

        org.openide.awt.Mnemonics.setLocalizedText(libletLabel, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_libletLabel"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletUrl, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletUrl"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletVendor, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletVendor"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletInfo, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletInfo"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonBrowseJad, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_buttonBrowseJad"), new Object[] {})); // NOI18N
        buttonBrowseJad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBrowseJadActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletLocalJad, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletLocalJad"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletType, "Type:");

        buttonGroupLibletType.add(jRadioButtonLiblet);
        jRadioButtonLiblet.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonLiblet, "Liblet");
        jRadioButtonLiblet.setActionCommand("LIBLET");

        buttonGroupLibletType.add(jRadioButtonStandard);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonStandard, "Standard");
        jRadioButtonStandard.setActionCommand("STANDARD");

        buttonGroupLibletType.add(jRadioButtonService);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonService, "Service");
        jRadioButtonService.setActionCommand("SERVICE");

        buttonGroupLibletType.add(jRadioButtonProprietary);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonProprietary, "Proprietary");
        jRadioButtonProprietary.setActionCommand("PROPRIETARY");

        org.openide.awt.Mnemonics.setLocalizedText(buttonRemoveLiblet, "Remove...");
        buttonRemoveLiblet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveLibletActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonAddLiblet, "Add...");
        buttonAddLiblet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddLibletActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(labelLibletUsage, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelLibletUsage"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(checkBoxLibletUsage, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_checkboxLibletUsage")); // NOI18N
        checkBoxLibletUsage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxLibletUsageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelLibletSettingsLayout = new javax.swing.GroupLayout(jPanelLibletSettings);
        jPanelLibletSettings.setLayout(jPanelLibletSettingsLayout);
        jPanelLibletSettingsLayout.setHorizontalGroup(
            jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                        .addComponent(labelLibletInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                        .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelLibletUrl)
                                    .addComponent(libletLabel)
                                    .addComponent(labelLibletLevel)
                                    .addComponent(labelLibletType))
                                .addGap(18, 18, 18)
                                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                                        .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                                                .addComponent(jRadioButtonOptional)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButtonRequired))
                                            .addComponent(labelLibletLocalJad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLibletSettingsLayout.createSequentialGroup()
                                        .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jTextFieldLibletUrl)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelLibletSettingsLayout.createSequentialGroup()
                                                .addComponent(jRadioButtonLiblet)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButtonStandard)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButtonService)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButtonProprietary)
                                                .addGap(0, 9, Short.MAX_VALUE))
                                            .addComponent(jComboBoxLiblet, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                                                .addComponent(buttonAddLiblet, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonRemoveLiblet))
                                            .addComponent(buttonBrowseJad)))))
                            .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLibletSettingsLayout.createSequentialGroup()
                                                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(labelLibletName)
                                                    .addComponent(labelLibletVendor))
                                                .addGap(18, 18, 18))
                                            .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                                                .addComponent(labelLibletVersion)
                                                .addGap(17, 17, 17)))
                                        .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jTextFieldLibletNameValue, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                                            .addComponent(jTextFieldLibletVendorValue)
                                            .addComponent(jTextFieldLibletVersionValue)))
                                    .addComponent(labelLibletUsage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(checkBoxLibletUsage))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanelLibletSettingsLayout.setVerticalGroup(
            jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLibletSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(libletLabel)
                    .addComponent(jComboBoxLiblet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonRemoveLiblet)
                    .addComponent(buttonAddLiblet))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLibletLevel)
                    .addComponent(jRadioButtonOptional)
                    .addComponent(jRadioButtonRequired))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLibletType)
                    .addComponent(jRadioButtonLiblet)
                    .addComponent(jRadioButtonStandard)
                    .addComponent(jRadioButtonService)
                    .addComponent(jRadioButtonProprietary))
                .addGap(18, 18, 18)
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLibletUrl)
                    .addComponent(jTextFieldLibletUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonBrowseJad))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelLibletLocalJad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelLibletInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLibletName)
                    .addComponent(jTextFieldLibletNameValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLibletVendor)
                    .addComponent(jTextFieldLibletVendorValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLibletSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelLibletVersion)
                    .addComponent(jTextFieldLibletVersionValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelLibletUsage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkBoxLibletUsage)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jComboBoxLiblet.getAccessibleContext().setAccessibleDescription(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("ACSD_CustomizeLibraries_jComboBoxLiblet"), new Object[] {})); // NOI18N
        buttonBrowseJad.getAccessibleContext().setAccessibleDescription(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("ACSD_CustomizeLibraries_buttonBrowseJad"), new Object[] {})); // NOI18N

        jPanelLibletsCards.add(jPanelLibletSettings, "panelLibletSettings");

        org.openide.awt.Mnemonics.setLocalizedText(labelNoLiblets, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_labelNoLiblet"), new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bAddFirstLiblet, "Add...");
        bAddFirstLiblet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddLibletActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelNoLibletsLayout = new javax.swing.GroupLayout(jPanelNoLiblets);
        jPanelNoLiblets.setLayout(jPanelNoLibletsLayout);
        jPanelNoLibletsLayout.setHorizontalGroup(
            jPanelNoLibletsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNoLibletsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelNoLibletsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelNoLiblets)
                    .addComponent(bAddFirstLiblet))
                .addContainerGap())
        );
        jPanelNoLibletsLayout.setVerticalGroup(
            jPanelNoLibletsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelNoLibletsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelNoLiblets)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bAddFirstLiblet)
                .addContainerGap(286, Short.MAX_VALUE))
        );

        jPanelLibletsCards.add(jPanelNoLiblets, "panelNoLiblets");

        javax.swing.GroupLayout jPanelLibletsLayout = new javax.swing.GroupLayout(jPanelLiblets);
        jPanelLiblets.setLayout(jPanelLibletsLayout);
        jPanelLibletsLayout.setHorizontalGroup(
            jPanelLibletsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelLibletsCards, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanelLibletsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelLibletsNote, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelLibletsLayout.setVerticalGroup(
            jPanelLibletsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLibletsLayout.createSequentialGroup()
                .addComponent(jPanelLibletsCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelLibletsNote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("LBL_CustomizeLibraries_LibletsTab"), new Object[] {}), jPanelLiblets); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxBuildSubprojects, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeLibraries_Build_Subprojects")); // NOI18N

        sharedLibrariesLabel.setLabelFor(librariesLocation);
        org.openide.awt.Mnemonics.setLocalizedText(sharedLibrariesLabel, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizeGeneral_SharedLibraries")); // NOI18N

        librariesLocation.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(librariesBrowse, org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizerLibraries_Browse_JButton")); // NOI18N
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
                .addComponent(sharedLibrariesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jCheckBoxBuildSubprojects, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sharedLibrariesLabel)
                    .addComponent(librariesBrowse)
                    .addComponent(librariesLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxBuildSubprojects))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSN_CustomizerLibraries_JTabbedPane")); // NOI18N
        jTabbedPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_CustomizerLibraries_JTabbedPane")); // NOI18N
        jCheckBoxBuildSubprojects.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "AD_CheckBoxBuildSubprojects")); // NOI18N
        librariesLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_librariesLocation")); // NOI18N
        librariesBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(J2MELibrariesPanel.class, "ACSD_librariesBrowse")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void librariesBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_librariesBrowseActionPerformed
        if (!isSharable) {
            if (uiProperties.makeSharable()) {
                isSharable = true;
                sharedLibrariesLabel.setEnabled(true);
                librariesLocation.setEnabled(true);
                librariesLocation.setText(uiProperties.getProject().getHelper().getLibrariesLocation());
                Mnemonics.setLocalizedText(librariesBrowse, NbBundle.getMessage(J2MELibrariesPanel.class, "LBL_CustomizerLibraries_Browse_JButton")); // NOI18N
                updateJars(uiProperties.JAVAC_CLASSPATH_MODEL);
                updateJars(uiProperties.JAVAC_PROCESSORPATH_MODEL);
                updateJars(uiProperties.JAVAC_TEST_CLASSPATH_MODEL);
                updateJars(uiProperties.RUN_CLASSPATH_MODEL);
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
                            NbBundle.getMessage(J2MELibrariesPanel.class, "ERR_InvalidProjectLibrariesFolder"),
                            NbBundle.getMessage(J2MELibrariesPanel.class, "TITLE_InvalidProjectLibrariesFolder"),
                            NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object[] {NotifyDescriptor.OK_OPTION},
                            NotifyDescriptor.OK_OPTION));
                }
            }
        }
}//GEN-LAST:event_librariesBrowseActionPerformed

    private void buttonAddLibletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddLibletActionPerformed
        int origSize = uiProperties.LIBLETS_MODEL.getSize();
        uiProperties.LIBLETS_MODEL.addElement(new LibletInfo(LibletInfo.LibletType.STANDARD, "liblet-name", "liblet-vendor", "1.0", LibletInfo.Requirement.OPTIONAL, null, false)); //NOI18N
        uiProperties.LIBLETS_MODEL.setSelectedItem(uiProperties.LIBLETS_MODEL.getElementAt(uiProperties.LIBLETS_MODEL.getSize() - 1));
        if (origSize == 0 && uiProperties.LIBLETS_MODEL.getSize() != 0) {
            ((CardLayout)jPanelLibletsCards.getLayout()).show(jPanelLibletsCards, "panelLibletSettings");
        }
    }//GEN-LAST:event_buttonAddLibletActionPerformed

    private void checkBoxLibletUsageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxLibletUsageActionPerformed
        LibletInfo selected = (LibletInfo) jComboBoxLiblet.getSelectedItem();
        if (selected != null) {
            selected.setExtractClasses(checkBoxLibletUsage.isSelected());
        }
    }//GEN-LAST:event_checkBoxLibletUsageActionPerformed

    private void buttonRemoveLibletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveLibletActionPerformed
        int origSize = uiProperties.LIBLETS_MODEL.getSize();
        uiProperties.LIBLETS_MODEL.removeElement(uiProperties.LIBLETS_MODEL.getSelectedItem());
        if (origSize != 0 && uiProperties.LIBLETS_MODEL.getSize() == 0) {
            ((CardLayout)jPanelLibletsCards.getLayout()).show(jPanelLibletsCards, "panelNoLiblets");
        }
    }//GEN-LAST:event_buttonRemoveLibletActionPerformed

    private void buttonBrowseJadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBrowseJadActionPerformed
        LibletInfo selected = (LibletInfo) jComboBoxLiblet.getSelectedItem();
        if (selected != null) {
            switch (selected.getItem().getType()) {
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                LibletInfo li = LibletInfo.createLibletInfoForProject(selected.getItem());
                jTextFieldLibletUrl.setText(li.getUrl());
                break;
                case ClassPathSupport.Item.TYPE_JAR:
                LibletInfo liJar = LibletInfo.createLibletInfoForJar(selected.getItem());
                jTextFieldLibletUrl.setText(liJar.getUrl());
                break;
            }
        }
    }//GEN-LAST:event_buttonBrowseJadActionPerformed

    private void jComboBoxLibletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxLibletActionPerformed
        updateLibletFormValues();
    }//GEN-LAST:event_jComboBoxLibletActionPerformed

    private void updateJars(DefaultListModel model) {
        for (int i = 0; i < model.size(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) model.get(i);
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                if (item.getReference() != null) {
                    item.updateJarReference(uiProperties.getProject().getHelper());
                }
            }
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAddFirstLiblet;
    private javax.swing.JButton buttonAddLiblet;
    private javax.swing.JButton buttonBrowseJad;
    private javax.swing.ButtonGroup buttonGroupLibletLevel;
    private javax.swing.ButtonGroup buttonGroupLibletType;
    private javax.swing.JButton buttonRemoveLiblet;
    private javax.swing.JCheckBox checkBoxLibletUsage;
    private javax.swing.JButton jButtonAddArtifactC;
    private javax.swing.JButton jButtonAddArtifactCT;
    private javax.swing.JButton jButtonAddArtifactP;
    private javax.swing.JButton jButtonAddArtifactR;
    private javax.swing.JButton jButtonAddArtifactRT;
    private javax.swing.JButton jButtonAddJarC;
    private javax.swing.JButton jButtonAddJarCT;
    private javax.swing.JButton jButtonAddJarP;
    private javax.swing.JButton jButtonAddJarR;
    private javax.swing.JButton jButtonAddJarRT;
    private javax.swing.JButton jButtonAddLibraryC;
    private javax.swing.JButton jButtonAddLibraryCT;
    private javax.swing.JButton jButtonAddLibraryP;
    private javax.swing.JButton jButtonAddLibraryR;
    private javax.swing.JButton jButtonAddLibraryRT;
    private javax.swing.JButton jButtonEditC;
    private javax.swing.JButton jButtonEditCT;
    private javax.swing.JButton jButtonEditP;
    private javax.swing.JButton jButtonEditR;
    private javax.swing.JButton jButtonEditRT;
    private javax.swing.JButton jButtonMoveDownC;
    private javax.swing.JButton jButtonMoveDownCT;
    private javax.swing.JButton jButtonMoveDownP;
    private javax.swing.JButton jButtonMoveDownR;
    private javax.swing.JButton jButtonMoveDownRT;
    private javax.swing.JButton jButtonMoveUpC;
    private javax.swing.JButton jButtonMoveUpCT;
    private javax.swing.JButton jButtonMoveUpP;
    private javax.swing.JButton jButtonMoveUpR;
    private javax.swing.JButton jButtonMoveUpRT;
    private javax.swing.JButton jButtonRemoveC;
    private javax.swing.JButton jButtonRemoveCT;
    private javax.swing.JButton jButtonRemoveP;
    private javax.swing.JButton jButtonRemoveR;
    private javax.swing.JButton jButtonRemoveRT;
    private javax.swing.JCheckBox jCheckBoxBuildSubprojects;
    private javax.swing.JComboBox jComboBoxLiblet;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jListCpC;
    private javax.swing.JList jListCpCT;
    private javax.swing.JList jListCpP;
    private javax.swing.JList jListCpR;
    private javax.swing.JList jListCpRT;
    private javax.swing.JPanel jPanelCompile;
    private javax.swing.JPanel jPanelCompileProcessor;
    private javax.swing.JPanel jPanelCompileTests;
    private javax.swing.JPanel jPanelLibletSettings;
    private javax.swing.JPanel jPanelLiblets;
    private javax.swing.JPanel jPanelLibletsCards;
    private javax.swing.JPanel jPanelNoLiblets;
    private javax.swing.JPanel jPanelRun;
    private javax.swing.JPanel jPanelRunTests;
    private javax.swing.JRadioButton jRadioButtonLiblet;
    private javax.swing.JRadioButton jRadioButtonOptional;
    private javax.swing.JRadioButton jRadioButtonProprietary;
    private javax.swing.JRadioButton jRadioButtonRequired;
    private javax.swing.JRadioButton jRadioButtonService;
    private javax.swing.JRadioButton jRadioButtonStandard;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldLibletNameValue;
    private javax.swing.JTextField jTextFieldLibletUrl;
    private javax.swing.JTextField jTextFieldLibletVendorValue;
    private javax.swing.JTextField jTextFieldLibletVersionValue;
    private javax.swing.JLabel labelLibletInfo;
    private javax.swing.JLabel labelLibletLevel;
    private javax.swing.JLabel labelLibletLocalJad;
    private javax.swing.JLabel labelLibletName;
    private javax.swing.JLabel labelLibletType;
    private javax.swing.JLabel labelLibletUrl;
    private javax.swing.JLabel labelLibletUsage;
    private javax.swing.JLabel labelLibletVendor;
    private javax.swing.JLabel labelLibletVersion;
    private javax.swing.JLabel labelLibletsNote;
    private javax.swing.JLabel labelNoLiblets;
    private javax.swing.JLabel libletLabel;
    private javax.swing.JButton librariesBrowse;
    private javax.swing.JLabel librariesJLabel1;
    private javax.swing.JLabel librariesJLabel2;
    private javax.swing.JLabel librariesJLabel3;
    private javax.swing.JLabel librariesJLabel4;
    private javax.swing.JLabel librariesJLabel5;
    private javax.swing.JScrollPane librariesJScrollPane;
    private javax.swing.JScrollPane librariesJScrollPane1;
    private javax.swing.JScrollPane librariesJScrollPane2;
    private javax.swing.JScrollPane librariesJScrollPane3;
    private javax.swing.JScrollPane librariesJScrollPane4;
    private javax.swing.JTextField librariesLocation;
    private javax.swing.JLabel sharedLibrariesLabel;
    // End of variables declaration//GEN-END:variables

    private void updateLibletFormValues() {
        LibletInfo selected = (LibletInfo) jComboBoxLiblet.getSelectedItem();
        if (selected != null) {
            jRadioButtonOptional.setSelected(jRadioButtonOptional.getActionCommand().equals(selected.getRequirement().name()));
            jRadioButtonRequired.setSelected(jRadioButtonRequired.getActionCommand().equals(selected.getRequirement().name()));
            if (selected.getType() == LibletInfo.LibletType.LIBLET) {
                jTextFieldLibletUrl.setText(selected.getUrl());
            } else {
                jTextFieldLibletUrl.setText(null);
            }
            labelLibletUrl.setVisible(selected.getType() == LibletInfo.LibletType.LIBLET);
            jTextFieldLibletUrl.setVisible(selected.getType() == LibletInfo.LibletType.LIBLET);
            buttonBrowseJad.setVisible(selected.getType() == LibletInfo.LibletType.LIBLET);
            labelLibletLocalJad.setVisible(selected.getType() == LibletInfo.LibletType.LIBLET);

            jRadioButtonLiblet.setSelected(jRadioButtonLiblet.getActionCommand().equals(selected.getType().name()));
            jRadioButtonStandard.setSelected(jRadioButtonStandard.getActionCommand().equals(selected.getType().name()));
            jRadioButtonService.setSelected(jRadioButtonService.getActionCommand().equals(selected.getType().name()));
            jRadioButtonProprietary.setSelected(jRadioButtonProprietary.getActionCommand().equals(selected.getType().name()));
            jRadioButtonLiblet.setEnabled(selected.getType() == LibletInfo.LibletType.LIBLET);
            jRadioButtonStandard.setEnabled(selected.getType() != LibletInfo.LibletType.LIBLET);
            jRadioButtonService.setEnabled(selected.getType() != LibletInfo.LibletType.LIBLET);
            jRadioButtonProprietary.setEnabled(selected.getType() != LibletInfo.LibletType.LIBLET);

            buttonRemoveLiblet.setEnabled(selected.getType() != LibletInfo.LibletType.LIBLET);

            jTextFieldLibletNameValue.setText(selected.getName());
            jTextFieldLibletVendorValue.setText(selected.getType() != LibletInfo.LibletType.SERVICE ? selected.getVendor() : "");
            jTextFieldLibletVersionValue.setText(selected.getType() != LibletInfo.LibletType.SERVICE ? selected.getVersion() : "");

            jTextFieldLibletNameValue.setEnabled(selected.getType() != LibletInfo.LibletType.LIBLET);
            jTextFieldLibletVendorValue.setEnabled(selected.getType() != LibletInfo.LibletType.LIBLET);
            labelLibletVendor.setVisible(selected.getType() != LibletInfo.LibletType.SERVICE);
            jTextFieldLibletVendorValue.setVisible(selected.getType() != LibletInfo.LibletType.SERVICE);
            labelLibletVersion.setVisible(selected.getType() != LibletInfo.LibletType.SERVICE);
            jTextFieldLibletVersionValue.setVisible(selected.getType() != LibletInfo.LibletType.SERVICE);

            checkBoxLibletUsage.setSelected(selected.isExtractClasses());
            labelLibletUsage.setVisible(selected.getType() == LibletInfo.LibletType.LIBLET);
            checkBoxLibletUsage.setVisible(selected.getType() == LibletInfo.LibletType.LIBLET);
        }
    }
}
