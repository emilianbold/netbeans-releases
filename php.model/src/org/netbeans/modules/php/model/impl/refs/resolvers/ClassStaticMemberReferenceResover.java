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

import org.netbeans.modules.php.model.Attribute;
import org.netbeans.modules.php.model.AttributesDeclaration;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassConst;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassFunctionDeclaration;
import org.netbeans.modules.php.model.ClassFunctionDefinition;
import org.netbeans.modules.php.model.ClassStatement;
import org.netbeans.modules.php.model.ConstDeclaration;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.InterfaceDefinition;
import org.netbeans.modules.php.model.Reference;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.VariableAppearance;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.model.refs.ReferenceResolver.class)
public class ClassStaticMemberReferenceResover 
    extends InterfaceStaticMemberReferenceResover 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceResolver#isApplicable(java.lang.Class)
     */
    public <T extends SourceElement> boolean isApplicable( Class<T> clazz ) {
        return VariableAppearance.class.isAssignableFrom( clazz ) 
            || ClassStatement.class.isAssignableFrom( clazz )
            || ClassConst.class.isAssignableFrom( clazz )
            || Attribute.class.isAssignableFrom( clazz );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.refs.resolvers.StaticMemberReferenceResolver#getOwnType()
     */
    @Override
    protected Class<? extends SourceElement> getOwnType() {
        return ClassDefinition.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.refs.resolvers.StaticMemberReferenceResolver#resolve(org.netbeans.modules.php.model.SourceElement, java.lang.String, java.lang.Class, org.netbeans.modules.php.model.SourceElement, boolean)
     */
    @Override

    protected <T extends SourceElement> List<T> resolve( String memberName, 
            Class<T> clazz, SourceElement owner, boolean exactComparison )
    {
        // avoiding cyclic references in super classes or super interfaces
        Set<String> set = new HashSet<String>(); 
        return resolve(memberName, clazz, owner, exactComparison, set );
    }

    protected <T extends SourceElement> List<T> resolve( String memberName, 
            Class<T> clazz, SourceElement owner, boolean exactComparison , 
            Set<String> classNames )
    {
        if ( !(owner instanceof ClassDefinition ) ){
            return Collections.emptyList();
        }
        List<T> result = new LinkedList<T>();
        ClassDefinition statement = (ClassDefinition)owner;
        String name = statement.getName();
        if ( classNames.contains( name ) ){
            return Collections.emptyList();
        }
        else {
            classNames.add( name );
        }
        ClassBody body = statement.getBody();
        if ( body == null){
            return Collections.emptyList();
        }
        List<ClassStatement> statements = body.getStatements();
        for (ClassStatement classStatement : statements) {
            findInStatement( memberName, classStatement , clazz , result ,
                    exactComparison );
        }
        
        if ( exactComparison && result.size() >0 ){
            return result;
        }
        
        Reference<ClassDefinition> superClassRef = statement.getSuperClass();
        if (superClassRef != null) {
            ClassDefinition superClass = superClassRef.get();
            if (superClass != null) {
                List<T> list = resolve(memberName, clazz, superClass,
                        exactComparison , classNames );
                result.addAll(list);
            }
        }
        
        if ( exactComparison && result.size() >0 ){
            return result;
        }
        
        List<Reference<InterfaceDefinition>> ifaces = 
            statement.getImplementedInterfaces();
        for (Reference<InterfaceDefinition> reference : ifaces) {
            InterfaceDefinition iface = reference.get();
            if ( iface == null ){
                continue;
            }
            List<T> list =  super.resolve(memberName, clazz, iface, 
                    exactComparison , classNames );
            result.addAll( list );
            if ( exactComparison && result.size() >0 ){
                return result;
            }
        }
        return result;
    }

    private <T extends SourceElement> void findInStatement( String name, 
            ClassStatement statement, Class<T> clazz, List<T> collected ,
            boolean exactComparison)
    {
        Class<? extends SourceElement> type = statement.getElementType();
        if ( type.equals( AttributesDeclaration.class )){
            handleAttributes( name , (AttributesDeclaration)statement , 
                    clazz , collected , exactComparison );
        }
        else if ( type.equals( ClassFunctionDeclaration.class )){
            handleFunctionDecl( name , (ClassFunctionDeclaration)statement , 
                    clazz , collected , exactComparison );
        }
        else if ( type.equals( ClassFunctionDefinition.class )){
            handleFunctionDef( name , (ClassFunctionDefinition)statement , 
                    clazz , collected , exactComparison );
        }
        else if ( type.equals( ConstDeclaration.class )){
            handleConstDecl( name , (ConstDeclaration)statement , 
                    clazz , collected , exactComparison);
        }
        else {
            // at the moment of writing this code there was no other child types for ClassStatement
            assert false;
        }
    }

    private <T extends SourceElement> void handleAttributes( String name, 
            AttributesDeclaration decl , Class<T> clazz, List<T> collected ,
            boolean exactComparison) {
        List<Attribute> attributes = decl.getDeclaredAttributes();
        for (Attribute attribute : attributes) {
            if ( !clazz.isAssignableFrom( attribute.getElementType())){
                continue;
            }
            String attrName = attribute.getName();
            if ( exactComparison && name.equals( attrName ) ){
                collected.add( clazz.cast( attribute ));
            }
            if ( !exactComparison && attrName.startsWith( name ) ){
                collected.add(clazz.cast( attribute ));
            }
        }
    }
    
    private <T extends SourceElement> void handleFunctionDef( String name,
            ClassFunctionDefinition def, Class<T> clazz, List<T> collected,
            boolean exactComparison )
    {
        if (!clazz.isAssignableFrom(def.getElementType())) {
            return;
        }
        FunctionDeclaration decl = def.getDeclaration();
        String declName = decl.getName();
        if (exactComparison && name.equals(declName)) {
            collected.add(clazz.cast(def));
        }
        if (!exactComparison && declName.startsWith(name)) {
            collected.add(clazz.cast(def));
        }
    }
    
}
