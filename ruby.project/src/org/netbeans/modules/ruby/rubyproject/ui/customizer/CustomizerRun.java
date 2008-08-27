/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.rubyproject.ui.customizer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.Collator;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory;
import org.netbeans.modules.ruby.platform.RubyPlatformCustomizer;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.SourceRoots;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CustomizerRun extends JPanel implements HelpCtx.Provider {
    
    private final RubyProject project;
    
    private final JTextField[] configFields;
    private final String[] configPropsKeys;
    
    private final Map<String, Map<String, String>> configs;
    private final RubyProjectProperties uiProperties;
    private PlatformComponentFactory.PlatformChangeListener platformListener;

    public CustomizerRun(RubyProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        initComponents();

        project = uiProperties.getRubyProject();
        configs = uiProperties.getRunConfigs();
        
        configFields = new JTextField[] {
            jTextFieldMainClass,
            jTextFieldArgs,
            rubyOptions,
            jTextWorkingDirectory,
            rakeTextField,
            jrubyPropsText,
        };
        JLabel[] configLabels = new JLabel[]{
            jLabelMainClass,
            jLabelArgs,
            rubyOptionsLabel,
            jLabelWorkingDirectory,
            rakeLabel,
            jrubyPropsLabel
        };
        configPropsKeys = new String[] {
            RubyProjectProperties.MAIN_CLASS,
            RubyProjectProperties.APPLICATION_ARGS,
            RubyProjectProperties.RUBY_OPTIONS,
            RubyProjectProperties.RUN_WORK_DIR,
            RubyProjectProperties.RAKE_ARGS,
            RubyProjectProperties.JRUBY_PROPS
        
        };
        assert configFields.length == configPropsKeys.length;
        
        configChanged(uiProperties.getActiveConfig());
        
        configCombo.setRenderer(new DefaultListCellRenderer() {
            public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String config = (String) value;
                String label;
                if (config == null) {
                    // uninitialized?
                    label = null;
                } else if (config.length() > 0) {
                    Map<String, String> m = configs.get(config);
                    label = m != null ? m.get("$label") : /* temporary? */ null; // NOI18N
                    if (label == null) {
                        label = config;
                    }
                } else {
                    label = NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.default");
                }
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        });
        
        for (int i = 0; i < configFields.length; i++) {
            configFields[i].getDocument().addDocumentListener(new ConfigChangeListener(
                    configPropsKeys[i], configLabels[i], configFields[i]));
        }

        jButtonMainClass.addActionListener(new MainClassListener(project.getSourceRoots(), jTextFieldMainClass));
        platforms.setSelectedItem(uiProperties.getPlatform());
        updateEnabled();
    }

    private String getSelectedConfig() {
        String config = (String) configCombo.getSelectedItem();
        if (config.length() == 0) {
            config = null;
        }
        return config;
    }

    private void updateEnabled() {
        RubyPlatform platform = uiProperties.getPlatform();
        boolean irJRuby = platform != null && platform.isJRuby();
        jrubyPropsExample.setEnabled(irJRuby);
        jrubyPropsLabel.setEnabled(irJRuby);
        jrubyPropsText.setEnabled(irJRuby);
    }

    public @Override void addNotify() {
        super.addNotify();
        platformListener = new PlatformComponentFactory.PlatformChangeListener() {
            public void platformChanged() {
                RubyPlatform platform = (RubyPlatform) platforms.getSelectedItem();
                if (platform != null) {
                    uiProperties.setPlatform(platform);
                    configs.get(getSelectedConfig()).put(RubyProjectProperties.PLATFORM_ACTIVE, platform.getID());
                    updateEnabled();
                }
            }
        };
        PlatformComponentFactory.addPlatformChangeListener(platforms, platformListener);
    }
    
    public @Override void removeNotify() {
        PlatformComponentFactory.removePlatformChangeListener(platforms, platformListener);
        super.removeNotify();
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configSep = new javax.swing.JSeparator();
        configPanel = new javax.swing.JPanel();
        mainPanel = new javax.swing.JPanel();
        jLabelMainClass = new javax.swing.JLabel();
        jTextFieldMainClass = new javax.swing.JTextField();
        jButtonMainClass = new javax.swing.JButton();
        jLabelArgs = new javax.swing.JLabel();
        jTextFieldArgs = new javax.swing.JTextField();
        jLabelWorkingDirectory = new javax.swing.JLabel();
        jTextWorkingDirectory = new javax.swing.JTextField();
        jButtonWorkingDirectoryBrowse = new javax.swing.JButton();
        rubyOptionsLabel = new javax.swing.JLabel();
        rubyOptions = new javax.swing.JTextField();
        jLabelVMOptionsExample = new javax.swing.JLabel();
        rakeLabel = new javax.swing.JLabel();
        rakeTextField = new javax.swing.JTextField();
        rakeExampleLabel = new javax.swing.JLabel();
        jrubyPropsLabel = new javax.swing.JLabel();
        jrubyPropsText = new javax.swing.JTextField();
        jrubyPropsExample = new javax.swing.JLabel();
        rubyPlatformLabel = new javax.swing.JLabel();
        platforms = org.netbeans.modules.ruby.platform.PlatformComponentFactory.getRubyPlatformsComboxBox();
        manageButton = new javax.swing.JButton();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        configDel = new javax.swing.JButton();
        configNew = new javax.swing.JButton();

        configPanel.setLayout(new java.awt.GridBagLayout());

        jLabelMainClass.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JButton")); // NOI18N

        jLabelArgs.setLabelFor(jTextFieldArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N

        jLabelWorkingDirectory.setLabelFor(jTextWorkingDirectory);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelWorkingDirectory, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonWorkingDirectoryBrowse, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse")); // NOI18N
        jButtonWorkingDirectoryBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWorkingDirectoryBrowseActionPerformed(evt);
            }
        });

        rubyOptionsLabel.setLabelFor(rubyOptions);
        org.openide.awt.Mnemonics.setLocalizedText(rubyOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N

        rakeLabel.setLabelFor(rakeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RakeArgs")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rakeExampleLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RakeArgsEx")); // NOI18N

        jrubyPropsLabel.setLabelFor(rubyOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jrubyPropsLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.jrubyPropsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jrubyPropsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.jrubyPropsExample.text")); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jrubyPropsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabelWorkingDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, Short.MAX_VALUE)
                    .add(rakeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                    .add(rubyOptionsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabelArgs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabelMainClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabelVMOptionsExample)
                            .add(rakeExampleLabel)
                            .add(jrubyPropsExample)
                            .add(rakeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                            .add(jrubyPropsText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)))
                    .add(jTextFieldArgs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .add(rubyOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .add(jTextFieldMainClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .add(jTextWorkingDirectory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE))
                .add(6, 6, 6)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jButtonMainClass)
                    .add(jButtonWorkingDirectoryBrowse)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelMainClass)
                    .add(jButtonMainClass)
                    .add(jTextFieldMainClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelArgs)
                    .add(jTextFieldArgs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelWorkingDirectory)
                    .add(jButtonWorkingDirectoryBrowse)
                    .add(jTextWorkingDirectory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rubyOptionsLabel)
                    .add(rubyOptions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(jLabelVMOptionsExample)
                .add(9, 9, 9)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rakeLabel)
                    .add(rakeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rakeExampleLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jrubyPropsLabel)
                    .add(jrubyPropsText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jrubyPropsExample))
        );

        jTextFieldMainClass.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.jTextFieldMainClass.AccessibleContext.accessibleName")); // NOI18N
        jTextFieldMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jTextFieldMainClass")); // NOI18N
        jButtonMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jButtonMainClass")); // NOI18N
        jTextFieldArgs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.jTextFieldArgs.AccessibleContext.accessibleName")); // NOI18N
        jTextFieldArgs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jTextFieldArgs")); // NOI18N
        jTextWorkingDirectory.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.jTextWorkingDirectory.AccessibleContext.accessibleName")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/ruby/rubyproject/ui/customizer/Bundle"); // NOI18N
        jTextWorkingDirectory.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizeRun_Run_Working_Directory")); // NOI18N
        jButtonWorkingDirectoryBrowse.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizeRun_Run_Working_Directory_Browse")); // NOI18N
        rubyOptions.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.jTextVMOptions.AccessibleContext.accessibleName")); // NOI18N
        rubyOptions.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizeRun_Run_VM_Options")); // NOI18N
        jLabelVMOptionsExample.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N
        rakeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.rakeLabel.AccessibleContext.accessibleDescription")); // NOI18N
        rakeTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.rakeTextField.AccessibleContext.accessibleName")); // NOI18N

        rubyPlatformLabel.setLabelFor(platforms);
        org.openide.awt.Mnemonics.setLocalizedText(rubyPlatformLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RubyPlatformLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RubyHomeBrowse")); // NOI18N
        manageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageButtonActionPerformed(evt);
            }
        });

        configLabel.setLabelFor(configCombo);
        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel")); // NOI18N

        configCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default>" }));
        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configDelete")); // NOI18N
        configDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew")); // NOI18N
        configNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, configPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 410, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rubyPlatformLabel)
                            .add(configLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, platforms, 0, 452, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, configCombo, 0, 452, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(configNew)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(configDel))
                            .add(manageButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(configSep, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rubyPlatformLabel)
                    .add(platforms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(manageButton))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(configLabel)
                            .add(configCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(configDel)
                            .add(configNew))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configSep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rubyPlatformLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.rubyPlatformLabel.AccessibleContext.accessibleDescription")); // NOI18N
        manageButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.manageButton.AccessibleContext.accessibleDescription")); // NOI18N
        configLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel.AccessibleContext.accessibleDescription")); // NOI18N
        configNew.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
        String config = getSelectedConfig();
        assert config != null;
        configs.put(config, null);
        configChanged(null);
        uiProperties.setActiveConfig(null);
    }//GEN-LAST:event_configDelActionPerformed

    private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
        String config = CustomizerSupport.askForNewConfiguration(configs);
        configChanged(config);
        uiProperties.setActiveConfig(config);
    }//GEN-LAST:event_configNewActionPerformed

    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        String config = getSelectedConfig();
        configChanged(config);
        uiProperties.setActiveConfig(config);
    }//GEN-LAST:event_configComboActionPerformed

    private void jButtonWorkingDirectoryBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWorkingDirectoryBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = jTextWorkingDirectory.getText();
        if (workDir.equals("")) {
            workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse_Title"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextWorkingDirectory.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonWorkingDirectoryBrowseActionPerformed

    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        RubyPlatformCustomizer.manage(platforms);
    }//GEN-LAST:event_manageButtonActionPerformed

    private void configChanged(String activeConfig) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("");
        SortedSet<String> alphaConfigs = new TreeSet<String>(new Comparator<String>() {
            Collator coll = Collator.getInstance();
            public int compare(String s1, String s2) {
                return coll.compare(label(s1), label(s2));
            }
            private String label(String c) {
                Map<String,String> m = configs.get(c);
                String label = m.get("$label"); // NOI18N
                return label != null ? label : c;
            }
        });
        for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config != null && entry.getValue() != null) {
                alphaConfigs.add(config);
            }
        }
        for (String c : alphaConfigs) {
            model.addElement(c);
        }
        configCombo.setModel(model);
        configCombo.setSelectedItem(activeConfig != null ? activeConfig : "");
        Map<String,String> active = configs.get(activeConfig);
        Map<String,String> def = configs.get(null);
        if (active != null) {
            for (int i = 0; i < configFields.length; i++) {
                String v = active.get(configPropsKeys[i]);
                if (v == null) {
                    // display default value
                    v = def.get(configPropsKeys[i]);
                }
                configFields[i].setText(v);
            }
            String activePlatformID = active.get(RubyProjectProperties.PLATFORM_ACTIVE);
            if (activePlatformID == null) {
                activePlatformID = def.get(RubyProjectProperties.PLATFORM_ACTIVE);
            }
            platforms.setSelectedItem(RubyPlatformManager.getPlatformByID(activePlatformID));
        } // else ??
        configDel.setEnabled(activeConfig != null);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox configCombo;
    private javax.swing.JButton configDel;
    private javax.swing.JLabel configLabel;
    private javax.swing.JButton configNew;
    private javax.swing.JPanel configPanel;
    private javax.swing.JSeparator configSep;
    private javax.swing.JButton jButtonMainClass;
    private javax.swing.JButton jButtonWorkingDirectoryBrowse;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JLabel jLabelWorkingDirectory;
    private javax.swing.JTextField jTextFieldArgs;
    private javax.swing.JTextField jTextFieldMainClass;
    private javax.swing.JTextField jTextWorkingDirectory;
    private javax.swing.JLabel jrubyPropsExample;
    private javax.swing.JLabel jrubyPropsLabel;
    private javax.swing.JTextField jrubyPropsText;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton manageButton;
    private javax.swing.JComboBox platforms;
    private javax.swing.JLabel rakeExampleLabel;
    private javax.swing.JLabel rakeLabel;
    private javax.swing.JTextField rakeTextField;
    private javax.swing.JTextField rubyOptions;
    private javax.swing.JLabel rubyOptionsLabel;
    private javax.swing.JLabel rubyPlatformLabel;
    // End of variables declaration//GEN-END:variables
    
    private class MainClassListener implements ActionListener /*, DocumentListener */ {
        
        private final JButton okButton;
        private final SourceRoots sourceRoots;
        private final JTextField mainClassTextField;
        
        MainClassListener( SourceRoots sourceRoots, JTextField mainClassTextField ) {            
            this.sourceRoots = sourceRoots;
            this.mainClassTextField = mainClassTextField;
            this.okButton  = new JButton (NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_OK"));
            this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (CustomizerRun.class, "AD_ChooseMainClass_OK"));
        }
        
        /** Handles button events */        
        public void actionPerformed( ActionEvent e ) {
            // only chooseMainClassButton can be performed
            final MainClassChooser panel = new MainClassChooser (sourceRoots.getRoots());
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
                NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_Title" ),
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
    
    private class ConfigChangeListener implements DocumentListener {

        private final JTextField field;
        private final JLabel label;
        private final String prop;

        ConfigChangeListener(final String prop, final JLabel label, final JTextField field) {
            this.field = field;
            this.label = label;
            this.prop = prop;
            updateFont();
        }

        public void insertUpdate(DocumentEvent e) {
            changed();
        }

        public void removeUpdate(DocumentEvent e) {
            changed();
        }

        public void changedUpdate(DocumentEvent e) {
        }

        void changed() {
            String config = getSelectedConfig();
            String v = field.getText();
            if (v != null && config != null && v.equals(configs.get(null).get(prop))) {
                // default value, do not store as such
                v = null;
            }
            configs.get(config).put(prop, v);
            updateFont();
        }

        void updateFont() {
            Font basefont = label.getFont();
            Font boldfont = basefont.deriveFont(Font.BOLD);

            String v = field.getText();
            String def = configs.get(null).get(prop);
            label.setFont(getSelectedConfig() != null && !Utilities.compareObjects(v != null ? v : "", def != null ? def : "") ? boldfont : basefont); // NOI18N
        }
    }
}
