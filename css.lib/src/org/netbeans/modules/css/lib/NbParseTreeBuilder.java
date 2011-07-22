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
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.debug.BlankDebugEventListener;

import java.util.Stack;
import java.util.ArrayList;
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
    static boolean debug_tokens = false; //testing 

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

        //ignore 'syncToIdent' rule - the DBG.enter/exit/Rule calls are generated
        //automatically by ANTLR but we do not care about them since 
        //the error recovery implementation in syncToSet(...)
        //calls DBG.enter/exit/Rule("recovery") itself.
        //
        //TODO - possibly remove the syncToIdent rule and use the syncToSet(BitSet.of(IDENT)) directly
        if ("syncToIdent".equals(ruleName)) {
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
        if ("syncToIdent".equals(ruleName)) {
            return;
        }

        RuleNode ruleNode = callStack.pop();
        //error nodes handling - since the error node is pushed by the recognitionException method
        //it must be popped explicitly since the exitRule rule is not called
        if(ruleNode.type() == NodeType.error) {
            if (lastConsumedToken != null) {
                ruleNode.setLastToken(lastConsumedToken);
            }
            ruleNode = callStack.pop(); //pop next rule
        }
        
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

//        System.err.println("consume token " + token);
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

        lastConsumedToken = (CommonToken) token;

        RuleNode ruleNode = callStack.peek();
        TokenNode elementNode = new TokenNode((CommonToken) token);
        elementNode.hiddenTokens = this.hiddenTokens;
        hiddenTokens.clear();
        ruleNode.addChild(elementNode);

        updateFirstTokens(ruleNode, lastConsumedToken);

        
        
        if (resyncing) {
            System.out.println("resyncing over token " + token);
        }

    }

    //set first token for all RuleNode-s in the stack without the first token set
    private void updateFirstTokens(RuleNode ruleNode, CommonToken token) {
        while (true) {
            CommonToken bound = ruleNode.getFirstToken();
            if (bound != null) {
                break;
            }
            ruleNode.setFirstToken(token);
            ruleNode = (RuleNode) ruleNode.getParent();
            if (ruleNode == null) {
                break;
            }
        }
    }
    
    //in case of recognition error the ancestor of the error node needs to
    //be updated so they have the same node range
    private void updateErrorNodeAncestorRanges(ErrorNode errorNode) {
        RuleNode node = errorNode;
        while (true) {
            node = (RuleNode)node.parent();
            if (node == null) {
                break;
            }
            if(node.from() == -1 && node.to() == -1) {
                node.from = errorNode.from();
                node.to = errorNode.to();
            } else {
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
//        System.err.println("recognition exception " + e);

        if (backtracking > 0) {
            return;
        }
        //add error node
        RuleNode ruleNode = callStack.peek();

        final String message;
        final int from, to;

        assert e.token != null;

        //invalid token found int the stream
        CommonToken token = (CommonToken) e.token;
        int unexpectedTokenCode = e.getUnexpectedType();
        CssTokenId uneexpectedToken = CssTokenId.forTokenTypeCode(unexpectedTokenCode);

        //let the error range be the area between last consumed token end and the error token end
        from = lastConsumedToken != null
                ? CommonTokenUtil.getCommonTokenOffsetRange(lastConsumedToken)[1]
                : 0;

        to = CommonTokenUtil.getCommonTokenOffsetRange(token)[1];

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
        
        problems.add(problemDescription);

        //create an error node and add it to the parse tree
        ErrorNode errorNode = new ErrorNode(from, to, problemDescription, source);
        ruleNode.addChild(errorNode);
        errorNode.setParent(ruleNode);
        
        updateErrorNodeAncestorRanges(errorNode);
        
        //push the error node so the subsequent consumeToken will add the error node to the error rule
        //the error node must be explicitly pop-ed!
        callStack.push(errorNode); 
 
    }

    public Collection<ProblemDescription> getProblems() {
        return problems;
    }
}
