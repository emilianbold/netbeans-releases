/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.util.*;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.openide.ErrorManager;


/**
* Support methods for syntax analyzes working with java JMI interfaces.
*
* @author Dusan Balek, Martin Roskanin, Vladimir Voskresensky
* @version 1.00
*/
class CsmDeclarationProcessor
implements ExtSyntaxSupport.DeclarationTokenProcessor,
ExtSyntaxSupport.VariableMapTokenProcessor {
    
    // Internal java declaration token processor states (in the state variable
    private static final int INIT = 0; // nothing recognized yet
    private static final int TYPE_VAR = 1; // initial datatype or variable already recognized
    private static final int TYPE_EXP = 2; // active type expression recognition in progress
    private static final int VAR_EXP = 3; // active variable declaration in progress
    private static final int EQ_EXP = 4; // "type var = " was recognized and now in after "=" exp
    
    
    // CsmCompletionExpression constants
    private static final int VARIABLE = CsmCompletionExpression.VARIABLE;
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
    private static final int TYPE = CsmCompletionExpression.TYPE;
    private static final int GENERIC_TYPE = CsmCompletionExpression.GENERIC_TYPE;
    private static final int GENERIC_TYPE_OPEN = CsmCompletionExpression.GENERIC_TYPE_OPEN;
    private static final int GENERIC_WILD_CHAR = CsmCompletionExpression.GENERIC_WILD_CHAR;

    // Invlaid value marking no expression on the given stack position for peekExp()
    private static final int NO_EXP = -1;

    /** Internal state of recognition engine. */
    private int state;
    
    /** Stack of the expressions related to declarations recognition. */
    private List expStack = new ArrayList();
    
    /** Type expression of the declaration being recognized. */
    private CsmCompletionExpression typeExp;
    
    /** Support backing this declaration recognizer. */
    private CsmSyntaxSupport sup;
    
    /** Position of the begining of the declaration to be returned */
    private int decStartPos = -1;
    
    /**
     * Absolute offset of the initial variable or type name of declaration.
     * The appropriate CsmCompletionExpression gets created later
     * once another token gets recognized and there
     * is still a probability that a real declaration was found.
     */
    private int typeVarTokenOffset;

    /**
     * Length of the initial variable or type name. The appropriate CsmCompletionExpression
     * gets created later once another token gets recognized and there
     * is still a probability that a real declaration was found.
     */
    private int typeVarTokenLength;
    
    /**
     * State whether the initial variable is a primitive datatype.
     */
    private TokenID typeVarTokenID;
    
    /** Currently inside parenthesis, i.e. comma delimits declarations */
    private int parenDepth;

    private int eqExpBraceDepth;

    private TokenID curTokenID;
    private int curTokenPosition;
    private int curTokenLength;
    private char[] buffer;
    
    private int bufferStartPos;
    
    /** Variable name to be recognized when operating in a single variable mode. */
    private String varName;
    
    /** 
     * Map filled with the [varName, TypeResolver] pairs.
     */
    private StackedMap varMap;

    private boolean forScope;
    private int forScopeParenDepth;

    private Stack stackedConfigs = new Stack();


    /** Construct new token processor
     * @param varName it contains valid varName name or null to search
     *   for all variables and construct the variable map.
     */
    CsmDeclarationProcessor(CsmSyntaxSupport sup, String varName) {
        this.sup = sup;
        this.varName = varName;
        if (varName == null) {
            varMap = new StackedMap();
        }
    }
    
    /**
     * Return integer offset corresponding to the begining
     * of the declaration of the requested variable name.
     */
    public int getDeclarationPosition() {
        return decStartPos;
    }

    /**
     * Return map containing <code>[varName, CsmSyntaxSupport.JavaVariable]</code> pairs
     * where typeResolver allows lazy resolving of the variable's type
     * by using {@link #resolveType(Object)}.
     */
    public Map getVariableMap() {
        return varMap;
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
        return (cnt >= ind) ? (CsmCompletionExpression)expStack.get(cnt - ind) : null;
    }

    private CsmCompletionExpression createTokenExp(int id) {
        CsmCompletionExpression exp = new CsmCompletionExpression(id);
        addTokenTo(exp);
        return exp;
    }

    /** Add the token to a given expression */
    private void addTokenTo(CsmCompletionExpression exp) {
        exp.addToken(curTokenID, curTokenPosition, getTokenText(curTokenPosition, curTokenLength));
    }
    
    private int getValidExpID(CsmCompletionExpression exp) {
        return (exp != null) ? exp.getExpID() : NO_EXP;
    }
    
    private void pushTypeVar() {
        if (state != TYPE_VAR) {
            throw new IllegalArgumentException();
        }

        state = TYPE_EXP; // var or type will be pushed to stack
        int expType;
        TokenID id;
        if (typeVarTokenID != null) {
            expType = CsmCompletionExpression.TYPE;
            id = typeVarTokenID;

        } else { // Variable
            expType = CsmCompletionExpression.VARIABLE;
            id = CCTokenContext.IDENTIFIER;
        }

        CsmCompletionExpression exp = new CsmCompletionExpression(expType);
        exp.addToken(id, typeVarTokenOffset,
            getTokenText(typeVarTokenOffset, typeVarTokenLength));
        pushExp(exp); // push initial exp on the stack
    }
    
    private void reset() {
        state = INIT;
        clearStack();
        typeExp = null;
    }
    
    private void pushConfig() {
        stackedConfigs.push(new Integer(parenDepth));
        stackedConfigs.push(new Integer(eqExpBraceDepth));
        stackedConfigs.push(new Integer(state));
        stackedConfigs.push(typeExp);
        stackedConfigs.push(expStack);
        parenDepth = 0;
        eqExpBraceDepth = 0;
        state = INIT;
        typeExp = null;
        expStack = new ArrayList();
    }
    
    private void popConfig() {
        expStack = (List)stackedConfigs.pop();
        typeExp = (CsmCompletionExpression)stackedConfigs.pop();
        state = ((Integer)stackedConfigs.pop()).intValue();
        eqExpBraceDepth = ((Integer)stackedConfigs.pop()).intValue();
        parenDepth = ((Integer)stackedConfigs.pop()).intValue();
    }
    
    /**
     * Form the type expression.
     *
     * @return true if the expression was formed successfully
     *  or false if not (reset() will be called in that case).
     */
    private boolean formTypeExp() {
        if (expStack.size() == 1) { // must be exactly one exp on stack
            typeExp = popExp();
            switch (typeExp.getExpID()) {
                case ARRAY_OPEN:
                case DOT_OPEN:
                case ARROW_OPEN:
                case SCOPE_OPEN:
                    reset();
                    return false;
                    
                default:
                    state = VAR_EXP; // start forming variable expression
                    return true;
            }
            
        } else { // more than one exp on stack -> incomplete exp
            reset();
            return false;
        }
    }
    
    /**
     * Create variable expression.
     *
     * @param lastVar true if this formed variable is the last one
     *  (is followed e.g. by semicolon) or whether there can be more variables
     *  (such as after comma) which will assign TYPE_EXP state.
     *
     * @return true if variable expression formed successfully
     *  from the information on the stack
     *  or false if not (reset() will be called in that case).
     */
    private boolean formVarExp(boolean lastVar) {
        CsmCompletionExpression varExp;
        if (expStack.size() == 1) { // must be exactly one exp on stack
            // leave state == VAR_EXP
            varExp = popExp(); // stack will be empty
            if (varExp != null) { // formed successfully
                switch (varExp.getExpID()) {
                    case VARIABLE:
                    case ARRAY:
                        break; // leave varExp assigned to non-null
                        
                    default:
                        varExp = null;
                        break;
                }
            }
            
        } else { // more than one exp on stack -> incomplete exp
            varExp = null;
        }
            
        if (varExp != null) {
            processDeclaration(typeExp, varExp);
            if (lastVar) {
                reset();
            } else { // not last var
                // Continue to be in VAR_EXP state
                // Stack already empty from previous popExp()
            }
            return true;

        } else {
            reset();
            return false;
        }
    }
    
    private String getTokenText(int offset, int length) {
        try {
            return sup.getDocument().getText(offset, length);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return "";
        }
    }

    private void processDeclaration(CsmCompletionExpression typeExp, CsmCompletionExpression varExp) {
        if (varName == null) { // collect all variables
            String varName = getVarName(varExp);
            JavaVar javaVar = new JavaVar(typeExp, varExp);
            varMap.put(varName, javaVar);
            
        } else if (varName.equals(getVarName(varExp))) { // found var of the requested name
            decStartPos = typeVarTokenOffset;
        }
    }
    
    private String getVarName(CsmCompletionExpression varExp) {
        switch (varExp.getExpID()) {
            case VARIABLE:
                return varExp.getTokenText(0);
                
            case ARRAY:
                CsmCompletionExpression var = varExp.getParameter(0); // variable of the array
                return var.getTokenText(0);
                
            default:
                throw new IllegalStateException();
        }
    }
    
    public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
    int tokenOffset, int tokenLen) {
        int pos = bufferStartPos + tokenOffset;
        
        curTokenID = tokenID;
        curTokenPosition = pos;
        curTokenLength = tokenLen;
        
        // Check whether we are really recognizing the java tokens
        if (!tokenContextPath.contains(CCTokenContext.contextPath)) {
            state = INIT;
            return true;
        }
        
        switch (tokenID.getNumericID()) {
            case CCTokenContext.BOOLEAN_ID:
            case CCTokenContext.CHAR_ID:
            case CCTokenContext.DOUBLE_ID:
            case CCTokenContext.FLOAT_ID:
            case CCTokenContext.INT_ID:
            case CCTokenContext.LONG_ID:
            case CCTokenContext.SHORT_ID:
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case INIT:
                            typeVarTokenOffset = pos;
                            typeVarTokenLength = tokenLen;
                            typeVarTokenID = tokenID;
                            state = TYPE_VAR;
                            break;

                        case TYPE_VAR: // var-or-type type => invalid
                        case TYPE_EXP: // something followed by datatype => invalid
                        case VAR_EXP: // varName followed by datatype => invalid
                            reset();
                            break;

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            throw new IllegalStateException();
                    }
                } // else -> in parens -> do nothing
                break;

            case CCTokenContext.FOR_ID:
                if (varMap != null) {
                    varMap.pushNewScope();
                    forScope = true;
                }
                break;

            case CCTokenContext.IDENTIFIER_ID:
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case INIT:
                            typeVarTokenOffset = pos;
                            typeVarTokenLength = tokenLen;
                            typeVarTokenID = null;
                            state = TYPE_VAR;
                            break;

                        case TYPE_VAR: // var-or-type ident => maybe declaration
                            pushTypeVar();
                            // continue to TYPE_EXP handling
                        case TYPE_EXP: // something followed by ident => varName
                            CsmCompletionExpression top = peekExp();
                            switch (getValidExpID(top)) {
                                case VARIABLE: // var "ident" -> decl
                                case TYPE: // "int" "ident" -> decl
                                case DOT: // var1.var2. ... var-n "ident"
                                case ARROW: // var1.var2. ... var-n "ident"
                                case SCOPE: // Ns::Class:: ... ::member "ident"
                                case ARRAY: // "array[]" "ident"
                                case GENERIC_TYPE: // List<String> "ident"
                                    if (formTypeExp()) { // formed successfully
                                        // start to form variable expression
                                        pushExp(createTokenExp(VARIABLE));
                                    }
                                    break;

                                case DOT_OPEN: // closing dot expr.
                                    top.setExpID(DOT);
                                    top.addParameter(createTokenExp(VARIABLE));
                                    break;

                                case ARROW_OPEN: // closing arrow expr.
                                    top.setExpID(ARROW);
                                    top.addParameter(createTokenExp(VARIABLE));
                                    break;

                                case SCOPE_OPEN: // closing arrow expr.
                                    top.setExpID(SCOPE);
                                    top.addParameter(createTokenExp(VARIABLE));
                                    break;
                                    
                                case GENERIC_TYPE_OPEN: // 
                                    pushExp(createTokenExp(VARIABLE));
                                    break;

                                case GENERIC_WILD_CHAR:
                                    top.setExpID(VARIABLE);
                                    addTokenTo(top);
                                    break;

                                default:
                                    reset();
                                    break;
                            }
                            break;

                        case VAR_EXP: // unclosed var decl. followed by ident -> invalid
                            if (expStack.size() == 0) { // nothing on stack
                                // This can happen after comma that was separating
                                // two variables of the same type
                                pushExp(createTokenExp(VARIABLE));

                            } else { // invalid "var" "var" (or e.g. "var[]" "var")
                                reset();
                            }
                            break;

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            throw new IllegalStateException();
                    }
                } // else -> in parens -> do nothing
                break;

            case CCTokenContext.DOT_ID:
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case INIT: // dot in initial state -> do nothing
                            break;

                        case TYPE_VAR: // var-or-type "." -> open dot
                            pushTypeVar();
                            // continue to TYPE_EXP handling
                        case TYPE_EXP:
                            CsmCompletionExpression top = peekExp();
                            switch (getValidExpID(top)) {
                                case VARIABLE: // var "." -> DOT_OPEN
                                case GENERIC_TYPE: // "C1<A1>.C2<A2>" -> DOT_OPEN
                                    popExp(); // pop var
                                    CsmCompletionExpression opExp = createTokenExp(DOT_OPEN);
                                    opExp.addParameter(top);
                                    pushExp(opExp);
                                    break;

                                case DOT: // var1.var2. ... var-n "."
                                    top.setExpID(DOT_OPEN);
                                    break;

                                default:
                                    reset();
                                    break;
                            }
                            break;
                            
                        case VAR_EXP: // "type var." -> reset
                            reset();
                            break;

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            throw new IllegalStateException();
                    }
                } // else -> in parens -> do nothing
                break;
                
            case CCTokenContext.ARROW_ID:
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case INIT: // arrow in initial state -> do nothing
                            break;

                        case TYPE_VAR: // var-or-type "->" -> open arrow
                            pushTypeVar();
                            // continue to TYPE_EXP handling
                        case TYPE_EXP:
                            CsmCompletionExpression top = peekExp();
                            switch (getValidExpID(top)) {
                                case VARIABLE: // var "->" -> ARROW_OPEN
                                case GENERIC_TYPE: // "C1<A1>.C2<A2>" -> ARROW_OPEN
                                    popExp(); // pop var
                                    CsmCompletionExpression opExp = createTokenExp(ARROW_OPEN);
                                    opExp.addParameter(top);
                                    pushExp(opExp);
                                    break;

                                case ARROW: // var1->var2-> ... var-n "->"
                                    top.setExpID(ARROW_OPEN);
                                    break;

                                default:
                                    reset();
                                    break;
                            }
                            break;
                            
                        case VAR_EXP: // "type var->" -> reset
                            reset();
                            break;

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            throw new IllegalStateException();
                    }
                } // else -> in parens -> do nothing
                break;

            case CCTokenContext.SCOPE_ID:
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case INIT: // arrow in initial state -> do nothing
                            break;

                        case TYPE_VAR: // var-or-type "->" -> open arrow
                            pushTypeVar();
                            // continue to TYPE_EXP handling
                        case TYPE_EXP:
                            CsmCompletionExpression top = peekExp();
                            switch (getValidExpID(top)) {
                                case VARIABLE: // var "::" => SCOPE_OPEN
                                case GENERIC_TYPE: // "C1<A1>.C2<A2>" => SCOPE_OPEN
                                    popExp(); // pop var
                                    CsmCompletionExpression opExp = createTokenExp(SCOPE_OPEN);
                                    opExp.addParameter(top);
                                    pushExp(opExp);
                                    break;

                                case SCOPE: // Ns::Class:: ... var "::"
                                    top.setExpID(SCOPE_OPEN);
                                    break;

                                default:
                                    reset();
                                    break;
                            }
                            break;
                            
                        case VAR_EXP: // "type var->" -> reset
                            reset();
                            break;

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            throw new IllegalStateException();
                    }
                } // else -> in parens -> do nothing
                break;
                
            case CCTokenContext.LBRACKET_ID: // "["
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case INIT: // '[' in initial state -> do nothing
                            break;

                        case TYPE_VAR: // var-or-type "[" -> open array
                            pushTypeVar();
                            // continue to TYPE_EXP handling
                        case TYPE_EXP:
                            {
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case DOT: // var1.var2. ... var-n "["
                                    case ARROW: // var1.var2-> ... var-n "["
                                    case SCOPE: // NS::Class::var "["
                                    case VARIABLE: // var "[" -> ARRAY_OPEN
                                    case TYPE:
                                    case GENERIC_TYPE: // e.g. "List<String>" "[" -> ARRAY_OPEN
                                        popExp(); // pop var
                                        CsmCompletionExpression opExp = createTokenExp(ARRAY_OPEN);
                                        opExp.addParameter(top);
                                        pushExp(opExp);
                                        break;

                                    case ARRAY: // array[] "[" -> sub-ARRAY_OPEN again
                                        top.setExpID(ARRAY_OPEN);
                                        addTokenTo(top);
                                        break;

                                    default:
                                        reset();
                                        break;
                                }
                                break;
                            }

                        case VAR_EXP: // e.g. "int a" "[" -> ARRAY_OPEN
                            {
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case VARIABLE: // var "[" -> ARRAY_OPEN
                                        popExp(); // pop var
                                        CsmCompletionExpression opExp = createTokenExp(ARRAY_OPEN);
                                        opExp.addParameter(top);
                                        pushExp(opExp);
                                        break;

                                    case ARRAY: // array[] "[" -> sub-ARRAY_OPEN again
                                        top.setExpID(ARRAY_OPEN);
                                        addTokenTo(top);

                                    default:
                                        reset();
                                        break;
                                }
                                break;
                            }

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            throw new IllegalStateException();
                    }
                } // else -> in parens -> do nothing
                break;

		//XXX
