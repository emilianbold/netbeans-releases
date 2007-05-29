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
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class AttributesImpl implements Attributes {

    private final EntityImpl entity;

    private boolean fieldAccess;
    private EmbeddedId embeddedId;
    private final List<Id> idList = new ArrayList<Id>();
    private final List<Basic> basicList = new ArrayList<Basic>();
    private final List<OneToOne> oneToOneList = new ArrayList<OneToOne>();
    private final List<OneToMany> oneToManyList = new ArrayList<OneToMany>();
    private final List<ManyToOne> manyToOneList = new ArrayList<ManyToOne>();
    private final List<ManyToMany> manyToManyList = new ArrayList<ManyToMany>();

    public AttributesImpl(EntityImpl entity) {
        this.entity = entity;
        TypeElement typeElement = entity.getTypeElement();
        if (typeElement == null) {
            // entity was removed, we should get an event soon
            // XXX log
            return;
        }
        AnnotationModelHelper helper = entity.getRoot().getHelper();

        List<? extends Element> elements = typeElement.getEnclosedElements();
        fieldAccess = EntityMappingsUtilities.hasFieldAccess(helper, elements);

        // handle this entity's attributes
        for (Element element : elements) {
            ElementKind elementKind = element.getKind();
            if (fieldAccess) {
                if (ElementKind.FIELD.equals(elementKind)) {
                    handleField(element);
                }
            } else {
                if (ElementKind.METHOD.equals(elementKind)) {
                    handleField(element);
                }
            }
        }
    }

    private void handleField(Element element) {
        AnnotationModelHelper helper = entity.getRoot().getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(element.getAnnotationMirrors());
        if (EntityMappingsUtilities.isTransient(annByType, element.getModifiers())) {
            return;
        }
        String propertyName = element.getSimpleName().toString();
        if (ElementKind.METHOD.equals(element.getKind())) {
            propertyName = EntityMappingsUtilities.getterNameToPropertyName(propertyName);
            if (propertyName == null) {
                return;
            }
        }

        AnnotationMirror oneToOneAnnotation = annByType.get("javax.persistence.OneToOne"); // NOI18N
        AnnotationMirror oneToManyAnnotation = annByType.get("javax.persistence.OneToMany"); // NOI18N
        AnnotationMirror manyToOneAnnotation = annByType.get("javax.persistence.ManyToOne"); // NOI18N
        AnnotationMirror manyToManyAnnotation = annByType.get("javax.persistence.ManyToMany"); // NOI18N
        AnnotationMirror idAnnotation = annByType.get("javax.persistence.Id"); // NOI18N
        AnnotationMirror embeddedIdAnnotation = annByType.get("javax.persistence.EmbeddedId"); // NOI18N

        if (oneToOneAnnotation != null) {
            oneToOneList.add(new OneToOneImpl(helper, element, oneToOneAnnotation, propertyName, annByType));
        } else if (oneToManyAnnotation != null) {
            oneToManyList.add(new OneToManyImpl(helper, element, oneToManyAnnotation, propertyName, annByType));
        } else if (manyToOneAnnotation != null) {
            manyToOneList.add(new ManyToOneImpl(helper, element, manyToOneAnnotation, propertyName, annByType));
        } else if (manyToManyAnnotation != null) {
            manyToManyList.add(new ManyToManyImpl(helper, element, manyToManyAnnotation, propertyName, annByType));
        } else {
            // not a relationship field
            if (embeddedIdAnnotation != null) {
                embeddedId = new EmbeddedIdImpl(propertyName);
            } else {
                AnnotationMirror columnAnnotation = annByType.get("javax.persistence.Column"); // NOI18N
                Column column = new ColumnImpl(helper, columnAnnotation, propertyName.toUpperCase()); // NOI18N
                if (idAnnotation != null) {
                    idList.add(new IdImpl(propertyName, column));
                } else {
                    AnnotationMirror basicAnnotation = annByType.get("javax.persistence.Basic"); // NOI18N
                    basicList.add(new BasicImpl(helper, basicAnnotation, propertyName, column));
                }
            }
        }
    }

    boolean hasFieldAccess() {
        return fieldAccess;
    }

    public void setId(int index, Id value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Id getId(int index) {
        return idList.get(index);
    }

    public int sizeId() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setId(Id[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Id[] getId() {
        return idList.toArray(new Id[idList.size()]);
    }

    public int addId(Id value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeId(Id value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Id newId() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEmbeddedId(EmbeddedId value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmbeddedId getEmbeddedId() {
        return embeddedId;
    }

    public EmbeddedId newEmbeddedId() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setBasic(int index, Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic getBasic(int index) {
        return basicList.get(index);
    }

    public int sizeBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setBasic(Basic[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic[] getBasic() {
        return basicList.toArray(new Basic[basicList.size()]);
    }

    public int addBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeBasic(Basic value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Basic newBasic() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setVersion(int index, Version value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Version getVersion(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeVersion() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setVersion(Version[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Version[] getVersion() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addVersion(Version value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeVersion(Version value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Version newVersion() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setManyToOne(int index, ManyToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public ManyToOne getManyToOne(int index) {
        return manyToOneList.get(index);
    }

    public int sizeManyToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setManyToOne(ManyToOne[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public ManyToOne[] getManyToOne() {
        return manyToOneList.toArray(new ManyToOne[manyToOneList.size()]);
    }

    public int addManyToOne(ManyToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeManyToOne(ManyToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public ManyToOne newManyToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setOneToMany(int index, OneToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public OneToMany getOneToMany(int index) {
        return oneToManyList.get(index);
    }

    public int sizeOneToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setOneToMany(OneToMany[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public OneToMany[] getOneToMany() {
        return oneToManyList.toArray(new OneToMany[oneToManyList.size()]);
    }

    public int addOneToMany(OneToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeOneToMany(OneToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public OneToMany newOneToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setOneToOne(int index, OneToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public OneToOne getOneToOne(int index) {
        return oneToOneList.get(index);
    }

    public int sizeOneToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setOneToOne(OneToOne[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public OneToOne[] getOneToOne() {
        return oneToOneList.toArray(new OneToOne[oneToOneList.size()]);
    }

    public int addOneToOne(OneToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeOneToOne(OneToOne value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public OneToOne newOneToOne() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setManyToMany(int index, ManyToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public ManyToMany getManyToMany(int index) {
        return manyToManyList.get(index);
    }

    public int sizeManyToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setManyToMany(ManyToMany[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public ManyToMany[] getManyToMany() {
        return manyToManyList.toArray(new ManyToMany[manyToManyList.size()]);
    }

    public int addManyToMany(ManyToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeManyToMany(ManyToMany value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public ManyToMany newManyToMany() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEmbedded(int index, Embedded value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embedded getEmbedded(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeEmbedded() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setEmbedded(Embedded[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embedded[] getEmbedded() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addEmbedded(Embedded value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeEmbedded(Embedded value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Embedded newEmbedded() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTransient(int index, Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient getTransient(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTransient(Transient[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient[] getTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeTransient(Transient value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Transient newTransient() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
