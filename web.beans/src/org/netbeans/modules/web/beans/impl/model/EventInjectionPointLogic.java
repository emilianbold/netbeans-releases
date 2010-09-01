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
package org.netbeans.modules.web.beans.impl.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.beans.api.model.AbstractModelImplementation;


/**
 * @author ads
 *
 */
abstract class EventInjectionPointLogic extends ParameterInjectionPointLogic {
    
    public static final String EVENT_INTERFACE = 
        "javax.enterprise.event.Event";             // NOI18N


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getObservers(javax.lang.model.element.VariableElement, javax.lang.model.type.DeclaredType, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    @Override
    public List<ExecutableElement> getObservers( VariableElement element,
            DeclaredType parentType,
            AbstractModelImplementation modelImplementation )
    {
        WebBeansModelImplementation impl = WebBeansModelProviderImpl
                .getImplementation(modelImplementation);
        DeclaredType parent = parentType;
        try {
            parent = getParent(element, parentType, impl);
        }
        catch (DefinitionError e) {
            return null;
        }
        
        /*TypeMirror type = impl.getHelper().getCompilationController().
            getTypes().asMemberOf(parent, element );*/
        
        TypeMirror type = getParameterType(element, parent, 
                impl.getHelper().getCompilationController(), EVENT_INTERFACE);
        
        List<AnnotationMirror> qualifierAnnotations = new LinkedList<AnnotationMirror>();
        boolean hasAny = false;
        try {
            hasAny = hasAnyQualifier(element, impl, true, true ,qualifierAnnotations);
        }
        catch(InjectionPointDefinitionError e ){
            return null;
        }
        if ( hasAny ){
            hasAny = qualifierAnnotations.size()==0;
        }
        
        final List<ObserverTriple> methodObservesParameters = 
            findObservesParameters(impl.getHelper());
        
        Map<Element, TypeMirror> parameterTypesMap = 
                new HashMap<Element, TypeMirror>();
        for (ObserverTriple triple : methodObservesParameters ) {
            ExecutableElement method = triple.getFirst();
            VariableElement parameter = triple.getSecond();
            int index = triple.getThird();
            TypeElement typeElement = impl.getHelper().getCompilationController().
                getElementUtilities().enclosingTypeElement( method );
            TypeMirror typeMirror = typeElement.asType();
            if ( typeMirror instanceof DeclaredType ){
                ExecutableType methodType = (ExecutableType)impl.getHelper().
                    getCompilationController().getTypes().asMemberOf(
                            (DeclaredType)typeMirror, method );
                List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
                
                TypeMirror parameterType = parameterTypes.get( index );
                parameterTypesMap.put(parameter, parameterType);
            }
            
        }
        if ( !hasAny ){
            Set<Element> elements = parameterTypesMap.keySet();
            filterQualifiers( qualifierAnnotations , elements, 
                    impl.getHelper().getCompilationController());
            filterBindingsByMembers(qualifierAnnotations, elements, impl, 
                    Element.class);
        }
        
        List<ExecutableElement> result = new ArrayList<ExecutableElement>( 
                parameterTypesMap.size());
        filterParametersByType( parameterTypesMap , element, type , impl );
        for( Element parameter : parameterTypesMap.keySet() ){
            Element method = parameter.getEnclosingElement();
            if ( method.getKind() == ElementKind.METHOD){
                result.add( (ExecutableElement)method);
            }
        }
        return result;
    }

    private void filterQualifiers( List<? extends AnnotationMirror> qualifierAnnotations,
            Set<Element> elements , CompilationController compilationController )
    {
        Set<String> requiredQalifiers = getAnnotationFqns(qualifierAnnotations);
        
        for (Iterator<Element>  iterator = elements.iterator(); iterator.hasNext(); ) {
            Element  element = iterator.next();
            List<? extends AnnotationMirror> annotationMirrors = 
                compilationController.getElements().getAllAnnotationMirrors( element );
            Set<String> availableAnnotations = getAnnotationFqns(annotationMirrors);
            if ( !availableAnnotations.containsAll( requiredQalifiers )){
                iterator.remove();
            }
        }
    }

    private Set<String> getAnnotationFqns( List<? extends AnnotationMirror> annotations )
    {
        Set<String> annotationFqns = new HashSet<String>();
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element annotationElement = annotationType.asElement();
            TypeElement annotation = (TypeElement) annotationElement;
            annotationFqns.add( annotation.getQualifiedName().toString());
        }
        return annotationFqns;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getEventInjectionPoints(javax.lang.model.element.ExecutableElement, javax.lang.model.type.DeclaredType, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    @Override
    public List<VariableElement> getEventInjectionPoints(
            ExecutableElement element, DeclaredType parentType,
            AbstractModelImplementation modelImplementation )
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void filterParametersByType(
            Map<Element, TypeMirror> parameterTypesMap,
            VariableElement element, TypeMirror type,
            WebBeansModelImplementation impl )
    {
        AssignabilityChecker checker = new AssignabilityChecker( true );
        for (Iterator<Entry<Element, TypeMirror>> iterator =
            parameterTypesMap.entrySet().iterator();iterator.hasNext() ; ) 
        {
            boolean assignable = false;
            Entry<Element, TypeMirror> entry = iterator.next();
            TypeMirror typeMirror = entry.getValue();
            
            Element typeElement = impl.getHelper().
                getCompilationController().getTypes().asElement(type);
        
            boolean isGeneric = (typeElement instanceof TypeElement) &&
                ((TypeElement)typeElement).getTypeParameters().size() != 0;
            
            if ( !isGeneric && impl.getHelper().getCompilationController().
                    getTypes().isAssignable( type, typeMirror))
            {
                assignable = true;
            }
            
            
            if ( type instanceof ReferenceType && 
                    typeMirror instanceof ReferenceType)
            {
                checker.init((ReferenceType)typeMirror,  (ReferenceType)type, impl);
                assignable = checker.check();
            }
            if ( !assignable ){
                iterator.remove();
            }
        }
    }

    /*
     * Unfortunately annotation scanner ( getHelper().getAnnotationScanner() )
     * cannot be used for finding annotated method parameters. It doesn't
     * work for them. So this method performs find usages of @Observes annotation
     * and chooses appropriate elements.   
     */
    private List<ObserverTriple> findObservesParameters(AnnotationModelHelper helper )
    {
        List<ObserverTriple> result = new LinkedList<ObserverTriple>();
        CompilationController compilationController = 
            helper.getCompilationController();
        TypeElement observesType = compilationController.getElements().getTypeElement(
                OBSERVES_ANNOTATION);
        ElementHandle<TypeElement> observesHandle = ElementHandle.create(observesType);
        final Set<ElementHandle<TypeElement>> elementHandles = compilationController.
            getClasspathInfo().getClassIndex().getElements(
                    observesHandle,
                EnumSet.of(SearchKind.TYPE_REFERENCES),
                EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            TypeElement resolvedType = elementHandle.resolve( compilationController);
            
            List<? extends Element> enclosedElements = resolvedType.
                getEnclosedElements();
            List<ExecutableElement> methods = ElementFilter.methodsIn( 
                    enclosedElements);
            for (ExecutableElement method : methods) {
                List<? extends VariableElement> parameters = method.getParameters();
                int index = 0;
                for (VariableElement parameter : parameters) {
                    List<? extends AnnotationMirror> annotationMirrors = 
                        compilationController.getElements().
                        getAllAnnotationMirrors( parameter);
                    if ( helper.hasAnnotation( annotationMirrors, 
                            OBSERVES_ANNOTATION) ){
                        result.add( new ObserverTriple( method, parameter, index)  );
                    }
                    index++;
                }
            }
        }
        return result;
    }
    
    private class ObserverTriple extends Triple<ExecutableElement, VariableElement, Integer>{
        
        ObserverTriple( ExecutableElement method, VariableElement parameter, 
                Integer index )
        {
            super( method , parameter , index );
        }
    }
    
    private static class Triple<T,R,S> {
        Triple( T t , R r , S s){
            myFirst = t;
            mySecond = r;
            myThird = s;
        }
        
        T getFirst(){
            return myFirst;
        }
        
        R getSecond(){
            return mySecond;
        }
        
        S getThird(){
            return myThird;
        }
        
        private T myFirst;
        private R mySecond;
        private S myThird;
    }
    
}
