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
package org.netbeans.modules.html.editor.gsf;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 */
public class HtmlGSFParser implements Parser {

    private static final Logger LOGGER = Logger.getLogger(HtmlGSFParser.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public void parseFiles(Job job) {
        for (ParserFile file : job.files) {
            try {
                ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
                job.listener.started(beginEvent);

                ParserResult result = null;

                CharSequence buffer = job.reader.read(file);
                int caretOffset = job.reader.getCaretOffset(file);

                SyntaxParser parser = SyntaxParser.create(buffer);
                List<SyntaxElement> elements = parser.parseImmutableSource();

                if (LOG) {
                    for (SyntaxElement element : elements) {
                        LOGGER.log(Level.FINE, element.toString());
                    }
                }

                result = new HtmlParserResult(this, file, elements);

//                //delete last results - shared instance
//                //TODO fix this in the parser
//                parser().errors().clear();
//                
//                parser().ReInit(new ASCII_CharStream(new StringReader(source), 0, 0));
//
//                SimpleNode node = parser().styleSheet();
//                
//                node.dump("");
//
//                for (ParseException pe : (List<ParseException>) parser().errors()) {
//
//                    System.out.println(pe.getMessage());
//                    
//                    Token lastSuccessToken = pe.currentToken;
//                    Token errorToken = lastSuccessToken.next;
//                    int from = errorToken.offset;
//                    int to = from + errorToken.image.length();
//                    
//                    Error error =
//                            new DefaultError(pe.getMessage(), pe.getLocalizedMessage(), null, file.getFileObject(),
//                            from, from, Severity.ERROR);
//
//                    job.listener.error(error);
//
//                }

//                result = new CSSParserResult(this, file, node);

//                Context context = new Context(file, listener, source, caretOffset);
//                result = parseBuffer(context, Sanitize.NONE);
                //result = new HtmlParserResult(this, file);
                ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
                job.listener.finished(doneEvent);
            } catch (IOException ex) {
                job.listener.exception(ex);
                Exceptions.printStackTrace(ex);
            }

        }
    }

    public PositionManager getPositionManager() {
//        return new CSSPositionManager();
        return null;
    }

//    public SemanticAnalyzer getSemanticAnalysisTask() {
//        return new CSSSemanticAnalyzer();
//    }
//
//    public OccurrencesFinder getMarkOccurrencesTask(int caretPosition) {
//        return new CSSOccurancesFinder();
//    }
//
//    public <T extends Element> ElementHandle<T> createHandle(CompilationInfo info, T element) {
//        return null;
//    }
//
//    public <T extends Element> T resolveHandle(CompilationInfo info, ElementHandle<T> handle) {
//        return null;
//    }
}
