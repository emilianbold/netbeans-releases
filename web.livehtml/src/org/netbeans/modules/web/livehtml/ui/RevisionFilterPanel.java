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
package org.netbeans.modules.web.livehtml.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import org.netbeans.modules.web.livehtml.Analysis;
import org.netbeans.modules.web.livehtml.AnalysisStorage;
import org.netbeans.modules.web.livehtml.filter.AndRevisionFilter;
import org.netbeans.modules.web.livehtml.filter.NotRevisionFilter;
import org.netbeans.modules.web.livehtml.filter.OrRevisionFilter;
import org.netbeans.modules.web.livehtml.Revision;
import org.netbeans.modules.web.livehtml.filter.RevisionFilter;
import org.netbeans.modules.web.livehtml.filter.ScriptRevisionFilter;

/**
 *
 * @author petr-podzimek
 */
public class RevisionFilterPanel extends javax.swing.JPanel {
    
    private Analysis analysis;
    
    /**
     * Creates new form RevisionFilterPanel
     */
    public RevisionFilterPanel() {
        initComponents();
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
        
        Set<String> scripts = new HashSet<String>();
        
        for (int i = 0; i < analysis.getRevisionsCount(); i++) {
            final Revision revision = analysis.getRevision(i, true);
            if (revision == null) {
                continue;
            }
            final List<Object> values = revision.getCallStackValues("script");
            for (Object object : values) {
                if (object instanceof String) {
                    String s = (String) object;
                    scripts.add(s);
                }
            }
        }
        
        scriptsPane.removeAll();
        for (String script : scripts) {
            final JCheckBox checkBox = new JCheckBox(script);
            checkBox.setToolTipText(script);
            
            scriptsPane.add(checkBox);
        }
    }
    
    private List<String> getSelectedScripts() {
        List<String> selectedScripts = new ArrayList<String>();
        for (Component component : scriptsPane.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (checkBox.isSelected()) {
                    selectedScripts.add(checkBox.getToolTipText());
                }
            }
        }
        return selectedScripts;
    }
    
    public void updateRevisionFilter(RevisionFilter revisionFilter) {
        if (revisionFilter != null && revisionFilter instanceof OrRevisionFilter) {
            OrRevisionFilter orRevisionFilter = (OrRevisionFilter) revisionFilter;
            for (RevisionFilter revisionFilter1 : orRevisionFilter.getRevisionFilters()) {
                if (revisionFilter1 instanceof NotRevisionFilter) {
                    NotRevisionFilter notRevisionFilter = (NotRevisionFilter) revisionFilter1;
                    final RevisionFilter sourceRevisionFilter = notRevisionFilter.getRevisionFilter();
                    if (sourceRevisionFilter instanceof ScriptRevisionFilter) {
                        ScriptRevisionFilter scriptRevisionFilter = (ScriptRevisionFilter) sourceRevisionFilter;
                        for (Component component : scriptsPane.getComponents()) {
                            if (component instanceof JCheckBox) {
                                JCheckBox checkBox = (JCheckBox) component;
                                if (checkBox.getText().equals(scriptRevisionFilter.getScriptUrl())) {
                                    checkBox.setSelected(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public RevisionFilter getRevisionFilter() {
        final List<String> selectedScripts = getSelectedScripts();
        
        if (selectedScripts == null || selectedScripts.isEmpty()) {
            return null;
        }
        
        List<RevisionFilter> revisionFilters = new ArrayList<RevisionFilter>();
        for (String script : selectedScripts) {
            revisionFilters.add(new NotRevisionFilter(new ScriptRevisionFilter(script)));
        }
        
        RevisionFilter revisionFilter = new OrRevisionFilter(revisionFilters);
        
        return revisionFilter;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        muteREvisionsLabel = new javax.swing.JLabel();
        scriptsScrollPane = new javax.swing.JScrollPane();
        scriptsPane = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(muteREvisionsLabel, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.muteREvisionsLabel.text")); // NOI18N

        scriptsPane.setOpaque(false);
        scriptsPane.setLayout(new javax.swing.BoxLayout(scriptsPane, javax.swing.BoxLayout.Y_AXIS));
        scriptsScrollPane.setViewportView(scriptsPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scriptsScrollPane)
            .addComponent(muteREvisionsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(muteREvisionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scriptsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel muteREvisionsLabel;
    private javax.swing.JPanel scriptsPane;
    private javax.swing.JScrollPane scriptsScrollPane;
    // End of variables declaration//GEN-END:variables
}
