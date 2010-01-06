/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.modules.web.beans.api.model.AbstractModelImplementation;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=WebBeansModelProvider.class)
public class WebBeansModelProviderImpl extends ParameterInjectionPointLogic 
    implements WebBeansModelProvider 
{

    public Result getInjectable(VariableElement element, DeclaredType parentType, 
            AbstractModelImplementation impl) 
    {
        WebBeansModelImplementation modelImpl = getImplementation(impl);
        if ( modelImpl == null ){
            return null;
        }
        /* 
         * Element could be injection point. One need first if all to check this.  
         */
        Element parent = element.getEnclosingElement();
        
        if ( parent instanceof TypeElement){
            return findVariableInjectable(element, parentType , modelImpl);
        }
        else if ( parent instanceof ExecutableElement ){
            // Probably injected field in method. One need to check method.
            /*
             * There are two cases where parameter is injected :
             * 1) Method has some annotation which require from 
             * parameters to be injection points.
             * 2) Method is disposer method. In this case injectable
             * is producer corresponding method.
             */
            return findParameterInjectable(element, parentType, modelImpl );
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#lookupInjectables(javax.lang.model.element.VariableElement, javax.lang.model.type.DeclaredType, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    public Result lookupInjectables( VariableElement element,
            DeclaredType parentType, AbstractModelImplementation modelImpl )
    {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#isDynamicInjectionPoint(javax.lang.model.element.VariableElement)
     */
    public boolean isDynamicInjectionPoint( VariableElement element , 
            AbstractModelImplementation impl) 
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#isInjectionPoint(javax.lang.model.element.VariableElement)
     */
    public boolean isInjectionPoint( VariableElement element , 
            AbstractModelImplementation modelImpl)  throws InjectionPointDefinitionError
    {
        WebBeansModelImplementation impl = getImplementation( modelImpl);
        if ( impl == null ){
            return false;
        }
        
        Element parent = element.getEnclosingElement();
        
        if ( parent instanceof TypeElement){
            List<? extends AnnotationMirror> annotations = 
                impl.getHelper().getCompilationController().getElements().
                getAllAnnotationMirrors(element);
            return impl.getHelper().hasAnnotation(annotations, INJECT_ANNOTATION);
        }
        else if ( parent instanceof ExecutableElement ){
            return isMethodParameterInjection(element, impl, 
                    (ExecutableElement)parent);
        }
        return false;
    }

    private boolean isMethodParameterInjection( VariableElement element,
            WebBeansModelImplementation impl, ExecutableElement parent )
            throws InjectionPointDefinitionError
    {
        List<? extends AnnotationMirror> annotations = 
            impl.getHelper().getCompilationController().getElements().
            getAllAnnotationMirrors(parent);
        if (isDisposeParameter( element, parent, annotations,  impl))
        {
            return true;
        }
        if ( isObservesParameter(element, parent, annotations, impl)){
            return true;
        }
        return impl.getHelper().hasAnnotation(annotations, INJECT_ANNOTATION)||
            impl.getHelper().hasAnnotation(annotations, PRODUCER_ANNOTATION);
    }

    public List<AnnotationMirror> getQualifiers(Element element, 
            AbstractModelImplementation modelImpl) 
    {
        WebBeansModelImplementation impl = getImplementation( modelImpl);
        if ( impl == null ){
            return Collections.emptyList();
        }
        List<AnnotationMirror> result = new LinkedList<AnnotationMirror>();
        List<? extends AnnotationMirror> annotations = impl.getHelper().
            getCompilationController().getElements().getAllAnnotationMirrors( 
                    element);
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( isQualifier( annotationElement , impl.getHelper()) ){
                result.add( annotationMirror );
            }
        }
        return result;
    }

    private static WebBeansModelImplementation getImplementation(
            AbstractModelImplementation impl )
    {
        WebBeansModelImplementation modelImpl = null;
        try {
            modelImpl = (WebBeansModelImplementation) impl;
        }
        catch (ClassCastException e) {
            return null;
        }
        return modelImpl;
    }
    
    /*
     * Observer method could have only one parameter.
     * Other parameters are error for observer method.
     * They are not injection points.
     */
    private boolean isObservesParameter( VariableElement element,
            ExecutableElement method , List<? extends AnnotationMirror> annotations, 
            AbstractModelImplementation modelImpl ) throws InjectionPointDefinitionError
    {
        List<? extends VariableElement> parameters = method.getParameters();
        boolean observesFound = false;
        for (VariableElement variableElement : parameters) {
            if (  AnnotationObjectProvider.hasAnnotation(variableElement, 
                    OBSERVES_ANNOTATION, getImplementation(modelImpl).getHelper()))
            {
                if ( observesFound ){
                    throw new InjectionPointDefinitionError(method, 
                            NbBundle.getMessage(WebBeansModelImplementation.class, 
                                    "ERR_MultipleObserves" , method.getSimpleName()));
                }
                observesFound = true;
            }
        }
        if ( !observesFound ){
            return false;
        }
        
        String badAnnotation = checkInjectProducers(annotations, modelImpl);
        if ( badAnnotation != null ){
            throw new InjectionPointDefinitionError( method, 
                    NbBundle.getMessage(WebBeansModelImplementation.class, 
                            "ERR_ObserverHasInjectOrProduces" , method.getSimpleName(),
                            badAnnotation ));
        }
        return observesFound;
    }

    /*
     * All parameters of disposer method are injection points.
     */
    private boolean isDisposeParameter( VariableElement element,
            ExecutableElement method , List<? extends AnnotationMirror> annotations, 
            AbstractModelImplementation modelImpl ) throws InjectionPointDefinitionError
    {
        List<? extends VariableElement> parameters = method.getParameters();
        boolean disposeFound = false;
        boolean observesFound = false;
        for (VariableElement variableElement : parameters) {
            if (  AnnotationObjectProvider.hasAnnotation(variableElement, 
                    DISPOSES_ANNOTATION, getImplementation(modelImpl).getHelper()))
            {
                if ( disposeFound ){
                    throw new InjectionPointDefinitionError(method, 
                            NbBundle.getMessage(WebBeansModelImplementation.class, 
                                    "ERR_MultipleDisposes" , method.getSimpleName()));
                }
                disposeFound = true;
            }
            if (  AnnotationObjectProvider.hasAnnotation(variableElement, 
                    OBSERVES_ANNOTATION, getImplementation(modelImpl).getHelper()))
            {
                observesFound = true;
            }
        }
        if ( !disposeFound ){
            return false;
        }
        if ( observesFound ){
            throw new InjectionPointDefinitionError(method, 
                    NbBundle.getMessage(WebBeansModelImplementation.class, 
                            "ERR_DisposesHasObserves" , method.getSimpleName()));
        }
        String badAnnotation = checkInjectProducers(annotations, modelImpl);
        if ( badAnnotation != null ){
            throw new InjectionPointDefinitionError( method, 
                    NbBundle.getMessage(WebBeansModelImplementation.class, 
                            "ERR_DisposesHasInjectOrProduces" , method.getSimpleName(),
                            badAnnotation ));
        }
        return disposeFound;
    }
    
    private String checkInjectProducers(List<? extends AnnotationMirror> annotations, 
            AbstractModelImplementation impl ) 
    {
        if (getImplementation(impl).getHelper().hasAnnotation(annotations, 
                INJECT_ANNOTATION))
        {
            return INJECT_ANNOTATION;
        }
        if ( getImplementation(impl).getHelper().hasAnnotation(annotations, 
                        PRODUCER_ANNOTATION))
        {
            return PRODUCER_ANNOTATION; 
        }
        return null;
    }

}
