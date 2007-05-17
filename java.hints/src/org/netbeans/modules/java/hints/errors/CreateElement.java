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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;

/**
 *
 * @author Jan Lahoda
 */
public final class CreateElement implements ErrorRule<Void> {
    
    /** Creates a new instance of CreateElement */
    public CreateElement() {
    }
    
    public Set<String> getCodes() {
        return Collections.singleton("compiler.err.cant.resolve.location");
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        return analyze(info, offset);
    }
    
    static List<Fix> analyze(CompilationInfo info, int offset) {
        TreePath errorPath = ErrorHintsProvider.findUnresolvedElement(info, offset);
        
        if (errorPath == null) {
            return Collections.<Fix>emptyList();
        }
        
        TreePath parent = null;
        TreePath firstClass = null;
        TreePath firstMethod = null;
        TreePath firstInitializer = null;
        
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        while(path != null) {
            if (parent != null && parent.getLeaf() == errorPath.getLeaf())
                parent = path;
            if (path.getLeaf() == errorPath.getLeaf() && parent == null)
                parent = path;
            if (path.getLeaf().getKind() == Kind.CLASS && firstClass == null)
                firstClass = path;
            if (path.getLeaf().getKind() == Kind.METHOD && firstMethod == null && firstClass == null)
                firstMethod = path;
            //static/dynamic initializer:
            if (   path.getLeaf().getKind() == Kind.BLOCK && path.getParentPath().getLeaf().getKind() == Kind.CLASS
                && firstMethod == null && firstClass == null)
                firstInitializer = path;
            path = path.getParentPath();
        }
        
        if (parent == null || parent.getLeaf() == errorPath.getLeaf() || firstClass == null)
            return Collections.<Fix>emptyList();
        
        Element e = info.getTrees().getElement(errorPath);
        
        if (e == null) {
            return Collections.<Fix>emptyList();
        }
        
        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        String simpleName = e.getSimpleName().toString();
        TypeElement source = (TypeElement) info.getTrees().getElement(firstClass);
        TypeElement target = null;
        boolean allowLocalVariables = true;
        
        if (errorPath.getLeaf().getKind() == Kind.MEMBER_SELECT) {
            TreePath exp = new TreePath(errorPath, ((MemberSelectTree) errorPath.getLeaf()).getExpression());
            Element targetElement = info.getTrees().getElement(exp);
            TypeMirror targetType = info.getTrees().getTypeMirror(exp);
            
            if (targetElement != null && targetType != null && targetType.getKind() != TypeKind.ERROR) {
                switch (targetElement.getKind()) {
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case ANNOTATION_TYPE:
                        //situation like <something>.ClassName.<identifier>,
                        //targetElement representing <something>.ClassName:
                        //the new element needs to be static
                        target = (TypeElement) targetElement;
                        modifiers.add(Modifier.STATIC);
                        break;
                        
                    case FIELD:
                    case ENUM_CONSTANT:
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                    case EXCEPTION_PARAMETER:
                        TypeMirror tm = targetElement.asType();
                        if (tm.getKind() == TypeKind.DECLARED) {
                            target = (TypeElement)((DeclaredType)tm).asElement();
                        }
                        break;
                    case METHOD:
                        Element el = info.getTypes().asElement(((ExecutableElement) targetElement).getReturnType());
                        
                        if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            target = (TypeElement) el;
                        }
                        
                        break;
                    case CONSTRUCTOR:
                        target = (TypeElement) targetElement.getEnclosingElement();
                        break;
                    //TODO: type parameter?
                }
            }
            
            allowLocalVariables = false;
        } else {
            if (errorPath.getLeaf().getKind() == Kind.IDENTIFIER) {
                target = source;
                
                if (firstMethod != null) {
                    if (((MethodTree)firstMethod.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                        modifiers.add(Modifier.STATIC);
                    }
                } else {
                    //TODO: outside of any method...
                }
            }
        }
        
        if (target == null) {
            if (ErrorHintsProvider.ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ErrorHintsProvider.ERR.log(org.openide.ErrorManager.INFORMATIONAL,"target=null");
                ErrorHintsProvider.ERR.log(org.openide.ErrorManager.INFORMATIONAL,"offset=" + offset);
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "errorTree=" + errorPath.getLeaf());
            }
            
            return Collections.<Fix>emptyList();
        }
        
        modifiers.addAll(getAccessModifiers(source, target));
        
        List<Fix> result = new ArrayList<Fix>();
        
        Set<FixTypes> fixTypes = EnumSet.noneOf(FixTypes.class);
        List<? extends TypeMirror> types = resolveType(fixTypes, info, parent, errorPath.getLeaf(), offset);
        
