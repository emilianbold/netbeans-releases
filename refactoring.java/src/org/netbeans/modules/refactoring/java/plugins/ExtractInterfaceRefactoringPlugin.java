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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
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
import javax.lang.model.type.TypeMirror;
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
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
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

/**
 * Plugin that implements the core functionality of Extract Interface refactoring.
 * <br>Extracts: <ul>
 * <li>implements interfaces</li>
 * <li>public nonstatic methods</li>
 * <li>public static final fields</li>
 * <li>XXX public static class/interface/enum/annotation type.<br><i>dangerous, it might contain
 *     elements that will be unaccessible from the new interface. Maybe reusing Move Class refactoring
 *     would be appropriate. Not implemented in 6.0 yet. Pre-6.0 implementation was not solved references at all.</i></li>
 * </ul>
 * XXX there should be option Copy/Move/AsIs javadoc.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public final class ExtractInterfaceRefactoringPlugin extends JavaRefactoringPlugin {
    
    /** Reference to the parent refactoring instance */
    private final ExtractInterfaceRefactoring refactoring;
    
    private String pkgName;
    
    /** class for extracting interface */
    private ElementHandle<TypeElement> classHandle;
    
    /** Creates a new instance of ExtractInterfaceRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    ExtractInterfaceRefactoringPlugin(ExtractInterfaceRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem fastCheckParameters() {
        Problem result = null;
        
        String newName = refactoring.getInterfaceName();
        
        if (!Utilities.isJavaIdentifier(newName)) {
            result = createProblem(result, true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_InvalidIdentifier", newName)); // NOI18N
            return result;
        }
        
        FileObject primFile = refactoring.getSourceType().getFileObject();
        FileObject folder = primFile.getParent();
        FileObject[] children = folder.getChildren();
        for (FileObject child: children) {
            if (!child.isVirtual() && child.getName().equals(newName) && "java".equals(child.getExt())) { // NOI18N
                result = createProblem(result, true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ClassClash", newName, pkgName)); // NOI18N
                return result;
            }
        }

        return null;
    }

    public Problem prepare(RefactoringElementsBag bag) {
        FileObject primFile = refactoring.getSourceType().getFileObject();
        try {
            // create interface file
            bag.add(refactoring, new CreateInterfaceElement(refactoring, primFile.getParent(), classHandle));
            UpdateClassTask.create(bag, primFile, refactoring, classHandle);
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }
        return null;
    }
    
    protected JavaSource getJavaSource(Phase p) {
        return JavaSource.forFileObject(refactoring.getSourceType().getFileObject());
    }
    
    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        // fire operation start on the registered progress listeners (1 step)
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 1);
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
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ProjectNotOpened")); // NOI18N
            }
            
            // check whether the element is an unresolved class
            Element sourceElm = sourceType.resolveElement(javac);
            if (sourceElm == null || (sourceElm.getKind() != ElementKind.CLASS && sourceElm.getKind() != ElementKind.INTERFACE && sourceElm.getKind() != ElementKind.ENUM)) {
                // fatal error -> return
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
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
    protected Problem checkParameters(CompilationController javac) throws IOException {
        if (refactoring.getMethods().isEmpty() && refactoring.getFields().isEmpty() && refactoring.getImplements().isEmpty()) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_MembersNotAvailable")); // NOI18N);
        }
        // check whether the selected members are public and non-static in case of methods, static in other cases
        // check whether all members belong to the source type
        // XXX check if method params and return type will be accessible after extraction; likely not fatal
        javac.toPhase(JavaSource.Phase.RESOLVED);
        
        TypeElement sourceType = (TypeElement) refactoring.getSourceType().resolveElement(javac);
        assert sourceType != null;
        
        Set<? extends Element> members = new HashSet<Element>(sourceType.getEnclosedElements());
        
        for (ElementHandle<ExecutableElement> elementHandle : refactoring.getMethods()) {
            ExecutableElement elm = elementHandle.resolve(javac);
            if (elm == null) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
            }
            if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.METHOD) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            if (!members.contains(elm)) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            Set<Modifier> mods = elm.getModifiers();
            if (!mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.STATIC)) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
            }
        }
        
        for (ElementHandle<VariableElement> elementHandle : refactoring.getFields()) {
            VariableElement elm = elementHandle.resolve(javac);
            if (elm == null) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
            }
            if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.FIELD) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            if (!members.contains(elm)) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            Set<Modifier> mods = elm.getModifiers();
            if (mods.contains(Modifier.PUBLIC) && mods.contains(Modifier.STATIC) && mods.contains(Modifier.FINAL)) {
                VariableTree tree = (VariableTree) javac.getTrees().getTree(elm);
                if (tree.getInitializer() != null) {
                    continue;
                }
            }
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
        }
        
        // XXX check refactoring.getImplements()

        return null;
    }
    
    /**
     * Finds all type parameters of <code>javaClass</code> that are referenced by
     * any member that is going to be extract.
     * @param refactoring the refactoring containing members to extract
     * @param javac compilation info
     * @param javaClass java class declaring parameters to find
     * @return type parameters to extract
     */
    private static List<TypeMirror> findUsedGenericTypes(ExtractInterfaceRefactoring refactoring, CompilationInfo javac, TypeElement javaClass) {
        List<TypeMirror> typeArgs = RetoucheUtils.resolveTypeParamsAsTypes(javaClass.getTypeParameters());
        if (typeArgs.isEmpty())
            return typeArgs;
        
        Types typeUtils = javac.getTypes();
        List<TypeMirror> result = new ArrayList<TypeMirror>(typeArgs.size());

        // do not check fields since static fields cannot use type parameter of the enclosing class
        
        // check methods
        for (Iterator<ElementHandle<ExecutableElement>> methodIter = refactoring.getMethods().iterator(); methodIter.hasNext() && !typeArgs.isEmpty();) {
            ElementHandle<ExecutableElement> handle = methodIter.next();
            ExecutableElement elm = handle.resolve(javac);
            
            RetoucheUtils.findUsedGenericTypes(typeUtils, typeArgs, result, elm.getReturnType());
            
            for (Iterator<? extends VariableElement> paramIter = elm.getParameters().iterator(); paramIter.hasNext() && !typeArgs.isEmpty();) {
                VariableElement param = paramIter.next();
                RetoucheUtils.findUsedGenericTypes(typeUtils, typeArgs, result, param.asType());
            }
        }
        
        // check implements
        for (Iterator<TypeMirrorHandle<TypeMirror>> it = refactoring.getImplements().iterator(); it.hasNext() && !typeArgs.isEmpty();) {
            TypeMirrorHandle<TypeMirror> handle = it.next();
            TypeMirror implemetz = handle.resolve(javac);
            RetoucheUtils.findUsedGenericTypes(typeUtils, typeArgs, result, implemetz);
        }

        return result;
    }

    // --- REFACTORING ELEMENTS ------------------------------------------------
    
    /**
     * creates new file with empty interface and adds type params if necessary
     */
    private static final class CreateInterfaceElement extends SimpleRefactoringElementImplementation implements CancellableTask<WorkingCopy> {
        private final URL folderURL;
        private URL ifcURL;
        private final String ifcName;
        private final ExtractInterfaceRefactoring refactoring;
        private final ElementHandle<TypeElement> sourceType;
        
        private CreateInterfaceElement(ExtractInterfaceRefactoring refactoring, FileObject folder, ElementHandle<TypeElement> sourceType) {
            this.refactoring = refactoring;
            this.folderURL = URLMapper.findURL(folder, URLMapper.INTERNAL);
            this.ifcName = refactoring.getInterfaceName();
            this.sourceType = sourceType;
        }

        // --- SimpleRefactoringElementImpl methods ----------------------------------
        
        public void performChange() {
            try {
                FileObject folderFO = URLMapper.findFileObject(folderURL);
                if (folderFO == null)
                    return;
                
                // create new file
                
                // XXX not nice; user might modify the template to something entirely different from the interface
                FileObject tempFO = Repository.getDefault().getDefaultFileSystem().findResource("Templates/Classes/Interface.java"); // NOI18N
                
                DataFolder folder = (DataFolder) DataObject.find(folderFO);
                DataObject template = DataObject.find(tempFO);
                DataObject newIfcDO = template.createFromTemplate(folder, ifcName);
                this.ifcURL = URLMapper.findURL(newIfcDO.getPrimaryFile(), URLMapper.INTERNAL);
                refactoring.getContext().add(newIfcDO.getPrimaryFile());
                
                // add type params and members
                JavaSource js = JavaSource.forFileObject(newIfcDO.getPrimaryFile());
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
            if (ifcURL != null) {
                ifcFO = URLMapper.findFileObject(ifcURL);
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
            return NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "TXT_ExtractInterface_CreateIfc", ifcName); // NOI18N
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
            FileObject fo = ifcURL == null? null: URLMapper.findFileObject(ifcURL);
            return fo != null? Lookups.singleton(fo): Lookup.EMPTY;
        }
        
        // --- CancellableTask methods ----------------------------------
        
        public void cancel() {
            
        }

        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            ClassTree interfaceTree = findInterface(wc, ifcName);
            TreeMaker make = wc.getTreeMaker();
            
            
            // add type parameters
            List<TypeMirror> typeParams = findUsedGenericTypes(refactoring, wc, sourceType.resolve(wc));
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

            // add new fields
            List<Tree> members = new ArrayList<Tree>();
            for (ElementHandle<VariableElement> handle : refactoring.getFields()) {
                VariableElement memberElm = handle.resolve(wc);
                Tree tree = wc.getTrees().getTree(memberElm);
                members.add(tree);
            }
            // add newmethods
            for (ElementHandle<ExecutableElement> handle : refactoring.getMethods()) {
                ExecutableElement memberElm = handle.resolve(wc);
                members.add(make.Method(memberElm, null));
            }
            // add super interfaces
            List <Tree> extendsList = new ArrayList<Tree>();
            extendsList.addAll(interfaceTree.getImplementsClause());
            for (TypeMirrorHandle<? extends TypeMirror> handle : refactoring.getImplements()) {
                // XXX check if interface is not aready there; the templates might be changed by user :-(
                TypeMirror implMirror = handle.resolve(wc);
                extendsList.add(make.Type(implMirror));
            }
            // create new interface
            ClassTree newInterfaceTree = make.Interface(
                    interfaceTree.getModifiers(),
                    interfaceTree.getSimpleName(),
                    newTypeParams,
                    extendsList,
                    members);
            
            wc.rewrite(interfaceTree, newInterfaceTree);
        }
        
        // --- helper methods ----------------------------------
        
        private ClassTree findInterface(CompilationInfo javac, String name) {
            for (Tree tree : javac.getCompilationUnit().getTypeDecls()) {
                if (Tree.Kind.CLASS == tree.getKind()
                        && javac.getTreeUtilities().isInterface((ClassTree) tree)
                        && name.contentEquals(((ClassTree) tree).getSimpleName())) {
                    return (ClassTree) tree;
                }
            }
            throw new IllegalStateException("wrong template, cannot find the interface in " + javac.getFileObject()); // NOI18N
        }
    }
    
    private final static class UpdateClassTask implements CancellableTask<WorkingCopy> {
        private final ExtractInterfaceRefactoring refactoring;
        private final ElementHandle<TypeElement> sourceType;
        
        private UpdateClassTask(ExtractInterfaceRefactoring refactoring, ElementHandle<TypeElement> sourceType) {
            this.sourceType = sourceType;
            this.refactoring = refactoring;
        }
        
        public static void create(RefactoringElementsBag bag, FileObject fo, ExtractInterfaceRefactoring refactoring, ElementHandle<TypeElement> sourceType) throws IOException {
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
            TreeMaker maker = wc.getTreeMaker();
            // fake interface since interface file does not exist yet
            Tree interfaceTree;
            List<TypeMirror> typeParams = findUsedGenericTypes(refactoring, wc, clazz);
            if (typeParams.isEmpty()) {
                interfaceTree = maker.Identifier(refactoring.getInterfaceName());
            } else {
                List<ExpressionTree> typeParamTrees = new ArrayList<ExpressionTree>(typeParams.size());
                for (TypeMirror typeParam : typeParams) {
                    Tree t = maker.Type(typeParam);
                    typeParamTrees.add((ExpressionTree) t);
                }
                interfaceTree = maker.ParameterizedType(
                        maker.Identifier(refactoring.getInterfaceName()),
                        typeParamTrees
                        );
            }
            
            Set<Tree> members2Remove = new HashSet<Tree>();
            Set<Tree> interfaces2Remove = new HashSet<Tree>();
            
            members2Remove.addAll(getFields2Remove(wc, refactoring.getFields()));
            members2Remove.addAll(getMethods2Remove(wc, refactoring.getMethods(), clazz));
            interfaces2Remove.addAll(getImplements2Remove(wc, refactoring.getImplements(), clazz));
            
            // filter out obsolete members
            List<Tree> members2Add = new ArrayList<Tree>();
            for (Tree tree : classTree.getMembers()) {
                if (!members2Remove.contains(tree)) {
                    members2Add.add(tree);
                }
            }
            // filter out obsolete implements trees
            List<Tree> impls2Add = resolveImplements(classTree.getImplementsClause(), interfaces2Remove, interfaceTree);

            ClassTree nc;
            if (clazz.getKind() == ElementKind.CLASS) {
                nc = maker.Class(
                        classTree.getModifiers(),
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        classTree.getExtendsClause(),
                        impls2Add,
                        members2Add);
            } else if (clazz.getKind() == ElementKind.INTERFACE) {
                nc = maker.Interface(
                        classTree.getModifiers(),
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        impls2Add,
                        members2Add);
            } else if (clazz.getKind() == ElementKind.ENUM) {
                nc = maker.Enum(
                        classTree.getModifiers(),
                        classTree.getSimpleName(),
                        impls2Add,
                        members2Add);
            } else {
                throw new IllegalStateException(classTree.toString());
            }
            
            wc.rewrite(classTree, nc);
        }
        
        private List<Tree> getFields2Remove(CompilationInfo javac, List<ElementHandle<VariableElement>> members) {
            if (members.isEmpty()) {
                return Collections.<Tree>emptyList();
            }
            List<Tree> result = new ArrayList<Tree>(members.size());
            for (ElementHandle<VariableElement> handle : members) {
                VariableElement elm = handle.resolve(javac);
                assert elm != null;
                Tree t = javac.getTrees().getTree(elm);
                assert t != null;
                result.add(t);
            }

            return result;
        }
        
        private List<Tree> getMethods2Remove(CompilationInfo javac, List<ElementHandle<ExecutableElement>> members, TypeElement clazz) {
            if (members.isEmpty()) {
                return Collections.<Tree>emptyList();
            }
            boolean isInterface = clazz.getKind() == ElementKind.INTERFACE;
            List<Tree> result = new ArrayList<Tree>(members.size());
            for (ElementHandle<ExecutableElement> handle : members) {
                ExecutableElement elm = handle.resolve(javac);
                assert elm != null;
                
                
                if (isInterface || elm.getModifiers().contains(Modifier.ABSTRACT)) {
                    // it is interface method nor abstract method
                    Tree t = javac.getTrees().getTree(elm);
                    assert t != null;
                    result.add(t);
                }
            }

            return result;
        }
        
        private List<Tree> getImplements2Remove(CompilationInfo javac, List<TypeMirrorHandle<TypeMirror>> members, TypeElement clazz) {
            if (members.isEmpty()) {
                return Collections.<Tree>emptyList();
            }
            
            // resolve members to remove
            List<TypeMirror> memberTypes = new ArrayList<TypeMirror>(members.size());
            for (TypeMirrorHandle<TypeMirror> handle : members) {
                TypeMirror tm = handle.resolve(javac);
                memberTypes.add(tm);
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
        
        private static List<Tree> resolveImplements(List<? extends Tree> allImpls, Set<Tree> impls2Remove, Tree impl2Add) {
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
            ret.add(impl2Add);
            return ret;
        }
    }
    
}
