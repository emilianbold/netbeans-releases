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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;


/**
 *
 * @author  Pavel Buzek
 */
public class EntitySelectionPanelVisual extends javax.swing.JPanel {
    
    private WizardDescriptor wizard;
    //private ChangeSupport changeSupport = new ChangeSupport(this);
    private Project project;
    private List<JavaSource> availableEntities;
    private List<JavaSource> selectedEntities;
    private List<ChangeListener> listeners;
    
    //boolean waitingForScan;
    //boolean waitingForEntities;
    private List<JavaSource> entityClasses;
    private String  persistenceUnit;
    
    //private List<EntityMappings> waitForMappings = new ArrayList<EntityMappings>();
    //private PersistenceUnit persistenceUnit;
    // TODO: RETOUCHE
    //    private EntityClosure entityClosure;
    
    
    
    /** Creates new form CrudSetupPanel */
    public EntitySelectionPanelVisual(String name, WizardDescriptor wizard) {
        setName(name);
        this.wizard = wizard;
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listAvailable = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        listSelected = new javax.swing.JList();
        labelAvailableEntities = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        buttonAddAll = new javax.swing.JButton();
        buttonRemoveAll = new javax.swing.JButton();
        labelSelectedEntities = new javax.swing.JLabel();

        listAvailable.setCellRenderer(ENTITY_LIST_RENDERER);
        jScrollPane1.setViewportView(listAvailable);
        listAvailable.getAccessibleContext().setAccessibleName(null);
        listAvailable.getAccessibleContext().setAccessibleDescription(null);

        listSelected.setCellRenderer(ENTITY_LIST_RENDERER);
        jScrollPane2.setViewportView(listSelected);
        listSelected.getAccessibleContext().setAccessibleName(null);
        listSelected.getAccessibleContext().setAccessibleDescription(null);

        labelAvailableEntities.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle").getString("MNE_AvailableEntityClasses").charAt(0));
        labelAvailableEntities.setLabelFor(listAvailable);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle"); // NOI18N
        labelAvailableEntities.setText(bundle.getString("LBL_AvailableEntities")); // NOI18N

        buttonAddAll.setMnemonic(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "MNE_AddAll").charAt(0));
        buttonAddAll.setText(bundle.getString("LBL_AddAll")); // NOI18N
        buttonAddAll.setActionCommand("Add A&ll >>");
        buttonAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddAllActionPerformed(evt);
            }
        });

        buttonRemoveAll.setMnemonic(org.openide.util.NbBundle.getMessage(EntitySelectionPanelVisual.class, "MNE_RemoveAll").charAt(0));
        buttonRemoveAll.setText(bundle.getString("LBL_RemoveAll")); // NOI18N
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
                    .add(buttonRemoveAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(buttonAddAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelButtonsLayout.createSequentialGroup()
                .add(116, 116, 116)
                .add(buttonAddAll)
                .add(18, 18, 18)
                .add(buttonRemoveAll)
                .addContainerGap(122, Short.MAX_VALUE))
        );

        labelSelectedEntities.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle").getString("MNE_SelectedEntityClasses").charAt(0));
        labelSelectedEntities.setLabelFor(listSelected);
        labelSelectedEntities.setText(bundle.getString("LBL_SelectedEntities")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(labelAvailableEntities)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelButtons, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
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
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                    .add(panelButtons, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
                .add(52, 52, 52))
        );
    }// </editor-fold>//GEN-END:initComponents
            
    private void buttonRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveAllActionPerformed
        //        entityClosure.removeAllEntities();
        availableEntities.addAll(selectedEntities);
        selectedEntities.clear();
        listSelected.clearSelection();
        updateButtons();
        fireChange();
    }//GEN-LAST:event_buttonRemoveAllActionPerformed
    
    private void buttonAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddAllActionPerformed
        //        entityClosure.addAllEntities();
        selectedEntities.addAll(availableEntities);
        availableEntities.clear();
        listAvailable.clearSelection();
        updateButtons();
        fireChange();
    }//GEN-LAST:event_buttonAddAllActionPerformed
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddAll;
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
    
    void read(WizardDescriptor settings) {
        project = Templates.getProject(settings);
        availableEntities = JavaSourceHelper.getEntityClasses(project);
        selectedEntities = new ArrayList<JavaSource>();
        
        EntityListModel model = new EntityListModel(availableEntities, true);
        listAvailable.setModel(model);
        this.addChangeListener(model);
        
        model = new EntityListModel(selectedEntities, false);
        listSelected.setModel(model);
        this.addChangeListener(model);
    }
    
    void store(WizardDescriptor settings) {
        ListModel model = listSelected.getModel();
        if (model instanceof EntityListModel) {
            EntityListModel elm = (EntityListModel) model;
            settings.putProperty(WizardProperties.ENTITY_CLASSES, elm.getEntityClasses());
        }
    }
    
    private void updateButtons() {
        //buttonAdd.setEnabled(listAvailable.getSelectedValues().length > 0);
        //        buttonAddAll.setEnabled(entityClosure.getAvailableEntities().size() > 0);
        //buttonRemove.setEnabled(listSelected.getSelectedValues().length > 0);
        //        buttonRemoveAll.setEnabled(entityClosure.getSelectedEntities().size() > 0);
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
    
    private class EntityListModel extends AbstractListModel implements ChangeListener {
        
        private List<JavaSource> entities;
        private boolean available;
        
        EntityListModel(List<JavaSource> entities, boolean available) {
            this.entities = entities;
            this.available = available;
            refresh();
        }
        
        public int getSize() {
            return entities.size();
        }
        
        public Object getElementAt(int index) {
            return entities.get(index);
        }
        
        public List<JavaSource> getEntityClasses() {
            return entities;
        }
        
        public void stateChanged(ChangeEvent e) {
            refresh();
        }
        
        private void refresh() {
            int oldSize = getSize();
            //            entities = new ArrayList(available ? entityClosure.getAvailableEntities() : entityClosure.getSelectedEntities());
            //Collections.sort(entities, ENTITY_COMPARATOR);
            fireContentsChanged(this, 0, Math.max(oldSize, getSize()));
        }
    }
    
    private final class EntityListCellRenderer extends JLabel implements ListCellRenderer {
        public EntityListCellRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = null;
            
            if (value instanceof JavaSource) {
                JavaSource source = (JavaSource) value;
                String simpleName = JavaSourceHelper.getClassName(source);
                String packageName = JavaSourceHelper.getPackageName(source);
                text = simpleName + " (" +  packageName + ")";
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
            
            //setEnabled(entityClosure.getAvailableEntities().contains(value) || entityClosure.getWantedEntities().contains(value));
            setFont(list.getFont());
            setText(text);
            return this;
        }
    }
}
