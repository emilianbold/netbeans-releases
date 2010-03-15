/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class LoremIpsumGenerator implements CodeGenerator {

    JTextComponent textComp;

    /**
     *
     * @param context containing JTextComponent and possibly other items registered by {@link CodeGeneratorContextProvider}
     */
    private LoremIpsumGenerator(Lookup context) { // Good practice is not to save Lookup outside ctor
        textComp = context.lookup(JTextComponent.class);
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new LoremIpsumGenerator(context));
        }
    }

    /**
     * The name which will be inserted inside Insert Code dialog
     */
    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_lorem_ipsum"); //NOI18N
    }

    private static List<String> completeParagraphList() {
        List<String> paragraphs = new ArrayList<String>();
        for (int paragraphNumber = 1; paragraphNumber <= 10; ++paragraphNumber) {
            paragraphs.add(NbBundle.getMessage(LoremIpsumGenerator.class, "lorem_ipsum_paragraph_" + paragraphNumber)); //NOI18N
        }
        return paragraphs;
    }

    /**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     */
    @Override
    public void invoke() {
        final int caretOffset = textComp.getCaretPosition();
        final LoremIpsumPanel panel = new LoremIpsumPanel(completeParagraphList());
        String title = NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_generate_lorem_ipsum"); //NOI18N
        DialogDescriptor dialogDescriptor = createDialogDescriptor(panel, title);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            BaseDocument document = (BaseDocument) textComp.getDocument();
            try {
                insertLoremIpsum(document, panel.getParagraphs(), panel.getTag(), getIndentSize(document), caretOffset);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Insert lorem ipsum text into the given document at the given offset.
     * The inserted text is indented with respect to the current line.  Each
     * paragraph is wrapped in the given HTML element.  The HTML element is
     * expected to be an open element that will be closed so taht the generated
     * text is XHTML-compliant.
     *
     * @param document to indent into
     * @param paragraphs list of paragraphs of lorem ipsum to insert.  Must not be null.
     * @param tag to wrap paragraphs in.
     * @param indent text to be used for a single-level of nesting.
     * @param caretOffset index into the document at which text is to be inserted
     * @throws BadLocationException cannot identify location in document.
     */
    public static void insertLoremIpsum(BaseDocument document, List<String> paragraphs, String tag, CharSequence indent, int caretOffset) throws BadLocationException {
        StringBuilder insertText = new StringBuilder();
        CharSequence indentText = findIndent(document, caretOffset, indent);
        boolean insertAtEndOfLine = isLineCharacter(document.getChars(caretOffset, 1)[0]);
        boolean insertAfterIndent = (caretOffset > 0) ? isIndentCharacter(document.getChars(caretOffset - 1, 1)[0]) : true;
        if (!insertAfterIndent) {
            insertText.append('\n');
        }
        String closeTag = tag.replaceFirst("<", "</");
        for (String paragraph : paragraphs) {
            insertText.append(indentText).append(tag).append("\n");
            insertText.append(indentText).append(indent).append(paragraph).append("\n");
            insertText.append(indentText).append(closeTag).append("\n");
        }
        if (insertAtEndOfLine) {
            insertText.setLength(insertText.length() - 1);
        }
        if (insertAfterIndent) {
            while (isIndentCharacter(document.getChars(caretOffset - 1, 1)[0])) {
                --caretOffset;
            }
        }
        document.insertString(caretOffset, insertText.toString(), null);
    }

    private static boolean isLineCharacter(char c) {
        return c == '\n' || c == '\r';
    }

    private CharSequence getIndentSize(Document document) {
        StringBuilder indentation = new StringBuilder();
        if (IndentUtils.isExpandTabs(document)) {
            int spacesInIndent = IndentUtils.indentLevelSize(document);
            for (int i = 0; i < spacesInIndent; ++i) {
                indentation.append(' ');
            }
        } else {
            indentation.append('\t');
        }
        return indentation;
    }

    private static boolean isIndentCharacter(char c) {
        return Character.isWhitespace(c) && !isLineCharacter(c);
    }

    private static boolean isIndentCharacter(CharSequence tokenText, int index) {
        if (index >= tokenText.length()) {
            return false;
        } else {
            char c = tokenText.charAt(index);
            return isIndentCharacter(c);
        }
    }

    /**
     * Identifies the whitespace characters that makes up the line indentation
     * of the line of text on which the caret is placed.  The token is found
     * where the caret is placed.  A backwards search then commences to find the
     * preceding new-line character.  If necessary looks back through preceding
     * text and whitespace tokens.  The document offset of this character is
     * noted as is the offset for the last non-whitespace character.  These are
     * then used to extract the sub-string that forms the indentation of the
     * line.
     * <p>
     * This method also attempts to ensure that the returned indent accounts for
     * the opening and closing of tags between the caret and the new-line
     * character.  If opening tags are in the majority then the indent text
     * argument is appended to the returned indent.  If closing tags are in the
     * majority then the indent is shortened by the number of characters in the
     * indent text argument.  Outdenting however will not happen if any of the
     * characters in the discovered indent are different from the first character
     * of the given single-level indent string.
     * <p>
     * If no new-line character is found then the empty string is
     * returned.
     * </p>
     *
     * @param document to search through
     * @param caretOffset offset in the document to where the caret is currently placed.
     * @param indent string used to indent nesting by one level.
     * @return string to indent new lines by
     */
    private static CharSequence findIndent(BaseDocument document, final int caretOffset, final CharSequence indent) {
        TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence(document, caretOffset, HTMLTokenId.language());
        while (ts.moveNext() && ts.offset() <= caretOffset) {
        }
        boolean firstToken = true;
        int nestingCount = 0;
        while (ts.movePrevious()) {
            Token<HTMLTokenId> token = ts.token();
            HTMLTokenId id = token.id();
            if (HTMLTokenId.TAG_OPEN.equals(id)) {
                ++nestingCount;
            } else if (HTMLTokenId.TAG_CLOSE.equals(id)) {
                --nestingCount;
            } else if (HTMLTokenId.TAG_CLOSE_SYMBOL.equals(id)) {
                if ("/>".equals(token.text().toString())) {
                    --nestingCount;
                }
            } else if (HTMLTokenId.TEXT.equals(id)
                    || HTMLTokenId.WS.equals(id)) {
                CharSequence tokenText = token.text();
                int nonWhitespaceIndex = firstToken ? caretOffset - ts.offset() : tokenText.length() - 1;
                char indexedChar = tokenText.charAt(nonWhitespaceIndex);
                if (isLineCharacter(indexedChar)) {
                    --nonWhitespaceIndex;
                }
                for (int index = nonWhitespaceIndex; index >= 0; --index) {
                    indexedChar = tokenText.charAt(index);
                    if (isLineCharacter(indexedChar)) {
                        if (nonWhitespaceIndex == 0) {
                            return "";
                        } else if (firstToken) {
                            while (isIndentCharacter(tokenText, nonWhitespaceIndex)) {
                                ++nonWhitespaceIndex;
                            }
                            return tokenText.subSequence(index + 1, nonWhitespaceIndex);
                        } else {
                            String indentText = tokenText.subSequence(index + 1, nonWhitespaceIndex + 1).toString();
                            if (nestingCount > 0) {
                                indentText = indentText + indent;
                            } else if (nestingCount < 0 && indentText.length() >= indent.length()) {
                                boolean outdent = true;
                                char indentCharacter = indent.charAt(0);
                                for (char c : indentText.toCharArray()) {
                                    outdent = outdent && c == indentCharacter;
                                }
                                if (outdent) {
                                    indentText = indentText.substring(0, indentText.length() - indent.length());
                                }
                            }
                            return indentText;
                        }
                    } else if (!isIndentCharacter(indexedChar)) {
                        nonWhitespaceIndex = index;
                    }
                }
            }
            firstToken = false;
        }
        return "";
    }

    private static DialogDescriptor createDialogDescriptor(JComponent content, String label) {
        JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_generate_button"));//NOI18N
        buttons[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LoremIpsumGenerator.class, "A11Y_Generate"));//NOI18N
        buttons[1] = new JButton(NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_cancel_button"));//NOI18N
        return new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
    }
}
