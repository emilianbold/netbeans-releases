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
