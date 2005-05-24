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
/*
 * Principal.java
 *
 * Created on November 17, 2004, 5:12 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Principal extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String NAME = "Name";	// NOI18N
    
    /** Setter for name property
     * @param value property value
     */
    public void setName(java.lang.String value);
    /** Getter for name property.
     * @return property value
     */
    public java.lang.String getName();
}
