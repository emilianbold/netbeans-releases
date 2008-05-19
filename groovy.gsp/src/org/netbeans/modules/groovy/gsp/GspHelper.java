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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.gsp;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.gsp.lexer.api.GspTokenId;
import org.openide.util.Exceptions;

/**
 * Helper functions for Schliemann definition of the GSP NBS
 *
 * @author Tor Norbye
 */
public class GspHelper {

    /** Number of characters to display from the Groovy fragments in the navigator */
    private static final int MAX_GROOVY_LABEL_LENGTH = 30;
    /** Default label to use on navigator items where we don't have more accurate
     * information */
    private static final String DEFAULT_LABEL = "<% %>"; // NOI18N

    public static String navigatorName(SyntaxContext context) {
        ASTItem leaf = context.getASTPath().getLeaf();
        if (leaf != null) {
            int offset = leaf.getOffset();
            AbstractDocument doc = (AbstractDocument) context.getDocument();
            doc.readLock ();
            try {
                TokenSequence ts = TokenHierarchy.get (doc).tokenSequence ();
                ts.move(offset);
                if (ts.moveNext()) {
                    TokenId id = ts.token().id();
                    if (id == GspTokenId.DELIMITER) {
                        if (ts.moveNext()) {
                            id = ts.token().id();
                            if (GspTokenId.isGroovy(id)) {
                                TokenSequence t = ts.embedded();
                                if (t != null) {
                                    t.moveStart();
                                    t.moveNext();
                                    while (t.token().id() == GroovyTokenId.WHITESPACE) {
                                        if (!t.moveNext()) {
                                            break;
                                        }
                                    }
                                    int begin = t.offset();
                                    id = t.token().id();

                                    if (id == GroovyTokenId.WHITESPACE) {
                                        // Empty tag
                                        return DEFAULT_LABEL;
                                    }

                                    if (id == GroovyTokenId.STRING_BEGIN || id == GroovyTokenId.QUOTED_STRING_BEGIN || id == GroovyTokenId.REGEXP_BEGIN) {
                                        while (t.moveNext()) {
                                            id = t.token().id();
                                            if (id == GroovyTokenId.STRING_END || id == GroovyTokenId.QUOTED_STRING_END || id == GroovyTokenId.REGEXP_END) {
                                                int end = t.offset() + t.token().length();

                                                return createName(doc, begin, end);
                                            }
                                        }
                                    }

                                    int end = t.offset() + t.token().length();

                                    // See if this is a "foo.bar" expression and if so, include ".bar"
                                    if (t.moveNext()) {
                                        id = t.token().id();
                                        if (id == GroovyTokenId.DOT) {
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
            } finally {
                doc.readUnlock ();
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
            if (length > MAX_GROOVY_LABEL_LENGTH) {
                length = MAX_GROOVY_LABEL_LENGTH;
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