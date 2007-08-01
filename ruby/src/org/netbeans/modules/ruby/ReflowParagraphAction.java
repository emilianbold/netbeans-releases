/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import javax.swing.text.BadLocationException;
import org.netbeans.api.gsf.EditorOptions;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.util.Exceptions;

/**
 * Reflow paragraphs (currently, rdoc comments and =begin/=end documentatio sections.)
 * Take RDoc conventions into consideration such that preformatted rdoc text is left alone,
 * bulleted lists get properly aligned, etc.
 * 
 * @author Tor Norbye
 */
public class ReflowParagraphAction extends AbstractAction {

    public ReflowParagraphAction() {
        super(NbBundle.getMessage(ReflowParagraphAction.class, "ruby-reflow-paragraph")); // NOI18N
        putValue("PopupMenuText", NbBundle.getBundle(ReflowParagraphAction.class).getString("editor-popup-ruby-reflow-paragraph")); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent ev) {
        JEditorPane pane = NbUtilities.getOpenPane();

        if (pane != null) {
            actionPerformed(pane);
        }
    }

    void actionPerformed(final JTextComponent target) {
        FileObject fo = NbUtilities.findFileObject(target);
        if (target.getCaret() == null) {
            return;
        }
        int offset = target.getCaret().getDot();

        if (fo != null) {
            new ParagraphFormatter(false).reflowParagraph(target, offset);
        }
    }
    
    public void reflowEditedComment(JTextComponent target) {
        FileObject fo = NbUtilities.findFileObject(target);
        if (target.getCaret() == null) {
            return;
        }
        int offset = target.getCaret().getDot();

        if (fo != null) {
            new ParagraphFormatter(true).reflowParagraph(target, offset);
        }
    }

    private class ParagraphFormatter {
        private JTextComponent target;
        private BaseDocument doc;
        private int oldCaretPosition;
        private boolean inVerbatim;
        private boolean indentedList;
        private boolean inList;
        private int listIndentation;
        private boolean documentation;
        private final StringBuilder sb = new StringBuilder(500);
        private StringBuilder buffer = new StringBuilder();
        private int indent = 4;
        private int rightMargin = EditorOptions.get(RubyInstallation.RUBY_MIME_TYPE).getRightMargin();
        private boolean currentSectionOnly;
        private final char CARET_MARKER = '\u4dca'; // Random character inserted into the text and formatted to represent the caret

        /**
         * @param currentSectionOnly Whether it should reflow the entire paragraph or only the current section
         */
        ParagraphFormatter(boolean currentSectionOnly) {
            this.currentSectionOnly = currentSectionOnly;
        }
        
        private void reflowParagraph(JTextComponent target, int offset) {
            this.target = target;
            this.oldCaretPosition = offset;
            this.doc = (BaseDocument)target.getDocument();
            try {
                offset = Utilities.getRowFirstNonWhite(doc, offset);
                if (offset == -1) {
                    // PENDING - Be smarter about empty lines -- do previous line for example? Or next?
                    return;
                }
                Token<? extends GsfTokenId> token = LexUtilities.getToken(doc, offset);
                if (token == null) {
                    return;
                }

                if (token.id() == RubyTokenId.DOCUMENTATION) {
                    documentation = true;
                } else if (token.id() != RubyTokenId.LINE_COMMENT) {
                    // Currently only reflows comments...
                    // In RHTML I could reflow text too...
                    // And even in Ruby, I could compute the surrounding block (e.g. look for empty lines
                    // on both sides?, or {start}/end
                    // blocks), and reindent that as a "paragraph"
                    return;
                }

                OffsetRange range = findParagraph(offset);

                if (range != OffsetRange.NONE) {
                    reflow(range);
                }
            }
            catch (BadLocationException ble){
                Exceptions.printStackTrace(ble);
            }
        }

