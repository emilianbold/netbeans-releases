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


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.ComponentFactory;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 * Factory for providing concrete implementations of the CommonSchemaElement
 * subclasses.
 * @author Chris Webster
 */
public interface SchemaComponentFactory extends ComponentFactory<SchemaComponent> {
    All createAll();
    AllElement createAllElement();
    AllElementReference createAllElementReference();
    Annotation createAnnotation();
    AnyElement createAny();
    AnyAttribute createAnyAttribute();
    AppInfo createAppInfo();
    AttributeGroupReference createAttributeGroupReference();
    Choice createChoice();
    ComplexContent createComplexContent();
    ComplexContentRestriction createComplexContentRestriction();
    ComplexExtension createComplexExtension();
    Documentation createDocumentation();
    Enumeration createEnumeration();
    Field createField();
    FractionDigits createFractionDigits();
    GlobalAttribute createGlobalAttribute();
    GlobalAttributeGroup createGlobalAttributeGroup();
    GlobalComplexType createGlobalComplexType();
    GlobalElement createGlobalElement();
    GlobalSimpleType createGlobalSimpleType();
    GroupAll createGroupAll();
    GroupChoice createGroupChoice();
    GlobalGroup createGroupDefinition();
    GroupReference createGroupReference();
    GroupSequence createGroupSequence();
    Import createImport();
    Include createInclude();
    Key createKey();
    KeyRef createKeyRef();
    Length createLength();
    List createList();
    LocalAttribute createLocalAttribute();
    AttributeReference createAttributeReference();
    LocalComplexType createLocalComplexType();
    LocalElement createLocalElement();
    ElementReference createElementReference();
    LocalSimpleType createLocalSimpleType();
    MaxExclusive createMaxExclusive();
    MaxInclusive createMaxInclusive();
    MaxLength createMaxLength();
    MinInclusive createMinInclusive();
    MinExclusive createMinExclusive();
    MinLength createMinLength();
    Notation createNotation();
    Pattern createPattern();
    Redefine createRedefine();
    Schema createSchema();
    Sequence createSequence();
    Selector createSelector();
    SimpleContent createSimpleContent();
    SimpleContentRestriction createSimpleContentRestriction();
    SimpleExtension createSimpleExtension();
    SimpleTypeRestriction createSimpleTypeRestriction();
    TotalDigits createTotalDigits();
    Union createUnion();
    Unique createUnique();
    Whitespace createWhitespace();
    <T extends ReferenceableSchemaComponent> GlobalReference<T> 
        createGlobalReference(T referenced, Class<T> c, SchemaComponent referencing);
}
