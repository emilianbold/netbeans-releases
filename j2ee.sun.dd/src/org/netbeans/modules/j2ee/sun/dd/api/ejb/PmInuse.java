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
 * PmInuse.java
 *
 * Created on November 18, 2004, 10:40 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PmInuse extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String PM_IDENTIFIER = "PmIdentifier";	// NOI18N
    public static final String PM_VERSION = "PmVersion";	// NOI18N

    /** Setter for pm-identifier property
     * @param value property value
     */
    public void setPmIdentifier(java.lang.String value);
    /** Getter for pm-identifier property.
     * @return property value
     */
    public java.lang.String getPmIdentifier();
    /** Setter for pm-version property
     * @param value property value
     */
    public void setPmVersion(java.lang.String value);
    /** Getter for pm-version property.
     * @return property value
     */
    public java.lang.String getPmVersion();
    
}
