/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents a sequence definition. 
 * @author Chris Webster
 */
public interface Sequence extends CommonSequence, SequenceDefinition, 
ComplexExtensionDefinition, ComplexTypeDefinition, SchemaComponent  {
    
    public static final String MIN_OCCURS_PROPERTY = "minOccurs";
    public static final String MAX_OCCURS_PROPERTY = "maxOccurs";
    
	String getMaxOccurs();
	void setMaxOccurs(String occurs);
        
	Integer getMinOccurs();
	void setMinOccurs(Integer occurs);
        
        /**
         * Returns default values for attribute minOccurs.
         */
        int getMinOccursDefault();
        
        /**
         * Returns the actual value set by user or default value if not set.
         */
        int getMinOccursEffective();
	
        /**
         * Returns default values for attribute minOccurs.
         */
        String getMaxOccursDefault();
        
        /**
         * Returns the actual value set by user or default value if not set.
         */
        String getMaxOccursEffective();
}
