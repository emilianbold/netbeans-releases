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
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.analysis.spi.Analyzer.Context;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class RunAnalysisPanel extends javax.swing.JPanel {
    
    private final JPanel progress;
    private final DefaultComboBoxModel configurationModel;
    private final RequiredPluginsPanel requiredPlugins;
    private final Collection<? extends Analyzer> analyzers;

    public RunAnalysisPanel(ProgressHandle handle, Collection<? extends Analyzer> analyzers) {
        this.analyzers = analyzers;
        
        configurationModel = new DefaultComboBoxModel();
        configurationModel.addElement(null);

        for (Analyzer analyzer : analyzers) {
            configurationModel.addElement(analyzer);
        }
        
        initComponents();

        configurationCombo.setRenderer(new ConfigurationRenderer());

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
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
    }

    void started() {
        ((CardLayout) progress.getLayout()).show(progress, "progress");
        progress.invalidate();
    }

    private void updatePlugins() {
        Collection<? extends Analyzer> toRun;

        if (configurationCombo.getSelectedItem() == null) {
            toRun = analyzers;
        } else {
            toRun = Collections.singleton((Analyzer) configurationCombo.getSelectedItem());
        }

        Context ctx = SPIAccessor.ACCESSOR.createContext(null, null, -1, -1);
        Set<MissingPlugin> plugins = new HashSet<MissingPlugin>();

        for (Analyzer a : toRun) {
            plugins.addAll(a.requiredPlugins(ctx));
        }

        if (plugins.isEmpty()) {
            ((CardLayout) progress.getLayout()).show(progress, "empty");
        } else {
            requiredPlugins.setRequiredPlugins(plugins);
            ((CardLayout) progress.getLayout()).show(progress, "plugins");
        }
    }

    public Analyzer getSelectedAnalyzer() {
        return (Analyzer) configurationCombo.getSelectedItem();
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

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        configurationCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        add(jLabel1, gridBagConstraints);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All Opened Projects" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jComboBox1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel2.text")); // NOI18N
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(configurationCombo, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        updatePlugins();
    }//GEN-LAST:event_configurationComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    private static final class ConfigurationRenderer extends DefaultListCellRenderer {
        @Messages({"LBL_RunAllAnalyzers=All Analyzers", "LBL_RunAnalyzer={0}"})
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                value = Bundle.LBL_RunAllAnalyzers();
            } else if (value instanceof Analyzer) {
                value = Bundle.LBL_RunAnalyzer(((Analyzer) value).getDisplayName());
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
