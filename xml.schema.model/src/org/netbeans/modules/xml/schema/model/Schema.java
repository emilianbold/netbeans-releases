/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Derivation.Type;
import org.netbeans.modules.xml.xam.EmbeddableRoot;

/**
 * This interface represents the schema element.
 * @author Chris Webster
 */
public interface Schema extends SchemaComponent, EmbeddableRoot {
	public static final String TARGET_NAMESPACE_PROPERTY = "targetNamespace";
	public static final String BLOCK_DEFAULT_PROPERTY = "blockDefault";
	public static final String ATTRIBUTE_FORM_DEFAULT_PROPERTY = "attributeFormDefault";
	public static final String FINAL_DEFAULT_PROPERTY = "finalDefault";
	public static final String LANGUAGE_PROPERTY = "language";
	public static final String ELEMENT_FORM_DEFAULT_PROPERTY = "elementFormDefault";
	public static final String VERSION_PROPERTY = "version";
	public static final String SCHEMA_REFERENCES_PROPERTY = "schemaReferences";
	public static final String ATTRIBUTES_PROPERTY = "attributes";
	public static final String ELEMENTS_PROPERTY = "elements";
	public static final String ATTRIBUTE_GROUPS_PROPERTY = "attributeGroups";
	public static final String SIMPLE_TYPES_PROPERTY = "simpleTypes";
	public static final String COMPLEX_TYPES_PROPERTY = "complexTypes";
	public static final String GROUPS_PROPERTY = "groups";
	public static final String NOTATIONS_PROPERTY = "notations";
	
	Form getAttributeFormDefault();
	void setAttributeFormDefault(Form form);
        /**
         * @return default for schema global default value for 'form' property on attributes.
         */
	Form getAttributeFormDefaultDefault();
        Form getAttributeFormDefaultEffective();

        public enum Block implements Derivation {
            ALL(Type.ALL), RESTRICTION(Type.RESTRICTION), EXTENSION(Type.EXTENSION), SUBSTITUTION(Type.SUBSTITUTION), EMPTY(Type.EMPTY);
            private Derivation.Type value;
            Block(Derivation.Type v) { value = v; }
            public String toString() { return value.toString(); }
        }
	Set<Block> getBlockDefault();
	void setBlockDefault(Set<Block> blockDefault);
        /**
         * @return default for schema global default value for 'block' property.
         */
	Set<Block> getBlockDefaultDefault();
        Set<Block> getBlockDefaultEffective();
	
	Form getElementFormDefault();
	void setElementFormDefault(Form form);
        /**
         * @return default for schema global default value for 'form' property on elements.
         */
	Form getElementFormDefaultDefault();
        Form getElementFormDefaultEffective();
	
        public enum Final implements Derivation {
            ALL(Type.ALL), RESTRICTION(Type.RESTRICTION), EXTENSION(Type.EXTENSION), LIST(Type.LIST), UNION(Type.UNION), EMPTY(Type.EMPTY);
            private Derivation.Type value;
            Final(Derivation.Type v) { value = v; }
            public String toString() { return value.toString(); }
        }
	Set<Final> getFinalDefault();
	void setFinalDefault(Set<Final> finalDefault);
        /**
         * @return default for schema global default value for 'final' property.
         */
	Set<Final> getFinalDefaultDefault();
        Set<Final> getFinalDefaultEffective();
	
	String getTargetNamespace();
	void setTargetNamespace(String uri);
	
	String getVersion();
	void setVersion(String ver);
	
	String getLanguage();
	void setLanguage(String language);
	
	// Content
	// import, include, redefine
	Collection<SchemaModelReference> getSchemaReferences();
	Collection<Import> getImports();
	Collection<Include> getIncludes();
	Collection<Redefine> getRedefines();
	void addExternalReference(SchemaModelReference ref);
	void removeExternalReference(SchemaModelReference ref);
	
	Collection<GlobalAttribute> getAttributes();
	void addAttribute(GlobalAttribute attr);
	void removeAttribute(GlobalAttribute attr);
	
	Collection<GlobalElement> getElements();
	void addElement(GlobalElement element);
	void removeElement(GlobalElement element);
        
        Collection<GlobalElement> findAllGlobalElements();
	
	Collection<GlobalAttributeGroup> getAttributeGroups();
	void addAttributeGroup(GlobalAttributeGroup group);
	void removeAttributeGroup(GlobalAttributeGroup group);
	
	Collection<GlobalSimpleType> getSimpleTypes();
	void addSimpleType(GlobalSimpleType type);
	void removeSimpleType(GlobalSimpleType type);
	
	Collection<GlobalComplexType> getComplexTypes();
	void addComplexType(GlobalComplexType type);
	void removeComplexType(GlobalComplexType type);
        
        Collection<GlobalType> findAllGlobalTypes();
	
	Collection<GlobalGroup> getGroups();
	void addGroup(GlobalGroup group);
	void removeGroup(GlobalGroup group);
	
	Collection<Notation> getNotations();
	void addNotation(Notation notation);
	void removeNotation(Notation notation);
	
	Map<String, String> getPrefixes();
	void addPrefix(String prefix, String namespace);
	void removePrefix(String prefix);
}
