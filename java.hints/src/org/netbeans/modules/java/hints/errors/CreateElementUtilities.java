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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Lahoda
 */
public final class CreateElementUtilities {
    
    private CreateElementUtilities() {}
    
    public static List<? extends TypeMirror> resolveType(Set<ElementKind> types, CompilationInfo info, TreePath currentPath, Tree unresolved, int offset, TypeMirror[] typeParameterBound, int[] numTypeParameters) {
        switch (currentPath.getLeaf().getKind()) {
            case METHOD:
                return computeMethod(types, info, currentPath, unresolved, offset);
            case MEMBER_SELECT:
                return computeMemberSelect(types, info, currentPath, unresolved, offset);
            case ASSIGNMENT:
                return computeAssignment(types, info, currentPath, unresolved, offset);
            case ENHANCED_FOR_LOOP:
                return computeEnhancedForLoop(types, info, currentPath, unresolved, offset);
            case ARRAY_ACCESS:
                return computeArrayAccess(types, info, currentPath, unresolved, offset);
            case VARIABLE:
                return computeVariableDeclaration(types, info, currentPath, unresolved, offset);
            case ASSERT:
                return computeAssert(types, info, currentPath, unresolved, offset);
            case PARENTHESIZED:
                return computeParenthesis(types, info, currentPath, unresolved, offset);            
            case DO_WHILE_LOOP:
                return computePrimitiveType(types, info, ((DoWhileLoopTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case FOR_LOOP:
                return computePrimitiveType(types, info, ((ForLoopTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case IF:
                return computePrimitiveType(types, info, ((IfTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case WHILE_LOOP:
                return computePrimitiveType(types, info, ((WhileLoopTree) currentPath.getLeaf()).getCondition(), unresolved, TypeKind.BOOLEAN);
            case SYNCHRONIZED:
                return computeReferenceType(types, info, ((SynchronizedTree) currentPath.getLeaf()).getExpression(), unresolved, "java.lang.Object");
            case THROW:
                return computeReferenceType(types, info, ((ThrowTree) currentPath.getLeaf()).getExpression(), unresolved, "java.lang.Exception");
            case INSTANCE_OF:
                return computeReferenceType(types, info, ((InstanceOfTree) currentPath.getLeaf()).getExpression(), unresolved, "java.lang.Object");
            case SWITCH:
                //TODO: should consider also values in the cases?:
                return computePrimitiveType(types, info, ((SwitchTree) currentPath.getLeaf()).getExpression(), unresolved, TypeKind.INT);
            case EXPRESSION_STATEMENT:
                return Collections.singletonList(info.getTypes().getNoType(TypeKind.VOID));

            case RETURN:
                return computeReturn(types, info, currentPath, unresolved, offset);
            case TYPE_PARAMETER:
                return computeTypeParameter(types, info, currentPath, unresolved, offset);
            case PARAMETERIZED_TYPE:
                return computeParametrizedType(types, info, currentPath, unresolved, offset, typeParameterBound, numTypeParameters);
            case CLASS:
                return computeClass(types, info, currentPath, unresolved, offset);
                
            case CONDITIONAL_EXPRESSION:
                return computeConditionalExpression(types, info, currentPath, unresolved, offset);
                
            case NEW_ARRAY:
                return computeNewArray(types, info, currentPath, unresolved, offset);
                
            case POSTFIX_INCREMENT:
            case POSTFIX_DECREMENT:
            case PREFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case UNARY_PLUS:
            case UNARY_MINUS:
            case BITWISE_COMPLEMENT:
            case LOGICAL_COMPLEMENT:
                return computeUnary(types, info, currentPath, unresolved, offset);

            case MULTIPLY:
            case DIVIDE:
            case REMAINDER:
            case PLUS:
            case MINUS:
            case LEFT_SHIFT:
            case RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_EQUAL:
            case GREATER_THAN_EQUAL:
            case EQUAL_TO:
            case NOT_EQUAL_TO:
            case AND:
            case XOR:
            case OR:
            case CONDITIONAL_AND:
            case CONDITIONAL_OR:
                return computeBinaryOperator(types, info, currentPath, unresolved, offset);
                
            case MULTIPLY_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
            case REMAINDER_ASSIGNMENT:
            case PLUS_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case LEFT_SHIFT_ASSIGNMENT:
            case RIGHT_SHIFT_ASSIGNMENT:
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            case AND_ASSIGNMENT:
            case XOR_ASSIGNMENT:
            case OR_ASSIGNMENT:
                //XXX: return computeCompoundAssignment(types, info, currentPath, unresolved, offset);
                return null;
                        
            case ARRAY_TYPE:
            case BLOCK:
            case BREAK:
            case CATCH:
            case COMPILATION_UNIT:
            case CONTINUE:
            case IMPORT:
            case IDENTIFIER:
            case TYPE_CAST:
            case TRY:
            case EMPTY_STATEMENT:
            case PRIMITIVE_TYPE:
            case LABELED_STATEMENT:
            case MODIFIERS:
            case ERRONEOUS:
            case OTHER:
            case INT_LITERAL:
            case LONG_LITERAL:
            case FLOAT_LITERAL:
            case DOUBLE_LITERAL:
            case BOOLEAN_LITERAL:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case NULL_LITERAL:
                //ignored:
                return null;
                
            case CASE:
            case ANNOTATION:
            case NEW_CLASS:
            case UNBOUNDED_WILDCARD:
            case EXTENDS_WILDCARD:
            case SUPER_WILDCARD:
                //XXX: currently unhandled
                return null;
                
            default:
                //should not happen unless set of Tree.Kind changes:
                return null;
        }
    }
    
    private static List<? extends TypeMirror> computeBinaryOperator(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        BinaryTree bt = (BinaryTree) parent.getLeaf();
        TreePath typeToResolve = null;
        
        if (bt.getLeftOperand() == error) {
            typeToResolve = new TreePath(parent, bt.getRightOperand());
        }
        
        if (bt.getRightOperand() == error) {
            typeToResolve = new TreePath(parent, bt.getLeftOperand());
        }
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);
        
        return typeToResolve != null ? Collections.singletonList(info.getTrees().getTypeMirror(typeToResolve)) : null;
    }
    
    private static List<? extends TypeMirror> computeMethod(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        //class or field:
        //check the error is in the body:
        //#92419: check for abstract method/method without body:
        MethodTree mt = (MethodTree) parent.getLeaf();
        
        if (mt.getBody() == null) {
            return null;
        }
        
        try {
            Document doc = info.getDocument();
            
            if (doc != null) {//XXX
                int bodyStart = Utilities.findBodyStart(parent.getLeaf(), info.getCompilationUnit(), info.getTrees().getSourcePositions(), doc);
                int bodyEnd   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), parent.getLeaf());

                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);

                if (bodyStart <= offset && offset <= bodyEnd)
                    return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
            }
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, ex.getMessage(), ex);
        }
        
        if (mt.getReturnType() == error) {
            types.add(ElementKind.CLASS);
            types.add(ElementKind.INTERFACE);
            types.add(ElementKind.ENUM);
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeMemberSelect(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        //class or field:
        MemberSelectTree ms = (MemberSelectTree) parent.getLeaf();
        final TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
        
        if (   jlObject != null //may happen if the platform is broken
            && !"class".equals(ms.getIdentifier().toString())) {//we obviously should not propose "Create Field" for unknown.class:
            types.add(ElementKind.FIELD);
            types.add(ElementKind.CLASS);
            return Collections.singletonList(jlObject.asType());
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeAssignment(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        AssignmentTree at = (AssignmentTree) parent.getLeaf();
        TypeMirror     type = null;
        
        if (at.getVariable() == error) {
            type = info.getTrees().getTypeMirror(new TreePath(parent, at.getExpression()));
            
            if (type.getKind() == TypeKind.EXECUTABLE) {
                //TODO: does not actualy work, attempt to solve situations like:
                //t = Collections.emptyList()
                //t = Collections.<String>emptyList();
                //see also testCreateFieldMethod1 and testCreateFieldMethod2 tests:
                type = ((ExecutableType) type).getReturnType();
            }
        }
        
        if (at.getExpression() == error) {
            type = info.getTrees().getTypeMirror(new TreePath(parent, at.getVariable()));
        }
        
        //class or field:
        if (type == null) {
            if (ErrorHintsProvider.ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "offset=" + offset);
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "errorTree=" + error);
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "type=null");
            }
            
            return null;
        }
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);
        
        return Collections.singletonList(type);
    }
    
    private static List<? extends TypeMirror> computeEnhancedForLoop(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        EnhancedForLoopTree efl = (EnhancedForLoopTree) parent.getLeaf();
        
        if (efl.getExpression() != error) {
            return null;
        }
                        
        TypeMirror argument = info.getTrees().getTypeMirror(new TreePath(new TreePath(parent, efl.getVariable()), efl.getVariable().getType()));
        
        if (argument == null)
            return null;
        
        if (argument.getKind().isPrimitive()) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);

            return Collections.singletonList(info.getTypes().getArrayType(argument));
        }
        
        TypeElement iterable = info.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
        if (iterable == null) {
            return null;
        }
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);
        
        return Collections.singletonList(info.getTypes().getDeclaredType(iterable, argument));
    }
    
    private static List<? extends TypeMirror> computeArrayAccess(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ArrayAccessTree aat = (ArrayAccessTree) parent.getLeaf();
        
        if (aat.getExpression() == error) {
            TreePath parentParent = parent.getParentPath();
            List<? extends TypeMirror> upperTypes = resolveType(types, info, parentParent, aat, offset, null, null);
            
            if (upperTypes == null) {
                return null;
            }
            
            List<TypeMirror> arrayTypes = new ArrayList<TypeMirror>();
            
            for (TypeMirror tm : upperTypes) {
                if (tm == null)
                    continue;
                switch (tm.getKind()) {
                    case VOID:
                    case EXECUTABLE:
                    case WILDCARD:
                    case PACKAGE:
                        continue;
                }
                
                arrayTypes.add(info.getTypes().getArrayType(tm));
            }
            
            if (arrayTypes.isEmpty())
                return null;
            
            return arrayTypes;
        }
        
        if (aat.getIndex() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeVariableDeclaration(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        VariableTree vt = (VariableTree) parent.getLeaf();
        
        if (vt.getInitializer() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTrees().getTypeMirror(new TreePath(parent, vt.getType())));
        }
        
        if (vt.getType() == error) {
            types.add(ElementKind.CLASS);
            return Collections.<TypeMirror>emptyList();
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeAssert(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        AssertTree at = (AssertTree) parent.getLeaf();
        
        types.add(ElementKind.PARAMETER);
        types.add(ElementKind.LOCAL_VARIABLE);
        types.add(ElementKind.FIELD);
        
        if (at.getCondition() == error) {
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.BOOLEAN));
        }
        
        if (at.getDetail() == error) {
            return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
        }
        
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeParenthesis(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ParenthesizedTree pt = (ParenthesizedTree) parent.getLeaf();
        
        if (pt.getExpression() != error) {
            return null;
        }
        
        TreePath parentParent = parent.getParentPath();
        List<? extends TypeMirror> upperTypes = resolveType(types, info, parentParent, pt, offset, null, null);
        
        if (upperTypes == null) {
            return null;
        }
        
        return upperTypes;
    }
    
    private static List<? extends TypeMirror> computeConditionalExpression(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ConditionalExpressionTree cet = (ConditionalExpressionTree) parent.getLeaf();
        
        if (cet.getCondition() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.BOOLEAN));
        }
        
        if (cet.getTrueExpression() == error || cet.getFalseExpression() == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return resolveType(types, info, parent.getParentPath(), cet, offset, null, null);
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computePrimitiveType(Set<ElementKind> types, CompilationInfo info, Tree expression, Tree error, TypeKind kind) {
        if (expression == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(kind));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeReferenceType(Set<ElementKind> types, CompilationInfo info, Tree expression, Tree error, String type) {
        if (expression == error) {
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(info.getElements().getTypeElement(type).asType());
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeUnary(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        UnaryTree tree = (UnaryTree) parent.getLeaf();
        
        if (tree.getExpression() == error) {
            List<? extends TypeMirror> parentTypes = resolveType(types, info, parent.getParentPath(), tree, offset, null, null);
            
            if (parentTypes != null) {
                //may contain only "void", ignore:
                if (parentTypes.size() != 1) {
                    return parentTypes;
                }
                if (parentTypes.get(0).getKind() != TypeKind.VOID) {
                    return parentTypes;
                }
            }
            
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);

            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeReturn(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ReturnTree rt = (ReturnTree) parent.getLeaf();
        
        if (rt.getExpression() == error) {
            TreePath method = findMethod(parent);
            
            if (method == null) {
                return null;
            }
            
            Element el = info.getTrees().getElement(method);
            
            if (el == null || el.getKind() != ElementKind.METHOD) {
                return null;
            }
            
            types.add(ElementKind.PARAMETER);
            types.add(ElementKind.LOCAL_VARIABLE);
            types.add(ElementKind.FIELD);
            
            return Collections.singletonList(((ExecutableElement) el).getReturnType());
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeTypeParameter(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        TypeParameterTree tpt = (TypeParameterTree) parent.getLeaf();
        
        for (Tree t : tpt.getBounds()) {
            if (t == error) {
                types.add(ElementKind.CLASS); //XXX: class/interface/enum/annotation?
                return null;
            }
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeParametrizedType(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset, TypeMirror[] typeParameterBound, int[] numTypeParameters) {
        ParameterizedTypeTree ptt = (ParameterizedTypeTree) parent.getLeaf();
        
        if (ptt.getType() == error) {
            types.add(ElementKind.CLASS);
            types.add(ElementKind.INTERFACE);
            
            if (numTypeParameters != null) {
                numTypeParameters[0] = ptt.getTypeArguments().size();
            }
            return null;
        }
        
        TypeMirror resolved = info.getTrees().getTypeMirror(parent);
        DeclaredType resolvedDT = null;
        
        if (resolved != null && resolved.getKind() == TypeKind.DECLARED) {
            resolvedDT = (DeclaredType) resolved;
        }
        
        int index = 0;
        
        for (Tree t : ptt.getTypeArguments()) {
            if (t == error) {
                if (resolvedDT != null && typeParameterBound != null) {
                    List<? extends TypeMirror> typeArguments = ((DeclaredType) resolvedDT.asElement().asType()).getTypeArguments();
                    
                    if (typeArguments.size() > index) {
                        typeParameterBound[0] = ((TypeVariable) typeArguments.get(index)).getUpperBound();
                    }
                }
                
                types.add(ElementKind.CLASS); //XXX: class/interface/enum/annotation?
                return null;
            }
            
            index++;
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeClass(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ClassTree ct = (ClassTree) parent.getLeaf();
        
        if (ct.getExtendsClause() == error) {
            types.add(ElementKind.CLASS);
            return null;
        }
        
        for (Tree t : ct.getImplementsClause()) {
            if (t == error) {
                types.add(ElementKind.INTERFACE);
                return null;
            }
        }
        
        //XXX: annotation types...
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeNewArray(Set<ElementKind> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        NewArrayTree nat = (NewArrayTree) parent.getLeaf();
        
        if (nat.getType() == error) {
            types.add(ElementKind.CLASS);
            types.add(ElementKind.ENUM);
            types.add(ElementKind.INTERFACE);
            
            return null;
        }
        
        for (Tree dimension : nat.getDimensions()) {
            if (dimension == error) {
                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);
                
                return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
            }
        }
        
        for (Tree init : nat.getInitializers()) {
            if (init == error) {
                TypeMirror whole = info.getTrees().getTypeMirror(parent);
                
                if (whole == null || whole.getKind() != TypeKind.ARRAY)
                    return null;
                
                types.add(ElementKind.PARAMETER);
                types.add(ElementKind.LOCAL_VARIABLE);
                types.add(ElementKind.FIELD);
                
                return Collections.singletonList(((ArrayType) whole).getComponentType());
            }
        }
        
        return null;
    }
    
    private static final Set<Kind> STOP_LOOKING_FOR_METHOD = EnumSet.of(Kind.METHOD, Kind.CLASS, Kind.COMPILATION_UNIT);
            
    private static TreePath findMethod(TreePath tp) {
        while (!STOP_LOOKING_FOR_METHOD.contains(tp.getLeaf().getKind())) {
            tp = tp.getParentPath();
        }
        
        if (tp.getLeaf().getKind() == Kind.METHOD) {
            return tp;
        }
        
        return null;
    }
    
}
