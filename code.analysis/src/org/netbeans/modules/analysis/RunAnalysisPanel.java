/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.analysis;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.modules.analysis.spi.Analyzer.Context;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.netbeans.modules.analysis.spi.Analyzer.WarningDescription;
import org.netbeans.modules.analysis.ui.AdjustConfigurationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class RunAnalysisPanel extends javax.swing.JPanel {
    
    private final JPanel progress;
    private final DefaultComboBoxModel configurationModel;
    private final RequiredPluginsPanel requiredPlugins;
    private final Collection<? extends AnalyzerFactory> analyzers;
    private final Map<String, WarningDescription> warningId2Description = new HashMap<String, WarningDescription>();

    public RunAnalysisPanel(ProgressHandle handle, Collection<? extends AnalyzerFactory> analyzers) {
        this.analyzers = analyzers;
        
        configurationModel = new DefaultComboBoxModel();
        configurationModel.addElement("Predefined");
        configurationModel.addElement(null);

        for (AnalyzerFactory analyzer : analyzers) {
            configurationModel.addElement(analyzer);
        }

        configurationModel.addElement("Custom");

        if (!RunAnalysis.readConfigurations().iterator().hasNext()) {
            RunAnalysis.getConfigurationSettingsRoot("default").put("displayName", "Default");
        }

        for (Configuration c : RunAnalysis.readConfigurations()) {
            configurationModel.addElement(c);
        }
        
        initComponents();

        configurationCombo.setRenderer(new ConfigurationRenderer(true));

        DefaultComboBoxModel inspectionModel = new DefaultComboBoxModel();

        for (AnalyzerFactory a : analyzers) {
            inspectionModel.addElement(SPIAccessor.ACCESSOR.getAnalyzerDisplayName(a));

            Map<String, Collection<WarningDescription>> cat2Warnings = new TreeMap<String, Collection<WarningDescription>>();

            for (WarningDescription wd : a.getWarnings()) {
                String cat = SPIAccessor.ACCESSOR.getWarningCategoryDisplayName(wd); //TODO: should be based on the id rather than on the display name
                Collection<WarningDescription> warnings = cat2Warnings.get(cat);

                if (warnings == null) {
                    cat2Warnings.put(cat, warnings = new TreeSet<WarningDescription>(new Comparator<WarningDescription>() {
                        @Override public int compare(WarningDescription o1, WarningDescription o2) {
                            return SPIAccessor.ACCESSOR.getWarningDisplayName(o1).compareToIgnoreCase(SPIAccessor.ACCESSOR.getWarningDisplayName(o2));
                        }
                    }));
                }

                warnings.add(wd);
                warningId2Description.put(SPIAccessor.ACCESSOR.getWarningId(wd), wd);
            }

            for (Entry<String, Collection<WarningDescription>> catE : cat2Warnings.entrySet()) {
                inspectionModel.addElement("  " + catE.getKey());

                for (WarningDescription wd : catE.getValue()) {
                    inspectionModel.addElement(wd);
                }
            }
        }

        inspectionCombo.setModel(inspectionModel);
        inspectionCombo.setRenderer(new InspectionRenderer());
        inspectionCombo.setSelectedIndex(2);

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        progress = new JPanel(new CardLayout());
        progress.add(new JPanel(), "empty");
        progress.add(ProgressHandleFactory.createProgressComponent(handle), "progress");
        progress.add(requiredPlugins = new RequiredPluginsPanel(), "plugins");
        add(progress, gridBagConstraints);
        ((CardLayout) progress.getLayout()).show(progress, "empty");

        updatePlugins();
        configurationCombo.setSelectedIndex(1);//XXX: the value should be kept across invocations of the dialog

        configurationRadio.setSelected(true);
        updateEnableDisable();

        setBorder(new EmptyBorder(12, 12, 12, 12));
    }

    void started() {
        ((CardLayout) progress.getLayout()).show(progress, "progress");
        progress.invalidate();
        //XXX: should disable all elements in the dialog.
    }

    private void updatePlugins() {
        Collection<? extends AnalyzerFactory> toRun;

        if (!(configurationCombo.getSelectedItem() instanceof AnalyzerFactory)) {
            toRun = analyzers;
        } else {
            toRun = Collections.singleton((AnalyzerFactory) configurationCombo.getSelectedItem());
        }

        Context ctx = SPIAccessor.ACCESSOR.createContext(null, null, null, null, -1, -1);
        Set<MissingPlugin> plugins = new HashSet<MissingPlugin>();

        for (AnalyzerFactory a : toRun) {
            plugins.addAll(a.requiredPlugins(ctx));
        }

        if (plugins.isEmpty()) {
            ((CardLayout) progress.getLayout()).show(progress, "empty");
        } else {
            requiredPlugins.setRequiredPlugins(plugins);
            ((CardLayout) progress.getLayout()).show(progress, "plugins");
        }
    }

    public AnalyzerFactory getSelectedAnalyzer() {
        if (!(configurationCombo.getSelectedItem() instanceof AnalyzerFactory)) return null;
        return (AnalyzerFactory) configurationCombo.getSelectedItem();
    }

    public String getConfiguration() {
        if (inspectionCombo.isEnabled()) return "internal-temporary";
        Object selected = configurationCombo.getSelectedItem();

        if (selected instanceof Configuration) return ((Configuration) selected).id();
        else return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        radioButtons = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        scopeCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        configurationCombo = new javax.swing.JComboBox();
        manage = new javax.swing.JButton();
        configurationRadio = new javax.swing.JRadioButton();
        singleInspectionRadio = new javax.swing.JRadioButton();
        inspectionCombo = new javax.swing.JComboBox();
        browse = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        add(jLabel1, gridBagConstraints);

        scopeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All Opened Projects" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(scopeCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        configurationCombo.setModel(configurationModel);
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(configurationCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manage, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.manage.text")); // NOI18N
        manage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(manage, gridBagConstraints);

        radioButtons.add(configurationRadio);
        org.openide.awt.Mnemonics.setLocalizedText(configurationRadio, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.configurationRadio.text")); // NOI18N
        configurationRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(configurationRadio, gridBagConstraints);

        radioButtons.add(singleInspectionRadio);
        org.openide.awt.Mnemonics.setLocalizedText(singleInspectionRadio, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.singleInspectionRadio.text")); // NOI18N
        singleInspectionRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleInspectionRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(singleInspectionRadio, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(inspectionCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(browse, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        updatePlugins();
    }//GEN-LAST:event_configurationComboActionPerformed

    private void manageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageActionPerformed
        AdjustConfigurationPanel panel = new AdjustConfigurationPanel(analyzers, null);
        DialogDescriptor nd = new DialogDescriptor(panel, "Configurations", true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            panel.save();
        }
    }//GEN-LAST:event_manageActionPerformed

    private void configurationRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationRadioActionPerformed
        updateEnableDisable();
    }//GEN-LAST:event_configurationRadioActionPerformed

    private void singleInspectionRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleInspectionRadioActionPerformed
        updateEnableDisable();
    }//GEN-LAST:event_singleInspectionRadioActionPerformed

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        AdjustConfigurationPanel panel = new AdjustConfigurationPanel(analyzers, "XXX");
        DialogDescriptor nd = new DialogDescriptor(panel, "Configurations", true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            inspectionCombo.setSelectedItem(warningId2Description.get(panel.getIdToRun()));
        }
    }//GEN-LAST:event_browseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JRadioButton configurationRadio;
    private javax.swing.JComboBox inspectionCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton manage;
    private javax.swing.ButtonGroup radioButtons;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JRadioButton singleInspectionRadio;
    // End of variables declaration//GEN-END:variables

    private void updateEnableDisable() {
        boolean configuration = configurationRadio.isSelected();

        configurationCombo.setEnabled(configuration);
        manage.setEnabled(configuration);
        inspectionCombo.setEnabled(!configuration);
        browse.setEnabled(!configuration);
    }

    String getSingleWarningId() {
        return inspectionCombo.isEnabled() ? SPIAccessor.ACCESSOR.getWarningId((WarningDescription) inspectionCombo.getSelectedItem()) : null;
    }

    public static final class ConfigurationRenderer extends DefaultListCellRenderer {

        private final boolean indent;

        public ConfigurationRenderer(boolean indent) {
            this.indent = indent;
        }

        @Messages({"LBL_RunAllAnalyzers=All Analyzers", "LBL_RunAnalyzer={0}"})
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                value = Bundle.LBL_RunAllAnalyzers();
            } else if (value instanceof AnalyzerFactory) {
                value = Bundle.LBL_RunAnalyzer(SPIAccessor.ACCESSOR.getAnalyzerDisplayName((AnalyzerFactory) value));
            } else if (value instanceof Configuration) {
                value = ((Configuration) value).getDisplayName();
            } else if (value instanceof String) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setText((String) value);
                setEnabled(false);
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));

                return this;
            }
            
            return super.getListCellRendererComponent(list, (indent ? "  " : "") + value, index, isSelected, cellHasFocus);
        }
    }

    private static final class InspectionRenderer extends DefaultListCellRenderer {

        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof WarningDescription) {
                value = "    " + SPIAccessor.ACCESSOR.getWarningDisplayName((WarningDescription) value);
            } else if (value instanceof String) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setText((String) value);
                setEnabled(false);
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));

                return this;
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
