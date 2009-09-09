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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java_cup.runtime.Symbol;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.project.api.PhpLanguageOptions;
import org.netbeans.modules.php.project.api.PhpLanguageOptions.Properties;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;


/**
 *
 * @author Petr Pisl
 */
public class GSFPHPParser extends Parser implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(GSFPHPParser.class.getName());

    private boolean shortTags = true;
    private boolean aspTags = false;
    private ParserResult result = null;
    private boolean projectPropertiesListenerAdded = false;
    private Collection<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void cancel() {
        // TODO
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        FileObject file = snapshot.getSource().getFileObject();
        LOGGER.fine("parseFiles " + file);
//        ParseListener listener = request.listener;
        
//        ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
//        request.listener.started(beginEvent);
        Properties languageProperties = PhpLanguageOptions.getDefault().getProperties(file);

        if (!projectPropertiesListenerAdded){
            PropertyChangeListener weakListener = WeakListeners.propertyChange(this, languageProperties);
            PhpLanguageOptions.getDefault().addPropertyChangeListener(weakListener);
            projectPropertiesListenerAdded = true;
        }

        shortTags = languageProperties.areShortTagsEnabled();
        aspTags = languageProperties.areAspTagsEnabled();
        int end = 0;
        try {
            String source = snapshot.getText().toString();
            end = source.length();
            int caretOffset = GsfUtilities.getLastKnownCaretOffset(snapshot, event);
            LOGGER.fine("caretOffset: " + caretOffset); //NOI18N
            Context context = new Context(snapshot, source, caretOffset);
            result = parseBuffer(context, Sanitize.NONE, null);
        } catch (Exception exception) {
            LOGGER.fine ("Exception during parsing: " + exception);
            ASTError error = new ASTError(0, end);
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(error);
            Program emptyProgram = new Program(0, end, statements, Collections.<Comment>emptyList());
            result = new PHPParseResult(snapshot, emptyProgram);
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
//                System.out.println("------- " + sanitizing.name() + "-------------------");
//                System.out.println(source);
//                System.out.println("------------------------------------------------------");
            } else {
                // Try next trick
                return sanitize(context, sanitizing, errorHandler);
            }
        }

        PHPParseResult result;
        // calling the php ast parser itself
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(source), shortTags,  aspTags);
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);

        //if (!sanitizedSource) {
            parser.setErrorHandler(errorHandler);
