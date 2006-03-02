/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents a simple type.
 * @author Chris Webster
 */
public interface SimpleType {
	public static final String DEFINITION_PROPERTY = "definition";
        
	SimpleTypeDefinition getDefinition();
	void setDefinition(SimpleTypeDefinition def);
}
