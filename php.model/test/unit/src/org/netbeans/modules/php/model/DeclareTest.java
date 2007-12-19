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

import java.util.List;

import org.netbeans.modules.php.model.resources.ResourceMarker;


/**
 * @author ads
 *
 */
public class DeclareTest extends BaseCase {

    
    public void testDeclareStatement() throws Exception {
        PhpModel model = getModel( ResourceMarker.DECLARE );
        model.sync();
        model.readLock();
        try {
            List<DeclareStatement> statements = model.getStatements( 
                    DeclareStatement.class );
            assert statements.size() > 0 : "Expected at least one " +
                DeclareStatement.class +" class element, but found :" +statements.size();
            
            DeclareStatement statement = (DeclareStatement) statements.get(0);
            assert statement != null;
            Declare declare = statement.getDeclare();
            assert declare != null : 
                "Expected to find declare in declaration statement";
            
            assert declare.getElementType().equals( Declare.class ) :"Expected " +
            		"to find " +Declare.class + " as type for declare element, " +
            				"but found " + declare.getElementType();
            
            List<Statement> list = statement.getStatements();
            assert list.size() ==1 : "Expected to find exactly one statement in" +
            		"declaration, but found :" + list.size();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testDeclare() throws Exception {
        PhpModel model = getModel( ResourceMarker.DECLARE );
        model.sync();
        model.readLock();
        try {
            List<DeclareStatement> statements = model.getStatements( 
                    DeclareStatement.class );
            
            DeclareStatement statement = (DeclareStatement) statements.get(0);
            Declare declare = statement.getDeclare();
            
            Expression expression = declare.getExpression();
            assert expression != null : "Expected to find expression in declare ";
            
            assert expression.getElementType().equals( Variable.class ) :
                    "Expected to find  variable as expression , but found :" + 
                    expression.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testStatements() throws Exception {
        PhpModel model = getModel( ResourceMarker.DECLARE );
        model.sync();
        model.readLock();
        try {
            List<DeclareStatement> statements = model.getStatements( 
                    DeclareStatement.class );
            
            DeclareStatement statement = (DeclareStatement) statements.get(0);
            List<Statement> list = statement.getStatements();
            Statement stmnt = list.get(0);
            
            assert stmnt != null;
            assert stmnt.getElementType().equals( Block.class  ) :
                "Expected to find block as statement in declare , but found :" +
                stmnt.getElementType();
            
            Block block = (Block) stmnt;
            list = block.getStatements();
            assert list.size() >0 :"Expected to find at least one statement inside " +
            		" block of declare statement";
            stmnt = list.get(0);
            
            assert stmnt != null;
            assert stmnt.getElementType().equals( ExpressionStatement.class ):
                "Expetcted to find expression statement inside block , but found " +
                ": " + stmnt.getElementType();
            
            ExpressionStatement expressionStatement = (ExpressionStatement) stmnt;
            Expression expression = expressionStatement.getExpression();
            assert expression != null :"Expected to find expression in expresison " +
            		"statement of block";
            assert expression.getElementType().equals( Variable.class ) :
                "Expected to find variable as expression , but found :" +
                expression.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }   
}
