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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.elements.DiffElement;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.cnd.refactoring.support.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.api.*;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * base class for C/C++ refactoring plug-ins
 * 
 * @author Vladimir Voskresensky
 */
public abstract class CsmRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    protected volatile boolean cancelRequest = false;

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return fastCheckParameters();
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public final void cancelRequest() {
        cancelRequest = true;
    }

    protected final boolean isCancelled() {
        return cancelRequest;
    }

    protected abstract ModificationResult processFiles(Collection<CsmFile> files, AtomicReference<Problem> outProblem);

    private Collection<ModificationResult> processFiles(Iterable<? extends List<CsmFile>> fileGroups, AtomicReference<Problem> outProblem) {
        Collection<ModificationResult> results = new LinkedList<ModificationResult>();
        for (List<CsmFile> list : fileGroups) {
            if (isCancelled()) {
                // may be return partial "results"?
                return Collections.<ModificationResult>emptyList();
            }
            ModificationResult modification = processFiles(list, outProblem);
            if (modification != null) {
                results.add(modification);
            }
        }
        return results;
    }

    protected final Problem createAndAddElements(Collection<CsmFile> files, RefactoringElementsBag elements, AbstractRefactoring refactoring) {
        Iterable<? extends List<CsmFile>> fileGroups = groupByRoot(files);
        AtomicReference<Problem> outProblem = new AtomicReference<Problem>(null);
        final Collection<ModificationResult> results = processFiles(fileGroups, outProblem);
        elements.registerTransaction(new RefactoringCommit(results));
        for (ModificationResult result : results) {
            for (FileObject fo : result.getModifiedFileObjects()) {
                for (Difference dif : result.getDifferences(fo)) {
                    elements.add(refactoring, DiffElement.create(dif, fo, result));
                }
            }
        }
        return outProblem.get();
    }

    protected static final Problem createProblem(Problem prevProblem, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (prevProblem == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(prevProblem);
            return problem;
        } else {
            //problem.setNext(result.getNext());
            //result.setNext(problem);

            // [TODO] performance
            Problem p = prevProblem;
            while (p.getNext() != null) {
                p = p.getNext();
            }
            p.setNext(problem);
            return prevProblem;
        }
    }

    private Iterable<? extends List<CsmFile>> groupByRoot(Iterable<? extends CsmFile> files) {
        Map<CsmProject, List<CsmFile>> result = new HashMap<CsmProject, List<CsmFile>>();
        for (CsmFile file : files) {
            CsmProject prj = file.getProject();
            if (prj != null) {
                List<CsmFile> group = result.get(prj);
                if (group == null) {
                    group = new LinkedList<CsmFile>();
                    result.put(prj, group);
                }
                group.add(file);
            }
        }
        return result.values();
    }

    protected Collection<CsmFile> getRelevantFiles(CsmFile startFile, CsmObject referencedObject, AbstractRefactoring refactoring) {
        CsmObject enclScope = referencedObject == null ? null : CsmRefactoringUtils.getEnclosingElement(referencedObject);
        CsmFile scopeFile = null;
        if (enclScope == null && !CsmKindUtilities.isNamespace(referencedObject)) {
            return Collections.<CsmFile>emptyList();
        }
        if (CsmKindUtilities.isFunction(enclScope)) {
            scopeFile = ((CsmOffsetable) enclScope).getContainingFile();
        } else if (CsmKindUtilities.isNamespaceDefinition(enclScope)) {
            CsmNamespace ns = ((CsmNamespaceDefinition) enclScope).getNamespace();
            if (ns != null && ns.getName().length() == 0) {
                // this is unnamed namespace and has file local visibility
                // if declared in source file which is not included anywhere
                if (isDeclarationInLeafFile(enclScope)) {
                    scopeFile = ((CsmNamespaceDefinition) enclScope).getContainingFile();
                }
            }
        } else if (CsmKindUtilities.isFunction(referencedObject)) {
            // this is possible file local function
            // if declared in source file which is not included anywhere
            if (CsmBaseUtilities.isFileLocalFunction((CsmFunction) referencedObject)) {
                if (isDeclarationInLeafFile(referencedObject)) {
                    scopeFile = ((CsmFunction) referencedObject).getContainingFile();
                }
            }
        }
        if (startFile.equals(scopeFile)) {
            return Collections.singleton(scopeFile);
        } else {
            CsmProject[] prjs = refactoring.getContext().lookup(CsmProject[].class);
            CsmFile declFile = CsmRefactoringUtils.getCsmFile(referencedObject);
            if (prjs == null || prjs.length == 0 || declFile == null) {
                CsmProject prj = startFile.getProject();
                return prj.getAllFiles();
            } else {
                CsmProject declPrj = declFile.getProject();
                Collection<CsmProject> relevantPrjs = new HashSet<CsmProject>();
                for (CsmProject csmProject : prjs) {
                    // if the same project or declaration from shared library
                    if (csmProject.equals(declPrj) || csmProject.getLibraries().contains(declPrj)) {
                        relevantPrjs.add(csmProject);
                    }
                }
                Collection<CsmFile> relevantFiles = new HashSet<CsmFile>();
                for (CsmProject csmProject : relevantPrjs) {
                    relevantFiles.addAll(csmProject.getAllFiles());
                }
                return relevantFiles;
            }
        }
    }

    private boolean isDeclarationInLeafFile(CsmObject obj) {
        boolean out = false;
        if (CsmKindUtilities.isOffsetable(obj)) {
            CsmFile file = ((CsmOffsetable) obj).getContainingFile();
            // check that file is not included anywhere yet
            out = CsmIncludeHierarchyResolver.getDefault().getFiles(file).isEmpty();
        }
        return out;
    }

    protected Problem isResovledElement(CsmObject ref) {
        if (ref == null) {
            //reference is null or is not valid.
            return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElNotAvail")); // NOI18N
        } else {
            CsmObject referencedObject = CsmRefactoringUtils.getReferencedElement(ref);
            if (referencedObject == null) {
                return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElementNotResolved"));
            }
            if (!CsmBaseUtilities.isValid(referencedObject)) {
                return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElementNotResolved"));
            }
            // element is still available
            return null;
        }
    }
}
