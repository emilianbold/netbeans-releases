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

public interface Entity {
    
    public void setName(String value);
    
    public String getName();
    
    public void setClass2(String value);
    
    public String getClass2();
    
    public void setAccess(String value);
    
    public String getAccess();
    
    public void setMetadataComplete(boolean value);
    
    public boolean isMetadataComplete();
    
    public void setDescription(String value);
    
    public String getDescription();
    
    public void setTable(Table value);
    
    public Table getTable();
    
    public Table newTable();
    
    public void setSecondaryTable(int index, SecondaryTable value);
    
    public SecondaryTable getSecondaryTable(int index);
    
    public int sizeSecondaryTable();
    
    public void setSecondaryTable(SecondaryTable[] value);
    
    public SecondaryTable[] getSecondaryTable();
    
    public int addSecondaryTable(SecondaryTable value);
    
    public int removeSecondaryTable(SecondaryTable value);
    
    public SecondaryTable newSecondaryTable();
    
    public void setPrimaryKeyJoinColumn(int index, PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn getPrimaryKeyJoinColumn(int index);
    
    public int sizePrimaryKeyJoinColumn();
    
    public void setPrimaryKeyJoinColumn(PrimaryKeyJoinColumn[] value);
    
    public PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumn();
    
    public int addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public int removePrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn newPrimaryKeyJoinColumn();
    
    public void setIdClass(IdClass value);
    
    public IdClass getIdClass();
    
    public IdClass newIdClass();
    
    public void setInheritance(Inheritance value);
    
    public Inheritance getInheritance();
    
    public Inheritance newInheritance();
    
    public void setDiscriminatorValue(String value);
    
    public String getDiscriminatorValue();
    
    public void setDiscriminatorColumn(DiscriminatorColumn value);
    
    public DiscriminatorColumn getDiscriminatorColumn();
    
    public DiscriminatorColumn newDiscriminatorColumn();
    
    public void setSequenceGenerator(SequenceGenerator value);
    
    public SequenceGenerator getSequenceGenerator();
    
    public SequenceGenerator newSequenceGenerator();
    
    public void setTableGenerator(TableGenerator value);
    
    public TableGenerator getTableGenerator();
    
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
    
    public void setExcludeDefaultListeners(EmptyType value);
    
    public EmptyType getExcludeDefaultListeners();
    
    public EmptyType newEmptyType();
    
    public void setExcludeSuperclassListeners(EmptyType value);
    
    public EmptyType getExcludeSuperclassListeners();
    
    public void setEntityListeners(EntityListeners value);
    
    public EntityListeners getEntityListeners();
    
    public EntityListeners newEntityListeners();
    
    public void setPrePersist(PrePersist value);
    
    public PrePersist getPrePersist();
    
    public PrePersist newPrePersist();
    
    public void setPostPersist(PostPersist value);
    
    public PostPersist getPostPersist();
    
    public PostPersist newPostPersist();
    
    public void setPreRemove(PreRemove value);
    
    public PreRemove getPreRemove();
    
    public PreRemove newPreRemove();
    
    public void setPostRemove(PostRemove value);
    
    public PostRemove getPostRemove();
    
    public PostRemove newPostRemove();
    
    public void setPreUpdate(PreUpdate value);
    
    public PreUpdate getPreUpdate();
    
    public PreUpdate newPreUpdate();
    
    public void setPostUpdate(PostUpdate value);
    
    public PostUpdate getPostUpdate();
    
    public PostUpdate newPostUpdate();
    
    public void setPostLoad(PostLoad value);
    
    public PostLoad getPostLoad();
    
    public PostLoad newPostLoad();
    
    public void setAttributeOverride(int index, AttributeOverride value);
    
    public AttributeOverride getAttributeOverride(int index);
    
    public int sizeAttributeOverride();
    
    public void setAttributeOverride(AttributeOverride[] value);
    
    public AttributeOverride[] getAttributeOverride();
    
    public int addAttributeOverride(AttributeOverride value);
    
    public int removeAttributeOverride(AttributeOverride value);
    
    public AttributeOverride newAttributeOverride();
    
    public void setAssociationOverride(int index, AssociationOverride value);
    
    public AssociationOverride getAssociationOverride(int index);
    
    public int sizeAssociationOverride();
    
    public void setAssociationOverride(AssociationOverride[] value);
    
    public AssociationOverride[] getAssociationOverride();
    
    public int addAssociationOverride(AssociationOverride value);
    
    public int removeAssociationOverride(AssociationOverride value);
    
    public AssociationOverride newAssociationOverride();
    
    public void setAttributes(Attributes value);
    
    public Attributes getAttributes();
    
    public Attributes newAttributes();
    
}
