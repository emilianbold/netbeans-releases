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

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.TextBatchProcessor;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.TokenID;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.editor.spi.cplusplus.CCSyntaxSupport;

/**
* Support methods for csm based syntax analyzes
*
* @author Vladimir Voskresensky
* @version 1.00
* implemented after JavaSyntaxSupport
*/
abstract public class CsmSyntaxSupport extends CCSyntaxSupport {

    // Internal C++ declaration token processor states
    static final int INIT = 0;
    static final int AFTER_TYPE = 1;
    static final int AFTER_VARIABLE = 2;
    static final int AFTER_COMMA = 3;
    static final int AFTER_DOT = 4;
    static final int AFTER_TYPE_LSB = 5;
    static final int AFTER_MATCHING_VARIABLE_LSB = 6;
    static final int AFTER_MATCHING_VARIABLE = 7;
    static final int AFTER_EQUAL = 8; // in decl after "var ="
    static final int AFTER_ARROW = 9;
    static final int AFTER_SCOPE = 10;

    private static final TokenID[] COMMENT_TOKENS = new TokenID[] {
                CCTokenContext.LINE_COMMENT,
                CCTokenContext.BLOCK_COMMENT
            };

    private static final TokenID[] BRACKET_SKIP_TOKENS = new TokenID[] {
                CCTokenContext.LINE_COMMENT,
                CCTokenContext.BLOCK_COMMENT,
                CCTokenContext.CHAR_LITERAL,
                CCTokenContext.STRING_LITERAL
            };

    // tokens valid for include-completion provider
    private static final TokenID[] INCLUDE_COMPLETION_TOKENS = new TokenID[] {
                CCTokenContext.USR_INCLUDE,
                CCTokenContext.SYS_INCLUDE,
                CCTokenContext.INCOMPLETE_SYS_INCLUDE,
                CCTokenContext.INCOMPLETE_USR_INCLUDE
            };
    // tokens invalid for general completion provider: skip tokens + include tokens
    private static final TokenID[] COMPLETION_SKIP_TOKENS;
    static {
        int brLen = BRACKET_SKIP_TOKENS.length;
        int incLen = INCLUDE_COMPLETION_TOKENS.length;
        COMPLETION_SKIP_TOKENS = new TokenID[brLen + incLen];
        System.arraycopy(BRACKET_SKIP_TOKENS, 0, COMPLETION_SKIP_TOKENS, 0, brLen);
        System.arraycopy(INCLUDE_COMPLETION_TOKENS, 0, COMPLETION_SKIP_TOKENS, brLen, incLen);
    }

    private static final char[] COMMAND_SEPARATOR_CHARS = new char[] {
                ';', '{', '}'
            };

    /** Whether java 1.5 constructs are recognized. */
    private boolean java15;
    
    private int lastSeparatorOffset = -1;

    public CsmSyntaxSupport(BaseDocument doc) {
        super(doc);

        tokenNumericIDsValid = true;
    }


    abstract protected CsmFinder getFinder();
    abstract protected FileReferencesContext getFileReferencesContext();

    protected void setJava15(boolean java15) {
        this.java15 = java15;
    }

    void setLastSeparatorOffset(int lastSeparatorOffset) {
        this.lastSeparatorOffset = lastSeparatorOffset;
    }

    int getLastSeparatorOffset() {
        return lastSeparatorOffset;
    }

    @Override
    public TokenID[] getCommentTokens() {
        return COMMENT_TOKENS;
    }

    @Override
    public TokenID[] getBracketSkipTokens() {
        return BRACKET_SKIP_TOKENS;
    }

