/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents the xs:keyref element which provides referential
 * integrity.
 * @author Chris Webster
 */
public interface KeyRef extends Constraint, SchemaComponent  {
    
    public static final String REFERER_PROPERTY = "referer";
    
    Constraint getReferer();
    void setReferer(Constraint c);
}
