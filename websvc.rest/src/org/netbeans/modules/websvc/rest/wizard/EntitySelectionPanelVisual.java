/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceModelBuilder;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.codegen.model.RuntimeJpaEntity;
import org.netbeans.modules.websvc.rest.support.MetadataModelReadHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Liu
 * @author  Nam Nguyen
 */
public class EntitySelectionPanelVisual extends javax.swing.JPanel implements AbstractPanel.Settings {

    //private ChangeSupport changeSupport = new ChangeSupport(this);
    private Project project;
    private List<ChangeListener> listeners;
    //boolean waitingForScan;
    //boolean waitingForEntities;
    private String persistenceUnit;
    MetadataModelReadHelper<EntityMappingsMetadata, EntityMappings> entitiesHelper;
    private EntityMappings mappings;
    private EntityResourceModelBuilder builder;
    private EntityResourceBeanModel resourceModel;
    //private List<EntityMappings> waitForMappings = new ArrayList<EntityMappings>();
    //private PersistenceUnit persistenceUnit;

    /** Creates new form CrudSetupPanel */
    public EntitySelectionPanelVisual(String name) {
        setName(name);
        listeners = new ArrayList<ChangeListener>();
        initComponents();
        ListSelectionListener selectionListener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }
        };
        listAvailable.getSelectionModel().addListSelectionListener(selectionListener);
        listSelected.getSelectionModel().addListSelectionListener(selectionListener);
    }

    /**
     * @return PersistenceUnit that was created by invoking the PU wizard from
     * this wizard or <code>null</code> if there was already a persistence unit, i.e.
     * there was no reason to create a new persistence unit from this wizard.
     */
    /*
    public PersistenceUnit getPersistenceUnit() {
    return persistenceUnit;
    }
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listAvailable = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        listSelected = new javax.swing.JList();
        labelAvailableEntities = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        buttonAdd = new javax.swing.JButton();
        buttonRemove = new javax.swing.JButton();
        buttonAddAll = new javax.swing.JButton();
        buttonRemoveAll = new javax.swing.JButton();
        labelSelectedEntities = new javax.swing.JLabel();

        listAvailable.setCellRenderer(ENTITY_LIST_RENDERER);
        jScrollPane1.setViewportView(listAvailable);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle"); // NOI18N
        listAvailable.getAccessibleContext().setAccessibleName(bundle.getString("AvailableEntityClasses")); // NOI18N
        listAvailable.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_AvailableEntityClasses")); // NOI18N

        listSelected.setCellRenderer(ENTITY_LIST_RENDERER);
        listSelected.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listSelectedValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(listSelected);
        listSelected.getAccessibleContext().setAccessibleName(bundle.getString("SelectedEntityList")); // NOI18N
        listSelected.getAccessibleContext().setAccessibleDescription(bundle.getString("DESC_SelectedEntityClasses")); // NOI18N

        labelAvailableEntities.setLabelFor(listAvailable);
        org.openide.awt.Mnemonics.setLocalizedText(labelAvailableEntities, bundle.getString("LBL_AvailableEntities")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(buttonAdd, org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "LBL_Add")); // NOI18N
        buttonAdd.setEnabled(false);
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonRemove, org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "LBL_Remove")); // NOI18N
        buttonRemove.setEnabled(false);
        buttonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonAddAll, org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "LBL_AddAll")); // NOI18N
        buttonAddAll.setEnabled(false);
        buttonAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(buttonRemoveAll, org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "LBL_RemoveAll")); // NOI18N
        buttonRemoveAll.setActionCommand(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "LBL_RemoveAll")); // NOI18N
        buttonRemoveAll.setEnabled(false);
        buttonRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveAllActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panelButtonsLayout = new org.jdesktop.layout.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .add(panelButtonsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(buttonRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .add(buttonAdd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .add(buttonAddAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .add(buttonRemoveAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelButtonsLayout.createSequentialGroup()
                .add(70, 70, 70)
                .add(buttonAdd)
                .add(18, 18, 18)
                .add(buttonRemove)
                .add(18, 18, 18)
                .add(buttonAddAll)
                .add(18, 18, 18)
                .add(buttonRemoveAll)
                .addContainerGap(139, Short.MAX_VALUE))
        );

        buttonAdd.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "AddEntityClass")); // NOI18N
        buttonAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "DESC_AddEntityClass")); // NOI18N
        buttonRemove.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "RemoveEntityClass")); // NOI18N
        buttonRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "DESC_RemoveEntityClass")); // NOI18N
        buttonAddAll.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "AddAllEntityClasses")); // NOI18N
        buttonAddAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "DESC_AddAllEntityClasses")); // NOI18N
        buttonRemoveAll.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "RemoveAllEntityClasses")); // NOI18N
        buttonRemoveAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "DESC_RemoveAllEntityClasses")); // NOI18N

        labelSelectedEntities.setLabelFor(listSelected);
        org.openide.awt.Mnemonics.setLocalizedText(labelSelectedEntities, bundle.getString("LBL_SelectedEntities")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panelButtons, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(7, 7, 7))
                    .add(layout.createSequentialGroup()
                        .add(labelAvailableEntities)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                    .add(labelSelectedEntities))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelAvailableEntities)
                    .add(labelSelectedEntities))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, panelButtons, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE))
                .addContainerGap())
        );

        labelAvailableEntities.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "AvailableEntityClasses")); // NOI18N
        labelAvailableEntities.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "DESC_AvailableEntityClasses")); // NOI18N
        panelButtons.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "AddOrRemoveEntityClasses")); // NOI18N
        panelButtons.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "DESC_AddRemovePanel")); // NOI18N
        labelSelectedEntities.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "SelectedEntityClasses")); // NOI18N
        labelSelectedEntities.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "DESC_SelectedList")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
    EntityListModel sourceModel = (EntityListModel) listSelected.getModel();
    EntityListModel destModel = (EntityListModel) listAvailable.getModel();
    for (Object entity : listSelected.getSelectedValues()) {
        sourceModel.removeElement((Entity) entity);
        if (!destModel.contains(entity)) {
            destModel.addElement(entity);
        }
    }
    refreshModel();
    updateButtons();
    fireChange();
}//GEN-LAST:event_removeActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        EntityListModel sourceModel = (EntityListModel) listAvailable.getModel();
        EntityListModel destModel = (EntityListModel) listSelected.getModel();
        Set<EntityClassInfo> closure = new HashSet<EntityClassInfo>();
        for (Object o : listAvailable.getSelectedValues()) {
            Entity entity = (Entity) o;
            EntityClassInfo info = builder.getEntityClassInfo(entity.getClass2());
            closure.addAll(info.getEntityClosure(closure));
        }
        for (EntityClassInfo e : closure) {
            sourceModel.removeElement(e.getEntity());
            if (!destModel.contains(e.getEntity())) {
                destModel.addElement(e.getEntity());
            }
        }
        refreshModel();
        updateButtons();
        fireChange();
}//GEN-LAST:event_addActionPerformed

private void listSelectedValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listSelectedValueChanged
    fireChange();
}//GEN-LAST:event_listSelectedValueChanged

    private void buttonRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveAllActionPerformed
        EntityListModel model = (EntityListModel) listAvailable.getModel();
        EntityListModel selectedModel = (EntityListModel) listSelected.getModel();
        for (int i = 0; i < selectedModel.getSize(); i++) {
            model.addElement(selectedModel.elementAt(i));
        }
        selectedModel.clear();
        refreshModel();
        updateButtons();
        fireChange();
    }//GEN-LAST:event_buttonRemoveAllActionPerformed

    private void buttonAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddAllActionPerformed
        EntityListModel model = (EntityListModel) listSelected.getModel();
        EntityListModel availableModel = (EntityListModel) listAvailable.getModel();
        for (int i = 0; i < availableModel.getSize(); i++) {
            model.addElement(availableModel.elementAt(i));
        }
        availableModel.clear();
        refreshModel();
        updateButtons();
        fireChange();
    }//GEN-LAST:event_buttonAddAllActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonAddAll;
    private javax.swing.JButton buttonRemove;
    private javax.swing.JButton buttonRemoveAll;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelAvailableEntities;
    private javax.swing.JLabel labelSelectedEntities;
    private javax.swing.JList listAvailable;
    private javax.swing.JList listSelected;
    private javax.swing.JPanel panelButtons;
    // End of variables declaration//GEN-END:variables

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void fireChange() {
        ChangeEvent event = new ChangeEvent(this);

        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    public void read(WizardDescriptor settings) {
        project = Templates.getProject(settings);
        PersistenceScope ps = PersistenceScope.getPersistenceScope(project.getProjectDirectory());
        if (ps == null) {
            return;
        }

        if (persistenceUnit == null) {
            if (EntitySelectionPanel.getPersistenceUnit(settings, project) != null) {
                persistenceUnit = EntitySelectionPanel.getPersistenceUnit(settings, project).getName();
            }
        }

        if (mappings == null) {
            MetadataModel<EntityMappingsMetadata> entityModel = ps.getEntityMappingsModel(persistenceUnit);
            entitiesHelper = MetadataModelReadHelper.create(entityModel, new MetadataModelAction<EntityMappingsMetadata, EntityMappings>() {

                public EntityMappings run(EntityMappingsMetadata metadata) throws Exception {
                    EntityMappings mappings = metadata.getRoot();
                    mappings.getEntity();
                    return mappings;
                }
            });

            EntityListModel availableModel = new EntityListModel(true);
            listAvailable.setModel(availableModel);
            addChangeListener(availableModel);

            availableModel.addElement(MSG_RETRIEVING);
            listAvailable.ensureIndexIsVisible(0);

            entitiesHelper.addChangeListener(availableModel);
            entitiesHelper.start();

            EntityListModel selectedModel = new EntityListModel(false);
            listSelected.setModel(selectedModel);
            this.addChangeListener(selectedModel);
        }
    }

    public void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.ENTITY_RESOURCE_MODEL, getResourceModel());
    }

    private void updateButtons() {
        buttonAdd.setEnabled(listAvailable.getSelectedValues().length > 0);
        buttonAddAll.setEnabled(listAvailable.getModel().getSize() > 0);
        buttonRemove.setEnabled(listSelected.getSelectedValues().length > 0);
        buttonRemoveAll.setEnabled(listSelected.getModel().getSize() > 0);
    }

    public void updatePersistenceUnitButton() {
        /*
        boolean visible = getPersistenceUnit() == null;
        if (ProviderUtil.isValidServerInstanceOrNone(project) && visible) {
        PersistenceScope scopes[] = PersistenceUtils.getPersistenceScopes(project);
        for (int i = 0; i < scopes.length; i++) {
        FileObject persistenceXml = scopes[i].getPersistenceXml();
        if (persistenceXml != null) {
        try {
        Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceXml);
        // TODO: should ask the user which pu should be used
        if (persistence.getPersistenceUnit().length > 0) {
        visible = false;
        break;
        }
        } catch (IOException ex) {
        ErrorManager.getDefault().notify(ex);
        }
        }
        }
        }
        createPUButton.setVisible(visible);
         */
    }
    /*
    private static final class EntityComparator implements Comparator<Entity> {
    public int compare(Entity o1, Entity o2) {
    return o1.getName().compareTo(o2.getName());
    }
    }
     */
    //private static final EntityComparator ENTITY_COMPARATOR = new EntityComparator();
    private final ListCellRenderer ENTITY_LIST_RENDERER = new EntityListCellRenderer();
    private static final String MSG_RETRIEVING = NbBundle.getMessage(EntitySelectionPanel.class, "MSG_Retrieving");

    private class EntityListModel extends DefaultListModel implements ChangeListener {

        private boolean available;

        EntityListModel(boolean available) {
            this.available = available;
        }

        public void stateChanged(ChangeEvent e) {
            if (available && e.getSource() instanceof MetadataModelReadHelper) {
                switch (((MetadataModelReadHelper) e.getSource()).getState()) {
                    case FINISHED:
                        // when we got here the model action has been executed
                        if (mappings == null && entitiesHelper.isReady()) {
                            try {
                                mappings = entitiesHelper.getResult();
                            } catch (ExecutionException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.ALL, "stateChanged", ex); //NOI18N

                                throw new IllegalStateException(ex);
                            }

                            RequestProcessor.getDefault().post(new Runnable() {

                                public void run() {
                                    final Map<String, Entity> entities = new HashMap<String, Entity>();
                                    for (Entity e : RuntimeJpaEntity.getEntityFromClasspath(project)) {
                                        if (entities.get(e.getClass2()) == null) {
                                            entities.put(e.getClass2(), e);
                                        }
                                    }

                                    for (Entity e : mappings.getEntity()) {
                                        if (entities.get(e.getClass2()) == null) {
                                            entities.put(e.getClass2(), e);
                                        }
                                    }

                                    builder = new EntityResourceModelBuilder(project, entities.values());

                                    // Update the ListModel on the AWT thread.
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            removeElement(MSG_RETRIEVING);
                                            for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                                                addElement(entry.getValue());
                                            }
                                            updateButtons();
                                        }
                                    });
                                }
                            });
                        }
                        break;
                    default:
                    //Already displaying in available list.
                }
            }
        }
    }

    private final class EntityListCellRenderer extends JLabel implements ListCellRenderer {

        public EntityListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = null;

            if (value instanceof Entity) {
                Entity entity = (Entity) value;
                String simpleName = entity.getName();
                text = simpleName + " (" + entity.getClass2() + ")";
            }

            if (text == null) {
                text = value.toString();
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setFont(list.getFont());
            setText(text);
            return this;
        }
    }

    public boolean valid(WizardDescriptor wizard) {
        if (persistenceUnit == null || entitiesHelper == null) {
            AbstractPanel.setErrorMessage(wizard, "ERR_NoPersistenceUnit");
            return false;
        }

        if (entitiesHelper.isReady() && (listAvailable.getModel().getSize() + listSelected.getModel().getSize()) == 0) {
            AbstractPanel.setErrorMessage(wizard, "MSG_EntitySelectionPanel_NoEntities");
            return false;
        }

        if (listSelected.getModel().getSize() == 0) {
            AbstractPanel.setErrorMessage(wizard, "MSG_EntitySelectionPanel_NoneSelected");
            return false;
        }

        EntityResourceBeanModel model = getResourceModel();
        if (model != null && !model.isValid()) {
            AbstractPanel.setErrorMessage(wizard, "MSG_EntitySelectionPanel_InvalidEntityClasses");
            return false;
        }

        AbstractPanel.clearErrorMessage(wizard);
        return true;
    }

    private EntityResourceBeanModel getResourceModel() {
        if (resourceModel == null) {
            refreshModel();
        }
        return resourceModel;
    }

    private void refreshModel() {
        if (builder != null) {
            List<Entity> entities = new ArrayList<Entity>();
            for (int i = 0; i < listSelected.getModel().getSize(); i++) {
                Object o = listSelected.getModel().getElementAt(i);
                if (o instanceof Entity) {
                    entities.add((Entity) o);
                }
            }
            resourceModel = builder.build(entities);
        }
    }
}