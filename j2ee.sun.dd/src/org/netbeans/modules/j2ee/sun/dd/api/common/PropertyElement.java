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
 * PropertyElement.java
 *
 * Created on November 18, 2004, 10:45 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PropertyElement extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

     public static final String NAME = "Name";	// NOI18N
     public static final String VALUE = "Value";	// NOI18N
     public static final String DESCRIPTION = "Description";	// NOI18N

    /** Setter for name property
     * @param value property value
     */
    public void setName(java.lang.String value);
    /** Getter for name property.
     * @return property value
     */
    public java.lang.String getName();
    /** Setter for value property
     * @param value property value
     */
    public void setValue(java.lang.String value);
    /** Getter for value property.
     * @return property value
     */
    public java.lang.String getValue();
}
