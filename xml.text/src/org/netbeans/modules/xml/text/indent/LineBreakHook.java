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
package org.netbeans.modules.xml.text.indent;

import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.openide.util.Exceptions;

/**
 * This Typing Hook works in conjuction with the XML reformatting, which happens
 * when a line break is inserted. If the formatter inserts an empty line, this Hook
 * positions the caret at the end of the line immediately following the opening tag
 * assuming the line is properly indented already.
 * <p/>
 * The code tries to avoid repositioning in all other situations.
 * 
 * 
 * @author sdedic
 */
public class LineBreakHook implements TypedBreakInterceptor {
    private static final Logger LOG = Logger.getLogger(LineBreakHook.class.getName());
    
    /**
     * Checks conditions for repositioning the caret:
     * <ul>
     * <li>Some text precedes the insertion point at the same line
     * <li>Some text follows the caret position at the same line
     * <li>The preceding text (whitespace skipped) is the opening tag
     * <li>The following text (ws skipped) is closing tag
     * <li>The tags are on different lines and there is a line in between them
     * </ul>
     * If something is not true, the method does nothing. If all conditions are met,
     * it positions the caret at the end of the line just below the insertion point.
     * 
     * @param context the hook context
     * @throws BadLocationException programmer's error :)
     */
    private void repositionCaret(Context context) throws BadLocationException {
        if (!(context.getDocument() instanceof BaseDocument)) {
            return;
        }
        BaseDocument doc = (BaseDocument)context.getDocument();

        int insertPos = context.getCaretOffset();
        int caretPos = context.getComponent().getCaretPosition();
        int lineStartPos = Utilities.getRowStart(doc, insertPos);
        int nonWhiteBefore = Utilities.getFirstNonWhiteBwd(doc, insertPos, 
                lineStartPos);
        if (nonWhiteBefore == -1) {
            // ignore if not directly at the line with the start tag
            return;
        }
        int lineEndPos = Utilities.getRowEnd(doc, caretPos);
        int nonWhiteAfter = Utilities.getFirstNonWhiteFwd(doc, caretPos, lineEndPos);
        if (nonWhiteAfter == -1) {
            // ignore if the (supposedly) closing tag is not immediately after
            // the caret + whitespace
            return;
        }
        
        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence seq = h.tokenSequence();
        // check the actual tokens
        seq.move(nonWhiteBefore + 1);
        // check whether the preceding tokens form a opening tag
        int closingIndex = followsOpeningTag(seq);
        if (closingIndex == -1) {
            return;
        }
        // check that the following token (after whitespace(s)) is a 
        // opening tag
        seq.move(nonWhiteAfter);
        if (!precedesClosingTag(seq)) {
            return;
        }
        // now we need to position the caret at the END of the line immediately 
        // preceding the closing tag. Assuming it's already indented
        int startClosingLine = Utilities.getRowStart(doc, nonWhiteAfter);
        if (startClosingLine == lineStartPos) {
            // open and close tag on the same line for some reason
            return;
        }
        int nextLineStart = Utilities.getRowStart(doc, insertPos, 1);
        if (nextLineStart >= startClosingLine) {
            // no change, we're at the line with closing tag
            return;
        }
        int newCaretPos = Utilities.getRowEnd(doc, nextLineStart);
        context.getComponent().getCaret().setDot(newCaretPos);
    }
    
    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        context.getDocument().render(new Runnable() {
            public void run() {
                try {
                    repositionCaret(context);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    private boolean precedesClosingTag(TokenSequence seq) {
        if (!seq.moveNext()) {
            return false;
        }
        // all whitespace should have been skipped by now
        Token tukac = seq.token();
        if (tukac.id() != XMLTokenId.TAG) {
            return false;
        }
        String text = tukac.text().toString();
        return text.startsWith("</");
    }
    
    /**
     * Determines whether the token sequence immediately follows an opening tag
     * (possibly with some whitespace in between the token sequence and the
     * opening tag ending > sign.
     * 
     * @param seq positioned sequence
     * @return index just after the > sign, or -1 if opening tag is not found
     */
    private int followsOpeningTag(TokenSequence seq) {
        int closingIndex = -1;
        while (seq.movePrevious()) {
            Token tukac = seq.token();
            switch ((XMLTokenId)tukac.id()) {
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                    if (closingIndex == -1) {
                        // in the middle of a tag
                        return -1;
                    }
                    
                case WS:
                    continue;

                case TAG: {
                    String text = tukac.text().toString();
                    // it may be the closing tag
                    if (">".equals(text)) {
                        if (closingIndex > -1) {
                            return -1;
                        }
                        closingIndex = seq.offset() + tukac.length();
                        continue;
                    }
                    if (text.startsWith("<") && text.length() > 1 && text.charAt(1) != '/') {
                        // found start tag
                        return closingIndex;
                    }
                    return -1;
                }
                    
                default:
                    return -1;
            }
        }
        return -1;
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        // no op
        return false;
    }

    @Override
    public void cancelled(Context context) {
        // no op
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        // no op
    }
    
    @MimeRegistration(mimeType="text/xml", service=TypedBreakInterceptor.Factory.class)
    public static class F implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new LineBreakHook();
        }
    }
}
