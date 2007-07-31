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

import java.awt.dnd.DropTargetDragEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.NewComponentDrop;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * Result of DB connection drop.
 *
 * @author Jan Stola
 */
public class DBConnectionDrop implements NewComponentDrop {
    /** Dropped connection. */
    private DatabaseMetaDataTransfer.Connection connection;
    /** Determines whether to drag just the binding or also a component. */
    private boolean bindingOnly;
    /** Form model. */
    FormModel model;
    /** Enclosing project. */
    Project project;
    /** Determines whether the assistant context messages were initialized. */
    boolean assistantInitialized;

    /**
     * Creates new <code>DBConnectionDrop</code>.
     *
     * @param model form model.
     * @param connection dropped connection.
     */
    public DBConnectionDrop(FormModel model, DatabaseMetaDataTransfer.Connection connection) {
        this.model = model;
        this.connection = connection;
    }

    /**
     * Returns <code>EntityManager</code> palette item.
     *
     * @param dtde corresponding drop target drag event.
     * @return <code>EntityManager</code> palette item.
     */
    public PaletteItem getPaletteItem(DropTargetDragEvent dtde) {
        PaletteItem pItem = new PaletteItem(new ClassSource("javax.persistence.EntityManager", // NOI18N
            new String[] { ClassSource.LIBRARY_SOURCE },
            new String[] { "toplink" }), null); // NOI18N
        pItem.setIcon(new ImageIcon(
            Utilities.loadImage("org/netbeans/modules/form/j2ee/resources/EntityManager.png")).getImage()); // NOI18N
        return pItem;
    }

    /**
     * Post-processing after placement of the dragged connection.
     *
     * @param componentId ID of the corresponding component.
     * @param droppedOverId ID of a component the new component has been dropped over.
     */
    public void componentAdded(String componentId, String droppedOverId) {
        try {
            FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
            project = FileOwnerQuery.getOwner(formFile);

            // Make sure persistence.xml file exists
            FileObject persistenceXML = J2EEUtils.getPersistenceXML(project, true);
            
            // Initializes persistence unit and persistence descriptor
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, connection.getDatabaseConnection());

            // Initializes project's classpath
            J2EEUtils.updateProjectForUnit(persistenceXML, unit, connection.getJDBCDriver());

            RADComponent entityManager = model.getMetaComponent(componentId);
            entityManager.getPropertyByName("persistenceUnit").setValue(unit.getName()); // NOI18N
            J2EEUtils.renameComponent(entityManager, true, unit.getName() + "EntityManager", "entityManager"); // NOI18N
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    /**
     * Ensures existence of an entity manager "bean" that corresponds to the given persistence unit.
     *
     * @param unit persistence unit.
     * @return RAD component encapsulating entity manager corresponding to the given persistence unit.
     * @throws Exception when something goes wrong.
     */
    protected RADComponent initEntityManagerBean(PersistenceUnit unit) throws Exception {
        String puName = unit.getName();
        RADComponent entityManager = J2EEUtils.findEntityManager(model, puName);
        if (entityManager == null) {
            entityManager = J2EEUtils.createEntityManager(model, puName);
        }
        return entityManager;
    }

    /**
     * Sets <code>bindingOnly</code> property.
     *
     * @param bindingOnly new value of the property.
     */
    void setBindingOnly(boolean bindingOnly) {
        this.bindingOnly = bindingOnly;
    }

    /**
     * Returns value of <code>bindingOnly</code> property.
     *
     * @return value of <code>bindingOnly</code> property.
     */
    boolean isBindingOnly() {
        return bindingOnly;
    }

}