//        }
//        else {
//            parser.setErrorHandler(null);
//        }

        java_cup.runtime.Symbol rootSymbol = parser.parse();
        if (scanner.getCurlyBalance() != 0 && !sanitizedSource) {
            sanitizeSource(context, Sanitize.MISSING_CURLY, null);
            if (context.sanitizedSource != null) {
                context.source = context.getSanitizedSource();
//                System.out.println("---------- Curly  -------------");
//                System.out.println(context.sanitizedSource);
//                System.out.println("-----------------------");
                source = context.source;
                scanner = new ASTPHP5Scanner(new StringReader(source), shortTags, aspTags);
                parser = new ASTPHP5Parser(scanner);
                rootSymbol = parser.parse();
            }
        }
        if (rootSymbol != null) {
            Program program = null;
            if (rootSymbol.value instanceof Program) {
                program = (Program)rootSymbol.value; // call the parser itself
                List<Statement> statements = program.getStatements();
                //do we need sanitization?
                boolean ok = true;
                for (Statement statement : statements) {
                    if (statement instanceof ASTError) {
                        // if there is an errot, try to sanitize only if there 
                        // is a class or function inside the error
                        String errorCode = "<?" + source.substring(statement.getStartOffset(), statement.getEndOffset()) + "?>";
                        ASTPHP5Scanner fcScanner = new ASTPHP5Scanner(new StringReader(errorCode), shortTags, aspTags);
                        Symbol token = fcScanner.next_token();
                        while (token.sym != ASTPHP5Symbols.EOF) {
                            if (token.sym == ASTPHP5Symbols.T_CLASS || token.sym == ASTPHP5Symbols.T_FUNCTION) {
                                ok = false;
                                break;
                            }
                            token = fcScanner.next_token();
                        }
                        if (!ok) {
                            break;
                        }
                    }
                }
                if (ok) {
                        result = new PHPParseResult(context.getSnapshot(), program);
                    }
                else {
                    result = sanitize(context, sanitizing, errorHandler);
                }
            }
            else {
                LOGGER.fine ("The parser value is not a Program: " + rootSymbol.value);
                result = sanitize(context, sanitizing, errorHandler);
            }

            //if (!sanitizedSource) {
                result.setErrors(errorHandler.displaySyntaxErrors(program));
            //}
        }
        else { // there was no rootElement
            result = sanitize(context, sanitizing, errorHandler);
            result.setErrors(errorHandler.displayFatalError());
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
                String replace = source.substring(start, end);
                if ("}".equals(replace)) {
                    return false;
                }
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
                if (source.substring(start, end).equals("}"))
                    return false;
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

                StringBuffer sb = new StringBuffer(end - start);
                for (int index = start; index < end; index++) {
                    if (source.charAt(index) == ' ' || source.charAt(index) == '}'
                            || source.charAt(index) == '\n' || source.charAt(index) == '\r'){
                        sb.append(source.charAt(index));
                    }
                    else {
                        sb.append(' ');
                    }
                }

                context.sanitizedSource = source.substring(0, start) + sb.toString() + source.substring(end);
                context.sanitizedRange = new OffsetRange(start, end);
                return true;
            }
        }
        if (sanitizing == Sanitize.EDITED_LINE) {
            if (context.caretOffset > -1) {
                String source = context.getSource();
                int start = context.caretOffset - 1;
                int end = context.caretOffset;
                // fix until new line or }
                char c = source.charAt(start);
                while (start > 0 && c != '\n' && c != '\r' && c != '{' && c != '}') {
                    c = source.charAt(--start);
                }
                start++;
                if (end < source.length()) {
                    c = source.charAt(end);
                    while (end < source.length() && c != '\n' && c != '\r' && c != '{' && c != '}') {
                        c = source.charAt(end++);
                    }
                }
                context.sanitizedSource = source.substring(0, start) + Utils.getSpaces(end-start) + source.substring(end);
                context.sanitizedRange = new OffsetRange(start, end);
                return true;
            }
        }
        if (sanitizing == Sanitize.MISSING_CURLY) {
            return sanitizeCurly (context);
        }
        if (sanitizing == Sanitize.SYNTAX_ERROR_BLOCK) {
            List<PHP5ErrorHandler.SyntaxError> syntaxErrors = errorHandler.getSyntaxErrors();
            if (syntaxErrors.size() > 0) {
                PHP5ErrorHandler.SyntaxError error =  syntaxErrors.get(0);
                return sanitizeRemoveBlock(context, error.getCurrentToken().left);
            }
        }
        return false;
    }

    protected boolean sanitizeCurly (Context context) {
        String source = context.getSource();
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(source), shortTags, aspTags);
        //keep index of last ?>
        Symbol lastPHPToken = null;
        Symbol token = null;

        int bracketCounter = 0;
        int bracketClassCounter = 0;

        try {
            token = scanner.next_token();
            boolean inClass = false;
            int lastOpenInClass = -1;
            while (token.sym != ASTPHP5Symbols.EOF) {
                switch (token.sym) {
                    case ASTPHP5Symbols.T_CLASS:
                        if (!inClass) {
                            inClass = true;
                        }
                        else {
                            char c;
                            int index = token.left;
                            // missing only the only one }
                            int max = bracketClassCounter;
                            for (int i = 0; i < max; i++) {
                                index--;
                                c = source.charAt(index);
                                while (index > lastOpenInClass &&  c != '}'
                                        && c != '\n' && c != '\r' && c != '\t' && c != ' ') {
                                    index--;
                                    c = source.charAt(index);
                                }
                                if (c != '}' && c !='{') {
                                    source = source.substring(0, index) + '}' + source.substring(index+1);
                                    bracketClassCounter--;
                                }
                            }

                            if (bracketClassCounter > 0) {
                                // try to add } at the beginig of line
                                index--;
                                c = source.charAt(index);
                                while (index > 0 &&  c != '}'
                                        && c != '\n' && c != '\r' ) {
                                    index--;
                                    c = source.charAt(index);
                                }
                                if (c == '}') {
                                    c = source.charAt(--index);
                                }
                                while (index < source.length() && bracketClassCounter > 0
                                        && (c == '\n' || c == '\r' || c == '\t' || c == ' ')) {
                                    source = source.substring(0, index) + '}' + source.substring(index+1);
                                    bracketClassCounter--;
                                    c = source.charAt(--index);
                                }
                            }
                            context.sanitizedSource = source;
                        }
                        break;
                    case ASTPHP5Symbols.T_CURLY_OPEN:
                    case ASTPHP5Symbols.T_CURLY_OPEN_WITH_DOLAR:
                        if (inClass) {
                            bracketClassCounter++;
                            lastOpenInClass = token.left;
                        }
                        else {
                            bracketCounter++;
                        }
                        break;
                    case ASTPHP5Symbols.T_CURLY_CLOSE:
                        if (inClass) {
                            bracketClassCounter--;
                            if (bracketClassCounter == 0) {
                                inClass = false;
                            }
                        }
                        else {
                            bracketCounter--;
                        }
                        break;
                }
                if (token.sym != ASTPHP5Symbols.T_INLINE_HTML) {
                    lastPHPToken = token;
                }
                token = scanner.next_token();
            }
        } catch (IOException exception) {
            LOGGER.log(Level.INFO, "Exception during calculating missing }", exception);
        }
        int count = bracketCounter + bracketClassCounter;
        if (count > 0){
            if(lastPHPToken != null) {
                String lastTokenText = source.substring(lastPHPToken.left, lastPHPToken.right).trim();
                if ("?>".equals(lastTokenText)) {   //NOI18N
                    context.sanitizedSource = source.substring(0, lastPHPToken.left) + Utils.getRepeatingChars('}', count) + source.substring(lastPHPToken.left);
                    context.sanitizedRange = new OffsetRange(lastPHPToken.left, lastPHPToken.left + count);
                    return true;
                }
                if (token.sym == ASTPHP5Symbols.EOF) {
                    context.sanitizedSource = source.substring(0, token.left) + Utils.getRepeatingChars('}', count) + source.substring(token.left);
                    context.sanitizedRange = new OffsetRange(token.left, token.left + count);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean sanitizeRemoveBlock(Context context, int index) {
        String source = context.getSource();
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(source), shortTags, aspTags);
        Symbol token = null;
        int start = -1;
        int end = -1;
        try {
            token = scanner.next_token();
            while (token.sym != ASTPHP5Symbols.EOF && end == -1) {
                if (token.sym == ASTPHP5Symbols.T_CURLY_OPEN && token.left <= index) {
                    start = token.right;
                }
                if (token.sym == ASTPHP5Symbols.T_CURLY_CLOSE && token.left >= index ) {
                    end = token.right - 1;
                }
                token = scanner.next_token();
            }
        }
        catch (IOException exception) {
            LOGGER.log(Level.INFO, "Exception during removing block", exception);   //NOI18N
        }
        if (start > -1 && start < end) {
            context.sanitizedSource = source.substring(0, start) + Utils.getSpaces(end-start) + source.substring(end);
            context.sanitizedRange = new OffsetRange(start, end);
            return true;
        }
        return false;
    }

    private PHPParseResult sanitize(final Context context, final Sanitize sanitizing, PHP5ErrorHandler errorHandler) throws Exception {
        switch(sanitizing) {
            case NONE:
            case MISSING_CURLY:
                return parseBuffer(context, Sanitize.SYNTAX_ERROR_CURRENT, errorHandler);
            case SYNTAX_ERROR_CURRENT:
                // one more time
                return parseBuffer(context, Sanitize.SYNTAX_ERROR_PREVIOUS, errorHandler);
            case SYNTAX_ERROR_PREVIOUS:
                return parseBuffer(context, Sanitize.SYNTAX_ERROR_PREVIOUS_LINE, errorHandler);
            case SYNTAX_ERROR_PREVIOUS_LINE:
                return parseBuffer(context, Sanitize.EDITED_LINE, errorHandler);
            case EDITED_LINE:
                return parseBuffer(context, Sanitize.SYNTAX_ERROR_BLOCK, errorHandler);
            default:
                int end = context.getSource().length();
                // add the ast error, some features can recognized that there is something wrong.
                // for example folding.
                ASTError error = new ASTError(0, end);
                List<Statement> statements = new ArrayList<Statement>();
                statements.add(error);
                Program emptyProgram = new Program(0, end, statements, Collections.<Comment>emptyList());

                return new PHPParseResult(context.getSnapshot(), emptyProgram);
        }
        
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PhpLanguageOptions.PROP_PHP_VERSION.equals(evt.getPropertyName())){
            forceReparsing();
        }
    }

    private void forceReparsing(){
        for (ChangeListener changeListener : changeListeners){
            changeListener.stateChanged(new ChangeEvent(this));
        }
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
        /** try to delete the whole block, where is the error*/
        SYNTAX_ERROR_BLOCK,
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
        /** Attempt to fix missing } */
        MISSING_CURLY,
    }
    
    /** Parsing context */
    public static class Context {
        private final Snapshot snapshot;
        private int errorOffset;
        private String source;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private String sanitizedContents;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;

        
        public Context(Snapshot snapshot, String source, int caretOffset) {
            this.snapshot = snapshot;
            this.source = source;
            this.caretOffset = caretOffset;
        }
        
        @Override
        public String toString() {
            return "PHPParser.Context(" + snapshot.getSource().getFileObject() + ")"; // NOI18N
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
         * @return the file
         */
        public Snapshot getSnapshot() {
            return snapshot;
        }

        /**
         * @return the source
         */
        public String getSource() {
            return source;
        }
    }
}
