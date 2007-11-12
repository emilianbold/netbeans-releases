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
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteType;
import com.sun.jdi.CharType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleType;
import com.sun.jdi.Field;
import com.sun.jdi.FloatType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongType;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
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
import com.sun.source.tree.TreeVisitor;
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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.java.source.ElementUtilities;
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
    
    public EvaluatorVisitor() {
    }

    public Mirror visitAnnotation(AnnotationTree arg0, EvaluationContext evaluationContext) {
        return null;
    }

    public Mirror visitMethodInvocation(MethodInvocationTree arg0, EvaluationContext evaluationContext) {
        if (!evaluationContext.canInvokeMethods()) {
            Assert2.error(arg0, "calleeException", new UnsupportedOperationException(), evaluationContext);
        }
        if (loggerMethod.isLoggable(Level.FINE)) {
            loggerMethod.fine("STARTED : "+arg0+" in thread "+evaluationContext.getFrame().thread());
        }
        TreePath methodInvokePath = TreePath.getPath(getCurrentPath(), arg0);
        if (methodInvokePath == null) methodInvokePath = getCurrentPath();
        Element elm = evaluationContext.getTrees().getElement(methodInvokePath);
        TypeMirror typeMirror = elm.asType();
        TypeKind kind = typeMirror.getKind();
        if (kind != TypeKind.EXECUTABLE) {
            Assert2.error(arg0, "noSuchMethod", elm.getSimpleName().toString(), elm.getEnclosingElement().getSimpleName().toString());
        }
        ExecutableElement methodElement = (ExecutableElement) elm;
        ExecutableType execTypeMirror = (ExecutableType) typeMirror;
        String methodName = methodElement.getSimpleName().toString();
        
        List<? extends ExpressionTree> args = arg0.getArguments();
        List<Value> argVals = new ArrayList<Value>(args.size());
        for (ExpressionTree arg : args) {
            Mirror argValue = arg.accept(this, evaluationContext);
            if (!(argValue instanceof Value)) {
                Assert2.error(arg, "Not a value");
            }
            argVals.add((Value) argValue);
        }
        ExpressionTree identifier = arg0.getMethodSelect();
        //String methodName;
        Mirror object = null;
        if (identifier.getKind() == Tree.Kind.MEMBER_SELECT) {
            //methodName = ((IdentifierTree) identifier).getName().toString();
            // or
            //methodName = ((MemberSelectTree) identifier).getIdentifier().toString();
            object = ((MemberSelectTree) identifier).getExpression().accept(this, evaluationContext);
        }
        ObjectReference objectReference;
        ClassType type;
        boolean isStatic = methodElement.getModifiers().contains(Modifier.STATIC);
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
        /*
        System.err.println("TreePath = "+evaluationContext.getTreePath());
        TreePath tp = evaluationContext.getTreePath();
        for (java.util.Iterator<Tree> it = tp.iterator(); it.hasNext(); ) {
            System.err.println("  element: '"+it.next()+"'");
        }
         */
        /*
        System.err.println("MIT = "+arg0);
        System.err.println("methodInvokePath = "+methodInvokePath);
        Element elm = evaluationContext.getTrees().getElement(methodInvokePath);
        TypeMirror typeMirror = evaluationContext.getTrees().getTypeMirror(methodInvokePath);
        System.err.println("TypeMirror = "+typeMirror+", kind = "+typeMirror.getKind()+", toString() = "+typeMirror.toString());
        typeMirror = elm.asType();
        System.err.println("TypeMirror = "+typeMirror+", kind = "+typeMirror.getKind()+", toString() = "+typeMirror.toString());
         */
        
        System.err.println("Element = "+elm+", kind = "+elm.getKind()+", name = "+elm.getSimpleName());
        Method method = getConcreteMethod(type, methodName, execTypeMirror.getParameterTypes());
        if (method == null) {
            Assert2.error(arg0, "noSuchMethod", type.name(), methodName);
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
            String paramType = param.toString();//getSimpleName().toString();
            signature.append(getSignature(paramType));
        }
        signature.append(')');
        //String returnType = elm.getReturnType().toString();
        //signature.append(getSignature(returnType));
        return signature.toString();
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

    
    public Mirror visitAssert(AssertTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitAssignment(AssignmentTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitCompoundAssignment(CompoundAssignmentTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitBinary(BinaryTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitBlock(BlockTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitBreak(BreakTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitCase(CaseTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitCatch(CatchTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitClass(ClassTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitConditionalExpression(ConditionalExpressionTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitContinue(ContinueTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitDoWhileLoop(DoWhileLoopTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitErroneous(ErroneousTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitExpressionStatement(ExpressionStatementTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitEnhancedForLoop(EnhancedForLoopTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitForLoop(ForLoopTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

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
                    return field.declaringType().getValue(field);
                }
                ObjectReference thisObject = evaluationContext.getFrame().thisObject();
                if (thisObject != null) {
                    return thisObject.getValue(field);
                } else {
                    throw new IllegalStateException("No current instance available.");
                }
            case LOCAL_VARIABLE:
                ve = (VariableElement) elm;
                String varName = ve.getSimpleName().toString();
                try {
                    LocalVariable lv = evaluationContext.getFrame().visibleVariableByName(varName);
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

    public Mirror visitIf(IfTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitImport(ImportTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitArrayAccess(ArrayAccessTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitLabeledStatement(LabeledStatementTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitLiteral(LiteralTree arg0, EvaluationContext evaluationContext) {
        Object value = arg0.getValue();
        if (value instanceof Boolean) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Boolean) value).booleanValue());
        }
        if (value instanceof Byte) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Byte) value).byteValue());
        }
        if (value instanceof Character) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Character) value).charValue());
        }
        if (value instanceof Double) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Double) value).doubleValue());
        }
        if (value instanceof Float) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Float) value).floatValue());
        }
        if (value instanceof Integer) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Integer) value).intValue());
        }
        if (value instanceof Long) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Long) value).longValue());
        }
        if (value instanceof Short) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf(((Short) value).shortValue());
        }
        if (value instanceof String) {
            return evaluationContext.getDebugger().getVirtualMachine().mirrorOf((String) value);
        }
        throw new UnsupportedOperationException("Unsupported value: "+value);
    }

    public Mirror visitMethod(MethodTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitModifiers(ModifiersTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitNewArray(NewArrayTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitNewClass(NewClassTree arg0, EvaluationContext evaluationContext) {
        TreePath identifierPath = TreePath.getPath(getCurrentPath(), arg0);
        if (identifierPath == null) identifierPath = getCurrentPath();
        Element elm = evaluationContext.getTrees().getElement(identifierPath);
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
    
    public Mirror visitParenthesized(ParenthesizedTree arg0, EvaluationContext evaluationContext) {
        return arg0.getExpression().accept(this, evaluationContext);
    }

    public Mirror visitReturn(ReturnTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

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
                try {
                    if (loggerMethod.isLoggable(Level.FINE)) {
                        loggerMethod.fine("STARTED : "+enumType+"."+valueOfMethod+" ("+constantNameRef+") in thread "+evaluationContext.getFrame().thread());
                    }
                    evaluationContext.methodToBeInvoked();
                    Value enumValue =
                            ((ClassType) enumType).invokeMethod(evaluationContext.getFrame().thread(),
                                                                valueOfMethod,
                                                                Collections.singletonList(constantNameRef),
                                                                ObjectReference.INVOKE_SINGLE_THREADED);
                    return enumValue;
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
                        loggerMethod.fine("FINISHED: "+enumType+"."+valueOfMethod+" ("+constantNameRef+") in thread "+evaluationContext.getFrame().thread());
                    }
                }
                
        }
        arg0.getExpression();
        String name = arg0.getIdentifier().toString();
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitEmptyStatement(EmptyStatementTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitSwitch(SwitchTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitSynchronized(SynchronizedTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitThrow(ThrowTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitCompilationUnit(CompilationUnitTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitTry(TryTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitParameterizedType(ParameterizedTypeTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitArrayType(ArrayTypeTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

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

    public Mirror visitPrimitiveType(PrimitiveTypeTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitTypeParameter(TypeParameterTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitInstanceOf(InstanceOfTree arg0, EvaluationContext evaluationContext) {
        Mirror expression = arg0.getExpression().accept(this, evaluationContext);
        VirtualMachine vm = evaluationContext.getDebugger().getVirtualMachine();
        if (expression == null) return vm.mirrorOf(false);
        Assert2.assertAssignable(expression, ObjectReference.class, arg0, "instanceOfLeftOperandNotAReference", expression);

        ReferenceType expressionType = ((ObjectReference) expression).referenceType();
        Type type = (Type) arg0.getType().accept(this, evaluationContext);

        return vm.mirrorOf(instanceOf(expressionType, type));
    }

    public Mirror visitUnary(UnaryTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitVariable(VariableTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitWhileLoop(WhileLoopTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitWildcard(WildcardTree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

    public Mirror visitOther(Tree arg0, EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("Not supported yet."+" Tree = '"+arg0+"'");
    }

}
