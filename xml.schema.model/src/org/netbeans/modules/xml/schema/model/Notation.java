/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.schema.model;

/**
 * Represents schema component 'notation'.
 *
 * @author Nam Nguyen
 */
public interface Notation extends SchemaComponent, ReferenceableSchemaComponent {
    public static final String PUBLIC_PROPERTY = "public";
    public static final String SYSTEM_PROPERTY = "system";
    
    /**
     * @return public identifier of this notation.
     */
    String getPublicIdentifier();
    /**
     * Set the public identifier of this notation.
     */
    void setPublicIdentifier(String publicID);
    
    /**
     * @return system identifier of this notation.
     */
    String getSystemIdentifier();
    /**
     * Sets system identifier of this notation.
     */
    void setSystemIdentifier(String systemID);
  
}
