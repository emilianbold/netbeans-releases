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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.NbBundle;


/**
 * Engine that evaluates a Java expression in a context of a running JVM. The
 * JVM (or at least the current thread) must be suspended. A single instance of this evaluator may
 * only be used from a single thread, no multithreading support is provided.
 *
 * TODO: the evaluator should check the expression's language version
 *
 * @author Maros Sandor
 */
public class Evaluator implements JavaParserVisitor {
    
    private static final boolean      verbose = 
        System.getProperty ("netbeans.debugger.noInvokeMethods") != null;
    
    private static final Logger loggerMethod = Logger.getLogger("org.netbeans.modules.debugger.jpda.invokeMethod"); // NOI18N
    private static final Logger loggerValue = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N

    private Expression              expression;
    private EvaluationContext       evaluationContext;

    private VirtualMachine          vm;
    private StackFrame              frame;
    private ThreadReference         frameThread;
    private int                     frameIndex;

    private SimpleNode              currentNode;
    private String                  currentPackage;
    private Operators               operators;

    Evaluator(Expression expression, EvaluationContext context) {
        this.expression = expression;
        this.evaluationContext = context;
    }

    /**
     * Evaluates the expression for which it was created.
     *
     * @return the result of evaluating the expression as a JDI Value object.
     *         It returns null if the result itself is null.
     * @throws EvaluationException if the expression cannot be evaluated for whatever reason
     * @throws IncompatibleThreadStateException if the context thread is in an
     * incompatible state (running, dead)
     */
    public Value evaluate() throws EvaluationException, IncompatibleThreadStateException
    {
        frame = evaluationContext.getFrame();
        vm = evaluationContext.getFrame().virtualMachine();
        frameThread = frame.thread();
        frameIndex = indexOf(frameThread.frames(), frame);
        if (frameIndex == -1) {
            throw new IncompatibleThreadStateException("Thread does not contain current frame");
        }
        currentPackage = evaluationContext.getFrame().location().declaringType().name();
        int idx = currentPackage.lastIndexOf('.');
        currentPackage = (idx > 0) ? currentPackage.substring(0, idx + 1) : "";
        operators = new Operators(vm);
        SimpleNode rootNode = expression.getRoot();
        return (Value) rootNode.jjtAccept(this, null);
    }

    private int indexOf(List frames, StackFrame frame) {
        int n = frames.size();
        Location loc = frame.location();
        for (int i = 0; i < n; i++) {
            if (loc.equals(((StackFrame)frames.get(i)).location())) return i;
        }
        return -1;
    }

    public Object visit(SimpleNode node, Object data) throws EvaluationException {
        currentNode = node;
        switch (node.jjtGetID())
        {
        case JavaParserTreeConstants.JJTRESULTTYPE:
            return visitResultType(node, data);

        case JavaParserTreeConstants.JJTARRAYINITIALIZER:
            return visitArrayInitializer(node, data);

        case JavaParserTreeConstants.JJTARRAYDIMSANDINITS:
            return visitArrayDimsAndInits(node, data);

        case JavaParserTreeConstants.JJTALLOCATIONEXPRESSION:
            return visitAllocationExpression(node, data);

        case JavaParserTreeConstants.JJTARGUMENTLIST:
            return visitArgumentList(node, data);

        case JavaParserTreeConstants.JJTARGUMENTS:
            return visitArguments(node, data);

        case JavaParserTreeConstants.JJTCASTEXPRESSION:
            return visitCastExpression(node, data);

        case JavaParserTreeConstants.JJTPOSTFIXEXPRESSION:
            return visitPostfixExpression(node, data);

        case JavaParserTreeConstants.JJTPREDECREMENTEXPRESSION:
        case JavaParserTreeConstants.JJTPREINCREMENTEXPRESSION:
            return visitPrefixExpression(node, data);

        case JavaParserTreeConstants.JJTUNARYEXPRESSION:
        case JavaParserTreeConstants.JJTUNARYEXPRESSIONNOTPLUSMINUS:
            return visitUnaryExpression(node, data);

        case JavaParserTreeConstants.JJTCLASSORINTERFACETYPE:
            return visitClassOrInterfaceType(node, data);

        case JavaParserTreeConstants.JJTIDENTIFIER:
            return visitIdentifier(node, data);

        case JavaParserTreeConstants.JJTPRIMITIVETYPE:
            return visitPrimitiveType(node, data);

        case JavaParserTreeConstants.JJTREFERENCETYPE:
            return visitReferenceType(node, data);

        case JavaParserTreeConstants.JJTNAME:
            return visitName(node, data);

        case JavaParserTreeConstants.JJTEXPRESSION:
            return visitExpression(node, data);

        case JavaParserTreeConstants.JJTPRIMARYEXPRESSION:
            return visitPrimaryExpression(node, data);

        case JavaParserTreeConstants.JJTPRIMARYPREFIX:
            return visitPrimaryPrefix(node, data);

        case JavaParserTreeConstants.JJTPRIMARYSUFFIX:
            return visitPrimarySuffix(node, data);

        case JavaParserTreeConstants.JJTCONDITIONALEXPRESSION:
            return visitConditionalExpression(node, data);

        case JavaParserTreeConstants.JJTCONDITIONALOREXPRESSION:
        case JavaParserTreeConstants.JJTCONDITIONALANDEXPRESSION:
            return visitConditionalOrAndExpression(node, data);

        case JavaParserTreeConstants.JJTSHIFTEXPRESSION:
        case JavaParserTreeConstants.JJTRELATIONALEXPRESSION:
        case JavaParserTreeConstants.JJTEQUALITYEXPRESSION:
        case JavaParserTreeConstants.JJTINCLUSIVEOREXPRESSION:
        case JavaParserTreeConstants.JJTANDEXPRESSION:
        case JavaParserTreeConstants.JJTEXCLUSIVEOREXPRESSION:
        case JavaParserTreeConstants.JJTADDITIVEEXPRESSION:
        case JavaParserTreeConstants.JJTMULTIPLICATIVEEXPRESSION:
            return visitBinaryExpression(node, data);

        case JavaParserTreeConstants.JJTINSTANCEOFEXPRESSION:
            return visitInstanceOfExpression(node, data);

        case JavaParserTreeConstants.JJTLITERAL:
            return visitLiteral(node, data);

        case JavaParserTreeConstants.JJTBOOLEANLITERAL:
            return visitBooleanLiteral(node, data);

        case JavaParserTreeConstants.JJTNULLLITERAL:
            return null;
        }
        return Assert.error(node, "unknownNonterminal");
    }

    private ObjectReference primitiveClass(String name) throws IncompatibleThreadStateException {
        ReferenceType primType = resolveType("java.lang." + name.substring(0, 1).toUpperCase() + name.substring(1));
        try {
            if (loggerValue.isLoggable(Level.FINE)) {
                loggerValue.fine("STARTED : "+primType+".getValue("+primType.fieldByName("TYPE")+")");
            }
            return (ObjectReference) primType.getValue(primType.fieldByName("TYPE"));
        } finally {
            if (loggerValue.isLoggable(Level.FINE)) {
                loggerValue.fine("FINISHED: "+primType+".getValue("+primType.fieldByName("TYPE")+")");
            }
        }
    }

    private Object visitResultType(SimpleNode node, Object data) {
        try {
            if (node.getAttribute("void") != null) {
                return primitiveClass("void");
            }
            Type type = (Type) node.jjtGetChild(0).jjtAccept(this, data);
            if (type instanceof ReferenceType) return type;

            return primitiveClass(type.name());
        } catch (IncompatibleThreadStateException e) {
            return Assert.error(node, "internalErrorResolvingType", "void");
        }
    }

    private Object visitArrayInitializer(SimpleNode node, Object data) {
        Object [] values = new Object[node.jjtGetNumChildren()];
        for (int i = 0; i < values.length; i++) {
            values[i] = node.jjtGetChild(i).jjtAccept(this, data);
            if (!(values[i] instanceof Value) && !(values[i] instanceof Object[])) {
                Assert.error(node, "invalidArrayInitializer", values[i]);
            }
        }
        return values;
    }

