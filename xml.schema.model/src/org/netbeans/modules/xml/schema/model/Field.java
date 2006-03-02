/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface defines the location of the fields used in a constraint.
 * @author Chris Webster
 */
public interface Field extends SchemaComponent {
    public static final String XPATH_PROPERTY = "xpath"; //NOI18N
    
    String getXPath();
    void setXPath(String xPath);
    
    
}
