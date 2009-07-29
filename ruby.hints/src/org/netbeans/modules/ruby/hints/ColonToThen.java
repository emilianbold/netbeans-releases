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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.WhenNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Convert "when foo : bar" to "when foo then bar" as required by Ruby 1.9
 * @todo This is not particular to when/case - the following is also illegal in 1.9
 *   if true: puts "True!"
 *   end
 * @todo Add a reference link to the wiki page with more information about the quickfix and 
 *   references
 * 
 * @author Tor Norbye
 */
public class ColonToThen extends RubyAstRule {

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.WHENNODE);
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        ParserResult info = context.parserResult;

        WhenNode when = (WhenNode)node;
        Node body = when.getBodyNode();
        if (RubyHints.isNullOrInvisible(body)) {
            return;
        }
        
        // See if the child contains
        BaseDocument doc = context.doc;
        try {
            // (1) make sure the body is on the same line as the when, and
            // (2) the separator is ":", not "then" or something else
            int astWhenStart = when.getPosition().getStartOffset();
            int astBodyStart = body.getPosition().getStartOffset();
            int lexWhenStart = LexUtilities.getLexerOffset(info, astWhenStart);
            int lexBodyStart = LexUtilities.getLexerOffset(info, astBodyStart);
            if (lexWhenStart == -1 || lexBodyStart == -1) {
                return;
            }
            int docLength = doc.getLength();
            if (lexWhenStart > docLength || lexBodyStart > docLength) {
                return;
            }
            if (Utilities.getRowEnd(doc, lexBodyStart) !=
                    Utilities.getRowEnd(doc, lexWhenStart)) {
                return;
            }

            int offset = -1;
            try {
                // Check tokens - look for ":" as opposed to then
                doc.readLock();
                TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc,
                       lexBodyStart);
                if (ts == null) {
                    return;
                }
                ts.move(lexBodyStart);
                while (ts.movePrevious()) {
                    Token<? extends RubyTokenId> token = ts.token();
                    TokenId id = token.id();
                    if (id == RubyTokenId.WHITESPACE) {
                        continue;
                    } else if (id == RubyTokenId.NONUNARY_OP) {
                        String s = token.text().toString();
                        if (":".equals(s)) {
                            offset = ts.offset();
                            break;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } finally {
                doc.readUnlock();
            }
            
            if (offset == -1) {
                return;
            }
            OffsetRange range = new OffsetRange(offset, offset+1);
            String displayName = NbBundle.getMessage(ColonToThen.class, "ColonToThenGutter");
            List<HintFix> fixes = new ArrayList<HintFix>(3);
            fixes.add(new ColonFix(doc, offset, INSERT_THEN));
            fixes.add(new ColonFix(doc, offset, INSERT_SEMICOLON));
            fixes.add(new ColonFix(doc, offset, INSERT_NEWLINE));
            Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), range,
                    fixes, 150);
            result.add(desc);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getId() {
        return "ColonToThen"; // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(ColonToThen.class, "ColonToThenDesc");
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
        return NbBundle.getMessage(ColonToThen.class, "ColonToThen");
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    // ColonFix possibilities
    private static final int INSERT_THEN = 1;
    private static final int INSERT_NEWLINE = 2;
    private static final int INSERT_SEMICOLON = 3;
    
    private static class ColonFix implements PreviewableFix {
        private BaseDocument doc;
        private int offset;
        private int mode;
        

        public ColonFix(BaseDocument doc, int offset, int mode) {
            this.doc = doc;
            this.offset = offset;
            this.mode = mode;
        }

        public String getDescription() {
            switch (mode) {
                case INSERT_SEMICOLON: 
                    return NbBundle.getMessage(Deprecations.class, "ColonToThenFixSemi");
                case INSERT_THEN: 
                    return NbBundle.getMessage(Deprecations.class, "ColonToThenFix");
                case INSERT_NEWLINE: 
                default:
                    return NbBundle.getMessage(Deprecations.class, "ColonToThenFixNewline");
            }
        }

        public void implement() throws Exception {
            getEditList().apply();
        }

        public EditList getEditList() throws Exception {
            EditList list = new EditList(doc);
            switch (mode) {
            case INSERT_NEWLINE:
                list.setFormatAll(false);
                list.replace(offset, 1, "\n", true, 0); // NOI18N
                break;
            case INSERT_THEN: {
                String s = doc.getText(offset, 3);
                StringBuilder sb = new StringBuilder();
                if (!Character.isWhitespace(doc.getText(offset-1, 1).charAt(0))) {
                    sb.append(' ');
                }
                sb.append("then"); // NOI18N
                if (offset < doc.getLength()-2) {
                    if (!Character.isWhitespace(doc.getText(offset+1, 1).charAt(0))) {
                        sb.append(' ');
                    }
                }
                list.replace(offset, 1, sb.toString(), false, 0);
                break;
                }
            case INSERT_SEMICOLON:
                list.replace(offset, 1, ";", false, 0); // NOI18N
                break;
            }
            
            return list;
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
