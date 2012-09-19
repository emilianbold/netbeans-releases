/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 */
@NbBundle.Messages({"ERR_NotClass=Selected element is not a top-level class or interface.",
    "# {0} - The file not of java type.",
    "ERR_NotJava=Selected element is not defined in a java file. {0}",
    "ERR_CannotMovePublicIntoSamePackage=Cannot move public class to the same package.",
    "ERR_NoTargetFound=Cannot find the target to move to.",
    "# {0} - Class name.",
    "ERR_ClassToMoveClashes=Class \"{0}\" already exists in the target package.",
    "# {0} - Source Class name.",
    "# {1} - Target Class name.",
    "ERR_ClassToMoveClashesInner=Type \"{0}\" already exists in the target \"{1}\"."})
public class MoveClassRefactoringPlugin extends JavaRefactoringPlugin {

    private final MoveRefactoring moveRefactoring;
    private Set<ElementHandle<TypeElement>> elementHandles;
    private Set<ElementHandle<TypeElement>> handlesToMove;

    public MoveClassRefactoringPlugin(MoveRefactoring moveRefactoring) {
        this.moveRefactoring = moveRefactoring;
        elementHandles = new HashSet<ElementHandle<TypeElement>>();
        handlesToMove = new HashSet<ElementHandle<TypeElement>>();
    }

    @Override
    public Problem preCheck() {
        cancelRequest = false;
        cancelRequested.set(false);
        Problem preCheckProblem = null;
        for (TreePathHandle tph : moveRefactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            ElementHandle elementHandle = tph.getElementHandle();
            if (elementHandle == null
                    || (!elementHandle.getKind().isClass() && !elementHandle.getKind().isClass())) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(
                        MoveClassRefactoringPlugin.class,
                        "ERR_NotClass"));
                continue;
            }

