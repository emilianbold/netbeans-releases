/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xpath.ext.impl;

import javax.xml.namespace.QName;
import org.apache.commons.jxpath.ri.Compiler;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTestType;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;

/**
 * @author nk160297
 *
 * Extendes the Compiler for constructing netbeans specific XPath model.
 */
public class XPathTreeCompiler implements Compiler {

    private XPathModel myXPathModel;
    
    public XPathTreeCompiler(XPathModel xPathModel) {
        myXPathModel = xPathModel;
    }
        
    /**
     * overriden this method to create
     * appropriate Long or Double as the value stored in
     * Constant.
     */
    public Object number(String value) {
        //distinguish between Long and Double
        try {
            long intVal = Long.parseLong(value);
            return myXPathModel.getFactory().
                    newXPathNumericLiteral(new Long(intVal));
        } catch (NumberFormatException ex) {
            //Do Nothing
        }
        //
        try {
            double doubleVal = Double.parseDouble(value);
            return myXPathModel.getFactory().
                    newXPathNumericLiteral(new Double(doubleVal));
        } catch (NumberFormatException ex) {
            //Do Nothing
        }
        //
        return null;
    }

    public Object literal(String value) {
        return myXPathModel.getFactory().newXPathStringLiteral(value);
    }

    public Object qname(String prefix, String name) {
        if (prefix == null || prefix.length() == 0) {
            return new QName(name);
        } else {
            return new QName(null, name, prefix);
        }
    }

    public Object sum(Object[] arguments) {
        return createOperation(arguments, CoreOperationType.OP_SUM);
    }

    public Object minus(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_MINUS);
    }

    public Object multiply(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_MULT);
    }

    public Object divide(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_DIV);
    }

    public Object mod(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_MOD);
    }

    public Object lessThan(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_LT);
    }

    public Object lessThanOrEqual(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_LE);
    }

    public Object greaterThan(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_GT);
    }

    public Object greaterThanOrEqual(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_GE);
    }

    public Object equal(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_EQ);

        // TODO sort out if the old code is necessary or not.
//        if (isNameAttributeTest((XPathExpression) left)) {
//            return new NameAttributeTest((Expression) left, (Expression) right);
//        }
//        else {
//            return createOperation(left, right, XPathCoreOperation.OP_EQ);
//        }
    }

    public Object notEqual(Object left, Object right) {
        return createOperation(left, right, CoreOperationType.OP_NE);
    }

    public Object minus(Object argument) {
        XPathCoreOperation operation = myXPathModel.getFactory().
                newXPathCoreOperation(CoreOperationType.OP_NEGATIVE);
        operation.addChild((XPathExpression)argument);
        return operation;
    }

    public Object variableReference(Object qName) {
        assert qName instanceof QName;
        XPathVariableReference var = myXPathModel.getFactory().
                newXPathVariableReference((QName)qName);
        return var;
    }

    public Object function(int code, Object[] args) {
        CoreFunctionType funcType = convertFunctionCodeToType(code);
        if (funcType == null) {
            return null;
        } 
        //
        XPathCoreFunction func = myXPathModel.getFactory().
                newXPathCoreFunction(funcType);
        if (args != null) {
            for (Object arg : args) {
                func.addChild((XPathExpression)arg);
            }
        }
        return func;
    }

    public Object function(Object name, Object[] args) {
        XPathExtensionFunction func = myXPathModel.getFactory().
                newXPathExtensionFunction((QName)name);
        if (args != null) {
            for (Object arg : args) {
                func.addChild((XPathExpression)arg);
            }
        }
        return func;
    }

    public Object and(Object[] arguments) {
        return createOperation(arguments, CoreOperationType.OP_AND);
    }

    public Object or(Object[] arguments) {
        return createOperation(arguments, CoreOperationType.OP_OR);
    }

    public Object union(Object[] arguments) {
        return createOperation(arguments, CoreOperationType.OP_UNION);
    }

    public Object locationPath(boolean absolute, Object[] steps) {
        XPathLocationPath locPath = myXPathModel.getFactory().
                newXPathLocationPath(toLocationStepArray(steps));
        locPath.setAbsolute(absolute);
        return locPath;
    }

    public Object expressionPath(Object expression, Object[] predicates, Object[] steps) {
        // TODO Predicates are ignored here ????
        XPathExpressionPath exprPath = myXPathModel.getFactory().
                newXPathExpressionPath((XPathExpression)expression, 
                toLocationStepArray(steps));
        return exprPath;
    }

    public Object nodeNameTest(Object qname) {
        return new StepNodeNameTest((QName)qname);
    }

    public Object nodeTypeTest(int nodeType) {
        return new StepNodeTypeTest(nodeType, null);
    }

    public Object processingInstructionTest(String instruction) {
        // instruction is ignored in this model
        StepNodeTypeTest newST = new StepNodeTypeTest(
                StepNodeTestType.NODETYPE_PI, instruction);
        return newST;
    }

    public Object step(int axis, Object nodeTest, Object[] predicates) {
        LocationStep newStep = new LocationStepImpl(
                myXPathModel, 
                axis, 
                (StepNodeTest)nodeTest, 
                toPredicateExpressionArray(predicates));
        return newStep;
    }

    // =========================================================================
    
    private XPathPredicateExpression[] toPredicateExpressionArray(Object[] array) {
        XPathPredicateExpression[] pExpArray = null;
        if (array != null) {
            pExpArray = new XPathPredicateExpression[array.length];
            for (int i = 0; i < pExpArray.length; i++) {
                XPathExpression expr = (XPathExpression) array[i];
                if (expr instanceof XPathPredicateExpression) {
                    pExpArray[i] = (XPathPredicateExpression)expr;
                } else {
                    pExpArray[i] = myXPathModel.getFactory().
                            newXPathPredicateExpression(expr);
                }
            }
        }
        return pExpArray;
    }

    private LocationStep[] toLocationStepArray(Object[] array) {
        LocationStep[] stepArray = null;
        if (array != null) {
            stepArray = new LocationStep[array.length];
            for (int i = 0; i < stepArray.length; i++) {
                stepArray[i] = (LocationStep) array[i];
            }
        }
        return stepArray;
    }


    
