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

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface EntityMappings {
    
    public void setVersion(String value);
    
    public String getVersion();
    
    public void setDescription(String value);
    
    public String getDescription();
    
    public void setPersistenceUnitMetadata(PersistenceUnitMetadata value);
    
    public PersistenceUnitMetadata getPersistenceUnitMetadata();
    
    public PersistenceUnitMetadata newPersistenceUnitMetadata();
    
    public void setPackage(String value);
    
    public String getPackage();
    
    public void setSchema(String value);
    
    public String getSchema();
    
    public void setCatalog(String value);
    
    public String getCatalog();
    
    public void setAccess(String value);
    
    public String getAccess();
    
    public void setSequenceGenerator(int index, SequenceGenerator value);
    
    public SequenceGenerator getSequenceGenerator(int index);
    
    public int sizeSequenceGenerator();
    
    public void setSequenceGenerator(SequenceGenerator[] value);
    
    public SequenceGenerator[] getSequenceGenerator();
    
    public int addSequenceGenerator(SequenceGenerator value);
    
    public int removeSequenceGenerator(SequenceGenerator value);
    
    public SequenceGenerator newSequenceGenerator();
    
    public void setTableGenerator(int index, TableGenerator value);
    
    public TableGenerator getTableGenerator(int index);
    
    public int sizeTableGenerator();
    
    public void setTableGenerator(TableGenerator[] value);
    
    public TableGenerator[] getTableGenerator();
    
    public int addTableGenerator(TableGenerator value);
    
    public int removeTableGenerator(TableGenerator value);
    
    public TableGenerator newTableGenerator();
    
    public void setNamedQuery(int index, NamedQuery value);
    
    public NamedQuery getNamedQuery(int index);
    
    public int sizeNamedQuery();
    
    public void setNamedQuery(NamedQuery[] value);
    
    public NamedQuery[] getNamedQuery();
    
    public int addNamedQuery(NamedQuery value);
    
    public int removeNamedQuery(NamedQuery value);
    
    public NamedQuery newNamedQuery();
    
    public void setNamedNativeQuery(int index, NamedNativeQuery value);
    
    public NamedNativeQuery getNamedNativeQuery(int index);
    
    public int sizeNamedNativeQuery();
    
    public void setNamedNativeQuery(NamedNativeQuery[] value);
    
    public NamedNativeQuery[] getNamedNativeQuery();
    
    public int addNamedNativeQuery(NamedNativeQuery value);
    
    public int removeNamedNativeQuery(NamedNativeQuery value);
    
    public NamedNativeQuery newNamedNativeQuery();
    
    public void setSqlResultSetMapping(int index, SqlResultSetMapping value);
    
    public SqlResultSetMapping getSqlResultSetMapping(int index);
    
    public int sizeSqlResultSetMapping();
    
    public void setSqlResultSetMapping(SqlResultSetMapping[] value);
    
    public SqlResultSetMapping[] getSqlResultSetMapping();
    
    public int addSqlResultSetMapping(SqlResultSetMapping value);
    
    public int removeSqlResultSetMapping(SqlResultSetMapping value);
    
    public SqlResultSetMapping newSqlResultSetMapping();
    
    public void setMappedSuperclass(int index, MappedSuperclass value);
    
    public MappedSuperclass getMappedSuperclass(int index);
    
    public int sizeMappedSuperclass();
    
    public void setMappedSuperclass(MappedSuperclass[] value);
    
    public MappedSuperclass[] getMappedSuperclass();
    
    public int addMappedSuperclass(MappedSuperclass value);
    
    public int removeMappedSuperclass(MappedSuperclass value);
    
    public MappedSuperclass newMappedSuperclass();
    
    public void setEntity(int index, Entity value);
    
    public Entity getEntity(int index);
    
    public int sizeEntity();
    
    public void setEntity(Entity[] value);
    
    public Entity[] getEntity();
    
    public int addEntity(Entity value);
    
    public int removeEntity(Entity value);
    
    public Entity newEntity();
    
    public void setEmbeddable(int index, Embeddable value);
    
    public Embeddable getEmbeddable(int index);
    
    public int sizeEmbeddable();
    
    public void setEmbeddable(Embeddable[] value);
    
    public Embeddable[] getEmbeddable();
    
    public int addEmbeddable(Embeddable value);
    
    public int removeEmbeddable(Embeddable value);
    
    public Embeddable newEmbeddable();
    
}
