/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.util.Collections;
import org.clang.basic.IdentifierInfo;
import org.clang.basic.tok;
import org.clang.lex.Token;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.ClankPreprocessorServices;
import org.clang.tools.services.ClankRunPreprocessorSettings;
import org.clang.tools.services.support.Interrupter;
import org.clang.tools.services.support.TrackIncludeInfoCallback;
import static org.clank.java.std.strcmp;
import org.clank.support.Casts;
import org.clank.support.NativePointer;
import org.llvm.support.llvm;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteConstTextToken;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ClankDriverImpl {

    private static final boolean TRACE = true;

    public static TokenStream getTokenStream(APTFileBuffer buffer,
            PreprocHandler ppHandler,
            final ClankDriver.ClankPreprocessorCallback callback,
            final org.netbeans.modules.cnd.support.Interrupter interrupter) {
        try {
            ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl) ppHandler.getIncludeHandler();
            Token[] tokens = includeHandler.getTokens();
            int nrTokens = includeHandler.getNrTokens();
            if (tokens == null) {
                CharSequence path = buffer.getAbsolutePath();
                byte[] bytes = toBytes(buffer.getCharBuffer());
                // prepare params to run preprocessor
                ClankRunPreprocessorSettings settings = new ClankRunPreprocessorSettings();
                settings.WorkName = path;
                settings.GenerateDiagnostics = true;
                settings.PrettyPrintDiagnostics = true;
                settings.PrintDiagnosticsOS = llvm.errs();
                settings.TraceClankStatistics = false;
                settings.cancelled = new Interrupter() {
                    @Override
                    public boolean isCancelled() {
                        return interrupter.cancelled();
                    }
                };
                FileTokensCallback fileTokensCallback = new FileTokensCallback(path, STOP_AT_FILE_PATH, llvm.errs(), callback);
                settings.IncludeInfoCallbacks = fileTokensCallback;
                ClankCompilationDataBase db = APTToClankCompilationDB.convertPPHandler(ppHandler, path);
                ClankPreprocessorServices.preprocess(Collections.singleton(db), settings);
                tokens = fileTokensCallback.getTokens(); 
                nrTokens = fileTokensCallback.getNrTokens();
                includeHandler.setTokens(tokens, nrTokens);
            }
            if (interrupter.cancelled() || tokens == null) {
                return null;
            }
            return new ClankToAPTTokenStream(tokens, nrTokens);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    private static final int STOP_AT_FILE_PATH = -1;

    private static byte[] toBytes(char[] chars) {
        byte[] asciis = new byte[chars.length];
        for (int i = 0; i < asciis.length; i++) {
            asciis[i] = NativePointer.$(chars[i]);
        }
        return asciis;
    }

    private static class FileTokensCallback extends TrackIncludeInfoCallback {

        private final ClankDriver.ClankPreprocessorCallback delegate;
        private final CharSequence path;
        private final int stopAtIndex;
        private Token[] tokens;
        private int nrTokens;

        public FileTokensCallback(CharSequence path, int stopAtIndex, raw_ostream traceOS, ClankDriver.ClankPreprocessorCallback delegate) {
            super(traceOS);
            this.path = path;
            this.stopAtIndex = stopAtIndex;
            this.delegate = delegate;
        }

        @Override
        public void onExit(TrackIncludeInfoCallback.IncludeFileInfo fileInfo) {
            if (ClankDriverImpl.TRACE) {
                traceOS.$out("Exit from ");
                if (fileInfo.isFile()) {
                    traceOS.$out(fileInfo.getName());
                } else {
                    traceOS.$out(fileInfo.getFileID());
                }
                traceOS.$out(" with #Token: ").$out(fileInfo.getTokens().size()).$out("\n");
                int[] offs = fileInfo.getSkippedRanges();
                if (offs.length > 0) {
                    for (int i = 0; i < offs.length; i += 2) {
                        int st = offs[i];
                        int end = offs[i + 1];
                        traceOS.$out("[").$out(st).$out("-").$out(end).$out("] ");
                    }
                    traceOS.$out("\n");
                }
                traceOS.flush();
            }
            
            if (stopAtIndex != STOP_AT_FILE_PATH) {
                if (stopAtIndex == fileInfo.getIncludeIndex()) {
                    nrTokens = fileInfo.getTokens().size();
                    tokens = fileInfo.stealTokens();
                }
            } else if (fileInfo.isFile() && (strcmp(path, fileInfo.getName()) == 0)) {
                nrTokens = fileInfo.getTokens().size();
                tokens = fileInfo.stealTokens();
            }
        }

        private Token[] getTokens() {
            return tokens;
        }

        public int getNrTokens() {
            return nrTokens;
        }
    }

    private static final class ClankToAPTTokenStream implements APTTokenStream, TokenStream {

        private int index;
        private final int lastIndex;
        private final Token[] tokens;

        public ClankToAPTTokenStream(Token[] tokens, int nrTokens) {
            this.tokens = tokens;
            this.lastIndex = nrTokens - 1;
            this.index = 0;
        }

        @Override
        public APTToken nextToken() {
            if (index < lastIndex) {
                return new ClankToAPTToken(tokens[index++]);
            } else {
                return APTUtils.EOF_TOKEN;
            }
        }
    }

    private static boolean assertSpellings(int aptTokenType, int clankKind) {
        CharSequence tokenSimpleSpelling = tok.getTokenSimpleSpelling(clankKind);
        boolean lwToken = APTLiteConstTextToken.isLiteConstTextType(aptTokenType); 
        assert (tokenSimpleSpelling != null) == lwToken : aptTokenType + " vs. " + tokenSimpleSpelling + ":" + clankKind;
        if (lwToken) {
            APTLiteConstTextToken aptToken = new APTLiteConstTextToken(aptTokenType, 0, 0, 0);
            String text = aptToken.getText();
            assert text.contentEquals(tokenSimpleSpelling) : text + " vs. " + tokenSimpleSpelling;
        }
        return true;
    }
    
    private static final class ClankToAPTToken extends APTTokenAbstact {
        private static final CharSequence COMMENT_TEXT_ID = CharSequences.create("/*COMMENT*/");

        private final Token orig;
        private final int aptTokenType;

        private ClankToAPTToken(Token token) {
            this.orig = token;
            this.aptTokenType = convertClankToAPTTokenKind(token.getKind());
            assert assertSpellings(aptTokenType, token.getKind());
        }

        @Override
        public int getType() {
            return aptTokenType;
        }

        @Override
        public String getText() {
            if (APTLiteConstTextToken.isLiteConstTextType(aptTokenType)) {
                return APTLiteConstTextToken.toText(aptTokenType);
            }
            return getTextID().toString();
        }

        @Override
        public CharSequence getTextID() {
            if (APTLiteConstTextToken.isLiteConstTextType(aptTokenType)) {
                return APTLiteConstTextToken.toTextID(aptTokenType);
            } else if (orig.isLiteral()) {
                return CharSequences.create(Casts.toCharSequence(orig.getLiteralData()));
            } else if (orig.is(tok.TokenKind.comment)) {
                return COMMENT_TEXT_ID;
            } else if (orig.is(tok.TokenKind.raw_identifier)) {
                return CharSequences.create(Casts.toCharSequence(orig.getRawIdentifierData()));
            } else {
                IdentifierInfo identifierInfo = orig.getIdentifierInfo();
                assert identifierInfo != null : "No Text for " + orig;
                return CharSequences.create(Casts.toCharSequence(identifierInfo.getNameStart()));
            }
        }

        @Override
        public String toString() {
            return "ClankToAPTToken{" + ", aptTokenType=" + APTUtils.getAPTTokenName(aptTokenType) + "\norig=" + orig + '}';
        }
    }
    
    private static int convertClankToAPTTokenKind(short clankTokenKind) {
        switch (clankTokenKind) {
            // These define members of the tok::* namespace.
            case tok.TokenKind.unknown: // Not a token.
            case tok.TokenKind.eof: // End of file.
                return APTTokenTypes.EOF;                
            case tok.TokenKind.eod: // End of preprocessing directive (end of line inside a 
            // directive).
            case tok.TokenKind.code_completion: // Code completion marker
            case tok.TokenKind.cxx_defaultarg_end: // C++ default argument end marker
                assert false : tok.getTokenName(clankTokenKind) + " [" + tok.getTokenSimpleSpelling(clankTokenKind) + "]";
            // C99 6.4.9: Comments.
            case tok.TokenKind.comment: // Comment (only in -E -C[C] mode)
                return APTTokenTypes.COMMENT;
            // C99 6.4.2: Identifiers.
            case tok.TokenKind.identifier: // abcde123
            case tok.TokenKind.raw_identifier: // Used only in raw lexing mode.
                return APTTokenTypes.IDENT;
            // C99 6.4.4.1: Integer Constants
            // C99 6.4.4.2: Floating Constants
            case tok.TokenKind.numeric_constant: // 0x123
                return APTTokenTypes.NUMBER;
            // C99 6.4.4: Character Constants
            case tok.TokenKind.char_constant: // 'a'
            case tok.TokenKind.wide_char_constant: // L'b'
            // C++11 Character Constants
            case tok.TokenKind.utf16_char_constant: // u'a'
            case tok.TokenKind.utf32_char_constant: // U'a'
                return APTTokenTypes.CHAR_LITERAL;

            // C99 6.4.5: String Literals.
            case tok.TokenKind.string_literal: // "foo"
            case tok.TokenKind.wide_string_literal: // L"foo"
            case tok.TokenKind.angle_string_literal: // <foo>

            // C++11 String Literals.
            case tok.TokenKind.utf8_string_literal: // u8"foo"
            case tok.TokenKind.utf16_string_literal: // u"foo"
            case tok.TokenKind.utf32_string_literal: // U"foo"
                return APTTokenTypes.STRING_LITERAL;
            // C99 6.4.6: Punctuators.
            case tok.TokenKind.l_square:
                return APTTokenTypes.LSQUARE;
            case tok.TokenKind.r_square:
                return APTTokenTypes.RSQUARE;
            case tok.TokenKind.l_paren:
                return APTTokenTypes.LPAREN;
            case tok.TokenKind.r_paren:
                return APTTokenTypes.RPAREN;
            case tok.TokenKind.l_brace:
                return APTTokenTypes.LCURLY;
            case tok.TokenKind.r_brace:
                return APTTokenTypes.RCURLY;
            case tok.TokenKind.period:
                return APTTokenTypes.DOT;
            case tok.TokenKind.ellipsis:
                return APTTokenTypes.ELLIPSIS;
            case tok.TokenKind.amp:
                return APTTokenTypes.AMPERSAND;
            case tok.TokenKind.ampamp:
                return APTTokenTypes.AND;
            case tok.TokenKind.ampequal:
                return APTTokenTypes.BITWISEANDEQUAL;
            case tok.TokenKind.star:
                return APTTokenTypes.STAR;
            case tok.TokenKind.starequal:
                return APTTokenTypes.TIMESEQUAL;
            case tok.TokenKind.plus:
                return APTTokenTypes.PLUS;
            case tok.TokenKind.plusplus:
                return APTTokenTypes.PLUSPLUS;
            case tok.TokenKind.plusequal:
                return APTTokenTypes.PLUSEQUAL;
            case tok.TokenKind.minus:
                return APTTokenTypes.MINUS;
            case tok.TokenKind.arrow:
                return APTTokenTypes.POINTERTO;
            case tok.TokenKind.minusminus:
                return APTTokenTypes.MINUSMINUS;
            case tok.TokenKind.minusequal:
                return APTTokenTypes.MINUSEQUAL;
            case tok.TokenKind.tilde:
                return APTTokenTypes.TILDE;
            case tok.TokenKind.exclaim:
                return APTTokenTypes.NOT;
            case tok.TokenKind.exclaimequal:
                return APTTokenTypes.NOTEQUAL;
            case tok.TokenKind.slash:
                return APTTokenTypes.DIVIDE;
            case tok.TokenKind.slashequal:
                return APTTokenTypes.DIVIDEEQUAL;
            case tok.TokenKind.percent:
                return APTTokenTypes.MOD;
            case tok.TokenKind.percentequal:
                return APTTokenTypes.MODEQUAL;
            case tok.TokenKind.less:
                return APTTokenTypes.LESSTHAN;
            case tok.TokenKind.lessless:
                return APTTokenTypes.SHIFTLEFT;
            case tok.TokenKind.lessequal:
                return APTTokenTypes.LESSTHANOREQUALTO;
            case tok.TokenKind.lesslessequal:
                return APTTokenTypes.SHIFTLEFTEQUAL;
            case tok.TokenKind.greater:
                return APTTokenTypes.GREATERTHAN;
            case tok.TokenKind.greatergreater:
                return APTTokenTypes.SHIFTRIGHT;
            case tok.TokenKind.greaterequal:
                return APTTokenTypes.GREATERTHANOREQUALTO;
            case tok.TokenKind.greatergreaterequal:
                return APTTokenTypes.SHIFTRIGHTEQUAL;
            case tok.TokenKind.caret:
                return APTTokenTypes.BITWISEXOR;
            case tok.TokenKind.caretequal:
                return APTTokenTypes.BITWISEXOREQUAL;
            case tok.TokenKind.pipe:
                return APTTokenTypes.BITWISEOR;
            case tok.TokenKind.pipepipe:
                return APTTokenTypes.OR;
            case tok.TokenKind.pipeequal:
                return APTTokenTypes.BITWISEOREQUAL;
            case tok.TokenKind.question:
                return APTTokenTypes.QUESTIONMARK;
            case tok.TokenKind.colon:
                return APTTokenTypes.COLON;
            case tok.TokenKind.semi:
                return APTTokenTypes.SEMICOLON;
            case tok.TokenKind.equal:
                return APTTokenTypes.ASSIGNEQUAL;
            case tok.TokenKind.equalequal:
                return APTTokenTypes.EQUAL;
            case tok.TokenKind.comma:
                return APTTokenTypes.COMMA;
            case tok.TokenKind.hash:
                return APTTokenTypes.SHARP;
            case tok.TokenKind.hashhash:
                return APTTokenTypes.DBL_SHARP;
            case tok.TokenKind.hashat:
                assert false : tok.getTokenName(clankTokenKind) + " [" + tok.getTokenSimpleSpelling(clankTokenKind) + "]";
            // C++ Support
            case tok.TokenKind.periodstar:
                return APTTokenTypes.DOTMBR;
            case tok.TokenKind.arrowstar:
                return APTTokenTypes.POINTERTOMBR;
            case tok.TokenKind.coloncolon:
                return APTTokenTypes.SCOPE;

            // Objective C support.
            case tok.TokenKind.at:
                return APTTokenTypes.AT;
            // CUDA support.
            case tok.TokenKind.lesslessless:                
                assert false : tok.getTokenName(clankTokenKind) + " [" + tok.getTokenSimpleSpelling(clankTokenKind) + "]";
            case tok.TokenKind.greatergreatergreater:
                assert false : tok.getTokenName(clankTokenKind) + " [" + tok.getTokenSimpleSpelling(clankTokenKind) + "]";
            // C99 6.4.1: Keywords.  These turn into kw_* tokens.
            // Flags allowed:
            //   KEYALL   - This is a keyword in all variants of C and C++, or it
            //              is a keyword in the implementation namespace that should
            //              always be treated as a keyword
            //   KEYC99   - This is a keyword introduced to C in C99
            //   KEYC11   - This is a keyword introduced to C in C11
            //   KEYCXX   - This is a C++ keyword, or a C++-specific keyword in the
            //              implementation namespace
            //   KEYNOCXX - This is a keyword in every non-C++ dialect.
            //   KEYCXX11 - This is a C++ keyword introduced to C++ in C++11
            //   KEYGNU   - This is a keyword if GNU extensions are enabled
            //   KEYMS    - This is a keyword if Microsoft extensions are enabled
            //   KEYNOMS  - This is a keyword that must never be enabled under
            //              Microsoft mode
            //   KEYOPENCL  - This is a keyword in OpenCL
            //   KEYALTIVEC - This is a keyword in AltiVec
            //   KEYBORLAND - This is a keyword if Borland extensions are enabled
            //   BOOLSUPPORT - This is a keyword if 'bool' is a built-in type
            //   WCHARSUPPORT - This is a keyword if 'wchar_t' is a built-in type
            //
            case tok.TokenKind.kw_auto:
            case tok.TokenKind.kw_break:
            case tok.TokenKind.kw_case:
            case tok.TokenKind.kw_char:
            case tok.TokenKind.kw_const:
            case tok.TokenKind.kw_continue:
            case tok.TokenKind.kw_default:
            case tok.TokenKind.kw_do:
            case tok.TokenKind.kw_double:
            case tok.TokenKind.kw_else:
            case tok.TokenKind.kw_enum:
            case tok.TokenKind.kw_extern:
            case tok.TokenKind.kw_float:
            case tok.TokenKind.kw_for:
            case tok.TokenKind.kw_goto:
            case tok.TokenKind.kw_if:
            case tok.TokenKind.kw_inline:
            case tok.TokenKind.kw_int:
            case tok.TokenKind.kw_long:
            case tok.TokenKind.kw_register:
            case tok.TokenKind.kw_restrict:
            case tok.TokenKind.kw_return:
            case tok.TokenKind.kw_short:
            case tok.TokenKind.kw_signed:
            case tok.TokenKind.kw_sizeof:
            case tok.TokenKind.kw_static:
            case tok.TokenKind.kw_struct:
            case tok.TokenKind.kw_switch:
            case tok.TokenKind.kw_typedef:
            case tok.TokenKind.kw_union:
            case tok.TokenKind.kw_unsigned:
            case tok.TokenKind.kw_void:
            case tok.TokenKind.kw_volatile:
            case tok.TokenKind.kw_while:
            case tok.TokenKind.kw__Alignas:
            case tok.TokenKind.kw__Alignof:
            case tok.TokenKind.kw__Atomic:
            case tok.TokenKind.kw__Bool:
            case tok.TokenKind.kw__Complex:
            case tok.TokenKind.kw__Generic:
            case tok.TokenKind.kw__Imaginary:
            case tok.TokenKind.kw__Noreturn:
            case tok.TokenKind.kw__Static_assert:
            case tok.TokenKind.kw__Thread_local:
            case tok.TokenKind.kw___func__:
            case tok.TokenKind.kw___objc_yes:
            case tok.TokenKind.kw___objc_no:

            // C++ 2.11p1: Keywords.
            case tok.TokenKind.kw_asm:
            case tok.TokenKind.kw_bool:
            case tok.TokenKind.kw_catch:
            case tok.TokenKind.kw_class:
            case tok.TokenKind.kw_const_cast:
            case tok.TokenKind.kw_delete:
            case tok.TokenKind.kw_dynamic_cast:
            case tok.TokenKind.kw_explicit:
            case tok.TokenKind.kw_export:
            case tok.TokenKind.kw_false:
            case tok.TokenKind.kw_friend:
            case tok.TokenKind.kw_mutable:
            case tok.TokenKind.kw_namespace:
            case tok.TokenKind.kw_new:
            case tok.TokenKind.kw_operator:
            case tok.TokenKind.kw_private:
            case tok.TokenKind.kw_protected:
            case tok.TokenKind.kw_public:
            case tok.TokenKind.kw_reinterpret_cast:
            case tok.TokenKind.kw_static_cast:
            case tok.TokenKind.kw_template:
            case tok.TokenKind.kw_this:
            case tok.TokenKind.kw_throw:
            case tok.TokenKind.kw_true:
            case tok.TokenKind.kw_try:
            case tok.TokenKind.kw_typename:
            case tok.TokenKind.kw_typeid:
            case tok.TokenKind.kw_using:
            case tok.TokenKind.kw_virtual:
            case tok.TokenKind.kw_wchar_t:

            // C++11 keywords
            case tok.TokenKind.kw_alignas:
            case tok.TokenKind.kw_alignof:
            case tok.TokenKind.kw_char16_t:
            case tok.TokenKind.kw_char32_t:
            case tok.TokenKind.kw_constexpr:
            case tok.TokenKind.kw_decltype:
            case tok.TokenKind.kw_noexcept:
            case tok.TokenKind.kw_nullptr:
            case tok.TokenKind.kw_static_assert:
            case tok.TokenKind.kw_thread_local:

            // GNU Extensions (in impl-reserved namespace)
            case tok.TokenKind.kw__Decimal32:
            case tok.TokenKind.kw__Decimal64:
            case tok.TokenKind.kw__Decimal128:
            case tok.TokenKind.kw___null:
            case tok.TokenKind.kw___alignof:
            case tok.TokenKind.kw___attribute:
            case tok.TokenKind.kw___builtin_choose_expr:
            case tok.TokenKind.kw___builtin_offsetof:
            case tok.TokenKind.kw___builtin_types_compatible_p:
            case tok.TokenKind.kw___builtin_va_arg:
            case tok.TokenKind.kw___extension__:
            case tok.TokenKind.kw___imag:
            case tok.TokenKind.kw___int128:
            case tok.TokenKind.kw___label__:
            case tok.TokenKind.kw___real:
            case tok.TokenKind.kw___thread:
            case tok.TokenKind.kw___FUNCTION__:
            case tok.TokenKind.kw___PRETTY_FUNCTION__:

            // GNU Extensions (outside impl-reserved namespace)
            case tok.TokenKind.kw_typeof:

            // MS Extensions
            case tok.TokenKind.kw___FUNCDNAME__:
            case tok.TokenKind.kw_L__FUNCTION__:
            case tok.TokenKind.kw___is_interface_class:
            case tok.TokenKind.kw___is_sealed:

            // GNU and MS Type Traits
            case tok.TokenKind.kw___has_nothrow_assign:
            case tok.TokenKind.kw___has_nothrow_move_assign:
            case tok.TokenKind.kw___has_nothrow_copy:
            case tok.TokenKind.kw___has_nothrow_constructor:
            case tok.TokenKind.kw___has_trivial_assign:
            case tok.TokenKind.kw___has_trivial_move_assign:
            case tok.TokenKind.kw___has_trivial_copy:
            case tok.TokenKind.kw___has_trivial_constructor:
            case tok.TokenKind.kw___has_trivial_move_constructor:
            case tok.TokenKind.kw___has_trivial_destructor:
            case tok.TokenKind.kw___has_virtual_destructor:
            case tok.TokenKind.kw___is_abstract:
            case tok.TokenKind.kw___is_base_of:
            case tok.TokenKind.kw___is_class:
            case tok.TokenKind.kw___is_convertible_to:
            case tok.TokenKind.kw___is_empty:
            case tok.TokenKind.kw___is_enum:
            case tok.TokenKind.kw___is_final:
            // Tentative name - there's no implementation of std::is_literal_type yet.
            case tok.TokenKind.kw___is_literal:
            // Name for GCC 4.6 compatibility - people have already written libraries using
            // this name unfortunately.
            case tok.TokenKind.kw___is_literal_type:
            case tok.TokenKind.kw___is_pod:
            case tok.TokenKind.kw___is_polymorphic:
            case tok.TokenKind.kw___is_trivial:
            case tok.TokenKind.kw___is_union:

            // Clang-only C++ Type Traits
            case tok.TokenKind.kw___is_trivially_constructible:
            case tok.TokenKind.kw___is_trivially_copyable:
            case tok.TokenKind.kw___is_trivially_assignable:
            case tok.TokenKind.kw___underlying_type:

            // Embarcadero Expression Traits
            case tok.TokenKind.kw___is_lvalue_expr:
            case tok.TokenKind.kw___is_rvalue_expr:

            // Embarcadero Unary Type Traits
            case tok.TokenKind.kw___is_arithmetic:
            case tok.TokenKind.kw___is_floating_point:
            case tok.TokenKind.kw___is_integral:
            case tok.TokenKind.kw___is_complete_type:
            case tok.TokenKind.kw___is_void:
            case tok.TokenKind.kw___is_array:
            case tok.TokenKind.kw___is_function:
            case tok.TokenKind.kw___is_reference:
            case tok.TokenKind.kw___is_lvalue_reference:
            case tok.TokenKind.kw___is_rvalue_reference:
            case tok.TokenKind.kw___is_fundamental:
            case tok.TokenKind.kw___is_object:
            case tok.TokenKind.kw___is_scalar:
            case tok.TokenKind.kw___is_compound:
            case tok.TokenKind.kw___is_pointer:
            case tok.TokenKind.kw___is_member_object_pointer:
            case tok.TokenKind.kw___is_member_function_pointer:
            case tok.TokenKind.kw___is_member_pointer:
            case tok.TokenKind.kw___is_const:
            case tok.TokenKind.kw___is_volatile:
            case tok.TokenKind.kw___is_standard_layout:
            case tok.TokenKind.kw___is_signed:
            case tok.TokenKind.kw___is_unsigned:

            // Embarcadero Binary Type Traits
            case tok.TokenKind.kw___is_same:
            case tok.TokenKind.kw___is_convertible:
            case tok.TokenKind.kw___array_rank:
            case tok.TokenKind.kw___array_extent:

            // Apple Extension.
            case tok.TokenKind.kw___private_extern__:
            case tok.TokenKind.kw___module_private__:

            // Microsoft Extension.
            case tok.TokenKind.kw___declspec:
            case tok.TokenKind.kw___cdecl:
            case tok.TokenKind.kw___stdcall:
            case tok.TokenKind.kw___fastcall:
            case tok.TokenKind.kw___thiscall:
            case tok.TokenKind.kw___forceinline:
            case tok.TokenKind.kw___unaligned:

            // OpenCL-specific keywords
            case tok.TokenKind.kw___kernel:
            case tok.TokenKind.kw_vec_step:
            case tok.TokenKind.kw___private:
            case tok.TokenKind.kw___global:
            case tok.TokenKind.kw___local:
            case tok.TokenKind.kw___constant:
            case tok.TokenKind.kw___read_only:
            case tok.TokenKind.kw___write_only:
            case tok.TokenKind.kw___read_write:
            case tok.TokenKind.kw___builtin_astype:
            case tok.TokenKind.kw_image1d_t:
            case tok.TokenKind.kw_image1d_array_t:
            case tok.TokenKind.kw_image1d_buffer_t:
            case tok.TokenKind.kw_image2d_t:
            case tok.TokenKind.kw_image2d_array_t:
            case tok.TokenKind.kw_image3d_t:
            case tok.TokenKind.kw_sampler_t:
            case tok.TokenKind.kw_event_t:

            // Borland Extensions.
            case tok.TokenKind.kw___pascal:

            // Altivec Extension.
            case tok.TokenKind.kw___vector:
            case tok.TokenKind.kw___pixel:

            // OpenCL Extension.
            case tok.TokenKind.kw_half:

            // Objective-C ARC keywords.
            case tok.TokenKind.kw___bridge:
            case tok.TokenKind.kw___bridge_transfer:
            case tok.TokenKind.kw___bridge_retained:
            case tok.TokenKind.kw___bridge_retain:

            // Microsoft extensions which should be disabled in strict conformance mode
            case tok.TokenKind.kw___ptr64:
            case tok.TokenKind.kw___ptr32:
            case tok.TokenKind.kw___sptr:
            case tok.TokenKind.kw___uptr:
            case tok.TokenKind.kw___w64:
            case tok.TokenKind.kw___uuidof:
            case tok.TokenKind.kw___try:
            case tok.TokenKind.kw___finally:
            case tok.TokenKind.kw___leave:
            case tok.TokenKind.kw___int64:
            case tok.TokenKind.kw___if_exists:
            case tok.TokenKind.kw___if_not_exists:
            case tok.TokenKind.kw___single_inheritance:
            case tok.TokenKind.kw___multiple_inheritance:
            case tok.TokenKind.kw___virtual_inheritance:
            case tok.TokenKind.kw___interface:

            // Clang Extensions.
            case tok.TokenKind.kw___builtin_convertvector:

            // Clang-specific keywords enabled only in testing.
            case tok.TokenKind.kw___unknown_anytype:
                return APTTokenTypes.IDENT;
                
            // TODO: What to do about context-sensitive keywords like:
            //       bycopy/byref/in/inout/oneway/out?
            case tok.TokenKind.annot_cxxscope: // annotation for a C++ scope spec, e.g. "::foo::bar::"
            case tok.TokenKind.annot_typename: // annotation for a C typedef name, a C++ (possibly
            // qualified) typename, e.g. "foo::MyClass", or
            // template-id that names a type ("std::vector<int>")
            case tok.TokenKind.annot_template_id: // annotation for a C++ template-id that names a
            // function template specialization (not a type),
            // e.g., "std::swap<int>"
            case tok.TokenKind.annot_primary_expr: // annotation for a primary expression
            case tok.TokenKind.annot_decltype: // annotation for a decltype expression,
            // e.g., "decltype(foo.bar())"

            // Annotation for #pragma unused(...)
            // For each argument inside the parentheses the pragma handler will produce
            // one 'pragma_unused' annotation token followed by the argument token.
            case tok.TokenKind.annot_pragma_unused:

            // Annotation for #pragma GCC visibility...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_vis:

            // Annotation for #pragma pack...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_pack:

            // Annotation for #pragma clang __debug parser_crash...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_parser_crash:

            // Annotation for #pragma clang __debug captured...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_captured:

            // Annotation for #pragma ms_struct...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_msstruct:

            // Annotation for #pragma align...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_align:

            // Annotation for #pragma weak id
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_weak:

            // Annotation for #pragma weak id = id
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_weakalias:

            // Annotation for #pragma redefine_extname...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_redefine_extname:

            // Annotation for #pragma STDC FP_CONTRACT...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_fp_contract:

            // Annotation for #pragma OPENCL EXTENSION...
            // The lexer produces these so that they only take effect when the parser
            // handles them.
            case tok.TokenKind.annot_pragma_opencl_extension:

            // Annotations for OpenMP pragma directives - #pragma omp ...
            // The lexer produces these so that they only take effect when the parser
            // handles #pragma omp ... directives.
            case tok.TokenKind.annot_pragma_openmp:
            case tok.TokenKind.annot_pragma_openmp_end:

            // Annotation for module import translated from #include etc.
            case tok.TokenKind.annot_module_include:
            case tok.TokenKind.NUM_TOKENS:
                assert false : tok.getTokenName(clankTokenKind) + " [" + tok.getTokenSimpleSpelling(clankTokenKind) + "]";
        }
        assert false : tok.getTokenName(clankTokenKind) + " [" + tok.getTokenSimpleSpelling(clankTokenKind) + "]";
        return APTTokenTypes.EOF;
    }
}
