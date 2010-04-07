/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.ExtenderController.Properties;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Petr Pisl, Radko Najman, alexeybutenko
 */
public class JSFConfigurationPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener  {

    private static final Logger LOG = Logger.getLogger(JSFConfigurationPanelVisual.class.getName());

    private static final String JSF_SERVLET_NAME="Faces Servlet";   //NOI18N
    private String jsfServletName=null;
    private JSFConfigurationPanel panel;
    private boolean customizer;
    
    private final List<LibraryItem> jsfLibraries = new ArrayList<LibraryItem>();
    private boolean libsInitialized;
    private String serverInstanceID;
    private final List<String> preferredLanguages = new ArrayList<String>();
    private String currentServerInstanceID;
    private final List<String> excludeLibs = Arrays.asList("javaee-web-api-6.0", "javaee-api-6.0"); //NOI18N
    
    /** Creates new form JSFConfigurationPanelVisual */
    public JSFConfigurationPanelVisual(JSFConfigurationPanel panel, boolean customizer) {
        this.panel = panel;
        this.customizer = customizer;

        initComponents();
        
        tURLPattern.getDocument().addDocumentListener(this);
        cbPackageJars.setVisible(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initLibraries();
        
        if (customizer) {
            enableComponents(false);
        } else {
            updateLibrary();
        }
    }

    void initLibraries() {
        long time = System.currentTimeMillis();
        if (libsInitialized) {
            return;
        }

        final Vector <String> items = new Vector <String>();
        jsfLibraries.clear();
        final Runnable libraryFinder = new Runnable() {

            @Override
            public void run() {
                synchronized (this) {
                    long time = System.currentTimeMillis();
                    List<URL> content;
                    for (Library library : LibraryManager.getDefault().getLibraries()) {
                        if (!"j2se".equals(library.getType())) { // NOI18N
                            continue;
                        }
                        if (library.getName().startsWith("facelets-") && !library.getName().endsWith("el-api")    //NOI18N
                        && !library.getName().endsWith("jsf-ri") && !library.getName().endsWith("myfaces")){                                            //NOI18N
                            String displayName = library.getDisplayName();
                            items.add(displayName);
                            //TODO XX Add correct version
                            jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_2));
                        }

                        content = library.getContent("classpath"); //NOI18N
                        try {
                            if (Util.containsClass(content, JSFUtils.FACES_EXCEPTION) && !excludeLibs.contains(library.getName())) {
                                items.add(library.getDisplayName());
                                boolean isJSF12 = Util.containsClass(content, JSFUtils.JSF_1_2__API_SPECIFIC_CLASS);
                                boolean isJSF20 = Util.containsClass(content, JSFUtils.JSF_2_0__API_SPECIFIC_CLASS);
                                if (isJSF12 && !isJSF20) {
                                    jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_2));
                                } else if (isJSF20){
                                    jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_2_0));
                                } else {
                                    jsfLibraries.add(new LibraryItem(library, JSFVersion.JSF_1_1));
                                }
                            }
                        } catch (IOException exception) {
                            Exceptions.printStackTrace(exception);
                        }
                    }
                    setLibraryModel(items);
                    updatePreferredLanguages();
                    LOG.finest("Time spent in initLibraries in Runnable = "+(System.currentTimeMillis()-time) +" ms");  //NOI18N
                }
            }
        };
        RequestProcessor.getDefault().post(libraryFinder);

        libsInitialized = true;
