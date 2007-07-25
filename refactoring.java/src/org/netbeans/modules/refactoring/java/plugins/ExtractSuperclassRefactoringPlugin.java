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

import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/** Plugin that implements the core functionality of Extract Super Class refactoring.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public final class ExtractSuperclassRefactoringPlugin extends JavaRefactoringPlugin {
    /** Reference to the parent refactoring instance */
    private final ExtractSuperclassRefactoring refactoring;
    
    /** source class */
    private ElementHandle<TypeElement> classHandle;
        
    private String pkgName;

    /** Creates a new instance of ExtractSuperClassRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    ExtractSuperclassRefactoringPlugin(ExtractSuperclassRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    protected JavaSource getJavaSource(Phase p) {
        return JavaSource.forFileObject(refactoring.getSourceType().getFileObject());
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        // fire operation start on the registered progress listeners (2 step)
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        javac.toPhase(JavaSource.Phase.RESOLVED);
        try {
            TreePathHandle sourceType = refactoring.getSourceType();
            
            // check whether the element is valid
            Problem result = isElementAvail(sourceType, javac);
            if (result != null) {
                // fatal error -> don't continue with further checks
                return result;
            }
            if (!RetoucheUtils.isElementInOpenProject(sourceType.getFileObject())) {
                return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ProjectNotOpened")); // NOI18N
            }
            
            // check whether the element is an unresolved class
            Element sourceElm = sourceType.resolveElement(javac);
            if (sourceElm == null || (sourceElm.getKind() != ElementKind.CLASS)) {
                // fatal error -> return
                return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
            }
            
            classHandle = ElementHandle.<TypeElement>create((TypeElement) sourceElm);
            
            PackageElement pkgElm = (PackageElement) javac.getElementUtilities().outermostTypeElement(sourceElm).getEnclosingElement();
            pkgName = pkgElm.getQualifiedName().toString();
            
            // increase progress (step 1)
            fireProgressListenerStep();
            
            // all checks passed -> return null
            return null;
        } finally {
            // fire operation end on the registered progress listeners
            fireProgressListenerStop();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        Problem result = null;
        
        String newName = refactoring.getSuperClassName();
        
        if (!Utilities.isJavaIdentifier(newName)) {
            result = createProblem(result, true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_InvalidIdentifier", newName)); // NOI18N
            return result;
        }
        
        FileObject primFile = refactoring.getSourceType().getFileObject();
        FileObject folder = primFile.getParent();
        FileObject[] children = folder.getChildren();
        for (FileObject child: children) {
            if (!child.isVirtual() && child.getName().equals(newName) && "java".equals(child.getExt())) { // NOI18N
                result = createProblem(result, true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ClassClash", newName, pkgName)); // NOI18N
                return result;
            }
        }

        return null;
    }

    @Override
    public Problem checkParameters() {
        MemberInfo[] members = refactoring.getMembers();
        if (members.length == 0) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ExtractSuperClass_MembersNotAvailable")); // NOI18N);
        }
        return super.checkParameters();

    }
    
    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        
        TypeElement sourceType = (TypeElement) refactoring.getSourceType().resolveElement(javac);
        assert sourceType != null;
        
        Set<? extends Element> members = new HashSet<Element>(sourceType.getEnclosedElements());
        
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, refactoring.getMembers().length);
        try {
            for (MemberInfo info : refactoring.getMembers()) {
                Problem p = null;
                switch(info.getGroup()) {
                case FIELD:
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> vehandle = (ElementHandle<VariableElement>) info.getElementHandle();
                    VariableElement field = vehandle.resolve(javac);
                    p = checkFieldParameter(javac, field, members);
                    break;
                case METHOD:
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> eehandle = (ElementHandle<ExecutableElement>) info.getElementHandle();
                    ExecutableElement method = eehandle.resolve(javac);
                    p = checkMethodParameter(javac, method, members);
                    break;
                }

                if (p != null) {
                    return p;
                }
                
                fireProgressListenerStep();
            }
        } finally {
            fireProgressListenerStop();
        }

        // XXX check refactoring.getImplements()

        return null;
    }
    
    private Problem checkFieldParameter(CompilationController javac, VariableElement elm, Set<? extends Element> members) throws IOException {
        if (elm == null) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
        }
        if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.FIELD) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
        if (!members.contains(elm)) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
//        Set<Modifier> mods = elm.getModifiers();
//        if (mods.contains(Modifier.PUBLIC) && mods.contains(Modifier.STATIC) && mods.contains(Modifier.FINAL)) {
//            VariableTree tree = (VariableTree) javac.getTrees().getTree(elm);
//            if (tree.getInitializer() != null) {
//                continue;
//            }
//        }
//        return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
        return null;
    }
    
    private Problem checkMethodParameter(CompilationController javac, ExecutableElement elm, Set<? extends Element> members) throws IOException {
        if (elm == null) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
        }
        if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.METHOD) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
        if (!members.contains(elm)) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
//        Set<Modifier> mods = elm.getModifiers();
//        if (!mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.STATIC)) {
//            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
//        }
        return null;
        
    }

    public Problem prepare(RefactoringElementsBag bag) {
        FileObject primFile = refactoring.getSourceType().getFileObject();
        try {
            bag.add(refactoring, new CreateSuperclassElement(refactoring, primFile.getParent(), classHandle));
            UpdateClassTask.create(bag, primFile, refactoring, classHandle);
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }
        return null;
    }
    
    private static List<TypeMirror> findUsedGenericTypes(CompilationInfo javac, TypeElement javaClass,ExtractSuperclassRefactoring refactoring) {
        List<TypeMirror> typeArgs = RetoucheUtils.resolveTypeParamsAsTypes(javaClass.getTypeParameters());
        if (typeArgs.isEmpty())
            return typeArgs;
        
        Types typeUtils = javac.getTypes();
        List<TypeMirror> result = new ArrayList<TypeMirror>(typeArgs.size());
        
        // check super class
        TypeMirror superClass = javaClass.getSuperclass();
        RetoucheUtils.findUsedGenericTypes(typeUtils, typeArgs, result, superClass);
        
        MemberInfo[] members = refactoring.getMembers();
        for (int i = 0; i < members.length && !typeArgs.isEmpty(); i++) {
            if (members[i].getGroup() == MemberInfo.Group.METHOD) {
            // check methods
                @SuppressWarnings("unchecked")
                ElementHandle<ExecutableElement> handle = (ElementHandle<ExecutableElement>) members[i].getElementHandle();
                ExecutableElement elm = handle.resolve(javac);
            
                RetoucheUtils.findUsedGenericTypes(typeUtils, typeArgs, result, elm.getReturnType());

                for (Iterator<? extends VariableElement> paramIter = elm.getParameters().iterator(); paramIter.hasNext() && !typeArgs.isEmpty();) {
                    VariableElement param = paramIter.next();
                    RetoucheUtils.findUsedGenericTypes(typeUtils, typeArgs, result, param.asType());
                }
            } else if (members[i].getGroup() == MemberInfo.Group.IMPLEMENTS) {
                // check implements
                TypeMirrorHandle handle = (TypeMirrorHandle) members[i].getElementHandle();
                TypeMirror implemetz = handle.resolve(javac);
                RetoucheUtils.findUsedGenericTypes(typeUtils, typeArgs, result, implemetz);
            }
            // do not check fields since static fields cannot use type parameter of the enclosing class
        }
        
        return result;
    }
    
    // --- REFACTORING ELEMENTS ------------------------------------------------
    
    private static final class CreateSuperclassElement extends SimpleRefactoringElementImplementation implements CancellableTask<WorkingCopy> {
        private final URL folderURL;
        private URL superClassURL;
        private final String superClassName;
        private final ExtractSuperclassRefactoring refactoring;
        private final ElementHandle<TypeElement> sourceType;
        
        private CreateSuperclassElement(ExtractSuperclassRefactoring refactoring, FileObject folder, ElementHandle<TypeElement> sourceType) {
            this.refactoring = refactoring;
            this.folderURL = URLMapper.findURL(folder, URLMapper.INTERNAL);
            this.superClassName = refactoring.getSuperClassName();
            this.sourceType = sourceType;
        }

        // --- SimpleRefactoringElementImpl methods ----------------------------------
        
        public void performChange() {
            try {
                FileObject folderFO = URLMapper.findFileObject(folderURL);
                if (folderFO == null)
                    return;
                
                // create new file
                
                // XXX not nice; user might modify the template to something entirely different from the standard template.
                FileObject tempFO = Repository.getDefault().getDefaultFileSystem().findResource("Templates/Classes/Class.java"); // NOI18N
                
                DataFolder folder = (DataFolder) DataObject.find(folderFO);
                DataObject template = DataObject.find(tempFO);
                DataObject newSuperClassDO = template.createFromTemplate(folder, superClassName);
                this.superClassURL = URLMapper.findURL(newSuperClassDO.getPrimaryFile(), URLMapper.INTERNAL);
                refactoring.getContext().add(newSuperClassDO.getPrimaryFile());
                
                // add type params and members
                JavaSource js = JavaSource.forFileObject(newSuperClassDO.getPrimaryFile());
                js.runModificationTask(this).commit();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        @Override
        public void undoChange() {
            FileObject ifcFO = null;
            if (superClassURL != null) {
                ifcFO = URLMapper.findFileObject(superClassURL);
            }
            if (ifcFO != null) {
                try {
                    ifcFO.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        public String getText() {
            return NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "TXT_ExtractSC_CreateSC", superClassName); // NOI18N
        }

        public String getDisplayText() {
            return getText();
        }

        public FileObject getParentFile() {
            return URLMapper.findFileObject(folderURL);
        }

        public PositionBounds getPosition() {
            return null;
        }
    
        public Lookup getLookup() {
            FileObject fo = superClassURL == null? null: URLMapper.findFileObject(superClassURL);
            return fo != null? Lookups.singleton(fo): Lookup.EMPTY;
        }
        
        // --- CancellableTask methods ----------------------------------
        
        public void cancel() {
            
        }

        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            ClassTree classTree = findClass(wc, superClassName);
            boolean makeAbstract = false;
            TreeMaker make = wc.getTreeMaker();
            
            // add type parameters
            List<TypeMirror> typeParams = findUsedGenericTypes(wc, sourceType.resolve(wc), refactoring);
            List<TypeParameterTree> newTypeParams = new ArrayList<TypeParameterTree>(typeParams.size());
            // lets retrieve param type trees from origin class since it is
            // almost impossible to create them via TreeMaker
            TypeElement sourceTypeElm = sourceType.resolve(wc);
            for (TypeParameterElement typeParam : sourceTypeElm.getTypeParameters()) {
                TypeMirror origParam = typeParam.asType();
                for (TypeMirror newParam : typeParams) {
                    if (wc.getTypes().isSameType(origParam, newParam)) {
                        Tree t = wc.getTrees().getTree(typeParam);
                        if (t.getKind() == Tree.Kind.TYPE_PARAMETER) {
                            newTypeParams.add((TypeParameterTree) t);
                        }
                    }
                }
            }

            // add fields, methods and implements
            List<Tree> members = new ArrayList<Tree>();
            List <Tree> implementsList = new ArrayList<Tree>();
            
            addConstructors(wc, sourceTypeElm, members);
            
            for (MemberInfo member : refactoring.getMembers()) {
                if (member.getGroup() == MemberInfo.Group.FIELD) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> handle = (ElementHandle<VariableElement>) member.getElementHandle();
                    VariableElement elm = handle.resolve(wc);
                    VariableTree tree = (VariableTree) wc.getTrees().getTree(elm);
                    // TODO: copying the tree is workaround for the issue #101395
                    // When issue will be correctly claused, copy can be removed
                    // and original tree added to members.
                    VariableTree copy = make.Variable(
                            make.Modifiers(tree.getModifiers().getFlags(), tree.getModifiers().getAnnotations()),
                            tree.getName(),
                            tree.getType(),
                            tree.getInitializer()
                    );
                    members.add(copy);
                } else if (member.getGroup() == MemberInfo.Group.METHOD) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> handle = (ElementHandle<ExecutableElement>) member.getElementHandle();
                    ExecutableElement elm = handle.resolve(wc);
                    MethodTree methodTree = wc.getTrees().getTree(elm);
                    if (member.isMakeAbstract() && !elm.getModifiers().contains(Modifier.ABSTRACT)) {
                        methodTree = make.Method(
                                makeAbstract(make, methodTree.getModifiers()),
                                methodTree.getName(),
                                methodTree.getReturnType(),
                                methodTree.getTypeParameters(),
                                methodTree.getParameters(),
                                methodTree.getThrows(),
                                (BlockTree) null,
                                null);
                    }
                    makeAbstract |= methodTree.getModifiers().getFlags().contains(Modifier.ABSTRACT);
                    members.add(methodTree);
                } else if (member.getGroup() == MemberInfo.Group.IMPLEMENTS) {
                    TypeMirrorHandle handle = (TypeMirrorHandle) member.getElementHandle();
                    // XXX check if interface is not aready there; the templates might be changed by user :-(
                    TypeMirror implMirror = handle.resolve(wc);
                    implementsList.add(make.Type(implMirror));
                    // XXX needs more granular check
                    makeAbstract |= true;
                }
            }

            // create superclass
            Tree superClass = makeSuperclass(make, sourceTypeElm);
            
            // create new class
            ClassTree newClassTree = make.Class(
                    makeAbstract? makeAbstract(make, classTree.getModifiers()): classTree.getModifiers(),
                    classTree.getSimpleName(),
                    newTypeParams,
                    superClass,
                    implementsList,
                    members);
            
            wc.rewrite(classTree, newClassTree);
        }
        
        // --- helper methods ----------------------------------
        
        private static ClassTree findClass(CompilationInfo javac, String name) {
            for (Tree tree : javac.getCompilationUnit().getTypeDecls()) {
                if (Tree.Kind.CLASS == tree.getKind()
                        && !javac.getTreeUtilities().isInterface((ClassTree) tree)
                        && !javac.getTreeUtilities().isAnnotation((ClassTree) tree)
                        && !javac.getTreeUtilities().isEnum((ClassTree) tree)
                        && name.contentEquals(((ClassTree) tree).getSimpleName())) {
                    return (ClassTree) tree;
                }
            }
            throw new IllegalStateException("wrong template, cannot find the class in " + javac.getFileObject()); // NOI18N
        }
        
        private static ModifiersTree makeAbstract(TreeMaker make, ModifiersTree oldMods) {
            if (oldMods.getFlags().contains(Modifier.ABSTRACT)) {
                return oldMods;
            }
            Set<Modifier> flags = new HashSet<Modifier>(oldMods.getFlags());
            flags.add(Modifier.ABSTRACT);
            flags.remove(Modifier.FINAL);
            return make.Modifiers(flags, oldMods.getAnnotations());
        }
        
        private static Tree makeSuperclass(TreeMaker make, TypeElement clazz) {
            DeclaredType supType = (DeclaredType) clazz.getSuperclass();
            TypeElement supEl = (TypeElement) supType.asElement();
            return supEl.getSuperclass().getKind() == TypeKind.NONE
                    ? null
                    : make.Type(supType);
        }
        
        /* in case there are constructors delegating to old superclass it is necessery to create delegates in new superclass */
        private static void addConstructors(final WorkingCopy javac, final TypeElement origClass, final List<Tree> members) {
            final TreeMaker make = javac.getTreeMaker();
            // cache of already resolved constructors
            final Set<Element> added = new HashSet<Element>();
            for (ExecutableElement constr : ElementFilter.constructorsIn(origClass.getEnclosedElements())) {
                if (javac.getElementUtilities().isSynthetic(constr)) {
                    continue;
                }
                
                TreePath path = javac.getTrees().getPath(constr);
                MethodTree mc = (MethodTree) (path != null? path.getLeaf(): null);
                if (mc != null) {
                    for (StatementTree stmt : mc.getBody().getStatements()) {
                        // search super(...); statement
                        if (stmt.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                            ExpressionStatementTree estmt = (ExpressionStatementTree) stmt;
                            boolean isSyntheticSuper = javac.getTreeUtilities().isSynthetic(javac.getTrees().getPath(path.getCompilationUnit(), estmt));
                            ExpressionTree expr = estmt.getExpression();
                            TreePath expath = javac.getTrees().getPath(path.getCompilationUnit(), expr);
                            Element el = javac.getTrees().getElement(expath);
                            if (el != null && el.getKind() == ElementKind.CONSTRUCTOR && added.add(el)) {
                                MethodTree template = (MethodTree) javac.getTrees().getTree(el);
                                MethodInvocationTree invk = (MethodInvocationTree) expr;
                                // create constructor block with super call
                                BlockTree block = isSyntheticSuper
                                        ? make.Block(Collections.<StatementTree>emptyList(), false)
                                        : make.Block(Collections.<StatementTree>singletonList(
                                            make.ExpressionStatement(
                                                make.MethodInvocation(
                                                    Collections.<ExpressionTree>emptyList(),
                                                    invk.getMethodSelect(),
                                                    params2Arguments(make, template.getParameters())
                                                ))), false);
                                // create constructor
                                MethodTree newConstr = make.Constructor(
                                        template.getModifiers(),
                                        template.getTypeParameters(),
                                        template.getParameters(),
                                        template.getThrows(),
                                        block);
                                members.add(newConstr);
                            }
                            
                        }
                        // take just first statement super(...)
                        break;
                    }
                }
            }
        }
        
        private static List<? extends ExpressionTree> params2Arguments(TreeMaker make, List<? extends VariableTree> params) {
            if (params.isEmpty()) {
                return Collections.<ExpressionTree>emptyList();
            }
            List<ExpressionTree> args = new ArrayList<ExpressionTree>(params.size());
            for (VariableTree param : params) {
                args.add(make.Identifier(param.getName()));
            }
            return args;
        }
        
    }
    
    private final static class UpdateClassTask implements CancellableTask<WorkingCopy> {
        private final ExtractSuperclassRefactoring refactoring;
        private final ElementHandle<TypeElement> sourceType;
        
        private UpdateClassTask(ExtractSuperclassRefactoring refactoring, ElementHandle<TypeElement> sourceType) {
            this.sourceType = sourceType;
            this.refactoring = refactoring;
        }
        
        public static void create(RefactoringElementsBag bag, FileObject fo,ExtractSuperclassRefactoring refactoring, ElementHandle<TypeElement> sourceType) throws IOException {
            JavaSource js = JavaSource.forFileObject(fo);
            ModificationResult modification = js.runModificationTask(new UpdateClassTask(refactoring, sourceType));
            List<? extends ModificationResult.Difference> diffs = modification.getDifferences(fo);
            for (ModificationResult.Difference diff : diffs) {
                bag.add(refactoring, DiffElement.create(diff, fo, modification));
            }
            bag.registerTransaction(new RetoucheCommit(Collections.singletonList(modification)));
        }
        
        public void cancel() {
        }

        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement clazz = this.sourceType.resolve(wc);
            assert clazz != null;
            ClassTree classTree = wc.getTrees().getTree(clazz);
            TreeMaker make = wc.getTreeMaker();
            // fake interface since interface file does not exist yet
            Tree superClassTree;
            List<TypeMirror> typeParams = findUsedGenericTypes(wc, clazz, refactoring);
            if (typeParams.isEmpty()) {
                superClassTree = make.Identifier(refactoring.getSuperClassName());
            } else {
                List<ExpressionTree> typeParamTrees = new ArrayList<ExpressionTree>(typeParams.size());
                for (TypeMirror typeParam : typeParams) {
                    Tree t = make.Type(typeParam);
                    typeParamTrees.add((ExpressionTree) t);
                }
                superClassTree = make.ParameterizedType(
                        make.Identifier(refactoring.getSuperClassName()),
                        typeParamTrees
                        );
            }
            
            Set<Tree> members2Remove = new HashSet<Tree>();
            Set<Tree> interfaces2Remove = new HashSet<Tree>();
            
            members2Remove.addAll(getMembers2Remove(wc, refactoring.getMembers()));
            interfaces2Remove.addAll(getImplements2Remove(wc, refactoring.getMembers(), clazz));
            
            // filter out obsolete members
            List<Tree> members2Add = new ArrayList<Tree>();
            for (Tree tree : classTree.getMembers()) {
                if (!members2Remove.contains(tree)) {
                    members2Add.add(tree);
                }
            }
            // filter out obsolete implements trees
            List<Tree> impls2Add = resolveImplements(classTree.getImplementsClause(), interfaces2Remove);

            ClassTree nc;
            nc = make.Class(
                    classTree.getModifiers(),
                    classTree.getSimpleName(),
                    classTree.getTypeParameters(),
                    superClassTree,
                    impls2Add,
                    members2Add);
            
            wc.rewrite(classTree, nc);
        }
        
        private List<Tree> getMembers2Remove(CompilationInfo javac,MemberInfo[] members) {
            if (members == null || members.length == 0) {
                return Collections.<Tree>emptyList();
            }
            List<Tree> result = new ArrayList<Tree>(members.length);
            for (MemberInfo member : members) {
                if (member.getGroup() == MemberInfo.Group.FIELD) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> handle = (ElementHandle<VariableElement>) member.getElementHandle();
                    VariableElement elm = handle.resolve(javac);
                    assert elm != null;
                    Tree t = javac.getTrees().getTree(elm);
                    assert t != null;
                    result.add(t);
                } else if (member.getGroup() == MemberInfo.Group.METHOD && !member.isMakeAbstract()) {
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> handle = (ElementHandle<ExecutableElement>) member.getElementHandle();
                    ExecutableElement elm = handle.resolve(javac);
                    assert elm != null;
                    Tree t = javac.getTrees().getTree(elm);
                    assert t != null;
                    result.add(t);
                }
                
            }

            return result;
        }
        
        private List<Tree> getImplements2Remove(CompilationInfo javac,MemberInfo[] members, TypeElement clazz) {
            if (members == null || members.length == 0) {
                return Collections.<Tree>emptyList();
            }
            
            // resolve members to remove
            List<TypeMirror> memberTypes = new ArrayList<TypeMirror>(members.length);
            for (MemberInfo member : members) {
                if (member.getGroup() == MemberInfo.Group.IMPLEMENTS) {
                    TypeMirrorHandle handle = (TypeMirrorHandle) member.getElementHandle();
                    TypeMirror tm = handle.resolve(javac);
                    memberTypes.add(tm);
                }
            }

            
            ClassTree classTree = javac.getTrees().getTree(clazz);
            List<Tree> result = new ArrayList<Tree>();
            Types types = javac.getTypes();
            
            // map TypeMirror to Tree
            for (Tree tree : classTree.getImplementsClause()) {
                TreePath path = javac.getTrees().getPath(javac.getCompilationUnit(), tree);
                TypeMirror existingTM = javac.getTrees().getTypeMirror(path);
                
                for (TypeMirror tm : memberTypes) {
                    if (types.isSameType(tm, existingTM)) {
                        result.add(tree);
                        break;
                    }
                }
            }

            return result;
        }
        
        private static List<Tree> resolveImplements(List<? extends Tree> allImpls, Set<Tree> impls2Remove) {
            List<Tree> ret;
            if (allImpls == null) {
                ret = new ArrayList<Tree>(1);
            } else {
                ret = new ArrayList<Tree>(allImpls.size() + 1);
                ret.addAll(allImpls);
            }
            
            if (impls2Remove != null && !impls2Remove.isEmpty()) {
                ret.removeAll(impls2Remove);
            }
            return ret;
        }
    }
}
