/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java_cup.runtime.Symbol;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.php.editor.parser.GSFPHPParser.Context;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class PHP5ErrorHandler implements ParserErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(PHP5ErrorHandler.class.getName());

    public static class SyntaxError {
        private final short[] expectedTokens;
        private final Symbol currentToken;
        private final Symbol previousToken;

        public SyntaxError(short[] expectedTokens, Symbol currentToken, Symbol previousToken) {
            this.expectedTokens = expectedTokens;
            this.currentToken = currentToken;
            this.previousToken = previousToken;
        }

        public Symbol getCurrentToken() {
            return currentToken;
        }

        public Symbol getPreviousToken() {
            return previousToken;
        }

        public short[] getExpectedTokens() {
            return expectedTokens;
        }
    }

    private final List<SyntaxError> syntaxErrors;

    private final Context context;
    GSFPHPParser outer;

    public PHP5ErrorHandler(Context context, GSFPHPParser outer) {
        super();
        this.outer = outer;
        this.context = context;
        syntaxErrors = new ArrayList<SyntaxError>();
    }

    @Override
    public void handleError(Type type, short[] expectedtokens, Symbol current, Symbol previous) {
        if (type == ParserErrorHandler.Type.SYNTAX_ERROR) {
            // logging syntax error
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Syntax error:"); //NOI18N
                LOGGER.log(Level.FINEST, "Current [{0}, {1}]({2}): {3}", new Object[]{current.left, current.right, Utils.getASTScannerTokenName(current.sym), current.value}); //NOI18N
                LOGGER.log(Level.FINEST, "Previous [{0}, {1}] ({2}):{3}", new Object[]{previous.left, previous.right, Utils.getASTScannerTokenName(previous.sym), previous.value}); //NOI18N
                StringBuilder message = new StringBuilder();
                message.append("Expected tokens:"); //NOI18N
                for (int i = 0; i < expectedtokens.length; i += 2) {
                    message.append(" ").append( Utils.getASTScannerTokenName(expectedtokens[i])); //NOI18N
                }
                LOGGER.finest(message.toString());
            }
            syntaxErrors.add(new SyntaxError(expectedtokens, current, previous));
        }
    }

    public List<Error> displayFatalError(){
        Error error = new FatalError();
        return Arrays.asList(error);
    }

    public List<Error> displaySyntaxErrors(Program program) {
        List<Error> errors = new ArrayList<Error>();
        for (SyntaxError syntaxError : syntaxErrors) {
            ASTNode astError = null;
            if (program != null) {
                astError = org.netbeans.modules.php.editor.parser.api.Utils.getNodeAtOffset(program, syntaxError.currentToken.left);
                if (!(astError instanceof ASTError)) {
                    astError = org.netbeans.modules.php.editor.parser.api.Utils.getNodeAtOffset(program, syntaxError.previousToken.right);
                    if (!(astError instanceof ASTError)) {
                        astError = null;
                    }
                }
                if (astError != null) {
                    LOGGER.log(Level.FINEST, "ASTError [{0}, {1}]", new Object[]{astError.getStartOffset(), astError.getEndOffset()}); //NOI18N
                } else {
                    LOGGER.finest("ASTError was not found");  //NOI18N
                }
            }
            Error error = defaultSyntaxErrorHandling(syntaxError, astError);
            errors.add(error);
        }
        return errors;
    }

    // This is just defualt handling. We can do a logic, which will find metter
    private Error defaultSyntaxErrorHandling(SyntaxError syntaxError, ASTNode astError) {
        Error error;
        String unexpectedText = "";     //NOI18N
        StringBuilder message = new StringBuilder();
        boolean isUnexpected;
        int start  = syntaxError.getCurrentToken().left;
        int end = syntaxError.getCurrentToken().right;

        if (syntaxError.getCurrentToken().sym == ASTPHP5Symbols.EOF) {
            isUnexpected = true;
            unexpectedText = NbBundle.getMessage(PHP5ErrorHandler.class, "SE_EOF"); //NOI18N
            start = end - 1;
        }
        else if (syntaxError.getCurrentToken().sym == ASTPHP5Symbols.T_CONSTANT_ENCAPSED_STRING) {
            isUnexpected = true;
            unexpectedText = "String"; //NOI18N
            end = start + ((String)syntaxError.getCurrentToken().value).trim().length();
        }
        else {
            String currentText = (String)syntaxError.getCurrentToken().value;
            isUnexpected = currentText != null && currentText.trim().length() > 0;
            if (isUnexpected) {
                unexpectedText = currentText.trim();
                end = start + unexpectedText.length();
            }
        }

        List<String> possibleTags = new ArrayList<String>();
        for (int i = 0; i < syntaxError.getExpectedTokens().length; i += 2) {
            String text = getTokenTextForm(syntaxError.getExpectedTokens()[i]);
            if (text != null) {
                possibleTags.add(text);
            }
        }


        message.append(NbBundle.getMessage(PHP5ErrorHandler.class, "SE_Message"));  //NOI18N
        message.append(':'); //NOI18N
        if (isUnexpected) {
            message.append(' ').append(NbBundle.getMessage(PHP5ErrorHandler.class, "SE_Unexpected")); //NOI18N
            message.append(": "); //NOI18N
            message.append(unexpectedText);
        }
        if (possibleTags.size() > 0) {
            message.append('\n').append(NbBundle.getMessage(PHP5ErrorHandler.class, "SE_Expected")); //NOI18N
            message.append(": "); //NOI18N
            boolean addOR = false;
            for (String tag : possibleTags) {
                if (addOR) {
                    message.append(", "); //NOI18N
                }
                else {
                    addOR = true;
                }

                message.append(tag);
            }
        }

        if (astError != null){
            start = astError.getStartOffset();
            end = astError.getEndOffset();
            // if the asterror is trough two lines, the problem is ussually at the end
            String text = context.getSource().substring(start, end);
            int lastNewLine = text.length()-1;
            while (text.charAt(lastNewLine) == '\n' || text.charAt(lastNewLine) == '\r'
                    || text.charAt(lastNewLine) == '\t' || text.charAt(lastNewLine) == ' ') {
                lastNewLine--;
                if (lastNewLine < 0) {
                    break;
                }
            }
            lastNewLine = text.lastIndexOf('\n', lastNewLine);   //NOI18N
            if (lastNewLine > 0) {
                start = start + lastNewLine + 1;
            }
        }
        error = new GSFPHPError(message.toString(), context.getSnapshot().getSource().getFileObject(), start, end, Severity.ERROR, new Object[]{syntaxError});
        return error;
    }

    public List<SyntaxError> getSyntaxErrors() {
        return syntaxErrors;
    }

    private String getTokenTextForm (int token) {
        String text = null;
        switch (token) {
            case ASTPHP5Symbols.T_STRING : text = "identifier"; break; //NOI18N
            case ASTPHP5Symbols.T_VARIABLE : text = "variable"; break; //NOI18N
            case ASTPHP5Symbols.T_INC : text = "++"; break; //NOI18N
            case ASTPHP5Symbols.T_DEC : text = "--"; break; //NOI18N
            case ASTPHP5Symbols.T_IS_IDENTICAL : text = "==="; break; //NOI18N
            case ASTPHP5Symbols.T_IS_NOT_IDENTICAL : text = "!=="; break; //NOI18N
            case ASTPHP5Symbols.T_IS_EQUAL : text = "=="; break; //NOI18N
            case ASTPHP5Symbols.T_IS_NOT_EQUAL : text = "!="; break; //NOI18N
            case ASTPHP5Symbols.T_IS_SMALLER_OR_EQUAL : text = "<=+"; break; //NOI18N
            case ASTPHP5Symbols.T_IS_GREATER_OR_EQUAL : text = ">=+"; break; //NOI18N
            case ASTPHP5Symbols.T_PLUS_EQUAL : text = "+="; break; //NOI18N
            case ASTPHP5Symbols.T_MINUS_EQUAL : text = "-="; break; //NOI18N
            case ASTPHP5Symbols.T_MUL_EQUAL : text = "*="; break; //NOI18N
            case ASTPHP5Symbols.T_DIV_EQUAL : text = "/="; break; //NOI18N
            case ASTPHP5Symbols.T_CONCAT_EQUAL : text = ".="; break; //NOI18N
            case ASTPHP5Symbols.T_MOD_EQUAL : text = "%="; break; //NOI18N
            case ASTPHP5Symbols.T_SL_EQUAL : text = "<<="; break; //NOI18N
            case ASTPHP5Symbols.T_SR_EQUAL : text = ">>="; break; //NOI18N
            case ASTPHP5Symbols.T_AND_EQUAL : text = "&="; break; //NOI18N
            case ASTPHP5Symbols.T_OR_EQUAL : text = "|+"; break; //NOI18N
            case ASTPHP5Symbols.T_XOR_EQUAL : text = "^="; break; //NOI18N
            case ASTPHP5Symbols.T_BOOLEAN_OR : text = "||"; break; //NOI18N
            case ASTPHP5Symbols.T_BOOLEAN_AND : text = "&&"; break; //NOI18N
            case ASTPHP5Symbols.T_LOGICAL_OR : text = "OR"; break; //NOI18N
            case ASTPHP5Symbols.T_LOGICAL_AND : text = "AND"; break; //NOI18N
            case ASTPHP5Symbols.T_LOGICAL_XOR : text = "XOR"; break; //NOI18N
            case ASTPHP5Symbols.T_SL : text = "<<"; break; //NOI18N
            case ASTPHP5Symbols.T_SR : text = ">>"; break; //NOI18N
            case ASTPHP5Symbols.T_SEMICOLON : text = "';'"; break; //NOI18N
            case ASTPHP5Symbols.T_NEKUDOTAIM : text = "':'"; break; //NOI18N
            case ASTPHP5Symbols.T_COMMA : text = "','"; break; //NOI18N
            case ASTPHP5Symbols.T_NEKUDA : text = "'.'"; break; //NOI18N
            case ASTPHP5Symbols.T_OPEN_RECT : text = "["; break; //NOI18N
            case ASTPHP5Symbols.T_CLOSE_RECT : text = "]"; break; //NOI18N
            case ASTPHP5Symbols.T_OPEN_PARENTHESE : text = "("; break; //NOI18N
            case ASTPHP5Symbols.T_CLOSE_PARENTHESE : text = ")"; break; //NOI18N
            case ASTPHP5Symbols.T_OR : text = "|"; break; //NOI18N
            case ASTPHP5Symbols.T_KOVA : text = "^"; break; //NOI18N
            case ASTPHP5Symbols.T_REFERENCE : text = "&"; break; //NOI18N
            case ASTPHP5Symbols.T_PLUS : text = "+"; break; //NOI18N
            case ASTPHP5Symbols.T_MINUS : text = "-"; break; //NOI18N
            case ASTPHP5Symbols.T_DIV : text = "/"; break; //NOI18N
            case ASTPHP5Symbols.T_TIMES : text = "*"; break; //NOI18N
            case ASTPHP5Symbols.T_EQUAL : text = "="; break; //NOI18N
            case ASTPHP5Symbols.T_PRECENT : text = "%"; break; //NOI18N
            case ASTPHP5Symbols.T_NOT : text = "!"; break; //NOI18N
            case ASTPHP5Symbols.T_TILDA : text = "~"; break; //NOI18N
            case ASTPHP5Symbols.T_DOLLAR : text = "$"; break; //NOI18N
            case ASTPHP5Symbols.T_RGREATER : text = "<"; break; //NOI18N
            case ASTPHP5Symbols.T_LGREATER : text = ">"; break; //NOI18N
            case ASTPHP5Symbols.T_QUESTION_MARK : text = "?"; break; //NOI18N
            case ASTPHP5Symbols.T_AT : text = "@"; break; //NOI18N
            case ASTPHP5Symbols.T_EXIT : text = "exit"; break; //NOI18N
            case ASTPHP5Symbols.T_FUNCTION : text = "function"; break; //NOI18N
            case ASTPHP5Symbols.T_CONST : text = "const"; break; //NOI18N
            case ASTPHP5Symbols.T_RETURN : text = "return"; break; //NOI18N
            case ASTPHP5Symbols.T_IF : text = "if"; break; //NOI18N
            case ASTPHP5Symbols.T_ELSEIF : text = "elseif"; break; //NOI18N
            case ASTPHP5Symbols.T_ENDIF : text = "endif"; break; //NOI18N
            case ASTPHP5Symbols.T_ELSE : text = "else"; break; //NOI18N
            case ASTPHP5Symbols.T_WHILE : text = "while"; break; //NOI18N
            case ASTPHP5Symbols.T_ENDWHILE : text = "endwhile"; break; //NOI18N
            case ASTPHP5Symbols.T_DO : text = "do"; break; //NOI18N
            case ASTPHP5Symbols.T_FOR : text = "for"; break; //NOI18N
            case ASTPHP5Symbols.T_ENDFOR : text = "endfor"; break; //NOI18N
            case ASTPHP5Symbols.T_FOREACH : text = "foreach"; break; //NOI18N
            case ASTPHP5Symbols.T_ENDFOREACH : text = "endforeach"; break; //NOI18N
            case ASTPHP5Symbols.T_AS : text = "as"; break; //NOI18N
            case ASTPHP5Symbols.T_SWITCH : text = "switch"; break; //NOI18N
            case ASTPHP5Symbols.T_ENDSWITCH : text = "endswitch"; break; //NOI18N
            case ASTPHP5Symbols.T_CASE : text = "case"; break; //NOI18N
            case ASTPHP5Symbols.T_DEFAULT : text = "default"; break; //NOI18N
            case ASTPHP5Symbols.T_BREAK : text = "break"; break; //NOI18N
            case ASTPHP5Symbols.T_CONTINUE : text = "continue"; break; //NOI18N
            case ASTPHP5Symbols.T_ECHO : text = "echo"; break; //NOI18N
            case ASTPHP5Symbols.T_PRINT : text = "print"; break; //NOI18N
            case ASTPHP5Symbols.T_CLASS : text = "class"; break; //NOI18N
            case ASTPHP5Symbols.T_TRY : text = "try"; break; //NOI18N
            case ASTPHP5Symbols.T_CATCH : text = "catch"; break; //NOI18N
            case ASTPHP5Symbols.T_THROW : text = "throw"; break; //NOI18N
            case ASTPHP5Symbols.T_INSTANCEOF : text = "instanceof"; break; //NOI18N
            case ASTPHP5Symbols.T_INTERFACE : text = "interface"; break; //NOI18N
            case ASTPHP5Symbols.T_IMPLEMENTS : text = "implements"; break; //NOI18N
            case ASTPHP5Symbols.T_ABSTRACT : text = "abstract"; break; //NOI18N
            case ASTPHP5Symbols.T_FINAL : text = "final"; break; //NOI18N
            case ASTPHP5Symbols.T_PRIVATE : text = "private"; break; //NOI18N
            case ASTPHP5Symbols.T_PROTECTED : text = "protected"; break; //NOI18N
            case ASTPHP5Symbols.T_PUBLIC : text = "public"; break; //NOI18N
            case ASTPHP5Symbols.T_EXTENDS : text = "extends"; break; //NOI18N
            case ASTPHP5Symbols.T_NEW : text = "new"; break; //NOI18N
            case ASTPHP5Symbols.T_EVAL : text = "eval"; break; //NOI18N
            case ASTPHP5Symbols.T_INCLUDE : text = "include"; break; //NOI18N
            case ASTPHP5Symbols.T_INCLUDE_ONCE : text = "include_once"; break; //NOI18N
            case ASTPHP5Symbols.T_REQUIRE : text = "require"; break; //NOI18N
            case ASTPHP5Symbols.T_REQUIRE_ONCE : text = "require_once"; break; //NOI18N
            case ASTPHP5Symbols.T_USE : text = "use"; break; //NOI18N
            case ASTPHP5Symbols.T_GLOBAL : text = "global"; break; //NOI18N
            case ASTPHP5Symbols.T_ISSET : text = "isset"; break; //NOI18N
            case ASTPHP5Symbols.T_EMPTY : text = "empty"; break; //NOI18N
            case ASTPHP5Symbols.T_STATIC : text = "static"; break; //NOI18N
            case ASTPHP5Symbols.T_UNSET : text = "unset"; break; //NOI18N
            case ASTPHP5Symbols.T_LIST : text = "array"; break; //NOI18N
            case ASTPHP5Symbols.T_VAR : text = "var"; break; //NOI18N
            case ASTPHP5Symbols.T_DECLARE : text = "declare"; break; //NOI18N
            case ASTPHP5Symbols.T_ENDDECLARE : text = "enddeclare"; break; //NOI18N
            case ASTPHP5Symbols.T_OBJECT_OPERATOR : text = "->"; break; //NOI18N
            case ASTPHP5Symbols.T_PAAMAYIM_NEKUDOTAYIM : text = "::"; break; //NOI18N
            case ASTPHP5Symbols.T_CURLY_CLOSE : text = "}"; break; //NOI18N
            case ASTPHP5Symbols.T_CURLY_OPEN : text = "{"; break; //NOI18N
            case ASTPHP5Symbols.T_DOUBLE_ARROW : text = "=>"; break; //NOI18N
            case ASTPHP5Symbols.T_DOLLAR_OPEN_CURLY_BRACES : text = "${"; break; //NOI18N
            default:
                //no-op
        }
        return text;
    }

    private class FatalError extends GSFPHPError{
        FatalError(){
            super(NbBundle.getMessage(PHP5ErrorHandler.class, "MSG_FatalError"),
                context.getSnapshot().getSource().getFileObject(),
                0, context.getSource().length(),
                Severity.ERROR, null);
        }

        @Override
        public boolean isLineError() {
            return false;
        }
    }
}
