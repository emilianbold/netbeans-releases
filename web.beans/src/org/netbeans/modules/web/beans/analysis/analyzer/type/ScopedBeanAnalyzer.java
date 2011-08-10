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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.web.beans.analysis.analyzer.AbstractScopedAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ScopedBeanAnalyzer extends AbstractScopedAnalyzer 
    implements ClassAnalyzer 
{
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel,
            Result result )
    {
        analyzeScope(element, model, cancel , result );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractScopedAnalyzer#checkScope(javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    protected void checkScope( TypeElement scopeElement, Element element,
            WebBeansModel model, AtomicBoolean cancel , Result result )
    {
        if ( cancel.get() ){
            return;
        }
        checkProxiability(scopeElement, element, model, result );
        if ( cancel.get() ){
            return;
        }
        checkPublicField(scopeElement , element , model , result  );
        if ( cancel.get() ){
            return;
        }
        checkParameterizedBean(scopeElement , element , model , result );
    }

    private void checkParameterizedBean( TypeElement scopeElement,
            Element element, WebBeansModel model,
            Result result )
    {
        if ( AnnotationUtil.DEPENDENT.contentEquals( 
                scopeElement.getQualifiedName()))
        {
            return;
        }  
        TypeMirror type = element.asType();
        if ( type instanceof DeclaredType ){
            List<? extends TypeMirror> typeArguments = ((DeclaredType)type).getTypeArguments();
            if ( typeArguments.size() != 0 ){
                result.addError(element, model,  
                    NbBundle.getMessage(ScopedBeanAnalyzer.class,
                        "ERR_IncorrectScopeForParameterizedBean" ));    // NOI18N
            }
        }
    }

    private void checkPublicField( TypeElement scopeElement, Element element,
            WebBeansModel model, Result result )
    {
        if ( AnnotationUtil.DEPENDENT.contentEquals( 
                scopeElement.getQualifiedName()))
        {
            return;
        }
        List<VariableElement> fields = ElementFilter.fieldsIn( 
                element.getEnclosedElements());
        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if ( modifiers.contains(Modifier.PUBLIC )){
                result.addError(element, model ,  
                        NbBundle.getMessage(ScopedBeanAnalyzer.class,
                            "ERR_IcorrectScopeWithPublicField", 
                            field.getSimpleName().toString()));
                return;
            }
        }
    }

    private void checkProxiability( TypeElement scopeElement, Element element,
            WebBeansModel model, Result result )
    {
        boolean isNormal = AnnotationUtil.hasAnnotation(scopeElement, 
                AnnotationUtil.NORMAL_SCOPE_FQN, model.getCompilationController());
        if ( isNormal ){
            checkFinal( element , model, result );
        }
    }

    private void checkFinal( Element element, WebBeansModel model,
            Result result )
    {
        if ( !( element instanceof TypeElement )){
            return;
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains( Modifier.FINAL) ){
            result.addError( element, model,  
                    NbBundle.getMessage(ScopedBeanAnalyzer.class, 
                            "ERR_FinalScopedClass"));
            return;
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                element.getEnclosedElements());
        for (ExecutableElement method : methods) {
            modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.FINAL)) {
                result.addNotification( Severity.WARNING, method, model, 
                                NbBundle.getMessage(
                                ScopedBeanAnalyzer.class,
                                "WARN_FinalScopedClassMethod"));
            }
        }
    }


}
