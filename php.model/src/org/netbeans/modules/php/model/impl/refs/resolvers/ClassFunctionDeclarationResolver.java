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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.model.impl.refs.resolvers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassFunctionDeclaration;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.InterfaceDefinition;
import org.netbeans.modules.php.model.Reference;
import org.netbeans.modules.php.model.SourceElement;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.model.refs.ReferenceResolver.class)
public class ClassFunctionDeclarationResolver extends FunctionDefinitionResolver {

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceResolver#isApplicable(java.lang.Class)
     */
    public <T extends SourceElement> boolean isApplicable( Class<T> clazz ) {
        return ClassFunctionDeclaration.class.isAssignableFrom( clazz );
    }

    protected <T extends SourceElement> void  resolve( 
            SourceElement sourceElement , List<T> functions , String funcName ,
            Class<T> clazz , boolean exactComparison) 
    {
        SourceElement scope = sourceElement.getParent();
        
        if (  InterfaceDefinition.class.isAssignableFrom( clazz ) && 
                scope instanceof InterfaceDefinition )
        {
           // avoid cyclic references to super class and interface
            Set<String> set = new HashSet<String>();
            findInInterface( (InterfaceDefinition) scope , functions , funcName , 
                    clazz, exactComparison , set );
        }
        else {
            super.resolve(sourceElement , functions , funcName ,  clazz, 
                    exactComparison);
        }
    }
    
    protected <T extends SourceElement> void findInScope( SourceElement scope, 
            List<T> functions,String funcName, Class<T> clazz, 
            boolean exactComparison )
    {
        List<ClassFunctionDeclaration> list = 
            scope.getChildren( ClassFunctionDeclaration.class );
        for ( ClassFunctionDeclaration decl : list ){
            if ( !clazz.isAssignableFrom( decl.getElementType() )){
                continue;
            }
            String name = decl.getName();
            if ( !exactComparison && name.startsWith(funcName )){
                functions.add( clazz.cast( decl) );
            }
            else if ( exactComparison && name.equals( funcName )){
                functions.add( clazz.cast( decl ));
            }
        }
    }
    
    protected <T extends SourceElement> void findInClass(ClassDefinition scope , 
            List<T> functions , String funcName ,Class<T> clazz , 
            boolean exactComparison , Set<String> classNames )
    {
        super.findInClass(scope, functions, funcName, clazz, exactComparison, 
                classNames);
        
        if ( exactComparison && functions.size() > 0 ){
            return;
        }
        
        List<Reference<InterfaceDefinition>> ifaces = 
            scope.getImplementedInterfaces();
        for (Reference<InterfaceDefinition> reference : ifaces) {
            InterfaceDefinition iface = reference.get();
            if ( iface == null ){
                continue;
            }
            findInInterface( iface, functions, funcName, clazz, 
                    exactComparison, classNames);
            if ( exactComparison && functions.size() >0 ){
                return ;
            }
        }
    }
    
    protected <T extends SourceElement> void findInList( List<T> functions, 
            String funcName,Class<T> clazz, boolean exactComparison,
            List<FunctionDefinition> list )
    {
    }
    
    
    private <T extends SourceElement> void findInInterface(
            InterfaceDefinition scope , List<T> functions , String funcName ,
            Class<T> clazz , boolean exactComparison , Set<String> classNames )
    {
        String name = scope.getName();
        if ( classNames.contains(name )){
            return;
        }
        else {
            classNames.add( name );
        }
        
        findInScope( scope.getBody(), functions, funcName, clazz, 
                exactComparison);
        
        if ( exactComparison && functions.size() > 0 ){
            return;
        }
        
        List<Reference<InterfaceDefinition>> ifaces = scope.getSuperInterfaces();
        for (Reference<InterfaceDefinition> reference : ifaces) {
            InterfaceDefinition iface = reference.get();
            if ( iface == null ){
                continue;
            }
            findInInterface( iface, functions, funcName, clazz, 
                    exactComparison, classNames);
            if ( exactComparison && functions.size() >0 ){
                return ;
            }
        }
    }
}
