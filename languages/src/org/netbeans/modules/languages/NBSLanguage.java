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

package org.netbeans.modules.languages;

import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import java.util.Arrays;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.api.languages.ASTToken;

    
/**
 *
 * @author Jan Jancura
 */
public class NBSLanguage {
    
    static final String NBS_MIME_TYPE = "text/x-nbs";
    
    public static int WHITESPACE_ID;
    public static int COMMENT_ID;
    public static int IDENTIFIER_ID;

    private static Language nbsLanguage;
    
    static Language getNBSLanguage () {
        if (nbsLanguage == null) {
            nbsLanguage = new Language (NBS_MIME_TYPE);
            try {
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "keyword",
                    Pattern.create (
                        "'ACTION' |" +
                        "'AST' |" +
                        "'BRACE' |" +
                        "'BUNDLE' |" +
                        "'COLOR' |" +
                        "'COMMENT_LINE' |" +
                        "'COMPLETE' |" +
                        "'COMPLETION' |" +
                        "'FOLD' |" +
                        "'FORMAT' |" +
                        "'HYPERLINK' |" +
                        "'IMPORT' |" +
                        "'INDENT' |" +
                        "'MARK' | " +
                        "'NAVIGATOR' |" +
                        "'PARSE' |" +
                        "'PROPERTIES' |" +
                        "'REFORMAT' |" +
                        "'SELECTION' | " +
                        "'SEMANTIC_CONTEXT' | " +
                        "'SEMANTIC_DECLARATION' | " +
                        "'SEMANTIC_USAGE' | " +
                        "'SKIP' |" +
                        "'TOKEN' |" +
                        "'TOOLTIP'"
                    ),
                    Parser.DEFAULT_STATE,
                    null
                );
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "identifier",
                    Pattern.create (
                        "['a'-'z' 'A'-'Z'] ['a'-'z' 'A'-'Z' '0'-'9' '_']*"
                    ),
                    Parser.DEFAULT_STATE,
                    null
                );
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "operator",
                    Pattern.create (
                        "':' | '*' | '?' | '+' | '-' | '[' | ']' | '<' | " +
                        "'>' | '^' | '|' | '{' | '}' | '(' | ')' | ',' | " +
                        "'=' | ';' | '.' | '$'"
                    ),
                    Parser.DEFAULT_STATE,
                    null
                );
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "string",
                    Pattern.create (
                        "'\\\"'" +
                        "(" +
                            "[^'\\\"' '\\\\' '\\r' '\\n'] |" +
                            "('\\\\' ['r' 'n' 't' '\\\\' '\\\'' '\\\"']) |" +
                            "('\\\\' 'u' ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'])" +
                        ")*" +
                        "'\\\"'"
                    ),
                    Parser.DEFAULT_STATE,
                    null
                );
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "string",
                    Pattern.create (
                        "'\\\''" +
                        "(" +
                            "[^'\\\'' '\\\\' '\\r' '\\n'] |" +
                            "('\\\\' ['r' 'n' 't' '\\\\' '\\\'' '\\\"']) |" +
                            "('\\\\' 'u' ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'] ['0'-'9' 'a'-'f' 'A'-'F'])" +
                        ")*" +
                        "'\\\''"
                    ),
                    Parser.DEFAULT_STATE,
                    null
                );
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "comment",
                    Pattern.create ("'#' [^'\\n' '\\r']* ['\\n' '\\r']+"),
                    Parser.DEFAULT_STATE,
                    null
                );
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "comment",
                    Pattern.create ("'/#' - '#/'"),
                    Parser.DEFAULT_STATE,
                    null
                );
                nbsLanguage.addToken (
                    Parser.DEFAULT_STATE, 
                    "whitespace",
                    Pattern.create ("['\\n' '\\r' ' ' '\\t']+"),
                    Parser.DEFAULT_STATE,
                    null
                );

                int OPERATOR_ID = nbsLanguage.getTokenID ("operator");
                ASTToken COLON = ASTToken.create (nbsLanguage, OPERATOR_ID, ":", 0);
                ASTToken PARENTHESIS = ASTToken.create (nbsLanguage, OPERATOR_ID, "(", 0);
                ASTToken PARENTHESIS2 = ASTToken.create (nbsLanguage, OPERATOR_ID, ")", 0);
                ASTToken BRACE = ASTToken.create (nbsLanguage, OPERATOR_ID, "{", 0);
                ASTToken BRACE2 = ASTToken.create (nbsLanguage, OPERATOR_ID, "}", 0);
                ASTToken LT = ASTToken.create (nbsLanguage, OPERATOR_ID, "<", 0);
                ASTToken GT = ASTToken.create (nbsLanguage, OPERATOR_ID, ">", 0);
                ASTToken DOT = ASTToken.create (nbsLanguage, OPERATOR_ID, ".", 0);
                ASTToken PLUS = ASTToken.create (nbsLanguage, OPERATOR_ID, "+", 0);
                ASTToken QUESTION = ASTToken.create (nbsLanguage, OPERATOR_ID, "?", 0);
                ASTToken MULTIPLY = ASTToken.create (nbsLanguage, OPERATOR_ID, "*", 0);
                ASTToken OR = ASTToken.create (nbsLanguage, OPERATOR_ID, "|", 0);
                ASTToken MINUS = ASTToken.create (nbsLanguage, OPERATOR_ID, "-", 0);
                ASTToken BRACKET = ASTToken.create (nbsLanguage, OPERATOR_ID, "[", 0);
                ASTToken BRACKET2 = ASTToken.create (nbsLanguage, OPERATOR_ID, "]", 0);
                ASTToken UPP = ASTToken.create (nbsLanguage, OPERATOR_ID, "^", 0);
                ASTToken EQUAL = ASTToken.create (nbsLanguage, OPERATOR_ID, "=", 0);
                ASTToken SEMICOLON = ASTToken.create (nbsLanguage, OPERATOR_ID, ";", 0);
                ASTToken COMMA = ASTToken.create (nbsLanguage, OPERATOR_ID, ",", 0);
                int KEYWORD_ID = nbsLanguage.getTokenID ("keyword");
                ASTToken KEYWORD = ASTToken.create (nbsLanguage, KEYWORD_ID, null, 0);
                ASTToken KEYWORD_TOKEN = ASTToken.create (nbsLanguage, KEYWORD_ID, "TOKEN", 0);
                IDENTIFIER_ID = nbsLanguage.getTokenID ("identifier");
                ASTToken IDENTIFIER = ASTToken.create (nbsLanguage, IDENTIFIER_ID, null, 0);
                ASTToken IDENTIFIER_I = ASTToken.create (nbsLanguage, IDENTIFIER_ID, "i", 0);
                int STRING_ID = nbsLanguage.getTokenID ("string");
                ASTToken STRING = ASTToken.create (nbsLanguage, STRING_ID, null, 0);
                WHITESPACE_ID = nbsLanguage.getTokenID ("whitespace");
                COMMENT_ID = nbsLanguage.getTokenID ("comment");

                nbsLanguage.addRule (rule ("S", new Object[] {"token", "S"}));
                nbsLanguage.addRule (rule ("S", new Object[] {"tokenState", "S"}));
                nbsLanguage.addRule (rule ("S", new Object[] {"grammarRule", "S"}));
                nbsLanguage.addRule (rule ("S", new Object[] {"command", "S"}));
                nbsLanguage.addRule (rule ("S", new Object[] {}));

                nbsLanguage.addRule (rule ("tokenState", new Object[] {"state", "tokenState1"}));
                nbsLanguage.addRule (rule ("tokenState1", new Object[] {COLON, "token"}));
                nbsLanguage.addRule (rule ("tokenState1", new Object[] {BRACE, "tokenGroup"}));
                nbsLanguage.addRule (rule ("token", new Object[] {KEYWORD_TOKEN, COLON, IDENTIFIER, COLON, "token2"}));
                nbsLanguage.addRule (rule ("token2", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2, "token3"}));
                nbsLanguage.addRule (rule ("token2", new Object[] {BRACE, "properties", BRACE2}));
                nbsLanguage.addRule (rule ("token3", new Object[] {COLON, "state"}));
                nbsLanguage.addRule (rule ("token3", new Object[] {}));
                nbsLanguage.addRule (rule ("state", new Object[] {LT, IDENTIFIER, GT}));
                nbsLanguage.addRule (rule ("tokenGroup", new Object[] {"tokensInGroup", BRACE2}));
                nbsLanguage.addRule (rule ("tokensInGroup", new Object[] {"token", "tokensInGroup"}));
                nbsLanguage.addRule (rule ("tokensInGroup", new Object[] {}));

                nbsLanguage.addRule (rule ("regularExpression", new Object[] {"reChoice", "regularExpression1"}));
                nbsLanguage.addRule (rule ("regularExpression1", new Object[] {OR, "reChoice", "regularExpression1"}));
                nbsLanguage.addRule (rule ("regularExpression1", new Object[] {}));
                nbsLanguage.addRule (rule ("reChoice", new Object[] {"rePart", "reChoice1"}));
                nbsLanguage.addRule (rule ("reChoice1", new Object[] {"rePart", "reChoice1"}));
                nbsLanguage.addRule (rule ("reChoice1", new Object[] {}));
                nbsLanguage.addRule (rule ("rePart", new Object[] {STRING, "rePartOperatorOrMinus"}));
                nbsLanguage.addRule (rule ("rePart", new Object[] {STRING, IDENTIFIER_I, "rePartOperatorOrMinus"}));
                nbsLanguage.addRule (rule ("rePart", new Object[] {DOT, "rePartOperator"}));
                nbsLanguage.addRule (rule ("rePart", new Object[] {"reClass", "rePartOperator"}));
                nbsLanguage.addRule (rule ("rePart", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2, "rePartOperator"}));
                nbsLanguage.addRule (rule ("rePartOperator", new Object[] {}));
                nbsLanguage.addRule (rule ("rePartOperator", new Object[] {PLUS}));
                nbsLanguage.addRule (rule ("rePartOperator", new Object[] {QUESTION}));
                nbsLanguage.addRule (rule ("rePartOperator", new Object[] {MULTIPLY}));
                nbsLanguage.addRule (rule ("rePartOperatorOrMinus", new Object[] {MINUS, STRING}));
                nbsLanguage.addRule (rule ("rePartOperatorOrMinus", new Object[] {"rePartOperator"}));
                nbsLanguage.addRule (rule ("reClass", new Object[] {BRACKET, "reInClassNegation", "reInClass", BRACKET2}));
                nbsLanguage.addRule (rule ("reInClassNegation", new Object[] {UPP}));
                nbsLanguage.addRule (rule ("reInClassNegation", new Object[] {}));
                nbsLanguage.addRule (rule ("reInClass", new Object[] {STRING, "reInClassMinus", "reInClass1"}));
                nbsLanguage.addRule (rule ("reInClass1", new Object[] {STRING, "reInClassMinus", "reInClass1"}));
                nbsLanguage.addRule (rule ("reInClass1", new Object[] {}));
                nbsLanguage.addRule (rule ("reInClassMinus", new Object[] {MINUS, STRING}));
                nbsLanguage.addRule (rule ("reInClassMinus", new Object[] {}));

                nbsLanguage.addRule (rule ("grammarRule", new Object[] {IDENTIFIER, EQUAL, "grRightSide", SEMICOLON}));
                nbsLanguage.addRule (rule ("grRightSide", new Object[] {"grChoice", "grRightSide1"}));
                nbsLanguage.addRule (rule ("grRightSide1", new Object[] {OR, "grChoice", "grRightSide1"}));
                nbsLanguage.addRule (rule ("grRightSide1", new Object[] {}));
                nbsLanguage.addRule (rule ("grChoice", new Object[] {"grPart", "grChoice"}));
                nbsLanguage.addRule (rule ("grChoice", new Object[] {}));
                nbsLanguage.addRule (rule ("grPart", new Object[] {IDENTIFIER, "grOperator"}));
                nbsLanguage.addRule (rule ("grPart", new Object[] {"tokenDef", "grOperator"}));
                nbsLanguage.addRule (rule ("grPart", new Object[] {STRING, "grOperator"}));
                nbsLanguage.addRule (rule ("grPart", new Object[] {BRACKET, "grRightSide", BRACKET2}));
                nbsLanguage.addRule (rule ("grPart", new Object[] {PARENTHESIS, "grRightSide", PARENTHESIS2, "grOperator"}));
                nbsLanguage.addRule (rule ("grOperator", new Object[] {PLUS}));
                nbsLanguage.addRule (rule ("grOperator", new Object[] {MULTIPLY}));
                nbsLanguage.addRule (rule ("grOperator", new Object[] {QUESTION}));
                nbsLanguage.addRule (rule ("grOperator", new Object[] {}));
                nbsLanguage.addRule (rule ("tokenDef", new Object[] {LT, IDENTIFIER, "tokenDef1", GT}));
                nbsLanguage.addRule (rule ("tokenDef1", new Object[] {COMMA, STRING}));
                nbsLanguage.addRule (rule ("tokenDef1", new Object[] {}));

                nbsLanguage.addRule (rule ("command", new Object[] {KEYWORD, "command0"}));
                nbsLanguage.addRule (rule ("command0", new Object[] {COLON, "selector", "command1"}));
                nbsLanguage.addRule (rule ("command0", new Object[] {"value"}));
                nbsLanguage.addRule (rule ("command1", new Object[] {COLON, "value"}));
                nbsLanguage.addRule (rule ("command1", new Object[] {}));
                nbsLanguage.addRule (rule ("value", new Object[] {"class"}));
                nbsLanguage.addRule (rule ("value", new Object[] {STRING}));
                nbsLanguage.addRule (rule ("value", new Object[] {BRACE, "properties", BRACE2}));
                nbsLanguage.addRule (rule ("value", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2}));
                nbsLanguage.addRule (rule ("properties", new Object[] {"property", "properties"}));
                nbsLanguage.addRule (rule ("properties", new Object[] {}));
                nbsLanguage.addRule (rule ("property", new Object[] {IDENTIFIER, COLON, "propertyValue", SEMICOLON}));
                nbsLanguage.addRule (rule ("propertyValue", new Object[] {STRING}));
                nbsLanguage.addRule (rule ("propertyValue", new Object[] {"class"}));
                nbsLanguage.addRule (rule ("propertyValue", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2}));
                nbsLanguage.addRule (rule ("selector", new Object[] {"class", "selector1"}));
                nbsLanguage.addRule (rule ("selector1", new Object[] {COMMA, "class", "selector1"}));
                nbsLanguage.addRule (rule ("selector1", new Object[] {}));
                nbsLanguage.addRule (rule ("class", new Object[] {IDENTIFIER, "class1"}));
                nbsLanguage.addRule (rule ("class1", new Object[] {DOT, IDENTIFIER, "class1"}));
                nbsLanguage.addRule (rule ("class1", new Object[] {}));

                nbsLanguage.addFeature (Feature.create ("SKIP", Selector.create ("whitespace")));
                nbsLanguage.addFeature (Feature.create ("SKIP", Selector.create ("comment")));
            } catch (ParseException ex) {
                Utils.message (ex.getMessage ());
            }
        }
        return nbsLanguage;
    }

    
    private static LLSyntaxAnalyser.Rule rule (String nt, Object[] right) {
        return LLSyntaxAnalyser.Rule.create (
            nt, 
            Arrays.asList (right)
        );
    }
}
