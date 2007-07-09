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

import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Use;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.SetAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public class RPCStyleWrappedParameterStyle extends AbstractWebServiceRule {

    public RPCStyleWrappedParameterStyle() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_SOAPBINDING);
        if (annEntity != null) {
            AnnotationValue styleVal = Utilities.getAnnotationAttrValue(annEntity, 
                    ANNOTATION_ATTRIBUTE_STYLE);
            Style style = styleVal==null?null:Style.valueOf(styleVal.getValue().toString());
            AnnotationValue useVal = Utilities.getAnnotationAttrValue(annEntity, 
                    ANNOTATION_ATTRIBUTE_USE);
            Use use = useVal==null?null:Use.valueOf(useVal.getValue().toString());
            AnnotationValue paramStyleVal = Utilities.getAnnotationAttrValue
                    (annEntity, ANNOTATION_ATTRIBUTE_PARAMETERSTYLE);
            ParameterStyle paramStyle = paramStyleVal==null?null:ParameterStyle.valueOf(paramStyleVal.getValue().toString());
            if (style == Style.RPC && 
                    use == Use.LITERAL && 
                    paramStyle != ParameterStyle.WRAPPED) {
                String label = NbBundle.getMessage(RPCStyleWrappedParameterStyle.class, 
                        "MSG_RPCStyle_ParameterStyleWrapped");
                Fix fix = new SetAnnotationArgument(ctx.getFileObject(), subject, 
                        annEntity, ANNOTATION_ATTRIBUTE_PARAMETERSTYLE, ParameterStyle.WRAPPED);
                AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                        getTrees().getTree(subject, annEntity);
                Tree problemTree = paramStyle!=null? Utilities.getAnnotationArgumentTree(
                        annotationTree, ANNOTATION_ATTRIBUTE_PARAMETERSTYLE):
                    ctx.getCompilationInfo().getTrees().getTree(subject, annEntity);
                ctx.setElementToAnnotate(problemTree);
                ErrorDescription problem = createProblem(subject, ctx, label, fix);
                ctx.setElementToAnnotate(null);
                return new ErrorDescription[]{problem};
            }
        }
        return null;
    }
}
