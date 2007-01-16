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
package org.netbeans.modules.refactoring.api.impl;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProblemDetails;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
public abstract class APIAccessor {
    public static APIAccessor DEFAULT;

    static {
        Class c = AbstractRefactoring.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract Collection getGBHandlers(AbstractRefactoring refactoring);
    public abstract Problem chainProblems(Problem p, Problem p1);
    public abstract ProblemDetails createProblemDetails(ProblemDetailsImplementation pdi);
    public abstract boolean isCommit(RefactoringSession session);
    public abstract RefactoringElementImplementation getRefactoringElementImplementation(RefactoringElement el) ;
}
