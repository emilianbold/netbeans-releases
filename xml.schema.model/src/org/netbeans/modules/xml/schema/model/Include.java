/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.net.URI;

/**
 * The interface represents the xs:include element which provides a staight 
 * inclusion of a schema into another schema with the same target namespace.
 * @author Chris Webster
 */
public interface Include extends SchemaComponent {
	public static final String SCHEMA_LOCATION_PROPERTY = "schemaLocation"; 
	
	String getSchemaLocation();
	void setSchemaLocation(String uri);
}
