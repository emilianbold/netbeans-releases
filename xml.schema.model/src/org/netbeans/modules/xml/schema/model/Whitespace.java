/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents the whitespace facet.
 * @author Chris Webster
 */
public interface Whitespace extends SchemaComponent  {
        public static final String FIXED_PROPERTY = "fixed";
        public static final String VALUE_PROPERTY = "value";
        
	Boolean isFixed();
	void setFixed(Boolean iFixed);
	boolean getFixedDefault();
        boolean getFixedEffective();
        
	enum Treatment {
		PRESERVE("preserve"), REPLACE("replace"), COLLAPSE("collapse");
                Treatment(String value) {
                    this.value = value;
                }
                public String toString() {
                    return value;
                }
                private String value;
	}
	
	Treatment getValue();
	void setValue(Treatment whitespaceTreatment);
}
