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

package org.netbeans.modules.websvc.axis2.actions;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.axis2.Axis2ModelProvider;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.Libraries;
import org.netbeans.modules.websvc.axis2.config.model.LibraryRef;


/**
 *
 * @author Milan Kuchtiak
 */
public class LibrarySelectionPanel extends javax.swing.JPanel {

    private Project project;
    private List<URL> libraries;
    private List<URL> selectedLibraries;
    private String persistenceUnit;

    /** Creates new form CrudSetupPanel */
    public LibrarySelectionPanel(Project project) {
        this.project = project;
        initComponents();
        try {
            libraries = AxisUtils.getReferencedJars(project);
        } catch (java.io.IOException ex) {
            libraries = Collections.<URL>emptyList();
        }
        
        selectedLibraries = new ArrayList<URL>();
        Axis2ModelProvider axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
        Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
        if (axis2Model != null) {
            Libraries references = axis2Model.getRootComponent().getLibraries();
            if (references != null) {
                List<LibraryRef> refs = references.getLibraryRefs();
                for (LibraryRef ref:refs) {
                    boolean found = false;
                    for (URL lib:libraries) {
                        if (ref.getNameAttr().equals(AxisUtils.getJarName(lib))) {
                            selectedLibraries.add(lib);
                            libraries.remove(lib);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        try {
                            selectedLibraries.add(new URL("file:"+ref.getNameAttr())); // NOI18N
                        } catch (MalformedURLException ex) {
                            
                        }
                    }
                }
            }
        }
        
        ListModel listModel = new URLListModel(libraries);
        listAvailable.setModel(listModel);
        listAvailable.setCellRenderer(ENTITY_LIST_RENDERER);
        
        
        listModel = new URLListModel(selectedLibraries);
        listSelected.setModel(listModel);
        
        ListSelectionListener selectionListener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }
        };
        
        listAvailable.getSelectionModel().addListSelectionListener(selectionListener);
        listSelected.getSelectionModel().addListSelectionListener(selectionListener);
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listAvailable = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        listSelected = new javax.swing.JList();
        labelAvailableLibraries = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        buttonAdd = new javax.swing.JButton();
        buttonRemove = new javax.swing.JButton();
        buttonAddAll = new javax.swing.JButton();
        buttonRemoveAll = new javax.swing.JButton();
        labelSelectedLibraries = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        listAvailable.setCellRenderer(ENTITY_LIST_RENDERER);
        jScrollPane1.setViewportView(listAvailable);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/axis2/actions/Bundle"); // NOI18N
        listAvailable.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_AvailableEntityClasses")); // NOI18N

        listSelected.setCellRenderer(ENTITY_LIST_RENDERER);
        jScrollPane2.setViewportView(listSelected);
        listSelected.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_SelectedEntityClasses")); // NOI18N

