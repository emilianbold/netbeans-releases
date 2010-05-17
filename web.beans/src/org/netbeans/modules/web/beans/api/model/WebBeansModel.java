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
package org.netbeans.modules.web.beans.api.model;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;


/**
 * @author ads
 *
 */
public final class WebBeansModel {
    
    WebBeansModel( AbstractModelImplementation impl ){
        myImpl = impl;
    }

    /**
     * Find injectable element that is used for given injection point.
     * 
     * <code>parentType</code> parameter could be a null . In this case 
     * type definition which contains <code>element</code> is used as parentType.  
     * This parameter is needed when <code>element</code> is defined in 
     * superclass and this superclass is generic. In this case <code>element</code>
     * type ( TypeMirror ) could vary respectively subclass definition ( it could uses
     * real class in generic type parameter ). Type of element in this case
     * is not just <code>element.asType()<code>. It is 
     * <code>CompilationInfo.getTypes().asMemberOf(parentType,element)<code>.
     * This is significant difference.  
     *    
     * @param element injection point
     * @param parentType parent type of <code>element</code>
     * @return search result information
     */
    public Result getInjectable( VariableElement element , DeclaredType parentType ) 
    {
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().getInjectable(element, parentType, 
                getModelImplementation());
    }
    
    /**
     * Find injectable elements that could be used for given injection point.
     * This method differs from {@link #getInjectable(VariableElement, DeclaredType)}
     * by injection point type. Injection point could be defined via 
     * programmatic lookup which is dynamically specify injectable type.
     * Such situation appears when injection point uses Instance<?> interface. 
     * In case of @Any binding usage this list will contain all 
     * possible binding types for <code>element</code>  ( all beans 
     * that implements or extends type parameter for Instance<> ). 
     * 
     * See <code>parentType</code> parameter explanation in 
     * {@link #getInjectable(VariableElement, DeclaredType)}.
     * 
     * @param element injection point
     * @param parentType parent type of <code>element</code>
     * @return search result information
     */
    public Result lookupInjectables( VariableElement element , 
            DeclaredType parentType)
    {
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().lookupInjectables(element, parentType,
                getModelImplementation());
    }
    
    /**
     * Test if variable element is injection point.
     * <pre> 
     * Two cases possible here:
     * - element has @Inject annotation
     * - element is parameter of method which is annotated with @Inject 
     * </pre> 
     * 
     * @param element element for check
     * @return true if element is simple injection point
     * @throws WebBeansModelException if <code>element</code> could be injection 
     * point but something wrong ( f.e. it has bindings and has no @Produces 
     * annotation bit it is initialized ).
     * @throws InjectionPointDefinitionError if element definition contains error   
     */
    public boolean isInjectionPoint( VariableElement element )  
        throws InjectionPointDefinitionError
    {
        if ( getProvider() == null ){
            return false;
        }
        return getProvider().isInjectionPoint(element, getModelImplementation());
    }
    
    /**
     * Test if variable element is injection point that is used for
     * programmatic lookup. It could happen if variable declared via 
     * Instance<?> interface with binding annotations.
     * Typesafe resolution in this case could not be done 
     * statically and method 
     * {@link #lookupInjectables(VariableElement, DeclaredType)} should
     * be used to access to possible bean types.
     * @param element  element for check
     * @return true if element is dynamic injection point
     */
    public boolean isDynamicInjectionPoint( VariableElement element ) 
    {
        if ( getProvider() == null ){
            return false;
        }
        return getProvider().isDynamicInjectionPoint(element, 
                getModelImplementation());
    }
    
    /**
     * Access to @Named elements. Method {@link #getName(Element)} 
     * should be used for getting name of element. 
     * @return list of elements annotated with @Named
     */
    public List<Element> getNamedElements(){
        if ( getProvider() == null ){
            return Collections.emptyList();
        }
        return getProvider().getNamedElements( getModelImplementation() );
    }
    
    /**
     * Returns name of element if it annotated with @Named.
     * Otherwise returns null.
     * @param element @Named element
     * @return name of element
     */
    public String getName( Element element ){
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().getName( element, getModelImplementation() );
    }
    
    /**
     * This method is used for resolve name to Java model type.
     * One can resolve enclosed elements ( fields , methods ,....  )
     * via Java model API and reference which method returns.  
     * @param fqn fully qualified name of type 
     * @return type with given FQN <code>fqn</code>
     */
    public TypeMirror resolveType(String fqn){
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().resolveType(fqn, getModelImplementation().getHelper());
    }
    
    public CompilationController getCompilationController(){
        return getModelImplementation().getHelper().getCompilationController();
    }
    
    /**
     * Returns all qualifiers for <code>element</code>.
     * <code>element</code> could be variable ( injection point , producer field ),
     * type element ( bean type with binding ) and production method. 
     * @param element element with bindings
     * @return list of all bindings for <code>element</code>
     */
    public List<AnnotationMirror> getQualifiers( Element element ){
        return getProvider().getQualifiers( element , getModelImplementation());
    }
    
    public AbstractModelImplementation getModelImplementation(){
        return myImpl;
    }
    
    private WebBeansModelProvider getProvider(){
        return getModelImplementation().getProvider();
    }
    
    private AbstractModelImplementation myImpl;
}
