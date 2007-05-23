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

    static int nextCamelCasePosition(JTextComponent textComponent) {
        SyntaxSupport syntaxSupport =  Utilities.getSyntaxSupport(textComponent);
        if (syntaxSupport == null) {
            // no syntax support available :(
            return -1;
        }

        // get current caret position
        int offset = textComponent.getCaretPosition();
        try {
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
        } catch (BadLocationException ble) {
            // something went wrong :(
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static int previousCamelCasePosition(JTextComponent textComponent) {
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

        try {
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
                        whitespaceTokenItem =((ExtSyntaxSupport) syntaxSupport).getTokenChain(wsOffset - 1, wsOffset);;
                    }
                    if (whitespaceTokenItem != null) {
                        return whitespaceTokenItem.getOffset() + whitespaceTokenItem.getImage().length();
                    }
                }
            }

            // not an identifier - simply return previous word offset
            return Utilities.getPreviousWord(textComponent, offset);
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        }
        return -1;
    }

    static void replaceChar(JTextComponent textComponent, int offset, char c) {
        if (!textComponent.isEditable()) {
            return;
        }
        replaceText(textComponent, offset, 1, String.valueOf(c));
    }

    static void replaceText(JTextComponent textComponent, int offset, int length, String text) {
        if (!textComponent.isEditable()) {
            return;
        }
        Document document = textComponent.getDocument();
        if (document instanceof BaseDocument) {
            ((BaseDocument)document).atomicLock();
        }
        try {
            if (length > 0) {
                document.remove(offset, length);
            }
            document.insertString(offset, text, null);
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        } finally {
            if (document instanceof BaseDocument) {
                ((BaseDocument)document).atomicUnlock();
            }
        }
    }
}
