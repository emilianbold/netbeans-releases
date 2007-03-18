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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Context;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;

/**
 * This a base class for Faces refactoring plugins.
 * 
 * @author 
 */
abstract class FacesRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {
    private AbstractRefactoring abstractRefactoring;
    private boolean userInvokedRefactoring;
    private boolean requestCancelled;
    
    protected FacesRefactoringPlugin(AbstractRefactoring abstractRefactoring, boolean userInvokedRefactoring) {
        this.abstractRefactoring = abstractRefactoring;
        this.userInvokedRefactoring = userInvokedRefactoring;
    }
    
    protected Context getContext() {
        return abstractRefactoring.getContext();
    }
    
    protected AbstractRefactoring getRefactoring() {
        return abstractRefactoring;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    public void cancelRequest() {
        requestCancelled = true;
    }
    
    protected boolean isUserInvokedRefactoring() {
        return userInvokedRefactoring;
    }

    protected boolean isRequestCancelled() {
        return requestCancelled;
    }
    /**
     * Quick checking being done on typing within rename panel
     */
    public abstract Problem fastCheckParameters();
    
    public Problem checkParameters() {
        return null;
    }

    public abstract Problem prepare(RefactoringElementsBag refactoringElements);


    public void start(ProgressEvent event) {
        fireProgressListenerStart(event.getOperationType(), event.getCount());
    }

    public void step(ProgressEvent event) {
        fireProgressListenerStep();
    }

    public void stop(ProgressEvent event) {
        fireProgressListenerStop();
    }
    
    protected static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            //problem.setNext(result.getNext());
            //result.setNext(problem);
            
            // [TODO] performance
            Problem p = result;
            while (p.getNext() != null)
                p = p.getNext();
            p.setNext(problem);
            return result;
        }
    }
}
