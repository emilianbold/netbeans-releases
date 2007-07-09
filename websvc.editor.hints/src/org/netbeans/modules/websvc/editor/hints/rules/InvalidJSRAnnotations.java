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

import java.util.ArrayList;

import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotation;

/**
 *
 * @author Ajit.Bhate@sun.com
 */
public class InvalidJSRAnnotations extends AbstractWebServiceRule {

    public InvalidJSRAnnotations() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        ArrayList<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_WEBSERVICE);
        if (subject.getKind() == ElementKind.CLASS && Utilities.getAnnotationAttrValue
                (annEntity, ANNOTATION_ATTRIBUTE_SEI) != null) {
            for (String aName : new String[]{
                ANNOTATION_WEBMETHOD, 
                ANNOTATION_WEBPARAM, 
                ANNOTATION_WEBRESULT, 
                ANNOTATION_ONEWAY, 
                ANNOTATION_SOAPMESSAGEHANDLERS, 
                ANNOTATION_INITPARAM, 
                ANNOTATION_SOAPBINDING, 
                ANNOTATION_SOAPMESSAGEHANDLER}) {
                AnnotationMirror wrongAnnon = Utilities.findAnnotation(subject, aName);
                if (wrongAnnon != null) {
                    String label = NbBundle.getMessage(InvalidJSRAnnotations.class, "MSG_Invalid_JSR181Annotation");
                    Tree problemTree = ctx.getCompilationInfo().getTrees().getTree(subject, wrongAnnon);
                    Fix removeHC = new RemoveAnnotation(ctx.getFileObject(), subject, wrongAnnon);
                    ctx.setElementToAnnotate(problemTree);
                    errors.add(createProblem(subject, ctx, label, removeHC));
                    ctx.setElementToAnnotate(null);
                }
            }
        }
        return errors.isEmpty() ? null : errors.toArray(new ErrorDescription[errors.size()]);
    }
}
