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

import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;


/**
 * This object provider cares about types that directly have annotations,
 * types that inherit annotations ( they extends class or interface that
 * directly have annotation and either 
 * - annotations is @Inherited
 * - type specializes type with annotation ( each hierarchy step has @Specializes
 * annotations ).
 * 
 * So result object ( type ) could not have directly annotation under subject and also 
 * there could be objects ( types ) which don't have even inherited annotation.
 * ( but hey have parents with this annotation and they specializes this parent ). 
 * @author ads
 *
 */
class AnnotationObjectProvider implements ObjectProvider<Binding> {
    
    private static final String SPECILIZES_ANNOTATION = 
        "javax.enterprise.inject.deployment.Specializes";       // NOI18N
    
    static final Logger LOGGER = Logger.getLogger(
            AnnotationObjectProvider.class.getName());

    AnnotationObjectProvider( AnnotationModelHelper helper , String annotation) {
        myHelper = helper;
        myAnnotationName = annotation;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
     */
    public List<Binding> createInitialObjects() throws InterruptedException {
        final List<Binding> result = new LinkedList<Binding>();
        final Set<TypeElement> set = new HashSet<TypeElement>(); 
        getHelper().getAnnotationScanner().findAnnotations(getAnnotationName(), 
                AnnotationScanner.TYPE_KINDS, 
                new AnnotationHandler() {
                    public void handleAnnotation(TypeElement type, 
                            Element element, AnnotationMirror annotation) 
                    {
                        if ( !set.contains( type )){
                            result.add( new Binding( getHelper(), type , 
                                getAnnotationName()));
                        }
                        set.add( type );
                        if ( !getHelper().hasAnnotation( annotation.
                                getAnnotationType().asElement().
                                getAnnotationMirrors(), 
                                Inherited.class.getCanonicalName()))
                        {
                            /*
                             *  if annotation is inherited then method 
                             *  findAnnotations()
                             *  method will return types with this annotation.
                             *  Otherwise there could be implementors which 
                             *  specialize this type.
                             */
                            collectSpecializedImplementors( type , set, result );
                        }
                    }

        } );
        return new ArrayList<Binding>( result );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
     */
    public List<Binding> createObjects( TypeElement type ) {
        final List<Binding> result = new ArrayList<Binding>();
        Map<String, ? extends AnnotationMirror> annotationsByType = 
            getHelper().getAnnotationsByType(getHelper().getCompilationController().
                getElements().getAllAnnotationMirrors( type ));
        AnnotationMirror annotationMirror = annotationsByType.get( 
                getAnnotationName());
        if (annotationMirror != null ) {
            result.add( new Binding(getHelper(), type, getAnnotationName()));
        }
        if ( annotationMirror == null || !getHelper().hasAnnotation( annotationMirror.
                getAnnotationType().asElement().
                getAnnotationMirrors(), 
                Inherited.class.getCanonicalName()))
        {
            if ( checkSuper( type , getAnnotationName() , getHelper())!= null ){
                result.add( new Binding( getHelper(), type, getAnnotationName()) );
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
     */
    public boolean modifyObjects( TypeElement type, List<Binding> bindings ) {
        /*
         * Type element couldn't have the same annotation twice.
         * Provider based on single annotation ( its FQN  ).
         * So each type could have only one annotation at most.
         */
        assert bindings.size() ==1;
        Binding binding = bindings.get(0);
        assert binding!= null;
        if ( ! binding.refresh(type)){
            bindings.remove(0);
            return true;
        }
        return false;
    }
    
    static TypeElement checkSuper( TypeElement type , String annotationName, 
            AnnotationModelHelper helper ) 
    {
        if ( !hasSpecializes( type, helper )){
            return null;
        }
        
        TypeElement superClass = helper.getSuperclass(type);
        if ( FieldInjectionPointLogic.CURRENT_BINDING_ANNOTATION.equals( annotationName)){
            if ( checkSpecializedCurrent( superClass, helper )){
                return superClass;
            }
        }
        if ( hasAnnotation( superClass , annotationName, helper)){
            return superClass;
        }
        TypeElement foundSuper = checkSuper( superClass, annotationName , helper );
        if ( foundSuper!= null ){
            return foundSuper;
        }
        
        /* interfaces could not be injectables , but let's inspect them as possible 
         * injectables for notifying user about error if any.
         */   
        
        List<? extends TypeMirror> interfaces = type.getInterfaces();
        for (TypeMirror typeMirror : interfaces) {
            Element el = helper.getCompilationController().getTypes().
                asElement(typeMirror);
            if ( el instanceof TypeElement ){
                TypeElement interfaceElement = (TypeElement) el;
                if ( FieldInjectionPointLogic.CURRENT_BINDING_ANNOTATION.equals( annotationName)){
                    if ( checkSpecializedCurrent( interfaceElement, helper )){
                        return superClass;
                    }
                }
                if ( hasAnnotation( interfaceElement  , annotationName, helper)){
                    return interfaceElement;
                }
                foundSuper = checkSuper( interfaceElement , annotationName , helper);
                if ( foundSuper != null ){
                    return foundSuper;
                }
            }
        }
        
        return null;
    }
    
    /*
     * This method is called only for parent which are specialized.
     * In this case @Current is not "inherited" child from parents. 
     */
    static boolean checkSpecializedCurrent( Element element , AnnotationModelHelper helper){
        /*
        Set<String> bindingNames = getBindings(element, helper);
        if ( bindingNames.contains(
                        WebBeansModelProviderImpl.CURRENT_BINDING_ANNOTATION))
        {
            return true;
        }
        if ( bindingNames.size() == 0 ){
            return true;
        }
        */
        return helper.hasAnnotation( helper.getCompilationController().
                getElements().getAllAnnotationMirrors(element), 
                WebBeansModelProviderImpl.CURRENT_BINDING_ANNOTATION);
    }
    
    static  boolean checkCurrent( Element element , AnnotationModelHelper helper){
        Set<String> bindingNames = getBindings(element, helper);
        if ( bindingNames.contains(
                        WebBeansModelProviderImpl.CURRENT_BINDING_ANNOTATION))
        {
            return true;
        }
        if ( bindingNames.size() == 0 ){
            return true;
        }
        return false;
    }
    
    static Set<String> getBindings(Element element , AnnotationModelHelper helper){
        Set<String> bindingNames = new HashSet<String>();
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            helper.getCompilationController().getElements().getAllAnnotationMirrors( element );
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            DeclaredType annotationType = annotationMirror
                    .getAnnotationType();
            TypeElement annotationElement = (TypeElement) annotationType
                    .asElement();
            if (isBinding(annotationElement, helper )) {
                bindingNames.add(annotationElement.getQualifiedName()
                        .toString());
            }
        }
        return bindingNames;
    }
    
    static boolean isBinding( TypeElement annotationElement , 
            AnnotationModelHelper helper) 
    {
        BindingChecker checker = BindingChecker.get();
        checker.init(annotationElement, helper );
        return checker.check();
    }
    
    static boolean hasSpecializes( Element element , 
            AnnotationModelHelper helper )
    {
        return hasAnnotation(element , SPECILIZES_ANNOTATION , helper );
    }
    
    static boolean hasAnnotation( Element element, String annotation, 
            AnnotationModelHelper helper )
    {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            helper.getCompilationController().getElements().
            getAllAnnotationMirrors(element);
        return helper.hasAnnotation(allAnnotationMirrors, 
                annotation );
    }
    
    private String getAnnotationName(){
        return myAnnotationName;
    }
    
    private AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private void collectSpecializedImplementors( TypeElement type, Set<TypeElement> set, 
            List<Binding> bindings ) 
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        Set<TypeElement> toProcess = new HashSet<TypeElement>();
        toProcess.add(type);
        while (toProcess.size() > 0) {
            TypeElement element = toProcess.iterator().next();
            toProcess.remove(element);
            Set<TypeElement> implementors = doCollectSpecializedImplementors(
                    element,bindings);
            if (implementors.size() == 0) {
                continue;
            }
            result.addAll(implementors);
            for (TypeElement impl : implementors) {
                toProcess.add(impl);
            }
        }
        for (TypeElement derivedElement : result) {
            if (!hasSpecializes(derivedElement, getHelper())) {
                continue;
            }
            handleSuper(type, derivedElement, bindings, set);
        }
    }
    
    private Set<TypeElement> doCollectSpecializedImplementors( TypeElement type, 
            List<Binding> bindings )
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        ElementHandle<TypeElement> handle = ElementHandle.create(type);
        final Set<ElementHandle<TypeElement>> handles = getHelper()
                .getClasspathInfo().getClassIndex().getElements(
                        handle,
                        EnumSet.of(SearchKind.IMPLEMENTORS),
                        EnumSet
                                .of(SearchScope.SOURCE,
                                        SearchScope.DEPENDENCIES));
        if (handles == null) {
            LOGGER.log(Level.WARNING,
                    "ClassIndex.getElements() was interrupted"); // NOI18N
            return Collections.emptySet();
        }
        for (ElementHandle<TypeElement> elementHandle : handles) {
            LOGGER.log(Level.FINE, "found derived element {0}", elementHandle
                    .getQualifiedName()); // NOI18N
            TypeElement derivedElement = elementHandle.resolve(getHelper().
                    getCompilationController());
            if (derivedElement == null) {
                continue;
            }
            result.add(derivedElement);
        }
        return result;
    }
    
    private boolean  handleInterface( TypeElement element, TypeElement child,
            Set<TypeElement> collectedElements , Set<TypeElement>  bindingTypes )
    {
        /* interfaces could not be injectables , but let's inspect them as possible 
         * injectables for notifying user about error if any.
         */ 
        List<? extends TypeMirror> interfaces = child.getInterfaces();
        for (TypeMirror typeMirror : interfaces) {
            if ( getHelper().getCompilationController().getTypes().isSameType(
                    element.asType(), typeMirror) )
            {
                return true;
            }
            if ( getHelper().getCompilationController().getTypes().
                    isAssignable( typeMirror, element.asType()))
            {
                Element el = getHelper().getCompilationController().
                    getTypes().asElement( typeMirror );
                if ( !( el instanceof TypeElement )){
                    return false;
                }
                TypeElement interfaceElement = (TypeElement)el;
                if ( bindingTypes.contains( interfaceElement) ){
                    return true;
                }
                collectedElements.add( interfaceElement);
                if ( !hasSpecializes( interfaceElement , getHelper() ) ){
                    return false;
                }
                else {
                    return handleInterface(element, interfaceElement, 
                            collectedElements, bindingTypes );
                }
            }
        }  
        
        return false;
    }

    private void handleSuper(TypeElement type ,TypeElement child, 
            List<Binding> bindings, Set<TypeElement> set) 
    {
        if ( !getHelper().getCompilationController().getTypes().isAssignable( 
                child.asType(), type.asType()))
        {
            return;
        }
        List<? extends TypeElement> superclasses = getHelper().getSuperclasses(
                child);
        Set<TypeElement> collectedSuper = new HashSet<TypeElement>();
        collectedSuper.add( child );
        boolean specializes = true;
        TypeElement previous = child;
        for (TypeElement superElement : superclasses) {
            if (superElement.equals(type) || set.contains( superElement)) {
                break;
            }
            if ( getHelper().getCompilationController().getTypes().
                    isAssignable( superElement.asType(), type.asType()))
            {
                previous = superElement;
            }
            else {
                if ( !hasSpecializes(superElement, getHelper())) {
                    specializes = false;
                    break;
                }
                collectedSuper.add(superElement);
                specializes = handleInterface(type, previous, collectedSuper, set );
                break;
            }
        }
        if (specializes) {
            for (TypeElement superElement : collectedSuper) {
                if (!set.contains(superElement)) {
                    set.add(superElement);
                    bindings.add(new Binding(getHelper(), superElement,
                            getAnnotationName()));
                }
            }
        }
    }
    
    private AnnotationModelHelper myHelper;
    private String myAnnotationName;

}
