/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.Referenceable;

/**
 * This interface represents a globally defined group of elements. 
 * @author Chris Webster
 */
public interface GlobalGroup extends SchemaComponent, ReferenceableSchemaComponent  {
    public static final String DEFINITION_PROPERTY = "definition";
    
	LocalGroupDefinition getDefinition();
	void setDefinition(LocalGroupDefinition definition);
}
