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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.api;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tim Boudreau
 */
public final class JavaRefactoringUtils {
    private JavaRefactoringUtils() {}

    public static Collection<ExecutableElement> getOverriddenMethods(ExecutableElement e, CompilationInfo info) {
        return RetoucheUtils.getOverridenMethods (e, info);
    }

    public static Collection<ExecutableElement> getOverridingMethods(ExecutableElement e, CompilationInfo info) {
        return RetoucheUtils.getOverridingMethods(e, info);
    }

    public static boolean isValidPackageName(String name) {
        return RetoucheUtils.isValidPackageName(name);
    }

    public static boolean isOnSourceClasspath(FileObject fo) {
        return RetoucheUtils.isOnSourceClasspath(fo);
    }

    public static boolean isRefactorable(FileObject file) {
        return RetoucheUtils.isRefactorable(file) && file.canWrite() && file.canRead();
    }

    public static Collection<Element> getSuperTypes(TypeElement type, CompilationInfo info, boolean sourceOnly) {
        return RetoucheUtils.getSuperTypes(type, info);
    }

    /**
     * Finds the nearest enclosing ClassTree on <code>path</code> that
     * is class or interface or enum or annotation type and is or is not annonymous.
     * In case no ClassTree is found the first top level ClassTree is returned.
     *
     * Especially useful for selecting proper tree to refactor.
     *
     * @param javac javac
     * @param path path to search
     * @param isClass stop on class
     * @param isInterface  stop on interface
     * @param isEnum stop on enum
     * @param isAnnotation stop on annotation type
     * @param isAnonymous check if class or interface is annonymous
     * @return path to the enclosing ClassTree
     */
    public static TreePath findEnclosingClass(CompilationInfo javac, TreePath path, boolean isClass, boolean isInterface, boolean isEnum, boolean isAnnotation, boolean isAnonymous) {
        return RetoucheUtils.findEnclosingClass(javac, path, isClass, isInterface, isEnum, isAnnotation, isAnonymous);
    }

    public static List<TypeMirror> resolveTypeParamsAsTypes(List<? extends Element> typeParams) {
        return RetoucheUtils.resolveTypeParamsAsTypes(typeParams);
    }

    /**
     * Finds type parameters from <code>typeArgs</code> list that are referenced
     * by <code>tm</code> type.
     * @param utils compilation type utils
     * @param typeArgs modifiable list of type parameters to search; found types will be removed (performance reasons).
     * @param result modifiable list that will contain referenced type parameters
     * @param tm parametrized type to analyze
     */
    public static void findUsedGenericTypes(Types utils, List<TypeMirror> typeArgs, List<TypeMirror> result, TypeMirror tm) {
        RetoucheUtils.findUsedGenericTypes(utils, typeArgs, result, tm);
    }

