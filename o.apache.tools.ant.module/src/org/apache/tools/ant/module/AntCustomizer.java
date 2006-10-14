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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/**
 * Implementation of one panel in Options Dialog.
 * @author Jan Jancura, Jesse Glick
 */
public class AntCustomizer extends JPanel implements ActionListener {
    
    private List<File> classpath;
    private Map<String,String> properties;
    private boolean         changed = false;
    private boolean         listen = false;
    private File            originalAntHome;

    public AntCustomizer() {
        initComponents();
        bAntHome.addActionListener (this);
        ((DefaultComboBoxModel) cbVerbosity.getModel()).removeAllElements(); // just have prototype for form editor
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_warn"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_info"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_verbose"));
        cbVerbosity.addItem(NbBundle.getMessage(AntCustomizer.class, "LBL_verbosity_debug"));
        bProperties.addActionListener (this);
        bClasspath.addActionListener (this);
        cbSaveFiles.addActionListener (this);
        cbReuseOutput.addActionListener (this);
        cbAlwaysShowOutput.addActionListener (this);
        cbVerbosity.addActionListener (this);
    }
    
    void update () {
        listen = false;
        classpath = AntSettings.getExtraClasspath();
        properties = AntSettings.getProperties();
        originalAntHome = AntSettings.getAntHome();
            
        tfAntHome.setText(originalAntHome != null ? originalAntHome.toString() : null);
        cbSaveFiles.setSelected(AntSettings.getSaveAll());
        cbReuseOutput.setSelected(AntSettings.getAutoCloseTabs());
        cbAlwaysShowOutput.setSelected(AntSettings.getAlwaysShowOutput());
        cbVerbosity.setSelectedIndex(AntSettings.getVerbosity() - 1);
        lAntVersion.setText("(" + AntSettings.getAntVersion() + ")");
        changed = false;
        initialized = true;
        listen = true;
    }
    
    private boolean initialized = false;
    
    void applyChanges () {
        if (!initialized) return;
        String antHome = tfAntHome.getText ().trim ();
        AntSettings.setAntHome(new File(antHome));
        if (AntSettings.getAutoCloseTabs() != cbReuseOutput.isSelected()) {
            AntSettings.setAutoCloseTabs(cbReuseOutput.isSelected());
        }
        if (AntSettings.getSaveAll() != cbSaveFiles.isSelected()) {
            AntSettings.setSaveAll(cbSaveFiles.isSelected());
        }
        if (AntSettings.getAlwaysShowOutput() != cbAlwaysShowOutput.isSelected()) {
            AntSettings.setAlwaysShowOutput(cbAlwaysShowOutput.isSelected());
        }
        if (AntSettings.getVerbosity() != cbVerbosity.getSelectedIndex() + 1) {
            AntSettings.setVerbosity(cbVerbosity.getSelectedIndex() + 1);
        }
        if (!AntSettings.getProperties().equals(properties)) {
            AntSettings.setProperties(properties);
        }
        if (!AntSettings.getExtraClasspath().equals(classpath)) {
            AntSettings.setExtraClasspath(classpath);
        }
        changed = false;
    }
    
