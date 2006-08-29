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

/* Preprocessor Scanner 
 *
 * !!! do not modify PreprocessorScanner.java !!! primary source is PreprocessorScanner.jflex !!!
 *
 * @author Adam Sotona
 *
 */

package org.netbeans.mobility.antext.preprocessor;

import java.io.*;

%%

%class PreprocessorScanner
%implements LineParserTokens

%unicode
%pack
%column
%line
%char
%final
%public

%integer
%eofval{
    return token(END_OF_FILE);
%eofval}
%eofclose

%{

    private StringBuffer padding = new StringBuffer();
    private PPToken lastToken;

    public boolean hasMoreTokens() {
    	return lastToken == null || lastToken.getType() != END_OF_FILE;
    }

    public PPToken nextToken() throws IOException {
    	yylex();
    	return lastToken;
    }	

    public PPToken getLastToken() {
    	return lastToken;
    }	

    private int token(int type) {
    	lastToken = new PPToken(type, yyline + 1, yycolumn, padding.toString(), yytext());
        padding = new StringBuffer();
        return type;
    }
    
    public static void main(String argv[]) throws IOException {
        if (argv.length == 0) {
            System.out.println("Usage : java PreprocessorScanner <inputfile>");
        } else {
            PreprocessorScanner scanner = new PreprocessorScanner( new FileReader(argv[0]));
            while (scanner.hasMoreTokens()) System.out.print(scanner.nextToken());
        }
    }
%}

InputCharacter = [^\n\r]
LineTerminator = \r|\n|\r\n
WhiteSpace = [ \t\f]
NonWhiteSpace = [^ \t\f\r\n]
StringText = (\\\"|[^\r\n\"])*

%state COMMAND, OLDCOMMAND, OTHERTEXT

%%

<YYINITIAL> {
  "//#if"       		     { yybegin(COMMAND); return token(COMMAND_IF); }
  "//#condition"       		     { yybegin(COMMAND); return token(COMMAND_CONDITION); }
  "//#ifdef"                         { yybegin(COMMAND); return token(COMMAND_IFDEF); }
  "//#ifndef"                        { yybegin(COMMAND); return token(COMMAND_IFNDEF); }
  "//#elif"                          { yybegin(COMMAND); return token(COMMAND_ELIF); }
  "//#elifdef"                       { yybegin(COMMAND); return token(COMMAND_ELIFDEF); }
  "//#elifndef"                      { yybegin(COMMAND); return token(COMMAND_ELIFNDEF); }
  "//#else"                          { yybegin(COMMAND); return token(COMMAND_ELSE); }
  "//#endif"                         { yybegin(COMMAND); return token(COMMAND_ENDIF); }
  "//#debug"                         { yybegin(COMMAND); return token(COMMAND_DEBUG); }
  "//#mdebug"                        { yybegin(COMMAND); return token(COMMAND_MDEBUG); }
  "//#enddebug"                      { yybegin(COMMAND); return token(COMMAND_ENDDEBUG); }
  "//#define"                        { yybegin(COMMAND); return token(COMMAND_DEFINE); }
  "//#undefine"                      { yybegin(COMMAND); return token(COMMAND_UNDEFINE); }
  "/*#"                              { yybegin(OLDCOMMAND); return token(OLD_STX_HEADER_START); }
  "/*$"                              { yybegin(OLDCOMMAND); return token(OLD_STX_FOOTER_START); }
  "//#"{NonWhiteSpace}+              { yybegin(OTHERTEXT); return token(COMMAND_UNKNOWN); }
  "//-----"                          { yybegin(OTHERTEXT); padding.append(yytext()); }
  "//#"{WhiteSpace}? | "//--"        { yybegin(OTHERTEXT); return token(PREPROCESSOR_COMMENT); }
  {WhiteSpace}+                      { padding.append(yytext()); }
  {LineTerminator}                   { return token(END_OF_LINE); }
  {InputCharacter}                   { yybegin(OTHERTEXT); padding.append(yytext()); }
}

<COMMAND> {
  "("                                     { return token(LEFT_BRACKET); }       
  ")"                                     { return token(RIGHT_BRACKET); }       
  [&][&]?                                 { return token(OP_AND); }      
  [|][|]?                                 { return token(OP_OR); }      
  "^"                                     { return token(OP_XOR); }      
  "@"                                     { return token(OP_AT); }       
  "!"                                     { return token(OP_NOT); }       
  "=="                                    { return token(OP_EQUALS); }      
  "!="                                    { return token(OP_NOT_EQUALS); }      
  "<"                                     { return token(OP_LESS); }       
  ">"                                     { return token(OP_GREATER); }       
  "<="                                    { return token(OP_LESS_OR_EQUAL); }      
  ">="                                    { return token(OP_GREATER_OR_EQUAL); }      
  ","                                     { return token(COMMA); }       
  ":defined"                              { return token(COLON_DEFINED); }
  "defined"                               { return token(DEFINED); }
  "="                                     { return token(ASSIGN); }       
  "//" | "/*"                             { yybegin(OTHERTEXT); return token(SIMPLE_COMMENT); }
  [a-zA-Z_]([a-zA-Z0-9_\.\\\/$])*         { return token(ABILITY); }
  [0-9]+                                  { return token(NUMBER); }
  \"{StringText}\"                        { return token(STRING); }
  \"{StringText}                          { return token(UNFINISHED_STRING); }
  {WhiteSpace}+                           { padding.append(yytext()); }
  {LineTerminator}                        { yybegin(YYINITIAL); return token(END_OF_LINE); }
  {InputCharacter}                        { yybegin(OTHERTEXT); padding.append(yytext()); }
}

<OLDCOMMAND> {
  "!"                                     { return token(OP_NOT); }       
  ","                                     { return token(COMMA); }       
  "#*/"                                   { yybegin(OTHERTEXT); return token(OLD_STX_HEADER_END); }
  "$*/"                                   { yybegin(OTHERTEXT); return token(OLD_STX_FOOTER_END); }
  "//" | "/*"                             { yybegin(OTHERTEXT); return token(SIMPLE_COMMENT); }
  [a-zA-Z_]([a-zA-Z0-9_\.\\\/])*          { return token(ABILITY); }
  {WhiteSpace}+                           { padding.append(yytext()); }
  {LineTerminator}                        { yybegin(YYINITIAL); return token(END_OF_LINE); }
  {InputCharacter}                        { yybegin(OTHERTEXT); padding.append(yytext()); }
}

<OTHERTEXT> {
  {InputCharacter}*                  { return token(OTHER_TEXT); }
  {LineTerminator}                   { yybegin(YYINITIAL); return token(END_OF_LINE); }
}