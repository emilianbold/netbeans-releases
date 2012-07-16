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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
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
    private DefaultListModel listModel = new DefaultListModel();
    
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
        
        listModel.clear();
        for (String script : scripts) {
            listModel.addElement(script);
        }
    }
    
    private List<String> getSelectedScripts() {
        final Object[] selectedValues = jList1.getSelectedValues();
        List<String> selectedScripts = new ArrayList<String>();
        for (Object object : selectedValues) {
            if (object instanceof String) {
                String s = (String) object;
                selectedScripts.add(s);
            }
        }
        return selectedScripts;
    }
    
    public RevisionFilter getRevisionFilter() {
        final List<String> selectedScripts = getSelectedScripts();
        
        List<RevisionFilter> revisionFilters = new ArrayList<RevisionFilter>();
        for (String script : selectedScripts) {
            revisionFilters.add(new ScriptRevisionFilter(script));
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
        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.jCheckBox1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(RevisionFilterPanel.class, "RevisionFilterPanel.jCheckBox2.text")); // NOI18N

        jList1.setModel(listModel);
        jScrollPane2.setViewportView(jList1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jCheckBox1)
                            .addComponent(jCheckBox2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
