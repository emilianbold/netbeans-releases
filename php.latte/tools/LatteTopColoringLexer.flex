/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.latte.lexer;

import java.util.Objects;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%class LatteTopColoringLexer
%type LatteTopTokenId
%function findNextToken
%unicode
%caseless
%char

%eofval{
        if(input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as error token
            return LatteTopTokenId.T_LATTE_ERROR;
        } else {
            return null;
        }
%eofval}

%{

    private LatteStateStack stack = new LatteStateStack();
    private LexerInput input;
    private Syntax syntax;

    public LatteTopColoringLexer(LexerRestartInfo info) {
        this.input = info.input();
        if(info.state() != null) {
            //reset state
            setState((LexerState) info.state());
            this.syntax = ((LexerState) info.state()).syntax;
        } else {
            zzState = zzLexicalState = YYINITIAL;
            this.syntax = Syntax.LATTE;
            stack.clear();
        }

    }

    private enum Syntax {
        LATTE,
        DOUBLE,
        ASP,
        PYTHON,
        OFF;
    }

    public static final class LexerState  {
        final LatteStateStack stack;
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        private final Syntax syntax;

        LexerState(LatteStateStack stack, int zzState, int zzLexicalState, Syntax syntax) {
            this.stack = stack;
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.syntax = syntax;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + Objects.hashCode(this.stack);
            hash = 29 * hash + this.zzState;
            hash = 29 * hash + this.zzLexicalState;
            hash = 29 * hash + (this.syntax != null ? this.syntax.hashCode() : 0);
            return hash;
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
            if (!Objects.equals(this.stack, other.stack)) {
                return false;
            }
            if (this.zzState != other.zzState) {
                return false;
            }
            if (this.zzLexicalState != other.zzLexicalState) {
                return false;
            }
            if (this.syntax != other.syntax) {
                return false;
            }
            return true;
        }
    }

    public LexerState getState() {
        return new LexerState(stack.createClone(), zzState, zzLexicalState, syntax);
    }

    public void setState(LexerState state) {
        this.stack.copyFrom(state.stack);
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
    }

    protected int getZZLexicalState() {
        return zzLexicalState;
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

WHITESPACE=[ \t\r\n]+
COMMENT_START="{*"
COMMENT_END=~"*}"
MACRO_SYNTAX_START="syntax"[ \t]+
MACRO_SYNTAX_END="/syntax"
SYNTAX_LATTE_START="{"
SYNTAX_LATTE_END="}"
SYNTAX_DOUBLE_START="{{"
SYNTAX_DOUBLE_END="}}"
SYNTAX_ASP_START="<%"
SYNTAX_ASP_END="%>"
SYNTAX_PYTHON_START="{%"
SYNTAX_PYTHON_END="%}"

%state ST_COMMENT
%state ST_LATTE
%state ST_DOUBLE
%state ST_ASP
%state ST_PYTHON
%state ST_SYNTAX_CHANGE
%state ST_HIGHLIGHTING_ERROR

%%
<YYINITIAL, ST_COMMENT, ST_LATTE, ST_DOUBLE>{WHITESPACE}+ {
}

<YYINITIAL> {
    {COMMENT_START} {
        if (syntax != Syntax.OFF) {
            pushState(ST_COMMENT);
        }
    }
    {SYNTAX_LATTE_START} {
        if (syntax == Syntax.LATTE) {
            pushState(ST_LATTE);
        }
    }
    {SYNTAX_DOUBLE_START} {
        if (syntax == Syntax.DOUBLE) {
            pushState(ST_DOUBLE);
        }
        if (syntax == Syntax.PYTHON) {
            pushState(ST_PYTHON);
        }
    }
    {SYNTAX_ASP_START} {
        if (syntax == Syntax.ASP) {
            pushState(ST_ASP);
        }
    }
    {SYNTAX_PYTHON_START} {
        if (syntax == Syntax.PYTHON) {
            pushState(ST_PYTHON);
        }
    }
    . {
        return LatteTopTokenId.T_HTML;
    }
}

<ST_LATTE, ST_DOUBLE, ST_ASP, ST_PYTHON> {
    {MACRO_SYNTAX_START} {
        pushState(ST_SYNTAX_CHANGE);
    }
    {MACRO_SYNTAX_END} {
        syntax = Syntax.LATTE;
    }
}

<ST_LATTE> {
    {SYNTAX_LATTE_END} {
        popState();
        return LatteTopTokenId.T_LATTE;
    }
    [^"}"] {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_DOUBLE> {
    {SYNTAX_DOUBLE_END} {
        popState();
        return LatteTopTokenId.T_LATTE;
    }
    [^"}"] | }[^"}"] {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_ASP> {
    {SYNTAX_ASP_END} {
        popState();
        return LatteTopTokenId.T_LATTE;
    }
    [^"%"] | %[^">"] {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_PYTHON> {
    {SYNTAX_PYTHON_END} {
        popState();
        return LatteTopTokenId.T_LATTE;
    }
    [^"%"] | %[^"}"] {
        return LatteTopTokenId.T_LATTE;
    }
    {SYNTAX_DOUBLE_END} {
        popState();
        return LatteTopTokenId.T_LATTE;
    }
    [^"}"] | }[^"}"] {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_SYNTAX_CHANGE> {
    "latte" {
        syntax = Syntax.LATTE;
        popState();
    }
    "double" {
        syntax = Syntax.DOUBLE;
        popState();
    }
    "asp" {
        syntax = Syntax.ASP;
        popState();
    }
    "python" {
        syntax = Syntax.PYTHON;
        popState();
    }
    "off" {
        syntax = Syntax.OFF;
        popState();
    }
    . {
        popState();
    }
}

<ST_COMMENT> {
    {COMMENT_END} {
        popState();
        return LatteTopTokenId.T_LATTE;
    }
}

/* ============================================
   Stay in this state until we find a whitespace.
   After we find a whitespace we go the the prev state and try again from the next token.
   ============================================ */
<ST_HIGHLIGHTING_ERROR> {
    {WHITESPACE} {
        popState();
    }
    . {
        return LatteTopTokenId.T_LATTE_ERROR;
    }
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<YYINITIAL, ST_COMMENT, ST_LATTE, ST_DOUBLE, ST_ASP, ST_PYTHON> {
    . {
        yypushback(yylength());
        pushState(ST_HIGHLIGHTING_ERROR);
    }
}