    void cancel () {
        if (AntSettings.getAntHome() != originalAntHome) {
            AntSettings.setAntHome(originalAntHome);
        }
        changed = false;
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return changed;
    }
    
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        Object o = e.getSource ();
        if (o == cbAlwaysShowOutput) {
            changed = true;
        } else
        if (o == cbReuseOutput) {
            changed = true;
        } else
        if (o == cbSaveFiles) {
            changed = true;
        } else
        if (o == cbVerbosity) {
            changed = true;
        } else
        if (o == bAntHome) {
            JFileChooser chooser = new JFileChooser (tfAntHome.getText ());
            chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            int r = chooser.showDialog (
                SwingUtilities.getWindowAncestor (this),
                NbBundle.getMessage(AntCustomizer.class, "Select_Directory")
            );
            if (r == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile ();
                if (!new File (new File (file, "lib"), "ant.jar").isFile ()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(AntCustomizer.class, "Not_a_ant_home", file),
                        NotifyDescriptor.Message.WARNING_MESSAGE
                    ));
                    return;
                }
                tfAntHome.setText (file.getAbsolutePath ());
                AntSettings.setAntHome(file);
                lAntVersion.setText("(" + AntSettings.getAntVersion() + ")");
                changed = true;
            }
        } else
        if (o == bClasspath) {
            PropertyEditor editor = PropertyEditorManager.findEditor(NbClassPath.class);
            editor.setValue(new NbClassPath(classpath.toArray(new File[classpath.size()])));
            Component customEditor = editor.getCustomEditor ();
            DialogDescriptor dd = new DialogDescriptor (
                customEditor,
                NbBundle.getMessage(AntCustomizer.class, "Classpath_Editor_Title")
            );
            Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
            dialog.setVisible (true);
            if (dd.getValue () == NotifyDescriptor.OK_OPTION) {
                String cp = ((NbClassPath) editor.getValue()).getClassPath();
                if (cp.startsWith("\"") && cp.endsWith("\"")) {
                    // *@%!* NbClassPath.getClassPath semantics.
                    cp = cp.substring(1, cp.length() - 1);
                }
                classpath = new ArrayList<File>();
                for (String f : cp.split(Pattern.quote(File.pathSeparator))) {
                    classpath.add(new File(f));
                }
                changed = true;
            }
        } else
        if (o == bProperties) {
            PropertyEditor editor = PropertyEditorManager.findEditor(Properties.class);
            Properties p = new Properties();
            p.putAll(properties);
            editor.setValue(p);
            Component customEditor = editor.getCustomEditor ();
            DialogDescriptor dd = new DialogDescriptor (
                customEditor,
                NbBundle.getMessage(AntCustomizer.class, "Properties_Editor_Title")
            );
            Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
            dialog.setVisible (true);
            if (dd.getValue () == NotifyDescriptor.OK_OPTION) {
                properties = NbCollections.checkedMapByCopy((Properties) editor.getValue(), String.class, String.class, true);
                changed = true;
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JLabel antHomeLabel;
        javax.swing.JLabel classpathLabel;
        javax.swing.JPanel classpathPanel;
        javax.swing.JLabel propertiesLabel;
        javax.swing.JPanel propertiesPanel;
        javax.swing.JLabel verbosityLabel;

        antHomeLabel = new javax.swing.JLabel();
        tfAntHome = new javax.swing.JTextField();
        bAntHome = new javax.swing.JButton();
        bAntHomeDefault = new javax.swing.JButton();
        lAntVersion = new javax.swing.JLabel();
        cbSaveFiles = new javax.swing.JCheckBox();
        cbReuseOutput = new javax.swing.JCheckBox();
        cbAlwaysShowOutput = new javax.swing.JCheckBox();
        cbVerbosity = new javax.swing.JComboBox();
        verbosityLabel = new javax.swing.JLabel();
        propertiesPanel = new javax.swing.JPanel();
        propertiesLabel = new javax.swing.JLabel();
        bProperties = new javax.swing.JButton();
        classpathPanel = new javax.swing.JPanel();
        classpathLabel = new javax.swing.JLabel();
        bClasspath = new javax.swing.JButton();

        setBackground(java.awt.Color.white);
        antHomeLabel.setLabelFor(tfAntHome);
        org.openide.awt.Mnemonics.setLocalizedText(antHomeLabel, NbBundle.getMessage(AntCustomizer.class, "Ant_Home"));

        org.openide.awt.Mnemonics.setLocalizedText(bAntHome, NbBundle.getMessage(AntCustomizer.class, "Ant_Home_Button"));

        org.openide.awt.Mnemonics.setLocalizedText(bAntHomeDefault, NbBundle.getMessage(AntCustomizer.class, "Ant_Home_Default_Button"));
        bAntHomeDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAntHomeDefaultActionPerformed(evt);
            }
        });

        lAntVersion.setBackground(java.awt.Color.white);
        org.openide.awt.Mnemonics.setLocalizedText(lAntVersion, "<Ant version here...>");

        cbSaveFiles.setBackground(java.awt.Color.white);
        org.openide.awt.Mnemonics.setLocalizedText(cbSaveFiles, NbBundle.getMessage(AntCustomizer.class, "Save_Files"));
        cbSaveFiles.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbSaveFiles.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbReuseOutput.setBackground(java.awt.Color.white);
        org.openide.awt.Mnemonics.setLocalizedText(cbReuseOutput, NbBundle.getMessage(AntCustomizer.class, "Reuse_Output"));
        cbReuseOutput.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbReuseOutput.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbAlwaysShowOutput.setBackground(java.awt.Color.white);
        org.openide.awt.Mnemonics.setLocalizedText(cbAlwaysShowOutput, NbBundle.getMessage(AntCustomizer.class, "Always_Show_Output"));
        cbAlwaysShowOutput.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAlwaysShowOutput.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbVerbosity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Normal" }));

        verbosityLabel.setLabelFor(cbVerbosity);
        org.openide.awt.Mnemonics.setLocalizedText(verbosityLabel, NbBundle.getMessage(AntCustomizer.class, "Verbosity"));

        propertiesPanel.setBackground(java.awt.Color.white);
        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(AntCustomizer.class, "Properties_Panel")));
        org.openide.awt.Mnemonics.setLocalizedText(propertiesLabel, NbBundle.getMessage(AntCustomizer.class, "Properties_Text_Area"));

        org.openide.awt.Mnemonics.setLocalizedText(bProperties, NbBundle.getMessage(AntCustomizer.class, "Properties_Button"));

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(propertiesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 25, Short.MAX_VALUE)
                .add(bProperties)
                .addContainerGap())
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesLabel)
                    .add(bProperties))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        classpathPanel.setBackground(java.awt.Color.white);
        classpathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(AntCustomizer.class, "Classpath_Panel")));
        org.openide.awt.Mnemonics.setLocalizedText(classpathLabel, NbBundle.getMessage(AntCustomizer.class, "Classpath_Text_Area"));

        org.openide.awt.Mnemonics.setLocalizedText(bClasspath, NbBundle.getMessage(AntCustomizer.class, "Classpath_Button"));

        org.jdesktop.layout.GroupLayout classpathPanelLayout = new org.jdesktop.layout.GroupLayout(classpathPanel);
        classpathPanel.setLayout(classpathPanelLayout);
        classpathPanelLayout.setHorizontalGroup(
            classpathPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(classpathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(classpathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(bClasspath)
                .addContainerGap())
        );
        classpathPanelLayout.setVerticalGroup(
            classpathPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(classpathLabel)
            .add(bClasspath)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(antHomeLabel)
                    .add(verbosityLabel))
                .add(16, 16, 16)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(cbAlwaysShowOutput)
                        .addContainerGap())
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(lAntVersion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                        .add(layout.createSequentialGroup()
                            .add(cbVerbosity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .add(layout.createSequentialGroup()
                            .add(cbReuseOutput)
                            .addContainerGap())
                        .add(layout.createSequentialGroup()
                            .add(cbSaveFiles)
                            .addContainerGap())
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(tfAntHome, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(bAntHome)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(bAntHomeDefault)))))
            .add(classpathPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                    .add(antHomeLabel)
                    .add(bAntHomeDefault)
                    .add(bAntHome)
                    .add(tfAntHome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lAntVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbSaveFiles)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbReuseOutput)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbAlwaysShowOutput)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbVerbosity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(verbosityLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(classpathPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bAntHomeDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAntHomeDefaultActionPerformed
        AntSettings.setAntHome(null);
        File antHome = AntSettings.getAntHome();
        if (antHome != null) {
            tfAntHome.setText(antHome.getAbsolutePath());
        } else {
            tfAntHome.setText(null);
        }
        lAntVersion.setText("(" + AntSettings.getAntVersion() + ")");
        changed = true;
    }//GEN-LAST:event_bAntHomeDefaultActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAntHome;
    private javax.swing.JButton bAntHomeDefault;
    private javax.swing.JButton bClasspath;
    private javax.swing.JButton bProperties;
    private javax.swing.JCheckBox cbAlwaysShowOutput;
    private javax.swing.JCheckBox cbReuseOutput;
    private javax.swing.JCheckBox cbSaveFiles;
    private javax.swing.JComboBox cbVerbosity;
    private javax.swing.JLabel lAntVersion;
    private javax.swing.JTextField tfAntHome;
    // End of variables declaration//GEN-END:variables
    
}
