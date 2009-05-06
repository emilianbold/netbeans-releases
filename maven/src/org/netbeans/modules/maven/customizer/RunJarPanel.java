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

package org.netbeans.modules.maven.customizer;

import hidden.org.codehaus.plexus.util.IOUtil;
import java.awt.Component;
import java.awt.Dialog;
import java.util.MissingResourceException;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.maven.MavenSourcesImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * panel for displaying the Run Jar project related properties..
 * in older version was bound to netbeans-jar-plugin, now is bound to plain
 * exec-maven-plugin:exec
 * @author Milos Kleint 
 */
public class RunJarPanel extends javax.swing.JPanel {
    /**deprecated items
     */
    private static final String ARTFACTID_JAR = "maven-jar-plugin";//NOI18N
    private static final String ARITFACTID_ASSEMBLY = "maven-assembly-plugin";//NOI18N
    private static final String DEPRECATED_RUN_PARAMS = "netbeans.jar.run.params"; //NOI18N
    private static final String DEPRECATED_RUN_WORKDIR = "netbeans.jar.run.workdir"; //NOI18N
    private static final String DEPRECATED_RUN_JVM_PARAMS = "netbeans.jar.run.jvmparams"; //NOI18N

    private boolean isDeprecatedRun = false;
    private boolean isDeprecatedDebug = false;
    private boolean isDeprecatedProfile = false;
    private org.netbeans.modules.maven.model.pom.Plugin jarPlugin;
    private org.netbeans.modules.maven.model.pom.Plugin assemblyPlugin;
    
    private boolean isCurrentRun = true;
    private boolean isCurrentDebug = true;
    private boolean isCurrentProfile = true;
    private static final String RUN_PARAMS = "exec.args"; //NOI18N
    private static final String RUN_WORKDIR = "exec.workingdir"; //NOI18N
    private static final String DEFAULT_DEBUG_PARAMS = "-Xdebug -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address}"; //NOI18N
    private static final String DEFAULT_PROFILE_PARAMS = "${profiler.args}"; // NOI18N
    private static final String DEFAULT_PROFILER_EXEC = "${profiler.java}"; // NOI18N
    private static final String RUN_EXEC = "exec.executable"; // NOI18N

    private static final String PROFILE_CMD = "profile"; // NOI18N
    
    private ModelHandle handle;
    private NbMavenProjectImpl project;
    private NetbeansActionMapping run;
    private NetbeansActionMapping debug;
    private NetbeansActionMapping profile;
    private String oldMainClass;
    private String oldParams;
    private String oldVMParams;
    private String oldWorkDir;
    private String oldAllParams;
    private DocumentListener docListener;
    private ActionListener comboListener;
    
