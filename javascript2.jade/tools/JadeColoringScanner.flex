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

package org.netbeans.modules.javascript2.jade.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

%%

%public
%final
%class JadeColoringLexer
%type JadeTokenId
%unicode
%caseless
%char

%{
    private LexerInput input;
    private boolean canFollowTag = false;
    int parenBalance = 1;
    int braceBalance = 0;
    int bracketBalance = 0;
    int indent = 0;
    int eolPosition = 0;
    boolean dotAfterTag = false;
    int blockIndent = -1;
    boolean hasCssId = false;
    int lastReaded = 0;
    boolean continueJS = false;
    boolean inString = false;
    

    public JadeColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        if(info.state() != null) {
            //reset state
            setState((LexerState)info.state());
        } else {
            //initial state
            zzState = zzLexicalState = YYINITIAL;
        }
    }


    public LexerState getState() {
        if (zzState == YYINITIAL && zzLexicalState == YYINITIAL) {
            return null;
        }
        return new LexerState(zzState, zzLexicalState, canFollowTag, indent, hasCssId);
    }

    public void setState(LexerState state) {
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
        this.canFollowTag = state.canFollowTag;
        this.indent = state.indent;
        this.hasCssId = state.hasCssId;
    }

    public JadeTokenId nextToken() throws java.io.IOException {
        JadeTokenId token = yylex();
        return token;
    }

    public static final class LexerState  {
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        final boolean canFollowTag;
        /** indent of the new line */
        final int indent;
        final boolean hasCssId;

        LexerState (int zzState, int zzLexicalState, boolean canFollowTag, int indent, boolean hasCssId) {
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.canFollowTag = canFollowTag;
            this.indent = indent;
            this.hasCssId = hasCssId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LexerState other = (LexerState) obj;
            if (this.zzState != other.zzState) {
                return false;
            }
            if (this.zzLexicalState != other.zzLexicalState) {
                return false;
            }
            if (this.canFollowTag != other.canFollowTag) {
                return false;
            }
            if (this.hasCssId != other.hasCssId) {
                return false;
            }
            if (this.indent != other.indent) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + this.zzState;
            hash = 31 * hash + this.zzLexicalState;
            hash = 31 * hash + (this.canFollowTag ? 0 : 1);
            hash = 31 * hash + (this.hasCssId ? 0 : 1);
            hash = 31 * hash + this.indent;
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{" + "zzState=" + zzState + ", zzLexicalState=" + zzLexicalState + '}';
        }
    }

 // End user code
    boolean checkEndJS(int tokenLength, char ch) {
        if (!continueJS 
                && ((ch == ')' && parenBalance == 0) 
                || (ch != ')' && parenBalance == 1))
                && braceBalance == 0 && bracketBalance == 0) {
            if (lastReaded > 0 && ((tokenLength - lastReaded) > 0)) {
                yypushback(tokenLength - lastReaded);
                yybegin(HTML_ATTRIBUTE);
                return true;
            }
        } 
        lastReaded = tokenLength;
        continueJS = false;
        
        return false;
    }
%}

/* states */
%state AFTER_EOL
%state DOCTYPE
%state AFTER_DOCTYPE
%state DOCTYPE_STRING
%state DOCTYPE_STRING_END
%state AFTER_TAG
%state AFTER_CODE_DELIMITER
%state AFTER_CODE_DELIMITER_WITH_BLOCK_EXPANSION
%state IN_COMMENT
%state IN_COMMENT_AFTER_EOL
%state IN_UNBUFFERED_COMMENT
%state IN_UNBUFFERED_COMMENT_AFTER_EOL
%state TEXT_LINE
%state IN_PLAIN_TEXT_LINE
%state IN_PLAIN_TEXT_BLOCK
%state IN_PLAIN_TEXT_BLOCK_AFTER_EOL
%state AFTER_PLAIN_TEXT_BLOCK_DELIMITER
%state HTML_ATTRIBUTE
%state HTML_ATTRIBUTE_VALUE
%state JAVASCRIPT_VALUE
%state JAVASCRIPT
%state JAVASCRIPT_LINE
%state JAVASCRIPT_EXPRESSION
%state JAVASCRIPT_WITH_BLOCK_EXPANSION
%state JS_SSTRING
%state JS_STRING
%state FILEPATH
%state IN_FILTER_BLOCK
%state IN_FILTER_BLOCK_AFTER_EOL
%state AFTER_INCLUDE
%state AFTER_COLON_IN_TAG

