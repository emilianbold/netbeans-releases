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
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

%%

%public
%class DocumentorColoringScanner
%type PHPDocCommentTokenId
%function nextToken
%unicode
%caseless
%char




%state ST_IN_TAG
%state ST_NO_TAG


%{
        private LexerInput input;

        DocumentorColoringScanner (LexerRestartInfo info) {
            this.input = info.input();

            if(info.state() != null) {
                //reset state
                setState((LexerState)info.state());
            } else {
                //initial state
                zzState = zzLexicalState = YYINITIAL;
            }
       }
        

        public int getTokenLength() {
            return yylength();
        }

        public class LexerState  {
            /** the current state of the DFA */
            final int zzState;
            /** the current lexical state */
            final int zzLexicalState;
            
            LexerState () {
                zzState =  DocumentorColoringScanner.this.zzState;
                zzLexicalState = DocumentorColoringScanner.this.zzLexicalState;
            }
            
        }
        
        public LexerState getState() {
            return new LexerState();
        }
        
        public void setState(LexerState state) {    
            this.zzState = state.zzState;
            this.zzLexicalState = state.zzLexicalState;
        }

   // End user code

%}

TABS_AND_SPACES=[ \t]*
ANY_CHAR=(.|[\n])
NEWLINE=("\r"|"\n"|"\r\n")
LINESTART=({TABS_AND_SPACES}"*"?{TABS_AND_SPACES})
EMPTYLINE=({LINESTART}{TABS_AND_SPACES}{NEWLINE})






%%

<YYINITIAL> "@" {
    yybegin(ST_IN_TAG);
    yypushback(1);
}

<YYINITIAL>[^@]* {
    return PHPDocCommentTokenId.PHPDOC_COMMENT;
}

<ST_IN_TAG> {
    "@access"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_ACCESS;}
    "@abstract"      {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_ABSTRACT;}
    "@author"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_AUTHOR;}
    "@category"      {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_CATEGORY;}
    "@copyright"     {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_COPYRIGHT;}
    "@deprecated"    {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_DEPRECATED;}
    "@desc"          {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_DESC;}
    "@example"       {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_EXAMPLE;}
    "@exception"     {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_EXCEPTION;}
    "@final"         {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_FINAL;}
    "@filesource"    {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_FILESOURCE;}
    "@global"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_GLOBAL;}
    "@ignore"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_IGNORE;}
    "@internal"      {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_INTERNAL;}
    "@license"       {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_LICENSE;}
    "@link"          {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_LINK;}
    "@magic"         {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_MAGIC;}
    "@method"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_METHOD;}    
    "@name"          {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_NAME;}
    "@package"       {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_PACKAGE;}
    "@param"         {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_PARAM;}
    "@property"      {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_PROPERTY;}
    "@property-read"  {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_PROPERTY_READ;}
    "@property-write" {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_PROPERTY_WRITE;}
    "@return"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_RETURN;}
    "@see"           {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_SEE;}
    "@since"         {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_SINCE;}
    "@static"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_STATIC;}
    "@staticvar"     {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_STATICVAR;}
    "@subpackage"    {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_SUBPACKAGE;}
    "@throws"        {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_THROWS;}
    "@todo"          {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_TODO;}
    "@tutorial"      {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_TUTORIAL;}
    "@uses"          {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_USES;}
    "@var"           {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_VAR;}
    "@version"       {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_VERSION;}
    {ANY_CHAR}       {yybegin(ST_NO_TAG); yypushback(1);}
}

<ST_NO_TAG> "@"[^@]* {
    yybegin(YYINITIAL);
    return PHPDocCommentTokenId.PHPDOC_COMMENT;
}
