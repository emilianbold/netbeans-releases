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
