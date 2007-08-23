/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Pavel Buzek
 */
public class PersistenceClientEntitySelectionVisual extends javax.swing.JPanel {

    private WizardDescriptor wizard;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private Project project;
    boolean waitingForScan;
    boolean waitingForEntities;
    private PersistenceUnit persistenceUnit;

    private EntityClosure entityClosure;


    /** Creates new form CrudSetupPanel */
    public PersistenceClientEntitySelectionVisual(String name, WizardDescriptor wizard) {
        setName(name);
        this.wizard = wizard;
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
    public PersistenceUnit getPersistenceUnit() {
        return persistenceUnit;
    }

    private Set<String> getSelectedEntities(JList list) {
        Set<String> result = new HashSet<String>();
        for (Object elem : list.getSelectedValues()){
            result.add((String) elem);
        }
        return result;
    }
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        listAvailable = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        listSelected = new javax.swing.JList();
        cbAddRelated = new javax.swing.JCheckBox();
        labelAvailableEntities = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        buttonRemove = new javax.swing.JButton();
        buttonAdd = new javax.swing.JButton();
        buttonAddAll = new javax.swing.JButton();
        buttonRemoveAll = new javax.swing.JButton();
        labelSelectedEntities = new javax.swing.JLabel();
        createPUButton = new javax.swing.JButton();

        listAvailable.setCellRenderer(ENTITY_LIST_RENDERER);
        jScrollPane1.setViewportView(listAvailable);
        listAvailable.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_AvailableEntitiesList"));
        listAvailable.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("ACSD_AvailableEntitiesList"));

