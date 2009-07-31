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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;


/**
 * @author ads
 *
 */
class DeploymentTypeFilter<T extends Element> extends Filter<T> {
    
    static <T extends Element> DeploymentTypeFilter<T> get(Class<T> clazz )
    {
        assertElement(clazz);
        if ( clazz.equals(TypeElement.class )){
            return (DeploymentTypeFilter<T>) 
                new DeploymentTypeFilter<TypeElement>( TypeElement.class );
        }
        else if ( clazz.equals(Element.class )){
            return (DeploymentTypeFilter<T>) 
            new DeploymentTypeFilter<Element>( Element.class );
        }
        return null;
    }
    
    private DeploymentTypeFilter( Class<T> clazz ){
        myClass = clazz;
    }
    
    void init( WebBeansModelImplementation model ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.Filter#filter(java.util.Set)
     */
    @Override
    void filter( Set<T> set ) {
        super.filter(set);
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myModel;
    }

    static AnnotationMirror getDeploymentType( Element element,
            WebBeansModelImplementation model )
    {
        // Ask ONLY annotations for element itself. Not inherited.
        List<? extends AnnotationMirror> annotationMirrors = 
            element.getAnnotationMirrors();
        Set<AnnotationMirror> deploymentTypes = new HashSet<AnnotationMirror>();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if ( isDeploymentType(annotationMirror)){
                deploymentTypes.add( annotationMirror );
            }
            else if ( isStereotype(annotationMirror)){
                AnnotationMirror deploymentType = getDeploymentType( 
                        annotationMirror );
                if ( deploymentType != null ){
                    deploymentTypes.add( deploymentType );
                }
            }
        }
        if ( deploymentTypes.size() == 0 && ( element instanceof TypeElement )){
            TypeElement superclass = model.getHelper().
                getSuperclass((TypeElement)element);
            if ( superclass!= null ){
                return getDeploymentType( superclass , model );
            }
        }
        else if ( deploymentTypes.size() > 1 ){
            // TODO : throws exception 
        }
        return deploymentTypes.iterator().next();
    }
    
    static AnnotationMirror getDeploymentType( AnnotationMirror stereotype ){
        return null;
    }
    
    static boolean isDeploymentType( AnnotationMirror annotation){
        return false;
    }
    
    static boolean isStereotype( AnnotationMirror annotation ){
        return false;
    }
    
    private Class<T> myClass;
    private WebBeansModelImplementation myModel;

}
