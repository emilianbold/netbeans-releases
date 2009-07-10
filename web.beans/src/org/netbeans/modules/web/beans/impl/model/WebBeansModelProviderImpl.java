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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
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
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.web.beans.api.model.AbstractModelImplementation;
import org.netbeans.modules.web.beans.api.model.WebBeansModelException;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=WebBeansModelProvider.class)
public class WebBeansModelProviderImpl implements WebBeansModelProvider {
    
    private static final String VALUE = "value";                         // NOI18N
    private static final String PRODUCER_ANNOTATION = 
                                "javax.enterprise.inject.Produces";      // NOI18N
    private static final String BINDING_TYPE_ANNOTATION=
                                "javax.enterprise.inject.BindingType";   // NOI18N
    
    private static final String NON_BINDING_MEMBER_ANNOTATION =
                                "javax.enterprise.inject.NonBinding";    // NOI18N
    
    private static final String ANY_BINDING_ANNOTATION =
                                "javax.enterprise.inject.Any";           // NOI18N
    
    private static final String CURRENT_BINDING_ANNOTATION =
                                "javax.enterprise.inject.Current";       // NOI18N
    
    private static final String NEW_BINDING_ANNOTATION =
                                 "javax.enterprise.inject.New";          // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(
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
        for (Iterator<TypeElement> iterator = assignableTypes.iterator(); 
                iterator.hasNext() ; ) 
        {
            TypeElement typeElement = iterator.next();
            List<? extends AnnotationMirror> allAnnotationMirrors = 
                modelImpl.getHelper().getCompilationController().getElements().
                    getAllAnnotationMirrors( typeElement );
            Set<String> bindingNames = new HashSet<String>();
            for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
                DeclaredType annotationType = annotationMirror.getAnnotationType();
                TypeElement annotationElement = (TypeElement)annotationType.asElement();
                if ( isBinding( annotationElement, modelImpl.getHelper())){
                    bindingNames.add( annotationElement.getQualifiedName().toString());
                }
            }
            bindingNames.remove( ANY_BINDING_ANNOTATION );
            bindingNames.remove( CURRENT_BINDING_ANNOTATION );
            if ( bindingNames.size() != 0 ){
                iterator.remove();
            }
        }
    }

    private Set<TypeElement> getAssignableTypes( VariableElement element,
            WebBeansModelImplementation modelImpl )
    {
        // TODO : care about assignability of raw and parameterized types.
        TypeMirror typeMirror = element.asType();
        if ( typeMirror.getKind() != TypeKind.DECLARED ){
            return Collections.emptySet();
        }
        Element typeElement = ((DeclaredType)typeMirror).asElement();
        if ( !( typeElement instanceof TypeElement) ){
            return Collections.emptySet();
        }
        ElementHandle<TypeElement> handle = ElementHandle.create( 
                (TypeElement)typeElement);
        final Set<ElementHandle<TypeElement>> handles = modelImpl.getHelper()
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
        Set<TypeElement> result = new HashSet<TypeElement>();
        for (ElementHandle<TypeElement> elementHandle : handles) {
            LOGGER.log(Level.FINE, "found derived element {0}", elementHandle
                    .getQualifiedName()); // NOI18N
            TypeElement derivedElement = elementHandle.resolve(
                    modelImpl.getHelper().getCompilationController());
            if (derivedElement == null) {
                continue;
            }
            result.add( derivedElement );
        }
        return result;
    }

    private void filterBindingsByType( VariableElement element,
            Set<TypeElement> typesWithBindings,
            WebBeansModelImplementation modelImpl )
    {
        if ( typesWithBindings.size() == 0 ){
            return;
        }
        TypeMirror typeMirror = element.asType();
        TypeKind kind = typeMirror.getKind();
        if ( kind == TypeKind.DECLARED ){
            for ( Iterator<TypeElement> iterator = typesWithBindings.iterator(); 
                iterator.hasNext(); )
            {
                TypeElement type = iterator.next();
                if ( modelImpl.getHelper().getCompilationController().getTypes().
                        isAssignable( type.asType(), typeMirror))
                {
                    LOGGER.fine("Found type element " +type.getQualifiedName() +
                            " for variable element " +element.getSimpleName()+ 
                            " by typesafe resolution");                 // NOI18N
                }
                else if ( checkAssignability( element , type , modelImpl)){
                    LOGGER.fine("Found parametrizied  or raw type element " +
                            type.getQualifiedName() +
                            " for variable element " +element.getSimpleName()+ 
                            " by typesafe resolution");                 // NOI18N
                }
                else {
                    iterator.remove();
                }
            }
        }
        else if ( kind.isPrimitive()  ){
            LOGGER.fine("Variable element " +element.getSimpleName()+ " " +
            		"couldn't have type as eligible for inection becuase its " +
            		"type is primitive. It is unproxyable bean types"); // NOI18N
        }
        else if ( kind == TypeKind.ARRAY ){
            LOGGER.fine("Variable element " +element.getSimpleName()+ " " +
                    "couldn't have type as eligible for inection becuase its " +
                    "type has array type. It is unproxyable bean types");// NOI18N
        }
    }

    private boolean checkAssignability( VariableElement element,
            TypeElement type, WebBeansModelImplementation modelImpl )
    {
        // TODO : check raw types and parameters...
        type.getTypeParameters();
        return false;
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
        if ( BUILT_IN_BINDINGS.contains( element.getQualifiedName().toString())){
            return true;
        }
        else {
            List<? extends AnnotationMirror> annotations = 
                element.getAnnotationMirrors();
            boolean isBindingType = helper.hasAnnotation(annotations, 
                    BINDING_TYPE_ANNOTATION);
            boolean hasRequiredRetention = helper.hasAnnotation(annotations, 
                    Retention.class.getCanonicalName());
            boolean hasRequiredTarget = helper.hasAnnotation(annotations, 
                    Target.class.getCanonicalName());
            
            if ( !isBindingType || !hasRequiredRetention || !hasRequiredTarget){
                return false;
            }
            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expectEnumConstant(VALUE, 
                    helper.resolveType(RetentionPolicy.class.getCanonicalName()) , 
                    null);
            Map<String, ? extends AnnotationMirror> types = helper.
                getAnnotationsByType(annotations);
            AnnotationMirror retention = types.get(
                    Retention.class.getCanonicalName() );              // NOI18N
            String retentionPolicy = parser.parse(retention).get( VALUE , 
                    String.class);
            hasRequiredRetention = retentionPolicy.equals( 
                    RetentionPolicy.RUNTIME.toString());
            if ( !hasRequiredRetention ){
                return false;
            }
            hasRequiredTarget = checkTarget(helper, hasRequiredTarget, types);
            
            return hasRequiredTarget;
        }
    }

    private boolean checkTarget( AnnotationModelHelper helper,
            boolean hasRequiredTarget,
            Map<String, ? extends AnnotationMirror> types )
    {
        AnnotationParser parser;
        parser = AnnotationParser.create(helper);
        final Set<String> elementTypes = new HashSet<String>();
        parser.expectEnumConstantArray( VALUE, helper.resolveType(
                ElementType.class.getCanonicalName()), 
                new ArrayValueHandler() {
                    
                    public Object handleArray( List<AnnotationValue> arrayMembers ) {
                        for (AnnotationValue arrayMember : arrayMembers) {
                            String value = arrayMember.getValue().toString();
                            elementTypes.add(value);
                        }
                        return null;
                    }
                } , null);
        
        parser.parse( types.get(Target.class.getCanonicalName() ));
        if ( elementTypes.contains( ElementType.METHOD.toString()) &&
                elementTypes.contains(ElementType.FIELD.toString()) &&
                        elementTypes.contains(ElementType.PARAMETER.toString())&&
                        elementTypes.contains( ElementType.TYPE.toString()))
        {
            hasRequiredTarget = true;
        }
        return hasRequiredTarget;
    }
    
    private void filterBindingsByMembers(
            List<AnnotationMirror> bindingAnnotations,
            Set<TypeElement> typesWithBindings, WebBeansModelImplementation impl )
    {
        if ( typesWithBindings.size() == 0 ){
            return;
        }
        /*
         * Binding annotation could have members. See example :
         * @BindingType
         * @Retention(RUNTIME)
         * @Target({METHOD, FIELD, PARAMETER, TYPE})
         * public @interface PayBy {
         * PaymentMethod value();
         * @NonBinding String comment();
         * }    
         * One need to check presence of member in binding annotation at 
         * injected point and compare this member with member in annotation
         * for discovered type.
         * Members with  @NonBinding annotation should be iognored. 
         */
         for (AnnotationMirror annotation : bindingAnnotations) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> 
                elementValues = annotation.getElementValues();
            Set<ExecutableElement> bindingMembers = collectBindingMembers(
                    annotation, impl );
            checkMembers(elementValues, bindingMembers, typesWithBindings, impl );
        }
    }

    private void checkMembers(
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues,
            Set<ExecutableElement> members , Set<TypeElement> typesWithBindings,
            WebBeansModelImplementation impl )
    {
        for( Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
            elementValues.entrySet())
        {
            ExecutableElement execElement = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( members.contains( execElement )) {
                checkMember( execElement, value, typesWithBindings , impl );
            }
        }
    }

    private Set<ExecutableElement> collectBindingMembers( 
            AnnotationMirror annotation , WebBeansModelImplementation impl ) 
    {
        DeclaredType annotationType  = annotation.getAnnotationType();
        TypeElement annotationElement = (TypeElement)annotationType.asElement();
        List<? extends Element> members = annotationElement.getEnclosedElements();
        Set<ExecutableElement> bindingMembers = new HashSet<ExecutableElement>();
        for (Element member : members) {
            if ( member instanceof ExecutableElement ){
                ExecutableElement exec = (ExecutableElement)member;
                if ( isBindingMember( exec , impl)){
                    bindingMembers.add( exec );
                }
            }
        }
        return bindingMembers;
    }

    private void checkMember( ExecutableElement exec, AnnotationValue value,
            Set<TypeElement> typesWithBindings,
            WebBeansModelImplementation impl )
    {
        // annotation member should be checked for presence at Binding type
        for (Iterator<TypeElement> iterator = typesWithBindings.iterator(); 
            iterator.hasNext(); ) 
        {
            // TODO : care about specialize
            TypeElement element = iterator.next();
            List<? extends AnnotationMirror> allAnnotationMirrors = impl
                    .getHelper().getCompilationController().getElements()
                    .getAllAnnotationMirrors(element);
            for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> 
                    elementValues = annotationMirror.getElementValues();
                AnnotationValue valueForType = elementValues.get( exec );
                if ( !equals( value, valueForType)){
                    iterator.remove( );
                }
            }
        }
    }
    
    private boolean isBindingMember( ExecutableElement element , 
            WebBeansModelImplementation impl)
    {
        List<? extends AnnotationMirror> annotationMirrors = 
            impl.getHelper().getCompilationController().getElements().
                    getAllAnnotationMirrors( element);
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            Name name = ((TypeElement)annotationMirror.getAnnotationType().asElement()).
                getQualifiedName();
            if ( NON_BINDING_MEMBER_ANNOTATION.contentEquals(name)){
                return false;
            }
        }
        return true;
    }
    
    private boolean equals( AnnotationValue value1 , AnnotationValue value2 ){
        if ( value1== null ){
            return value2 == null;
        }
        else {
            if ( value1.getValue() == null ){
                return value2!= null && value2.getValue()==null;
            }
            else {
                return value1.getValue().equals( value2 == null ? null : value2.getValue());
            }
        }
    }
    
    private static final Set<String> BUILT_IN_BINDINGS = new HashSet<String>();
    static {
        BUILT_IN_BINDINGS.add(ANY_BINDING_ANNOTATION);
        BUILT_IN_BINDINGS.add(NEW_BINDING_ANNOTATION);
        BUILT_IN_BINDINGS.add(CURRENT_BINDING_ANNOTATION);
    }

}
