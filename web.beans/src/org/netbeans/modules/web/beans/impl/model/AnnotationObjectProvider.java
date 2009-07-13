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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;


/**
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
                        result.add( new Binding( getHelper(), type , 
                                getAnnotationName()));
                        set.add( type );
                        if ( !getHelper().hasAnnotation( annotation.
                                getAnnotationType().asElement().
                                getAnnotationMirrors(), 
                                Inherited.class.getCanonicalName()))
                        {
                            /*
                             *  if annotation is inherited then findAnnotations
                             *  method will return types with this annotation.
                             *  Otherwise there could be implementors which 
                             *  specialize this type.
                             */
                            collectImplementors( type , set, result );
                        }
                    }

        }, true );
        return new ArrayList<Binding>( result );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
     */
    public List<Binding> createObjects( TypeElement type ) {
        final List<Binding> result = new ArrayList<Binding>();
        /* TODO :  check presence @Specializes annotation. In this case
         * one need to investigate parents of Type for annotation presence.  
         */
        if (getHelper().hasAnnotation(getHelper().getCompilationController().
                getElements().getAllAnnotationMirrors( type ), 
                getAnnotationName())) 
        {
            result.add( new Binding(getHelper(), type, getAnnotationName()));
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
    
    private String getAnnotationName(){
        return myAnnotationName;
    }
    
    private AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private void collectImplementors( TypeElement type, Set<TypeElement> set, 
            List<Binding> bindings ) 
    {
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
            return ;
        }
        for (ElementHandle<TypeElement> elementHandle : handles) {
            LOGGER.log(Level.FINE, "found derived element {0}", elementHandle
                    .getQualifiedName()); // NOI18N
            TypeElement derivedElement = elementHandle.resolve(getHelper().
                    getCompilationController());
            if (derivedElement == null) {
                continue;
            }
            
            List<? extends AnnotationMirror> allAnnotationMirrors = 
                getHelper().getCompilationController().getElements().
                getAllAnnotationMirrors(derivedElement);
            if ( getHelper().hasAnnotation( allAnnotationMirrors, 
                    SPECILIZES_ANNOTATION))
            {
                continue;
            }
            
            List<? extends TypeMirror> directSupertypes = getHelper().
                    getCompilationController().getTypes().directSupertypes( 
                    derivedElement.asType());
            boolean directParent = false;
            for (TypeMirror typeMirror : directSupertypes) {
                if ( getHelper().getCompilationController().getTypes().
                        isSameType( typeMirror, type.asType()))
                {
                    directParent = true;
                }
            }
            if ( directParent && !set.contains( derivedElement )){
                bindings.add( new Binding(getHelper(), derivedElement, 
                        getAnnotationName()));
                set.add( derivedElement );
            }
            collectImplementors(derivedElement, set, bindings);
        }
        
    }
    
    private AnnotationModelHelper myHelper;
    private String myAnnotationName;

}
