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
package org.netbeans.modules.form.j2ee;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.form.BindingDesignSupport;
import org.netbeans.modules.form.BindingProperty;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormJavaSource;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.MetaBinding;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.assistant.AssistantMessages;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Result of database column DnD.
 *
 * @author Jan Stola
 */
public class DBColumnDrop extends DBConnectionDrop {
    /** Dropped column. */
    private DatabaseMetaDataTransfer.Column column;
    
    /**
     * Creates new <code>DBColumnDrop</code>.
     *
     * @param model form model.
     * @param column dropped column.
     */
    public DBColumnDrop(FormModel model, DatabaseMetaDataTransfer.Column column) {
        super(model, null);
        this.column = column;
    }

    /**
     * Returns <code>JTextField</code> palette item.
     *
     * @param dtde corresponding drop target drag event.
     * @return <code>JTextField</code> palette item.
     */
    @Override
    public PaletteItem getPaletteItem(DropTargetDragEvent dtde) {
        PaletteItem pItem;
        if (!assistantInitialized) {
            initAssistant();
        }
        if (!J2EEUtils.hasPrimaryKey(column.getDatabaseConnection(), column.getTableName())) {
            FormEditor.getAssistantModel(model).setContext("tableWithoutPK"); // NOI18N
            return null;
        }
        if (FormJavaSource.isInDefaultPackage(model)) {
            // 97982: default package
            FormEditor.getAssistantModel(model).setContext("columnDefaultPackage"); // NOI18N
            return null;
        }
        setBindingOnly(dtde.getDropAction() == DnDConstants.ACTION_MOVE);
        if (isBindingOnly()) {
            FormEditor.getAssistantModel(model).setContext("columnDropBinding", "columnDropComponent"); // NOI18N
            pItem = new PaletteItem(new ClassSource("javax.persistence.EntityManager", // NOI18N
                new String[] { ClassSource.LIBRARY_SOURCE },
                new String[] { "toplink" }), null); // NOI18N
            pItem.setIcon(new ImageIcon(
                Utilities.loadImage("org/netbeans/modules/form/j2ee/resources/binding.gif")).getImage()); // NOI18N
        } else {
            pItem = new PaletteItem(new ClassSource("javax.swing.JTextField", null, null), null); // NOI18N
        }
        return pItem;
    }

    /**
     * Registers assistant messages related to DB column DnD.
     */
    private void initAssistant() {
        ResourceBundle bundle = NbBundle.getBundle(DBColumnDrop.class);
        String dropBindingMsg = bundle.getString("MSG_ColumnDropBinding"); // NOI18N
        String dropComponentMsg = bundle.getString("MSG_ColumnDropComponent"); // NOI18N
        String tableWithoutPKMsg = bundle.getString("MSG_TableWithoutPK"); // NOI18N
        String columnDefaultPackageMsg = bundle.getString("MSG_ColumnDefaultPackage"); // NOI18N
        AssistantMessages messages = AssistantMessages.getDefault();
        messages.setMessages("columnDropBinding", dropBindingMsg); // NOI18N
        messages.setMessages("columnDropComponent", dropComponentMsg); // NOI18N
        messages.setMessages("tableWithoutPK", tableWithoutPKMsg); // NOI18N
        messages.setMessages("columnDefaultPackage", columnDefaultPackageMsg);
        assistantInitialized = true;
    }