/* base structural elements */
AnyChar = (.|[\n])
HtmlString = [<] [^"\r"|"\n"|"\r\n"|">"|"*"]* [>]?
HtmlIdentifierPart = [-[:letter:][:digit:]]+
HtmlIdentifier = {HtmlIdentifierPart}(:{HtmlIdentifierPart})*
CssIdentifier = [@\-[:letter:][:digit:]]+
LineTerminator = \r|\n|\r\n
StringCharacter  = [^\r\n\"\\] | \\{LineTerminator}
WS = [ \t\f\u00A0\u000B]
WhiteSpace = [ \t\f\u00A0\u000B]+
Input = [^\r\n \t\f\u00A0\u000B]+

Comment = "//"
UnbufferedComment = "//-"

%%

<YYINITIAL> {
    {AnyChar}   {
            yypushback(1);
            indent = 0;
            yybegin(AFTER_EOL);
    }

}

<AFTER_EOL> {
    /* doctype */
    "doctype"                       {   yybegin(AFTER_DOCTYPE);
                                        return JadeTokenId.DOCTYPE; }

    "if"                            {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_IF;}
    "else"                          {   return JadeTokenId.KEYWORD_ELSE;}
    "unless"                        {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_UNLESS;}
                                    
    "case"                          {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.KEYWORD_CASE;}
    "when"                          {   yybegin(AFTER_CODE_DELIMITER_WITH_BLOCK_EXPANSION);
                                        return JadeTokenId.KEYWORD_WHEN;}
    "default"                       {   yybegin(AFTER_TAG); // handling : after the keyword
                                        return JadeTokenId.KEYWORD_DEFAULT;}

    "block"                         {   yybegin(AFTER_TAG);
                                        return JadeTokenId.KEYWORD_BLOCK;}
    "extends"                       {   yybegin(FILEPATH);
                                        return JadeTokenId.KEYWORD_EXTENDS;}
    "include"                       {   yybegin(AFTER_INCLUDE);
                                        return JadeTokenId.KEYWORD_INCLUDE;}

    "-"|"="|"!="                    {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.CODE_DELIMITER; }
    {WhiteSpace}                    {   indent = tokenLength;
                                        return JadeTokenId.WHITESPACE; }
    {HtmlIdentifier}                {   yybegin(AFTER_TAG);
                                        dotAfterTag = true;
                                        hasCssId = false;
                                        return JadeTokenId.TAG ;}
    {LineTerminator}                {   indent = 0; System.out.println("Indent reset");
                                        return JadeTokenId.EOL; }
    
    {UnbufferedComment}             {   yybegin(IN_UNBUFFERED_COMMENT);
                                        return JadeTokenId.UNBUFFERED_COMMENT_DELIMITER; }
    
    {Comment}                       {   yybegin(IN_COMMENT); 
                                        return JadeTokenId.COMMENT_DELIMITER; }
    
    [#\.]                            {   hasCssId = false;
                                        yypushback(1);
                                        yybegin(AFTER_TAG); }
        
    "|"                             {   yybegin(IN_PLAIN_TEXT_LINE);
                                        return JadeTokenId.PLAIN_TEXT_DELIMITER; }
    ":"{Input}                      {   yybegin (IN_FILTER_BLOCK);
                                        blockIndent = -1;
                                        return JadeTokenId.FILTER; }
    "<"                             {   yybegin(IN_PLAIN_TEXT_LINE); }
    .                               {   return JadeTokenId.UNKNOWN;}
    
}

/* TODO - this rure shold be rewrite. I don't like it. Mainly because the dot after tag handling*/
<AFTER_TAG> {
    
    "#"{CssIdentifier}                {   if (!hasCssId) {
                                            hasCssId = true;
                                            return JadeTokenId.CSS_ID;
                                        } else {
                                            // only one css id is allowed in tag
                                            return JadeTokenId.UNKNOWN;
                                        }
                                    }
    "\."{CssIdentifier}                {   return JadeTokenId.CSS_CLASS; }
    "("                             {   yybegin(HTML_ATTRIBUTE);
                                        return JadeTokenId.BRACKET_RIGHT_PAREN;
                                    }
    ":"                             {   yybegin(AFTER_COLON_IN_TAG);
                                        return JadeTokenId.OPERATOR_COLON;
                                    }
    {WhiteSpace}                    {   yybegin(TEXT_LINE);
                                        return JadeTokenId.WHITESPACE;
                                    }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        if (tokenLength > 0) {
                                            return JadeTokenId.EOL;
                                        }
                                    }
    "="|"!="                        {   yybegin(AFTER_CODE_DELIMITER);
                                        return JadeTokenId.CODE_DELIMITER; }
    "/"                             {   return JadeTokenId.OPERATOR_DIVISION;}
    "\."                            {   
                                        yybegin(AFTER_PLAIN_TEXT_BLOCK_DELIMITER);
                                        return JadeTokenId.PLAIN_TEXT_DELIMITER; 
                                        
                                    }
    .                               {   // we expect = != / or Css Id or Css class
                                        return JadeTokenId.UNKNOWN; }
}