//            case CCTokenContext.ELLIPSIS_ID: // "..."
//                if (parenDepth <= 1) { // no parens or in method declaration parms
//                    switch (state) {
//                        case INIT: // '[' in initial state -> do nothing
//                            break;
//
//                        case TYPE_VAR: // var-or-type "..." -> array
//                            pushTypeVar();
//                            // continue to TYPE_EXP handling
//                        case TYPE_EXP:
//                            {
//                                CsmCompletionExpression top = peekExp();
//                                switch (getValidExpID(top)) {
//                                    case DOT: // var1.var2. ... var-n "..." -> ARRAY
//                                    case VARIABLE: // var "..." -> ARRAY
//                                    case TYPE: // int "..." -> ARRAY
//                                    case GENERIC_TYPE: // e.g. "List<String>" "..." -> ARRAY
//                                        popExp(); // pop var
//                                        CsmCompletionExpression opExp = createTokenExp(ARRAY);
//                                        // Add "..." again to have the even token count like with "[" "]"
//                                        addTokenTo(opExp);
//                                        opExp.addParameter(top);
//                                        pushExp(opExp);
//                                        break;
//
//                                    case ARRAY: // array[] "[" -> sub-ARRAY_OPEN again
//                                        addTokenTo(top);
//                                        // Add "..." again to have the even token count like with "[" "]"
//                                        addTokenTo(top);
//                                        break;
//
//                                    default:
//                                        reset();
//                                        break;
//                                }
//                                break;
//                            }
//
//                        case VAR_EXP: // e.g. "int a" "..." -> ARRAY_OPEN
//                            reset();
//                            break;
//
//                        case EQ_EXP: // in expression after "type var = "
//                            // do nothing - leave the EQ_EXP state
//                            break;
//
//                        default:
//                            throw new IllegalStateException();
//                    }
//                } // else -> in parens -> do nothing
//                break;


            case CCTokenContext.RBRACKET_ID: // "]"
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case INIT: // ']' in initial state -> do nothing
                            break;

                        case TYPE_VAR: // var-or-type "]" -> error
                            reset();
                            break;

                        case TYPE_EXP:
                            {
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case ARRAY_OPEN: // "type[" "]" -> ARRAY
                                        addTokenTo(top);
                                        top.setExpID(ARRAY);
                                        break;

                                    default:
                                        reset();
                                        break;
                                }
                                break;
                            }

                        case VAR_EXP: // e.g. "int a[" "]" -> ARRAY
                            {
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case ARRAY_OPEN: // type var[ "]" -> ARRAY
                                        addTokenTo(top);
                                        top.setExpID(ARRAY);
                                        break;

                                    default:
                                        reset();
                                        break;
                                }
                                break;
                            }

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            throw new IllegalStateException();
                    }
                } // else -> in parens -> do nothing
                break;


            case CCTokenContext.LT_ID: // "<"
                {
                    if (parenDepth <= 1) { // no parens or in method declaration parms
                        switch (state) {
                            case TYPE_VAR:
                                pushTypeVar();
                                // continue to TYPE_EXP handling
                            case TYPE_EXP:
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case VARIABLE:
                                    case DOT:
                                        popExp(); // pop the top expression
                                        CsmCompletionExpression genExp = createTokenExp(GENERIC_TYPE_OPEN);
                                        genExp.addParameter(top);
                                        pushExp(genExp);
                                        break;
                                        
                                    default:
                                        // could possibly still be acceptable as operator '<'
                                        break;
                                }
                                break;

                            case EQ_EXP: // in expression after "type var = "
                                // do nothing - leave the EQ_EXP state
                                break;
                                
                            default:
                                reset();
                                break;
                        }
                    } // else - inside parens inside the exp after "="
                    break;
                }


            case CCTokenContext.GT_ID: // ">"
                {
                    if (parenDepth <= 1) { // no parens or in method declaration parms
                        switch (state) {
                            case TYPE_VAR:
                                pushTypeVar();
                                // continue to TYPE_EXP handling
                            case TYPE_EXP:
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case VARIABLE:
                                    case ARRAY:
                                    case DOT:
                                    case GENERIC_TYPE:
                                    case GENERIC_WILD_CHAR:
                                        CsmCompletionExpression top2 = peekExp2();
                                        switch (getValidExpID(top2)) {
                                            case GENERIC_TYPE_OPEN:
                                                popExp(); // pop the top expression
                                                top2.addParameter(top);
                                                top2.setExpID(GENERIC_TYPE);
                                                addTokenTo(top2);
                                                top = top2;
                                                break;
                                                
                                            default:
                                                reset();
                                                break;
                                        }
                                        break;

                                    default:
                                        reset();
                                        break;
                                }
                                break;

                            case EQ_EXP: // in expression after "type var = "
                                // do nothing - leave the EQ_EXP state
                                break;
                                
                            default:
                                reset();
                                break;
                        }
                    } // else - inside parens inside the exp after "="
                    break;
                }


            case CCTokenContext.RSSHIFT_ID: // ">>"
                {
                    if (parenDepth <= 1) { // no parens or in method declaration parms
                        switch (state) {
                            case TYPE_VAR:
                                pushTypeVar();
                                // continue to TYPE_EXP handling
                            case TYPE_EXP:
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case VARIABLE:
                                    case DOT:
                                    case GENERIC_TYPE:
                                    case GENERIC_WILD_CHAR:
                                        CsmCompletionExpression top2 = peekExp2();
                                        switch (getValidExpID(top2)) {
                                            case GENERIC_TYPE_OPEN:
                                                // Check whether outer is open as well
                                                CsmCompletionExpression top3 = peekExp(3);
                                                if (getValidExpID(top3) == GENERIC_TYPE_OPEN) {
                                                    popExp();
                                                    top2.addParameter(top);
                                                    top2.setExpID(GENERIC_TYPE);
                                                    addTokenTo(top2); // [TODO] revise possible spliting of the token
                                                    
                                                    popExp();
                                                    top3.addParameter(top2);
                                                    top3.setExpID(GENERIC_TYPE);
                                                    addTokenTo(top3); // [TODO] revise possible spliting of the token
                                                    
                                                } else { // inner is not generic type
                                                    reset();
                                                }
                                                break;
                                                
                                            default:
                                                reset();
                                                break;
                                        }
                                        break;
                                        
                                    default:
                                        reset();
                                        break;
                                }
                                break;

                            case EQ_EXP: // in expression after "type var = "
                                // do nothing - leave the EQ_EXP state
                                break;
                                
                            default:
                                reset();
                                break;
                        }
                    } // else - inside parens inside the exp after "="
                    break;
                }
                
                
