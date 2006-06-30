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
 * This interface represents a choice outside a definition of a group. 
 * @author Chris Webster
 */
public interface Choice extends CommonChoice, ComplexExtensionDefinition, 
ComplexTypeDefinition, SequenceDefinition, SchemaComponent {
        public static final String MAX_OCCURS_PROPERTY  = "maxOccurs";
	public static final String MIN_OCCURS_PROPERTY  = "minOccurs";
    
        /**
         * @return mmaxOccurs attribute value if set, otherwise null.
         */
	String getMaxOccurs();
        /**
         * Set maxOccurs attribute value.
         * @param mmx minOccurs value; null value means reset to default.
         */
	void setMaxOccurs(String max);
	
        /**
         * @return default values for attribute minOccurs.
         */
        String getMaxOccursDefault();
        
        /**
         * @return the actual value set by user or default value if not set.
         */
        String getMaxOccursEffective();
        
        /**
         * @return minOccurs attribute value if set, otherwise null.
         */
	Integer getMinOccurs();
        
        /**
         * Set minOccurs attribute value.
         * @param min minOccurs value; null value means reset to default.
         */
	void setMinOccurs(Integer min);

        /**
         * @return default value for attribute minOccurs.
         */
        int getMinOccursDefault();
        
        /**
         * @return the actual value set by user or default value if not set.
         */
        int getMinOccursEffective();
}
