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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 * @author Ajit.Bhate@Sun.COM
 */
public class InterfaceEndpointInterface  extends AbstractWebServiceRule {
    
    /** Creates a new instance of InterfaceEndpointInterface */
    public InterfaceEndpointInterface() {
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        if(subject.getKind() == ElementKind.INTERFACE) {
            AnnotationMirror annEntity = Utilities.findAnnotation(subject,ANNOTATION_WEBSERVICE);
            AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                    getTrees().getTree(subject, annEntity);
            //endpointInterface not allowed
            if(Utilities.getAnnotationAttrValue(annEntity, ANNOTATION_ATTRIBUTE_SEI)!=null) {
                String label = NbBundle.getMessage(InterfaceEndpointInterface.class, "MSG_IF_SEINotAllowed");
                Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(),
                        subject, annEntity, ANNOTATION_ATTRIBUTE_SEI);
                Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree, ANNOTATION_ATTRIBUTE_SEI);
                ctx.setElementToAnnotate(problemTree);
                ErrorDescription problem = createProblem(subject, ctx, label, fix);
                ctx.setElementToAnnotate(null);
                return new ErrorDescription[]{problem};
            }
        }        
        return null;
    }
}
