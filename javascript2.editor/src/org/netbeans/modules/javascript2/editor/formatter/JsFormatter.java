/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import com.oracle.nashorn.ir.FunctionNode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatter implements Formatter {

    private static final Logger LOGGER = Logger.getLogger(JsFormatter.class.getName());

    @Override
    public int hangingIndentSize() {
        return -1; // Use IDE defaults
    }

    @Override
    public int indentSize() {
        return -1; // Use IDE defaults
    }

    @Override
    public boolean needsParserResult() {
        return true;
    }

    @Override
    public void reformat(final Context context, final ParserResult compilationInfo) {
        final BaseDocument doc = (BaseDocument) context.document();

        doc.runAtomic(new Runnable() {

            @Override
            public void run() {
                FormatTokenStream tokenStream = FormatTokenStream.create(LexUtilities.getJsTokenSequence(compilationInfo.getSnapshot()),
                        context.startOffset(), context.endOffset());
                FormatVisitor visitor = new FormatVisitor(tokenStream,
                        LexUtilities.getJsTokenSequence(compilationInfo.getSnapshot()));

                FunctionNode root = ((JsParserResult) compilationInfo).getRoot();
                if (root != null) {
                    root.accept(visitor);
                }

                int offsetDiff = 0;
                int indentationLevel = 0;

                List<FormatToken> tokens = tokenStream.getTokens();
                for (int i = 0; i < tokens.size(); i++) {
                    FormatToken token = tokens.get(i);
                    LOGGER.log(Level.FINE, token.toString());

                    switch (token.getKind()) {
                        case EOL:
                            // remove trailing spaces
                            FormatToken start = null;
                            for (int j = i - 1; j > 0; j--) {
                                if (!tokens.get(j).isVirtual()
                                        && tokens.get(j).getKind() != FormatToken.Kind.WHITESPACE) {
                                    break;
                                } else {
                                    start = tokens.get(j);
                                }
                            }
                            while (start != null
                                    && start.getKind() != FormatToken.Kind.EOL) {
                                if (!start.isVirtual()) {
                                    offsetDiff = remove(doc, start.getOffset(),
                                            start.getText().length(), offsetDiff);
                                }
                                start = start.next();
                            }
                            break;
                        case INDENTATION_INC:
                            indentationLevel++;
                            break;
                        case INDENTATION_DEC:
                            indentationLevel--;
                            break;
                    }
                }
            }
        });
    }

    private int remove(BaseDocument doc, int offset, int length, int offsetDiff) {
        try {
            doc.remove(offset + offsetDiff, length);
            return offsetDiff - length;
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return 0;
    }

    @Override
    public void reindent(Context context) {
        // TODO
    }

}
