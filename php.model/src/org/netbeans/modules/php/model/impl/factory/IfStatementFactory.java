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
package org.netbeans.modules.php.model.impl.factory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.IfStatement;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.ElseIfImpl;
import org.netbeans.modules.php.model.impl.ElseImpl;
import org.netbeans.modules.php.model.impl.IfImpl;


/**
 * @author ads
 *
 */
public class IfStatementFactory {
    
    private static final String IF          = "IfCommon";           // NOI18N
    
    public  static final String ALTERNATIVE = "IfStatementColon";   // NOI18N 
    
    private static final String USUAL       = "IfStatementCurly";   // NOI18N
    
    private static final String 
                            ELSE_IF_CURLY   = "ElseIfCurly";        // NOI18N
    
    private static final String ELSE_CURLY  = "ElseCurly";          // NOI18N
    
    private static final String 
                            ELSE_IF_COLON   = "ElseIfColon";        // NOI18N
    
    private static final String ELSE_COLON  = "ElseColon";          // NOI18N
    
    private IfStatementFactory() {
    }

    public static IfStatementFactory getInstance() {
        return INSTANCE;
    }
    
    public List<SourceElement> build( IfStatement statement, ASTNode node, 
            TokenSequence<?> sequence ) 
    {
        if ( node.getNT().equals( IF )) {
            return Collections.singletonList( 
                    (SourceElement)new IfImpl( statement , node, node , sequence ) );
        }
        else if ( node.getNT().equals( USUAL )) {
            return handleCurly( statement , node , sequence ); 
        }
        else if ( node.getNT().equals( ALTERNATIVE )) {
            return handleColon( statement , node, sequence );
        }
        return null;
    } 

    private List<SourceElement> handleColon( IfStatement statement, ASTNode node, 
            TokenSequence<?> sequence ) 
    {
        List<ASTItem> children =  node.getChildren();
        List<SourceElement> list = new LinkedList<SourceElement>();
        for( ASTItem item : children ) {
            if ( item instanceof ASTNode ) {
                ASTNode child = (ASTNode) item;
                list.add( buildColon(statement, child, sequence));
            }
            else {
                // TODO
            }
        }
        return list;
    }

    private List<SourceElement> handleCurly( IfStatement statement, ASTNode node, 
            TokenSequence<?> sequence ) 
    {
        List<ASTItem> children =  node.getChildren();
        List<SourceElement> list = new LinkedList<SourceElement>();
        for( ASTItem item : children ) {
            if ( item instanceof ASTNode ) {
                ASTNode child = (ASTNode) item;
                list.add( buildCurly(statement, child, sequence));
            }
            else {
                // TODO
            }
        }
        return list;
    }
    
    private SourceElement buildCurly( IfStatement statement , ASTNode node , 
            TokenSequence<?> sequence ) 
    {
        String type = node.getNT();
        if ( StatementsListFactory.getInstance().isStatement(node) ) {
             return StatementsListFactory.getInstance().build(
                    statement, node, sequence);
        }
        else if ( ELSE_IF_CURLY.equals( type )) {
            return new ElseIfImpl(  statement , node, node, sequence , false);
        }
        else if ( ELSE_CURLY.equals( type )) {
            return new ElseImpl(  statement , node, node, sequence , false );
        }
        else {
            assert false : "Found type : " +type;
        }
        return null;
    }
    
    private SourceElement buildColon( IfStatement statement , ASTNode node , 
            TokenSequence<?> sequence ) 
    {
        if ( StatementsListFactory.getInstance().isStatement(node) ) {
             return StatementsListFactory.getInstance().build(
                    statement, node, sequence);
        }
        else if ( ELSE_IF_COLON.equals( node.getNT())) {
            return new ElseIfImpl(  statement , node, node, sequence , true );
        }
        else if ( ELSE_COLON.equals( node.getNT())) {
            return new ElseImpl(  statement , node, node, sequence , true );
        }
        else {
            assert false;
        }
        return null;
    }

    private static final IfStatementFactory INSTANCE = new IfStatementFactory();
}
