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
package org.netbeans.modules.web.livehtml.filter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.StackTrace;

/**
 *
 * @author petr-podzimek
 */
public class RevisionFilterPanel extends javax.swing.JPanel {
    
    private Analysis analysis;

    /**
     * Creates new form NewRevisionFilterPanelService
     */
    public RevisionFilterPanel() {
        initComponents();
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
        
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

    public Analysis getAnalysis() {
        return analysis;
    }
    
    private void selectScripts(Collection<String> scriptLocations) {
        for (Component component : scriptsPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                checkBox.setSelected(scriptLocations.contains(checkBox.getName()));
            }
        }
    }

    public FilteredAnalysis createFilteredAnalysis() {
        if (isClear()) {
            return null;
        }
        FilteredAnalysis filteredAnalysis = 
                new FilteredAnalysis(
                        getSelectedScripts(), 
                        groupByStackTraceCheckBox.isSelected(), 
                        groupByWhiteSpacesCheckBox.isSelected(), 
                        getAnalysis());
        
        return filteredAnalysis;
    }

    public void setFilteredAnalysis(FilteredAnalysis filteredAnalysis) {
        if (filteredAnalysis == null) {
            clear();
            return;
        }
        
        groupByStackTraceCheckBox.setSelected(filteredAnalysis.isGroupIdenticalStackTraces());
        groupByWhiteSpacesCheckBox.setSelected(filteredAnalysis.isGroupWhiteSpaces());
        selectScripts(filteredAnalysis.getGroupScriptLocations());
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
                !groupByStackTraceCheckBox.isSelected() && 
                !groupByWhiteSpacesCheckBox.isSelected();
    }
    
    private void clear() {
        selectScripts(Collections.<String>emptyList());
        groupByStackTraceCheckBox.setSelected(false);
        groupByWhiteSpacesCheckBox.setSelected(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptsScrollPane = new javax.swing.JScrollPane();
        scriptsPanel = new javax.swing.JPanel();
        scriptsLabel = new javax.swing.JLabel();
        groupByStackTraceCheckBox = new javax.swing.JCheckBox();
        groupByWhiteSpacesCheckBox = new javax.swing.JCheckBox();
        clearFiltersButton = new javax.swing.JButton();

        scriptsPanel.setOpaque(false);
        scriptsPanel.setLayout(new javax.swing.BoxLayout(scriptsPanel, javax.swing.BoxLayout.Y_AXIS));
        scriptsScrollPane.setViewportView(scriptsPanel);

        org.openide.awt.Mnemonics.setLocalizedText(scriptsLabel, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.scriptsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(groupByStackTraceCheckBox, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.groupByStackTraceCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(groupByWhiteSpacesCheckBox, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.groupByWhiteSpacesCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(clearFiltersButton, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.clearFiltersButton.text")); // NOI18N
        clearFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFiltersButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scriptsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scriptsScrollPane)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupByStackTraceCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(groupByWhiteSpacesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFiltersButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scriptsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scriptsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(groupByStackTraceCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(groupByWhiteSpacesCheckBox))
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
    private javax.swing.JCheckBox groupByStackTraceCheckBox;
    private javax.swing.JCheckBox groupByWhiteSpacesCheckBox;
    private javax.swing.JLabel scriptsLabel;
    private javax.swing.JPanel scriptsPanel;
    private javax.swing.JScrollPane scriptsScrollPane;
    // End of variables declaration//GEN-END:variables
}
