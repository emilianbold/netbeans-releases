/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.cnd.api.lexer;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.cnd.utils.MIMENames;

/**
 *
 * @author Vladirmir Voskresensky
 */
public final class CndLexerUtilities {

    public static final String LEXER_FILTER = "lexer-filter"; // NOI18N
    public static final String FORTRAN_FREE_FORMAT = "fortran-free-format"; // NOI18N
    public static final String FORTRAN_MAXIMUM_TEXT_WIDTH = "fortran-maximum-text-width"; // NOI18N

    private CndLexerUtilities() {
    }

    /**
     * returns C/C++/Preprocessor tokens sequence for component
     * @param component component
     * @param offset offset
     * @param lexPP if <code>true</code> and offset is in preprocessor directive then return tokens sequnce of this 
     * directive. If <code>false</code> and offset is in preprocessor directive do not dive into embedding
     * @param backwardBias @see TokenHierarchy.embeddedTokenSequences
     * If <code>true</code> the backward lying token will
     *   be used in case that the <code>offset</code> specifies position between
     *   two tokens. If <code>false</code> the forward lying token will be used.     * 
     * @return token sequence positioned on token with offset (no need to call moveNext()/movePrevious() before token())
     */
    public static TokenSequence<CppTokenId> getCppTokenSequence(final JTextComponent component, final int offset,
            boolean lexPP, boolean backwardBias) {
        Document doc = component.getDocument();
        return getCppTokenSequence(doc, offset, lexPP, backwardBias);
    }

    public static Language<CppTokenId> getLanguage(String mime) {
        if (MIMENames.C_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageC();
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageCpp();
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageHeader();
        }
        return null;
    }

    public static Language<CppTokenId> getLanguage(final Document doc) {
        // try from property
        Language<?> lang = (Language<?>) doc.getProperty(Language.class);
        if (!isCppLanguage(lang, true)) {
            lang = getLanguage((String) doc.getProperty("mimeType")); // NOI18N
        }
        @SuppressWarnings("unchecked")
        Language<CppTokenId> out = (Language<CppTokenId>) lang;
        return out;
    }

    /**
     * returns C/C++/Preprocessor tokens sequence for document
     * @param doc dicument
     * @param offset offset
     * @param lexPP if <code>true</code> and offset is in preprocessor directive then return tokens sequnce of this 
     * directive. If <code>false</code> and offset is in preprocessor directive do not dive into embedding
     * @param backwardBias @see TokenHierarchy.embeddedTokenSequences
     * If <code>true</code> the backward lying token will
     *   be used in case that the <code>offset</code> specifies position between
     *   two tokens. If <code>false</code> the forward lying token will be used.     * 
     * @return token sequence positioned on token with offset (no need to call moveNext()/movePrevious() before token())
     */
    public static TokenSequence<CppTokenId> getCppTokenSequence(final Document doc, final int offset,
            boolean lexPP, boolean backwardBias) {
        if (doc == null) {
            return null;
        }
        TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(offset, backwardBias);
        // Go from inner to outer TSes
        for (int i = tsList.size() - 1; i >= 0; i--) {
            TokenSequence<?> ts = tsList.get(i);
            final Language<?> lang = ts.languagePath().innerLanguage();
            if (isCppLanguage(lang, lexPP)) {
                @SuppressWarnings("unchecked")
                TokenSequence<CppTokenId> cppInnerTS = (TokenSequence<CppTokenId>) ts;
                return cppInnerTS;
            }
        }
        return null;
    }

    public static boolean isCppLanguage(Language<?> lang, boolean allowPrepoc) {
        return lang == CppTokenId.languageC() || lang == CppTokenId.languageCpp()
                || lang == CppTokenId.languageHeader()
                || (allowPrepoc && lang == CppTokenId.languagePreproc());
    }

