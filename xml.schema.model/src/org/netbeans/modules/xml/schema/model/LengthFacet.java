/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 *
 * @author nn136682
 */
public interface LengthFacet extends SchemaComponent {
    
        public static final String FIXED_PROPERTY = "fixed";
        public static final String VALUE_PROPERTY = "value";
        
	Boolean isFixed();
	void setFixed(Boolean fixed);
        boolean getFixedDefault();
        boolean getFixedEffective();
	
	int getValue();
	void setValue(int value);
    
}
