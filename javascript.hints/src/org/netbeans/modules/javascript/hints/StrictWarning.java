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
package org.netbeans.modules.javascript.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.BrowserVersion;
import org.netbeans.modules.javascript.editing.JsParseResult;
import org.netbeans.modules.javascript.editing.SupportedBrowsers;
import org.netbeans.modules.javascript.editing.embedding.JsModel;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.hints.infrastructure.JsErrorRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsRuleContext;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Wrap builtin strict-warning errors
 * 
 * @todo Gotta pass name/node as error key such that I can properly fill in {0} etc.
 *   in the error messages! Alternatively, use error description...
 * 
 * @todo Go and write the wiki pages for these errors, at
 *         http://wiki.netbeans.org/JavaScript_-key-
 *   where -key- is the error key, minus the "msg." prefix and minus any .'s.,
 *   e.g.  http://wiki.netbeans.org/JavaScript_varhidesarg 
 * 
 *   Resources: http://www.javascriptkit.com/javatutors/serror.shtml
 *              https://bugzilla.mozilla.org/show_bug.cgi?id=378790
 *
 * @author Tor Norbye
 */
public class StrictWarning extends JsErrorRule {
    public static final String ANON_NO_RETURN_VALUE = "msg.anon.no.return.value"; // NOI18N
    public static final String BAD_OCTAL_LITERAL = "msg.bad.octal.literal"; // NOI18N
    public static final String DUP_PARAMS = "msg.dup.parms"; // NOI18N
    public static final String NO_RETURN_VALUE = "msg.no.return.value"; // NOI18N
    public static final String NO_SIDE_EFFECTS = "msg.no.side.effects"; // NOI18N
    public static final String RESERVED_KEYWORD = "msg.reserved.keyword"; // NOI18N
    public static final String RETURN_INCONSISTENT = "msg.return.inconsistent"; // NOI18N
    public static final String TRAILING_COMMA = "msg.trailing.comma"; // NOI18N
    public static final String VAR_HIDES_ARG = "msg.var.hides.arg"; // NOI18N
    public static final String VAR_REDECL = "msg.var.redecl"; // NOI18N

    // Handled by separate hint implementation, AccidentalAssignment
    //"msg.equal.as.assign", // NOI18N
    /** Public only for testing infrastructure. Others should not touch!! */
    public static final String[] KNOWN_STRICT_ERROR_KEYS = new String[] {
        // NetBeans custom rule
        TRAILING_COMMA,
        BAD_OCTAL_LITERAL, 
        RESERVED_KEYWORD,
        DUP_PARAMS, 
        RETURN_INCONSISTENT, 
        NO_RETURN_VALUE, 
        ANON_NO_RETURN_VALUE, 
        VAR_HIDES_ARG, 
        VAR_REDECL, 
        NO_SIDE_EFFECTS
    };
    private String key;
    private HintSeverity defaultSeverity = HintSeverity.WARNING;

    public StrictWarning(String key) {
        this.key = key;
    }

    public Set<String> getCodes() {
        return Collections.singleton(key);
    }