<AFTER_COLON_IN_TAG>                {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE;
                                    }
    {HtmlIdentifier}                {   yybegin(AFTER_TAG);
                                        dotAfterTag = true;
                                        hasCssId = false;
                                        return JadeTokenId.TAG ;}
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        return JadeTokenId.EOL;
                                    }
    .                               {   System.out.println("Chyba v after colon in tag");
                                        return JadeTokenId.UNKNOWN; }
}

<TEXT_LINE>                         {
    
    [#!]"{"                         {   yypushback(2);
                                        yybegin(JAVASCRIPT_EXPRESSION);
                                        return JadeTokenId.TEXT;
                                    }
    {LineTerminator}                {   
                                        yypushback(1);
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        
                                        if (tokenLength -1 > 0) {
                                            return JadeTokenId.TEXT;
                                        }
                                    }
    {AnyChar}                       {  }
}

<HTML_ATTRIBUTE> {
    {HtmlIdentifier}                {   return JadeTokenId.ATTRIBUTE; }
    "="                             {   yybegin(HTML_ATTRIBUTE_VALUE);
                                        return JadeTokenId.OPERATOR_ASSIGNMENT; }
    "!="                            {   yybegin(HTML_ATTRIBUTE_VALUE);
                                        return JadeTokenId.OPERATOR_NOT_EQUALS; }
    ","                             {   return JadeTokenId.OPERATOR_COMMA; }
    {LineTerminator}                {   return JadeTokenId.EOL; }
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    ")"                             {   yybegin(AFTER_TAG);
                                        return JadeTokenId.BRACKET_RIGHT_PAREN;}
     .                              {   return JadeTokenId.UNKNOWN;}

}

<HTML_ATTRIBUTE_VALUE> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {LineTerminator}                {   return JadeTokenId.EOL; }
    {AnyChar}                       {   System.out.println("Switch into javascript value " + tokenLength);
                                        parenBalance = 1;
                                        lastReaded = bracketBalance = braceBalance = 0;
                                        yypushback(1);
                                        yybegin(JAVASCRIPT_VALUE);}
    
}



