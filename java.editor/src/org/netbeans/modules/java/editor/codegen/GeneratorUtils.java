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
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.lang.model.util.Types;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.Document;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedDocument;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class GeneratorUtils {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(GeneratorUtils.class.getName());
    private static final String ERROR = "<error>"; //NOI18N
    public static final int GETTERS_ONLY = 1;
    public static final int SETTERS_ONLY = 2;

    private GeneratorUtils() {
    }
    
    public static ClassTree insertClassMember(WorkingCopy copy, TreePath path, Tree member) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TreeUtilities tu = copy.getTreeUtilities();
        int idx = 0;
        for (Tree tree : ((ClassTree)path.getLeaf()).getMembers()) {
            if (!tu.isSynthetic(new TreePath(path, tree)) && ClassMemberComparator.compare(member, tree) < 0)
                break;
            idx++;
        }
        return copy.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), idx, member);        
    }
    
    public static List<? extends ExecutableElement> findUndefs(CompilationInfo info, TypeElement impl) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "findUndefs(" + info + ", " + impl + ")");
        List<? extends ExecutableElement> undef = findUndefs(info, impl, impl);
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "undef=" + undef);
        return undef;
    }
    
    public static List<? extends ExecutableElement> findOverridable(CompilationInfo info, TypeElement impl) {
        List<ExecutableElement> overridable = new ArrayList<ExecutableElement>();
        List<TypeElement> classes = getAllClasses(impl);
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "classes=" + classes);
        
        for (TypeElement te : classes.subList(1, classes.size())) {
            for (ExecutableElement ee : ElementFilter.methodsIn(te.getEnclosedElements())) {
                Set<Modifier> set = EnumSet.copyOf(NOT_OVERRIDABLE);
                
                set.removeAll(ee.getModifiers());
                
                if (set.size() != NOT_OVERRIDABLE.size())
                    continue;
                
                if(ee.getModifiers().contains(Modifier.PRIVATE)) //do not offer overriding of private methods
                    continue;
                    
                if(overridesPackagePrivateOutsidePackage(ee, impl)) //do not offer package private methods in case they're from different package
                    continue;
                
                int thisElement = classes.indexOf(te);
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "ee=" + ee);
                    ERR.log(ErrorManager.INFORMATIONAL, "thisElement = " + thisElement);
                    ERR.log(ErrorManager.INFORMATIONAL, "classes.subList(0, thisElement + 1)=" + classes.subList(0, thisElement + 1));
                    ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(info, ee, classes.subList(0, thisElement + 1))=" + isOverriden(info, ee, classes.subList(0, thisElement + 1)));
                }
                
                if (!isOverriden(info, ee, classes.subList(0, thisElement + 1))) {
                    overridable.add(ee);
                }
            }
        }
        
        return overridable;
    }

    public static Map<? extends TypeElement, ? extends List<? extends VariableElement>> findAllAccessibleFields(CompilationInfo info, TypeElement clazz) {
        Map<TypeElement, List<? extends VariableElement>> result = new HashMap<TypeElement, List<? extends VariableElement>>();

        result.put(clazz, findAllAccessibleFields(info, clazz, clazz));

        for (TypeElement te : getAllParents(clazz)) {
            result.put(te, findAllAccessibleFields(info, clazz, te));
        }

        return result;
    }
    
    public static void scanForFieldsAndConstructors(CompilationInfo info, final TreePath clsPath, final Set<VariableElement> initializedFields, final Set<VariableElement> uninitializedFields, final List<ExecutableElement> constructors) {
        final Trees trees = info.getTrees();
        new TreePathScanner<Void, Boolean>() {
            @Override
            public Void visitVariable(VariableTree node, Boolean p) {
                if (ERROR.contentEquals(node.getName()))
                    return null;
                Element el = trees.getElement(getCurrentPath());
                if (el != null && el.getKind() == ElementKind.FIELD && !el.getModifiers().contains(Modifier.STATIC) && node.getInitializer() == null && !initializedFields.remove(el))
                    uninitializedFields.add((VariableElement)el);
                return null;
            }
            @Override
            public Void visitAssignment(AssignmentTree node, Boolean p) {
                Element el = trees.getElement(new TreePath(getCurrentPath(), node.getVariable()));
                if (el != null && el.getKind() == ElementKind.FIELD && !uninitializedFields.remove(el))
                    initializedFields.add((VariableElement)el);
                return null;
            }
            @Override
            public Void visitClass(ClassTree node, Boolean p) {
                //do not analyse the inner classes:
                return p ? super.visitClass(node, false) : null;
            }
            @Override
            public Void visitMethod(MethodTree node, Boolean p) {
                Element el = trees.getElement(getCurrentPath());
                if (el != null && el.getKind() == ElementKind.CONSTRUCTOR)
                    constructors.add((ExecutableElement)el);
                return null;
            }
        }.scan(clsPath, Boolean.TRUE);
    }

    public static void generateAllAbstractMethodImplementations(WorkingCopy wc, TreePath path) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            TreeMaker make = wc.getTreeMaker();
            ClassTree clazz = (ClassTree)path.getLeaf();
            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
            for(ExecutableElement element : findUndefs(wc, te, te))
                members.add(createMethodImplementation(wc, element, (DeclaredType)te.asType()));
            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
            wc.rewrite(clazz, nue);
        }
    }
    
    public static void generateAbstractMethodImplementations(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int index) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            TreeMaker make = wc.getTreeMaker();
            ClassTree clazz = (ClassTree)path.getLeaf();
            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
            List<Tree> methods = new ArrayList<Tree>();
            for(ExecutableElement element : elements)
                methods.add(createMethodImplementation(wc, element, (DeclaredType)te.asType()));
            members.addAll(index, methods);
            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
            wc.rewrite(clazz, nue);
        }
    }
    
    public static void generateAbstractMethodImplementation(WorkingCopy wc, TreePath path, ExecutableElement element, int index) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree decl = wc.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), index, createMethodImplementation(wc, element, (DeclaredType)te.asType()));
            wc.rewrite(path.getLeaf(), decl);
        }
    }
    
    public static void generateMethodOverrides(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int index) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            TreeMaker make = wc.getTreeMaker();
            ClassTree clazz = (ClassTree)path.getLeaf();
            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
            List<Tree> methods = new ArrayList<Tree>();
            for(ExecutableElement element : elements)
                methods.add(createMethodImplementation(wc, element, (DeclaredType)te.asType()));
            members.addAll(index, methods);
            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
            wc.rewrite(clazz, nue);
        }
    }
    
    public static void generateMethodOverride(WorkingCopy wc, TreePath path, ExecutableElement element, int index) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree decl = wc.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), index, createMethodImplementation(wc, element, (DeclaredType)te.asType()));
            wc.rewrite(path.getLeaf(), decl);
        }
    }

    public static void generateConstructor(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> initFields, ExecutableElement inheritedConstructor, int index) {
        TreeMaker make = wc.getTreeMaker();
        ClassTree clazz = (ClassTree)path.getLeaf();
        List<VariableTree> parameters = new ArrayList<VariableTree>();
        List<StatementTree> statements = new ArrayList<StatementTree>();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));        
        if (inheritedConstructor != null && !inheritedConstructor.getParameters().isEmpty()) {
            List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
            for (VariableElement ve : inheritedConstructor.getParameters()) {
                parameters.add(make.Variable(parameterModifiers, ve.getSimpleName(), make.Type(ve.asType()), null));
                arguments.add(make.Identifier(ve.getSimpleName())); //NOI18N
            }
            statements.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("super"), arguments)));
        }
        for (VariableElement ve : initFields) {
            parameters.add(make.Variable(parameterModifiers, ve.getSimpleName(), make.Type(ve.asType()), null));
            statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName())))); //NOI18N
        }
        BlockTree body = make.Block(statements, false);
        ClassTree decl = make.insertClassMember(clazz, index, make.Method(make.Modifiers(EnumSet.of(wc.getTreeUtilities().isEnum(clazz) ? Modifier.PRIVATE : Modifier.PUBLIC)), "<init>", null, Collections.<TypeParameterTree> emptyList(), parameters, Collections.<ExpressionTree>emptyList(), body, null)); //NOI18N
        wc.rewrite(path.getLeaf(), decl);
    }
    
    public static void generateGettersAndSetters(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> fields, int type, int index) {
        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            TreeMaker make = wc.getTreeMaker();
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            ClassTree clazz = (ClassTree)path.getLeaf();
            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
            List<Tree> methods = new ArrayList<Tree>();
            for(VariableElement element : fields) {
                if (type != SETTERS_ONLY)
                    methods.add(gu.createGetter(te, element));
                if (type != GETTERS_ONLY)
                    methods.add(gu.createSetter(te, element));
            }
            members.addAll(index, methods);
            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
            wc.rewrite(clazz, nue);
        }
    }
    
    public static boolean hasGetter(CompilationInfo info, VariableElement field, Map<String, List<ExecutableElement>> methods) {
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = field.asType();
        StringBuilder sb = new StringBuilder();
        sb.append(type.getKind() == TypeKind.BOOLEAN ? "is" : "get").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
        Types types = info.getTypes();
        List<ExecutableElement> candidates = methods.get(sb.toString());
        if (candidates != null) {
            for (ExecutableElement candidate : candidates) {
                if (candidate.getParameters().isEmpty() && types.isSameType(candidate.getReturnType(), type))
                    return true;
            }
        }
        return false;
    }
    
    public static boolean hasSetter(CompilationInfo info, VariableElement field, Map<String, List<ExecutableElement>> methods) {
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = field.asType();
        StringBuilder sb = new StringBuilder();
        sb.append("set").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
        Types types = info.getTypes();
        List<ExecutableElement> candidates = methods.get(sb.toString());
        if (candidates != null) {
            for (ExecutableElement candidate : candidates) {
                if (candidate.getReturnType().getKind() == TypeKind.VOID && candidate.getParameters().size() == 1 && types.isSameType(candidate.getParameters().get(0).asType(), type))
                    return true;
            }
        }
        return false;
    }
    
    public static int findClassMemberIndex(WorkingCopy wc, ClassTree clazz, int offset) {
        int index = 0;
        SourcePositions sp = wc.getTrees().getSourcePositions();
        GuardedDocument gdoc = null;
        try {
            Document doc = wc.getDocument();
            if (doc != null && doc instanceof GuardedDocument)
                gdoc = (GuardedDocument)doc;
        } catch (IOException ioe) {}
        Tree lastMember = null;
        for (Tree tree : clazz.getMembers()) {
            if (offset <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
                if (gdoc == null)
                    break;
                int pos = (int)(lastMember != null ? sp.getEndPosition(wc.getCompilationUnit(), lastMember) : sp.getStartPosition(wc.getCompilationUnit(), clazz));
                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                if (pos <= sp.getStartPosition(wc.getCompilationUnit(), tree))
                    break;
            }
            index++;
            lastMember = tree;
        }
        return index;
    }
    
    private static MethodTree createMethodImplementation(WorkingCopy wc, ExecutableElement element, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = element.getModifiers();
        Set<Modifier> flags = mods.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(mods);
        boolean isAbstract = flags.remove(Modifier.ABSTRACT);
        flags.remove(Modifier.NATIVE);
        
        ExecutableType et = (ExecutableType)wc.getTypes().asMemberOf(type, element);
        List<TypeParameterTree> typeParams = new ArrayList<TypeParameterTree>();
        for (TypeParameterElement tpe: element.getTypeParameters()) {
            List<ExpressionTree> bounds = new ArrayList<ExpressionTree>();
            for (TypeMirror bound : tpe.getBounds()) {
                if (bound.getKind() != TypeKind.NULL) {
                    //if the bound is java.lang.Object, do not generate the extends clause:
                    if (bound.getKind() != TypeKind.DECLARED || !"java.lang.Object".contentEquals(((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName())) //NOI18N
                        bounds.add((ExpressionTree)make.Type(bound));
                }
            }            
            typeParams.add(make.TypeParameter(tpe.getSimpleName(), bounds));
        }

        Tree returnType = make.Type(et.getReturnType());

        List<VariableTree> params = new ArrayList<VariableTree>();        
        boolean isVarArgs = element.isVarArgs();
        Iterator<? extends VariableElement> formArgNames = element.getParameters().iterator();
        Iterator<? extends TypeMirror> formArgTypes = et.getParameterTypes().iterator();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        while (formArgNames.hasNext() && formArgTypes.hasNext()) {
            VariableElement formArgName = formArgNames.next();
            TypeMirror formArgType = formArgTypes.next();
            if (isVarArgs && !formArgNames.hasNext())
                parameterModifiers = make.Modifiers(1L<<34, Collections.<AnnotationTree>emptyList());
            params.add(make.Variable(parameterModifiers, formArgName.getSimpleName(), make.Type(formArgType), null));
        }

        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (TypeMirror tm : et.getThrownTypes()) {
            throwsList.add((ExpressionTree)make.Type(tm));
        }
        
        BlockTree body;
        List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();
        if (isAbstract) {
            List<StatementTree> blockStatements = new ArrayList<StatementTree>();
            TypeElement uoe = wc.getElements().getTypeElement("java.lang.UnsupportedOperationException"); //NOI18N
            //TODO: if uoe == null: cannot resolve UnsupportedOperationException for some reason, create a different body in such a case
            if (uoe != null) {
                NewClassTree nue = make.NewClass(null, Collections.<ExpressionTree>emptyList(), make.QualIdent(uoe), Collections.singletonList(make.Literal("Not supported yet.")), null);
                blockStatements.add(make.Throw(nue));
            }
            body = make.Block(blockStatements, false);
        } else {
            List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
            for (VariableElement ve : element.getParameters()) {
                arguments.add(make.Identifier(ve.getSimpleName()));
            }            
            MethodInvocationTree inv = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("super"), element.getSimpleName()), arguments); //NOI18N
            StatementTree statement = wc.getTypes().getNoType(TypeKind.VOID) == element.getReturnType() ?
                make.ExpressionStatement(inv) : make.Return(inv);
            body = make.Block(Collections.singletonList(statement), false);
            
            //add @Override annotation if developing for 1.5:
            if (GeneratorUtils.supportsOverride(wc.getFileObject())) {
                annotations.add(make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList())); //NOI18N
            }
        }

        return make.Method(make.Modifiers(flags, annotations), element.getSimpleName(), returnType, typeParams, params, throwsList, body, null);
    }
    
    private static MethodTree createGetterMethod(WorkingCopy wc, VariableElement element, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        if (element.getModifiers().contains(Modifier.STATIC))
            mods.add(Modifier.STATIC);
        CharSequence name = element.getSimpleName();
        assert name.length() > 0;
        StringBuilder sb = new StringBuilder();
        sb.append(element.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
        BlockTree body = make.Block(Collections.singletonList(make.Return(make.Identifier(element.getSimpleName()))), false);
        return make.Method(make.Modifiers(mods), sb, make.Type(element.asType()), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null);
    }
    
    private static MethodTree createSetterMethod(WorkingCopy wc, VariableElement element, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = element.getModifiers().contains(Modifier.STATIC);
        if (isStatic)
            mods.add(Modifier.STATIC);
        CharSequence name = element.getSimpleName();
        assert name.length() > 0;
        StringBuilder sb = new StringBuilder();
        sb.append("set").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), element.getSimpleName(), make.Type(element.asType()), null));
        BlockTree body = make.Block(Collections.singletonList(make.ExpressionStatement(make.Assignment(make.MemberSelect(isStatic? make.Identifier(element.getEnclosingElement().getSimpleName()) : make.Identifier("this"), element.getSimpleName()), make.Identifier(element.getSimpleName())))), false); //NOI18N
        return make.Method(make.Modifiers(mods), sb, make.Type(wc.getTypes().getNoType(TypeKind.VOID)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null);
    }
    
    private static List<? extends ExecutableElement> findUndefs(CompilationInfo info, TypeElement impl, TypeElement element) {
        List<ExecutableElement> undef = new ArrayList<ExecutableElement>();
        ElementUtilities eu = info.getElementUtilities();
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            for (Element e : element.getEnclosedElements()) {
                if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.ABSTRACT)) {
                    ExecutableElement ee = (ExecutableElement)e;
                    Element eeImpl = eu.getImplementationOf(ee, impl);
                    if (eeImpl == null || (eeImpl == ee && impl != element))                        
                        undef.add(ee);
                }
            }
        }
        Types types = info.getTypes();
        DeclaredType implType = (DeclaredType)impl.asType();
        for (TypeMirror t : types.directSupertypes(element.asType())) {
            for (ExecutableElement ee : findUndefs(info, impl, (TypeElement)((DeclaredType)t).asElement())) {
                //check if "the same" method has already been added:
                boolean exists = false;
                TypeMirror eeType = types.asMemberOf(implType, ee);
                for (ExecutableElement existing : undef) {
                    if (existing.getSimpleName().contentEquals(ee.getSimpleName())) {
                        TypeMirror existingType = types.asMemberOf(implType, existing);
                        if (types.isSameType(eeType, existingType)) {
                            exists = true;
                            break;
                        }
                    }
                }                
                if (!exists)
                    undef.add(ee);
            }
        }        
        return undef;
    }
    
    private static List<? extends VariableElement> findAllAccessibleFields(CompilationInfo info, TypeElement accessibleFrom, TypeElement toScan) {
        List<VariableElement> result = new ArrayList<VariableElement>();

        for (VariableElement ve : ElementFilter.fieldsIn(toScan.getEnclosedElements())) {
            //check if ve is accessible from accessibleFrom:
            if (ve.getModifiers().contains(Modifier.PUBLIC)) {
                result.add(ve);
                continue;
            }
            if (ve.getModifiers().contains(Modifier.PRIVATE)) {
                if (accessibleFrom == toScan)
                    result.add(ve);
                continue;
            }
            if (ve.getModifiers().contains(Modifier.PROTECTED)) {
                if (getAllParents(accessibleFrom).contains(toScan))
                    result.add(ve);
                continue;
            }
            //TODO:package private:
        }

        return result;
    }
    
    public static Collection<TypeElement> getAllParents(TypeElement of) {
        Set<TypeElement> result = new HashSet<TypeElement>();
        
        for (TypeMirror t : of.getInterfaces()) {
            TypeElement te = (TypeElement) ((DeclaredType)t).asElement();
            
            if (te != null) {
                result.add(te);
                result.addAll(getAllParents(te));
            } else {
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + t);
                }
            }
        }
        
        TypeMirror sup = of.getSuperclass();
        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
        
        if (te != null) {
            result.add(te);
            result.addAll(getAllParents(te));
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
            }
        }
        
        return result;
    }

    public static boolean supportsOverride(FileObject file) {
        return SUPPORTS_OVERRIDE_SOURCE_LEVELS.contains(SourceLevelQuery.getSourceLevel(file));
    }
    
    private static final Set<String> SUPPORTS_OVERRIDE_SOURCE_LEVELS;
    
    static {
        SUPPORTS_OVERRIDE_SOURCE_LEVELS = new HashSet();
        
        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.5");
        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.6");
    }
    
    private static List<TypeElement> getAllClasses(TypeElement of) {
        List<TypeElement> result = new ArrayList<TypeElement>();
        TypeMirror sup = of.getSuperclass();
        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
        
        result.add(of);
        
        if (te != null) {
            result.addAll(getAllClasses(te));
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
            }
        }
        
        return result;
    }
    
    private static boolean isOverriden(CompilationInfo info, ExecutableElement methodBase, List<TypeElement> classes) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(" + info + ", " + methodBase + ", " + classes + ")");
        }
        
        for (TypeElement impl : classes) {
            for (ExecutableElement methodImpl : ElementFilter.methodsIn(impl.getEnclosedElements())) {
                if (   ERR.isLoggable(ErrorManager.INFORMATIONAL)
                && info.getElements().overrides(methodImpl, methodBase, impl)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "overrides:");
                    ERR.log(ErrorManager.INFORMATIONAL, "impl=" + impl);
                    ERR.log(ErrorManager.INFORMATIONAL, "methodImpl=" + methodImpl);
                }
                
                if (info.getElements().overrides(methodImpl, methodBase, impl))
                    return true;
            }
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "no overriding methods overrides:");
        }
        
        return false;
    }

    private static final Set<Modifier> NOT_OVERRIDABLE = /*EnumSet.noneOf(Modifier.class);/*/EnumSet.of(Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL);

    public static boolean isAccessible(TypeElement from, Element what) {
        if (what.getModifiers().contains(Modifier.PUBLIC))
            return true;

        TypeElement fromTopLevel = SourceUtils.getOutermostEnclosingTypeElement(from);
        TypeElement whatTopLevel = SourceUtils.getOutermostEnclosingTypeElement(what);

        if (fromTopLevel.equals(whatTopLevel))
            return true;

        if (what.getModifiers().contains(Modifier.PRIVATE))
            return false;

        if (what.getModifiers().contains(Modifier.PROTECTED)) {
            if (getAllClasses(fromTopLevel).contains(SourceUtils.getEnclosingTypeElement(what)))
                return true;
        }

        //package private:
        return ((PackageElement) fromTopLevel.getEnclosingElement()).getQualifiedName().toString().contentEquals(((PackageElement) whatTopLevel.getEnclosingElement()).getQualifiedName());
    }
    
    static DialogDescriptor createDialogDescriptor( JComponent content, String label ) {
        JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_button") );
	buttons[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GeneratorUtils.class, "A11Y_Generate"));
        buttons[1] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_cancel_button") );
        return new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        
    }
    
    /**
     * Detects if this element overrides package private element from superclass
     * outside package
     * @param ee elememt to test
     * @return true if it does
     */ 
    private static boolean overridesPackagePrivateOutsidePackage(ExecutableElement ee, TypeElement impl) {
        String elemPackageName = ee.getEnclosingElement().getEnclosingElement().getSimpleName().toString();
        String currentPackageName = impl.getEnclosingElement().getSimpleName().toString();
        if(!ee.getModifiers().contains(Modifier.PRIVATE) && !ee.getModifiers().contains(Modifier.PUBLIC) && !ee.getModifiers().contains(Modifier.PROTECTED) && !currentPackageName.equals(elemPackageName))
            return true;
        else
            return false;
    }
    
    private static class ClassMemberComparator {
        
        public static int compare(Tree tree1, Tree tree2) {
            if (tree1 == tree2)
                return 0;
            int importanceDiff = getSortPriority(tree1) - getSortPriority(tree2);
            if (importanceDiff != 0)
                return importanceDiff;
            int alphabeticalDiff = getSortText(tree1).compareTo(getSortText(tree2));
            if (alphabeticalDiff != 0)
                return alphabeticalDiff;
            return -1;
        }
        
        private static int getSortPriority(Tree tree) {
            int ret = 0;
            ModifiersTree modifiers = null;
            switch (tree.getKind()) {
            case CLASS:
                ret = 400;
                modifiers = ((ClassTree)tree).getModifiers();
                break;
            case METHOD:
                MethodTree mt = (MethodTree)tree;
                if (mt.getName().contentEquals("<init>"))
                    ret = 200;
                else
                    ret = 300;
                modifiers = mt.getModifiers();
                break;
            case VARIABLE:
                ret = 100;
                modifiers = ((VariableTree)tree).getModifiers();
                break;
            }
            if (modifiers != null) {
                if (!modifiers.getFlags().contains(Modifier.STATIC))
                    ret += 1000;
                if (modifiers.getFlags().contains(Modifier.PUBLIC))
                    ret += 10;
                else if (modifiers.getFlags().contains(Modifier.PROTECTED))
                    ret += 20;
                else if (modifiers.getFlags().contains(Modifier.PRIVATE))
                    ret += 40;
                else
                    ret += 30;
            }
            return ret;
        }
        
        private static String getSortText(Tree tree) {
            switch (tree.getKind()) {
            case CLASS:
                return ((ClassTree)tree).getSimpleName().toString();
            case METHOD:
                MethodTree mt = (MethodTree)tree;
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for(Iterator<? extends VariableTree> it = mt.getParameters().iterator(); it.hasNext();) {
                    VariableTree param = it.next();
                    if (param.getType().getKind() == Tree.Kind.IDENTIFIER)
                        sortParams.append(((IdentifierTree)param.getType()).getName().toString());
                    else if (param.getType().getKind() == Tree.Kind.MEMBER_SELECT)
                        sortParams.append(((MemberSelectTree)param.getType()).getIdentifier().toString());
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                return mt.getName().toString() + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            case VARIABLE:
                return ((VariableTree)tree).getName().toString();
            }
            return ""; //NOI18N
        }
    }
}