        listSelected.setCellRenderer(ENTITY_LIST_RENDERER);
        jScrollPane2.setViewportView(listSelected);
        listSelected.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_SelectedEntitiesList"));
        listSelected.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("ACSD_SelectedEntitiesList"));

        cbAddRelated.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_InludeRelated").charAt(0));
        cbAddRelated.setSelected(true);
        cbAddRelated.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_IncludeReferenced"));
        cbAddRelated.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAddRelated.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbAddRelated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAddRelatedActionPerformed(evt);
            }
        });

        cbAddRelated.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_IncludeReferencedCheckbox"));
        cbAddRelated.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("ACSD_IncludeReferencedCheckbox"));

        labelAvailableEntities.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("MNE_AvailableEntityClasses").charAt(0));
        labelAvailableEntities.setLabelFor(listAvailable);
        labelAvailableEntities.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_AvailableEntities"));

        buttonRemove.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_Remove").charAt(0));
        buttonRemove.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_Remove"));
        buttonRemove.setActionCommand("< &Remove");
        buttonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveActionPerformed(evt);
            }
        });

        buttonAdd.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_Add").charAt(0));
        buttonAdd.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_Add"));
        buttonAdd.setActionCommand("&Add >");
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });

        buttonAddAll.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_AddAll").charAt(0));
        buttonAddAll.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_AddAll"));
        buttonAddAll.setActionCommand("Add A&ll >>");
        buttonAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddAllActionPerformed(evt);
            }
        });

        buttonRemoveAll.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_RemoveAll").charAt(0));
        buttonRemoveAll.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_RemoveAll"));
        buttonRemoveAll.setActionCommand("<< Re&moveAll");
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
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonAdd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonRemoveAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonAddAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelButtonsLayout.createSequentialGroup()
                .add(67, 67, 67)
                .add(buttonAdd)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonRemove)
                .add(20, 20, 20)
                .add(buttonAddAll)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonRemoveAll)
                .addContainerGap(103, Short.MAX_VALUE))
        );

        labelSelectedEntities.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("MNE_SelectedEntityClasses").charAt(0));
        labelSelectedEntities.setLabelFor(listSelected);
        labelSelectedEntities.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_SelectedEntities"));

        createPUButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("MNE_CreatePersistenceUnit").charAt(0));
        createPUButton.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_CreatePersistenceUnit"));
        createPUButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPUButtonActionPerformed(evt);
            }
        });

        createPUButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("LBL_CreatePersistenceUnitButton"));
        createPUButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/Bundle").getString("ACSD_CreatePersistenceUnitButton"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelAvailableEntities)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panelButtons, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0))
                    .add(layout.createSequentialGroup()
                        .add(createPUButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbAddRelated)
                    .add(labelSelectedEntities)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labelSelectedEntities)
                    .add(labelAvailableEntities))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                    .add(panelButtons, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(cbAddRelated)
                        .add(31, 31, 31))
                    .add(layout.createSequentialGroup()
                        .add(createPUButton)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbAddRelatedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAddRelatedActionPerformed
        listSelected.clearSelection();
        listAvailable.clearSelection();
        entityClosure.setClosureEnabled(cbAddRelated.isSelected());

        changeSupport.fireChange();
    }//GEN-LAST:event_cbAddRelatedActionPerformed

    private void createPUButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPUButtonActionPerformed
        persistenceUnit = Util.buildPersistenceUnitUsingWizard(project, null, TableGeneration.CREATE);
        if (persistenceUnit != null) {
            updatePersistenceUnitButton();
            changeSupport.fireChange();
        }
    }//GEN-LAST:event_createPUButtonActionPerformed

    private void buttonRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveAllActionPerformed
        entityClosure.removeAllEntities();
        listSelected.clearSelection();
        updateButtons();
        changeSupport.fireChange();
    }//GEN-LAST:event_buttonRemoveAllActionPerformed

    private void buttonAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddAllActionPerformed
        entityClosure.addAllEntities();
        listAvailable.clearSelection();
        updateButtons();
        changeSupport.fireChange();
    }//GEN-LAST:event_buttonAddAllActionPerformed

    private void buttonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveActionPerformed
        entityClosure.removeEntities(getSelectedEntities(listSelected));
        listSelected.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_buttonRemoveActionPerformed

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed
        entityClosure.addEntities(getSelectedEntities(listAvailable));
        listAvailable.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_buttonAddActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonAddAll;
    private javax.swing.JButton buttonRemove;
    private javax.swing.JButton buttonRemoveAll;
    private javax.swing.JCheckBox cbAddRelated;
    private javax.swing.JButton createPUButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelAvailableEntities;
    private javax.swing.JLabel labelSelectedEntities;
    private javax.swing.JList listAvailable;
    private javax.swing.JList listSelected;
    private javax.swing.JPanel panelButtons;
    // End of variables declaration//GEN-END:variables
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    boolean valid(WizardDescriptor wizard) {
        // check PU - not just warning, required
        if (createPUButton.isVisible()) {
            wizard.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ERR_NoPersistenceUnit"));
            return false;
        }

        if (!entityClosure.isModelReady()) {
            RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {

                public void run() {
                    entityClosure.waitModelIsReady();
                    changeSupport.fireChange();
                    updateButtons();
                }
            });
            wizard.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "scanning-in-progress"));
            task.schedule(0);
            return false;
        }

        if (listSelected.getModel().getSize() == 0) {
            wizard.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MSG_NoEntityClassesSelected"));
            return false;
        }
        wizard.putProperty("WizardPanel_errorMessage", " "); //NOI18N
        return true;
    }

    
    void read(WizardDescriptor settings) {
        project = Templates.getProject(settings);

        EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(project.getProjectDirectory());
        
        entityClosure = EntityClosure.create(entityClassScope, project);
        entityClosure.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateAddAllButton();
            }
        });
        entityClosure.setClosureEnabled(cbAddRelated.isSelected());
        listAvailable.setModel(new EntityListModel(entityClosure, true));
        listSelected.setModel(new EntityListModel(entityClosure, false));
        @SuppressWarnings("unchecked")
        List<String> entities = (List<String>) settings.getProperty(WizardProperties.ENTITY_CLASS);
        if (entities == null) {
            entities = new ArrayList<String>();
        }
        entityClosure.addEntities(new HashSet<String>(entities));
        updateButtons();
        updatePersistenceUnitButton();
    }

    void store(WizardDescriptor settings) {
        ListModel model = listSelected.getModel();
        if (model instanceof EntityListModel) {
            EntityListModel elm = (EntityListModel) model;
            settings.putProperty(WizardProperties.ENTITY_CLASS, elm.getEntityClasses());
        }
    }

    private void updateButtons() {
        buttonAdd.setEnabled(listAvailable.getSelectedValues().length > 0);
        updateAddAllButton();
        buttonRemove.setEnabled(listSelected.getSelectedValues().length > 0);
        buttonRemoveAll.setEnabled(entityClosure.getSelectedEntities().size() > 0);
    }

    private void updateAddAllButton(){
        buttonAddAll.setEnabled(entityClosure.getAvailableEntities().size() > 0);
    }
    
    public void updatePersistenceUnitButton() {
        boolean visible = getPersistenceUnit() == null;
        if (ProviderUtil.isValidServerInstanceOrNone(project) && visible) {
            PersistenceScope[] scopes = PersistenceUtils.getPersistenceScopes(project);
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
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        createPUButton.setVisible(visible);
    }

    private final ListCellRenderer ENTITY_LIST_RENDERER = new EntityListCellRenderer();

    private class EntityListModel extends AbstractListModel implements ChangeListener {

        private EntityClosure entityClosure;
        private List<String> entities = new ArrayList<String>();
        private boolean available;

        EntityListModel(EntityClosure entityClosure, boolean available) {
            this.entityClosure = entityClosure;
            this.available = available;
            entityClosure.addChangeListener(this);
            refresh();
        }

        public int getSize() {
            return entities.size();
        }

        public Object getElementAt(int index) {
            return entities.get(index);
        }

        /**
         * @return the fully qualified names of the entities in this model.
         */ 
        public List<String> getEntityClasses() {
            return entities;
        }

        public void stateChanged(ChangeEvent e) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            entities = new ArrayList<String>(available ? entityClosure.getAvailableEntities() : entityClosure.getSelectedEntities());
            Collections.sort(entities);
            fireContentsChanged(this, 0, Math.max(oldSize, getSize()));
        }
    }

    private final class EntityListCellRenderer extends JLabel implements ListCellRenderer {

        public EntityListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = null;
            if (value instanceof Entity) {
                text = ((Entity) value).getClass2();
                if (text != null) {
                    String simpleName = Util.simpleClassName(text);
                    String packageName = text.length() > simpleName.length() ? text.substring(0, text.length() - simpleName.length() - 1) : "<default package>";
                    text = simpleName + " (" + packageName + ")";
                } else {
                    Logger.getLogger("global").log(Level.INFO, "Entity:" + value + " returns null from getClass2(); see IZ 80024"); //NOI18N
                }
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
            setEnabled(entityClosure.getAvailableEntities().contains(value) || entityClosure.getWantedEntities().contains(value));
            setFont(list.getFont());
            setText(text);
            return this;
        }
    }
}
