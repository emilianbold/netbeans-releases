/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
    static final String NBS = "text/x-nbs";
    static final ASTToken STRING = ASTToken.create (NBS, "string", null, 0);
    static final ASTToken OPERATOR = ASTToken.create (NBS, "operator", null, 0);
    static final ASTToken KEYWORD = ASTToken.create (NBS, "keyword", null, 0);
    static final ASTToken IDENTIFIER = ASTToken.create (NBS, "identifier", null, 0);
    static final ASTToken COMMENT = ASTToken.create (NBS, "comment", null, 0);
    static final ASTToken WHITESPACE = ASTToken.create (NBS, "whitespace", null, 0);
    static final ASTToken BRACE = ASTToken.create (NBS, "operator", "{", 0);
    static final ASTToken BRACE2 = ASTToken.create (NBS, "operator", "}", 0);
    static final ASTToken BRACKET = ASTToken.create (NBS, "operator", "[", 0);
    static final ASTToken BRACKET2 = ASTToken.create (NBS, "operator", "]", 0);
    static final ASTToken KEYWORD_TOKEN = ASTToken.create (NBS, "keyword", "TOKEN", 0);
    static final ASTToken COLON = ASTToken.create (NBS, "operator", ":", 0);
    static final ASTToken SEMICOLON = ASTToken.create (NBS, "operator", ";", 0);
    static final ASTToken EQUAL = ASTToken.create (NBS, "operator", "=", 0);
    static final ASTToken MINUS = ASTToken.create (NBS, "operator", "-", 0);
    static final ASTToken DOLLAR = ASTToken.create (NBS, "operator", "$", 0);
    static final ASTToken UPP = ASTToken.create (NBS, "operator", "^", 0);
    static final ASTToken OR = ASTToken.create (NBS, "operator", "|", 0);
    static final ASTToken COMMA = ASTToken.create (NBS, "operator", ",", 0);
    static final ASTToken DOT = ASTToken.create (NBS, "operator", ".", 0);
    static final ASTToken PLUS = ASTToken.create (NBS, "operator", "+", 0);
    static final ASTToken MULTIPLY = ASTToken.create (NBS, "operator", "*", 0);
    static final ASTToken QUESTION = ASTToken.create (NBS, "operator", "?", 0);
    static final ASTToken DOUBLE_QUOTES = ASTToken.create (NBS, "operator", "\"", 0);
    static final ASTToken LT = ASTToken.create (NBS, "operator", "<", 0);
    static final ASTToken GT = ASTToken.create (NBS, "operator", ">", 0);
    static final ASTToken PARENTHESIS = ASTToken.create (NBS, "operator", "(", 0);
    static final ASTToken PARENTHESIS2 = ASTToken.create (NBS, "operator", ")", 0);
    static final ASTToken IDENTIFIER_I = ASTToken.create (NBS, "identifier", "i", 0);

    
    static Language getNBSLanguage () throws ParseException {
        Language l = new Language (NBS);
        l.addToken (
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
        l.addToken (
            Parser.DEFAULT_STATE, 
            "identifier",
            Pattern.create (
                "['a'-'z' 'A'-'Z'] ['a'-'z' 'A'-'Z' '0'-'9' '_']*"
            ),
            Parser.DEFAULT_STATE,
            null
        );
        l.addToken (
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
        l.addToken (
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
        l.addToken (
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
        l.addToken (
            Parser.DEFAULT_STATE, 
            "comment",
            Pattern.create ("'#' [^'\\n' '\\r']* ['\\n' '\\r']+"),
            Parser.DEFAULT_STATE,
            null
        );
        l.addToken (
            Parser.DEFAULT_STATE, 
            "comment",
            Pattern.create ("'/#' - '#/'"),
            Parser.DEFAULT_STATE,
            null
        );
        l.addToken (
            Parser.DEFAULT_STATE, 
            "whitespace",
            Pattern.create ("['\\n' '\\r' ' ' '\\t']+"),
            Parser.DEFAULT_STATE,
            null
        );
        l.addRule (rule ("S", new Object[] {"token", "S"}));
        l.addRule (rule ("S", new Object[] {"tokenState", "S"}));
        l.addRule (rule ("S", new Object[] {"grammarRule", "S"}));
        l.addRule (rule ("S", new Object[] {"command", "S"}));
        l.addRule (rule ("S", new Object[] {}));
        
        l.addRule (rule ("tokenState", new Object[] {"state", "tokenState1"}));
        l.addRule (rule ("tokenState1", new Object[] {COLON, "token"}));
        l.addRule (rule ("tokenState1", new Object[] {BRACE, "tokenGroup"}));
        l.addRule (rule ("token", new Object[] {KEYWORD_TOKEN, COLON, IDENTIFIER, COLON, "token2"}));
        l.addRule (rule ("token2", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2, "token3"}));
        l.addRule (rule ("token2", new Object[] {BRACE, "properties", BRACE2}));
        l.addRule (rule ("token3", new Object[] {COLON, "state"}));
        l.addRule (rule ("token3", new Object[] {}));
        l.addRule (rule ("state", new Object[] {LT, IDENTIFIER, GT}));
        l.addRule (rule ("tokenGroup", new Object[] {"tokensInGroup", BRACE2}));
        l.addRule (rule ("tokensInGroup", new Object[] {"token", "tokensInGroup"}));
        l.addRule (rule ("tokensInGroup", new Object[] {}));
        
        l.addRule (rule ("regularExpression", new Object[] {"reChoice", "regularExpression1"}));
        l.addRule (rule ("regularExpression1", new Object[] {OR, "reChoice", "regularExpression1"}));
        l.addRule (rule ("regularExpression1", new Object[] {}));
        l.addRule (rule ("reChoice", new Object[] {"rePart", "reChoice1"}));
        l.addRule (rule ("reChoice1", new Object[] {"rePart", "reChoice1"}));
        l.addRule (rule ("reChoice1", new Object[] {}));
        l.addRule (rule ("rePart", new Object[] {STRING, "rePartOperatorOrMinus"}));
        l.addRule (rule ("rePart", new Object[] {STRING, IDENTIFIER_I, "rePartOperatorOrMinus"}));
        l.addRule (rule ("rePart", new Object[] {DOT, "rePartOperator"}));
        l.addRule (rule ("rePart", new Object[] {"reClass", "rePartOperator"}));
        l.addRule (rule ("rePart", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2, "rePartOperator"}));
        l.addRule (rule ("rePartOperator", new Object[] {}));
        l.addRule (rule ("rePartOperator", new Object[] {PLUS}));
        l.addRule (rule ("rePartOperator", new Object[] {QUESTION}));
        l.addRule (rule ("rePartOperator", new Object[] {MULTIPLY}));
        l.addRule (rule ("rePartOperatorOrMinus", new Object[] {MINUS, STRING}));
        l.addRule (rule ("rePartOperatorOrMinus", new Object[] {"rePartOperator"}));
        l.addRule (rule ("reClass", new Object[] {BRACKET, "reInClassNegation", "reInClass", BRACKET2}));
        l.addRule (rule ("reInClassNegation", new Object[] {UPP}));
        l.addRule (rule ("reInClassNegation", new Object[] {}));
        l.addRule (rule ("reInClass", new Object[] {STRING, "reInClassMinus", "reInClass1"}));
        l.addRule (rule ("reInClass1", new Object[] {STRING, "reInClassMinus", "reInClass1"}));
        l.addRule (rule ("reInClass1", new Object[] {}));
        l.addRule (rule ("reInClassMinus", new Object[] {MINUS, STRING}));
        l.addRule (rule ("reInClassMinus", new Object[] {}));
        
        l.addRule (rule ("grammarRule", new Object[] {IDENTIFIER, EQUAL, "grRightSide", SEMICOLON}));
        l.addRule (rule ("grRightSide", new Object[] {"grChoice", "grRightSide1"}));
        l.addRule (rule ("grRightSide1", new Object[] {OR, "grChoice", "grRightSide1"}));
        l.addRule (rule ("grRightSide1", new Object[] {}));
        l.addRule (rule ("grChoice", new Object[] {"grPart", "grChoice"}));
        l.addRule (rule ("grChoice", new Object[] {}));
        l.addRule (rule ("grPart", new Object[] {IDENTIFIER, "grOperator"}));
        l.addRule (rule ("grPart", new Object[] {"tokenDef", "grOperator"}));
        l.addRule (rule ("grPart", new Object[] {STRING, "grOperator"}));
        l.addRule (rule ("grPart", new Object[] {BRACKET, "grRightSide", BRACKET2}));
        l.addRule (rule ("grPart", new Object[] {PARENTHESIS, "grRightSide", PARENTHESIS2, "grOperator"}));
        l.addRule (rule ("grOperator", new Object[] {PLUS}));
        l.addRule (rule ("grOperator", new Object[] {MULTIPLY}));
        l.addRule (rule ("grOperator", new Object[] {QUESTION}));
        l.addRule (rule ("grOperator", new Object[] {}));
        l.addRule (rule ("tokenDef", new Object[] {LT, IDENTIFIER, "tokenDef1", GT}));
        l.addRule (rule ("tokenDef1", new Object[] {COMMA, STRING}));
        l.addRule (rule ("tokenDef1", new Object[] {}));
        
        l.addRule (rule ("command", new Object[] {KEYWORD, "command0"}));
        l.addRule (rule ("command0", new Object[] {COLON, "selector", "command1"}));
        l.addRule (rule ("command0", new Object[] {"value"}));
        l.addRule (rule ("command1", new Object[] {COLON, "value"}));
        l.addRule (rule ("command1", new Object[] {}));
        l.addRule (rule ("value", new Object[] {"class"}));
        l.addRule (rule ("value", new Object[] {STRING}));
        l.addRule (rule ("value", new Object[] {BRACE, "properties", BRACE2}));
        l.addRule (rule ("value", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2}));
        l.addRule (rule ("properties", new Object[] {"property", "properties"}));
        l.addRule (rule ("properties", new Object[] {}));
        l.addRule (rule ("property", new Object[] {IDENTIFIER, COLON, "propertyValue", SEMICOLON}));
        l.addRule (rule ("propertyValue", new Object[] {STRING}));
        l.addRule (rule ("propertyValue", new Object[] {"class"}));
        l.addRule (rule ("propertyValue", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2}));
        l.addRule (rule ("selector", new Object[] {"class", "selector1"}));
        l.addRule (rule ("selector1", new Object[] {COMMA, "class", "selector1"}));
        l.addRule (rule ("selector1", new Object[] {}));
        l.addRule (rule ("class", new Object[] {IDENTIFIER, "class1"}));
        l.addRule (rule ("class1", new Object[] {DOT, IDENTIFIER, "class1"}));
        l.addRule (rule ("class1", new Object[] {}));

        l.addFeature (Feature.create ("SKIP", Selector.create ("whitespace")));
        l.addFeature (Feature.create ("SKIP", Selector.create ("comment")));
        return l;
    }

    
    private static LLSyntaxAnalyser.Rule rule (String nt, Object[] right) {
        return LLSyntaxAnalyser.Rule.create (
            nt, 
            Arrays.asList (right)
        );
    }
}
