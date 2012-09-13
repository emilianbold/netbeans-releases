/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.nashorn.parser.Token;
import com.oracle.nashorn.runtime.ErrorManager;
import com.oracle.nashorn.runtime.Source;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsErrorManager extends ErrorManager {

    private static final Logger LOGGER = Logger.getLogger(JsErrorManager.class.getName());

    private List<ParserError> parserErrors;

    private List<JsParserError> convertedErrors;

    private FileObject fileObject;

    public JsErrorManager(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    public Error getMissingCurlyError() {
        if (parserErrors == null) {
            return null;
        }
        for (ParserError error : parserErrors) {
            if (error.message != null
                    && (error.message.contains("Expected }") || error.message.contains("but found }"))) { // NOI18N
                return convert(error);
            }
        }
        return null;
    }

    public Error getMissingSemicolonError() {
        if (parserErrors == null) {
            return null;
        }
        for (ParserError error : parserErrors) {
            if (error.message != null
                    && error.message.contains("Expected ;")) { // NOI18N
                return convert(error);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return parserErrors == null;
    }

    @Override
    public void error(String message, Source source, int line, int column, long token) {
        LOGGER.log(Level.FINE, "Error {0} [{1}, {2}]", new Object[] {message, line, column});
        addParserError(new ParserError(message, line, column, token));
    }

    @Override
    public void error(String message, Source source, long token) {
        LOGGER.log(Level.FINE, "Error {0} ({1})", new Object[] {message, token});
        addParserError(new ParserError(message, token));
    }

    @Override
    public void error(String message, long token) {
        LOGGER.log(Level.FINE, "Error {0} ({1})", new Object[] {message, token});
        addParserError(new ParserError(message, token));
    }

    @Override
    public void error(String message) {
        LOGGER.log(Level.FINE, "Error {0}", message);
        addParserError(new ParserError(message));
    }

    @Override
    public void warning(String message, Source source, int line, int column, long token) {
        LOGGER.log(Level.FINE, "Warning {0} [{1}, {2}]",  new Object[] {message, line, column});
    }

    @Override
    public void warning(String message, Source source, long token) {
        LOGGER.log(Level.FINE, "Warning {0} ({1})", new Object[] {message, token});
    }

    @Override
    public void warning(String message, long token) {
        LOGGER.log(Level.FINE, "Warning {0} ({1})", new Object[] {message, token});
    }

    @Override
    public void warning(String message) {
        LOGGER.log(Level.FINE, "Warning {0}", message);
    }

    public List<? extends Error> getErrors() {
        if (convertedErrors == null) {
            if (parserErrors == null) {
                convertedErrors = Collections.emptyList();
            } else {
                ArrayList<JsParserError> errors = new ArrayList<JsParserError>(parserErrors.size());
                for (ParserError error : parserErrors) {
                    errors.add(convert(error));
                }
                Collections.sort(errors, JsParserError.POSITION_COMPARATOR);
                convertedErrors = errors;
            }
        }
        return Collections.unmodifiableList(convertedErrors);
    }

    JsErrorManager fill(JsErrorManager original) {
        this.fileObject = original.fileObject;
        if (original.parserErrors != null) {
            this.parserErrors = new ArrayList<ParserError>(original.parserErrors);
        } else {
            this.parserErrors = null;
        }
        this.convertedErrors = null;
        return this;
    }

    private void addParserError(ParserError error) {
        convertedErrors = null;
        if (parserErrors == null) {
            parserErrors = new ArrayList<ParserError>();
        }
        parserErrors.add(error);
    }

    private JsParserError convert(ParserError error) {
        String message = error.message;
        int offset = -1;
        if (error.token > 0) {
            offset = Token.descPosition(error.token);
        } else if (error.line == -1 && error.column == -1) {
            String parts[] = error.message.split(":");
            if (parts.length > 4) {
                message = parts[4];
                int index = message.indexOf('\n');
                if (index > 0) {
                    message = message.substring(0, index);
                }

            }
            if (parts.length > 3) {
                try {
                    offset = Integer.parseInt(parts[3]);
                } catch (NumberFormatException nfe) {
                    // do nothing
                }
            }
        }

        return new JsParserError(message, fileObject, offset, offset, Severity.ERROR, null,  true);
    }

    private static class ParserError {

        protected final String message;

        protected final int line;

        protected final int column;

        protected final long token;

        public ParserError(String message, int line, int column, long token) {
            this.message = message;
            this.line = line;
            this.column = column;
            this.token = token;
        }

        public ParserError(String message, long token) {
            this(message, -1, -1, token);
        }

        public ParserError(String message) {
            this(message, -1, -1, -1);
        }
    }
}
