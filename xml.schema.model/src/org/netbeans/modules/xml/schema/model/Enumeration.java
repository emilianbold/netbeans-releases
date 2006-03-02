/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents the enumeration facet which defines a list of 
 * possible values for a primitive type.
 * @author Chris Webster
 */
public interface Enumeration extends SchemaComponent {
        public static final String VALUE_PROPERTY = "value"; //NOI18N
    
	String getValue();
	void setValue(String value);
}
