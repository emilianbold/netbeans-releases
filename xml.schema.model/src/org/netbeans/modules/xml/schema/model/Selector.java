/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents the selector element.
 * @author Chris Webster
 */
public interface Selector extends SchemaComponent  {
        public static final String XPATH_PROPERTY = "xPath"; //NOI18N
	String getXPath();
	void setXPath(String xPath);
}