        private OffsetRange findParagraph(int offset) {
            try {
                int start = Utilities.getRowStart(doc, offset);
                int end = Utilities.getRowEnd(doc, offset);
                while (offset >= 0) {
                    // Find beginning of the paragraph
                    if (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset)) {
                        if (currentSectionOnly) {
                            break;
                        }
                        // Empty lines not allowed within an rdoc
                        if (documentation) {
                            // Empty lines are okay
                            offset = Utilities.getRowStart(doc, offset)-1;
                            continue;
                        }
                        break;
                    }

                    offset = Utilities.getRowStart(doc, offset);
                    int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);
                    Token<? extends GsfTokenId> token = LexUtilities.getToken(doc, lineBegin);
                    if (token == null) {
                        break;
                    }

                    if (token.id() == RubyTokenId.DOCUMENTATION) {
                        String line = doc.getText(offset, Utilities.getRowEnd(doc, offset)-offset);
                        if (line.startsWith("=begin")) {
                            // We're done
                            break;
                        }
                        start = offset;
                    } else if (token.id() == RubyTokenId.LINE_COMMENT) {
                        start = offset;
                    } else {
                        break;
                    }

                    // If currentSectionOnly I can stop reformatting when I get up to
                    // a preformatted section or a numbered section etc. where it breaks with
                    // previous formatting
                    if (currentSectionOnly) {
                        if (!documentation) { // I'm catching empty documentation lines with the "isRowWhite" above
                            String line = doc.getText(lineBegin, Utilities.getRowEnd(doc, lineBegin)-lineBegin);
                            if (line.startsWith("#") && line.length() == 1 || line.equals("# ") ||/* line.startsWith("#  ") ||*/
                                    line.startsWith("# *") || line.startsWith("# - ")) {
                                break;
                            }
                        }
                    }
                    
                    // Previous line
                    offset--;
                }

                int length = doc.getLength();
                offset = end;
                while (offset < length) {
                    // Find beginning of the paragraph
                    if (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset)) {
                        if (currentSectionOnly) {
                            break;
                        }
                        if (documentation) {
                            // Empty lines are okay
                            offset = Utilities.getRowEnd(doc, offset)+1;
                            continue;
                        }
                        // Empty lines not allowed within an rdoc
                        break;
                    }

                    offset = Utilities.getRowStart(doc, offset);
                    int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);
                    int lineEnd = Utilities.getRowEnd(doc, offset);
                    Token<? extends GsfTokenId> token = LexUtilities.getToken(doc, lineBegin);
                    if (token == null) {
                        break;
                    }

                    if (token.id() == RubyTokenId.DOCUMENTATION) {
                        String line = doc.getText(offset, lineEnd-offset);
                        if (line.startsWith("=end")) {
                            // We're done
                            break;
                        }
                        end = lineEnd;
                    } else if (token.id() == RubyTokenId.LINE_COMMENT) {
                        end = lineEnd;
                    } else {
                        break;
                    }

                    // If currentSectionOnly I can stop reformatting when I get down to
                    // a preformatted section or a numbered section etc. where it breaks with
                    // previous formatting
                    if (currentSectionOnly) {
                        if (!documentation) { // I'm catching empty documentation lines with the "isRowWhite" above
                            String line = doc.getText(lineBegin, lineEnd-lineBegin);
                            if (line.startsWith("#") && line.length() == 1 || line.equals("# ") || /*line.startsWith("#  ") ||*/
                                    line.startsWith("# *") || line.startsWith("# - ")) {
                                break;
                            }
                        }
                    }
                    
