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
package org.netbeans.modules.j2ee.sun.dd.api;


/**
 * Interface representing the root of interfaces bean tree structure.
 *
*
 */
public interface RootInterface extends CommonDDBean {    
    
    public static final String PROPERTY_STATUS="dd_status";
    public static final String PROPERTY_VERSION="dd_version";
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;
    public static final int STATE_VALID=0;
    
 
    /** Setter for version property.
     * Warning : Only the upgrade from lower to higher version is supported.
     * @param version 
     */
    public void setVersion(java.math.BigDecimal version);
    
    /** Getter for version property.
     * @return property value
     */
    public java.math.BigDecimal getVersion();
        
}
