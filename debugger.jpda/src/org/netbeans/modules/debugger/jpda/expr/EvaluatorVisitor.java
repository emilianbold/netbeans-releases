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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2007 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatType;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongType;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import javax.lang.model.type.TypeVariable;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext.VariableInfo;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.openide.util.NbBundle;

/**
 * Mirror is either Value or ReferenceType
 * 
 * @author Martin Entlicher
 */
 class EvaluatorVisitor extends TreePathScanner<Mirror, EvaluationContext> {

    private static final Logger loggerMethod = Logger.getLogger("org.netbeans.modules.debugger.jpda.invokeMethod"); // NOI18N
    private static final Logger loggerValue = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI8N
    
    private Type newArrayType;
    
    public EvaluatorVisitor() {
    }

    @Override
    public Mirror visitAnnotation(AnnotationTree arg0, EvaluationContext evaluationContext) {
        return null;
    }

    @Override
    public Mirror visitMethodInvocation(MethodInvocationTree arg0, EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert2.error(arg0, "calleeException", new UnsupportedOperationException(), evaluationContext);
        }
        if (loggerMethod.isLoggable(Level.FINE)) {
            loggerMethod.fine("STARTED : "+arg0+" in thread "+evaluationContext.getFrame().thread());
        }
        Mirror object = null;
        String methodName;
        boolean isStatic;
        ExpressionTree expression = arg0.getMethodSelect();
        Element elm;
        if (expression.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree mst = (MemberSelectTree) expression;
            object = mst.getExpression().accept(this, evaluationContext);
            methodName = mst.getIdentifier().toString();
            TreePath memberSelectPath = TreePath.getPath(getCurrentPath(), mst);
            if (memberSelectPath == null) memberSelectPath = getCurrentPath();
            elm = evaluationContext.getTrees().getElement(memberSelectPath);
        } else {
            TreePath methodInvokePath = TreePath.getPath(getCurrentPath(), arg0);
            if (methodInvokePath == null) methodInvokePath = getCurrentPath();
            elm = evaluationContext.getTrees().getElement(methodInvokePath);
            methodName = elm.getSimpleName().toString();
        }
        TypeMirror typeMirror = elm.asType();
        TypeKind kind = typeMirror.getKind();
        if (kind != TypeKind.EXECUTABLE) {
            Assert2.error(arg0, "noSuchMethod", elm.getSimpleName().toString(), elm.getEnclosingElement().getSimpleName().toString());
        }
        ExecutableElement methodElement = (ExecutableElement) elm;
        ExecutableType execTypeMirror = (ExecutableType) typeMirror;
        isStatic = methodElement.getModifiers().contains(Modifier.STATIC);
        
        List<? extends ExpressionTree> args = arg0.getArguments();
        List<Value> argVals = new ArrayList<Value>(args.size());
        for (ExpressionTree arg : args) {
            Mirror argValue = arg.accept(this, evaluationContext);
            if (!(argValue instanceof Value)) {
                Assert2.error(arg, "Not a value");
            }
            argVals.add((Value) argValue);
        }
        ObjectReference objectReference;
        ClassType type;
        if (isStatic) {
            objectReference = null;
            if (object instanceof ClassType) {
                type = (ClassType) object;
            } else if (object instanceof ObjectReference) {
                type = (ClassType) ((ObjectReference) object).type();
            } else {
                type = (ClassType) evaluationContext.getFrame().location().declaringType();
            }
        } else {
            if (object != null) {
                objectReference = (ObjectReference) object;
            } else {
                objectReference = evaluationContext.getFrame().thisObject();
            }
            type = (ClassType) objectReference.referenceType();
        }
        Method method = getConcreteMethod(type, methodName, execTypeMirror.getParameterTypes());
        if (method == null) {
            Assert2.error(arg0, "noSuchMethod", methodName, type.name());
        }
        return invokeMethod(arg0, method, isStatic, type, objectReference, argVals, evaluationContext);
    }
    
    /*private Method getConcreteMethod(ReferenceType type, String methodName, List<? extends ExpressionTree> typeArguments) {
        List<Method> methods = type.methodsByName(methodName);
        String signature = createSignature(typeArguments);
        for (Method method : methods) {
            if (egualMethodSignatures(method.signature(), signature)) {
                return method;
            }
        }
        return null;
    }*/

    private static Method getConcreteMethod(ReferenceType type, String methodName, List<? extends TypeMirror> typeArguments) {
        List<Method> methods = type.methodsByName(methodName);
        String signature = createSignature(typeArguments);
        for (Method method : methods) {
            if (!method.isAbstract() && egualMethodSignatures(method.signature(), signature)) {
                return method;
            }
        }
        return null;
    }

    private static boolean egualMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) s1 = s1.substring(0, i);
        i = s2.lastIndexOf(")");
        if (i > 0) s2 = s2.substring(0, i);
        return s1.equals(s2);
    }
    
    private static String createSignature(List<? extends TypeMirror> typeArguments) {
        StringBuilder signature = new StringBuilder("(");
        for (TypeMirror param : typeArguments) {
            String paramType = getTypeName(param);//getSimpleName().toString();
            signature.append(getSignature(paramType));
        }
        signature.append(')');
        //String returnType = elm.getReturnType().toString();
        //signature.append(getSignature(returnType));
        return signature.toString();
    }
    
    private static String getTypeName(TypeMirror type) {
        if (type.getKind() == TypeKind.ARRAY) {
            return getTypeName(((javax.lang.model.type.ArrayType) type).getComponentType())+"[]";
        }
        if (type.getKind() == TypeKind.TYPEVAR) {
            TypeVariable tv = (TypeVariable) type;
            return getTypeName(tv.getUpperBound());
        }
        if (type.getKind() == TypeKind.DECLARED) {
            return ((DeclaredType) type).asElement().toString();
        }
        return type.toString();
    }
    
    private static String getSignature(String javaType) {
        if (javaType.equals("boolean")) {
            return "Z";
        } else if (javaType.equals("byte")) {
            return "B";
        } else if (javaType.equals("char")) {
            return "C";
        } else if (javaType.equals("short")) {
            return "S";
        } else if (javaType.equals("int")) {
            return "I";
        } else if (javaType.equals("long")) {
            return "J";
        } else if (javaType.equals("float")) {
            return "F";
        } else if (javaType.equals("double")) {
            return "D";
        } else if (javaType.endsWith("[]")) {
            return "["+getSignature(javaType.substring(0, javaType.length() - 2));
        } else {
            return "L"+javaType.replace('.', '/')+";";
        }
    }
    
    private static ReferenceType getClassType(Tree tree, TypeMirror type, EvaluationContext evaluationContext) {
        String className = ElementUtilities.getBinaryName((TypeElement) ((DeclaredType) type).asElement());
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        List<ReferenceType> classes = vm.classesByName(className);
        if (classes.size() == 0) {
            Assert2.error(tree, "unknownType", className);
        }
        return classes.get(0);
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

    
    @Override
    public Mirror visitAssert(AssertTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitAssignment(AssignmentTree arg0, EvaluationContext evaluationContext) {
        Mirror var = arg0.getVariable().accept(this, evaluationContext);
        Mirror exp = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        Value value = (Value) exp;
        setToMirror(arg0.getVariable(), value, evaluationContext);
        return value;
    }

    @Override
    public Mirror visitCompoundAssignment(CompoundAssignmentTree arg0, EvaluationContext evaluationContext) {
        Mirror var = arg0.getVariable().accept(this, evaluationContext);
        Mirror exp = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        Tree.Kind kind = arg0.getKind();
        if (var instanceof BooleanValue) {
            boolean v = ((BooleanValue) var).value();
            boolean e = ((BooleanValue) exp).value();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: throw new IllegalStateException("Unknown assignment: "+kind+" of "+arg0);
            }
            Value value = vm.mirrorOf(v);
            setToMirror(arg0.getVariable(), value, evaluationContext);
            return value;
        }
        if (var instanceof DoubleValue) {
            double v = ((DoubleValue) var).value();
            double e = ((PrimitiveValue) exp).doubleValue();
            switch (kind) {
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                default: throw new IllegalStateException("Unknown assignment: "+kind+" of "+arg0);
            }
            Value value = vm.mirrorOf(v);
            setToMirror(arg0.getVariable(), value, evaluationContext);
            return value;
        }
        if (var instanceof FloatValue) {
            float v = ((FloatValue) var).value();
            float e = ((PrimitiveValue) exp).floatValue();
            switch (kind) {
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                default: throw new IllegalStateException("Unknown assignment: "+kind+" of "+arg0);
            }
            Value value = vm.mirrorOf(v);
            setToMirror(arg0.getVariable(), value, evaluationContext);
            return value;
        }
        if (var instanceof LongValue) {
            long v = ((LongValue) var).value();
            long e = ((PrimitiveValue) exp).longValue();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case LEFT_SHIFT_ASSIGNMENT:
                    v <<= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                case REMAINDER_ASSIGNMENT:
                    v %= e; break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    v >>= e; break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    v >>>= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: throw new IllegalStateException("Unknown assignment: "+kind+" of "+arg0);
            }
            Value value = vm.mirrorOf(v);
            setToMirror(arg0.getVariable(), value, evaluationContext);
            return value;
        }
        if (var instanceof IntegerValue) {
            int v = ((IntegerValue) var).value();
            int e = ((PrimitiveValue) exp).intValue();
            switch (kind) {
                case AND_ASSIGNMENT:
                    v &= e; break;
                case DIVIDE_ASSIGNMENT:
                    v /= e; break;
                case LEFT_SHIFT_ASSIGNMENT:
                    v <<= e; break;
                case MINUS_ASSIGNMENT:
                    v -= e; break;
                case MULTIPLY_ASSIGNMENT:
                    v *= e; break;
                case OR_ASSIGNMENT:
                    v |= e; break;
                case PLUS_ASSIGNMENT:
                    v += e; break;
                case REMAINDER_ASSIGNMENT:
                    v %= e; break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    v >>= e; break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    v >>>= e; break;
                case XOR_ASSIGNMENT:
                    v ^= e; break;
                default: throw new IllegalStateException("Unknown assignment: "+kind+" of "+arg0);
            }
            Value value = vm.mirrorOf(v);
            setToMirror(arg0.getVariable(), value, evaluationContext);
            return value;
        }
        if (var instanceof StringReference) {
            String v = ((StringReference) var).value();
            String e = ((StringReference) exp).value();
            switch (kind) {
                case PLUS_ASSIGNMENT:
                    v += e; break;
                default: throw new IllegalStateException("Unknown assignment: "+kind+" of "+arg0);
            }
            Value value = vm.mirrorOf(v);
            setToMirror(arg0.getVariable(), value, evaluationContext);
            return value;
        }
        throw new IllegalStateException("Unknown assignment var type: "+var);
    }

    @Override
    public Mirror visitBinary(BinaryTree arg0, EvaluationContext evaluationContext) {
        Mirror left = arg0.getLeftOperand().accept(this, evaluationContext);
        Mirror right = arg0.getRightOperand().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        Tree.Kind kind = arg0.getKind();
        if ((left instanceof BooleanValue) && (right instanceof BooleanValue)) {
            boolean op1 = ((BooleanValue) left).booleanValue();
            boolean op2 = ((BooleanValue) right).booleanValue();
            boolean res;
            switch (kind) {
                case AND: res = op1 & op2; break;
                case CONDITIONAL_AND: res = op1 && op2; break;
                case CONDITIONAL_OR: res = op1 || op2; break;
                case EQUAL_TO: res = op1 == op2; break;
                case NOT_EQUAL_TO: res = op1 != op2; break;
                case OR: res = op1 | op2; break;
                case XOR: res = op1 ^ op2; break;
                default:
                    throw new IllegalArgumentException("Unhandled binary tree: "+arg0);
            }
            return vm.mirrorOf(res);
        }
        boolean isLeftNumeric = left instanceof PrimitiveValue && !(left instanceof BooleanValue);
        boolean isRightNumeric = right instanceof PrimitiveValue && !(right instanceof BooleanValue);
        if (isLeftNumeric && isRightNumeric) {
            if ((left instanceof DoubleValue) || (right instanceof DoubleValue)) {
                double l = ((PrimitiveValue) left).doubleValue();
                double r = ((PrimitiveValue) right).doubleValue();
                double v = 0.;
                boolean b = false;
                boolean isBoolean = true;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; isBoolean = false; break;
                    case MINUS:
                        v = l - r; isBoolean = false; break;
                    case MULTIPLY:
                        v = l * r; isBoolean = false; break;
                    case PLUS:
                        v = l + r; isBoolean = false; break;
                    case EQUAL_TO:
                        b = l == r; break;
                    case GREATER_THAN:
                        b = l > r; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; break;
                    case LESS_THAN:
                        b = l < r; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; break;
                    case NOT_EQUAL_TO:
                        b = l != r; break;
                    default: throw new IllegalStateException("Unhandled binary tree: "+arg0);
                }
                if (isBoolean) {
                    return vm.mirrorOf(b);
                } else {
                    return vm.mirrorOf(v);
                }
            }
            if ((left instanceof FloatValue) || (right instanceof FloatValue)) {
                float l = ((PrimitiveValue) left).floatValue();
                float r = ((PrimitiveValue) right).floatValue();
                float v = 0.f;
                boolean b = false;
                boolean isBoolean = true;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; isBoolean = false; break;
                    case MINUS:
                        v = l - r; isBoolean = false; break;
                    case MULTIPLY:
                        v = l * r; isBoolean = false; break;
                    case PLUS:
                        v = l + r; isBoolean = false; break;
                    case EQUAL_TO:
                        b = l == r; break;
                    case GREATER_THAN:
                        b = l > r; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; break;
                    case LESS_THAN:
                        b = l < r; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; break;
                    case NOT_EQUAL_TO:
                        b = l != r; break;
                    default: throw new IllegalStateException("Unhandled binary tree: "+arg0);
                }
                if (isBoolean) {
                    return vm.mirrorOf(b);
                } else {
                    return vm.mirrorOf(v);
                }
            }
            if ((left instanceof LongValue) || (right instanceof LongValue)) {
                long l = ((PrimitiveValue) left).longValue();
                long r = ((PrimitiveValue) right).longValue();
                long v = 0l;
                boolean b = false;
                boolean isBoolean = false;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; break;
                    case MINUS:
                        v = l - r; break;
                    case MULTIPLY:
                        v = l * r; break;
                    case PLUS:
                        v = l + r; break;
                    case REMAINDER:
                        v = l % r; break;
                    case LEFT_SHIFT:
                        v = l << r; break;
                    case RIGHT_SHIFT:
                        v = l >> r; break;
                    case UNSIGNED_RIGHT_SHIFT:
                        v = l >>> r; break;
                    case AND:
                        v = l & r; break;
                    case OR:
                        v = l | r; break;
                    case XOR:
                        v = l ^ r; break;
                    case EQUAL_TO:
                        b = l == r; isBoolean = true; break;
                    case GREATER_THAN:
                        b = l > r; isBoolean = true; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; isBoolean = true; break;
                    case LESS_THAN:
                        b = l < r; isBoolean = true; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; isBoolean = true; break;
                    case NOT_EQUAL_TO:
                        b = l != r; isBoolean = true; break;
                    default: throw new IllegalStateException("Unhandled binary tree: "+arg0);
                }
                if (isBoolean) {
                    return vm.mirrorOf(b);
                } else {
                    return vm.mirrorOf(v);
                }
            }
            //if ((left instanceof IntegerValue) || (right instanceof IntegerValue)) {
            // int, short, char and byte - operations have int result
                int l = ((PrimitiveValue) left).intValue();
                int r = ((PrimitiveValue) right).intValue();
                int v = 0;
                boolean b = false;
                boolean isBoolean = false;
                switch (kind) {
                    case DIVIDE:
                        v = l / r; break;
                    case MINUS:
                        v = l - r; break;
                    case MULTIPLY:
                        v = l * r; break;
                    case PLUS:
                        v = l + r; break;
                    case REMAINDER:
                        v = l % r; break;
                    case LEFT_SHIFT:
                        v = l << r; break;
                    case RIGHT_SHIFT:
                        v = l >> r; break;
                    case UNSIGNED_RIGHT_SHIFT:
                        v = l >>> r; break;
                    case AND:
                        v = l & r; break;
                    case OR:
                        v = l | r; break;
                    case XOR:
                        v = l ^ r; break;
                    case EQUAL_TO:
                        b = l == r; isBoolean = true; break;
                    case GREATER_THAN:
                        b = l > r; isBoolean = true; break;
                    case GREATER_THAN_EQUAL:
                        b = l >= r; isBoolean = true; break;
                    case LESS_THAN:
                        b = l < r; isBoolean = true; break;
                    case LESS_THAN_EQUAL:
                        b = l <= r; isBoolean = true; break;
                    case NOT_EQUAL_TO:
                        b = l != r; isBoolean = true; break;
                    default: throw new IllegalStateException("Unhandled binary tree: "+arg0);
                }
                if (isBoolean) {
                    return vm.mirrorOf(b);
                } else {
                    return vm.mirrorOf(v);
                }
            //}
        }
        if (((left == null || left instanceof StringReference) && (right == null || right instanceof StringReference))
            && kind == Tree.Kind.PLUS) {
            String s1 = (left == null) ? null : ((StringReference) left).value();
            String s2 = (left == null) ? null : ((StringReference) right).value();
            switch (kind) {
                case PLUS:
                    return vm.mirrorOf(s1 + s2);
                default: throw new IllegalStateException("Unhandled binary tree: "+arg0);
            }
        }
        switch (kind) {
            case EQUAL_TO:
                return vm.mirrorOf(left == right || (left != null && left.equals(right)));
            case NOT_EQUAL_TO:
                return vm.mirrorOf(left == null && right != null || (left != null && !left.equals(right)));
            default: throw new IllegalStateException("Unhandled binary tree: "+arg0);
        }
    }

    @Override
    public Mirror visitBlock(BlockTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitBreak(BreakTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitCase(CaseTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitCatch(CatchTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitClass(ClassTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitConditionalExpression(ConditionalExpressionTree arg0, EvaluationContext evaluationContext) {
        Mirror condition = arg0.getCondition().accept(this, evaluationContext);
        if (!(condition instanceof BooleanValue)) {
            throw new IllegalStateException("Condition must be boolean: "+arg0.getCondition());
        }
        boolean isTrue = ((BooleanValue) condition).value();
        if (isTrue) {
            return arg0.getTrueExpression().accept(this, evaluationContext);
        } else {
            return arg0.getFalseExpression().accept(this, evaluationContext);
        }
    }

    @Override
    public Mirror visitContinue(ContinueTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitDoWhileLoop(DoWhileLoopTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitErroneous(ErroneousTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "errorneous");
        return null;
    }

    @Override
    public Mirror visitExpressionStatement(ExpressionStatementTree arg0, EvaluationContext evaluationContext) {
        return arg0.getExpression().accept(this, evaluationContext);
    }

    @Override
    public Mirror visitEnhancedForLoop(EnhancedForLoopTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitForLoop(ForLoopTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitIdentifier(IdentifierTree arg0, EvaluationContext evaluationContext) {
        TreePath identifierPath = TreePath.getPath(getCurrentPath(), arg0);
        if (identifierPath == null) identifierPath = getCurrentPath();
        //TreePath identifierPath = getCurrentPath();
        Element elm = evaluationContext.getTrees().getElement(identifierPath);
        switch(elm.getKind()) {
            case CLASS:
            case ENUM:
            case INTERFACE:
                TypeElement te = (TypeElement) elm;
                String className = ElementUtilities.getBinaryName(te);
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                List<ReferenceType> classes = vm.classesByName(className);
                if (classes.size() > 0) {
                    return classes.get(0);
                }
                Assert2.error(arg0, "unknownType", className);
            case ENUM_CONSTANT:
                VariableElement ve = (VariableElement) elm;
                String constantName = ve.getSimpleName().toString();
                ve.asType().toString();
                break;
            case FIELD:
                ve = (VariableElement) elm;
                String fieldName = ve.getSimpleName().toString();
                Field field = evaluationContext.getFrame().location().declaringType().fieldByName(fieldName);
                if (field == null) {
                    Assert2.error(arg0, "unknownVariable", fieldName);
                }
                if (field.isStatic()) {
                    evaluationContext.getVariables().put(arg0, new VariableInfo(field));
                    return field.declaringType().getValue(field);
                }
                ObjectReference thisObject = evaluationContext.getFrame().thisObject();
                if (thisObject != null) {
                    evaluationContext.getVariables().put(arg0, new VariableInfo(field, thisObject));
                    return thisObject.getValue(field);
                } else {
                    throw new IllegalStateException("No current instance available.");
                }
            case LOCAL_VARIABLE:
                ve = (VariableElement) elm;
                String varName = ve.getSimpleName().toString();
                try {
                    LocalVariable lv = evaluationContext.getFrame().visibleVariableByName(varName);
                    evaluationContext.getVariables().put(arg0, new VariableInfo(lv));
                    return evaluationContext.getFrame().getValue(lv);
                } catch (AbsentInformationException aiex) {
                    return (Value) Assert2.error(arg0, "unknownVariable", varName);
                }
            case PARAMETER:
                ve = (VariableElement) elm;
                String paramName = ve.getSimpleName().toString();
                StackFrame frame = evaluationContext.getFrame();
                try {
                    LocalVariable lv = frame.visibleVariableByName(paramName);
                    evaluationContext.getVariables().put(arg0, new VariableInfo(lv));
                    return frame.getValue(lv);
                } catch (AbsentInformationException aiex) {
                    try {
                        org.netbeans.api.debugger.jpda.LocalVariable[] lvs;
                        lvs = new CallStackFrameImpl(frame, 0, evaluationContext.getDebugger()).getMethodArguments();
                        if (lvs != null) {
                            for (org.netbeans.api.debugger.jpda.LocalVariable lv : lvs) {
                                if (paramName.equals(lv.getName())) {
                                    return ((JDIVariable) lv).getJDIValue();
                                }
                            }
                        }
                    } catch (NativeMethodException nmex) {
                        // ignore - no arguments available
                    }
                    return (Value) Assert2.error(arg0, "unknownVariable", paramName);
                }
            default:
                throw new UnsupportedOperationException("Not supported element kind:"+elm.getKind()+" Tree = '"+arg0+"'");
        }
        arg0.getName();
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    @Override
    public Mirror visitIf(IfTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitImport(ImportTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitArrayAccess(ArrayAccessTree arg0, EvaluationContext evaluationContext) {
        Mirror array = arg0.getExpression().accept(this, evaluationContext);
        Mirror index = arg0.getIndex().accept(this, evaluationContext);
        return ((ArrayReference) array).getValue(((PrimitiveValue) index).intValue());
    }

    @Override
    public Mirror visitLabeledStatement(LabeledStatementTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitLiteral(LiteralTree arg0, EvaluationContext evaluationContext) {
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        Object value = arg0.getValue();
        if (value instanceof Boolean) {
            return vm.mirrorOf(((Boolean) value).booleanValue());
        }
        if (value instanceof Byte) {
            return vm.mirrorOf(((Byte) value).byteValue());
        }
        if (value instanceof Character) {
            return vm.mirrorOf(((Character) value).charValue());
        }
        if (value instanceof Double) {
            return vm.mirrorOf(((Double) value).doubleValue());
        }
        if (value instanceof Float) {
            return vm.mirrorOf(((Float) value).floatValue());
        }
        if (value instanceof Integer) {
            return vm.mirrorOf(((Integer) value).intValue());
        }
        if (value instanceof Long) {
            return vm.mirrorOf(((Long) value).longValue());
        }
        if (value instanceof Short) {
            return vm.mirrorOf(((Short) value).shortValue());
        }
        if (value instanceof String) {
            StringReference str = vm.mirrorOf((String) value);
            ClassType strClass = (ClassType) vm.classesByName("java.lang.String").get(0);
            try {
                List<? extends Value> args = Collections.emptyList();
                return invokeMethod(arg0, strClass.methodsByName("intern").get(0),
                                    false, strClass, str, args, evaluationContext);
            } catch (Exception ex) {
                return str;
            }
        }
        if (value == null) {
            return null;
        }
        throw new UnsupportedOperationException("Unsupported value: "+value);
    }

    @Override
    public Mirror visitMethod(MethodTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitModifiers(ModifiersTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitNewArray(NewArrayTree arg0, EvaluationContext evaluationContext) {
        Type type;
        Tree typeTree = arg0.getType();
        if (typeTree == null) {
            if (newArrayType == null) {
                throw new IllegalStateException("No type info for "+arg0);
            }
            type = newArrayType;
        } else {
            type = (Type) arg0.getType().accept(this, evaluationContext);
        }
        List<? extends ExpressionTree> dimensionTrees = arg0.getDimensions();
        int numDimensions = dimensionTrees.size();
        if (numDimensions > 0) {
            int[] dimensions = new int[numDimensions];
            ArrayType[] arrayTypes = new ArrayType[numDimensions];
            String arrayClassName = type.name()+"[]";
            for (int i = 0; i < numDimensions; i++, arrayClassName += "[]") {
                dimensions[i] = ((PrimitiveValue) dimensionTrees.get(numDimensions - 1 - i).accept(this, evaluationContext)).intValue();
                List<ReferenceType> classes = type.virtualMachine().classesByName(arrayClassName);
                if (classes.size() == 0) {
                    Assert2.error(arg0, "unknownType", arrayClassName);
                }
                arrayTypes[i] = (ArrayType) classes.get(0);
            }
            return constructNewArray(arrayTypes, dimensions, numDimensions - 1);
        } else {
            List<? extends ExpressionTree> initializerTrees = arg0.getInitializers();
            return constructNewArray(arg0, type, initializerTrees, evaluationContext);
        }
    }
    
    private ArrayReference constructNewArray(ArrayType[] arrayTypes, int[] dimensions, int dimension) {
        ArrayReference array = arrayTypes[dimension].newInstance(dimensions[dimension]);
        if (dimension > 0) {
            List<ArrayReference> elements = new ArrayList<ArrayReference>(dimensions[dimension]);
            for (int i = 0; i < dimensions[dimension]; i++) {
                ArrayReference subArray = constructNewArray(arrayTypes, dimensions, dimension - 1);
                elements.add(subArray);
            }
            try {
                array.setValues(elements);
            } catch (InvalidTypeException ex) {
                throw new IllegalStateException("ArrayType "+arrayTypes[dimension]+" can not have "+elements+" elements.");
            } catch (ClassNotLoadedException ex) {
                throw new IllegalStateException(new InvalidExpressionException (ex));
            }
        }
        return array;
    }
    
    private ArrayReference constructNewArray(NewArrayTree arg0, Type type, List<? extends ExpressionTree> initializerTrees, EvaluationContext evaluationContext) {
        int n = initializerTrees.size();
        List<Value> elements = new ArrayList<Value>(n);
        for (int i = 0; i < n; i++) {
            ExpressionTree exp = initializerTrees.get(i);
            newArrayType = getSubArrayType(arg0, type);
            // might call visitNewArray()
            Value element = (Value) exp.accept(this, evaluationContext);
            elements.add(element);
        }
        int depth = 1;
        ArrayReference array = getArrayType(arg0, type, depth).newInstance(n);
        try {
            array.setValues(elements);
        } catch (InvalidTypeException ex) {
            throw new IllegalStateException("ArrayType "+getArrayType(arg0, type, depth)+" can not have "+elements+" elements.");
        } catch (ClassNotLoadedException ex) {
            throw new IllegalStateException(new InvalidExpressionException (ex));
        }
        return array;
    }
    
    private static final String BRACKETS = "[][][][][][][][][][][][][][][][][][][][]"; // NOI8N
    
    private ArrayType getArrayType(NewArrayTree arg0, Type type, int depth) {
        String arrayClassName;
        if (depth < BRACKETS.length()/2) {
            arrayClassName = type.name() + BRACKETS.substring(0, 2*depth);
        } else {
            arrayClassName = type.name() + BRACKETS;
            for (int i = BRACKETS.length()/2; i < depth; i++) {
                arrayClassName += "[]"; // NOI8N
            }
        }
        List<ReferenceType> classes = type.virtualMachine().classesByName(arrayClassName);
        if (classes.size() == 0) {
            Assert2.error(arg0, "unknownType", arrayClassName);
        }
        return (ArrayType) classes.get(0);
    }
    
    private Type getSubArrayType(Tree arg0, Type type) {
        String name = type.name();
        if (name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);
            if (!name.endsWith("[]")) {
                Type pType = getPrimitiveType(name, type.virtualMachine());
                if (pType != null) return pType;
            }
            List<ReferenceType> classes = type.virtualMachine().classesByName(name);
            if (classes.size() == 0) {
                Assert2.error(arg0, "unknownType", name);
            }
            type = classes.get(0);
        }
        return type;
    }
    
    private Type getPrimitiveType(String name, VirtualMachine vm) {
        if (name.equals(Boolean.TYPE.getName())) {
            return vm.mirrorOf(true).type();
        }
        if (name.equals((Byte.TYPE.getName()))) {
            return vm.mirrorOf((byte) 0).type();
        }
        if (name.equals((Character.TYPE.getName()))) {
            return vm.mirrorOf('a').type();
        }
        if (name.equals((Double.TYPE.getName()))) {
            return vm.mirrorOf(0.).type();
        }
        if (name.equals((Float.TYPE.getName()))) {
            return vm.mirrorOf(0f).type();
        }
        if (name.equals((Integer.TYPE.getName()))) {
            return vm.mirrorOf(0).type();
        }
        if (name.equals((Long.TYPE.getName()))) {
            return vm.mirrorOf(0l).type();
        }
        if (name.equals((Short.TYPE.getName()))) {
            return vm.mirrorOf((short) 0).type();
        }
        return null;
    }

    @Override
    public Mirror visitNewClass(NewClassTree arg0, EvaluationContext evaluationContext) {
        TreePath identifierPath = TreePath.getPath(getCurrentPath(), arg0);
        if (identifierPath == null) identifierPath = getCurrentPath();
        Element elm = evaluationContext.getTrees().getElement(identifierPath);
        if (elm == null) {
            // Unresolved class
            Assert2.error(arg0, "unknownType", arg0.getIdentifier());
        }
        if (elm.getKind() != ElementKind.CONSTRUCTOR) {
            throw new IllegalStateException("Element "+elm+" is of "+elm.getKind()+" kind. Tree = "+arg0);
        }
        ExpressionTree classIdentifier = arg0.getIdentifier();
        Mirror clazz = classIdentifier.accept(this, evaluationContext);
        ClassType classType = (ClassType) clazz;
        ExecutableElement cElem = (ExecutableElement) elm;
        TypeMirror cType = cElem.asType();
        //ReferenceType classType = getClassType(arg0, cType, evaluationContext);
        List<? extends ExpressionTree> args = arg0.getArguments();
        List<Value> argVals = new ArrayList<Value>(args.size());
        for (ExpressionTree arg : args) {
            Mirror argValue = arg.accept(this, evaluationContext);
            if (!(argValue instanceof Value)) {
                Assert2.error(arg, "Not a value");
            }
            argVals.add((Value) argValue);
        }
        try {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("STARTED : "+classType+"."+"<init>"+" ("+argVals+") in thread "+evaluationContext.getFrame().thread());
            }
            evaluationContext.methodToBeInvoked();
            Method constructorMethod = getConcreteMethod(classType, "<init>", ((ExecutableType) cType).getParameterTypes());
            if (constructorMethod == null) {
                Assert2.error(arg0, "noSuchMethod", "<init>", classType);
            }
            return classType.newInstance(evaluationContext.getFrame().thread(),
                                         constructorMethod,
                                         argVals,
                                         ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(new InvalidExpressionException (cnlex));
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            ieex.initCause(itsex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            InvalidExpressionException ieex = new InvalidExpressionException (iex);
            ieex.initCause(iex);
            throw new IllegalStateException(ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            ieex.initCause(uoex);
            throw new IllegalStateException(ieex);
        } catch (ObjectCollectedException ocex) {
            throw new IllegalStateException(new InvalidExpressionException(NbBundle.getMessage(
                Evaluator.class, "CTL_EvalError_collected")));
        } finally {
            try {
                evaluationContext.methodInvokeDone();
            } catch (IncompatibleThreadStateException itsex) {
                InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                ieex.initCause(itsex);
                throw new IllegalStateException(ieex);
            }
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("FINISHED: "+classType+"."+"<init>"+" ("+argVals+") in thread "+evaluationContext.getFrame().thread());
            }
        }
    }
    
    @Override
    public Mirror visitParenthesized(ParenthesizedTree arg0, EvaluationContext evaluationContext) {
        return arg0.getExpression().accept(this, evaluationContext);
    }

    @Override
    public Mirror visitReturn(ReturnTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitMemberSelect(MemberSelectTree arg0, EvaluationContext evaluationContext) {
        TreePath memberSelectPath = TreePath.getPath(getCurrentPath(), arg0);
        if (memberSelectPath == null) memberSelectPath = getCurrentPath();
        Element elm = evaluationContext.getTrees().getElement(memberSelectPath);
        switch(elm.getKind()) {
            case ENUM_CONSTANT:
                VariableElement ve = (VariableElement) elm;
                String constantName = ve.getSimpleName().toString();
                ReferenceType enumType = getClassType(arg0, ve.asType(), evaluationContext);
                Method valueOfMethod = enumType.methodsByName("valueOf").get(0);
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                StringReference constantNameRef = vm.mirrorOf(constantName);
                Value enumValue = invokeMethod(arg0, valueOfMethod, true, (ClassType) enumType, null,
                             Collections.singletonList(constantNameRef), evaluationContext);
                return enumValue;
            case FIELD:
                ve = (VariableElement) elm;
                String fieldName = ve.getSimpleName().toString();
                Mirror expression = arg0.getExpression().accept(this, evaluationContext);
                if (expression instanceof ClassType) {
                    Field f = ((ClassType) expression).fieldByName(fieldName);
                    if (f != null) {
                        return ((ClassType) expression).getValue(f);
                    } else {
                        Assert2.error(arg0, "unknownField", fieldName);
                        return null;
                    }
                }
                if (expression instanceof ObjectReference) {
                    if (expression instanceof ArrayReference && "length".equals(fieldName)) {
                        return expression.virtualMachine().mirrorOf(((ArrayReference) expression).length());
                    }
                    ReferenceType type = ((ObjectReference) expression).referenceType();
                    Field f = type.fieldByName(fieldName);
                    if (f != null) {
                        return ((ObjectReference) expression).getValue(f);
                    } else {
                        Assert2.error(arg0, "unknownField", fieldName);
                        return null;
                    }
                }
                throw new IllegalArgumentException("Wrong expression value: "+expression);
            case CLASS:
                TypeElement te = (TypeElement) elm;
                String className = te.getQualifiedName().toString();
                vm = evaluationContext.getDebugger().getVirtualMachine();
                List<ReferenceType> classes = vm.classesByName(className);
                if (classes.size() == 0) {
                    Assert2.error(arg0, "unknownType", className);
                }
                return classes.get(0);
            default:
                throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"', element kind = "+elm.getKind());
        }
    }

    @Override
    public Mirror visitEmptyStatement(EmptyStatementTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitSwitch(SwitchTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitSynchronized(SynchronizedTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    public Mirror visitThrow(ThrowTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    @Override
    public Mirror visitCompilationUnit(CompilationUnitTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitTry(TryTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitParameterizedType(ParameterizedTypeTree arg0, EvaluationContext evaluationContext) {
        return arg0.getType().accept(this, evaluationContext);
    }

    @Override
    public Mirror visitArrayType(ArrayTypeTree arg0, EvaluationContext evaluationContext) {
        Type type = (Type) arg0.getType().accept(this, evaluationContext);
        if (type == null) return null;
        String arrayClassName = type.name()+"[]";
        List<ReferenceType> aTypes = type.virtualMachine().classesByName(arrayClassName);
        if (aTypes.size() > 0) {
            return aTypes.get(0);
        } else {
            Assert2.error(arg0, "unknownType", arrayClassName);
            return null;
        }
    }

    @Override
    public Mirror visitTypeCast(TypeCastTree arg0, EvaluationContext evaluationContext) {
        ExpressionTree expTree = arg0.getExpression();
        Mirror expression = expTree.accept(this, evaluationContext);
        if (expression == null) return null;
        Tree typeTree = arg0.getType();
        Mirror type = typeTree.accept(this, evaluationContext);
        if (expression instanceof PrimitiveValue) {
            PrimitiveValue primValue = (PrimitiveValue) expression;
            if (primValue instanceof BooleanValue) {
                Assert2.assertAssignable(type, BooleanType.class, arg0, "castToBooleanRequired", primValue, type);
                return primValue;
            }
            Assert2.assertNotAssignable(type, BooleanType.class, arg0, "castFromBooleanRequired", primValue, type);
            VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
            if (type instanceof ByteType) {
                return vm.mirrorOf(primValue.byteValue());
            } else if (type instanceof CharType) {
                return vm.mirrorOf(primValue.charValue());
            } else if (type instanceof DoubleType) {
                return vm.mirrorOf(primValue.doubleValue());
            } else if (type instanceof FloatType) {
                return vm.mirrorOf(primValue.floatValue());
            } else if (type instanceof IntegerType) {
                return vm.mirrorOf(primValue.intValue());
            } else if (type instanceof LongType) {
                return vm.mirrorOf(primValue.longValue());
            } else {
                return vm.mirrorOf(primValue.shortValue());
            }
        }
        if (!instanceOf((Type) type, ((ObjectReference) expression).type())) {
            Assert2.error(arg0, "castError", ((ObjectReference) expression).type(), type);
        }
        return expression;
    }

    @Override
    public Mirror visitPrimitiveType(PrimitiveTypeTree arg0, EvaluationContext evaluationContext) {
        TypeKind type = arg0.getPrimitiveTypeKind();
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        switch(type) {
            case BOOLEAN:
                return vm.mirrorOf(true).type();
            case BYTE:
                return vm.mirrorOf((byte) 0).type();
            case CHAR:
                return vm.mirrorOf('a').type();
            case DOUBLE:
                return vm.mirrorOf(0.).type();
            case FLOAT:
                return vm.mirrorOf(0f).type();
            case INT:
                return vm.mirrorOf(0).type();
            case LONG:
                return vm.mirrorOf(0l).type();
            case SHORT:
                return vm.mirrorOf((short) 0).type();
            default:
                throw new IllegalStateException("Tree = "+arg0);
        }
    }

    public Mirror visitTypeParameter(TypeParameterTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    @Override
    public Mirror visitInstanceOf(InstanceOfTree arg0, EvaluationContext evaluationContext) {
        Mirror expression = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (expression == null) return vm.mirrorOf(false);
        Assert2.assertAssignable(expression, ObjectReference.class, arg0, "instanceOfLeftOperandNotAReference", expression);

        ReferenceType expressionType = ((ObjectReference) expression).referenceType();
        Type type = (Type) arg0.getType().accept(this, evaluationContext);

        return vm.mirrorOf(instanceOf(expressionType, type));
    }

    @Override
    public Mirror visitUnary(UnaryTree arg0, EvaluationContext evaluationContext) {
        Mirror expression = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        Tree.Kind kind = arg0.getKind();
        if (expression instanceof BooleanValue) {
            boolean v = ((BooleanValue) expression).value();
            switch (kind) {
                case LOGICAL_COMPLEMENT:
                    v = !v;
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        if (expression instanceof ByteValue) {
            byte v = ((ByteValue) expression).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    int i = ~v;
                    return vm.mirrorOf(i);
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    i = -v;
                    return vm.mirrorOf(i);
                case UNARY_PLUS:
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        if (expression instanceof CharValue) {
            char v = ((CharValue) expression).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    int i = ~v;
                    return vm.mirrorOf(i);
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    i = -v;
                    return vm.mirrorOf(i);
                case UNARY_PLUS:
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        if (expression instanceof ShortValue) {
            short v = ((ShortValue) expression).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    int i = ~v;
                    return vm.mirrorOf(i);
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    i = -v;
                    return vm.mirrorOf(i);
                case UNARY_PLUS:
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        if (expression instanceof IntegerValue) {
            int v = ((IntegerValue) expression).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    v = ~v;
                    break;
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        if (expression instanceof LongValue) {
            long v = ((LongValue) expression).value();
            switch (kind) {
                case BITWISE_COMPLEMENT:
                    v = ~v;
                    break;
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        if (expression instanceof DoubleValue) {
            double v = ((DoubleValue) expression).value();
            switch (kind) {
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        if (expression instanceof FloatValue) {
            float v = ((FloatValue) expression).value();
            switch (kind) {
                case POSTFIX_DECREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v - 1), evaluationContext);
                    break;
                case POSTFIX_INCREMENT:
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v + 1), evaluationContext);
                    break;
                case PREFIX_DECREMENT:
                    --v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case PREFIX_INCREMENT:
                    ++v;
                    setToMirror(arg0.getExpression(), vm.mirrorOf(v), evaluationContext);
                    break;
                case UNARY_MINUS:
                    v = -v;
                    break;
                case UNARY_PLUS:
                    break;
                default: throw new IllegalStateException("Tree = "+arg0);
            }
            return vm.mirrorOf(v);
        }
        throw new IllegalStateException("Bad expression type: "+expression);
    }

    public Mirror visitVariable(VariableTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    @Override
    public Mirror visitWhileLoop(WhileLoopTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    public Mirror visitWildcard(WildcardTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    @Override
    public Mirror visitOther(Tree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }
    
    private void setToMirror(Tree var, Value value, EvaluationContext evaluationContext) {
        VariableInfo varInfo = evaluationContext.getVariables().get(var);
        if (varInfo == null) {
            throw new IllegalStateException("Unknown variable "+var);
        }
        try {
            if (varInfo.field != null) {
                if (varInfo.fieldObject != null) {
                    varInfo.fieldObject.setValue(varInfo.field, value);
                } else {
                    ((ClassType) varInfo.field.declaringType()).setValue(varInfo.field, value);
                }
            } else {
                evaluationContext.getFrame().setValue(varInfo.var, value);
            }
        } catch (InvalidTypeException itex) {
            
        } catch (ClassNotLoadedException cnlex) {
            
        }
    }
    
    private Value invokeMethod(Tree arg0, Method method, boolean isStatic, ClassType type,
                               ObjectReference objectReference, List<? extends Value> argVals,
                               EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert2.error(arg0, "calleeException", new UnsupportedOperationException(), evaluationContext);
        }
        ThreadReference evaluationThread = evaluationContext.getFrame().thread();
        try {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("STARTED : "+objectReference+"."+method+" ("+argVals+") in thread "+evaluationThread);
            }
            evaluationContext.methodToBeInvoked();
            Value value;
            if (isStatic) {
                value = type.invokeMethod(evaluationThread, method, argVals,
                                          ObjectReference.INVOKE_SINGLE_THREADED);
            } else {
                value = objectReference.invokeMethod(evaluationThread, method,
                                                     argVals,
                                                     ObjectReference.INVOKE_SINGLE_THREADED);
            }
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("   return = "+value);
            }
            return value;
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(new InvalidExpressionException (cnlex));
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            ieex.initCause(itsex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            InvalidExpressionException ieex = new InvalidExpressionException (iex);
            ieex.initCause(iex);
            throw new IllegalStateException(ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            ieex.initCause(uoex);
            throw new IllegalStateException(ieex);
        } catch (ObjectCollectedException ocex) {
            throw new IllegalStateException(new InvalidExpressionException(NbBundle.getMessage(
                Evaluator.class, "CTL_EvalError_collected")));
        } finally {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("FINISHED: "+objectReference+"."+method+" ("+argVals+") in thread "+evaluationThread);
            }
            try {
                evaluationContext.methodInvokeDone();
            } catch (IncompatibleThreadStateException itsex) {
                InvalidExpressionException ieex = new InvalidExpressionException (itsex);
                ieex.initCause(itsex);
                throw new IllegalStateException(ieex);
            }
        }
    }

}
