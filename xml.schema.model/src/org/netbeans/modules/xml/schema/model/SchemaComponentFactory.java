/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
