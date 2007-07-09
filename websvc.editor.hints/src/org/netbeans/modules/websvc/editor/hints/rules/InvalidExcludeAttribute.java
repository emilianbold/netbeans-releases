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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public class InvalidExcludeAttribute extends Rule<ExecutableElement> implements WebServiceAnnotations {

    public InvalidExcludeAttribute() {
    }

    protected ErrorDescription[] apply(ExecutableElement subject, ProblemContext ctx) {
        AnnotationMirror methodAnn = Utilities.findAnnotation(subject, ANNOTATION_WEBMETHOD);
        AnnotationValue val = Utilities.getAnnotationAttrValue
                (methodAnn, ANNOTATION_ATTRIBUTE_EXCLUDE);
        if (val != null && val.getValue() == Boolean.TRUE) {
            String label = NbBundle.getMessage(InvalidExcludeAttribute.class, 
                    "MSG_WebMethod_ExcludeNotAllowed");
            Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(), 
                    subject, methodAnn, ANNOTATION_ATTRIBUTE_EXCLUDE);
            AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                        getTrees().getTree(subject, methodAnn);
            Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree,
                    ANNOTATION_ATTRIBUTE_EXCLUDE);
            ctx.setElementToAnnotate(problemTree);
            ErrorDescription problem = createProblem(subject, ctx, label, fix);
            ctx.setElementToAnnotate(null);
            return new ErrorDescription[]{problem};
        }
        return null;
    }

    protected boolean isApplicable(ExecutableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_WEBMETHOD);
    }
}
