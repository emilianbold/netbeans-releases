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
 * WsdlPort.java
 *
 * Created on November 18, 2004, 12:06 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface WsdlPort extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String NAMESPACEURI = "NamespaceURI";	// NOI18N
    public static final String LOCALPART = "Localpart";	// NOI18N
    
    /** Setter for namespaceURI property
     * @param value property value
     */
    public void setNamespaceURI(java.lang.String value);
    /** Getter for namespaceURI property.
     * @return property value
     */
    public java.lang.String getNamespaceURI();
    /** Setter for localpart property
     * @param value property value
     */
    public void setLocalpart(java.lang.String value);
    /** Getter for localpart property.
     * @return property value
     */
    public java.lang.String getLocalpart();
}
