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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.web.beans.api.model.AbstractModelImplementation;
import org.netbeans.modules.web.beans.api.model.WebBeansModelException;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=WebBeansModelProvider.class)
public class WebBeansModelProviderImpl implements WebBeansModelProvider {
    
    private static final String PRODUCER_ANNOTATION = 
                                "javax.enterprise.inject.Produces";      // NOI18N
    
    static final String ANY_BINDING_ANNOTATION =
                                "javax.enterprise.inject.Any";           // NOI18N
    
    static final String CURRENT_BINDING_ANNOTATION =
                                "javax.enterprise.inject.Current";       // NOI18N
    
    static final String NEW_BINDING_ANNOTATION =
                                 "javax.enterprise.inject.New";          // NOI18N
    
    static final Logger LOGGER = Logger.getLogger(
            WebBeansModelProvider.class.getName());
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getInjectable(javax.lang.model.element.VariableElement, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    public Element getInjectable( VariableElement element , 
            AbstractModelImplementation impl) throws WebBeansModelException
    {
        WebBeansModelImplementation modelImpl = null;
        try {
            modelImpl = (WebBeansModelImplementation)impl;
        }
        catch( ClassCastException e ){
            return null;
        }
        /* 
         * Element could be injection point. One need first if all to check this.  
         */
        Element parent = element.getEnclosingElement();
        
        if ( parent instanceof TypeElement){
            /*if ( element.getSimpleName().contentEquals("myClass")){
                boolean isAss = modelImpl.getHelper().getCompilationController().getTypes().
                    isAssignable( resolveType("foo.MyClass", modelImpl.getHelper()), 
                            element.asType());
                System.out.println("%% "+isAss);
            }*/
            return findFieldInjectable(element, modelImpl);
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
            return findParameterInjectable(element);
        }
        
        return null;
    }
    
    public List<Element> getInjectables( VariableElement element ,
            AbstractModelImplementation impl )
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#resolveType(java.lang.String, org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper)
     */
    public TypeMirror resolveType( String fqn , AnnotationModelHelper helper ) {
        return helper.resolveType( fqn );
    }

    private Element findParameterInjectable( VariableElement element ) {
        /*
         * TODO : care about @Current, @Any , @New  
         *
         */
        List<? extends AnnotationMirror> annotations = 
            element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
        }
        return null;
    }

    private Element findFieldInjectable( VariableElement element,
            WebBeansModelImplementation modelImpl )
    {
        // Probably injected field.
        List<? extends AnnotationMirror> annotations = 
            modelImpl.getHelper().getCompilationController().getElements().
            getAllAnnotationMirrors(element);
        List<AnnotationMirror> bindingAnnotations = new LinkedList<AnnotationMirror>();
        boolean isProducer = false;
        
        /* Single @Any annotation means skip searching in bindings .
         * One need to check any bean that has required type .
         * @Any binding type along with other binding types 
         * equivalent to the same list of binding types without @Any.
         */
        boolean anyBindingType = false;
        
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( ANY_BINDING_ANNOTATION.equals( 
                    annotationElement.getQualifiedName().toString()))
            {
                anyBindingType = true;
            }
            else if ( isBinding( annotationElement , modelImpl.getHelper()) ){
                bindingAnnotations.add( annotationMirror );
            }
            if ( PRODUCER_ANNOTATION.equals( annotationElement.getQualifiedName())){
                isProducer = true;
            }
            /* TODO : one needs somehow to check absence of initialization
             * for field... 
             */
        }
        // producer is not injection point , it is injectable
        if ( isProducer || ( bindingAnnotations.size() == 0 && !anyBindingType )){
            return null;
        }
        /*
         * Single @Current annotation means increasing types that 
         * is eligible for injection. Each bean without any binding
         * type has @Current binding type by default. So it should
         * be also considered as injectable.  
         */
        boolean currentBindingType = false;
        /*
         * It is not clear for me situation with new.
         * Is it possible to assign this binding type manually ?
         * If not then it also should be single binding .
         * Otherwise the algorithm of finding injectable changes
         * even in situation of several binding types with @New between them.  
         */
        boolean newBindingType = false; // TODO ???
        String annotationName = null; 
        List<Element> result = new LinkedList<Element>();
        if ( bindingAnnotations.size() == 1 ){
            AnnotationMirror annotationMirror = bindingAnnotations.get( 0 );
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            annotationName = annotationElement.getQualifiedName().toString();
            currentBindingType = annotationElement.getQualifiedName().contentEquals( 
                    CURRENT_BINDING_ANNOTATION);
        }
        if ( (bindingAnnotations.size() == 0 && anyBindingType) || 
                currentBindingType )
        {
            LOGGER.fine("Found built-in binding "+annotationName); // NOI18N
            Set<TypeElement> assignableTypes= getAssignableTypes( element , modelImpl );
            if ( currentBindingType ){
                LOGGER.fine("@Current annotation requires test for implementors" +
                		" of varaible type");                      // NOI18N
                /*
                 *  Filter all appropriate types for presence binding type.
                 *  It should be absent at all or just single @Current.  
                 */
                filterBindnigsByCurrent( assignableTypes, modelImpl );
            }
            result.addAll( assignableTypes );
        }
        else {
            /*
             * This is list with types that have all required bindings. This
             * list will be used for further typesafe resolution.
             */
            Set<TypeElement> typesWithBindings = getBindingTypes(
                    bindingAnnotations, modelImpl);
            
            filterBindingsByMembers(bindingAnnotations, typesWithBindings,
                    modelImpl);
            /*
             * Now <code>typesWithBindings</code> contains appropriate types
             * which has required binding with required parameters ( if any ).
             * Next step is filter types via typesafe resolution.
             */
            filterBindingsByType( element , typesWithBindings , modelImpl );
            result.addAll( typesWithBindings );
        }
        
