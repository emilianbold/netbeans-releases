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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.Error;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.Statement;
import org.netbeans.modules.php.model.impl.ErrorImpl;
import org.netbeans.modules.php.model.impl.FactoryVisitor;
import org.netbeans.modules.php.model.impl.Utils.NodeFinder;
import org.netbeans.modules.php.model.impl.builders.BlockBuilder;
import org.netbeans.modules.php.model.impl.builders.BreakBuilder;
import org.netbeans.modules.php.model.impl.builders.ClassDefBuilder;
import org.netbeans.modules.php.model.impl.builders.ContinueStatBuilder;
import org.netbeans.modules.php.model.impl.builders.DeclareStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.DoStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.EchoBuilder;
import org.netbeans.modules.php.model.impl.builders.EmptyStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.ExpressionStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.ForEachStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.ForStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.FunctionDefBuilder;
import org.netbeans.modules.php.model.impl.builders.GlobalStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.IfStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.InterfaceDefBuilder;
import org.netbeans.modules.php.model.impl.builders.ReturnStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.SourceElementBuilder;
import org.netbeans.modules.php.model.impl.builders.StaticStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.SwitchStatementBuilder;
import org.netbeans.modules.php.model.impl.builders.WhileStatementBuilder;


/**
 * @author ads
 *
 */
public class StatementsListFactory {
    
    private static final String BLOCK               = "Block";              // NOI18N
    
    private static final String BREAK_STATEMENT   
                                                    = "BreakStatement";     // NOI18N
    
    private static final String CLASS_DEF           = "ClassDefinition";    // NOI18N
    
    private static final String CONTINUE_STAT       = "ContinueStatement";  // NOI18N
    
    private static final String DO_STAT             = "DoStatement";        // NOI18N    
    
    private static final String ECHO_STAT           = "EchoStatement";      // NOI18N
    
    private static final String EMPTY_STAT          = "EmptyStatement";     // NOI81N
    
    private static final String EXPRESS_STAT        = "ExpressionStatement";// NOI81N
    
    private static final String FOR_EACH_STAT       = "ForEachStatement";   // NOI81N 
    
    private static final String FOR_STAT            = "ForStatement";       // NOI81N
    
    public  static final String FUNC_DEF            = "FunctionDefinition"; // NOI18N
    
    private static final String GLOBAL_STAT         = "GlobalStatement";    // NOI18N      
    
    private static final String IF_STAT             = "IfStatement";        // NOI18N
    
    private static final String INTERFACE_DEF       = "InterfaceDefinition";// NOI18N    
    
    private static final String RETURN_STAT         = "ReturnStatement";    // NOI18N
    
    private static final String SWITCH_STAT         = "SwitchStatement";    // NOI18N
    
    private static final String STATIC_STAT         = "StaticStatement";    // NOI18N
    
    private static final String WHILE_STAT          = "WhileStatement";     // NOI18N
    
    private static final String DECLARE_STAT        = "DeclareStatement";   // NOI18N
    
    public static final StatementsListFactory getInstance(){
        return INSTANCE;
    }
    
    public SourceElement build( PhpModel model , ASTNode node , 
            TokenSequence sequence )
    {
        NodeFinder finder = new NodeFinder( node , TYPES.keySet());
        finder.check();
        if ( finder.isFound()) {
            SourceElementBuilder builder = TYPES.get( finder.getType() );
            return (Statement)builder.build( model , node , finder.getNode(),
                    sequence  );
        }
        else {
            return buildError( model , node, sequence );
        }
    }

    public boolean isStatement( ASTNode  node ){
        NodeFinder finder = new NodeFinder( node , TYPES.keySet());
        finder.check();
        return finder.isFound();
    }
    
    public SourceElement build( SourceElement parent , ASTNode node , 
            TokenSequence sequence )
    {
        NodeFinder finder = new NodeFinder( node , TYPES.keySet());
        finder.check();
        if ( finder.isFound() ) {
            SourceElementBuilder builder = TYPES.get(finder.getType());
            return (Statement)builder.build( parent , node , finder.getNode(),
                    sequence );            
        }
        else {
            return buildError(parent, node, sequence);
        }
    }
    
    private Error buildError( PhpModel model, ASTNode node, 
            TokenSequence sequence ) 
    {
        ASTNode error =  getError(node);
        return new ErrorImpl( model , node , error, sequence );
    }

    private Error buildError( SourceElement parent , ASTNode node, 
            TokenSequence sequence ) 
    {
        ASTNode error = getError(node);
        return new ErrorImpl( parent , node , error, sequence );
    }

    private ASTNode getError( ASTNode node ) {
        List<ASTItem> children = node.getChildren();
        byte count = 0;
        ASTNode ret = null;
        for (ASTItem item : children) {
            if ( item instanceof ASTNode ) {
                ASTNode error = (ASTNode) item;
                ret = error;
                assert error.getNT().equals( FactoryVisitor.ERROR );
                assert count == 0;
                count++;
            }
        }
        return ret;
    }

    
    private static final StatementsListFactory INSTANCE = new StatementsListFactory(); 
    
    private static final Map<String,SourceElementBuilder> TYPES 
        = new HashMap<String,SourceElementBuilder>();
    
    static {
        TYPES.put( BLOCK , BlockBuilder.getInstance() );
        TYPES.put( BREAK_STATEMENT , BreakBuilder.getInstance() );
        TYPES.put( CLASS_DEF , ClassDefBuilder.getInstance() );
        TYPES.put( CONTINUE_STAT , ContinueStatBuilder.getInstance() );
        TYPES.put( DO_STAT , DoStatementBuilder.getInstance()  );
        TYPES.put( ECHO_STAT , EchoBuilder.getInstance() );
        TYPES.put( EMPTY_STAT  , EmptyStatementBuilder.getInstance() );
        TYPES.put( EXPRESS_STAT , ExpressionStatementBuilder.getInstance() );
        TYPES.put( FOR_EACH_STAT , ForEachStatementBuilder.getInstance() );
        TYPES.put( FOR_STAT , ForStatementBuilder.getInstance() );
        TYPES.put( FUNC_DEF , FunctionDefBuilder.getInstance() );
        TYPES.put( GLOBAL_STAT , GlobalStatementBuilder.getInstance() );
        TYPES.put( IF_STAT , IfStatementBuilder.getInstance() );
        TYPES.put( INTERFACE_DEF , InterfaceDefBuilder.getInstance() );
        TYPES.put( RETURN_STAT , ReturnStatementBuilder.getInstance() );
        TYPES.put( SWITCH_STAT , SwitchStatementBuilder.getInstance() );
        TYPES.put( STATIC_STAT , StaticStatementBuilder.getInstance() );
        TYPES.put( WHILE_STAT , WhileStatementBuilder.getInstance() );
        TYPES.put( DECLARE_STAT , DeclareStatementBuilder.getInstance() );
    }

}
