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
package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.NbBundle;
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
	
	public SchemaModel resolveReferencedModel() throws CatalogModelException {
	    ModelSource ms = resolveModel(getSchemaLocation());
        return SchemaModelFactory.getDefault().getModel(ms);
	}
}
