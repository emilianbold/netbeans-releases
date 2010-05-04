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

package org.netbeans.modules.ruby.railsprojects.ui.customizer;

import java.nio.charset.Charset;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory.PlatformChangeListener;
import org.netbeans.modules.ruby.platform.RubyPlatformCustomizer;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.server.RailsServerUiUtils;
import org.netbeans.modules.ruby.railsprojects.server.ServerRegistry;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.CustomizerSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CustomizerRun extends JPanel implements HelpCtx.Provider {
    
    private final RailsProject project;
    private String originalEncoding;
    private boolean notified;
    
    private final JTextField[] configFields;
    private final String[] configPropsKeys;
    
    private final Map<String, Map<String, String>> configs;
    private final RailsProjectProperties uiProperties;
    private PlatformChangeListener platformListener;
    
    public CustomizerRun( RailsProjectProperties uiProperties ) {
        this.uiProperties = uiProperties;
        initComponents();

        this.project = uiProperties.getRailsProject();
        configs = uiProperties.getRunConfigs();
        
        configFields = new JTextField[] {
            portField,
            urlTextField,
            rakeTextField,
            serverArgsField
        };
        JLabel[] configLabels = new JLabel[] {
            portLabel,
            urlLabel,
            rakeLabel,
            serverLabel
        };
        configPropsKeys = new String[] {
            RailsProjectProperties.RAILS_PORT,
            RailsProjectProperties.RAILS_URL,
            RailsProjectProperties.RAKE_ARGS,
            RailsProjectProperties.RAILS_SERVER_ARGS
        };
        assert configFields.length == configPropsKeys.length;
        
        configChanged(uiProperties.getActiveConfig());
        
        configCombo.setRenderer(new DefaultListCellRenderer() {
            public @Override Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                String config = (String) value;
                String label;
                if (config == null) {
                    // uninitialized?
                    label = null;
                } else if (config.length() > 0) {
                    Map<String,String> m = configs.get(config);
                    label = m != null ? m.get("$label") : /* temporary? */ null;
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
            final JTextField field = configFields[i];
            final String prop = configPropsKeys[i];
            final JLabel label = configLabels[i];
            field.getDocument().addDocumentListener(new DocumentListener() {
                Font basefont = label.getFont();
                Font boldfont = basefont.deriveFont(Font.BOLD);
                {
                    updateFont();
                }
                public void insertUpdate(DocumentEvent e) {
                    changed();
                }
                public void removeUpdate(DocumentEvent e) {
                    changed();
                }
                public void changedUpdate(DocumentEvent e) {}
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
                    String v = field.getText();
                    String config = getSelectedConfig();
                    String def = configs.get(null).get(prop);
                    label.setFont(config != null && !Utilities.compareObjects(v != null ? v : "", def != null ? def : "") ? boldfont : basefont);
                }
            });
        }

        this.originalEncoding = project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
        if (this.originalEncoding == null) {
            this.originalEncoding = Charset.defaultCharset().name();
        }
        
        this.encoding.setModel(ProjectCustomizer.encodingModel(this.originalEncoding));
        this.encoding.setRenderer(ProjectCustomizer.encodingRenderer());
        
        final String lafid  = UIManager.getLookAndFeel().getID();
        if (!"Aqua".equals(lafid)) { //NOI18N
            this.encoding.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);    //NOI18N
            this.encoding.addItemListener(new java.awt.event.ItemListener() {

                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    JComboBox combo = (JComboBox) e.getSource();
                    combo.setPopupVisible(false);
                }
            });
        }

        this.encoding.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }            
        });
        platforms.setSelectedItem(uiProperties.getPlatform());
        String serverId = project.evaluator().getProperty(RailsProjectProperties.RAILS_SERVERTYPE);
        selectServer(serverId);
        serverComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleServerChanged();
            }
        });
        
        initRailsEnvCombo();
    }
    
    private void handleRailsEnvChanged() {
        String env = (String) railsEnvCombo.getSelectedItem();
        uiProperties.setRailsEnvironment(env, getSelectedConfig());
    }

    private void handleServerChanged() {
        RubyInstance server = (RubyInstance) serverComboBox.getSelectedItem();
        if (server != null) {
            uiProperties.setServer(server, getSelectedConfig());
        }
    }
    
    private void initRailsEnvCombo() {
        //XXX: may need to make dependent on the server combo when the V3 plugin is available
        // search for environments in config/environments
        List<String> environments = new ArrayList<String>();
        FileObject envFolder = project.getProjectDirectory().getFileObject("config/environments"); //NOI18N
        if (envFolder != null) {
            for (FileObject each : envFolder.getChildren()) {
                if (!each.isFolder() && "rb".equals(each.getExt())) { //NOI18N
                    environments.add(each.getName());
                }
            }
        }
        Collections.sort(environments);
        railsEnvCombo.setModel(new DefaultComboBoxModel(environments.toArray(new String[environments.size()]))); //NOI18N
        String definedEnv = project.evaluator().getProperty(RailsProjectProperties.RAILS_ENV);
        selectRailsEnv(definedEnv);
        railsEnvCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleRailsEnvChanged();
            }
        });
    }

    public @Override void addNotify() {
        super.addNotify();
        platformListener = new PlatformChangeListener() {
            public void platformChanged() {
                RubyPlatform platform = (RubyPlatform) platforms.getSelectedItem();
                if (platform != null) {
                    uiProperties.setPlatform(platform);
                    configs.get(getSelectedConfig()).put(RailsProjectProperties.PLATFORM_ACTIVE, platform.getID());
                }
            }
        };
        PlatformComponentFactory.addPlatformChangeListener(platforms, platformListener);
    }
    
    public @Override void removeNotify() {
        PlatformComponentFactory.removePlatformChangeListener(platforms, platformListener);
        super.removeNotify();
    }
    
    private void handleEncodingChange() {
        Charset enc = (Charset)encoding.getSelectedItem();
        String encName;
        if (enc != null) {
            encName = enc.name();
        } else {
            encName = originalEncoding;
        }
        if (!notified && encName != null && !encName.equals(originalEncoding)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(CustomizerRun.class, "MSG_EncodingWarning"), NotifyDescriptor.WARNING_MESSAGE));
            notified = true;
        }

        this.uiProperties.putAdditionalProperty(RailsProjectProperties.SOURCE_ENCODING, encName);
    }

    private String getSelectedConfig() {
        String config = (String) configCombo.getSelectedItem();
        if (config.length() == 0) {
            config = null;
        }
        return config;
    }

    private void selectServer(String serverId) {
        RubyPlatform platform = uiProperties.getPlatform();
        if (platform == null) {
            return;
        }
        RubyInstance server = ServerRegistry.getDefault().getServer(serverId, platform);
        if (server != null) {
            serverComboBox.setSelectedItem(server);
        }
    }

    private void selectRailsEnv(final String definedEnv) {
        if (definedEnv != null && !"".equals(definedEnv.trim())) {
            railsEnvCombo.setSelectedItem(definedEnv);
        } else {
            railsEnvCombo.setSelectedIndex(-1);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerRun.class );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configSep = new javax.swing.JSeparator();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        configNew = new javax.swing.JButton();
        configDel = new javax.swing.JButton();
        rubyPlatformLabel = new javax.swing.JLabel();
        platforms = org.netbeans.modules.ruby.platform.PlatformComponentFactory.getRubyPlatformsComboxBox();
        manageButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        serverComboBox = RailsServerUiUtils.getServerComboBox(getPlatform());
        serverLabel = new javax.swing.JLabel();
        railsEnvCombo = new javax.swing.JComboBox();
        railsEnvLabel = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        urlHelpLabel = new javax.swing.JLabel();
        serverArgsLabel = new javax.swing.JLabel();
        serverArgsField = new javax.swing.JTextField();
        encoding = new javax.swing.JComboBox();
        encodingLabel = new javax.swing.JLabel();
        rakeLabel = new javax.swing.JLabel();
        rakeTextField = new javax.swing.JTextField();
        rakeHelpLabel = new javax.swing.JLabel();

        configLabel.setLabelFor(configCombo);
        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel")); // NOI18N

        configCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default>" }));
        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew")); // NOI18N
        configNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configDelete")); // NOI18N
        configDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });

        rubyPlatformLabel.setLabelFor(platforms);
        org.openide.awt.Mnemonics.setLocalizedText(rubyPlatformLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RubyPlatformLabel")); // NOI18N

        platforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RubyHomeBrowse")); // NOI18N
        manageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageButtonActionPerformed(evt);
            }
        });

        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N

        portField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portFieldActionPerformed(evt);
            }
        });

        serverLabel.setLabelFor(serverComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(serverLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ServerLabel")); // NOI18N

        railsEnvCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(railsEnvLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RailsEnv")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, "UR&L:");

        urlTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlTextFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(urlHelpLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_URL_Help")); // NOI18N

        serverArgsLabel.setLabelFor(serverArgsField);
        org.openide.awt.Mnemonics.setLocalizedText(serverArgsLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ServerArguments")); // NOI18N

        encodingLabel.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "TXT_Encoding")); // NOI18N

        rakeLabel.setLabelFor(rakeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RakeArgs")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rakeHelpLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RakeArgsEx")); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(serverLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(portLabel))
                            .add(railsEnvLabel)
                            .add(urlLabel)
                            .add(serverArgsLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(rakeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(encodingLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(rakeHelpLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 408, Short.MAX_VALUE))
                    .add(rakeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, encoding, 0, 512, Short.MAX_VALUE)
                    .add(urlHelpLabel)
                    .add(railsEnvCombo, 0, 512, Short.MAX_VALUE)
                    .add(portField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                    .add(serverComboBox, 0, 512, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, serverArgsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverLabel)
                    .add(serverComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portLabel)
                    .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(railsEnvCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(railsEnvLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(urlLabel)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(urlHelpLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverArgsLabel)
                    .add(serverArgsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(encoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(encodingLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rakeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rakeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rakeHelpLabel))
        );

        portLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_ServerPort")); // NOI18N
        portField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_ServerPort")); // NOI18N
        serverComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_ServerEnvironment")); // NOI18N
        railsEnvCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_RailsEnv")); // NOI18N
        railsEnvLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_RailsEnv")); // NOI18N
        serverArgsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_ServerArguments")); // NOI18N
        serverArgsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_ServerArguments")); // NOI18N
        encoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_Encoding")); // NOI18N
        rakeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_RakeArguments")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, configSep, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(configLabel)
                    .add(rubyPlatformLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(configCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(platforms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 374, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(configNew)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configDel))
                    .add(manageButton))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(platforms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(manageButton)
                    .add(rubyPlatformLabel))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(configCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(configNew)
                    .add(configDel)
                    .add(configLabel))
                .add(14, 14, 14)
                .add(configSep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        configCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_Configuration")); // NOI18N
        configNew.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_NewConfiguration")); // NOI18N
        platforms.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_RubyPlatformLabel")); // NOI18N
        manageButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_RubyHomeBrowse")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portFieldActionPerformed

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

    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        RubyPlatformCustomizer.manage(platforms);
    }//GEN-LAST:event_manageButtonActionPerformed

    private void platformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformsActionPerformed
        initServerComboBox();
    }//GEN-LAST:event_platformsActionPerformed

    private void urlTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_urlTextFieldActionPerformed

    private void initServerComboBox(){
        if (getPlatform() == null) {
            serverComboBox.setModel(new DefaultComboBoxModel());
        } else {
            serverComboBox.setModel(new RailsServerUiUtils.ServerListModel(getPlatform()));
            String serverID = configs.get(getSelectedConfig()).get(RailsProjectProperties.RAILS_SERVERTYPE);
            if (serverID != null) {
                selectServer(serverID);
            }
        }
    }
    
    private RubyPlatform getPlatform() {
        return PlatformComponentFactory.getPlatform(platforms);
    }

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
            String activePlatformID = active.get(RailsProjectProperties.PLATFORM_ACTIVE);
            if (activePlatformID == null) {
                activePlatformID = def.get(RailsProjectProperties.PLATFORM_ACTIVE);
            }
            platforms.setSelectedItem(RubyPlatformManager.getPlatformByID(activePlatformID));

            String serverID = active.get(RailsProjectProperties.RAILS_SERVERTYPE);
            if (serverID == null) {
                serverID = def.get(RailsProjectProperties.RAILS_SERVERTYPE);
            }
            selectServer(serverID);

            String environment = active.get(RailsProjectProperties.RAILS_ENV);
            if (environment == null) {
                environment = def.get(RailsProjectProperties.RAILS_ENV);
            }
            selectRailsEnv(environment);
        } // else ??
        configDel.setEnabled(activeConfig != null);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox configCombo;
    private javax.swing.JButton configDel;
    private javax.swing.JLabel configLabel;
    private javax.swing.JButton configNew;
    private javax.swing.JSeparator configSep;
    private javax.swing.JComboBox encoding;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton manageButton;
    private javax.swing.JComboBox platforms;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JComboBox railsEnvCombo;
    private javax.swing.JLabel railsEnvLabel;
    private javax.swing.JLabel rakeHelpLabel;
    private javax.swing.JLabel rakeLabel;
    private javax.swing.JTextField rakeTextField;
    private javax.swing.JLabel rubyPlatformLabel;
    private javax.swing.JTextField serverArgsField;
    private javax.swing.JLabel serverArgsLabel;
    private javax.swing.JComboBox serverComboBox;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JLabel urlHelpLabel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
