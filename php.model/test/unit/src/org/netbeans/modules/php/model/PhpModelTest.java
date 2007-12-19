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
package org.netbeans.modules.php.model;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.resources.ResourceMarker;


/**
 * @author ads
 *
 */
public class PhpModelTest extends BaseCase {
    
    public void testStatements() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<Statement> children = model.getStatements();
            assert children.size() == 18: "Found " +children.size()
            +" source elements , but supposed 18";           // NOI18N
            
            checkElementTypes(children);
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testSeparated() throws Exception {
        PhpModel model = getModel( ResourceMarker.SEPARATED );
        model.sync();
        model.readLock();
        try {
            List<DoStatement> doStats = model.getStatements( DoStatement.class );
            assert doStats.size() >0 : 
                "Expected at least one do statement";       // NOI18N
            checkDo( doStats.get(0 ));
            List<IfStatement> ifStats = model.getStatements( IfStatement.class );
            assert ifStats.size() >0 :
                "Expected at least one if statement";       // NOI18N
            checkIf( ifStats.get(0) );
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testBroken() throws Exception{
        PhpModel model = getModel( ResourceMarker.BROKEN1);
        model.sync();
        model.readLock();
        try {
            List<Statement> list = model.getStatements();
            System.out.println( list.size() );
            System.out.println( list.get(0) );
        }
        finally {
            model.readUnlock();
        }
    }


    private void checkIf( IfStatement statement ) {
        // TODO
        TokenSequence seq = statement.getTokenSequence();
        while( seq.moveNext()) {
        }
    }

    private void checkDo( DoStatement doStatement ) {
        Statement statement = doStatement.getStatement();
        assert statement instanceof Block;
        assert statement.getElementType().equals( Block.class );
        List<Statement> statements = ((Block)statement).getStatements();
        assert statements.size() > 0 : 
            "Expected to find at least statement in do block";  // NOI18N
        Statement stat = statements.get( 0 );
        assert stat instanceof ExpressionStatement;
        assert stat.getText().trim().equals( "echo \"a\";" )
            :"Found text is "+stat.getText().trim() ;
        
        stat.getTokenSequence().moveNext();
        assert stat.getTokenSequence().token().toString().equals( "echo" ):
                "Expected to find 'echo' as first token in token sequence";// NOI18N
    }

    private void checkElementTypes( List<Statement> children ) {
        List<Class<? extends SourceElement>> classes = 
            new ArrayList<Class<? extends SourceElement>>( children.size() );
        classes.add( Block.class );
        classes.add( BreakStatement.class );
        classes.add( ClassDefinition.class );
        classes.add( ContinueStatement.class );
        classes.add( DoStatement.class );
        classes.add( ExpressionStatement.class );
        classes.add( ExpressionStatement.class );
        classes.add( ExpressionStatement.class );
        classes.add( ForEachStatement.class );
        classes.add( ForStatement.class );
        classes.add( FunctionDefinition.class );
        classes.add( GlobalStatement.class );
        classes.add( IfStatement.class );
        classes.add( InterfaceDefinition.class );
        classes.add( ReturnStatement.class );
        classes.add( SwitchStatement.class );
        classes.add( StaticStatement.class );
        classes.add( WhileStatement.class );
        
        int i = 0;
        for (Class<? extends SourceElement> clazz : classes) {
            assert clazz.equals( children.get(i).getElementType() ) : "At "+ i
                    +" position found " +
                    "element type " +children.get(i).getElementType()+
                    " , but expected "+clazz;           // NOI18N
            i++;
        }
    }
    
}
