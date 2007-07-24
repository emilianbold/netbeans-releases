/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.freeform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.freeform.WebProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Radko Najman
 */
public class WebLocationsPanel extends javax.swing.JPanel implements HelpCtx.Provider {
    
    private static String J2EE_SPEC_5 = "1.5";    //NOI18N
    private static String J2EE_SPEC_1_4 = "1.4";    //NOI18N
    private static String J2EE_SPEC_1_3 = "1.3";    //NOI18N
    
    /** Original project base folder */
    private File baseFolder;
    /** Freeform Project base folder */
    private File nbProjectFolder;

    private AntProjectHelper projectHelper;
    
    private File srcPackagesLocation;
    private String classpath;
    
    private WizardDescriptor wizardDescriptor;
    
    /** Creates new form WebLocations */
    public WebLocationsPanel(WizardDescriptor wizardDescriptor) {
        initComponents();
        this.wizardDescriptor = wizardDescriptor;
        jComboBoxJ2eeLevel.addItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_5"));    //NOI18N
        jComboBoxJ2eeLevel.addItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_0"));    //NOI18N
        jComboBoxJ2eeLevel.addItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_1"));    //NOI18N
        jComboBoxJ2eeLevel.setSelectedIndex(0);
    }
    
    public WebLocationsPanel(AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        this(null);
        this.projectHelper = projectHelper;
        setFolders(Util.getProjectLocation(projectHelper, projectEvaluator), FileUtil.toFile(projectHelper.getProjectDirectory()));
        
        List l = WebProjectGenerator.getWebmodules(projectHelper, aux);
        if (l != null) {
            WebProjectGenerator.WebModule wm = (WebProjectGenerator.WebModule)l.get(0);
            String docroot = getLocationDisplayName(projectEvaluator, nbProjectFolder, wm.docRoot);
            String webInf;
            if (wm.webInf != null)
                webInf = getLocationDisplayName(projectEvaluator, nbProjectFolder, wm.webInf);
            else
                ////NetBeans 5.x and older projects (WEB-INF is placed under Web Pages)
                webInf = docroot + "/WEB-INF"; //NOI18N
            classpath = wm.classpath;
            jTextFieldWeb.setText(docroot);
            jTextFieldWebInf.setText(webInf);
            
            jTextFieldContextPath.setText(wm.contextPath);
            if (wm.j2eeSpecLevel.equals(J2EE_SPEC_5))
                jComboBoxJ2eeLevel.setSelectedItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_5"));
            else if (wm.j2eeSpecLevel.equals(J2EE_SPEC_1_4))
                jComboBoxJ2eeLevel.setSelectedItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_0"));
            else
                jComboBoxJ2eeLevel.setSelectedItem(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_1"));
        }
    }

    /**
     * Convert given string value (e.g. "${project.dir}/src" to a file
     * and try to relativize it.
     */
    // XXX: copied from java/freeform:SourceFoldersPanel.getLocationDisplayName
    public static String getLocationDisplayName(PropertyEvaluator evaluator, File base, String val) {
        File f = Util.resolveFile(evaluator, base, val);
        if (f == null) {
            return val;
        }
        String location = f.getAbsolutePath();
        if (CollocationQuery.areCollocated(base, f)) {
            location = PropertyUtils.relativizeFile(base, f).replace('/', File.separatorChar); // NOI18N
        }
        return location;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( WebLocationsPanel.class );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldWeb = new javax.swing.JTextField();
        jButtonWeb = new javax.swing.JButton();
        jLabelWebInf = new javax.swing.JLabel();
        jTextFieldWebInf = new javax.swing.JTextField();
        jButtonWebInf = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxJ2eeLevel = new javax.swing.JComboBox();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_Description")); // NOI18N

        jLabel2.setLabelFor(jTextFieldWeb);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_WebPagesLocation_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonWeb, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "BTN_BasicProjectInfoPanel_browseAntScript")); // NOI18N
        jButtonWeb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWebActionPerformed(evt);
            }
        });

        jLabelWebInf.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/freeform/ui/Bundle").getString("MNE_DeploymentDescriptorFolder").charAt(0));
        jLabelWebInf.setLabelFor(jTextFieldWebInf);
        jLabelWebInf.setText(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_DeploymentDescriptorFolder_Label")); // NOI18N

        jButtonWebInf.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/freeform/ui/Bundle").getString("MNE_BrowseWebInfLocation").charAt(0));
        jButtonWebInf.setText(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_DeploymentDescriptorBrowse_Label")); // NOI18N
        jButtonWebInf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWebInfActionPerformed(evt);
            }
        });

        jLabel4.setLabelFor(jTextFieldContextPath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_ContextPath_Label")); // NOI18N

        jLabel5.setLabelFor(jComboBoxJ2eeLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "LBL_WebPagesPanel_J2EESpecLevel_Label")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel1)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(jLabelWebInf))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldContextPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(jTextFieldWeb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(jComboBoxJ2eeLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldWebInf, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jButtonWebInf, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButtonWeb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .add(9, 9, 9)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonWeb)
                    .add(jLabel2)
                    .add(jTextFieldWeb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonWebInf)
                    .add(jTextFieldWebInf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelWebInf))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jTextFieldContextPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxJ2eeLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .add(22, 22, 22))
        );

        jTextFieldWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_WebPagesLocation_A11YDesc")); // NOI18N
        jButtonWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_WebPagesLocationBrowse_A11YDesc")); // NOI18N
        jTextFieldWebInf.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACSD_WEBINF_TEXTFIELD")); // NOI18N
        jButtonWebInf.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACSD_WEBINF_BROWSE")); // NOI18N
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_ContextPath_A11YDesc")); // NOI18N
        jComboBoxJ2eeLevel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebLocationsPanel.class, "ACS_LBL_WebPagesPanel_J2EESpecLevel_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jButtonWebInfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWebInfActionPerformed
        JFileChooser chooser = createChooser(getWebInfLocation(), wizardDescriptor);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            setWebInf(chooser.getSelectedFile());
        }
}//GEN-LAST:event_jButtonWebInfActionPerformed

    private void jButtonWebActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWebActionPerformed
        JFileChooser chooser = createChooser(getWebPagesLocation(), wizardDescriptor);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            setWebPages(chooser.getSelectedFile());
        }
    }//GEN-LAST:event_jButtonWebActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonWeb;
    private javax.swing.JButton jButtonWebInf;
    private javax.swing.JComboBox jComboBoxJ2eeLevel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelWebInf;
    private javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JTextField jTextFieldWeb;
    private javax.swing.JTextField jTextFieldWebInf;
    // End of variables declaration//GEN-END:variables
    
    private static JFileChooser createChooser(File webPagesLoc, WizardDescriptor wizardDescriptor) {
	String path = webPagesLoc.getAbsolutePath();
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, new File(path));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (path.length() > 0 && webPagesLoc.exists()) {
            chooser.setSelectedFile(webPagesLoc);
        } else {
	    if (wizardDescriptor != null) {
		// honor the contract in issue 58987
		File currentDirectory = null;
		FileObject existingSourcesFO = Templates.getExistingSourcesFolder(wizardDescriptor);
		if (existingSourcesFO != null) {
		    File existingSourcesFile = FileUtil.toFile(existingSourcesFO);
		    if (existingSourcesFile != null && existingSourcesFile.isDirectory()) {
			currentDirectory = existingSourcesFile;
		    }
		}
		if (currentDirectory != null) {
		    chooser.setCurrentDirectory(currentDirectory);
		} else {
		    chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
		}
	    } else {
		chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
	    }
        }
	
        return chooser;
    }

    protected List getWebModules() {
        ArrayList l = new ArrayList();

        WebProjectGenerator.WebModule wm = new WebProjectGenerator.WebModule ();
        wm.docRoot = getRelativeLocation(getWebPagesLocation());
        wm.webInf = getRelativeLocation(getWebInfLocation());
        wm.contextPath = jTextFieldContextPath.getText().trim();
        
        String j2eeLevel = (String) jComboBoxJ2eeLevel.getSelectedItem();
        if (j2eeLevel.equals(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_5")))
            wm.j2eeSpecLevel = J2EE_SPEC_5;
        else if (j2eeLevel.equals(NbBundle.getMessage(WebLocationsPanel.class, "TXT_J2EESpecLevel_0")))
            wm.j2eeSpecLevel = J2EE_SPEC_1_4;
        else
            wm.j2eeSpecLevel = J2EE_SPEC_1_3;
        
        wm.classpath = classpath;
        l.add (wm);
        return l;
    }

    protected List getJavaSrcFolder() {
        ArrayList l = new ArrayList();
        File sourceLoc = getSrcPackagesLocation();
        l.add(getRelativeLocation(sourceLoc));
        l.add(sourceLoc.getName());
        return l;
    }

    /**
     * @return list of pairs [relative path, display name]
     */
    protected List getWebSrcFolder() {
        ArrayList l = new ArrayList();
        final File webLocation = getWebPagesLocation();
        l.add(getRelativeLocation(webLocation));
        l.add(webLocation.getName());
        return l;
    }

    /**
     * @return list of pairs [relative path, display name]
     */
    protected List getWebInfFolder() {
        ArrayList l = new ArrayList();
        final File webInfLocation = getWebInfLocation();
        l.add(getRelativeLocation(webInfLocation));
        l.add(webInfLocation.getName());
        return l;
    }

    private File getAsFile(String filename) {
        return PropertyUtils.resolveFile(nbProjectFolder, filename);
    }

    /** Called from WizardDescriptor.Panel and ProjectCustomizer.Panel
     * to set base folder. Panel will use this for default position of JFileChooser.
     * @param baseFolder original project base folder
     * @param nbProjectFolder Freeform Project base folder
     */
    public void setFolders(File baseFolder, File nbProjectFolder) {
        this.baseFolder = baseFolder;
        this.nbProjectFolder = nbProjectFolder;
    }
    
    protected void setWebPages(String path) {
        jTextFieldWeb.setText(path);
    }
    
    protected void setWebInf(String path) {
        jTextFieldWebInf.setText(path);
    }

    protected void setSrcPackages(String path) {
        setSrcPackages(getAsFile(path));
    }

    private void setWebPages(final File file) {
        setWebPages(relativizeFile(file));
    }
    
    private void setWebInf(final File file) {
        setWebInf(relativizeFile(file));
    }

    protected File getWebPagesLocation() {
        return getAsFile(jTextFieldWeb.getText()).getAbsoluteFile();
    }
    
    protected File getWebInfLocation() {
        return getAsFile(jTextFieldWebInf.getText()).getAbsoluteFile();
    }

    private void setSrcPackages(final File file) {
        srcPackagesLocation = file;
    }

    protected File getSrcPackagesLocation() {
        return srcPackagesLocation;
    }

    private String relativizeFile(final File file) {
        File normalizedFile = FileUtil.normalizeFile(file);
        if (CollocationQuery.areCollocated(nbProjectFolder, file)) {
            return PropertyUtils.relativizeFile(nbProjectFolder, normalizedFile);
        } else {
            return normalizedFile.getAbsolutePath();
        }
    }

    private String getRelativeLocation(final File location) {
        final File normalizedLocation = FileUtil.normalizeFile(location);
        return Util.relativizeLocation(baseFolder, nbProjectFolder, normalizedLocation);
    }

    ActionListener getCustomizerOkListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(projectHelper);
                WebProjectGenerator.putWebSourceFolder(projectHelper, getWebSrcFolder());
                WebProjectGenerator.putWebInfFolder(projectHelper, getWebInfFolder());
                WebProjectGenerator.putWebModules(projectHelper, aux, getWebModules());
            }
        };
    }
    
}