//    private boolean isNameAttributeTest(XPathExpression arg) {
//        if (!(arg instanceof XPathLocationPath)) {
//            return false;
//        }
//
//        LocationStep[] steps = ((XPathLocationPath)arg).getSteps();
//        if (steps.length != 1) {
//            return false;
//        }
//        if (steps[0].getAxis() != LocationStep.AXIS_ATTRIBUTE) {
//            return false;
//        }
//        StepNodeTest test = steps[0].getNodeTest();
//        if (!(test instanceof StepNodeNameTest)) {
//            return false;
//        }
//        if (!((StepNodeNameTest)test).getNodeName().equals("name")) {
//            return false;
//        }
//        return true;
//    }
    
    private Object createOperation(Object left, Object right, 
            CoreOperationType operationType) {
        XPathCoreOperation operation = myXPathModel.getFactory().
                newXPathCoreOperation(operationType);
        operation.addChild((XPathExpression)left);
        operation.addChild((XPathExpression)right);
        return operation;
    }
    
    private Object createOperation(Object[] arguments, 
            CoreOperationType operationType) {
        XPathCoreOperation operation = myXPathModel.getFactory().
                newXPathCoreOperation(operationType);
        for (Object arg : arguments) {
            operation.addChild((XPathExpression)arg);
        }
        return operation;
    }

    private CoreFunctionType convertFunctionCodeToType(int code) {
        switch (code) {
        case 1: return CoreFunctionType.FUNC_LAST;
        case 2: return CoreFunctionType.FUNC_POSITION;
        case 3: return CoreFunctionType.FUNC_COUNT;
        case 4: return CoreFunctionType.FUNC_ID;
        case 5: return CoreFunctionType.FUNC_LOCAL_NAME;
        case 6: return CoreFunctionType.FUNC_NAMESPACE_URI;
        case 7: return CoreFunctionType.FUNC_NAME;
        case 8: return CoreFunctionType.FUNC_STRING;
        case 9: return CoreFunctionType.FUNC_CONCAT;
        case 10: return CoreFunctionType.FUNC_STARTS_WITH;
        case 11: return CoreFunctionType.FUNC_CONTAINS;
        case 12: return CoreFunctionType.FUNC_SUBSTRING_BEFORE;
        case 13: return CoreFunctionType.FUNC_SUBSTRING_AFTER;
        case 14: return CoreFunctionType.FUNC_SUBSTRING;
        case 15: return CoreFunctionType.FUNC_STRING_LENGTH;
        case 16: return CoreFunctionType.FUNC_NORMALIZE_SPACE;
        case 17: return CoreFunctionType.FUNC_TRANSLATE;
        case 18: return CoreFunctionType.FUNC_BOOLEAN;
        case 19: return CoreFunctionType.FUNC_NOT;
        case 20: return CoreFunctionType.FUNC_TRUE;
        case 21: return CoreFunctionType.FUNC_FALSE;
        case 22: return CoreFunctionType.FUNC_LANG;
        case 23: return CoreFunctionType.FUNC_NUMBER;
        case 24: return CoreFunctionType.FUNC_SUM;
        case 25: return CoreFunctionType.FUNC_FLOOR;
        case 26: return CoreFunctionType.FUNC_CEILING;
        case 27: return CoreFunctionType.FUNC_ROUND;
        //
        // WRONG Functions
// COMMENTED BECAUSE THE RUNTIME DOESN'T SUPPORT THEM!
//        case 28: return CoreFunctionType.FUNC_NULL;
//        case 29: return CoreFunctionType.FUNC_KEY;
//        case 30: return CoreFunctionType.FUNC_FORMAT_NUMBER;
        case 28: 
        case 29: 
        case 30: return null;
        }
        //
        assert true : "Unknown Core Function";
        return null;
    }
    
}
