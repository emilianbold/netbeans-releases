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

/*
 * AnyElement.java
 *
 * Created on September 29, 2005, 6:59 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.schema.model;

/**
 * AnyElement represents a wildcard that allows the insertion of any element belonging
 * to a list of namespaces. 
 * 
 * @author Chris Webster
 */
public interface AnyElement extends Any, SequenceDefinition, SchemaComponent {
	
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
