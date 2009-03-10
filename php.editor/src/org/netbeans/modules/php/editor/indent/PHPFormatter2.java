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
package org.netbeans.modules.php.editor.indent;

import java.io.IOException;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * Formatting and indentation for PHP
 *
 * @author Tor Norbye
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPFormatter2 implements org.netbeans.modules.gsf.api.Formatter {

    private static final Logger LOG = Logger.getLogger(PHPFormatter.class.getName());

    public PHPFormatter2() {
        LOG.fine("PHP Formatter: " + this); //NOI18N
    }

    public boolean needsParserResult() {
        return true;
    }

    public void reindent(final Context context) {
        // performance optimization: do nth unless caret is within actual PHP code
        // TODO: this may not be correct for indenting multiple lines
        String mimeType = getMimeTypeAtOffset(context.document(), context.caretOffset());
        System.err.println("mimeType=" + mimeType);

        if (!PHPLanguage.PHP_MIME_TYPE.equals(mimeType)){
            return;
        }


        FileObject file = NavUtils.getFile(context.document());
        try {
            SourceModelFactory.getInstance().getModel(file).runUserActionTask(new CancellableTask<CompilationInfo>() {

                public void cancel() {
                }

                public void run(CompilationInfo parameter) throws Exception {
                    astReindent(context, parameter);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void reformat(Context context, CompilationInfo info) {
        prettyPrint(context, info);
        astReindent(context, info);
    }

    public int indentSize() {
        return CodeStyle.get((Document) null).getIndentSize();
    }

    public int hangingIndentSize() {
        return CodeStyle.get((Document) null).getContinuationIndentSize();
    }

    private void prettyPrint(final Context context, final CompilationInfo info) {
        final BaseDocument doc = (BaseDocument) context.document();
        final String openingBraceStyle = CodeStyle.get(doc).getOpeningBraceStyle();

        if (FmtOptions.OBRACE_PRESERVE.equals(openingBraceStyle)){
            return;
        }

        doc.runAtomic(new Runnable() {

            public void run() {
                final WSTransformer wsTransformer = new WSTransformer(context);
                PHPParseResult result = (PHPParseResult) info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);
                result.getProgram().accept(wsTransformer);


                for (WSTransformer.Replacement replacement : wsTransformer.getReplacements()){
                    if (replacement.offset() < context.startOffset()
                            || replacement.offset() > context.endOffset()){
                        continue;
                    }

                    try {
                        doc.insertString(replacement.offset(), replacement.newString(), null);

                        if (replacement.length() > 0){
                            doc.remove(replacement.offset() - replacement.length(), replacement.length());
                        }

                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    private void astReindent(final Context context, CompilationInfo info) {
        final BaseDocument doc = (BaseDocument)context.document();
        doc.putProperty("HTML_FORMATTER_ACTS_ON_TOP_LEVEL", Boolean.TRUE); //NOI18N

        try {
            final int startOffset = Utilities.getRowStart(doc, context.startOffset());
            final int endOffset = Utilities.getRowEnd(doc, context.endOffset());
            final int firstLine = Utilities.getLineOffset(doc, startOffset);
            final Map<Integer, Integer> indentLevels = new LinkedHashMap<Integer, Integer>();
            final IndentLevelCalculator indentCalc = new IndentLevelCalculator(doc, indentLevels);
            PHPParseResult result = (PHPParseResult) info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);
            result.getProgram().accept(indentCalc);

            doc.runAtomic(new Runnable() {

                public void run() {
                    int indentBias = 0;
                    try {
                        int numberOfLines = Utilities.getLineOffset(doc, doc.getLength() - 1);
                        Map<Integer, Integer> indentDeltaByLine = new LinkedHashMap<Integer, Integer>();

                        for (int point : indentLevels.keySet()) {
                            int indentDelta = indentLevels.get(point);
                            int lineNumber = Utilities.getLineOffset(doc, point);
                            int rowStart = Utilities.getRowStart(doc, point);
                            int firstNonWSBefore = Utilities.getFirstNonWhiteBwd(doc, point);

                            if (firstNonWSBefore >= rowStart){
                                    lineNumber ++;
                                }

                            Integer lineDelta = indentDeltaByLine.get(lineNumber);
                            indentDeltaByLine.put(lineNumber, lineDelta == null
                                    ? indentDelta : lineDelta + indentDelta);
                        }

                        for (int i = 0, currentIndent = 0; i < numberOfLines; i++) {
                            int lineStart = Utilities.getRowStartFromLineOffset(doc, i);
                            Integer lineDelta = indentDeltaByLine.get(i);
                            System.err.println("lineDelta[" + i + "]=" + lineDelta);

                            if (i == firstLine){
                                // TODO: do it also if there was HTML block in the middle
                                // and this is the first line after the HTML
                                indentBias = lineStart == 0 ? 0: currentIndent - GsfUtilities.getLineIndent(doc, lineStart-1);
                            }

                            if (lineDelta != null) {
                                currentIndent += lineDelta;
                                assert currentIndent >= 0;
                            }

                            //TODO:
                            if (lineStart >= startOffset && lineStart <= endOffset){
                                int actualIndent = 0;

                                if (currentIndent > indentBias){
                                    actualIndent = currentIndent - indentBias;
                                }

                                GsfUtilities.setLineIndentation(doc, lineStart, actualIndent);
                            }
                        }

                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private static String getMimeTypeAtOffset(Document doc, int offset){
        TokenHierarchy th = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsl = th.embeddedTokenSequences(offset, false);
        if (tsl != null && tsl.size() > 0) {
            TokenSequence<?> tokenSequence = tsl.get(tsl.size() - 1);
            return tokenSequence.language().mimeType();
        }

        return null;
    }
}