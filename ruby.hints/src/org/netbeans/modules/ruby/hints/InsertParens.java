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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jruby.ast.Node;
import org.jruby.common.IRubyWarnings.ID;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyParser.RubyError;
import org.netbeans.modules.ruby.hints.infrastructure.RubyErrorRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * Offer to insert missing parentheses for ambiguous parenthesis errors
 * 
 * @todo Check http://eigenclass.org/hiki/ruby-warnings-SEX-and-stds
 *   In particular, handle the '&' interpreted as arg prefix warning which occurs
 *   a lot in Mephisto code (and probably elsewhere - I googled for an explanation
 *   and instead found -tons- of ruby output logs spitting out this warning - this
 *   and '*' interpreted as argument prefix
 * 
 * @author Tor Norbye
 */
public class InsertParens extends RubyErrorRule {

    public Set<ID> getCodes() {
       //        Set<String> s = new HashSet<String>();
       //        s.add("`*' interpreted as argument prefix");
       //        s.add("`&' interpreted as argument prefix");
       //        s.add("parenthesize argument(s) for future version");
       //        return s;
        //return Collections.singleton("parenthesize argument(s) for future version");
        return Collections.singleton(ID.PARENTHISE_ARGUMENTS);
    }

    public void run(RubyRuleContext context, RubyError error,
             List<Hint> result) {
        CompilationInfo info = context.compilationInfo;

        Node root = AstUtilities.getRoot(info);
        if (root != null) {
            int astOffset = error.getStartPosition();
            AstPath path = new AstPath(root, astOffset);
            Node node = path.leaf();
            if (node != null) {
                OffsetRange range = AstUtilities.getRange(node);
                Node callNode = null;
                if (AstUtilities.isCall(path.leafParent())) {
                    callNode = path.leafParent();
                } else if (path.leafGrandParent() != null && AstUtilities.isCall(path.leafGrandParent())) {
                    callNode = path.leafGrandParent();
                } else if (AstUtilities.isCall(node)) {
                    callNode = node;
                }
                if (callNode != null) {
                    HintFix fix = new InsertParenFix(context, callNode);
                    List<HintFix> fixList = Collections.singletonList(fix);
                    range = LexUtilities.getLexerOffsets(info, range);
                    if (range != OffsetRange.NONE) {
                        Hint desc = new Hint(this, getDisplayName(), info.getFileObject(), range, fixList, 500);
                        result.add(desc);
                    }
                }
            }
        }
    }

    public boolean appliesTo(RuleContext context) {
        CompilationInfo info = context.compilationInfo;
        // Skip for RHTML files for now - isn't implemented properly
        return info.getFileObject().getMIMEType().equals("text/x-ruby");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(InsertParens.class, "InsertParens");
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public boolean showInTasklist() {
        return false;
    }

    private static class InsertParenFix implements PreviewableFix {

        private final RubyRuleContext context;
        private final Node node;

        InsertParenFix(RubyRuleContext context, Node node) {
            this.context = context;
            this.node = node;
        }

        public String getDescription() {
            return NbBundle.getMessage(InsertParens.class,"InsertParenFix");
        }

        public void implement() throws Exception {
            getEditList().apply();
        }
        
        public EditList getEditList() throws Exception {
            ISourcePosition pos = node.getPosition();
            int endOffset = pos.getEndOffset();
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);
            if (endOffset > doc.getLength()) {
                return edits;
            }

            // Insert parentheses
            assert AstUtilities.isCall(node);
            OffsetRange astRange = AstUtilities.getCallRange(node);
            OffsetRange range = LexUtilities.getLexerOffsets(context.compilationInfo, astRange);
            if (range == OffsetRange.NONE) {
                return edits;
            }
            int insertPos = range.getEnd();
            // Check if I should remove a space; e.g. replace "foo arg" with "foo(arg"
            if (Character.isWhitespace(doc.getText(insertPos, 1).charAt(0))) {
                edits.replace(insertPos, 1, "(", false, 0); // NOI18N
            } else {
                edits.replace(insertPos, 0, "(", false, 0); // NOI18N
                endOffset++;
            }

            // Insert )
            // Look backwards from endOffset to see the first nonspace char and insert
            // after that
            for (int i = endOffset-1; i >= 0; i--) {
                // TODO - find more efficient doc iterator!
                char c = doc.getText(i, 1).charAt(0);
                if (Character.isWhitespace(c)) {
                    continue;
                } else {
                    endOffset = i+1;
                    break;
                }
            }
            edits.replace(endOffset, 0, ")", false, 1); // NOI18N
            
            return edits;
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
