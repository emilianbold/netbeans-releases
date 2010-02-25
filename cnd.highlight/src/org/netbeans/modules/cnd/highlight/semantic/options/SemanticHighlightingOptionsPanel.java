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
import javax.swing.UIManager;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelutil.NamedEntity;
import org.netbeans.modules.cnd.highlight.semantic.SemanticEntitiesProvider;
import org.netbeans.modules.cnd.highlight.semantic.SemanticEntity;
import org.netbeans.modules.cnd.modelutil.NamedEntityOptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sergey Grinev
 */
public class SemanticHighlightingOptionsPanel extends javax.swing.JPanel implements ActionListener {

    public SemanticHighlightingOptionsPanel() {
        initComponents();
        initGeneratedComponents();
        initMnemonics();
        cbKeepMarks.addActionListener(this);
        setName("TAB_SemanticHighlightingTab"); // NOI18N (used as a pattern...)
        // background color fixup
        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            jPanel1.setOpaque(false);
            setOpaque(false);
        }
    }

    // for OptionsPanelSupport
    private boolean isChanged = false;

    void applyChanges() {
        SemanticHighlightingOptions.instance().setEnableMarkOccurrences(cbMarkOccurrences.isSelected());
        SemanticHighlightingOptions.instance().setKeepMarks(cbKeepMarks.isSelected());

        for (Entity e : entities) {
            NamedEntityOptions.instance().setEnabled(e.se, e.cb.isSelected());
        }
        SemanticHighlightingOptions.instance().propertyChange(null);
        isChanged = false;
    }

    void update() {
        cbMarkOccurrences.setSelected(SemanticHighlightingOptions.instance().getEnableMarkOccurrences());
        cbKeepMarks.setSelected(SemanticHighlightingOptions.instance().getKeepMarks());

        for (Entity e : entities) {
            e.cb.setSelected(NamedEntityOptions.instance().isEnabled(e.se));
        }
        
        updateValidation();
    }

    void cancel() {
        isChanged = false;
    }

    boolean isChanged() {
        return isChanged;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        isChanged = true;
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

        public final NamedEntity se;
        public final JCheckBox cb;

        public Entity(NamedEntity se, JCheckBox cb) {
            this.se = se;
            this.cb = cb;
        }
    }
    private List<Entity> entities = new ArrayList<Entity>();
    JCheckBox cbMacros;
    
    private void addEntity(NamedEntity ne) {
        JCheckBox cb = new JCheckBox();
        cb.setMnemonic(getString("Show-" + ne.getName() + "-mnemonic").charAt(0)); //NOI18N
        cb.getAccessibleContext().setAccessibleDescription(getString("Show-" + ne.getName() + "-AD")); //NOI18N
        cb.setText(getString("Show-" + ne.getName())); //NOI18N
        cb.setToolTipText(getString("Show-" + ne.getName() + "-AD")); //NOI18N
        cb.setOpaque(false);
        entities.add(new Entity(ne, cb));
    }

    private void initGeneratedComponents() {
        for (SemanticEntity se : SemanticEntitiesProvider.instance().get()) {
            addEntity(se);
        }
        for (NamedEntity ee : Lookup.getDefault().lookupResult(CsmErrorProvider.class).allInstances()) {
            addEntity(ee);
        }

        addEntity(new NamedEntity(){
            @Override
            public String getName() {
                return "reparse-on-document-changed"; //NOI18N
            }
            @Override
            public boolean isEnabledByDefault() {
                return true;
            }
        });

        NamedEntity overridesEntity = new NamedEntity() {
            public String getName() {
                return "overrides-annotations"; //NOI18N
            }
            public boolean isEnabledByDefault() {
                return true;
            }
        };
        addEntity(overridesEntity);

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
