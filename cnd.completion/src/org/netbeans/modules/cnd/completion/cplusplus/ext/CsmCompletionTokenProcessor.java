/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.api.lexer.TokenId;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.CndTokenProcessor;
import static org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression.*;

/**
 * Token processor that parses the text and produces jc expressions.
 *
 * @author Vladimir Voskresensky
 * @version 1.00
 */
final class CsmCompletionTokenProcessor implements CndTokenProcessor<Token<TokenId>> {

    private static final int NO_EXP = -1;
    /** Buffer that is scanned */
//    private char[] buffer;
    /** Start position of the buffer in the document */
    private int bufferStartPos;
    /** Delta of the token processor buffer offsets against the offsets given
     * in the source buffer.
     */
    private int bufferOffsetDelta;
    /** Stack of the expressions. */
    private List<CsmCompletionExpression> expStack = new ArrayList<CsmCompletionExpression>();
    /** TokenID of the last found token except Syntax.EOT and Syntax.EOL */
    private CppTokenId lastValidTokenID;
    /** Text of the last found token except Syntax.EOT and Syntax.EOL */
    private String lastValidTokenText;
    private boolean errorState = false;
    private boolean inPreprocessor = false;

    // helper variables
    private CppTokenId curTokenID;
    private int curTokenPosition;
    private String curTokenText;
    private int endScanOffset;
    private boolean supportTemplates;
    private int nrQuestions = 0;

    // isMacro callback
    private MacroCallback macroCallback = null;
    
    private List<OffsetableToken> lookaheadTokens = new ArrayList<OffsetableToken>();
    private int lookaheadTokensParensLevel = 0;
    private int lookaheadTokensBracketsLevel = 0;
    private int lookaheadTokensBracesLevel = 0;
    private int lookaheadTokensLtgtsLevel = 0;
    
    
    CsmCompletionTokenProcessor(int endScanOffset, int lastSeparatorOffset) {
        this.endScanOffset = endScanOffset;
        this.lastSeparatorOffset = lastSeparatorOffset;
    }

    /**
     * Set whether templates features should be enabled.
     *
     * @param supportTemplates true to parse expression as being in syntax with templates
     */
    void enableTemplateSupport(boolean supportTemplates) {
        this.supportTemplates = supportTemplates;
    }

    /** Get the expression stack from the bottom to top */
    final List<CsmCompletionExpression> getStack() {
        return expStack;
    }

    /** Get the last token that was processed that wasn't
     * either Syntax.EOT or Syntax.EOL.
     */
    final CppTokenId getLastValidTokenID() {
        return lastValidTokenID;
    }

    final String getLastValidTokenText() {
        return lastValidTokenText;
    }

    final int getCurrentOffest() {
        return curTokenPosition;
    }

    final boolean isErrorState() {
        return errorState;
    }

    final boolean isInPreprocessor() {
        return inPreprocessor;
    }

    final CsmCompletionExpression getResultExp() {
        CsmCompletionExpression result = peekExp();
        return result;
    }

    private void clearStack() {
        expStack.clear();
    }

    boolean isSeparatorOrOperator(CppTokenId tokenID) {
        return CndLexerUtilities.isSeparatorOrOperator(tokenID);
    }

    private boolean isEqOperator(CppTokenId tokenID) {
        switch (tokenID) {
            case EQ:
            case EQEQ:
            case GTEQ:
            case GTGTEQ:
            case AMPEQ:
            case LTEQ:
            case LTLTEQ:
            case PLUSEQ:
            case NOTEQ:
            case MINUSEQ:
            case STAREQ:
            case SLASHEQ:
            case BAREQ:
            case CARETEQ:
            case PERCENTEQ:
                return true;
        }
        return false;
    }

    /**
     * Macro callback setter
     */
    public void setMacroCallback(MacroCallback callback) {
        macroCallback = callback;
    }

    /** Returns is current token macro expansion or not */
    private boolean isMacroExpansion() {
        if(macroCallback != null) {
            return macroCallback.isMacroExpansion();
        } else {
            return false;
        }
    }

    /** Push exp to top of stack */
    private void pushExp(CsmCompletionExpression exp) {
        expStack.add(exp);
    }

    /** Pop exp from top of stack */
    private CsmCompletionExpression popExp() {
        int cnt = expStack.size();
        return (cnt > 0) ? expStack.remove(cnt - 1) : null;
    }

    /** Look at the exp at top of stack */
    private CsmCompletionExpression peekExp() {
        int cnt = expStack.size();
        return (cnt > 0) ? expStack.get(cnt - 1) : null;
    }

    /** Look at the second exp on stack */
    private CsmCompletionExpression peekExp2() {
        int cnt = expStack.size();
        return (cnt > 1) ? expStack.get(cnt - 2) : null;
    }

    /** Look at the third exp on stack */
    private CsmCompletionExpression peekExp(int ind) {
        int cnt = expStack.size();
        return (cnt >= ind && cnt > 0) ? expStack.get(cnt - ind) : null;
    }

    private CsmCompletionExpression createTokenExp(int id) {
        CsmCompletionExpression exp = new CsmCompletionExpression(id);
        addTokenTo(exp);
        return exp;
    }
    
    private CsmCompletionExpression createTokenExp(int id, CsmCompletionExpression oldExpression) {
        CsmCompletionExpression exp = new CsmCompletionExpression(id);
        for (int i = 0; i < oldExpression.getTokenCount(); i++) {
            exp.addToken(oldExpression.getTokenID(i), oldExpression.getTokenOffset(i), oldExpression.getTokenText(i));
        }
        return exp;
    }    

    /** Add the token to a given expression */
    private void addTokenTo(CsmCompletionExpression exp) {
        exp.addToken(curTokenID, curTokenPosition, curTokenText);
    }

    private int getValidExpID(CsmCompletionExpression exp) {
        return (exp != null) ? exp.getExpID() : NO_EXP;
    }

    private int tokenID2OpenExpID(CppTokenId tokenID) {
        switch (tokenID) {
            case DOT: // '.' found
            case DOTMBR: // '.*' found  
                return DOT_OPEN;
            case ARROW: // '->' found
            case ARROWMBR: // '->*' found    
                return ARROW_OPEN;
            case SCOPE: // '::' found    
                return SCOPE_OPEN;
            case IF:
                return IF;
            case FOR:
                return FOR;
            case SWITCH:
                return SWITCH;
            case WHILE:
                return WHILE;
            default:
                assert (false) : "unexpected tokenID " + tokenID;
        }
        return 0;
    }

    private int openExpID2ExpID(int openExpID) {
        switch (openExpID) {
            case DOT_OPEN:
                return DOT;
            case ARROW_OPEN:
                return ARROW;
            case SCOPE_OPEN:
                return SCOPE;
            default:
                assert (false) : "unexpected expID" + CsmCompletionExpression.getIDName(openExpID);
                return 0;
        }
    }

