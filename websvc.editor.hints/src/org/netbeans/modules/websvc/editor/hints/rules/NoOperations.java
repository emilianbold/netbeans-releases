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
package org.netbeans.modules.websvc.editor.hints.rules;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.fixes.AddWSOpeartion;

/**
 *
 * @author Ajit.Bhate@sun.com
 */
public class NoOperations extends AbstractWebServiceRule {

    public NoOperations() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        if (!hasWebMethods(subject)) {
            String label = NbBundle.getMessage(NoOperations.class, "MSG_AddOperation");
            Fix addOperFix = new AddWSOpeartion(ctx.getFileObject());
            ErrorDescription problem = createProblem(subject, ctx, label, addOperFix);
            return new ErrorDescription[]{problem};
        }
        return null;
    }

    private boolean hasWebMethods(TypeElement classElement) {
        for (ExecutableElement method:ElementFilter.methodsIn(classElement.getEnclosedElements())) {
            if (method.getModifiers().contains(Modifier.PUBLIC)) {
                return true;
            }
        }
        return false;
    }

}
