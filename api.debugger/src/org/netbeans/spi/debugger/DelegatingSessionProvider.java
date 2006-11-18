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

package org.netbeans.spi.debugger;

import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.Session;


/**
 * Delegates {@link org.netbeans.api.debugger.DebuggerInfo}
 * support to some some existing
 * {@link org.netbeans.api.debugger.Session}.
 *
 * @author Jan Jancura
 * @deprecated This class is of no use. Nobody can create Session object, but debuggercore.
 */
// XXX: What this is for??
// XXX: Not usable anyway, Session is final with private constructor
// XXX: Should be deprecated? Or removed - can not be meaningfully implemented anyway...
public abstract class DelegatingSessionProvider {

    /**
     * Returns a {@link org.netbeans.api.debugger.Session} to delegate
     * on.
     *
     * @return Session to delegate on
     */
    public abstract Session getSession (
        DebuggerInfo debuggerInfo
    );
}

