/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
