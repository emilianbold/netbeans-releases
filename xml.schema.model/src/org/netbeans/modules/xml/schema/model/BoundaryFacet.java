/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * Common interface for boundary facets MaxInclusive, MaxExclusive, MinInclusive, 
 * MinExclusive.
 *
 * @author nn136682
 */
public interface BoundaryFacet extends SchemaComponent {
        public static final String FIXED_PROPERTY = "fixed";
        public static final String VALUE_PROPERTY = "value";
        
	Boolean isFixed();
	void setFixed(Boolean isFixed);
	boolean getFixedDefault();
	boolean getFixedEffective();
	
	String getValue();
	void setValue(String maxValue);
}