    public RunJarPanel(ModelHandle handle, NbMavenProjectImpl project) {
        initComponents();
        this.handle = handle;
        this.project = project;
        comConfiguration.setEditable(false);
        comConfiguration.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component com = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (com instanceof JLabel) {
                    if (value == RunJarPanel.this.handle.getActiveConfiguration()) {
                        com.setFont(com.getFont().deriveFont(Font.BOLD));
                    }
                }
                return com;
            }
        });
        setupConfigurations();
        
        initValues();
        lblMainClass.setFont(lblMainClass.getFont().deriveFont(Font.BOLD));
        List<FileObject> roots = new ArrayList<FileObject>();
        Sources srcs =  ProjectUtils.getSources(project);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            SourceGroup sourceGroup = grps[i];
            if (MavenSourcesImpl.NAME_SOURCE.equals(sourceGroup.getName())) {
                roots.add(sourceGroup.getRootFolder());
            }
        }
        grps = srcs.getSourceGroups(MavenSourcesImpl.TYPE_GEN_SOURCES);
        for (int i = 0; i < grps.length; i++) {
            SourceGroup sourceGroup = grps[i];
            roots.add(sourceGroup.getRootFolder());
        }

        btnMainClass.addActionListener(new MainClassListener(roots.toArray(new FileObject[roots.size()]), txtMainClass));
        docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {
                applyChanges();
            }

            public void removeUpdate(DocumentEvent arg0) {
                applyChanges();
            }

            public void changedUpdate(DocumentEvent arg0) {
                applyChanges();
            }
        };
        comboListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeListeners();
                initValues();
                addListeners();
            }
        };
    }

    private void addListeners() {
        comConfiguration.addActionListener(comboListener);
        txtMainClass.getDocument().addDocumentListener(docListener);
        txtArguments.getDocument().addDocumentListener(docListener);
        txtVMOptions.getDocument().addDocumentListener(docListener);
        txtWorkDir.getDocument().addDocumentListener(docListener);
    }
    
    private void removeListeners() {
        comConfiguration.removeActionListener(comboListener);
        txtMainClass.getDocument().removeDocumentListener(docListener);
        txtArguments.getDocument().removeDocumentListener(docListener);
        txtVMOptions.getDocument().removeDocumentListener(docListener);
        txtWorkDir.getDocument().removeDocumentListener(docListener);
    }
    

    /**
     * @deprecated 
     */
    private @Deprecated void applyDeprecatedExternalChanges() throws MissingResourceException {
        File assDir = new File(new File(new File(project.getOriginalMavenProject().getBasedir(), "src"), "main"), "assemblies"); //NOI18N
        if (!assDir.exists()) {
            assDir.mkdirs();
        }
        File assembly = new File(assDir, "netbeans-run.xml"); //NOI18N
        if (!assembly.exists()) {
            InputStream instr = null;
            OutputStream outstr = null;
            try {
                assembly.createNewFile();
                instr = getClass().getResourceAsStream("/org/netbeans/modules/maven/execute/netbeans-run.xml"); //NOI18N
                outstr = new FileOutputStream(assembly);
                IOUtil.copy(instr, outstr);
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
                NotifyDescriptor nd = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(RunJarPanel.class, "Err_CannotCreate", assembly));
                DialogDisplayer.getDefault().notify(nd);
            } finally {
                IOUtil.close(instr);
                IOUtil.close(outstr);
            }
        }
    }
    
    private void initValues() {
        run = null;
        debug = null;
        profile = null;
        if (handle.isConfigurationsEnabled()) {
            ActionToGoalMapping mapp = handle.getActionMappings((ModelHandle.Configuration) comConfiguration.getSelectedItem());
            @SuppressWarnings("unchecked")
            List<NetbeansActionMapping> lst = mapp.getActions();
            for (NetbeansActionMapping m : lst) {
                if (ActionProvider.COMMAND_RUN.equals(m.getActionName())) {
                    run = m;
                }
                if (ActionProvider.COMMAND_DEBUG.equals(m.getActionName())) {
                    debug = m;
                }
                if (PROFILE_CMD.equals(m.getActionName())) {
                    profile = m;
                }
            }
        }
        if (run == null) {
            run = ModelHandle.getActiveMapping(ActionProvider.COMMAND_RUN, project);
        }
        if (debug == null) {
            debug = ModelHandle.getActiveMapping(ActionProvider.COMMAND_DEBUG, project);
        }
        if (profile == null) {
            profile = ModelHandle.getActiveMapping(PROFILE_CMD, project);
        }
        isCurrentRun = checkNewMapping(run);
        isCurrentDebug = checkNewMapping(debug);
        isCurrentProfile = checkNewMapping(profile);
        if (isCurrentDebug || isCurrentRun || isCurrentProfile) {
            oldWorkDir = run.getProperties().getProperty(RUN_WORKDIR);
            if (oldWorkDir == null) {
                oldWorkDir = debug.getProperties().getProperty(RUN_WORKDIR);
            }
            if (oldWorkDir == null && profile != null) {
                oldWorkDir = profile.getProperties().getProperty(RUN_WORKDIR);
            }
            String params = run.getProperties().getProperty(RUN_PARAMS);
            if (params == null) {
                params = debug.getProperties().getProperty(RUN_PARAMS);
            }
            if (params == null && profile != null) {
                params = profile.getProperties().getProperty(RUN_PARAMS);
            }
            if (params != null) {
                oldAllParams = params;
                oldVMParams = splitJVMParams(params);
                if (oldVMParams != null && oldVMParams.contains("-classpath %classpath")) {
                    oldVMParams = oldVMParams.replace("-classpath %classpath", "");
                }
                oldMainClass = splitMainClass(params);
                if (oldMainClass != null && oldMainClass.equals("${packageClassName}")) {
                    oldMainClass = "";
                }
                oldParams = splitParams(params);
            } else {
                oldAllParams = "";
            }
        } else {
            isDeprecatedRun = checkDeprecatedMapping(run);
            isDeprecatedDebug = checkDeprecatedMapping(debug);
            isDeprecatedProfile = checkDeprecatedMapping(profile);
            if (isDeprecatedDebug || isDeprecatedRun || isDeprecatedProfile) {
                org.netbeans.modules.maven.model.pom.Profile publicProfile = handle.getNetbeansPublicProfile(false);
                jarPlugin = null;
                assemblyPlugin = null;
                if (publicProfile != null && publicProfile.getBuildBase() != null) {
                    org.netbeans.modules.maven.model.pom.BuildBase bld = publicProfile.getBuildBase();
                    List<org.netbeans.modules.maven.model.pom.Plugin> plugins = bld.getPlugins();
                    for (org.netbeans.modules.maven.model.pom.Plugin elem : plugins) {
                        if (ARTFACTID_JAR.equals(elem.getArtifactId())) { //NOI18N
                            jarPlugin = elem;
                        }
                        if (ARITFACTID_ASSEMBLY.equals(elem.getArtifactId())) { //NOI18N
                            assemblyPlugin = elem;
                        }
                    }
                }
                if (jarPlugin != null) {
                    Xpp3Dom conf = (Xpp3Dom)jarPlugin.getConfiguration();
                    Xpp3Dom archive = conf.getChild("archive"); //NOI18N
                    if (archive != null) {
                        Xpp3Dom manifest = archive.getChild("manifest"); //NOI18N
                        if (manifest != null) {
                            Xpp3Dom mainClass = manifest.getChild("mainClass"); //NOI18N
                            if (mainClass != null) {
                                oldMainClass = mainClass.getValue();
                            }
                        }
                    }
                }
                if (isDeprecatedRun) {
                    oldParams = run.getProperties().getProperty(DEPRECATED_RUN_PARAMS);
                    oldWorkDir = run.getProperties().getProperty(DEPRECATED_RUN_WORKDIR);
                    oldVMParams = run.getProperties().getProperty(DEPRECATED_RUN_JVM_PARAMS);
                } else if (isDeprecatedDebug) {
                    oldParams = debug.getProperties().getProperty(DEPRECATED_RUN_PARAMS);
                    oldWorkDir = debug.getProperties().getProperty(DEPRECATED_RUN_WORKDIR);
                    oldVMParams = debug.getProperties().getProperty(DEPRECATED_RUN_JVM_PARAMS);
                } else if (isDeprecatedProfile) {
                    oldParams = profile.getProperties().getProperty(DEPRECATED_RUN_PARAMS);
                    oldWorkDir = profile.getProperties().getProperty(DEPRECATED_RUN_WORKDIR);
                    oldVMParams = profile.getProperties().getProperty(DEPRECATED_RUN_JVM_PARAMS);
                }
            }
        }
        
        if (oldMainClass == null) {
            oldMainClass = ""; //NOI18N
        }
        txtMainClass.setText(oldMainClass);
        if (oldParams == null) {
            oldParams = ""; //NOI18N
        }
        txtArguments.setText(oldParams);
        if (oldVMParams == null) {
            oldVMParams = ""; //NOI18N
        }
        txtVMOptions.setText(oldVMParams);
        if (oldWorkDir == null) {
            oldWorkDir = ""; //NOI18N
        }
        txtWorkDir.setText(oldWorkDir);
        
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setupConfigurations();
        initValues();
        addListeners();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        removeListeners();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblMainClass = new javax.swing.JLabel();
        txtMainClass = new javax.swing.JTextField();
        btnMainClass = new javax.swing.JButton();
        lblArguments = new javax.swing.JLabel();
        txtArguments = new javax.swing.JTextField();
        lblWorkDir = new javax.swing.JLabel();
        txtWorkDir = new javax.swing.JTextField();
        btnWorkDir = new javax.swing.JButton();
        lblVMOptions = new javax.swing.JLabel();
        txtVMOptions = new javax.swing.JTextField();
        lblHint = new javax.swing.JLabel();
        lblConfiguration = new javax.swing.JLabel();
        comConfiguration = new javax.swing.JComboBox();

        lblMainClass.setLabelFor(txtMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(lblMainClass, org.openide.util.NbBundle.getMessage(RunJarPanel.class, "LBL_MainClass")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnMainClass, org.openide.util.NbBundle.getMessage(RunJarPanel.class, "BTN_Browse")); // NOI18N

        lblArguments.setLabelFor(txtArguments);
        org.openide.awt.Mnemonics.setLocalizedText(lblArguments, org.openide.util.NbBundle.getMessage(RunJarPanel.class, "LBL_Arguments")); // NOI18N

        lblWorkDir.setLabelFor(txtWorkDir);
        org.openide.awt.Mnemonics.setLocalizedText(lblWorkDir, org.openide.util.NbBundle.getMessage(RunJarPanel.class, "LBL_WorkDir")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnWorkDir, org.openide.util.NbBundle.getMessage(RunJarPanel.class, "BTN_Browse")); // NOI18N
        btnWorkDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWorkDirActionPerformed(evt);
            }
        });

        lblVMOptions.setLabelFor(txtVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(lblVMOptions, org.openide.util.NbBundle.getMessage(RunJarPanel.class, "LBL_VMOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint, org.openide.util.NbBundle.getMessage(RunJarPanel.class, "LBL_VMHint")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblConfiguration, "Configuration:");

        comConfiguration.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblWorkDir)
                    .add(lblVMOptions)
                    .add(lblArguments)
                    .add(lblConfiguration)
                    .add(lblMainClass))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(txtVMOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .add(txtWorkDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .add(txtArguments, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .add(txtMainClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .add(comConfiguration, 0, 209, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btnWorkDir)
                    .add(btnMainClass)))
            .add(layout.createSequentialGroup()
                .add(128, 128, 128)
                .add(lblHint)
                .addContainerGap(208, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblConfiguration)
                    .add(comConfiguration, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMainClass)
                    .add(btnMainClass)
                    .add(txtMainClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblArguments)
                    .add(txtArguments, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblWorkDir)
                    .add(txtWorkDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnWorkDir))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblVMOptions)
                    .add(txtVMOptions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHint)
                .addContainerGap(139, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnWorkDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWorkDirActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = txtWorkDir.getText();
        if (workDir.equals("")) { //NOI18N
            workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(RunJarPanel.class, "TIT_SelectWorkingDirectory"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            txtWorkDir.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_btnWorkDirActionPerformed

    void applyExternalChanges() {
        String newMainClass = txtMainClass.getText().trim();
        if (!newMainClass.equals(oldMainClass)) {
            if (isDeprecatedRun || isDeprecatedDebug) {
                applyDeprecatedExternalChanges();
            }
        }
        
    }
    
    void applyChanges() {
        String newMainClass = txtMainClass.getText().trim();
        String newParams = txtArguments.getText().trim();
        String newVMParams = txtVMOptions.getText().trim();
        String newWorkDir = txtWorkDir.getText().trim();
        ActionToGoalMapping a2gm; 
        if (handle.isConfigurationsEnabled()) {
            a2gm = handle.getActionMappings((ModelHandle.Configuration) comConfiguration.getSelectedItem());
        } else {
            a2gm = handle.getActionMappings();
        }
        if (isCurrentRun || isCurrentDebug || isCurrentProfile) {
            String newAllParams = newVMParams + " -classpath %classpath "; //NOI18N
            if (newMainClass.trim().length() > 0) {
                newAllParams = newAllParams + newMainClass + " "; //NOI18N
            } else {
                newAllParams = newAllParams + "${packageClassName} "; //NOI18N
            }
            newAllParams = newAllParams + newParams;
            newAllParams = newAllParams.trim();
            if (isCurrentRun) {
                boolean changed = false;
                if (!oldAllParams.equals(newAllParams)) {
                    run.getProperties().setProperty(RUN_PARAMS, newAllParams);
                    changed = true;
                }
                if (!oldWorkDir.equals(newWorkDir)) {
                    run.getProperties().setProperty(RUN_WORKDIR, newWorkDir);
                    changed = true;
                }
                if (changed) {
                    ModelHandle.setUserActionMapping(run, a2gm);
                    handle.markAsModified(a2gm);
                }
            }
            if (isCurrentDebug) {
                boolean changed = false;
                if (!oldAllParams.equals(newAllParams)) {
                    debug.getProperties().setProperty(RUN_PARAMS, DEFAULT_DEBUG_PARAMS + " " + newAllParams);
                    changed = true;
                }
                if (!oldWorkDir.equals(newWorkDir)) {
                    debug.getProperties().setProperty(RUN_WORKDIR, newWorkDir);
                    changed = true;
                }
                if (changed) {
                    ModelHandle.setUserActionMapping(debug, a2gm);
                    handle.markAsModified(a2gm);
                }
            }
            if (isCurrentProfile) {
                boolean changed = false;
                if (!oldAllParams.equals(newAllParams)) {
                    profile.getProperties().setProperty(RUN_PARAMS, DEFAULT_PROFILE_PARAMS + " " + newAllParams);
                    changed = true;
                }
                if (!oldWorkDir.equals(newWorkDir)) {
                    profile.getProperties().setProperty(RUN_WORKDIR, newWorkDir);
                    changed = true;
                }
                profile.getProperties().setProperty(RUN_EXEC, DEFAULT_PROFILER_EXEC);
                if (changed) {
                    ModelHandle.setUserActionMapping(profile, a2gm);
                    handle.markAsModified(a2gm);
                }
            }
        } else if (isDeprecatedRun || isDeprecatedDebug || isDeprecatedProfile) {
            if (!newMainClass.equals(oldMainClass)) {
                jarPlugin = checkJarPlugin(jarPlugin, newMainClass);
                assemblyPlugin = checkAssemblyPlugin(assemblyPlugin);
                handle.markAsModified(handle.getPOMModel());
            }
            if (!newParams.equals(oldParams)) {
                if (isDeprecatedRun) {
                    run.getProperties().setProperty(DEPRECATED_RUN_PARAMS, newParams);
                    ModelHandle.setUserActionMapping(run, a2gm);
                    handle.markAsModified(a2gm);
                }
                if (isDeprecatedDebug) {
                    debug.getProperties().setProperty(DEPRECATED_RUN_PARAMS, newParams);
                    ModelHandle.setUserActionMapping(debug, a2gm);
                    handle.markAsModified(a2gm);
                }
                if (isDeprecatedProfile) {
                    profile.getProperties().setProperty(DEPRECATED_RUN_PARAMS, newParams);
                    ModelHandle.setUserActionMapping(profile, a2gm);
                    handle.markAsModified(a2gm);
                }
            }
            if (!newVMParams.equals(oldVMParams)) {
                if (isDeprecatedRun) {
                    run.getProperties().setProperty(DEPRECATED_RUN_JVM_PARAMS, newVMParams);
                    ModelHandle.setUserActionMapping(run, a2gm);
                    handle.markAsModified(a2gm);
                }
                if (isDeprecatedDebug) {
                    debug.getProperties().setProperty(DEPRECATED_RUN_JVM_PARAMS, newVMParams);
                    ModelHandle.setUserActionMapping(debug, a2gm);
                    handle.markAsModified(a2gm);
                }
                if (isDeprecatedProfile) {
                    profile.getProperties().setProperty(DEPRECATED_RUN_JVM_PARAMS, newVMParams);
                    ModelHandle.setUserActionMapping(profile, a2gm);
                    handle.markAsModified(a2gm);
                }
            }
            if (!newWorkDir.equals(oldWorkDir)) {
                if (isDeprecatedRun) {
                    run.getProperties().setProperty(DEPRECATED_RUN_WORKDIR, newWorkDir);
                    ModelHandle.setUserActionMapping(run, a2gm);
                    handle.markAsModified(a2gm);
                }
                if (isDeprecatedDebug) {
                    debug.getProperties().setProperty(DEPRECATED_RUN_WORKDIR, newWorkDir);
                    ModelHandle.setUserActionMapping(debug, a2gm);
                    handle.markAsModified(a2gm);
                }
                if (isDeprecatedProfile) {
                    profile.getProperties().setProperty(DEPRECATED_RUN_WORKDIR, newWorkDir);
                    ModelHandle.setUserActionMapping(profile, a2gm);
                    handle.markAsModified(a2gm);
                }
                //MEVENIDE-599
                NetbeansActionMapping[] activeCustomMappings = ActionToGoalUtils.getActiveCustomMappings(project);
                for (NetbeansActionMapping actionMapping : activeCustomMappings) {
                    if (actionMapping.getProperties().getProperty(DEPRECATED_RUN_WORKDIR) != null) {
                        actionMapping.getProperties().setProperty(DEPRECATED_RUN_WORKDIR, newWorkDir);
                        ModelHandle.setUserActionMapping(actionMapping, a2gm);
                        handle.markAsModified(a2gm);
                    }
                }
            }
        }
    }

    private boolean checkDeprecatedMapping(NetbeansActionMapping map) {
        if (map == null || map.getGoals() == null) {
            return false; //#164323
        }
        Iterator it = map.getGoals().iterator();
        while (it.hasNext()) {
            String goal = (String) it.next();
            if (goal.indexOf("org.codehaus.mevenide:netbeans-run-plugin") > -1) { //NOI18N
                return true;
            }
        }
        return false;
    }
    
    private boolean checkNewMapping(NetbeansActionMapping map) {
        if (map == null || map.getGoals() == null) {
            return false; //#164323
        }
        Iterator it = map.getGoals().iterator();
        while (it.hasNext()) {
            String goal = (String) it.next();
            if (goal.matches("org\\.codehaus\\.mojo\\:exec-maven-plugin\\:(.)+\\:exec") //NOI18N
                    || goal.indexOf("exec:exec") > -1) { //NOI18N
                return true;
            }
        }
        return false;
    }
    

    /**
     * @deprecated 
     */
    private @Deprecated Plugin checkJarPlugin(Plugin jarPlugin, String val) {
        if (jarPlugin == null) {
            jarPlugin = handle.getPOMModel().getFactory().createPlugin();
            jarPlugin.setArtifactId(ARTFACTID_JAR); 
            jarPlugin.setGroupId(Constants.GROUP_APACHE_PLUGINS); 
            jarPlugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_JAR));
            BuildBase base = handle.getNetbeansPublicProfile().getBuildBase();
            if (base == null) {
                base = handle.getPOMModel().getFactory().createBuildBase();
                handle.getNetbeansPublicProfile().setBuildBase(base);
            }
            base.addPlugin(jarPlugin);
        }
        Configuration config = jarPlugin.getConfiguration();
        if (config == null) {
            config = handle.getPOMModel().getFactory().createConfiguration();
            jarPlugin.setConfiguration(config);
        }
        POMExtensibilityElement manifest = getOrCreateChild(getOrCreateChild(config, "achive"), "manifest");
        getOrCreateChild(manifest, "addClasspath").setElementText("true");
        getOrCreateChild(manifest, "classpathPrefix").setElementText("lib"); //NOI18N
        getOrCreateChild(manifest, "mainClass").setElementText(val); //NOI18N
        return jarPlugin;
    }

    /**
     * @deprecated 
     */
    private @Deprecated Plugin checkAssemblyPlugin(Plugin assPlugin) {
        POMModel model = handle.getPOMModel();
        if (assPlugin == null) {
            assPlugin = model.getFactory().createPlugin();
            assPlugin.setArtifactId(ARITFACTID_ASSEMBLY); 
            assPlugin.setGroupId(Constants.GROUP_APACHE_PLUGINS); 
            assPlugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_ASSEMBLY));
            BuildBase base = handle.getNetbeansPublicProfile().getBuildBase();
            if (base == null) {
                base = handle.getPOMModel().getFactory().createBuildBase();
                handle.getNetbeansPublicProfile().setBuildBase(base);
            }
            base.addPlugin(assPlugin);
        }
        PluginExecution exec = assPlugin.findExecutionById("nb"); //NOI18N
        if (exec == null) {
            exec = model.getFactory().createExecution();
            exec.setId("nb"); //NOI18N
            assPlugin.addExecution(exec);
        }
        exec.setPhase("package"); //NOI18N
        List<String> goals = exec.getGoals();
        if (goals == null || !goals.contains("directory-single")) {
            exec.addGoal("directory-single"); //NOI18N
        }

        Configuration config = assPlugin.getConfiguration();
        if (config == null) {
            config = handle.getPOMModel().getFactory().createConfiguration();
            assPlugin.setConfiguration(config);
        }

        getOrCreateChild(config, "descriptor").setElementText("${basedir}/src/main/assemblies/netbeans-run.xml"); //NOI18N
        getOrCreateChild(config, "finalName").setElementText("executable"); //NOI18N
        
        return assPlugin;
    }
    
    private POMExtensibilityElement getOrCreateChild(POMComponent parent, String name) {
        List<POMExtensibilityElement> childs = parent.getChildren(POMExtensibilityElement.class);
        if (childs == null) {
            for (POMExtensibilityElement el : childs) {
                if (name.equals(el.getQName().getLocalPart())) {
                    return el;
                }
            }
        }
        POMExtensibilityElement el = handle.getPOMModel().getFactory().createPOMExtensibilityElement(POMQName.createQName(name));
        parent.addExtensibilityElement(el);
        return el;
    }

    /**
     * used by quickrun configuration.
     * @param argline
     * @return
     */
    public static String[] splitAll(String argline) {
        String jvm = splitJVMParams(argline);
        String mainClazz = splitMainClass(argline);
        String args = splitParams(argline);
        if (jvm != null && jvm.contains("-classpath %classpath")) {
            jvm = jvm.replace("-classpath %classpath", "");
        }
        if (mainClazz != null && mainClazz.equals("${packageClassName}")) {
                    mainClazz = "";
        }
        return new String[] {
            (jvm != null ? jvm : ""),
            (mainClazz != null ? mainClazz : ""),
            (args != null ? args : "")
        };
    }
    
    static String splitJVMParams(String line) {
        String[] splitted = line.split(" "); //NOI18N
        String jvms = "";
        for (String s : splitted) {
            if (s.startsWith("-") || s.contains("%classpath")) { //NOI18N
                jvms = jvms + " " + s;
            } else if (s.equals("${packageClassName}") || s.matches("[\\w]+[\\.]{0,1}[\\w\\.]*")) { //NOI18N
                break;
            }
        }
        return jvms.trim();
    }
    
    static String splitMainClass(String line) {
        String[] splitted = line.split(" ");
        for (String s : splitted) {
            if (s.startsWith("-") || s.contains("%classpath")) { //NOI18N
                continue;
            } else if (s.equals("${packageClassName}") || s.matches("[\\w]+[\\.]{0,1}[\\w\\.]*")) { //NOI18N
                return s;
            } else {
                Logger.getLogger(RunJarPanel.class.getName()).fine("failed splitting main class from=" + line); //NOI18N
            }
        }
        return ""; //NOI18N
    }
    
    static String splitParams(String line) {
        String main = splitMainClass(line);
        int i = line.indexOf(main);
        if (i > -1) {
            return line.substring(i + main.length()).trim();
        }
        return "";
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMainClass;
    private javax.swing.JButton btnWorkDir;
    private javax.swing.JComboBox comConfiguration;
    private javax.swing.JLabel lblArguments;
    private javax.swing.JLabel lblConfiguration;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblMainClass;
    private javax.swing.JLabel lblVMOptions;
    private javax.swing.JLabel lblWorkDir;
    private javax.swing.JTextField txtArguments;
    private javax.swing.JTextField txtMainClass;
    private javax.swing.JTextField txtVMOptions;
    private javax.swing.JTextField txtWorkDir;
    // End of variables declaration//GEN-END:variables

    private void setupConfigurations() {
        if (handle.isConfigurationsEnabled()) {
            lblConfiguration.setVisible(true);
            comConfiguration.setVisible(true);
            DefaultComboBoxModel comModel = new DefaultComboBoxModel();
            for (ModelHandle.Configuration conf : handle.getConfigurations()) {
                comModel.addElement(conf);
            }
            comConfiguration.setModel(comModel);
            comConfiguration.setSelectedItem(handle.getActiveConfiguration());
        } else {
            lblConfiguration.setVisible(false);
            comConfiguration.setVisible(false);
            DefaultComboBoxModel comModel = new DefaultComboBoxModel();
            comConfiguration.setModel(comModel);
        }
    }
    // End of variables declaration

        // Innercasses -------------------------------------------------------------
    
    private class MainClassListener implements ActionListener /*, DocumentListener */ {
        
        private final JButton okButton;
        private FileObject[] sourceRoots;
        private JTextField mainClassTextField;
        
        MainClassListener( FileObject[] sourceRoots, JTextField mainClassTextField ) {            
            this.sourceRoots = sourceRoots;
            this.mainClassTextField = mainClassTextField;
            this.okButton  = new JButton (NbBundle.getMessage (RunJarPanel.class, "LBL_ChooseMainClass_OK"));
            this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (RunJarPanel.class, "AD_ChooseMainClass_OK"));
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) {
            
            // only chooseMainClassButton can be performed
            
            final MainClassChooser panel = new MainClassChooser (sourceRoots);
            Object[] options = new Object[] {
                okButton,
                DialogDescriptor.CANCEL_OPTION
            };
            panel.addChangeListener (new ChangeListener () {
               public void stateChanged(ChangeEvent e) {
                   if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                       // click button and finish the dialog with selected class
                       okButton.doClick ();
                   } else {
                       okButton.setEnabled (panel.getSelectedMainClass () != null);
                   }
               }
            });
            okButton.setEnabled (false);
            DialogDescriptor desc = new DialogDescriptor (
                panel,
                NbBundle.getMessage (RunJarPanel.class, "LBL_ChooseMainClass_Title" ),
                true, 
                options, 
                options[0], 
                DialogDescriptor.BOTTOM_ALIGN, 
                null, 
                null);
            //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() == options[0]) {
               mainClassTextField.setText (panel.getSelectedMainClass ());
            } 
            dlg.dispose();
        }
        
    }
}
