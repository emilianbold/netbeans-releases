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
 * GenClasses.java
 *
 * Created on November 17, 2004, 5:18 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface GenClasses extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String REMOTE_IMPL = "RemoteImpl";	// NOI18N
    public static final String LOCAL_IMPL = "LocalImpl";	// NOI18N
    public static final String REMOTE_HOME_IMPL = "RemoteHomeImpl";	// NOI18N
    public static final String LOCAL_HOME_IMPL = "LocalHomeImpl";	// NOI18N
        
    /** Setter for remote-impl property
     * @param value property value
     */
    public void setRemoteImpl(java.lang.String value);
    /** Getter for remote-impl property.
     * @return property value
     */
    public java.lang.String getRemoteImpl();
    /** Setter for local-impl property
     * @param value property value
     */
    public void setLocalImpl(java.lang.String value);
    /** Getter for local-impl property.
     * @return property value
     */
    public java.lang.String getLocalImpl();
    /** Setter for remote-home-impl property
     * @param value property value
     */
    public void setRemoteHomeImpl(java.lang.String value);
    /** Getter for remote-home-impl property.
     * @return property value
     */
    public java.lang.String getRemoteHomeImpl();
    /** Setter for local-home-impl property
     * @param value property value
     */
    public void setLocalHomeImpl(java.lang.String value);
    /** Getter for local-home-impl property.
     * @return property value
     */
    public java.lang.String getLocalHomeImpl();
}
