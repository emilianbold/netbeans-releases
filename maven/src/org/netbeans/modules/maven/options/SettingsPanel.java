/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.ActionMappings;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.NbGlobalActionGoalProvider;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The visual panel that displays in the Options dialog. Some properties
 * are written to the settings file, some into the Netbeans settings..
 * @author  mkleint
 */
public class SettingsPanel extends javax.swing.JPanel {
    private static final String CP_SELECTED = "wasSelected"; //NOI18N
    private boolean changed;
    private boolean valid;
    private ActionListener listener;
    private DocumentListener docList;
    private MavenOptionController controller;
    private TextValueCompleter completer;
    
    /** Creates new form SettingsPanel */
    SettingsPanel(MavenOptionController controller) {
        initComponents();
        this.controller = controller;
        docList = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                documentChanged(e);
            }
            public void removeUpdate(DocumentEvent e) {
                documentChanged(e);
            }
            public void changedUpdate(DocumentEvent e) {
                documentChanged(e);
            }
        };
        initValues();
        ((MyJTextField)txtLocalRepository).setHintText("<Use default local repository as defined by Maven>");
        listener = new ActionListenerImpl();
        cbSnapshots.addActionListener(listener);
        comIndex.addActionListener(listener);
        completer = new TextValueCompleter(getGlobalOptions(), txtOptions, " ");
        initEmbeddedVersion();
    }

    static String[] AVAILABLE_OPTIONS = new String[] {
            "--offline", //NOI18N
            "--debug", //NOI18N
            "--errors", //NOI18N
            "--batch-mode", //NOI18N
            "--fail-fast", //NOI18N
            "--fail-at-end", //NOI18N
            "--fail-never", //NOI18N
            "--strict-checksums", //NOI18N
            "--lax-checksums", //NOI18N
            "--check-plugin-updates", //NOI18N
            "--no-plugin-updates", //NOI18N
            "--update-snapshots", //NOI18N
            "--no-plugin-registry" //NOI18N
        };


    static String[] getAvailableOptionsDescriptions() {
        return new String[] {
            "Work offline.",
            "Produce execution debug output.",
            "Produce execution error messages.",
            "Run in non-interactive (batch) mode.",
            "Stop at first failure in reactorized builds\n\nExclusive with --fail-at-end and --fail-never.",
            "Only fail the build afterwards; allow all non-impacted builds to continue.\n\nExclusive with --fail-fast and --fail-never.",
            "NEVER fail the build, regardless of project result.\n\nExclusive with --fail-fast and --fail-at-end.",
            "Fail the build if checksums don't match.\n\n Exclusive with --lax-checksums.",
            "Warn if checksums don't match.\n\nExclusive with --strict-checksums.",
            "Force upToDate check for any relevant registered plugins.\n\nExclusive with --no-plugin-updates.",
            "Suppress upToDate check for any relevant registered plugins.\n\nExclusive with --check-plugin-updates.",
            "Forces a check for updated releases and snapshots on remote repositories.",
            "Don't use ~/.m2/plugin-registry.xml for plugin versions"
        };
    }

    private static List<String> getGlobalOptions() {
        return Arrays.asList(AVAILABLE_OPTIONS);
    }

    private void initEmbeddedVersion()
    {
        //there was a renumbering scheme for maven. current trunk is not 2.1 but 3.0
        //http://blogs.sonatype.com/brian/2008/09/05/1220649145080.html
        //XXX: just hardwire here to not confuse people with old style versions.
          lblEmbeddedVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_MavenVersion2", "3.0-SNAPSHOT")); //NOI18N
        
//        InputStream resourceAsStream;
//        try {
//            Properties properties = new Properties();
//            resourceAsStream = EmbedderFactory.class.getClassLoader().getResourceAsStream( "META-INF/maven/org.apache.maven/maven-core/pom.properties" ); //NOI18N
//            properties.load( resourceAsStream );
//
//            if ( properties.getProperty( "builtOn" ) != null ) { //NOI18N
//                lblEmbeddedVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_MavenVersion1", 
//                        properties.getProperty( "version", "unknown" ), properties.getProperty( "builtOn" )));
//            } else {
//                lblEmbeddedVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_MavenVersion2", properties.getProperty( "version", "unknown" )));
//            }
//        }
//        catch  ( IOException e ) {
//            lblEmbeddedVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_MavenVersion3"));
//        }
    }

    private void initExternalVersion()
    {
        String path = txtCommandLine.getText().trim();
        if (path.length() == 0) {
            String ver = MavenSettings.getDefaultMavenInstanceVersion();
            if (ver != null) {
                lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_ExMavenVersion3", ver));//NOI18N
            } else {
                lblExternalVersion.setText(ver != null ? ver : ""); //NOI18N
            }
            return;
        }
        File root = new File(path);
        File lib = new File(root, "lib"); //NOI18N
        if (lib.exists()) {
            File[] jars = lib.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar"); //NOI18N
                }
            });
            for (File jar : jars) {
                JarFile jf = null;
                try
                {
                    jf = new JarFile(jar);
                    ZipEntry entry = jf.getEntry("META-INF/maven/org.apache.maven/maven-core/pom.properties");//NOI18N
                    if (entry != null) {
                        InputStream resourceAsStream = jf.getInputStream(entry);
                        Properties properties = new Properties();
                        properties.load( resourceAsStream );
                        if ( properties.getProperty( "builtOn" ) != null ) { //NOI18N
                            lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_ExMavenVersion1", 
                                    properties.getProperty( "version", "unknown" ), properties.getProperty( "builtOn" )));//NOI18N
                        } else {
                            lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_ExMavenVersion2", properties.getProperty( "version", "unknown" )));//NOI18N
                        }
                        return;
                    }
                } catch ( IOException ex )
                {
                    //ignore..
                } finally {
                    if (jf != null) {
                        try {
                            jf.close();
                        } catch (IOException x) {}
                    }
                }
            }
        }
        //add red color..
        lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "ERR_NoValidInstallation"));
    }
    
    private void initValues() {
        comIndex.setSelectedIndex(0);
        cbSnapshots.setSelected(true);
    }
    
    private void documentChanged(DocumentEvent e) {
        changed = true;
        boolean oldvalid = valid;
        if (txtCommandLine.getText().trim().length() > 0) {
            File fil = new File(txtCommandLine.getText());
            if (fil.exists() && new File(fil, "bin" + File.separator + "mvn").exists()) { //NOI18N
                valid = true;
            } else {
                valid = false;
            }
        } else {
            valid = true;
        }
        if (oldvalid != valid) {
            controller.firePropChange(MavenOptionController.PROP_VALID, Boolean.valueOf(oldvalid), Boolean.valueOf(valid));
        }
        initExternalVersion();
    }
    
    private ComboBoxModel createComboModel() {
        return new DefaultComboBoxModel(
                new String[] { 
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FREQ_weekly"), 
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FREQ_Daily"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FREQ_Always"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FREQ_Never") });
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgChecksums = new javax.swing.ButtonGroup();
        bgPlugins = new javax.swing.ButtonGroup();
        bgFailure = new javax.swing.ButtonGroup();
        lblCommandLine = new javax.swing.JLabel();
        txtCommandLine = new javax.swing.JTextField();
        btnCommandLine = new javax.swing.JButton();
        lblOptions = new javax.swing.JLabel();
        txtOptions = new javax.swing.JTextField();
        btnOptions = new javax.swing.JButton();
        btnGoals = new javax.swing.JButton();
        lblEmbeddedVersion = new javax.swing.JLabel();
        lblExternalVersion = new javax.swing.JLabel();
        lblLocalRepository = new javax.swing.JLabel();
        txtLocalRepository = new MyJTextField();
        btnLocalRepository = new javax.swing.JButton();
        lblIndex = new javax.swing.JLabel();
        comIndex = new javax.swing.JComboBox();
        btnIndex = new javax.swing.JButton();
        cbSnapshots = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(lblCommandLine, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblCommandLine.text")); // NOI18N

        txtCommandLine.setText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.txtCommandLine.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnCommandLine, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnCommandLine.text")); // NOI18N
        btnCommandLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommandLineActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblOptions, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblOptions.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnOptions, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnOptions.text")); // NOI18N
        btnOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnGoals, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnGoals.text")); // NOI18N
        btnGoals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoalsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblEmbeddedVersion, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblEmbeddedVersion.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblExternalVersion, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblExternalVersion.text", new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblLocalRepository, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblLocalRepository.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnLocalRepository, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnLocalRepository.text")); // NOI18N
        btnLocalRepository.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLocalRepositoryActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblIndex, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblIndex.text")); // NOI18N

        comIndex.setModel(createComboModel());

        org.openide.awt.Mnemonics.setLocalizedText(btnIndex, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnIndex.text")); // NOI18N
        btnIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIndexActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbSnapshots, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbSnapshots.text")); // NOI18N
        cbSnapshots.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lblIndex)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(comIndex, 0, 225, Short.MAX_VALUE)
                            .add(cbSnapshots))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnIndex))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblCommandLine)
                            .add(lblOptions)
                            .add(lblLocalRepository))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblEmbeddedVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(txtCommandLine, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                                .add(6, 6, 6)
                                .add(btnCommandLine))
                            .add(lblExternalVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(txtOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                                    .add(txtLocalRepository, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, btnLocalRepository)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, btnOptions)))))
                    .add(btnGoals))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnCommandLine, btnIndex, btnLocalRepository, btnOptions}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lblEmbeddedVersion)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCommandLine)
                    .add(txtCommandLine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnCommandLine))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblExternalVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblOptions)
                    .add(txtOptions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnOptions))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(btnLocalRepository)
                        .add(txtLocalRepository, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lblLocalRepository))
                .add(18, 18, 18)
                .add(btnGoals)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 114, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnIndex)
                    .add(lblIndex)
                    .add(comIndex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbSnapshots)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIndexActionPerformed
        btnIndex.setEnabled(false);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                //TODO shall we iterate all "local" repositories??
                RepositoryInfo info = RepositoryPreferences.getInstance().getRepositoryInfoById(RepositoryPreferences.LOCAL_REPO_ID);
                if (info != null) {
                    RepositoryIndexer.indexRepo(info);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            btnIndex.setEnabled(true);
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_btnIndexActionPerformed
    
    private void btnLocalRepositoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLocalRepositoryActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(SettingsPanel.class, "TIT_Select"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        String path = ((MyJTextField)txtLocalRepository).getRealText();
        if (path.trim().length() == 0) {
            path = new File(System.getProperty("user.home"), ".m2").getAbsolutePath(); //NOI18N
        }
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            ((MyJTextField)txtLocalRepository).setRealText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }
    }//GEN-LAST:event_btnLocalRepositoryActionPerformed

    private void btnCommandLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommandLineActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Select2"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        String path = txtCommandLine.getText();
        if (path.trim().length() == 0) {
            path = new File(System.getProperty("user.home")).getAbsolutePath(); //NOI18N
        }
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            txtCommandLine.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }
        
    }//GEN-LAST:event_btnCommandLineActionPerformed

    private void btnGoalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoalsActionPerformed
        NbGlobalActionGoalProvider provider = null;
        for (MavenActionsProvider prov : Lookup.getDefault().lookupAll(MavenActionsProvider.class)) {
            if (prov instanceof NbGlobalActionGoalProvider) {
                provider = (NbGlobalActionGoalProvider)prov;
            }
        }
        assert provider != null;
        try {
            ActionToGoalMapping mappings = new NetbeansBuildActionXpp3Reader().read(new StringReader(provider.getRawMappingsAsString()));
            ActionMappings panel = new ActionMappings(mappings);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Global"));
            Object retVal = DialogDisplayer.getDefault().notify(dd);
            if (retVal == DialogDescriptor.OK_OPTION) {
                FileObject dir = FileUtil.getConfigFile("Projects/org-netbeans-modules-maven"); //NOI18N
                // just make sure the name of the file is always nbactions.xml
                CustomizerProviderImpl.writeNbActionsModel(dir, mappings, M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnGoalsActionPerformed

    private void btnOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptionsActionPerformed
        GlobalOptionsPanel pnl = new GlobalOptionsPanel();
        DialogDescriptor dd = new DialogDescriptor(pnl, "Add Global Option(s)");
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            txtOptions.setText(txtOptions.getText() + pnl.getSelectedOnes());
        }

    }//GEN-LAST:event_btnOptionsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgChecksums;
    private javax.swing.ButtonGroup bgFailure;
    private javax.swing.ButtonGroup bgPlugins;
    private javax.swing.JButton btnCommandLine;
    private javax.swing.JButton btnGoals;
    private javax.swing.JButton btnIndex;
    private javax.swing.JButton btnLocalRepository;
    private javax.swing.JButton btnOptions;
    private javax.swing.JCheckBox cbSnapshots;
    private javax.swing.JComboBox comIndex;
    private javax.swing.JLabel lblCommandLine;
    private javax.swing.JLabel lblEmbeddedVersion;
    private javax.swing.JLabel lblExternalVersion;
    private javax.swing.JLabel lblIndex;
    private javax.swing.JLabel lblLocalRepository;
    private javax.swing.JLabel lblOptions;
    private javax.swing.JTextField txtCommandLine;
    private javax.swing.JTextField txtLocalRepository;
    private javax.swing.JTextField txtOptions;
    // End of variables declaration//GEN-END:variables
    
    public void setValues() {
        changed = false;

        txtOptions.setText(MavenSettings.getDefault().getDefaultOptions());
        txtCommandLine.getDocument().removeDocumentListener(docList);
        File command = MavenSettings.getDefault().getCommandLinePath();
        txtCommandLine.setText(command != null ? command.getAbsolutePath() : ""); //NOI18N
        initExternalVersion();
        txtCommandLine.getDocument().addDocumentListener(docList);
        
        cbSnapshots.setSelected(RepositoryPreferences.getInstance().isIncludeSnapshots());
        comIndex.setSelectedIndex(RepositoryPreferences.getInstance().getIndexUpdateFrequency());
        String repo = MavenSettings.getDefault().getCustomLocalRepository();
        ((MyJTextField)txtLocalRepository).setRealText(repo != null ? repo : "");
    }
    
    public void applyValues() {
        MavenSettings.getDefault().setDefaultOptions(txtOptions.getText().trim());
        MavenSettings.getDefault().setCustomLocalRepository(((MyJTextField)txtLocalRepository).getRealText());
        String cl = txtCommandLine.getText().trim();
        if (cl.length() == 0) {
            cl = null;
        }
        //MEVENIDE-553
        File command = cl != null ? new File(cl) : null;
        if (command != null && command.exists()) {
            MavenSettings.getDefault().setCommandLinePath(command);
        } else {
            MavenSettings.getDefault().setCommandLinePath(null);
        }
        RepositoryPreferences.getInstance().setIndexUpdateFrequency(comIndex.getSelectedIndex());
        RepositoryPreferences.getInstance().setIncludeSnapshots(cbSnapshots.isSelected());
        changed = false;
    }
    
    boolean hasValidValues() {
        return valid;
    }
    
    boolean hasChangedValues() {
        return changed;
    }
    
    private class ActionListenerImpl implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            changed = true;
        }
        
    }
}