    private void setExprToTYPE(CsmCompletionExpression exp) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < exp.getTokenCount(); i++) {
            if (i > 0) {
                buf.append(" ");// NOI18N
            }
            buf.append(exp.getTokenText(i));
        }
        exp.setType(buf.toString());
        exp.setExpID(TYPE);
    }
    
    @Override
    public boolean token(Token<TokenId> token, int tokenOffset) {
        if(!(token.id() instanceof CppTokenId)) {
            return false;
        }
        if (inPP == null) { // not yet initialized
            inPP = (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE);
        }
        if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            return inPP;
        }
        
        lookahead(token, tokenOffset);
        return false;
    }
    
    private void lookahead(Token<TokenId> token, int tokenOffset) {
        boolean lookahead = false;
        if(isTemplateAmbiguity(token)) {
            if(isLookaheadNeeded(token)) {
                lookahead = true;
                lookaheadTokens.add(new OffsetableToken(token, tokenOffset, isMacroExpansion(), inPP));
                switch ((CppTokenId) token.id()) {
                    case LT:
                        lookaheadTokensLtgtsLevel++;
                        break;
                    case GT:
                        lookaheadTokensLtgtsLevel--;
                        break;
                    case LPAREN:
                        lookaheadTokensParensLevel++;
                        break;
                    case RPAREN:
                        lookaheadTokensParensLevel--;
                        break;
                    case LBRACKET:
                        lookaheadTokensBracketsLevel++;
                        break;
                    case RBRACKET:
                        lookaheadTokensBracketsLevel--;
                        break;
                    case LBRACE:
                        lookaheadTokensBracesLevel++;
                        break;
                    case RBRACE:
                        lookaheadTokensBracesLevel--;
                        break;
                }            
            }
        } 
        if(!lookahead) {
            if(lookaheadTokens.isEmpty()) {
                tokenImpl(token, tokenOffset, isMacroExpansion());                
            } else {
                Boolean oldInPP = inPP;
                for (OffsetableToken offsetableToken : lookaheadTokens) {
                    inPP = offsetableToken.inPP;
                    tokenImpl(offsetableToken.token, offsetableToken.offset, offsetableToken.macro);
                }
                lookaheadTokens.clear();                
                tokenImpl(token, tokenOffset, isMacroExpansion());
                inPP = oldInPP;
                lookaheadTokensParensLevel = 0;
                lookaheadTokensBracketsLevel = 0;
                lookaheadTokensBracesLevel = 0;
                lookaheadTokensLtgtsLevel = 0;
            }
        }
    }
    
    private boolean isLookaheadNeeded(Token<TokenId> token) {
        int tempLookaheadTokensParensLevel = lookaheadTokensParensLevel;
        int tempLookaheadTokensBracketsLevel = lookaheadTokensBracketsLevel;
        int tempLookaheadTokensBracesLevel = lookaheadTokensBracesLevel;
        int tempLookaheadTokensLtgtsLevel = lookaheadTokensLtgtsLevel;
        
        switch ((CppTokenId) token.id()) {
            case LT:
                tempLookaheadTokensLtgtsLevel++;
                break;
            case GT:
                tempLookaheadTokensLtgtsLevel--;
                break;
            case LPAREN:
                tempLookaheadTokensParensLevel++;
                break;
            case RPAREN:
                tempLookaheadTokensParensLevel--;
                break;
            case LBRACKET:
                tempLookaheadTokensBracketsLevel++;
                break;
            case RBRACKET:
                tempLookaheadTokensBracketsLevel--;
                break;
            case LBRACE:
                tempLookaheadTokensBracesLevel++;
                break;
            case RBRACE:
                tempLookaheadTokensBracesLevel--;
                break;
        }        
        if (!lookaheadTokens.isEmpty()) {
            return !(tempLookaheadTokensParensLevel == 0 && tempLookaheadTokensBracketsLevel == 0 && tempLookaheadTokensBracesLevel == 0 && tempLookaheadTokensLtgtsLevel == 0);
        } else {
            return true;
        }
    }
    
    private boolean isTemplateAmbiguity(Token<TokenId> token) {
        if(supportTemplates && (token.id() == CppTokenId.LT || 
                (!lookaheadTokens.isEmpty() && lookaheadTokens.get(0).token.id() == CppTokenId.LT))) {
            CsmCompletionExpression top = peekExp();
            if(top != null) {
                // from rule about generic types in tokenImpl
                switch(top.getExpID()) {
                    case VARIABLE:
                    case DOT:
                    case ARROW:
                    case SCOPE:
                    case MEMBER_POINTER:
                        return true;

                    default:
                        break;   
                }
            }
        }    
        return false;
    }
    
    private OffsetableToken isSupportTemplates() {
        LinkedList<CppTokenId> stack = new LinkedList<CppTokenId>();
        for (OffsetableToken offsetableToken : lookaheadTokens) {
            switch((CppTokenId)offsetableToken.token.id()) {
                case LT:
                    stack.push(CppTokenId.LT);
                    break;
                case GT:
                    if(!stack.isEmpty() && stack.pop() != CppTokenId.LT) {
                        return offsetableToken;
                    }
                    break;
                case LPAREN:
                    stack.push(CppTokenId.LPAREN);
                    break;
                case RPAREN:
                    if(!stack.isEmpty() && stack.pop() != CppTokenId.LPAREN) {
                        return offsetableToken;
                    }
                    break;
                case LBRACKET:
                    stack.push(CppTokenId.LBRACKET);
                    break;
                case RBRACKET:
                    if(!stack.isEmpty() && stack.pop() != CppTokenId.LBRACKET) {
                        return offsetableToken;
                    }
                    break;
                case LBRACE:
                    stack.push(CppTokenId.LBRACE);
                    break;
                case RBRACE:
                    if(!stack.isEmpty() && stack.pop() != CppTokenId.LBRACE) {
                        return offsetableToken;
                    }
                    break;
            }
        }
        return null;
    }
    
    /** Check whether there can be any joining performed
     * for current expressions on the stack.
     * @param tokenID tokenID of the current token
     * @return true to continue, false if errorneous construction found
     */
    @SuppressWarnings("fallthrough")
    private boolean checkJoin(CppTokenId tokenID) {
        boolean ret = true;

        boolean cont = true;
        while (cont) {
            cont = false;
            CsmCompletionExpression top = peekExp();
            CsmCompletionExpression top2 = peekExp2();
            int top2ID = getValidExpID(top2);
            int topID = getValidExpID(top);
            switch (topID) {
                case GENERIC_TYPE:
                {
                    boolean stop = false;
                    switch (top2ID) {
                        case METHOD_OPEN:
                            switch (tokenID) {
                                case STAR:
                                case AMP:
                                case CONST:
                                case IDENTIFIER:
                                    popExp();
                                    CsmCompletionExpression typeExpr = new CsmCompletionExpression(TYPE);
                                    typeExpr.addParameter(top);
                                    pushExp(typeExpr);
                                    stop = true;
                                    break;
                            }
                            break;
                    }
                    if (stop) {
                        break;
                    }
                }
                case VARIABLE:
                    boolean stop = false;
                    switch (top2ID) {
                        case METHOD_OPEN:
                            switch (tokenID) {
                                case STAR:
                                case AMP:
                                case CONST:
                                case IDENTIFIER:
                                    setExprToTYPE(top);
                                    stop = true;
                                    break;
                            }
                            break;
                        case TYPE_PREFIX:
                            // Merge TYPE_PREFIX and VARIABLE expressions
                            // into a single TYPE expression.
                            switch (tokenID) {
                                case STAR:
                                case AMP:
                                case CONST:
                                case IDENTIFIER:
                                    popExp();
                                    setExprToTYPE(top2);
                                    for (int i = 0; i < top.getTokenCount(); ++i) {
                                        top2.addToken(top.getTokenID(i),
                                                top.getTokenOffset(i),
                                                top.getTokenText(i));
                                    }
                                    top = top2;
                                    stop = true;
                                    break;
                            }
                            break;
                    }
                    if (stop) {
                        break;
                    }
                // nobreak;
                case METHOD:
                case CONSTRUCTOR:
                case ARRAY:
                case DOT:
                case ARROW:
                case SCOPE:
                case PARENTHESIS:
                case OPERATOR: // operator on top of stack
                    switch (top2ID) {
                        case METHOD_OPEN:
                            switch (tokenID) {
                                case STAR:
                                case AMP:
                                case CONST:
                                case IDENTIFIER:
                                    setExprToTYPE(top);
                                    break;
                            }
                            break;
                        case UNARY_OPERATOR:
                            switch (tokenID) {
                                case DOT:
                                case DOTMBR:
                                case ARROW:
                                case ARROWMBR:
                                case SCOPE:
                                case LBRACKET:
                                case PLUSPLUS:
                                case MINUSMINUS:
                                    break;
                                case LT:
                                    if (supportTemplates) {
                                        break;
                                    }

                                case LPAREN:
                                     {
                                        if (topID == VARIABLE &&
                                                top2ID == UNARY_OPERATOR && top2.getParameterCount() == 0 &&
                                                top2.getTokenCount() == 1 && top2.getTokenID(0) == CppTokenId.TILDE &&
                                                top.getParameterCount() == 0 && top.getTokenCount() == 1) {
                                            // we have tilda and variable on top of the stack, this is destructor in fact
                                            // like ~Clazz(
                                            // join into variable
                                            popExp(); // pop VARIABLE
                                            popExp(); // pop '~'

                                            // construct new VARIABLE expression
                                            CppTokenId aCurTokenID = top.getTokenID(0);
                                            int aCurTokenPosition = top2.getTokenOffset(0);
                                            String aCurTokenText = top2.getTokenText(0) + top.getTokenText(0);
                                            CsmCompletionExpression exp = new CsmCompletionExpression(VARIABLE);
                                            exp.addToken(aCurTokenID, aCurTokenPosition, aCurTokenText);
                                            pushExp(exp);
                                        }
                                    }
                                    break;

                                case WHITESPACE:
                                case NEW_LINE:
                                case LINE_COMMENT:
                                case DOXYGEN_LINE_COMMENT:
                                case BLOCK_COMMENT:
                                case DOXYGEN_COMMENT:
                                    break;

                                default: // Join
                                    if (top2.getParameterCount() == 0) {
                                        popExp(); // pop top
                                        top2.addParameter(top);
                                    }
                                    break;

                            }
                            break;

                        case DOT_OPEN:
                        case ARROW_OPEN:
                        case SCOPE_OPEN:
                            if (isSeparatorOrOperator(tokenID)) {
                                switch (tokenID) {
                                    case LPAREN:
                                        break;
                                    case LT:
                                        if(supportTemplates) {
                                            break;
                                        }
                                    default:
                                        popExp();
                                        top2.addParameter(top);
                                        top2.setExpID(openExpID2ExpID(top2ID)); // *_OPEN -> * conversion, use value of case
                                        cont = true;
                                }
                            }
                            break;

                        case MEMBER_POINTER_OPEN:
                            if (isSeparatorOrOperator(tokenID)) {
                                switch (tokenID) {
                                    case LPAREN:
                                    case SCOPE:
                                    case LBRACKET:
                                    case DOT:
                                    case ARROW:
                                        break;
                                    default:
                                        popExp();
                                        top2.addParameter(top);
                                        top2.setExpID(MEMBER_POINTER); // *_OPEN -> * conversion, use value of case
                                        cont = true;
                                }
                            }
                            break;

                        case CONVERSION:
                            if (isSeparatorOrOperator(tokenID)) {
                                switch (tokenID) {
                                    case RPAREN:
                                    case COMMA:
                                        CsmCompletionExpression top3 = peekExp(3);
                                        if (top3 != null) {
                                            switch (top3.getExpID()) {
                                                case CsmCompletionExpression.PARENTHESIS_OPEN:
                                                case CsmCompletionExpression.METHOD_OPEN:
                                                    popExp(); // pop top
                                                    top2.addParameter(top); // add last to conversion
                                                    break;
                                            }
                                        }
                                        break;
                                }
                            }
                            break;

                        case TYPE:
                        case VARIABLE:
                        case TYPE_REFERENCE:
                        case SCOPE:
                            if (isSeparatorOrOperator(tokenID)) {
                                switch (tokenID) {
                                    case RPAREN:
                                    case STAR:
                                    case AMP:
                                    case LBRACKET:
                                    case GT:
                                         {
                                            if (topID == OPERATOR && top.getParameterCount() == 0 &&
                                                    top.getTokenCount() == 1 &&
                                                    (top.getTokenID(0) == CppTokenId.STAR ||
                                                    top.getTokenID(0) == CppTokenId.AMP ||
                                                    (supportTemplates && top.getTokenID(0) == CppTokenId.AMPAMP))) {
                                                // we have variable and then * or &, or &&
                                                // join into TYPE_REFERENCE
                                                popExp(); // pop '&' or '*' (top)
                                                popExp(); // pop second (top2)
                                                CsmCompletionExpression exp = new CsmCompletionExpression(TYPE_REFERENCE);
                                                exp.addParameter(top2);
                                                exp.addToken(top.getTokenID(0), top.getTokenOffset(0), top.getTokenText(0));
                                                pushExp(exp);
                                            }
                                        }
                                        break;
                                }
                            }
                            break;
                    }

                    break;
            }
        }

        int leftOpID = CsmCompletionExpression.getOperatorID(tokenID);

        if (leftOpID >= 0) {
            switch (CsmCompletionExpression.getOperatorPrecedence(leftOpID)) {
                case 0: // stop ID - try to join the exps on stack
                    CsmCompletionExpression lastVar = null;
                    CsmCompletionExpression rightOp = peekExp();
                    int rightOpID = -1;
                    rightOpID = CsmCompletionExpression.getOperatorID(rightOp);
                    switch (CsmCompletionExpression.getOperatorPrecedence(rightOpID)) {
                        case 0: // stop - nothing to join
                            rightOp = null;
                            break;

                        case 1: // single item - move to next and add this one
                            lastVar = rightOp;
                            rightOp = peekExp2();
                            rightOpID = CsmCompletionExpression.getOperatorID(rightOp);
                            switch (CsmCompletionExpression.getOperatorPrecedence(rightOpID)) {
                                case 0: // stop - only one item on the stack
                                    rightOp = null;
                                    break;

                                case 1: // two items without operator - error
                                    ret = false;
                                    rightOp = null;
                                    break;

                                default:
                                    popExp(); // pop item
                                    rightOp.addParameter(lastVar); // add item as parameter
                                    lastVar = null;
                            }
                            break;
                    }

                    if (rightOp != null) {
                        popExp(); // pop rightOp
                        cont = true;
                        ArrayList<CsmCompletionExpression> opStack = new ArrayList<CsmCompletionExpression>(); // operator stack
                        CsmCompletionExpression leftOp = null;
                        do {
                            if (leftOp == null) {
                                leftOp = popExp();
                                if (leftOp == null) {
                                    break;
                                }
                                leftOpID = CsmCompletionExpression.getOperatorID(leftOp);
                            }
                            switch (CsmCompletionExpression.getOperatorPrecedence(leftOpID)) {
                                case 0: // stop here
                                    pushExp(leftOp); // push last exp back to stack
                                    cont = false;
                                    break;

                                case 1: // item found
                                    lastVar = leftOp;
                                    leftOp = null; // ask for next pop
                                    break;

                                default: // operator found
                                    int leftOpPrec = CsmCompletionExpression.getOperatorPrecedence(leftOpID);
                                    int rightOpPrec = CsmCompletionExpression.getOperatorPrecedence(rightOpID);
                                    boolean rightPrec;
                                    if (leftOpPrec > rightOpPrec) { // left has greater priority
                                        rightPrec = false;
                                    } else if (leftOpPrec < rightOpPrec) { // right has greater priority
                                        rightPrec = true;
                                    } else { // equal priorities
                                        rightPrec = CsmCompletionExpression.isOperatorRightAssociative(rightOpID);
                                    }


                                    if (rightPrec) { // right operator has precedence
                                        if (lastVar != null) {
                                            rightOp.addParameter(lastVar);
                                        }
                                        if (opStack.size() > 0) { // at least one right stacked op
                                            lastVar = rightOp; // rightOp becomes item
                                            rightOp = opStack.remove(opStack.size() - 1); // get stacked op
                                            rightOpID = CsmCompletionExpression.getOperatorID(rightOp);
                                        } else { // shift the leftOp to rightOp
                                            leftOp.addParameter(rightOp);
                                            lastVar = null;
                                            rightOp = leftOp;
                                            rightOpID = leftOpID;
                                            leftOp = null; // ask for next poping
                                        }
                                    } else { // left operator has precedence
                                        if (lastVar != null) {
                                            leftOp.addParameter(lastVar);
                                            lastVar = null;
                                        }
                                        opStack.add(rightOp); // push right operator to stack
                                        //                      rightOp.addParameter(leftOp);
                                        rightOp = leftOp; // shift left op to right op
                                        rightOpID = leftOpID;
                                        leftOp = null;
                                    }
                            }
                        } while (cont);

                        // add possible valid last item
                        if (lastVar != null) {
                            rightOp.addParameter(lastVar);
                        }

                        // pop the whole stack adding the current right op to the stack exp
                        for (int i = opStack.size() - 1; i >= 0; i--) {
                            CsmCompletionExpression op = opStack.get(i);
                            op.addParameter(rightOp);
                            rightOp = op;
                        }

                        rightOp.swapOperatorParms();
                        pushExp(rightOp); // push the top operator
                    }
                    break;
            }
        }

        return ret;
    }

    @SuppressWarnings("fallthrough")
    private void tokenImpl(Token<TokenId> token, int tokenOffset, boolean macro) {
        int tokenLen = token.length();
        tokenOffset += bufferOffsetDelta;
        CppTokenId tokenID = (CppTokenId)token.id();
        if (!macro && tokenID != null) {
            String category = tokenID.primaryCategory();
            if (CppTokenId.KEYWORD_CATEGORY.equals(category) || CppTokenId.KEYWORD_DIRECTIVE_CATEGORY.equals(category)) {
                if (tokenOffset + tokenLen == endScanOffset) {
                    tokenID = CppTokenId.IDENTIFIER;
                }
            }
        }

        if (tokenID == CppTokenId.PREPROCESSOR_IDENTIFIER 
                || tokenID == CppTokenId.SIZEOF
                || tokenID == CppTokenId.TYPEID) {
            // change preproc identifier into normal identifier
            // to simplify handling of result expression
            tokenID = CppTokenId.IDENTIFIER;
        }
        // assign helper variables
        if (tokenID != null) {
            if (lastValidTokenID == CppTokenId.COLON) {
                nrQuestions--;
            }
            lastValidTokenID = tokenID;
        }

        curTokenID = tokenID;
        curTokenPosition = bufferStartPos + tokenOffset;
        // System.err.printf("tokenOffset = %d, tokenLen = %d, tokenID = %s\n", tokenOffset, tokenLen, tokenID == null ? "null" : tokenID.toString());
        CharSequence txt = token.text();
        if (!isMacroExpansion() && tokenOffset + tokenLen > endScanOffset) {
            assert (endScanOffset > tokenOffset) : "end - " + endScanOffset + " start - " + tokenOffset + " tokenLen - " + tokenLen + "tokenID - " + tokenID + " txt = \n" + txt;
            txt = txt.subSequence(0, endScanOffset - tokenOffset);
        }
        StringBuilder buf = new StringBuilder(txt);
        curTokenText = buf.toString();
        //curTokenText = txt.toString();
        lastValidTokenText = curTokenText;
        errorState = false; // whether the parser cannot understand given tokens

        checkJoin(tokenID);

        CsmCompletionExpression top = peekExp(); // exp at top of stack
        int topID = getValidExpID(top); // id of the exp at top of stack

        CsmCompletionExpression constExp = null; // possibly assign constant into this exp
        String kwdType = CndLexerUtilities.isType(tokenID) ? curTokenText : null; // keyword constant type (used in conversions)

        // clear stack on absent token or prerpocessor token
        if (tokenID == null) {
            errorState = true;
        } else { // valid token-id
            if (true) {
                switch (tokenID) { // test the token ID
// XXX
//                    case BOOLEAN:
//                        kwdType = JavaCompletion.BOOLEAN_TYPE;
//                        break;
//                    case BYTE:
//                        kwdType = JavaCompletion.BYTE_TYPE;
//                        break;
//                    case CHAR:
//                        kwdType = JavaCompletion.CHAR_TYPE;
//                        break;
//                    case DOUBLE:
//                        kwdType = JavaCompletion.DOUBLE_TYPE;
//                        break;
//                    case FLOAT:
//                        kwdType = JavaCompletion.FLOAT_TYPE;
//                        break;
//                    case INT:
//                        kwdType = JavaCompletion.INT_TYPE;
//                        break;
//                    case LONG:
//                        kwdType = JavaCompletion.LONG_TYPE;
//                        break;
//                    case SHORT:
//                        kwdType = JavaCompletion.SHORT_TYPE;
//                        break;

                    case PREPROCESSOR_START:
                    case PREPROCESSOR_START_ALT:
                        pushExp(createTokenExp(PREPROC_DIRECTIVE_OPEN));
                        break;

                    case PREPROCESSOR_DEFINE:
                    case PREPROCESSOR_ELIF:
                    case PREPROCESSOR_ELSE:
                    case PREPROCESSOR_ENDIF:
                    case PREPROCESSOR_ERROR:
                    case PREPROCESSOR_IDENT:
                    case PREPROCESSOR_IF:
                    case PREPROCESSOR_IFDEF:
                    case PREPROCESSOR_IFNDEF:
                    case PREPROCESSOR_INCLUDE:
                    case PREPROCESSOR_INCLUDE_NEXT:
                    case PREPROCESSOR_LINE:
                    case PREPROCESSOR_PRAGMA:
                    case PREPROCESSOR_UNDEF:
                    case PREPROCESSOR_WARNING:
                        if (topID == PREPROC_DIRECTIVE_OPEN) {
                            top.setExpID(PREPROC_DIRECTIVE);
                            addTokenTo(top);
                        } else {
                            errorState = true;
                        }
                        break;
                    case STATIC_CAST:
                    case DYNAMIC_CAST:
                    case CONST_CAST:
                    case REINTERPRET_CAST:
                        pushExp(createTokenExp(CONVERSION_OPEN));
                        break;
                    case TRUE:
                    case FALSE:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType(CsmCompletion.BOOLEAN_CLASS.getName().toString()); // NOI18N
                        break;

                    case NULL:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("null"); // NOI18N
                        break;

                    case CLASS:
                        if (topID == DOT_OPEN || topID == ARROW_OPEN || topID == SCOPE_OPEN) {
                            pushExp(createTokenExp(VARIABLE));
                            break;
                        }
                    //nobreak
                    case STRUCT:
                    case UNION:
                        pushExp(createTokenExp(CLASSIFIER));
                        break;
                    case GOTO:
                        pushExp(createTokenExp(GOTO));
                        break;
                    case NEW:
                        switch (topID) {
                            case VARIABLE:
                            case NEW:
                                errorState = true;
                                break;

                            default:
                                pushExp(createTokenExp(NEW));
                                break;
                        }
                        break;

//                    case CPPINCLUDE:
//                        pushExp(createTokenExp(CPPINCLUDE));
//                        break;

                    case STATIC:
                        switch (topID) {
//                        case CPPINCLUDE:
//                            top.addParameter(createTokenExp(CPPINCLUDE));
//                            break;
                            default:
                                errorState = true;
                                break;
                        }
                        break;

//                    case SUPER:
//                        if (topID == GENERIC_WILD_CHAR)
//                            break;
                    case THIS:
                        pushExp(createTokenExp(VARIABLE));
                        break;

//                    case ANNOTATION:
//                        pushExp(createTokenExp(ANNOTATION));
//                        break;

//                    case INSTANCEOF:
//                        switch (topID) {
//                        case CONSTANT:
//                        case VARIABLE:
//                        case METHOD:
//                        case CONSTRUCTOR:
//                        case ARRAY:
//                        case DOT:
//                        case PARENTHESIS:
//                            pushExp(createTokenExp(INSTANCEOF));
//                            break;
//                        default:
//                            errorContext = true;
//                            break;
//                        }
//                        break;

                    case CASE:
                        pushExp(createTokenExp(CASE));
                        break;

                    case FOR:
                    case IF:
                    case SWITCH:
                    case WHILE:
                        if (topID == NO_EXP) {
                            pushExp(createTokenExp(tokenID2OpenExpID(tokenID)));
                        } else {
                            errorState = true;
                        }
                        break;
//                    case EXTENDS:
//                        if (topID == GENERIC_WILD_CHAR)
//                            break;

                    case IDENTIFIER: // identifier found e.g. 'a'
                         {
                            switch (topID) {
                                case OPERATOR:
                                case DOT_OPEN:
                                case ARROW_OPEN:
                                case SCOPE_OPEN:
                                case ARRAY_OPEN:
                                case PARENTHESIS_OPEN:
                                case SPECIAL_PARENTHESIS_OPEN:
                                case METHOD_OPEN:
                                case MEMBER_POINTER_OPEN:
                                case NEW:
                                case GOTO:
//                            case CPPINCLUDE:
                                case CONVERSION:
                                case UNARY_OPERATOR:
                                case MEMBER_POINTER:
                                case INSTANCEOF:
                                case NO_EXP:
                                case GENERIC_TYPE_OPEN:
                                case ANNOTATION:
                                case ANNOTATION_OPEN:
                                case CASE:
                                case CLASSIFIER:
                                case CONVERSION_OPEN:
                                case TYPE_PREFIX:
                                    pushExp(createTokenExp(VARIABLE));
                                    break;
                                case TERNARY_OPERATOR:
                                    popExp();
                                    top = peekExp();
                                    while (getValidExpID(top) == VARIABLE
                                            || getValidExpID(top) == OPERATOR
                                            || getValidExpID(top) == CONSTANT) {
                                        popExp();
                                        top = peekExp();
                                    }                                    
                                    pushExp(createTokenExp(VARIABLE));
                                    break;

                                case GENERIC_WILD_CHAR:
                                    top.setExpID(VARIABLE);
                                    addTokenTo(top);
                                    break;

                                case TYPE:
                                    if (getValidExpID(peekExp2()) != METHOD_OPEN) {
                                        popExp();
                                        pushExp(createTokenExp(VARIABLE));
                                        break;
                                    } else {
                                        popExp(); // top
                                        CsmCompletionExpression var = createTokenExp(VARIABLE);
                                        var.addParameter(top);
                                        pushExp(var);
                                        break;
                                    }
                                // no break;
                                case VARIABLE:
                                    if (getValidExpID(peekExp2()) == METHOD_OPEN) {
                                        //top.setExpID(VARIABLE);
                                        addTokenTo(top);
                                    //pushExp(createTokenExp(VARIABLE));
                                    // TODO: need to create parameter, we know, that METHOD_OPEN is declaration/definition of method
                                        break;
                                    }
                                    int cnt = expStack.size();
                                    CsmCompletionExpression gen = null;
                                    for (int i = 0; i < cnt; i++) {
                                        CsmCompletionExpression expr = peekExp(i + 1);
                                        if (expr.getExpID() == GENERIC_TYPE_OPEN) {
                                            gen = expr;
                                            break;
                                        }
                                    }
                                    if (gen != null) {
                                        pushExp(createTokenExp(VARIABLE));
                                        break;
                                    }
                                    errorState = true;
                                    break;
                                case TYPE_REFERENCE:
                                    if (getValidExpID(peekExp2()) == METHOD_OPEN) {
                                        //top.setExpID(VARIABLE);
                                        //addTokenTo(top);
                                        popExp(); // top
                                        CsmCompletionExpression var = createTokenExp(VARIABLE);
                                        var.addParameter(top);
                                        pushExp(var);
                                    // TODO: need to create parameter, we know, that METHOD_OPEN is declaration/definition of method
                                    } else {
                                        errorState = true;
                                    }
                                    break;
                                case PREPROC_DIRECTIVE_OPEN:
                                    top.setExpID(PREPROC_DIRECTIVE);
                                    top.addParameter(createTokenExp(VARIABLE));
                                    break;
                                default:
                                    errorState = true;
                                    break;
                            }
                        }
                        break;

                    case QUESTION:
                        nrQuestions++;
                        CsmCompletionExpression ternary = new CsmCompletionExpression(TERNARY_OPERATOR);
                        switch (topID) {
                            case CONSTANT:
                            case VARIABLE:
                            case METHOD:
                            case CONSTRUCTOR:
                            case ARRAY:
                            case DOT:
                            case ARROW:
                            case SCOPE:
                            case PARENTHESIS:
                            case UNARY_OPERATOR:
                            case MEMBER_POINTER:
                            case GENERIC_TYPE_OPEN:
                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
                            case SPECIAL_PARENTHESIS_OPEN:
                            case MEMBER_POINTER_OPEN:
                            case OPERATOR:
                                popExp();
                                ternary.addParameter(top);
                                break;
                            default:
                                errorState = true;
                                break;
                        }
                        pushExp(ternary);
                        break;

                    case STAR:
                    case AMPAMP:
                    case AMP: {
                        if (tokenID != CppTokenId.AMPAMP || supportTemplates) {
                            boolean pointer = false;
                            // special handling of *, & and && because it can be not operator
                            // while dereference and address-of expression
                            // try to handle it the same ways as UNARY_OPERATOR
                            switch (topID) {
                                case GENERIC_TYPE_OPEN:
                                case MEMBER_POINTER_OPEN: // next is operator as well
                                case METHOD_OPEN:
                                case ARRAY_OPEN:
                                case PARENTHESIS_OPEN:
                                case SPECIAL_PARENTHESIS_OPEN:
                                //case OPERATOR: ??? collision with usual operator behavior
                                //case UNARY_OPERATOR:
                                case NO_EXP:
                                case CONVERSION:
                                    // member pointer operator
                                    CsmCompletionExpression opExp = createTokenExp(MEMBER_POINTER_OPEN);
                                    pushExp(opExp); // add operator as new exp
                                    pointer = true;
                                    break;
                                case TYPE:
                                case TYPE_REFERENCE:
                                case GENERIC_TYPE:
                                case SCOPE_OPEN:
                                    // we have type or type reference and then * or &,
                                    // join into TYPE_REFERENCE
                                    popExp();
                                    CsmCompletionExpression exp = createTokenExp(TYPE_REFERENCE);
                                    exp.addParameter(top);
                                    pushExp(exp);
                                    pointer = true;
                                    break;
                                case OPERATOR:
                                    if ((top.getTokenCount() == 1 && isEqOperator(top.getTokenID(0))) ||
                                            (top.getTokenID(0) == CppTokenId.COLON)) {
                                        // member pointer operator
                                        CsmCompletionExpression memPtrExp = createTokenExp(MEMBER_POINTER_OPEN);
                                        pushExp(memPtrExp); // add operator as new exp
                                        pointer = true;
                                    }
                                    break;
                            }
                            if (pointer) {
                                break;
                            } else {
                                // else "nobreak" to allow to be handled as normal operators
                            }
                        }
                    }
                    case EQ: // Assignment operators
                    case PLUSEQ:
                    case MINUSEQ:
                    case STAREQ:
                    case SLASHEQ:
                    case AMPEQ:
                    case BAREQ:
                    case CARETEQ:
                    case PERCENTEQ:
                    case LTLTEQ:
                    case GTGTEQ:
//                    case RUSHIFTEQ:

                    case LTEQ:
                    case GTEQ:
                    case EQEQ:
                    case NOTEQ:

                    case BARBAR:// Binary, result is boolean

                    case LTLT: // Always binary
                    case SLASH:
                    case BAR:
                    case CARET:
                    case PERCENT:

                    case COLON:
                        // Operator handling
                        switch (topID) {
                            case CONSTANT:
                            case VARIABLE:
                            case METHOD:
                            case CONSTRUCTOR:
                            case ARRAY:
                            case DOT:
                            case ARROW:
                            case SCOPE:
                            case PARENTHESIS:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case MEMBER_POINTER:
                                pushExp(createTokenExp(OPERATOR));
                                break;

                            case TYPE:
                            case TYPE_REFERENCE:
                                if (tokenID == CppTokenId.STAR || tokenID == CppTokenId.AMP) {// '*' or '&' as type reference
                                    pushExp(createTokenExp(OPERATOR));
                                    break;
                                }
                                if (tokenID == CppTokenId.EQ) {// function param = value
                                    pushExp(createTokenExp(OPERATOR));
                                    break;
                                }
                            // else flow to errorContextor
                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case LT: {
                        boolean genericType = false;
                        if (supportTemplates) { // special treatment of Java 1.5 features
                            switch (topID) {
                                case VARIABLE:
                                case DOT:
                                case ARROW:
                                case SCOPE:
                                case MEMBER_POINTER:
                                    popExp(); // pop the top expression
                                    CsmCompletionExpression genExp = createTokenExp(GENERIC_TYPE_OPEN);
                                    genExp.addParameter(top);
                                    pushExp(genExp);
                                    genericType = true; // handled successfully as generic type
                                    break;

                                default:
                                    // could possibly still be acceptable as operator '<'
                                    break;
                            }
                        }

                        if (topID == CONVERSION_OPEN) {
                            addTokenTo(top);
                            break;
                        }

                        if (!errorState && !genericType) { // not generics -> handled compatibly
                            // Operator handling
                            switch (topID) {
                                case CONSTANT:
                                case VARIABLE:
                                case METHOD:
                                case CONSTRUCTOR:
                                case ARRAY:
                                case DOT:
                                case ARROW:
                                case SCOPE:
                                case PARENTHESIS:
                                case OPERATOR:
                                case UNARY_OPERATOR:
                                case MEMBER_POINTER:
                                case CONVERSION:
                                    pushExp(createTokenExp(OPERATOR));
                                    break;

                                default:
                                    errorState = true;
                                    break;
                            }
                        }
                        break;
                    }

                    case GT: // ">"
                    {
                        boolean genericType = false;
                        if (supportTemplates) { // special treatment of Java 1.5 features
                            switch (topID) {
                                case CONSTANT: // check for "List<const" plus ">" case
                                case VARIABLE: // check for "List<var" plus ">" case
                                case TYPE: // check for "List<int" plus ">" case
                                case TYPE_REFERENCE: // check for "List<int*" plus ">" case
                                case DOT: // check for "List<var1.var2" plus ">" case
                                case ARROW: // check for "List<var1.var2" plus ">" case
                                case SCOPE: // check for "List<NS::Class" plus ">" case
                                case GENERIC_TYPE: // check for "List<HashMap<String, Integer>" plus ">" case
                                case GENERIC_TYPE_OPEN: // check for "List<" plus ">" case
                                case GENERIC_WILD_CHAR: // check for "List<?" plus ">" case
                                case ARRAY: // check for "List<String[]" plus ">" case
                                case PARENTHESIS: // check for "T<(1+1)" plus ">" case
                                case METHOD: // check for "T<func(param)" plus ">" case
                                case UNARY_OPERATOR:
                                    int cnt = expStack.size();
                                    CsmCompletionExpression gen = null;
                                    for (int i = 0; i < cnt; i++) {
                                        CsmCompletionExpression expr = peekExp(i + 1);
                                        if (expr.getExpID() == PARENTHESIS_OPEN) {
                                            break;
                                        }
                                        if (expr.getExpID() == GENERIC_TYPE_OPEN) {
                                            gen = expr;
                                            break;
                                        }
                                    }
                                    if (gen != null) {
                                        while (peekExp().getExpID() != GENERIC_TYPE_OPEN) {
                                            gen.addParameter(popExp());
                                        }
                                        gen.setExpID(GENERIC_TYPE);

                                        top = gen;
                                        genericType = true;

                                        // It seems that bugs 159068 and 159054 are now handled by other branches,
                                        // so this fix is used for cases as in bug 230079
                                        
                                        // IZ#159068 : Unresolved ids in instantiations after &
                                        // IZ#159054 : Unresolved id in case of reference to template as return type
                                        if (gen.getParameterCount() > 0) {
                                            CsmCompletionExpression param = gen.getParameter(0);
                                            if (param.getParameterCount() > 0) {
                                                switch (param.getExpID()) {
                                                    case MEMBER_POINTER: // check for "&List<...>" case
                                                        CsmCompletionExpression newGen = createTokenExp(GENERIC_TYPE);
                                                        newGen.addParameter(param.getParameter(0));
                                                        for (int i = 1; i < gen.getParameterCount(); i++) {
                                                            newGen.addParameter(gen.getParameter(i));
                                                        }

                                                        top = newGen;
                                                        
                                                        // pop generic type
                                                        popExp();          
                                                        
                                                        // push MEMBER_POINTER_OPEN (it must be reduced to MEMBER_POINTER later)
                                                        pushExp(createTokenExp(MEMBER_POINTER_OPEN, param)); 
                                                        
                                                        // push new GENERIC_TYPE (without MEMBER_POINTER)
                                                        pushExp(newGen);
                                                        
                                                    default:
                                                }
                                            }
                                        }
                                    }
                                    break;

                                default:
                                    // Will be handled as operator
                                    break;
                            }
                        }

                        boolean conversion = false;
                        if (!errorState && !genericType) {
                            CsmCompletionExpression top2 = peekExp2();
                            switch (getValidExpID(top2)) {
                                case CLASSIFIER:
                                    CsmCompletionExpression top3 = peekExp(3);
                                    if (getValidExpID(top3) == CONVERSION_OPEN && CsmCompletionExpression.isValidType(top)) {
                                        popExp();
                                        popExp();
                                        top3.addParameter(top);
                                        top3.addParameter(top2);
                                        addTokenTo(top3);

                                        conversion = true;
                                    }
                                    break;
                                case CONVERSION_OPEN:
                                    if (CsmCompletionExpression.isValidType(top)) {
                                        popExp();
                                        top2.addParameter(top);
                                        addTokenTo(top2);

                                        conversion = true;
                                    }
                                    break;
                            }
                        }

                        if (!errorState && !genericType && !conversion) { // not generics - handled compatibly
                            // Operator handling
                            switch (topID) {
                                case CONSTANT:
                                case VARIABLE: // List<String
                                case METHOD:
                                case CONSTRUCTOR:
                                case ARRAY:
                                case DOT:
                                case ARROW:
                                case SCOPE:
                                case PARENTHESIS:
                                case OPERATOR:
                                case UNARY_OPERATOR:
                                case MEMBER_POINTER:
                                    pushExp(createTokenExp(OPERATOR));
                                    break;

                                default:
                                    errorState = true;
                                    break;
                            }
                        }
                        break;
                    }

                    case GTGT: // ">>"
                    {
                        boolean genericType = false;
                        if (supportTemplates) { // special treatment of C++ template features
                            switch (topID) {
                                case CONSTANT: // check for "List<const" plus ">" case
                                case VARIABLE: // check for "List<var" plus ">" case
                                case TYPE: // check for "List<int" plus ">" case
                                case DOT: // check for "List<var.var2" plus ">" case
                                case ARROW: // check for "List<var.var2" plus ">" case
                                case SCOPE: // check for "List<NS::Class" plus ">" case
                                case GENERIC_TYPE: // check for "List<HashMap<String, Integer>" plus ">" case
                                case GENERIC_TYPE_OPEN: // chack for "List<" plus ">" case
                                case GENERIC_WILD_CHAR: // chack for "List<?" plus ">" case
                                case ARRAY: // chack for "List<String[]" plus ">" case
                                case PARENTHESIS: // chack for "T<(1+1)" plus ">" case
                                    int cnt = expStack.size();
                                    CsmCompletionExpression genTop = null;
                                    CsmCompletionExpression genBottom = null;
                                    for (int i = 0; i < cnt; i++) {
                                        CsmCompletionExpression expr = peekExp(i + 1);
                                        if (expr.getExpID() == GENERIC_TYPE_OPEN) {
                                            CsmCompletionExpression expr2 = peekExp(i + 2);
                                            if (getValidExpID(expr2) == GENERIC_TYPE_OPEN) {
                                                genTop = expr;
                                                genBottom = expr2;
                                                break;
                                            }
                                        }
                                    }
                                    if (genTop != null && genBottom != null) {
                                        while (peekExp().getExpID() != GENERIC_TYPE_OPEN) {
                                            genTop.addParameter(popExp());
                                        }
                                        genTop.setExpID(GENERIC_TYPE);
                                        popExp();
                                        genBottom.addParameter(genTop);
                                        genBottom.setExpID(GENERIC_TYPE);
                                        top = genBottom;

                                        genericType = true;
                                    }
                                    break;

                                default:
                                    // Will be handled as operator
                                    break;
                            }
                        }


                        if (!errorState && !genericType) { // not generics - handled compatibly
                            // Operator handling
                            switch (topID) {
                                case CONSTANT:
                                case VARIABLE: // List<String
                                case METHOD:
                                case CONSTRUCTOR:
                                case ARRAY:
                                case DOT:
                                case ARROW:
                                case SCOPE:
                                case PARENTHESIS:
                                case OPERATOR:
                                case UNARY_OPERATOR:
                                case MEMBER_POINTER:
                                case CONVERSION: // (a)>>1
                                    pushExp(createTokenExp(OPERATOR));
                                    break;

                                default:
                                    errorState = true;
                                    break;
                            }
                        }
                        break;
                    }

//                    case RUSHIFT: // ">>>"
//                        {
//                            boolean genericType = false;
//                            if (java15) { // special treatment of Java 1.5 features
//                                switch (topID) {
//                                    case VARIABLE: // check for "List<var" plus ">" case
//                                    case DOT: // check for "List<var.var2" plus ">" case
//                                    case GENERIC_TYPE: // check for "List<HashMap<String, Integer>" plus ">" case
//                                    case GENERIC_WILD_CHAR: // chack for "List<?" plus ">" case
//                                    case ARRAY: // chack for "List<String[]" plus ">" case
//                                        CsmCompletionExpression top2 = peekExp2();
//                                        switch (getValidExpID(top2)) {
//                                            case GENERIC_TYPE_OPEN:
//                                                // Check whether outer is open as well
//                                                CsmCompletionExpression top3 = peekExp(3);
//                                                CsmCompletionExpression top4 = peekExp(4);
//                                                if (getValidExpID(top3) == GENERIC_TYPE_OPEN
//                                                    && getValidExpID(top4) == GENERIC_TYPE_OPEN
//                                                ) {
//                                                    genericType = true;
//                                                    popExp();
//                                                    top2.addParameter(top);
//                                                    top2.setExpID(GENERIC_TYPE);
//                                                    addTokenTo(top2); // [TODO] revise possible spliting of the token
//
//                                                    popExp();
//                                                    top3.addParameter(top2);
//                                                    top3.setExpID(GENERIC_TYPE);
//                                                    addTokenTo(top3); // [TODO] revise possible spliting of the token
//
//                                                    popExp();
//                                                    top4.addParameter(top3);
//                                                    top4.setExpID(GENERIC_TYPE);
//                                                    addTokenTo(top4); // [TODO] revise possible spliting of the token
//
//                                                    top = top4;
//
//                                                } else { // inner is not generic type
//                                                    errorContext = true;
//                                                }
//                                                break;
//
//                                            default:
//                                                errorContext = true;
//                                                break;
//                                        }
//                                        break;
//
//                                    default:
//                                        // Will be handled as operator
//                                        break;
//                                }
//                            }
//
//
//                            if (!errorContext && !genericType) { // not generics - handled compatibly
//                                // Operator handling
//                                switch (topID) {
//                                    case CONSTANT:
//                                    case VARIABLE: // List<String
//                                    case METHOD:
//                                    case CONSTRUCTOR:
//                                    case ARRAY:
//                                    case DOT:
//                                    case PARENTHESIS:
//                                    case OPERATOR:
//                                    case UNARY_OPERATOR:
//                                        pushExp(createTokenExp(OPERATOR));
//                                        break;
//
//                                    default:
//                                        errorContext = true;
//                                        break;
//                                }
//                            }
//                            break;
//                        }



                    case PLUSPLUS: // Prefix or postfix
                    case MINUSMINUS:
                        switch (topID) {
                            case GENERIC_TYPE_OPEN:
                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
                            case SPECIAL_PARENTHESIS_OPEN:
                            case MEMBER_POINTER_OPEN:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case MEMBER_POINTER:
                            case NO_EXP:
                                // Prefix operator
                                CsmCompletionExpression opExp = createTokenExp(UNARY_OPERATOR);
                                pushExp(opExp); // add operator as new exp
                                break;

                            case VARIABLE: // is it only one permitted?
                                // Postfix operator
                                opExp = createTokenExp(UNARY_OPERATOR);
                                popExp(); // pop top
                                opExp.addParameter(top);
                                pushExp(opExp);
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case PLUS: // Can be unary or binary
                    case MINUS:
                        switch (topID) {
                            case CONSTANT:
                            case VARIABLE:
                            case METHOD:
                            case CONSTRUCTOR:
                            case ARRAY:
                            case DOT:
                            case ARROW:
                            case SCOPE:
                            case PARENTHESIS:
                            case UNARY_OPERATOR:
                            case TERNARY_OPERATOR:
                            case MEMBER_POINTER:
                                CsmCompletionExpression opExp = createTokenExp(OPERATOR);
                                pushExp(opExp);
                                break;

                            case GENERIC_TYPE_OPEN:
                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
                            case SPECIAL_PARENTHESIS_OPEN:
                            case MEMBER_POINTER_OPEN:
                            case OPERATOR:
                            case CONVERSION:
                            case NO_EXP:
                                // Unary operator
                                opExp = createTokenExp(UNARY_OPERATOR);
                                pushExp(opExp); // add operator as new exp
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;


                    case TILDE: // Always unary
                    case NOT:
                        switch (topID) {
                            case GENERIC_TYPE_OPEN:
                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
                            case SPECIAL_PARENTHESIS_OPEN:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case MEMBER_POINTER:
                            case MEMBER_POINTER_OPEN:
                            case NO_EXP: {
                                // Unary operator
                                CsmCompletionExpression opExp = createTokenExp(UNARY_OPERATOR);
                                pushExp(opExp); // add operator as new exp
                                break;
                            }
                            case DOT_OPEN:
                            case ARROW_OPEN:
                            case SCOPE_OPEN: {
                                if (tokenID == CppTokenId.TILDE) {
                                    // this is ~ of destructor, will be handled later in checkJoin
                                    CsmCompletionExpression opExp = createTokenExp(UNARY_OPERATOR);
                                    pushExp(opExp); // add operator as new exp
                                    break;
                                } else {
                                    // flow down to errorContextor
                                }
                            }
                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case DOT: // '.' found
                    case DOTMBR: // '.*' found
                    case ARROW: // '->' found
                    case ARROWMBR: // '->*' found
                    case SCOPE: // '::' found
                        switch (topID) {
                            case CONSTANT:
                            case VARIABLE:
                            case ARRAY:
                            case METHOD:
                            case CONSTRUCTOR:
                            case PARENTHESIS:
                            case CONVERSION:
                            case GENERIC_TYPE:
                            case MEMBER_POINTER: {
                                popExp();
                                // tokenID.getNumericID() is the parameter of the main switch
                                // create correspondent *_OPEN expression ID
                                int openExpID = tokenID2OpenExpID(tokenID);
                                CsmCompletionExpression opExp = createTokenExp(openExpID);
                                if (topID == CONVERSION && tokenID != CppTokenId.SCOPE) {
                                    // Now we know that previous exp is PARENTHESIS, not CONVERSION.
                                    top.setExpID(PARENTHESIS);
                                }
                                opExp.addParameter(top);
                                pushExp(opExp);
                                break;
                            }
                            case DOT:
                                addTokenTo(top);
                                top.setExpID(tokenID2OpenExpID(tokenID));
                                break;

                            case ARROW:
                                addTokenTo(top);
                                top.setExpID(tokenID2OpenExpID(tokenID));
                                break;

                            case SCOPE:
                                addTokenTo(top);
                                top.setExpID(tokenID2OpenExpID(tokenID));
                                break;

                            case GENERIC_TYPE_OPEN:
                            case METHOD_OPEN:
                            case PARENTHESIS_OPEN:
                            case SPECIAL_PARENTHESIS_OPEN:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case TYPE_PREFIX:
                            case NO_EXP: // alone :: is OK as access to global context
                                CsmCompletionExpression emptyVar = CsmCompletionExpression.createEmptyVariable(curTokenPosition);
                                int openExpID = tokenID2OpenExpID(tokenID);
                                CsmCompletionExpression opExp = createTokenExp(openExpID);
                                opExp.addParameter(emptyVar);
                                pushExp(opExp);
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case COMMA: // ',' found
                        switch (topID) {
                            case ARRAY:
                            case DOT:
                            case ARROW:
                            case SCOPE:
                            case TYPE:
                            case TYPE_REFERENCE:
                            case CONSTANT:
                            case VARIABLE: // can be "List<String" plus "," state
                            case CONSTRUCTOR:
                            case CONVERSION:
                            case PARENTHESIS:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case MEMBER_POINTER:
                            case INSTANCEOF:
                            case METHOD:
                            case GENERIC_TYPE: // can be "HashMap<List<String>" plus "," state
                            case GENERIC_WILD_CHAR: // chack for "HashMap<?" plus "," case
                                CsmCompletionExpression top2 = peekExp2();
                                switch (getValidExpID(top2)) {
                                    case METHOD_OPEN:
                                        popExp();
                                        top2.addParameter(top);
                                        top = top2;
                                        break;

                                    case ANNOTATION_OPEN:
                                        popExp();
                                        top2.addParameter(top);
                                        addTokenTo(top2);
                                        top = top2;
                                        break;

                                    case PARENTHESIS_OPEN:
                                        popExp();
                                        top2.addParameter(top);
                                        top = top2;
                                        break;

                                    default:
                                        int cnt = expStack.size();
                                        CsmCompletionExpression gen = null;
                                        for (int i = 0; i < cnt; i++) {
                                            CsmCompletionExpression expr = peekExp(i + 1);
                                            if (expr.getExpID() == GENERIC_TYPE_OPEN) {
                                                gen = expr;
                                                break;
                                            }
                                        }
                                        if (gen != null) {
                                            while (peekExp().getExpID() != GENERIC_TYPE_OPEN) {
                                                gen.addParameter(popExp());
                                            }
                                            top = gen;
                                            break;
                                        }

                                        errorState = true;
                                        break;
                                }
                                break;

                            case METHOD_OPEN:
                                addTokenTo(top);
                                break;

                            default:
                                errorState = true;
                                break;

                        }
                        break;

                    case SEMICOLON:
                        errorState = true;
                        break;

                    case LPAREN:
                        switch (topID) {
                            case VARIABLE:
                            case GENERIC_TYPE:
                                popExp();
                                CsmCompletionExpression top2 = peekExp();
                                int top2ID = getValidExpID(top2);
                                switch (top2ID) {
                                    case ANNOTATION:
                                        top2.setExpID(ANNOTATION_OPEN);
                                        top2.addParameter(top);
                                        break;
                                    case DOT_OPEN:
                                    case ARROW_OPEN:
                                    case SCOPE_OPEN:
                                    {
                                        CsmCompletionExpression top3 = peekExp2();
                                        if (getValidExpID(top3) == ANNOTATION) {
                                            top2.setExpID(openExpID2ExpID(top2ID)); // *_OPEN => *, use value of case
                                            top2.addParameter(top);
                                            top3.setExpID(ANNOTATION_OPEN);
                                            top3.addParameter(top2);
                                            popExp();
                                            break;
                                        }
                                        // nobreak
                                    }
                                    default:
                                        CsmCompletionExpression mtdOpExp = createTokenExp(METHOD_OPEN);
                                        mtdOpExp.addParameter(top);
                                        pushExp(mtdOpExp);
                                }
                                break;

                            case ARRAY: // a[0](
                                popExp();
                                CsmCompletionExpression mtdExp = createTokenExp(METHOD);
                                mtdExp.addParameter(top);
                                pushExp(mtdExp);
                                break;

                            case DECLTYPE_OPEN:    // decltype(                                
                            case ARRAY_OPEN:       // a[(
                            case PARENTHESIS_OPEN: // ((
                            case SPECIAL_PARENTHESIS_OPEN: // if((
                            case METHOD_OPEN:      // a((
                            case NO_EXP:
                            case OPERATOR:         // 3+(
                            case CONVERSION:       // (int)(
                            case CONVERSION_OPEN:  // static_cast<int>(
                            case PARENTHESIS:      // if (a > b) (
                            case GENERIC_TYPE_OPEN:// a < (
                            case MEMBER_POINTER_OPEN:// *(
                            case UNARY_OPERATOR: // !(
                            case TYPE: // int(a*)()                                
                                pushExp(createTokenExp(PARENTHESIS_OPEN));
                                break;

                            case METHOD: // a()(
                                popExp();
                                CsmCompletionExpression mtdOpExp = createTokenExp(METHOD_OPEN);
                                mtdOpExp.addParameter(top);
                                pushExp(mtdOpExp);
                                break;

                            case IF:
                            case FOR:
                            case SWITCH:
                            case WHILE:
                                popExp();
                                pushExp(createTokenExp(SPECIAL_PARENTHESIS_OPEN));
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case RPAREN:
                        boolean mtd = false;
                        switch (topID) {
                            case CONSTANT:
                            case VARIABLE:
                            case ARRAY:
                            case DOT:
                            case ARROW:
                            case SCOPE:
                            case TYPE:
                            case CONSTRUCTOR:
                            case CONVERSION:
                            case PARENTHESIS:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case TERNARY_OPERATOR:
                            case MEMBER_POINTER:
                            case TYPE_REFERENCE:
                            case INSTANCEOF:
                            case METHOD:
                            case GENERIC_TYPE:
                                CsmCompletionExpression top2 = peekExp2();
                                CsmCompletionExpression top3;
                                switch (getValidExpID(top2)) {
                                    case CLASSIFIER:
                                        top3 = peekExp(3);
                                        if (getValidExpID(top3) == PARENTHESIS_OPEN && CsmCompletionExpression.isValidType(top)) {
                                            popExp();
                                            popExp();
                                            top3.addParameter(top);
                                            top3.addParameter(top2);
                                            top3.setExpID(CONVERSION);
                                            addTokenTo(top3);
                                        } else if(getValidExpID(top3) == METHOD_OPEN) {
                                            popExp();
                                            popExp();
                                            top3.addParameter(top);
                                            top = top3;
                                            mtd = true;
                                            break;
                                        }
                                        break;
                                    case PARENTHESIS_OPEN:
                                        top3 = peekExp(3);
                                        if (getValidExpID(top3) == CONVERSION_OPEN) {
                                            popExp();
                                            popExp();
                                            popExp();

                                            top3.addParameter(top);
                                            top3.setExpID(CONVERSION);

                                            top2.addParameter(top3);
                                            top2.setExpID(PARENTHESIS);
                                            top = top2;

                                            pushExp(top);
                                        } else if (getValidExpID(top3) == DECLTYPE_OPEN) {
                                            popExp();
                                            popExp();
                                            popExp();
                                            
                                            top2.addParameter(top);
                                            top2.setExpID(DECLTYPE);                                            

                                            top3.addParameter(top2);
                                            top3.setExpID(PARENTHESIS);

                                            top = top3;

                                            pushExp(top);                                              
                                        } else {
                                            popExp();
                                            top2.addParameter(top);
                                            if (top2.getParameterCount() == 1 && CsmCompletionExpression.isValidType(top)
                                                    && getValidExpID(top3) != PARENTHESIS && getValidExpID(top3) != TYPE) {
                                                top2.setExpID(CONVERSION);
                                            } else {
                                                top2.setExpID(PARENTHESIS);
                                            }
                                            addTokenTo(top2);
                                        }
                                        break;

                                    case SPECIAL_PARENTHESIS_OPEN:
                                        popExp();
                                        popExp();
                                        break;

                                    case GENERIC_TYPE_OPEN:
                                        popExp();
                                        top2.setExpID(OPERATOR);
                                        top2.addParameter(top);
                                        top = top2;
                                        top2 = peekExp2();
                                        // revert #223298 - Wrong recognition of function
//                                        if (getValidExpID(top2) == DOT_OPEN) {
//                                            popExp();
//                                            top2.addParameter(top);
//                                            top = top2;
//                                        }
//                                        top2 = peekExp2();
                                        if (getValidExpID(top2) != METHOD_OPEN) {
                                            break;
                                        }

                                    case METHOD_OPEN:
                                        popExp();
                                        top2.addParameter(top);
                                        top = top2;
                                        mtd = true;
                                        break;

                                    case CONVERSION:
                                        popExp();
                                        top2.addParameter(top);
                                        top = top2;
                                        top2 = peekExp2();
                                        switch (getValidExpID(top2)) {
                                            case PARENTHESIS_OPEN:
                                                popExp();
                                                top2.addParameter(top);
                                                top2.setExpID(PARENTHESIS);
                                                top = top2;
                                                break;

                                            case METHOD_OPEN:
                                                popExp();
                                                top2.addParameter(top);
                                                top = top2;
                                                mtd = true;
                                                break;
                                        }
                                        break;

                                    case MEMBER_POINTER_OPEN:
                                        popExp();
                                        top2.addParameter(top);
                                        top2.setExpID(MEMBER_POINTER);
                                        top = top2;
                                        top2 = peekExp2();
                                        switch (getValidExpID(top2)) {
                                            case PARENTHESIS_OPEN:
                                                popExp();
                                                top2.addParameter(top);
                                                top2.setExpID(PARENTHESIS);
                                                top = top2;
                                                break;

                                            case METHOD_OPEN:
                                                popExp();
                                                top2.addParameter(top);
                                                top = top2;
                                                mtd = true;
                                                break;
                                        }
                                        break;

                                    default:
                                        errorState = true;
                                        break;
                                }
                                break;

                            case METHOD_OPEN:
                                mtd = true;
                                break;

                            case PARENTHESIS_OPEN: // empty parenthesis
                                popExp();
                                break;
                                
                            default:
                                errorState = true;
                                break;
                        }

                        if (mtd) {
                            addTokenTo(top);
                            top.setExpID(METHOD);
                            CsmCompletionExpression top2 = peekExp2();
                            int top2ID = getValidExpID(top2);
                            switch (top2ID) {
                                case DOT_OPEN:
                                case ARROW_OPEN:
                                case SCOPE_OPEN: {
                                    CsmCompletionExpression top3 = peekExp(3);
                                    if (getValidExpID(top3) == NEW) {
                                        popExp(); // pop top
                                        top2.addParameter(top); // add METHOD to DOT
                                        top2.setExpID(openExpID2ExpID(top2ID)); // *_OPEN => *, use value of case
                                        popExp(); // pop top2
                                        top3.setExpID(CONSTRUCTOR);
                                        top3.addParameter(top2); // add DOT to CONSTRUCTOR
                                    }
                                    break;
                                }
                                case NEW:
                                    top2.setExpID(CONSTRUCTOR);
                                    top2.addParameter(top);
                                    popExp(); // pop top
                                    break;
                            }
                        }
                        break;

                    case LBRACKET:
                        switch (topID) {
                            case VARIABLE:
                            case METHOD:
                            case DOT:
                            case ARROW:
                            case SCOPE:
                            case ARRAY:
                            case TYPE: // ... int[ ...
                            case GENERIC_TYPE: // List<String> "["
                            case PARENTHESIS: // ... ((String[]) obj)[ ...
                                popExp(); // top popped
                                CsmCompletionExpression arrExp = createTokenExp(ARRAY_OPEN);
                                arrExp.addParameter(top);
                                pushExp(arrExp);
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case ELLIPSIS:
                        switch (topID) {
                            case VARIABLE:
                            case METHOD:
                            case DOT:
                            case ARRAY:
                            case TYPE: 
                            case GENERIC_TYPE:
                            case PARENTHESIS_OPEN:
                                CsmCompletionExpression exp = createTokenExp(OPERATOR);
                                addTokenTo(exp);
                                top.addParameter(exp);
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case RBRACKET:
                        switch (topID) {
                            case VARIABLE:
                            case METHOD:
                            case DOT:
                            case ARROW:
                            case SCOPE:
                            case ARRAY:
                            case PARENTHESIS:
                            case CONSTANT:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case MEMBER_POINTER:
                            case INSTANCEOF:
                            case CONVERSION:
                                CsmCompletionExpression top2 = peekExp2();
                                switch (getValidExpID(top2)) {
                                    case ARRAY_OPEN:
                                        CsmCompletionExpression top3 = peekExp(3);
                                        popExp(); // top popped
                                        if (getValidExpID(top3) == NEW) {
                                            popExp(); // top2 popped
                                            top3.setExpID(ARRAY);
                                            top3.addParameter(top2.getParameter(0));
                                            top3.addToken(top2.getTokenID(0), top2.getTokenOffset(0), top2.getTokenText(0));
                                            addTokenTo(top2);
                                        } else {
                                            top2.setExpID(ARRAY);
                                            top2.addParameter(top);
                                            addTokenTo(top2);
                                        }
                                        break;

                                    default:
                                        errorState = true;
                                        break;
                                }
                                break;

                            case ARRAY_OPEN:
                                top.setExpID(ARRAY);
                                addTokenTo(top);
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case LBRACE:
                        if (topID == ARRAY) {
                            CsmCompletionExpression top2 = peekExp2();
                            if (getValidExpID(top2) == NEW) {
                                popExp(); // top popped
                                top2.setExpID(ARRAY);
                                top2.addParameter(top.getParameter(0));
                                top2.addToken(top.getTokenID(0), top.getTokenOffset(0), top.getTokenText(0));
                                top2.addToken(top.getTokenID(1), top.getTokenOffset(1), top.getTokenText(1));
//                                stopped = true;
                                break;
                            }
                        }
                        errorState = true;
                        break;

                    case RBRACE:
                        errorState = true;
                        break;

                    case NEW_LINE:
                        if (topID == PREPROC_DIRECTIVE) {
                            // end line finishes preproc directive
                            popExp();
                        }
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                        // just skip them
                        break;

                    case CHAR_LITERAL:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("char"); // NOI18N
                        break;

                    case RAW_STRING_LITERAL:
                    case STRING_LITERAL:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType(CsmCompletion.CONST_STRING_TYPE.format(true)); // NOI18N
                        break;

                    case INT_LITERAL:
                    case UNSIGNED_LITERAL:
//                    case HEX_LITERAL:
//                    case OCTAL_LITERAL:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("int"); // NOI18N
                        break;

                    case LONG_LITERAL:
                    case LONG_LONG_LITERAL:
                    case UNSIGNED_LONG_LITERAL:
                    case UNSIGNED_LONG_LONG_LITERAL:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("long"); // NOI18N
                        break;

                    case FLOAT_LITERAL:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("float"); // NOI18N
                        break;

                    case DOUBLE_LITERAL:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("double"); // NOI18N
                        break;

                    case TEMPLATE:
                    case TYPENAME:
                    case CONST:
                        // OK, just skip it
                        break;
                        
                    case DECLTYPE:
                        pushExp(createTokenExp(DECLTYPE_OPEN));
                        break;                        

                    default:
                        errorState = true;
                } // end of testing keyword type
            }
        }


        // Check whether a constant or data type keyword was found
        if (constExp != null) {
            switch (topID) {
                case DOT_OPEN:
                case ARROW_OPEN:
                case SCOPE_OPEN:
                case MEMBER_POINTER_OPEN:
                    errorState = true;
                    break;

                case ARRAY_OPEN:
                case PARENTHESIS_OPEN:
                case SPECIAL_PARENTHESIS_OPEN:
                case PARENTHESIS: // can be conversion
                case METHOD_OPEN:
                case ANNOTATION_OPEN:
                case OPERATOR:
                case UNARY_OPERATOR:
                case TERNARY_OPERATOR:
                case MEMBER_POINTER:
                case CONVERSION:
                case NO_EXP:
                    pushExp(constExp);
                    errorState = false;
                    break;

                case GENERIC_TYPE_OPEN:
                    pushExp(constExp);
                    errorState = false;
                    break;

                case CONSTANT:
                    if (CsmCompletion.CONST_STRING_TYPE.format(true).equals(top.getType()) &&
                            CsmCompletion.CONST_STRING_TYPE.format(true).equals(constExp.getType())) {
                        errorState = false;
                    } else {
                        errorState = true;
                    }
                    break;

                default:
                    errorState = true;
                    break;
            }
        }

        if (kwdType != null) { // keyword constant (in conversions)
            switch (topID) {
                case NO_EXP: // declaration started with type name
                case NEW: // possibly new kwdType[]
                case PARENTHESIS_OPEN: // conversion
                {
                    CsmCompletionExpression kwdExp = createTokenExp(TYPE);
                    //addTokenTo(kwdExp);
                    kwdExp.setType(kwdType);
                    pushExp(kwdExp);
                    errorState = false;
                    break;
                }
                case METHOD_OPEN:
                case GENERIC_TYPE_OPEN: {
                    int expType = TYPE;
                    switch (tokenID) {
                        case CONST:
                        case VOLATILE:
                            expType = TYPE_PREFIX;
                    }
                    CsmCompletionExpression kwdExp = createTokenExp(expType);
                    kwdExp.setType(kwdType);
                    pushExp(kwdExp);
                    errorState = false;
                    break;
                }
                case TYPE_PREFIX: {
                    int expType = TYPE;
                    switch (tokenID) {
                        case CONST:
                        case VOLATILE:
                            expType = TYPE_PREFIX;
                    }
                    setExprToTYPE(top);
                    top.setExpID(expType);
                // fallthrough
                }
                case TYPE: {
                    CsmCompletionExpression kwdExp = top;
                    addTokenTo(kwdExp);
                    kwdExp.setType(kwdExp.getType() + " " + kwdType); // NOI18N
                    errorState = false;
                    break;
                }
                case TYPE_REFERENCE: {
                    CsmCompletionExpression kwdExp = createTokenExp(TYPE);
                    kwdExp.setType(kwdType);
                    top.addParameter(kwdExp);
                    errorState = false;
                    break;
                }
                default: // otherwise not recognized
                    if(tokenID != CppTokenId.CONST) {
                        errorState = true;
                    }
                    break;
            }
        }

        if (errorState) {
            clearStack();

            if (tokenID == CppTokenId.IDENTIFIER) {
                pushExp(createTokenExp(VARIABLE));
                errorState = false;
            } else {
                if(!macro) {
                    lastSeparatorOffset = tokenOffset;
                }
            }
        }
    }
    private int lastSeparatorOffset = -1;
    private Boolean inPP;
    public int getLastSeparatorOffset() {
        return lastSeparatorOffset;
    }

    @Override
    public void start(int startOffset, int firstTokenOffset, int lastOffset) {
        inPP = null;
    }

    @SuppressWarnings("fallthrough")
    @Override
    public void end(int offset, int lastTokenOffset) {
        boolean oldSupportTemplates = supportTemplates;
        Boolean oldInPP = inPP;
        int lookaheadSize = lookaheadTokens.size();
        OffsetableToken disableTillToken = isSupportTemplates();
        supportTemplates = disableTillToken == null;
        for (int i = 0; i < lookaheadSize; i++) {
            OffsetableToken currentToken = lookaheadTokens.remove(0);
            inPP = currentToken.inPP;
            tokenImpl(currentToken.token, currentToken.offset, currentToken.macro);
            if(currentToken == disableTillToken) {
                disableTillToken = isSupportTemplates();
                supportTemplates = disableTillToken == null;
            }
        }
        lookaheadTokens.clear();
        supportTemplates = oldSupportTemplates;
        inPP = oldInPP;

        if (lastValidTokenID != null) {
            // if space or comment occurs as last token
            // add empty variable to save last position
            switch (lastValidTokenID) {
                case COLON:
                    if (nrQuestions <= 0) {
                        // error construction
                        break;
                    }
                // else continue, it was (...) ? (...) : (...)
                // break;
                case WHITESPACE:
                case LINE_COMMENT:
                case DOXYGEN_LINE_COMMENT:
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                case SEMICOLON:
                case LBRACE:
                case RBRACE:

                // Operators
                case QUESTION:
                case STAR:
                case AMP:
                case GT:
                    if (getValidExpID(peekExp()) == GENERIC_TYPE) {
                        break;
                    }
                case LT:
                case EQ:
                case PLUSEQ:
                case MINUSEQ:
                case STAREQ:
                case SLASHEQ:
                case AMPEQ:
                case BAREQ:
                case CARETEQ:
                case PERCENTEQ:
                case LTLTEQ:
                case GTGTEQ:
                case LTEQ:
                case GTEQ:
                case EQEQ:
                case NOTEQ:
                case AMPAMP:
                case BARBAR:
                case LTLT:
                case SLASH:
                case BAR:
                case CARET:
                case PERCENT:
                case GTGT:
                case PLUSPLUS:
                case MINUSMINUS:
                case PLUS:
                case MINUS:
                case NOT:
                    pushExp(CsmCompletionExpression.createEmptyVariable(
                            bufferStartPos + bufferOffsetDelta + offset));
                    errorState = false;
                    break;
                default:
                    if (getValidExpID(peekExp()) == GENERIC_TYPE) {
                        pushExp(CsmCompletionExpression.createEmptyVariable(
                                bufferStartPos + bufferOffsetDelta + offset));
                        errorState = false;
                    }
                    break;
            }
        }
        // Check for joins
        boolean reScan = true;
        while (reScan) {
            reScan = false;
            CsmCompletionExpression top = peekExp();
            CsmCompletionExpression top2 = peekExp2();
            int top2ID = getValidExpID(top2);
            if (top != null) {
                switch (getValidExpID(top)) {
                    case VARIABLE:
                        switch (top2ID) {
                            case DOT_OPEN:
                            case ARROW_OPEN:
                            case SCOPE_OPEN:
                                popExp();
                                top2.addParameter(top);
                                top2.setExpID(openExpID2ExpID(top2ID)); // *_OPEN => *, use value of case
                                reScan = true;
                                break;
                            case NEW:
                                popExp();
                                top2.addParameter(top);
                                top2.setExpID(CONSTRUCTOR);
                                reScan = true;
                                break;
                            case GOTO:
                                popExp();
                                top2.addParameter(top);
                                top2.setExpID(LABEL);
                                reScan = false;
                                break;
                            case ANNOTATION:
                            case ANNOTATION_OPEN:
                            case CASE:
                                popExp();
                                top2.addParameter(top);
                                reScan = false; // by default do not nest more - can be changed if necessary
                                break;
                            case GENERIC_TYPE_OPEN: // e.g. "List<String"
                                reScan = false;
                                break;
                        }
                        break;

                    case METHOD_OPEN:
                    // let it flow to METHOD
                    case METHOD:
                        switch (top2ID) {
                            case DOT_OPEN:
                            case ARROW_OPEN:
                            case SCOPE_OPEN:
                                popExp();
                                top2.addParameter(top);
                                top2.setExpID(openExpID2ExpID(top2ID)); // *_OPEN => *, use value of case
                                reScan = true;
                                break;
                            case NEW:
                                popExp();
                                top2.addParameter(top);
                                top2.setExpID(CONSTRUCTOR);
                                reScan = true;
                                break;
                        }
                        break;

                    case DOT:
                    case DOT_OPEN:
                    case ARROW:
                    case ARROW_OPEN:
                    case SCOPE:
                    case SCOPE_OPEN:
                        switch (top2ID) {
                            case NEW:
                                popExp();
                                top2.addParameter(top);
                                top2.setExpID(CONSTRUCTOR);
                                reScan = true;
                                break;
//                    case CPPINCLUDE:
//                        popExp();
//                        top2.addParameter(top);
//                        break;
                            case ANNOTATION:
                            case ANNOTATION_OPEN:
                                popExp();
                                top2.addParameter(top);
                                reScan = false; // by default do not nest more - can be changed if necessary
                                break;
                            case GENERIC_TYPE_OPEN: // e.g. "List<String"
                                reScan = false; // by default do not nest more - can be changed if necessary
                                break;
                            case OPERATOR:
                                CsmCompletionExpression top4 = peekExp(4);
                                if (getValidExpID(top4) == ANNOTATION_OPEN) {
                                    top2.addParameter(peekExp(3));
                                    top2.addParameter(top);
                                    top4.addParameter(top2);
                                    popExp();
                                    popExp();
                                    popExp();
                                    reScan = false;
                                }
                                break;
                            case MEMBER_POINTER_OPEN:
                                popExp();
                                top2.addParameter(top);
                                reScan = false; // by default do not nest more - can be changed if necessary
                                break;
                        }
                        break;
                    case ARRAY_OPEN:
                    case PARENTHESIS_OPEN:
                        pushExp(CsmCompletionExpression.createEmptyVariable(
                                bufferStartPos + bufferOffsetDelta + offset));
                        break;

                    case PREPROC_DIRECTIVE_OPEN:
                        top.addParameter(CsmCompletionExpression.createEmptyVariable(
                                bufferStartPos + bufferOffsetDelta + offset));
                        break;

                    case GENERIC_TYPE_OPEN:
                        if (top.getParameterCount() > 1) {
                            break;
                        }
                    //nobreak
                    case CASE:
//                case CPPINCLUDE:
                        top.addParameter(CsmCompletionExpression.createEmptyVariable(
                                bufferStartPos + bufferOffsetDelta + offset));
                        break;

                    case OPERATOR:
                        if (top2ID == VARIABLE) {
                            CsmCompletionExpression top3 = peekExp(3);
                            if (getValidExpID(top3) == ANNOTATION_OPEN) {
                                top.addParameter(top2);
                                top.addParameter(CsmCompletionExpression.createEmptyVariable(offset));
                                top3.addParameter(top);
                                popExp();
                                popExp();
                            }
                        }
                        break;

                    case MEMBER_POINTER_OPEN:
//                    if (top2ID == NO_EXP) {
                        popExp();
                        pushExp(CsmCompletionExpression.createEmptyVariable(
                                bufferStartPos + bufferOffsetDelta + offset));
//                    }
                        break;
                    case UNARY_OPERATOR:
                        switch (top2ID) {
                            case NO_EXP:
                                popExp();
                                pushExp(CsmCompletionExpression.createEmptyVariable(
                                        bufferStartPos + bufferOffsetDelta + offset));
                                break;
                            case DOT_OPEN:
                            case ARROW_OPEN:
                            case SCOPE_OPEN:
                                if (top.getParameterCount() == 0 &&
                                        top.getTokenCount() == 1 &&
                                        top.getTokenID(0) == CppTokenId.TILDE) {
                                    // this is call of destructor after ., ::, ->
                                    // consider as VARIABLE expression
                                    top.setExpID(VARIABLE);
                                    reScan = true;
                                }
                        }
                        break;
                }
            } else { // nothing on the stack, create empty variable
//                if (!isErrorState()) {
                pushExp(CsmCompletionExpression.createEmptyVariable(
                        bufferStartPos + bufferOffsetDelta + offset));
//                }
            }
        }
    //    System.out.println(this);
    }

//    public void nextBuffer(char[] buffer, int offset, int len,
//                           int startPos, int preScan, boolean lastBuffer) {
//        // System.err.printf("offset = %d, len = %d, startPos = %d, preScan = %d, lastBuffer = %s\n", offset, len, startPos, preScan, lastBuffer ? "true" : "false");
////        this.buffer = new char[len + preScan];
////        System.arraycopy(buffer, offset - preScan, this.buffer, 0, len + preScan);
//        bufferOffsetDelta = preScan - offset;
//        this.bufferStartPos = startPos - preScan;
//    }
    @Override
    public String toString() {
        int cnt = expStack.size();
        StringBuilder sb = new StringBuilder();
//        if (stopped) {
//            sb.append("Parsing STOPPED by request.\n"); // NOI18N
//        }
        sb.append("Stack size is ").append(cnt).append("\n"); // NOI18N
        if (cnt > 0) {
            sb.append("Stack expressions:\n"); // NOI18N
            for (int i = 0; i < cnt; i++) {
                CsmCompletionExpression e = expStack.get(i);
                sb.append("Stack["); // NOI18N
                sb.append(i);
                sb.append("]: "); // NOI18N
                sb.append(e.toString(0));
                sb.append('\n'); //NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isStopped() {
        return false;
    }
    
    private class OffsetableToken {
        Token<TokenId> token;
        int offset;
        boolean macro;
        Boolean inPP;

        public OffsetableToken(Token<TokenId> token, int offset, boolean macro, Boolean inPP) {
            this.token = token;
            this.offset = offset;
            this.macro = macro;
            this.inPP = inPP;
        }

        @Override
        public String toString() {
            return token.id().toString();
        }
    }
    
}

