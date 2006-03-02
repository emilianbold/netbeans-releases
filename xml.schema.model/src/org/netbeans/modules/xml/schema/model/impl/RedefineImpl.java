/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * RedefineImpl.java
 *
 * Created on October 7, 2005, 8:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class RedefineImpl extends SchemaComponentImpl implements Redefine {
	
        public RedefineImpl(SchemaModelImpl model) {
            this(model,createNewComponent(SchemaElements.REDEFINE,model));
        }
	/**
     * Creates a new instance of RedefineImpl
     */
	public RedefineImpl(SchemaModelImpl model, Element el) {
		super(model, el);
	}

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Redefine.class;
	}
	
	/**
	 *
	 */
	public void setSchemaLocation(String uri) {
		setAttribute(SCHEMA_LOCATION_PROPERTY, SchemaAttributes.SCHEMA_LOCATION, uri);
	}
	
	/**
	 *
	 */
	public void addComplexType(GlobalComplexType type) {
		appendChild(COMPLEX_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void removeComplexType(GlobalComplexType type) {
		removeChild(COMPLEX_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void addAttributeGroup(GlobalAttributeGroup group) {
		appendChild(ATTRIBUTE_GROUP_PROPERTY, group);
	}
	
	/**
	 *
	 */
	public void removeAttributeGroup(GlobalAttributeGroup group) {
		removeChild(ATTRIBUTE_GROUP_PROPERTY, group);
	}
	
	/**
	 *
	 */
	public void removeSimpleType(GlobalSimpleType type) {
		removeChild(SIMPLE_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void addSimpleType(GlobalSimpleType type) {
		appendChild(SIMPLE_TYPE_PROPERTY, type);
	}
	
	/**
	 *
	 */
	public void accept(SchemaVisitor visitor) {
		visitor.visit(this);
	}
	
	/**
	 *
	 */
	public void addGroupDefinition(GlobalGroup def) {
		appendChild(GROUP_DEFINITION_PROPERTY, def);
	}
	
	/**
	 *
	 */
	public void removeGroupDefinition(GlobalGroup def) {
		removeChild(GROUP_DEFINITION_PROPERTY, def);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalAttributeGroup> getAttributeGroups() {
		return getChildren(GlobalAttributeGroup.class);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalComplexType> getComplexTypes() {
		return getChildren(GlobalComplexType.class);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalGroup> getGroupDefinitions() {
		return getChildren(GlobalGroup.class);
	}
	
	/**
	 *
	 */
	public String getSchemaLocation() {
		   return getAttribute(SchemaAttributes.SCHEMA_LOCATION);
	}
	
	/**
	 *
	 */
	public java.util.Collection<GlobalSimpleType> getSimpleTypes() {
		return getChildren(GlobalSimpleType.class);
	}
}
