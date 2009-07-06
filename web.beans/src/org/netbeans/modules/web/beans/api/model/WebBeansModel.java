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

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;
import org.openide.util.Lookup;


/**
 * @author ads
 *
 */
public final class WebBeansModel {
    
    WebBeansModel( ModelUnit unit ){
        myUnit = unit;
    }

    /**
     * Find injectable type that is used for given injection point.
     * @param element injection point
     * @return type that is used in injected point identified by <code>element</code>
     */
    public TypeMirror getInjectable( VariableElement element ){
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().getInjectable(element, getModelUnit().getHelper());
    }
    
    /**
     * Find injectable types that could be used for given injection point.
     * This method differs from {@link #getInjectable(VariableElement)}
     * by injection point type. Injection point could be defined via 
     * programmatic lookup which is dynamically specify injectable type.
     * Such situation appears when injection point uses Instance interface. 
     * In case of @Any binding usage this list will contain all 
     * possible binding types for <code>element</code>  ( all beans 
     * that implements or extends type parameter for Instance<> ). 
     * @param element injection point
     * @return types that is used in injected point identified by <code>element</code>
     */
    public List<TypeMirror> getInjectables( VariableElement element ){
        if ( getProvider() == null ){
            return null;
        }
        return getProvider().getInjectables(element, getModelUnit().getHelper());
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
        return getProvider().resolveType(fqn, getModelUnit().getHelper());
    }
    
    public ModelUnit getModelUnit(){
        return myUnit;
    }
    
    private WebBeansModelProvider getProvider(){
        return Lookup.getDefault().lookup( WebBeansModelProvider.class);
    }
    
    private ModelUnit myUnit;
}
