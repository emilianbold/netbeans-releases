/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Context;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.visualweb.insync.faces.refactoring.FacesRefactoringsPluginFactory.DelegatedRefactoring;

/**
 * This a base class for Faces refactoring plugins.
 * 
 * @author 
 */
abstract class FacesRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {
    private AbstractRefactoring abstractRefactoring;
    private boolean requestCancelled;
    
    protected FacesRefactoringPlugin(AbstractRefactoring abstractRefactoring) {
        this.abstractRefactoring = abstractRefactoring;
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
        
    protected static boolean isDelegatedRefactoring(AbstractRefactoring abstractRefactoring) {
        return abstractRefactoring.getContext().lookup(DelegatedRefactoring.class) != null;
    }    
}
