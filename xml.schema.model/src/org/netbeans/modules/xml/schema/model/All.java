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
 * This interface represents the xml schema all type. The all 
 * type describes an unordered group of elements. 
 * @author Chris Webster
 */
public interface All extends GroupAll, ComplexExtensionDefinition, ComplexTypeDefinition, SchemaComponent {

        public static final String MIN_OCCURS_PROPERTY = "minOccurs";
        
        /**
	 * @return minimum occurrences, must be 0 <= x <= 1
	 */
	Occur.ZeroOne getMinOccurs();
	
	/**
	 * set the minimum number of occurs. 
	 * @param occurs must satisfy 0 <= occurs <= 1
	 */
	void setMinOccurs(Occur.ZeroOne occurs);
        
        /**
         * Returns default values for attribute minOccurs.
         */
        Occur.ZeroOne getMinOccursDefault();
        
        /**
         * Returns the actual value set by user or default value if not set.
         */
        Occur.ZeroOne getMinOccursEffective();
}
