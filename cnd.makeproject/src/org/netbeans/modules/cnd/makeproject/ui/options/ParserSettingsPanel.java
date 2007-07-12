/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.ui.options;

import java.awt.event.ActionEvent;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import java.awt.event.ActionListener;
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
    private ToolsPanel tp;
    /**
     * Creates new form ParserSettingsPanel
     */
    public ParserSettingsPanel() {
        setName("TAB_CodeAssistanceTab");// NOI18N
        initComponents();
        
        //infoTextArea.setBackground(collectionPanel.getBackground());
        //setPreferredSize(new java.awt.Dimension(600, 700));
        
        // Accessible Description
        getAccessibleContext().setAccessibleDescription(getString("MANAGE_COMPILERS_SETTINGS_AD"));
        compilerCollectionComboBox.getAccessibleContext().setAccessibleDescription(getString("COMPILER_COLLECTION_AD"));
        tabbedPane.getAccessibleContext().setAccessibleDescription(getString("COMPILERS_TABBEDPANE_AD"));
        tabbedPane.getAccessibleContext().setAccessibleName(getString("COMPILERS_TABBEDPANE_AN"));
        tp = ToolsPanel.getToolsPanel();
        if (tp != null) {
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
        CompilerSet compilerCollection = (CompilerSet)compilerCollectionComboBox.getSelectedItem();
        for (Tool tool : compilerCollection.getTools()) {
            if (tool instanceof CCCCompiler) { // FIXUP: should implement/use 'capability' of tool
                PredefinedPanel predefinedPanel = (PredefinedPanel)predefinedPanels.get(tool);
                if (predefinedPanel == null) {
                    predefinedPanel = new PredefinedPanel((CCCCompiler)tool, this);
                    predefinedPanels.put(tool, predefinedPanel);
                }
                tabbedPane.addTab(tool.getDisplayName(), predefinedPanel);
            }
        }
    }
    
    public void fireFilesPropertiesChanged() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("fireFilesPropertiesChanged for ParserSettingsPanel");
	Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
	for (int i = 0; i < openProjects.length; i++) {
            NativeProjectProvider npv = (NativeProjectProvider)openProjects[i].getLookup().lookup(NativeProjectProvider.class );
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        collectionPanel = new javax.swing.JPanel();
        compilerCollectionLabel = new javax.swing.JLabel();
        compilerCollectionComboBox = new javax.swing.JComboBox();
        tabPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();

        compilerCollectionLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("COMPILER_COLLECTION_MN").charAt(0));
        compilerCollectionLabel.setLabelFor(compilerCollectionComboBox);
        compilerCollectionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("COMPILER_COLLECTION_LBL"));

        org.jdesktop.layout.GroupLayout collectionPanelLayout = new org.jdesktop.layout.GroupLayout(collectionPanel);
        collectionPanel.setLayout(collectionPanelLayout);
        collectionPanelLayout.setHorizontalGroup(
            collectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(collectionPanelLayout.createSequentialGroup()
                .add(compilerCollectionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(compilerCollectionComboBox, 0, 217, Short.MAX_VALUE)
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
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
        );
        tabPanelLayout.setVerticalGroup(
            tabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(collectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(collectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel collectionPanel;
    private javax.swing.JComboBox compilerCollectionComboBox;
    private javax.swing.JLabel compilerCollectionLabel;
    private javax.swing.JPanel tabPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
    
    private static String getString(String s) {
        return NbBundle.getMessage(ParserSettingsPanel.class, s);
    }

    void update() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("update for ParserSettingsPanel");
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
        if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("cancel for ParserSettingsPanel");
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
        if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("isDataValid for ParserSettingsPanel is " + isDataValid);
        return isDataValid;
    }

    boolean isChanged() {
        boolean isChanged = false;
        PredefinedPanel[] viewedPanels = getPredefinedPanels();
        for (int i = 0; i < viewedPanels.length; i++) {
            isChanged |= viewedPanels[i].isChanged();
        }
        if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("isChanged for ParserSettingsPanel is " + isChanged);
        return isChanged;
    }
        
    public void save() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("save for ParserSettingsPanel");
        PredefinedPanel[] viewedPanels = getPredefinedPanels();
        boolean wasChanges = false;
        for (int i = 0; i < viewedPanels.length; i++) {
            wasChanges |= viewedPanels[i].save();
        }
        if (wasChanges) {
            if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("fireFilesPropertiesChanged in save for ParserSettingsPanel");
            fireFilesPropertiesChanged();
        } else {
            if (CodeAssistancePanelController.TRACE_CODEASSIST) System.err.println("not need to fireFilesPropertiesChanged in save for ParserSettingsPanel");
        }
    }
    
    private PredefinedPanel[] getPredefinedPanels() {
        return (PredefinedPanel[])predefinedPanels.values().toArray(new PredefinedPanel[predefinedPanels.size()]);
    }
}
