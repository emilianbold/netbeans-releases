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

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.ui.options.IsChangedListener;
import org.netbeans.modules.cnd.ui.options.ToolsPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

public class ParserSettingsPanel extends JPanel implements ChangeListener, ActionListener, IsChangedListener {

    private Map<String, PredefinedPanel> predefinedPanels = new HashMap<String, PredefinedPanel>();
    private boolean updating = false;
    private boolean modified = false;
    private ToolsPanel tp;
//    private boolean initialized = false;
    
    /**
     * Creates new form ParserSettingsPanel
     */
    public ParserSettingsPanel() {
        setName("TAB_CodeAssistanceTab"); // NOI18N
        initComponents();

        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            setOpaque(false);
        }

        //infoTextArea.setBackground(collectionPanel.getBackground());
        //setPreferredSize(new java.awt.Dimension(600, 700));
        // Accessible Description
        getAccessibleContext().setAccessibleDescription(getString("MANAGE_COMPILERS_SETTINGS_AD"));
        compilerCollectionComboBox.getAccessibleContext().setAccessibleDescription(getString("COMPILER_COLLECTION_AD"));
        compilerCollectionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                //CompilerSet cs = (CompilerSet) value;
                return label;
            }
        });
        tp = ToolsPanel.getToolsPanel();
        if (tp != null) {
            // This gets called from commitValidation and tp is null - its not a run-time problem
            // because the "real" way we create this a ToolsPanel exists. But not the commitValidation way!
            ToolsPanel.addCompilerSetChangeListener(this);
            ToolsPanel.addIsChangedListener(this);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (!updating && isShowing()) {
            updateTabs();
        }
    }

    private static class CompilerSetPresenter {

        public CompilerSet cs;
        private String displayName;

        public CompilerSetPresenter(CompilerSet cs, String displayName) {
            this.cs = cs;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public CompilerSetManager getCompilerSetManager(ExecutionEnvironment execEnv) {
        return ToolsPanel.getToolsPanel().getToolsCacheManager().getCompilerSetManagerCopy(execEnv, true);
    }
    
    private void updateCompilerCollections(CompilerSet csToSelect) {
        compilerCollectionComboBox.removeAllItems();

        CompilerSetPresenter toSelect = null;
        List<CompilerSetPresenter> allCS = new ArrayList<CompilerSetPresenter>();
        Collection<? extends ServerRecord> servers = ServerList.getRecords();
        if (servers.size() > 1) {
            for (ServerRecord record : servers) {
                for (CompilerSet cs : getCompilerSetManager(record.getExecutionEnvironment()).getCompilerSets()) {
                    CompilerSetPresenter csp = new CompilerSetPresenter(cs, record.getDisplayName() + " : " + cs.getName()); //NOI18N
                    if (csToSelect == cs) {
                        toSelect = csp;
                    }
                    allCS.add(csp);
                }
            }
        } else {
            assert servers.iterator().hasNext();
            assert ! servers.iterator().next().isRemote();
        }

        if (allCS.size() == 0) {
            // localhost only mode (either cnd.remote is not installed or no devhosts were specified
            for (CompilerSet cs : getCompilerSetManager(ExecutionEnvironmentFactory.getLocal()).getCompilerSets()) {
                CompilerSetPresenter csp = new CompilerSetPresenter(cs, cs.getName());
                if (csToSelect == cs) {
                    toSelect = csp;
                }
                allCS.add(csp);
            }
        }

        for (CompilerSetPresenter cs : allCS) {
            compilerCollectionComboBox.addItem(cs);
        }

        if (toSelect == null) {
            if (compilerCollectionComboBox.getItemCount() > 0) {
                compilerCollectionComboBox.setSelectedIndex(0);
            }
        }
        else {
            compilerCollectionComboBox.setSelectedItem(toSelect);
        }
        updateTabs();
    }

    private synchronized void updateTabs() {
        tabbedPane.removeAll();
        CompilerSetPresenter csp = ((CompilerSetPresenter) compilerCollectionComboBox.getSelectedItem());
        if (csp == null || csp.cs == null) {
            return;
        }
        CompilerSet compilerCollection = csp.cs;
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
            String key = ""+tool.getKind()+csp.displayName + tool.getPath(); // display name has collection name and hkey
            PredefinedPanel predefinedPanel = predefinedPanels.get(key);
            if (predefinedPanel == null) {
                predefinedPanel = new PredefinedPanel((CCCCompiler) tool, this);
                predefinedPanels.put(key, predefinedPanel);
            //modified = true; // See 126368
            } else {
                predefinedPanel.updateCompiler((CCCCompiler) tool);
            }
            tabbedPane.addTab(tool.getDisplayName(), predefinedPanel);
        }
    }

    public void setModified(boolean val) {
        modified = val;
    }

    public boolean isModified() {
        return modified;
    }

    public void fireFilesPropertiesChanged() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("fireFilesPropertiesChanged for ParserSettingsPanel");
        }
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < openProjects.length; i++) {
            NativeProjectProvider npv = openProjects[i].getLookup().lookup(NativeProjectProvider.class);
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

        collectionPanel.setOpaque(false);

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
                .add(compilerCollectionComboBox, 0, 310, Short.MAX_VALUE)
                .addContainerGap())
        );
        collectionPanelLayout.setVerticalGroup(
            collectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(collectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(compilerCollectionLabel)
                .add(compilerCollectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        scrollPane.setOpaque(false);

        tabPanel.setOpaque(false);

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
                .addContainerGap(272, Short.MAX_VALUE))
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
//            init();
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

    public boolean isChanged() {
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
        return predefinedPanels.values().toArray(new PredefinedPanel[predefinedPanels.size()]);
    }
    
//    private synchronized void init() {
//        if (!initialized) {
//            ServerList registry = (ServerList) Lookup.getDefault().lookup(ServerList.class);
//            if (registry != null) {
//                ServerRecord record = registry.getDefaultRecord();
//                if (record != null) {
//                    Logger rdlog = Logger.getLogger("cnd.remote.logger"); // NOI18N
//                    rdlog.fine("ParserSettingsPanel<Init>: Validating " + record.getName());
//                    record.validate(); // ensure the development host is initialized
//                }
//            }
//        }
//        initialized = true;
//    }
}
