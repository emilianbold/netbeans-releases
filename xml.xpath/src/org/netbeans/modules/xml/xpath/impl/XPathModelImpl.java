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

package org.netbeans.modules.xml.xpath.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.Parser;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAdd;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAnd;
import org.apache.commons.jxpath.ri.compiler.CoreOperationDivide;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan;
import org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThanOrEqual;
import org.apache.commons.jxpath.ri.compiler.CoreOperationLessThan;
import org.apache.commons.jxpath.ri.compiler.CoreOperationLessThanOrEqual;
import org.apache.commons.jxpath.ri.compiler.CoreOperationMod;
import org.apache.commons.jxpath.ri.compiler.CoreOperationMultiply;
import org.apache.commons.jxpath.ri.compiler.CoreOperationNegate;
import org.apache.commons.jxpath.ri.compiler.CoreOperationNotEqual;
import org.apache.commons.jxpath.ri.compiler.CoreOperationOr;
import org.apache.commons.jxpath.ri.compiler.CoreOperationSubtract;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.jxpath.ri.compiler.TreeCompiler;
import org.apache.commons.jxpath.ri.compiler.VariableReference;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.StepNodeTest;
import org.netbeans.modules.xml.xpath.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xml.xpath.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.common.MessageManager;

/**
 * Implementation of the XPathModel interface using Apache's JXPath.
 * JXPath does not validate function arguments, i.e., whether the
 * number of arguments is correct, so we may have to do that validation
 * ourselves or wait for a later version.
 * <p>
 * We also implement extensions to handle
 * <a href="http://www-106.ibm.com/developerworks/webservices/library/ws-bpel/#Expressions">
 * expressions in BPEL4WS</a>.
 *
 * @author Enrico Lelina
 * @version 
 */
