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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.beans.api.model.Result;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class WebBeansModelProviderImpl extends EventInjectionPointLogic {
    
    protected WebBeansModelProviderImpl(WebBeansModelImplementation model){
        super( model );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getCompilationController()
     */
    @Override
    public CompilationController getCompilationController(){
        return getModel().getHelper().getCompilationController();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#resolveType(java.lang.String)
     */
    @Override
    public TypeMirror resolveType( String fqn ) {
        return getModel().getHelper().resolveType( fqn );
    }

    public Result getInjectable(VariableElement element, DeclaredType parentType) 
    {
        /* 
         * Element could be injection point. One need first if all to check this.  
         */
        Element parent = element.getEnclosingElement();
        
        if ( parent instanceof TypeElement){
            return findVariableInjectable(element, parentType );
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
            return findParameterInjectable(element, parentType );
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#isInjectionPoint(javax.lang.model.element.VariableElement)
     */
    @Override
    public boolean isInjectionPoint( VariableElement element )  
        throws org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError
    {
        Element parent = element.getEnclosingElement();
        
        if ( parent instanceof TypeElement){
            List<? extends AnnotationMirror> annotations = 
                getModel().getHelper().getCompilationController().getElements().
                getAllAnnotationMirrors(element);
            return getModel().getHelper().hasAnnotation(annotations, INJECT_ANNOTATION);
        }
        else if ( parent instanceof ExecutableElement ){
            return isMethodParameterInjection(element,(ExecutableElement)parent);
        }
        return false;
    }

    private boolean isMethodParameterInjection( VariableElement element,
            ExecutableElement parent )
            throws org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError
    {
        List<? extends AnnotationMirror> annotations = 
            getModel().getHelper().getCompilationController().getElements().
            getAllAnnotationMirrors(parent);
        if (isDisposeParameter( element, parent, annotations)){
            return true;
        }
        /*
         * Parameter with @Observes annotation is not plain injection point. 
         */
        boolean hasObserves = AnnotationObjectProvider.hasAnnotation(element, 
                OBSERVES_ANNOTATION, getModel().getHelper());
        if ( !hasObserves && isObservesParameter(element, parent, annotations)){
            return true;
        }
        return getModel().getHelper().hasAnnotation(annotations, INJECT_ANNOTATION)||
            getModel().getHelper().hasAnnotation(annotations, PRODUCER_ANNOTATION);
    }

    public List<AnnotationMirror> getQualifiers(Element element) {
        List<AnnotationMirror> result = new LinkedList<AnnotationMirror>();
        List<? extends AnnotationMirror> annotations = getModel().getHelper().
            getCompilationController().getElements().getAllAnnotationMirrors( 
                    element);
        
        boolean event = getParameterType(element, null, EVENT_INTERFACE) != null;
        
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( isQualifier( annotationElement , getModel().getHelper(), event) ){
                result.add( annotationMirror );
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getName(javax.lang.model.element.Element)
     */
    @Override
    public String getName( Element element)
    {
        String name = inspectSpecializes( element );
        if ( name != null ){
            return name;
        }
        List<AnnotationMirror> allStereotypes = getAllStereotypes(element, 
                getModel().getHelper());
        for (AnnotationMirror annotationMirror : allStereotypes) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            TypeElement annotation = (TypeElement)annotationType.asElement();
            if ( AnnotationObjectProvider.hasAnnotation(annotation, 
                    NAMED_QUALIFIER_ANNOTATION, getModel().getHelper() ) )
            {
                return getNamedName(element , null);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getNamedElements()
     */
    @Override
    public List<Element> getNamedElements() {
        boolean dirty = isDirty.getAndSet( false );
        
        if ( !isIndexListenerAdded ){
            addIndexListener( );
        }
        
        if ( !dirty ) {
            List<Element> result = getCachedNamedElements( );
            if ( !isDirty.get() ) {
                return result;
            }
        }
        
        List<Element> result = new LinkedList<Element>();
        Collection<BindingQualifier> objects = getModel().getNamedManager().getObjects();
        for (BindingQualifier named : objects) {
            TypeElement element = named.getTypeElement();
            // filter stereotypes
            if ( element.getKind() != ElementKind.ANNOTATION_TYPE) {
                result.add( element );
            }
        }
        List<Element> members = AbstractObjectProvider.getNamedMembers( 
                getModel().getHelper() );
        for (Element element : members) {
            if ( element.getKind()!= ElementKind.METHOD ){
                continue;
            }
            Set<Element> childSpecializes = getChildSpecializes( element, getModel() );
            result.addAll( childSpecializes );
        }
        result.addAll( members );
        
        Set<String> stereotypeNames = getModel().adjustStereotypesManagers();
        for (String stereotype : stereotypeNames) {
            PersistentObjectManager<StereotypedObject> manager = 
                getModel().getStereotypedManager(stereotype);
            Collection<StereotypedObject> beans = manager.getObjects();
            for (StereotypedObject bean : beans) {
                TypeElement element = bean.getTypeElement();
                // filter stereotypes
                if ( element.getKind() != ElementKind.ANNOTATION_TYPE) {
                    result.add( element );
                }
            }
            List<Element> stereotypedMembers = StereotypedObjectProvider.
                getAnnotatedMembers( stereotype, getModel().getHelper());
            result.addAll( stereotypedMembers );
        }
        PackagingFilter filter = new PackagingFilter(getModel());
        filter.filter(result);
        
        setCachedResult( result );
        return result;
    }
    
    public static List<AnnotationMirror> getAllStereotypes( Element element ,
            AnnotationModelHelper helper  ) 
    {
        List<AnnotationMirror> result = new LinkedList<AnnotationMirror>();
        Set<Element> foundStereotypesElement = new HashSet<Element>(); 
        StereotypeChecker checker = new StereotypeChecker( helper);
        doGetStereotypes(element, result, foundStereotypesElement, checker,
                helper);
        return result;
    }
    
    public static boolean isStereotype( TypeElement annotationElement,
            StereotypeChecker checker ) 
    {
        checker.init(annotationElement);
        boolean result = checker.check();
        checker.clean();
        return result;
    }
    
    private void setCachedResult( List<Element> list) {
        myNamedElement = new ArrayList<ElementHandle<? extends Element>>( list.size());
        for( Element element : list ){
            myNamedElement.add( ElementHandle.create( element ));
        }
    }

    private List<Element> getCachedNamedElements()  {
        List<Element> result = new ArrayList<Element>( myNamedElement.size());
        for ( ElementHandle<? extends Element> handle : myNamedElement ){
            Element element = handle.resolve(getModel().getHelper().
                    getCompilationController());
            if ( element != null ){
                result.add( element );
            }
        }
        return result;
    }
    
    private void addIndexListener( ) {
        final AnnotationModelHelper helper = getModel().getHelper();
        helper.getClasspathInfo().getClassIndex().addClassIndexListener( 
            new ClassIndexListener(){
            
                public void typesAdded(final TypesEvent event) {
                    setDirty();
                }

                public void typesRemoved(final TypesEvent event) {
                    setDirty();
                }

                public void typesChanged(final TypesEvent event) {
                    setDirty();
                }

                public void rootsAdded(RootsEvent event) {
                    setDirty();
                }

                public void rootsRemoved(RootsEvent event) {
                    setDirty();
                }
                
                private void setDirty(){
                    isDirty.set( true );
                    
                }
        });        
    }
    
    private String inspectSpecializes( Element element){
        if (element instanceof TypeElement) {
            String name = doGetName(element, element);
            if ( name != null ){
                return name;
            }
            TypeElement superElement = AnnotationObjectProvider.checkSuper(
                    (TypeElement)element, NAMED_QUALIFIER_ANNOTATION, 
                    getModel().getHelper());
            if ( superElement != null ){
                return doGetName(element, superElement);
            }
        }
        else if ( element instanceof ExecutableElement ){
            String name = doGetName(element, element);
            if ( name == null ){
                Element specialized = MemberCheckerFilter.getSpecialized( element, 
                        getModel(), NAMED_QUALIFIER_ANNOTATION);
                if ( specialized!= null ){
                    return doGetName(element , specialized);
                }
            }
            else {
                return name;
            }
        }
        else {
            return doGetName(element, element);
        }
        return null;
    }
    
    private String doGetName( Element original , Element element ){
        List<? extends AnnotationMirror> annotations = getModel().getHelper().
            getCompilationController().getElements().getAllAnnotationMirrors( 
                element);
        for (AnnotationMirror annotationMirror : annotations) {
        DeclaredType type = annotationMirror.getAnnotationType();
        TypeElement annotationElement = (TypeElement)type.asElement();
            if ( NAMED_QUALIFIER_ANNOTATION.contentEquals( 
                    annotationElement.getQualifiedName()))
            {
                return getNamedName( original , annotationMirror );
            }
        }
        return null;
    }
    
    private static void doGetStereotypes( Element element , 
            List<AnnotationMirror> result ,Set<Element>  foundStereotypesElement,
            StereotypeChecker checker , AnnotationModelHelper helper ) 
    {
        List<? extends AnnotationMirror> annotationMirrors = helper.
            getCompilationController().getElements().getAllAnnotationMirrors( element );
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            TypeElement annotationElement = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if ( foundStereotypesElement.contains( annotationElement)){
                continue;
            }
            if ( isStereotype( annotationElement, checker ) ){
                foundStereotypesElement.add( annotationElement );
                result.add(annotationMirror);
                doGetStereotypes(annotationElement, result, 
                        foundStereotypesElement, checker , helper);
            }
        }
    }
    
    private String getNamedName( Element element, AnnotationMirror namedAnnotation )
    {
        if (namedAnnotation != null) {
            AnnotationParser parser = AnnotationParser.create(getModel().getHelper());
            parser.expectString(RuntimeAnnotationChecker.VALUE, null);
            ParseResult result = parser.parse(namedAnnotation);
            String name = result.get(RuntimeAnnotationChecker.VALUE, String.class);
            if ( name != null ){
                return name;
            }
        }
        if ( element instanceof TypeElement ){
            String name = element.getSimpleName().toString();
            if ( name.length() >0 ){
                return Character.toLowerCase(name.charAt( 0 ))+name.substring(1);
            }
            else {
                return name;
            }
        }
        if ( element instanceof VariableElement ){
            return element.getSimpleName().toString();
        }
        if ( element instanceof ExecutableElement ){
            String name = element.getSimpleName().toString();
            if ( name.startsWith("get") && name.length() > 3 ){     // NOI18N
                return getPropertyName(name, 3);
            }
            else if ( name.startsWith("is") && name.length() >2 ){  // NOI18N
                return getPropertyName(name, 2);
            }
            return name;
        }
        return null;
    }
    
    private String getPropertyName(String methodName, int prefixLength) {
        String propertyName = methodName.substring(prefixLength);
        String propertyNameWithoutFL = propertyName.substring(1);

        if (propertyNameWithoutFL.length() > 0) {
            if (propertyNameWithoutFL.equals(propertyNameWithoutFL.toUpperCase())) {
                //property is in uppercase
                return propertyName;
            }
        }
        return Character.toLowerCase(propertyName.charAt(0)) + propertyNameWithoutFL;
    }

    /*
     * Observer method could have only one parameter.
     * Other parameters are error for observer method.
     * They are not injection points.
     */
    private boolean isObservesParameter( VariableElement element,
            ExecutableElement method , List<? extends AnnotationMirror> annotations ) 
        throws org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError
    {
        List<? extends VariableElement> parameters = method.getParameters();
        boolean observesFound = false;
        for (VariableElement variableElement : parameters) {
            if (  AnnotationObjectProvider.hasAnnotation(variableElement, 
                    OBSERVES_ANNOTATION, getModel().getHelper()))
            {
                if ( observesFound ){
                    throw new org.netbeans.modules.web.beans.api.model.
                        InjectionPointDefinitionError(method, 
                            NbBundle.getMessage(WebBeansModelImplementation.class, 
                                    "ERR_MultipleObserves" , method.getSimpleName()));
                }
                observesFound = true;
            }
        }
        if ( !observesFound ){
            return false;
        }
        
        String badAnnotation = checkInjectProducers(annotations);
        if ( badAnnotation != null ){
            throw new org.netbeans.modules.web.beans.api.model.
                InjectionPointDefinitionError( method, 
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
            ExecutableElement method , List<? extends AnnotationMirror> annotations) 
            throws org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError
    {
        List<? extends VariableElement> parameters = method.getParameters();
        boolean disposeFound = false;
        boolean observesFound = false;
        for (VariableElement variableElement : parameters) {
            if (  AnnotationObjectProvider.hasAnnotation(variableElement, 
                    DISPOSES_ANNOTATION, getModel().getHelper()))
            {
                if ( disposeFound ){
                    throw new org.netbeans.modules.web.beans.api.model. 
                    InjectionPointDefinitionError(method, 
                            NbBundle.getMessage(WebBeansModelImplementation.class, 
                                    "ERR_MultipleDisposes" , method.getSimpleName()));
                }
                disposeFound = true;
            }
            if (  AnnotationObjectProvider.hasAnnotation(variableElement, 
                    OBSERVES_ANNOTATION, getModel().getHelper()))
            {
                observesFound = true;
            }
        }
        if ( !disposeFound ){
            return false;
        }
        if ( observesFound ){
            throw new org.netbeans.modules.web.beans.api.model.
                InjectionPointDefinitionError(method, 
                    NbBundle.getMessage(WebBeansModelImplementation.class, 
                            "ERR_DisposesHasObserves" , method.getSimpleName()));
        }
        String badAnnotation = checkInjectProducers(annotations);
        if ( badAnnotation != null ){
            throw new org.netbeans.modules.web.beans.api.model.
                InjectionPointDefinitionError( method, 
                    NbBundle.getMessage(WebBeansModelImplementation.class, 
                            "ERR_DisposesHasInjectOrProduces" , method.getSimpleName(),
                            badAnnotation ));
        }
        return disposeFound;
    }
    
    private String checkInjectProducers(List<? extends AnnotationMirror> annotations) 
    {
        if (getModel().getHelper().hasAnnotation(annotations, 
                INJECT_ANNOTATION))
        {
            return INJECT_ANNOTATION;
        }
        if ( getModel().getHelper().hasAnnotation(annotations, 
                        PRODUCER_ANNOTATION))
        {
            return PRODUCER_ANNOTATION; 
        }
        return null;
    }

    private AtomicBoolean isDirty = new AtomicBoolean(true);
    private volatile boolean isIndexListenerAdded;
    private List<ElementHandle<? extends Element>> myNamedElement;
}
