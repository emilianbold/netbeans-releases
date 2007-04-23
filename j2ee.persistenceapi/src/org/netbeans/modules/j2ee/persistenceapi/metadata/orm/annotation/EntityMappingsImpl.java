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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.TypeAnnotationHandler;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class EntityMappingsImpl implements EntityMappings {

    private final AnnotationModelHelper helper;
    private final PersistentObjectManager<EntityImpl> entityManager;

    public EntityMappingsImpl(AnnotationModelHelper helper) {
        this.helper = helper;
        entityManager = helper.createPersistentObjectManager(new EntityProvider());
    }

    AnnotationModelHelper getHelper() {
        return helper;
    }

    public void setVersion(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getVersion() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPersistenceUnitMetadata(PersistenceUnitMetadata value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PersistenceUnitMetadata getPersistenceUnitMetadata() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PersistenceUnitMetadata newPersistenceUnitMetadata() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPackage(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getPackage() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSchema(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getSchema() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setCatalog(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getCatalog() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAccess(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getAccess() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSequenceGenerator(int index, SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator getSequenceGenerator(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSequenceGenerator(SequenceGenerator[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator[] getSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addSequenceGenerator(SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeSequenceGenerator(SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator newSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTableGenerator(int index, TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator getTableGenerator(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTableGenerator(TableGenerator[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator[] getTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addTableGenerator(TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeTableGenerator(TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator newTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedQuery(int index, NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery getNamedQuery(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedQuery(NamedQuery[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery[] getNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery newNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedNativeQuery(int index, NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery getNamedNativeQuery(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedNativeQuery(NamedNativeQuery[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery[] getNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery newNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSqlResultSetMapping(int index, SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping getSqlResultSetMapping(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSqlResultSetMapping(SqlResultSetMapping[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping[] getSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping newSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setMappedSuperclass(int index, MappedSuperclass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public MappedSuperclass getMappedSuperclass(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeMappedSuperclass() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setMappedSuperclass(MappedSuperclass[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public MappedSuperclass[] getMappedSuperclass() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addMappedSuperclass(MappedSuperclass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeMappedSuperclass(MappedSuperclass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public MappedSuperclass newMappedSuperclass() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEntity(int index, Entity value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Entity getEntity(int index) {
        return getEntity()[index];
    }

    public int sizeEntity() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEntity(Entity[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Entity[] getEntity() {
        Collection<EntityImpl> entities = entityManager.getObjects();
        return entities.toArray(new Entity[entities.size()]);
    }

    public int addEntity(Entity value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeEntity(Entity value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Entity newEntity() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEmbeddable(int index, Embeddable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embeddable getEmbeddable(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeEmbeddable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEmbeddable(Embeddable[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embeddable[] getEmbeddable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addEmbeddable(Embeddable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeEmbeddable(Embeddable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embeddable newEmbeddable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    private final class EntityProvider implements ObjectProvider<EntityImpl> {

        public List<EntityImpl> createInitialObjects() {
            final List<EntityImpl> result = new ArrayList<EntityImpl>();
            helper.getAnnotationScanner().findAnnotatedTypes("javax.persistence.Entity", new TypeAnnotationHandler() { // NOI18N
                public void typeAnnotation(TypeElement typeElement, AnnotationMirror annotation) {
                    result.add(new EntityImpl(helper, EntityMappingsImpl.this, typeElement));
                }
            });
            return result;
        }

        public EntityImpl createObject(TypeElement type) {
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.persistence.Entity")) { // NOI18N
                return new EntityImpl(helper, EntityMappingsImpl.this, type);
            }
            return null;
        }
    }
}
