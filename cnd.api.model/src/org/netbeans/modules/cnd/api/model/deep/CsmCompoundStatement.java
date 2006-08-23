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
import java.util.List;

/**
 * Represents compound statement;
 * acts as an ancestor for some derived interfaces,
 * which aren't compound statements
 * as such, e.g. CsmCatchClause
 *
 * @author Vladimir Kvashin
 */
public interface CsmCompoundStatement extends CsmStatement, CsmScope {
    /** Gets statements this one consists of*/
    List/*<CsmStatement>*/ getStatements();
}
