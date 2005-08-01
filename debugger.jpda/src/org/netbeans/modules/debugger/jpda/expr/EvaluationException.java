/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Value;
import com.sun.jdi.InvocationException;

import java.util.*;
import java.text.MessageFormat;

import org.openide.util.NbBundle;

/**
 * This class is a runtime exception because it integrates better with the generated code and
 * it also prevents unnecessary code bloat.
 *
 * @author Maros Sandor
 */
public class EvaluationException extends RuntimeException {
    
    private SimpleNode      node;
    private String          reason;
    private Object      []  params;

    private String          message;

    public EvaluationException(SimpleNode node, String reason, Object[] params) {
        this.node = node;
        this.reason = reason;
        this.params = params;
    }

    public String getMessage() {
        try {
            return getMessageImpl();
        } catch (Exception e) {
            return message = formatMessage("CTL_EvalError_unknownInternalError", null);
        }
    }

    public String getMessageImpl() {
        if (message != null) return message;

        String [] msgParams = null;

        if (reason.equals("unknownNonterminal"))
            msgParams = new String [] { JavaParserTreeConstants.jjtNodeName[node.jjtGetID()] };
        else if (reason.equals("internalError"))
            msgParams = new String [] { JavaParserTreeConstants.jjtNodeName[node.jjtGetID()] };
        else if (reason.equals("invalidArrayInitializer"))
            msgParams = new String [] { params[0] == null ? null : params[0].toString() };
        else if (reason.equals("arraySizeBadType"))
            msgParams = new String [] { params[0] == null ? null : params[0].toString() };
        else if (reason.equals("arrayCreateError"))
            msgParams = new String [] { params[0] == null ? null : params[0].toString() };
        else if (reason.equals("instantiateInterface"))
            msgParams = new String [] { params[0] == null ? null : params[0].toString() };
        else if (reason.equals("castToBooleanRequired"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("castFromBooleanRequired"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("castError"))
            msgParams = new String [] { params[0].toString(), params[1].toString() };
        else if (reason.equals("badOperandForPostfixOperator"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("postfixOperatorEvaluationError"))
            msgParams = new String [] { params[1].toString() };
        else if (reason.equals("badOperandForPrefixOperator"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("prefixOperatorEvaluationError"))
            msgParams = new String [] { params[1].toString() };
        else if (reason.equals("badOperandForUnaryOperator"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("unaryOperatorEvaluationError"))
            msgParams = new String [] { params[1].toString() };
        else if (reason.equals("unknownType"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("internalErrorResolvingType"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("instanceOfLeftOperandNotAReference"))
            msgParams = new String [] { ((Value)params[0]).type().name() };
        else if (reason.equals("conditionalOrAndBooleanOperandRequired"))
            msgParams = new String [] { ((Value)params[0]).type().name() };
        else if (reason.equals("conditionalQuestionMarkBooleanOperandRequired"))
            msgParams = new String [] { ((Value)params[0]).type().name() };
        else if (reason.equals("thisObjectUnavailable"))
            msgParams = null;
        else if (reason.equals("objectReferenceRequiredOnDereference"))
            msgParams = new String [] { ((Value)params[0]).type().name() };
        else if (reason.equals("badArgument"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("argumentsBadSyntax"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("ambigousMethod"))
            msgParams = new String [] { ((Identifier)params[0]).typeContext.name(),  ((Identifier)params[0]).identifier };
        else if (reason.equals("noSuchMethod"))
            msgParams = new String [] { ((Identifier)params[0]).typeContext.name(),  ((Identifier)params[0]).identifier };
        else if (reason.equals("callException"))
            msgParams = new String [] { ((Identifier)params[1]).typeContext.name(),  ((Identifier)params[1]).identifier, params[0].toString() };
        else if (reason.equals("calleeException"))
            msgParams = new String [] { ((Identifier)params[1]).typeContext.name(),  ((Identifier)params[1]).identifier,
                                        ((InvocationException)(params[0])).exception().toString() };
        else if (reason.equals("identifierNotAReference"))
            msgParams = new String [] { ((Value)params[0]).type().name() };
        else if (reason.equals("notarray"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("arrayIndexNAN"))
            msgParams = new String [] { params[1].toString() };
        else if (reason.equals("arrayIndexOutOfBounds"))
            msgParams = new String [] { params[1].toString(), Integer.toString(((ArrayReference)params[0]).length() - 1) };
        else if (reason.equals("unknownVariable"))
            msgParams = new String [] { ((Identifier)params[0]).identifier };
        else if (reason.equals("integerLiteralTooBig"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("badFormatOfIntegerLiteral"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("unknownLiteralType"))
            msgParams = new String [] { params[0].toString() };
        else if (reason.equals("evaluateError"))
//            return Assert.error(node, "evaluateError", value, ((Token) operators[i-1]).image, next);
            msgParams = new String [] { params[1].toString(), params[0].toString(), params[2].toString() };
        else if (reason.equals("notEnclosingType"))
            msgParams = new String [] { ((Identifier)params[0]).typeContext.name(),  ((Identifier)params[0]).superQualifier };
        else if (reason.equals("accessInstanceVariableFromStaticContext"))
            msgParams = new String [] { ((Identifier)params[0]).identifier };
        else if (reason.equals("methodCallOnNull"))
            msgParams = new String[] { params[0].toString() };
        else {
            reason = "unknownInternalError";
            msgParams = null;
        }

        message = formatMessage("CTL_EvalError_" + reason, msgParams);

        return message;
    }

    private String formatMessage(String msg, String [] params) {
        ResourceBundle bundle = NbBundle.getBundle (EvaluationException.class);
        msg = bundle.getString(msg);
        return MessageFormat.format(msg, params);
    }
}