    /** Return the position of the last command separator before
    * the given position.
    */
    @Override
    public int getLastCommandSeparator(final int pos) throws BadLocationException {
        if (pos == 0)
            return 0;
        final int posLine = Utilities.getLineOffset(getDocument(), pos);
        TextBatchProcessor tbp = new TextBatchProcessor() {
                 public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                             boolean lastBatch) {
                     try {
                         int[] blks = getCommentBlocks(endPos, startPos);
                         FinderFactory.CharArrayBwdFinder cmdFinder
                         = new FinderFactory.CharArrayBwdFinder(COMMAND_SEPARATOR_CHARS);
                         int lastSeparatorOffset = findOutsideBlocks(cmdFinder, startPos, endPos, blks);
                         if (lastSeparatorOffset<1) return lastSeparatorOffset;
                         TokenID separatorID = getTokenID(lastSeparatorOffset);
                         if (separatorID.getNumericID() == CCTokenContext.RBRACE_ID) {
                             int matchingBrkPos[] = findMatchingBlock(lastSeparatorOffset, true);
                             if (matchingBrkPos != null){
                                 int prev = Utilities.getFirstNonWhiteBwd(getDocument(), matchingBrkPos[0]);
                                 if (prev > -1 && getTokenID(prev).getNumericID() == CCTokenContext.RBRACKET_ID){
                                     return getLastCommandSeparator(prev);
                                 }
                             }
                         } else if (separatorID.getCategory() == CCTokenContext.CPP) {
                             // found preprocessor directive, skip till the end of it
                             int separatorLine = Utilities.getLineOffset(getDocument(), lastSeparatorOffset);
                             assert (separatorLine <= posLine);
                             if (separatorLine != posLine) {
                                 lastSeparatorOffset = Utilities.getRowEnd(getDocument(), lastSeparatorOffset);
                             }
                         }
                         if (separatorID.getNumericID() != CCTokenContext.LBRACE_ID &&
                             separatorID.getNumericID() != CCTokenContext.RBRACE_ID &&
                             separatorID.getNumericID() != CCTokenContext.SEMICOLON_ID &&
                             separatorID.getCategory() != CCTokenContext.CPP){
                                 lastSeparatorOffset = processTextBatch(doc, lastSeparatorOffset, 0, lastBatch);
                         }
                         return lastSeparatorOffset;
                     } catch (BadLocationException e) {
                         e.printStackTrace();
                         return -1;
                     }
                 }
             };
        int lastPos = getDocument().processText(tbp, pos, 0);

        //ensure we return last command separator from last
        //block of C++ tokens from <startPos;endPos> offset interval
        //AFAIK this is currently needed only for JSP code completion
        TokenItem item = getTokenChain(pos - 1, pos);
        //go back throught the token chain and try to find last C++ token
        while (item != null) {
            int tokenOffset = item.getOffset();
            if(lastPos != -1 && tokenOffset < lastPos) break; //stop backtracking if we met the lastPos
            //test token type
            if(!item.getTokenContextPath().contains(CCTokenContext.contextPath)) {
                //return offset of last C++ token - this token isn't already a C++ token so return offset of next token
                lastPos = item.getNext() != null ? item.getNext().getOffset() : item.getOffset() + item.getImage().length();
                break;
            }
            item = item.getPrevious();
        }

