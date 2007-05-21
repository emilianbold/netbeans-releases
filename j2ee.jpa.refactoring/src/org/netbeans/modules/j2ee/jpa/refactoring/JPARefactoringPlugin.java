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


package org.netbeans.modules.j2ee.jpa.refactoring;

import java.util.List;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.util.Parameters;

/**
 * Wraps a list of <code>JPARefactoring</code>s and provides 
 * the <code>RefactoringPlugin</code> interface for them.
 * 
 * @author Erno Mononen
 */
public class JPARefactoringPlugin implements RefactoringPlugin{
    
    private final List<JPARefactoring> refactorings;
    
    /** Creates a new instance of JPARefactoringPlugin */
    public JPARefactoringPlugin(List<JPARefactoring> refactorings) {
        Parameters.notNull("refactorings", refactorings); //NO18N
        this.refactorings = refactorings;
    }

    public Problem preCheck() {
        Problem result = null;
        for (JPARefactoring each : refactorings){
            RefactoringUtil.addToEnd(each.preCheck(), result);
        }
        return result;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Problem result = null;
        for (JPARefactoring each : refactorings){
            RefactoringUtil.addToEnd(each.prepare(refactoringElements), result);
        }
        return result;
    }
}
