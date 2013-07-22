/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.ActionMappings;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.NbGlobalActionGoalProvider;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.maven.options.Bundle.*;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle.Messages;

/**
 * The visual panel that displays in the Options dialog. Some properties
 * are written to the settings file, some into the Netbeans settings..
 * @author  mkleint
 */
@OptionsPanelController.Keywords(keywords={"maven"}, location=JavaOptions.JAVA, tabTitle= "#TIT_Maven_Category")
public class SettingsPanel extends javax.swing.JPanel {
    private static final String SEPARATOR = "SEPARATOR";
    public  static final String BUNDLED_RUNTIME_VERSION =
            MavenSettings.getCommandLineMavenVersion(EmbedderFactory.getDefaultMavenHome());
    public static final int RUNTIME_COUNT_LIMIT = 5;
    private boolean changed;
    private boolean valid;
    private final ActionListener listener;
    private final MavenOptionController controller;
    private final TextValueCompleter completer;
    private final ActionListener   listItemChangedListener;
    private final List<String>       userDefinedMavenRuntimes = new ArrayList<String>();
    private final List<String>       predefinedRuntimes = new ArrayList<String>();
    private final DefaultComboBoxModel mavenHomeDataModel = new DefaultComboBoxModel();
    private String             mavenRuntimeHome = null;
    private int                lastSelected = -1;
    private final static RequestProcessor RP = new RequestProcessor(SettingsPanel.class);

    private static class ComboBoxRenderer extends DefaultListCellRenderer {

        private final JSeparator separator;

        public ComboBoxRenderer() {
            super();
            separator = new JSeparator(JSeparator.HORIZONTAL);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (SEPARATOR.equals(value)) {
                return separator;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    };

    /** Creates new form SettingsPanel */
    @Messages({
        "CAT_Dependencies=Dependencies",
        "CAT_Appearance=Appearance",
        "CAT_Index=Index",
        "CAT_Execution=Execution"
    })
    SettingsPanel(MavenOptionController controller) {
        initComponents();

        MavenSettings.DownloadStrategy[] downloads = MavenSettings.DownloadStrategy.values();
        comBinaries.setModel(new DefaultComboBoxModel(downloads));
        comJavadoc.setModel(new DefaultComboBoxModel(downloads));
        comSource.setModel(new DefaultComboBoxModel(downloads));
        comMavenHome.setModel(mavenHomeDataModel);

        ListCellRenderer rend = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String txt = ""; //NOI18N
                if (value.equals(MavenSettings.DownloadStrategy.NEVER)) {
                    txt = org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_NEVER");
                } else if (value.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
                    txt = org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_EVERY");
                } else if (value.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
                    txt = org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_FIRST");
                }
                return super.getListCellRendererComponent(list, txt, index, isSelected, cellHasFocus);
            }
        };
        comBinaries.setRenderer(rend);
        comSource.setRenderer(rend);
        comJavadoc.setRenderer(rend);
        comMavenHome.setRenderer(new ComboBoxRenderer());

        this.controller = controller;
        listItemChangedListener = new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SEPARATOR.equals(comMavenHome.getSelectedItem())) {
                    comMavenHome.setSelectedIndex(lastSelected);
                    return;
                }
                
