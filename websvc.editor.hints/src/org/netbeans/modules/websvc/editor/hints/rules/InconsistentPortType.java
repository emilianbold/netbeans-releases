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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public class InconsistentPortType extends AbstractWebServiceRule {

    public InconsistentPortType() {
    }

    protected ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        AnnotationMirror annEntity = Utilities.findAnnotation(subject, ANNOTATION_WEBSERVICE);
        if (subject.getKind() == ElementKind.CLASS && Utilities.getAnnotationAttrValue(annEntity, ANNOTATION_ATTRIBUTE_SEI) == null) {
            Service service = ctx.getLookup().lookup(Service.class);
            WSDLModel model = ctx.getLookup().lookup(WSDLModel.class);
            if (service != null && model != null && model.getState() == State.VALID) {
                PortType portType = model.findComponentByName(subject.getSimpleName().toString(), PortType.class);
                if (portType == null) {
                    AnnotationValue nameVal = Utilities.getAnnotationAttrValue(annEntity, ANNOTATION_ATTRIBUTE_NAME);
                    if(nameVal!=null)
                        portType = model.findComponentByName(nameVal.toString(), PortType.class);
                }
                if (portType == null) {
                    String label = NbBundle.getMessage(InconsistentPortType.class, "MSG_InconsistentPortType");
                    AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                    getTrees().getTree(subject, annEntity);
                    Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree, ANNOTATION_ATTRIBUTE_WSDLLOCATION);
                    ctx.setElementToAnnotate(problemTree);
                    ErrorDescription problem = createProblem(subject, ctx, label, (Fix) null);
                    ctx.setElementToAnnotate(null);
                    return new ErrorDescription[]{problem};
                }
            }
        }
        return null;
    }
}