        if (types == null || types.isEmpty()) {
            return Collections.<Fix>emptyList();
        }
        
        //XXX: should reasonably consider all the found type candidates, not only the one:
        TypeMirror type = types.get(0);
        
        if (type == null || type.getKind() == TypeKind.VOID) {
            return Collections.<Fix>emptyList();
        }
        
        //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
        if (containsErrorsOrTypevarsRecursively(type)) {
            return Collections.<Fix>emptyList();
        }
        
        if (fixTypes.contains(FixTypes.FIELD)) {
            result.add(new CreateFieldFix(info, simpleName, modifiers, target, type));
        }
        
        if (allowLocalVariables && (fixTypes.contains(FixTypes.LOCAL) || types.contains(FixTypes.PARAM))) {
            ExecutableElement ee = null;
            
            if (firstMethod != null) {
                ee = (ExecutableElement) info.getTrees().getElement(firstMethod);
            }
            
            if ((ee != null) && type != null) {
                int identifierPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), errorPath.getLeaf());
                if (ee != null && fixTypes.contains(FixTypes.PARAM))
                    result.add(new AddParameterOrLocalFix(info, type, simpleName, true, identifierPos));
                if (fixTypes.contains(FixTypes.LOCAL))
                    result.add(new AddParameterOrLocalFix(info, type, simpleName, false, identifierPos));
            }
        }
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return CreateElement.class.getName();
    }
    
    public String getDisplayName() {
        return "Create Field Fix";
    }
    
    public String getDescription() {
        return "Create Field Fix";
    }
    
    //XXX: currently we cannot fix:
    //xxx = new ArrayList<Unknown>();
    //=>
    //ArrayList<Unknown> xxx;
    //xxx = new ArrayList<Unknown>();
    private static boolean containsErrorsOrTypevarsRecursively(TypeMirror tm) {
        switch (tm.getKind()) {
            case WILDCARD:
            case TYPEVAR:
            case ERROR:
                return true;
            case DECLARED:
                DeclaredType type = (DeclaredType) tm;
                
                for (TypeMirror t : type.getTypeArguments()) {
                    if (containsErrorsOrTypevarsRecursively(t))
                        return true;
                }
                
                return false;
            case ARRAY:
                return containsErrorsOrTypevarsRecursively(((ArrayType) tm).getComponentType());
            default:
                return false;
        }
    }
    
    private static EnumSet<Modifier> getAccessModifiers(TypeElement source, TypeElement target) {
        TypeElement outterMostSource = SourceUtils.getOutermostEnclosingTypeElement(source);
        TypeElement outterMostTarget = SourceUtils.getOutermostEnclosingTypeElement(target);
        
        if (outterMostSource.equals(outterMostTarget)) {
            return EnumSet.of(Modifier.PRIVATE);
        }
        
        Element sourcePackage = outterMostSource.getEnclosingElement();
        Element targetPackage = outterMostTarget.getEnclosingElement();
        
        if (sourcePackage.equals(targetPackage)) {
            return EnumSet.noneOf(Modifier.class);
        }
        
        //TODO: protected?
        return EnumSet.of(Modifier.PUBLIC);
    }
    
    private static enum FixTypes {
        PARAM, LOCAL, FIELD
    }
    
    private static List<? extends TypeMirror> resolveType(Set<FixTypes> types, CompilationInfo info, TreePath currentPath, Tree unresolved, int offset) {
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
            case RETURN:
                return computeReturn(types, info, currentPath, unresolved, offset);
                
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
            case CLASS:
            case COMPILATION_UNIT:
            case CONTINUE:
            case EXPRESSION_STATEMENT:
            case IMPORT:
            case IDENTIFIER:
            case TYPE_CAST:
            case PARAMETERIZED_TYPE:
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
            case TYPE_PARAMETER:
                //ignored:
                return null;
                
            case CASE:
            case ANNOTATION:
            case CONDITIONAL_EXPRESSION:
            case NEW_ARRAY:
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
    
    private static List<? extends TypeMirror> computeBinaryOperator(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        BinaryTree bt = (BinaryTree) parent.getLeaf();
        TreePath typeToResolve = null;
        
        if (bt.getLeftOperand() == error) {
            typeToResolve = new TreePath(parent, bt.getRightOperand());
        }
        
        if (bt.getRightOperand() == error) {
            typeToResolve = new TreePath(parent, bt.getLeftOperand());
        }
        
        types.add(FixTypes.PARAM);
        types.add(FixTypes.LOCAL);
        types.add(FixTypes.FIELD);
        
        return typeToResolve != null ? Collections.singletonList(info.getTrees().getTypeMirror(typeToResolve)) : null;
    }
    
    private static List<? extends TypeMirror> computeMethod(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        //class or field:
        //check the error is in the body:
        //#92419: check for abstract method/method without body:
        MethodTree mt = (MethodTree) parent.getLeaf();
        
        if (mt.getBody() == null) {
            return null;
        }
        
        try {
            int bodyStart = Utilities.findBodyStart(parent.getLeaf(), info.getCompilationUnit(), info.getTrees().getSourcePositions(), info.getDocument());
            int bodyEnd   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), parent.getLeaf());
            
            types.add(FixTypes.PARAM);
            types.add(FixTypes.LOCAL);
            types.add(FixTypes.FIELD);
            
            if (bodyStart <= offset && offset <= bodyEnd)
                return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, ex.getMessage(), ex);
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeMemberSelect(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        //class or field:
        MemberSelectTree ms = (MemberSelectTree) parent.getLeaf();
        if (!"class".equals(ms.getIdentifier().toString())) {//we obviously should not propose "Create Field" for unknown.class:
            types.add(FixTypes.FIELD);
            return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeAssignment(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
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
                ErrorHintsProvider.ERR.log(org.openide.ErrorManager.INFORMATIONAL,"offset=" + offset);
                ErrorHintsProvider.ERR.log(org.openide.ErrorManager.INFORMATIONAL,"errorTree=" + error);
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "type=null");
            }
            
            return null;
        }
        
        types.add(FixTypes.PARAM);
        types.add(FixTypes.LOCAL);
        types.add(FixTypes.FIELD);
        
        return Collections.singletonList(type);
    }
    
    private static List<? extends TypeMirror> computeEnhancedForLoop(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        EnhancedForLoopTree efl = (EnhancedForLoopTree) parent.getLeaf();
        
        if (efl.getExpression() != error) {
            return null;
        }
        
        types.add(FixTypes.PARAM);
        types.add(FixTypes.LOCAL);
        types.add(FixTypes.FIELD);
        
        TypeElement iterable = info.getElements().getTypeElement("java.lang.Iterable");
        TypeMirror argument = info.getTrees().getTypeMirror(new TreePath(new TreePath(parent, efl.getVariable()), efl.getVariable().getType()));
        
        return Collections.singletonList(info.getTypes().getDeclaredType(iterable, argument));
    }
    
    private static List<? extends TypeMirror> computeArrayAccess(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ArrayAccessTree aat = (ArrayAccessTree) parent.getLeaf();
        
        if (aat.getExpression() == error) {
            TreePath parentParent = parent.getParentPath();
            List<? extends TypeMirror> upperTypes = resolveType(types, info, parentParent, aat, offset);
            
            if (upperTypes == null) {
                return null;
            }
            
            List<TypeMirror> arrayTypes = new ArrayList<TypeMirror>();
            
            for (TypeMirror tm : upperTypes) {
                arrayTypes.add(info.getTypes().getArrayType(tm));
            }
            
            return arrayTypes;
        }
        
        if (aat.getIndex() == error) {
            types.add(FixTypes.PARAM);
            types.add(FixTypes.LOCAL);
            types.add(FixTypes.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeVariableDeclaration(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        VariableTree vt = (VariableTree) parent.getLeaf();
        
        if (vt.getInitializer() != error) {
            return null;
        }
        
        types.add(FixTypes.PARAM);
        types.add(FixTypes.LOCAL);
        types.add(FixTypes.FIELD);
        
        return Collections.singletonList(info.getTrees().getTypeMirror(new TreePath(parent, vt.getType())));
    }
    
    private static List<? extends TypeMirror> computeAssert(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        AssertTree at = (AssertTree) parent.getLeaf();
        
        types.add(FixTypes.PARAM);
        types.add(FixTypes.LOCAL);
        types.add(FixTypes.FIELD);
        
        if (at.getCondition() == error) {
            return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.BOOLEAN));
        }
        
        if (at.getDetail() == error) {
            return Collections.singletonList(info.getElements().getTypeElement("java.lang.Object").asType());
        }
        
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeParenthesis(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        ParenthesizedTree pt = (ParenthesizedTree) parent.getLeaf();
        
        if (pt.getExpression() != error) {
            return null;
        }
        
        TreePath parentParent = parent.getParentPath();
        List<? extends TypeMirror> upperTypes = resolveType(types, info, parentParent, pt, offset);
        
        if (upperTypes == null) {
            return null;
        }
        
        return upperTypes;
    }
    
    private static List<? extends TypeMirror> computePrimitiveType(Set<FixTypes> types, CompilationInfo info, Tree expression, Tree error, TypeKind kind) {
        if (expression == error) {
            types.add(FixTypes.PARAM);
            types.add(FixTypes.LOCAL);
            types.add(FixTypes.FIELD);
            
            return Collections.singletonList(info.getTypes().getPrimitiveType(kind));
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeReferenceType(Set<FixTypes> types, CompilationInfo info, Tree expression, Tree error, String type) {
        if (expression == error) {
            types.add(FixTypes.PARAM);
            types.add(FixTypes.LOCAL);
            types.add(FixTypes.FIELD);
            
            return Collections.singletonList(info.getElements().getTypeElement(type).asType());
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeUnary(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
        UnaryTree tree = (UnaryTree) parent.getLeaf();
        
        if (tree.getExpression() == error) {
            List<? extends TypeMirror> parentTypes = resolveType(types, info, parent.getParentPath(), tree, offset);
            
            if (parentTypes == null) {
                types.add(FixTypes.PARAM);
                types.add(FixTypes.LOCAL);
                types.add(FixTypes.FIELD);
                
                return Collections.singletonList(info.getTypes().getPrimitiveType(TypeKind.INT));
            }
            
            return parentTypes;
        }
        
        return null;
    }
    
    private static List<? extends TypeMirror> computeReturn(Set<FixTypes> types, CompilationInfo info, TreePath parent, Tree error, int offset) {
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
            
            types.add(FixTypes.PARAM);
            types.add(FixTypes.LOCAL);
            types.add(FixTypes.FIELD);
            
            return Collections.singletonList(((ExecutableElement) el).getReturnType());
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
    
    static final class CreateFieldFix implements Fix {
        
        private FileObject targetFile;
        private ElementHandle<TypeElement> target;
        private TypeMirrorHandle proposedType;
        private ClasspathInfo cpInfo;
        private Set<Modifier> modifiers;
        
        private String name;
        private String inFQN;
        
        public CreateFieldFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, TypeMirror proposedType) {
            this.name = name;
            this.inFQN = target.getQualifiedName().toString();
            this.cpInfo = info.getClasspathInfo();
            this.modifiers = modifiers;
            this.targetFile = SourceUtils.getFile(target, cpInfo);
            this.target = ElementHandle.create(target);
            if (proposedType.getKind() == TypeKind.NULL) {
                proposedType = info.getElements().getTypeElement("java.lang.Object").asType();
            }
            this.proposedType = TypeMirrorHandle.create(proposedType);
        }
        
        public String getText() {
            return "Create field " + name + " in " + inFQN;
        }
        
        public ChangeInfo implement() {
            try {
                //use the original cp-info so it is "sure" that the proposedType can be resolved:
                JavaSource js = JavaSource.create(cpInfo, targetFile);
                
                js.runModificationTask(new CancellableTask<WorkingCopy>() {
                    public void cancel() {
                    }
                    public void run(final WorkingCopy working) throws IOException {
                        working.toPhase(Phase.RESOLVED);
                        TypeElement targetType = target.resolve(working);
                        
                        if (targetType == null) {
                            ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target.");
                            return;
                        }
                        
                        ClassTree targetTree = working.getTrees().getTree(targetType);
                        
                        if (targetTree == null) {
                            ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target tree: " + targetType.getQualifiedName() + ".");
                            return;
                        }
                        
                        TypeMirror proposedType = CreateFieldFix.this.proposedType.resolve(working);
                        
                        if (proposedType == null) {
                            ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type.");
                            return;
                        }
                        
                        TreeMaker make = working.getTreeMaker();
                        TypeMirror tm = proposedType;
                        VariableTree var = null;
                        
                        if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY) {
                            var = make.Variable(make.Modifiers(modifiers), name, make.Type(tm), null);
                        }
                        
                        if (tm.getKind().isPrimitive()) {
                            var = make.Variable(make.Modifiers(modifiers), name, make.Type(tm), null);
                        }
                        
                        assert var != null : tm.getKind();
                        ClassTree decl = make.addClassMember(targetTree, var);
                        working.rewrite(targetTree, decl);
                    }
                }).commit();
            } catch (IOException e) {
                throw (IllegalStateException) new IllegalStateException().initCause(e);
            }
            
            return null;
        }
        
        String toDebugString(CompilationInfo info) {
            return "CreateFieldFix:" + name + ":" + target.getQualifiedName() + ":" + proposedType.resolve(info).toString() + ":" + modifiers;
        }
    }
    
}
