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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
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
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getName(javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    @Override
    public String getName( Element element,
            AbstractModelImplementation modelImpl )
    {
        WebBeansModelImplementation impl = getImplementation( modelImpl);
        if ( impl == null ){
            return null;
        }
        String name = inspectSpecializes( element , impl );
        if ( name != null ){
            return name;
        }
        List<AnnotationMirror> allStereotypes = getAllStereotypes(element, 
                impl.getHelper());
        for (AnnotationMirror annotationMirror : allStereotypes) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            TypeElement annotation = (TypeElement)annotationType.asElement();
            if ( AnnotationObjectProvider.hasAnnotation(annotation, 
                    NAMED_QUALIFIER_ANNOTATION, impl.getHelper() ) )
            {
                return getNamedName(element , null, impl.getHelper());
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getNamedElements(org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    @Override
    public List<Element> getNamedElements( AbstractModelImplementation modelImpl ) {
        WebBeansModelImplementation impl = getImplementation( modelImpl);
        if ( impl == null ){
            return Collections.emptyList();
        }
        List<Element> result = new LinkedList<Element>();
        Collection<BindingQualifier> objects = impl.getNamedManager().getObjects();
        for (BindingQualifier named : objects) {
            result.add( named.getTypeElement() );
        }
        List<Element> members = AbstractObjectProvider.getNamedMembers( 
                impl.getHelper() );
        for (Element element : members) {
            if ( element.getKind()!= ElementKind.METHOD ){
                continue;
            }
            Set<Element> childSpecializes = getChildSpecializes( element, impl );
            result.addAll( childSpecializes );
        }
        result.addAll( members );
        
        Set<String> stereotypeNames = impl.adjustStereotypesManagers();
        for (String stereotype : stereotypeNames) {
            PersistentObjectManager<StereotypedObject> manager = 
                impl.getStereotypedManager(stereotype);
            Collection<StereotypedObject> beans = manager.getObjects();
            for (StereotypedObject bean : beans) {
                result.add( bean.getTypeElement() );
            }
            List<Element> stereotypedMembers = StereotypedObjectProvider.
                getAnnotatedMembers( stereotype, impl.getHelper());
            result.addAll( stereotypedMembers );
        }
        
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
    
    private String inspectSpecializes( Element element,
            WebBeansModelImplementation impl )
    {
        if (element instanceof TypeElement) {
            String name = doGetName(element, element, impl);
            if ( name != null ){
                return name;
            }
            TypeElement superElement = AnnotationObjectProvider.checkSuper(
                    (TypeElement)element, NAMED_QUALIFIER_ANNOTATION, 
                    impl.getHelper());
            if ( superElement != null ){
                return doGetName(element, superElement, impl);
            }
        }
        else if ( element instanceof ExecutableElement ){
            String name = doGetName(element, element, impl);
            if ( name == null ){
                Element specialized = MemberCheckerFilter.getSpecialized( element, 
                        impl, NAMED_QUALIFIER_ANNOTATION);
                if ( specialized!= null ){
                    return doGetName(element , specialized, impl);
                }
            }
            else {
                return name;
            }
        }
        else {
            return doGetName(element, element, impl);
        }
        return null;
    }
    
    private String doGetName( Element original , Element element, 
            WebBeansModelImplementation impl )
    {
        List<? extends AnnotationMirror> annotations = impl.getHelper().
            getCompilationController().getElements().getAllAnnotationMirrors( 
                element);
        for (AnnotationMirror annotationMirror : annotations) {
        DeclaredType type = annotationMirror.getAnnotationType();
        TypeElement annotationElement = (TypeElement)type.asElement();
            if ( NAMED_QUALIFIER_ANNOTATION.contentEquals( 
                    annotationElement.getQualifiedName()))
            {
                return getNamedName( original , annotationMirror ,
                        impl.getHelper());
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
    
    private String getNamedName( Element element, AnnotationMirror namedAnnotation,
            AnnotationModelHelper helper )
    {
        if (namedAnnotation != null) {
            AnnotationParser parser = AnnotationParser.create(helper);
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
