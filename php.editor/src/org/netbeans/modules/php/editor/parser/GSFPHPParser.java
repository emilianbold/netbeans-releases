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

package org.netbeans.modules.php.editor.parser;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.*;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;


/**
 *
 * @author Petr Pisl
 */
public class GSFPHPParser implements Parser {

    private PositionManager positionManager = null;
    
    private static final Logger LOGGER = Logger.getLogger(GSFPHPParser.class.getName());
    
    public void parseFiles(Job request) {
        LOGGER.fine("parseFiles " + request.toString());
        ParseListener listener = request.listener;
        SourceFileReader reader = request.reader;
        
        for (ParserFile file : request.files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            request.listener.started(beginEvent);
            ParserResult result = null;
            try {
                CharSequence buffer = reader.read(file);
                String source = asString(buffer);
                
                int caretOffset = reader.getCaretOffset(file);
                if (caretOffset != -1 && request.translatedSource != null) {
                    caretOffset = request.translatedSource.getAstOffset(caretOffset);
                }
                LOGGER.fine("caretOffset: " + caretOffset); //NOI18N
                Context context = new Context(file, listener, source, caretOffset, request.translatedSource);
                result = parseBuffer(context, Sanitize.NONE, null);
            } catch (Exception exception) {
                listener.exception(exception);
                LOGGER.fine ("Exception during parsing: " + exception);
            }
            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            listener.finished(doneEvent);
        }
        
    }

