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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.web.beans.api.model.AbstractModelImplementation;
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
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getInjectable(javax.lang.model.element.VariableElement, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    public Element getInjectable( VariableElement element , 
            AbstractModelImplementation impl) 
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

    private Element findParameterInjectable( VariableElement element ) {
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
            element.getAnnotationMirrors();
        List<AnnotationMirror> bindingAnnotations = new LinkedList<AnnotationMirror>();
        boolean isProducer = false;
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( isBinding( annotationElement , modelImpl.getHelper()) ){
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
        if ( isProducer ){
            return null;
        }
        /*
         * This is list with types that have all required bindings.
         * This list will be used for further typesafe resolution. 
         */
        List<TypeElement> typesWithBindings = getBindingTypes( bindingAnnotations , 
                modelImpl );
        /*
         * This is list with production fields or methods ( they have @Produces annotation )
         * that  have all required bindings.
         * This list will be also used for further typesafe resolution. 
         */
        List<Element> productionElements = getProductions( bindingAnnotations, 
                modelImpl); 
        return null;
    }

    private List<Element> getProductions( List<AnnotationMirror> bindings ,
            WebBeansModelImplementation modelImpl ) 
    {
        // TODO Auto-generated method stub
        return null;
    }

    private List<TypeElement> getBindingTypes( List<AnnotationMirror> bindingAnnotations ,
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
    
    private boolean isBinding( TypeElement element, AnnotationModelHelper helper)
    {
        if ( BUILT_IN_BINDINGS.contains( element.getQualifiedName())){
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
    
    private static final Set<String> BUILT_IN_BINDINGS = new HashSet<String>();
    static {
        BUILT_IN_BINDINGS.add("javax.enterprise.inject.Any");
        BUILT_IN_BINDINGS.add("javax.enterprise.inject.New");
        BUILT_IN_BINDINGS.add("javax.enterprise.inject.Current");
    }

}
