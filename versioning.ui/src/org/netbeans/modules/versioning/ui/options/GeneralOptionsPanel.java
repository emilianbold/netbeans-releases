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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.versioning.ui.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.Utils;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

final class GeneralOptionsPanel extends javax.swing.JPanel implements ActionListener {
    
    private final GeneralOptionsPanelController controller;
    
    GeneralOptionsPanel (GeneralOptionsPanelController controller) {
        this.controller = controller;        
        initComponents();
        cmbVersioningSystems.setRenderer(new Renderer());
        cmbVersioningSystems.addActionListener(this);
        btnRemove.addActionListener(this);
        btnAdd.addActionListener(this);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();        
    }

    @Override
    public void removeNotify() {        
        super.removeNotify();
    }

    private void fillDisconnectedFolders () {
        if (cmbVersioningSystems.getSelectedItem() instanceof VersioningSystem) {
            String[] disconnected = Utils.getDisconnectedRoots(((VersioningSystem) cmbVersioningSystems.getSelectedItem()));
            DefaultListModel model = new DefaultListModel();
            for (String f : disconnected) {
                model.addElement(f);
            }
            lstDisconnectedFolders.setModel(model);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cmbVersioningSystems = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDisconnectedFolders = new javax.swing.JList();
        btnRemove = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        jLabel1.setLabelFor(cmbVersioningSystems);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(lstDisconnectedFolders);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.jLabel2.text")); // NOI18N

        jScrollPane1.setViewportView(lstDisconnectedFolders);

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.btnRemove.text")); // NOI18N
        btnRemove.setToolTipText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.btnRemove.TTtext")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.btnAdd.text")); // NOI18N
        btnAdd.setToolTipText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.btnAdd.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_OptionsPanel.disconnectedFolders.title")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbVersioningSystems, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRemove)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAdd))
                            .addComponent(jScrollPane1)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbVersioningSystems, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemove)
                    .addComponent(btnAdd))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load () {
        fillVersioningSystems();
    }
    
    void store() {
        
    }
    
    boolean valid() {
        return true;
    }
 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnRemove;
    private javax.swing.JComboBox cmbVersioningSystems;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList lstDisconnectedFolders;
    // End of variables declaration//GEN-END:variables

    private void fillVersioningSystems () {
        List<VersioningSystem> systems = new LinkedList<VersioningSystem>();
        for (VersioningSystem system : Lookup.getDefault().lookupAll(VersioningSystem.class)) {
            systems.add(system);
        }
        cmbVersioningSystems.setModel(new DefaultComboBoxModel(systems.toArray(new VersioningSystem[systems.size()])));
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == cmbVersioningSystems) {
            fillDisconnectedFolders();
        } else if (e.getSource() == btnRemove) {
            if (cmbVersioningSystems.getSelectedItem() instanceof VersioningSystem && lstDisconnectedFolders.getSelectedValue() != null) {
                String f = (String) lstDisconnectedFolders.getSelectedValue();
                Utils.connectRepository((VersioningSystem) cmbVersioningSystems.getSelectedItem(), f);
                fillDisconnectedFolders();
                refreshSystems();
            }
        } else if (e.getSource() == btnAdd) {
            if (cmbVersioningSystems.getSelectedItem() instanceof VersioningSystem) {
                VersioningSystem vcs = (VersioningSystem) cmbVersioningSystems.getSelectedItem();
                File f = new FileChooserBuilder("VersioningOptions.disconnected").setTitle(NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_DisconnectingFolder.title")) //NOI18N
                        .setDirectoriesOnly(true).setFileHiding(true).showOpenDialog();
                if (f != null && (vcs.getTopmostManagedAncestor(VCSFileProxy.createFileProxy(f)) != null)) {
                    Utils.disconnectRepository(vcs, f.getAbsolutePath());
                    fillDisconnectedFolders();
                    refreshSystems();
                }
            }
        }
    }

    private void refreshSystems () {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run () {
                Utils.versionedRootsChanged();
            }
        }).schedule(100);
    }
    
    private static class Renderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof VersioningSystem) {
                value = ((VersioningSystem) value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