        labelAvailableLibraries.setLabelFor(listAvailable);
        org.openide.awt.Mnemonics.setLocalizedText(labelAvailableLibraries, bundle.getString("LBL_AvailableLibraries")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonAdd, org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "LBL_Add")); // NOI18N
        buttonAdd.setEnabled(false);
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonRemove, org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "LBL_Remove")); // NOI18N
        buttonRemove.setEnabled(false);
        buttonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonAddAll, org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "LBL_AddAll")); // NOI18N
        buttonAddAll.setEnabled(false);
        buttonAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonRemoveAll, org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "LBL_RemoveAll")); // NOI18N
        buttonRemoveAll.setEnabled(false);
        buttonRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonRemove, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addComponent(buttonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addComponent(buttonRemoveAll, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                    .addComponent(buttonAddAll, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(buttonAdd)
                .addGap(18, 18, 18)
                .addComponent(buttonRemove)
                .addGap(18, 18, 18)
                .addComponent(buttonAddAll)
                .addGap(18, 18, 18)
                .addComponent(buttonRemoveAll)
                .addContainerGap(108, Short.MAX_VALUE))
        );

        buttonAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "DESC_AddEntityClass")); // NOI18N
        buttonRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "DESC_RemoveEntityClass")); // NOI18N
        buttonAddAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "DESC_AddAllEntityClasses")); // NOI18N
        buttonRemoveAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "DESC_RemoveAllEntityClasses")); // NOI18N

        labelSelectedLibraries.setLabelFor(listSelected);
        org.openide.awt.Mnemonics.setLocalizedText(labelSelectedLibraries, bundle.getString("LBL_SelectedLibraries")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(248, 248, 248)
                        .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelAvailableLibraries)
                        .addGap(288, 288, 288)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelSelectedLibraries)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(410, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAvailableLibraries)
                    .addComponent(labelSelectedLibraries))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                    .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(33, 33, 33)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        labelAvailableLibraries.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "DESC_AvailableEntityClasses")); // NOI18N
        labelSelectedLibraries.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "DESC_SelectedEntityClasses")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "LBL_Libraries"), jPanel1); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LibrarySelectionPanel.class, "DESC_LibrariesPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
    URLListModel sourceModel = (URLListModel) listSelected.getModel();
    URLListModel destModel = (URLListModel) listAvailable.getModel();
    for (Object entity : listSelected.getSelectedValues()) {
        sourceModel.removeElement((URL) entity);
        if (! destModel.contains(entity)) {
            destModel.addElement(entity);
        }
    }
    refreshModel();
    updateButtons();
}//GEN-LAST:event_removeActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        URLListModel sourceModel = (URLListModel) listAvailable.getModel();
        URLListModel destModel = (URLListModel) listSelected.getModel();
        for (Object entity : listAvailable.getSelectedValues()) {
            sourceModel.removeElement((URL) entity);
            if (! destModel.contains(entity)) {
                destModel.addElement(entity);
            }
        }
        refreshModel();
        updateButtons();
}//GEN-LAST:event_addActionPerformed

    private void buttonRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveAllActionPerformed
        URLListModel model = (URLListModel) listAvailable.getModel();
        URLListModel selectedModel = (URLListModel) listSelected.getModel();
        for (int i = 0; i < selectedModel.getSize(); i++) {
            model.addElement(selectedModel.elementAt(i));
        }
        selectedModel.clear();
        refreshModel();
        updateButtons();
    }//GEN-LAST:event_buttonRemoveAllActionPerformed

    private void buttonAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddAllActionPerformed
        URLListModel model = (URLListModel) listSelected.getModel();
        URLListModel availableModel = (URLListModel) listAvailable.getModel();
        for (int i = 0; i < availableModel.getSize(); i++) {
            model.addElement(availableModel.elementAt(i));
        }
        availableModel.clear();
        refreshModel();
        updateButtons();
    }//GEN-LAST:event_buttonAddAllActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonAddAll;
    private javax.swing.JButton buttonRemove;
    private javax.swing.JButton buttonRemoveAll;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelAvailableLibraries;
    private javax.swing.JLabel labelSelectedLibraries;
    private javax.swing.JList listAvailable;
    private javax.swing.JList listSelected;
    private javax.swing.JPanel panelButtons;
    // End of variables declaration//GEN-END:variables


    private void updateButtons() {
        buttonAdd.setEnabled(listAvailable.getSelectedValues().length > 0);
        buttonAddAll.setEnabled(listAvailable.getModel().getSize() > 0);
        buttonRemove.setEnabled(listSelected.getSelectedValues().length > 0);
        buttonRemoveAll.setEnabled(listSelected.getModel().getSize() > 0);
    }

    private final ListCellRenderer ENTITY_LIST_RENDERER = new EntityListCellRenderer();

    private class URLListModel extends DefaultListModel {

        URLListModel(List<URL> libraries) {
            for (URL url:libraries) {
                addElement(url);
            }
        }

    }

    private final class EntityListCellRenderer extends JLabel implements ListCellRenderer {

        public EntityListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = null;
            String fullName = null;
            boolean fileExists = true;
            
            if (value instanceof URL) {
                URL url = (URL) value;
                fullName = url.toExternalForm();
                text = AxisUtils.getJarName(url);
                try {
                    File f = new File(url.toURI());
                    fileExists = f.exists();
                } catch (URISyntaxException ex) {
                    fileExists = false;
                } catch (IllegalArgumentException ex) {
                    fileExists = false;
                }
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(fileExists ? list.getForeground() : Color.RED);
            }

            setFont(list.getFont());
            setText(text);
            setToolTipText(fullName);
            return this;
        }
    }


    private void refreshModel() {
        selectedLibraries = new ArrayList<URL>();
        for (int i = 0; i < listSelected.getModel().getSize(); i++) {
            Object o = listSelected.getModel().getElementAt(i);
            if (o instanceof URL) {
                selectedLibraries.add((URL) o);
            }
        }
    }
    
    public List<URL> getSelectedLibraries() {
        return selectedLibraries;
    }
}