        /*
         * This is list with production fields or methods ( they have @Produces annotation )
         * that  have all required bindings.
         * This list will be also used for further typesafe resolution. 
         */
        List<Element> productionElements = getProductions( bindingAnnotations, 
                modelImpl); 
        return null;
    }

    private void filterBindnigsByCurrent( Set<TypeElement> assignableTypes,
            WebBeansModelImplementation modelImpl )
    {
        CurrentBindingTypeFilter filter = CurrentBindingTypeFilter.get();
        filter.init( modelImpl );
        filter.filter( assignableTypes );
    }

    private Set<TypeElement> getAssignableTypes( VariableElement element,
            WebBeansModelImplementation modelImpl )
    {
        TypeMirror typeMirror = element.asType();
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return Collections.emptySet();
        }
        Element typeElement = ((DeclaredType) typeMirror).asElement();
        if (!(typeElement instanceof TypeElement)) {
            return Collections.emptySet();
        }
        Set<TypeElement> result = new HashSet<TypeElement>();
        if (((TypeElement) typeElement).getTypeParameters().size() != 0) {
            // TODO : care about assignability of raw and parameterized types.
            // one needs inspect all references (recursively) to variable element type 
            // and choose appropriate parameterized types . 
        }
        else {
            ElementHandle<TypeElement> handle = ElementHandle
                    .create((TypeElement) typeElement);
            final Set<ElementHandle<TypeElement>> handles = modelImpl
                    .getHelper().getClasspathInfo().getClassIndex()
                    .getElements(
                            handle,
                            EnumSet.of(SearchKind.IMPLEMENTORS),
                            EnumSet.of(SearchScope.SOURCE,
                                    SearchScope.DEPENDENCIES));
            if (handles == null) {
                LOGGER.log(Level.WARNING,
                        "ClassIndex.getElements() was interrupted"); // NOI18N
                return Collections.emptySet();
            }
            for (ElementHandle<TypeElement> elementHandle : handles) {
                LOGGER.log(Level.FINE, "found derived element {0}",
                        elementHandle.getQualifiedName()); // NOI18N
                TypeElement derivedElement = elementHandle.resolve(modelImpl
                        .getHelper().getCompilationController());
                if (derivedElement == null) {
                    continue;
                }
                result.add(derivedElement);
            }
        }
        return result;
    }

    private void filterBindingsByType( VariableElement element,
            Set<TypeElement> typesWithBindings,
            WebBeansModelImplementation modelImpl )
    {
        TypeBindingFilter filter = TypeBindingFilter.get();
        filter.init( element , modelImpl );
        filter.filter( typesWithBindings );
    }

    private List<Element> getProductions( List<AnnotationMirror> bindings ,
            WebBeansModelImplementation modelImpl ) 
    {
        // TODO Auto-generated method stub
        return null;
    }

    private Set<TypeElement> getBindingTypes( List<AnnotationMirror> bindingAnnotations ,
            WebBeansModelImplementation modelImpl )
    {
        List<List<Binding>> bindingCollections = 
            new ArrayList<List<Binding>>( bindingAnnotations.size());
        for (AnnotationMirror annotationMirror : bindingAnnotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            String annotationFQN = annotationElement.getQualifiedName().toString();
            PersistentObjectManager<Binding> manager = modelImpl.getManager( 
                    annotationFQN );
            Collection<Binding> bindings = manager.getObjects();
            bindingCollections.add( new ArrayList<Binding>( bindings) );
        }
        List<Binding> result= null;
        for ( int i=0; i<bindingCollections.size() ; i++ ){
            List<Binding> list = bindingCollections.get(i);
            if ( i==0 ){
                result = list;
            }
            else {
                result.retainAll( list );
            }
        }
        if ( result == null ){
            return Collections.emptySet();
        }
        else {
            Set<TypeElement> list = new HashSet<TypeElement>( result.size());
            for (Binding binding : result) {
                list.add( binding.getType() );
            }
            return list;
        }
    }

    private boolean isBinding( TypeElement element, AnnotationModelHelper helper)
    {
        BindingChecker checker = BindingChecker.get();
        checker.init( element , helper );
        return checker.check();
    }

    private void filterBindingsByMembers(
            List<AnnotationMirror> bindingAnnotations,
            Set<TypeElement> typesWithBindings, WebBeansModelImplementation impl )
    {
        MemberBindingFilter filter = MemberBindingFilter.get();
        filter.init( bindingAnnotations, impl );
        filter.filter( typesWithBindings );
    }
    
}
