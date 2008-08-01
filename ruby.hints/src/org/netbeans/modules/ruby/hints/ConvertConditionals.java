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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.ast.IfNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeType;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Convert conditionals of the form "if foo; bar; end" to "bar if foo".
 * Inspired by the excellent blog entry
 *   http://langexplr.blogspot.com/2007/11/creating-netbeans-ruby-hints-with-scala_24.html
 * by Luis Diego Fallas.
 * 
 * @author Tor Norbye
 */
public class ConvertConditionals extends RubyAstRule {

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.IFNODE);
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        CompilationInfo info = context.compilationInfo;

        IfNode ifNode = (IfNode) node;
        if (ifNode.getCondition() == null) {
            // Can happen for this code:
            //   if ()
            //   end
            // (typically while editing)
            return;
        }
        Node body = ifNode.getThenBody();
        Node elseNode = ifNode.getElseBody();

        if (body != null && elseNode != null) {
            // Can't convert if-then-else conditionals
            return;
        }

        if (body == null && elseNode == null) {
            // Can't convert empty conditions
            return;
        }

        // Can't convert if !x/elseif blocks
        if (ifNode.getElseBody() != null && ifNode.getElseBody().nodeId == NodeType.IFNODE) {
            return;
        }
        
        int start = ifNode.getPosition().getStartOffset();
        if (body != null && (
                // Can't convert blocks with multiple statements
                body.nodeId == NodeType.BLOCKNODE ||
                // Already a statement modifier?
                body.getPosition().getStartOffset() <= start)) {
            return;
        } else if (elseNode != null && (
                elseNode.nodeId == NodeType.BLOCKNODE ||
                elseNode.getPosition().getStartOffset() <= start)) {
            return;
        }
        
        BaseDocument doc = context.doc;
        try {
            int keywordOffset = ConvertIfToUnless.findKeywordOffset(context, ifNode);
            if (keywordOffset == -1 || keywordOffset > doc.getLength() - 1) {
                return;
            }

            char k = doc.getText(keywordOffset, 1).charAt(0);
            if (!(k == 'i' || k == 'u')) {
                return; // Probably ternary operator, ?:
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        // If statement that is not already a statement modifier
        OffsetRange range = AstUtilities.getRange(node);

        if (RubyUtils.isRhtmlDocument(doc) || RubyUtils.isYamlDocument(doc)) {
            // Make sure that we're in a single contiguous Ruby section; if not, this won't work
            range = LexUtilities.getLexerOffsets(info, range);
            if (range == OffsetRange.NONE) {
                return;
            }

            try {
                doc.readLock();
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence ts = th.tokenSequence();
                ts.move(range.getStart());
                if (!ts.moveNext() && !ts.movePrevious()) {
                    return;
                }
                
                if (ts.offset()+ts.token().length() < range.getEnd()) {
                    return;
                }
            } finally {
                doc.readUnlock();
            }
        }
        
        
        ConvertToModifier fix = new ConvertToModifier(context, ifNode);
        
        if (fix.getEditList() == null) {
            return;
        }

        List<HintFix> fixes = Collections.<HintFix>singletonList(fix);

        String displayName = NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionals");
        Hint desc = new Hint(this, displayName, info.getFileObject(), range,
                fixes, 500);
        result.add(desc);
    }

    public String getId() {
        return "ConvertConditionals"; // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionalsDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionals");
    }

    public boolean showInTasklist() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }
    
    private class ConvertToModifier implements PreviewableFix {
        private final RubyRuleContext context;
        private IfNode ifNode;

        public ConvertToModifier(RubyRuleContext context, IfNode ifNode) {
            this.context = context;
            this.ifNode = ifNode;
        }

        public String getDescription() {
            return NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionalsFix");
        }

        public void implement() throws Exception {
            EditList edits = getEditList();
            if (edits != null) {
                edits.apply();
            }
        }
        
        public EditList getEditList() {
            try {
                BaseDocument doc = context.doc;

                Node bodyNode = ifNode.getThenBody();
                boolean isIf = bodyNode != null;
                if (bodyNode == null) {
                    bodyNode = ifNode.getElseBody();
                }
                CompilationInfo info = context.compilationInfo;
                OffsetRange bodyRange = AstUtilities.getRange(bodyNode);
                bodyRange = LexUtilities.getLexerOffsets(info, bodyRange);
                if (bodyRange == OffsetRange.NONE) {
                    return null;
                }

                String body = doc.getText(bodyRange.getStart(), bodyRange.getLength()).trim();
                if (body.endsWith(";")) {
                    body = body.substring(0, body.length()-1);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(body);
                sb.append(" ");
                sb.append(isIf ? "if" : "unless"); // NOI18N
                sb.append(" ");
                OffsetRange range = AstUtilities.getRange(ifNode.getCondition());
                range = LexUtilities.getLexerOffsets(info, range);
                if (range == OffsetRange.NONE) {
                    return null;
                }
                sb.append(doc.getText(range.getStart(), range.getLength()));

                OffsetRange ifRange = AstUtilities.getRange(ifNode);
                ifRange = LexUtilities.getLexerOffsets(info, ifRange);
                if (ifRange == OffsetRange.NONE) {
                    return null;
                }

                return new EditList(doc).replace(ifRange.getStart(), ifRange.getLength(), sb.toString(), false, 0);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean canPreview() {
            return true;
        }
    }
}
