/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents the xs:list element, which is a whitespace 
 * separated list of values.
 * @author Chris Webster
 */
public interface List extends SimpleTypeDefinition, SchemaComponent  {
        public static final String TYPE_PROPERTY = "type";
        public static final String INLINE_TYPE_PROPERTY = "inlineType";
        
	GlobalReference<GlobalSimpleType> getType();
	void setType(GlobalReference<GlobalSimpleType> t);
	
	LocalSimpleType getInlineType();
	void setInlineType(LocalSimpleType st);
}
