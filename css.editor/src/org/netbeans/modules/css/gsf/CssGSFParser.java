/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.util.ArrayList;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.ASCII_CharStream;
import java.io.StringReader;
import java.util.List;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.css.editor.LexerUtils;
import org.netbeans.modules.css.lexer.api.CssTokenId;
import org.netbeans.modules.css.parser.CssParser;
import org.netbeans.modules.css.parser.CssParserConstants;
import org.netbeans.modules.css.parser.ParseException;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.Token;
import org.netbeans.modules.css.parser.TokenMgrError;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Marek Fukala
 */
public class CssGSFParser extends Parser {

    private final CssParser PARSER = new CssParser();
    private CssParserResult lastResult = null;
    private static final String PARSE_ERROR_KEY = "parse_error";

    private static final int SEARCH_LIMIT = 10; //magic number :-)

    //string which is substituted instead of any 
    //templating language in case of css embedding
    public static final String GENERATED_CODE = "@@@"; //NOI18N
    private static final String ERROR_MESSAGE_PREFIX = NbBundle.getMessage(CssGSFParser.class, "unexpected_symbols");

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        List<ParseException> parseExceptions = new ArrayList<ParseException>(1);
        SimpleNode root = null;
        try {
            PARSER.errors().clear();
            PARSER.ReInit(new ASCII_CharStream(new StringReader(snapshot.getText().toString())));
            root = PARSER.styleSheet();
            parseExceptions = PARSER.errors();
        } catch (ParseException pe) {
            parseExceptions.add(pe);
        } catch (TokenMgrError tme) {
            parseExceptions.add(new ParseException(tme.getMessage()));
        }

        List<Error> errors = new ArrayList<Error>();
        errors.addAll(errors(parseExceptions, snapshot)); //parser errors
        errors.addAll(CssAnalyser.checkForErrors(snapshot, root));
        
        this.lastResult = new CssParserResult(this, snapshot, root, errors);
    }

        @Override
    public Result getResult(Task task) {
        return lastResult;
    }

    @Override
    public void cancel() {
        //xxx do we need this? can we do that?
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        //no-op, no state changes supported
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        //no-op, no state changes supported
    }

    public List<Error> errors(List<ParseException> parseExceptions, Snapshot snapshot) {
        List<Error> errors = new ArrayList<Error>(parseExceptions.size());
        for (ParseException pe : parseExceptions) {
            Error e = createError(pe, snapshot);
            if (e != null) {
                errors.add(e);
            }
        }
        return errors;
    }

    public static  boolean containsGeneratedCode(CharSequence text) {
        return CharSequenceUtilities.indexOf(text, GENERATED_CODE) != -1;
    }

    private Error createError(ParseException pe, Snapshot snapshot) {
        FileObject fo = snapshot.getSource().getFileObject();
        Token lastSuccessToken = pe.currentToken;
        if (lastSuccessToken == null) {
            //The pe was created in response to a TokenManagerError
            return new DefaultError(PARSE_ERROR_KEY, pe.getMessage(), pe.getMessage(), fo,
                    0, 0, Severity.ERROR);
        }
        Token errorToken = lastSuccessToken.next;
        int from = errorToken.offset;

        if (!(containsGeneratedCode(lastSuccessToken.image) || containsGeneratedCode(errorToken.image))) {
            if(!filterError(pe, snapshot, errorToken)) {
                String errorMessage = buildErrorMessage(pe);
                int documentStartOffset = LexerUtils.findNearestMappableSourcePosition(snapshot, from, false, SEARCH_LIMIT);
                int documentEndOffset = LexerUtils.findNearestMappableSourcePosition(snapshot, from + errorToken.image.length(), true, SEARCH_LIMIT);

                if (documentStartOffset == -1 && documentEndOffset == -1) {
                    //the error is completely out of the mappable area, map it to the beginning of the document
                    documentStartOffset = documentEndOffset = 0;
                } else if (documentStartOffset == -1) {
                    documentStartOffset = documentEndOffset;
                } else if (documentEndOffset == -1) {
                    documentEndOffset = documentStartOffset;
                }

                assert documentStartOffset <= documentEndOffset;

                return new DefaultError(PARSE_ERROR_KEY, errorMessage, errorMessage, fo,
                        documentStartOffset, documentEndOffset, Severity.ERROR);
            }
        }
        return null;
    }

    private boolean filterError(ParseException pe, Snapshot snapshot, Token errorToken) {
        //#182133 - filter error in css virtual source code for empty html tag class attribute
        //<div class=""/> generates .|{} for the empty value so the css completion can work there
        //and offer all classes
        if (pe.currentToken.kind == CssParserConstants.DOT
                && errorToken.kind == CssParserConstants.LBRACE
                && snapshot.getOriginalOffset(pe.currentToken.offset) == -1) {
            return true;
        }

        return false;
    }

    private String buildErrorMessage(ParseException pe) {
        StringBuilder buff = new StringBuilder();
        buff.append(ERROR_MESSAGE_PREFIX);

        int maxSize = 0;
        for (int i = 0; i < pe.expectedTokenSequences.length; i++) {
            if (maxSize < pe.expectedTokenSequences[i].length) {
                maxSize = pe.expectedTokenSequences[i].length;
            }
        }

        Token tok = pe.currentToken.next;
        buff.append('"');
        for (int i = 0; i < maxSize; i++) {
            buff.append(tok.image);
            if (i < maxSize - 1) {
                buff.append(',');
                buff.append(' ');
            }
            tok = tok.next;
        }
        buff.append('"');

        return buff.toString();
    }

}
