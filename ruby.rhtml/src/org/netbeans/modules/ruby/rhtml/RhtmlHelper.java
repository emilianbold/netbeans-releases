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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rhtml;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.util.Exceptions;

/**
 * Helper functions for Schliemann definition of the RHTML NBS
 *
 * @author Tor Norbye
 */
public class RhtmlHelper {

    /** Number of characters to display from the Ruby fragments in the navigator */
    private static final int MAX_RUBY_LABEL_LENGTH = 30;
    /** Default label to use on navigator items where we don't have more accurate
     * information */
    private static final String DEFAULT_LABEL = "<% %>"; // NOI18N

    public static String navigatorName(SyntaxContext context) {
        ASTItem leaf = context.getASTPath().getLeaf();
        if (leaf != null) {
            int offset = leaf.getOffset();
            Document doc = context.getDocument();
            TokenSequence ts = context.getTokenSequence();
            ts.move(offset);
            if (ts.moveNext()) {
                TokenId id = ts.token().id();
                if (id == RhtmlTokenId.DELIMITER) {
                    if (ts.moveNext()) {
                        id = ts.token().id();
                        if (RhtmlTokenId.isRuby(id)) {
                            TokenSequence t = ts.embedded();
                            if (t != null) {
                                t.moveStart();
                                t.moveNext();
                                while (t.token().id() == RubyTokenId.WHITESPACE) {
                                    if (!t.moveNext()) {
                                        break;
                                    }
                                }
                                int begin = t.offset();
                                id = t.token().id();

                                if (id == RubyTokenId.WHITESPACE) {
                                    // Empty tag
                                    return DEFAULT_LABEL;
                                }

                                if (id == RubyTokenId.STRING_BEGIN || id == RubyTokenId.QUOTED_STRING_BEGIN || id == RubyTokenId.REGEXP_BEGIN) {
                                    while (t.moveNext()) {
                                        id = t.token().id();
                                        if (id == RubyTokenId.STRING_END || id == RubyTokenId.QUOTED_STRING_END || id == RubyTokenId.REGEXP_END) {
                                            int end = t.offset() + t.token().length();

                                            return createName(doc, begin, end);
                                        }
                                    }
                                }

                                int end = t.offset() + t.token().length();

                                // See if this is a "foo.bar" expression and if so, include ".bar"
                                if (t.moveNext()) {
                                    id = t.token().id();
                                    if (id == RubyTokenId.DOT) {
                                        if (t.moveNext()) {
                                            end = t.offset() + t.token().length();
                                        }
                                    }
                                }

                                return createName(doc, begin, end);
                            }
                        }
                    }
                }
            }

            // Fallback mechanism - just pull text out of the document
            String content = createName(doc, offset, offset + leaf.getLength());
            if (content.startsWith("<%= ")) { // NOI18N
                // NOI18N
                if (content.startsWith("<%= ")) { // NOI18N
                    content = content.substring(4);
                } else {
                    content = content.substring(3);
                }
            } else if (content.startsWith("<%")) { // NOI18N
                // NOI18N
                if (content.startsWith("<% ")) { // NOI18N
                    content = content.substring(3);
                } else {
                    content = content.substring(2);
                }
            }
            if (content.endsWith("-%>")) { // NOI18N
                content = content.substring(0, content.length() - 3);
            } else if (content.endsWith("%>")) { // NOI18N
                content = content.substring(0, content.length() - 2);
            }
            return content;
        }
        return DEFAULT_LABEL;
    }

    /** Create label for a navigator item */
    private static String createName(Document doc, int begin, int end) {
        try {
            boolean truncated = false;
            int length = end - begin;
            if (begin + length > doc.getLength()) {
                length = doc.getLength() - begin;
                truncated = true;
            }
            if (length > MAX_RUBY_LABEL_LENGTH) {
                length = MAX_RUBY_LABEL_LENGTH;
                truncated = true;
            }
            String content = doc.getText(begin, length);
            int newline = content.indexOf('\n');
            if (newline != -1) {
                if (content.startsWith("<%\n") || content.startsWith("<%#\n")) {
                    content = content.substring(newline+1);
                    newline = content.indexOf('\n');
                    if (newline != -1) {
                        content = content.substring(0, newline);
                    }
                } else {
                    boolean startsWithNewline = true;
                    for (int i = 0; i < newline; i++) {
                        if (!Character.isWhitespace((content.charAt(i)))) {
                            startsWithNewline = false;
                            break;
                        }
                    }
                    if (startsWithNewline) {
                        content = content.substring(newline+1);
                    } else {
                        content = content.substring(0, newline);
                    }
                }
            }
            if (truncated) {
                return content + "..."; // NOI18N
            } else {
                return content;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return DEFAULT_LABEL;
    }
}