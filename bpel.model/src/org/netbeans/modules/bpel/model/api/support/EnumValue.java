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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *
 */
package org.netbeans.modules.bpel.model.api.support;

/**
 * @author ads This is interface that represent value of attribute that have
 *         possible values of enumeration type. There can be situation when
 *         original value in XML cannot be represented in valid enum values. In
 *         this case this is invalid value and method "isInvalid" will return
 *         true. Method "toString" for enumeration will return invalid XML
 *         string in this case ( if enum is valid then it will return valid
 *         string representation ).
 */
public interface EnumValue {

    /**
     * @return Is valid or not this enumeration object.
     */
    boolean isInvalid();

}