    private Object visitArrayDimsAndInits(SimpleNode node, Object data) {

        Type arrayType = (Type) data;
        int dimensions = ((Integer) node.getAttribute("dimensions")).intValue();
        ArrayReference arrayRef = null;

        try {
            if (node.getAttribute("initializers") != null) {
                Object [] initValues = (Object []) node.jjtGetChild(0).jjtAccept(this, data);
                return createArray(arrayType, dimensions, initValues);
            } else {
                int sizeCount = node.jjtGetNumChildren();
                int [] sizes = new int[sizeCount];
                for (int i = 0; i < sizeCount; i++) {
                    Object sizeObj = node.jjtGetChild(i).jjtAccept(this, data);
                    Assert.assertAssignable(sizeObj, PrimitiveValue.class, node, "arraySizeBadType", sizeObj);
                    Assert.assertNotAssignable(sizeObj, BooleanValue.class, node, "arraySizeBadType", sizeObj);
                    Assert.assertNotAssignable(sizeObj, FloatValue.class, node, "arraySizeBadType", sizeObj);
                    Assert.assertNotAssignable(sizeObj, DoubleValue.class, node, "arraySizeBadType", sizeObj);
                    sizes[i] = ((PrimitiveValue) sizeObj).intValue();
                }
                return createArray(arrayType, dimensions, sizes, 0);
            }
        } catch (IncompatibleThreadStateException e) {
            Assert.error(node, "arrayCreateError", e);
        } catch (ClassNotLoadedException e) {
            Assert.error(node, "arrayCreateError", e);
        } catch (InvalidTypeException e) {
            Assert.error(node, "arrayCreateError", e);
        } catch (UnsupportedOperationException uoex) {
            return Assert.error(node, "calleeException", uoex);
        }

        return arrayRef;
    }

    private String brackets = "[[[[[[[[[[[[[[[[[[[";
    private String brackets(int length) {
        if (brackets.length() < length) {
            char [] bracketsArray = new char[length];
            Arrays.fill(bracketsArray, '[');
            brackets = new String(bracketsArray);
        }
        return brackets.substring(0, length);
    }

    private ArrayReference createArray(Type baseType, int dimensions, int [] sizes, int index) throws IncompatibleThreadStateException,
            ClassNotLoadedException, InvalidTypeException {

        ArrayType arrayType = (ArrayType) resolveType(brackets(dimensions) + baseType.signature());
        ArrayReference arrayRef = arrayType.newInstance(sizes[index]);
        if (sizes.length > index + 1) {
            for (int i = 0; i < sizes[index]; i++) {
                arrayRef.setValue(i, createArray(baseType, dimensions - 1, sizes, index + 1));
            }
        }
        return arrayRef;
    }

