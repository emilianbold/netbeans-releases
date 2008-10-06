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
package org.netbeans.cnd.api.lexer;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Vladirmir Voskresensky
 */
public final class CndLexerUtilities {

    public static String C_MIME_TYPE = "text/x-c";// NOI18N
    public static String CPLUSPLUS_MIME_TYPE = "text/x-c++";    // NOI18N
    public static String PREPROC_MIME_TYPE = "text/x-cpp-preprocessor";// NOI18N
    public static String LEXER_FILTER = "lexer-filter"; // NOI18N

    private CndLexerUtilities() {
    }

    public static TokenSequence<CppTokenId> getCppTokenSequence(final JTextComponent component, final int offset) {
        Document doc = component.getDocument();
        return getCppTokenSequence(doc, offset);
    }

    public static Language<CppTokenId> getLanguage(String mime) {
        if (C_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageC();
        } else if (CPLUSPLUS_MIME_TYPE.equals(mime)) {
            return CppTokenId.languageCpp();
        }
        return null;
    }

    public static Language<CppTokenId> getLanguage(final Document doc) {
        // try from property
        Language lang = (Language) doc.getProperty(Language.class);
        if (lang == null || (lang != CppTokenId.languageC() &&
                             lang != CppTokenId.languageCpp())) {
            lang = getLanguage((String) doc.getProperty("mimeType")); // NOI18N
        }
        @SuppressWarnings("unchecked")
        Language<CppTokenId> out = (Language<CppTokenId>)lang;
        return out;
    }

    public static TokenSequence<CppTokenId> getCppTokenSequence(final Document doc, final int offset) {
        TokenHierarchy th = doc != null ? TokenHierarchy.get(doc) : null;
        TokenSequence<CppTokenId> ts = th != null ? getCppTokenSequence(th, offset) : null;
        return ts;
    }

    @SuppressWarnings("unchecked")
    public static TokenSequence<CppTokenId> getCppTokenSequence(final TokenHierarchy hierarchy, final int offset) {
        if (hierarchy != null) {
            TokenSequence<?> ts = hierarchy.tokenSequence();
            while(ts != null && (offset == 0 || ts.moveNext())) {
                ts.move(offset);
                if (ts.language() == CppTokenId.languageC() ||
                        ts.language() == CppTokenId.languageCpp() ||
                        ts.language() == CppTokenId.languagePreproc()) {
                    return (TokenSequence<CppTokenId>)ts;
                }
                if (!ts.moveNext() && !ts.movePrevious()) {
                    return null;
                }
                ts = ts.embedded();
            }
        }
        return null;
    }

    public static boolean isCppIdentifierStart(char ch) {
        return Character.isJavaIdentifierStart(ch);
    }

    public static boolean isCppIdentifierStart(int codePoint) {
        return Character.isJavaIdentifierStart(codePoint);
    }

    public static boolean isCppIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
    }

    public static boolean isCppIdentifierPart(int codePoint) {
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
                    if ((i < lengthM1) && (text.charAt(i+1) == '\r')) {
                        i++;
                        append = false;
                    }
                    if ((i < lengthM1) && (text.charAt(i+1) == '\n')) {
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
            CppTokenId.PREPROCESSOR_ERROR,
        };
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
            CppTokenId.WHILE,
        };
        addToFilter(ids, filterToModify);
    }

    private static  void addCppOnlyKeywords(Filter<CppTokenId> filterToModify) {
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
        CppTokenId[] ids = new CppTokenId[] {
            CppTokenId.INLINE, // gcc, C++, now in C also
            CppTokenId.RESTRICT, // C
            CppTokenId._BOOL, // C
            CppTokenId._COMPLEX, // C
            CppTokenId._IMAGINARY, // C
        };
        addToFilter(ids, filterToModify);
    }

    private static void addGccOnlyCommonCCKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[] {
            CppTokenId.ASM,
            CppTokenId.__ALIGNOF__,
            CppTokenId.__ASM,
            CppTokenId.__ASM__,
            CppTokenId.__ATTRIBUTE__,
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
            CppTokenId.__UNUSED__,
        };
        addToFilter(ids, filterToModify);
    }

    /*
    private static void addGccOnlyCOnlyKeywords(Filter<CppTokenId> filterToModify) {
        // no C only tokens in gnu c
    }
    */

    private static void addGccOnlyCppOnlyKeywords(Filter<CppTokenId> filterToModify) {
        CppTokenId[] ids = new CppTokenId[] {
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
            CppTokenId.__W64,
        };
        addToFilter(ids, filterToModify);
    }

    private static void addToFilter(CppTokenId[] ids, Filter<CppTokenId> filterToModify) {
        for (CppTokenId id : ids) {
            assert id.fixedText() != null : "id " + id + " must have fixed text";
            filterToModify.addMatch(id.fixedText(), id);
        }
    }
}
