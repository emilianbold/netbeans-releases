/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Implemented abilities: <ul> <li>Move field(s)</li> <li>Move method(s)</li>
 * </ul>
 *
 * @author Ralph Ruijs
 */
@NbBundle.Messages({"ERR_NothingSelected=Nothing selected to move",
    "ERR_MoveToLibrary=Cannot move to a library",
    "ERR_MoveFromLibrary=Cannot move from a library",
    "ERR_MoveToSameClass=Target can not be the same as the source class",
    "ERR_MoveToSuperClass=Cannot move to a superclass, maybe you need the Pull Up Refactoring?",
    "ERR_MoveToSubClass=Cannot move to a subclass, maybe you need the Push Down Refactoring?",
    "WRN_InitNoAccess=Field initializer uses local accessors which will not be accessible",
    "WRN_NoAccessor=No accessor found to invoke the method in the new target"})
public class MoveMembersRefactoringPlugin extends JavaRefactoringPlugin {

    private final MoveRefactoring refactoring;

    public MoveMembersRefactoringPlugin(MoveRefactoring moveRefactoring) {
        this.refactoring = moveRefactoring;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle source = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (source != null && source.getFileObject() != null) {
            switch (p) {
                default:
                    return JavaSource.forFileObject(source.getFileObject());
            }
        } else {
            return null;
        }
    }

    @Override
    public Problem preCheck() {
        // Nothing to precheck, target and source are not known yet.
        return null;
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        // TODO if target is different project, there is no dependency on the target from source
        // TODO source method is using something not available at target
        // TODO source using generics not available at target
        return null;
    }

    @Override
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Collection<? extends TreePathHandle> source = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        TreePathHandle target = refactoring.getTarget().lookup(TreePathHandle.class);

        if (source.isEmpty()) { // [f] nothing is selected
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_NothingSelected")); //NOI18N
        }

        if (target.getFileObject() == null || !JavaRefactoringUtils.isOnSourceClasspath(target.getFileObject())) { // [f] target is not on source classpath
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToLibrary")); //NOI18N
        }
        TreePathHandle sourceTph = source.iterator().next();
        if (sourceTph.getFileObject() == null || !JavaRefactoringUtils.isOnSourceClasspath(sourceTph.getFileObject())) { // [f] source is not on source classpath
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveFromLibrary")); //NOI18N
        }

        TreePath sourceClass = JavaRefactoringUtils.findEnclosingClass(javac, sourceTph.resolve(javac), true, true, true, true, true);
        TreePath targetClass = JavaRefactoringUtils.findEnclosingClass(javac, target.resolve(javac), true, true, true, true, true);
        TypeMirror sourceType = javac.getTrees().getTypeMirror(sourceClass);
        TypeMirror targetType = javac.getTrees().getTypeMirror(targetClass);
        if (sourceType.equals(targetType)) { // [f] target is the same as source
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToSameClass")); //NOI18N
        }
        if (javac.getTypes().isSubtype(sourceType, targetType)) { // [f] target is a superclass of source
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToSuperClass")); //NOI18N
        }
        if (javac.getTypes().isSubtype(targetType, sourceType)) { // [f] target is a subclass of source
            return new Problem(true, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "ERR_MoveToSubClass")); //NOI18N
        }
        return null;
    }

    private Set<FileObject> getRelevantFiles() {
        final Set<FileObject> set = new LinkedHashSet<FileObject>();

        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final ClassIndex idx = cpInfo.getClassIndex();
        final Collection<? extends TreePathHandle> tphs = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        TreePathHandle target = refactoring.getTarget().lookup(TreePathHandle.class);
        FileObject file = target.getFileObject();
        JavaSource source = createSource(file, cpInfo, target);
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

            public void cancel() {
            }

            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                Set<ClassIndex.SearchScopeType> searchScopeType = new HashSet<ClassIndex.SearchScopeType>(1);
                searchScopeType.add(ClassIndex.SearchScope.SOURCE);

                for (TreePathHandle tph : tphs) {
                    set.add(tph.getFileObject());
                    final Element el = tph.resolveElement(info);
                    if (el.getKind() == ElementKind.METHOD) {
                        // get method references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType)); //?????
                    }
                    if (el.getKind().isField()) {
                        // get field references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), searchScopeType));
                    }
                }
            }
        };
        try {
            source.runUserActionTask(task, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        // Make sure the target is added last. Needed for escalating visibility.
        set.remove(file);
        set.add(file);
        return set;
    }

    private static JavaSource createSource(final FileObject file, final ClasspathInfo cpInfo, final TreePathHandle tph) throws IllegalArgumentException {
        JavaSource source;
        if (file != null) {
            final ClassPath mergedPlatformPath = merge(cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT), ClassPath.getClassPath(file, ClassPath.BOOT));
            final ClassPath mergedCompilePath = merge(cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE), ClassPath.getClassPath(file, ClassPath.COMPILE));
            final ClassPath mergedSourcePath = merge(cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE), ClassPath.getClassPath(file, ClassPath.SOURCE));
            final ClasspathInfo mergedInfo = ClasspathInfo.create(mergedPlatformPath, mergedCompilePath, mergedSourcePath);
            source = JavaSource.create(mergedInfo, new FileObject[]{tph.getFileObject()});
        } else {
            source = JavaSource.create(cpInfo);
        }
        return source;
    }

    private static ClassPath merge(final ClassPath... cps) {
        final Set<URL> roots = new LinkedHashSet<URL>();
        for (final ClassPath cp : cps) {
            if (cp != null) {
                for (final ClassPath.Entry entry : cp.entries()) {
                    final URL root = entry.getURL();
                    if (!roots.contains(root)) {
                        roots.add(root);
                    }
                }
            }
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        fireProgressListenerStart(ProgressEvent.START, -1);

        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStep(a.size());
        MoveMembersTransformer transformer = new MoveMembersTransformer(refactoring);
        TransformTask task = new TransformTask(transformer, refactoring.getTarget().lookup(TreePathHandle.class));
        Problem prob = createAndAddElements(a, task, refactoringElements, refactoring);
        prob = JavaPluginUtils.chainProblems(prob, transformer.getProblem());
        fireProgressListenerStop();
        return prob != null ? prob : transformer.getProblem();
    }
}
