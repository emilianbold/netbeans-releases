/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. Copyright 1997-2007
 * Sun Microsystems, Inc. All rights reserved. The contents of this file are
 * subject to the terms of either the GNU General Public License Version 2 only
 * ("GPL") or the Common Development and Distribution License("CDDL")
 * (collectively, the "License"). You may not use this file except in compliance
 * with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP.
 * See the License for the specific language governing permissions and
 * limitations under the License. When distributing the software, include this
 * License Header Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this particular file as
 * subject to the "Classpath" exception as provided by Sun in the GPL Version 2
 * section of the License file that accompanied this code. If applicable, add
 * the following below the License Header, with the fields enclosed by brackets
 * [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]" Contributor(s): The
 * Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc.
 * All Rights Reserved. If you wish your version of this file to be governed by
 * only the CDDL or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution under the
 * [CDDL or GPL Version 2] license." If you do not indicate a single choice of
 * license, a recipient has the option to distribute your version of this file
 * under either the CDDL, the GPL Version 2 or to extend the choice of license
 * to its licensees as provided above. However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright holder.
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.beans.api.model.AmbiguousDependencyException;
import org.netbeans.modules.web.beans.api.model.WebBeansModelException;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;

/**
 * @author ads
 */
abstract class FieldInjectionPointLogic {

    static final String PRODUCER_ANNOTATION = 
                    "javax.enterprise.inject.Produces";             // NOI18N

    static final String ANY_BINDING_ANNOTATION = 
                     "javax.enterprise.inject.Any";                 // NOI18N

    static final String CURRENT_BINDING_ANNOTATION = 
                     "javax.enterprise.inject.Current";             // NOI18N

    static final String NEW_BINDING_ANNOTATION = 
                      "javax.enterprise.inject.New";                // NOI18N

    static final Logger LOGGER = Logger.getLogger(WebBeansModelProvider.class
            .getName());
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#resolveType(java.lang.String, org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper)
     */
    public TypeMirror resolveType( String fqn , AnnotationModelHelper helper ) {
        return helper.resolveType( fqn );
    }
    
    protected Element findVariableInjectable( VariableElement element,
            WebBeansModelImplementation modelImpl ) throws WebBeansModelException
    {
        Set<Element> injectables = findVariableInjectable(element, 
                modelImpl, false);
        return getResult(injectables);
    }

    protected Element getResult( Set<Element> injectables )
            throws AmbiguousDependencyException
    {
        /*
         *  TODO : need to compare set before filtering and after.
         *  If after filtering there is only one element then 
         *  it could be safely return as result.
         *  Otherwise there is two cases:
         *  1) Empty set after filtering means unsatisfied dependency : 
         *  there was discovered elements (if original set was not empty ) 
         *  but they are not eligible for injection for various reasons
         * ( unproxyables, etc. ).
         * 2) Several elements set means ambiguous dependency.    
         */
        filterBeans( injectables );
        if ( injectables.size() ==1 ){
            return injectables.iterator().next();
        }
        else if ( injectables.size() == 0 ){
            return null;
        }
        else {
            throw new AmbiguousDependencyException( injectables );
        }
    }
    