    public void run(JsRuleContext context, Error error, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        BaseDocument doc = context.doc;

        OffsetRange range = null;

        int astOffset = error.getStartPosition();
        int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
        if (lexOffset == -1) {
            return;
        }

        if (TRAILING_COMMA.equals(key)) { // NOI18N
            // See if we're targeting the applicable browsers

            if (!SupportedBrowsers.getInstance().isSupported(BrowserVersion.IE7)) { // If you want IE5.5 you're also affected
                // We don't care about this error anyway

                context.remove = true;
                return;
            }

            astOffset = (Integer) error.getParameters()[0];
            lexOffset = LexUtilities.getLexerOffset(info, astOffset);
            if (lexOffset == -1) {
                return;
            }
            range = new OffsetRange(lexOffset, lexOffset + 1);
        } else if (RESERVED_KEYWORD.equals(key)) {
            String keyword = (String) error.getParameters()[1];
            
            // The debugger keyword seems to have graduated from reserved word to
            // getting used in the field. I should consider enabling this based on specific
            // language support values
            if ("debugger".equals(keyword)) { // NOI18N
                context.remove = true;
                return;
            }

            range = new OffsetRange(lexOffset - keyword.length(), lexOffset);
        } else if (error.getParameters() != null) {
            Node node = (Node) error.getParameters()[0];

            final boolean isInconsistentReturn = RETURN_INCONSISTENT.equals(key); // NOI18N

            if (isInconsistentReturn) {
                // Find the corresponding return node
                Node c = node;
                while (c != null) {
                    if (c.getType() == Token.RETURN) {
                        node = c;
                        break;
                    }
                    c = c.getParentNode();
                }
            }

            if (key.equals(NO_SIDE_EFFECTS) && node.hasChildren() && node.getFirstChild().getType() == Token.NAME &&
                "debugger".equals(node.getFirstChild().getString())) { // NOI18N
                // Don't warn about no side effects on the debugger keyword...
                context.remove = true;
                return;
            }

            // In HTML etc ignore these
            if (/*node.getType() == Token.EMPTY && */!JsTokenId.JAVASCRIPT_MIME_TYPE.equals(info.getFileObject().getMIMEType())) {
                context.remove = true;
                return;
            }
            
            if (node.getType() == Token.EXPR_VOID) {
                Node firstChild = node.getFirstChild();
                if (firstChild != null && firstChild.getType() == Token.NAME &&
                        JsModel.isGeneratedIdentifier(firstChild.getString())) {
                    context.remove = true;
                    return;
                }
            }

            OffsetRange astRange = AstUtilities.getRange(node);
            range = LexUtilities.getLexerOffsets(info, astRange);
            if (range.getLength() == 0) {
                int start = Math.min(doc.getLength(), range.getStart());

                range = new OffsetRange(start, Math.min(doc.getLength(), start+1));
            }

            if (key.equals(NO_SIDE_EFFECTS)) {
                // When you're typing "foo.", you don't want a warning that
                // the foo range has no side effects
                JsParseResult jpr = (JsParseResult)context.parserResult;
                OffsetRange sanitizedRange = jpr.getSanitizedRange();
                if (sanitizedRange.overlaps(range) || sanitizedRange.containsInclusive(range.getStart()) ||
                        sanitizedRange.containsInclusive(range.getEnd())) {
                    context.remove = true;
                    return;
                }
            }
        } else {
            int errorOffset = lexOffset;
            errorOffset = Math.min(errorOffset, doc.getLength());
            try {
                int rowLastNonWhite = Utilities.getRowLastNonWhite(doc, errorOffset);
                if (rowLastNonWhite <= errorOffset) {
                    rowLastNonWhite = Utilities.getRowEnd(doc, errorOffset);
                    if (errorOffset == rowLastNonWhite) {
                        errorOffset = Utilities.getRowFirstNonWhite(doc, errorOffset);
                        rowLastNonWhite = Utilities.getRowLastNonWhite(doc, errorOffset) + 1;
                    }
                }
                range = new OffsetRange(errorOffset, rowLastNonWhite);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                range = OffsetRange.NONE;
            }
        }

        if (range != OffsetRange.NONE) {
            range = limitErrorToLine(doc, range, lexOffset);

            //HintFix fix = new InsertParenFix(info, offset, callNode);
            //List<HintFix> fixList = Collections.singletonList(fix);
            List<HintFix> fixList;
            if (key.equals(TRAILING_COMMA)) { // NOI18N
                fixList = new ArrayList<HintFix>(2);
                fixList.add(new RemoveTrailingCommaFix(context, lexOffset));
                fixList.add(new MoreInfoFix(key));
            } else if (key.equals(NO_SIDE_EFFECTS)) {
                Node node = (Node) error.getParameters()[0];
                if (node.getType() == Token.EXPR_VOID) {
                    fixList = new ArrayList<HintFix>(2);
                    fixList.add(new AssignToVar(context, node, true));
                    // Determine if we can return this expression
                    Node returnNode = node.getNext();
                    if (returnNode != null && returnNode.getType() == Token.RETURN &&
                            // Implicit returns only for now
                            !returnNode.hasChildren() && returnNode.getSourceStart() == returnNode.getSourceEnd()) {
                        fixList.add(new AssignToVar(context, node, false));
                    }
                    fixList.add(new MoreInfoFix(key));
                } else {
                    fixList = Collections.<HintFix>singletonList(new MoreInfoFix(key));
                }
            } else {
                fixList = Collections.<HintFix>singletonList(new MoreInfoFix(key));
            }

            String message = getDisplayName();
            // I don't have the strings to pass to the error here
            // so for now just use the original error message instead
            if (message.indexOf("{0}") != -1) { // NOI18N

                message = error.getDisplayName();
            }

            Hint desc = new Hint(this, message, info.getFileObject(), range, fixList, 500);
            result.add(desc);
        }
    }

