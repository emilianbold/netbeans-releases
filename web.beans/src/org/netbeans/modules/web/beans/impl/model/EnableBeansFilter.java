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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.impl.model.results.ErrorImpl;
import org.netbeans.modules.web.beans.impl.model.results.InjectableResultImpl;
import org.netbeans.modules.web.beans.impl.model.results.ResolutionErrorImpl;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class EnableBeansFilter {
    
    EnableBeansFilter(ResultImpl result, WebBeansModelImplementation model )
    {
        myResult = result;
        myHelper = model.getHelper();
        myBeansModel = model.getBeansModel();
    }
    
    Result filter(){
        myAlternatives = new HashSet<Element>();
        myEnabledAlternatives = new HashSet<Element>();
        
        Set<TypeElement> typeElements = getResult().getTypeElements();
        for (TypeElement typeElement : typeElements) {
            if ( getResult().isAlternative(typeElement)){
                myAlternatives.add( typeElement );
                addEnabledAlternative( typeElement , typeElement);
            }
        }
        Set<Element> productions = getResult().getProductions();
        for (Element element : productions) {
            TypeElement enclosingTypeElement = myHelper.getCompilationController().
                getElementUtilities().enclosingTypeElement(element);
            if ( getResult().isAlternative(element)){
                myAlternatives.add( element );
                addEnabledAlternative( enclosingTypeElement , element );
            }
        }
        
        Set<Element> enabledTypeElements = new HashSet<Element>( typeElements );
        Set<Element> enabledProductions = new HashSet<Element>( productions );
        myAlternatives.removeAll(myEnabledAlternatives);
        // now myAlternative contains only disabled alternatives.
        enabledProductions.removeAll( myAlternatives );
        enabledTypeElements.removeAll( myAlternatives );
        
        int typesSize = enabledTypeElements.size();
        int productionsSize = enabledProductions.size();
        
        // filter enabled/disabled beans
        Set<Element> enabledTypes = findEnabledTypes( enabledTypeElements );
        findEnabledProductions( enabledProductions);
        int commonSize = enabledTypes.size() + enabledProductions.size();
        if ( commonSize == 1 ){
            Element injectable = enabledTypes.size() ==0 ? 
                    enabledProductions.iterator().next(): 
                        enabledTypes.iterator().next();
            enabledTypes.addAll( enabledProductions);
            return new InjectableResultImpl( getResult(), injectable, enabledTypes ); 
        }
        if ( commonSize ==0 ){
            if ( typeElements.size() == 0 && productions.size() == 0 ){
                return new ErrorImpl(getResult().getVariable(), 
                        getResult().getVariableType(), NbBundle.getMessage(
                                EnableBeansFilter.class, "ERR_NoFound"));
            }
            if ( typesSize==0 && productionsSize == 0 )
            {
                /* no elements was eliminated after check for "enabling" 
                 * ( by the spec ). So they are all alternatives that 
                 * was not turned on in beans.xml. 
                 */  
                return new ResolutionErrorImpl(getResult(), NbBundle.getMessage(
                        EnableBeansFilter.class, "ERR_AlternativesOnly"));
            }
            return new ResolutionErrorImpl( getResult(),  NbBundle.getMessage(
                    EnableBeansFilter.class, "ERR_NoEnabledBeans"));
        }
        Set<Element> allElements = new HashSet<Element>( enabledTypes );
        allElements.addAll( enabledProductions );
        allElements.retainAll( myEnabledAlternatives );
        boolean hasSingleAlternative = allElements.size() == 1;
        if ( hasSingleAlternative ){
            /*
             * Spec : When an ambiguous dependency exists, the container attempts 
             * to resolve the ambiguity:
             * - If any matching beans are alternatives, the container 
             * eliminates all matching beans that are not alternatives.
             * If there is exactly one bean remaining, the container will select 
             * this bean, and the ambiguous dependency is called resolvable.
             */
            enabledTypes.addAll( enabledProductions);
            return new InjectableResultImpl( getResult(), 
                    allElements.iterator().next(), enabledTypes );
        }
        
        enabledTypes.addAll( enabledProductions);
        String message =NbBundle.getMessage(EnableBeansFilter.class, 
                "ERR_UnresolvedAmbiguousDependency");               // NOI81N
        return new ResolutionErrorImpl( getResult() , message, enabledTypes);
    }
    
    private void findEnabledProductions(Set<Element> productions )
    {
        /*
         * This is partial implementation of the spec :
         * A bean is said to be enabled if:
         * - it is not a producer method or field of a disabled bean
         * Full check for enabled/disabled bean is very complicated.
         * Here is check only for enabled alternatives if any. 
         */
        for (Iterator<Element> iterator =  productions.iterator(); 
            iterator.hasNext(); ) 
        {
            Element element = iterator.next();
            TypeElement enclosingTypeElement = getHelper().
                getCompilationController().getElementUtilities().
                enclosingTypeElement(element);
            if ( getResult().isAlternative(enclosingTypeElement)){
                String name = enclosingTypeElement.getQualifiedName().toString();
                if ( getResult().hasAlternative(enclosingTypeElement) ){
                    if ( !getModel().getAlternativeClasses().contains( name ) ){
                        iterator.remove();
                    }
                }
                if ( !alternativeStereotypesEnabled(enclosingTypeElement) ){
                    iterator.remove();
                }
            }
        }
    }

    private Set<Element> findEnabledTypes(Set<Element> elements) {
        LinkedList<Element> types = new LinkedList<Element>( elements );
        Set<Element> result = new HashSet<Element>( elements );
        while( types.size() != 0 ) {
            TypeElement typeElement = (TypeElement)types.remove();
            if ( typeElement.getKind() != ElementKind.CLASS){
                result.remove( typeElement );
                continue;
            }
            checkProxyability( typeElement , types, result );
            checkSpecializes(typeElement, types, result ,  elements );
        }
        return result;
    }
    
    private void checkProxyability( TypeElement typeElement,
            LinkedList<Element> types , Set<Element> result)
    {
        /*
         * Certain legal bean types cannot be proxied by the container:
         * - classes which don't have a non-private constructor with no parameters,
         * - classes which are declared final or have final methods,
         * - primitive types,
         * -  and array types.
         */
        if ( hasModifier(typeElement, Modifier.FINAL)){
            types.remove(typeElement);
            result.remove( typeElement );
            return;
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                typeElement.getEnclosedElements()) ;
        for (ExecutableElement executableElement : methods) {
            if ( hasModifier(executableElement, Modifier.FINAL)){
                types.remove(typeElement);
                result.remove( typeElement );
                return;
            }
        }
        
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(
                typeElement.getEnclosedElements()) ;
        boolean appropriateCtor = false;
        for (ExecutableElement constructor : constructors) {
            if ( hasModifier(constructor, Modifier.PRIVATE)){
                continue;
            }
            if ( constructor.getParameters().size() == 0 ){
                appropriateCtor = true;
                break;
            }
        }
        
        if ( !appropriateCtor){
            types.remove(typeElement);
            result.remove( typeElement );
        }
    }
    
    private boolean hasModifier ( Element element , Modifier mod){
        Set<Modifier> modifiers = element.getModifiers();
        for (Modifier modifier : modifiers) {
            if ( modifier.equals( mod )){
                return true;
            }
        }
        return false;
    }

    private void checkSpecializes( TypeElement typeElement, 
            LinkedList<Element> beans, Set<Element> resultSet, 
            Set<Element> originalElements)
    {
        TypeElement current = typeElement;
        while( current != null ){
            TypeMirror superClass = current.getSuperclass(); 
            if (!(superClass instanceof DeclaredType)) {
                break;
            }
            if (!AnnotationObjectProvider.hasSpecializes(current, getHelper())) {
                break;
            }
            TypeElement superElement = (TypeElement) ((DeclaredType) superClass)
                .asElement();
            if (originalElements.contains(superElement)) {
                resultSet.remove(superElement);
            }
            beans.remove( superElement );
            if ( !getResult().getTypeElements().contains( superElement)){
                break;
            }
            current = superElement;
        }
    }

    private void addEnabledAlternative( TypeElement typeElement , Element element) {
        String name = typeElement.getQualifiedName().toString();
        if ( getResult().hasAlternative(element) ){
            if ( !getModel().getAlternativeClasses().contains( name ) ){
                return;
            }
            /*
             * I have commented the code below but I'm not sure is it 
             * correct. Specification doesn't mention the case 
             * when @Alternative annotation presents along with 
             * alternative Stereotypes.
             * 
             * if ( getModel().getAlternativeClasses().contains( name ) ){
             *  myEnabledAlternatives.add( element );
                return;
            }
             */
        }
        if ( alternativeStereotypesEnabled(element)){
            myEnabledAlternatives.add( element );
        }
    }
    
    private boolean alternativeStereotypesEnabled( Element element ){
        List<AnnotationMirror> stereotypes = getResult().getStereotypes(element);
        for (AnnotationMirror annotationMirror : stereotypes) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            TypeElement annotationTypeElement = (TypeElement)annotationType.asElement();
            if ( getResult().isAlternative(annotationTypeElement) ){
                if ( getResult().hasAlternative(annotationTypeElement) ){
                    String name = annotationTypeElement.getQualifiedName().toString();
                    if ( !getModel().getAlternativeStereotypes().contains(name) ){
                        return false;
                    }
                }
                else if ( !alternativeStereotypesEnabled(annotationTypeElement) ){
                        return false;
                }
            }
        }
        return true;
    }

    private ResultImpl getResult(){
        return myResult;
    }
    
    private BeansModel getModel(){
        return myBeansModel;
    }
    
    private AnnotationModelHelper getHelper(){
        return myHelper;
    }

    private Set<Element> myAlternatives;
    private Set<Element> myEnabledAlternatives;
    private ResultImpl myResult;
    private final AnnotationModelHelper myHelper;
    private final BeansModel myBeansModel;
}
