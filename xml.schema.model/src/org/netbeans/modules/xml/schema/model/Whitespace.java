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
