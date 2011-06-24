/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.beans.analysis.analyzer.method;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResultKind;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InjectionPointParameterAnalyzer 
   extends AbstractDecoratorAnalyzer<ExecutableElement> implements MethodAnalyzer 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, WebBeansModel model,
            List<ErrorDescription> descriptions, CompilationInfo info , 
            AtomicBoolean cancel )
    {
        for (VariableElement var : element.getParameters()) {
            if (cancel.get()) {
                return;
            }
            try {
                if (model.isInjectionPoint(var)) {
                    if (!model.isDynamicInjectionPoint(var)) {
                        DependencyInjectionResult result = model.lookupInjectables(
                            var, (DeclaredType) parent.asType());
                        checkResult(result, element, var, model,descriptions, info );
                        if ( AnnotationUtil.isDelegate(var, parent, model)){
                            analyzeDecoratedBeans(result, var, element, parent,
                                    model, info, descriptions);
                        }
                    }
                    if ( cancel.get()){
                        return;
                    }
                    checkName(element, var, model,descriptions, info );
                    if ( cancel.get()){
                        return;
                    }
                    checkInjectionPointMetadata( var, element, parent , model , 
                            descriptions, info , cancel );
            }
            }
            catch( InjectionPointDefinitionError e ){
                informInjectionPointDefError(e, element, model, descriptions, info );
            }
        }

    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer#addClassError(javax.lang.model.element.VariableElement, java.lang.Object, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    protected void addClassError( VariableElement element, ExecutableElement method,
            TypeElement decoratedBean, WebBeansModel model,
            CompilationInfo info, List<ErrorDescription> descriptions )
    {
        ErrorDescription description = CdiEditorAnalysisFactory.
            createError( element , method, model, info , 
                    NbBundle.getMessage(InjectionPointParameterAnalyzer.class, 
                            "ERR_FinalDecoratedBean",                   // NOI18N
                            decoratedBean.getQualifiedName().toString()));
        descriptions.add( description );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer#addMethodError(javax.lang.model.element.VariableElement, java.lang.Object, javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    protected void addMethodError( VariableElement element,
            ExecutableElement method, TypeElement decoratedBean,
            Element decoratedMethod, WebBeansModel model, CompilationInfo info,
            List<ErrorDescription> descriptions )
    {
        ErrorDescription description = CdiEditorAnalysisFactory.createError(
                element, method, model, info, NbBundle.getMessage(
                        InjectionPointParameterAnalyzer.class,
                        "ERR_FinalMethodDecoratedBean", // NOI18N
                        decoratedBean.getQualifiedName().toString(),
                        decoratedMethod.getSimpleName().toString()));
        descriptions.add(description);
    }
    
    private void checkInjectionPointMetadata( VariableElement var,
            ExecutableElement method, TypeElement parent, WebBeansModel model,
            List<ErrorDescription> descriptions, CompilationInfo info,
            AtomicBoolean cancel )
    {
        TypeElement injectionPointType = model.getCompilationController()
                .getElements().getTypeElement(AnnotationUtil.INJECTION_POINT);
        if (injectionPointType == null) {
            return;
        }
        Element varElement = model.getCompilationController().getTypes()
                .asElement(var.asType());
        if (!injectionPointType.equals(varElement)) {
            return;
        }
        if (cancel.get()) {
            return;
        }
        List<AnnotationMirror> qualifiers = model.getQualifiers(varElement,
                true);
        AnnotationHelper helper = new AnnotationHelper(
                model.getCompilationController());
        Map<String, ? extends AnnotationMirror> qualifiersFqns = helper
                .getAnnotationsByType(qualifiers);
        boolean hasDefault = model.hasImplicitDefaultQualifier(varElement);
        if (!hasDefault
                && qualifiersFqns.keySet().contains(AnnotationUtil.DEFAULT_FQN))
        {
            hasDefault = true;
        }
        if (!hasDefault || cancel.get()) {
            return;
        }
        try {
            String scope = model.getScope(parent);
            if (scope != null && !AnnotationUtil.DEPENDENT.equals(scope)) {
                ErrorDescription description = CdiEditorAnalysisFactory
                        .createError(var, method, model, info,
                                "ERR_WrongQualifierInjectionPointMeta"); // NOI18N
                if ( description != null ){
                    descriptions.add(description);
                }
            }
        }
        catch (CdiException e) {
            // this exception will be handled in the appropriate scope analyzer
            return;
        }
    }

    private void checkName( ExecutableElement element, VariableElement var,
            WebBeansModel model, List<ErrorDescription> descriptions , 
            CompilationInfo info )
    {
        AnnotationMirror annotation = AnnotationUtil.getAnnotationMirror( 
                var , AnnotationUtil.NAMED, model.getCompilationController());
        if ( annotation!= null && annotation.getElementValues().size() == 0 ){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError(var, element,  model, info , 
                        NbBundle.getMessage( InjectionPointParameterAnalyzer.class, 
                                "ERR_ParameterNamedInjectionPoint"));        // NOI18N
            if ( description != null ){
                descriptions.add( description );
            }
        }
    }

    private void checkResult( DependencyInjectionResult result ,
            ExecutableElement method , VariableElement element, WebBeansModel model,
            List<ErrorDescription> descriptions, CompilationInfo info )
    {
        if ( result instanceof DependencyInjectionResult.Error ){
            ResultKind kind = result.getKind();
            Severity severity = Severity.WARNING;
            if ( kind == DependencyInjectionResult.ResultKind.DEFINITION_ERROR){
                severity = Severity.ERROR;
            }
            String message = ((DependencyInjectionResult.Error)result).getMessage();
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification(severity, element , method , 
                        model, info ,  message);
            if ( description != null ){
                descriptions.add( description );
            }
        }
    }

    private void informInjectionPointDefError(InjectionPointDefinitionError exception , 
            Element element, WebBeansModel model, 
            List<ErrorDescription> descriptions, CompilationInfo info )
    {
        ErrorDescription description = CdiEditorAnalysisFactory.createError(
                element, model, info , exception.getMessage());
        if ( description != null ){
            descriptions.add(description);
        }
    }

}
