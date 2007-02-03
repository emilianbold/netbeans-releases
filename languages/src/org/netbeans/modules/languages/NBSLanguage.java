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
import org.netbeans.api.languages.SToken;
import java.util.Arrays;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.api.languages.SToken;

    
    
/**
 *
 * @author Jan Jancura
 */
public class NBSLanguage {
    
    static final String NBS = "text/x-nbs";
    static final SToken STRING = SToken.create ("string", null);
    static final SToken OPERATOR = SToken.create ("operator", null);
    static final SToken KEYWORD = SToken.create ("keyword", null);
    static final SToken IDENTIFIER = SToken.create ("identifier", null);
    static final SToken COMMENT = SToken.create ("comment", null);
    static final SToken WHITESPACE = SToken.create ("whitespace", null);
    static final SToken BRACE = SToken.create ("operator", "{");
    static final SToken BRACE2 = SToken.create ("operator", "}");
    static final SToken BRACKET = SToken.create ("operator", "[");
    static final SToken BRACKET2 = SToken.create ("operator", "]");
    static final SToken KEYWORD_TOKEN = SToken.create ("keyword", "TOKEN");
    static final SToken COLON = SToken.create ("operator", ":");
    static final SToken SEMICOLON = SToken.create ("operator", ";");
    static final SToken EQUAL = SToken.create ("operator", "=");
    static final SToken MINUS = SToken.create ("operator", "-");
    static final SToken DOLLAR = SToken.create ("operator", "$");
    static final SToken UPP = SToken.create ("operator", "^");
    static final SToken OR = SToken.create ("operator", "|");
    static final SToken COMMA = SToken.create ("operator", ",");
    static final SToken DOT = SToken.create ("operator", ".");
    static final SToken PLUS = SToken.create ("operator", "+");
    static final SToken MULTIPLY = SToken.create ("operator", "*");
    static final SToken QUESTION = SToken.create ("operator", "?");
    static final SToken DOUBLE_QUOTES = SToken.create ("operator", "\"");
    static final SToken LT = SToken.create ("operator", "<");
    static final SToken GT = SToken.create ("operator", ">");
    static final SToken PARENTHESIS = SToken.create ("operator", "(");
    static final SToken PARENTHESIS2 = SToken.create ("operator", ")");
    
    private static Parser nbsParser;
    
    static Language getNBSLanguage () throws ParseException {
        Language l = new Language (NBS);
        l.addToken (
            Parser.DEFAULT_STATE, 
            "keyword",
            Pattern.create (
                "'ACTION' |" +
                "'AST' |" +
                "'BRACE' |" +
                "'COLOR' |" +
                "'COMPLETE' |" +
                "'COMPLETION' |" +
                "'FOLD' |" +
                "'HYPERLINK' |" +
                "'IMPORT' |" +
                "'INDENT' |" +
                "'MARK' | " +
                "'NAVIGATOR' |" +
                "'PARSE' |" +
                "'PROPERTIES' |" +
                "'REFORMAT' |" +
                "'SKIP' |" +
                "'STORE' |" +
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
                "'\\\"' ([^'\\\"' '\\\\' '\\r' '\\n'] |" +
                "('\\\\' ['r' 'n' 't' '\\\\' '\\\'' '\\\"']) )* '\\\"'"
            ),
            Parser.DEFAULT_STATE,
            null
        );
        l.addToken (
            Parser.DEFAULT_STATE, 
            "string",
            Pattern.create (
                "'\\\'' ([^'\\\'' '\\\\' '\\r' '\\n'] |" +
                "('\\\\' ['r' 'n' 't' '\\\\' '\\\'' '\\\"']) )* '\\\''"
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
        l.addRule (rule ("token", new Object[] {KEYWORD_TOKEN, COLON, IDENTIFIER, "token2"}));
        l.addRule (rule ("token2", new Object[] {COLON, PARENTHESIS, "regularExpression", PARENTHESIS2, "token3"}));
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
        l.addRule (rule ("command0", new Object[] {BRACE, "properties", BRACE2}));
        l.addRule (rule ("command0", new Object[] {COLON, "class", "command1"}));
        l.addRule (rule ("command0", new Object[] {"command2"}));
        l.addRule (rule ("command1", new Object[] {BRACE, "properties", BRACE2}));
        l.addRule (rule ("command1", new Object[] {COLON, "command2"}));
        l.addRule (rule ("command1", new Object[] {}));
        l.addRule (rule ("command2", new Object[] {"class"}));
        l.addRule (rule ("command2", new Object[] {STRING}));
        l.addRule (rule ("properties", new Object[] {"property", "properties"}));
        l.addRule (rule ("properties", new Object[] {}));
        l.addRule (rule ("property", new Object[] {IDENTIFIER, COLON, "propertyValue", SEMICOLON}));
        l.addRule (rule ("propertyValue", new Object[] {STRING}));
        l.addRule (rule ("propertyValue", new Object[] {"class"}));
        l.addRule (rule ("propertyValue", new Object[] {PARENTHESIS, "regularExpression", PARENTHESIS2}));
        
        l.addRule (rule ("class", new Object[] {IDENTIFIER, "class1"}));
        l.addRule (rule ("class1", new Object[] {DOT, IDENTIFIER, "class1"}));
        l.addRule (rule ("class1", new Object[] {}));

        l.addSkipTokenType ("whitespace");
        l.addSkipTokenType ("comment");
        return l;
    }

    
    private static LLSyntaxAnalyser.Rule rule (String nt, Object[] right) {
        return LLSyntaxAnalyser.Rule.create (
            nt, 
            Arrays.asList (right)
        );
    }
}
