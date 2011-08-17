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
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.io.IOException;
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
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceModelBuilder;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.codegen.model.RuntimeJpaEntity;
import org.netbeans.modules.websvc.rest.support.MetadataModelReadHelper;
import org.netbeans.modules.websvc.rest.support.MetadataModelReadHelper.State;
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

    private static final int MAX_RETRY = 10;

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
    private EntityListModel availableModel;
    private EntityListModel selectedModel;
    private int retryCount = 0;

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

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonRemove, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(buttonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(buttonAddAll, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(buttonRemoveAll, javax.swing.GroupLayout.PREFERRED_SIZE, 121, Short.MAX_VALUE))
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
                .addContainerGap(155, Short.MAX_VALUE))
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7))
                    .addComponent(labelAvailableEntities))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                    .addComponent(labelSelectedEntities))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAvailableEntities)
                    .addComponent(labelSelectedEntities))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                    .addComponent(panelButtons, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
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
            sourceModel.removeElement( entity );
            if (!destModel.contains(entity)) {
                destModel.addElement(entity);
            }
        /*    EntityClassInfo info = builder.getEntityClassInfo(entity.getClass2());
            closure.addAll(info.getEntityClosure(closure));
            closure.add( info );*/
        }
        /*for (EntityClassInfo e : closure) {
            sourceModel.removeElement(e.getEntity());
            if (!destModel.contains(e.getEntity())) {
                destModel.addElement(e.getEntity());
            }
        }*/
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

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
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
            if (Util.getPersistenceUnit(settings, project) != null) {
                persistenceUnit = Util.getPersistenceUnit(settings, project).getName();
            }
        }

        if (mappings == null) {
            setupModel();
        }
    }

    private void setupModel() {
        PersistenceScope ps = PersistenceScope.getPersistenceScope(project.getProjectDirectory());
        MetadataModel<EntityMappingsMetadata> entityModel = ps.getEntityMappingsModel(persistenceUnit);
        entitiesHelper = MetadataModelReadHelper.create(entityModel, new MetadataModelAction<EntityMappingsMetadata, EntityMappings>() {

            public EntityMappings run(EntityMappingsMetadata metadata) throws Exception {
                EntityMappings mappings = metadata.getRoot();
                mappings.getEntity();
                return mappings;
            }
        });

        if (availableModel != null) {
            removeChangeListener(availableModel);
        }
        availableModel = new EntityListModel(true);
        listAvailable.setModel(availableModel);
        addChangeListener(availableModel);

        availableModel.addElement(MSG_RETRIEVING);
        listAvailable.ensureIndexIsVisible(0);

        entitiesHelper.addChangeListener(availableModel);
        entitiesHelper.start();

        if (selectedModel != null) {
            removeChangeListener(selectedModel);
        }
        selectedModel = new EntityListModel(false);
        listSelected.setModel(selectedModel);
        this.addChangeListener(selectedModel);
    }

    public void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.ENTITY_RESOURCE_MODEL, getResourceModel());
    }

    private void updateButtons() {
        /*
         *  Fix for BZ#151256 -  [65cat] ClassCastException: java.lang.String 
         *  cannot be cast to org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity
         */
        boolean smthSelected = listAvailable.getSelectedValues().length > 0;
        boolean noEntities = false;
        if ( listAvailable.getSelectedValues().length == 1 ){
            noEntities = MSG_RETRIEVING.equals(listAvailable.
                    getSelectedValue());
        }
        buttonAdd.setEnabled(smthSelected && !noEntities);
        buttonAddAll.setEnabled(!noEntities);
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
                final MetadataModelReadHelper helper = 
                    (MetadataModelReadHelper)e.getSource();
                switch (helper.getState()) {
                    case FINISHED:
                        // when we got here the model action has been executed
                        if (mappings == null && entitiesHelper.isReady()) {
                            try {
                                mappings = entitiesHelper.getResult();
                            } catch (ExecutionException ex) {
                                Logger.getLogger(getClass().getName()).
                                    log(Level.ALL, "stateChanged", ex); //NOI18N

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

                                    MetadataModel model = helper.getModel();
                                    try {
                                        model.runReadAction(new MetadataModelAction() {
                                            public Void run(Object metadata) throws Exception {
                                                for (Entity e : mappings.getEntity()) {
                                                    if (entities.get(e.getClass2()) == null) {
                                                        entities.put(e.getClass2(), e);
                                                    }
                                                }
                                                return null;
                                            }
                                        });
                                    }
                                    catch (MetadataModelException e) {
                                        Throwable ex = e.getCause()== null ? e: e.getCause();
                                        Logger.getLogger(EntitySelectionPanel.class.getName()).
                                            log(Level.ALL, null , ex); //NOI18N
                                    }
                                    catch (IOException e) {
                                        Logger.getLogger(EntitySelectionPanel.class.getName()).
                                        log(Level.ALL, null, e); //NOI18N
                                    }

                                    builder = new EntityResourceModelBuilder(project, entities.values());

                                    if (builder.getEntities().size() == entities.size() ||
                                            retryCount++ > MAX_RETRY) {

                                        // Update the ListModel on the AWT thread.
                                        SwingUtilities.invokeLater(new Runnable() {

                                            public void run() {
                                                removeElement(MSG_RETRIEVING);
                                                for (Entity entity : builder.getValidEntities()) {
                                                    addElement(entity);
                                                }
                                                updateButtons();
                                            }
                                        });
                                    } else {
                                        // Something is wrong. The entities in our resource model
                                        // doesn't match the actual number of entities. Retry to
                                        // see if we get a better result.
                                        //System.out.println("retrying count = " + retryCount);
                                        mappings = null;
                                        setupModel();
                                    }
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
            Set<Entity> entities = new HashSet<Entity>();
            Set<EntityClassInfo> closure = new HashSet<EntityClassInfo>();
            for (int i = 0; i < listSelected.getModel().getSize(); i++) {
                Object o = listSelected.getModel().getElementAt(i);
                if (o instanceof Entity) {
                    Entity entity = (Entity) o;
                    entities.add( entity );
                    EntityClassInfo info = builder.getEntityClassInfo(
                            entity.getClass2());
                    info.getEntityClosure( closure );
                }
            }
            for (EntityClassInfo classInfo : closure) {
                entities.add( classInfo.getEntity());
            }
            resourceModel = builder.build(entities);
        }
    }
}
