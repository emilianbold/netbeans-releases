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
package org.netbeans.modules.css.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.gsf.spi.DefaultError;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
public class CssParserAccess {

    private static final String PREFIX = "GENERATED_";
    private static final String ERROR_MESSAGE_PREFIX = NbBundle.getMessage(CssParserAccess.class, "unexpected_symbols");
    
    private static CssParserAccess DEFAULT = newInstance();

    public static CssParserAccess newInstance() {
        return new CssParserAccess();
    }

    public static CssParserAccess getDefault() {
        return DEFAULT;
    }
    private final CSSParser PARSER;

    private CssParserAccess() {
        PARSER = new CSSParser();
    }

    public synchronized CssParserResult parse(Reader reader) {
        SimpleNode root = null;
        List<ParseException> parseExceptions = new ArrayList<ParseException>(1);
        try {
            PARSER.errors().clear();
            PARSER.ReInit(new ASCII_CharStream(reader));
            root = PARSER.styleSheet();
            parseExceptions = PARSER.errors();
        } catch (ParseException pe) {
            parseExceptions.add(pe);
        } catch (TokenMgrError tme) {
            parseExceptions.add(new ParseException(tme.getMessage()));
        }
        
        return new CssParserResult(root, parseExceptions);

    }

    public static boolean containsGeneratedCode(String text) {
        return text.contains(PREFIX);
    }

    public static final class CssParserResult {

        private List<ParseException> parseExceptions;
        private SimpleNode root;

        private CssParserResult(SimpleNode root, List<ParseException> parseExceptions) {
            this.root = root;
            this.parseExceptions = parseExceptions;
        }

        public SimpleNode root() {
            return root;
        }

        public List<Error> errors(ParserFile file) {
            List<Error> errors = new ArrayList<Error>(parseExceptions.size());
            for (ParseException pe : parseExceptions) {
                Error e = createError(pe, file);
                if(e != null) {
                    errors.add(e);
                }
            }
            return errors;
        }

        private Error createError(ParseException pe, ParserFile file) {
            Token lastSuccessToken = pe.currentToken;
            if(lastSuccessToken == null) {
                //The pe was created in response to a TokenManagerError 
                return new DefaultError(pe.getMessage(), pe.getMessage(), null, file.getFileObject(),
                        0, 0, Severity.ERROR);
            }
            Token errorToken = lastSuccessToken.next;
            int from = errorToken.offset;

            if (!(containsGeneratedCode(lastSuccessToken.image) || containsGeneratedCode(errorToken.image))) {
                String errorMessage = buildErrorMessage(pe);
                return new DefaultError(errorMessage, errorMessage, null, file.getFileObject(),
                        from, from, Severity.ERROR);
            }
            return null;
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
}
