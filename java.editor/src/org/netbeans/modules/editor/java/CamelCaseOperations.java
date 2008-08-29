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

package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.openide.ErrorManager;

/**
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
/* package */ class CamelCaseOperations {

    static int nextCamelCasePosition(JTextComponent textComponent) throws BadLocationException {
        SyntaxSupport syntaxSupport =  Utilities.getSyntaxSupport(textComponent);
        if (syntaxSupport == null) {
            // no syntax support available :(
            return -1;
        }

        // get current caret position
        int offset = textComponent.getCaretPosition();
        // get token chain at the offset + 1 ( + 1 so that the following uppercase char is skipped
        TokenItem tokenItem = ((ExtSyntaxSupport) syntaxSupport).getTokenChain(offset, offset + 1);

        // is this an identifier
        if (tokenItem != null && ("identifier".equals(tokenItem.getTokenID().getName()))) { // NOI18N
            String image = tokenItem.getImage();
            if (image != null && image.length() > 0) {
                int length = image.length();
                // is caret at the end of the identifier
                if (offset != (tokenItem.getOffset() + length)) {
                    int offsetInImage = offset - tokenItem.getOffset();
                    int start = offsetInImage + 1;
                    if (Character.isUpperCase(image.charAt(offsetInImage))) {
                        // if starting from a Uppercase char, first skip over follwing upper case chars
                        for (int i = start; i < length; i++) {
                            char charAtI = image.charAt(i);
                            if (!Character.isUpperCase(charAtI)) {
                                break;
                            }
                            start++;
                        }
                    }
                    for (int i = start; i < length; i++) {
                        char charAtI = image.charAt(i);
                        if (Character.isUpperCase(charAtI)) {
                            // return offset of next uppercase char in the identifier
                            return tokenItem.getOffset() + i;
                        }
                    }
                }
                return tokenItem.getOffset() + image.length();
            }
        }

        // not an identifier - simply return next word offset
        return Utilities.getNextWord(textComponent, offset);
    }

    static int previousCamelCasePosition(JTextComponent textComponent) throws BadLocationException {
        SyntaxSupport syntaxSupport = Utilities.getSyntaxSupport(textComponent);
        if (syntaxSupport == null) {
            // no syntax support available :(
            return -1;
        }

        // get current caret position
        int offset = textComponent.getCaretPosition();

        // Are we at the beginning of the document
        if (offset == 0) {
            return -1;
        }

        // get token chain at the offset
        TokenItem tokenItem = ((ExtSyntaxSupport) syntaxSupport).getTokenChain(offset - 1, offset);

        // is this an identifier
        if (tokenItem != null) {
            if ("identifier".equals(tokenItem.getTokenID().getName())) { // NOI18N
                String image = tokenItem.getImage();
                if (image != null && image.length() > 0) {
                    int length = image.length();
                    int offsetInImage = offset - 1 - tokenItem.getOffset();
                    if (Character.isUpperCase(image.charAt(offsetInImage))) {
                        for (int i = offsetInImage - 1; i >= 0; i--) {
                            char charAtI = image.charAt(i);
                            if (!Character.isUpperCase(charAtI)) {
                                // return offset of previous uppercase char in the identifier
                                return tokenItem.getOffset() + i + 1;
                            }
                        }
                        return tokenItem.getOffset();
                    } else {
                        for (int i = offsetInImage - 1; i >= 0; i--) {
                            char charAtI = image.charAt(i);
                            if (Character.isUpperCase(charAtI)) {
                                // now skip over previous uppercase chars in the identifier
                                for (int j = i; j >= 0; j--) {
                                    char charAtJ = image.charAt(j);
                                    if (!Character.isUpperCase(charAtJ)) {
                                        // return offset of previous uppercase char in the identifier
                                        return tokenItem.getOffset() + j + 1;
                                    }
                                }
                                return tokenItem.getOffset();
                            }
                        }
                    }
                    return tokenItem.getOffset();
                }
            } else if ("whitespace".equals(tokenItem.getTokenID().getName())) { // NOI18N
                TokenItem whitespaceTokenItem = tokenItem;
                while (whitespaceTokenItem != null && "whitespace".equals(whitespaceTokenItem.getTokenID().getName())) {
                    int wsOffset = whitespaceTokenItem.getOffset();
                    if (wsOffset == 0) {
                        //#145250: at the very beginning of a file
                        return 0;
                    }
                    whitespaceTokenItem =((ExtSyntaxSupport) syntaxSupport).getTokenChain(wsOffset - 1, wsOffset);
                }
                if (whitespaceTokenItem != null) {
                    return whitespaceTokenItem.getOffset() + whitespaceTokenItem.getImage().length();
                }
            }
        }

        // not an identifier - simply return previous word offset
        return Utilities.getPreviousWord(textComponent, offset);
    }

    static void replaceChar(JTextComponent textComponent, int offset, char c) {
        if (!textComponent.isEditable()) {
            return;
        }
        replaceText(textComponent, offset, 1, String.valueOf(c));
    }

    static void replaceText (final JTextComponent textComponent, final int offset, final int length, final String text) {
        if (!textComponent.isEditable()) {
            return;
        }
        Document document = textComponent.getDocument();
        if (document instanceof BaseDocument)
            ((BaseDocument) document).runAtomic (new Runnable () {
                public void run () {
                    replaceText2 (textComponent, offset, length, text);
                }
            });
        else
            replaceText2 (textComponent, offset, length, text);
    }

    static void replaceText2 (JTextComponent textComponent, int offset, int length, String text) {
        Document document = textComponent.getDocument();
        try {
            if (length > 0) {
                document.remove(offset, length);
            }
            document.insertString(offset, text, null);
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        }
    }
}
