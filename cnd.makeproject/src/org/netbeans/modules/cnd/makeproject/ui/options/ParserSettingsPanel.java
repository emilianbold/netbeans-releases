/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.ui.options;

import java.awt.event.ActionEvent;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.ui.options.ToolsPanel;
import org.openide.util.NbBundle;

public class ParserSettingsPanel extends JPanel implements ChangeListener, ActionListener {

    private HashMap predefinedPanels = new HashMap();
    private boolean updating = false;
    private boolean modified = false;
    private ToolsPanel tp;

    /**
     * Creates new form ParserSettingsPanel
     */
    public ParserSettingsPanel() {
        setName("TAB_CodeAssistanceTab"); // NOI18N
        initComponents();

        //infoTextArea.setBackground(collectionPanel.getBackground());
        //setPreferredSize(new java.awt.Dimension(600, 700));
        // Accessible Description
        getAccessibleContext().setAccessibleDescription(getString("MANAGE_COMPILERS_SETTINGS_AD"));
        compilerCollectionComboBox.getAccessibleContext().setAccessibleDescription(getString("COMPILER_COLLECTION_AD"));
        tp = ToolsPanel.getToolsPanel();
        if (tp != null) {
            // This gets called from commitValidation and tp is null - its not a run-time problem
            // because the "real" way we create this a ToolsPanel exists. But not the commitValidation way!
            tp.addCompilerSetChangeListener(this);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (!updating && isShowing()) {
            updateTabs();
        }
    }

    private void updateCompilerCollections(CompilerSet cs) {
        compilerCollectionComboBox.removeAllItems();
        for (CompilerSet cs2 : tp.getCompilerSetManager().getCompilerSets()) {
            compilerCollectionComboBox.addItem(cs2);
        }

        if (cs == null) {
            cs = tp.getCompilerSetManager().getCompilerSet(0);
        }
        if (cs != null) {
            compilerCollectionComboBox.setSelectedItem(cs);
        }
        updateTabs();
    }

    private void updateTabs() {
        tabbedPane.removeAll();
        CompilerSet compilerCollection = (CompilerSet) compilerCollectionComboBox.getSelectedItem();
        if (compilerCollection == null) {
            return;
        }
        // Show only the selected C and C++ compiler from the compiler collection
        ArrayList<Tool> toolSet = new ArrayList<Tool>();
        Tool cCompiler = compilerCollection.getTool(Tool.CCompiler);
        if (cCompiler != null && cCompiler.getPath().length() > 0) {
            toolSet.add(cCompiler);
        }
        Tool cppCompiler = compilerCollection.getTool(Tool.CCCompiler);
        if (cppCompiler != null && cppCompiler.getPath().length() > 0) {
            toolSet.add(cppCompiler);
        }
        for (Tool tool : toolSet) {
            PredefinedPanel predefinedPanel = (PredefinedPanel) predefinedPanels.get(tool.getPath());
            if (predefinedPanel == null) {
                predefinedPanel = new PredefinedPanel((CCCCompiler) tool, this);
                predefinedPanels.put(tool.getPath(), predefinedPanel);
                //modified = true; // See 126368
            }
            tabbedPane.addTab(tool.getDisplayName(), predefinedPanel);
        }
    }

    public void fireFilesPropertiesChanged() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("fireFilesPropertiesChanged for ParserSettingsPanel");
        }
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < openProjects.length; i++) {
            NativeProjectProvider npv = (NativeProjectProvider) openProjects[i].getLookup().lookup(NativeProjectProvider.class );
            if (npv != null) {
                npv.fireFilesPropertiesChanged();
            }
        }
    }

    public void stateChanged(ChangeEvent ev) {
        Object o = ev.getSource();
        if (o instanceof CompilerSet) {
            updateCompilerCollections((CompilerSet) o);
        } else {
            updateCompilerCollections(null);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        collectionPanel = new javax.swing.JPanel();
        compilerCollectionLabel = new javax.swing.JLabel();
        compilerCollectionComboBox = new javax.swing.JComboBox();
        scrollPane = new javax.swing.JScrollPane();
        tabPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();

        compilerCollectionLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("COMPILER_COLLECTION_MN").charAt(0));
        compilerCollectionLabel.setLabelFor(compilerCollectionComboBox);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle"); // NOI18N
        compilerCollectionLabel.setText(bundle.getString("COMPILER_COLLECTION_LBL")); // NOI18N

        org.jdesktop.layout.GroupLayout collectionPanelLayout = new org.jdesktop.layout.GroupLayout(collectionPanel);
        collectionPanel.setLayout(collectionPanelLayout);
        collectionPanelLayout.setHorizontalGroup(
            collectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(collectionPanelLayout.createSequentialGroup()
                .add(compilerCollectionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(compilerCollectionComboBox, 0, 246, Short.MAX_VALUE)
                .addContainerGap())
        );
        collectionPanelLayout.setVerticalGroup(
            collectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(collectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(compilerCollectionLabel)
                .add(compilerCollectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout tabPanelLayout = new org.jdesktop.layout.GroupLayout(tabPanel);
        tabPanel.setLayout(tabPanelLayout);
        tabPanelLayout.setHorizontalGroup(
            tabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
        );
        tabPanelLayout.setVerticalGroup(
            tabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
        );

        tabbedPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParserSettingsPanel.class, "COMPILERS_TABBEDPANE_AN")); // NOI18N
        tabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserSettingsPanel.class, "COMPILERS_TABBEDPANE_AD")); // NOI18N

        scrollPane.setViewportView(tabPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(collectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 0, 0)
                    .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                    .add(0, 0, 0)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(collectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(265, Short.MAX_VALUE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(36, 36, 36)
                    .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .add(0, 0, 0)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel collectionPanel;
    private javax.swing.JComboBox compilerCollectionComboBox;
    private javax.swing.JLabel compilerCollectionLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JPanel tabPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(ParserSettingsPanel.class, s);
    }

    void update() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("update for ParserSettingsPanel");
        }
        try {
            updating = true;
            compilerCollectionComboBox.removeActionListener(this);
            updateCompilerCollections(tp.getCurrentCompilerSet());
            compilerCollectionComboBox.addActionListener(this);
            PredefinedPanel[] viewedPanels = getPredefinedPanels();
            for (int i = 0; i < viewedPanels.length; i++) {
                viewedPanels[i].update();
            }
        } finally {
            updating = false;
        }
    }

    void cancel() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("cancel for ParserSettingsPanel");
        }
        PredefinedPanel[] viewedPanels = getPredefinedPanels();
        for (int i = 0; i < viewedPanels.length; i++) {
            viewedPanels[i].cancel();
        }
    }

    boolean isDataValid() {
        boolean isDataValid = true;
        PredefinedPanel[] viewedPanels = getPredefinedPanels();
        for (int i = 0; i < viewedPanels.length; i++) {
            isDataValid &= viewedPanels[i].isDataValid();
        }
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("isDataValid for ParserSettingsPanel is " + isDataValid);
        }
        return isDataValid;
    }

    boolean isChanged() {
        boolean isChanged = false;
        PredefinedPanel[] viewedPanels = getPredefinedPanels();
        for (int i = 0; i < viewedPanels.length; i++) {
            isChanged |= viewedPanels[i].isChanged();
        }
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("isChanged for ParserSettingsPanel is " + isChanged);
        }
        return isChanged;
    }

    public void save() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("save for ParserSettingsPanel");
        }
        PredefinedPanel[] viewedPanels = getPredefinedPanels();
        boolean wasChanges = false;
        for (int i = 0; i < viewedPanels.length; i++) {
            wasChanges |= viewedPanels[i].save();
        }
        if (wasChanges || modified) {
            if (CodeAssistancePanelController.TRACE_CODEASSIST) {
                System.err.println("fireFilesPropertiesChanged in save for ParserSettingsPanel");
            }
            fireFilesPropertiesChanged();
            modified = false;
        } else {
            if (CodeAssistancePanelController.TRACE_CODEASSIST) {
                System.err.println("not need to fireFilesPropertiesChanged in save for ParserSettingsPanel");
            }
        }
    }

    private PredefinedPanel[] getPredefinedPanels() {
        return (PredefinedPanel[]) predefinedPanels.values().toArray(new PredefinedPanel[predefinedPanels.size()]);
    }
}