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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.spring.beans.refactoring.plugins;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.spring.beans.refactoring.RetoucheUtils;
import org.netbeans.modules.spring.beans.refactoring.WhereUsedElement;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

/**
 * @author John Baker
 */
public class SpringBeansRefactoringFindUsagesPlugin extends SpringBeansJavaRefactoringPlugin {
    private WhereUsedQuery springBeansWhereUsed;
    
  enum WhereUsedQueryConstants {
    /**
     * Find overriding methods
     */
    FIND_OVERRIDING_METHODS,
    /**
     * Find All Sublcasses recursively
     */
    FIND_SUBCLASSES,
    /**
     * Find only direct subclasses
     */
    FIND_DIRECT_SUBCLASSES,
    /**
     * Search from base class
     */
    SEARCH_FROM_BASECLASS;
}
    
    SpringBeansRefactoringFindUsagesPlugin(WhereUsedQuery query) {
        springBeansWhereUsed = query;
    }

     private Set<FileObject> getRelevantFiles(final TreePathHandle tph) {
        final ClasspathInfo cpInfo = getClasspathInfo(springBeansWhereUsed);
        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new HashSet<FileObject>();
                
        final FileObject file = tph.getFileObject();
        JavaSource source;
        if (file!=null) {
           set.add(file);
            source = JavaSource.create(cpInfo, new FileObject[]{tph.getFileObject()});
        } else {
            source = JavaSource.create(cpInfo);
        }
        //XXX: This is slow!
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                final Element el = tph.resolveElement(info);
                if (el.getKind().isField()) {
                    //get field references from index
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                } else if (el.getKind().isClass() || el.getKind().isInterface()) {
                    if (isFindSubclasses()||isFindDirectSubclassesOnly()) {
                        if (isFindDirectSubclassesOnly()) {
                            //get direct implementors from index
                            EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
                            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el), searchKind,EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        } else {
                            //itererate implementors recursively
                            set.addAll(getImplementorsRecursive(idx, cpInfo, (TypeElement)el));
                        }
                    } else {
                        //get type references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    }
                } else if (el.getKind() == ElementKind.METHOD && isFindOverridingMethods()) {
                    //Find overriding methods
                    TypeElement type = (TypeElement) el.getEnclosingElement();
                    set.addAll(getImplementorsRecursive(idx, cpInfo, type));
                } 
                if (el.getKind() == ElementKind.METHOD && isFindUsages()) {
                    //get method references for method and for all it's overriders
                    Set<ElementHandle<TypeElement>> s = RetoucheUtils.getImplementorsAsHandles(idx, cpInfo, (TypeElement)el.getEnclosingElement());
                    for (ElementHandle<TypeElement> eh:s) {
                        TypeElement te = eh.resolve(info);
                        if (te==null) {
                            continue;
                        }
                        for (Element e:te.getEnclosedElements()) {
                            if (e instanceof ExecutableElement) {
                                if (info.getElements().overrides((ExecutableElement)e, (ExecutableElement)el, te)) {
                                    set.addAll(idx.getResources(ElementHandle.create(te), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                                }
                            }
                        }
                    }
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
                } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                }
                    
            }
        };
        try {
            source.runUserActionTask(task, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
     
      private Set<FileObject> getImplementorsRecursive(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el) {
        Set<FileObject> set = new HashSet<FileObject>();
        for (ElementHandle<TypeElement> e : RetoucheUtils.getImplementorsAsHandles(idx, cpInfo, el)) {
            FileObject fo = SourceUtils.getFile(e, cpInfo);
            assert fo != null : "issue 90196, Cannot find file for " + e + ". cpInfo=" + cpInfo;
            set.add(fo);
        }
        return set;
    }
     
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {        
        Set<FileObject> a = getRelevantFiles(springBeansWhereUsed.getRefactoringSource().lookup(TreePathHandle.class));
        fireProgressListenerStart(ProgressEvent.START, a.size());
        processFiles(a, new FindTask(refactoringElements));
        fireProgressListenerStop();
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }
    
    public void doRefactoring(List<RefactoringElementImplementation> elements)
            throws IOException {
    }


    @Override
    public void cancelRequest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private boolean isFindSubclasses() {
        return springBeansWhereUsed.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }
    private boolean isFindUsages() {
        return springBeansWhereUsed.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
    }
    private boolean isFindDirectSubclassesOnly() {
        return springBeansWhereUsed.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }
    
    private boolean isFindOverridingMethods() {
        return springBeansWhereUsed.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }
    private boolean isSearchFromBaseClass() {
        return false;
    }
    
    private class FindTask implements CancellableTask<WorkingCopy> {

        private RefactoringElementsBag elements;
        private volatile boolean cancelled;

        public FindTask(RefactoringElementsBag elements) {
            super();
            this.elements = elements;
        }

        public void cancel() {
            cancelled=true;
        }

        public void run(WorkingCopy compiler) throws IOException {
            if (cancelled)
                return ;
            if (compiler.toPhase(JavaSource.Phase.RESOLVED)!=JavaSource.Phase.RESOLVED) {
                return;
            }
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler); // NOI18N
                return;
            }
            Element element = springBeansWhereUsed.getRefactoringSource().lookup(TreePathHandle.class).resolveElement(compiler);
            assert element != null;
            Collection<TreePath> result = new ArrayList<TreePath>();
            if (isFindUsages()) {
                FindUsagesJavaVisitor findVisitor = new FindUsagesJavaVisitor(compiler, springBeansWhereUsed.getBooleanValue(springBeansWhereUsed.SEARCH_IN_COMMENTS));
                findVisitor.scan(compiler.getCompilationUnit(), element);
                result.addAll(findVisitor.getUsages());
                for (FindUsagesJavaVisitor.UsageInComment usageInComment : findVisitor.getUsagesInComments()) {
                    elements.add(springBeansWhereUsed, WhereUsedElement.create(usageInComment.from, usageInComment.to, compiler));
                }
            }
            if (element.getKind() == ElementKind.METHOD && isFindOverridingMethods()) {
                FindOverridingVisitor override = new FindOverridingVisitor(compiler);
                override.scan(compiler.getCompilationUnit(), element);
                result.addAll(override.getUsages());
            } else if ((element.getKind().isClass() || element.getKind().isInterface()) &&
                    (isFindSubclasses()||isFindDirectSubclassesOnly())) {
                FindSubtypesVisitor subtypes = new FindSubtypesVisitor(!isFindDirectSubclassesOnly(), compiler);
                subtypes.scan(compiler.getCompilationUnit(), element);
                result.addAll(subtypes.getUsages());
            }
            for (TreePath tree : result) {
                elements.add(springBeansWhereUsed, WhereUsedElement.create(compiler, tree));
            }
            fireProgressListenerStep();
        }
    }

}
