/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.cnd.highlight.semantic.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class SemanticHighlightingOptionsPanel extends javax.swing.JPanel implements ActionListener {

    public SemanticHighlightingOptionsPanel() {
        initComponents();
        initGeneratedComponents();
        initMnemonics();
        cbMarkOccurrences.addActionListener(this);
        cbKeepMarks.addActionListener(this);
        setName("TAB_SemanticHighlightingTab"); // NOI18N (used as a pattern...)
    }

    // for OptionsPanelSupport
    private boolean isChanged = false;

    void applyChanges() {
        SemanticHighlightingOptions.instance().setEnableMarkOccurrences(cbMarkOccurrences.isSelected());
        SemanticHighlightingOptions.instance().setKeepMarks(cbKeepMarks.isSelected());

        for (Entity e : entities) {
            NamedOption.getAccessor().setBoolean(e.se.getName(), e.cb.isSelected());
        }
        SemanticHighlightingOptions.instance().propertyChange(null);
        isChanged = false;
    }

    void update() {
        cbMarkOccurrences.setSelected(SemanticHighlightingOptions.instance().getEnableMarkOccurrences());
        cbKeepMarks.setSelected(SemanticHighlightingOptions.instance().getKeepMarks());

        for (Entity e : entities) {
            e.cb.setSelected(NamedOption.getAccessor().getBoolean(e.se.getName()));
        }
        
        updateValidation();
        isChanged = false;
    }

    void cancel() {
        isChanged = false;
    }

    boolean isChanged() {
        return isChanged;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean changed = cbMarkOccurrences.isSelected() != SemanticHighlightingOptions.instance().getEnableMarkOccurrences()
                || cbKeepMarks.isSelected() != SemanticHighlightingOptions.instance().getKeepMarks();

        for (Entity entity : entities) {
            changed |= entity.cb.isSelected() != NamedOption.getAccessor().getBoolean(entity.se.getName());
        }
        isChanged = changed;
    }

    private void updateValidation() {
        cbKeepMarks.setEnabled(cbMarkOccurrences.isSelected());
    }

    private void initMnemonics() {
        cbMarkOccurrences.setMnemonic(getString("EnableMarkOccurrences_Mnemonic").charAt(0));
        cbKeepMarks.setMnemonic(getString("KeepMarks_Mnemonic").charAt(0));

        cbMarkOccurrences.setToolTipText(getString("EnableMarkOccurrences_AD"));
        cbKeepMarks.setToolTipText(getString("KeepMarks_AD"));

        cbMarkOccurrences.getAccessibleContext().setAccessibleDescription(getString("EnableMarkOccurrences_AD"));
        cbKeepMarks.getAccessibleContext().setAccessibleDescription(getString("KeepMarks_AD"));
    }

    private static class Entity {

        public final NamedOption se;
        public final JCheckBox cb;

        public Entity(NamedOption se, JCheckBox cb) {
            this.se = se;
            this.cb = cb;
        }
    }
    private final List<Entity> entities = new ArrayList<>();
    JCheckBox cbMacros;
    
    private void addEntity(NamedOption ne) {
        JCheckBox cb = new JCheckBox();
        Mnemonics.setLocalizedText(cb, ne.getDisplayName());
        if (ne.getDescription() != null) {
            cb.setToolTipText(ne.getDescription());
        }
        cb.setOpaque(false);
        cb.addActionListener(this);
        entities.add(new Entity(ne, cb));
    }

    private void initGeneratedComponents() {
        for(NamedOption ee : Lookups.forPath(NamedOption.HIGHLIGTING_CATEGORY).lookupAll(NamedOption.class)) {
            if (ee.isVisible()) {
                addEntity(ee);
            }
        }
        GroupLayout layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(layout);

        ParallelGroup pg = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup sg = layout.createSequentialGroup();
        for (Entity e : entities) {
            pg.addComponent(e.cb);
            sg.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(e.cb);
        }

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pg))).addContainerGap()));

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(sg.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbKeepMarks = new javax.swing.JCheckBox();
        cbMarkOccurrences = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();

        cbKeepMarks.setText(getString("KeepMarks"));
        cbKeepMarks.setOpaque(false);

        cbMarkOccurrences.setText(getString("EnableMarkOccurrences"));
        cbMarkOccurrences.setOpaque(false);
        cbMarkOccurrences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMarkOccurrencesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 214, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(cbKeepMarks))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cbMarkOccurrences))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbMarkOccurrences)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbKeepMarks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SemanticHighlightingOptionsPanel.class, "SemanticHighlightingOptionsPanel_AN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SemanticHighlightingOptionsPanel.class, "SemanticHighlightingOptionsPanel_AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbMarkOccurrencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMarkOccurrencesActionPerformed
        updateValidation();
    }//GEN-LAST:event_cbMarkOccurrencesActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbKeepMarks;
    private javax.swing.JCheckBox cbMarkOccurrences;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(SemanticHighlightingOptionsPanel.class, key);
    }
}
