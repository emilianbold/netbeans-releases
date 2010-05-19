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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.rubyproject.ui.customizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.Gem;
import org.netbeans.modules.ruby.platform.gems.GemAction;
import org.netbeans.modules.ruby.platform.gems.GemListPanel;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.rubyproject.GemRequirement;
import org.netbeans.modules.ruby.rubyproject.RequiredGems;
import org.netbeans.modules.ruby.rubyproject.RequiredGems.GemIndexingStatus;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Erno Mononen
 */
public class GemRequirementsPanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(GemRequirementsPanel.class.getName());

    private final RubyBaseProject project;
    private final SharedRubyProjectProperties properties;
    private RequiredGems requiredGems;
    private RequiredGems requiredGemsTest;

    GemRequirementsPanel(RubyBaseProject project, SharedRubyProjectProperties uiProps) {
        this.project = project;
        this.properties = uiProps;
        RequiredGems[] reqs = RequiredGems.lookup(project);
        this.requiredGems = reqs[0];
        this.requiredGemsTest = reqs[1];
        //XXX need to init this somewhere else
        requiredGems.setRequiredGems(project.evaluator().getProperty(RequiredGems.REQUIRED_GEMS_PROPERTY));
        requiredGemsTest.setRequiredGems(project.evaluator().getProperty(RequiredGems.REQUIRED_GEMS_TESTS_PROPERTY));
        initComponents();
        enableButtons(true);
    }

    private void enableButtons(boolean enabled) {
        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        editButton.setVisible(enabled);
    }

    private DefaultTableModel createTableModel() {
        return createTableModelFor(requiredGems);
    }

    private DefaultTableModel createTestTableModel() {
        return createTableModelFor(requiredGemsTest);
    }

    private DefaultTableModel createTableModelFor(RequiredGems requiredGems) {
        List<GemIndexingStatus> gems = requiredGems.getGemIndexingStatuses();
        if (gems == null) {
            return createTableModelWithDefaultGems();
        }

        GemManager gemManager = getGemManager();

        Object[][] data = new Object[gems.size()][3];
        for (int i = 0; i < gems.size(); i++) {
            GemIndexingStatus indexedGem = gems.get(i);
            data[i][0] = indexedGem.getRequirement().getName();
            data[i][1] = indexedGem.getRequirement().getVersionRequirement();
            String indexedVersion = indexedGem.getIndexedVersion();
            if (indexedVersion == null) {
                indexedVersion = gemManager.getLatestVersion(indexedGem.getRequirement().getName());
            }
            data[i][2] = indexedVersion != null
                    ? indexedVersion
                    : NbBundle.getMessage(GemRequirementsPanel.class, "NoVersionInstalled");
        }

        NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title0");
        return new DefaultTableModel(data,
                new Object[]{NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title0"),
                NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title1"),
                NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title2")});
    }

    private DefaultTableModel createTableModelWithDefaultGems() {

        RubyPlatform platform = project.getPlatform();
        if (platform == null) {
            return new DefaultTableModel();
        }
        GemManager gemManager = platform.getGemManager();
        if (gemManager == null) {
            return null;
        }
        List<Gem> gems = gemManager.getInstalledGems(new ArrayList<String>());

        Object[][] data = new Object[gems.size()][3];
        for (int i = 0; i < gems.size(); i++) {
            Gem gem = gems.get(i);
            data[i][0] = gem.getName();
            data[i][1] = "";
            data[i][2] = gemManager.getLatestVersion(gem.getName());
        }

        return new DefaultTableModel(data,
                new Object[]{NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title0"),
                NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title1"),
                NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title2")});
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        runScrollPane = new javax.swing.JScrollPane();
        gemsTable = new javax.swing.JTable();
        testScrollPane = new javax.swing.JScrollPane();
        testGemsTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        gemManagerButton = new javax.swing.JButton();

        gemsTable.setModel(createTableModel());
        gemsTable.getTableHeader().setReorderingAllowed(false);
        runScrollPane.setViewportView(gemsTable);
        gemsTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title0")); // NOI18N
        gemsTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title1")); // NOI18N
        gemsTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemsTable.columnModel.title2")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.runScrollPane.TabConstraints.tabTitle"), runScrollPane); // NOI18N

        testGemsTable.setModel(createTestTableModel());
        testScrollPane.setViewportView(testGemsTable);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.testScrollPane.TabConstraints.tabTitle"), testScrollPane); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.editButton.text")); // NOI18N
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(gemManagerButton, org.openide.util.NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.gemManagerButton.text")); // NOI18N
        gemManagerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gemManagerButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 403, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .add(gemManagerButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, editButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(43, 43, 43)
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 114, Short.MAX_VALUE)
                        .add(gemManagerButton)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private JTable getSelectedTable() {
        if (jTabbedPane1.getSelectedIndex() == 0) {
            return gemsTable;
        }
        return testGemsTable;
    }

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        JTable selected = getSelectedTable();
        DefaultTableModel model = (DefaultTableModel) selected.getModel();
        int[] rows = selected.getSelectedRows();
        for (int row : rows) {
            String name = (String) model.getValueAt(row, 0);
            getSelectedRequiredGems().removeRequirement(name);
        }
        for (int i = rows.length - 1; i >= 0; i--) {
            model.removeRow(rows[i]);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private RequiredGems getSelectedRequiredGems() {
        if (jTabbedPane1.getSelectedIndex() == 0) {
            return requiredGems;
        }
        return requiredGemsTest;
    }

    private GemManager getGemManager() {
        return project.getPlatform().getGemManager();
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        AtomicBoolean cancelled = new AtomicBoolean();
        final List<Gem> gems = new ArrayList<Gem>();
        Runnable fetchGemsTask = new Runnable() {

            public void run() {
                GemManager gemManager = getGemManager();
                if (gemManager == null) {
                    return;
                }
                gems.addAll(gemManager.getInstalledGems(new ArrayList<String>()));
            }
        };

        ProgressUtils.runOffEventDispatchThread(fetchGemsTask, 
                NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.fetchGems"),
                cancelled,
                true);
        
        if (cancelled.get()) {
            return;
        }

        final GemListPanel gemListPanel = new GemListPanel(gems);
        DialogDescriptor dd = new DialogDescriptor(gemListPanel, NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementsPanel.chooseGems"));
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(GemRequirementsPanel.class));
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
            List<GemRequirement> reqsToAdd = new ArrayList<GemRequirement>();
            for (Gem each : gemListPanel.getSelectedGems()) {
                reqsToAdd.add(GemRequirement.forGem(each));
            }
            getSelectedRequiredGems().addRequirements(reqsToAdd);
            getSelectedTable().setModel(createTableModel());
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_addButtonActionPerformed

    private void gemManagerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gemManagerButtonActionPerformed
        GemAction.showGemManager(project.getPlatform(), false);
    }//GEN-LAST:event_gemManagerButtonActionPerformed

    private String[] getSelection() {
        JTable table = getSelectedTable();
        int index = table.getSelectedRow();
        if (index == -1) {
            return null;
        }
        String[] result = new String[3];
        result[0] = (String) table.getValueAt(index, 0);
        result[1] = (String) table.getValueAt(index, 1);
        result[2] = (String) table.getValueAt(index, 2);
        return result;

    }

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        String[] selected = getSelection();
        if (selected == null) {
            return;
        }
        GemRequirementDetailsPanel panel = new GemRequirementDetailsPanel(getGemManager(), selected[0], selected[1], selected[2]);
        DialogDescriptor dd = new DialogDescriptor(panel, 
                NbBundle.getMessage(GemRequirementsPanel.class, "GemRequirementDetailsPanel.title",
                selected[0]));
        
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(GemRequirementsPanel.class));
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
            GemRequirement req = panel.getGemRequirement();
            getSelectedRequiredGems().removeRequirement(req.getName());
            getSelectedRequiredGems().addRequirements(Collections.singleton(req));
            getSelectedTable().setModel(createTableModel());
        }
    }//GEN-LAST:event_editButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton gemManagerButton;
    private javax.swing.JTable gemsTable;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane runScrollPane;
    private javax.swing.JTable testGemsTable;
    private javax.swing.JScrollPane testScrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void removeNotify() {
        super.removeNotify();
        properties.setGemRequirements(requiredGems.getGemRequirements());
        properties.setGemRequirementsForTests(requiredGemsTest.getGemRequirements());
    }


}