            FileObject file = tph.getFileObject();
            if (!RefactoringUtils.isFileInOpenProject(file)) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(
                        MoveFileRefactoringPlugin.class,
                        "ERR_ProjectNotOpened",
                        FileUtil.getFileDisplayName(file)));
            }
        }
        return preCheckProblem;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        try {
            for (TreePathHandle tph : moveRefactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
                FileObject f = tph.getFileObject();
                if (!RefactoringUtils.isJavaFile(f)) {
                    return new Problem(true, NbBundle.getMessage(MoveClassRefactoringPlugin.class, "ERR_NotJava", f));
                }
                final URL targetUrl = moveRefactoring.getTarget().lookup(URL.class);
                if (targetUrl != null) {
                    String targetPackageName = targetUrl != null ? RefactoringUtils.getPackageName(targetUrl) : null;
                    if (targetPackageName == null || !RefactoringUtils.isValidPackageName(targetPackageName)) {
                        String s = NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_InvalidPackage"); //NOI18N
                        String msg = new MessageFormat(s).format(
                                new Object[]{targetPackageName});
                        return new Problem(true, msg);
                    }

                    FileObject targetRoot = RefactoringUtils.getClassPathRoot(targetUrl);
                    FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));

                    if ((targetF != null && !targetF.canWrite())) {
                        return new Problem(true, new MessageFormat(NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_PackageIsReadOnly")).format( // NOI18N
                                new Object[]{targetPackageName}));
                    }

                    String fileName = f.getName();
                    if (targetF != null) {
                        FileObject[] children = targetF.getChildren();
                        for (int i = 0; i < children.length; i++) {
                            if (children[i].getName().equals(fileName) && "java".equals(children[i].getExt()) && !children[i].equals(f) && !children[i].isVirtual()) { //NOI18N
                                return new Problem(true, new MessageFormat(
                                        NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_ClassToMoveClashes")).format(new Object[]{fileName} // NOI18N
                                        ));
                            }
                        }
                    }
                } else {
                    TreePathHandle target = moveRefactoring.getTarget().lookup(TreePathHandle.class);
                    if(target == null) {
                        String s = NbBundle.getMessage(MoveClassRefactoringPlugin.class, "ERR_NoTargetFound"); //NOI18N
                        return new Problem(true, s);
                    }
                }
            }
        } catch (IOException ioe) {
            //do nothing
        }
        return super.fastCheckParameters();
    }

    @Override
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        for (TreePathHandle tph : moveRefactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            FileObject f = tph.getFileObject();
            Element resolveElement = tph.resolveElement(javac);
            URL targetUrl = moveRefactoring.getTarget().lookup(URL.class);
            if(targetUrl != null) {
                FileObject targetRoot = RefactoringUtils.getClassPathRoot(targetUrl);
                String targetPackageName = RefactoringUtils.getPackageName(targetUrl);
                FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));
                if (f.getParent().equals(targetF)) {
                    if(resolveElement.getModifiers().contains(Modifier.PUBLIC)) {
                        return new Problem(true, NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_CannotMovePublicIntoSamePackage"));
                    }
                }
            } else {
                TreePathHandle target = moveRefactoring.getTarget().lookup(TreePathHandle.class);
                ElementHandle elementHandle = target.getElementHandle();
                assert elementHandle != null;
                TypeElement targetType = (TypeElement) elementHandle.resolve(javac);
                List<? extends Element> enclosedElements = targetType.getEnclosedElements();
                for (Element element : enclosedElements) {
                    switch (element.getKind()) {
                        case ENUM:
                        case CLASS:
                        case ANNOTATION_TYPE:
                        case INTERFACE:
                            if(element.getSimpleName().contentEquals(resolveElement.getSimpleName())) {
                                return new Problem(true, ERR_ClassToMoveClashesInner(element.getSimpleName(), targetType.getSimpleName()));
                            }
                    }
                }
            }
        }
        return super.fastCheckParameters(javac);
    }

    private Set<FileObject> getRelevantFiles(TreePathHandle tph) {
        ClasspathInfo cpInfo = getClasspathInfo(moveRefactoring);
        ClassIndex idx = cpInfo.getClassIndex();
        Set<FileObject> set = new HashSet<FileObject>();
        elementHandles.clear();
        ElementHandle elementHandle = tph.getElementHandle();
        handlesToMove.add(elementHandle);
        set.add(SourceUtils.getFile(elementHandle, cpInfo));
        Set<FileObject> files = idx.getResources(elementHandle,
                EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES,
                ClassIndex.SearchKind.IMPLEMENTORS),
                EnumSet.of(ClassIndex.SearchScope.SOURCE));
        set.addAll(files);
        Set<ElementHandle<TypeElement>> handles = idx.getElements(elementHandle,
                EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES,
                ClassIndex.SearchKind.IMPLEMENTORS),
                EnumSet.of(ClassIndex.SearchScope.SOURCE));
        elementHandles.addAll(handles);
        TreePathHandle targetHandle = moveRefactoring.getTarget().lookup(TreePathHandle.class);
        if(targetHandle != null) {
            set.add(targetHandle.getFileObject());
        }
        return set;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        case FASTCHECKPARAMETERS: {
            ClasspathInfo cpInfo = getClasspathInfo(moveRefactoring);
            TreePathHandle tph = moveRefactoring.getRefactoringSource().lookup(TreePathHandle.class);
            return JavaSource.create(cpInfo, tph.getFileObject());
        }
        default:
            TreePathHandle tph = moveRefactoring.getRefactoringSource().lookup(TreePathHandle.class);
            return JavaSource.forFileObject(tph.getFileObject());
        }
    }

    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        fireProgressListenerStart(AbstractRefactoring.PREPARE, -1);
        TreePathHandle tph = moveRefactoring.getRefactoringSource().lookup(TreePathHandle.class);
        Set<FileObject> a = getRelevantFiles(tph);
        Problem p = checkProjectDeps(a);
        fireProgressListenerStep(a.size());
        MoveClassTransformer transformer;
        URL target = moveRefactoring.getTarget().lookup(URL.class);
        if(target != null) {
            transformer = new MoveClassTransformer(tph, target);
        } else {
            transformer = new MoveClassTransformer(tph, moveRefactoring.getTarget().lookup(TreePathHandle.class).getElementHandle());
        }
        TransformTask task = new TransformTask(transformer, null);
        Problem prob = createAndAddElements(a, task, elements, moveRefactoring);
        if(transformer.deleteFile()) {
            elements.add(moveRefactoring, new DeleteFile(tph.getFileObject(), elements));
        }
        fireProgressListenerStop();
        return prob != null ? prob : JavaPluginUtils.chainProblems(p, transformer.getProblem());
    }

    @SuppressWarnings("CollectionContainsUrl")
    private Problem checkProjectDeps(Set<FileObject> a) {
        ClasspathInfo cpInfo = getClasspathInfo(moveRefactoring);
        Set<FileObject> sourceRoots = new HashSet<FileObject>();
        for (TreePathHandle tph : moveRefactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            FileObject file = tph.getFileObject();
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(file);
                sourceRoots.add(root);
            }
        }
        // XXX: there should be no URL in lookup when moving to Class
        URL target = moveRefactoring.getTarget().lookup(URL.class);
        if (target == null) {
            return null;
        }
        try {
            FileObject r = RefactoringUtils.getClassPathRoot(target);
            URL targetUrl = URLMapper.findURL(r, URLMapper.EXTERNAL);
            Project targetProject = FileOwnerQuery.getOwner(r);
            Set<URL> deps = SourceUtils.getDependentRoots(targetUrl);
            for (FileObject sourceRoot : sourceRoots) {
                URL sourceUrl = URLMapper.findURL(sourceRoot, URLMapper.INTERNAL);
                if (!deps.contains(sourceUrl)) {
                    Project sourceProject = FileOwnerQuery.getOwner(sourceRoot);
                    for (ElementHandle<TypeElement> affected : elementHandles) {
                        FileObject affectedFile = SourceUtils.getFile(affected, cpInfo);
                        if (FileOwnerQuery.getOwner(affectedFile).equals(sourceProject)
                                && !handlesToMove.contains(affected)
                                && !sourceProject.equals(targetProject)) {
                            assert sourceProject != null;
                            assert targetProject != null;
                            String sourceName = ProjectUtils.getInformation(sourceProject).getDisplayName();
                            String targetName = ProjectUtils.getInformation(targetProject).getDisplayName();
                            return createProblem(null, false, NbBundle.getMessage(MoveFileRefactoringPlugin.class, "ERR_MissingProjectDeps", sourceName, targetName));
                        }
                    }
                }
            }
        } catch (IOException iOException) {
            Exceptions.printStackTrace(iOException);
        }
        return null;
    }
}
