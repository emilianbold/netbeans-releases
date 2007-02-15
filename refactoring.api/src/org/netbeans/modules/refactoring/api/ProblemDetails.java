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

import javax.swing.Action;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.openide.util.Cancellable;

/**
 * This class holds details of a Problem
 * @author Jan Becicka
 * @since 1.5.0
 */
public final class ProblemDetails {

    private ProblemDetailsImplementation pdi;

    ProblemDetails (ProblemDetailsImplementation pdi) {
        this.pdi=pdi;
    }

    /**
     * This method will typically invoke component with ProblemDetails.
     * It is fully upon clients, how this component will be implemented.
     * @param rerunRefactoringAction this action is passed to client component
     * to allow clients to invoke refactoring once the Problem is fixed.
     * @param parent component, which can be cancelled
     * @see ProblemDetailsImplementation
     */
    public void showDetails(Action rerunRefactoringAction, Cancellable parent) {
        pdi.showDetails(rerunRefactoringAction, parent);
    }
    
    /**
     * Message that will be displayed in parameters panel as a hint to suggest user,
     * that there are more details available.
     * @return string representation of hint
     */
    public String getDetailsHint() {
        return pdi.getDetailsHint();
    }
}
