/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
