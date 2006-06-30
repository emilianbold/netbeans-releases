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
 * ServiceQname.java
 *
 * Created on November 18, 2004, 10:29 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ServiceQname extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

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
