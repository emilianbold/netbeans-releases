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
package org.netbeans.modules.web.livehtml.filter.groupscripts;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.StackTrace;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.filter.FilteredAnalysis;
import org.netbeans.modules.web.livehtml.filter.NotStackTraceFilter;
import org.netbeans.modules.web.livehtml.filter.OrStackTraceFilter;
import org.netbeans.modules.web.livehtml.ui.RevisionFilterPanel;

/**
 *
 * @author petr-podzimek
 */
public class GroupScriptsRevisionFilterPanel extends RevisionFilterPanel<GroupScriptsFilteredAnalysis> {
    
    /**
     * Creates new form NewRevisionFilterPanelService
     */
    public GroupScriptsRevisionFilterPanel() {
        initComponents();
    }

    @Override
    public void setAnalysis(Analysis analysis) {
        super.setAnalysis(analysis);
        
        Set<String> scripts = new HashSet<String>();
        
        for (int i = 0; i < analysis.getRevisionsCount(); i++) {
            final Revision revision = analysis.getRevision(i);
            if (revision == null) {
                continue;
            }
            final List<Object> values = revision.getStackTraceValues(StackTrace.SCRIPT);
            for (Object object : values) {
                if (object instanceof String) {
                    String s = (String) object;
                    scripts.add(s);
                }
            }
        }
        
        scriptsPanel.removeAll();
        for (String script : scripts) {
            final JCheckBox checkBox = new JCheckBox(script);
            checkBox.setToolTipText(script);
            checkBox.setName(script);
            
            scriptsPanel.add(checkBox);
        }
    }

    @Override
    public GroupScriptsFilteredAnalysis createFilteredAnalysis() {
        if (isClear()) {
            return null;
        }
        GroupScriptsFilteredAnalysis filteredAnalysis = 
                new GroupScriptsFilteredAnalysis(
                        createScriptRevisionFilter(), 
                        createstackTraceRevisionFilter(), 
                        ignoreWhiteSpacesCheckBox.isSelected(), 
                        getAnalysis());
        
        return filteredAnalysis;
    }

    @Override
    public void setFilteredAnalysis(GroupScriptsFilteredAnalysis filteredAnalysis) {
        if (filteredAnalysis == null) {
            clear();
            return;
        }
        
        final ScriptRevisionFilter revisionFilter = filteredAnalysis.getScriptRevisionFilter();
        setScriptRevisionFilter(revisionFilter);
        
        groupRevisionsCheckBox.setSelected(filteredAnalysis.getStackTraceRevisionFilter() != null);
        ignoreWhiteSpacesCheckBox.setSelected(filteredAnalysis.isIgnoreWhiteSpaces());
    }
    
    private void setScriptRevisionFilter(ScriptRevisionFilter scriptRevisionFilter) {
        if (scriptRevisionFilter == null) {
            setStackTraceFilter(null);
        } else {
            setStackTraceFilter(scriptRevisionFilter.getStackTraceFilter());
        }
    }
    
    private void setStackTraceFilter(StackTraceFilter stackTraceFilter) {
        if (stackTraceFilter == null) {
            return;
        }
        
        if (stackTraceFilter instanceof NotStackTraceFilter) {
            NotStackTraceFilter notStackTraceFilter = (NotStackTraceFilter) stackTraceFilter;
            final StackTraceFilter stackTraceFilter1 = notStackTraceFilter.getStackTraceFilter();
            if (stackTraceFilter1 instanceof OrStackTraceFilter) {
                OrStackTraceFilter orStackTraceFilter = (OrStackTraceFilter) stackTraceFilter1;
                for (StackTraceFilter stackTraceFilter2 : orStackTraceFilter.getStackTraceFilters()) {
                    if (stackTraceFilter2 instanceof ScriptStackTraceFilter) {
                        ScriptStackTraceFilter scriptStackTraceFilter = (ScriptStackTraceFilter) stackTraceFilter2;
                        
                        for (Component component : scriptsPanel.getComponents()) {
                            if (component instanceof JCheckBox) {
                                JCheckBox checkBox = (JCheckBox) component;
                                if (scriptStackTraceFilter.getScriptUrl().equals(checkBox.getName())) {
                                    checkBox.setSelected(true);
                                }
                            }
                        }
                        
                    }
                }
            }
        }
    }
    
