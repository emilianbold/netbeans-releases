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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring;

import java.util.List;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;

/**
 * A refactoring plugin for web refactorings.
 * 
 * @author Erno Mononen
 */
public class WebRefactoringPlugin implements RefactoringPlugin{

    private final List<WebRefactoring> refactorings;

    public WebRefactoringPlugin(List<WebRefactoring> refactorings) {
        this.refactorings = refactorings;
    }

    public Problem preCheck() {
        Problem result = null;
        for (WebRefactoring each : refactorings){
            result = RefactoringUtil.addToEnd(each.preCheck(), result);
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
        return;
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Problem result = null;
        for (WebRefactoring each : refactorings){
            result = RefactoringUtil.addToEnd(each.prepare(refactoringElements), result);
        }
        return result;
    }
}
