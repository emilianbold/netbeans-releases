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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;


/**
 * @author ads
 *
 */
class ObjectProviders {

    static final class ComponentProvider extends AbstractProvider<ComponentImpl> 
        implements ObjectProvider<ComponentImpl> 
    {

        ComponentProvider( AnnotationModelHelper helper )
        {
            super(helper, "javax.faces.component.FacesComponent");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ComponentImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ComponentImpl( helper , typeElement );
        }

    }
    
    static final class BehaviorProvider extends AbstractProvider<BehaviorImpl> {

        BehaviorProvider( AnnotationModelHelper helper ) {
            super( helper , "javax.faces.component.behavior.FacesBehavior");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        BehaviorImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new BehaviorImpl( helper , typeElement );
        }
        
    }
    
    static final class ConverterProvider extends AbstractProvider<ConverterImpl>
        implements ObjectProvider<ConverterImpl>
    {

        ConverterProvider( AnnotationModelHelper helper )
        {
            super(helper, "javax.faces.convert.FacesConverter");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ConverterImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ConverterImpl( helper , typeElement );
        }

        
    }
    
    static final class ManagedBeanProvider extends AbstractProvider<ManagedBeanImpl>
        implements ObjectProvider<ManagedBeanImpl>
    {

        ManagedBeanProvider( AnnotationModelHelper helper)
        {
            super(helper, "javax.faces.bean.ManagedBean");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ManagedBeanImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ManagedBeanImpl( helper , typeElement );
        }
        
        public boolean modifyObjects(TypeElement type, List<ManagedBeanImpl> objects) {
            boolean isModified = false;
            for( Iterator<ManagedBeanImpl> iterator = objects.iterator() ; iterator.hasNext();  ){
                ManagedBeanImpl object = iterator.next();
                if (!object.refresh(type)) {
                    iterator.remove();
                    isModified = true;
                }
                else {
                    if ( object.scopeChanged() || object.propertyChanged() ){
                        isModified = true;
                    }
                }
            }
            return isModified;
        }

    }
    
    static final class ValidatorProvider extends AbstractProvider<ValidatorImpl> 
        implements ObjectProvider<ValidatorImpl>
    {

        ValidatorProvider( AnnotationModelHelper helper )
        {
            super(helper, "javax.faces.validator.FacesValidator");
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.impl.metamodel.ObjectProviders.AbstractProvider#createObject(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper, javax.lang.model.element.TypeElement)
         */
        @Override
        ValidatorImpl createObject( AnnotationModelHelper helper,
                TypeElement typeElement )
        {
            return new ValidatorImpl( helper , typeElement );
        }

    }
    
    private static abstract class AbstractProvider<T extends Refreshable> 
        implements ObjectProvider<T>
    {
        AbstractProvider(AnnotationModelHelper helper, String annotationName) {
            myAnnotationName = annotationName;
            myHelper = helper;
        }

        public List<T> createInitialObjects() throws InterruptedException {
            final List<T> result = new ArrayList<T>();
            getHelper().getAnnotationScanner().findAnnotations(getAnnotationName(), 
                    EnumSet.of( ElementKind.CLASS ), 
                    new AnnotationHandler() {
                        public void handleAnnotation(TypeElement type, 
                                Element element, AnnotationMirror annotation) 
                        {
                                result.add(createObject(getHelper(), type));
                        }
            });
            return result;
        }

        public List<T> createObjects(TypeElement type) {
            final List<T> result = new ArrayList<T>();
            if (getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                    getAnnotationName())) 
            {
                result.add(createObject(getHelper(), type));
            }
            return result;
        }

        public boolean modifyObjects(TypeElement type, List<T> objects) {
            boolean isModified = false;
            for( Iterator<T> iterator = objects.iterator() ; iterator.hasNext();  ){
                T object = iterator.next();
                if (!object.refresh(type)) {
                    iterator.remove();
                    isModified = true;
                }
            }
            return isModified;
        }

        abstract T createObject(AnnotationModelHelper helper, TypeElement typeElement);
        
        private AnnotationModelHelper getHelper(){
            return myHelper;
        }
        
        private String getAnnotationName(){
            return myAnnotationName;
        }
        
        private String myAnnotationName;
        private AnnotationModelHelper myHelper;
    }
}