//            case CCTokenContext.RUSHIFT_ID: // ">>>"
//                {
//                    if (parenDepth <= 1) { // no parens or in method declaration parms
//                        switch (state) {
//                            case TYPE_VAR:
//                                pushTypeVar();
//                                // continue to TYPE_EXP handling
//                            case TYPE_EXP:
//                                CsmCompletionExpression top = peekExp();
//                                switch (getValidExpID(top)) {
//                                    case VARIABLE:
//                                    case DOT:
//                                    case GENERIC_TYPE:
//                                    case GENERIC_WILD_CHAR:
//                                        CsmCompletionExpression top2 = peekExp2();
//                                        switch (getValidExpID(top2)) {
//                                            case GENERIC_TYPE_OPEN:
//                                                // Check whether outer is open as well
//                                                CsmCompletionExpression top3 = peekExp(3);
//                                                CsmCompletionExpression top4 = peekExp(4);
//                                                if (getValidExpID(top3) == GENERIC_TYPE_OPEN
//                                                && getValidExpID(top4) == GENERIC_TYPE_OPEN
//                                                ) {
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
//                                                } else { // inner is not generic type
//                                                    reset();
//                                                }
//                                                break;
//                                                
//                                            default:
//                                                reset();
//                                                break;
//                                        }
//                                        break;
//                                        
//                                    default:
//                                        reset();
//                                        break;
//                                }
//                                break;
//
//                            case EQ_EXP: // in expression after "type var = "
//                                // do nothing - leave the EQ_EXP state
//                                break;
//                                
//                            default:
//                                reset();
//                                break;
//                        }
//                    } // else - inside parens inside the exp after "="
//                    break;
//                }
                
                
            case CCTokenContext.LPAREN_ID: // "("
                switch (state) {
                    case TYPE_VAR:
                        pushTypeVar();
                        // continue to TYPE_EXP handling
                    case TYPE_EXP:
                    case VAR_EXP: // unclosed var decl. followed by ident -> invalid
                    case INIT:
                        reset();
                        break;

                    case EQ_EXP: // in expression after "type var = "
                        break;

                    default:
                        throw new IllegalStateException();
                }

                // In all cases increase the parenthesis depth
                parenDepth++;
                if (forScope)
                    forScopeParenDepth++;
                break;
                
            case CCTokenContext.RPAREN_ID: // ")"
                switch (state) {
                    case TYPE_VAR:
                        pushTypeVar();
                        // continue to TYPE_EXP handling
                    case TYPE_EXP:
                    case INIT:
                        reset();
                        break;

                    case VAR_EXP: // unclosed var decl. followed by ident -> invalid
                        if (parenDepth == 1) { // method declaration end?
                            formVarExp(true);
                        } else {
                            reset();
                        }
                        break;
                        
                    case EQ_EXP: // in expression after "type var = "
                        break;

                    default:
                        throw new IllegalStateException();

                }
                
                // // In all cases decrease the parenthesis depth
                if (parenDepth > 0) {
                    parenDepth--;
                }
                if (forScope && forScopeParenDepth > 0) {
                    forScopeParenDepth--;
                }
                break;


            case CCTokenContext.LBRACE_ID: // "{" treated as cmd sep.
                if (state != EQ_EXP) {
                    reset();
                    parenDepth = 0; // Reset the parenthesis depth to zero
                    if (!forScope && varMap != null)
                        varMap.pushNewScope();
                    forScope = false;
                    if (!stackedConfigs.isEmpty())
                        eqExpBraceDepth++;
                } else {
                    pushConfig();
                    if (!forScope && varMap != null)
                        varMap.pushNewScope();
                    forScope = false;
                }
                break;

            case CCTokenContext.RBRACE_ID: // "}" treated as cmd sep.
                if ((!stackedConfigs.isEmpty()) && eqExpBraceDepth == 0) {
                    popConfig();
                    if (varMap != null)
                        varMap.removeScope();
                } else {
                    reset();
                    parenDepth = 0; // Reset the parenthesis depth to zero
                    if (varMap != null)
                        varMap.removeScope();                        
                    if (!stackedConfigs.isEmpty())
                        eqExpBraceDepth--;
                }
                break;


            case CCTokenContext.SEMICOLON_ID: // ';' treated as cmd sep.
                if (forScope && forScopeParenDepth == 0) {
                    if (varMap != null)
                        varMap.removeScope();
                    forScope = false;
                }
                switch (state) {
                    case VAR_EXP:
                        formVarExp(true);
                        break;

                    case EQ_EXP:
                        if (parenDepth == 0 || (parenDepth ==1 && forScope)) {
                            formVarExp(false);
                        }

                    default:
                        reset();
                        break;

                }
                break;


            case CCTokenContext.COMMA_ID:
                if (parenDepth <= 1) { // no parens or in method declaration parms
                    switch (state) {
                        case TYPE_EXP:
                            CsmCompletionExpression top2 = peekExp2();
                            if (getValidExpID(top2) == GENERIC_TYPE_OPEN) {
                                CsmCompletionExpression top = peekExp();
                                switch (getValidExpID(top)) {
                                    case VARIABLE:
                                    case DOT:
                                    case GENERIC_TYPE:
                                    case GENERIC_WILD_CHAR:
                                        popExp();
                                        top2.addParameter(top);
                                        addTokenTo(top2); // add "," to open generics type
                                        break;

                                    default:
                                        reset();
                                        break;
                                }

                            } else { // not open generics
                                // and not followed by identifier so it can't be
                                // param separator in parenDepth == 1 as well
                                reset();
                            }
                            break;

                        case VAR_EXP:
                            // can either be "int var1, var2" or m1(int i, String s)
                            formVarExp(parenDepth == 1);
                            break;
                            
                        case EQ_EXP: // in expression after "type var = "
                            if (parenDepth == 0) { // separates variable expressions
                                formVarExp(false);
                                state = VAR_EXP; // expect next variable
                            } // else - commas e.g. in "type var = method(a, b, c)" => do nothing
                            break;

                        default:
                            reset();
                            break;
                    }
                    
                } // else -> in parens -> do nothing
                break;
                
                
            case CCTokenContext.EQ_ID:
                if (parenDepth <= 1) {
                    switch (state) {
                        case VAR_EXP:
//                            formVarExp(false);
                            state = EQ_EXP;
                            break;
                            
                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            reset();
                            break;
                    }
                } // else -> in parens -> do nothing
                break;

                
            case CCTokenContext.WHITESPACE_ID: // whitespace ignored
            case CCTokenContext.LINE_COMMENT_ID: // line comment ignored
            case CCTokenContext.BLOCK_COMMENT_ID: // block comment ignored
                break;
                        
            case CCTokenContext.COLON_ID: // 1.5 enhanced for loop sysntax
                if (parenDepth <= 1) {
                    switch (state) {
                        case VAR_EXP:
                            formVarExp(true); // no more vars after ":" (in foreach only?)
                            break;

                        case EQ_EXP: // in expression after "type var = "
                            // do nothing - leave the EQ_EXP state
                            break;

                        default:
                            reset();
                            break;
                    }
                } // else -> in parens -> do nothing
                break;

            case CCTokenContext.QUESTION_ID:
                if (parenDepth <= 1 && state == TYPE_EXP) {
                    CsmCompletionExpression top = peekExp();
                    if (getValidExpID(top) == GENERIC_TYPE_OPEN) {
                        pushExp(new CsmCompletionExpression(GENERIC_WILD_CHAR));
                        break;
                    }
                }
                if (state != EQ_EXP)
                    reset();
                break;

		///XXX
