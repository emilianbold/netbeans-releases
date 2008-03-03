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

package org.netbeans.modules.php.editor.lexer;

import org.netbeans.modules.php.editor.PHPVersion;
%%

%public
%class PHP5ColoringLexer
%implements PHPScanner
%type PHPTokenId
%function nextToken
%unicode
%caseless
%char




%state ST_PHP_IN_SCRIPTING
%state ST_PHP_DOUBLE_QUOTES
%state ST_PHP_BACKQUOTE
%state ST_PHP_QUOTES_AFTER_VARIABLE
%state ST_PHP_HEREDOC
%state ST_PHP_START_HEREDOC
%state ST_PHP_END_HEREDOC
%state ST_PHP_LOOKING_FOR_PROPERTY
%state ST_PHP_VAR_OFFSET
%state ST_PHP_COMMENT
%state ST_PHP_DOC_COMMENT
%state ST_PHP_LINE_COMMENT
%state ST_PHP_HIGHLIGHTING_ERROR


%{

    protected String heredoc = null;
    protected int heredoc_len = 0;
    private boolean asp_tags = false;
    private StateStack stack = new StateStack();

    private boolean short_tags_allowed = true;

    
    /*public PhpLexer5(int state){
        initialize(state);
    }*/
    /*public void reset(char array[], int offset, int length) {
        this.zzBuffer = array;
        this.zzCurrentPos = offset;
        this.zzMarkedPos = offset;
        this.zzPushbackPos = offset;
        this.yychar = offset;
        this.zzEndRead = offset + length;
        this.zzStartRead = offset;
        this.zzAtEOF = zzCurrentPos >= zzEndRead;
        this.firstPos = offset;
    }

    

    public void reset(java.io.Reader  reader, char[] buffer, int[] parameters){
    	this.zzReader = reader;
    	this.zzBuffer = buffer;
    	this.zzMarkedPos = parameters[0];
    	this.zzPushbackPos = parameters[1];
    	this.zzCurrentPos = parameters[2];
    	this.zzStartRead = parameters[3];
    	this.zzEndRead = parameters[4];
    	this.yyline = parameters[5];  
    	initialize(parameters[6]);
    }
    */
        public PHP5ColoringLexer(java.io.Reader  reader, boolean asp_tags) {
            this(reader);
            this.asp_tags = asp_tags;
        }

        public void reset(java.io.Reader  reader) {
            yyreset(reader);
        }

        public class LexerState implements PHPScannerState{
            final int saveState;
            final StateStack saveStack;
            LexerState () {
                this.saveState = yystate(); 
                this.saveStack = stack.createClone();
            }
        }
        
        public PHPScannerState getState() {
            return new LexerState();
        }
        
        public void setState(PHPScannerState state) {
            LexerState lstate = (LexerState)state;
            this.stack.copyFrom(lstate.saveStack);
            yybegin(lstate.saveState);
        }

        public PHPVersion getPHPVersion () {
            return PHPVersion.PHP_5;
        }

        public int getTokenLength() {
            return yylength();
        }

    public int getOffset() {
        return yychar;
    }

    protected boolean isHeredocState(int state){
    	    	return state == ST_PHP_HEREDOC || state == ST_PHP_START_HEREDOC || state == ST_PHP_END_HEREDOC;
    }
    
    public int[] getParamenters(){
    	return new int[]{zzMarkedPos, zzPushbackPos, zzCurrentPos, zzStartRead, zzEndRead, yyline, zzLexicalState};
    }

    protected int getZZLexicalState() {
        return zzLexicalState;
    }

    protected int getZZMarkedPos() {
        return zzMarkedPos;
    }

    protected int getZZEndRead() {
        return zzEndRead;
    }

    public char[] getZZBuffer() {
        return zzBuffer;
    }
    
    protected int getZZStartRead() {
    	return this.zzStartRead;
    }

    protected int getZZPushBackPosition() {
    	return this.zzPushbackPos;
    }

        protected void pushBack(int i) {
		yypushback(i);
	}

        protected void popState() {
		yybegin(stack.popStack());
	}

	protected void pushState(final int state) {
		stack.pushStack(getZZLexicalState());
		yybegin(state);
	}

    
 // End user code

%}
LNUM=[0-9]+
DNUM=([0-9]*[\.][0-9]+)|([0-9]+[\.][0-9]*)
EXPONENT_DNUM=(({LNUM}|{DNUM})[eE][+-]?{LNUM})
HNUM="0x"[0-9a-fA-F]+
LABEL=[a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*
WHITESPACE=[ \n\r\t]+
TABS_AND_SPACES=[ \t]*
TOKENS=[:,.\[\]()|\^&+-//*=%!~$<>?@]
CLOSE_EXPRESSION=[;]
ANY_CHAR=(.|[\n])
NEWLINE=("\r"|"\n"|"\r\n")
DOUBLE_QUOTES_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\"\\{]|("\\"{ANY_CHAR})))
BACKQUOTE_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$`\\{]|("\\"{ANY_CHAR})))
HEREDOC_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\n\r\\{]|("\\"[^\n\r])))
HEREDOC_NEWLINE=((({LABEL}";"?((("{"+|"$"+)"\\"?)|"\\"))|(("{"*|"$"*)"\\"?)){NEWLINE})
HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR=(("{"+[^$\n\r\\{])|("{"*"\\"[^\n\r])|{HEREDOC_LITERAL_DOLLAR})
HEREDOC_NON_LABEL=([^a-zA-Z_\x7f-\xff$\n\r\\{]|{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})
HEREDOC_LABEL_NO_NEWLINE=({LABEL}([^a-zA-Z0-9_\x7f-\xff;$\n\r\\{]|(";"[^$\n\r\\{])|(";"?{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})))
DOUBLE_QUOTES_CHARS=("{"*([^$\"\\{]|("\\"{ANY_CHAR}))|{DOUBLE_QUOTES_LITERAL_DOLLAR})
BACKQUOTE_CHARS=("{"*([^$`\\{]|("\\"{ANY_CHAR}))|{BACKQUOTE_LITERAL_DOLLAR})
HEREDOC_CHARS=("{"*([^$\n\r\\{]|("\\"[^\n\r]))|{HEREDOC_LITERAL_DOLLAR}|({HEREDOC_NEWLINE}+({HEREDOC_NON_LABEL}|{HEREDOC_LABEL_NO_NEWLINE})))

PHP_OPERATOR=       "=>"|"++"|"--"|"==="|"!=="|"=="|"!="|"<>"|"<="|">="|"+="|"-="|"*="|"/="|".="|"%="|"<<="|">>="|"&="|"|="|"^="|"||"|"&&"|"OR"|"AND"|"XOR"|"<<"|">>"






%%

<YYINITIAL>(([^<]|"<"[^?%s<])+)|"<s"|"<" {
    return PHPTokenId.T_INLINE_HTML;
}

<YYINITIAL>"<?"|"<script"{WHITESPACE}+"language"{WHITESPACE}*"="{WHITESPACE}*("php"|"\"php\""|"\'php\'"){WHITESPACE}*">" {
    if (short_tags_allowed || yylength()>2) { /* yyleng>2 means it's not <? but <script> */
        yybegin(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_OPENTAG;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<%="|"<?=" {
    String text = yytext();
    if ((text.charAt(1)=='%' && asp_tags)
        || (text.charAt(1)=='?' && short_tags_allowed)) {
        yybegin(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.T_OPEN_TAG_WITH_ECHO;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<%" {
    if (asp_tags) {
        yybegin(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_OPENTAG;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<?php" {
    yybegin(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_OPENTAG;
    //return createSymbol(ASTSymbol.T_OPEN_TAG);
}


/***********************************************************************************************
**************************************** P  H  P ***********************************************
***********************************************************************************************/

<ST_PHP_IN_SCRIPTING> "exit" {
    return PHPTokenId.PHP_EXIT;
}

<ST_PHP_IN_SCRIPTING>"die" {
    return PHPTokenId.PHP_DIE;
}

<ST_PHP_IN_SCRIPTING>"function" {
    return PHPTokenId.PHP_FUNCTION;
}

<ST_PHP_IN_SCRIPTING>"const" {
    return PHPTokenId.PHP_CONST;
}

<ST_PHP_IN_SCRIPTING>"return" {
    return PHPTokenId.PHP_RETURN;
}

<ST_PHP_IN_SCRIPTING>"try" {
    return PHPTokenId.PHP_TRY;
}

<ST_PHP_IN_SCRIPTING>"catch" {
    return PHPTokenId.PHP_CATCH;
}

<ST_PHP_IN_SCRIPTING>"throw" {
    return PHPTokenId.PHP_THROW;
}

<ST_PHP_IN_SCRIPTING>"if" {
    return PHPTokenId.PHP_IF;
}

<ST_PHP_IN_SCRIPTING>"elseif" {
    return PHPTokenId.PHP_ELSEIF;
}

<ST_PHP_IN_SCRIPTING>"endif" {
    return PHPTokenId.PHP_ENDIF;
}

<ST_PHP_IN_SCRIPTING>"else" {
    return PHPTokenId.PHP_ELSE;
}

<ST_PHP_IN_SCRIPTING>"while" {
    return PHPTokenId.PHP_WHILE;
}

<ST_PHP_IN_SCRIPTING>"endwhile" {
    return PHPTokenId.PHP_ENDWHILE;
}

<ST_PHP_IN_SCRIPTING>"do" {
    return PHPTokenId.PHP_DO;
}

<ST_PHP_IN_SCRIPTING>"for" {
    return PHPTokenId.PHP_FOR;
}

<ST_PHP_IN_SCRIPTING>"endfor" {
    return PHPTokenId.PHP_ENDFOR;
}

<ST_PHP_IN_SCRIPTING>"foreach" {
    return PHPTokenId.PHP_FOREACH;
}

<ST_PHP_IN_SCRIPTING>"endforeach" {
    return PHPTokenId.PHP_ENDFOREACH;
}

<ST_PHP_IN_SCRIPTING>"declare" {
    return PHPTokenId.PHP_DECLARE;
}

<ST_PHP_IN_SCRIPTING>"enddeclare" {
    return PHPTokenId.PHP_ENDDECLARE;
}

<ST_PHP_IN_SCRIPTING>"instanceof" {
    return PHPTokenId.PHP_INSTANCEOF;
}

<ST_PHP_IN_SCRIPTING>"as" {
    return PHPTokenId.PHP_AS;
}

<ST_PHP_IN_SCRIPTING>"switch" {
    return PHPTokenId.PHP_SWITCH;
}

<ST_PHP_IN_SCRIPTING>"endswitch" {
    return PHPTokenId.PHP_ENDSWITCH;
}

<ST_PHP_IN_SCRIPTING>"case" {
    return PHPTokenId.PHP_CASE;
}

<ST_PHP_IN_SCRIPTING>"default" {
    return PHPTokenId.PHP_DEFAULT;
}

<ST_PHP_IN_SCRIPTING>"break" {
    return PHPTokenId.PHP_BREAK;
}

<ST_PHP_IN_SCRIPTING>"continue" {
    return PHPTokenId.PHP_CONTINUE;
}

<ST_PHP_IN_SCRIPTING>"echo" {
    return PHPTokenId.PHP_ECHO;
}

<ST_PHP_IN_SCRIPTING>"print" {
    return PHPTokenId.PHP_PRINT;
}

<ST_PHP_IN_SCRIPTING>"class" {
    return PHPTokenId.PHP_CLASS;
}

<ST_PHP_IN_SCRIPTING>"interface" {
    return PHPTokenId.PHP_INTERFACE;
}

<ST_PHP_IN_SCRIPTING>"extends" {
    return PHPTokenId.PHP_EXTENDS;
}

<ST_PHP_IN_SCRIPTING>"implements" {
    return PHPTokenId.PHP_IMPLEMENTS;
}

<ST_PHP_IN_SCRIPTING>"self" {
    return PHPTokenId.PHP_SELF;
}

<ST_PHP_IN_SCRIPTING>"->" {
    pushState(ST_PHP_LOOKING_FOR_PROPERTY);
    return PHPTokenId.PHP_OBJECT_OPERATOR;
}

<ST_PHP_QUOTES_AFTER_VARIABLE> {
    "->" {
    popState();
    pushState(ST_PHP_LOOKING_FOR_PROPERTY);
    return PHPTokenId.PHP_OBJECT_OPERATOR;
    }
    {ANY_CHAR} {
        yypushback(1);
        popState();
    }
}

<ST_PHP_LOOKING_FOR_PROPERTY>"->" {
	return PHPTokenId.PHP_OBJECT_OPERATOR;
}

<ST_PHP_LOOKING_FOR_PROPERTY>{LABEL} {
    popState();
    return PHPTokenId.PHP_STRING;
}

<ST_PHP_LOOKING_FOR_PROPERTY>{ANY_CHAR} {
    yypushback(1);
    popState();
}

<ST_PHP_IN_SCRIPTING>"::" {
    return PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM;
}

<ST_PHP_IN_SCRIPTING>"new" {
    return PHPTokenId.PHP_NEW;
}

<ST_PHP_IN_SCRIPTING>"clone" {
    return PHPTokenId.PHP_CLONE;
}

<ST_PHP_IN_SCRIPTING>"var" {
    return PHPTokenId.PHP_VAR;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("int"|"integer"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("real"|"double"|"float"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"string"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"binary"{TABS_AND_SPACES}")" {
	return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"array"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"object"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("bool"|"boolean"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("unset"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"eval" {
    return PHPTokenId.PHP_EVAL;
}

<ST_PHP_IN_SCRIPTING>"include" {
    return PHPTokenId.PHP_INCLUDE;
}

<ST_PHP_IN_SCRIPTING>"include_once" {
    return PHPTokenId.PHP_INCLUDE_ONCE;
}

<ST_PHP_IN_SCRIPTING>"require" {
    return PHPTokenId.PHP_REQUIRE;
}

<ST_PHP_IN_SCRIPTING>"require_once" {
    return PHPTokenId.PHP_REQUIRE_ONCE;
}

<ST_PHP_IN_SCRIPTING>"use" {
    return PHPTokenId.PHP_USE;
}

<ST_PHP_IN_SCRIPTING>"global" {
    return PHPTokenId.PHP_GLOBAL;
}

<ST_PHP_IN_SCRIPTING>"isset" {
    return PHPTokenId.PHP_ISSET;
}

<ST_PHP_IN_SCRIPTING>"empty" {
    return PHPTokenId.PHP_EMPTY;
}

<ST_PHP_IN_SCRIPTING>"__halt_compiler" {
	return PHPTokenId.PHP_HALT_COMPILER;
}

<ST_PHP_IN_SCRIPTING>"static" {
    return PHPTokenId.PHP_STATIC;
}

<ST_PHP_IN_SCRIPTING>"abstract" {
    return PHPTokenId.PHP_ABSTRACT;
}

<ST_PHP_IN_SCRIPTING>"final" {
    return PHPTokenId.PHP_FINAL;
}

<ST_PHP_IN_SCRIPTING>"private" {
    return PHPTokenId.PHP_PRIVATE;
}

<ST_PHP_IN_SCRIPTING>"protected" {
    return PHPTokenId.PHP_PROTECTED;
}

<ST_PHP_IN_SCRIPTING>"public" {
    return PHPTokenId.PHP_PUBLIC;
}

<ST_PHP_IN_SCRIPTING>"unset" {
    return PHPTokenId.PHP_UNSET;
}

<ST_PHP_IN_SCRIPTING>"list" {
    return PHPTokenId.PHP_LIST;
}

<ST_PHP_IN_SCRIPTING>"array" {
    return PHPTokenId.PHP_ARRAY;
}

<ST_PHP_IN_SCRIPTING>"parent" {
    return PHPTokenId.PHP_PARENT;
}

<ST_PHP_IN_SCRIPTING>"from" {
    return PHPTokenId.PHP_FROM;
}

<ST_PHP_IN_SCRIPTING>"true" {
    return PHPTokenId.PHP_TRUE;
}

<ST_PHP_IN_SCRIPTING>"false" {
    return PHPTokenId.PHP_FALSE;
}

<ST_PHP_IN_SCRIPTING>{PHP_OPERATOR} {
    return PHPTokenId.PHP_OPERATOR;
}

<ST_PHP_IN_SCRIPTING>{TOKENS} {
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_IN_SCRIPTING>{CLOSE_EXPRESSION} {
    return PHPTokenId.PHP_SEMICOLON;
}

<ST_PHP_IN_SCRIPTING>"{" {
    return PHPTokenId.PHP_CURLY_OPEN;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC>"${" {
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_IN_SCRIPTING>"}" {
    if (!stack.isEmpty()) {
        popState();
    }
    return  PHPTokenId.PHP_CURLY_CLOSE;
}

<ST_PHP_IN_SCRIPTING>{LNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>{HNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_VAR_OFFSET>0|([1-9][0-9]*) {
	return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_VAR_OFFSET>{LNUM}|{HNUM} {
	return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>{DNUM}|{EXPONENT_DNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>"__CLASS__" {
    return PHPTokenId.PHP__CLASS__;
}

<ST_PHP_IN_SCRIPTING>"__FUNCTION__" {
    return PHPTokenId.PHP__FUNCTION__;
}

<ST_PHP_IN_SCRIPTING>"__METHOD__" {
    return PHPTokenId.PHP__METHOD__;
}

<ST_PHP_IN_SCRIPTING>"__LINE__" {
    return PHPTokenId.PHP__LINE__;
}

<ST_PHP_IN_SCRIPTING>"__FILE__" {
    return PHPTokenId.PHP__FILE__;
}

<ST_PHP_IN_SCRIPTING>"$"{LABEL} {
    return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_VAR_OFFSET>"$"{LABEL} {
    pushState(ST_PHP_QUOTES_AFTER_VARIABLE);
    return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_HEREDOC,ST_PHP_BACKQUOTE>"$"{LABEL}"[" {
	yypushback(1);
	pushState(ST_PHP_VAR_OFFSET);
	return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_VAR_OFFSET>"]" {
	popState();
	return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_VAR_OFFSET>"[" { 
	return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_VAR_OFFSET>{TOKENS}|[;{}\"`] {//the difference from the original rules comes from the fact that we took ';' out out of tokens 
	return  PHPTokenId.UNKNOWN_TOKEN;
}

<ST_PHP_VAR_OFFSET>[ \n\r\t\\'#] {
	yypushback(1);
	popState();
	return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_IN_SCRIPTING,ST_PHP_VAR_OFFSET>{LABEL} {
    return  PHPTokenId.PHP_STRING;
}

<ST_PHP_IN_SCRIPTING>{WHITESPACE} {
    return  PHPTokenId.WHITESPACE;
}

<ST_PHP_IN_SCRIPTING>([#]|"//") {
    pushState(ST_PHP_LINE_COMMENT);
    return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>"?"|"%"|">" {
    return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>[^\n\r?%>]*{ANY_CHAR} {
	String yytext = yytext();
	switch (yytext.charAt(yytext.length() - 1)) {
		case '?':
		case '%':
		case '>':
			yypushback(1);
			break;
		default:
			popState();
	}
	 return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>{NEWLINE} {
    popState();
    return PHPTokenId.PHP_LINE_COMMENT;
}


<ST_PHP_IN_SCRIPTING>"/**"{WHITESPACE} {
    pushState(ST_PHP_DOC_COMMENT);
    yypushback(yylength()-3);
    return PHPTokenId.PHPDOC_COMMENT_START;
}

<ST_PHP_DOC_COMMENT>"*/" {
    popState();
    return PHPTokenId.PHPDOC_COMMENT_END;
}



<ST_PHP_DOC_COMMENT> {
    "@access"        {return PHPTokenId.PHPDOC_ACCESS;}
    "@abstract"      {return PHPTokenId.PHPDOC_ABSTRACT;}
    "@author"        {return PHPTokenId.PHPDOC_AUTHOR;}
    "@category"      {return PHPTokenId.PHPDOC_CATEGORY;}
    "@copyright"     {return PHPTokenId.PHPDOC_COPYRIGHT;}
    "@deprecated"    {return PHPTokenId.PHPDOC_DEPRECATED;}
    "@desc"          {return PHPTokenId.PHPDOC_DESC;}
    "@example"       {return PHPTokenId.PHPDOC_EXAMPLE;}
    "@exception"     {return PHPTokenId.PHPDOC_EXCEPTION;}
    "@final"         {return PHPTokenId.PHPDOC_FINAL;}
    "@filesource"    {return PHPTokenId.PHPDOC_FILESOURCE;}
    "@global"        {return PHPTokenId.PHPDOC_GLOBAL;}
    "@ignore"        {return PHPTokenId.PHPDOC_IGNORE;}
    "@internal"      {return PHPTokenId.PHPDOC_INTERNAL;}
    "@license"       {return PHPTokenId.PHPDOC_LICENSE;}
    "@link"          {return PHPTokenId.PHPDOC_LINK;}
    "@magic"         {return PHPTokenId.PHPDOC_MAGIC;}
    "@method"        {return PHPTokenId.PHPDOC_METHOD;}    
    "@name"          {return PHPTokenId.PHPDOC_NAME;}
    "@package"       {return PHPTokenId.PHPDOC_PACKAGE;}
    "@param"         {return PHPTokenId.PHPDOC_PARAM;}
    "@property"      {return PHPTokenId.PHPDOC_PROPERTY;}
    "@return"        {return PHPTokenId.PHPDOC_RETURN;}
    "@see"           {return PHPTokenId.PHPDOC_SEE;}
    "@since"         {return PHPTokenId.PHPDOC_SINCE;}
    "@static"        {return PHPTokenId.PHPDOC_STATIC;}
    "@staticvar"     {return PHPTokenId.PHPDOC_STATICVAR;}
    "@subpackage"    {return PHPTokenId.PHPDOC_SUBPACKAGE;}
    "@throws"        {return PHPTokenId.PHPDOC_THROWS;}
    "@todo"          {return PHPTokenId.PHPDOC_TODO;}
    "@tutorial"      {return PHPTokenId.PHPDOC_TUTORIAL;}
    "@uses"          {return PHPTokenId.PHPDOC_USES;}
    "@var"           {return PHPTokenId.PHPDOC_VAR;}
    "@version"       {return PHPTokenId.PHPDOC_VERSION;}
    
    [^/@]* {
    int len = yylength();
        if (len > 1 && (yycharat(len-1) == '*')) {
            yypushback(1); // go back to mark end of comment in the next token
        }
        return PHPTokenId.PHPDOC_COMMENT;
    }
}


<ST_PHP_IN_SCRIPTING>"/*" {
    pushState(ST_PHP_COMMENT);
    return PHPTokenId.PHP_COMMENT_START;
}

<ST_PHP_COMMENT>"*/" {
    popState();
    return PHPTokenId.PHP_COMMENT_END;
}

<ST_PHP_COMMENT>(.|[\r\n])*?\*[/] {
//<ST_PHP_COMMENT>((.|[\r\n])*\*[/])?? {
//<ST_PHP_COMMENT>([^*]|[\r\n]|(\*([^/]|[\r\n])))*\*[/] {
//<ST_PHP_COMMENT> ([/][*][.]*?[*][/])|([/][*][.]*) {
//<ST_PHP_COMMENT>[^*]+ {
    int len = yylength();
    
    if (len > 1 && (yycharat(len-2) == '*') && (yycharat(len-1) == '/')) {
        yypushback(2);
    }
    return PHPTokenId.PHP_COMMENT;
}

//<ST_PHP_COMMENT>[^/]* {
//    int len = yylength();
//    if (len > 1 && (yycharat(len-1) == '*')) {
//        yypushback(1); // go back to mark end of comment in the next token
//    }
//    return PHPTokenId.PHP_COMMENT;
//}


<ST_PHP_IN_SCRIPTING,ST_PHP_LINE_COMMENT>"?>"{WHITESPACE}? {
        //popState();
	return PHPTokenId.PHP_CLOSETAG;
}

<ST_PHP_IN_SCRIPTING>"%>"{WHITESPACE}? {
	if (asp_tags) {
	    return PHPTokenId.PHP_CLOSETAG;
	}
	return  PHPTokenId.UNKNOWN_TOKEN;
}

<ST_PHP_LINE_COMMENT>"%>"{WHITESPACE}? {
	if (asp_tags) {
	    return PHPTokenId.PHP_CLOSETAG;
	}
	String text = yytext();
	if(text.indexOf('\r') != -1 || text.indexOf('\n') != -1 ){
		popState();
	}
	return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_IN_SCRIPTING>(b?[\"]{DOUBLE_QUOTES_CHARS}*("{"*|"$"*)[\"]) {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>(b?[']([^'\\]|("\\"{ANY_CHAR}))*[']) {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>b?[\"] {
    pushState(ST_PHP_DOUBLE_QUOTES);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}{LABEL}{NEWLINE} {
    int bprefix = (yytext().charAt(0) != '<') ? 1 : 0;
    int startString=3+bprefix;
    heredoc_len = yylength()-bprefix-3-1-(yytext().charAt(yylength()-2)=='\r'?1:0);
    while ((yytext().charAt(startString) == ' ') || (yytext().charAt(startString) == '\t')) {
        startString++;
        heredoc_len--;
    }
    heredoc = yytext().substring(startString,heredoc_len+startString);
    yybegin(ST_PHP_START_HEREDOC);
    return PHPTokenId.PHP_HEREDOC_TAG;
}

<ST_PHP_IN_SCRIPTING>[`] {
    pushState(ST_PHP_BACKQUOTE);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_START_HEREDOC>{ANY_CHAR} {
	yypushback(1);
	yybegin(ST_PHP_HEREDOC);
}

<ST_PHP_START_HEREDOC>{LABEL}";"?[\n\r] {
    int label_len = yylength() - 1;

    if (yytext().charAt(label_len-1)==';') {
	    label_len--;
    }

    if (label_len==heredoc_len && yytext().substring(0,label_len).equals(heredoc)) {
        heredoc=null;
        heredoc_len=0;
        yybegin(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_HEREDOC_TAG;
    } else {
        return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
    }
}

<ST_PHP_HEREDOC>{HEREDOC_CHARS}*{HEREDOC_NEWLINE}+{LABEL}";"?[\n\r] {
    int label_len = yylength() - 1;

    if (yytext().charAt(label_len-1)==';') {
	   label_len--;
    }
    if (label_len > heredoc_len && yytext().substring(label_len - heredoc_len,label_len).equals(heredoc)) {
    	   yypushback(1);
        yybegin(ST_PHP_END_HEREDOC);
    }
        return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_END_HEREDOC>{ANY_CHAR} {
    heredoc=null;
    heredoc_len=0;
    yybegin(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_QUOTES_AFTER_VARIABLE>"{$" {
    yypushback(1);
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_CURLY_OPEN;
}

<ST_PHP_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}+ {
	return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {DOUBLE_QUOTES_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[\"]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[\"])) {
    yypushback(1);
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_BACKQUOTE>{BACKQUOTE_CHARS}+ {
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {BACKQUOTE_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[`]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_BACKQUOTE>{BACKQUOTE_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[`])) {
	yypushback(1);
	return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)? {
	return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?("{"{2,}|"$"{2,})
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_HEREDOC>{HEREDOC_CHARS}*({HEREDOC_NEWLINE}+({LABEL}";"?)?)?("{""{"+|"$""$"+) {
    yypushback(1);
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_DOUBLE_QUOTES>[\"] {
    popState();
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_BACKQUOTE>[`] {
    popState();
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_DOUBLE_QUOTES>. {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_BACKQUOTE>. {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

/* ============================================
   Stay in this state until we find a whitespace.
   After we find a whitespace we go the the prev state and try again from the next token.
   ============================================ */
<ST_PHP_HIGHLIGHTING_ERROR> {
	{WHITESPACE}	{popState();return PHPTokenId.WHITESPACE;}
    .   	        {return  PHPTokenId.UNKNOWN_TOKEN;}
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<ST_PHP_IN_SCRIPTING,ST_PHP_DOUBLE_QUOTES,ST_PHP_VAR_OFFSET,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_START_HEREDOC,ST_PHP_END_HEREDOC,ST_PHP_COMMENT>. {
    yypushback(1);
    pushState(ST_PHP_HIGHLIGHTING_ERROR);
}
