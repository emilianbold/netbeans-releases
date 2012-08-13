/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.hints;

import com.oracle.nashorn.ir.BinaryNode;
import com.oracle.nashorn.ir.Block;
import com.oracle.nashorn.ir.DoWhileNode;
import com.oracle.nashorn.ir.ExecuteNode;
import com.oracle.nashorn.ir.ForNode;
import com.oracle.nashorn.ir.IfNode;
import com.oracle.nashorn.ir.Node;
import com.oracle.nashorn.ir.ObjectNode;
import com.oracle.nashorn.ir.PropertyNode;
import com.oracle.nashorn.ir.VarNode;
import com.oracle.nashorn.ir.WhileNode;
import com.oracle.nashorn.parser.TokenType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript2.editor.hints.JsHintsProvider.JsRuleContext;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.PathNodeVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class JsConventionRule implements Rule.AstRule{
    public static final String JSCONVENTION_HINTS = "jsconvention.line.hints"; //NOI18N
    
    void computeHintsImpl(JsRuleContext context, List<Hint> hints) {
        ConventionVisitor conventionVisitor = new ConventionVisitor(this);
        conventionVisitor.process(context, hints);
    }
            
    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JSCONVENTION_HINTS);
    }

    @Override
    public String getId() {
        return "jsconvention.hint"; //NOI18N
    }

    @Override
    @NbBundle.Messages("JsConventionHintDesc=JavaScript Code Convention Hint")
    public String getDescription() {
        return Bundle.JsConventionHintDesc();
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    @NbBundle.Messages("JsConventionHintDisplayName=JavaScript Code Convention")
    public String getDisplayName() {
        return Bundle.JsConventionHintDisplayName();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
    
    private static class ConventionVisitor extends PathNodeVisitor {

        private List<Hint> hints;
        private JsRuleContext context;
        private final Rule rule;
        
        public ConventionVisitor(Rule rule) {
            this.rule = rule;
        }
        
        @NbBundle.Messages("ExpectedInstead=Expected \"{0}\" and instead saw \"{1}\".")
        public void process(JsRuleContext context, List<Hint> hints) {
            this.hints = hints;
            this.context = context;
            context.getJsParserResult().getRoot().accept(this);
        }
        
        @NbBundle.Messages("MissingSemicolon=Expected semicolon ; after \"{0}\".")
        private void checkSemicolon(int offset) {
            int fileOffset = context.parserResult.getSnapshot().getOriginalOffset(offset);
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(context.doc, fileOffset);
            ts.move(fileOffset);
            if(ts.movePrevious() && ts.moveNext()) {
                JsTokenId id = ts.token().id();
                if(id == JsTokenId.STRING_END && ts.moveNext()) {
                    id = ts.token().id();
                }
                if (id == JsTokenId.EOL && ts.movePrevious()) {
                    id = ts.token().id();
                }
                if (id != JsTokenId.OPERATOR_SEMICOLON) {
                    LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE));
                    hints.add(new Hint(rule, Bundle.MissingSemicolon(ts.token().text().toString()), 
                            context.getJsParserResult().getSnapshot().getSource().getFileObject(), 
                            new OffsetRange(ts.offset(), ts.offset() + ts.token().length()), null, 500));
                }
            }
        }

        @NbBundle.Messages("AssignmentCondition=Expected a conditional expression and instead saw an assignment.")
        private void checkCondition(Node condition) {
            if(condition instanceof BinaryNode) {
                BinaryNode binaryNode = (BinaryNode)condition;
                if (binaryNode.isAssignment()) {
                    hints.add(new Hint(rule, Bundle.AssignmentCondition(), 
                            context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                            ModelUtils.documentOffsetRange(context.getJsParserResult(), condition.getStart(), condition.getFinish()), null, 500));
                } else {
                    String message = null;
                    switch(binaryNode.tokenType()) {
                        case EQ:
                            message = Bundle.ExpectedInstead("===", "=="); //NOI18N
                            break;
                        case NE:
                            message = Bundle.ExpectedInstead("!==", "!="); //NOI18N
                            break;
                    }
                    if (message != null) {
                        hints.add(new Hint(rule, message, 
                            context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                            ModelUtils.documentOffsetRange(context.getJsParserResult(), condition.getStart(), condition.getFinish()), null, 500));
                    }
                }
            }
        }

        
       
        @Override
        public Node visit(DoWhileNode doWhileNode, boolean onset) {
            if (onset) {
                checkCondition(doWhileNode.getTest());
            }
            return super.visit(doWhileNode, onset);
        }
        
        
        @Override
        public Node visit(ExecuteNode executeNode, boolean onset) {
            if (onset && !(executeNode.getExpression() instanceof Block)) {
                checkSemicolon(executeNode.getFinish());
            }
            return super.visit(executeNode, onset);
        }

        @Override
        public Node visit(ForNode forNode, boolean onset) {
            if (onset) {
                checkCondition(forNode.getTest());
            }
            return super.visit(forNode, onset);
        }

        @Override
        public Node visit(IfNode ifNode, boolean onset) {
            if (onset) {
                checkCondition(ifNode.getTest());
            }
            return super.visit(ifNode, onset);
        }
        
        

        @Override
        @NbBundle.Messages("Unexpected=Unexpected \"{0}\".")
        public Node visit(ObjectNode objectNode, boolean onset) {
            if (onset) {
                int offset = context.parserResult.getSnapshot().getOriginalOffset(objectNode.getFinish());
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(context.doc, offset);
                ts.move(offset);
                if(ts.movePrevious() && ts.moveNext()) {
                    LexUtilities.findPrevious(ts, Arrays.asList(
                            JsTokenId.EOL, JsTokenId.WHITESPACE, 
                            JsTokenId.BRACKET_RIGHT_CURLY, JsTokenId.LINE_COMMENT,
                            JsTokenId.BLOCK_COMMENT));
                    if (ts.token().id() == JsTokenId.OPERATOR_COMMA) {
                        hints.add(new Hint(rule, Bundle.Unexpected(ts.token().text().toString()), 
                            context.getJsParserResult().getSnapshot().getSource().getFileObject(), 
                            new OffsetRange(ts.offset(), ts.offset() + ts.token().length()), null, 500));
                    }
                }
            }
            return super.visit(objectNode, onset);
        }

        @Override
        public Node visit(VarNode varNode, boolean onset) {
            if (onset) {
                checkSemicolon(varNode.getFinish());
            }
            return super.visit(varNode, onset);
        }

        @Override
        public Node visit(WhileNode whileNode, boolean onset) {
            if (onset) {
                checkCondition(whileNode.getTest());
            }
            return super.visit(whileNode, onset);
        }
        
    }
}