//        repaint();
        LOG.finest("Time spent in "+this.getClass().getName() +" initLibraries = "+(System.currentTimeMillis()-time) +" ms");   //NOI18N
    }

    private void setLibraryModel(Vector<String> items) {
        long time = System.currentTimeMillis();
        cbLibraries.setModel(new DefaultComboBoxModel(items));
        if (items.size() == 0) {
            rbRegisteredLibrary.setEnabled(false);
            cbLibraries.setEnabled(false);
            rbNewLibrary.setSelected(true);
            panel.setLibrary(null);
        } else if (items.size() != 0 &&  panel.getLibraryType() == JSFConfigurationPanel.LibraryType.USED){
            rbRegisteredLibrary.setEnabled(true);
            rbRegisteredLibrary.setSelected(true);
            cbLibraries.setEnabled(true);
            if (jsfLibraries.size() > 0){
                panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
            }
        }

//        libsInitialized = true;
        repaint();
        LOG.finest("Time spent in "+this.getClass().getName() +" setLibraryModel = "+(System.currentTimeMillis()-time) +" ms");   //NOI18N
    }
    
    /**
     * Init Preferred Languages check box with "JSP" and/or "Facelets"
     * according to choosen library
     */
    private void updatePreferredLanguages() {
        boolean faceletsPresent = false;
        Library jsfLibrary = null;
        if (panel.getLibraryType()==null)
            return;
        if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.USED) {
            if (!libsInitialized)
                initLibraries();
            jsfLibrary = panel.getLibrary();
        } else if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.NEW) {
            if (panel.getNewLibraryName()!=null) {
                jsfLibrary = LibraryManager.getDefault().getLibrary(panel.getNewLibraryName());
            }
        } else if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.NONE) {
            //XXX: need to find lib version
            if (rbNoneLibrary.getText().indexOf("2.0")!=-1) {
                faceletsPresent = true;
            }
        }
        if (jsfLibrary != null) {
            List<URL> content = jsfLibrary.getContent("classpath"); //NOI18N
            try {
                faceletsPresent = Util.containsClass(content, "com.sun.facelets.Facelet") ||        //NOI18N
                                  Util.containsClass(content, JSFUtils.MYFACES_SPECIFIC_CLASS) ||   //NOI18N
                                  Util.containsClass(content, "com.sun.faces.facelets.Facelet");    //NOI18N
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        preferredLanguages.clear();
        preferredLanguages.add(JSFConfigurationPanel.PreferredLanguage.JSP.getName()); 
        if (faceletsPresent) {
            if (!customizer)
                panel.setEnableFacelets(true);

            if (panel.isEnableFacelets())
                preferredLanguages.add(0,JSFConfigurationPanel.PreferredLanguage.Facelets.getName());
            else 
                preferredLanguages.add(JSFConfigurationPanel.PreferredLanguage.Facelets.getName());
        } else {
            panel.setEnableFacelets(false);
        }
        cbPreferredLang.setModel(new DefaultComboBoxModel(preferredLanguages.toArray()));
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jsfTabbedPane = new javax.swing.JTabbedPane();
        libPanel = new javax.swing.JPanel();
        rbNoneLibrary = new javax.swing.JRadioButton();
        rbRegisteredLibrary = new javax.swing.JRadioButton();
        cbLibraries = new javax.swing.JComboBox();
        rbNewLibrary = new javax.swing.JRadioButton();
        lDirectory = new javax.swing.JLabel();
        jtFolder = new javax.swing.JTextField();
        jbBrowse = new javax.swing.JButton();
        lVersion = new javax.swing.JLabel();
        jtNewLibraryName = new javax.swing.JTextField();
        confPanel = new javax.swing.JPanel();
        lURLPattern = new javax.swing.JLabel();
        tURLPattern = new javax.swing.JTextField();
        cbPackageJars = new javax.swing.JCheckBox();
        cbPreferredLang = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.CardLayout());

        jsfTabbedPane.setMinimumSize(new java.awt.Dimension(106, 62));
        jsfTabbedPane.setPreferredSize(new java.awt.Dimension(483, 210));

        libPanel.setAlignmentX(0.2F);
        libPanel.setAlignmentY(0.2F);

        buttonGroup1.add(rbNoneLibrary);
        rbNoneLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbNoAppend").charAt(0));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        rbNoneLibrary.setText(bundle.getString("LBL_Any_Library")); // NOI18N
        rbNoneLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNoneLibraryItemStateChanged(evt);
            }
        });

        buttonGroup1.add(rbRegisteredLibrary);
        rbRegisteredLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbRegLibs").charAt(0));
        rbRegisteredLibrary.setSelected(true);
        rbRegisteredLibrary.setText(bundle.getString("LBL_REGISTERED_LIBRARIES")); // NOI18N
        rbRegisteredLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbRegisteredLibraryItemStateChanged(evt);
            }
        });

        cbLibraries.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Search Libraries..." }));
        cbLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLibrariesActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbNewLibrary);
        rbNewLibrary.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_rbCrNewLib").charAt(0));
        rbNewLibrary.setText(bundle.getString("LBL_CREATE_NEW_LIBRARY")); // NOI18N
        rbNewLibrary.setToolTipText(bundle.getString("MSG_CreatingLibraries")); // NOI18N
        rbNewLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNewLibraryItemStateChanged(evt);
            }
        });

        lDirectory.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lJSFDir").charAt(0));
        lDirectory.setLabelFor(jtFolder);
        lDirectory.setText(bundle.getString("LBL_INSTALL_DIR")); // NOI18N
        lDirectory.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N

        jtFolder.setToolTipText(bundle.getString("HINT_JSF_Directory")); // NOI18N
        jtFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtFolderKeyPressed(evt);
            }
        });

        jbBrowse.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Browse").charAt(0));
        jbBrowse.setText(bundle.getString("LBL_Browse")); // NOI18N
        jbBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBrowseActionPerformed(evt);
            }
        });

        lVersion.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lJSFVer").charAt(0));
        lVersion.setLabelFor(jtNewLibraryName);
        lVersion.setText(bundle.getString("LBL_VERSION")); // NOI18N
        lVersion.setToolTipText(bundle.getString("HINT_Version")); // NOI18N

        jtNewLibraryName.setToolTipText(bundle.getString("HINT_Version")); // NOI18N
        jtNewLibraryName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtNewLibraryNameKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout libPanelLayout = new org.jdesktop.layout.GroupLayout(libPanel);
        libPanel.setLayout(libPanelLayout);
        libPanelLayout.setHorizontalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rbNoneLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .add(libPanelLayout.createSequentialGroup()
                        .add(rbRegisteredLibrary)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbLibraries, 0, 293, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, libPanelLayout.createSequentialGroup()
                                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jtNewLibraryName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                                    .add(jtFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jbBrowse))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, rbNewLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .add(libPanelLayout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                            .add(lDirectory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE))))
                .addContainerGap())
        );
        libPanelLayout.setVerticalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(rbNoneLibrary)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbRegisteredLibrary)
                    .add(cbLibraries, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbNewLibrary)
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jbBrowse)
                    .add(jtFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lDirectory))
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jtNewLibraryName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lVersion))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Libraries"), libPanel); // NOI18N

        lURLPattern.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lURLPattern").charAt(0));
        lURLPattern.setLabelFor(tURLPattern);
        lURLPattern.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_URL_Pattern")); // NOI18N

        tURLPattern.setText(panel.getFacesMapping());

        cbPackageJars.setSelected(true);
        cbPackageJars.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Package_JARs")); // NOI18N

        cbPreferredLang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPreferredLang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbPreferredLangActionPerformed(evt);
            }
        });

        jLabel1.setLabelFor(cbPreferredLang);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_PREFERRED_LANGUAGE")); // NOI18N

        org.jdesktop.layout.GroupLayout confPanelLayout = new org.jdesktop.layout.GroupLayout(confPanel);
        confPanel.setLayout(confPanelLayout);
        confPanelLayout.setHorizontalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(confPanelLayout.createSequentialGroup()
                        .add(lURLPattern)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tURLPattern, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE))
                    .add(confPanelLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbPreferredLang, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(cbPackageJars))
                .addContainerGap())
        );
        confPanelLayout.setVerticalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lURLPattern)
                    .add(tURLPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbPackageJars)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cbPreferredLang, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(98, 98, 98))
        );

        tURLPattern.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_Mapping")); // NOI18N
        cbPackageJars.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "ACSD_PackageJarToWar")); // NOI18N

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Configuration"), confPanel); // NOI18N

        add(jsfTabbedPane, "card10");
        jsfTabbedPane.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

