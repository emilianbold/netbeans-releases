/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.net.URI;

/**
 * This interface represents the xs:import statement which provides hints to 
 * the schema validator for how to resolve a uri.
 * @author Chris Webster
 */
public interface Import extends SchemaComponent  {
	
	public static final String SCHEMA_LOCATION_PROPERTY = "schemaLocation";
	public static final String NAMESPACE_PROPERTY = "namespace";
		
	String getNamespace();
	void setNamespace(String uri);
	
	String getSchemaLocation();
	void setSchemaLocation(String uri);
}
