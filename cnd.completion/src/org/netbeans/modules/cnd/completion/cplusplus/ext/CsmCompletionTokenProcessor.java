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
import java.util.List;
import java.util.ArrayList;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.TokenCategory;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
* Token processor that parses the text and produces jc expressions.
*
* @author Vladimir Voskresensky
* @version 1.00
*/

final class CsmCompletionTokenProcessor implements TokenProcessor {

    private static final int CONSTANT = CsmCompletionExpression.CONSTANT;
    private static final int VARIABLE = CsmCompletionExpression.VARIABLE;
    private static final int OPERATOR = CsmCompletionExpression.OPERATOR;
    private static final int UNARY_OPERATOR = CsmCompletionExpression.UNARY_OPERATOR;
    private static final int DOT = CsmCompletionExpression.DOT;
    private static final int DOT_OPEN = CsmCompletionExpression.DOT_OPEN; 
    private static final int ARROW = CsmCompletionExpression.ARROW;
    private static final int ARROW_OPEN = CsmCompletionExpression.ARROW_OPEN;
    private static final int SCOPE = CsmCompletionExpression.SCOPE;
    private static final int SCOPE_OPEN = CsmCompletionExpression.SCOPE_OPEN;
    private static final int ARRAY_OPEN = CsmCompletionExpression.ARRAY_OPEN;
    private static final int ARRAY = CsmCompletionExpression.ARRAY;
    private static final int PARENTHESIS_OPEN = CsmCompletionExpression.PARENTHESIS_OPEN;
    private static final int PARENTHESIS = CsmCompletionExpression.PARENTHESIS;
    private static final int METHOD_OPEN = CsmCompletionExpression.METHOD_OPEN;
    private static final int METHOD = CsmCompletionExpression.METHOD;
    private static final int CONSTRUCTOR = CsmCompletionExpression.CONSTRUCTOR;
    private static final int CONVERSION = CsmCompletionExpression.CONVERSION;
    private static final int TYPE = CsmCompletionExpression.TYPE;
    private static final int NEW = CsmCompletionExpression.NEW;
    private static final int INSTANCEOF = CsmCompletionExpression.INSTANCEOF;
    private static final int GENERIC_TYPE = CsmCompletionExpression.GENERIC_TYPE;
    private static final int GENERIC_TYPE_OPEN = CsmCompletionExpression.GENERIC_TYPE_OPEN;
    private static final int GENERIC_WILD_CHAR = CsmCompletionExpression.GENERIC_WILD_CHAR;
    private static final int ANNOTATION = CsmCompletionExpression.ANNOTATION;
    private static final int ANNOTATION_OPEN = CsmCompletionExpression.ANNOTATION_OPEN;
//    private static final int CPPINCLUDE = CsmCompletionExpression.CPPINCLUDE;
    private static final int CASE = CsmCompletionExpression.CASE;
    /** "const" as type prefix in the 'const A*'*/
    private static final int TYPE_PREFIX = CsmCompletionExpression.TYPE_PREFIX;
    /** "const" as type postfix in the 'char* const'*/
    private static final int TYPE_POSTFIX = CsmCompletionExpression.TYPE_PREFIX;
    /** "*" or "&" at type postfix in the 'char*' or 'int &'*/
    private static final int TYPE_REFERENCE = CsmCompletionExpression.TYPE_REFERENCE;    
    /** dereference "*" or address-of "&" operators in the '*value' or '&value'*/
    private static final int MEMBER_POINTER = CsmCompletionExpression.MEMBER_POINTER;
    private static final int MEMBER_POINTER_OPEN = CsmCompletionExpression.MEMBER_POINTER_OPEN;
    private static final int NO_EXP = -1;

    /** Buffer that is scanned */
    private char[] buffer;

    /** Start position of the buffer in the document */
    private int bufferStartPos;

    /** Delta of the token processor buffer offsets against the offsets given
    * in the source buffer.
    */
    private int bufferOffsetDelta;

    /** The scanning was stopped by request by the token processor */
    private boolean stopped;

    /** Stack of the expressions. */
    private ArrayList expStack = new ArrayList();

    /** TokenID of the last found token except Syntax.EOT and Syntax.EOL */
    private TokenID lastValidTokenID;

    /** Text of the last found token except Syntax.EOT and Syntax.EOL */
    private String lastValidTokenText;

    private boolean errorState = false;

    // helper variables
    private TokenID curTokenID;
    private int curTokenPosition;
    private String curTokenText;
    
    private int endScanOffset;
    
    private boolean java15;

    CsmCompletionTokenProcessor(int endScanOffset) {
        this.endScanOffset = endScanOffset;
    }