<JAVASCRIPT_VALUE> {
    \'                              {   yybegin(JS_SSTRING); }
    \"                              {   yybegin(JS_STRING); }
    [\+\-\.&\*/%|]"="?              {   continueJS = true; lastReaded = tokenLength; System.out.println("consumuju +-*/% '" + (char)zzInput + "' : " +zzInput );}
    "["                             {   braceBalance++; lastReaded = tokenLength; }
    "]"                             {   braceBalance--; lastReaded = tokenLength; }
    "{"                             {   bracketBalance++; lastReaded = tokenLength; }
    "}"                             {   bracketBalance--; lastReaded = tokenLength; }
    "("                             {   parenBalance++; lastReaded = tokenLength;}
    ")"                             {   parenBalance--; 
                                        System.out.println("zaviraci zavorka");
                                        if (checkEndJS(tokenLength, (char)zzInput)) {
                                            return JadeTokenId.JAVASCRIPT; 
                                        }
                                   }
    {WS}+                           {   System.out.println("consumuju WS");} 
    ","                             {   System.out.println("consumuju ,");                
                                        if (checkEndJS(tokenLength, (char)zzInput)) {
                                            System.out.println("vracim js po ws s carkou");
                                            return JadeTokenId.JAVASCRIPT; 
                                        }
                                    }
    {HtmlIdentifier}                {
                                        System.out.println("consumuju idetifikator");
                                        if (zzInput == ')') parenBalance--;
                                        if (checkEndJS(tokenLength, (char)zzInput)) {
                                            return JadeTokenId.JAVASCRIPT; 
                                        }
                                        if (zzInput == ')') parenBalance++;  // ned to return back 
    }
    
    {AnyChar}                         { lastReaded = tokenLength; continueJS = false; System.out.println("precten: " + (char)zzInput);}
    
}

<JS_STRING> {
    \"                              {
                                        System.out.println("consumed string \"");  
                                        continueJS = false;
                                        lastReaded = tokenLength;
                                        yybegin(JAVASCRIPT_VALUE);
                                        
                                    }

                                    
    "\\\""                          { }  
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(AFTER_EOL);
                                     if (tokenLength -1 > 0) {
                                         return JadeTokenId.UNKNOWN;
                                     }
                                 }
   {AnyChar}                    { System.out.println("String char: " + (char)zzInput); }
}

<JS_SSTRING> {
    \'                              {
                                        System.out.println("consumed string '");  
                                        continueJS = false;
                                        lastReaded = tokenLength;
                                        yybegin(JAVASCRIPT_VALUE);
                                        
                                    }


  "\\'"                          { }                                  
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(AFTER_EOL);
                                     if (tokenLength -1 > 0) {
                                         return JadeTokenId.UNKNOWN;
                                     }
                                 }
   {AnyChar}                    { System.out.println("String char: " + (char)zzInput); }
}
<AFTER_INCLUDE> {
    ":"{Input}                      {   return JadeTokenId.FILTER; }
    .                               {   yypushback(1); yybegin(FILEPATH); }
}

<JAVASCRIPT> {
    [\"'{}(),\n\r]                       {  
        switch (zzInput) {
            case '(': parenBalance++; break;
            case '{': braceBalance++; break;
            case '}': braceBalance--; break; 
            case ')':
                parenBalance--;
                break;    
            case ',':
            case '\r':
            case '\n':
                if (parenBalance == 1 && braceBalance == 0) {
                    parenBalance = 0;
                }
                break;
        }
        if (parenBalance == 0 && braceBalance == 0) {
            yypushback(1);
            yybegin(HTML_ATTRIBUTE);
            parenBalance = 1;
            return JadeTokenId.JAVASCRIPT;
        }
                                    }
    {AnyChar}                       {}
}

<JAVASCRIPT_EXPRESSION> {
    [#!]"{"                            {   braceBalance = 1; return JadeTokenId.EXPRESSION_DELIMITER_OPEN; }
    "{"                             {   braceBalance++; }
    "}"                             {   braceBalance--;
                                        if (braceBalance == 0) {
                                            yypushback(1);
                                            return JadeTokenId.JAVASCRIPT;
                                        } else if (braceBalance == -1) {
                                            yybegin(TEXT_LINE);
                                            return JadeTokenId.EXPRESSION_DELIMITER_CLOSE; 
                                        }
                                    }
    {LineTerminator}                {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength - 1 > 0) {
                                            return JadeTokenId.JAVASCRIPT;
                                        }
                                    }
    .                               { }
}
<JAVASCRIPT_WITH_BLOCK_EXPANSION>   {
    ":"                             {   yypushback(1);
                                        yybegin(AFTER_TAG);
                                    }
    [^:\r\n]+                       {   return JadeTokenId.JAVASCRIPT; }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        return JadeTokenId.EOL; }
}

