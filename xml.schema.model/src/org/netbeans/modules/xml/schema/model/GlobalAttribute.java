/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents the attributes defined at the top level of the 
 * schema (direct children of the schema element). 
 * @author Chris Webster
 */
public interface GlobalAttribute extends Attribute, ReferenceableSchemaComponent  {
	GlobalReference<GlobalSimpleType> getType();
	void setType(GlobalReference<GlobalSimpleType> type);
	
	LocalSimpleType getInlineType();
	void setInlineType(LocalSimpleType type);
}
