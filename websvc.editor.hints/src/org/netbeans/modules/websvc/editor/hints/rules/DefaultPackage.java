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

import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.AddAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@sun.com
 */
public class DefaultPackage extends AbstractWebServiceRule {

    public DefaultPackage() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_WEBSERVICE);
        Element packageElement = ctx.getCompilationInfo().getElementUtilities().
                outermostTypeElement(subject).getEnclosingElement();
        if (packageElement instanceof PackageElement && ((PackageElement) 
                packageElement).isUnnamed() && Utilities.getAnnotationAttrValue
                (annEntity, ANNOTATION_ATTRIBUTE_TARGETNAMESPACE) == null) {
            String label = NbBundle.getMessage(DefaultPackage.class, "MSG_AddTargetNamespace");
            String tgtNamespace = "http://my.org/ns/";
            Tree problemTree = ctx.getCompilationInfo().getTrees().getTree(subject, annEntity);
            Fix removeHC = new AddAnnotationArgument(ctx.getFileObject(), 
                    subject, annEntity, ANNOTATION_ATTRIBUTE_TARGETNAMESPACE, tgtNamespace);
            ctx.setElementToAnnotate(problemTree);
            ErrorDescription problem = createProblem(subject, ctx, label, removeHC);
            ctx.setElementToAnnotate(null);
            return new ErrorDescription[]{problem};
        }
        return null;
    }
}