//            case CCTokenContext.EXTENDS_ID:
//            case CCTokenContext.SUPER_ID:
//                if (parenDepth <= 1 && state == TYPE_EXP) {
//                    if (getValidExpID(peekExp()) == GENERIC_WILD_CHAR) {
//                        break;
//                    }
//                }

            default:
                if (state != EQ_EXP)
                    reset();

        }
        
        return true;
    }
    
    public int eot(int offset) {
        return 0;
    }
    
    public void nextBuffer(char[] buffer, int offset, int len,
    int startPos, int preScan, boolean lastBuffer) {
        this.buffer = buffer;
        bufferStartPos = startPos - offset;
    }
    

    private static class JavaVar implements CsmSyntaxSupport.JavaVariable {
        
        private CsmCompletionExpression typeExp;
        
        private CsmCompletionExpression varExp;
        
        JavaVar(CsmCompletionExpression typeExp, CsmCompletionExpression varExp) {
            this.typeExp = typeExp;
            this.varExp = varExp;
        }

        public CsmCompletionExpression getTypeExpression() {
            return typeExp;
        }
        
        public CsmCompletionExpression getVariableExpression() {
            return varExp;
        }

        public String toString() {
            return "TYPE:\n" + typeExp + "\nVAR:\n" + varExp; // NOI18N
        }

    }

    private static class StackedMap implements Map {

        private Stack stack;

        private StackedMap() {
            stack = new Stack();
        }

        public int size() {
            int size = 0;
            for (Iterator it = stack.iterator(); it.hasNext();) {
                size += ((Map)it.next()).size();
            }
            return size;
        }

        public boolean isEmpty() {
            for (Iterator it = stack.iterator(); it.hasNext();) {
                if (!((Map)it.next()).isEmpty())
                    return false;
            }
            return true;
        }

        public boolean containsKey(Object key) {
            for (Iterator it = stack.iterator(); it.hasNext();) {
                if (((Map)it.next()).containsKey(key))
                    return true;
            }
            return false;
        }

        public boolean containsValue(Object value) {
            for (Iterator it = stack.iterator(); it.hasNext();) {
                if (((Map)it.next()).containsValue(value))
                    return true;
            }
            return false;
        }

        public Object get(Object key) {
            for (Iterator it = stack.iterator(); it.hasNext();) {
                Object o = ((Map)it.next()).get(key);
                if (o != null)
                    return o;
            }
            return null;
        }

        public Object put(Object key, Object value) {
            if (stack.isEmpty())
                pushNewScope();
            return ((Map)stack.peek()).put(key, value);
        }

        public Object remove(Object key) {
            for (Iterator it = stack.iterator(); it.hasNext();) {
                Object o = ((Map)it.next()).remove(key);
                if (o != null)
                    return o;
            }
            return null;
        }

        public void putAll(Map t) {
            if (stack.isEmpty())
                pushNewScope();
            ((Map)stack.peek()).putAll(t);
        }

        public void clear() {
            for (Iterator it = stack.iterator(); it.hasNext();) {
                ((Map)it.next()).clear();
            }
        }

        public Set keySet() {
            Set set = new TreeSet();
            for (Iterator it = stack.iterator(); it.hasNext();) {
                set.addAll(((Map)it.next()).keySet());
            }
            return set;
        }

        public Collection values() {
            Collection col = new ArrayList();
            for (Iterator it = stack.iterator(); it.hasNext();) {
                col.addAll(((Map)it.next()).values());
            }
            return col;
        }

        public Set entrySet() {
            Set set = new HashSet();
            for (Iterator it = stack.iterator(); it.hasNext();) {
                set.addAll(((Map)it.next()).entrySet());
            }
            return set;
        }

        public void pushNewScope() {
            stack.push(new HashMap());
        }

        public void removeScope() {
            if (!stack.isEmpty())
                stack.pop();
        }
    }
}
