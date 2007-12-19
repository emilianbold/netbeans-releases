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
public class WhileTest extends BaseCase {

    public void testWhile() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<WhileStatement> children = model.getStatements( 
                    WhileStatement.class );
            assert children.size() > 0 : "Expected at least one for statement";
            WhileStatement wile = children.get( 0 );
            
            assert !wile.isAlternative() : "While recognized as alternative, " +
                    "but shouldn't";
            
            assert wile.getWhile()!= null: "Expected not null 'while' expression";
            assert wile.getStatements().size() == 1 :"Expected exactly one statement " +
                    " body for 'while', found :" +wile.getStatements().size();
            Statement statement = wile.getStatements().get( 0 );
            assert statement instanceof Block : "Expected Block as " +
                    "body statement in while";
            
            assert statement.getElementType().equals( Block.class ); 
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testWhileCondition() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<WhileStatement> children = model.getStatements( 
                    WhileStatement.class );
            WhileStatement whileStatement = children.get( 0 );
            While wile = whileStatement.getWhile();
            
            Expression expression = wile.getExpression();
            assert expression != null : "Expected expression in while condition";

            assert expression.getElementType().equals( Literal.class ) :
                "Expected literal type as condition expression type in while," +
                " but found : " +expression.getElementType();
            assert expression instanceof Literal :"Expected literal as condition " +
                    "expression in while";
        }
        finally {
            model.readUnlock();
        }
    }
    
}