    protected Set<Element> findVariableInjectable( VariableElement element,
            WebBeansModelImplementation modelImpl , boolean currentByDefault )
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
             * Throw exception if it initialized. 
             */
        }
        // producer is not injection point , it is injectable
        if ( isProducer || ( bindingAnnotations.size() == 0 && !anyBindingType 
                && !currentByDefault))
        {
            return null;
        }
        /*
         * Single @Current annotation means increasing types that 
         * is eligible for injection. Each bean without any binding
         * type has @Current binding type by default. So it should
         * be also considered as injectable.  
         */
        boolean currentBindingType = false;
        if ( currentByDefault && bindingAnnotations.size() == 0 ){
            currentBindingType = true;
        }
        /*
         * The @New target is 
         * @Target(value={FIELD,PARAMETER})
         * and injectable couldn't have any other bindings.
         * So @New should be the only binding type for injection point 
         * and it could be assigned by user to bean type.
         */
        boolean newBindingType = false; 
        String annotationName = null; 
        Set<Element> result = new HashSet<Element>();
        Set<TypeElement> types = new HashSet<TypeElement>();
        if ( bindingAnnotations.size() == 1 ){
            AnnotationMirror annotationMirror = bindingAnnotations.get( 0 );
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            annotationName = annotationElement.getQualifiedName().toString();
            currentBindingType = annotationElement.getQualifiedName().contentEquals( 
                    CURRENT_BINDING_ANNOTATION);
            newBindingType = annotationElement.getQualifiedName().contentEquals( 
                    NEW_BINDING_ANNOTATION );
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
                 *  It should be either absent at all or binding types 
                 *  should contain @Current.  
                 */
                filterBindingsByCurrent( assignableTypes, modelImpl );
            }
            types.addAll( assignableTypes );
        }
        else if (newBindingType){
            AnnotationMirror annotationMirror = bindingAnnotations.get( 0 );
            AnnotationParser parser = AnnotationParser.create( modelImpl.getHelper());
            parser.expectClass( "value", null);                         // NOI18N 
            ParseResult parseResult = parser.parse(annotationMirror);
            String clazz = parseResult.get( "value" , String.class );   // NOI18N 
            TypeMirror typeMirror;
            if ( clazz == null ){
                typeMirror = element.asType();
            }
            else {
                typeMirror = resolveType( clazz, modelImpl.getHelper());
            }
            Element typeElement = null;
            if ( typeMirror != null ) {
                typeElement = modelImpl.getHelper().getCompilationController().
                getTypes().asElement(typeMirror);
            }
            if ( typeElement!= null ){
                types.addAll(getImplementors(modelImpl, typeElement ));
            }
        }
        else {
            /*
             * This is list with types that have all required bindings. This
             * list will be used for further typesafe resolution.
             */
            Set<TypeElement> typesWithBindings = getBindingTypes(
                    bindingAnnotations, modelImpl);
            
            filterBindingsByMembers(bindingAnnotations, typesWithBindings,
                    modelImpl, TypeElement.class );
            /*
             * Now <code>typesWithBindings</code> contains appropriate types
             * which has required binding with required parameters ( if any ).
             * Next step is filter types via typesafe resolution.
             */
            filterBindingsByType( element , typesWithBindings , modelImpl );
            types.addAll( typesWithBindings );
        }
        
        DeploymentTypeFilter<TypeElement> filter = DeploymentTypeFilter.get( 
                TypeElement.class);
        filter.init( modelImpl );
        filter.filter( types );
        result.addAll( types );
        
        /*
         * This is list with production fields or methods ( they have @Produces annotation )
         * that  have all required bindings.
         * This list will be also used for further typesafe resolution. 
         */
        Set<Element> productionElements;
        if ( (bindingAnnotations.size() == 0 && anyBindingType) || 
                currentBindingType )
        {
            productionElements = getAllProductions( modelImpl);
            if ( currentBindingType ){
                filterCurrentProductions( productionElements , modelImpl);
            }
        }
        else {
            productionElements = getProductions( bindingAnnotations, 
                    modelImpl); 
            filterBindingsByMembers( bindingAnnotations , productionElements , 
                    modelImpl , Element.class );
        }
        filterProductionByType( element, productionElements, modelImpl );
        
        DeploymentTypeFilter<Element> deploymentFilter = DeploymentTypeFilter.get( 
                Element.class);
        deploymentFilter.init( modelImpl );
        deploymentFilter.filter( productionElements );
        
        result.addAll( productionElements );
        return result;
    }
    
    protected boolean isBinding( TypeElement element, 
            AnnotationModelHelper helper)
    {
        BindingChecker checker = BindingChecker.get();
        checker.init( element , helper );
        return checker.check();
    }
    
    private Set<Element> getChildSpecializes( Element productionElement,
            WebBeansModelImplementation model )
    {
        TypeElement typeElement = model.getHelper().getCompilationController()
                .getElementUtilities().enclosingTypeElement(productionElement);
        Set<TypeElement> implementors = getImplementors(model, typeElement);
        implementors.remove( productionElement.getEnclosingElement());
        Set<Element> specializeElements = new HashSet<Element>();
        specializeElements.add(productionElement);
        for (TypeElement implementor : implementors) {
            inspectHierarchy(productionElement, implementor,
                    specializeElements, model);
        }
        specializeElements.remove(productionElement);
        return specializeElements;
    }
    
    private void inspectHierarchy( Element productionElement,
            TypeElement implementor, Set<Element> specializeElements ,
            WebBeansModelImplementation model )
    {
        List<? extends Element> enclosedElements = implementor.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if ( enclosedElement.getKind() != ElementKind.METHOD) {
                continue;
            }
            if ( !productionElement.getSimpleName().contentEquals(
                    enclosedElement.getSimpleName()))
            {
                continue;
            }
            Set<Element> probableSpecializes = new HashSet<Element>();
            if ( collectSpecializes( productionElement ,
                    (ExecutableElement)enclosedElement , model ,
                    probableSpecializes , specializeElements))
            {
                // for one method there could be just one override method in considered class
                specializeElements.addAll( probableSpecializes );
                return;
            }
        }
    }
    
    private boolean collectSpecializes( Element productionElement,
            ExecutableElement element, WebBeansModelImplementation model,
            Set<Element> probableSpecializes, Set<Element> specializeElements )
    {
        ElementUtilities elementUtilities =
            model.getHelper().getCompilationController().getElementUtilities();
        if ( !elementUtilities.overridesMethod(element)){
            return false;
        }
        ExecutableElement overriddenMethod = elementUtilities.
            getOverriddenMethod( element);
        if ( overriddenMethod == null ){
            return false;
        }
        if (!AnnotationObjectProvider.hasSpecializes(element,  model.getHelper())){
            return false;
        }
        probableSpecializes.add( element);
        if( overriddenMethod.equals( productionElement ) ||
                specializeElements.contains( productionElement))
        {
            return true;
        }
        else {
            return collectSpecializes(productionElement, overriddenMethod, model,
                    probableSpecializes, specializeElements);
        }
    }

    private Set<TypeElement> doGetImplementors( WebBeansModelImplementation modelImpl,
            TypeElement typeElement )
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
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
        return result;
    }
    
    private void filterCurrentProductions( Set<Element> productionElements , 
            WebBeansModelImplementation model) 
    {
        CurrentBindingTypeFilter<Element> filter = CurrentBindingTypeFilter.get( 
                Element.class);
        filter.init( model );
        filter.filter( productionElements );
    }

    private Set<Element> getAllProductions(WebBeansModelImplementation model )
    {
        final Set<Element> result = new HashSet<Element>();
        try {
            model.getHelper().getAnnotationScanner().findAnnotations( 
                    PRODUCER_ANNOTATION, 
                    EnumSet.of( ElementKind.FIELD, ElementKind.METHOD), 
                    new AnnotationHandler() {
                        public void handleAnnotation( TypeElement type, 
                                Element element,AnnotationMirror annotation )
                        {
                                result.add( element );
                        }
                    });
        }
        catch (InterruptedException e) {
            LOGGER.warning("Finding annotation "+PRODUCER_ANNOTATION+
                    " was interrupted"); // NOI18N
        }
        return result;
    }

    private void filterProductionByType( VariableElement element,
            Set<Element> productionElements,
            WebBeansModelImplementation model )
    {
        TypeProductionFilter filter = TypeProductionFilter.get( );
        filter.init(element, model);
        filter.filter( productionElements );
    }
    
    private void filterBindingsByCurrent( Set<TypeElement> assignableTypes,
            WebBeansModelImplementation modelImpl )
    {
        CurrentBindingTypeFilter<TypeElement> filter = CurrentBindingTypeFilter.get( 
                TypeElement.class);
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
        if (((TypeElement) typeElement).getTypeParameters().size() != 0) {
            return getAssignables( modelImpl, (TypeElement)typeElement, element );
        }
        else {
            return getImplementors(modelImpl, typeElement);
        }
    }
    
    private Set<TypeElement> getAssignables( WebBeansModelImplementation model , 
            TypeElement typeElement  , VariableElement element) 
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        
        CompilationController controller = model.getHelper().getCompilationController();
        ElementHandle<TypeElement> searchedTypeHandle = ElementHandle.create(typeElement);
        final Set<ElementHandle<TypeElement>> elementHandles = model.getHelper().
            getClasspathInfo().getClassIndex().getElements(
                searchedTypeHandle,
                EnumSet.of(SearchKind.TYPE_REFERENCES),
                EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        if (elementHandles == null) {
            LOGGER.warning("ClassIndex.getElements() was interrupted"); // NOI18N
            return result;
        }
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            LOGGER.log(Level.FINE, "found element {0}", 
                    elementHandle.getQualifiedName()); // NOI18N
            TypeElement found = elementHandle.resolve(controller);
            if (typeElement == null) {
                continue;
            }
            // collect all references , further there will be assignability filtering 
            result.add( found );
        }
        TypeBindingFilter filter = TypeBindingFilter.get();
        filter.init( element , model );
        filter.filter( result );
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
    
    private Set<TypeElement> getImplementors( WebBeansModelImplementation modelImpl,
            Element typeElement )
    {
        if (! (typeElement instanceof TypeElement )){
            return Collections.emptySet();
        }
        Set<TypeElement> result = new HashSet<TypeElement>();
        result.add( (TypeElement) typeElement );
        
        Set<TypeElement> toProcess = new HashSet<TypeElement>();
        toProcess.add((TypeElement) typeElement );
        while ( toProcess.size() >0 ){
            TypeElement element = toProcess.iterator().next();
            toProcess.remove( element );
            Set<TypeElement> set = doGetImplementors(modelImpl, element );
            if ( set.size() == 0 ){
                continue;
            }
            result.addAll( set );
            for (TypeElement impl : set) {
                toProcess.add(impl);
            }
        }
        return result;
    }

    private Set<Element> getProductions( 
            List<AnnotationMirror> bindingAnnotations ,
            final WebBeansModelImplementation model ) 
    {
        List<Set<Element>> bindingCollections = 
            new ArrayList<Set<Element>>( bindingAnnotations.size());
        /*
         * One need to handle special case with @Current annotation 
         * in case of specialization. There can be a case 
         * when production method doesn't explicitly declare @Current but 
         * specialize other method with several appropriate binding types.
         * In this case original method will have @Current along with 
         * binding types "inherited" from specialized methods.  
         */
        boolean hasCurrent = model.getHelper().getAnnotationsByType( bindingAnnotations ).
                get(CURRENT_BINDING_ANNOTATION) != null ;
        Set<Element> currentBindings = new HashSet<Element>();
        for (AnnotationMirror annotationMirror : bindingAnnotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            String annotationFQN = annotationElement.getQualifiedName().toString();
            findAnnotation(model, bindingCollections, annotationFQN , hasCurrent,
                    currentBindings );
        }

        if ( hasCurrent ){
            bindingCollections.add( currentBindings );
        }
        
        Set<Element> result= null;
        for ( int i=0; i<bindingCollections.size() ; i++ ){
            Set<Element> list = bindingCollections.get(i);
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
        return result;
    }

    private void findAnnotation( final WebBeansModelImplementation model,
            final List<Set<Element>> bindingCollections, final String annotationFQN ,
            final boolean hasCurrent , final Set<Element> currentBindings )
    {
        try {
            final Set<Element> bindings = new HashSet<Element>();
            model.getHelper().getAnnotationScanner().findAnnotations( 
                    annotationFQN, 
                    EnumSet.of( ElementKind.FIELD, ElementKind.METHOD), 
                    new AnnotationHandler() {
                        public void handleAnnotation( TypeElement type, 
                                Element element,AnnotationMirror annotation )
                        {
                            if ( AnnotationObjectProvider.hasAnnotation(element, 
                                    PRODUCER_ANNOTATION, model.getHelper()))
                            {
                                bindings.add( element );
                                bindings.addAll(getChildSpecializes( element , 
                                        model ));
                                if ( annotationFQN.contentEquals( 
                                        CURRENT_BINDING_ANNOTATION )){
                                    currentBindings.addAll( bindings );
                                }
                                else {
                                    bindingCollections.add( bindings );
                                }
                            }
                        }
                    });
            if ( hasCurrent ){
                for (Element element : bindings) {
                    if ( AnnotationObjectProvider.checkCurrent(
                            element, model.getHelper()))
                    {
                        currentBindings.add( element );
                    }
                }
            }
        }
        catch (InterruptedException e) {
            LOGGER.warning("Finding annotation "+annotationFQN+
                    " was interrupted"); // NOI18N
        }
    }

    private Set<TypeElement> getBindingTypes( List<AnnotationMirror> bindingAnnotations ,
            WebBeansModelImplementation modelImpl )
    {
        List<Set<Binding>> bindingCollections = 
            new ArrayList<Set<Binding>>( bindingAnnotations.size());

        /*
         * One need to handle special case with @Current annotation 
         * in case of specialization. There can be a case 
         * when bean doesn't explicitly declare @Current but 
         * specialize other beans with several appropriate binding types.
         * In this case original bean will have @Current along with 
         * binding types "inherited" from specialized beans.  
         */
        boolean hasCurrent = modelImpl.getHelper().getAnnotationsByType( bindingAnnotations ).
                get(CURRENT_BINDING_ANNOTATION) != null ;
        Set<Binding> currentBindings = new HashSet<Binding>();
        for (AnnotationMirror annotationMirror : bindingAnnotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            String annotationFQN = annotationElement.getQualifiedName().toString();
            PersistentObjectManager<Binding> manager = modelImpl.getManager( 
                    annotationFQN );
            Collection<Binding> bindings = manager.getObjects();
            if ( annotationFQN.contentEquals( CURRENT_BINDING_ANNOTATION )){
                currentBindings.addAll( bindings );
            }
            else {
                bindingCollections.add( new HashSet<Binding>( bindings) );
                if ( hasCurrent ){
                    for (Binding binding : bindings) {
                        if ( AnnotationObjectProvider.checkCurrent(
                                binding.getTypeElement(), modelImpl.getHelper()))
                        {
                            currentBindings.add( new Binding( 
                                    modelImpl.getHelper(), binding.getTypeElement(), 
                                    CURRENT_BINDING_ANNOTATION));
                        }
                    }
                }
            }
        }
        
        if ( hasCurrent ){
            bindingCollections.add( currentBindings );
        }
        
        Set<Binding> result= null;
        for ( int i=0; i<bindingCollections.size() ; i++ ){
            Set<Binding> set = bindingCollections.get(i);
            if ( i==0 ){
                result = set;
            }
            else {
                result.retainAll( set );
            }
        }
        if ( result == null ){
            return Collections.emptySet();
        }
        else {
            Set<TypeElement> set = new HashSet<TypeElement>();
            for (Binding binding : result) {
                set.add( binding.getTypeElement() );
            }
            return set;
        }
    }

    private <T extends Element> void filterBindingsByMembers(
            List<AnnotationMirror> bindingAnnotations,
            Set<T> elementsWithBindings, 
            WebBeansModelImplementation impl , Class<T> clazz)
    {
        MemberBindingFilter<T> filter = MemberBindingFilter.get( clazz );
        filter.init( bindingAnnotations, impl );
        filter.filter( elementsWithBindings );
    }
    
    private void filterBeans( Set<Element> result ) {
        BeansFilter filter = BeansFilter.get();
        filter.filter( result );
    }
}
