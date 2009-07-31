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

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.modules.web.beans.api.model.AbstractModelImplementation;
import org.netbeans.modules.web.beans.api.model.WebBeansModelException;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=WebBeansModelProvider.class)
public class WebBeansModelProviderImpl extends ParameterInjectionPointLogic 
    implements WebBeansModelProvider 
{
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getInjectable(javax.lang.model.element.VariableElement, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    public Element getInjectable( VariableElement element , 
            AbstractModelImplementation impl) throws WebBeansModelException
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
            return findVariableInjectable(element, modelImpl);
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
            return findParameterInjectable(element, modelImpl );
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getInjectables(javax.lang.model.element.VariableElement, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    public List<Element> getInjectables( VariableElement element ,
            AbstractModelImplementation impl ) 
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#isDynamicInjectionPoint(javax.lang.model.element.VariableElement)
     */
    public boolean isDynamicInjectionPoint( VariableElement element , 
            AbstractModelImplementation impl) throws WebBeansModelException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#isInjectionPoint(javax.lang.model.element.VariableElement)
     */
    public boolean isInjectionPoint( VariableElement element , 
            AbstractModelImplementation modelImpl) throws WebBeansModelException 
    {
        WebBeansModelImplementation impl = getImplementation( modelImpl);
        if ( impl == null ){
            return false;
        }
        List<? extends AnnotationMirror> annotations = 
            impl.getHelper().getCompilationController().getElements().
            getAllAnnotationMirrors(element);
        boolean hasBinding = false;
        boolean hasProduces = false;
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( isBinding( annotationElement , impl.getHelper()) ){
                hasBinding = true;
            }
            if ( PRODUCER_ANNOTATION.equals( annotationElement.getQualifiedName())){
                hasProduces = true;
            }
        }
        
        // TODO : check initialization of field
        return hasBinding && !hasProduces;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getBindings(javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    public List<AnnotationMirror> getBindings( Element element ,
        AbstractModelImplementation iml )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#getDeploymentType(javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.AbstractModelImplementation)
     */
    public AnnotationMirror getDeploymentType( Element element , 
            AbstractModelImplementation impl ) throws WebBeansModelException
    {
        WebBeansModelImplementation model = getImplementation( impl );
        if ( impl == null ){
            return null;
        }
        return DeploymentTypeFilter.getDeploymentType(element , model );
    }
    
    private WebBeansModelImplementation getImplementation(
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
}
