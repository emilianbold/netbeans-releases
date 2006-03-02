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
