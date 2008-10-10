/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.editor.ext.html;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class HtmlIndenter {

    private final static Logger LOGGER = Logger.getLogger(HtmlIndenter.class.getName());
    private final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static synchronized void indentEndTag(Document doc, LanguagePath languagePath, int offset, String endTagName) {
        try {
            //adjust offset, if called from code completion the offset points behind the tag closing symbol
            //if called from defaultkeytypedaction offset points to the closing greater than character.
            if (endTagName != null) {
                //called from completion
                offset--;
            }

            SyntaxElement element = HTMLSyntaxSupport.get(doc).getElementChain(offset);
            if(element == null || element.getType() != SyntaxElement.TYPE_ENDTAG) {
                LOGGER.info("Unexpected SyntaxElement at position " + offset + ": " + element);
                return ;
            }

            Stack<String> stack = new Stack<String>();
            SyntaxElement pair = null;

            main:
            do {
                if (element.getType() == SyntaxElement.TYPE_TAG) { //open tag
                    SyntaxElement.Tag tag = (SyntaxElement.Tag) element;
                    if(tag.isEmpty()) {
                        continue;
                    }
                    //pop from the stack until we find the pair
                    for (int i = stack.size() - 1; i >= 0; i--) {
                        String tName = stack.elementAt(i);
                        if (tName.equals(tag.getName())) {
                            if (i == 0) {
                                //found the searched pair
                                pair = tag;
                                break main;
                            } else  {
                                stack.remove(i);
                                break;
                            }
                        }
                    }
                } else if (element.getType() == SyntaxElement.TYPE_ENDTAG) { //end tag
                    SyntaxElement.Named tag = (SyntaxElement.Named) element;
                    stack.push(tag.getName());
                }
            } while ((element = element.getPrevious()) != null);

            if (pair != null) {
                changeRowIndent(doc, pair.getElementOffset(), offset);
            }

        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static void changeRowIndent(Document doc, int pairOffset, int offset) {
        try {
            int pairIndent = Utilities.getRowIndent((BaseDocument) doc, pairOffset);

            LOGGER.fine("Paired open tag indent level=" + pairIndent);

            String indentString = IndentUtils.createIndentString(doc, pairIndent);
            int rowStart = Utilities.getRowStart((BaseDocument) doc, offset);
            int textStart = Utilities.getFirstNonWhiteFwd((BaseDocument) doc, rowStart);

            doc.remove(rowStart, textStart - rowStart);
            doc.insertString(rowStart, indentString, null);

        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, "Error changing row indent", ex);
        }
    }
}

