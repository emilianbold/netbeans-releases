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
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ui.IsChangedListener;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class ParserSettingsPanel extends JPanel implements ChangeListener, ActionListener, IsChangedListener {

    private Map<String, PredefinedPanel> predefinedPanels = new HashMap<String, PredefinedPanel>();
    private boolean updating = false;
    private boolean modified = false;
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
        // This gets called from commitValidation and tp is null - its not a run-time problem
        // because the "real" way we create this a ToolsPanel exists. But not the commitValidation way!
        ToolsPanelSupport.addCompilerSetChangeListener(this);
        ToolsPanelSupport.addIsChangedListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!updating && isShowing()) {
            updateTabs();
        }
    }

    private static class CompilerSetPresenter {

        private CompilerSet cs;
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

    private CompilerSetManager getCompilerSetManager(ExecutionEnvironment execEnv) {
        ToolsCacheManager manager = ToolsPanelSupport.getToolsCacheManager();
        CompilerSetManager copy = manager.getCompilerSetManagerCopy(execEnv, true);
        while (copy.isPending()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // skip
            }
        }
        return copy;
    }
    
    private void updateCompilerCollections(final CompilerSet csToSelect) {

        final AtomicReference<CompilerSetPresenter> toSelect = new AtomicReference<CompilerSetPresenter>();
        final List<CompilerSetPresenter> allCS = new ArrayList<CompilerSetPresenter>();
        final Collection<? extends ServerRecord> servers = ServerList.getRecords();

        final Runnable uiUpdater = new Runnable() { //NOI18N
            @Override
            public void run() {
                compilerCollectionComboBox.removeActionListener(ParserSettingsPanel.this);
                compilerCollectionComboBox.removeAllItems();
                for (CompilerSetPresenter cs : allCS) {
                    compilerCollectionComboBox.addItem(cs);
                }

                if (toSelect.get() == null) {
                    if (compilerCollectionComboBox.getItemCount() > 0) {
                        compilerCollectionComboBox.setSelectedIndex(0);
                    }
                }
                else {
                    compilerCollectionComboBox.setSelectedItem(toSelect.get());
                }
                updateTabs();
                compilerCollectionComboBox.addActionListener(ParserSettingsPanel.this);
            }
        };

        final Runnable worker = new NamedRunnable("ParserSettings worker") { //NOI18N
            @Override
            protected void runImpl() {
                if (servers.size() > 1) {
                    for (ServerRecord record : servers) {
                        for (CompilerSet cs : getCompilerSetManager(record.getExecutionEnvironment()).getCompilerSets()) {
                            CompilerSetPresenter csp = new CompilerSetPresenter(cs, record.getDisplayName() + " : " + cs.getName()); //NOI18N
                            if (csToSelect == cs) {
                                toSelect.set(csp);
                            }
                            allCS.add(csp);
                        }
                    }
                } else {
                    assert servers.iterator().hasNext();
                    assert ! servers.iterator().next().isRemote();
                }

                if (allCS.isEmpty()) {
                    // localhost only mode (either cnd.remote is not installed or no devhosts were specified
                    for (CompilerSet cs : getCompilerSetManager(ExecutionEnvironmentFactory.getLocal()).getCompilerSets()) {
                        for (Tool tool : cs.getTools()) {
                            tool.waitReady(false);
                        }
                        CompilerSetPresenter csp = new CompilerSetPresenter(cs, cs.getName());
                        if (csToSelect == cs) {
                            toSelect.set(csp);
                        }
                        allCS.add(csp);
                    }
                }
                SwingUtilities.invokeLater(uiUpdater);
            }
        };
        RequestProcessor.getDefault().post(worker);
    }

    private synchronized void updateTabs() {
        tabbedPane.removeAll();
        CompilerSetPresenter csp = ((CompilerSetPresenter) compilerCollectionComboBox.getSelectedItem());
        if (csp == null || csp.cs == null) {
            return;
        }
        CompilerSet compilerCollection = csp.cs;
        if (compilerCollection.isUrlPointer()) {
            return;
        }
        // Show only the selected C and C++ compiler from the compiler collection
        ArrayList<Tool> toolSet = new ArrayList<Tool>();
        Tool cCompiler = compilerCollection.getTool(PredefinedToolKind.CCompiler);
        if (cCompiler != null && cCompiler.getPath().length() > 0) {
            toolSet.add(cCompiler);
        }
        Tool cppCompiler = compilerCollection.getTool(PredefinedToolKind.CCCompiler);
        if (cppCompiler != null && cppCompiler.getPath().length() > 0) {
            toolSet.add(cppCompiler);
        }
        for (Tool tool : toolSet) {
            String key = ""+tool.getKind()+csp.displayName + tool.getPath(); // display name has collection name and hkey
            PredefinedPanel predefinedPanel = predefinedPanels.get(key);
            if (predefinedPanel == null) {
                predefinedPanel = new PredefinedPanel((AbstractCompiler) tool, this);
                predefinedPanels.put(key, predefinedPanel);
            //modified = true; // See 126368
            } else {
                predefinedPanel.updateCompiler((AbstractCompiler) tool);
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

    @Override
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

        javax.swing.GroupLayout collectionPanelLayout = new javax.swing.GroupLayout(collectionPanel);
        collectionPanel.setLayout(collectionPanelLayout);
        collectionPanelLayout.setHorizontalGroup(
            collectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(collectionPanelLayout.createSequentialGroup()
                .addComponent(compilerCollectionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compilerCollectionComboBox, 0, 313, Short.MAX_VALUE)
                .addContainerGap())
        );
        collectionPanelLayout.setVerticalGroup(
            collectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(collectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(compilerCollectionLabel)
                .addComponent(compilerCollectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        scrollPane.setOpaque(false);

        tabPanel.setOpaque(false);

        javax.swing.GroupLayout tabPanelLayout = new javax.swing.GroupLayout(tabPanel);
        tabPanel.setLayout(tabPanelLayout);
        tabPanelLayout.setHorizontalGroup(
            tabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
        );
        tabPanelLayout.setVerticalGroup(
            tabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
        );

        tabbedPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParserSettingsPanel.class, "COMPILERS_TABBEDPANE_AN")); // NOI18N
        tabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserSettingsPanel.class, "COMPILERS_TABBEDPANE_AD")); // NOI18N

        scrollPane.setViewportView(tabPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(collectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(collectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(272, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(36, 36, 36)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
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
            updateCompilerCollections(ToolsPanelSupport.getCurrentCompilerSet());
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

    @Override
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
}
