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
 * SasContext.java
 *
 * Created on November 18, 2004, 10:05 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface SasContext extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String CALLER_PROPAGATION = "CallerPropagation";	// NOI18N
    
    /** Setter for caller-propagation property
     * @param value property value
     */
    public void setCallerPropagation(java.lang.String value);
    /** Getter for caller-propagation property.
     * @return property value
     */
    public java.lang.String getCallerPropagation();
    
}