    /**
     * Post-processing after placement of the dragged column.
     *
     * @param componentId ID of the corresponding UI component.
     * @param droppedOverId ID of a component the new component has been dropped over.
     */
    @Override
    public void componentAdded(String componentId, String droppedOverId) {
        try {
            FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
            project = FileOwnerQuery.getOwner(formFile);

            // Make sure persistence.xml file exists
            FileObject persistenceXML = J2EEUtils.getPersistenceXML(project, true);
            
            // Initializes persistence unit and persistence descriptor
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, column.getDatabaseConnection());

            // Initializes project's classpath
            J2EEUtils.updateProjectForUnit(persistenceXML, unit, column.getJDBCDriver());

            // Obtain description of entity mappings
            PersistenceScope scope = PersistenceScope.getPersistenceScope(formFile);
            MetadataModel<EntityMappingsMetadata> mappings = scope.getEntityMappingsModel(unit.getName());

            // Find entity that corresponds to the dragged table
            String[] entityInfo = J2EEUtils.findEntity(mappings, column.getTableName());
            
            // Create a new entity (if there isn't one that corresponds to the dragged table)
            if (entityInfo == null) {
                // Generates a Java class for the entity
                J2EEUtils.createEntity(formFile.getParent(), scope, unit, column.getDatabaseConnection(), column.getTableName(), null);

                entityInfo = J2EEUtils.findEntity(mappings, column.getTableName());
            } else {
                // Add the entity into the persistence unit if it is not there already
                J2EEUtils.addEntityToUnit(entityInfo[1], unit, project);
            }
            
            J2EEUtils.makeEntityObservable(formFile, entityInfo, mappings);

            // Find (or create) entity manager "bean" for the persistence unit
            RADComponent entityManager;
            if (isBindingOnly()) {
                String unitName = unit.getName();
                entityManager = J2EEUtils.findEntityManager(model, unitName);
                if (entityManager == null) {
                    entityManager = model.getMetaComponent(componentId);
                    entityManager.getPropertyByName("persistenceUnit").setValue(unitName); // NOI18N
                    J2EEUtils.renameComponent(entityManager, true, unitName + "EntityManager", "entityManager"); // NOI18N
                } else {
                    // The entity manager was already there => remove the dragged one
                    model.removeComponent(model.getMetaComponent(componentId), true);
                }
            } else {
                entityManager = initEntityManagerBean(unit);
            }

            RADComponent control = null;
            String controlProperty = null;
            if (isBindingOnly()) {
                if (droppedOverId == null) return;
                control = model.getMetaComponent(droppedOverId);
                Class controlClass = control.getBeanClass();
                controlProperty = controlProperty(controlClass);
                if (controlProperty == null) {
                    return;
                }
            } else {
                control = model.getMetaComponent(componentId);
                controlProperty = "text"; // NOI18N
                control.getPropertyByName("columns").setValue(15); // NOI18N
            }
            
            List<String> l = Collections.singletonList(column.getColumnName());
            l = J2EEUtils.propertiesForColumns(mappings, entityInfo[0], l);
            if (l.isEmpty()) {
                // There is no property corresponding to the dragged column
                return;
            }
            String sourcePath = l.get(0);
            
            RADComponent metaTable = null;
            for (RADComponent component : model.getAllComponents()) {
                if ("java.util.List".equals(component.getBeanClass().getName())) { // NOI18N
                    Object value = component.getSyntheticProperty("typeParameters").getValue();
                    if (value instanceof String) {
                        int index = ((String)value).indexOf(entityInfo[1] + '>'); // PENDING improve this check
                        if (index != -1) {
                            for (RADComponent tableCand : model.getAllComponents()) {
                                if (javax.swing.JTable.class.isAssignableFrom(tableCand.getBeanClass())) {
                                    BindingProperty prop = tableCand.getBindingProperty("elements"); // NOI18N
                                    if (prop != null) {
                                        MetaBinding binding = prop.getValue();
                                        if (binding.getSource() == component) {
                                            metaTable = tableCand;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BindingProperty prop;
            MetaBinding binding;
            if (metaTable == null) {
                RADComponent metaEntity = null;
                Class<?> entityClass = ClassPathUtils.loadClass(entityInfo[1], formFile); // NOI18N
                for (RADComponent component : model.getAllComponents()) {
                    if (entityClass.equals(component.getBeanClass())) {
                        metaEntity = component;
                    }
                }

                if (metaEntity == null) {
                    RADComponent metaQuery = new RADComponent();
                    Class<?> queryClass = ClassPathUtils.loadClass("javax.persistence.Query", formFile); // NOI18N
                    metaQuery.initialize(model);
                    metaQuery.initInstance(queryClass);
                    metaQuery.getPropertyByName("entityManager").setValue(entityManager); // NOI18N
                    char c = entityInfo[0].toLowerCase().charAt(0);
                    String query = "SELECT " + c + " FROM " + entityInfo[0] + " " + c; // NOI18N
                    metaQuery.getPropertyByName("query").setValue(query); // NOI18N
                    metaQuery.getPropertyByName("maxResults").setValue(1); // NOI18N
                    metaQuery.setStoredName(c + entityInfo[0].substring(1) + "Query"); // NOI18N
                    model.addComponent(metaQuery, null, true);

                    metaEntity = new RADComponent();
                    metaEntity.initialize(model);
                    metaEntity.initInstance(entityClass);
                    metaEntity.getPropertyByName("query").setValue(metaQuery); // NOI18N
                    model.addComponent(metaEntity, null, true);
                }

                prop = control.getBindingProperty(controlProperty); // NOI18N
                binding = new MetaBinding(metaEntity, BindingDesignSupport.elWrap(sourcePath), control, controlProperty); // NOI18N
            } else {
                prop = control.getBindingProperty("enabled"); // NOI18N
                binding = new MetaBinding(metaTable, BindingDesignSupport.elWrap("selectedElement != null"), control, "enabled"); // NOI18N
                prop.setValue(binding);
                prop = control.getBindingProperty(controlProperty);
                binding = new MetaBinding(metaTable, BindingDesignSupport.elWrap("selectedElement." + sourcePath), control, controlProperty); // NOI18N
            }
            prop.setValue(binding);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    /**
     * Determines the property that should be bound.
     * 
     * @param controlClass class of the control that will be bound.
     * @return the name of the property that should be bound.
     */
    private static String controlProperty(Class controlClass) {
        String controlProperty = null;
        if (javax.swing.text.JTextComponent.class.isAssignableFrom(controlClass)) {
            controlProperty = "text"; // NOI18N
        } else if (javax.swing.JToggleButton.class.isAssignableFrom(controlClass)) {
            controlProperty = "selected"; // NOI18N
        } else if (javax.swing.JSlider.class.isAssignableFrom(controlClass)) {
            controlProperty = "value"; // NOI18N
        }
        return controlProperty;
    }

}
