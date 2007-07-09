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
package org.netbeans.modules.websvc.editor.hints.rules;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotation;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 */
public class HandlerChainAndSoapMessageHandlers extends Rule<TypeElement> implements WebServiceAnnotations {

    public HandlerChainAndSoapMessageHandlers() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        ArrayList<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        String label = NbBundle.getMessage(HandlerChainAndSoapMessageHandlers.class, "MSG_HandlerChain_SoapMessageHandlers_Exclusive");

        AnnotationMirror annEntityHC = Utilities.findAnnotation(subject,ANNOTATION_HANDLERCHAIN);
        Tree problemTreeHC = ctx.getCompilationInfo().getTrees().getTree(subject, annEntityHC);
        Fix removeHC = new RemoveAnnotation(ctx.getFileObject(),
                subject, annEntityHC);
        ctx.setElementToAnnotate(problemTreeHC);
        ErrorDescription problemHC = createProblem(subject, ctx, label, removeHC);
        ctx.setElementToAnnotate(null);

        AnnotationMirror annEntitySMH = Utilities.findAnnotation(subject,ANNOTATION_SOAPMESSAGEHANDLERS);
        Tree problemTreeSMH = ctx.getCompilationInfo().getTrees().getTree(subject, annEntitySMH);
        Fix removeSMH = new RemoveAnnotation(ctx.getFileObject(),
                subject, annEntitySMH);
        ctx.setElementToAnnotate(problemTreeSMH);
        ErrorDescription problemSMH = createProblem(subject, ctx, label, removeSMH);
        ctx.setElementToAnnotate(null);

        return new ErrorDescription[]{problemHC, problemSMH};
    }

    protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_HANDLERCHAIN) &&
                Utilities.hasAnnotation(subject, ANNOTATION_SOAPMESSAGEHANDLERS);
    }

}