    private StackTraceFilter createStackTraceFilter() {
        final List<String> selectedScripts = getSelectedScripts();
        
        if (selectedScripts == null || selectedScripts.isEmpty()) {
            return null;
        }
        
        List<StackTraceFilter> stackTraceFilters = new ArrayList<StackTraceFilter>();
        for (String script : selectedScripts) {
            stackTraceFilters.add((new ScriptStackTraceFilter(script)));
        }
        
        StackTraceFilter stackTraceFilter = new NotStackTraceFilter(new OrStackTraceFilter(stackTraceFilters));
        return stackTraceFilter;
    }
    
    private ScriptRevisionFilter createScriptRevisionFilter() {
        final StackTraceFilter createStackTraceFilter = createStackTraceFilter();
        ScriptRevisionFilter scriptRevisionFilter = createStackTraceFilter == null ? null : new ScriptRevisionFilter(createStackTraceFilter);
        
        return scriptRevisionFilter;
    }
    
    private StackTraceRevisionFilter createstackTraceRevisionFilter() {
        if (groupRevisionsCheckBox.isSelected()) {
            return new StackTraceRevisionFilter();
        } else {
            return null;
        }
    }

    private List<String> getSelectedScripts() {
        List<String> selectedScripts = new ArrayList<String>();
        for (Component component : scriptsPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (checkBox.isSelected()) {
                    selectedScripts.add(checkBox.getToolTipText());
                }
            }
        }
        return selectedScripts;
    }
    
    private boolean isClear() {
        final List<String> selectedScripts = getSelectedScripts();
        return (selectedScripts == null || selectedScripts.isEmpty()) && 
                !groupRevisionsCheckBox.isSelected() && 
                !ignoreWhiteSpacesCheckBox.isSelected();
    }
    
    private void clear() {
        for (Component component : scriptsPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                checkBox.setSelected(false);
            }
        }
        groupRevisionsCheckBox.setSelected(false);
        ignoreWhiteSpacesCheckBox.setSelected(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        scriptsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        groupRevisionsCheckBox = new javax.swing.JCheckBox();
        ignoreWhiteSpacesCheckBox = new javax.swing.JCheckBox();
        clearFiltersButton = new javax.swing.JButton();

        scriptsPanel.setOpaque(false);
        scriptsPanel.setLayout(new javax.swing.BoxLayout(scriptsPanel, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(scriptsPanel);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GroupScriptsRevisionFilterPanel.class, "GroupScriptsRevisionFilterPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(groupRevisionsCheckBox, org.openide.util.NbBundle.getMessage(GroupScriptsRevisionFilterPanel.class, "GroupScriptsRevisionFilterPanel.groupRevisionsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ignoreWhiteSpacesCheckBox, org.openide.util.NbBundle.getMessage(GroupScriptsRevisionFilterPanel.class, "GroupScriptsRevisionFilterPanel.ignoreWhiteSpacesCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(clearFiltersButton, org.openide.util.NbBundle.getMessage(GroupScriptsRevisionFilterPanel.class, "GroupScriptsRevisionFilterPanel.clearFiltersButton.text")); // NOI18N
        clearFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFiltersButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupRevisionsCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ignoreWhiteSpacesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFiltersButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(groupRevisionsCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoreWhiteSpacesCheckBox))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(clearFiltersButton)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearFiltersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFiltersButtonActionPerformed
        clear();
    }//GEN-LAST:event_clearFiltersButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFiltersButton;
    private javax.swing.JCheckBox groupRevisionsCheckBox;
    private javax.swing.JCheckBox ignoreWhiteSpacesCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel scriptsPanel;
    // End of variables declaration//GEN-END:variables
}
