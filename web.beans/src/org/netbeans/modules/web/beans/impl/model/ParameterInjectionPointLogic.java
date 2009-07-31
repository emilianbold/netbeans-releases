/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import org.netbeans.modules.web.beans.api.model.WebBeansModelException;


/**
 * @author ads
 *
 */
abstract class ParameterInjectionPointLogic extends FieldInjectionPointLogic {

    private static final String INITIALIZER_ANNOTATION = 
            "javax.enterprise.inject.Initializer";                  // NOI18N
    
    private static final String DISPOSES_ANNOTATION = 
            "javax.enterprise.inject.Disposes";                     // NOI18N
    
    private static final String OBSERVES_ANNOTATION = 
            "javax.enterprise.event.Observes";                      // NOI18N

    protected Element findParameterInjectable( VariableElement element , 
            WebBeansModelImplementation model) throws WebBeansModelException 
    {
        ExecutableElement parent = (ExecutableElement)element.getEnclosingElement();
        Set<Element> injectables = null;
        boolean disposes = AnnotationObjectProvider.hasAnnotation( element, 
                DISPOSES_ANNOTATION, model.getHelper());
        if ( AnnotationObjectProvider.hasAnnotation( parent, 
                INITIALIZER_ANNOTATION, model.getHelper()) ||
                AnnotationObjectProvider.hasAnnotation( parent, 
                        PRODUCER_ANNOTATION, model.getHelper()) || disposes )
        {
            injectables = findVariableInjectable(element, model , true );
        }
        if ( disposes ){
            if( injectables!= null && injectables.size() >0 ){
                TypeElement enclosingTypeElement = model.getHelper().
                    getCompilationController().getElementUtilities().
                        enclosingTypeElement(element);
                for (Iterator<Element> iterator = injectables.iterator(); 
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
                return null;
            }
        }
        if ( AnnotationObjectProvider.hasAnnotation( element, 
                                    OBSERVES_ANNOTATION, model.getHelper()))
        {
            /*
             * From the spec : if the event parameter does not explicitly 
             * declare any binding, the observer method observes events with no binding.
             * 
             * TODO : if there is no binding type for parameter then
             * one needs to find elements without bindings ( they 
             * should not have any binding at all even explicit @Current
             * binding ).   
             * This is just implementor elements ( filtered by binding absence ). 
             */
            injectables = findVariableInjectable(element, model , false );
        }
        boolean isInjectionPoint = false;
        List<? extends VariableElement> parameters = parent.getParameters();
        /*
         * Check if method has parameters as injection points.
         * Parameter should differs from lead parameter ( parameter that define 
         * method with special meaning ) .
         */
        for (VariableElement variableElement : parameters) {
            if ( !variableElement.equals( element ) && 
                    AnnotationObjectProvider.hasAnnotation(variableElement, 
                    DISPOSES_ANNOTATION, model.getHelper()) ||
                    AnnotationObjectProvider.hasAnnotation(variableElement, 
                            OBSERVES_ANNOTATION, model.getHelper()) )
            {
                isInjectionPoint = true;
                break;
            }
        }
        if ( isInjectionPoint ){
            injectables = findVariableInjectable(element, model , true );
        }
        
        if ( injectables == null ){
            return null;
        }
        return getResult(injectables, model );
    }
}
