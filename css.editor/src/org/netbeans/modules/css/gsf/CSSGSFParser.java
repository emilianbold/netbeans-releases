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
package org.netbeans.modules.css.gsf;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.css.parser.ASCII_CharStream;
import org.netbeans.modules.css.parser.CSSParser;
import org.netbeans.modules.css.parser.ParseException;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.Token;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.spi.DefaultError;

/**
 *
 * @author marek
 */
public class CSSGSFParser implements Parser, PositionManager {

    private static CSSParser PARSER;

    private static synchronized CSSParser parser() {
        if (PARSER == null) {
            PARSER = new CSSParser();
        }
        return PARSER;

    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String) sequence;
        } else {
            return sequence.toString();
        }
    }

//    public void parseFiles(List<ParserFile> files, ParseListener listener, SourceFileReader reader) {
    public void parseFiles(Job job) {
        List<ParserFile> files = job.files;

        for (ParserFile file : files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            job.listener.started(beginEvent);

            CSSParserResult result = null;

            try {
                CharSequence buffer = job.reader.read(file);
                String source = asString(buffer);
                int caretOffset = job.reader.getCaretOffset(file);

                //delete last results - shared instance
                //TODO fix this in the parser
                parser().errors().clear();

                parser().ReInit(new ASCII_CharStream(new StringReader(source)));

                SimpleNode node = parser().styleSheet();

                result = new CSSParserResult(this, file, node);

                for (ParseException pe : (List<ParseException>) parser().errors()) {
                    Token lastSuccessToken = pe.currentToken;
                    Token errorToken = lastSuccessToken.next;
                    int from = errorToken.offset;
                    int to = from + errorToken.image.length();

                    Error error =
                            new DefaultError(pe.getMessage(), pe.getLocalizedMessage(), null, file.getFileObject(),
                            from, from, Severity.ERROR);

                    job.listener.error(error);

                }

                //do some semantic checking of the parse tree
                List<Error> semanticErrors = new CssAnalyser(result).checkForErrors(node);
                for (Error err : semanticErrors) {
                    job.listener.error(err);
                }

//                Context context = new Context(file, listener, source, caretOffset);
//                result = parseBuffer(context, Sanitize.NONE);
            } catch (ParseException ex) {
                Token lastSuccessToken = ex.currentToken;
                Token errorToken = lastSuccessToken.next;
                int from = errorToken.offset;
                int to = from + errorToken.image.length();

                Error error =
                        new DefaultError(ex.getMessage(), ex.getLocalizedMessage(), null, file.getFileObject(),
                        from, from, Severity.ERROR);

                result = new CSSParserResult(this, file, null);

                job.listener.error(error);
            } catch (IOException ioe) {
                job.listener.exception(ioe);
            }

            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            job.listener.finished(doneEvent);
        }
    }

    public PositionManager getPositionManager() {
        return this;
    }

    public OffsetRange getOffsetRange(CompilationInfo info, ElementHandle object) {
        if (object instanceof CssElementHandle) {
            ParserResult presult = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator().next();
            final TranslatedSource source = presult.getTranslatedSource();
            SimpleNode node = ((CssElementHandle) object).node();
            return new OffsetRange(AstUtils.documentPosition(node.startOffset(), source), AstUtils.documentPosition(node.endOffset(), source));
        } else {
            throw new IllegalArgumentException((("Foreign element: " + object + " of type " +
                    object) != null) ? object.getClass().getName() : "null"); //NOI18N
        }
    }
}
