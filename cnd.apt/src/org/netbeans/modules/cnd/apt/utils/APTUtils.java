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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.apt.utils;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.impl.support.APTCommentToken;
import org.netbeans.modules.cnd.apt.impl.support.APTConstTextToken;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroParamExpansion;
import org.netbeans.modules.cnd.apt.impl.support.APTTestToken;
import org.netbeans.modules.cnd.apt.impl.support.MacroExpandedToken;
import org.netbeans.modules.cnd.apt.impl.support.lang.APTBaseLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * APT utilities
 * @author Vladimir Voskresensky
 */
public class APTUtils {
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.apt"); // NOI18N

    static {
        // command line param has priority for logging
        String level = System.getProperty("org.netbeans.modules.cnd.apt.level"); // NOI18N
        // do not change it
        if (level == null) {
            // command line param has priority for logging
            if (APTTraceFlags.TRACE_APT | APTTraceFlags.TRACE_APT_LEXER) {
                LOG.setLevel(Level.ALL);
            } else {
                LOG.setLevel(Level.SEVERE);
            }
        } else {
            try {
                LOG.setLevel(Level.parse(level));
            } catch (IllegalArgumentException e) {
                // skip
            }
        }
    }

    /** Creates a new instance of APTUtils */
    private APTUtils() {
    }

    public static int hash(int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }

    public static int hash(List<?> list) {
        if (list == null) {
            return 0;
        }
        int hashCode = 1;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Object obj = list.get(i);
            hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
        }
        return hash(hashCode);
    }

    public static boolean equalArrayLists(List<?> l1, List<?> l2) {
        if (l1 != l2) {
            if (l1 == null || l2 == null) {
                return false;
            } else {
                int n1 = l1.size();
                int n2 = l2.size();
                if (n1 != n2) {
                    return false;
                }
                for (int i = 0; i < n1; i++) {
                    if (!l1.get(i).equals(l2.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return true;
        }
    }

    public static void setTokenText(APTToken _token, char buf[], int start, int count) {
        if (_token instanceof APTBaseToken) {
            _token.setTextID(CharSequenceKey.create(buf, start, count));
        } else if (_token instanceof APTCommentToken) {
            // no need to set text in comment token, but set text len
            ((APTCommentToken)_token).setTextLength(count);
        } else if (_token instanceof APTConstTextToken) {
            // no need to set text in comment token
        } else {
            System.err.printf("unexpected token %s while assigning text %s", _token, new String(buf, start, count));
            _token.setText(new String(buf, start, count));
        }
    }

    public static APTToken createAPTToken(int type) {
        // Preprocessor tokens can be made constText, but we can get '#define' and '# define'
        // which have different text. so for now they are treated as usual tokens
        if (isPreprocessorToken(type)) {
            return APTTraceFlags.USE_APT_TEST_TOKEN ? (APTToken)new APTTestToken() : new APTBaseToken();
        }
        switch (type) {
            // IDs
            case APTTokenTypes.ID:
            case APTTokenTypes.ID_DEFINED:
                // Strings and chars
            case APTTokenTypes.STRING_LITERAL:
            case APTTokenTypes.CHAR_LITERAL:
                // Numbers
            case APTTokenTypes.DECIMALINT:
            case APTTokenTypes.HEXADECIMALINT:
            case APTTokenTypes.FLOATONE:
            case APTTokenTypes.FLOATTWO:
            case APTTokenTypes.OCTALINT:
            case APTTokenTypes.NUMBER:
                // Include strings
            case APTTokenTypes.INCLUDE_STRING:
            case APTTokenTypes.SYS_INCLUDE_STRING:
                //Other
            case APTTokenTypes.END_PREPROC_DIRECTIVE:
                return APTTraceFlags.USE_APT_TEST_TOKEN ? (APTToken)new APTTestToken() : new APTBaseToken();
                
                // Comments
            case APTTokenTypes.CPP_COMMENT:
            case APTTokenTypes.COMMENT:
                return new APTCommentToken();
                
            default: /*assert(APTConstTextToken.constText[type] != null) : "Do not know text for constText token of type " + type;  // NOI18N*/
                return new APTConstTextToken();
        }
    }

    public static APTToken getLastToken(TokenStream ts) {
        APTToken last = null;
        try {
            for (APTToken token = (APTToken) ts.nextToken(); !APTUtils.isEOF(token);) {
                assert (token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                last = token;
                token = (APTToken) ts.nextToken();
            }
        } catch (TokenStreamException ex) {
            // ignore
        }
        return last;
    }

    public static String debugString(TokenStream ts) {
        // use simple stringize
        return stringize(ts, false);
    }
    
    public static String toString(TokenStream ts) {
        StringBuilder retValue = new StringBuilder();
        try {
            for (Token token = ts.nextToken();!isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.toString());
                
                token=ts.nextToken();
                
                if (!isEOF(token)) {
                    retValue.append(" "); // NOI18N
                }
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.SEVERE, "error on converting token stream to text\n{0}", new Object[] { ex }); // NOI18N
        }
        return retValue.toString();
    }
    
    public static String stringize(TokenStream ts, boolean inIncludeDirective) {
        StringBuilder retValue = new StringBuilder();
        try {
            for (APTToken token = (APTToken)ts.nextToken();!isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.getTextID());
                APTToken next =(APTToken)ts.nextToken();
                if (!isEOF(next) && !inIncludeDirective) { // disable for IZ#124635
                    // if tokens were without spaces => no space
                    // if were with spaces => insert only one space
                    retValue.append(next.getOffset() == token.getEndOffset() ? "" : ' ');// NOI18N
                }
                token = next;
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.SEVERE, "error on stringizing token stream\n{0}", new Object[] { ex }); // NOI18N
        }
        return retValue.toString();
    }
    
    public static String macros2String(Map<CharSequence/*getTokenTextKey(token)*/, APTMacro> macros) {
        StringBuilder retValue = new StringBuilder();
        retValue.append("MACROS (sorted "+macros.size()+"):\n"); // NOI18N
        List<CharSequence> macrosSorted = new ArrayList<CharSequence>(macros.keySet());
        Collections.sort(macrosSorted, CharSequenceKey.Comparator);
        for (CharSequence key : macrosSorted) {
            APTMacro macro = macros.get(key);
            assert(macro != null);
            retValue.append(macro);
            retValue.append("'\n"); // NOI18N
        }
        return retValue.toString();
    }
    
    public static CharSequence includes2String(List<IncludeDirEntry> includePaths) {
        StringBuilder retValue = new StringBuilder();
        for (Iterator<IncludeDirEntry> it = includePaths.iterator(); it.hasNext();) {
            IncludeDirEntry path = it.next();
            retValue.append(path.getAsSharedCharSequence());
            if (it.hasNext()) {
                retValue.append('\n'); // NOI18N
            }
        }
        return retValue;
    }
    
    public static boolean isPreprocessorToken(Token token) {
        assert (token != null);
        return isPreprocessorToken(token.getType());
    }
    
    public static boolean isPreprocessorToken(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.PREPROC_DIRECTIVE:
            case APTTokenTypes.INCLUDE:
            case APTTokenTypes.INCLUDE_NEXT:
            case APTTokenTypes.DEFINE:
            case APTTokenTypes.UNDEF:
            case APTTokenTypes.IFDEF:
            case APTTokenTypes.IFNDEF:
            case APTTokenTypes.IF:
            case APTTokenTypes.ELIF:
            case APTTokenTypes.ELSE:
            case APTTokenTypes.ENDIF:
            case APTTokenTypes.PRAGMA:
            case APTTokenTypes.LINE:
            case APTTokenTypes.ERROR:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isID(Token token) {
        return token != null && token.getType() == APTTokenTypes.ID;
    }

    public static boolean isInt(Token token) {
        if (token != null) {
            switch (token.getType()) {
                case APTTokenTypes.DECIMALINT:
                case APTTokenTypes.HEXADECIMALINT:
                case APTTokenTypes.OCTALINT:
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isEOF(Token token) {
        assert (token != null);
        return token == null || isEOF(token.getType());
    }
    
    public static boolean isEOF(int ttype) {
        return ttype == APTTokenTypes.EOF;
    }
    
    public static boolean isVaArgsToken(APTToken token) {
        return token != null && token.getTextID().equals(VA_ARGS_TOKEN.getTextID());
    }
    
    public static boolean isStartConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isStartOrSwitchConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
            case APT.Type.ELIF:
            case APT.Type.ELSE:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isEndCondition(Token token) {
        return isEndCondition(token.getType());
    }
    
    public static boolean isEndCondition(int/*APTTokenTypes*/ ttype) {
        switch (ttype) {
            case APTTokenTypes.ELIF:
            case APTTokenTypes.ELSE:
            case APTTokenTypes.ENDIF:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEndConditionNode(int/*APT.Type*/ ntype) {
        switch (ntype) {
            case APT.Type.ELIF:
            case APT.Type.ELSE:
            case APT.Type.ENDIF:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isCommentToken(Token token) {
        assert (token != null);
        return isCommentToken(token.getType());
    }
    
    public static boolean isCommentToken(int ttype) {
        switch (ttype) {
            case APTTokenTypes.COMMENT:
            case APTTokenTypes.CPP_COMMENT:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isOpenBracket(Token token) {
        assert (token != null);
        return isOpenBracket(token.getType());
    }
    
    public static boolean isOpenBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.LCURLY:
            case APTTokenTypes.LPAREN:
            case APTTokenTypes.LSQUARE:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isCloseBracket(Token token) {
        assert (token != null);
        return isCloseBracket(token.getType());
    }
    
    public static boolean isCloseBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.RCURLY:
            case APTTokenTypes.RPAREN:
            case APTTokenTypes.RSQUARE:
                return true;
            default:
                return false;
        }
    }
    
    public static int getMatchBracket(int ttype) {
        switch (ttype) {
            case APTTokenTypes.RCURLY:
                return APTTokenTypes.LCURLY;
            case APTTokenTypes.RPAREN:
                return APTTokenTypes.LPAREN;
            case APTTokenTypes.RSQUARE:
                return APTTokenTypes.LSQUARE;
            case APTTokenTypes.LCURLY:
                return APTTokenTypes.RCURLY;
            case APTTokenTypes.LPAREN:
                return APTTokenTypes.RPAREN;
            case APTTokenTypes.LSQUARE:
                return APTTokenTypes.RSQUARE;
            default:
                return APTUtils.EOF_TOKEN.EOF_TYPE;
        }
    }    
    
    public static boolean isEndDirectiveToken(int ttype) {
        switch(ttype) {
            case APTTokenTypes.END_PREPROC_DIRECTIVE:
            case APTTokenTypes.EOF:
                return true;
        }
        return false;
    }

    public static boolean isMacroExpandedToken(Token token) {
        if(token instanceof MacroExpandedToken) {
            return true;
        } else if (token instanceof APTBaseLanguageFilter.FilterToken) {
            return isMacroExpandedToken(((APTBaseLanguageFilter.FilterToken)token).getOriginalToken());
        }
        return false;
    }

    public static boolean isMacroParamExpandedToken(Token token) {
        if (token instanceof APTMacroParamExpansion) {
            return true;
        } else if (token instanceof MacroExpandedToken) {
            return isMacroParamExpandedToken(((MacroExpandedToken) token).getTo());
        } else if (token instanceof APTBaseLanguageFilter.FilterToken) {
            return isMacroParamExpandedToken(((APTBaseLanguageFilter.FilterToken) token).getOriginalToken());
        }
        return false;
    }

    public static APTToken getExpandedToken(APTToken token) {
        if (token instanceof APTMacroParamExpansion) {
            return getExpandedToken(((APTMacroParamExpansion) token).getOriginal());
        } else if (token instanceof MacroExpandedToken) {
            return getExpandedToken(((MacroExpandedToken) token).getTo());
        } else if (token instanceof APTBaseLanguageFilter.FilterToken) {
            return getExpandedToken(((APTBaseLanguageFilter.FilterToken) token).getOriginalToken());
        }
        return token;
    }

    public static boolean areAdjacent(APTToken left, APTToken right) {
        while (left instanceof MacroExpandedToken && right instanceof MacroExpandedToken) {
            left = ((MacroExpandedToken) left).getTo();
            right = ((MacroExpandedToken) right).getTo();
        }
//        if (left instanceof APTToken && right instanceof APTToken) {
        return (left).getEndOffset() == (right).getOffset();
//        } else {
//            return left.getLine() == right.getLine()
//                    && left.getColumn() + left.getText().length() == right.getColumn();
//        }
    }

    public static List<APTToken> toList(TokenStream ts) {
        ArrayList<APTToken> tokens = new ArrayList<APTToken>(1024);
        try {
            APTToken token = (APTToken) ts.nextToken();
            while (!isEOF(token)) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                tokens.add(token);
                token = (APTToken) ts.nextToken();
            }
        } catch (TokenStreamException ex) {
            LOG.log(Level.INFO, "error on converting token stream to list", ex.getMessage()); // NOI18N
        }
        tokens.trimToSize();
        return tokens;
    }
    
    public static Object getTextKey(String text) {
        assert (text != null);
        assert (text.length() > 0);
        // now use text as is, but it will be faster to use textID
        return text;
    }
    
    public static APTToken createAPTToken(APTToken token, int ttype) {
        APTToken newToken;
        if (APTTraceFlags.USE_APT_TEST_TOKEN) {
            newToken = new APTTestToken(token, ttype);
        } else {
            newToken = new APTBaseToken(token, ttype);
        }
        return newToken;
    }
    
    public static APTToken createAPTToken(APTToken token) {
        return createAPTToken(token, token.getType());
    }
    
    public static APTToken createAPTToken() {
        APTToken newToken;
        if (APTTraceFlags.USE_APT_TEST_TOKEN) {
            newToken = new APTTestToken();
        } else {
            newToken = new APTBaseToken();
        }
        return newToken;
    }
    
    public static final APTToken VA_ARGS_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final APTToken EMPTY_ID_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final APTToken COMMA_TOKEN; // support ELLIPSIS for IZ#83949 in macros
    public static final List<APTToken> DEF_MACRO_BODY; //support "1" as content of #defined tokens without body IZ#122091
    static {
        VA_ARGS_TOKEN = createAPTToken();
        VA_ARGS_TOKEN.setType(APTTokenTypes.ID);
        VA_ARGS_TOKEN.setText("__VA_ARGS__"); // NOI18N
        
        EMPTY_ID_TOKEN = createAPTToken();
        EMPTY_ID_TOKEN.setType(APTTokenTypes.ID);
        EMPTY_ID_TOKEN.setText(""); // NOI18N        

        COMMA_TOKEN = createAPTToken(APTTokenTypes.COMMA);
        COMMA_TOKEN.setType(APTTokenTypes.COMMA);
        COMMA_TOKEN.setText(","); // NOI18N             
        
        APTToken token = createAPTToken();
        token.setType(APTTokenTypes.NUMBER);
        token.setText("1"); // NOI18N
        DEF_MACRO_BODY = new ArrayList<APTToken>(1);
        DEF_MACRO_BODY.add(token);
    }
    
    public static final APTToken EOF_TOKEN = new APTEOFToken();
    
    public static final TokenStream EMPTY_STREAM = new TokenStream() {
        public Token nextToken() throws TokenStreamException {
            return EOF_TOKEN;
        }
    };
    
    private static final class APTEOFToken extends APTTokenAbstact {
        public APTEOFToken() {
        }
        
        @Override
        public int getOffset() {
            throw new UnsupportedOperationException("getOffset must not be used"); // NOI18N
        }
        
        @Override
        public void setOffset(int o) {
            throw new UnsupportedOperationException("setOffset must not be used"); // NOI18N
        }
        
        @Override
        public int getEndOffset() {
            throw new UnsupportedOperationException("getEndOffset must not be used"); // NOI18N
        }
        
        @Override
        public void setEndOffset(int o) {
            throw new UnsupportedOperationException("setEndOffset must not be used"); // NOI18N
        }
        
        @Override
        public CharSequence getTextID() {
            throw new UnsupportedOperationException("getTextID must not be used"); // NOI18N
        }
        
        @Override
        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("setTextID must not be used"); // NOI18N
        }
        
        @Override
        public int getEndColumn() {
            throw new UnsupportedOperationException("getEndColumn must not be used"); // NOI18N
        }
        
        @Override
        public void setEndColumn(int c) {
            throw new UnsupportedOperationException("setEndColumn must not be used"); // NOI18N
        }
        
        @Override
        public int getEndLine() {
            throw new UnsupportedOperationException("getEndLine must not be used"); // NOI18N
        }
        
        @Override
        public void setEndLine(int l) {
            throw new UnsupportedOperationException("setEndLine must not be used"); // NOI18N
        }
        
        @Override
        public int getType() {
            return APTTokenTypes.EOF;
        }

        @Override
        public String getText() {
            return "<EOF>"; // NOI18N
        }

        @Override
        public int getColumn() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getLine() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return this == obj;
        }

    }
}
