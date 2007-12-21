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
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.support.TBoolean;

/**
 * @author ads
 */
public interface JoinFailureSuppressor {

    /**
     * suppressJoinFailure attribute name.
     */
    String SUPPRESS_JOIN_FAILURE = "suppressJoinFailure"; // NOI18N

    /**
     * Gets the value of the suppressJoinFailure property.
     *
     * @return possible type is TBoolen
     */
    TBoolean getSuppressJoinFailure();

    /**
     * Sets the value of the suppressJoinFailure property.
     *
     * @param value
     *            allowed type is TBoolean
     */
    void setSuppressJoinFailure( TBoolean value );

    /**
     * Removes attribute suppressJoinFailure.
     */
    void removeSuppressJoinFailure();

}