    private ArrayReference createArray(Type baseType, int dimensions, Object [] values) throws IncompatibleThreadStateException,
            ClassNotLoadedException, InvalidTypeException {

        ArrayType arrayType = (ArrayType) resolveType(brackets(dimensions) + baseType.signature());
        ArrayReference arrayRef = arrayType.newInstance(values.length);
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Object []) {
                arrayRef.setValue(i, createArray(baseType, dimensions - 1, (Object[]) values[i]));
            } else {
                arrayRef.setValue(i, (Value) values[i]);
            }
        }
        return arrayRef;
    }

    private Object visitAllocationExpression(SimpleNode node, Object data) {

        Type arrayType = (Type) node.jjtGetChild(0).jjtAccept(this, data);

        // new object creation
        if (((SimpleNode) node.jjtGetChild(1)).jjtGetID() == JavaParserTreeConstants.JJTARGUMENTS) {
            if (arrayType instanceof ClassType) {
                Identifier fvmc = new Identifier(false, (ReferenceType) arrayType, "<init>");
                return node.jjtGetChild(1).jjtAccept(this, fvmc);
            }
            Assert.assertNotAssignable(arrayType, InterfaceType.class, node, "instantiateInterface", arrayType.name());
        }

        // an array
        return node.jjtGetChild(1).jjtAccept(this, arrayType);
    }

    private Object visitPrimitiveType(SimpleNode node, Object data) {
        //TODO: cache primitive types
        Token token = (Token) node.getAttribute("token");
        switch (token.kind) {
        case JavaParserConstants.BOOLEAN:
            return vm.mirrorOf(true).type();
        case JavaParserConstants.CHAR:
            return vm.mirrorOf('a').type();
        case JavaParserConstants.BYTE:
            return vm.mirrorOf((byte)0).type();
        case JavaParserConstants.SHORT:
            return vm.mirrorOf((short)0).type();
        case JavaParserConstants.INT:
            return vm.mirrorOf(0).type();
        case JavaParserConstants.LONG:
            return vm.mirrorOf(0L).type();
        case JavaParserConstants.FLOAT:
            return vm.mirrorOf(1.0f).type();
        case JavaParserConstants.DOUBLE:
            return vm.mirrorOf(1.0).type();
        default:
            throw new RuntimeException("Unknown primitive type: " + token.image);
        }
    }

    private Object visitCastExpression(SimpleNode node, Object data) {
        Object value = node.jjtGetChild(1).jjtAccept(this, data);
        if (value == null) return null;
        Type castType = (Type) node.jjtGetChild(0).jjtAccept(this, data);

        if (value instanceof PrimitiveValue) {
            PrimitiveValue primValue = (PrimitiveValue) value;
            if (primValue instanceof BooleanValue) {
                Assert.assertAssignable(castType, BooleanType.class, node, "castToBooleanRequired", primValue, castType);
                return primValue;
            }
            Assert.assertNotAssignable(castType, BooleanType.class, node, "castFromBooleanRequired", primValue, castType);
            if (castType instanceof ByteType) {
                return vm.mirrorOf(primValue.byteValue());
            } else if (castType instanceof CharType) {
                return vm.mirrorOf(primValue.charValue());
            } else if (castType instanceof DoubleType) {
                return vm.mirrorOf(primValue.doubleValue());
            } else if (castType instanceof FloatType) {
                return vm.mirrorOf(primValue.floatValue());
            } else if (castType instanceof IntegerType) {
                return vm.mirrorOf(primValue.intValue());
            } else if (castType instanceof LongType) {
                return vm.mirrorOf(primValue.longValue());
            } else {
                return vm.mirrorOf(primValue.shortValue());
            }
        }

        // value is an object reference
        ObjectReference valueType = (ObjectReference) value;
        if (!instanceOf(valueType.type(), castType)) {
            Assert.error(node, "castError", valueType.type(), castType);
        }
        return value;
    }

    /**
     * Evaluates a postfix expression (i++). This evaluation does NOT modify the variable i as would be
     * the case in the real life. It also does not check that the operand is a variable, thus 6++ is also legal.
     * These checks are really not necessary here in the evaluator.
     *
     * @param node
     * @param data
     * @return
     */
    private Object visitPostfixExpression(SimpleNode node, Object data) {
        Object value = node.jjtGetChild(0).jjtAccept(this, data);
        Assert.assertAssignable(value, PrimitiveValue.class, node, "badOperandForPostfixOperator", value);
        Assert.assertNotAssignable(value, BooleanValue.class, node, "badOperandForPostfixOperator", value);

        Token operator = (Token) node.getAttribute("operator");
        try {
            return operators.evaluate(operator, (PrimitiveValue) value);
        } catch (IllegalArgumentException e) {
            return Assert.error(node, "postfixOperatorEvaluationError", operator, e);
        }
    }

    /**
     * Evaluates a prefix expression (++i). This evaluation does NOT modify the variable i as would be
     * the case in the real life. It also does not check that the operand is a variable, thus ++6 is also legal.
     * These checks are really not necessary here in the evaluator.
     *
     * @param node
     * @param data
     * @return
     */
    private Object visitPrefixExpression(SimpleNode node, Object data) {
        Object value = node.jjtGetChild(0).jjtAccept(this, data);
        Assert.assertAssignable(value, PrimitiveValue.class, node, "badOperandForPrefixOperator", value);
        Assert.assertNotAssignable(value, BooleanValue.class, node, "badOperandForPrefixOperator", value);

        Token operator = (Token) node.getAttribute("operator");
        try {
            return operators.evaluate(operator, (PrimitiveValue) value);
        } catch (IllegalArgumentException e) {
            return Assert.error(node, "prefixOperatorEvaluationError", operator, e);
        }
    }

    private Object visitUnaryExpression(SimpleNode node, Object data) {

        Object value = node.jjtGetChild(0).jjtAccept(this, data);
//        System.out.println("In visitUnaryExpression:");
//        System.out.println("value -> " + value);
//        System.out.println("value.class -> " + value.getClass());
//        System.out.println("PrimitiveValue.class -> " + PrimitiveValue.class);
//        System.out.println("BooleanValue.class -> " + BooleanValue.class);
//        System.out.println("Assignable ? " + BooleanValue.class.isAssignableFrom(value.getClass()));
        Assert.assertAssignable(value, PrimitiveValue.class, node, "badOperandForUnaryOperator", value);
        // Assert on next line is probably a mistake:
        //Assert.assertNotAssignable(value, BooleanValue.class, node, "badOperandForUnaryOperator", value);
       

        Token operator = (Token) node.getAttribute("operator");
        try {
            return operators.evaluate(operator, (PrimitiveValue) value);
        } catch (IllegalArgumentException e) {
            return Assert.error(node, "unaryOperatorEvaluationError", operator, e);
        }
    }

    private Object visitIdentifier(SimpleNode node, Object data) {
        return ((Token) node.getAttribute("token")).image;
    }

    private Object visitClassOrInterfaceType(SimpleNode node, Object data) {

        StringBuffer fullName = new StringBuffer();

        int n = node.jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            String namePart = (String) node.jjtGetChild(i).jjtAccept(this, data);
            fullName.append('.');
            fullName.append(namePart);

            if (i < n-1) {
                SimpleNode nextNode = (SimpleNode) node.jjtGetChild(i + 1);
                if (nextNode.jjtGetID() == JavaParserTreeConstants.JJTTYPEARGUMENTS) {
                    i++;
                }
            }
        }

        String name = fullName.substring(1);
        try {
            return resolveType(name);
        } catch (IncompatibleThreadStateException e) {
            return Assert.error(node, "internalErrorResolvingType", name);
        }
    }

    /**
     * Resolving of types is slow.
     *
     * @param name
     * @return
     * @throws IncompatibleThreadStateException
     */
    private ReferenceType resolveType(String name) throws IncompatibleThreadStateException {
        ReferenceType type;

        if (name.charAt(0) == '[') {
            if ((type = getClass(name)) != null) return type;
            Assert.error(currentNode, "unknownType", name);
        }

        String innerName = frame.location().declaringType().name() + "$" + name;
        if ((type = getClass(innerName)) != null) return type;

        int idx = name.lastIndexOf('.');
        if (idx == -1) {
            if ((type = getClass(currentPackage + name)) != null) return type;
        } else {
            if ((type = getClass(name)) != null) return type;
        }

        if (idx != -1) {
            innerName = name.substring(0, idx) + "$" + name.substring(idx + 1);
            if (innerName.indexOf('.') == -1) innerName = currentPackage + innerName;
            if ((type = getClass(innerName)) != null) return type;
        }

        List imports = evaluationContext.getImports();
        for (Iterator i = imports.iterator(); i.hasNext();) {
            String importStatement = (String) i.next();
            int ix = importStatement.lastIndexOf('.');
            String qualifier = importStatement.substring(ix + 1);
            if (!qualifier.equals("*") && !qualifier.equals(name)) continue;
            String fullName = importStatement.substring(0, ix + 1) + name;
            type = getClass(fullName);
            if (type != null) return type;
        }

        Assert.error(currentNode, "unknownType", name);
        return null;
    }

    private ReferenceType getClass(String typeName) throws IncompatibleThreadStateException {

        List classes = vm.classesByName(typeName);
        if (classes.size() != 0) {
            return (ReferenceType) classes.get(0);
        }

//        if (forName == null) {
//            try {
//                ClassObjectReference executingClass = frame.location().declaringType().classObject();
//                ClassType currentClass = (ClassType) executingClass.referenceType();
//                forName = currentClass.concreteMethodByName("forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
//            } catch (Exception e) {
//                // The should not happen
//            }
//        }
//
//        try {
//            ClassLoaderReference executingClassloader = frame.location().declaringType().classLoader();
//            ClassObjectReference executingClass = frame.location().declaringType().classObject();
//            List args = new ArrayList();
//            args.add(vm.mirrorOf(typeName));
//            args.add(vm.mirrorOf(true));
//            args.add(executingClassloader);
//            if (verbose) {
//                throw new UnsupportedOperationException (NbBundle.getMessage (
//                    Evaluator.class, 
//                    "CTL_UnsupportedOperationException"
//                )); 
//            }
//            ClassObjectReference cor = (ClassObjectReference) executingClass.
//                invokeMethod (frameThread, forName, args, 0);
//            return cor.reflectedType();
//        } catch (Exception e) {
//            // The class cannot be loaded, return null
//        } finally {
//            // the stack frame may have been invalidated by invoking the forName method
//            frame = frameThread.frame(frameIndex);
//        }
        return null;
    }

    private Object visitReferenceType(SimpleNode node, Object data) {
        Type baseType = (Type) node.jjtGetChild(0).jjtAccept(this, data);
        int dimensions = ((Integer) node.getAttribute("arrayCount")).intValue();
        if (dimensions > 0) {
            try {
                return resolveType(brackets(dimensions) + baseType.signature().replace('/', '.'));
            } catch (IncompatibleThreadStateException e) {
                Assert.error(node, "internalError");
            }
        }
        return baseType;
    }

    private Object visitInstanceOfExpression(SimpleNode node, Object data) {

        Object leftOper = node.jjtGetChild(0).jjtAccept(this, data);
        if (leftOper == null) return vm.mirrorOf(false);
        Assert.assertAssignable(leftOper, ObjectReference.class, node, "instanceOfLeftOperandNotAReference", leftOper);

        ReferenceType left = ((ObjectReference) leftOper).referenceType();                  // org.netbeans.Sucks
        ReferenceType right = (ReferenceType) node.jjtGetChild(1).jjtAccept(this, data);    // int []

        return vm.mirrorOf(instanceOf(left, right));
    }

    private boolean instanceOf(Type left, Type right) {
        if (left == null) return false;
        if (left.equals(right)) return true;

        if (right instanceof ArrayType) {
            if (!(left instanceof ArrayType)) {
                return false;
            } else {
                ArrayType leftArray = (ArrayType) left;
                ArrayType rightArray = (ArrayType) right;
                Type leftType;
                Type rightType;
                try {
                    leftType = leftArray.componentType();
                    rightType = rightArray.componentType();
                } catch (ClassNotLoadedException e) {
                    // TODO: load missing classes
                    return false;
                }
                return instanceOf(leftType, rightType);
            }
        }

        if (left instanceof ClassType) {
            ClassType classLeft = (ClassType) left;
            if (right instanceof InterfaceType) {
                List ifaces = classLeft.allInterfaces();
                for (Iterator i = ifaces.iterator(); i.hasNext();) {
                    InterfaceType type = (InterfaceType) i.next();
                    if (type.equals(right)) return true;
                }
                return false;
            } else {  // right instanceof ClassType
                for (;;) {
                    classLeft = classLeft.superclass();
                    if (classLeft == null) return false;
                    if (classLeft.equals(right)) return true;
                }
            }
        }

        return false;
    }


    private Object visitConditionalOrAndExpression(SimpleNode node, Object data) {

        Token operator = (Token) node.getAttribute("operator");

        int n = node.jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            Object value = node.jjtGetChild(i).jjtAccept(this, data);
            Assert.assertAssignable(value, BooleanValue.class, node, "conditionalOrAndBooleanOperandRequired", value);
            boolean val = ((BooleanValue) value).booleanValue();
            if (operator.kind == JavaParserConstants.SC_OR && val || operator.kind == JavaParserConstants.SC_AND && !val) {
                return value;
            }
        }
        return vm.mirrorOf(operator.kind == JavaParserConstants.SC_AND);
    }

    private Object visitConditionalExpression(SimpleNode node, Object data) {

        Object condition = node.jjtGetChild(0).jjtAccept(this, data);
        Assert.assertAssignable(condition, BooleanValue.class, node, "conditionalQuestionMarkBooleanOperandRequired", condition);

        boolean val = ((BooleanValue) condition).booleanValue();

        if (val) {
            return node.jjtGetChild(1).jjtAccept(this, data);
        } else {
            return node.jjtGetChild(2).jjtAccept(this, data);
        }
    }

    private Object visitBooleanLiteral(SimpleNode node, Object data) {
        Token token = (Token) node.getAttribute("token");
        return vm.mirrorOf(token.kind == JavaParserConstants.TRUE);
    }

    private Object visitName(SimpleNode node, Object data) {
        Object [] tokens = node.getAttributes("token");
        StringBuffer name = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            name.append('.');
            name.append(tokens[i]);
        }
        return name.substring(1);
    }

    private Object visitPrimaryPrefix(SimpleNode node, Object data) {

        if (node.jjtGetNumChildren() == 0) {
            ObjectReference thisObject = frame.thisObject();
            if (thisObject == null) {
                Assert.error(node, "thisObjectUnavailable");
            }

            if (node.getAttribute("this") != null) return thisObject;     // this

            // (X.)*super.X?
            String qualifier = (String) node.getAttribute("qualifier");
            String identifier = (String) node.getAttribute("identifier");

            return new Identifier((ObjectReference) thisObject, identifier, qualifier);
        }

        SimpleNode first = (SimpleNode) node.jjtGetChild(0);
        switch (first.jjtGetID()) {

        case JavaParserTreeConstants.JJTLITERAL:
            return visit(first, data);

        case JavaParserTreeConstants.JJTEXPRESSION:
            return visit(first, data);

        case JavaParserTreeConstants.JJTNAME:
        {
            String identifier = (String) visit(first, data);
            if (identifier.indexOf('.') == -1) {
                return new Identifier(true, frame.thisObject(), frame.location().declaringType(), identifier);
            }

            // check for variable dereference:  var.toString
            int idx = identifier.indexOf('.');
            String name = identifier.substring(0, idx);

            ObjectReference member = null;
            try {
                Value variable = evaluateVariable(new Identifier(true, frame.thisObject(), frame.location().declaringType(), name));
                Assert.assertAssignable(variable, ObjectReference.class, node, "objectReferenceRequiredOnDereference", variable);
                member = (ObjectReference) variable;
            } catch (EvaluationException e) {
                // not a variable
            }

            ReferenceType type = null;
            if (member == null) {
                // type declaration first: System.out, or java.lang.System.out.println
                for (;;) {
                    try {
                        type = resolveType(name);
                        break;
                    } catch (EvaluationException e) {
                        // unknown type
                    } catch (IncompatibleThreadStateException e) {
                        Assert.error(node, "internalError");
                    }
                    idx = identifier.indexOf('.', idx + 1);
                    if (idx == -1) break;
                    name = identifier.substring(0, idx);
                }
                if (type == null) Assert.error(node, "unknownType", identifier);
            }

            // resolve dereferences until the last name component
            for (;;) {
                int idx2 = identifier.indexOf('.', idx + 1);
                int idx22 = idx2;
                if (idx2 == -1) {
                    idx2 = identifier.length();
                }
                Identifier ident;
                if (member != null) {
                    ident = new Identifier(false, member, identifier.substring(idx + 1, idx2));
                } else {
                    ident = new Identifier(false, type, identifier.substring(idx + 1, idx2));
                }
                if (idx22 == -1) return ident;
                Value variable = evaluateVariable(ident);
                Assert.assertAssignable(variable, ObjectReference.class, node, "objectReferenceRequiredOnDereference", variable);
                member = (ObjectReference) variable;
                idx = idx2;
            }
        }
        case JavaParserTreeConstants.JJTRESULTTYPE:
            Object type = first.jjtAccept(this, data);
            if (type instanceof ReferenceType) {
                return ((ReferenceType)type).classObject();
            } else {
                return type;
            }

        default:
            return first.jjtAccept(this, data);
        }
    }

    private Object visitArgumentList(SimpleNode node, Object data) {
        int n = node.jjtGetNumChildren();
        Value [] argValues = new Value[n];
        for (int i = 0; i < n ; i++) {
            Object val = node.jjtGetChild(i).jjtAccept(this, data);
            if (val != null) Assert.assertAssignable(val, Value.class, node, "badArgument", val);
            argValues[i] = (Value) val;
        }
        return argValues;
    }

    private class MethodCall {
        ReferenceType typeContext;
        ObjectReference instanceContext;
        Method  method;
        List<Value> args;

        public MethodCall(ReferenceType typeContext, ObjectReference instanceContext, Method method, List<Value> args) {
            this.typeContext = typeContext;
            this.instanceContext = instanceContext;
            this.method = method;
            this.args = args;
        }
    }

    private Object visitArguments(SimpleNode node, Object data) {
        Assert.assertAssignable(data, Identifier.class, node, "argumentsBadSyntax", data);
        Identifier ctx = (Identifier) data;

        Value [] args;
        if (node.jjtGetNumChildren() > 0) {
            args = (Value[]) node.jjtGetChild(0).jjtAccept(this, null);
        } else {
            args = new Value[0];
        }

        MethodCall method;
        try {
            method = getConcreteMethod(ctx, args);
        } catch (UnsupportedOperationException uoex) {
            return Assert.error(node, "calleeException", uoex, ctx);
        }

        if (method.instanceContext != null) {
            try {
                if (verbose) 
                    throw new UnsupportedOperationException (NbBundle.getMessage (
                        Evaluator.class, 
                        "CTL_UnsupportedOperationException"
                    )); 
                if (!evaluationContext.canInvokeMethods()) {
                    return Assert.error(node, "calleeException", new UnsupportedOperationException(), ctx);
                }
                evaluationContext.methodToBeInvoked();
                if (loggerMethod.isLoggable(Level.FINE)) {
                    loggerMethod.fine("STARTED : "+method.instanceContext+"."+method.method+" ("+method.args+") in thread "+frameThread);
                }
                return method.instanceContext.invokeMethod(frameThread, method.method, method.args,
                                                        ObjectReference.INVOKE_SINGLE_THREADED | ObjectReference.INVOKE_NONVIRTUAL);
            } catch (InvalidTypeException e) {
                Assert.error(node, "callException", e, ctx);
            } catch (ClassNotLoadedException e) {
                // TODO: load the class
                Assert.error(node, "callException", e, ctx);
            } catch (IncompatibleThreadStateException e) {
                Assert.error(node, "callException", e, ctx);
            } catch (InvocationException e) {
                Assert.error(node, "calleeException", e, ctx);
            } catch (UnsupportedOperationException e) {
                evaluationContext.setCanInvokeMethods(false);
                Assert.error(node, "calleeException", e, ctx);
            }
            finally {
                if (loggerMethod.isLoggable(Level.FINE)) {
                    loggerMethod.fine("FINISHED: "+method.instanceContext+"."+method.method+" ("+method.args+") in thread "+frameThread);
                }
                try {
                    evaluationContext.methodInvokeDone();
                } catch (IncompatibleThreadStateException itsex) {
                    InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                    ieex.initCause(itsex);
                    throw new IllegalStateException(ieex);
                }
                try {
                    frame = frameThread.frame(frameIndex);
                } catch (IncompatibleThreadStateException e) {
                    Assert.error(node, "callException", e, ctx);
                }
            }
        }

        if (method.typeContext instanceof ClassType) {
            ClassType classContext = (ClassType) method.typeContext;
            try {
                if (method.method.isConstructor()) {
                    if (verbose) 
                        throw new UnsupportedOperationException (NbBundle.getMessage (
                            Evaluator.class, 
                            "CTL_UnsupportedOperationException"
                        )); 
                    try {
                        return classContext.newInstance(frameThread, method.method, method.args, ClassType.INVOKE_SINGLE_THREADED);
                    } catch (UnsupportedOperationException uoex) {
                        return Assert.error(node, "calleeException", uoex, ctx);
                    }
                } else {
                    if (verbose) 
                        throw new UnsupportedOperationException (NbBundle.getMessage (
                            Evaluator.class, 
                            "CTL_UnsupportedOperationException"
                        )); 
                    if (!evaluationContext.canInvokeMethods()) {
                        return Assert.error(node, "calleeException", new UnsupportedOperationException(), ctx);
                    }
                    evaluationContext.methodToBeInvoked();
                    return classContext.invokeMethod(frameThread, method.method, method.args, ClassType.INVOKE_SINGLE_THREADED);
                }
            } catch (InvalidTypeException e) {
                Assert.error(node, "callException", e, ctx);
            } catch (ClassNotLoadedException e) {
                // TODO: load the class
                Assert.error(node, "callException", e, ctx);
            } catch (IncompatibleThreadStateException e) {
                Assert.error(node, "callException", e, ctx);
            } catch (InvocationException e) {
                Assert.error(node, "calleeException", e, ctx);
            } catch (IllegalArgumentException e) {
                Assert.error(node, "callException", e, ctx);
            } catch (UnsupportedOperationException e) {
                evaluationContext.setCanInvokeMethods(false);
                Assert.error(node, "calleeException", e, ctx);
            }
            finally {
                try {
                    evaluationContext.methodInvokeDone();
                } catch (IncompatibleThreadStateException itsex) {
                    InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                    ieex.initCause(itsex);
                    throw new IllegalStateException(ieex);
                }
                try {
                    frame = frameThread.frame(frameIndex);
                } catch (IncompatibleThreadStateException e) {
                    Assert.error(node, "callException", e, ctx);
                }
            }
        }

        return Assert.error(node, "noSuchMethod", ctx);
    }

    private boolean isAccessible(TypeComponent member) {
        if (member.isPublic()) return true;

        ReferenceType callerType = frame.location().declaringType();
        ReferenceType calleeType = member.declaringType();

        if (member.isPrivate()) {
            if (callerType.equals(calleeType)) return true;
            if (isNested(calleeType, callerType) || isNested(callerType, calleeType)) return true;
            return false;
        }

        String callerName = callerType.name();
        String calleeName = calleeType.name();
        int idx1 = callerName.lastIndexOf('.');
        int idx2 = calleeName.lastIndexOf('.');

        if (idx1 * idx2 < 0) return false;
        if (idx1 + idx2 == -2) return true;

        if (callerName.substring(0, idx1).equals(calleeName.substring(0, idx2))) return true;
        if (member.isProtected()) {
            return instanceOf(callerType, calleeType);
        }

        return false;
    }

    private boolean isNested(ReferenceType outter, ReferenceType inner) {
        List nestedTypes = outter.nestedTypes();
        for (Iterator i = nestedTypes.iterator(); i.hasNext();) {
            ReferenceType type = (ReferenceType) i.next();
            if (type.equals(inner) || isNested(type, inner)) return true;
        }
        return false;
    }

    private MethodCall getConcreteMethod(Identifier ctx, Value [] args) {
        ReferenceType type = ctx.typeContext;
        ObjectReference object = ctx.instanceContext;

        if (ctx.superQualifier != null) {
            if (!(ctx.typeContext instanceof ClassType)) Assert.error(currentNode, "superUsedOnNonClass", ctx);
            if (ctx.superQualifier.length() > 0) {
                object = getEnclosingObject(ctx.instanceContext, ctx.superQualifier);
                Assert.assertNotNull(object, currentNode, "notEnclosingType", ctx);
            }
            ClassType cls = (ClassType) object.referenceType();
            type = cls.superclass();
        }
        
        if (ctx.typeContext == null) {
            Assert.error(currentNode, "methodCallOnNull", ctx.identifier);
        }

        List<Method> methods = getMethodsByName(type, ctx.identifier);
        
        //Try outer classes
        ReferenceType origType = type;
        ObjectReference origObject = object;
        
        while (methods.size() == 0) {
            Field outerRef = type.fieldByName("this$0"); 
            if (outerRef == null) {
                //System.out.println("No outerRef.");
                type = origType;
                object = origObject;
                break; //No outer reference
            }
            if (loggerValue.isLoggable(Level.FINE)) {
                loggerValue.fine("STARTED : "+object+".getValue("+outerRef+")");
            }
            object = (ObjectReference) object.getValue(outerRef);
            if (loggerValue.isLoggable(Level.FINE)) {
                loggerValue.fine("FINISHED: getValue("+outerRef+") = "+object);
            }
            type = object.referenceType();
            methods = getMethodsByName(type, ctx.identifier);
                
        }
       
        //Static Imports
        if (ctx.localContext && methods.size() == 0) {
            for (Iterator i = staticImportsIterator(ctx.identifier); i.hasNext(); ) {
                String typeName = (String) i.next();
                try {
                    ReferenceType importedType = resolveType(typeName);
                    methods = getMethodsByName(importedType, ctx.identifier);
                    if (methods.size() > 0) {
                        type = importedType;
                        object = null;
                        break;
                    }
                } catch (Exception e) {
                    // no such method
                    continue;
                }
            }
        }

        List<MethodCall> possibleMethods = new ArrayList<MethodCall>();

        for (Iterator<Method> i = methods.iterator(); i.hasNext();) {
            Method method = i.next();
            if (!isAccessible(method)) continue;

            List argTypes = null;
            try {
                argTypes = method.argumentTypes();
            } catch (ClassNotLoadedException e) {
                // TODO: load class
                continue;
            } catch (ObjectCollectedException ocex) {
                continue;
            }

            // TODO: probably incomplete handling of an implicit constructor of a nested type
            if (args.length == 0 && "<init>".equals(ctx.identifier) && argTypes.size() == 1) {
                if (frame.thisObject() != null && argTypes.get(0).equals(frame.location().declaringType())) {
                    args = new Value[] { frame.thisObject() } ;
                }
            }

            List<Value> newArgs = prepareArguments(args, argTypes);
            if (newArgs == null) continue;
            possibleMethods.add(new MethodCall(type, object, method, newArgs));
        }
        Assert.assertNonEmpty(possibleMethods, currentNode, "noSuchMethod", ctx);
        MethodCall call = mostSpecific(possibleMethods, args);
        Assert.assertNotNull(call, currentNode, "ambigousMethod", ctx);
        call = findConcrete(call);
        return call;
    }
    
    private static List<Method> getMethodsByName(ReferenceType type, String name) {
        if (type instanceof ArrayType) { // There are no methods by JDI definition ?!?
            type = type.classObject().referenceType();
            if ("toString".equals(name)) { // NOI18N
                // We have to get the super class' toString() method for some strange reason...
                type = ((ClassType) type).superclass();
            }
        }
        List<Method> methods = type.methodsByName(name);
        return methods;
    }

    private MethodCall mostSpecific(List<MethodCall> possibleMethods, Value [] args) {
        if (possibleMethods.size() == 0) return null;
        if (possibleMethods.size() == 1) return possibleMethods.get(0);

        MethodCall mostSpecific = null;
        int conversions = Integer.MAX_VALUE;
        for (Iterator<MethodCall> i = possibleMethods.iterator(); i.hasNext();) {
            MethodCall methodCall = i.next();
            List methodArguments = null;
            try {
                methodArguments = methodCall.method.argumentTypes();
            } catch (ClassNotLoadedException e) {
                continue;
            } catch (ObjectCollectedException ocex) {
                continue;
            }
            int cc = conversionsCount(methodArguments, args);
            if (cc == 0) return methodCall;
            if (cc == conversions) {
                return null;
            }
            if (cc < conversions) {
                conversions = cc;
                mostSpecific = methodCall;
            }
        }
        return mostSpecific;
    }

    private int conversionsCount(List argumentTypes, Value[] args) {
        int idx = 0;
        int cc = 0;
        for (Iterator i = argumentTypes.iterator(); i.hasNext(); idx++) {
            Type argType = (Type) i.next();
            if (args[idx] == null) continue;
            if (representSameType(argType, args[idx].type())) continue;
            cc ++;
        }
        return cc;
    }

    private boolean representSameType(Type t1, Type t2) {
        String t1s = t1.signature();
        String t2s = t2.signature();

        if (t1s.equals(t2s)) return true;
        if (t1s.length() == 1 && t2s.length() == 1 ||
                t1s.length() > 1 && t2s.length() > 1) return false;

        String primitiveType = t1s.length() == 1 ? t1s : t2s;
        String classType = t1s.length() > 1 ? t1s : t2s;

        return wrapperSignature(primitiveType.charAt(0)).equals(classType);
    }
    
    private MethodCall findConcrete(MethodCall call) {
        if (call.method.isAbstract()) {
            ReferenceType type = call.instanceContext.referenceType();
            if (type instanceof ClassType) {
                Method m = ((ClassType) type).concreteMethodByName(call.method.name(), call.method.signature());
                if (m != null) {
                    call.method = m;
                    call.typeContext = type;
                }
            }
        }
        return call;
    }

    private List<Value> prepareArguments(Value [] args, List argTypes) {

        boolean ellipsis;
        try {
                ellipsis = argTypes.size() > 0 &&
                           argTypes.get(argTypes.size() - 1) instanceof ArrayType &&
                           (args.length == 0 ||
                            args[args.length - 1] == null ||
                            isConvertible(((ArrayType) argTypes.get(argTypes.size() - 1)).componentType(),
                                          args[args.length - 1]));
        } catch (ClassNotLoadedException e) {
            // TODO: load the offending class?
            return null;
        }
        if (ellipsis) {
            if (args.length < argTypes.size() - 1) return null;
        } else {
            if (args.length != argTypes.size()) return null;
        }

        List<Value> newArgs = new ArrayList<Value>();
        int idx = 0;
        for (Iterator i = argTypes.iterator(); i.hasNext(); idx++) {
            Type type = (Type) i.next();
            if (ellipsis && !i.hasNext()) continue;
            if (!isConvertible(type, args[idx])) return null;
            newArgs.add(boxUnboxIfNeeded(args[idx], type));
        }

        if (ellipsis) {
            // JDI will handle vararg calls, we just need to check argument types
            ArrayType elipsisType = (ArrayType) argTypes.get(argTypes.size() - 1);
            if (args.length == argTypes.size() - 1) {
                // nothing to check
            } else {
                Type componentType = null;
                try {
                    componentType = elipsisType.componentType();
                } catch (ClassNotLoadedException e) {
                    // TODO: load the offending class
                    return null;
                }
                if (args.length == argTypes.size()) {
                    if (args[args.length -1] != null && !elipsisType.equals(args[args.length -1].type())) {
                        if (!isConvertible(componentType, args[args.length -1])) return null;
                    }
                    newArgs.add(boxUnboxIfNeeded(args[args.length -1], componentType));
                } else if (args.length > argTypes.size()) {
                    for (int i = argTypes.size() - 1; i < args.length; i++) {
                        if (!isConvertible(componentType, args[i])) return null;
                        newArgs.add(boxUnboxIfNeeded(args[i], componentType));
                    }
                }
            }
        }

        return newArgs;
    }

    private Value boxUnboxIfNeeded(Value value, Type type) {
        if (value instanceof ObjectReference && type instanceof PrimitiveType) {
            return unbox((ObjectReference) value, type);
        } else if (value instanceof PrimitiveValue && type instanceof ClassType) {
            return box((PrimitiveValue) value, (ClassType) type);
        } else {
            return value;
        }
    }

    /**
     * Wraps the passed primitive value to an object of the given type. The class type of the object must be
     * wider than the primitive value type. For example, it is not possible to wrap a long value to a Short object.
     *
     * @param primitiveValue
     * @param type
     * @return
     */
    private ObjectReference box(PrimitiveValue primitiveValue, ClassType type) {
        try {
            if (type instanceof Object) {
                type = wrapperType((PrimitiveType) primitiveValue.type());
            }
            return newInstance(type, new Value[] { primitiveValue });
        } catch (Exception e) {
            // this should never happen, indicates an internal error
            throw new RuntimeException("Unexpected exception while invoking boxing method", e);
        }
    }

    private ClassType wrapperType(PrimitiveType type) throws IncompatibleThreadStateException {
        char sig = type.signature().charAt(0);
        return (ClassType) resolveType(wrapperClassname(sig));
    }

    private String wrapperSignature(char primitiveSignature) {
        switch (primitiveSignature) {
        case 'Z':
            return "Ljava/lang/Boolean;";
        case 'B':
            return "Ljava/lang/Byte;";
        case 'C':
            return "Ljava/lang/Character;";
        case 'S':
            return "Ljava/lang/Short;";
        case 'I':
            return "Ljava/lang/Integer;";
        case 'J':
            return "Ljava/lang/Long;";
        case 'F':
            return "Ljava/lang/Float;";
        case 'D':
            return "Ljava/lang/Double;";
        }
        throw new RuntimeException(); // never happens
    }

    private String wrapperClassname(char primitiveSignature) {
        switch (primitiveSignature) {
        case 'Z':
            return "java.lang.Boolean";
        case 'B':
            return "java.lang.Byte";
        case 'C':
            return "java.lang.Character";
        case 'S':
            return "java.lang.Short";
        case 'I':
            return "java.lang.Integer";
        case 'J':
            return "java.lang.Long";
        case 'F':
            return "java.lang.Float";
        case 'D':
            return "java.lang.Double";
        }
        throw new RuntimeException(); // never happens
    }

    private ObjectReference newInstance(ClassType type, Value [] constructorArgs) throws
            InvocationException, ClassNotLoadedException, IncompatibleThreadStateException, InvalidTypeException {

        MethodCall method = getConcreteMethod(new Identifier(type, "<init>"), constructorArgs);
        try {
            return type.newInstance(frameThread, method.method, method.args, ObjectReference.INVOKE_SINGLE_THREADED);
        } finally {
            frame = frameThread.frame(frameIndex);
        }
    }

    private PrimitiveValue unbox(ObjectReference val, Type type) {

        if (type instanceof BooleanType) return invokeUnboxingMethod(val, "booleanValue");
        if (type instanceof ByteType) return invokeUnboxingMethod(val, "byteValue");
        if (type instanceof CharType) return invokeUnboxingMethod(val, "charValue");
        if (type instanceof ShortType) return invokeUnboxingMethod(val, "shortValue");
        if (type instanceof IntegerType) return invokeUnboxingMethod(val, "intValue");
        if (type instanceof LongType) return invokeUnboxingMethod(val, "longValue");
        if (type instanceof FloatType) return invokeUnboxingMethod(val, "floatValue");
        if (type instanceof DoubleType) return invokeUnboxingMethod(val, "doubleValue");
        throw new RuntimeException("Invalid type while unboxing: " + type.signature());    // never happens
    }

    private PrimitiveValue invokeUnboxingMethod(ObjectReference reference, String methodName) {
        Method toCall = (Method) reference.referenceType().methodsByName(methodName).get(0);
        try {
            if (verbose) 
                throw new UnsupportedOperationException (NbBundle.getMessage (
                    Evaluator.class, 
                    "CTL_UnsupportedOperationException"
                )); 
            if (!evaluationContext.canInvokeMethods()) {
                throw new UnsupportedOperationException();
            }
            evaluationContext.methodToBeInvoked();
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("STARTED : "+reference+"."+toCall+" () in thread "+frameThread);
            }
            return (PrimitiveValue) reference.invokeMethod(frameThread, toCall, new ArrayList<Value>(0), ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (UnsupportedOperationException uoex) {
            evaluationContext.setCanInvokeMethods(false);
            // this can happen on VMs that can not invoke methods...
            throw new RuntimeException("Unexpected exception while invoking unboxing method", uoex);
        } catch (Exception e) {
            // this should never happen, indicates an internal error
            throw new RuntimeException("Unexpected exception while invoking unboxing method", e);
        } finally {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("FINISHED: "+reference+"."+toCall+" () in thread "+frameThread);
            }
            try {
                evaluationContext.methodInvokeDone();
            } catch (IncompatibleThreadStateException itsex) {
                InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                ieex.initCause(itsex);
                throw new IllegalStateException(ieex);
            }
            try {
                frame = frameThread.frame(frameIndex);
            } catch (IncompatibleThreadStateException e) {
                throw new RuntimeException("Unexpected exception while invoking unboxing method", e);
            }
        }
    }

    private static final String [] typeSignaturesSorted = {
            "Ljava/lang/Byte;", "B",
            "Ljava/lang/Character;", "C",
            "Ljava/lang/Short;", "S",
            "Ljava/lang/Integer;", "I",
            "Ljava/lang/Long;", "J",
            "Ljava/lang/Float;", "F",
            "Ljava/lang/Double;", "D"
    };

    /**
     * int      short
     * double   float
     * int      Integer
     * Integer  int
     * Runnable Thread
     * Object   Socket
     * <OREF>   null
     *
     * @param wideType
     * @param value
     */
    private boolean isConvertible(Type wideType, Value value) {

        if (value == null) return wideType instanceof ReferenceType;

        String narrow = value.type().signature();
        String wide = wideType.signature();
        if (wide.equals(narrow)) return true;

        if (wide.length() == 1) {
            if (wide.equals("Z")) return narrow.equals("Ljava/lang/Boolean;");
            for (int i = 0; i < typeSignaturesSorted.length; i++) {
                if (narrow.equals(typeSignaturesSorted[i])) return true;
                if (wide.equals(typeSignaturesSorted[i])) return false;
            }
            return false;
        }

        if (wide.equals("Ljava/lang/Object;")) return true;

        if (narrow.length() == 1) {
            if (narrow.equals("Z")) return wide.equals("Ljava/lang/Boolean;");
            for (int i = 0; i < typeSignaturesSorted.length; i++) {
                if (narrow.equals(typeSignaturesSorted[i])) {
                    for (int j = i - 1; j < typeSignaturesSorted.length; j++) {
                        if (wide.equals(typeSignaturesSorted[j])) return true;
                    }
                    return false;
                }
            }
        }

        return instanceOf(value.type(), wideType);
    }

    private Object visitPrimarySuffix(SimpleNode node, Object data) {
        Token token = (Token) node.getAttribute("token");
        if (token == null) {
            // AllocationExpression() | Arguments() | ReferenceTypeList()
            return node.jjtGetChild(0).jjtAccept(this, data);
        }
        switch (token.kind) {
        case JavaParserConstants.IDENTIFIER:
            data = resolveVariable(data); // data may be an Identifier, object.field.anotherfield
            Assert.assertAssignable(data, ObjectReference.class, node, "identifierNotAReference", data);
            return new Identifier(false, (ObjectReference)data, token.image);

        case JavaParserConstants.LBRACKET:
        {
            data = resolveVariable(data);
            Assert.assertAssignable(data, ArrayReference.class, node, "notarray", data, token);
            Object index = node.jjtGetChild(0).jjtAccept(this, data);
            Assert.assertAssignable(index, PrimitiveValue.class, node, "arrayIndexNAN", data, index);
            Assert.assertNotAssignable(index, BooleanValue.class, node, "arrayIndexNAN", data, index);
            int idx = ((PrimitiveValue) index).intValue();
            ArrayReference array = (ArrayReference) data;
            Assert.assertLess(idx, array.length(), node, "arrayIndexOutOfBounds", array, new Integer(idx));
            return array.getValue(idx);
        }

        case JavaParserConstants.THIS:
        case JavaParserConstants.SUPER:
        {
            Identifier ctx = (Identifier) data;
            if (!vm.canGetSyntheticAttribute()) Assert.error(node, "unknownType", ctx.identifier);

            ObjectReference enclosingObject = getEnclosingObject(frame.thisObject(), ctx.identifier);
            Assert.assertNotNull(enclosingObject, node, "unknownType", ctx.identifier);
            return enclosingObject;
        }
        }
        return Assert.error(node, "internalError");
    }

    private ObjectReference getEnclosingObject(ObjectReference obj, String typeQualifier) {
        boolean done;
        do {
            done = true;
            List fields = obj.referenceType().allFields();
            for (Iterator j = fields.iterator(); j.hasNext();) {
                Field field = (Field) j.next();
                if (field.isSynthetic() && field.name().startsWith("this$")) {
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("STARTED : "+obj+".getValue("+field+")");
                    }
                    obj = (ObjectReference) obj.getValue(field);
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("FINISHED: getValue("+field+") = "+obj);
                    }
                    ClassType type = (ClassType) obj.referenceType();
                    if (type.name().endsWith(typeQualifier)) {
                        return obj;
                    }
                    done = false;
                    break;
                }
            }
        } while (!done);
        return null;
    }

    private Value evaluateVariable(Identifier ctx) {
        
        // local variable
        if (ctx.localContext) {
            try {
                LocalVariable var = frame.visibleVariableByName(ctx.identifier);
                if (var != null) return frame.getValue(var);
            } catch (AbsentInformationException e) {
                // Try to get arguments
                try {
                    org.netbeans.api.debugger.jpda.LocalVariable[] lvs;
                    lvs = new CallStackFrameImpl((JPDAThreadImpl) ((JPDADebuggerImpl) evaluationContext.getDebugger()).getThread(frame.thread()),
                                                 frame, 0, evaluationContext.getDebugger()).getMethodArguments();
                    if (lvs != null) {
                        for (org.netbeans.api.debugger.jpda.LocalVariable lv : lvs) {
                            if (ctx.identifier.equals(lv.getName())) {
                                return ((JDIVariable) lv).getJDIValue();
                            }
                        }
                    }
                } catch (NativeMethodException nmex) {
                    // ignore - no arguments available
                } catch (InvalidStackFrameException ex) {
                }
            }
        }
          
        // field
        if (ctx.instanceContext != null) {
            Field field = ctx.typeContext.fieldByName(ctx.identifier);
            if (field != null) {
                try {
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("STARTED : "+ctx.instanceContext+".getValue("+field+")");
                    }
                    return ctx.instanceContext.getValue(field);
                } finally {
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("FINISHED : "+ctx.instanceContext+".getValue("+field+")");
                    }
                }
            }
            if (ctx.instanceContext instanceof ArrayReference) {
                if (ctx.identifier.equals("length")) {
                    return vm.mirrorOf(((ArrayReference) ctx.instanceContext).length());
                }
            }
        }
        
        // field from static context
        if (ctx.typeContext != null) {
            Field field = ctx.typeContext.fieldByName(ctx.identifier);
            try {
                if (field != null) {
                    try {
                        if (loggerValue.isLoggable(Level.FINE)) {
                            loggerValue.fine("STARTED : "+ctx.typeContext+".getValue("+field+")");
                        }
                        return ctx.typeContext.getValue(field);
                    } finally {
                        if (loggerValue.isLoggable(Level.FINE)) {
                            loggerValue.fine("FINISHED : "+ctx.typeContext+".getValue("+field+")");
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                Assert.error(currentNode, "accessInstanceVariableFromStaticContext", ctx);
            }
        }
        
        // local variable accessed from innerclass
        if (ctx.instanceContext != null) {
            Field field = ctx.typeContext.fieldByName("val$" + ctx.identifier);
            if (field != null) {
                try {
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("STARTED : "+ctx.instanceContext+".getValue("+field+")");
                    }
                    return ctx.instanceContext.getValue(field);
                } finally {
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("FINISHED: "+ctx.instanceContext+".getValue("+field+")");
                    }
                }
            }
        }
        
        // outer field accessed from innerclass
        if (ctx.instanceContext != null) {
            Field helpField = ctx.typeContext.fieldByName("this$0");
            if (helpField != null) {
                if (loggerValue.isLoggable(Level.FINE)) {
                    loggerValue.fine("STARTED : "+ctx.instanceContext+".getValue("+helpField+")");
                }
                ObjectReference or = (ObjectReference)ctx.instanceContext.getValue(helpField);
                if (loggerValue.isLoggable(Level.FINE)) {
                    loggerValue.fine("FINISHED: "+ctx.instanceContext+".getValue("+helpField+") = "+or);
                }
                if (or != null) {
                    Field field = or.referenceType().fieldByName(ctx.identifier);
                    if (field != null) {
                        try {
                            if (loggerValue.isLoggable(Level.FINE)) {
                                loggerValue.fine("STARTED : "+or+".getValue("+field+")");
                            }
                            return or.getValue(field);
                        } finally {
                            if (loggerValue.isLoggable(Level.FINE)) {
                                loggerValue.fine("FINISHED: "+or+".getValue("+field+")");
                            }
                        }
                    }
                }
            }
        }
        
        // static import
        for (Iterator i = staticImportsIterator(ctx.identifier); i.hasNext(); ) {
            String typeName = (String) i.next();
            try {
                ReferenceType type = resolveType(typeName);
                Field field = type.fieldByName(ctx.identifier);
                if (field != null) {
                    try {
                        if (loggerValue.isLoggable(Level.FINE)) {
                            loggerValue.fine("STARTED : "+type+".getValue("+field+")");
                        }
                        return type.getValue(field);
                    } finally {
                        if (loggerValue.isLoggable(Level.FINE)) {
                            loggerValue.fine("FINISHED: "+type+".getValue("+field+")");
                        }
                    }
                }
            } catch (Exception e) {
                // no such type or field
            }
        }
        
        // class special variable
        if (expression.classReplaced().equals(ctx.identifier)) {
            ReferenceType refType = frame.location().declaringType();
            JPDAClassType classType = evaluationContext.getDebugger().getClassType(refType);
            return ((JDIVariable) classType.classObject()).getJDIValue();
        }
        
        // return special variable
        if (expression.returnReplaced().equals(ctx.identifier)) {
            ThreadReference tr = frame.thread();
            JPDAThreadImpl thread = (JPDAThreadImpl) evaluationContext.getDebugger().getThread(tr);
            JDIVariable returnVar = (JDIVariable) thread.getReturnVariable();
            if (returnVar != null) {
                return returnVar.getJDIValue();
            } else {
                return null;
            }
        }

        return (Value) Assert.error(currentNode, "unknownVariable", ctx);
    }

    private Iterator<String> staticImportsIterator(String identifier) {
        return iterator(evaluationContext.getStaticImports(), identifier);
    }

    private Iterator<String> iterator(List<String> imports, String identifier) {
        List<String> filteredList = new ArrayList<String>();
        for (Iterator<String> i = imports.iterator(); i.hasNext();) {
            String statement = i.next();
            int idx = statement.lastIndexOf('.');
            String qualifier = statement.substring(idx + 1);
            if (qualifier.equals("*") || qualifier.equals(identifier)) {
                filteredList.add(statement.substring(0, idx));
            }
        }
        return filteredList.iterator();
    }

    private Value resolveVariable(Object data) {
        if (data == null || data instanceof Value) return (Value) data;

        Identifier name = (Identifier) data;
        return evaluateVariable(name);
    }

    private Object visitPrimaryExpression(SimpleNode node, Object data) {

        int n = node.jjtGetNumChildren();

        Object value = node.jjtGetChild(0).jjtAccept(this, data);
        for (int i = 1; i < n; i++) {
            value = node.jjtGetChild(i).jjtAccept(this, value);
        }

        if (value instanceof Identifier) {
            Identifier ctx = (Identifier) value;
            return evaluateVariable(ctx);
        }

        return value;
    }

    private Object visitExpression(SimpleNode node, Object data) {
        int n = node.jjtGetNumChildren();
        if (n == 1) {
            // conditionalExpression
            return node.jjtGetChild(0).jjtAccept(this, data);
        } else {
            // assignmentoperator conditionalExpression
            return node.jjtGetChild(2).jjtAccept(this, data);
        }
    }

    private Object visitLiteral(SimpleNode node, Object data) {
        Token token = (Token) node.getAttribute("token");
        if (token == null) return node.jjtGetChild(0).jjtAccept(this, data);


        try {
            switch (token.kind) {

            case JavaParser.INTEGER_LITERAL:
                // XXX might be simpler to use Long.decode()
                String  name    = token.image.toLowerCase();
                boolean isLong  = name.endsWith("l");
                long    value;

                if (isLong) {
                    name = name.substring(0, name.length() -1);
                }

                if (name.startsWith("0x")) {
                    value = Long.parseLong(name.substring(2), 16);
                }
                else if (name.length() > 1 && name.charAt(0) == '0') {
                    value = Long.parseLong(name.substring(1), 8);
                }
                else {
                    value = Long.parseLong(name);
                }

                if (isLong) {
                    return vm.mirrorOf(value);
                }
                else {
                    if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                        Assert.error(node, "integerLiteralTooBig", name);
                    }
                    else {
                        return vm.mirrorOf((int) value);
                    }
                }

            case JavaParser.FLOATING_POINT_LITERAL:
                char spec = token.image.charAt(token.image.length() - 1);
                if (spec == 'f' || spec == 'F') {
                    return vm.mirrorOf(Float.parseFloat(token.image));
                } else {
                    return vm.mirrorOf(Double.parseDouble(token.image));
                }

            case JavaParser.STRING_LITERAL:
                return vm.mirrorOf(resolveString(token.image.substring(1, token.image.length() - 1)));

            case JavaParser.CHARACTER_LITERAL:
                return vm.mirrorOf(resolveString(token.image.substring(1, token.image.length() - 1)).charAt(0));

            default:
                return Assert.error(node, "unknownLiteralType", token.image);
            }
        } catch (NumberFormatException e) {
            return Assert.error(node, "badFormatOfIntegerLiteral", token.image);
        }
    }

    private String resolveString(String input) {
        String result = "";
        int index = 0;
        while (index < input.length()) {
            if (input.charAt(index) != '\\')
                result = result + input.charAt (index);
            else { // resolve an escape sequence
                index++;
                char c;
                switch (input.charAt(index)) {
                    case 'b': c = '\b'; break;
                    case 't': c = '\t'; break;
                    case 'n': c = '\n'; break;
                    case 'f': c = '\f'; break;
                    case 'r': c = '\r'; break;
                    case '\"': c = '\"'; break;
                    case '\'': c = '\''; break;
                    case '\\': c = '\\'; break;

                    default:
                        // resolve octal value
                        c = 0;
                        while ((index < input.length ()) &&
                            (input.charAt (index) >= '0') && (input.charAt (index) <= '7') &&
                            (c*8 + input.charAt (index) - '0' < 256)) {
                                c = (char) (c*8 + (input.charAt (index) - '0'));
                                index++;
                        } // while
                        index--;
                } // switch
                result = result + c;
            } // else
            index++;
        } // while
        return result;
    }

    private Object visitBinaryExpression(SimpleNode node, Object data) {
        Object [] operators = node.getAttributes("operator");
        int n = node.jjtGetNumChildren();

        Value value = (Value) node.jjtGetChild(0).jjtAccept(this, data);
        for (int i = 1; i < n; i++) {
            Value next = (Value) node.jjtGetChild(i).jjtAccept(this, data);
            try {
                value = this.operators.evaluate(value, (Token) operators[i-1], next);
            } catch (IllegalArgumentException e) {
                return Assert.error(node, "evaluateError", value, ((Token) operators[i-1]).image, next);
            }
        }
        return value;
    }

    public static Value invokeVirtual (
        ObjectReference objectReference, 
        Method method, 
        ThreadReference evaluationThread, 
        List<Value> args
     ) throws InvalidExpressionException {
        
        if (verbose)
            throw new UnsupportedOperationException (NbBundle.getMessage (
                Evaluator.class,
                "CTL_UnsupportedOperationException"
            ));
        try {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("STARTED : "+objectReference+"."+method+" ("+args+") in thread "+evaluationThread);
            }
            Value value =
                    objectReference.invokeMethod(evaluationThread, method,
                                                 args,
                                                 ObjectReference.INVOKE_SINGLE_THREADED);
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("   return = "+value);
            }
            return value;
        } catch (InvalidTypeException itex) {
            throw new InvalidExpressionException (itex);
        } catch (ClassNotLoadedException cnlex) {
            throw new InvalidExpressionException (cnlex);
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            ieex.initCause(itsex);
            throw ieex;
        } catch (InvocationException iex) {
            InvalidExpressionException ieex = new InvalidExpressionException (iex);
            ieex.initCause(iex);
            throw ieex;
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            ieex.initCause(uoex);
            throw ieex;
        } catch (ObjectCollectedException ocex) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                Evaluator.class, "CTL_EvalError_collected"));
        } finally {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("FINISHED: "+objectReference+"."+method+" ("+args+") in thread "+evaluationThread);
            }
        }
    }
}
