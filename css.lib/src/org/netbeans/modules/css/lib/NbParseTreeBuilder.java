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
import java.util.List;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.BlankDebugEventListener;

import java.util.Stack;
import java.util.ArrayList;
import java.util.Arrays;
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

    //ignore 'syncToIdent' rule - the DBG.enter/exit/Rule calls are generated
    //automatically by ANTLR but we do not care about them since 
    //the error recovery implementation in syncToSet(...)
    //calls DBG.enter/exit/Rule("recovery") itself.
    private String[] IGNORED_RULES = new String[]{"syncToFollow", "syncTo_IDENT_RBRACE"}; //must be sorted alphabetically!
    
    Stack<RuleNode> callStack = new Stack<RuleNode>();
    List<CommonToken> hiddenTokens = new ArrayList<CommonToken>();
    private int backtracking = 0;
    private CommonToken lastConsumedToken;
    private boolean resyncing;
    private CharSequence source;
    static boolean debug_tokens = false; //testing 

    private List<ErrorNode> errorNodes = new ArrayList<ErrorNode>();
    
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

    private boolean isIgnoredRule(String ruleName) {
        return Arrays.binarySearch(IGNORED_RULES, ruleName) >= 0;
    }
    
    @Override
    public void enterRule(String filename, String ruleName) {
        if (backtracking > 0) {
            return;
        }
        if(isIgnoredRule(ruleName)) {
            return ;
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
        if(isIgnoredRule(ruleName)) {
            return ;
        }

        RuleNode ruleNode = callStack.pop();
        if (ruleNode.getChildCount() == 0) {
            RuleNode parent = (RuleNode) ruleNode.getParent();
            if (parent != null) {
                parent.deleteChild(ruleNode);
            }
        } else {
            //set the rule end offset
            if (lastConsumedToken != null) {
                ruleNode.setLastToken(lastConsumedToken);
            }
        }
    }

    @Override
    public void consumeToken(Token token) {
        if (backtracking > 0) {
            return;
        }

        if (debug_tokens) {
            CommonToken ct = (CommonToken) token;
            int[] ctr = CommonTokenUtil.getCommonTokenOffsetRange(ct);
            System.out.println(token + "(" + ctr[0] + "-" + ctr[1] + ")");
        }

        //ignore the closing EOF token, we do not want it
        //it the parse tree
        if (token.getType() == Css3Lexer.EOF) {
            return;
        }

        //also ignore error tokens - they are added as children of ErrorNode-s in the recognitionException(...) method
        if(token.getType() == Token.INVALID_TOKEN_TYPE) {
            return ;
        }
        
        lastConsumedToken = (CommonToken) token;

        RuleNode ruleNode = callStack.peek();
        TokenNode elementNode = new TokenNode((CommonToken) token);
        elementNode.hiddenTokens = this.hiddenTokens;
        hiddenTokens.clear();
        ruleNode.addChild(elementNode);

        updateFirstTokens(ruleNode, lastConsumedToken);

        
        
        if (resyncing) {
//            System.out.println("resyncing over token " + token);
        }

    }

    //set first token for all RuleNode-s in the stack without the first token set
    private void updateFirstTokens(RuleNode ruleNode, CommonToken token) {
        while (true) {
            
            if (ruleNode.from() != -1) {
                break;
            }
            ruleNode.setFirstToken(token);
            ruleNode = (RuleNode) ruleNode.getParent();
            if (ruleNode == null) {
                break;
            }
        }
    }
    
    @Override
    public void consumeHiddenToken(Token token) {
        if (backtracking > 0) {
            return;
        }

        if (debug_tokens) {
            CommonToken ct = (CommonToken) token;
            int[] ctr = CommonTokenUtil.getCommonTokenOffsetRange(ct);
            System.out.println(token + "(" + ctr[0] + "-" + ctr[1] + ")");
        }

        hiddenTokens.add((CommonToken) token);

        if (resyncing) {
//            System.out.println("resyncing over hidden token " + token);
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
        RuleNode ruleNode = callStack.peek();

        String message;
        int from, to;

        assert e.token != null;

        //invalid token found int the stream
        CommonToken unexpectedToken = (CommonToken) e.token;
        int unexpectedTokenCode = e.getUnexpectedType();
        CssTokenId uneexpectedToken = CssTokenId.forTokenTypeCode(unexpectedTokenCode);

        
        //let the error range be the area between last consumed token end and the error token end
        from = lastConsumedToken != null
                ? CommonTokenUtil.getCommonTokenOffsetRange(lastConsumedToken)[1]
                : 0;

        to = CommonTokenUtil.getCommonTokenOffsetRange(unexpectedToken)[0]; //beginning of the unexpected token

        if(uneexpectedToken == CssTokenId.ERROR) {
            //for error tokens always include them to the error node range,
            //they won't be matched by any other rule. Otherwise 
            //limit the error node to the beginning of the unexpected token
            //since the token may be matched later
            to++;
        }
        
        if (uneexpectedToken == CssTokenId.EOF) {
            message = String.format("Premature end of file");
        } else {
            message = String.format("Unexpected token '%s' found at %s:%s (offset range %s-%s).",
                    uneexpectedToken.name(),
                    e.line,
                    e.charPositionInLine,
                    from,
                    to);
        }
        //create a ParsingProblem
        ProblemDescription problemDescription = new ProblemDescription(
                from,
                to,
                message,
                ProblemDescription.Keys.PARSING.name(),
                ProblemDescription.Type.ERROR);
        
        //create an error node and add it to the parse tree
        ErrorNode errorNode = new ErrorNode(from, to, problemDescription, source);
        ruleNode.addChild(errorNode);
        errorNode.setParent(ruleNode);

        if(uneexpectedToken == CssTokenId.ERROR) {
            //if the unexpected token is error token, add the token as a child of the error node
            TokenNode tokenNode = new TokenNode(unexpectedToken);
            errorNode.addChild(tokenNode);
            tokenNode.setParent(errorNode);
        }
        
        //create and artificial error token so the rules on stack can properly set their ranges
        lastConsumedToken = new CommonToken(Token.INVALID_TOKEN_TYPE);
        lastConsumedToken.setStartIndex(from);
        lastConsumedToken.setStopIndex(to - 1); // ... ( -1 => last *char* index )
        
        errorNodes.add(errorNode);
 
    }

    @Override
    public void terminate() {
        //Finally after the parsing is done fix the error nodes and their predecessors.
        //This fixes the problem with rules where RecognitionException happened
        //but the errorneous or missing token has been matched in somewhere further
        super.terminate();
        
        for(ErrorNode en : errorNodes) {
            RuleNode n = en;
            for(;;) {
                if(n == null) {
                    break;
                }
                if(n.from() == -1 || n.to() == -1) {
                    //set the node range to the same range as the error node
                    n.from = en.from();
                    n.to = en.to();
                }
                n = (RuleNode)n.parent();
            }
            
        }
        
    }
    
    public Collection<ProblemDescription> getProblems() {
        Collection<ProblemDescription> problems = new ArrayList<ProblemDescription>();
        for(ErrorNode errorNode : errorNodes) {
            problems.add(errorNode.getProblemDescription());
        }
        return problems;
    }
}