                int selected = comMavenHome.getSelectedIndex();
                if (selected == mavenHomeDataModel.getSize() - 1) {
                    // browse
                    comMavenHome.setSelectedIndex(lastSelected);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            browseAddNewRuntime();
                        }
                        
                    });
                    return;
                }
                
                listDataChanged();
                lastSelected = selected;
            }
        };
        initValues();
        listener = new ActionListenerImpl();
        comIndex.addActionListener(listener);
        completer = new TextValueCompleter(getGlobalOptions(), txtOptions, " "); //NOI18N
        cbProjectNodeNameMode.addActionListener(listener);
        txtProjectNodeNameCustomPattern.setVisible(false);
        lstCategory.setSelectedIndex(0);
        lstCategory.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if ("dependencies".equals(value)) {
                    value = CAT_Dependencies();
                } else if ("appearance".equals(value)) {
                    value = CAT_Appearance();
                } else if ("index".equals(value)) {
                    value = CAT_Index();
                }else if ("execution".equals(value)) {
                    value = CAT_Execution();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }

    /** XXX update for M3 from {@link org.apache.maven.cli.CLIManager#CLIManager} */
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
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "WORK_OFFLINE"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "PRODUCE_EXECUTION_DEBUG_OUTPUT"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "PRODUCE_EXECUTION_ERROR_MESSAGES"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "NON-INTERACTIVE_MODE."),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "STOP_AT_FIRST_FAILURE"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "ONLY_FAIL_THE_BUILD_AFTERWARDS"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "NEVER_FAIL_THE_BUILD"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FAIL_CHECKSUMS"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "WARN_CHECKSUMS"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FORCE_UPTODATE_CHECK"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SUPPRESS_UPTODATE_CHECK"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "FORCES_A_CHECK"),
            org.openide.util.NbBundle.getMessage(SettingsPanel.class, "DON'T_USE_PLUGIN-REGISTRY")
        };
    }

    private static List<String> getGlobalOptions() {
        return Arrays.asList(AVAILABLE_OPTIONS);
    }

    private void initValues() {
        comIndex.setSelectedIndex(0);
    }
    
    private String getSelectedRuntime(int selected) {
        if (selected < 0) {
            return null;
        }

        if (selected < predefinedRuntimes.size()) {
            return predefinedRuntimes.get(selected);
    
        } else if (!userDefinedMavenRuntimes.isEmpty() &&
                selected - predefinedRuntimes.size() <= userDefinedMavenRuntimes.size()) {
            return userDefinedMavenRuntimes.get(selected - 1 - predefinedRuntimes.size());
        }
        
        return null;
    }
    
    private void listDataChanged() {
        changed = true;
        boolean oldvalid = valid;
        int selected = comMavenHome.getSelectedIndex();
        String path = getSelectedRuntime(selected);
        if (path != null) {
            path = path.trim();
            if ("".equals(path)) {
                path = null;
                valid = true;
                lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_ExMavenVersion2", BUNDLED_RUNTIME_VERSION));
            }
        }

        if (path != null) {
            path = path.trim();
            File fil = new File(path);
            String ver = null;
            if (fil.exists() && new File(fil, "bin" + File.separator + "mvn").exists()) { //NOI18N
                ver = MavenSettings.getCommandLineMavenVersion(new File(path));
            }

            if (ver != null) {
                lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "LBL_ExMavenVersion2", ver));
                valid = true;

            } else {
                lblExternalVersion.setText(NbBundle.getMessage(SettingsPanel.class, "ERR_NoValidInstallation"));
            }
        }

        mavenRuntimeHome = path;
        if (oldvalid != valid) {
            controller.firePropChange(MavenOptionController.PROP_VALID, Boolean.valueOf(oldvalid), Boolean.valueOf(valid));
        }
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

        pnlCards = new javax.swing.JPanel();
        pnlExecution = new javax.swing.JPanel();
        lblCommandLine = new javax.swing.JLabel();
        comMavenHome = new javax.swing.JComboBox();
        lblExternalVersion = new javax.swing.JLabel();
        lblOptions = new javax.swing.JLabel();
        txtOptions = new javax.swing.JTextField();
        btnOptions = new javax.swing.JButton();
        cbSkipTests = new javax.swing.JCheckBox();
        btnGoals = new javax.swing.JButton();
        cbAlwaysShow = new javax.swing.JCheckBox();
        cbReuse = new javax.swing.JCheckBox();
        cbCollapseSuccessFolds = new javax.swing.JCheckBox();
        pnlAppearance = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbProjectNodeNameMode = new javax.swing.JComboBox();
        txtProjectNodeNameCustomPattern = new javax.swing.JTextField();
        pnlDependencies = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblBinaries = new javax.swing.JLabel();
        comBinaries = new javax.swing.JComboBox();
        lblJavadoc = new javax.swing.JLabel();
        comJavadoc = new javax.swing.JComboBox();
        lblSource = new javax.swing.JLabel();
        comSource = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        pnlIndex = new javax.swing.JPanel();
        lblIndex = new javax.swing.JLabel();
        comIndex = new javax.swing.JComboBox();
        btnIndex = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCategory = new javax.swing.JList();
        lblCategory = new javax.swing.JLabel();

        pnlCards.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblCommandLine, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblCommandLine.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblOptions, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblOptions.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnOptions, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnOptions.text")); // NOI18N
        btnOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbSkipTests, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbSkipTests.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnGoals, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnGoals.text")); // NOI18N
        btnGoals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoalsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbAlwaysShow, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbAlwaysShow.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbReuse, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbReuse.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCollapseSuccessFolds, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.cbCollapseSuccessFolds.text")); // NOI18N

        javax.swing.GroupLayout pnlExecutionLayout = new javax.swing.GroupLayout(pnlExecution);
        pnlExecution.setLayout(pnlExecutionLayout);
        pnlExecutionLayout.setHorizontalGroup(
            pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExecutionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                        .addComponent(lblCommandLine)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblExternalVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(comMavenHome, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                        .addComponent(lblOptions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOptions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOptions))
                    .addGroup(pnlExecutionLayout.createSequentialGroup()
                        .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnGoals)
                            .addComponent(cbAlwaysShow)
                            .addComponent(cbReuse)
                            .addComponent(cbSkipTests)
                            .addComponent(cbCollapseSuccessFolds))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlExecutionLayout.setVerticalGroup(
            pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExecutionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCommandLine)
                    .addComponent(comMavenHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblExternalVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addGroup(pnlExecutionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOptions)
                    .addComponent(txtOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOptions))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbSkipTests)
                .addGap(18, 18, 18)
                .addComponent(btnGoals)
                .addGap(18, 18, 18)
                .addComponent(cbReuse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAlwaysShow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbCollapseSuccessFolds)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        pnlCards.add(pnlExecution, "execution");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jLabel4.text")); // NOI18N

        cbProjectNodeNameMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "${project.artifactId}", "${project.artifactId}-TRUNK", "${project.artifactId}-${project.version}", "${project.groupId}.${project.artifactId}", "${project.groupId}.${project.artifactId}-${project.version}", "Custom..." }));
        cbProjectNodeNameMode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbProjectNodeNameModeItemStateChanged(evt);
            }
        });
        cbProjectNodeNameMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProjectNodeNameModeActionPerformed(evt);
            }
        });

        txtProjectNodeNameCustomPattern.setText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.txtProjectNodeNameCustomPattern.text")); // NOI18N
        txtProjectNodeNameCustomPattern.setToolTipText(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.txtProjectNodeNameCustomPattern.toolTipText")); // NOI18N

        javax.swing.GroupLayout pnlAppearanceLayout = new javax.swing.GroupLayout(pnlAppearance);
        pnlAppearance.setLayout(pnlAppearanceLayout);
        pnlAppearanceLayout.setHorizontalGroup(
            pnlAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAppearanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(pnlAppearanceLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtProjectNodeNameCustomPattern)
                            .addComponent(cbProjectNodeNameMode, 0, 290, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlAppearanceLayout.setVerticalGroup(
            pnlAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppearanceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAppearanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbProjectNodeNameMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtProjectNodeNameCustomPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(243, Short.MAX_VALUE))
        );

        pnlCards.add(pnlAppearance, "appearance");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jLabel1.text")); // NOI18N

        lblBinaries.setLabelFor(comBinaries);
        org.openide.awt.Mnemonics.setLocalizedText(lblBinaries, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblBinaries.text")); // NOI18N

        lblJavadoc.setLabelFor(comJavadoc);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavadoc, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblJavadoc.text")); // NOI18N

        lblSource.setLabelFor(comSource);
        org.openide.awt.Mnemonics.setLocalizedText(lblSource, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblSource.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.jLabel3.text")); // NOI18N
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout pnlDependenciesLayout = new javax.swing.GroupLayout(pnlDependencies);
        pnlDependencies.setLayout(pnlDependenciesLayout);
        pnlDependenciesLayout.setHorizontalGroup(
            pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDependenciesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDependenciesLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlDependenciesLayout.createSequentialGroup()
                        .addComponent(lblJavadoc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comJavadoc, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDependenciesLayout.createSequentialGroup()
                        .addComponent(lblBinaries)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comBinaries, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDependenciesLayout.createSequentialGroup()
                        .addComponent(lblSource)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comSource, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
                .addContainerGap())
        );

        pnlDependenciesLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblBinaries, lblJavadoc, lblSource});

        pnlDependenciesLayout.setVerticalGroup(
            pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDependenciesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBinaries)
                    .addComponent(comBinaries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJavadoc)
                    .addComponent(comJavadoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDependenciesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSource)
                    .addComponent(comSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(161, Short.MAX_VALUE))
        );

        pnlCards.add(pnlDependencies, "dependencies");

        lblIndex.setLabelFor(comIndex);
        org.openide.awt.Mnemonics.setLocalizedText(lblIndex, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblIndex.text")); // NOI18N

        comIndex.setModel(createComboModel());

        org.openide.awt.Mnemonics.setLocalizedText(btnIndex, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.btnIndex.text")); // NOI18N
        btnIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIndexActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlIndexLayout = new javax.swing.GroupLayout(pnlIndex);
        pnlIndex.setLayout(pnlIndexLayout);
        pnlIndexLayout.setHorizontalGroup(
            pnlIndexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlIndexLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblIndex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comIndex, 0, 164, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnIndex)
                .addContainerGap())
        );
        pnlIndexLayout.setVerticalGroup(
            pnlIndexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlIndexLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlIndexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIndex)
                    .addComponent(comIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIndex))
                .addContainerGap(297, Short.MAX_VALUE))
        );

        pnlCards.add(pnlIndex, "index");

        lstCategory.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "execution", "appearance", "dependencies", "index" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstCategory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstCategory.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstCategoryValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstCategory);

        lblCategory.setLabelFor(lstCategory);
        org.openide.awt.Mnemonics.setLocalizedText(lblCategory, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "SettingsPanel.lblCategory.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblCategory)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIndexActionPerformed
        btnIndex.setEnabled(false);
        new RequestProcessor("Maven Repo Index Transfer/Scan").post(new Runnable() {
            @Override
            public void run() {
                //TODO shall we iterate all "local" repositories??
                RepositoryIndexer.indexRepo(RepositoryPreferences.getInstance().getLocalRepository());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        btnIndex.setEnabled(true);
                    }
                });
            }
        });
    }//GEN-LAST:event_btnIndexActionPerformed
    
    private void btnGoalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoalsActionPerformed
        NbGlobalActionGoalProvider provider = Lookup.getDefault().lookup(NbGlobalActionGoalProvider.class);
        assert provider != null;
        try {
            ActionToGoalMapping mappings = new NetbeansBuildActionXpp3Reader().read(new StringReader(provider.getRawMappingsAsString()));
            ActionMappings panel = new ActionMappings(mappings);
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SettingsPanel.class, "ACSD_Global"));
            DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Global"));
            Object retVal = DialogDisplayer.getDefault().notify(dd);
            if (retVal == DialogDescriptor.OK_OPTION) {
                FileObject dir = FileUtil.getConfigFile("Projects/org-netbeans-modules-maven"); //NOI18N
                // just make sure the name of the file is always nbactions.xml
                CustomizerProviderImpl.writeNbActionsModel(dir, mappings, M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
                panel.applyToolbarChanges();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnGoalsActionPerformed

    private void btnOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptionsActionPerformed
        GlobalOptionsPanel pnl = new GlobalOptionsPanel();
        DialogDescriptor dd = new DialogDescriptor(pnl, org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Add_Globals"));
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            txtOptions.setText(txtOptions.getText() + pnl.getSelectedOnes());
        }

    }//GEN-LAST:event_btnOptionsActionPerformed

    private void cbProjectNodeNameModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProjectNodeNameModeActionPerformed
        txtProjectNodeNameCustomPattern.setVisible(cbProjectNodeNameMode.getSelectedIndex()==cbProjectNodeNameMode.getItemCount()-1);
    }//GEN-LAST:event_cbProjectNodeNameModeActionPerformed

    private void cbProjectNodeNameModeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbProjectNodeNameModeItemStateChanged
    }//GEN-LAST:event_cbProjectNodeNameModeItemStateChanged

    private void lstCategoryValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstCategoryValueChanged
        CardLayout cl = (CardLayout) pnlCards.getLayout();
        cl.show(pnlCards, (String) lstCategory.getSelectedValue());
    }//GEN-LAST:event_lstCategoryValueChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGoals;
    private javax.swing.JButton btnIndex;
    private javax.swing.JButton btnOptions;
    private javax.swing.JCheckBox cbAlwaysShow;
    private javax.swing.JCheckBox cbCollapseSuccessFolds;
    private javax.swing.JComboBox cbProjectNodeNameMode;
    private javax.swing.JCheckBox cbReuse;
    private javax.swing.JCheckBox cbSkipTests;
    private javax.swing.JComboBox comBinaries;
    private javax.swing.JComboBox comIndex;
    private javax.swing.JComboBox comJavadoc;
    private javax.swing.JComboBox comMavenHome;
    private javax.swing.JComboBox comSource;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBinaries;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblCommandLine;
    private javax.swing.JLabel lblExternalVersion;
    private javax.swing.JLabel lblIndex;
    private javax.swing.JLabel lblJavadoc;
    private javax.swing.JLabel lblOptions;
    private javax.swing.JLabel lblSource;
    private javax.swing.JList lstCategory;
    private javax.swing.JPanel pnlAppearance;
    private javax.swing.JPanel pnlCards;
    private javax.swing.JPanel pnlDependencies;
    private javax.swing.JPanel pnlExecution;
    private javax.swing.JPanel pnlIndex;
    private javax.swing.JTextField txtOptions;
    private javax.swing.JTextField txtProjectNodeNameCustomPattern;
    // End of variables declaration//GEN-END:variables
    
    private void browseAddNewRuntime() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(SettingsPanel.class, "TIT_Select2"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        int selected = comMavenHome.getSelectedIndex();
        String path = getSelectedRuntime(selected);
        if (path == null || path.trim().length() == 0) {
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
            String newRuntimePath = FileUtil.normalizeFile(projectDir).getAbsolutePath();
            boolean existed = false;
            List<String> runtimes = new ArrayList<String>();
            runtimes.addAll(predefinedRuntimes);
            runtimes.addAll(userDefinedMavenRuntimes);
            for (String runtime : runtimes) {
                if (runtime.equals(newRuntimePath)) {
                    existed = true;
                }
            }
            if (!existed) {
                // do not add duplicated directory
                if (userDefinedMavenRuntimes.isEmpty()) {
                    mavenHomeDataModel.insertElementAt(SEPARATOR, predefinedRuntimes.size());
                }
                userDefinedMavenRuntimes.add(newRuntimePath);
                mavenHomeDataModel.insertElementAt(newRuntimePath, runtimes.size() + 1);
            }
            comMavenHome.setSelectedItem(newRuntimePath);
        }
    }
    
    @Messages({"MAVEN_RUNTIME_Bundled=Bundled", "MAVEN_RUNTIME_External={0}", "MAVEN_RUNTIME_Browse=Browse..."})
    public void setValues() {
        txtOptions.setText(MavenSettings.getDefault().getDefaultOptions());

        final List<String> predefined = new ArrayList<String>();
        final List<String> user = new ArrayList<String>();
        RP.post(new Runnable() {

            @Override
            public void run() {
                predefined.add("");
                String defaultExternalMavenRuntime = MavenSettings.getDefaultExternalMavenRuntime();
                if (defaultExternalMavenRuntime != null) {
                    predefined.add(defaultExternalMavenRuntime);
                }
                user.addAll(MavenSettings.getDefault().getUserDefinedMavenRuntimes());
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        predefinedRuntimes.clear();
                        userDefinedMavenRuntimes.clear();
                        predefinedRuntimes.addAll(predefined);
                        userDefinedMavenRuntimes.addAll(user);

                        comMavenHome.removeActionListener(listItemChangedListener);
                        mavenHomeDataModel.removeAllElements();
                        File command = EmbedderFactory.getMavenHome();
                        for (String runtime : predefinedRuntimes) {
                            boolean bundledRuntime = runtime.isEmpty();
                            String desc = bundledRuntime ? MAVEN_RUNTIME_Bundled()
                                    : MAVEN_RUNTIME_External(runtime);
                            mavenHomeDataModel.addElement(desc);
                        }

                        if (!userDefinedMavenRuntimes.isEmpty()) {
                            mavenHomeDataModel.addElement(SEPARATOR);
                            for (String runtime : userDefinedMavenRuntimes) {
                                String desc = MAVEN_RUNTIME_External(runtime); // NOI18N
                                mavenHomeDataModel.addElement(desc);
                            }
                        }

                        mavenHomeDataModel.addElement(SEPARATOR);
                        mavenHomeDataModel.addElement(MAVEN_RUNTIME_Browse());
                        comMavenHome.setSelectedItem(command.getAbsolutePath()); //NOI18N
                        listDataChanged();
                        lastSelected = comMavenHome.getSelectedIndex();
                        comMavenHome.addActionListener(listItemChangedListener);
                        changed = false;  //#163955 - do not fire change events on load
                        //listDataChanged() sets changed to true
                    }
                });
            }
        });
        
        comIndex.setSelectedIndex(RepositoryPreferences.getIndexUpdateFrequency());
        comBinaries.setSelectedItem(MavenSettings.getDefault().getBinaryDownloadStrategy());
        comJavadoc.setSelectedItem(MavenSettings.getDefault().getJavadocDownloadStrategy());
        comSource.setSelectedItem(MavenSettings.getDefault().getSourceDownloadStrategy());
        cbSkipTests.setSelected(MavenSettings.getDefault().isSkipTests());
        cbAlwaysShow.setSelected(MavenSettings.getDefault().isAlwaysShowOutput());
        cbReuse.setSelected(MavenSettings.getDefault().isReuseOutputTabs());
        cbCollapseSuccessFolds.setSelected(MavenSettings.getDefault().isCollapseSuccessFolds());

        final String pattern = MavenSettings.getDefault().getProjectNodeNamePattern();
        txtProjectNodeNameCustomPattern.setText("");
        if (null == pattern || pattern.isEmpty()) {
            //default
            cbProjectNodeNameMode.setSelectedIndex(0);
        } else {
            //ignore the "default" entry
            //ignore the "custom..." entry
            final int start = 1;
            final int end = cbProjectNodeNameMode.getItemCount() - 1;
        
            boolean foundPredefinedPattern = false;
            for (int i = start; i < end; i++) {
                final Object itemAt = cbProjectNodeNameMode.getItemAt(i);
                if (pattern.equals(itemAt)) {
                    cbProjectNodeNameMode.setSelectedIndex(i);
                    foundPredefinedPattern = true;
                    break;
                }
            }
            if (!foundPredefinedPattern) {
                //set mode to custom
                cbProjectNodeNameMode.setSelectedIndex(cbProjectNodeNameMode.getItemCount() - 1);
                txtProjectNodeNameCustomPattern.setText(pattern);
            }
        }
        
        changed = false;  //#163955 - do not fire change events on load
    }
    
    public void applyValues() {
        MavenSettings.getDefault().setDefaultOptions(txtOptions.getText().trim());
        
        // remember only user-defined runtimes of RUNTIME_COUNT_LIMIT count at the most
        List<String> runtimes = new ArrayList<String>();
        for (int i = 0; i < userDefinedMavenRuntimes.size() && i < RUNTIME_COUNT_LIMIT; ++i) {
            runtimes.add(0, userDefinedMavenRuntimes.get(userDefinedMavenRuntimes.size() - 1 - i));
        }
        int selected = comMavenHome.getSelectedIndex() - predefinedRuntimes.size() - 1;
        if (selected >= 0 && runtimes.size() == RUNTIME_COUNT_LIMIT &&
                userDefinedMavenRuntimes.size() - RUNTIME_COUNT_LIMIT > selected) {
            runtimes.set(0, userDefinedMavenRuntimes.get(selected));
        }
        if (predefinedRuntimes.size() > 1) {
            runtimes.add(0, predefinedRuntimes.get(1));
        }
        MavenSettings.getDefault().setMavenRuntimes(runtimes);
        String cl = mavenRuntimeHome;
        //MEVENIDE-553
        File command = (cl == null || cl.isEmpty()) ? null : new File(cl);
        if (command != null && command.isDirectory()) {
            EmbedderFactory.setMavenHome(command);
        } else {
            EmbedderFactory.setMavenHome(null);
        }
        RepositoryPreferences.setIndexUpdateFrequency(comIndex.getSelectedIndex());
        MavenSettings.getDefault().setBinaryDownloadStrategy((MavenSettings.DownloadStrategy) comBinaries.getSelectedItem());
        MavenSettings.getDefault().setJavadocDownloadStrategy((MavenSettings.DownloadStrategy) comJavadoc.getSelectedItem());
        MavenSettings.getDefault().setSourceDownloadStrategy((MavenSettings.DownloadStrategy) comSource.getSelectedItem());
        MavenSettings.getDefault().setSkipTests(cbSkipTests.isSelected());
        MavenSettings.getDefault().setAlwaysShowOutput(cbAlwaysShow.isSelected());
        MavenSettings.getDefault().setReuseOutputTabs(cbReuse.isSelected());
        MavenSettings.getDefault().setCollapseSuccessFolds(cbCollapseSuccessFolds.isSelected());
        
        if (0 == cbProjectNodeNameMode.getSelectedIndex()) {
            //selected "default" entry
            MavenSettings.getDefault().setProjectNodeNamePattern(null);
        } else if (cbProjectNodeNameMode.getSelectedIndex() == cbProjectNodeNameMode.getItemCount() - 1) {
            //selected "custom..." entry
            MavenSettings.getDefault().setProjectNodeNamePattern(txtProjectNodeNameCustomPattern.getText());
        } else {
            //a predefined pattern entry was selected
            MavenSettings.getDefault().setProjectNodeNamePattern(cbProjectNodeNameMode.getSelectedItem().toString());
        } 
        changed = false;
    }
    
    boolean hasValidValues() {
        return valid;
    }
    
    boolean hasChangedValues() {
        return changed;
    }
    
    private class ActionListenerImpl implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            changed = true;
        }
        
    }
}
