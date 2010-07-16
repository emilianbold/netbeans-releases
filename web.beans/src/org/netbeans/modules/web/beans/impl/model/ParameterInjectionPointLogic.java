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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.beans.impl.model;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.impl.model.results.DefinitionErrorResult;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
abstract class ParameterInjectionPointLogic extends FieldInjectionPointLogic {

    static final String DISPOSES_ANNOTATION = 
            "javax.enterprise.inject.Disposes";                     // NOI18N
    
    static final String OBSERVES_ANNOTATION = 
            "javax.enterprise.event.Observes";                      // NOI18N
    
    protected Result findParameterInjectable( VariableElement element , 
            DeclaredType parentType , WebBeansModelImplementation model) 
    {
        DeclaredType parent = parentType;
        if ( parent == null ){
            TypeElement type = 
                model.getHelper().getCompilationController().getElementUtilities().
                    enclosingTypeElement(element);
            boolean isDeclaredType = ( type.asType() instanceof DeclaredType );
            if ( isDeclaredType ){
                parent = (DeclaredType)type.asType();
            }
            if ( !isDeclaredType) {
                return new DefinitionErrorResult(element,  parentType, 
                        NbBundle.getMessage(WebBeansModelProviderImpl.class, 
                                "ERR_BadParent", element.getSimpleName(),
                                 type!= null? type.toString(): null));
            }
        }
        ExecutableElement parentMethod = (ExecutableElement)element.
            getEnclosingElement();
        ExecutableType methodType = (ExecutableType)model.getHelper().
            getCompilationController().getTypes().asMemberOf(parent, 
                    parentMethod );
        List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
        
        boolean isInjectionPoint = false;
        /*
         * Check if method has parameters as injection points.
         * F.e. disposer method has only one parameter with @Disposes annotation.
         * All other its parameters are injection points. 
         */
        List<? extends VariableElement> parameters = parentMethod.getParameters();
        int index =0;
        for (int i=0; i<parameters.size() ; i++ ) {
            VariableElement variableElement = parameters.get(i);
            if ( variableElement.equals( element )){
                index = i;
            }
            else if ( AnnotationObjectProvider.hasAnnotation(variableElement,
                    DISPOSES_ANNOTATION, model.getHelper()) ||
                    AnnotationObjectProvider.hasAnnotation(variableElement,
                            OBSERVES_ANNOTATION, model.getHelper()) )
            {
                isInjectionPoint = true;
            }
        }
        TypeMirror elementType = parameterTypes.get(index);
        
        Result result = null;
        boolean disposes = AnnotationObjectProvider.hasAnnotation( element, 
                DISPOSES_ANNOTATION, model.getHelper());
        boolean observes = AnnotationObjectProvider.hasAnnotation( element, 
                OBSERVES_ANNOTATION, model.getHelper());
        if ( isInjectionPoint || AnnotationObjectProvider.hasAnnotation( parentMethod, 
                INJECT_ANNOTATION, model.getHelper()) ||
                AnnotationObjectProvider.hasAnnotation( parentMethod, 
                        PRODUCER_ANNOTATION, model.getHelper()) || disposes||
                        observes)
        {
            result = doFindVariableInjectable(element, elementType, 
                    model , false );
            isInjectionPoint = true;
        }
        if ( disposes ){
            if( result instanceof ResultImpl ){
                ((ResultImpl) result).getTypeElements().clear();
                Set<Element> productions = ((ResultImpl) result).getProductions();
                TypeElement enclosingTypeElement = model.getHelper().
                    getCompilationController().getElementUtilities().
                        enclosingTypeElement(element);
                for (Iterator<Element> iterator = productions.iterator(); 
                    iterator.hasNext(); ) 
                {
                    Element injectable = iterator.next();
                    if ( !(injectable instanceof ExecutableElement) ||
                            !model.getHelper().getCompilationController().
                            getElementUtilities().isMemberOf( injectable, 
                                    enclosingTypeElement))
                    {
                        iterator.remove();
                    }
                }
            }
            else {
                return result;
            }
        }

        if ( isInjectionPoint ){
            return getResult(result, model );
        }
        else {
            return new DefinitionErrorResult(element, elementType, 
                    NbBundle.getMessage( WebBeansModelProviderImpl.class, 
                            "ERR_NoInjectPoint" , element.getSimpleName()));
        }
    }
}
