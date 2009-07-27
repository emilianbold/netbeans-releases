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
package org.netbeans.modules.web.beans.api.model;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;
import org.openide.util.Lookup;


/**
 * @author ads
 *
 */
public final class WebBeansModel {
    
    WebBeansModel( AbstractModelImplementation impl ){
        myImpl = impl;
    }

    /**
     * Find injectable type that is used for given injection point.
     * <code>null</code> is returned if there is no eligible element for injection
     * ( no element which could be a pretender).
     * Exception could be thrown if there are unsatisfied or ambiguous dependency.
     * F.e. unsatisfied dependency : there is a pretender satisfy typesafe 
     * resolution but something incorrect ( parameterized type is not valid , etc. ). 
     * Ambiguous dependency : there are a number of appropriate elements.
     * In this case various subclasses of WebBeansModelException will be thrown.   
     * @param element injection point
     * @return type that is used in injected point identified by <code>element</code>
     * @throws exception if there are problems with injectable identifying  
     */
    public Element getInjectable( VariableElement element ) 
        throws WebBeansModelException 
    {
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().getInjectable(element, getModelImplementation());
    }
    
    /**
     * Find injectable types that could be used for given injection point.
     * This method differs from {@link #getInjectable(VariableElement)}
     * by injection point type. Injection point could be defined via 
     * programmatic lookup which is dynamically specify injectable type.
     * Such situation appears when injection point uses Instance<?> interface. 
     * In case of @Any binding usage this list will contain all 
     * possible binding types for <code>element</code>  ( all beans 
     * that implements or extends type parameter for Instance<> ). 
     * @param element injection point
     * @return types that is used in injected point identified by <code>element</code>
     */
    public List<Element> getInjectables( VariableElement element ){
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().getInjectables(element, getModelImplementation());
    }
    
    /**
     * Test if variable element is injection point. 
     * It means that it has some bean type as type and binding annotations.
     * It differs from {@link #isDynamicInjectionPoint(VariableElement)}.
     * In the latter method injection point is used for programmatic 
     * lookup. Refer to javadoc of this method.
     * @param element element for check
     * @return true if element is simple injection point
     * @throws WebBeansModelException if <code>element</code> could be injection 
     * point but something wrong ( f.e. it has bindings and has no @Produces 
     * annotation bit it is initialized ).   
     */
    public boolean isInjectionPoint( VariableElement element ) 
        throws WebBeansModelException 
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
     * statically and method {@link #getInjectables(VariableElement)} should
     * be used to access to possible bean types.
     * @param element  element for check
     * @return true if element is dynamic injection point
     * @throws WebBeansModelException if <code>element</code> could be injection 
     * point but something wrong ( f.e. it has bindings and has no @Produces 
     * annotation bit it is initialized ). 
     */
    public boolean isDynamicInjectionPoint( VariableElement element ) 
        throws WebBeansModelException 
    {
        if ( getProvider() == null ){
            return false;
        }
        return getProvider().isDynamicInjectionPoint(element, 
                getModelImplementation());
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
     * Returns all bindings for <code>element</code>.
     * <code>element</code> could be variable ( injection point , producer field ),
     * type element ( bean type with binding ) and production method. 
     * @param element element with bindings
     * @return list of all bindings for <code>element</code>
     */
    public List<AnnotationMirror> getBindings( Element element ){
        return getProvider().getBindings( element );
    }
    
    /**
     * Returns deployment type for <code>element</code>.
     * <code>element</code> could be variable ( injection point , producer field ),
     * type element ( bean type with binding ) and production method. 
     * @param element element with bindings
     * @return deployment type for <code>element</code>    
     */
    public AnnotationMirror getDeploymentType( Element element ){
        return getProvider().getDeploymentType( element );
    }
    
    public AbstractModelImplementation getModelImplementation(){
        return myImpl;
    }
    
    private WebBeansModelProvider getProvider(){
        return Lookup.getDefault().lookup( WebBeansModelProvider.class);
    }
    
    private AbstractModelImplementation myImpl;
}