private void rbNoneLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNoneLibraryItemStateChanged
    updateLibrary();
    if (rbNoneLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbNoneLibraryItemStateChanged

private void jtNewLibraryNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtNewLibraryNameKeyReleased
    panel.setNewLibraryName(jtNewLibraryName.getText().trim());
}//GEN-LAST:event_jtNewLibraryNameKeyReleased

private void rbNewLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNewLibraryItemStateChanged
    updateLibrary();
    if (rbNewLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbNewLibraryItemStateChanged

private void cbLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLibrariesActionPerformed
    panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
    updatePreferredLanguages();
}//GEN-LAST:event_cbLibrariesActionPerformed

private void rbRegisteredLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbRegisteredLibraryItemStateChanged
    updateLibrary();
    if (rbRegisteredLibrary.isSelected()) {
        panel.fireChangeEvent();
    }
}//GEN-LAST:event_rbRegisteredLibraryItemStateChanged

private void jbBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBrowseActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(NbBundle.getMessage(JSFConfigurationPanelVisual.class,"LBL_SelectLibraryLocation")); //NOI18N
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setCurrentDirectory(new File(jtFolder.getText().trim()));
    
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File projectDir = chooser.getSelectedFile();
        jtFolder.setText(projectDir.getAbsolutePath());
        setNewLibraryFolder();
    }
}//GEN-LAST:event_jbBrowseActionPerformed

