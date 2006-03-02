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
