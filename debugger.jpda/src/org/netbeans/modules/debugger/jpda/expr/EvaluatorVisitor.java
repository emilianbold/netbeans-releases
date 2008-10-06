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
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.InvalidStackFrameException;

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
import java.util.Arrays;
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
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import javax.lang.model.type.TypeVariable;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext.VariableInfo;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.NbBundle;

/**
 * Mirror is either Value or ReferenceType
 * 
 * @author Martin Entlicher
 */
public class EvaluatorVisitor extends TreePathScanner<Mirror, EvaluationContext> {

    private static final Logger loggerMethod = Logger.getLogger("org.netbeans.modules.debugger.jpda.invokeMethod"); // NOI18N
    private static final Logger loggerValue = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N
    
    private Type newArrayType;
    private Expression2 expression;

    public EvaluatorVisitor(Expression2 expression) {
        this.expression = expression;
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
        Boolean isStatic = null;
        ExpressionTree expression = arg0.getMethodSelect();
        Element elm;
        TreePath currentPath = getCurrentPath();
        if (expression.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree mst = (MemberSelectTree) expression;
            object = mst.getExpression().accept(this, evaluationContext);
            methodName = mst.getIdentifier().toString();
            if (object == null) {
                Assert2.error(arg0, "methodCallOnNull", methodName);
            }
            if (currentPath != null) {
                TreePath memberSelectPath = TreePath.getPath(currentPath, mst);
                if (memberSelectPath == null) memberSelectPath = currentPath;
                elm = evaluationContext.getTrees().getElement(memberSelectPath);
            } else {
                elm = null;
            }
        } else {
            if (currentPath != null) {
                TreePath methodInvokePath = TreePath.getPath(currentPath, arg0);
                if (methodInvokePath == null) methodInvokePath = currentPath;
                elm = evaluationContext.getTrees().getElement(methodInvokePath);
                methodName = elm.getSimpleName().toString();
            } else {
                elm = null;
                methodName = expression.toString();
            }
        }
        List<? extends TypeMirror> paramTypes = null;
        String enclosingClass = null;
        if (elm != null) {
            TypeMirror typeMirror = elm.asType();
            TypeKind kind = typeMirror.getKind();
            if (kind == TypeKind.ERROR) { // In case of error type resolution we do not know parameter types
                elm = null;
            } else {
                if (kind != TypeKind.EXECUTABLE) {
                    Assert2.error(arg0, "noSuchMethod", elm.getSimpleName().toString(), elm.getEnclosingElement().getSimpleName().toString());
                }
                ExecutableElement methodElement = (ExecutableElement) elm;
                ExecutableType execTypeMirror = (ExecutableType) typeMirror;
                paramTypes = execTypeMirror.getParameterTypes();
                isStatic = methodElement.getModifiers().contains(Modifier.STATIC);
                Element enclosing = methodElement.getEnclosingElement();
                if (enclosing.getKind() == ElementKind.CLASS) {
                    TypeElement enclosingClassElement = (TypeElement) enclosing;
                    enclosingClass = ElementUtilities.getBinaryName(enclosingClassElement);
                }
            }
        }
        
        List<? extends ExpressionTree> args = arg0.getArguments();
        List<Value> argVals = new ArrayList<Value>(args.size());
        for (ExpressionTree arg : args) {
            Mirror argValue = arg.accept(this, evaluationContext);
            if (argValue != null && !(argValue instanceof Value)) {
                Assert2.error(arg, "Not a value");
            }
            argVals.add((Value) argValue);
        }
        List<Type> argTypes = null;
        if (elm == null) {
            argTypes = new ArrayList<Type>(argVals.size());
            for (Value value : argVals) {
                if (value == null) {
                    argTypes.add(evaluationContext.getDebugger().getVirtualMachine().classesByName("java.lang.Object").get(0));
                } else {
                    argTypes.add(value.type());
                }
            }
        }
        ObjectReference objectReference;
        ReferenceType type;
        if (isStatic == null) {
            if (object instanceof ClassType || object instanceof ArrayType) {
                type = (ReferenceType) object;
                objectReference = null;
                isStatic = Boolean.TRUE;
            } else if (object instanceof ObjectReference) {
                objectReference = (ObjectReference) object;
                type = (ReferenceType) objectReference.type();
            } else {
                objectReference = evaluationContext.getFrame().thisObject();
                type = (ReferenceType) evaluationContext.getFrame().location().declaringType();
            }
        } else if (isStatic) {
            objectReference = null;
            if (object instanceof ClassType || object instanceof ArrayType) {
                type = (ReferenceType) object;
            } else if (object instanceof ObjectReference) {
                type = (ReferenceType) ((ObjectReference) object).type();
            } else {
                type = evaluationContext.getFrame().location().declaringType();
                if (enclosingClass != null) {
                    ReferenceType dt = findEnclosingType(type, enclosingClass);
                    if (dt != null) type = dt;
                }
            }
        } else {
            if (object != null) {
                if (object instanceof ClassType) {
                    Assert2.error(arg0, "invokeInstanceMethodAsStatic", methodName);
                    objectReference = null;
                    type = null;
                } else {
                    objectReference = (ObjectReference) object;
                    type = objectReference.referenceType();
                }
            } else {
                objectReference = evaluationContext.getFrame().thisObject();
                if (objectReference != null) {
                    type = objectReference.referenceType();
                    if (enclosingClass != null) {
                        ReferenceType enclType = findEnclosingType(type, enclosingClass);
                        if (enclType != null) {
                            ObjectReference enclObject = findEnclosingObject(arg0, objectReference, enclType, null, methodName);
                            if (enclObject != null) type = enclObject.referenceType();
                            else Assert2.error(arg0, "noSuchMethod", methodName, type.name());
                        }
                    }
                } else {
                    type = null;
                }
            }
            if (objectReference == null) {
                Assert2.error(arg0, "methodCallOnNull", methodName);
            }
        }
        ClassType cType;
        if (type instanceof ArrayType) {
            Assert2.error(arg0, "methOnArray");
            return null;
        } else {
            cType = (ClassType) type;
        }
        Method method = getConcreteMethodAndReportProblems(arg0, type, methodName, null, paramTypes, argTypes);
        return invokeMethod(arg0, method, isStatic, cType, objectReference, argVals, evaluationContext);
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

    private static Method getConcreteMethodAndReportProblems(Tree arg0, ReferenceType type, String methodName, String firstParamSignature, List<? extends TypeMirror> paramTypes, List<? extends Type> argTypes) {
        Method method;
        try {
            if (paramTypes != null) {
                method = getConcreteMethod(type, methodName, firstParamSignature, paramTypes);
            } else {
                method = getConcreteMethod2(type, methodName, argTypes);
            }
        } catch (UnsuitableArgumentsException uaex) {
            StringBuilder methodArgs = new StringBuilder("(");
            if (paramTypes != null) {
                 for (TypeMirror paramType : paramTypes) {
                     if (methodArgs.length() > 1) methodArgs.append(", ");
                     methodArgs.append(paramType.toString());
                 }
            } else {
                for (Type argType : argTypes) {
                    if (methodArgs.length() > 1) methodArgs.append(", ");
                     methodArgs.append(argType.name());
                }
            }
            methodArgs.append(")");
            if ("<init>".equals(methodName)) {
                Assert2.error(arg0, "noSuchConstructorWithArgs", type.name(), methodArgs.toString());
            }
            if (methodArgs.length() == 2) {
                Assert2.error(arg0, "noSuchMethod", methodName+methodArgs, type.name());
            } else {
                Assert2.error(arg0, "noSuchMethodWithArgs", methodName, type.name(), methodArgs.toString());
            }
            method = null;
        }
        if (method == null) {
            Assert2.error(arg0, "noSuchMethod", methodName, type.name());
        }
        return method;
    }
    
    private static Method getConcreteMethod(ReferenceType type, String methodName, List<? extends TypeMirror> typeArguments) throws UnsuitableArgumentsException {
        return getConcreteMethod(type, methodName, null, typeArguments);
    }
    
    private static Method getConcreteMethod(ReferenceType type, String methodName, String firstParamSignature, List<? extends TypeMirror> typeArguments) throws UnsuitableArgumentsException {
        List<Method> methods = type.methodsByName(methodName);
        String signature = createSignature(firstParamSignature, typeArguments);
        boolean constructor = "<init>".equals(methodName);
        for (Method method : methods) {
            if (!method.isAbstract() &&
                (!constructor || type.equals(method.declaringType())) &&
                egualMethodSignatures(method.signature(), signature)) {
                return method;
            }
        }
        if (methods.size() > 0) throw new UnsuitableArgumentsException();
        return null;
    }

    private static Method getConcreteMethod2(ReferenceType type, String methodName, List<? extends Type> typeArguments) throws UnsuitableArgumentsException {
        List<Method> methods = type.methodsByName(methodName);
        List<Method> possibleMethods = new ArrayList<Method>();
        boolean constructor = "<init>".equals(methodName);
        for (Method method : methods) {
            if (!method.isAbstract() &&
                (!constructor || type.equals(method.declaringType()))) {
                try {
                    if (equalTypes(method.argumentTypes(), typeArguments)) {
                        return method;
                    }
                    if (acceptTypes(method.argumentTypes(), typeArguments)) {
                        possibleMethods.add(method);
                    }
                } catch (ClassNotLoadedException ex) {
                    // Ignore
                } catch (ObjectCollectedException ocex) {
                    // What can we do?
                }
            }
        }
        if (possibleMethods.size() == 0) {
            if (methods.size() > 0) throw new UnsuitableArgumentsException();
            return null;
        }
        return possibleMethods.get(0);
    }
    
    private static boolean equalTypes(List<? extends Type> methodTypes, List<? extends Type> argumentTypes) {
        if (methodTypes.size() != argumentTypes.size()) {
            return false;
        }
        int n = methodTypes.size();
        for (int i = 0; i < n; i++) {
            if (!methodTypes.get(i).equals(argumentTypes.get(i)) &&
                !unboxType(methodTypes.get(i)).equals(unboxType(argumentTypes.get(i)))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean acceptTypes(List<? extends Type> methodTypes, List<? extends Type> argumentTypes) {
        if (methodTypes.size() != argumentTypes.size()) {
            return false;
        }
        int n = methodTypes.size();
        for (int i = 0; i < n; i++) {
            Type methodType = unboxType(methodTypes.get(i));
            Type argType = unboxType(argumentTypes.get(i));
            if (!methodType.equals(argType)) {
                if (!extendsType(argType, methodType)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean extendsType(Type argType, Type methodType) {
        if (methodType instanceof ReferenceType && argType instanceof ReferenceType) {
            return extendsType((ReferenceType) argType, (ReferenceType) methodType);
        } else if (methodType instanceof PrimitiveType && argType instanceof PrimitiveType) {
            return extendsType((PrimitiveType) argType, (PrimitiveType) methodType);
        }
        return false;
    }
    
    /** @return true if t1 extends t2 */
    private static boolean extendsType(ReferenceType t1, ReferenceType t2) {
        if (t2 instanceof InterfaceType) {
            List<InterfaceType> superInterfaces;
            if (t1 instanceof ClassType) {
                superInterfaces = ((ClassType) t1).allInterfaces();
            } else if (t1 instanceof InterfaceType) {
                superInterfaces = ((InterfaceType) t1).superinterfaces();
            } else {
                return false;
            }
            return superInterfaces.contains(t2);
        }
        if (t2 instanceof ClassType) {
            if (t1 instanceof ClassType) {
                ClassType superClass = ((ClassType) t1).superclass();
                if (superClass != null) {
                    if (superClass.equals(t2)) return true;
                    else return extendsType(superClass, t2);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (t2 instanceof ArrayType) {
            if (t1 instanceof ArrayType) {
                try {
                    Type ct1 = ((ArrayType) t1).componentType();
                    Type ct2 = ((ArrayType) t2).componentType();
                    return extendsType(ct1, ct2);
                } catch (ClassNotLoadedException cnlex) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            throw new IllegalStateException("Unknown ReferenceType: "+t2);
        }
    }
    
    /** @return true if t2 is an extension of t1 */
    private static boolean extendsType(PrimitiveType t1, PrimitiveType t2) {
        // BooleanType, ByteType and CharType can be matched together only.
        if (t2 instanceof ShortType) {
            return t2 instanceof ByteType || t2 instanceof ShortType;
        }
        if (t2 instanceof IntegerType) {
            return t2 instanceof ByteType || t2 instanceof ShortType || t2 instanceof IntegerType;
        }
        if (t2 instanceof LongType) {
            return t2 instanceof ByteType || t2 instanceof ShortType ||
                   t2 instanceof IntegerType || t2 instanceof LongType;
        }
        if (t2 instanceof FloatType) {
            return !(t2 instanceof BooleanType || t2 instanceof CharType || t2 instanceof DoubleType);
        }
        if (t2 instanceof DoubleType) {
            return !(t2 instanceof BooleanType || t2 instanceof CharType);
        }
        return false;
    }
    
    private static Type unboxType(Type t) {
        if (t instanceof ClassType) {
            String name = ((ClassType) t).name();
            if (name.equals("java.lang.Boolean")) {
                t = t.virtualMachine().mirrorOf(true).type();
            } else if (name.equals("java.lang.Byte")) {
                t = t.virtualMachine().mirrorOf((byte) 10).type();
            } else if (name.equals("java.lang.Character")) {
                t = t.virtualMachine().mirrorOf('a').type();
            } else if (name.equals("java.lang.Integer")) {
                t = t.virtualMachine().mirrorOf(10).type();
            } else if (name.equals("java.lang.Long")) {
                t = t.virtualMachine().mirrorOf(10l).type();
            } else if (name.equals("java.lang.Short")) {
                t = t.virtualMachine().mirrorOf((short)10).type();
            } else if (name.equals("java.lang.Float")) {
                t = t.virtualMachine().mirrorOf(10f).type();
            } else if (name.equals("java.lang.Double")) {
                t = t.virtualMachine().mirrorOf(10.0).type();
            }
        }
        return t;
    }
    
    private static boolean egualMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) s1 = s1.substring(0, i);
        i = s2.lastIndexOf(")");
        if (i > 0) s2 = s2.substring(0, i);
        return s1.equals(s2);
    }
    
    private static String createSignature(String firstParamSignature, List<? extends TypeMirror> typeArguments) {
        StringBuilder signature = new StringBuilder("(");
        if (firstParamSignature != null) {
            signature.append(firstParamSignature);
        }
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
        if (vm == null) return null;
        List<ReferenceType> classes = vm.classesByName(className);
        if (classes.size() == 0) {
            Assert2.error(tree, "unknownType", className);
        }
        return classes.get(0);
    }

    public static boolean instanceOf(Type left, Type right) {
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
                List<InterfaceType> ifaces = classLeft.allInterfaces();
                for (Iterator<InterfaceType> i = ifaces.iterator(); i.hasNext();) {
                    InterfaceType type = i.next();
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
        
        if (left instanceof InterfaceType) {
            InterfaceType intLeft = (InterfaceType) left;
            if (right instanceof InterfaceType) {
                List<InterfaceType> ifaces = intLeft.superinterfaces();
                for (Iterator<InterfaceType> i = ifaces.iterator(); i.hasNext();) {
                    InterfaceType type = i.next();
                    if (type.equals(right)) return true;
                }
                return false;
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
        Value value = (Value) exp;
        setToMirror(arg0.getVariable(), value, evaluationContext);
        return value;
    }

    @Override
    public Mirror visitCompoundAssignment(CompoundAssignmentTree arg0, EvaluationContext evaluationContext) {
        Mirror var = arg0.getVariable().accept(this, evaluationContext);
        Mirror exp = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) return null;
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
        if (vm == null) return null;
        Tree.Kind kind = arg0.getKind();
        if (left instanceof ObjectReference) {
            left = unboxIfCan(arg0, (ObjectReference) left, evaluationContext);
        }
        if (right instanceof ObjectReference) {
            right = unboxIfCan(arg0, (ObjectReference) right, evaluationContext);
        }
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
            String s2 = (right == null) ? null : ((StringReference) right).value();
            switch (kind) {
                case PLUS:
                    return vm.mirrorOf(s1 + s2);
                default: throw new IllegalStateException("Unhandled binary tree: "+arg0);
            }
        }
        if ((left instanceof StringReference || right instanceof StringReference) && kind == Tree.Kind.PLUS) {
            String s1 = (left instanceof StringReference) ? ((StringReference) left).value() : toString(arg0, left, evaluationContext);
            String s2 = (right instanceof StringReference) ? ((StringReference) right).value() : toString(arg0, right, evaluationContext);
            return vm.mirrorOf(s1 + s2);
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
    
    private Mirror getIdentifierByName(IdentifierTree arg0, EvaluationContext evaluationContext) {
        String name = arg0.getName().toString();
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) return null;
        List<ReferenceType> classes = vm.classesByName(name);
        if (classes.size() > 0) {
            return classes.get(0);
        }
        // Class not found. If the source is not fully resolved, we may
        // get a field or local variable here:
        if (name.equals("this")) {
            return evaluationContext.getFrame().thisObject();
        }
        if (name.equals("super")) {
            ReferenceType thisType = evaluationContext.getFrame().location().declaringType();
            if (thisType instanceof ClassType) {
                ClassType superClass = ((ClassType) thisType).superclass();
                ObjectReference thisObject = evaluationContext.getFrame().thisObject();
                if (thisObject == null) {
                    return superClass;
                } else {
                    return thisObject;
                }
            }
        }
        Field field = evaluationContext.getFrame().location().declaringType().fieldByName(name);
        if (field != null) {
            if (field.isStatic()) {
                evaluationContext.getVariables().put(arg0, new VariableInfo(field));
                return field.declaringType().getValue(field);
            }
            ObjectReference thisObject = evaluationContext.getFrame().thisObject();
            if (thisObject != null) {
                evaluationContext.getVariables().put(arg0, new VariableInfo(field, thisObject));
                return thisObject.getValue(field);
            }
        }
        try {
            LocalVariable lv = evaluationContext.getFrame().visibleVariableByName(name);
            if (lv == null) {
                ObjectReference thiz = evaluationContext.getFrame().thisObject();
                if (thiz != null) {
                    Field outer = thiz.referenceType().fieldByName("val$"+name);
                    if (outer != null) {
                        Value val = thiz.getValue(outer);
                        evaluationContext.getVariables().put(arg0, new VariableInfo(outer, thiz));
                        return val;
                    }
                }
                Assert2.error(arg0, "unknownVariable", name);
            }
            evaluationContext.getVariables().put(arg0, new VariableInfo(lv));
            return evaluationContext.getFrame().getValue(lv);
        } catch (AbsentInformationException aiex) {}
        Assert2.error(arg0, "unknownType", name);
        return null;
    }

    @Override
    public Mirror visitIdentifier(IdentifierTree arg0, EvaluationContext evaluationContext) {
        String identifier = arg0.getName().toString();
        // class special variable
        if (expression.classReplaced().equals(identifier)) {
            ReferenceType refType = evaluationContext.getFrame().location().declaringType();
            JPDAClassType classType = evaluationContext.getDebugger().getClassType(refType);
            return ((JDIVariable) classType.classObject()).getJDIValue();
        }
        
        // return special variable
        if (expression.returnReplaced().equals(identifier)) {
            ThreadReference tr = evaluationContext.getFrame().thread();
            JPDAThreadImpl thread = (JPDAThreadImpl) evaluationContext.getDebugger().getThread(tr);
            JDIVariable returnVar = (JDIVariable) thread.getReturnVariable();
            if (returnVar != null) {
                return returnVar.getJDIValue();
            } else {
                return null;
            }
        }

        TreePath currentPath = getCurrentPath();
        Element elm = null;
        if (currentPath != null) {
            TreePath identifierPath = TreePath.getPath(currentPath, arg0);
            if (identifierPath == null) identifierPath = getCurrentPath();
            elm = evaluationContext.getTrees().getElement(identifierPath);
            if (elm instanceof TypeElement && ((TypeElement) elm).asType() instanceof ErrorType) {
                currentPath = null; // Elements not resolved correctly
            }
        }
        if (currentPath == null) {
            return getIdentifierByName(arg0, evaluationContext);
        }
        switch(elm.getKind()) {
            case CLASS:
            case ENUM:
            case INTERFACE:
                TypeElement te = (TypeElement) elm;
                String className = ElementUtilities.getBinaryName(te);
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                if (vm == null) return null;
                List<ReferenceType> classes = vm.classesByName(className);
                if (classes.size() > 0) {
                    return classes.get(0);
                }
                Assert2.error(arg0, "unknownType", className);
            case ENUM_CONSTANT:
                return getEnumConstant(arg0, (VariableElement) elm, evaluationContext);
            case FIELD:
                VariableElement ve = (VariableElement) elm;
                String fieldName = ve.getSimpleName().toString();
                if (fieldName.equals("this")) {
                    return evaluationContext.getFrame().thisObject();
                }
                if (fieldName.equals("super")) {
                    ReferenceType thisType = evaluationContext.getFrame().location().declaringType();
                    if (thisType instanceof ClassType) {
                        ClassType superClass = ((ClassType) thisType).superclass();
                        ObjectReference thisObject = evaluationContext.getFrame().thisObject();
                        if (thisObject == null) {
                            return superClass;
                        } else {
                            return thisObject;
                        }
                    }
                }
                Element enclosing = ve.getEnclosingElement();
                String enclosingClass = null;
                if (enclosing.getKind() == ElementKind.CLASS) {
                    TypeElement enclosingClassElement = (TypeElement) enclosing;
                    enclosingClass = ElementUtilities.getBinaryName(enclosingClassElement);
                }
                ReferenceType declaringType = evaluationContext.getFrame().location().declaringType();
                if (enclosingClass != null) {
                    ReferenceType dt = findEnclosingType(declaringType, enclosingClass);
                    if (dt != null) declaringType = dt;
                }
                Field field = declaringType.fieldByName(fieldName);
                if (field == null) {
                    Assert2.error(arg0, "unknownVariable", fieldName);
                }
                if (field.isStatic()) {
                    evaluationContext.getVariables().put(arg0, new VariableInfo(field));
                    return declaringType.getValue(field);
                }
                ObjectReference thisObject = evaluationContext.getFrame().thisObject();
                if (thisObject != null) {
                    if (field.isPrivate()) {
                        ObjectReference to = findEnclosingObject(arg0, thisObject, declaringType, field.name(), null);
                        thisObject = to;
                    } else {
                        if (!instanceOf(thisObject.referenceType(), declaringType)) {
                            ObjectReference to = findEnclosingObject(arg0, thisObject, declaringType, field.name(), null);
                            thisObject = to;
                        }
                    }
                }
                if (thisObject != null) {
                    evaluationContext.getVariables().put(arg0, new VariableInfo(field, thisObject));
                    try {
                        return thisObject.getValue(field);
                    } catch (IllegalArgumentException iaex) {
                        Logger.getLogger(getClass().getName()).severe("field = "+field+", thisObject = "+thisObject); // NOI18N
                        throw iaex;
                    }
                } else {
                    Assert2.error(arg0, "accessInstanceVariableFromStaticContext", fieldName);
                    throw new IllegalStateException("No current instance available.");
                }
            case LOCAL_VARIABLE:
            case EXCEPTION_PARAMETER:
                ve = (VariableElement) elm;
                String varName = ve.getSimpleName().toString();
                try {
                    LocalVariable lv = evaluationContext.getFrame().visibleVariableByName(varName);
                    if (lv == null) {
                        ObjectReference thiz = evaluationContext.getFrame().thisObject();
                        if (thiz != null) {
                            Field outer = thiz.referenceType().fieldByName("val$"+varName);
                            if (outer != null) {
                                Value val = thiz.getValue(outer);
                                evaluationContext.getVariables().put(arg0, new VariableInfo(outer, thiz));
                                return val;
                            }
                        }
                        Assert2.error(arg0, "unknownVariable", varName);
                    }
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
                    if (lv == null) {
                        ObjectReference thiz = frame.thisObject();
                        if (thiz != null) {
                            Field outer = thiz.referenceType().fieldByName("val$"+paramName);
                            if (outer != null) {
                                Value val = thiz.getValue(outer);
                                evaluationContext.getVariables().put(arg0, new VariableInfo(outer, thiz));
                                return val;
                            }
                        }
                        Assert2.error(arg0, "unknownVariable", paramName);
                    }
                    evaluationContext.getVariables().put(arg0, new VariableInfo(lv));
                    return frame.getValue(lv);
                } catch (AbsentInformationException aiex) {
                    try {
                        org.netbeans.api.debugger.jpda.LocalVariable[] lvs;
                        lvs = new CallStackFrameImpl((JPDAThreadImpl) ((JPDADebuggerImpl) evaluationContext.getDebugger()).getThread(frame.thread()),
                                                     frame, 0, evaluationContext.getDebugger()).getMethodArguments();
                        if (lvs != null) {
                            for (org.netbeans.api.debugger.jpda.LocalVariable lv : lvs) {
                                if (paramName.equals(lv.getName())) {
                                    return ((JDIVariable) lv).getJDIValue();
                                }
                            }
                        }
                    } catch (NativeMethodException nmex) {
                        // ignore - no arguments available
                    } catch (InvalidStackFrameException ex) {
                    }
                    return (Value) Assert2.error(arg0, "unknownVariable", paramName);
                }
            case PACKAGE:
                return (Value) Assert2.error(arg0, "notExpression");
            default:
                throw new UnsupportedOperationException("Not supported element kind:"+elm.getKind()+" Tree = '"+arg0+"'");
        }
    }
    
    private ReferenceType findEnclosingType(ReferenceType type, String name) {
        if (type.name().equals(name)) {
            return type;
        }
        List<ReferenceType> classes = type.virtualMachine().classesByName(name);
        if (classes.size() == 1) {
            return classes.get(0);
        }
        for (ReferenceType clazz : classes) {
            if (isNestedOf(clazz, type)) {
                return clazz;
            }
        }
        return null;
    }
    
    private boolean isNestedOf(ReferenceType nested, ReferenceType type) {
        if (nested.equals(type)) {
            return true;
        }
        for (ReferenceType n : nested.nestedTypes()) {
            if (isNestedOf(n, type)) {
                return true;
            }
        }
        return false;
    }
    
    private ObjectReference findEnclosingObject(Tree arg0, ObjectReference object, ReferenceType type, String fieldName, String methodName) {
        if (instanceOf(object.referenceType(), type)) {
            return object;
        }
        if (((ReferenceType) object.type()).isStatic()) {
            // instance fields/methods can not be accessed from static context.
            if (fieldName != null) {
                Assert2.error(arg0, "accessInstanceVariableFromStaticContext", fieldName);
            }
            if (methodName != null) {
                Assert2.error(arg0, "invokeInstanceMethodAsStatic", methodName);
            }
            return null;
        }
        Field outerRef = null;
        for (int i = 0; i < 9; i++) {
            outerRef = object.referenceType().fieldByName("this$"+i);
            if (outerRef != null) break;
        }
        if (outerRef == null) return null;
        object = (ObjectReference) object.getValue(outerRef);
        if (object == null) return null;
        return findEnclosingObject(arg0, object, type, fieldName, methodName);
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
        if (array == null) {
            Assert2.error(arg0, "arrayIsNull", arg0.getExpression());
        }
        Mirror index = arg0.getIndex().accept(this, evaluationContext);
        if (!(array instanceof ArrayReference)) {
            Assert2.error(arg0, "notArrayType", arg0.getExpression());
        }
        if (!(index instanceof PrimitiveValue)) {
            Assert2.error(arg0, "arraySizeBadType", index);
        }
        int i = ((PrimitiveValue) index).intValue();
        if (i >= ((ArrayReference) array).length()) {
            Assert2.error(arg0, "arrayIndexOutOfBounds", array, i);
        }
        return ((ArrayReference) array).getValue(i);
    }

    @Override
    public Mirror visitLabeledStatement(LabeledStatementTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitLiteral(LiteralTree arg0, EvaluationContext evaluationContext) {
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) return null;
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
                List<Value> args = Collections.emptyList();
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
        autoboxElements(arg0, type, elements, evaluationContext);
        try {
            array.setValues(elements);
        } catch (InvalidTypeException ex) {
            throw new IllegalStateException("ArrayType "+getArrayType(arg0, type, depth)+" can not have "+elements+" elements.");
        } catch (ClassNotLoadedException ex) {
            throw new IllegalStateException(new InvalidExpressionException (ex));
        }
        return array;
    }
    
    private static final String BRACKETS = "[][][][][][][][][][][][][][][][][][][][]"; // NOI18N
    
    private ArrayType getArrayType(NewArrayTree arg0, Type type, int depth) {
        String arrayClassName;
        if (depth < BRACKETS.length()/2) {
            arrayClassName = type.name() + BRACKETS.substring(0, 2*depth);
        } else {
            arrayClassName = type.name() + BRACKETS;
            for (int i = BRACKETS.length()/2; i < depth; i++) {
                arrayClassName += "[]"; // NOI18N
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
        TreePath currentPath = getCurrentPath();
        TypeMirror cType;
        if (currentPath != null) {
            TreePath identifierPath = TreePath.getPath(currentPath, arg0);
            if (identifierPath == null) identifierPath = currentPath;
            Element elm = evaluationContext.getTrees().getElement(identifierPath);
            if (elm == null) {
                // Unresolved class
                Assert2.error(arg0, "unknownType", arg0.getIdentifier());
            }
            if (elm.asType().getKind() == TypeKind.ERROR) {
                cType = null;
            } else {
                if (elm.getKind() != ElementKind.CONSTRUCTOR) {
                    throw new IllegalStateException("Element "+elm+" is of "+elm.getKind()+" kind. Tree = "+arg0);
                }
                ExecutableElement cElem = (ExecutableElement) elm;
                cType = cElem.asType();
            }
        } else {
            cType = null;
        }
        ExpressionTree classIdentifier = arg0.getIdentifier();
        Mirror clazz = classIdentifier.accept(this, evaluationContext);
        ClassType classType = (ClassType) clazz;
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
        List<? extends TypeMirror> paramTypes = null;
        String firstParamSignature = null;
        List<Type> argTypes = null;
        if (cType != null) {
            paramTypes = ((ExecutableType) cType).getParameterTypes();
            ObjectReference thisObject = evaluationContext.getFrame().thisObject();
            if (thisObject != null) {
                List<ReferenceType> nestedTypes = ((ReferenceType) thisObject.type()).nestedTypes();
                for (ReferenceType nested : nestedTypes) {
                    if (!nested.isStatic() && nested.equals(classType)) {
                        argVals.add(0, thisObject);
                        firstParamSignature = thisObject.type().signature();
                    }
                }
            }
        } else {
            argTypes = new ArrayList<Type>(argVals.size());
            for (Value value : argVals) {
                if (value == null) {
                    argTypes.add(evaluationContext.getDebugger().getVirtualMachine().classesByName("java.lang.Object").get(0));
                } else {
                    argTypes.add(value.type());
                }
            }
            ObjectReference thisObject = evaluationContext.getFrame().thisObject();
            if (thisObject != null) {
                List<ReferenceType> nestedTypes = ((ReferenceType) thisObject.type()).nestedTypes();
                for (ReferenceType nested : nestedTypes) {
                    if (!nested.isStatic() && nested.equals(classType)) {
                        argVals.add(0, thisObject);
                        argTypes.add(0, thisObject.type());
                    }
                }
            }
        }
        try {
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("STARTED : "+classType+"."+"<init>"+" ("+argVals+") in thread "+evaluationContext.getFrame().thread());
            }
            evaluationContext.methodToBeInvoked();
            Method constructorMethod = getConcreteMethodAndReportProblems(arg0, classType, "<init>", firstParamSignature, paramTypes, argTypes);
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
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex);
            ieex.initCause(ex);
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

    private Value getEnumConstant(Tree arg0, VariableElement ve, EvaluationContext evaluationContext) {
        String constantName = ve.getSimpleName().toString();
        ReferenceType enumType = getClassType(arg0, ve.asType(), evaluationContext);
        Method valueOfMethod = enumType.methodsByName("valueOf").get(0);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) return null;
        StringReference constantNameRef = vm.mirrorOf(constantName);
        Value enumValue = invokeMethod(arg0, valueOfMethod, true, (ClassType) enumType, null,
                     Collections.singletonList((Value) constantNameRef), evaluationContext);
        return enumValue;
    }
    
    @Override
    public Mirror visitMemberSelect(MemberSelectTree arg0, EvaluationContext evaluationContext) {
        TreePath currentPath = getCurrentPath();
        Element elm = null;
        if (currentPath != null) {
            // We have the path and resolved elements
            TreePath memberSelectPath = TreePath.getPath(currentPath, arg0);
            if (memberSelectPath == null) memberSelectPath = currentPath;
            elm = evaluationContext.getTrees().getElement(memberSelectPath);
            if (elm instanceof TypeElement && ((TypeElement) elm).asType() instanceof ErrorType) {
                currentPath = null; // Elements not resolved correctly
            }
        }
        if (currentPath == null) {
            Mirror expression = arg0.getExpression().accept(this, evaluationContext);
            String name = arg0.getIdentifier().toString();
            // try field:
            if (expression instanceof ClassType) {
                ClassType clazz = (ClassType) expression;
                if (name.equals("this")) {
                    ObjectReference thisObject = evaluationContext.getFrame().thisObject();
                    while (thisObject != null && !((ReferenceType) thisObject.type()).equals(clazz)) {
                        ReferenceType thisClass = (ReferenceType) thisObject.type();
                        Field outerThisField = thisClass.fieldByName("this$0");
                        if (outerThisField != null) {
                            thisObject = (ObjectReference) thisObject.getValue(outerThisField);
                        } else {
                            thisObject = null;
                        }
                    }
                    if (thisObject == null) {
                        Assert2.error(arg0, "unknownOuterClass", clazz.name());
                    } else {
                        return thisObject;
                    }
                }
                if (name.equals("class")) {
                    return clazz.classObject();
                }
                Field f = clazz.fieldByName(name);
                if (f != null) {
                    return clazz.getValue(f);
                }
            } else if (expression instanceof InterfaceType) {
                if (name.equals("class")) {
                    return ((InterfaceType) expression).classObject();
                }
            } else if (expression instanceof ObjectReference) {
                if (expression instanceof ArrayReference && "length".equals(name)) {
                    return expression.virtualMachine().mirrorOf(((ArrayReference) expression).length());
                }
                ReferenceType type = ((ObjectReference) expression).referenceType();
                Field f = type.fieldByName(name);
                if (f != null) {
                    return ((ObjectReference) expression).getValue(f);
                }
            }
            if (expression == null) {
                Assert2.error(arg0, "fieldOnNull", name);
            }
            // try class
            VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
            if (vm == null) return null;
            List<ReferenceType> classes = vm.classesByName(name);
            if (classes.size() == 0) {
                Assert2.error(arg0, "unknownType", name);
            }
            return classes.get(0);
        }
        // We have the path and resolved elements
        switch(elm.getKind()) {
            case ENUM_CONSTANT:
                return getEnumConstant(arg0, (VariableElement) elm, evaluationContext);
            case FIELD:
                VariableElement ve = (VariableElement) elm;
                String fieldName = ve.getSimpleName().toString();
                Mirror expression = arg0.getExpression().accept(this, evaluationContext);
                if (expression instanceof ClassType) {
                    ClassType clazz = (ClassType) expression;
                    if (fieldName.equals("this")) {
                        ObjectReference thisObject = evaluationContext.getFrame().thisObject();
                        while (thisObject != null && !((ReferenceType) thisObject.type()).equals(clazz)) {
                            ReferenceType thisClass = (ReferenceType) thisObject.type();
                            Field outerThisField = thisClass.fieldByName("this$0");
                            if (outerThisField != null) {
                                thisObject = (ObjectReference) thisObject.getValue(outerThisField);
                            } else {
                                thisObject = null;
                            }
                        }
                        if (thisObject == null) {
                            Assert2.error(arg0, "unknownOuterClass", clazz.name());
                        } else {
                            return thisObject;
                        }
                    }
                    if (fieldName.equals("class")) {
                        return clazz.classObject();
                    }
                    Field f = clazz.fieldByName(fieldName);
                    if (!f.isStatic()) {
                        Assert2.error(arg0, "accessInstanceVariableFromStaticContext", fieldName);
                        return null;
                    }
                    if (f != null) {
                        return clazz.getValue(f);
                    } else {
                        Assert2.error(arg0, "unknownField", fieldName);
                        return null;
                    }
                }
                if (expression instanceof InterfaceType) {
                    InterfaceType intrfc = (InterfaceType) expression;
                    if (fieldName.equals("class")) {
                        return intrfc.classObject();
                    }
                    Field f = intrfc.fieldByName(fieldName);
                    if (f != null) {
                        return intrfc.getValue(f);
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
                if (expression == null) {
                    Assert2.error(arg0, "fieldOnNull", fieldName);
                }
                throw new IllegalArgumentException("Wrong expression value: "+expression);
            case CLASS:
            case INTERFACE:
                TypeElement te = (TypeElement) elm;
                String className = ElementUtilities.getBinaryName(te);
                VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
                if (vm == null) return null;
                List<ReferenceType> classes = vm.classesByName(className);
                if (classes.size() == 0) {
                    Assert2.error(arg0, "unknownType", className);
                }
                return classes.get(0);
            case PACKAGE:
                return (Value) Assert2.error(arg0, "notExpression");
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

    @Override
    public Mirror visitThrow(ThrowTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
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
            if (vm == null) return null;
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
        if (!instanceOf(((ObjectReference) expression).type(), (Type) type)) {
            Assert2.error(arg0, "castError", ((ObjectReference) expression).type(), type);
        }
        return expression;
    }

    @Override
    public Mirror visitPrimitiveType(PrimitiveTypeTree arg0, EvaluationContext evaluationContext) {
        TypeKind type = arg0.getPrimitiveTypeKind();
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) return null;
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

    @Override
    public Mirror visitTypeParameter(TypeParameterTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitInstanceOf(InstanceOfTree arg0, EvaluationContext evaluationContext) {
        Mirror expression = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (vm == null) return null;
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
        if (vm == null) return null;
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

    @Override
    public Mirror visitVariable(VariableTree arg0, EvaluationContext evaluationContext) {
        // Variable declaration
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitWhileLoop(WhileLoopTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitWildcard(WildcardTree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    @Override
    public Mirror visitOther(Tree arg0, EvaluationContext evaluationContext) {
        Assert2.error(arg0, "unsupported");
        return null;
    }

    private void setToMirror(Tree var, Value value, EvaluationContext evaluationContext) {
        VariableInfo varInfo = evaluationContext.getVariables().get(var);
        if (varInfo == null) {
            Assert2.error(var, "unknownVariable", var.toString());
            // EvaluationException will be thrown from the Assert
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
    
    private Value invokeMethod(Tree arg0, Method method, Boolean isStatic, ClassType type,
                               ObjectReference objectReference, List<Value> argVals,
                               EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert2.error(arg0, "calleeException", new UnsupportedOperationException(), evaluationContext);
        }
        ThreadReference evaluationThread = null;
        try {
            evaluationThread = evaluationContext.getFrame().thread();
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("STARTED : "+objectReference+"."+method+" ("+argVals+") in thread "+evaluationThread);
            }
            evaluationContext.methodToBeInvoked();
            Value value;
            autoboxArguments(method.argumentTypes(), argVals, evaluationThread);
            if (Boolean.TRUE.equals(isStatic)) {
                value = type.invokeMethod(evaluationThread, method, argVals,
                                          ObjectReference.INVOKE_SINGLE_THREADED);
            } else {
                ObjectReference object = objectReference;
                if (type != null) {
                    if (method.isPrivate()) {
                        object = findEnclosingObject(arg0, objectReference, type, null, method.name());
                    } else {
                        if (!instanceOf(objectReference.referenceType(), type)) {
                            object = findEnclosingObject(arg0, objectReference, type, null, method.name());
                        }
                    }
                }
                if (object == null) {
                    Assert2.error(arg0, "noSuchMethod", method.name(), objectReference.referenceType().name());
                }
                value = object.invokeMethod(evaluationThread, method,
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
        } catch (InvalidStackFrameException isfex) {
            InvalidExpressionException ieex = new InvalidExpressionException (isfex);
            ieex.initCause(isfex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex);
            ieex.initCause(ex);
            throw new IllegalStateException(iex.getLocalizedMessage(), ieex);
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
    
    /**
     * Auto-boxes or un-boxes arguments of a method.
     */
    private static void autoboxArguments(List<Type> types, List<Value> argVals,
                                         ThreadReference evaluationThread) throws InvalidTypeException,
                                                                                  ClassNotLoadedException,
                                                                                  IncompatibleThreadStateException,
                                                                                  InvocationException {
        if (types.size() != argVals.size()) {
            return ;
        }
        int n = types.size();
        for (int i = 0; i < n; i++) {
            Type t = types.get(i);
            Value v = argVals.get(i);
            if (v instanceof ObjectReference && t instanceof PrimitiveType) {
                argVals.set(i, unbox((ObjectReference) v, (PrimitiveType) t, evaluationThread));
            }
            if (v instanceof PrimitiveValue && t instanceof ReferenceType) {
                argVals.set(i, box((PrimitiveValue) v, (ReferenceType) t, evaluationThread));
            }
        }
    }
    
    /**
     * Auto-boxes or un-boxes elements of an array.
     */
    private void autoboxElements(Tree arg0, Type type, List<Value> elements,
                                 EvaluationContext evaluationContext) {
        boolean methodCalled = false;
        ThreadReference evaluationThread = null;
        try {
            if (type instanceof PrimitiveType) {
                for (int i = 0; i < elements.size(); i++) {
                    Value v = elements.get(i);
                    if (v instanceof ObjectReference) {
                        if (!methodCalled) {
                            if (!evaluationContext.canInvokeMethods()) {
                                Assert2.error(arg0, "calleeException", new UnsupportedOperationException(), evaluationContext);
                            }
                            evaluationThread = evaluationContext.getFrame().thread();
                            if (loggerMethod.isLoggable(Level.FINE)) {
                                loggerMethod.fine("STARTED : Unbox "+v+" in thread "+evaluationThread);
                            }
                            evaluationContext.methodToBeInvoked();
                            methodCalled = true;
                        }
                        elements.set(i, unbox((ObjectReference) v, (PrimitiveType) type, evaluationThread));
                    }
                }
            } else if (type instanceof ReferenceType) {
                for (int i = 0; i < elements.size(); i++) {
                    Value v = elements.get(i);
                    if (v instanceof PrimitiveValue) {
                        if (!methodCalled) {
                            if (!evaluationContext.canInvokeMethods()) {
                                Assert2.error(arg0, "calleeException", new UnsupportedOperationException(), evaluationContext);
                            }
                            evaluationThread = evaluationContext.getFrame().thread();
                            if (loggerMethod.isLoggable(Level.FINE)) {
                                loggerMethod.fine("STARTED : Autobox "+v+" in thread "+evaluationThread);
                            }
                            evaluationContext.methodToBeInvoked();
                            methodCalled = true;
                        }
                        elements.set(i, box((PrimitiveValue) v, (ReferenceType) type, evaluationThread));
                    }
                }
            }
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(new InvalidExpressionException (cnlex));
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            ieex.initCause(itsex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex);
            ieex.initCause(ex);
            throw new IllegalStateException(ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            ieex.initCause(uoex);
            throw new IllegalStateException(ieex);
        } catch (ObjectCollectedException ocex) {
            throw new IllegalStateException(new InvalidExpressionException(NbBundle.getMessage(
                Evaluator.class, "CTL_EvalError_collected")));
        } finally {
            if (methodCalled) {
                if (loggerMethod.isLoggable(Level.FINE)) {
                    loggerMethod.fine("FINISHED: Autobox/unbox in thread "+evaluationThread);
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
    
    private static void unboxMethodToBeCalled(Tree arg0, Mirror v, EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert2.error(arg0, "calleeException", new UnsupportedOperationException(), evaluationContext);
        }
        if (loggerMethod.isLoggable(Level.FINE)) {
            loggerMethod.fine("STARTED : Unbox "+v+" in thread "+evaluationContext.getFrame().thread());
        }
        evaluationContext.methodToBeInvoked();
    }
    
    private static Mirror unboxIfCan(Tree arg0, ObjectReference r, EvaluationContext evaluationContext) {
        String name = ((ReferenceType) r.type()).name();
        boolean methodCalled = false;
        try {
            if (name.equals(Boolean.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "booleanValue", evaluationContext.getFrame().thread());
            }
            if (name.equals(Byte.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "byteValue", evaluationContext.getFrame().thread());
            }
            if (name.equals(Character.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "charValue", evaluationContext.getFrame().thread());
            }
            if (name.equals(Short.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "shortValue", evaluationContext.getFrame().thread());
            }
            if (name.equals(Integer.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "intValue", evaluationContext.getFrame().thread());
            }
            if (name.equals(Long.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "longValue", evaluationContext.getFrame().thread());
            }
            if (name.equals(Float.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "floatValue", evaluationContext.getFrame().thread());
            }
            if (name.equals(Double.class.getName())) {
                unboxMethodToBeCalled(arg0, r, evaluationContext);
                methodCalled = true;
                return invokeUnboxingMethod(r, "doubleValue", evaluationContext.getFrame().thread());
            }
            return r;
        } catch (InvalidTypeException itex) {
            throw new IllegalStateException(new InvalidExpressionException (itex));
        } catch (ClassNotLoadedException cnlex) {
            throw new IllegalStateException(new InvalidExpressionException (cnlex));
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException ieex = new InvalidExpressionException (itsex);
            ieex.initCause(itsex);
            throw new IllegalStateException(ieex);
        } catch (InvocationException iex) {
            Throwable ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            InvalidExpressionException ieex = new InvalidExpressionException (ex);
            ieex.initCause(ex);
            throw new IllegalStateException(ieex);
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            ieex.initCause(uoex);
            throw new IllegalStateException(ieex);
        } catch (ObjectCollectedException ocex) {
            throw new IllegalStateException(new InvalidExpressionException(NbBundle.getMessage(
                Evaluator.class, "CTL_EvalError_collected")));
        } finally {
            if (methodCalled) {
                if (loggerMethod.isLoggable(Level.FINE)) {
                    loggerMethod.fine("FINISHED: unbox in thread "+evaluationContext.getFrame().thread());
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

    public static PrimitiveValue unbox(ObjectReference val, PrimitiveType type,
                                        ThreadReference thread) throws InvalidTypeException,
                                                                       ClassNotLoadedException,
                                                                       IncompatibleThreadStateException,
                                                                       InvocationException {
        if (type instanceof BooleanType) return invokeUnboxingMethod(val, "booleanValue", thread);
        if (type instanceof ByteType) return invokeUnboxingMethod(val, "byteValue", thread);
        if (type instanceof CharType) return invokeUnboxingMethod(val, "charValue", thread);
        if (type instanceof ShortType) return invokeUnboxingMethod(val, "shortValue", thread);
        if (type instanceof IntegerType) return invokeUnboxingMethod(val, "intValue", thread);
        if (type instanceof LongType) return invokeUnboxingMethod(val, "longValue", thread);
        if (type instanceof FloatType) return invokeUnboxingMethod(val, "floatValue", thread);
        if (type instanceof DoubleType) return invokeUnboxingMethod(val, "doubleValue", thread);
        throw new RuntimeException("Invalid type while unboxing: " + type.signature());    // never happens
    }

    private static ReferenceType adjustBoxingType(ReferenceType type, PrimitiveType primitiveType) {
        if (primitiveType instanceof BooleanType) {
            type = type.virtualMachine().classesByName(Boolean.class.getName()).get(0);
        } else
        if (primitiveType instanceof ByteType) {
            type = type.virtualMachine().classesByName(Byte.class.getName()).get(0);
        } else
        if (primitiveType instanceof CharType) {
            type = type.virtualMachine().classesByName(Character.class.getName()).get(0);
        } else
        if (primitiveType instanceof ShortType) {
            type = type.virtualMachine().classesByName(Short.class.getName()).get(0);
        } else
        if (primitiveType instanceof IntegerType) {
            type = type.virtualMachine().classesByName(Integer.class.getName()).get(0);
        } else
        if (primitiveType instanceof LongType) {
            type = type.virtualMachine().classesByName(Long.class.getName()).get(0);
        } else
        if (primitiveType instanceof FloatType) {
            type = type.virtualMachine().classesByName(Float.class.getName()).get(0);
        } else
        if (primitiveType instanceof DoubleType) {
            type = type.virtualMachine().classesByName(Double.class.getName()).get(0);
        }
        return type;
    }
    
    public static ObjectReference box(PrimitiveValue v, ReferenceType type,
                                       ThreadReference thread) throws InvalidTypeException,
                                                                      ClassNotLoadedException,
                                                                      IncompatibleThreadStateException,
                                                                      InvocationException {
        try {
            Method constructor = null;
            type = adjustBoxingType(type, (PrimitiveType) v.type());
            List<Method> methods = type.methodsByName("<init>");
            String signature = "("+v.type().signature()+")";
            for (Method method : methods) {
                if (!method.isAbstract() && egualMethodSignatures(method.signature(), signature)) {
                    constructor = method;
                }
            }
            if (constructor == null) {
                throw new RuntimeException("No constructor "+type+" "+signature);
            }
            return ((ClassType) type).newInstance(thread, constructor, Arrays.asList(new Value[] { v }), ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (InvalidTypeException itex) {
            throw itex;
        } catch (ClassNotLoadedException cnlex) {
            throw cnlex;
        } catch (IncompatibleThreadStateException itsex) {
            throw itsex;
        } catch (InvocationException iex) {
            throw iex;
        } catch (Exception e) {
            // this should never happen, indicates an internal error
            throw new RuntimeException("Unexpected exception while invoking boxing method", e);
        }
    }

    private static PrimitiveValue invokeUnboxingMethod(ObjectReference reference, String methodName,
                                                       ThreadReference thread) throws InvalidTypeException,
                                                                                      ClassNotLoadedException,
                                                                                      IncompatibleThreadStateException,
                                                                                      InvocationException {
        Method toCall = (Method) reference.referenceType().methodsByName(methodName).get(0);
        try {
            return (PrimitiveValue) reference.invokeMethod(thread, toCall, new ArrayList<Value>(0), ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (InvalidTypeException itex) {
            throw itex;
        } catch (ClassNotLoadedException cnlex) {
            throw cnlex;
        } catch (IncompatibleThreadStateException itsex) {
            throw itsex;
        } catch (InvocationException iex) {
            throw iex;
        } catch (Exception e) {
            // this should never happen, indicates an internal error
            throw new RuntimeException("Unexpected exception while invoking unboxing method", e);
        }
    }

    private String toString(Tree arg0, Mirror v, EvaluationContext evaluationContext) {
        if (v instanceof PrimitiveValue) {
            PrimitiveValue pv = (PrimitiveValue) v;
            PrimitiveType t = (PrimitiveType) pv.type();
            if (t instanceof ByteType) {
                return Byte.toString(pv.byteValue());
            }
            if (t instanceof BooleanType) {
                return Boolean.toString(pv.booleanValue());
            }
            if (t instanceof CharType) {
                return Character.toString(pv.charValue());
            }
            if (t instanceof ShortType) {
                return Short.toString(pv.shortValue());
            }
            if (t instanceof IntegerType) {
                return Integer.toString(pv.intValue());
            }
            if (t instanceof LongType) {
                return Long.toString(pv.longValue());
            }
            if (t instanceof FloatType) {
                return Float.toString(pv.floatValue());
            }
            if (t instanceof DoubleType) {
                return Double.toString(pv.doubleValue());
            }
            throw new IllegalStateException("Unknown primitive type: "+t);
        }
        if (v == null) {
            return ""+null;
        }
        ObjectReference ov = (ObjectReference) v;
        // Call toString() method:
        List<? extends TypeMirror> typeArguments = Collections.emptyList();
        Method method;
        try {
            method = getConcreteMethod((ReferenceType) ov.type(), "toString", typeArguments);
        } catch (UnsuitableArgumentsException uaex) {
            throw new IllegalStateException(uaex);
        }
        ((ClassType) ov.type()).methodsByName("toString");
        List<Value> argVals = Collections.emptyList();
        Value sv = invokeMethod(arg0, method, false, null, ov, argVals, evaluationContext);
        if (sv instanceof StringReference) {
            return ((StringReference) sv).value();
        } else {
            throw new IllegalStateException("Result of toString() call on "+ov+" is not a String, but: "+sv);
        }
    }
    
    private static final class UnsuitableArgumentsException extends Exception {
        public UnsuitableArgumentsException() {}
    }
    
}