private void jtFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtFolderKeyPressed
    setNewLibraryFolder();
}//GEN-LAST:event_jtFolderKeyPressed

private void cbPreferredLangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPreferredLangActionPerformed
    if (preferredLanguages.get(cbPreferredLang.getSelectedIndex()).equals("Facelets")) { //NOI18N
        panel.setEnableFacelets(true);
    } else
        panel.setEnableFacelets(false);
}//GEN-LAST:event_cbPreferredLangActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbLibraries;
    private javax.swing.JCheckBox cbPackageJars;
    private javax.swing.JComboBox cbPreferredLang;
    private javax.swing.JPanel confPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jbBrowse;
    private javax.swing.JTabbedPane jsfTabbedPane;
    private javax.swing.JTextField jtFolder;
    private javax.swing.JTextField jtNewLibraryName;
    private javax.swing.JLabel lDirectory;
    private javax.swing.JLabel lURLPattern;
    private javax.swing.JLabel lVersion;
    private javax.swing.JPanel libPanel;
    private javax.swing.JRadioButton rbNewLibrary;
    private javax.swing.JRadioButton rbNoneLibrary;
    private javax.swing.JRadioButton rbRegisteredLibrary;
    private javax.swing.JTextField tURLPattern;
    // End of variables declaration//GEN-END:variables
 
    void enableComponents(boolean enable) {
        Component[] components;
        
        components = confPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }
        
        cbPreferredLang.setEnabled(true);
        jLabel1.setEnabled(true);
        
        components = libPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }

    }
    
    boolean valid() {
        ExtenderController controller = panel.getController();
        String urlPattern = tURLPattern.getText();
        if (urlPattern == null || urlPattern.trim().equals("")) { // NOI18N
            controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsEmpty"));
            return false;
        }
        if (!isPatternValid(urlPattern)) {
            controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsNotValid"));
            return false;
        }
        
        if (customizer) {
            return true;
        }

        if (rbRegisteredLibrary.isSelected()) {
            if (jsfLibraries == null || jsfLibraries.size() == 0) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_MissingJSF")); //NOI18N
                return false;
            }
        }
        
        if (rbNewLibrary.isSelected()) {
            // checking, whether the folder is the right one
            String folder = jtFolder.getText().trim();
            String message;
            
            // TODO: perhaps remove the version check at all:
            message = JSFUtils.isJSFInstallFolder(new File(folder), JSFVersion.JSF_2_0);
            if ("".equals(folder)) {
                Properties properties = controller.getProperties();
                controller.setErrorMessage(null);
                properties.setProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
                return false;
            }
            
            if (message != null) {
                controller.setErrorMessage(message);
                return false;
            }
            // checking new library name
            String newLibraryName = jtNewLibraryName.getText().trim();
            if (newLibraryName.length() <= 0) {
                controller.setErrorMessage(null);
                controller.getProperties().setProperty(WizardDescriptor.PROP_INFO_MESSAGE, NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_EmptyNewLibraryName"));
//                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_EmptyNewLibraryName")); //NOI18N
                return false;
            }
            
            message = checkLibraryName(newLibraryName);
            if (message != null) {
                controller.setErrorMessage(message); 
                return false;
            }
            Library lib = LibraryManager.getDefault().getLibrary(newLibraryName);
            if (lib != null) {
                controller.setErrorMessage(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_AlreadyExists")); //NOI18N
                return false;
            }
        }
                
        controller.setErrorMessage(null);
        return true;
    }
    
    private static final char[] INVALID_PATTERN_CHARS = {'%', '+'}; // NOI18N

    private boolean isPatternValid(String pattern) {
        for (char c : INVALID_PATTERN_CHARS) {
            if (pattern.indexOf(c) != -1) {
                return false;
            }
        }
        
        if (pattern.startsWith("*.")){
            String p = pattern.substring(2);
            if (p.indexOf('.') == -1 && p.indexOf('*') == -1
                    && p.indexOf('/') == -1 && !p.trim().equals(""))
                return true;
        }
        // pattern = "/.../*", where ... can't be empty.
        if ((pattern.length() > 3) && pattern.endsWith("/*") && pattern.startsWith("/"))
            return true;
        return false;
    }
    private boolean isWebLogic(String serverInstanceID) {
        if (serverInstanceID == null || "".equals(serverInstanceID)) {
            return false;
        }
        String shortName;
        try {
            shortName = Deployment.getDefault().getServerInstance(serverInstanceID).getServerID();
            if (shortName != null && shortName.toLowerCase().startsWith("weblogic")) {  //NOI18N
                return true;
            }
        } catch (InstanceRemovedException ex) {
            LOG.log(Level.WARNING, "Server Instance was removed", ex); //NOI18N
        }
        return false;
    }
    
    void update() {
        Properties properties = panel.getController().getProperties();
        String j2eeLevel = (String)properties.getProperty("j2eeLevel"); // NOI18N
        Profile prof = j2eeLevel == null ? Profile.JAVA_EE_6_FULL : Profile.fromPropertiesString(j2eeLevel);
        serverInstanceID = (String)properties.getProperty("serverInstanceID"); //NOI18N
        initLibSettings(prof, serverInstanceID);
    }
    
    /**  Method looks at the project classpath and is looking for javax.faces.FacesException.
     *   If there is not this class on the classpath, then is offered appropriate jsf library
     *   according web module version.
     */
    private void initLibSettings(Profile profile, String serverInstanceID) {
        if (panel==null || panel.getLibraryType() == null || isServerInstanceChanged()) {
            try {
                File[] cp;
                J2eePlatform platform = null;
                try {
                    if (serverInstanceID != null)
                        platform = Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
                } catch (InstanceRemovedException ex) {
                    platform = null;
                    LOG.log(Level.INFO, org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "SERVER_INSTANCE_REMOVED"), ex);
                }
                // j2eeplatform can be null, when the target server is not accessible.
                if (platform != null) {
                    cp = platform.getClasspathEntries();
                } else {
                    cp = new File[0];
                }

                // XXX: there should be a utility class for this:
                boolean isJSF = Util.containsClass(Arrays.asList(cp), JSFUtils.FACES_EXCEPTION);
                boolean isJSF12 = Util.containsClass(Arrays.asList(cp), JSFUtils.JSF_1_2__API_SPECIFIC_CLASS);
                boolean isJSF20 = Util.containsClass(Arrays.asList(cp), JSFUtils.JSF_2_0__API_SPECIFIC_CLASS);

                //XXX: 182282: disable bundled lib in WebLogic.
                if (isWebLogic(serverInstanceID)) {
                    isJSF = false;
                    isJSF12 = false;
                    isJSF20 = false;
                }
                
                String libName = null; //NOI18N
                if (isJSF20) {
                    libName = "JSF 2.0"; //NOI18N
                } else if (isJSF12) {
                    libName = "JSF 1.2"; //NOI18N
                } else if (isJSF) {
                    libName = "JSF 1.1"; //NOI18N
                } else {
                    rbNoneLibrary.setVisible(false);
                    Library profferedLibrary = null;
                    if (profile.equals(Profile.JAVA_EE_6_FULL) || profile.equals(Profile.JAVA_EE_6_WEB)) {
                        profferedLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSF_2_0_NAME);
                    } else {
                        profferedLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSF_1_2_NAME);
                    }

                    if (profferedLibrary != null) {
                        // if there is a proffered library, select
                        rbRegisteredLibrary.setSelected(true);
                        cbLibraries.setSelectedItem(profferedLibrary.getDisplayName());
                        updateLibrary();
                    } else {
                        // there is not a proffered library -> select one or select creating new one
                        if (jsfLibraries.size() == 0) {
                            rbNewLibrary.setSelected(true);
                        }
                    }
                }
                if (libName != null) {
                    rbNoneLibrary.setText(NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_Any_Library", libName)); //NOI18N
                    rbNoneLibrary.setSelected(true);
                    if (panel !=null)
                        panel.setLibraryType(JSFConfigurationPanel.LibraryType.NONE);
                    enableNewLibraryComponent(false);
                    enableDefinedLibraryComponent(false);
                }

            } catch (IOException exception) {
                Exceptions.printStackTrace(exception);
            }
        } else {
            switch( panel.getLibraryType()) {
                case NEW: {
                    rbNewLibrary.setSelected(true);
                    break;
                }
                case USED: {
                    rbRegisteredLibrary.setSelected(true);
                    break;
                }
                case NONE: {
                    rbNoneLibrary.setSelected(true);
                    enableDefinedLibraryComponent(false);
                    enableNewLibraryComponent(false);
                    break;
                }
            }

        }
    }

    private boolean isServerInstanceChanged() {
        if ((serverInstanceID==null && currentServerInstanceID !=null) ||
                (serverInstanceID != null &&  !serverInstanceID.equals(currentServerInstanceID))) {
            currentServerInstanceID = serverInstanceID;
            return true;
        }
        return false;
    }

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JSFConfigurationPanelVisual.class);
    }
    
    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public String getServletName(){
        return jsfServletName==null ? JSF_SERVLET_NAME : jsfServletName;
    }
    
    protected void setServletName(String name){
        jsfServletName = name;
    }
    
    public String getURLPattern(){
        return tURLPattern.getText();
    }
    
    protected void setURLPattern(String pattern){
        tURLPattern.setText(pattern);
    }
    
    public boolean packageJars(){
        return cbPackageJars.isSelected();
    }

    protected String getPreferredLanguage() {
        return (String) cbPreferredLang.getSelectedItem();
    }


    private void updateLibrary(){
        if (cbLibraries.getItemCount() == 0)
            rbRegisteredLibrary.setEnabled(false);
        
        if (rbNoneLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(false);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.NONE);
            panel.getController().setErrorMessage(null);
        } else if (rbRegisteredLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(true);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.USED);
            if (jsfLibraries.size() > 0){
                panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()).getLibrary());
            }
            panel.getController().setErrorMessage(null);
        } else if (rbNewLibrary.isSelected()){
            enableNewLibraryComponent(true);
            enableDefinedLibraryComponent(false);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.NEW);
            setNewLibraryFolder();
        }
        updatePreferredLanguages();
    }
    
    private void enableDefinedLibraryComponent(boolean enabled){
        cbLibraries.setEnabled(enabled);
    }
    
    private void enableNewLibraryComponent(boolean enabled){
        lDirectory.setEnabled(enabled);
        jtFolder.setEnabled(enabled);
        jbBrowse.setEnabled(enabled);
        lVersion.setEnabled(enabled);
        jtNewLibraryName.setEnabled(enabled);
    }

    private void setNewLibraryFolder() {
        String fileName = jtFolder.getText();
        
        if (fileName == null || "".equals(fileName)) {
            panel.setInstallFolder(null);
        } else {
            File folder = new File(fileName);
            panel.setInstallFolder(folder);            
        }
    }
    
    // the name of the library is used as ant property
    private static final Pattern VALID_PROPERTY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N
    
    private String checkLibraryName(String name) {
        String message = null;
        if (name.length() == 0) {
            message = NbBundle.getMessage(JSFUtils.class, "ERROR_InvalidLibraryName");
        } else {
            if (!VALID_PROPERTY_NAME.matcher(name).matches()) {
                message = NbBundle.getMessage(JSFUtils.class, "ERROR_InvalidLibraryNameCharacters");
            }
        }
        return message;
    }

    private static class LibraryItem {
        
        private Library library;
        private JSFVersion version;
        
        public LibraryItem(Library library, JSFVersion version) {
            this.library = library;
            this.version = version;
        }
        
        public Library getLibrary() {
            return library;
        }

        public JSFVersion getVersion() {
            return version;
        }
        
        public String toString() {
            return library.getDisplayName();
        }
    }
}