public class XPathModelImpl
    implements XPathModel {
    
    /** The XPath tree compiler. */
    private Compiler mCompiler;

    /** The logger. */
    private static final Logger mLogger = Logger.getLogger(XPathModelImpl.class.getName());
    
    /** The message manager. */
    private static MessageManager mMsgMgr = MessageManager.getManager(XPathModelImpl.class);


    /** Instantiates a new object. */
    public XPathModelImpl() {
        mCompiler = new XPathTreeCompiler();
    }


    /**
     * Parses an XPath expression.
     * @param expression the XPath expression to parse
     * @return an instance of XPathExpression
     * @throws XPathException for any parsing errors
     */
    public XPathExpression parseExpression(String expression)
        throws XPathException {
        try {
            Object expr = Parser.parseExpression(expression, mCompiler);
            if (expr instanceof Expression) {
                return processExpression((Expression) expr);
            } else {
                XPathException xpe = new XPathException(
                    mMsgMgr.getString("Unhandled_XPath_Expression",
                                            expression, expr.toString()));
                mLogger.log(Level.FINEST, "parseExpression", xpe);
                throw xpe;
            }
        } catch (JXPathException jxe) {
            mLogger.log(Level.FINEST, "parseExpression", jxe);
            throw new XPathException(jxe);
        }
    }

    /**
     * Processes the parsed expression to build an XPathExpression.
     * @param expression the parsed expression
     * @return an instance of XPathExpression
     * @throws XPathException for any errors
     */
    XPathExpression processExpression(Expression expression)
        throws XPathException {
        if (expression instanceof LocationPath) {
            return processLocationPath((LocationPath) expression);
        } else if (expression instanceof CoreFunction) {
            return processCoreFunction((CoreFunction) expression);
        } else if (expression instanceof ExtensionFunction) {
            return processExtensionFunction((ExtensionFunction) expression);
        } else if (expression instanceof CoreOperation) {
            return processCoreOperation((CoreOperation) expression);
        } else if (expression instanceof Constant) {
            return processConstant((Constant) expression);
        } else if (expression instanceof VariableReference) {
            return processVariableReference((VariableReference) expression);            
        } else if (expression instanceof ExpressionPath) {
            return processExpressionPath((ExpressionPath) expression);
        } else {
            XPathException xpe = new XPathException(
                mMsgMgr.getString("Unhandled_Expression_Type",
                                    expression.toString()));
            mLogger.log(Level.FINEST, "processExpression", xpe);
            throw xpe;
        }
    }

    XPathExpression processVariableReference(VariableReference varRef)
        throws XPathException {

//        String qNameStr = varRef.getVariableName().toString();
//        QName varQName = QName.valueOf(qNameStr);
        //String name = QName.getQNameFromString(qName).toString();
        return AbstractXPathModelHelper.getInstance().newXPathVariableReference(varRef);
    }

    /**
     * Processes a Constant expression.
     * @param constant the constant
     * @return the constant expression
     * @throws XPathException for any errors
     */
    XPathExpression processConstant(Constant constant)
        throws XPathException {
        Object value = constant.computeValue(null);
        XPathExpression xpexpr;

        if (value instanceof String) {
            xpexpr = AbstractXPathModelHelper.getInstance().newXPathStringLiteral((String) value);
        } else if (value instanceof Number) {
            xpexpr = AbstractXPathModelHelper.getInstance().newXPathNumericLiteral((Number) value);
        } else {
            XPathException xpe = new XPathException(
                mMsgMgr.getString("Invalid_XPath_Constant", value));
            mLogger.log(Level.FINEST, "processConstant", xpe);
            throw xpe;
        }
        
        mLogger.finer("constant=" + constant + " value=" + value);
        
        return xpexpr;
    }


    /**
     * Processes a CoreFunction expression. Converts the CoreFunction into an
     * XPathExpression and then adds it into the list of values. Then each
     * argument of the CoreFunction is processed.
     * @param coreFunction the core function
     * @return the core function expression
     * @throws XPathException for any errors
     */
    XPathExpression processCoreFunction(CoreFunction coreFunction)
        throws XPathException {
        int code = getCoreFunctionCode(coreFunction);
        Expression[] arguments = coreFunction.getArguments();

        if (-1 == code) {
            XPathException xpe = new XPathException(
                mMsgMgr.getString("Unhandled_XPath_Function",
                                    coreFunction.toString()));
            mLogger.log(Level.FINEST, "processCoreFunction", xpe);
            throw xpe;
        }
        
        XPathOperationOrFuntion xpexpr = AbstractXPathModelHelper.getInstance().newXPathCoreFunction(code);

        // Process the arguments, if any.
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                xpexpr.addChild(processExpression(arguments[i]));
            }
        }
        
        mLogger.finer("coreFunction=" + coreFunction);
        
        return xpexpr;
    }


    /**
     * Processes a CoreOperation expression. Converts the CoreOperation into an
     * XPathExpression and then adds it into the list of values. Then each
     * argument of the CoreOperation is processed.
     * @param coreOperation the core operation
     * @return the core operation expression
     * @throws XPathException for any errors
     */
    XPathExpression processCoreOperation(CoreOperation coreOperation)
        throws XPathException {
        int code = getCoreOperationCode(coreOperation);
        Expression[] arguments = coreOperation.getArguments();

        if (-1 == code) {
            XPathException xpe = new XPathException(
                mMsgMgr.getString("Unhandled_XPath_Operator",
                                    coreOperation.toString()));
            mLogger.log(Level.FINEST, "processCoreOperation", xpe);
            throw xpe;
        }
        
        XPathOperationOrFuntion xpexpr = AbstractXPathModelHelper.getInstance().newXPathCoreOperation(code);

        // Process the arguments. All core operations require arguments.
        // If arguments is null, it means there is a bug -- the error
        // should have been caught in parse expression.
        for (int i = 0; i < arguments.length; i++) {
            xpexpr.addChild(processExpression(arguments[i]));
        }
        
        mLogger.finer("coreOperation=" + coreOperation);
        
        return xpexpr;
    }


    /**
     * Processes an ExtensionFunction expression. Converts the ExtensionFunction
     * into an XPathExpression and then adds it into the list of values. Then
     * each argument of the ExtensionFunction is processed.
     * @param extensionFunction the extension function
     * @return the extension function expression
     * @throws XPathException for any errors
     */
    XPathExpression processExtensionFunction(ExtensionFunction extensionFunction)
        throws XPathException {
        String name = extensionFunction.getFunctionName().toString();
        Expression[] arguments = extensionFunction.getArguments();
        
        if (null == name) {
            XPathException xpe = new XPathException(
                mMsgMgr.getString("Unhandled_XPath_Function",
                                    extensionFunction.toString()));
            mLogger.log(Level.FINEST, "processExtensionFunction", xpe);
            throw xpe;
        } else if (!isValidFunction(name)) {
            XPathException xpe = new XPathException(
                mMsgMgr.getString("Invalid_XPath_Function",
                                    extensionFunction.toString()));
            mLogger.log(Level.FINEST, "processExtensionFunction", xpe);
            throw xpe;
        }
        
        XPathOperationOrFuntion xpexpr =
            AbstractXPathModelHelper.getInstance().newXPathExtensionFunction(name);
        
        // Process the arguments, if any.
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                xpexpr.addChild(processExpression(arguments[i]));
            }
        }
        
        mLogger.finer("extensionFunction=" + extensionFunction
                        + " name=" + name);
                        
        return xpexpr;
    }


    /**
     * Processes a LocationPath expression. Converts the LocationPath into a
     * TreePath whose components are nodes in the given schema model. Adds the
     * TreePath into the list of values. Each step in the location path is
     * equivalent to a TreePath component. TO DO: handle predicates! TO DO:
     * special handling for relative paths!
     * @param locationPath the location path
     * @return the location path expression
     * @throws XPathException for any errors
     */
    XPathExpression processLocationPath(LocationPath locationPath)
        throws XPathException {
        Step[] steps = locationPath.getSteps();
        
        ArrayList stepList = new ArrayList();
        if (steps != null) {
            for (int i = 0; i < steps.length; i++) {
                stepList.add(processStep(steps[i]));
            }
        }
        
        LocationStep[] locSteps = new LocationStepImpl[stepList.size()];
        Iterator iter = stepList.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            locSteps[i] = (LocationStep) iter.next();
        }          
        
        XPathLocationPath path =
            AbstractXPathModelHelper.getInstance().newXPathLocationPath(locSteps);
        path.setAbsolute(locationPath.isAbsolute());
        path.setSimplePath(locationPath.isSimplePath());
        
        return path;
    }


    /**
     * Processes a ExpressionPath expression. Converts the ExpressionPath into a
     * TreePath whose components are nodes in the given schema model. Adds the
     * TreePath into the list of values. Each step in the location path is
     * equivalent to a TreePath component. TO DO: handle predicates! TO DO:
     * special handling for relative paths!
     * @param expressionPath the location path
     * @return the expression path expression
     * @throws XPathException for any errors
     */
    XPathExpression processExpressionPath(ExpressionPath expressionPath)
        throws XPathException {
        Expression rootExpression = expressionPath.getExpression();
        XPathExpression rExpression = null;
        if(rootExpression != null) {
            rExpression = processExpression(rootExpression);
        }
        
        Step[] steps = expressionPath.getSteps();
        
        ArrayList stepList = new ArrayList();
        if (steps != null) {
            for (int i = 0; i < steps.length; i++) {
                stepList.add(processStep(steps[i]));
            }
        }
        
        LocationStep[] locSteps = new LocationStepImpl[stepList.size()];
        Iterator iter = stepList.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            locSteps[i] = (LocationStep) iter.next();
        }          
        
        XPathExpressionPath path =
            AbstractXPathModelHelper.getInstance().newXPathExpressionPath(rExpression, locSteps);
        path.setSimplePath(expressionPath.isSimplePath());
        
        return path;
    }
    
    /**
     * Processes a step in a location path.
     * @param step the step in the location path
     * @return the location step
     * @throws XPathException for any errors
     */
    LocationStep processStep(Step step)
        throws XPathException {
        int axis = getAxis(step.getAxis());
        NodeTest nodeTest = step.getNodeTest();
        Expression[] predicates = step.getPredicates();
        String name = null;
        StepNodeTest stepNodeTest = null;

        if (nodeTest instanceof NodeNameTest) {
            stepNodeTest =
                new StepNodeNameTest(
                    ((NodeNameTest) nodeTest).getNodeName().toString());
        } else if (nodeTest instanceof NodeTypeTest) {
            stepNodeTest =
                new StepNodeTypeTest(
                    getNodeType(((NodeTypeTest) nodeTest).getNodeType()));
        } else {
            XPathException xpe = new XPathException(
                mMsgMgr.getString("Invalid_Location_Step", step.toString()));
            mLogger.log(Level.FINEST, "processStep", xpe);
            throw xpe;
        }
        
        XPathPredicateExpression[] xpathPredicateExpressions = null;
        if (predicates != null && predicates.length > 0) {
            XPathExpression[] xpathPredicates = null;
            xpathPredicates = new XPathExpression[predicates.length];
            xpathPredicateExpressions = new XPathPredicateExpression[predicates.length];
            for (int i = 0, length = predicates.length; i < length; i++) {
                xpathPredicates[i] = processExpression(predicates[i]);
                if (xpathPredicates[i] instanceof XPathNumericLiteral) {
                    xpathPredicates[i] = AbstractXPathModelHelper.getInstance()
                        .newXPathPredicateNumericLiteral
                        (new Long(((XPathNumericLiteral) xpathPredicates[i]).getValue().longValue()));
                }
                
                //now wrap expression which are predicates inside XPathPredicateExpression
                xpathPredicateExpressions[i] = AbstractXPathModelHelper.getInstance()
                        .newXPathPredicateExpression(xpathPredicates[i]);
            }
        }
        return new LocationStepImpl(axis, stepNodeTest, xpathPredicateExpressions);
    }
    
    /**
     * Gets the axis.
     * @param code the axis code
     * @return the axis type or -1 if invalid
     */
    int getAxis(int code) {
        switch (code) {
        case Compiler.AXIS_SELF:
            return LocationStep.AXIS_SELF;
        case Compiler.AXIS_CHILD:
            return LocationStep.AXIS_CHILD;
        case Compiler.AXIS_PARENT:
            return LocationStep.AXIS_PARENT;
        case Compiler.AXIS_ANCESTOR:
            return LocationStep.AXIS_ANCESTOR;
        case Compiler.AXIS_ATTRIBUTE:
            return LocationStep.AXIS_ATTRIBUTE;
        case Compiler.AXIS_NAMESPACE:
            return LocationStep.AXIS_NAMESPACE;
        case Compiler.AXIS_PRECEDING:
            return LocationStep.AXIS_PRECEDING;
        case Compiler.AXIS_FOLLOWING:
            return LocationStep.AXIS_FOLLOWING;
        case Compiler.AXIS_DESCENDANT:
            return LocationStep.AXIS_DESCENDANT;
        case Compiler.AXIS_ANCESTOR_OR_SELF:        
            return LocationStep.AXIS_ANCESTOR_OR_SELF;
        case Compiler.AXIS_DESCENDANT_OR_SELF:
            return LocationStep.AXIS_DESCENDANT_OR_SELF;
        case Compiler.AXIS_FOLLOWING_SIBLING:
            return LocationStep.AXIS_FOLLOWING_SIBLING;
        case Compiler.AXIS_PRECEDING_SIBLING:
            return LocationStep.AXIS_PRECEDING_SIBLING;
        }
        
        return -1;
    }
    
    
    /**
     * Gets the node type.
     * @param code the node type code
     * @return the node type or -1 if invalid
     */
    int getNodeType(int code) {
        switch (code) {
        case Compiler.NODE_TYPE_NODE:
            return LocationStep.NODETYPE_NODE;
        case Compiler.NODE_TYPE_TEXT:
            return LocationStep.NODETYPE_TEXT;
        case Compiler.NODE_TYPE_COMMENT:
            return LocationStep.NODETYPE_COMMENT;
        case Compiler.NODE_TYPE_PI:
            return LocationStep.NODETYPE_PI;
        }
        
        return -1;
    }          


    /**
     * Gets the core function code
     * @param coreFunction the core function
     * @return the function code or -1 if invalid
     * @throws XPathException invalid operation
     */
    int getCoreFunctionCode(CoreFunction coreFunction) {
        int code = coreFunction.getFunctionCode();

        switch (code) {
        case Compiler.FUNCTION_LAST:
            return XPathCoreFunction.FUNC_LAST;
        case Compiler.FUNCTION_POSITION:
            return XPathCoreFunction.FUNC_POSITION;
        case Compiler.FUNCTION_COUNT:
            return XPathCoreFunction.FUNC_COUNT;
        case Compiler.FUNCTION_ID:
            return XPathCoreFunction.FUNC_ID;
        case Compiler.FUNCTION_LOCAL_NAME:
            return XPathCoreFunction.FUNC_LOCAL_NAME;
        case Compiler.FUNCTION_NAMESPACE_URI:
            return XPathCoreFunction.FUNC_NAMESPACE_URI;
        case Compiler.FUNCTION_NAME:
            return XPathCoreFunction.FUNC_NAME;
        case Compiler.FUNCTION_STRING:
            return XPathCoreFunction.FUNC_STRING;
        case Compiler.FUNCTION_CONCAT:
            return XPathCoreFunction.FUNC_CONCAT;
        case Compiler.FUNCTION_STARTS_WITH:
            return XPathCoreFunction.FUNC_STARTS_WITH;
        case Compiler.FUNCTION_CONTAINS:
            return XPathCoreFunction.FUNC_CONTAINS;
        case Compiler.FUNCTION_SUBSTRING_BEFORE:
            return XPathCoreFunction.FUNC_SUBSTRING_BEFORE;
        case Compiler.FUNCTION_SUBSTRING_AFTER:
            return XPathCoreFunction.FUNC_SUBSTRING_AFTER;
        case Compiler.FUNCTION_SUBSTRING:
            return XPathCoreFunction.FUNC_SUBSTRING;
        case Compiler.FUNCTION_STRING_LENGTH:
            return XPathCoreFunction.FUNC_STRING_LENGTH;
        case Compiler.FUNCTION_NORMALIZE_SPACE:
            return XPathCoreFunction.FUNC_NORMALIZE_SPACE;
        case Compiler.FUNCTION_TRANSLATE:
            return XPathCoreFunction.FUNC_TRANSLATE;
        case Compiler.FUNCTION_BOOLEAN:
            return XPathCoreFunction.FUNC_BOOLEAN;
        case Compiler.FUNCTION_NOT:
            return XPathCoreFunction.FUNC_NOT;
        case Compiler.FUNCTION_TRUE:
            return XPathCoreFunction.FUNC_TRUE;
        case Compiler.FUNCTION_FALSE:
            return XPathCoreFunction.FUNC_FALSE;
        case Compiler.FUNCTION_LANG:
            return XPathCoreFunction.FUNC_LANG;
        case Compiler.FUNCTION_NUMBER:
            return XPathCoreFunction.FUNC_NUMBER;
        case Compiler.FUNCTION_SUM:
            return XPathCoreFunction.FUNC_SUM;
        case Compiler.FUNCTION_FLOOR:
            return XPathCoreFunction.FUNC_FLOOR;
        case Compiler.FUNCTION_CEILING:
            return XPathCoreFunction.FUNC_CEILING;
        case Compiler.FUNCTION_ROUND:
            return XPathCoreFunction.FUNC_ROUND;
        case Compiler.FUNCTION_NULL:
            return XPathCoreFunction.FUNC_NULL;
        case Compiler.FUNCTION_KEY:
            return XPathCoreFunction.FUNC_KEY;
        case Compiler.FUNCTION_FORMAT_NUMBER:
            return XPathCoreFunction.FUNC_FORMAT_NUMBER;
        case Compiler.FUNCTION_EXISTS:
            return XPathCoreFunction.FUNC_EXISTS;
        }

        return -1;
    }


    /**
     * Gets the core operation code.
     *
     * @param coreOperation the core operation
     * @return the operation code or -1 if invalid
     * @throws XPathException invalid operation
     */
    int getCoreOperationCode(CoreOperation coreOperation) {
        if (coreOperation instanceof CoreOperationAdd) {
            return XPathCoreOperation.OP_SUM;
        } else if (coreOperation instanceof CoreOperationSubtract) {
            return XPathCoreOperation.OP_MINUS;
        } else if (coreOperation instanceof CoreOperationMultiply) {
            return XPathCoreOperation.OP_MULT;
        } else if (coreOperation instanceof CoreOperationDivide) {
            return XPathCoreOperation.OP_DIV;
        } else if (coreOperation instanceof CoreOperationMod) {
            return XPathCoreOperation.OP_MOD;
        } else if (coreOperation instanceof CoreOperationNegate) {
            return XPathCoreOperation.OP_NEGATIVE;
        } else if (coreOperation instanceof CoreOperationAnd) {
            return XPathCoreOperation.OP_AND;
        } else if (coreOperation instanceof CoreOperationOr) {
            return XPathCoreOperation.OP_OR;
        } else if (coreOperation instanceof CoreOperationEqual) {
            return XPathCoreOperation.OP_EQ;
        } else if (coreOperation instanceof CoreOperationNotEqual) {
            return XPathCoreOperation.OP_NE;
        } else if (coreOperation instanceof CoreOperationLessThan) {
            return XPathCoreOperation.OP_LT;
        } else if (coreOperation instanceof CoreOperationLessThanOrEqual) {
            return XPathCoreOperation.OP_LE;
        } else if (coreOperation instanceof CoreOperationGreaterThan) {
            return XPathCoreOperation.OP_GT;
        } else if (coreOperation instanceof CoreOperationGreaterThanOrEqual) {
            return XPathCoreOperation.OP_GE;
        }
       return -1;
    }
    
    
    /**
     * Determines if a function name is valid. Assumes the function name is
     * not one of the core functions.
     * @param functionName the name of the function
     * @return true if the function name is valid, false otherwise
     */
    static boolean isValidFunction(String functionName) {
        for (int i = 0; i < VALID_FUNCTION_NAMES.length; i++) {
            if (functionName.equals(VALID_FUNCTION_NAMES[i])) {
                return true;
            }
        }
        
        // For bpws, strip out the prefix because there's no guarantee that
        // the prefix is bpws.
        String name = functionName;
        int colon = name.indexOf(':');
        if (colon != -1) {
            name = name.substring(colon + 1, name.length());
        }
        for (int i = 0; i < VALID_BPWS_FUNCTION_NAMES.length; i++) {
            if (name.equals(VALID_BPWS_FUNCTION_NAMES[i])) {
                return true;
            }
        }
        
        return false;
    }
}
