/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.util.Collection;

/**
 * This interface represents the common signatures for components allowing
 * the creation of attributes. 
 * @author Chris Webster
 */
public interface LocalAttributeContainer extends SchemaComponent{
        public static final String LOCAL_ATTRIBUTE_PROPERTY = "localAttribute"; //NOI18N
        public static final String ATTRIBUTE_GROUP_REFERENCE_PROPERTY = "attributeGroupReferences"; //NOI18N
        public static final String ANY_ATTRIBUTE_PROPERTY = "anyAttribute"; //NOI18N
        
        
	Collection<LocalAttribute> getLocalAttributes();
	void addLocalAttribute(LocalAttribute attr);
	void removeLocalAttribute(LocalAttribute attr);
	
	Collection<AttributeReference> getAttributeReferences();
	void addAttributeReference(AttributeReference attr);
	void removeAttributeReference(AttributeReference attr);
	
	Collection<AttributeGroupReference> getAttributeGroupReferences();
	void addAttributeGroupReference(AttributeGroupReference ref);
	void removeAttributeGroupReference(AttributeGroupReference ref);
	
	AnyAttribute getAnyAttribute();
	void setAnyAttribute(AnyAttribute attr);
}
