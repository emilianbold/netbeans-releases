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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

import javax.jws.WebParam.Mode;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 */
public class WebParamHolder extends Rule<VariableElement> implements WebServiceAnnotations {
    
    public WebParamHolder() {
    }
    
    protected ErrorDescription[] apply(VariableElement subject, ProblemContext ctx) {
        AnnotationMirror paramAnn = Utilities.findAnnotation(subject, ANNOTATION_WEBPARAM);
        if(paramAnn!=null) {
            AnnotationValue val = Utilities.getAnnotationAttrValue(paramAnn, ANNOTATION_ATTRIBUTE_MODE);
            Mode value = null;
            if(val!=null) {
                try {
                    value = Mode.valueOf(val.getValue().toString());
                } catch (Exception e) {
                    // we dont need to worry as hints for invalid enum value kicks in.
                }
            }
            if((Mode.INOUT == value || Mode.OUT == value) && 
                    !"javax.xml.ws.Holder".equals(getVariableType(subject))) {
                String label = NbBundle.getMessage(WebParamHolder.class, "MSG_WebParam_HolderRequired");
                
                Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(),
                        subject, paramAnn, ANNOTATION_ATTRIBUTE_MODE);
                AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                        getTrees().getTree(subject, paramAnn);
                Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree, ANNOTATION_ATTRIBUTE_MODE);
                ctx.setElementToAnnotate(problemTree);
                ErrorDescription problem = createProblem(subject, ctx, label, fix);
                ctx.setElementToAnnotate(null);
                return new ErrorDescription[]{problem};
            }
        }
        return null;
    }
    
    protected boolean isApplicable(VariableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_WEBPARAM);
    }
    
    private static String getVariableType(VariableElement subject) {
        if(subject.asType() instanceof DeclaredType) {
            DeclaredType dt = (DeclaredType) subject.asType();
            if(dt.asElement() instanceof TypeElement)  {
                TypeElement elem = (TypeElement) dt.asElement();
                return elem.getQualifiedName().toString();
            }
        }
        return null;
    }
}