    static OffsetRange limitErrorToLine(BaseDocument doc, OffsetRange range, int offset) {
        try {
            // Adjust offsets, since (a) Node offsets are still kinda shaky, and
            // (b) for things like block nodes we want to limit the errors to
            // a single line!
            range = range.boundTo(0, doc.getLength());
            if (range.getStart() == 0) {
                offset = Math.min(offset, doc.getLength());

                // Probably an incorrectly initialized node AST offset somewhere
                int start = Math.max(range.getStart(), Utilities.getRowStart(doc, offset));
                int end = Math.max(start, Math.min(range.getEnd(), doc.getLength()));
                end = Math.min(end, Utilities.getRowEnd(doc, start));
                if (end == start) {
                    end = Utilities.getRowEnd(doc, end);
                }
                range = new OffsetRange(start, end);
            } else {
                int start = range.getStart();
                int end = Math.min(Utilities.getRowEnd(doc, start), range.getEnd());
                range = new OffsetRange(Math.min(start,end), end);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return range;
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(StrictWarning.class, key);
    }

    public void setDefaultSeverity(HintSeverity defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return defaultSeverity;
    }

    public String getId() {
        return key;
    }

    public String getDescription() {
        return NbBundle.getMessage(StrictWarning.class, key + ".desc"); // NOI18N

    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    private static class RemoveTrailingCommaFix implements PreviewableFix {

        private final JsRuleContext context;
        private final int offset;

        public RemoveTrailingCommaFix(JsRuleContext context, int offset) {
            this.context = context;
            this.offset = offset;
        }

        public String getDescription() {
            return NbBundle.getMessage(StrictWarning.class, "RemoveTrailingCommaFix");
        }

        public void implement() throws Exception {
            EditList edits = getEditList();
            if (edits != null) {
                edits.apply();
            }
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList list = new EditList(doc);
            list.replace(offset, 1, null, false, 0);

            return list;
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean canPreview() {
            return true;
        }
    }

    private static class AssignToVar implements PreviewableFix {
        private final boolean assign;
        private final JsRuleContext context;
        private final Node node;
        private int varOffset;
        private String varName;

        public AssignToVar(JsRuleContext context, Node node, boolean assign) {
            this.context = context;
            this.node = node;
            this.assign = assign;
        }

        public String getDescription() {
            return assign ?
                NbBundle.getMessage(StrictWarning.class, "AssignToVarFix") :
                NbBundle.getMessage(StrictWarning.class, "AssignToReturnFix");
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);

            OffsetRange astRange = AstUtilities.getRange(node);
            if (astRange != OffsetRange.NONE) {
                OffsetRange lexRange = LexUtilities.getLexerOffsets(context.compilationInfo, astRange);
                if (lexRange != OffsetRange.NONE) {
                    if (assign) {
                        int offset = lexRange.getStart();
                        StringBuilder sb = new StringBuilder();
                        varName = NbBundle.getMessage(StrictWarning.class, "VarName");
                        sb.append(varName);
                        sb.append(" = "); // NOI18N
                        varOffset = offset;
                        edits.replace(offset, 0, sb.toString(), false, 0);
                    } else {
                        if (node.getNext() != null && node.getNext().getType() == Token.RETURN &&
                            !node.getNext().hasChildren()) {
                            Node returnNode = node.getNext();
                            int returnEnd = returnNode.getSourceEnd();
                            int returnStart = returnNode.getSourceStart();
                            // Is this node in the source, or added automatically because it is implicit?
                            boolean implicit = returnStart == returnEnd;
                            int offset = lexRange.getStart();
                            edits.replace(offset, 0, "return ", false, 0); // NOI18N
                            if (!implicit) {
                                int returnLength = returnEnd - returnStart;
                                if (returnEnd < doc.getLength() &&
                                        "return;".equals(doc.getText(returnStart, returnLength+1))) { // NOI18N
                                    returnLength++;
                                }
                                edits.replace(returnStart, returnLength, "", false, 0);
                            }
                        }
                    }
                }
            }

            return edits;
        }

        public void implement() throws Exception {
            EditList edits = getEditList();

            Position pos = edits.createPosition(varOffset);
            edits.apply();
            if (pos != null && pos.getOffset() != -1) {
                JTextComponent target = GsfUtilities.getPaneFor(context.compilationInfo.getFileObject());
                if (target != null) {
                    int start = pos.getOffset();
                    int end = start + varName.length();
                    target.select(start, end);
                }
            }
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean canPreview() {
            return true;
        }
    }
}
