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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.model.ClassConst;
import org.netbeans.modules.php.model.ClassFunctionDeclaration;
import org.netbeans.modules.php.model.ConstDeclaration;
import org.netbeans.modules.php.model.InterfaceBody;
import org.netbeans.modules.php.model.InterfaceDefinition;
import org.netbeans.modules.php.model.InterfaceStatement;
import org.netbeans.modules.php.model.Reference;
import org.netbeans.modules.php.model.SourceElement;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.model.refs.ReferenceResolver.class)
public class InterfaceStaticMemberReferenceResover 
    extends StaticMemberReferenceResolver 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceResolver#isApplicable(java.lang.Class)
     */
    public <T extends SourceElement> boolean isApplicable( Class<T> clazz ) {
        return InterfaceStatement.class.isAssignableFrom( clazz ) ||
            ClassConst.class.isAssignableFrom( clazz );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.refs.resolvers.StaticMemberReferenceResolver#getOwnType()
     */
    @Override
    protected Class<? extends SourceElement> getOwnType() {
        return InterfaceDefinition.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.refs.resolvers.StaticMemberReferenceResolver#resolve(org.netbeans.modules.php.model.SourceElement, java.lang.String, java.lang.Class, org.netbeans.modules.php.model.SourceElement, boolean)
     */
    @Override
    protected <T extends SourceElement> List<T> resolve( String memberName, 
            Class<T> clazz, SourceElement owner, boolean exactComparison )
    {
        // avoid cyclic references to super interfaces
        Set<String> set = new HashSet<String>();
        return resolve(memberName, clazz, owner, exactComparison , set );
    }

    protected <T extends SourceElement> List<T> resolve( String memberName, 
            Class<T> clazz, SourceElement owner, boolean exactComparison , 
            Set<String> ifaceNames )
    {
        if ( !(  owner instanceof InterfaceDefinition )){
            return Collections.emptyList();
        }
        List<T> result = new LinkedList<T>();
        InterfaceDefinition definition = (InterfaceDefinition) owner;
        String name = definition.getName();
        if ( ifaceNames.contains( name )){
            return Collections.emptyList();
        }
        else {
            ifaceNames.add( name );
        }
        InterfaceBody body = definition.getBody();
        if ( body == null ){
            return Collections.emptyList();
        }
        List<InterfaceStatement> statements = body.getStatements();
        for (InterfaceStatement interfaceStatement : statements) {
            findInStatement( memberName , interfaceStatement , clazz,  result , 
                    exactComparison);
        }
        
        if ( exactComparison && result.size() >0 ){
            return result;
        }
        
        List<Reference<InterfaceDefinition>> superIfaces = definition.getSuperInterfaces();
        for (Reference<InterfaceDefinition> reference : superIfaces) {
            InterfaceDefinition def = reference.get();
            if ( def == null ){
                continue;
            }
            List<T> list = resolve(memberName, clazz, def, exactComparison , 
                    ifaceNames );
            result.addAll( list );
            
            if ( exactComparison && result.size() >0 ){
                return result;
            }
        }
        return result;
    }
    
    
    protected <T extends SourceElement> void handleFunctionDecl( String name,
            ClassFunctionDeclaration decl, Class<T> clazz, List<T> collected,
            boolean exactComparison )
    {
        if (!clazz.isAssignableFrom(decl.getElementType())) {
            return;
        }
        String declName = decl.getName();
        if (exactComparison && name.equals(declName)) {
            collected.add(clazz.cast(decl));
        }
        if (!exactComparison && declName.startsWith(name)) {
            collected.add(clazz.cast(decl));
        }
    }
    
    protected <T extends SourceElement> void handleConstDecl( String name, 
            ConstDeclaration decl , Class<T> clazz, List<T> collected ,
            boolean exactComparison) {
        List<ClassConst> consts = decl.getDeclaredConstants();
        for (ClassConst cnst : consts) {
            if ( !clazz.isAssignableFrom( cnst.getElementType())){
                continue;
            }
            String attrName = cnst.getName();
            if ( exactComparison && name.equals( attrName ) ){
                collected.add( clazz.cast( cnst ));
            }
            if ( !exactComparison && attrName.startsWith( name ) ){
                collected.add(clazz.cast( cnst ));
            }
        }
    }

    private <T extends SourceElement>  void findInStatement(  String name, 
            InterfaceStatement statement, Class<T> clazz, List<T> collected ,
            boolean exactComparison)
    {
        if ( statement.getElementType().equals( ClassFunctionDeclaration.class )){
            handleFunctionDecl(name, (ClassFunctionDeclaration)statement, 
                    clazz, collected, exactComparison);
        }
        else if ( statement.getElementType().equals( ConstDeclaration.class )){
            handleConstDecl(name, (ConstDeclaration)statement, clazz, 
                    collected, exactComparison);
        }
    }
    
    
}
