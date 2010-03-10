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

package org.netbeans.modules.css.gsf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lexer.api.CssTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class CssDeclarationFinder implements DeclarationFinder {

    private static final Pattern URI_PATTERN = Pattern.compile("url\\(\\s*(.*)\\s*\\)"); //NOI18N

     /**
     * Find the declaration for the program element that is under the caretOffset
     * Return a Set of regions that should be renamed if the element under the caret offset is
     * renamed.
     *
     * Return {@link DeclarationLocation#NONE} if the declaration can not be found, otherwise return
     *   a valid DeclarationLocation.
     */
    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(
                info.getSnapshot().getSource().getDocument(true),
                caretOffset,
                CssTokenId.language());
        Token<CssTokenId> valueToken = ts.token();
        String valueText = valueToken.text().toString();

        //adjust the value if a part of an URI
        if (valueToken.id() == CssTokenId.URI) {
            Matcher m = URI_PATTERN.matcher(valueToken.text());
            if (m.matches()) {
                int groupIndex = 1;
                valueText = m.group(groupIndex);
            }
        }
        
        valueText = WebUtils.unquotedValue(valueText);

        FileObject resolved = WebUtils.resolve(info.getSnapshot().getSource().getFileObject(), valueText);
        if(resolved != null) {
            return new DeclarationLocation(resolved, 0);
        }
        
        return DeclarationLocation.NONE;
    }

    /**
     * Check the caret offset in the document and determine if it is over a span
     * of text that should be hyperlinkable ("Go To Declaration" - in other words,
     * locate the reference and return it. When the user drags the mouse with a modifier
     * key held this will be hyperlinked, and so on.
     * <p>
     * Remember that when looking up tokens in the token hierarchy, you will get the token
     * to the right of the caret offset, so check for these conditions
     * {@code (sequence.move(offset); sequence.offset() == offset)} and check both
     * sides such that placing the caret between two tokens will match either side.
     *
     * @return {@link OffsetRange#NONE} if the caret is not over a valid reference span,
     *   otherwise return the character range for the given hyperlink tokens
     */
    @Override
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(doc, caretOffset, CssTokenId.language());
        if(ts == null) {
            return OffsetRange.NONE;
        }

        Token<CssTokenId> token = ts.token();
        int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
        OffsetRange range = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
        if(token.id() == CssTokenId.STRING || token.id() == CssTokenId.URI) {
            //check if there is @import token before
            if(ts.movePrevious()) {
                //whitespace expected
                if(ts.token().id() == CssTokenId.S) {
                    if(ts.movePrevious()) {
                        //@import token expected
                        if(ts.token().id() == CssTokenId.IMPORT_SYM) {
                            //gotcha!
                            return range;
                        }
                    }
                }
            }
        }

        return OffsetRange.NONE;
    }

}
