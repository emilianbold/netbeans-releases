/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;

import java.util.Arrays;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPNewLineIndenter {
    private Context context;

    private Collection<ScopeDelimiter> scopeDelimiters = null;

    public PHPNewLineIndenter(Context context) {
        this.context = context;

        int indentSize = CodeStyle.get(context.document()).getIndentSize();

        scopeDelimiters = Arrays.asList(
            new ScopeDelimiter(PHPTokenId.PHP_SEMICOLON, 0),
            new ScopeDelimiter(PHPTokenId.PHP_CURLY_OPEN, indentSize)
        );
    }

    public void process() {
        final BaseDocument doc = (BaseDocument) context.document();
        final int offset = context.caretOffset();

        doc.runAtomic(new Runnable() {

            public void run() {
                TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

                ts.move(offset);
                int newIndent = 0;

                while (ts.movePrevious()) {
                    Token token = ts.token();
                    ScopeDelimiter delimiter = getScopeDelimiter(token);

                    if (delimiter != null) {
                        int indentAtDelimiter = 0;
                        try {
                            indentAtDelimiter = Utilities.getRowIndent(doc, ts.offset());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        newIndent = indentAtDelimiter + delimiter.indentDelta;
                        break;
                    }
                }

                try {
                    int lineStart = Utilities.getRowStart(doc, offset);
                    context.modifyIndent(lineStart, newIndent);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private ScopeDelimiter getScopeDelimiter(Token token){
        // TODO: more efficient impl

        for (ScopeDelimiter scopeDelimiter : scopeDelimiters){
            if (scopeDelimiter.matches(token)){
                return scopeDelimiter;
            }
        }

        return null;
    }

    static class ScopeDelimiter{
        private TokenId tokenId;
        private String tokenContent;
        private int indentDelta;

        public ScopeDelimiter(TokenId tokenId, int indentDelta) {
            this(tokenId, null, indentDelta);
        }

        public ScopeDelimiter(TokenId tokenId, String tokenContent, int indentDelta) {
            this.tokenId = tokenId;
            this.tokenContent = tokenContent;
            this.indentDelta = indentDelta;
        }

        public boolean matches(Token token){
            if (tokenId != token.id()){
                return false;
            }

            if (tokenContent != null 
                    && TokenUtilities.equals(token.text(), tokenContent)){
                
                return false;
            }

            return true;
        }
    }
}
