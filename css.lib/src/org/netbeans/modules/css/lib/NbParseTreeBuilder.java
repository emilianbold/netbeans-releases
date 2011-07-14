/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib;

import java.util.Collection;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.BlankDebugEventListener;

import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.ProblemDescription;

/**
 * A patched version of ANLR's ParseTreeBuilder 
 * - the parse tree doesn't contain empty rule nodes with the <epsilon> sub-node.
 * - parsing errors are collected
 * 
 * @author marekfukala
 */
public class NbParseTreeBuilder extends BlankDebugEventListener {

    Stack<RuleNode> callStack = new Stack<RuleNode>();
    List<CommonToken> hiddenTokens = new ArrayList<CommonToken>();
    private int backtracking = 0;
    
    private CommonToken lastConsumedToken;
    private Collection<ProblemDescription> problems = new ArrayList<ProblemDescription>();
    private boolean resyncing;
    private CharSequence source;
    
    public NbParseTreeBuilder(CharSequence source) {
        this.source = source;
        callStack.push(new RootNode(source));
    }

    public AbstractParseTreeNode getTree() {
        return callStack.elementAt(0);
    }

    /** Backtracking or cyclic DFA, don't want to add nodes to tree */
    @Override
    public void enterDecision(int d, boolean couldBacktrack) {
        backtracking++;
    }

    @Override
    public void exitDecision(int i) {
        backtracking--;
    }

    @Override
    public void enterRule(String filename, String ruleName) {
        if (backtracking > 0) {
            return;
        }
        AbstractParseTreeNode parentRuleNode = callStack.peek();
        RuleNode ruleNode = new RuleNode(NodeType.valueOf(ruleName), source);
        parentRuleNode.addChild(ruleNode);
        ruleNode.setParent(parentRuleNode);
        callStack.push(ruleNode);
    }

    @Override
    public void exitRule(String filename, String ruleName) {
        if (backtracking > 0) {
            return;
        }
        RuleNode ruleNode = callStack.pop();
        if (ruleNode.getChildCount() == 0) {
            RuleNode parent = (RuleNode) ruleNode.getParent();
            if (parent != null) {
                parent.deleteChild(ruleNode);
            }
        } else {
            //set the rule end offset
            ruleNode.setLastToken(lastConsumedToken);
        }
    }
    
    @Override
    public void consumeToken(Token token) {
        if (backtracking > 0) {
            return;
        }
        
        //ignore the closing EOF token, we do not want it
        //it the parse tree
        if(token.getType() == Css3Lexer.EOF) {
            return ;
        }
        
        lastConsumedToken = (CommonToken)token;
        
        RuleNode ruleNode = callStack.peek();
        TokenNode elementNode = new TokenNode((CommonToken)token);
        elementNode.hiddenTokens = this.hiddenTokens;
        hiddenTokens.clear();
        ruleNode.addChild(elementNode);
        
        //set first token for all RuleNode-s in the stack without the first token set
        while(true) {
            CommonToken bound = ruleNode.getFirstToken();
            if(bound != null) {
                break;
            }
            ruleNode.setFirstToken(lastConsumedToken);
            ruleNode = (RuleNode)ruleNode.getParent();
            if(ruleNode == null) {
                break;
            }
        }
        
        
        if(resyncing) {
            System.out.println("resyncing over token " + token);
        }
        
    }

    @Override
    public void consumeHiddenToken(Token token) {
        if (backtracking > 0) {
            return;
        }
        hiddenTokens.add((CommonToken)token);
        
        if(resyncing) {
            System.out.println("resyncing over hidden token " + token);
        }
    }

    @Override
    public void beginResync() {
        resyncing = true;
    }

    @Override
    public void endResync() {
        resyncing = false;
    }
    
    @Override
    public void recognitionException(RecognitionException e) {
        if (backtracking > 0) {
            return;
        }
        //add error node
        RuleNode ruleNode = callStack.peek();
        
        final String message;
        final int from, to;
        
        if(e.token != null) {
            //invalid token found int the stream
            CommonToken token = (CommonToken)e.token;
            int unexpectedTokenCode = e.getUnexpectedType();
            CssTokenId uneexpectedToken = CssTokenId.forTokenTypeCode(unexpectedTokenCode);
            
            int[] range = CommonTokenUtil.getCommonTokenOffsetRange(token);
            from = range[0];
            to = range[1];
            
            if(uneexpectedToken == CssTokenId.EOF) {
                message = String.format("Premature end of file");
            } else {
                message = String.format("Unexpected token '%s' found at %s:%s (offset range %s-%s).", 
                        uneexpectedToken.name(), 
                        e.line, 
                        e.charPositionInLine, 
                        from, 
                        to);
            }            
        } else {
            //no token?!?!
            from = to = ruleNode.from();
            message = ruleNode.toString();
        }
        
        //create a ParsingProblem
        ProblemDescription pp = new ProblemDescription(
                from, 
                to,
                message, 
                ProblemDescription.Keys.PARSING.name(), 
                ProblemDescription.Type.ERROR);
        
        problems.add(pp);
        
        //create an error node and add it to the parse tree
        ErrorNode errorNode = new ErrorNode(e, pp, source);
        ruleNode.addChild(errorNode);
    }

    public Collection<ProblemDescription> getProblems() {
        return problems;
    }
  
}
