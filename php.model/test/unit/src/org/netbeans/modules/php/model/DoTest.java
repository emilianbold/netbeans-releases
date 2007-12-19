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


/**
 * @author ads
 *
 */
public class DoTest extends BaseCase {

    public void testDo() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<DoStatement> statements = model.getStatements( DoStatement.class );
            assert statements.size() > 0 : "Expected at least one " +
                DoStatement.class +" class element, but found :" +statements.size();
            
            DoStatement statement = statements.get( 0 );
            assert statement.getWhile() != null : "Not found while child in do " +
                    "statement";
            
            assert statement.getStatement() != null :"Expected statement child " +
                    " in do statement";
            
            assert statement.getWhile().getElementType().equals( While.class );
            assert statement.getStatement().getElementType().equals( Block.class );
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testDoBlock() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<DoStatement> statements = model.getStatements( DoStatement.class );
            DoStatement statement = statements.get( 0 );
            Statement blockStatement = statement.getStatement();
            assert blockStatement instanceof Block;
            Block block = (Block) blockStatement;
            
            List<Statement> children = block.getStatements();
            assert children.size() == 2 : "Expected 2 child in do block but " +
                    "found :" +children.size();
            
            assert children.get( 0 ).getElementType().equals( Block.class );
            assert children.get( 1 ).getElementType().equals( 
                    ExpressionStatement.class );
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testDoInnerBlock() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<DoStatement> statements = model.getStatements( DoStatement.class );
            DoStatement statement = statements.get( 0 );
            Statement blockStatement = statement.getStatement();
            Block block = (Block) blockStatement;
            List<Block> children = block.getChildren( Block.class );
            assert children.size() >0 : "Expected at least one inner block inside " +
                    "do statement";
            
            block = children.get( 0 );
            List<Statement> list = block.getStatements();
            assert list.size() > 0 : "Expected at least one expression inside " +
                    "inner block";
            assert list.get( 0 ).getElementType().equals( ExpressionStatement.class );
        }
        finally {
            model.readUnlock();
        }
    }
}
