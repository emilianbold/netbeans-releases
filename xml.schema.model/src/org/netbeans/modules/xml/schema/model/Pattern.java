/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents xs:pattern used to define regular expressions over 
 * the lexical space.
 * @author Chris Webster
 */
public interface Pattern extends SchemaComponent  {
        public static final String VALUE_PROPERTY = "value";

        String getValue();
	void setValue(String value);
}