    protected PHPParseResult parseBuffer(final Context context, final Sanitize sanitizing, PHP5ErrorHandler errorHandler) throws Exception  {
        boolean sanitizedSource = false;
        String source = context.source;
        if (errorHandler == null) {
            errorHandler = new PHP5ErrorHandler(context,this);
        }
        if (!((sanitizing == Sanitize.NONE) || (sanitizing == Sanitize.NEVER))) {
            boolean ok = sanitizeSource(context, sanitizing, errorHandler);

            if (ok) {
                assert context.sanitizedSource != null;
                sanitizedSource = true;
                source = context.sanitizedSource;
            } else {
                // Try next trick
                return sanitize(context, sanitizing, errorHandler);
            }
        }

        PHPParseResult result;
        // calling the php ast parser itself
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(source), false);
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);

        if (!sanitizedSource) {
            parser.setErrorHandler(errorHandler);
        }
        else {
            parser.setErrorHandler(null);
        }

        java_cup.runtime.Symbol rootSymbol = parser.parse();
        if (rootSymbol != null) {
            Program program = null;
            if (rootSymbol.value instanceof Program) {
                program = (Program)rootSymbol.value; // call the parser itself
                List<Statement> statements = program.getStatements();
                //do we need sanitization?
                boolean ok = false;
                for (Statement statement : statements) {
                    if (!(statement instanceof ASTError) && !(statement instanceof EmptyStatement)) {
                        ok = true;
                        break;
                    }
                }
                if (ok) {
                    result = new PHPParseResult(this, context.getFile(), program);
                }
                else {
                    result = sanitize(context, sanitizing, errorHandler);
                }
            }
            else {
                LOGGER.fine ("The parser value is not a Program: " + rootSymbol.value);
                result = sanitize(context, sanitizing, errorHandler);
            }
            if (!sanitizedSource) {
                errorHandler.displaySyntaxErrors(program);
            }
        }
        else {

            result = sanitize(context, sanitizing, errorHandler);
        }
        
        return result;
    }

    private boolean sanitizeSource(Context context, Sanitize sanitizing, PHP5ErrorHandler errorHandler) {
        if (sanitizing == Sanitize.SYNTAX_ERROR_CURRENT) {
            List<PHP5ErrorHandler.SyntaxError> syntaxErrors = errorHandler.getSyntaxErrors();
            if (syntaxErrors.size() > 0) {
                PHP5ErrorHandler.SyntaxError error =  syntaxErrors.get(0);
                String source;
                if (context.sanitized == Sanitize.NONE) {
                    source = context.source;
                }
                else {
                    source = context.sanitizedSource;
                }

                int end = error.getCurrentToken().right;
                int start = error.getCurrentToken().left;

                context.sanitizedSource = source.substring(0, start) + Utils.getSpaces(end-start) + source.substring(end);
                context.sanitizedRange = new OffsetRange(start, end);
                return true;
            }
        }
        if (sanitizing == Sanitize.SYNTAX_ERROR_PREVIOUS) {
            List<PHP5ErrorHandler.SyntaxError> syntaxErrors = errorHandler.getSyntaxErrors();
            if (syntaxErrors.size() > 0) {
                PHP5ErrorHandler.SyntaxError error =  syntaxErrors.get(0);
                String source = context.source;

                int end = error.getPreviousToken().right;
                int start = error.getPreviousToken().left;

                context.sanitizedSource = source.substring(0, start) + Utils.getSpaces(end-start) + source.substring(end);
                context.sanitizedRange = new OffsetRange(start, end);
                return true;
            }
        }
        if (sanitizing == Sanitize.SYNTAX_ERROR_PREVIOUS_LINE) {
            List<PHP5ErrorHandler.SyntaxError> syntaxErrors = errorHandler.getSyntaxErrors();
            if (syntaxErrors.size() > 0) {
                PHP5ErrorHandler.SyntaxError error =  syntaxErrors.get(0);
                String source = context.source;

                int end = Utils.getRowEnd(source, error.getPreviousToken().right);
                int start = Utils.getRowStart(source, error.getPreviousToken().left);

                context.sanitizedSource = source.substring(0, start) + Utils.getSpaces(end-start) + source.substring(end);
                context.sanitizedRange = new OffsetRange(start, end);
                return true;
            }
        }
        return false;
    }


    private PHPParseResult sanitize(final Context context, final Sanitize sanitizing, PHP5ErrorHandler errorHandler) throws Exception{
        
        switch(sanitizing) {
            case NONE:
                return parseBuffer(context, Sanitize.SYNTAX_ERROR_CURRENT, errorHandler);
            case SYNTAX_ERROR_CURRENT:
                // one more time
                return parseBuffer(context, Sanitize.SYNTAX_ERROR_PREVIOUS, errorHandler);
            case SYNTAX_ERROR_PREVIOUS:
                return parseBuffer(context, Sanitize.SYNTAX_ERROR_PREVIOUS_LINE, errorHandler);
            default:
                int end = context.getSource().length();
                Program emptyProgram = new Program(0, end, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
                return new PHPParseResult(this, context.getFile(), emptyProgram);
        }
        
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }
    
    public PositionManager getPositionManager() {
        if (positionManager == null) {
            positionManager = new PHPPositionManager();
        }
        return positionManager;
    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER, 
        /** Perform no sanitization */
        NONE,
        /** Remove current error token */
        SYNTAX_ERROR_CURRENT,
        /** Remove token before error */
        SYNTAX_ERROR_PREVIOUS,
        /** remove line with error */
        SYNTAX_ERROR_PREVIOUS_LINE,
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT, 
        /** Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line */
        ERROR_DOT, 
        /** Try to remove the initial "if" or "unless" on the block
         * in case it's not terminated
         */
        BLOCK_START,
        /** Try to cut out the error line */
        ERROR_LINE, 
        /** Try to cut out the current edited line, if known */
        EDITED_LINE,
        /** Attempt to add an "end" to the end of the buffer to make it compile */
        MISSING_END,
    }
    
    /** Parsing context */
    public static class Context {
        private final ParserFile file;
        private final ParseListener listener;
        private int errorOffset;
        private String source;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private String sanitizedContents;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;
        private TranslatedSource translatedSource;

        
        public Context(ParserFile parserFile, ParseListener listener, String source, int caretOffset, TranslatedSource translatedSource) {
            this.file = parserFile;
            this.listener = listener;
            this.source = source;
            this.caretOffset = caretOffset;
            this.translatedSource = translatedSource;
        }
        
        @Override
        public String toString() {
            return "PHPParser.Context(" + getFile().toString() + ")"; // NOI18N
        }
        
        public OffsetRange getSanitizedRange() {
            return sanitizedRange;
        }

        public Sanitize getSanitized() {
            return sanitized;
        }
        
        public String getSanitizedSource() {
            return sanitizedSource;
        }
        
        public int getErrorOffset() {
            return errorOffset;
        }

        /**
         * @return the listener
         */
        public ParseListener getListener() {
            return listener;
        }

        /**
         * @return the file
         */
        public ParserFile getFile() {
            return file;
        }

        /**
         * @return the source
         */
        public String getSource() {
            return source;
        }
    }
}
