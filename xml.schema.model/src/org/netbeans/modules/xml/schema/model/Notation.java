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
