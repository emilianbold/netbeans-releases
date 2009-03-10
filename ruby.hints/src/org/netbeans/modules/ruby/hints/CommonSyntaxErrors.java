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
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.nb.common.IRubyWarnings.ID;
import org.jruby.nb.lexer.yacc.SyntaxException.PID;
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
import org.netbeans.modules.ruby.RubyParser.RubyError;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyErrorRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Rule which identifies common syntax errors and offers to help
 * 
 * @author Tor Norbye
 */
public class CommonSyntaxErrors extends RubyErrorRule {

    public Set<ID> getCodes() {
        // Add more as necessary
        //return Collections.singleton("Syntax error, unexpected '=' "); // NOI18N
        return Collections.singleton(ID.SYNTAX_ERROR);
    }

    public void run(RubyRuleContext context, RubyError error, List<Hint> result) {
        ParserResult info = context.parserResult;

        PID pid = (PID) error.getParameters()[0];
        if (pid != PID.GRAMMAR_ERROR) {
            return;
        }

        String message = error.getDisplayName();
        if (message.indexOf("'='") == -1) { // NOI18N
            return;
        } 
        
        // See if it's a "begin"
        try {
            // TODO - if we get many codes, switch on these here!
            BaseDocument doc = context.doc;
            int astOffset = error.getStartPosition();
            int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
            if (lexOffset == -1) {
                return;
            }
            if ((lexOffset < doc.getLength()-"begin".length()) && // NOI18N
                    "begin".equals(doc.getText(lexOffset, "begin".length()))) { // NOI18N
                OffsetRange range = new OffsetRange(lexOffset-1, lexOffset+"=begin".length());
                HintFix fix = new FixDocIndent(context, lexOffset-1);
                List<HintFix> fixList = Collections.singletonList(fix);
                String displayName = NbBundle.getMessage(CommonSyntaxErrors.class, "DontIndentDocs");
                Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), range, fixList, 500);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public String getDisplayName() {
        return "Common Syntax Errors";
    }

    public String getId() {
        return "CommonSyntaxErrors"; // NOI18N
    }

    public String getDescription() {
        return "";
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    private class FixDocIndent implements PreviewableFix {
        private final RubyRuleContext context;
        private final int equalOffset;
        
        private FixDocIndent(RubyRuleContext context, int equalOffset) {
            this.context = context;
            this.equalOffset = equalOffset;
        }

        public String getDescription() {
            return NbBundle.getMessage(CommonSyntaxErrors.class, "ReindentBegin");
        }

        public void implement() throws Exception {
            getEditList().apply();
        }
        
        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;

            if (equalOffset > doc.getLength()) {
                return null; // recent edits
            }

            EditList edits = new EditList(doc);

            int rowStart = Utilities.getRowStart(doc, equalOffset);
            if (Utilities.getRowFirstNonWhite(doc, equalOffset) < equalOffset) {
                // There's something else on this line! Create a newline instead!
                edits.replace(equalOffset, 0, "\n", false, 0);
            } else {
                edits.replace(rowStart, equalOffset-rowStart, null, false, 0);
            }
            int nextRow = Utilities.getRowEnd(doc, rowStart)+1;
            if (nextRow < doc.getLength()) {
                String text = doc.getText(nextRow, doc.getLength()-nextRow);
                int index = text.indexOf("=end");
                if (index != -1) {
                    int beginIndex = text.indexOf("=begin");
                    if (index < beginIndex || beginIndex == -1) {
                        int offset = nextRow+index;
                        rowStart = Utilities.getRowStart(doc, offset);
                        if (Utilities.getRowFirstNonWhite(doc, offset) < offset) {
                            // There's something else on this line! Create a newline instead!
                            edits.replace(offset, 0, "\n", false, 1);
                        } else {
                            edits.replace(rowStart, offset-rowStart, null, false, 1);
                        }
                    }
                }
            }
            
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

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

}
