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
package org.netbeans.modules.php.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.php.model.ClassMemberReference;
import org.netbeans.modules.php.model.ObjectDefinition;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.refs.ReferenceResolver;


/**
 * This reference could be reference to class member.
 * This class member can be constant or static member ( attribute or method).
 * So method get() can return two types : ClassConst or ClassFunctionDefinition. 
 * @author ads
 *
 */
class ClassMemberReferenceImpl<T extends SourceElement> extends ReferenceImpl<T>     
    implements ClassMemberReference<T> 
{
    
    ClassMemberReferenceImpl( SourceElementImpl source , ASTNode identifierNode  )
    {
        this( source , identifierNode , null );
    }
    
    ClassMemberReferenceImpl( SourceElementImpl source , ASTNode identifierNode  , 
            Class<T> clazz)
    {
        super( source , identifierNode.getAsText().trim() , clazz );
        myNode = identifierNode;
    }

    public String getMemberName() {
        if ( myMemberName == null ) {
            initIds();
        }
        return myMemberName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.ClassReference#getObject()
     */
    public ObjectDefinition getObject() {
        List<ReferenceResolver> resolvers = getResolvers( ObjectDefinition.class );
        List<ObjectDefinition> result = null;
        for (ReferenceResolver referenceResolver : resolvers) {
            List<ObjectDefinition> list= referenceResolver.resolve( getSource(), 
                    getIdentifier(), ObjectDefinition.class , true );
            result = add( result , list);
        }
        if ( result != null && result.size() >0 ){
            return result.get( result.size() -1 );
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.ClassReference#getObjectName()
     */
    public String getObjectName() {
        if ( myClassName == null ) {
            initIds();
        }
        return myClassName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Reference#get()
     */
    public T get() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private List<ObjectDefinition> add( List<ObjectDefinition> list , 
            List<ObjectDefinition> elements )
    {
        if ( list == null ){
            list = new LinkedList<ObjectDefinition>();
        }
        list.addAll( elements );
        return list;
    }
    
    private void initIds() {
        List<ASTItem> children = myNode.getChildren();
        byte count = 0;
        for (ASTItem item : children) {
            if ( item instanceof ASTToken ) {
                ASTToken token = (ASTToken) item;
                if ( !token.getTypeName().equals( Utils.IDENTIFIER ) ) {
                    continue;
                }
                if ( count > 0 ) {
                    myMemberName = token.getIdentifier();
                }
                else {
                    myClassName = token.getIdentifier();
                }
                count++;
                assert count <2;
            }
        }
    }
    
    private String myClassName;
    
    private String myMemberName;
    
    private final ASTNode myNode;
    
}
