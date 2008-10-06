/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.callhierarchy;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.EventQueue;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.plugins.FindUsagesVisitor;
import org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Pokorsky
 */
final class CallHierarchyTasks {

    private static final RequestProcessor RP = new RequestProcessor("Call Hierarchy Processor", 1); // NOI18N
    
    private static final Object LOCK = new Object();
    private static CallersTask CURR_TASK;
    
    public static void stop() {
        synchronized (LOCK) {
            if (CURR_TASK != null) {
                CURR_TASK.cancel();
            }
        }
    }
    
    public static void findCallers(Call c, boolean includeTest, boolean searchAll, Runnable resultHandler) {
        RP.post(new CallersTask(c, resultHandler, includeTest, searchAll));
    }
    
    public static void findCallees(Call c, Runnable resultHandler) {
        RP.post(new CalleesTask(c, resultHandler));
    }
    
    public static Call resolveRoot(TreePathHandle selection, boolean isCallerGraph) {
        JavaSource js = JavaSource.forFileObject(selection.getFileObject());
        return resolveRoot(js, new RootResolver(selection, isCallerGraph));
    }
    
    public static Call resolveRoot(Lookup lookup, boolean isCallerGraph) {
        JavaSource js = null;
        RootResolver resolver = null;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null/*RefactoringActionsProvider.isFromEditor(ec)*/) {
            JEditorPane[] openedPanes = ec.getOpenedPanes();
            Document doc = ec.getDocument();
            js = JavaSource.forDocument(doc);
            resolver = new RootResolver(openedPanes[0].getCaretPosition(), isCallerGraph);
        } else {
            // XXX resolve Node.class
            
        }
        
