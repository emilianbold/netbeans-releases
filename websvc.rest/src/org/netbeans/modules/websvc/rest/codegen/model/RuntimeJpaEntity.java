/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.codegen.model;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import org.netbeans.modules.websvc.rest.support.*;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.AssociationOverride;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.AttributeOverride;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.DiscriminatorColumn;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EmptyType;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityListeners;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.IdClass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Inheritance;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedNativeQuery;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PostLoad;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PostPersist;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PostRemove;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PostUpdate;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PrePersist;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PreRemove;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PreUpdate;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PrimaryKeyJoinColumn;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.SecondaryTable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.SequenceGenerator;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.SqlResultSetMapping;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Table;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.TableGenerator;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 *
 * @author nam
 */
public class RuntimeJpaEntity implements Entity {
    
    private TypeElement typeElement;
    private String name;

    public RuntimeJpaEntity(TypeElement typeElement, String entityName) {
        this.typeElement = typeElement;
        this.name = entityName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
    
    public String getName() {
        return name;
    }

    public String getClass2() {
        return typeElement.getQualifiedName().toString();
    }

    public int addAssociationOverride(AssociationOverride value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addAttributeOverride(AttributeOverride value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addSecondaryTable(SecondaryTable value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAccess() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AssociationOverride getAssociationOverride(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AssociationOverride[] getAssociationOverride() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AttributeOverride getAttributeOverride(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AttributeOverride[] getAttributeOverride() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Attributes getAttributes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DiscriminatorColumn getDiscriminatorColumn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDiscriminatorValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EntityListeners getEntityListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EmptyType getExcludeDefaultListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EmptyType getExcludeSuperclassListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IdClass getIdClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Inheritance getInheritance() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedNativeQuery getNamedNativeQuery(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedNativeQuery[] getNamedNativeQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedQuery getNamedQuery(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedQuery[] getNamedQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostLoad getPostLoad() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostPersist getPostPersist() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostRemove getPostRemove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostUpdate getPostUpdate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrePersist getPrePersist() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PreRemove getPreRemove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PreUpdate getPreUpdate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrimaryKeyJoinColumn getPrimaryKeyJoinColumn(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecondaryTable getSecondaryTable(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecondaryTable[] getSecondaryTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SequenceGenerator getSequenceGenerator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SqlResultSetMapping getSqlResultSetMapping(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SqlResultSetMapping[] getSqlResultSetMapping() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Table getTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TableGenerator getTableGenerator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isMetadataComplete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AssociationOverride newAssociationOverride() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AttributeOverride newAttributeOverride() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Attributes newAttributes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DiscriminatorColumn newDiscriminatorColumn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EmptyType newEmptyType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EntityListeners newEntityListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IdClass newIdClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Inheritance newInheritance() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedNativeQuery newNamedNativeQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamedQuery newNamedQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostLoad newPostLoad() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostPersist newPostPersist() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostRemove newPostRemove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PostUpdate newPostUpdate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrePersist newPrePersist() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PreRemove newPreRemove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PreUpdate newPreUpdate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrimaryKeyJoinColumn newPrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecondaryTable newSecondaryTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SequenceGenerator newSequenceGenerator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SqlResultSetMapping newSqlResultSetMapping() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Table newTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TableGenerator newTableGenerator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeAssociationOverride(AssociationOverride value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeAttributeOverride(AttributeOverride value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removePrimaryKeyJoinColumn(PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeSecondaryTable(SecondaryTable value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAccess(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAssociationOverride(int index, AssociationOverride value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAssociationOverride(AssociationOverride[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAttributeOverride(int index, AttributeOverride value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAttributeOverride(AttributeOverride[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAttributes(Attributes value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setClass2(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDiscriminatorColumn(DiscriminatorColumn value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDiscriminatorValue(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setEntityListeners(EntityListeners value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExcludeDefaultListeners(EmptyType value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExcludeSuperclassListeners(EmptyType value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIdClass(IdClass value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInheritance(Inheritance value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMetadataComplete(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNamedNativeQuery(int index, NamedNativeQuery value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNamedNativeQuery(NamedNativeQuery[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNamedQuery(int index, NamedQuery value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNamedQuery(NamedQuery[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostLoad(PostLoad value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostPersist(PostPersist value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostRemove(PostRemove value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPostUpdate(PostUpdate value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrePersist(PrePersist value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPreRemove(PreRemove value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPreUpdate(PreUpdate value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrimaryKeyJoinColumn(int index, PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrimaryKeyJoinColumn(PrimaryKeyJoinColumn[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecondaryTable(int index, SecondaryTable value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecondaryTable(SecondaryTable[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSequenceGenerator(SequenceGenerator value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSqlResultSetMapping(int index, SqlResultSetMapping value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSqlResultSetMapping(SqlResultSetMapping[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTable(Table value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTableGenerator(TableGenerator value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeAssociationOverride() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeAttributeOverride() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeNamedNativeQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeNamedQuery() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizePrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeSecondaryTable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeSqlResultSetMapping() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static Set<Entity> getEntityFromClasspath(Project project) {
        SourceGroup[] sgs = SourceGroupSupport.getJavaSourceGroups(project);
        
        Set<Entity> result = new HashSet<Entity>();
        if (sgs == null || sgs.length < 1) {
            return result;
        }
        
        ClasspathInfo cpi = ClasspathInfo.create(sgs[0].getRootFolder());
        List<TypeElement> entityElements = TypeUtil.getAnnotatedTypeElementsFromClasspath(
                cpi, 
                Constants.PERSISTENCE_ENTITY);
        
        for (TypeElement te : entityElements) {
            String entityName = null;
            Class entityClass = Util.getType(project, te.getQualifiedName().toString());
            if (entityClass != null) {
                Annotation annotation = TypeUtil.getJpaTableAnnotation(entityClass);
                if (annotation != null) {
                    entityName = TypeUtil.getAnnotationValueName(annotation);
                }
                if (entityName == null) {
                    annotation = TypeUtil.getJpaEntityAnnotation(entityClass);
                    entityName = TypeUtil.getAnnotationValueName(annotation);
                }
            }
            if (entityName == null) {
                entityName = te.getSimpleName().toString();
            }
            result.add(new RuntimeJpaEntity(te, entityName));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof Entity)) {
            return false;
        }
        final Entity other = (Entity) obj;
        return getClass2().equals(other.getClass2()); 
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

}
