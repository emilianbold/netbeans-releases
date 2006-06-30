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
import java.util.Collection;
/**
 * This interface represents the xs:redefine element. 
 * @author Chris Webster
 */
public interface Redefine extends SchemaComponent  {
	
	public static final String SCHEMA_LOCATION_PROPERTY = "schemaLocation";
	public static final String COMPLEX_TYPE_PROPERTY = "complexType";
	public static final String ATTRIBUTE_GROUP_PROPERTY = "attributeGroup";
	public static final String SIMPLE_TYPE_PROPERTY = "simpleType";
	public static final String GROUP_DEFINITION_PROPERTY = "groupDefinition";
	
	String getSchemaLocation();
	void setSchemaLocation(String uri);
	
	Collection<GlobalSimpleType> getSimpleTypes();
	void addSimpleType(GlobalSimpleType type);
	void removeSimpleType(GlobalSimpleType type);
	
	Collection<GlobalComplexType> getComplexTypes();
	void addComplexType(GlobalComplexType type);
	void removeComplexType(GlobalComplexType type);
	
	Collection<GlobalGroup> getGroupDefinitions();
	void addGroupDefinition(GlobalGroup def);
	void removeGroupDefinition(GlobalGroup def);
	
	Collection<GlobalAttributeGroup> getAttributeGroups();
	void addAttributeGroup(GlobalAttributeGroup group);
	void removeAttributeGroup(GlobalAttributeGroup group);
}
