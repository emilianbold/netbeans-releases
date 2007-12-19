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

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;



/**
 * @author ads
 *
 */
public class BlockTest extends BaseCase {
    
    public void testBlock() throws Exception  {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<Block> children = model.getStatements( Block.class );
            assert children.size() > 0 : "Expect at least one block statement, but " +
                    " not found any";           // NOI18N
            Block block = children.get( 0 );
            List<SourceElement> blockChildren = block.getChildren();
            assert blockChildren.size() == 3 : 
                "Unexpected quantity of children :" + blockChildren.size()+
                ", should be 3";                // NOI18N
            assert blockChildren.get(0).getElementType().equals(Block.class);
            assert blockChildren.get(1).getElementType().equals( 
                    ExpressionStatement.class );
            assert blockChildren.get(2).getElementType().equals(DoStatement.class);
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testInnerBlock() throws Exception  {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<Block> children = model.getStatements( Block.class );
            assert children.size() > 0 : "Expect at least one block statement, but " +
                    " not found any";           // NOI18N
            Block block = children.get( 0 );
            children = block.getChildren( Block.class );
            assert children.size() > 0 : "Expect at least one block statement, but " +
                    " not found any";           // NOI18N
            block  = children.get( 0 );
            List<SourceElement> statements = block.getChildren();
            assert statements.size() == 1 : "Expected 1 child in inner block, " +
                    "but found " +statements.size();
            assert statements.get( 0 ).getElementType().equals( 
                    ExpressionStatement.class ) :"Expected " 
                        +ExpressionStatement.class+" as child in inner block but " +
                                "found :" + statements.get( 0 ).getElementType();
                    
            checkText( statements.get( 0 ) );
            checkTokenSequence( statements.get( 0 ) );
        }
        finally {
            model.readUnlock();
        }
    }

    

    private void checkTokenSequence( SourceElement element ) {
        List<String> list = new ArrayList<String>( 4 );
        list.add( "$h");
        list.add( "=");
        list.add( "1");
        list.add( ";");
        TokenSequence seq = element.getTokenSequence();
        seq.moveStart();
        while( seq.moveNext()) {
            Token token = seq.token();
            if ( list.size() >  0) {
                String str = list.remove( 0 );
                assert str.equals( token.text().toString() ) :"Expected "+str +" but found "+
                    token.text();
            }
            else {
                assert token.text().toString().trim().length() ==0 :"Not " +
                        "expected symbols :" + token.text();
            }
        }
    }

    private void checkText( SourceElement element ) {
        String text = element.getText().trim();
        assert text.equals( "$h=1;" ): "Expected text '$h=1;' , but found :" +text;
    }

}
