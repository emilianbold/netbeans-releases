/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents commanlity between global and local attributes.
 * @author Chris Webster
 */
public interface Attribute extends SchemaComponent {
        public static final String DEFAULT_PROPERTY = "default";
        public static final String FIXED_PROPERTY = "fixed";
        public static final String TYPE_PROPERTY = "type";
        public static final String INLINE_TYPE_PROPERTY = "inlineType";
    
	String getDefault();
	void setDefault(String defaultValue);
	
	String getFixed();
	void setFixed(String fixedValue);
	
	enum Use {
            PROHIBITED("prohibited"), OPTIONAL("optional"), REQUIRED("required");
            String value;
            Use(String s) {
                value = s;
            }
            public String toString() { return value; }
	}
}
