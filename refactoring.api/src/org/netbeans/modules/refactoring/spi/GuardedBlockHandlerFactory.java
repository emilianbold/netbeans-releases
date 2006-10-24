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
package org.netbeans.modules.refactoring.spi;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;

/** Factory for an object handling refactoring in a guarded block. This should be
 * implemented by modules providing guarded sections in Java documents. If
 * a change proposed by a refactoring affects a guarded section, the refactoring object
 * asks the registered GuardedBlockHandlers to handle that change.
 *
 * @author Martin Matula
 */
public interface GuardedBlockHandlerFactory {
    /** Creates and returns a new instance of the guarded block refactoring handler or
     * null if the handler is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the handler should be plugged in.
     * @return Instance of GuardedBlockHandler or null if the handler is not applicable to
     * the passed refactoring.
     */
    GuardedBlockHandler createInstance(AbstractRefactoring refactoring);
}
