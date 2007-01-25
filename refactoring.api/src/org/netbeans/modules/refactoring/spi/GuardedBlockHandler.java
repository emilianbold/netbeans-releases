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

import java.util.Collection;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;

/** Interface implemented by guarded block refactoring handlers. Contains a callback method
 * that gets a RefactoringElementImplementation affecting a guarded block as a parameter and can return
 * the new RefactoringElementImplementation that will replace the passed RefactoringElementImplementations
 * in the result collection of refactoring elements for a given refactoring.
 *
 * @author Martin Matula
 */
public interface GuardedBlockHandler {
    /** Collects replacements for refactoring element affecting a guarded block.
     * @param proposedChange RefactoringElementImplementation that affects a guarded block.
     * @param replacements Empty collection where the method implementation should add the
     * replacement RefactoringElementImplementations if this GuardedBlockHandler can handle changes in the
     * guarded block the original RefactoringElementImplementation affects.
     * @param replacement collection of Transactions. 
     *
     * @return Problems found or null (if no problems were identified)
     */
    Problem handleChange(RefactoringElementImplementation proposedChange, Collection<RefactoringElementImplementation> replacements, Collection<Transaction> transaction);
}