    public static ClasspathInfo getClasspathInfoFor(FileObject ... files) {
        assert files.length >0;
        Set<URL> dependentRoots = new HashSet <URL> ();
        for (FileObject fo: files) {
            Project p = null;
            if (fo!=null)
                p=FileOwnerQuery.getOwner(fo);
            if (p!=null) {
                URL sourceRoot = URLMapper.findURL(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo), URLMapper.INTERNAL);
                dependentRoots.addAll(SourceUtils.getDependentRoots(sourceRoot));
                for (SourceGroup root:ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                    dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
                }
            } else {
                for(ClassPath cp: GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                    for (FileObject root:cp.getRoots()) {
                        dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
                    }
                }
            }
        }

        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
        ClassPath boot = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.BOOT):nullPath;
        ClassPath compile = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.COMPILE):nullPath;
        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
        return cpInfo;
    }

    //From here down is useful stuff from contrib/refactorings

    public static <T extends Tree> T resolveTreePathHandle (final TreePathHandle handle) throws IOException {
        FileObject fob = handle.getFileObject();
        JavaSource src = JavaSource.forFileObject(fob);
        TreeFinder<T> finder = new TreeFinder<T>(handle);
        src.runUserActionTask(finder, true);
        return finder.tree;
    }

    private static class TreeFinder <T extends Tree> implements CancellableTask <CompilationController> {
        T tree;
        private final TreePathHandle handle;
        boolean cancelled;
        TreeFinder (TreePathHandle handle) {
            this.handle = handle;
        }
        public void cancel() {
            cancelled = true;
        }

        @SuppressWarnings("unchecked")
        public void run(CompilationController cc) throws Exception {
            cc.toPhase (Phase.RESOLVED);
            TreePath path = handle.resolve(cc);
            assert path != null : "Null path for " + handle; //NOI18N
            tree = (T) path.getLeaf();
        }
    }

    public static List <TreePathHandle> toHandles (TreePath parent, Iterable <? extends Tree> trees, CompilationInfo info) {
        List <TreePathHandle> result = new ArrayList <TreePathHandle> (
                trees instanceof Collection ? ((Collection)trees).size() : 11);
        for (Tree tree : trees) {
            TreePath path = TreePath.getPath(parent, tree);
            TreePathHandle handle = TreePathHandle.create(path, info);
            result.add (handle);
            assert handle.resolve(info) != null : "Newly created TreePathHandle resolves to null"; //NOI18N
            assert handle.resolve(info).getLeaf() != null : "Newly created TreePathHandle.getLeaf() resolves to null"; //NOI18N
        }
        return result;
    }

    public static List <TreePathHandle> toHandles (Iterable <? extends Tree> trees, CompilationInfo info) {
        List <TreePathHandle> result = new ArrayList <TreePathHandle> (trees instanceof Collection ?
            ((Collection)trees).size() : 11);
        for (Tree tree : trees) {
            TreePath path = TreePath.getPath(info.getCompilationUnit(), tree);
            if (path == null) {
                throw new IllegalArgumentException (tree + " does not belong to " + //NOI18N
                        "the same compilation unit passed to this method"); //NOI18N
            }
            TreePathHandle handle = TreePathHandle.create(path, info);
            result.add (handle);
            assert handle.resolve(info) != null : "Newly created TreePathHandle resolves to null"; //NOI18N
            assert handle.resolve(info).getLeaf() != null : "Newly created TreePathHandle.getLeaf() resolves to null"; //NOI18N
        }
        return result;
    }

    public static <T extends Element> List <T> toElements (Iterable <ElementHandle<T>> handles, CompilationInfo info) {
        List <T> result = new ArrayList <T> (handles instanceof Collection ? ((Collection)handles).size() : 0);
        for (ElementHandle<? extends T> h : handles) {
            T element = h.resolve(info);
            assert element != null : element + " resolves to null"; //NOI18N
            result.add (element);
        }
        return result;
    }


    public static List <TypeMirror> toTypeMirrors (Iterable <? extends TypeMirrorHandle> types, CompilationInfo info) {
        List <TypeMirror> result = new ArrayList <TypeMirror> ();
        for (TypeMirrorHandle h : types) {
            result.add (h.resolve(info));
        }
        return result;
    }

    public static List <TypeMirrorHandle> toTypeMirrorHandles (Iterable <? extends TypeMirror> types) {
        List <TypeMirrorHandle> result = new ArrayList <TypeMirrorHandle> ();
        for (TypeMirror h : types) {
            result.add (TypeMirrorHandle.create(h));
        }
        return result;
    }

    public static <T extends Element> List <ElementHandle<T>> toHandles (Iterable <? extends T> elements) {
        List <ElementHandle<T>> result = new ArrayList <ElementHandle<T>> (elements instanceof
                Collection ? ((Collection)elements).size() : 11);
        for (T element : elements) {
            ElementHandle<T> handle = ElementHandle.<T>create(element);
            assert handle != null : "Couldn't create handle for " + element; //NOI18N
            result.add (handle);
        }
        return result;
    }

    public static Collection<ElementHandle<ExecutableElement>> getOverridingMethodHandles(ExecutableElement e, CompilationInfo info) {
        //Copied from RetoucheUtils
        Collection<ElementHandle<ExecutableElement>> result = new ArrayList <ElementHandle<ExecutableElement>> ();
        TypeElement parentType = (TypeElement) e.getEnclosingElement();
        //XXX: Fixme IMPLEMENTORS_RECURSIVE were removed
        Set<ElementHandle<TypeElement>> subTypes = info.getClasspathInfo().getClassIndex().getElements(ElementHandle.create(parentType),  EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
        for (ElementHandle<TypeElement> subTypeHandle: subTypes){
            TypeElement type = subTypeHandle.resolve(info);
            for (ExecutableElement method: ElementFilter.methodsIn(type.getEnclosedElements())) {
                if (info.getElements().overrides(method, e, type)) {
                    result.add(ElementHandle.<ExecutableElement>create(method));
                }
            }
        }
        return result;
    }

    public static Collection<TreePathHandle> getOverridingMethodTreeHandles (ExecutableElement e, CompilationController cc) throws IOException {
        Collection <ElementHandle<ExecutableElement>> mtds = getOverridingMethodHandles (e, cc);
        Set <TreePathHandle> result = new HashSet <TreePathHandle> ();
        ElementHandle<ExecutableElement> toFind = ElementHandle.<ExecutableElement>create(e);
        for (ElementHandle<ExecutableElement> element : mtds) {
            FileObject fob = SourceUtils.getFile(element, cc.getClasspathInfo());
            JavaSource src = JavaSource.forFileObject(fob);
            assert src.getFileObjects().contains(fob);
            TreeFromElementFinder finder = new TreeFromElementFinder (element);
            src.runUserActionTask(finder, false);
            if (finder.handle != null) {
                result.add (finder.handle);
            }
        }
        return result;
    }

    public static Collection<ElementHandle<ExecutableElement>> getOverridingMethodHandle(ExecutableElement e, CompilationInfo info) {
        //Copied from RetoucheUtils
        Collection<ElementHandle<ExecutableElement>> result = new ArrayList <ElementHandle<ExecutableElement>> ();
        TypeElement parentType = (TypeElement) e.getEnclosingElement();
        //XXX: Fixme IMPLEMENTORS_RECURSIVE were removed
        Set<ElementHandle<TypeElement>> subTypes = info.getClasspathInfo().getClassIndex().getElements(ElementHandle.create(parentType),  EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
        for (ElementHandle<TypeElement> subTypeHandle: subTypes){
            TypeElement type = subTypeHandle.resolve(info);
            for (ExecutableElement method: ElementFilter.methodsIn(type.getEnclosedElements())) {
                if (info.getElements().overrides(method, e, type)) {
                    result.add(ElementHandle.<ExecutableElement>create(method));
                }
            }
        }
        return result;
    }

    private static class TreeFromElementFinder implements CancellableTask <CompilationController> {
        private volatile boolean cancelled;
        Tree tree;
        TreePathHandle handle;
        private final ElementHandle element;
        TreeFromElementFinder (ElementHandle element) {
            this.element = element;
        }

        public void cancel() {
            cancelled = true;
        }

        @SuppressWarnings("unchecked")
        public void run(CompilationController cc) throws Exception {
            if (cancelled) return;
            cc.toPhase(Phase.RESOLVED);
            Element e = element.resolve(cc);
            tree = cc.getTrees().getTree(e);
            assert tree != null : "Got null tree for " + element + " on " + cc.getFileObject().getPath();
            if (cancelled) return;
            CompilationUnitTree unit = cc.getCompilationUnit();
            TreePath path = TreePath.getPath(unit, tree);
            assert path != null : "Got null tree path for " + cc.getFileObject().getPath() + " tree is " + tree;
            handle = TreePathHandle.create(path, cc);
        }
    }

    public static Collection <TreePathHandle> getInvocationsOf(ElementHandle e, CompilationController wc) throws IOException {
        assert e != null;
        assert wc != null;
        wc.toPhase (Phase.RESOLVED);
        Element element = e.resolve(wc);
        TypeElement type = wc.getElementUtilities().enclosingTypeElement(element);
        ElementHandle<TypeElement> elh = ElementHandle.<TypeElement>create(type);
        assert elh != null;
        //XXX do I want the enclosing type element for elh here?
        Set <ElementHandle<TypeElement>> classes = wc.getClasspathInfo().getClassIndex().getElements(elh, EnumSet.<SearchKind>of (SearchKind.METHOD_REFERENCES), EnumSet.<SearchScope>of(SearchScope.SOURCE));
        List <TreePathHandle> result = new ArrayList <TreePathHandle> ();
        for (ElementHandle<TypeElement> h : classes) {
            result.addAll (getReferencesToMember(h, wc.getClasspathInfo(), e));
        }
        return result;
    }

    /**
     * Get all of the references to the given member element (which may be part of another type) on
     * the passed element.
     * @param on A type which presumably refers to the passed element
     * @param toFind An element, presumably a field or method, of some type (not necessarily the passed one)
     */
    public static Collection <TreePathHandle> getReferencesToMember (ElementHandle<TypeElement> on, ClasspathInfo info, ElementHandle toFind) throws IOException {
        FileObject ob = SourceUtils.getFile(on, info);
        assert ob != null : "SourceUtils.getFile(" + on + ") returned null"; //NOI18N
        JavaSource src = JavaSource.forFileObject(ob);
        InvocationScanner scanner = new InvocationScanner (toFind);
        src.runUserActionTask(scanner, true);
        return scanner.usages;
    }

    private static final class InvocationScanner extends TreePathScanner <Tree, ElementHandle> implements CancellableTask <CompilationController> {
        private CompilationController cc;
        private final ElementHandle toFind;
        InvocationScanner (ElementHandle toFind) {
            this.toFind = toFind;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, ElementHandle p) {
            assert cc != null;
            Element e = p.resolve(cc);
            addIfMatch(getCurrentPath(), node, e);
            return super.visitMemberSelect(node, p);
        }

        private void addIfMatch(TreePath path, Tree tree, Element elementToFind) {
            if (cc.getTreeUtilities().isSynthetic(path))
                return;

            Element el = cc.getTrees().getElement(path);
            if (el==null)
                return;

            if (elementToFind.getKind() == ElementKind.METHOD && el.getKind() == ElementKind.METHOD) {
                if (el.equals(elementToFind) || cc.getElements().overrides(((ExecutableElement) el), (ExecutableElement) elementToFind, (TypeElement) elementToFind.getEnclosingElement())) {
                    addUsage(getCurrentPath());
                }
            } else if (el.equals(elementToFind)) {
                addUsage(getCurrentPath());
            }
        }

        Set <TreePathHandle> usages = new HashSet <TreePathHandle> ();
        void addUsage (TreePath path) {
            usages.add (TreePathHandle.create(path, cc));
        }

        boolean cancelled;
        public void cancel() {
            cancelled = true;
        }

        public void run(CompilationController cc) throws Exception {
            if (cancelled) return;
            cc.toPhase(Phase.RESOLVED);
            if (cancelled) return;
            this.cc = cc;
            try {
                TreePath path = new TreePath (cc.getCompilationUnit());
                scan (path, toFind);
            } finally {
                this.cc = null;
            }
        }
    }

    public static <R, D> Map <TreePathHandle, Map<TreeVisitor<R,D>, R>> runAgainstSources (Iterable<TreePathHandle> handles, D arg, TreeVisitor<R,D>... visitors) throws IOException {
        Map <TreePathHandle, Map<TreeVisitor<R,D>, R>> results = new HashMap<TreePathHandle, Map<TreeVisitor<R, D>, R>>();
        for (TreePathHandle handle : handles) {
            FileObject fob = handle.getFileObject();
            JavaSource src = JavaSource.forFileObject(fob);
            MultiVisitorRunner<R, D> runner = new MultiVisitorRunner <R, D> (handle, arg, visitors);
            src.runUserActionTask(runner, true);
            results.put (handle, runner.results);
        }
        return results;
    }

    public static void runAgainstSources (Iterable <TreePathHandle> handles, CancellableTask<CompilationController> c) throws IOException {
        for (TreePathHandle handle : handles) {
            FileObject fob = handle.getFileObject();
            JavaSource src = JavaSource.forFileObject(fob);
            src.runUserActionTask(c, true);
        }
    }

    public static <T> void runAgainstSources (Iterable <TreePathHandle> handles,TreePathHandleTask<T> t, T arg) throws IOException {
        for (TreePathHandle handle : handles) {
            FileObject file = handle.getFileObject();
            t.handle = handle;
            t.arg = arg;
            t.file = file;
            JavaSource src = JavaSource.forFileObject(file);
            src.runUserActionTask(t, true);
        }
        t.handle = null;
        t.arg = null;
        t.file = null;
    }

    public abstract static class TreePathHandleTask<T> implements CancellableTask <CompilationController> {
        protected boolean cancelled;
        private TreePathHandle handle;
        private T arg;
        private FileObject file;
        public void cancel() {
            cancelled = true;
        }

        public final void run(CompilationController cc) throws Exception {
            cc.toPhase (Phase.RESOLVED);
            run (cc, handle, file, arg);
        }

        public abstract void run (CompilationController cc, TreePathHandle handle, FileObject file, T arg);
    }

    private static class MultiVisitorRunner <R, D> implements CancellableTask <CompilationController> {
        private final Map <TreeVisitor<R,D>, R> results = new HashMap <TreeVisitor<R,D>, R> ();
        private final TreeVisitor<R,D>[] visitors;
        private final D arg;
        private TreePathHandle handle;
        MultiVisitorRunner (TreePathHandle handle, D arg, TreeVisitor<R, D>... visitors) {
            this.visitors = visitors;
            this.handle = handle;
            this.arg = arg;
        }

        R getResult (TreeVisitor visitor) {
            return results.get(visitor);
        }

        volatile boolean cancelled;
        public void cancel() {
            cancelled = true;
        }

        public void run(CompilationController cc) throws Exception {
            if (cancelled) return;
            TreePath path = handle.resolve(cc);
            for (TreeVisitor<R,D> v : visitors) {
                if (cancelled) return;
                R result;
                if (v instanceof TreePathScanner) {
                    @SuppressWarnings("unchecked") //NOI18N
                    TreePathScanner<R,D> scanner = (TreePathScanner<R,D>) v;
                    result = scanner.scan(path, arg);
                } else if (v instanceof TreeScanner) {
                    @SuppressWarnings("unchecked") //NOI18N
                    TreeScanner<R,D> scanner = (TreeScanner<R,D>) v;
                    result = scanner.scan(path.getLeaf(), arg);
                } else {
                    result = path.getLeaf().accept(v, arg);
                }
                results.put (v, result);
            }
        }
    }
}
