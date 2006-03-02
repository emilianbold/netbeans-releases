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
