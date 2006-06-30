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