                    // Next line
                    offset = lineEnd + 1;
                }

                return new OffsetRange(start, end);
            }
            catch (BadLocationException ble){
                Exceptions.printStackTrace(ble);
            }

            return OffsetRange.NONE;
        }

        private int findWordEnd(StringBuilder sb, int start) {
            for (int i = start, length = sb.length(); i < length; i++) {
                char c = sb.charAt(i);
                if (Character.isWhitespace(c)) {
                    return i;
                }
            }

            return sb.length();
        }

        private void reflow(OffsetRange range) throws BadLocationException {
            int start = range.getStart();
            int end = range.getEnd();
            indent = LexUtilities.getLineIndent(doc, start);

            int offset = start;
            boolean foundCaret = false;
            while (offset < end) {
                int textBegin = documentation ?
                    Utilities.getRowStart(doc, offset) :
                    Utilities.getRowFirstNonWhite(doc, offset);
                int textEnd = Utilities.getRowLastNonWhite(doc, offset) + 1;
                int lineEnd = Utilities.getRowEnd(doc, offset);
                if (documentation) {
                    if (textEnd < textBegin) {
                        textEnd = lineEnd;
                    }
                    if (textBegin == -1 || textEnd == -1) {
                        // Blank lines can occur in documenation nodes - not in comments
                        assert documentation;
                        int lineBegin = Utilities.getRowStart(doc, offset);
                        textBegin = lineBegin;
                        textEnd = lineEnd;
                    }
                }
                String line = doc.getText(textBegin, textEnd - textBegin);
                if (!foundCaret) {
                    int lineBegin = Utilities.getRowStart(doc, offset);
                    if (oldCaretPosition >= lineBegin && oldCaretPosition <= lineEnd) {
                        foundCaret = true;
                        // Include trailing whitespace
                        if (oldCaretPosition > textEnd) {
                            line = doc.getText(textBegin, oldCaretPosition - textBegin);
                        }
                        if (oldCaretPosition < textBegin) {
                            line = CARET_MARKER + line;
                        } else if (oldCaretPosition > textEnd) {
                            line = line + CARET_MARKER;
                        } else {
                            int split = oldCaretPosition - textBegin;
                            if (split < line.length()) {
                                line = line.substring(0, split) + CARET_MARKER + line.substring(split);
                            } else {
                                line = line + CARET_MARKER;
                            }
                        }
                    }
                }

                appendLine(line);

                offset = lineEnd + 1;
            }
            flush();

            try {
                doc.atomicLock();
                String replaceWith = sb.toString();
                if (replaceWith.endsWith("\n")) {
                    replaceWith = replaceWith.substring(0, replaceWith.length() - 1);
                }
                int index = replaceWith.indexOf(CARET_MARKER);
                if (index != -1) {
                    replaceWith = replaceWith.substring(0, index) + replaceWith.substring(index + 1);
                }
                doc.replace(start, end - start, replaceWith, null);
                if (index != -1) {
                    target.getCaret().setDot(start + index);
                }
            }
            catch (BadLocationException ble){
                Exceptions.printStackTrace(ble);
            }
            finally{
                doc.atomicUnlock();
            }
        }

        public void appendLine(String text) {
            if (!documentation) {
                if (text.startsWith("# ")) {
                    text = text.substring(2);
                } else if (text.startsWith("#"+CARET_MARKER + " ")) {
                    text = CARET_MARKER + text.substring(3);
                } else if (text.equals("#")) {
                    // Empty comment line
                    text = "";
                } else if (text.length() == 2 && text.equals("#" + CARET_MARKER)) {
                    text = "" + CARET_MARKER;
                }
            }

            boolean isBlankLine = text.length() == 0;
            int caretIndex = text.indexOf(CARET_MARKER);
            if (caretIndex != -1) {
                if (text.substring(0, caretIndex).trim().length() == 0 &&
                        text.substring(caretIndex+1).trim().length() == 0) {
                    isBlankLine = true;
                }
            }

            if (isBlankLine) {
                flush();
                finishSection();
                // Insert a blank line
                startComment();
                // Don't chomp spaces here - lots of comments in the Ruby libraries have "# " on empty lines
                //chompSpaces();
                
                if (caretIndex != -1) {
                    sb.append(CARET_MARKER);
                }
                
                sb.append("\n");

                return;
            }

            if (text.startsWith("* ") || text.startsWith("- ") || text.matches("^[0-9]+\\.( .*)?")) {
                // Starting a bulleted list, or a numbered list:
                // Flush any existing items, then flow this text
                flush();
                if (!inList) {
                    finishSection();
                    inList = true;
                }
                indentedList = false;

                appendFlowed(text.trim());
                appendFlowed(" ");

                // TODO - compute indentLevel
                listIndentation = 2;

                if (text.startsWith("* ") || text.startsWith("- ")) {
                    listIndentation = 1;
                    for (int i = 1; i < text.length(); i++, listIndentation++) {
                        if (!Character.isWhitespace(text.charAt(i))) {
                            break;
                        }
                    }
                }

                return;
            } else if (text.matches("^[\\S]+::( .*)?") || text.matches("^\\[[\\S]+\\]( .+)?")) {
                // Labeled lists with flowed content after the label
                flush();
                if (!inList) {
                    finishSection();
                    inList = true;
                }
                indentedList = false;

                appendFlowed(text.trim());
                appendFlowed(" ");

                // TODO - compute indentLevel
                listIndentation = 2;

                // If this content had flowed text I want to flow
                // it here... otherwise flush
                if (!(text.matches("^\\[[\\S]+\\] .+") || text.matches("^[\\S]+:: .+"))) {
                    flush();
                    indentedList = true;
                }
                return;
            } else if (text.startsWith("Copyright")) {
                // Copyright lines should not be coalesced
                flush();
                startComment();

                finishSection();

                sb.append(text);
                sb.append("\n");

                return;
            } else if (!inList && text.length() > 0 && Character.isWhitespace(text.charAt(0))) {
                // Indented text in list is in same paragraph
                flush();
                startComment();

                if (!inVerbatim) {
                    finishSection();
                    inVerbatim = true;
                }

                sb.append(text);
                sb.append("\n");

                return;
            } else if (text.startsWith("=") || text.startsWith("#---")) {
                flush();
                finishSection();
                startComment();

                sb.append(text);
                sb.append("\n");

                return;
            } else {
                if (inVerbatim) {
                    finishSection();
                }

                appendFlowed(text.trim());
                appendFlowed(" ");

                return;
            }
        }

        private void startComment() {
            if (!documentation) {
                LexUtilities.indent(sb, indent);
                sb.append("# ");
            }
        }

        private void finishSection() {
            flush();
            if (inVerbatim) {
                flush();
                inVerbatim = false;
            }

            if (inList) {
                flush();
                indentedList = false;
                inList = false;
            }
        }

        private void appendFlowed(String text) {
            buffer.append(text);
        }
        
        private void chompSpaces() {
            // Chomp trailing extra space
            for (int i = sb.length()-1; i >= 0; i--) {
                char c = sb.charAt(i);
                if (c != ' ') {
                    sb.setLength(i+1);
                    break;
                }
            }
        }

        private void flush() {
            if (buffer.length() == 0) {
                return;
            }

            int column = 0;
            int offset = 0;
            int oldOffset = sb.length();
            startComment();
            if (inList && indentedList) {
                LexUtilities.indent(sb, listIndentation);
            }
            column += sb.length() - oldOffset;
            int maxWidth = rightMargin;

            // Skip spaces at the beginning of the line
            while (offset < buffer.length()) {
                char c = buffer.charAt(offset);
                if (Character.isWhitespace(c)) {
                    offset++;
                } else {
                    break;
                }
            }

            while (offset < buffer.length()) {
                int start = offset;
                int end = findWordEnd(buffer, start);

                int wordLength = end - start;
                // TODO - if the line contains the caret, reduce the right margin
                if (column + wordLength > maxWidth && (wordLength < maxWidth - indent)) {
                    chompSpaces();
                    sb.append("\n"); // NOI18N
                    oldOffset = sb.length();
                    startComment();
                    if (inList) {
                        LexUtilities.indent(sb, listIndentation);
                    }
                    //sb.append(" "); // NOI18N
                    column = sb.length() - oldOffset;
                }

                for (int i = start; i < end; i++) {
                    char c = buffer.charAt(i);
                    if (c == CARET_MARKER) {
                        maxWidth = rightMargin+1;
                    }
                    sb.append(c);
                    column++;
                }
                offset = end;

                sb.append(" "); // NOI18N
                column++;
                offset++;
                while (offset < buffer.length()) {
                    char c = buffer.charAt(offset);

                    if (Character.isWhitespace(c)) {
                        if (column < rightMargin) {
                            sb.append(c);
                            column++;
                        }
                        offset++;
                    } else {
                        break;
                    }
                }
            }

            // Chomp trailing extra space
            chompSpaces();
            
            sb.append("\n");
            buffer.setLength(0);
        }
    }
}
