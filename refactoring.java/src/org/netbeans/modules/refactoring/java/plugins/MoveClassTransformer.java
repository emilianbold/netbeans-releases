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

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 */
public class MoveClassTransformer extends RefactoringVisitor {

    private Problem problem;
    private final ElementHandle<TypeElement> elementHandle;
    private final URL targetURL;
    private boolean inMovingClass;
    private Set<Element> elementsToImport;
    private Set<Element> elementsAlreadyImported;
    private final String targetPackageName;
    private boolean moveToDefaulPackageProblem = false;
    private String originalPackage;
    private HashSet<ImportTree> importToRemove;
    private HashSet<String> importToAdd;
    private boolean isThisFileReferencingOldPackage = false;

    public MoveClassTransformer(ElementHandle<TypeElement> elementHandle, URL targetURL) {
        this.elementHandle = elementHandle;
        this.targetURL = targetURL;
        this.targetPackageName = RefactoringUtils.getPackageName(targetURL);
    }

    Problem getProblem() {
        return problem;
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        super.setWorkingCopy(workingCopy);
        this.elementsAlreadyImported = new HashSet<Element>();
        this.originalPackage = getPackageOf(this.elementHandle.resolve(workingCopy)).getQualifiedName().toString();
        this.elementsToImport = new HashSet<Element>();
        this.importToRemove = new HashSet<ImportTree>();
        this.importToAdd = new HashSet<String>();
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        Tree result = super.visitCompilationUnit(node, p);

        List<? extends Tree> typeDecls = node.getTypeDecls();
        CompilationUnitTree cut = node;
        for (Tree clazz : typeDecls) {
            TypeMirror type = workingCopy.getTrees().getTypeMirror(new TreePath(getCurrentPath(), clazz));
            if (workingCopy.getTypes().isSameType(type, elementHandle.resolve(workingCopy).asType())) {
                cut = make.removeCompUnitTypeDecl(cut, clazz);

                try {
                    FileObject targetRoot = RefactoringUtils.getClassPathRoot(targetURL);
                    FileObject target = getOrCreateFolder(targetURL);
                    String relativePath = FileUtil.getRelativePath(targetRoot, target);
                    List<ImportTree> imports = new LinkedList();
                    if (isThisFileReferencingOldPackage) {
                        //add import to old package
                        ExpressionTree newPackageName = cut.getPackageName();
                        if (newPackageName != null) {
                            for (String importToAddItem : importToAdd) {
                                imports.add(make.Import(make.QualIdent(importToAddItem), false));
                            }
                        } else {
                            if (!moveToDefaulPackageProblem) {
                                problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage")));
                                moveToDefaulPackageProblem = true;
                            }
                        }
                    }

                    GeneratorUtilities.get(workingCopy).importComments(clazz, node);
                    CompilationUnitTree compilationUnit = make.CompilationUnit(targetRoot, relativePath + "/" + ((ClassTree) clazz).getSimpleName() + ".java", imports, (List<? extends Tree>) Collections.singletonList(clazz));
                    CompilationUnitTree importFQNs = GeneratorUtilities.get(workingCopy).importFQNs(compilationUnit);
                    rewrite(null, importFQNs);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return result;
        }

        List<? extends ImportTree> imports = cut.getImports();
        if (!importToRemove.isEmpty()) {
            List<ImportTree> temp = new ArrayList<ImportTree>(imports);
            temp.removeAll(importToRemove);
            imports = temp;
        }
        if (!inMovingClass && !importToRemove.isEmpty()) {
            cut = make.CompilationUnit(cut.getPackageName(), imports, cut.getTypeDecls(), cut.getSourceFile());
        }

        for (Element el : elementsToImport) {
            if (!"".equals(targetPackageName)) { // NOI18N
                cut = insertImport(cut, targetPackageName + '.' + el.getSimpleName());
            }
        }
        rewrite(node, cut);
        return result;
    }

    @Override
    public Tree visitClass(ClassTree node, Element p) {
        Element element = workingCopy.getTrees().getElement(getCurrentPath());
        if (isTopLevelClass(element)) {
            inMovingClass = element == elementHandle.resolve(workingCopy);
        }
        return super.visitClass(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            final Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null) {
                if (isElementMoving(el)) {
                    elementsAlreadyImported.add(el);
                    String newPackageName = targetPackageName;

                    if (!"".equals(newPackageName)) {
                        Tree nju = make.MemberSelect(make.Identifier(newPackageName), el);
                        rewrite(node, nju);
                    } else {
                        if (!moveToDefaulPackageProblem) {
                            problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage")));
                            moveToDefaulPackageProblem = true;
                        }
                    }
                } else {
                    if (inMovingClass) {
                        if (el.getKind() != ElementKind.PACKAGE) {
                            Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                            EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                            Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                            if (enclosingTypeElement != null && enclosingClass != null
                                    && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                            }

                            TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
                            if (outermostTypeElement == null && (el.getKind().isClass() || el.getKind().isInterface())) {
                                outermostTypeElement = (TypeElement) el;
                            }
                            if (!targetPackageName.equals(originalPackage)
                                    && getPackageOf(el).toString().equals(originalPackage)
                                    && (!(containsAnyOf(el, neededMods))
                                    || (enclosingTypeElement != null ? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                    && !isElementMoving(outermostTypeElement)) {
                                problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature2", workingCopy.getFileObject().getName(), el, outermostTypeElement.getSimpleName())));
                            }
                        }
                    } else {
                        if (el.getKind() != ElementKind.PACKAGE) {
                            Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                            EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                            Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                            if (enclosingTypeElement != null && enclosingClass != null
                                    && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                            }

                            TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
                            if (outermostTypeElement == null && (el.getKind().isClass() || el.getKind().isInterface())) {
                                outermostTypeElement = (TypeElement) el;
                            }
                            if (!targetPackageName.equals(originalPackage)
                                    && getPackageOf(el).toString().equals(originalPackage)
                                    && (!(containsAnyOf(el, neededMods))
                                    || (enclosingTypeElement != null ? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                    && isElementMoving(outermostTypeElement)) {
                                problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature", workingCopy.getFileObject().getName(), el, outermostTypeElement.getSimpleName())));
                            }
                        }
                    }
                }
            }
        }
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null) {
                if (!inMovingClass) {
                    if (isElementMoving(el)) {
                        if (!elementsAlreadyImported.contains(el)) {
                            if (!RefactoringUtils.getPackageName(workingCopy.getCompilationUnit()).equals(targetPackageName)) {
                                elementsToImport.add(el);
                            }
                        }
                    } else if (el.getKind() != ElementKind.PACKAGE) {
                        Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                        EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                        Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                        if (enclosingTypeElement != null && enclosingClass != null
                                && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                            neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                        }

                        TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
                        if (outermostTypeElement == null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            outermostTypeElement = (TypeElement) el;
                        }
                        if (!targetPackageName.equals(originalPackage)
                                && getPackageOf(el).toString().equals(originalPackage)
                                && (!(containsAnyOf(el, neededMods))
                                || (enclosingTypeElement != null ? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                && isElementMoving(outermostTypeElement)) {
                            problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature", workingCopy.getFileObject().getName(), el, outermostTypeElement.getSimpleName())));
                        }
                    }
                } else {
                    if (isTopLevelClass(el) && !isElementMoving(el) && getPackageOf(el).toString().equals(originalPackage)) {
                        importToAdd.add(el.toString());
                        isThisFileReferencingOldPackage = true;
                    }
                    if (el.getKind() != ElementKind.PACKAGE) {
                        Element enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(el);

                        EnumSet<Modifier> neededMods = EnumSet.of(Modifier.PUBLIC);
                        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, getCurrentPath(), true, true, true, true, false);
                        if (enclosingClassPath != null) {
                            Element enclosingClass = workingCopy.getTrees().getElement(enclosingClassPath);
                            if (enclosingTypeElement != null && enclosingClass != null
                                    && workingCopy.getTypes().isSubtype(enclosingClass.asType(), enclosingTypeElement.asType())) {
                                neededMods = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
                            }
                        }

                        TypeElement outermostTypeElement = workingCopy.getElementUtilities().outermostTypeElement(el);
                        if (outermostTypeElement == null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            outermostTypeElement = (TypeElement) el;
                        }
                        if (!targetPackageName.equals(originalPackage)
                                && getPackageOf(el).toString().equals(originalPackage)
                                && (!(containsAnyOf(el, neededMods))
                                || (enclosingTypeElement != null ? !containsAnyOf(enclosingTypeElement, neededMods) : false))
                                && !isElementMoving(outermostTypeElement)) {
                            problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature2", workingCopy.getFileObject().getName(), el, outermostTypeElement.getSimpleName())));
                        }
                    }
                }
            }
        }

        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitImport(ImportTree node, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            final Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), node.getQualifiedIdentifier()));
            if (el != null) {
                if (isElementMoving(el)) {
                    if (!"".equals(targetPackageName)) {
                        String cuPackageName = RefactoringUtils.getPackageName(workingCopy.getCompilationUnit());
                        if (cuPackageName.equals(targetPackageName)) { //remove newly created import from same package
                            importToRemove.add(node);
                            return node;
                        }
                    }
                }
            }
        }
        return super.visitImport(node, p);
    }

    private boolean isElementMoving(Element el) {
        ElementKind kind = el.getKind();
        if (!(kind.isClass() || kind.isInterface())) {
            return false;
        }
        TypeElement resolved = elementHandle.resolve(workingCopy);
        return el == resolved;
    }

    private PackageElement getPackageOf(Element el) {
        //return workingCopy.getElements().getPackageOf(el);
        while (el.getKind() != ElementKind.PACKAGE) {
            el = el.getEnclosingElement();
        }
        return (PackageElement) el;
    }

    private boolean containsAnyOf(Element el, EnumSet<Modifier> neededMods) {
        for (Modifier mod : neededMods) {
            if (el.getModifiers().contains(mod)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTopLevelClass(Element el) {
        return (el.getKind().isClass()
                || el.getKind().isInterface())
                && el.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }

    private CompilationUnitTree insertImport(CompilationUnitTree node, String imp) {
        for (ImportTree tree : node.getImports()) {
            if (tree.getQualifiedIdentifier().toString().equals(imp)) {
                return node;
            }
        }
        CompilationUnitTree nju = make.insertCompUnitImport(node, 0, make.Import(make.Identifier(imp), false));
        return nju;
    }

    /**
     * creates or finds FileObject according to
     *
     * @param url
     * @return FileObject
     */
    private FileObject getOrCreateFolder(URL url) throws IOException {
        try {
            FileObject result = URLMapper.findFileObject(url);
            if (result != null) {
                return result;
            }
            File f = new File(url.toURI());

            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
