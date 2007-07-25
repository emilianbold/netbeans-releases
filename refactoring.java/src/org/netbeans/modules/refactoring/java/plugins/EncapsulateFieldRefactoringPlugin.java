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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Hurka
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldRefactoringPlugin extends JavaRefactoringPlugin {
    
    private static final Logger LOG = Logger.getLogger(EncapsulateFieldRefactoringPlugin.class.getName());
    
    private ElementHandle<TypeElement> fieldEncloserHandle;
    /**
     * most restrictive accessibility modifier on tree path 
     */
    private Modifier fieldEncloserAccessibility;
    /**
     * present accessibility of field
     */
    private Set<Modifier> fieldAccessibility;
    private ElementHandle<ExecutableElement> currentGetter;
    private ElementHandle<ExecutableElement> currentSetter;
    private static Set<Modifier> accessModifiers = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
    private static List<Modifier> MODIFIERS = Arrays.asList(Modifier.PRIVATE, null, Modifier.PROTECTED, Modifier.PUBLIC);
    private final EncapsulateFieldRefactoring refactoring;
    public static final String CLASS_FIELD_PREFIX = "_"; // NOI18N
    /**
     * path in source with field declaration; refactoring.getSelectedObject()
     * may contain path to a reference
     */
    private TreePathHandle sourceType;
    
    /** Creates a new instance of RenameRefactoring */
    public EncapsulateFieldRefactoringPlugin(EncapsulateFieldRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle handle = sourceType != null? sourceType: refactoring.getSourceType();
        FileObject fo = handle.getFileObject();
        return JavaSource.forFileObject(fo);
    }
    
    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        try {
            javac.toPhase(JavaSource.Phase.RESOLVED);
            sourceType = this.refactoring.getSourceType();
            Problem result = isElementAvail(sourceType, javac);
            if (result != null) {
                return result;
            }

            Element field = sourceType.resolveElement(javac);
            fireProgressListenerStep();
            if (ElementKind.FIELD == field.getKind()) {
               TreePath tp = javac.getTrees().getPath(field);
               sourceType = TreePathHandle.create(tp, javac);
            } else {
                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongType"));
            }
            if (!RetoucheUtils.isElementInOpenProject(sourceType.getFileObject())) {
                return new Problem(true, NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_ProjectNotOpened"));
            }
            
            TypeElement encloser = (TypeElement) field.getEnclosingElement();
            ElementKind classKind = encloser.getKind();
            if (classKind == ElementKind.INTERFACE || classKind == ElementKind.ANNOTATION_TYPE) {
                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateInIntf"));
            }
            
            fieldEncloserHandle = ElementHandle.create(encloser);
            fieldAccessibility = field.getModifiers();
            fieldEncloserAccessibility = resolveVisibility(encloser);
            
            return result;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        return fastCheckParameters(refactoring.getGetterName(), refactoring.getSetterName(), refactoring.getMethodModifiers(), refactoring.getFieldModifiers(), refactoring.isAlwaysUseAccessors());
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        Problem p = null;
        Element field = sourceType.resolveElement(javac);
        TypeElement clazz = (TypeElement) field.getEnclosingElement();
        String getname = refactoring.getGetterName();
        String setname = refactoring.getSetterName();
        ExecutableElement getter = null;
        ExecutableElement setter = null;
        
        if (getname != null) {
            getter = findMethod(javac, clazz, getname, Collections.<VariableElement>emptyList(), true);
        }
        
        if (getter != null) {
            if (field.asType() != getter.getReturnType()) {
                p = createProblem(p, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongGetter", null));
            }
            if (getter.getEnclosingElement() != field.getEnclosingElement()) {
                p = createProblem(p, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateGetterExists"));
            } else {
                currentGetter = ElementHandle.create(getter);
            }
        }
        
        if (setname != null) {
            setter = findMethod(javac, clazz, setname, Collections.singletonList((VariableElement) field), true);
        }
        
        if (setter != null) {
            if (TypeKind.VOID != setter.getReturnType().getKind()) {
                p = createProblem(p, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongSetter", null));
            }
            if (setter.getEnclosingElement() != field.getEnclosingElement()) {
                p = createProblem(p, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "MSG_EncapsulateSetterExists"));
            } else {
                currentSetter = ElementHandle.create(setter);
            }
        }
        return p;
    }
    
    private Problem fastCheckParameters(String getter, String setter,
            Set<Modifier> methodModifier, Set<Modifier> fieldModifier,
            boolean alwaysUseAccessors) {
        
        if ((getter != null && !Utilities.isJavaIdentifier(getter))
                || (setter != null && !Utilities.isJavaIdentifier(setter))
                || (getter == null && setter == null)) {
            // user doesn't use valid java identifier, it cannot be used
            // as getter/setter name
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethods"));
        } else {
            // we have no problem :-)
            return null;
        }
    }

    private Modifier resolveVisibility(TypeElement clazz) {
        NestingKind nestingKind = clazz.getNestingKind();
        
        if (nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL) {
            return Modifier.PRIVATE;
        }
        
        Set<Modifier> mods = clazz.getModifiers();
        if (nestingKind == NestingKind.TOP_LEVEL) {
            return mods.contains(Modifier.PUBLIC)
                    ? Modifier.PUBLIC
                    : null;
        }
        
        if (mods.contains(Modifier.PRIVATE)) {
            return Modifier.PRIVATE;
        
        }
        Modifier mod1 = resolveVisibility((TypeElement) clazz.getEnclosingElement());
        Modifier mod2 = null;
        if (mods.contains(Modifier.PUBLIC)) {
            mod2 = Modifier.PUBLIC;
        } else if (mods.contains(Modifier.PROTECTED)) {
            mod2 = Modifier.PROTECTED;
        }
        
        return max(mod1, mod2);
    }
    
    private Modifier max(Modifier a, Modifier b) {
        if (a == b) {
            return a;
        }
        int ai = MODIFIERS.indexOf(a);
        int bi = MODIFIERS.indexOf(b);
        return ai > bi? a: b;
    }

    private static Modifier getAccessibility(Set<Modifier> mods) {
        if (mods.isEmpty()) {
            return null;
        }
        Set<Modifier> s = new HashSet<Modifier>(mods);
        s.retainAll(accessModifiers);
        return s.isEmpty()? null: s.iterator().next();
    }

    private static Set<Modifier> replaceAccessibility(Modifier currentAccess, Modifier futureAccess, Element elm) {
        Set<Modifier> mods = new HashSet<Modifier>(elm.getModifiers());
        if (currentAccess != null) {
            mods.remove(currentAccess);
        }
        if (futureAccess != null) {
            mods.add(futureAccess);
        }
        return mods;
    }

    private static ExecutableElement findMethod(CompilationInfo javac, TypeElement clazz, String name, List<? extends VariableElement> params, boolean includeSupertypes) {
        TypeElement c = clazz;
        while (true) {
            for (Element elm : c.getEnclosedElements()) {
                if (ElementKind.METHOD == elm.getKind()) {
                    ExecutableElement m = (ExecutableElement) elm;
                    if (name.contentEquals(m.getSimpleName())
                            && compareParams(params, m.getParameters())
                            && isAccessible(javac, clazz, m)) {
                        return m;
                    }
                }
            }
            
            TypeMirror superType = c.getSuperclass();
            if (!includeSupertypes || superType.getKind() == TypeKind.NONE) {
                return null;
            }
            c = (TypeElement) ((DeclaredType) superType).asElement();
        }
    }

    /**
     * returns true if elm is accessible from clazz. elm must be member of clazz
     * or its superclass
     */
    private static boolean isAccessible(CompilationInfo javac, TypeElement clazz, Element elm) {
        if (clazz == elm.getEnclosingElement()) {
            return true;
        }
        Set<Modifier> mods = elm.getModifiers();
        if (mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.PROTECTED)) {
            return true;
        } else if (mods.contains(Modifier.PRIVATE)) {
            return false;
        }
        Elements utils = javac.getElements();
        return utils.getPackageOf(elm) == utils.getPackageOf(clazz);
    }
    
    private static boolean compareParams(List<? extends VariableElement> params1, List<? extends VariableElement> params2) {
        if (params1.size() == params2.size()) {
            Iterator<? extends VariableElement> it1 = params1.iterator();
            for (VariableElement ve : params2) {
                if (ve.asType() != it1.next().asType()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Removes the class field prefix from  the identifer of a field.
     * For example, if the class field prefix is "_", the identifier "_name" 
     * is stripped to become "name".
     * @param identifierString The identifer to strip.
     * @return The stripped identifier.
     */
    private static String stripPrefix(String identifierString){
        String stripped;
        if(identifierString.startsWith(CLASS_FIELD_PREFIX)){
            stripped = identifierString.substring(CLASS_FIELD_PREFIX.length());
        }
        else{
             stripped = identifierString;
        }
        return stripped;
    }
    
    private static StringBuilder getCapitalizedName(VariableElement field) {        
        StringBuilder name = new StringBuilder(stripPrefix(field.getSimpleName().toString()));
        
        name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return name;
    }
    
    
    public static String computeSetterName(VariableElement field) {
        if (field.getModifiers().contains(javax.lang.model.element.Modifier.FINAL)) {
            return null;
        }

        StringBuilder name = getCapitalizedName(field);
        
        name.insert(0, "set"); //NOI18N
        return name.toString();
    }
    
    public static String computeGetterName(VariableElement field) {
        StringBuilder name = getCapitalizedName(field);
        
        if (TypeKind.BOOLEAN == field.asType().getKind()) { // XXX check autoboxing???
            name.insert(0, "is"); //NOI18N
        } else {
            name.insert(0, "get"); //NOI18N
        }
        
        return name.toString();
    }
    
    public Problem prepare(RefactoringElementsBag bag) {
        
        fireProgressListenerStart(AbstractRefactoring.PREPARE, 9);
        try {
            fireProgressListenerStep();
            
            Set<FileObject> refs = getRelevantFiles();
            
            Problem p = null;
            
            if (refactoring.isAlwaysUseAccessors()
                    && refactoring.getMethodModifiers().contains(Modifier.PRIVATE)
                    // is reference fromother files?
                    && refs.size() > 1) {
                // breaks code
                return createProblem(p, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsAccess"));
            }
            if (refactoring.isAlwaysUseAccessors()
                    // is default accessibility?
                    && getAccessibility(refactoring.getMethodModifiers()) == null
                    // is reference fromother files?
                    && refs.size() > 1) {
                // breaks code likely
                p = createProblem(p, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsDefaultAccess"));
            }
            
            Encapsulator encapsulator = new Encapsulator(refactoring, sourceType.getFileObject(), currentGetter, currentSetter);
            createAndAddElements(refs, new TransformTask(encapsulator, sourceType), bag, refactoring);
            
            if (encapsulator.getProblem() != null) {
                if (p != null) {
                    p.setNext(encapsulator.getProblem());
                } else {
                    p = encapsulator.getProblem();
                }
            }
            
            return p;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    private Set<FileObject> getRelevantFiles() {
        // search class index just in case Use accessors even when the field is accessible == true
        // or the field is accessible:
        // * private eclosers|private field -> CP: .java (project) => JavaSource.forFileObject
        // * default enclosers|default field -> CP: package (project)
        // * public|protected enclosers&public|protected field -> CP: project + dependencies
        Set<FileObject> refs;
        FileObject source = sourceType.getFileObject();
        if (fieldAccessibility.contains(Modifier.PRIVATE) || fieldEncloserAccessibility == Modifier.PRIVATE) {
            // search file
            refs = Collections.singleton(source);
        } else { // visible field
            ClasspathInfo cpinfo;
            if (fieldEncloserAccessibility == Modifier.PUBLIC
                    && (fieldAccessibility.contains(Modifier.PUBLIC) || fieldAccessibility.contains(Modifier.PROTECTED))) {
                // search project and dependencies
                cpinfo = RetoucheUtils.getClasspathInfoFor(true, source);
            } else {
                // search project
                cpinfo = RetoucheUtils.getClasspathInfoFor(false, source);
            }
            ClassIndex index = cpinfo.getClassIndex();
            refs = index.getResources(fieldEncloserHandle, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            if (!refs.contains(source)) {
                refs = new HashSet<FileObject>(refs);
                refs.add(source);
            }
        }
        return refs;
    }
    
    private static boolean isSubclassOf(TypeElement subclass, TypeElement superclass) {
        TypeMirror superType = subclass.getSuperclass();
        while(superType.getKind() != TypeKind.NONE) {
            TypeElement superTypeElm = (TypeElement) ((DeclaredType) superType).asElement();
            if (superclass == superTypeElm) {
                return true;
            }
            superType = superTypeElm.getSuperclass();
        }
        return false;
    }
    
    private static final class Encapsulator extends RefactoringVisitor {
        
        private final EncapsulateFieldRefactoring refactoring;
        private final FileObject sourceFile;
        private Problem problem;
        private boolean useAccessors;
        private final ElementHandle<ExecutableElement> currentGetter;
        private final ElementHandle<ExecutableElement> currentSetter;

        public Encapsulator(EncapsulateFieldRefactoring refactoring, FileObject src,
                ElementHandle<ExecutableElement> currentGetter, ElementHandle<ExecutableElement> currentSetter) {
            this.refactoring = refactoring;
            this.sourceFile = src;
            this.currentGetter = currentGetter;
            this.currentSetter = currentSetter;
        }

        public Problem getProblem() {
            return problem;
        }

        @Override
        public Tree visitCompilationUnit(CompilationUnitTree node, Element field) {
            return scan(node.getTypeDecls(), field);
        }
        
        @Override
        public Tree visitClass(ClassTree node, Element field) {
            if (getCurrentPath().getCompilationUnit() == getCurrentPath().getParentPath().getLeaf()) {
                // node is toplevel class
                TypeElement clazz = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
                useAccessors = resolveAlwaysUseAccessors(clazz, field);
            }
            
            if (sourceFile == workingCopy.getFileObject()) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                if (el == field.getEnclosingElement()) {
                    createGetterAndSetter(node, (VariableElement) field);
                }
            }
            return scan(node.getMembers(), field);
        }
        
        @Override
        public Tree visitVariable(VariableTree node, Element field) {
            if (sourceFile == workingCopy.getFileObject()) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                if (el == field) {
                    resolveFieldDeclaration(node, field);
                    return null;
                }
            }
            return scan(node.getInitializer(), field);
        }
        
        @Override
        public Tree visitAssignment(AssignmentTree node, Element field) {
            if (!useAccessors) {
                return null;
            }
            
            ExpressionTree variable = node.getVariable();
            boolean isArray = false;
            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArray = true;
                variable = ((ArrayAccessTree) variable).getExpression();
            }
            
            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
            if (el == field && refactoring.getSetterName() != null
                    // check (field = 3) == 3
                    && (isArray || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), field)
                    && !isInGetterSetter(getCurrentPath())) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable);
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, refactoring.getSetterName());
                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(node.getExpression()));
                    rewrite(node, invksetter);
                }
            }
            return scan(node.getExpression(), field);
        }

        @Override
        public Tree visitCompoundAssignment(CompoundAssignmentTree node, Element field) {
            if (!useAccessors) {
                return null;
            }
            
            ExpressionTree variable = node.getVariable();
            boolean isArray = false;
            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArray = true;
                variable = ((ArrayAccessTree) variable).getExpression();
            }
            
            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
            if (el == field && refactoring.getSetterName() != null
                    // check (field += 3) == 3
                    && (isArray || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), field)
                    && !isInGetterSetter(getCurrentPath())) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable);
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, refactoring.getSetterName());

                    // translate compound op to binary op; ADD_ASSIGNMENT -> ADD
                    String s = node.getKind().name();
                    s = s.substring(0, s.length() - "_ASSIGNMENT".length()); // NOI18N
                    Tree.Kind operator = Tree.Kind.valueOf(s);

                    ExpressionTree invkgetter = createGetterInvokation(variable);
                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(make.Binary(operator, invkgetter, node.getExpression())));
                    rewrite(node, invksetter);
                }
            }
            return scan(node.getExpression(), field);
        }

        @Override
        public Tree visitUnary(UnaryTree node, Element field) {
            if (!useAccessors) {
                return null;
            }
            if (refactoring.getGetterName() != null && refactoring.getSetterName() != null) {
                ExpressionTree t = node.getExpression();
                Kind kind = node.getKind();
                boolean isArrayOrImmutable = kind != Kind.POSTFIX_DECREMENT
                        && kind != Kind.POSTFIX_INCREMENT
                        && kind != Kind.PREFIX_DECREMENT
                        && kind != Kind.PREFIX_INCREMENT;
                while (t.getKind() == Tree.Kind.ARRAY_ACCESS) {
                    isArrayOrImmutable = true;
                    t = ((ArrayAccessTree) t).getExpression();
                }
                Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), t));
                if (el == field && (isArrayOrImmutable || checkAssignmentInsideExpression())
                        && !isInConstructorOfFieldClass(getCurrentPath(), field)
                        && !isInGetterSetter(getCurrentPath())) {
                    // check (++field + 3)
                    ExpressionTree invkgetter = createGetterInvokation(t);
                    if (isArrayOrImmutable) {
                        rewrite(t, invkgetter);
                    } else {
                        ExpressionTree setter = createMemberSelection(node.getExpression(), refactoring.getSetterName());

                        Tree.Kind operator = kind == Tree.Kind.POSTFIX_INCREMENT || kind == Tree.Kind.PREFIX_INCREMENT
                                ? Tree.Kind.PLUS
                                : Tree.Kind.MINUS;
                        MethodInvocationTree invksetter = make.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                setter,
                                Collections.singletonList(make.Binary(operator, invkgetter, make.Literal(1))));
                        rewrite(node, invksetter);
                    }
                }
            }
            return null;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, Element field) {
            if (!useAccessors) {
                return null;
            }
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el == field && !isInConstructorOfFieldClass(getCurrentPath(), field)
                    && !isInGetterSetter(getCurrentPath())) {
                ExpressionTree nodeNew = createGetterInvokation(node);
                rewrite(node, nodeNew);
            }
            return super.visitMemberSelect(node, field);
        }

        @Override
        public Tree visitIdentifier(IdentifierTree node, Element field) {
            if (!useAccessors) {
                return null;
            }
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el == field && !isInConstructorOfFieldClass(getCurrentPath(), field)
                    && !isInGetterSetter(getCurrentPath())) {
                ExpressionTree nodeNew = createGetterInvokation(node);
                rewrite(node, nodeNew);
            }
            return null;
        }

        private boolean checkAssignmentInsideExpression() {
            Tree exp1 = getCurrentPath().getLeaf();
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
                // XXX would be useful if Problems support HTML
//                String code = parent.toString();
//                String replace = exp1.toString();
//                code = code.replace(replace, "&lt;b&gt;" + replace + "&lt;/b&gt;");
                problem = createProblem(
                        problem,
                        false,
                        NbBundle.getMessage(
                                EncapsulateFieldRefactoringPlugin.class,
                                "ERR_EncapsulateInsideAssignment",
                                exp1.toString(),
                                parent.toString(),
                                FileUtil.getFileDisplayName(workingCopy.getFileObject())));
                return false;
            }
            return true;
        }
        
        /**
         * replace current expresion with the proper one.<p>
         * c.field -> c.getField()
         * field -> getField()
         * or copy in case of refactoring.getGetterName() == null
         */
        private ExpressionTree createGetterInvokation(ExpressionTree current) {
            // check if exist refactoring.getGetterName() != null and visibility (subclases)
            if (refactoring.getGetterName() == null) {
                return current;
            }
            ExpressionTree getter = createMemberSelection(current, refactoring.getGetterName());
            
            MethodInvocationTree invkgetter = make.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),
                    getter,
                    Collections.<ExpressionTree>emptyList());
            return invkgetter;
        }
        
        private ExpressionTree createMemberSelection(ExpressionTree node, String name) {
            ExpressionTree selector;
            if (node.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree select = (MemberSelectTree) node;
                selector = make.MemberSelect(select.getExpression(), name);
            } else {
                selector = make.Identifier(name);
            }
            return selector;
        }
        
        private void createGetterAndSetter(ClassTree node, VariableElement field) {
            String fieldName = field.getSimpleName().toString();
            boolean staticMod = field.getModifiers().contains(Modifier.STATIC);
            String parName = staticMod ? "a" + getCapitalizedName(field) : stripPrefix(fieldName); //NOI18N
            String getterBody = "{return " + fieldName + ";}"; //NOI18N
            String setterBody = (staticMod? "{": "{this.") + fieldName + " = " + parName + ";}"; //NOI18N
            
            Set<Modifier> mods = new HashSet<Modifier>(refactoring.getMethodModifiers());
            if (staticMod) {
                mods.add(Modifier.STATIC);
            }
            
            VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
            ClassTree newNode = null;

            ExecutableElement getterElm = null;
            if (refactoring.getGetterName() != null) {
                getterElm = findMethod(
                        workingCopy,
                        (TypeElement) field.getEnclosingElement(),
                        refactoring.getGetterName(),
                        Collections.<VariableElement>emptyList(), false);
            }
            if (getterElm == null && refactoring.getGetterName() != null) {
                MethodTree getter = make.Method(
                        make.Modifiers(mods),
                        refactoring.getGetterName(),
                        fieldTree.getType(),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        getterBody,
                        null);
                newNode = make.addClassMember(node, getter);
            }
            
            ExecutableElement setterElm = null;
            if (refactoring.getSetterName() != null) {
                setterElm = findMethod(
                        workingCopy,
                        (TypeElement) field.getEnclosingElement(),
                        refactoring.getSetterName(),
                        Collections.<VariableElement>singletonList(field), false);
            }
            if (setterElm == null && refactoring.getSetterName() != null) {
                VariableTree paramTree = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()), parName, fieldTree.getType(), null);
                MethodTree setter = make.Method(
                        make.Modifiers(mods),
                        refactoring.getSetterName(),
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.singletonList(paramTree),
                        Collections.<ExpressionTree>emptyList(),
                        setterBody,
                        null);
                newNode = make.addClassMember(newNode == null? node: newNode, setter);
            }
            if (newNode != null) {
                rewrite(node, newNode);
            }
        }
        
        private void resolveFieldDeclaration(VariableTree node, Element field) {
            Modifier currentAccess = getAccessibility(field.getModifiers());
            Modifier futureAccess = getAccessibility(refactoring.getFieldModifiers());
            if (currentAccess != futureAccess) {
                ModifiersTree modTree = make.Modifiers(replaceAccessibility(currentAccess, futureAccess, field), node.getModifiers().getAnnotations());
                VariableTree newNode = make.Variable(modTree, node.getName(), node.getType(), node.getInitializer());
                rewrite(node, newNode);
            }
        }
        
        private boolean resolveAlwaysUseAccessors(TypeElement where, Element field) {
            if (refactoring.isAlwaysUseAccessors()) {
                return true;
            }
            
            // target field accessibility
            Set<Modifier> mods = refactoring.getFieldModifiers();
            if (mods.contains(Modifier.PRIVATE)) {
                // check enclosing top level class
                return SourceUtils.getOutermostEnclosingTypeElement(where) != SourceUtils.getOutermostEnclosingTypeElement(field);
            }
            
            if (mods.contains(Modifier.PROTECTED)) {
                // check inheritance
                if (isSubclassOf(where, (TypeElement) field.getEnclosingElement())) {
                    return false;
                }
                // check same package
                return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(field);
            }
            
            if (mods.contains(Modifier.PUBLIC)) {
                return false;
            }
            
            // default access
            // check same package
            return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(field);
        }

        private boolean isInConstructorOfFieldClass(TreePath path, Element field) {
            Tree leaf = path.getLeaf();
            Kind kind = leaf.getKind();
            while (true) {
                switch (kind) {
                case METHOD:
                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
                        return false;
                    }
                    Element m = workingCopy.getTrees().getElement(path);
                    return m.getKind() == ElementKind.CONSTRUCTOR
                            && (m.getEnclosingElement() == field.getEnclosingElement()
                                || isSubclassOf((TypeElement) m.getEnclosingElement(), (TypeElement) field.getEnclosingElement()));
                case COMPILATION_UNIT:
                case CLASS:
                case NEW_CLASS:
                    return false;
                }
                path = path.getParentPath();
                leaf = path.getLeaf();
                kind = leaf.getKind();
            }
        }

        private boolean isInGetterSetter(TreePath path) {
            if (sourceFile != workingCopy.getFileObject()) {
                return false;
            }
            
            Tree leaf = path.getLeaf();
            Kind kind = leaf.getKind();
            while (true) {
                switch (kind) {
                case METHOD:
                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
                        return false;
                    }
                    Element m = workingCopy.getTrees().getElement(path);
                    return currentGetter != null && m == currentGetter.resolve(workingCopy)
                            || currentSetter != null && m == currentSetter.resolve(workingCopy);
                case COMPILATION_UNIT:
                case CLASS:
                case NEW_CLASS:
                    return false;
                }
                path = path.getParentPath();
                leaf = path.getLeaf();
                kind = leaf.getKind();
            }
        }
        
    }
    
}