        return lastPos;
    }

    /** Get the class from name. The import sections are consulted to find
    * the proper package for the name. If the search in import sections fails
    * the method can ask the finder to search just by the given name.
    * @param className name to resolve. It can be either the full name
    *   or just the name without the package.
    * @param searchByName if true and the resolving through the import sections fails
    *   the finder is asked to find the class just by the given name
    */
    public CsmClassifier getClassFromName(String className, boolean searchByName) {
        return getClassFromName(this.getFinder(), className, searchByName);
    }

    /** Get the class from name. The import sections are consulted to find
    * the proper package for the name. If the search in import sections fails
    * the method can ask the finder to search just by the given name.
    * @param className name to resolve. It can be either the full name
    *   or just the name without the package.
    * @param searchByName if true and the resolving through the import sections fails
    *   the finder is asked to find the class just by the given name
    */
    public CsmClassifier getClassFromName(CsmFinder finder, String className, boolean searchByName) {
        // XXX handle primitive type
        CsmClassifier ret = null;
//        CsmClass ret = JavaCompletion.getPrimitiveClass(className);
//        if (ret == null) {
//
//            ret = getIncludeProc().getClassifier(className);
//        }
        if (ret == null && searchByName) {
            List clsList = finder.findClasses(null, className, true, false);
            if (clsList != null && clsList.size() > 0) {
                if (!clsList.isEmpty()) { // more matching classes
                    ret = (CsmClassifier)clsList.get(0); // get the first one
                }
            }

        }
        return ret;
    }

    protected void refreshClassInfo() {
    }

    /** Get the class that belongs to the given position */
    public CsmClass getClass(int pos) {
        return CompletionUtilities.findClassOnPosition(getDocument(), pos);
    }

    /** Get the class or function definition that belongs to the given position */
    public CsmOffsetableDeclaration getDefinition(int pos, FileReferencesContext fileContext) {
        return CompletionUtilities.findFunDefinitionOrClassOnPosition(getDocument(), pos, fileContext);
    }

    public boolean isStaticBlock(int pos) {
        return false;
    }

    public boolean isAnnotation(int pos) {
        try {
            BaseDocument document = getDocument();
            int off = Utilities.getFirstNonWhiteBwd(document, pos);
            char ch = '*'; // NOI18N
            while (off > -1 && (ch = document.getChars(off, 1)[0]) == '.') { // NOI18N
                off = Utilities.getFirstNonWhiteBwd(document, off);
                if (off > -1)
                    off = Utilities.getPreviousWord(document, off);
                if (off > -1)
                    off = Utilities.getFirstNonWhiteBwd(document, off);
            }
            if (off > -1 && ch == '@') // NOI18N
                return true;
        } catch (BadLocationException e) {}
        return false;
    }

    @Override
    public int[] getFunctionBlock(int[] identifierBlock) throws BadLocationException {
        int[] retValue = super.getFunctionBlock(identifierBlock);
        if (!isAnnotation(identifierBlock[0]))
            return retValue;
        return null;
    }

    public boolean isAssignable(CsmType from, CsmType to) {
        CsmClassifier fromCls = from.getClassifier();
        CsmClassifier toCls = to.getClassifier();

        if (fromCls == null) {
            return false;
        }

        if (toCls == null) {
            return false;
        }

        // XXX review!
        if (fromCls.equals(CsmCompletion.NULL_CLASS)) {
            return to.getArrayDepth() > 0 || !CsmCompletion.isPrimitiveClass(toCls);
        }

        if (toCls.equals(CsmCompletion.OBJECT_CLASS)) { // everything is object
            return (from.getArrayDepth() > to.getArrayDepth())
            || (from.getArrayDepth() == to.getArrayDepth()
            && !CsmCompletion.isPrimitiveClass(fromCls));
        }

        if (from.getArrayDepth() != to.getArrayDepth() ||
                from.getPointerDepth() != to.getPointerDepth()) {
            return false;
        }

        if (fromCls.equals(toCls)) {
            return true; // equal classes
        }
        String tfrom = from.getCanonicalText().toString().replaceAll("const", "").trim(); // NOI18N
        String tto = to.getCanonicalText().toString().replaceAll("const", "").trim(); // NOI18N

        if (tfrom.equals(tto)) {
            return true;
        }
        return false;
    }

    public CsmType getCommonType(CsmType typ1, CsmType typ2) {
        if (typ1.equals(typ2)) {
            return typ1;
        }

        // The following part
        if (!CndLexerUtilities.isType(typ1.getClassifier().getName().toString())
                && !!CndLexerUtilities.isType(typ2.getClassifier().getName().toString())) { // non-primitive classes
            if (isAssignable(typ1, typ2)) {
                return typ1;
            } else if (isAssignable(typ2, typ1)) {
                return typ2;
            } else {
                return null;
            }
        } else { // at least one primitive class
            if (typ1.getArrayDepth() != typ2.getArrayDepth()) {
                return null;
            }
            // XXX review
//            if (cls1Kwd != null && cls2Kwd != null) {
//                return JavaCompletion.getType(
//                JCUtilities.getPrimitivesCommonClass(cls1Kwd.getNumericID(), cls2Kwd.getNumericID()),
//                typ1.getArrayDepth());
//            } else { // one primitive but other not
//                return null;
//            }
            return null;
        }
    }

    /** Filter the list of the methods (usually returned from
     * Finder.findMethods()) or the list of the constructors
     * by the given parameter specification.
     * @param methodList list of the methods. They should have the same
     *   name but in fact they don't have to.
     * @param parmTypes parameter types specification. If set to null, no filtering
     *   is performed and the same list is returned. If a particular
     * @param acceptMoreParameters useful for code completion to get
     *   even the methods with more parameters.
     */
    public List filterMethods(List methodList, List parmTypeList,
    boolean acceptMoreParameters) {
        assert (methodList != null);
        if (parmTypeList == null) {
            return methodList;
        }

        List ret = new ArrayList();
        int parmTypeCnt = parmTypeList.size();
        int cnt = methodList.size();
        int maxMatched = -1;
        for (int i = 0; i < cnt; i++) {
            // Use constructor conversion to allow to use it too for the constructors
            CsmFunction m = (CsmFunction)methodList.get(i);
            CsmParameter[] methodParms = (CsmParameter[]) m.getParameters().toArray(new CsmParameter[0]);
            if (methodParms.length == parmTypeCnt
            || (acceptMoreParameters && methodParms.length >= parmTypeCnt)
            ) {
                boolean accept = true;
                boolean bestMatch = !acceptMoreParameters;
                int matched = 0;
                for (int j = 0; accept && j < parmTypeCnt; j++) {
                    if (methodParms[j] == null) {
                        System.err.println("Null parameter "+j+" in function "+m.getUID()); //NOI18N
                        bestMatch = false;
                        continue;
                    }
                    CsmType mpt = methodParms[j].getType();
                    CsmType t = (CsmType)parmTypeList.get(j);
                    if (t != null) {
                        if (!methodParms[j].isVarArgs() && !equalTypes(t, mpt)) {
                            bestMatch = false;
                            if (!isAssignable(t, mpt)) {
                                accept = false;
                                // TODO: do not break now, count matches
                                // break;
                            } else {
                                matched++;
                            }
                        } else {
                            matched++;
                        }
                    } else { // type in list is null
                        bestMatch = false;
                    }
                }

                if (accept) {
                    if (bestMatch) {
                        ret.clear();
                    } else if (matched > maxMatched) {
                        maxMatched = matched;
                        ret.clear();
                    }
                    ret.add(m);
                    if (bestMatch) {
                        break;
                    }
                } else {
                    if (matched > maxMatched) {
                        maxMatched = matched;
                        ret.clear();
                        ret.add(m);
                    }
                }

            } else if (methodParms.length == 0 && parmTypeCnt == 1) { // for cases like f(void)
                CsmType t = (CsmType)parmTypeList.get(0);
                if (t != null && "void".equals(t.getText())) { // best match // NOI18N
                    ret.clear();
                    ret.add(m);
                }
            }
        }
        return ret;
    }

    private boolean isOffsetInToken(TokenItem token, TokenID[] tokenIDs, int offset) {
        boolean exists = false;
        for (int i = tokenIDs.length - 1; i >= 0; i--) {
            if (token.getTokenID() == tokenIDs[i]) {
                exists = true;
                break;
            }
        }
        if (exists) {
            // check offset
            int st = token.getOffset();
            int len = token.getImage().length();
            if (st >= offset) {
                exists = false;
            } else if (len == 1) {
                exists = ((st + len) == offset);
            } else if (token.getTokenID().getCategory() == CCTokenContext.ERRORS) {
                exists = ((st + len) >= offset);
            } else {
                exists = ((st + len) > offset);
            }
        }
        return exists;
    }

    ////////////////////////////////////////////////
    // overriden functions to resolve expressions
    /////////////////////////////////////////////////

    // utitlies

    @Override
    protected boolean isAbbrevDisabled(int offset) {
        boolean abbrevDisabled = false;
        TokenID[] disableTokenIds = BRACKET_SKIP_TOKENS;
        if (disableTokenIds != null) {
            TokenItem token;
            try {
                token = getTokenChain(offset, offset + 1);
            } catch (BadLocationException e) {
                token = null;
            }
            if (token != null) {
                if (offset > token.getOffset()) { // not right at token's begining
                    for (int i = disableTokenIds.length - 1; i >= 0; i--) {
                        if (token.getTokenID() == disableTokenIds[i]) {
                            abbrevDisabled = true;
                            break;
                        }
                    }
                }
                if (!abbrevDisabled) { // check whether not right after line comment
                    if (token.getOffset() == offset) {
                        TokenItem prevToken = token.getPrevious();
                        if (prevToken != null
                            && prevToken.getTokenID() == CCTokenContext.LINE_COMMENT
                        ) {
                            abbrevDisabled = true;
                        }
                    }
                }
            }
        }
        return abbrevDisabled;
    }

    public boolean isIncludeCompletionDisabled(int offset) {
        boolean completionDisabled = true;
        TokenItem endToken = getTokenItem(offset);
        if (endToken != null) {
            TokenItem token = shiftToNonWhiteBwd(endToken);
            if (token != null) {
                completionDisabled = !isOffsetInToken(token, INCLUDE_COMPLETION_TOKENS, offset);
                if (completionDisabled) {
                    // check whether right after #include or #include_next directive
                    switch (token.getTokenID().getNumericID()) {
                        case CCTokenContext.CPPINCLUDE_ID:
                        case CCTokenContext.CPPINCLUDE_NEXT_ID:
                            return false; // return completionDisabled = false;
                    }
                }
            }
            // check for "#include prefix" (IZ 119931)
            if (completionDisabled) {
                token = endToken.getPrevious();
                if (token != null && token.getTokenID().getNumericID() == CCTokenContext.IDENTIFIER_ID) {
                    token = token.getPrevious();
                    if (token != null) {
                        token = shiftToNonWhiteBwd(token);
                        if (token != null) {
                            switch (token.getTokenID().getNumericID()) {
                                case CCTokenContext.CPPINCLUDE_ID:
                                case CCTokenContext.CPPINCLUDE_NEXT_ID:
                                    return false; // return completionDisabled = false;
                            }
                        }
                    }
                }
            }
        }
        return completionDisabled;
    }

    public TokenItem getTokenItem(int offset) {
        TokenItem token;
        try {
            int checkOffset = offset;
            if (offset == getDocument().getLength()) {
                if (offset == 0) {
                    return null;
                }
                checkOffset--;
            }
            token = getTokenChain(checkOffset, checkOffset + 1);
        } catch (BadLocationException e) {
            token = null;
        }
        return token;
    }

    public TokenItem shiftToNonWhiteBwd(TokenItem token) {
        if (token == null) {
            return null;
        }
        boolean checkedFirst = false;
        do {
            switch (token.getTokenID().getNumericID()) {
            case CCTokenContext.WHITESPACE_ID:
                if (checkedFirst) {
                    if (token.getImage().contains("\n")) { // NOI18N
                        return null;
                    }
                }
                break;
            case CCTokenContext.BLOCK_COMMENT_ID:
                // skip
                break;
            default:
                return token;
            }
            token = token.getPrevious();
            checkedFirst = true;
        } while (token != null);
        return null;
    }

    public boolean isCompletionDisabled(int offset) {
        boolean completionDisabled = false;
        TokenID[] disableTokenIds = COMPLETION_SKIP_TOKENS;
        if (disableTokenIds != null) {
            TokenItem token;
            try {
                token = getTokenChain(offset, offset + 1);
            } catch (BadLocationException e) {
                token = null;
            }
            if (token != null) {
                if (offset > token.getOffset()) { // not right at token's begining
                    for (int i = disableTokenIds.length - 1; i >= 0; i--) {
                        if (token.getTokenID() == disableTokenIds[i]) {
                            completionDisabled = true;
                            break;
                        }
                    }
                }
                if (!completionDisabled) { // check whether not right after line comment or float constant
                    if (token.getOffset() == offset) {
                        TokenItem prevToken = token.getPrevious();
                        if (prevToken != null
                            && (prevToken.getTokenID() == CCTokenContext.LINE_COMMENT
                                || prevToken.getTokenID() == CCTokenContext.FLOAT_LITERAL
                                || prevToken.getTokenID() == CCTokenContext.DOUBLE_LITERAL)
                        ) {
                            completionDisabled = true;
                        }
                    }
                }
            }
        }
        return completionDisabled;
    }

    public boolean needShowCompletionOnText(JTextComponent target, String typedText) throws BadLocationException {
        boolean showCompletion = false;
        char typedChar = typedText.charAt(typedText.length() - 1);
        if (typedChar == ' ' || typedChar == '>' || typedChar == ':' || typedChar == '.' || typedChar == '*') {

            int dotPos = target.getCaret().getDot();
            BaseDocument doc = (BaseDocument)target.getDocument();
            TokenItem item = getTokenChain(dotPos - 1, dotPos);
            TokenItem prev = null;
            if (typedChar == ' ' || typedChar == '.') { // init prev for space and dot
                try {
                    prev = item == null ? null : item.getPrevious();
                } catch (IllegalStateException ex) {
                    prev = null;
                }
            }
            switch (typedChar) {
                case ' ': // completion after "new" keyword
                    if (prev != null && prev.getTokenID() == CCTokenContext.NEW) {
                        showCompletion = true;
                    }
                    break;
                case '>': // completion after arrow
                    if (item != null && item.getTokenID() == CCTokenContext.ARROW) {
                        showCompletion = true;
                    }
                    break;
                case '.': // completion after dot
                    showCompletion = true;
                    // hide completion in inlclude strings
                    if (item != null && (
                            item.getTokenID().getCategory() == CCTokenContext.ERRORS ||
                            item.getTokenID() == CCTokenContext.USR_INCLUDE ||
                            item.getTokenID() == CCTokenContext.SYS_INCLUDE)) {
                        showCompletion = false;
                    } else if (prev != null && prev.getTokenID() == CCTokenContext.DOT) {
                        showCompletion = false;
                    }
                    break;
                case '*': // completion after star
                    if (item != null &&
                            (item.getTokenID() == CCTokenContext.ARROWMBR ||
                             item.getTokenID() == CCTokenContext.DOTMBR)) {
                        showCompletion = true;
                    }
                    break;
                case ':': // completion after scope
                    if (item != null && item.getTokenID() == CCTokenContext.SCOPE) {
                        showCompletion = true;
                    }
                    break;
            }
        }
        return showCompletion;
    }

    private boolean equalTypes(CsmType t, CsmType mpt) {
        assert t != null;
        if (t.equals(mpt)) {
            return true;
        } else if (mpt != null) {
            String t1 = t.getCanonicalText().toString();
            String t2 = mpt.getCanonicalText().toString();
            return t1.equals(t2);
        }
        return false;
    }
}