    public static TokenSequence<FortranTokenId> getFortranTokenSequence(final Document doc, final int offset) {
        InputAttributes ia = (InputAttributes) doc.getProperty(InputAttributes.class);
        boolean truFree = detectFortranFormat(doc);
        ia.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_FREE_FORMAT, truFree, true);
        TokenHierarchy<?> th = doc != null ? TokenHierarchy.get(doc) : null;
        TokenSequence<FortranTokenId> ts = th != null ? getFortranTokenSequence(th, offset) : null;
        return ts;
    }

    public static TokenSequence<FortranTokenId> getFortranTokenSequence(final TokenHierarchy<?> hierarchy, final int offset) {
        if (hierarchy != null) {
            TokenSequence<?> ts = hierarchy.tokenSequence();
            while (ts != null && (offset == 0 || ts.moveNext())) {
                ts.move(offset);
                if (ts.language() == FortranTokenId.languageFortran()) {
                    @SuppressWarnings("unchecked")
                    TokenSequence<FortranTokenId> innerTS = (TokenSequence<FortranTokenId>) ts;
                    return innerTS;
                }
                if (!ts.moveNext() && !ts.movePrevious()) {
                    return null;
                }
                ts = ts.embedded();
            }
        }
        return null;
    }
    public static final boolean FORTRAN_FIXED_FORMAT_VALUE = false;
    public static final boolean FORTRAN_FREE_FORMAT_VALUE = true;

    public static boolean detectFortranFormat(Document doc) {
        CharSequence sequence;
        try {
            sequence = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            return FORTRAN_FIXED_FORMAT_VALUE;
        }
        int column = 0;
        boolean ignoreRestLine = false;
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            if (c == '\n') {
                column = 0;
                ignoreRestLine = false;
                continue;
            }
            if (ignoreRestLine) {
                continue;
            }
            column++;
            switch (column) {
                case 1:
                    if (c == 'C' || c == 'c' || c == '*') {
                        //like to fixed format
                        ignoreRestLine = true;
                        break;
                    } else if (c >= '0' && c <= '9') {
                        //like to fixed format
                        break;
                    } else if (c == ' ') {
                        // undefined format
                        break;
                    } else if (c == '\t') {
                        // undefined format
                        column = 6;
                        break;
                    } else {
                        return FORTRAN_FREE_FORMAT_VALUE;
                    }
                case 2:
                case 3:
                case 4:
                case 5:
                    if (c >= '0' && c <= '9') {
                        //like to fixed format
                        break;
                    } else if (c == ' ') {
                        // undefined format
                        break;
                    } else if (c == '\t') {
                        // undefined format
                        column = 6;
                        break;
                    } else {
                        return FORTRAN_FREE_FORMAT_VALUE;
                    }
                default:
                    break;
            }
        }
        return FORTRAN_FIXED_FORMAT_VALUE;
    }

    public static boolean isCppIdentifier(CharSequence id) {
        if (id == null) {
            return false;
        }

        if (id.length() == 0) {
            return false;
        }

        if (!(isCppIdentifierStart(id.charAt(0)))) {
            return false;
        }

        for (int i = 1; i < id.length(); i++) {
            if (!(isCppIdentifierPart(id.charAt(i)))) {
                return false;
            }
        }
        return getDefatultFilter(true).check(id) == null && getDefatultFilter(false).check(id) == null;
    }

    public static boolean isCppIdentifierStart(char ch) {
        //MS VC also supports $ as start or part of id
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z') || (ch == '_') || (ch == '$');
    }

    public static boolean isCppIdentifierPart(char ch) {
        return ('0' <= ch && ch <= '9') || isCppIdentifierStart(ch);
    }

    public static boolean isCppIdentifierStart(int codePoint) {
        return Character.isJavaIdentifierStart(codePoint);
    }

    public static boolean isCppIdentifierPart(int codePoint) {
        return Character.isJavaIdentifierPart(codePoint);
    }

    public static boolean isFortranIdentifierPart(int codePoint) {
        return Character.isJavaIdentifierPart(codePoint);
    }

    public static CharSequence removeEscapedLF(CharSequence text, boolean escapedLF) {
        if (!escapedLF) {
            return text;
        } else {
            StringBuilder buffer = new StringBuilder();
            int lengthM1 = text.length() - 1;
            for (int i = 0; i <= lengthM1; i++) {
                char c = text.charAt(i);
                boolean append = true;
                if (c == '\\') { // check escaped LF
                    if ((i < lengthM1) && (text.charAt(i + 1) == '\r')) {
                        i++;
                        append = false;
                    }
                    if ((i < lengthM1) && (text.charAt(i + 1) == '\n')) {
                        i++;
                        append = false;
                    }
                }
                if (append) {
                    buffer.append(c);
                }
            }
            return buffer.toString();
        }
    }

    public static boolean isKeyword(String str) {
        try {
            CppTokenId id = CppTokenId.valueOf(str.toUpperCase());
            return id != null
                    && (CppTokenId.KEYWORD_CATEGORY.equals(id.primaryCategory())
                    || CppTokenId.KEYWORD_DIRECTIVE_CATEGORY.equals(id.primaryCategory())
                    || CppTokenId.PREPROCESSOR_KEYWORD_CATEGORY.equals(id.primaryCategory()))
                    || CppTokenId.PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY.equals(id.primaryCategory());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isType(String str) {
        try {
            // replace all spaces
            if (str.contains(" ")) { // NOI18N
                String[] parts = str.split(" "); // NOI18N
                for (String part : parts) {
                    if (isType(part)) {
                        return true;
                    }
                }
            } else {
                CppTokenId id = CppTokenId.valueOf(str.toUpperCase());
                return isType(id);
            }
        } catch (IllegalArgumentException ex) {
            // unknown value
        }
        return false;
    }

    public static boolean isType(CppTokenId id) {
        switch (id) {
            case AUTO:
            case BOOL:
            case CHAR:
            case CONST:
            case DOUBLE:
            case ENUM:
            case EXPORT:
            case FLOAT:
            case INLINE:
            case _INLINE:
            case __INLINE:
            case __INLINE__:
            case INT:
            case LONG:
            case MUTABLE:
            case REGISTER:
            case SHORT:
            case SIGNED:
            case __SIGNED:
            case __SIGNED__:
            case SIZEOF:
            case TYPEDEF:
            case TYPEID:
            case TYPEOF:
            case __TYPEOF:
            case __TYPEOF__:
            case UNSIGNED:
            case __UNSIGNED__:
            case VOID:
            case VOLATILE:
            case WCHAR_T:
            case _BOOL:
            case _COMPLEX:
            case __COMPLEX__:
            case _IMAGINARY:
            case __IMAG__:
            case _INT64:
            case __INT64:
            case __REAL__:
            case __W64:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSeparatorOrOperator(CppTokenId tokenID) {
        String category = tokenID.primaryCategory();
        return CppTokenId.OPERATOR_CATEGORY.equals(category) || CppTokenId.SEPARATOR_CATEGORY.equals(category);
    }
    // filters
    private static Filter<CppTokenId> FILTER_STD_C;
    private static Filter<CppTokenId> FILTER_GCC_C;
    private static Filter<CppTokenId> FILTER_STD_CPP;
    private static Filter<CppTokenId> FILTER_GCC_CPP;
    private static Filter<CppTokenId> FILTER_PREPRPOCESSOR;
    private static Filter<CppTokenId> FILTER_OMP;
    private static Filter<FortranTokenId> FILTER_FORTRAN;

    public static Filter<CppTokenId> getDefatultFilter(boolean cpp) {
        return cpp ? getStdCppFilter() : getStdCFilter();
    }

    public synchronized static Filter<CppTokenId> getPreprocFilter() {
        if (FILTER_PREPRPOCESSOR == null) {
            FILTER_PREPRPOCESSOR = new Filter<CppTokenId>();
            addPreprocKeywords(FILTER_PREPRPOCESSOR);
        }
        return FILTER_PREPRPOCESSOR;
    }

    public synchronized static Filter<CppTokenId> getOmpFilter() {
        if (FILTER_OMP == null) {
            FILTER_OMP = new Filter<CppTokenId>();
            addOmpKeywords(FILTER_OMP);
        }
        return FILTER_OMP;
    }

    public synchronized static Filter<CppTokenId> getStdCFilter() {
        if (FILTER_STD_C == null) {
            FILTER_STD_C = new Filter<CppTokenId>();
            addCommonCCKeywords(FILTER_STD_C);
            addCOnlyKeywords(FILTER_STD_C);
        }
        return FILTER_STD_C;
    }

    public synchronized static Filter<CppTokenId> getGccCFilter() {
        if (FILTER_GCC_C == null) {
            FILTER_GCC_C = new Filter<CppTokenId>();
            addCommonCCKeywords(FILTER_GCC_C);
            addCOnlyKeywords(FILTER_GCC_C);
            addGccOnlyCommonCCKeywords(FILTER_GCC_C);
            //addGccOnlyCOnlyKeywords(FILTER_GCC_C);
        }
        return FILTER_GCC_C;
    }

    public synchronized static Filter<CppTokenId> getStdCppFilter() {
        if (FILTER_STD_CPP == null) {
            FILTER_STD_CPP = new Filter<CppTokenId>();
            addCommonCCKeywords(FILTER_STD_CPP);
            addCppOnlyKeywords(FILTER_STD_CPP);
        }
        return FILTER_STD_CPP;
    }

    public synchronized static Filter<CppTokenId> getGccCppFilter() {
        if (FILTER_GCC_CPP == null) {
            FILTER_GCC_CPP = new Filter<CppTokenId>();
            addCommonCCKeywords(FILTER_GCC_CPP);
            addCppOnlyKeywords(FILTER_GCC_CPP);
            addGccOnlyCommonCCKeywords(FILTER_GCC_CPP);
            addGccOnlyCppOnlyKeywords(FILTER_GCC_CPP);
        }
        return FILTER_GCC_CPP;
    }

    public synchronized static Filter<FortranTokenId> getFortranFilter() {
        if (FILTER_FORTRAN == null) {
            FILTER_FORTRAN = new Filter<FortranTokenId>();
            addFortranKeywords(FILTER_FORTRAN);
        }
        return FILTER_FORTRAN;
    }

    ////////////////////////////////////////////////////////////////////////////
    // help methods
    private static void addPreprocKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.PREPROCESSOR_IF,
            CppTokenId.PREPROCESSOR_IFDEF,
            CppTokenId.PREPROCESSOR_IFNDEF,
            CppTokenId.PREPROCESSOR_ELSE,
            CppTokenId.PREPROCESSOR_ELIF,
            CppTokenId.PREPROCESSOR_ENDIF,
            CppTokenId.PREPROCESSOR_DEFINE,
            CppTokenId.PREPROCESSOR_UNDEF,
            CppTokenId.PREPROCESSOR_INCLUDE,
            CppTokenId.PREPROCESSOR_INCLUDE_NEXT,
            CppTokenId.PREPROCESSOR_LINE,
            CppTokenId.PREPROCESSOR_IDENT,
            CppTokenId.PREPROCESSOR_PRAGMA,
            CppTokenId.PREPROCESSOR_WARNING,
            CppTokenId.PREPROCESSOR_ERROR,};
        addToFilter(ids, filterToModify);
    }

    private static void addOmpKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.PRAGMA_OMP_START,
            CppTokenId.PRAGMA_OMP_PARALLEL,
            CppTokenId.PRAGMA_OMP_SECTIONS,
            CppTokenId.PRAGMA_OMP_NOWAIT,
            CppTokenId.PRAGMA_OMP_ORDERED,
            CppTokenId.PRAGMA_OMP_SCHEDULE,
            CppTokenId.PRAGMA_OMP_DYNAMIC,
            CppTokenId.PRAGMA_OMP_GUIDED,
            CppTokenId.PRAGMA_OMP_RUNTIME,
            CppTokenId.PRAGMA_OMP_SECTION,
            CppTokenId.PRAGMA_OMP_SINGLE,
            CppTokenId.PRAGMA_OMP_MASTER,
            CppTokenId.PRAGMA_OMP_CRITICAL,
            CppTokenId.PRAGMA_OMP_BARRIER,
            CppTokenId.PRAGMA_OMP_ATOMIC,
            CppTokenId.PRAGMA_OMP_FLUSH,
            CppTokenId.PRAGMA_OMP_THREADPRIVATE,
            CppTokenId.PRAGMA_OMP_PRIVATE,
            CppTokenId.PRAGMA_OMP_FIRSTPRIVATE,
            CppTokenId.PRAGMA_OMP_LASTPRIVATE,
            CppTokenId.PRAGMA_OMP_SHARED,
            CppTokenId.PRAGMA_OMP_NONE,
            CppTokenId.PRAGMA_OMP_REDUCTION,
            CppTokenId.PRAGMA_OMP_COPYIN,
            CppTokenId.PRAGMA_OMP_TASK,
            CppTokenId.PRAGMA_OMP_TASKWAIT,
            CppTokenId.PRAGMA_OMP_COLLAPSE,
            CppTokenId.PRAGMA_OMP_COPYPRIVATE,
            CppTokenId.PRAGMA_OMP_DEFAULT,
            CppTokenId.PRAGMA_OMP_STATIC,
            CppTokenId.PRAGMA_OMP_IF,
            CppTokenId.PRAGMA_OMP_FOR,
            CppTokenId.PRAGMA_OMP_AUTO,
            CppTokenId.PRAGMA_OMP_NUM_THREADS,};
        addToFilter(ids, filterToModify);
    }

    private static void addCommonCCKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.AUTO,
            CppTokenId.BREAK,
            CppTokenId.CASE,
            CppTokenId.CHAR,
            CppTokenId.CONST,
            CppTokenId.CONTINUE,
            CppTokenId.DEFAULT,
            CppTokenId.DO,
            CppTokenId.DOUBLE,
            CppTokenId.ELSE,
            CppTokenId.ENUM,
            CppTokenId.EXTERN,
            CppTokenId.FLOAT,
            CppTokenId.FOR,
            CppTokenId.__FUNC__,
            CppTokenId.GOTO,
            CppTokenId.IF,
            CppTokenId.INT,
            CppTokenId.LONG,
            CppTokenId.REGISTER,
            CppTokenId.RETURN,
            CppTokenId.SHORT,
            CppTokenId.SIGNED,
            CppTokenId.SIZEOF,
            CppTokenId.STATIC,
            CppTokenId.STRUCT,
            CppTokenId.SWITCH,
            CppTokenId.TYPEDEF,
            CppTokenId.UNION,
            CppTokenId.UNSIGNED,
            CppTokenId.VOID,
            CppTokenId.VOLATILE,
            CppTokenId.WHILE,};
        addToFilter(ids, filterToModify);
    }

    private static void addCppOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.ASM, // gcc and C++
            CppTokenId.BOOL, // C++
            CppTokenId.CATCH, //C++
            CppTokenId.CLASS, //C++
            CppTokenId.CONST_CAST, // C++
            CppTokenId.DELETE, // C++
            CppTokenId.DYNAMIC_CAST, // C++
            CppTokenId.EXPLICIT, // C++
            CppTokenId.EXPORT, // C++
            CppTokenId.FINALLY, //C++
            CppTokenId.FRIEND, // C++
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId.MUTABLE, // C++
            CppTokenId.NAMESPACE, //C++
            CppTokenId.NEW, //C++
            CppTokenId.OPERATOR, // C++
            CppTokenId.PRIVATE, //C++
            CppTokenId.PROTECTED, //C++
            CppTokenId.PUBLIC, // C++
            CppTokenId.REINTERPRET_CAST, //C++
            CppTokenId.STATIC_CAST, // C++
            CppTokenId.TEMPLATE, //C++
            CppTokenId.THIS, // C++
            CppTokenId.THROW, //C++
            CppTokenId.TRY, // C++
            CppTokenId.TYPEID, //C++
            CppTokenId.TYPENAME, //C++
            CppTokenId.TYPEOF, // gcc, C++
            CppTokenId.USING, //C++
            CppTokenId.VIRTUAL, //C++
            CppTokenId.WCHAR_T, // C++

            CppTokenId.TRUE, // C++
            CppTokenId.FALSE, // C++
        };
        addToFilter(ids, filterToModify);
    }

    private static void addCOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId.RESTRICT, // C
            CppTokenId._BOOL, // C
            CppTokenId._COMPLEX, // C
            CppTokenId._IMAGINARY, // C
        };
        addToFilter(ids, filterToModify);
    }

    private static void addGccOnlyCommonCCKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.ASM,
            CppTokenId.__ALIGNOF__,
            CppTokenId.__ASM,
            CppTokenId.__ASM__,
            CppTokenId.__ATTRIBUTE__,
            CppTokenId.__ATTRIBUTE,
            CppTokenId.__COMPLEX__,
            CppTokenId.__CONST,
            CppTokenId.__CONST__,
            CppTokenId.__IMAG__,
            CppTokenId.INLINE,
            CppTokenId.__INLINE,
            CppTokenId.__REAL__,
            CppTokenId.__RESTRICT,
            CppTokenId.__SIGNED,
            CppTokenId.__SIGNED__,
            CppTokenId.TYPEOF,
            CppTokenId.__TYPEOF,
            CppTokenId.__TYPEOF__,
            CppTokenId.__VOLATILE,
            CppTokenId.__VOLATILE__,
            CppTokenId.__THREAD,
            CppTokenId.__UNUSED__,};
        addToFilter(ids, filterToModify);
    }

    /*
    private static void addGccOnlyCOnlyKeywords(Filter<CppTokenId> filterToModify) {
    // no C only tokens in gnu c
    }
     */
    private static void addGccOnlyCppOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[]{
            CppTokenId.ALIGNOF,
            CppTokenId._ASM,
            CppTokenId._INLINE,
            CppTokenId.PASCAL,
            CppTokenId._PASCAL,
            CppTokenId.__PASCAL,
            CppTokenId.__UNSIGNED__,
            CppTokenId._CDECL,
            CppTokenId.__CDECL,
            CppTokenId._DECLSPEC,
            CppTokenId.__DECLSPEC,
            CppTokenId.__EXTENSION__,
            CppTokenId._FAR,
            CppTokenId.__FAR,
            CppTokenId._INT64,
            CppTokenId.__INT64,
            CppTokenId.__INTERRUPT,
            CppTokenId._NEAR,
            CppTokenId.__NEAR,
            CppTokenId._STDCALL,
            CppTokenId.__STDCALL,
            CppTokenId.__W64,};
        addToFilter(ids, filterToModify);
    }

    private static void addFortranKeywords(Filter<FortranTokenId> filterToModify) {
        FortranTokenId[] ids = new FortranTokenId[]{
            // Keywords
            FortranTokenId.KW_ALLOCATABLE,
            FortranTokenId.KW_ALLOCATE,
            FortranTokenId.KW_APOSTROPHE,
            FortranTokenId.KW_ASSIGNMENT,
            FortranTokenId.KW_ASSOCIATE,
            FortranTokenId.KW_ASYNCHRONOUS,
            FortranTokenId.KW_BACKSPACE,
            FortranTokenId.KW_BIND,
            FortranTokenId.KW_BLOCK,
            FortranTokenId.KW_BLOCKDATA,
            FortranTokenId.KW_CALL,
            FortranTokenId.KW_CASE,
            FortranTokenId.KW_CHARACTER,
            FortranTokenId.KW_CLASS,
            FortranTokenId.KW_CLOSE,
            FortranTokenId.KW_COMMON,
            FortranTokenId.KW_COMPLEX,
            FortranTokenId.KW_CONTAINS,
            FortranTokenId.KW_CONTINUE,
            FortranTokenId.KW_CYCLE,
            FortranTokenId.KW_DATA,
            FortranTokenId.KW_DEALLOCATE,
            FortranTokenId.KW_DEFAULT,
            FortranTokenId.KW_DIMENSION,
            FortranTokenId.KW_DO,
            FortranTokenId.KW_DOUBLE,
            FortranTokenId.KW_DOUBLEPRECISION,
            FortranTokenId.KW_ELEMENTAL,
            FortranTokenId.KW_ELSE,
            FortranTokenId.KW_ELSEIF,
            FortranTokenId.KW_ELSEWHERE,
            FortranTokenId.KW_END,
            FortranTokenId.KW_ENDASSOCIATE,
            FortranTokenId.KW_ENDBLOCK,
            FortranTokenId.KW_ENDBLOCKDATA,
            FortranTokenId.KW_ENDDO,
            FortranTokenId.KW_ENDENUM,
            FortranTokenId.KW_ENDFILE,
            FortranTokenId.KW_ENDFORALL,
            FortranTokenId.KW_ENDFUNCTION,
            FortranTokenId.KW_ENDIF,
            FortranTokenId.KW_ENDINTERFACE,
            FortranTokenId.KW_ENDMAP,
            FortranTokenId.KW_ENDMODULE,
            FortranTokenId.KW_ENDPROGRAM,
            FortranTokenId.KW_ENDSELECT,
            FortranTokenId.KW_ENDSTRUCTURE,
            FortranTokenId.KW_ENDSUBROUTINE,
            FortranTokenId.KW_ENDTYPE,
            FortranTokenId.KW_ENDUNION,
            FortranTokenId.KW_ENDWHERE,
            FortranTokenId.KW_ENDWHILE,
            FortranTokenId.KW_ENTRY,
            FortranTokenId.KW_ENUM,
            FortranTokenId.KW_ENUMERATOR,
            FortranTokenId.KW_EQUIVALENCE,
            FortranTokenId.KW_EXIT,
            FortranTokenId.KW_EXTERNAL,
            FortranTokenId.KW_FLUSH,
            FortranTokenId.KW_FORALL,
            FortranTokenId.KW_FORMAT,
            FortranTokenId.KW_FUNCTION,
            FortranTokenId.KW_GO,
            FortranTokenId.KW_GOTO,
            FortranTokenId.KW_IF,
            FortranTokenId.KW_IMPLICIT,
            FortranTokenId.KW_IN,
            FortranTokenId.KW_INCLUDE,
            FortranTokenId.KW_INOUT,
            FortranTokenId.KW_INQUIRE,
            FortranTokenId.KW_INTEGER,
            FortranTokenId.KW_INTENT,
            FortranTokenId.KW_INTERFACE,
            FortranTokenId.KW_INTRINSIC,
            FortranTokenId.KW_KIND,
            FortranTokenId.KW_LEN,
            FortranTokenId.KW_LOGICAL,
            FortranTokenId.KW_MAP,
            FortranTokenId.KW_MODULE,
            FortranTokenId.KW_NAMELIST,
            FortranTokenId.KW_NONE,
            FortranTokenId.KW_NULLIFY,
            FortranTokenId.KW_ONLY,
            FortranTokenId.KW_OPEN,
            FortranTokenId.KW_OPERATOR,
            FortranTokenId.KW_OPTIONAL,
            FortranTokenId.KW_OUT,
            FortranTokenId.KW_PARAMETER,
            FortranTokenId.KW_POINTER,
            FortranTokenId.KW_PRECISION,
            FortranTokenId.KW_PRINT,
            FortranTokenId.KW_PRIVATE,
            FortranTokenId.KW_PROCEDURE,
            FortranTokenId.KW_PROGRAM,
            FortranTokenId.KW_PROTECTED,
            FortranTokenId.KW_PUBLIC,
            FortranTokenId.KW_PURE,
            FortranTokenId.KW_QUOTE,
            FortranTokenId.KW_READ,
            FortranTokenId.KW_REAL,
            FortranTokenId.KW_RECURSIVE,
            FortranTokenId.KW_RESULT,
            FortranTokenId.KW_RETURN,
            FortranTokenId.KW_REWIND,
            FortranTokenId.KW_SAVE,
            FortranTokenId.KW_SELECT,
            FortranTokenId.KW_SELECTCASE,
            FortranTokenId.KW_SELECTTYPE,
            FortranTokenId.KW_SEQUENCE,
            FortranTokenId.KW_STAT,
            FortranTokenId.KW_STOP,
            FortranTokenId.KW_STRUCTURE,
            FortranTokenId.KW_SUBROUTINE,
            FortranTokenId.KW_TARGET,
            FortranTokenId.KW_THEN,
            FortranTokenId.KW_TO,
            FortranTokenId.KW_TYPE,
            FortranTokenId.KW_UNION,
            FortranTokenId.KW_USE,
            FortranTokenId.KW_VALUE,
            FortranTokenId.KW_VOLATILE,
            FortranTokenId.KW_WAIT,
            FortranTokenId.KW_WHERE,
            FortranTokenId.KW_WHILE,
            FortranTokenId.KW_WRITE,
            // Keyword C Extensions
            FortranTokenId.KW_INT,
            FortranTokenId.KW_SHORT,
            FortranTokenId.KW_LONG,
            FortranTokenId.KW_SIGNED,
            FortranTokenId.KW_UNSIGNED,
            FortranTokenId.KW_SIZE_T,
            FortranTokenId.KW_INT8_T,
            FortranTokenId.KW_INT16_T,
            FortranTokenId.KW_INT32_T,
            FortranTokenId.KW_INT64_T,
            FortranTokenId.KW_INT_LEAST8_T,
            FortranTokenId.KW_INT_LEAST16_T,
            FortranTokenId.KW_INT_LEAST32_T,
            FortranTokenId.KW_INT_LEAST64_T,
            FortranTokenId.KW_INT_FAST8_T,
            FortranTokenId.KW_INT_FAST16_T,
            FortranTokenId.KW_INT_FAST32_T,
            FortranTokenId.KW_INT_FAST64_T,
            FortranTokenId.KW_INTMAX_T,
            FortranTokenId.KW_INTPTR_T,
            FortranTokenId.KW_FLOAT,
            FortranTokenId.KW__COMPLEX,
            FortranTokenId.KW__BOOL,
            FortranTokenId.KW_CHAR,
            FortranTokenId.KW_BOOL,
            // Keyword Operator
            FortranTokenId.KWOP_EQ,
            FortranTokenId.KWOP_NE,
            FortranTokenId.KWOP_LT,
            FortranTokenId.KWOP_LE,
            FortranTokenId.KWOP_GT,
            FortranTokenId.KWOP_GE,
            FortranTokenId.KWOP_AND,
            FortranTokenId.KWOP_OR,
            FortranTokenId.KWOP_NOT,
            FortranTokenId.KWOP_EQV,
            FortranTokenId.KWOP_NEQV,
            FortranTokenId.KWOP_TRUE,
            FortranTokenId.KWOP_FALSE
        };
        addToFilter(ids, filterToModify);
    }

    private static void addToFilter(CppTokenId[] ids, Filter<CppTokenId> filterToModify) {
        for (CppTokenId id : ids) {
            assert id.fixedText() != null : "id " + id + " must have fixed text";
            filterToModify.addMatch(id.fixedText(), id);
        }
    }

    private static void addToFilter(FortranTokenId[] ids, Filter<FortranTokenId> filterToModify) {
        for (FortranTokenId id : ids) {
            assert id.fixedText() != null : "id " + id + " must have fixed text";
            filterToModify.addMatch(id.fixedText(), id);
        }
    }
}