<JAVASCRIPT_LINE> {
    .+                              {   return JadeTokenId.JAVASCRIPT; }
    {LineTerminator}                {   yybegin(AFTER_EOL);
                                        indent = 0;
                                        return JadeTokenId.EOL; }
}

<AFTER_CODE_DELIMITER_WITH_BLOCK_EXPANSION> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {AnyChar}                       {   yypushback(1);
                                        yybegin(JAVASCRIPT_WITH_BLOCK_EXPANSION);
                                    }
}

<AFTER_CODE_DELIMITER> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    {AnyChar}                       {   yypushback(1);
                                        yybegin(JAVASCRIPT_LINE);
                                    }
}

<IN_PLAIN_TEXT_LINE> {
    .*                              { }
    {LineTerminator}                {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        return JadeTokenId.PLAIN_TEXT;
                                    }
}

<AFTER_PLAIN_TEXT_BLOCK_DELIMITER> {
    {WhiteSpace}                    {   return JadeTokenId.WHITESPACE; }
    .*                              {   // the  text will not be renedered
                                        return JadeTokenId.UNKNOWN; 
                                    }
    {LineTerminator}                {   blockIndent = -1;
                                        eolPosition = 0;
                                        yybegin(IN_PLAIN_TEXT_BLOCK_AFTER_EOL);
                                        return JadeTokenId.EOL;
                                    }
}
<IN_PLAIN_TEXT_BLOCK> {
    .*                              { }
    {LineTerminator}                {   yybegin(IN_PLAIN_TEXT_BLOCK_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
    
}

<IN_PLAIN_TEXT_BLOCK_AFTER_EOL> {
    {WhiteSpace}                    {   
                                        int currentIndent = tokenLength - eolPosition;
                                        if (currentIndent <= indent) {
                                            // the block has to have one more space than the tag
                                            yybegin(AFTER_EOL);
                                            indent = currentIndent;
                                            return JadeTokenId.WHITESPACE;
                                        }
                                        if (blockIndent < 0) {
                                            blockIndent = currentIndent;
                                        }
                                        if (blockIndent > currentIndent) {
                                            yypushback(currentIndent);
                                            yybegin(AFTER_EOL);
                                            return JadeTokenId.PLAIN_TEXT;
                                        }
                                        yybegin(IN_PLAIN_TEXT_BLOCK);
                                    }
    {LineTerminator}                {}                                
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        return JadeTokenId.PLAIN_TEXT;
                                    }
}

<IN_FILTER_BLOCK>   {
    .*                              { }
    {LineTerminator}                {   yybegin(IN_FILTER_BLOCK_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
    
}

<IN_FILTER_BLOCK_AFTER_EOL> {
    {WhiteSpace}                    {   int indentInBlock = tokenLength - eolPosition;
                                        if (blockIndent < 0) {
                                            blockIndent = indentInBlock;
                                        }
                                        if (blockIndent > indentInBlock) {
                                            yypushback(indentInBlock);
                                            yybegin(AFTER_EOL);
                                            return JadeTokenId.FILTER_TEXT;
                                        }
                                        yybegin(IN_FILTER_BLOCK);
                                    }
    {LineTerminator}                {}                                
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength - 1 > 0) { 
                                            return JadeTokenId.FILTER_TEXT;
                                        }
                                    }
}

/* This is help rule. Read all until end of line and remember the number of read chars. */
<IN_COMMENT> {
    .*                              { }
    {LineTerminator}                {   yybegin(IN_COMMENT_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
}

/* Scan the begining of line in commnet. 
    If there is a whitespace, we need to find out, if the indentation says that the commment
    continues or finished already. */
<IN_COMMENT_AFTER_EOL>              {
    {WhiteSpace}                    {   int indentInComment = tokenLength - eolPosition;
                                        if (indent >= indentInComment) {
                                            yypushback(indentInComment + 1);  // return back also the EOL
                                            yybegin(AFTER_EOL);
                                            return JadeTokenId.COMMENT;
                                        }
                                        yybegin(IN_COMMENT);
                                    }
    {LineTerminator}                {}                                
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        return JadeTokenId.COMMENT;
                                    }   
}

/* Copy of the normal comment. Just return the appropriate tokens */
<IN_UNBUFFERED_COMMENT> {
    .*                              { }
    {LineTerminator}                {   yybegin(IN_UNBUFFERED_COMMENT_AFTER_EOL);
                                        eolPosition = tokenLength;
                                    }
}

<IN_UNBUFFERED_COMMENT_AFTER_EOL>              {
    {WhiteSpace}                    {   int indentInComment = tokenLength - eolPosition;
                                        if (indent >= indentInComment) {
                                            yypushback(indentInComment);
                                            yybegin(AFTER_EOL);
                                            return JadeTokenId.UNBUFFERED_COMMENT;
                                        }
                                        yybegin(IN_UNBUFFERED_COMMENT);
                                    }
    {LineTerminator}                {}                                    
    .                               {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        return JadeTokenId.UNBUFFERED_COMMENT;
                                    }   
}

<AFTER_DOCTYPE> {
    {LineTerminator}                { 
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        if (tokenLength > 0) {
                                            return JadeTokenId.EOL;
                                        }
                                    }
    {WhiteSpace}                    { return JadeTokenId.WHITESPACE; }
    {Input}                         { yybegin(DOCTYPE);
                                      return JadeTokenId.DOCTYPE_TEMPLATE; }
    
}

<DOCTYPE> {
    {LineTerminator}                { 
                                        yybegin(AFTER_EOL);
                                        indent = 0;
                                        if (tokenLength > 0) {
                                            return JadeTokenId.EOL;
                                        }
                                    }
    ['\"]                            {   yybegin(DOCTYPE_STRING);
                                        return JadeTokenId.DOCTYPE_STRING_START;}
    {WhiteSpace}                    { return JadeTokenId.WHITESPACE; }
    [^'\"\r\n \t\f\u00A0\u000B]+    { return JadeTokenId.DOCTYPE_ATTRIBUTE; }
    
}

<DOCTYPE_STRING> {
    {LineTerminator}                {   yypushback(1);
                                        yybegin(DOCTYPE);
                                        if (tokenLength > 0) {
                                            return JadeTokenId.UNKNOWN;
                                        }
                                    }
    [\"']                           {   yypushback(1);
                                        yybegin(DOCTYPE_STRING_END);
                                        return JadeTokenId.DOCTYPE_STRING_END;}
    [^\"'\r\n]+                     {   }
}

<DOCTYPE_STRING_END> {
    [\"']                           {   yybegin(DOCTYPE);
                                        return JadeTokenId.DOCTYPE_STRING_END;}
}

<FILEPATH> {
    {LineTerminator}                {   yypushback(1);
                                        yybegin(AFTER_EOL);
                                        if (tokenLength - 1 > 0) {
                                            return JadeTokenId.FILE_PATH;
                                        }
                                    }
    [^\r\n]                         { }
}

<IN_UNBUFFERED_COMMENT_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.UNBUFFERED_COMMENT;
    } else {
        return null;
    }}
<IN_UNBUFFERED_COMMENT><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.UNBUFFERED_COMMENT;
    } else {
        return null;
    }}
<IN_COMMENT_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.COMMENT;
    } else {
        return null;
    }}
<IN_COMMENT><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.COMMENT;
    } else {
        return null;
    }}
<IN_FILTER_BLOCK_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.FILTER_TEXT;
    } else {
        return null;
    }}
<IN_FILTER_BLOCK><<EOF>>                         {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.FILTER_TEXT;
    } else {
        return null;
    }}
<IN_PLAIN_TEXT_BLOCK_AFTER_EOL><<EOF>>              {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.PLAIN_TEXT;
    } else {
        return null;
    }}
<IN_PLAIN_TEXT_BLOCK><<EOF>>                         {   if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.PLAIN_TEXT;
    } else {
        return null;
    }}
<<EOF>> {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JadeTokenId.UNKNOWN;
    } else {
        return null;
    }
}