    /**
     * Set whether Java 1.5 features should be enabled.
     *
     * @param java15 true to parse expression as being in java 1.5 syntax.
     */
    void setJava15(boolean java15) {
        this.java15 = java15;
    }
    
    /** Get the expression stack from the bottom to top */
    final List getStack() {
        return expStack;
    }

    /** Get the last token that was processed that wasn't
    * either Syntax.EOT or Syntax.EOL.
    */
    final TokenID getLastValidTokenID() {
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
    
    /** Was the scanning stopped by request by the token processor */
    final boolean isStopped() {
        return stopped;
    }

    final CsmCompletionExpression getResultExp() {
        CsmCompletionExpression result = peekExp();
        return result;
    }

    private void clearStack() {
        expStack.clear();
    }

    /** Push exp to top of stack */
    private void pushExp(CsmCompletionExpression exp) {
        expStack.add(exp);
    }

    /** Pop exp from top of stack */
    private CsmCompletionExpression popExp() {
        int cnt = expStack.size();
        return (cnt > 0) ? (CsmCompletionExpression)expStack.remove(cnt - 1) : null;
    }

    /** Look at the exp at top of stack */
    private CsmCompletionExpression peekExp() {
        int cnt = expStack.size();
        return (cnt > 0) ? (CsmCompletionExpression)expStack.get(cnt - 1) : null;
    }

    /** Look at the second exp on stack */
    private CsmCompletionExpression peekExp2() {
        int cnt = expStack.size();
        return (cnt > 1) ? (CsmCompletionExpression)expStack.get(cnt - 2) : null;
    }

    /** Look at the third exp on stack */
    private CsmCompletionExpression peekExp(int ind) {
        int cnt = expStack.size();
        return (cnt >= ind && cnt > 0) ? (CsmCompletionExpression)expStack.get(cnt - ind) : null;
    }
    
    private CsmCompletionExpression createTokenExp(int id) {
        CsmCompletionExpression exp = new CsmCompletionExpression(id);
        addTokenTo(exp);
        return exp;
    }

    /** Add the token to a given expression */
    private void addTokenTo(CsmCompletionExpression exp) {
        exp.addToken(curTokenID, curTokenPosition, curTokenText);
    }

    private int getValidExpID(CsmCompletionExpression exp) {
        return (exp != null) ? exp.getExpID() : NO_EXP;
    }


    private int tokenID2OpenExpID(int tokenID) {
        switch (tokenID) {
            case CCTokenContext.DOT_ID: // '.' found
            case CCTokenContext.DOTMBR_ID: // '.*' found  
                return DOT_OPEN;
            case CCTokenContext.ARROW_ID: // '->' found
            case CCTokenContext.ARROWMBR_ID: // '->*' found    
                return ARROW_OPEN;
            case CCTokenContext.SCOPE_ID: // '::' found    
                return SCOPE_OPEN;
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
    
    /** Check whether there can be any joining performed
    * for current expressions on the stack.
    * @param tokenID tokenID of the current token
    * @return true to continue, false if errorneous construction found
    */
    private boolean checkJoin(TokenID tokenID) {
        boolean ret = true;

        boolean cont = true;
        while (cont) {
            cont = false;
            CsmCompletionExpression top = peekExp();
            CsmCompletionExpression top2 = peekExp2();
            int top2ID = getValidExpID(top2);
            int topID = getValidExpID(top);
            switch (topID) {
            case VARIABLE:
                boolean stop = false;
                switch (top2ID) {
                    case METHOD_OPEN:
                        switch (tokenID.getNumericID()) {
                            case CCTokenContext.MUL_ID:
                            case CCTokenContext.AND_ID:
                            case CCTokenContext.CONST_ID:
                            case CCTokenContext.IDENTIFIER_ID:
                                top.setExpID(TYPE);
                                stop = true;
                                break;
                        }
                        break;
                }
                if (stop) {
                    break;
                }
                // nobreak;
            case CONSTANT:                
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
                        switch (tokenID.getNumericID()) {
                            case CCTokenContext.MUL_ID:
                            case CCTokenContext.AND_ID:
                            case CCTokenContext.CONST_ID:
                            case CCTokenContext.IDENTIFIER_ID:
                                top.setExpID(TYPE);
                                break;
                        }
                        break;
                    case UNARY_OPERATOR:
                        switch (tokenID.getNumericID()) {
                            case CCTokenContext.DOT_ID:
                            case CCTokenContext.DOTMBR_ID:
                            case CCTokenContext.ARROW_ID:
                            case CCTokenContext.ARROWMBR_ID:    
                            case CCTokenContext.SCOPE_ID:
                            case CCTokenContext.LBRACKET_ID:
                            case CCTokenContext.PLUS_PLUS_ID:
                            case CCTokenContext.MINUS_MINUS_ID:
                                break;

                            case CCTokenContext.LPAREN_ID:
                            {
                                if (topID == VARIABLE && 
                                        top2ID == UNARY_OPERATOR && top2.getParameterCount() == 0 &&
                                        top2.getTokenCount() == 1 && top2.getTokenID(0).getNumericID() == CCTokenContext.NEG_ID &&
                                        top.getParameterCount() == 0 && top.getTokenCount() == 1) {
                                    // we have tilda and variable on top of the stack, this is destructor in fact
                                    // like ~Clazz(
                                    // join into variable
                                    popExp(); // pop VARIABLE
                                    popExp(); // pop '~'

                                    // construct new VARIABLE expression
                                    TokenID curTokenID = top.getTokenID(0);
                                    int curTokenPosition = top2.getTokenOffset(0);
                                    String curTokenText = top2.getTokenText(0) + top.getTokenText(0);
                                    CsmCompletionExpression exp = new CsmCompletionExpression(VARIABLE);
                                    exp.addToken(curTokenID, curTokenPosition, curTokenText);                                
                                    pushExp(exp);
                                }
                            }
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
                        if (tokenID.getCategory() == CCTokenContext.OPERATORS) {
                            switch( tokenID.getNumericID() ) { 
                                case CCTokenContext.LPAREN_ID:
                                    break;
                                default:                            
                                    popExp();
                                    top2.addParameter(top);
                                    top2.setExpID(openExpID2ExpID(top2ID)); // *_OPEN -> * conversion, use value of case
                                    cont = true;
                            }
                        }
                        break;

                    case MEMBER_POINTER_OPEN:    
                        if (tokenID.getCategory() == CCTokenContext.OPERATORS) {
                            switch( tokenID.getNumericID() ) { 
                                case CCTokenContext.LPAREN_ID:
                                case CCTokenContext.SCOPE_ID:
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
                        if (tokenID.getCategory() == CCTokenContext.OPERATORS) {
                            switch (tokenID.getNumericID()) {
                                case CCTokenContext.RPAREN_ID:
                                case CCTokenContext.COMMA_ID:
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
                        if (tokenID.getCategory() == CCTokenContext.OPERATORS) {
                            switch (tokenID.getNumericID()) {
                                case CCTokenContext.RPAREN_ID:
                                case CCTokenContext.MUL_ID:
                                case CCTokenContext.AND_ID:
                                case CCTokenContext.LBRACKET_ID:
                                {
                                    if (topID == OPERATOR && top.getParameterCount() == 0 &&
                                            top.getTokenCount() == 1 && 
                                                (top.getTokenID(0).getNumericID() == CCTokenContext.MUL_ID || 
                                                 top.getTokenID(0).getNumericID() == CCTokenContext.AND_ID)
                                            ) {
                                        // we have variable and then * or &, 
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
                    ArrayList opStack = new ArrayList(); // operator stack
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
                                    rightOp = (CsmCompletionExpression)opStack.remove(opStack.size() - 1); // get stacked op
                                    rightOpID = rightOp.getOperatorID(rightOp);
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
                        CsmCompletionExpression op = (CsmCompletionExpression)opStack.get(i);
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
    
    public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
    int tokenOffset, int tokenLen) {
        
        tokenOffset += bufferOffsetDelta;

        if (tokenID != null){
            TokenCategory category = tokenID.getCategory();
            if (CCTokenContext.KEYWORDS.equals(category)){
                if (tokenOffset+tokenLen+bufferStartPos == endScanOffset)
                    tokenID = CCTokenContext.IDENTIFIER;
            }
        }
        
        // assign helper variables
        if (tokenID != null) {
            lastValidTokenID = tokenID;
        }

        curTokenID = tokenID;
        curTokenPosition = bufferStartPos + tokenOffset;
        // System.err.printf("tokenOffset = %d, tokenLen = %d, tokenID = %s\n", tokenOffset, tokenLen, tokenID == null ? "null" : tokenID.toString());
        curTokenText = new String(buffer, tokenOffset, tokenLen);
        lastValidTokenText = curTokenText;
        errorState = false; // whether the parser cannot understand given tokens
        stopped = false;

        checkJoin(tokenID);

        CsmCompletionExpression top = peekExp(); // exp at top of stack
        int topID = getValidExpID(top); // id of the exp at top of stack
        
        CsmCompletionExpression constExp = null; // possibly assign constant into this exp
        String kwdType = CCTokenContext.isType(tokenID) ? curTokenText : null; // keyword constant type (used in conversions)
        
        // clear stack on absent token or prerpocessor token
        if (tokenID == null || tokenID.getCategory() == CCTokenContext.CPP) {
            errorState = true;
            
        } else { // valid token-id
            int tokenNumID = tokenID.getNumericID();
            if (tokenContextPath.contains(CCTokenContext.contextPath)){
                switch (tokenNumID) { // test the token ID
// XXX
//                    case CCTokenContext.BOOLEAN_ID:
//                        kwdType = JavaCompletion.BOOLEAN_TYPE;
//                        break;
//                    case CCTokenContext.BYTE_ID:
//                        kwdType = JavaCompletion.BYTE_TYPE;
//                        break;
//                    case CCTokenContext.CHAR_ID:
//                        kwdType = JavaCompletion.CHAR_TYPE;
//                        break;
//                    case CCTokenContext.DOUBLE_ID:
//                        kwdType = JavaCompletion.DOUBLE_TYPE;
//                        break;
//                    case CCTokenContext.FLOAT_ID:
//                        kwdType = JavaCompletion.FLOAT_TYPE;
//                        break;
//                    case CCTokenContext.INT_ID:
//                        kwdType = JavaCompletion.INT_TYPE;
//                        break;
//                    case CCTokenContext.LONG_ID:
//                        kwdType = JavaCompletion.LONG_TYPE;
//                        break;
//                    case CCTokenContext.SHORT_ID:
//                        kwdType = JavaCompletion.SHORT_TYPE;
//                        break;

                    case CCTokenContext.TRUE_ID:
                    case CCTokenContext.FALSE_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("boolean"); // NOI18N
                        break;

                    case CCTokenContext.NULL_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("null"); // NOI18N
                        break;

                    case CCTokenContext.CLASS_ID:
                        if (topID == DOT_OPEN || topID == ARROW_OPEN || topID == SCOPE_OPEN) {
                            pushExp(createTokenExp(VARIABLE));
                        } else {
                            errorState = true;
                        }
                        break;

                    case CCTokenContext.NEW_ID:
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

//                    case CCTokenContext.CPPINCLUDE_ID:
//                        pushExp(createTokenExp(CPPINCLUDE));
//                        break;

                    case CCTokenContext.STATIC_ID:
                        switch (topID) {
//                        case CPPINCLUDE:
//                            top.addParameter(createTokenExp(CPPINCLUDE));
//                            break;
                        default:
                            errorState = true;
                            break;
                        }
                        break;

//                    case CCTokenContext.SUPER_ID:
//                        if (topID == GENERIC_WILD_CHAR)
//                            break;
                    case CCTokenContext.THIS_ID:
                        pushExp(createTokenExp(VARIABLE));
                        break;

//                    case CCTokenContext.ANNOTATION_ID:
//                        pushExp(createTokenExp(ANNOTATION));
//                        break;

//                    case CCTokenContext.INSTANCEOF_ID:
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

                    case CCTokenContext.CASE_ID:
                        pushExp(createTokenExp(CASE));
                        break;

//                    case CCTokenContext.EXTENDS_ID:
//                        if (topID == GENERIC_WILD_CHAR)
//                            break;

                    // TODO - the following block should be in default:
                    case CCTokenContext.VOID_ID:
//                    case CCTokenContext.ABSTRACT_ID:
//                    case CCTokenContext.ASSERT_ID:
                    case CCTokenContext.BREAK_ID:
                    case CCTokenContext.CATCH_ID:
                    case CCTokenContext.CONTINUE_ID:
                    case CCTokenContext.DEFAULT_ID:
                    case CCTokenContext.DO_ID:
                    case CCTokenContext.ELSE_ID:
//                    case CCTokenContext.FINAL_ID:
//XXX                    case CCTokenContext.FINALLY_ID:
                    case CCTokenContext.FOR_ID:
                    case CCTokenContext.GOTO_ID:
                    case CCTokenContext.IF_ID:
//                    case CCTokenContext.IMPLEMENTS_ID:
//                    case CCTokenContext.INTERFACE_ID:
//                    case CCTokenContext.NATIVE_ID:
//                    case CCTokenContext.PACKAGE_ID:
                    case CCTokenContext.PRIVATE_ID:
                    case CCTokenContext.PROTECTED_ID:
                    case CCTokenContext.PUBLIC_ID:
                    case CCTokenContext.RETURN_ID:
//                    case CCTokenContext.STRICTFP_ID:
                    case CCTokenContext.SWITCH_ID:
//                    case CCTokenContext.SYNCHRONIZED_ID:
                    case CCTokenContext.THROW_ID:
//XXX                    case CCTokenContext.THROWS_ID:
//                    case CCTokenContext.TRANSIENT_ID:
                    case CCTokenContext.TRY_ID:
                    case CCTokenContext.VOLATILE_ID:
                    case CCTokenContext.WHILE_ID:
                        errorState = true;
                        break;

                    case CCTokenContext.IDENTIFIER_ID: // identifier found e.g. 'a'
                        {
                            switch (topID) {
                            case OPERATOR:
                            case DOT_OPEN:
                            case ARROW_OPEN:
                            case SCOPE_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
                            case METHOD_OPEN:
                            case MEMBER_POINTER_OPEN:
                            case NEW:
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
                                pushExp(createTokenExp(VARIABLE));
                                break;

                            case GENERIC_WILD_CHAR:
                                top.setExpID(VARIABLE);
                                addTokenTo(top);
                                break;

                            case TYPE:
                            case VARIABLE:
                                if (getValidExpID(peekExp2()) == METHOD_OPEN) {
                                    //top.setExpID(VARIABLE);
                                    addTokenTo(top);
                                    //pushExp(createTokenExp(VARIABLE));
                                    // TODO: need to create parameter, we know, that METHOD_OPEN is declaration/definition of method
                                    break;
                                }  
                            case TYPE_REFERENCE:
                                if (getValidExpID(peekExp2()) == METHOD_OPEN) {
                                    //top.setExpID(VARIABLE);
                                    //addTokenTo(top);
                                    popExp(); // top
                                    CsmCompletionExpression var = createTokenExp(VARIABLE);
                                    var.addParameter(top);
                                    pushExp(var);
                                    // TODO: need to create parameter, we know, that METHOD_OPEN is declaration/definition of method
                                    break;
                                }                                
                            default:
                                errorState = true;
                                break;
                            }
                        }
                        break;

                    case CCTokenContext.QUESTION_ID:
                        if (topID == GENERIC_TYPE_OPEN) {
                            pushExp(new CsmCompletionExpression(GENERIC_WILD_CHAR));
                            break;
                        }

                    case CCTokenContext.MUL_ID:
                    case CCTokenContext.AND_ID:
                    {
                        boolean pointer = false;
                        // special handling of * and &, because it can be not operator
                        // while dereference and address-of expression
                        // try to handle it the same ways as UNARY_OPERATOR
                        switch (topID) {
                            case MEMBER_POINTER_OPEN: // next is operator as well
                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
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
                                // we have type or type reference and then * or &, 
                                // join into TYPE_REFERENCE
                                popExp();
                                CsmCompletionExpression exp = createTokenExp(TYPE_REFERENCE);
                                exp.addParameter(top);
                                pushExp(exp); 
                                pointer = true;
                                break;
                        }
                        if (pointer) {
                            break;
                        } else {
                            // else "nobreak" to allow to be handled as normal operators
                        }
                    }
                    case CCTokenContext.EQ_ID: // Assignment operators
                    case CCTokenContext.PLUS_EQ_ID:
                    case CCTokenContext.MINUS_EQ_ID:
                    case CCTokenContext.MUL_EQ_ID:
                    case CCTokenContext.DIV_EQ_ID:
                    case CCTokenContext.AND_EQ_ID:
                    case CCTokenContext.OR_EQ_ID:
                    case CCTokenContext.XOR_EQ_ID:
                    case CCTokenContext.MOD_EQ_ID:
                    case CCTokenContext.LSHIFT_EQ_ID:
                    case CCTokenContext.RSSHIFT_EQ_ID:
//                    case CCTokenContext.RUSHIFT_EQ_ID:

                    case CCTokenContext.LT_EQ_ID:
                    case CCTokenContext.GT_EQ_ID:
                    case CCTokenContext.EQ_EQ_ID:
                    case CCTokenContext.NOT_EQ_ID:

                    case CCTokenContext.AND_AND_ID: // Binary, result is boolean
                    case CCTokenContext.OR_OR_ID:

                    case CCTokenContext.LSHIFT_ID: // Always binary
                    case CCTokenContext.DIV_ID:
                    case CCTokenContext.OR_ID:
                    case CCTokenContext.XOR_ID:
                    case CCTokenContext.MOD_ID:

                    case CCTokenContext.COLON_ID:

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
                                if (tokenNumID == CCTokenContext.MUL_ID || tokenNumID == CCTokenContext.AND_ID) {// '*' or '&' as type reference
                                    pushExp(createTokenExp(OPERATOR));
                                    break;
                                }
                                // else flow to errorContextor
                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case CCTokenContext.LT_ID:
                        {
                            boolean genericType = false;
                            if (java15) { // special treatment of Java 1.5 features
                                switch (topID) {
                                    case VARIABLE:
                                    case DOT:
                                    case ARROW:
                                    case SCOPE:
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
                                        pushExp(createTokenExp(OPERATOR));
                                        break;

                                    default:
                                        errorState = true;
                                        break;
                                }
                            }                        
                            break;
                        }

                    case CCTokenContext.GT_ID: // ">"
                        {
                            boolean genericType = false;
                            if (java15) { // special treatment of Java 1.5 features
                                switch (topID) {
                                    case CONSTANT: // check for "List<const" plus ">" case
                                    case VARIABLE: // check for "List<var" plus ">" case
                                    case TYPE: // check for "List<int" plus ">" case
                                    case DOT: // check for "List<var1.var2" plus ">" case
                                    case ARROW: // check for "List<var1.var2" plus ">" case
                                    case SCOPE: // check for "List<NS::Class" plus ">" case
                                    case GENERIC_TYPE: // check for "List<HashMap<String, Integer>" plus ">" case
                                    case GENERIC_TYPE_OPEN: // chack for "List<" plus ">" case
                                    case GENERIC_WILD_CHAR: // chack for "List<?" plus ">" case
                                    case ARRAY: // chack for "List<String[]" plus ">" case
                                    case PARENTHESIS: // chack for "T<(1+1)" plus ">" case
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
                                            gen.setExpID(GENERIC_TYPE);
                                            top = gen;
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
                                        pushExp(createTokenExp(OPERATOR));
                                        break;

                                    default:
                                        errorState = true;
                                        break;
                                }
                            }                        
                            break;
                        }

                    case CCTokenContext.RSSHIFT_ID: // ">>"
                        {
                            boolean genericType = false;
                            if (java15) { // special treatment of Java 1.5 features
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
                                        pushExp(createTokenExp(OPERATOR));
                                        break;

                                    default:
                                        errorState = true;
                                        break;
                                }
                            }                        
                            break;
                        }

//                    case CCTokenContext.RUSHIFT_ID: // ">>>"
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



                    case CCTokenContext.PLUS_PLUS_ID: // Prefix or postfix
                    case CCTokenContext.MINUS_MINUS_ID:
                        switch (topID) {
                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
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

                    case CCTokenContext.PLUS_ID: // Can be unary or binary
                    case CCTokenContext.MINUS_ID:
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
                                CsmCompletionExpression opExp = createTokenExp(OPERATOR);
                                pushExp(opExp);
                                break;

                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
                            case MEMBER_POINTER_OPEN:    
                            case OPERATOR:
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


                    case CCTokenContext.NEG_ID: // Always unary
                    case CCTokenContext.NOT_ID:
                        switch (topID) {
                            case METHOD_OPEN:
                            case ARRAY_OPEN:
                            case PARENTHESIS_OPEN:
                            case OPERATOR:
                            case UNARY_OPERATOR:
                            case MEMBER_POINTER:
                            case MEMBER_POINTER_OPEN:
                            case NO_EXP:
                            {
                                // Unary operator
                                CsmCompletionExpression opExp = createTokenExp(UNARY_OPERATOR);
                                pushExp(opExp); // add operator as new exp
                                break;
                            }
                            case DOT_OPEN:
                            case ARROW_OPEN:
                            case SCOPE_OPEN:
                            {
                                if (tokenNumID == CCTokenContext.NEG_ID) {
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

                    case CCTokenContext.DOT_ID: // '.' found
                    case CCTokenContext.DOTMBR_ID: // '.*' found    
                    case CCTokenContext.ARROW_ID: // '->' found
                    case CCTokenContext.ARROWMBR_ID: // '->*' found      
                    case CCTokenContext.SCOPE_ID: // '::' found                        
                        switch (topID) {
                            case CONSTANT:
                            case VARIABLE:
                            case ARRAY:
                            case METHOD:
                            case CONSTRUCTOR:
                            case PARENTHESIS:
                            case GENERIC_TYPE:
                            {
                                popExp();
                                 // tokenID.getNumericID() is the parameter of the main switch
                                // create correspondent *_OPEN expression ID
                                int openExpID = tokenID2OpenExpID(tokenNumID);
                                CsmCompletionExpression opExp = createTokenExp(openExpID);
                                opExp.addParameter(top);
                                pushExp(opExp);
                                break;
                            }
                            case DOT:
                                addTokenTo(top);
                                top.setExpID(DOT_OPEN);
                                break;

                            case ARROW:
                                addTokenTo(top);
                                top.setExpID(ARROW_OPEN);
                                break;

                            case SCOPE:
                                addTokenTo(top);
                                top.setExpID(SCOPE_OPEN);
                                break;

                            case NO_EXP: // alone :: is OK as access to global context
                                if (tokenNumID == CCTokenContext.SCOPE_ID) {
                                    CsmCompletionExpression emptyVar = CsmCompletionExpression.createEmptyVariable(curTokenPosition);
                                    int openExpID = tokenID2OpenExpID(CCTokenContext.SCOPE_ID);
                                    CsmCompletionExpression opExp = createTokenExp(openExpID);
                                    opExp.addParameter(emptyVar);
                                    pushExp(opExp);      
                                    break;
                                }
                            default:
                                errorState = true;
                                break;
                        }
                        break;
                        
                    case CCTokenContext.COMMA_ID: // ',' found
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

                    case CCTokenContext.SEMICOLON_ID:
                        errorState = true;
                        break;

                    case CCTokenContext.LPAREN_ID:
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

                            case ARRAY_OPEN:       // a[(
                            case PARENTHESIS_OPEN: // ((
                            case METHOD_OPEN:      // a((
                            case NO_EXP:
                            case OPERATOR:         // 3+(
                            case CONVERSION:       // (int)(
                            case PARENTHESIS:      // if (a > b) (
                            case GENERIC_TYPE_OPEN:// a < (
                                pushExp(createTokenExp(PARENTHESIS_OPEN));
                                break;

                            default:
                                errorState = true;
                                break;
                        }
                        break;

                    case CCTokenContext.RPAREN_ID:
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
                            case MEMBER_POINTER:
                            case TYPE_REFERENCE:
                            case INSTANCEOF:
                            case METHOD:
                            case GENERIC_TYPE:
                                CsmCompletionExpression top2 = peekExp2();
                                switch (getValidExpID(top2)) {
                                    case PARENTHESIS_OPEN:
                                        popExp();
                                        top2.addParameter(top);
                                        top2.setExpID(CsmCompletionExpression.isValidType(top) ? CONVERSION : PARENTHESIS);
                                        addTokenTo(top2);
                                        break;

                                    case GENERIC_TYPE_OPEN:
                                        popExp();
                                        top2.setExpID(OPERATOR);
                                        top2.addParameter(top);
                                        top = top2;
                                        top2 = peekExp2();
                                        if (getValidExpID(top2) != METHOD_OPEN)
                                            break;

                                    case METHOD_OPEN:
                                        popExp();
                                        top2.addParameter(top);
                                        top = top2;
                                        mtd = true;
                                        break;

                                    case CONVERSION:
                                        popExp();
                                        top2.addParameter( top );
                                        top=top2;
                                        top2 = peekExp2();
                                        switch (getValidExpID(top2)) {
                                            case PARENTHESIS_OPEN:
                                                popExp();
                                                top2.addParameter( top );
                                                top2.setExpID( PARENTHESIS );
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

                                //              case PARENTHESIS_OPEN: // empty parenthesis
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
                                case SCOPE_OPEN:
                                {
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

                    case CCTokenContext.LBRACKET_ID:
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

//                    case CCTokenContext.ELLIPSIS_ID:
//                        switch (topID) {
//                            case VARIABLE:
//                            case METHOD:
//                            case DOT:
//                            case ARRAY:
//                            case TYPE: // ... int[ ...
//                            case GENERIC_TYPE: // List<String> "["
//                                popExp(); // top popped
//                                CsmCompletionExpression arrExp = createTokenExp(ARRAY);
//                                // Add "..." again to have the even token count like with "[" "]"
//                                addTokenTo(arrExp);
//                                arrExp.addParameter(top);
//                                pushExp(arrExp);
//                                break;
//
//                            default:
//                                errorContext = true;
//                                break;
//                        }
//                        break;

                    case CCTokenContext.RBRACKET_ID:
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

                    case CCTokenContext.LBRACE_ID:
                        if (topID == ARRAY) {
                            CsmCompletionExpression top2 = peekExp2();
                            if (getValidExpID(top2) == NEW) {
                                popExp(); // top popped
                                top2.setExpID(ARRAY);
                                top2.addParameter(top.getParameter(0));
                                top2.addToken(top.getTokenID(0), top.getTokenOffset(0), top.getTokenText(0));
                                top2.addToken(top.getTokenID(1), top.getTokenOffset(1), top.getTokenText(1));
                                stopped = true;
                                break;
                            }
                        }
                        errorState = true;
                        break;

                    case CCTokenContext.RBRACE_ID:
                        errorState = true;
                        break;



                    case CCTokenContext.LINE_COMMENT_ID:
                        // Skip line comment
                        break;

                    case CCTokenContext.BLOCK_COMMENT_ID:
                        // Skip block comment
                        break;

                    case CCTokenContext.CHAR_LITERAL_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("char"); // NOI18N
                        break;

                    case CCTokenContext.STRING_LITERAL_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType(CsmCompletion.CONST_STRING_TYPE.format(true)); // NOI18N
                        break;

                    case CCTokenContext.INT_LITERAL_ID:
                    case CCTokenContext.HEX_LITERAL_ID:
                    case CCTokenContext.OCTAL_LITERAL_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("int"); // NOI18N
                        break;

                    case CCTokenContext.LONG_LITERAL_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("long"); // NOI18N
                        break;

                    case CCTokenContext.FLOAT_LITERAL_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("float"); // NOI18N
                        break;

                    case CCTokenContext.DOUBLE_LITERAL_ID:
                        constExp = createTokenExp(CONSTANT);
                        constExp.setType("double"); // NOI18N
                        break;
                    case CCTokenContext.CONST_ID:
                        // only type has const
                        kwdType = "const"; // NOI18N
                        break;
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
            case PARENTHESIS: // can be conversion
            case METHOD_OPEN:
            case ANNOTATION_OPEN:
            case OPERATOR:
            case UNARY_OPERATOR:
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

            default:
                errorState = true;
                break;
            }
        }
     
        if (kwdType != null) { // keyword constant (in conversions)
            switch (topID) {
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
            {
                // TODO: we know, this is method declaration/definition
                CsmCompletionExpression kwdExp = createTokenExp(TYPE);
                //addTokenTo(kwdExp);
                kwdExp.setType(kwdType);
                pushExp(kwdExp);
                errorState = false;
                break;
            }
            case TYPE:
            {
                CsmCompletionExpression kwdExp = top;
                addTokenTo(kwdExp);
                kwdExp.setType(kwdExp.getType() + " " + kwdType); // NOI18N
                errorState = false;
                break;
            }     
            case TYPE_REFERENCE:
            {
                CsmCompletionExpression kwdExp = createTokenExp(TYPE);
                kwdExp.setType(kwdType);
                top.addParameter(kwdExp);
                errorState = false;
                break;
            }     
            case GENERIC_TYPE_OPEN:
            {
                CsmCompletionExpression kwdExp = createTokenExp(TYPE);
                kwdExp.setType(kwdType);
                pushExp(kwdExp);
                errorState = false;
                break;
            }     
            default: // otherwise not recognized
                errorState = true;
                break;
            }
        }

        if (errorState) {
            clearStack();

            if (tokenID == CCTokenContext.IDENTIFIER) {
                pushExp(createTokenExp(VARIABLE));
                errorState = false;
            }
        }
        
        return !stopped;
    }

    public int eot(int offset) {
        if (lastValidTokenID != null) {
            // if space or comment occurs as last token
            // add empty variable to save last position
            switch (lastValidTokenID.getNumericID()) {
                case CCTokenContext.WHITESPACE_ID:
                case CCTokenContext.LINE_COMMENT_ID:
                case CCTokenContext.BLOCK_COMMENT_ID:
                    pushExp(CsmCompletionExpression.createEmptyVariable(
                        bufferStartPos + bufferOffsetDelta + offset));
                    break;
                default:
                    if (getValidExpID(peekExp()) == GENERIC_TYPE) {
                        pushExp(CsmCompletionExpression.createEmptyVariable(
                            bufferStartPos + bufferOffsetDelta + offset));
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
                    }
                    break;
                    
                case PARENTHESIS_OPEN:
                    pushExp(CsmCompletionExpression.createEmptyVariable(
                            bufferStartPos + bufferOffsetDelta + offset));
                     break;

                case GENERIC_TYPE_OPEN:
                    if (top.getParameterCount() > 1)
                        break;

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
                            top.getTokenID(0).getNumericID() == CCTokenContext.NEG_ID) {
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
        return 0;
    }

    public void nextBuffer(char[] buffer, int offset, int len,
                           int startPos, int preScan, boolean lastBuffer) {
        // System.err.printf("offset = %d, len = %d, startPos = %d, preScan = %d, lastBuffer = %s\n", offset, len, startPos, preScan, lastBuffer ? "true" : "false");
        this.buffer = new char[len + preScan];
        System.arraycopy(buffer, offset - preScan, this.buffer, 0, len + preScan);
        bufferOffsetDelta = preScan - offset;
        this.bufferStartPos = startPos - preScan;
    }

    public String toString() {
        int cnt = expStack.size();
        StringBuilder sb = new StringBuilder();
        if (stopped) {
            sb.append("Parsing STOPPED by request.\n"); // NOI18N
        }
        sb.append("Stack size is " + cnt + "\n"); // NOI18N
        if (cnt > 0) {
            sb.append("Stack expressions:\n"); // NOI18N
            for (int i = 0; i < cnt; i++) {
                CsmCompletionExpression e = (CsmCompletionExpression)expStack.get(i);
                sb.append("Stack["); // NOI18N
                sb.append(i);
                sb.append("]: "); // NOI18N
                sb.append(e.toString(0));
                sb.append('\n'); //NOI18N
            }
        }
        return sb.toString();
    }
}
