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
package org.netbeans.modules.refactoring.api;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
final class AccessorImpl extends APIAccessor {
    public Collection<GuardedBlockHandler> getGBHandlers(AbstractRefactoring refactoring) {
        assert refactoring != null;
        return refactoring.getGBHandlers();
    }
    
    public boolean hasPluginsWithProgress(AbstractRefactoring refactoring) {
        return refactoring.pluginsWithProgress!=null && !refactoring.pluginsWithProgress.isEmpty();
    }

    public Problem chainProblems(Problem p, Problem p1) {
        return AbstractRefactoring.chainProblems(p, p1);
    }
    
    public ProblemDetails createProblemDetails(ProblemDetailsImplementation pdi) {
        assert pdi != null;
        return new ProblemDetails(pdi);
    }

    public boolean isCommit(RefactoringSession session) {
        return session.realcommit;
    }
    
    public RefactoringElementImplementation getRefactoringElementImplementation(RefactoringElement el) {
        return el.impl;
    }
}