        return resolveRoot(js, resolver);
    }
    
    private static Call resolveRoot(JavaSource source, RootResolver resolver) {
        Call root = null;
        if (source != null && resolver != null) {
            try {
                source.runWhenScanFinished(resolver, true);
                root = resolver.getRoot();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return root;
    }
    
    private static final class RootResolver implements Task<CompilationController> {
        
        private int offset = -1;
        private TreePathHandle tHandle;
        private final boolean isCallerGraph;
        private Call root;

        public RootResolver(TreePathHandle tHandle, boolean isCallerGraph) {
            this.tHandle = tHandle;
            this.isCallerGraph = isCallerGraph;
        }

        public RootResolver(int offset, boolean isCallerGraph) {
            this.offset = offset;
            this.isCallerGraph = isCallerGraph;
        }

        public void run(CompilationController javac) throws Exception {
            TreePath tpath = null;
            Element method = null;
            
            javac.toPhase(JavaSource.Phase.RESOLVED);
            if (tHandle == null) {
                tpath = javac.getTreeUtilities().pathFor(offset);
            } else {
                tpath = tHandle.resolve(javac);
            }
            
            while (tpath != null) {
                Kind kind = tpath.getLeaf().getKind();
                if (kind == Kind.METHOD || kind == Kind.METHOD_INVOCATION || kind == Kind.MEMBER_SELECT) {
                    method = javac.getTrees().getElement(tpath);
                    if (method != null && (method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR)) {
                        break;
                    }
                    method = null;
                }
                tpath = tpath.getParentPath();
            }
            
            if (method != null) {
                root = Call.createRoot(javac, tpath, method, isCallerGraph);
            }
        }
        
        public Call getRoot() {
            return root;
        }
        
    }
    
    private static final class CallersTask implements Runnable, CancellableTask<WorkingCopy> {
        
        private final Call elmDesc;
        private final Runnable resultHandler;
        private AtomicBoolean isCanceled = new AtomicBoolean(false);
        private final List<Call> result = new ArrayList<Call>();
        private final boolean includeTest;
        private final boolean searchAll;
        
        public CallersTask(Call elmDesc, Runnable resultHandler, boolean includeTest, boolean searchAll) {
            this.elmDesc = elmDesc;
            this.resultHandler = resultHandler;
            this.includeTest = includeTest;
            this.searchAll = searchAll;
        }
        
        public void run() {
            try {
                notifyRunning(true);

                TreePathHandle sourceToQuery = elmDesc.getSourceToQuery();
                
                // validate source
                if (RetoucheUtils.getElementHandle(sourceToQuery) == null) {
                    elmDesc.setBroken();
                } else {
                    ClasspathInfo cpInfo;
                    if (searchAll) {
                        cpInfo = RetoucheUtils.getClasspathInfoFor(true, sourceToQuery.getFileObject());
                    } else {
                        cpInfo = RetoucheUtils.getClasspathInfoFor(false, elmDesc.selection.getFileObject());
                    }

                    Set<FileObject> relevantFiles = null;
                    if (!isCanceled()) {
                        relevantFiles = JavaWhereUsedQueryPlugin.getRelevantFiles(
                                sourceToQuery, cpInfo, false, false, false, true);
                    }
                    try {
                        if (!isCanceled()) {
                            processFiles(relevantFiles, this, null);
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    elmDesc.setCanceled(isCanceled());
                }
                elmDesc.setReferences(result);
                resultHandler.run();
            } finally {
                synchronized(LOCK) {
                    CURR_TASK = null;
                }
                notifyRunning(false);
            }
        }
        
        private void notifyRunning(final boolean isRunning) {
            synchronized (LOCK) {
                CURR_TASK = isRunning ? this : null;
            }
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    public void run() {
                        CallHierarchyTopComponent.findInstance().setRunningState(isRunning);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public void cancel() {
            isCanceled.set(true);
            RetoucheUtils.cancel = true;
        }
        
        private boolean isCanceled() {
            if (Thread.interrupted()) {
                isCanceled.set(true);
            }
            return isCanceled.get();
        }
        
        public void run(WorkingCopy javac) throws Exception {
            if (javac.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                return;
            }
            Element wanted = elmDesc.getSourceToQuery().resolveElement(javac);
            if (wanted == null) {
                // XXX log it
                return;
            }
            FindUsagesVisitor findVisitor = new FindUsagesVisitor(javac, false);
            findVisitor.scan(javac.getCompilationUnit(), wanted);
            Collection<TreePath> usages = findVisitor.getUsages();
            Map<Element, OccurrencesDesc> refs = new HashMap<Element, OccurrencesDesc>();
            int order = 0;
            
            for (TreePath treePath : usages) {
                TreePath declarationPath = resolveDeclarationContext(treePath);
                if (declarationPath == null) {
                    // XXX log unknown path
                    continue;
                }
                
                Element elm = null;
                if (declarationPath.getLeaf().getKind() != Kind.BLOCK) {
                    elm = javac.getTrees().getElement(declarationPath);
                } else {
                    // initializer
                    Element enclosing = javac.getTrees().getElement(declarationPath.getParentPath());
                    BlockTree block = (BlockTree) declarationPath.getLeaf();
                    elm = new InitializerElement(enclosing, block.isStatic());
                }
                
                if (elm == null) {
                    // XXX log unknown path
                    continue;
                }

                OccurrencesDesc occurDesc = refs.get(elm);
                if (occurDesc == null) {
                    occurDesc = new OccurrencesDesc(declarationPath, elm, order++);
                    refs.put(elm, occurDesc);
                }
                occurDesc.occurrences.add(treePath);
            }
            
            List<Call> usageDescs = new ArrayList<Call>(refs.size());
            for (OccurrencesDesc occurDesc : OccurrencesDesc.extract(refs)) {
                Call newDesc = Call.createUsage(
                        javac, occurDesc.selection, occurDesc.elm, elmDesc,
                        occurDesc.occurrences);
                usageDescs.add(newDesc);
            }
            result.addAll(usageDescs);
        }
        
        private static TreePath resolveDeclarationContext(TreePath usage) {
            TreePath declaration = usage;
            
            while (declaration != null) {
                switch (declaration.getLeaf().getKind()) {
                    case BLOCK:
                        if (declaration.getParentPath().getLeaf().getKind() == Kind.CLASS) {
                            // it is static or instance initializer
                            return declaration;
                        }
                        break;
                    case METHOD:
                        return declaration;
                    case VARIABLE:
                        if (declaration.getParentPath().getLeaf().getKind() == Kind.CLASS) {
                            // it is field declaration
                            // private int field = init();
                            return declaration;
                        }
                        break;
                }
                declaration = declaration.getParentPath();
            }
            return null;
        }
    
        private Iterable<? extends List<FileObject>> groupByRoot (Iterable<? extends FileObject> data) {
            Map<FileObject,List<FileObject>> result = new HashMap<FileObject,List<FileObject>> ();
            for (FileObject file : data) {
                if (isCanceled()) {
                    return Collections.emptyList();
                }
                
                ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                if (cp != null) {
                    FileObject root = cp.findOwnerRoot(file);
                    if (root != null) {
                        if (!includeTest && UnitTestForSourceQuery.findSources(root).length > 0) {
                            continue;
                        }
                        List<FileObject> subr = result.get (root);
                        if (subr == null) {
                            subr = new LinkedList<FileObject>();
                            result.put (root,subr);
                        }
                        subr.add(file);
                    }
                }
            }
            return result.values();
        }
        
        protected final void processFiles(Set<FileObject> files, Task<WorkingCopy> task, ClasspathInfo info) throws IOException {
            Iterable<? extends List<FileObject>> work = groupByRoot(files);
            for (List<FileObject> fos : work) {
                if (isCanceled()) {
                    return;
                }
                final JavaSource javaSource = JavaSource.create(info == null ? ClasspathInfo.create(fos.get(0)) : info, fos);
                javaSource.runModificationTask(task);
            }
        }

    }
    
    private static final class CalleesTask implements Runnable, Task<CompilationController> {
        
        private final Call elmDesc;
        private final Runnable resultHandler;

        public CalleesTask(Call element, Runnable resultHandler) {
            this.elmDesc = element;
            this.resultHandler = resultHandler;
        }
        
        public void run() {
            try {
                JavaSource js = JavaSource.forFileObject(elmDesc.getSourceToQuery().getFileObject());
                if (js != null) {
                    js.runWhenScanFinished(this, true);
                }
                resultHandler.run();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void run(CompilationController javac) throws Exception {
            javac.toPhase(JavaSource.Phase.RESOLVED);
            TreePath resolved = elmDesc.getSourceToQuery().resolve(javac);
            if (resolved == null) {
                // nothing to find
                // XXX descriptor should be invalidated and it should be presented to user
                return;
            }

            Element resolvedElm = javac.getTrees().getElement(resolved);
            resolved = javac.getTrees().getPath(resolvedElm);
            if (resolved == null) {
                // nothing to find, missing source file
                // XXX descriptor should be invalidated and it should be presented to user
                return;
            }

            CalleeScanner scanner = new CalleeScanner(javac);
            scanner.scan(resolved, null);
            List<Call> usages = new ArrayList<Call>();
            for (OccurrencesDesc occurDesc : scanner.getOccurrences()) {
                usages.add(Call.createUsage(
                        javac, occurDesc.selection, occurDesc.elm, elmDesc, occurDesc.occurrences));
            }
            elmDesc.setReferences(usages);
        }
        
    }
    
    private static final class CalleeScanner extends TreePathScanner<Void, Void> {

        private final CompilationInfo javac;
        /** map of all executables and their occurrences in the method body */
        private Map<Element, OccurrencesDesc> refs = new HashMap<Element, OccurrencesDesc>();
        private int elmCounter = 0;

        public CalleeScanner(CompilationInfo javac) {
            this.javac = javac;
        }
        
        public List<OccurrencesDesc> getOccurrences() {
            return OccurrencesDesc.extract(refs);
        }
        
        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
            resolvePath(getCurrentPath());
            return super.visitMethodInvocation(node, p);
        }

        @Override
        public Void visitNewClass(NewClassTree node, Void p) {
            resolvePath(getCurrentPath());
            return super.visitNewClass(node, p);
        }
        
        private void resolvePath(TreePath tpath) {
            Element resolved = javac.getTrees().getElement(tpath);
            if (resolved != null
                    && !javac.getElementUtilities().isSynthetic(resolved)
                    && (resolved.getKind() == ElementKind.METHOD || resolved.getKind() == ElementKind.CONSTRUCTOR)) {
                addRef(resolved, tpath);
            }
        }
        
        private void addRef(Element ref, TreePath occurrence) {
            OccurrencesDesc desc = refs.get(ref);
            if (desc == null) {
                desc = new OccurrencesDesc(occurrence, ref, elmCounter++);
                refs.put(ref, desc);
            }
            desc.occurrences.add(occurrence);
        }
    }
    
    private static final class OccurrencesDesc implements Comparable<OccurrencesDesc> {
        final List<TreePath> occurrences;
        final Element elm;
        final TreePath selection;
        final int order;

        public OccurrencesDesc(TreePath selection, Element elm, int order) {
            this.occurrences = new ArrayList<TreePath>();
            this.order = order;
            this.elm = elm;
            this.selection = selection;
        }

        public int compareTo(OccurrencesDesc o) {
            return order - o.order;
        }
        
        public static List<OccurrencesDesc> extract(Map<Element, OccurrencesDesc> refs) {
            int size = refs.size();
            List<OccurrencesDesc> l;
            if (size > 0) {
                l = new ArrayList<OccurrencesDesc>(size);
                l.addAll(refs.values());
                Collections.sort(l);
            } else {
                l = Collections.emptyList();
            }
            return l;
        }
    }
    
    private static final class InitializerElement implements Element {
        
        private static final Set<Modifier> STATICM = EnumSet.of(Modifier.STATIC);
        private final boolean isStatic;
        private final Element enclosing;

        public InitializerElement(Element enclosing, boolean isStatic) {
            this.isStatic = isStatic;
            this.enclosing = enclosing;
        }
        
        public TypeMirror asType() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public ElementKind getKind() {
            return isStatic ? ElementKind.STATIC_INIT : ElementKind.INSTANCE_INIT;
        }

        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public Set<Modifier> getModifiers() {
            return isStatic ? STATICM : Collections.<Modifier>emptySet();
        }

        public Name getSimpleName() {
            return null;
        }

        public Element getEnclosingElement() {
            return enclosing;
        }

        public List<? extends Element> getEnclosedElements() {
            return Collections.emptyList();
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
        
    }
}
