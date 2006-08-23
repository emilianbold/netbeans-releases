/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model.deep;

import org.netbeans.modules.cnd.api.model.CsmScope;

/**
 * Represents while(), do ... while() and for() statements
 * (for statement has a specialized interface)
 *
 * @author Vladimir Kvashin
 */
public interface CsmLoopStatement extends CsmStatement, CsmScope {

    /** Gets condition */
    CsmCondition getCondition();

    /** Gets a statement, which is performed in the case condition returns true */
    CsmStatement getBody();

    /**
     * Distinguishes pre check and post check.
     * Returns true in the case of post-check (i.e. for do ... while statement),
     * otherwise false.
     */
    boolean isPostCheck();
    
}
