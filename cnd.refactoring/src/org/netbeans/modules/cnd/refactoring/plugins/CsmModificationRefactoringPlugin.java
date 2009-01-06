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

package org.netbeans.modules.cnd.refactoring.plugins;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class CsmModificationRefactoringPlugin extends CsmRefactoringPlugin {
    // the context object where refactoring starts

    private final CsmObject startReferenceObject;
    private final AbstractRefactoring refactoring;

    protected CsmModificationRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
        this.startReferenceObject = refactoring.getRefactoringSource().lookup(CsmObject.class);
        assert startReferenceObject != null : "no start reference";
    }

    protected final CsmObject getStartReferenceObject() {
        return startReferenceObject;
    }

    protected abstract Collection<CsmObject> getRefactoredObjects();

    public final Problem prepare(RefactoringElementsBag elements) {
        Collection<CsmObject> referencedObjects = getRefactoredObjects();
        if (referencedObjects == null || referencedObjects.size() == 0) {
            return null;
        }
        Collection<CsmFile> files = new HashSet<CsmFile>();
        CsmFile startFile = getCsmFile(getStartReferenceObject());
        for (CsmObject obj : referencedObjects) {
            Collection<CsmProject> prjs = CsmRefactoringUtils.getRelatedCsmProjects(obj, true);
            CsmProject[] ar = prjs.toArray(new CsmProject[prjs.size()]);
            refactoring.getContext().add(ar);
            files.addAll(getRelevantFiles(startFile, obj, refactoring));
        }
        fireProgressListenerStart(ProgressEvent.START, files.size());
        createAndAddElements(files, elements, refactoring);
        fireProgressListenerStop();
        return null;
    }

    protected final Problem checkIfModificationPossible(Problem problem, CsmObject referencedObject,
            String fatalMessage, String warnMessage) {
        // check read-only elements
        problem = checkIfModificationPossibleInFile(problem, referencedObject);
        fireProgressListenerStep();
        if (problem != null) {
            return problem;
        }
        if (CsmKindUtilities.isMethod(referencedObject)) {
            fireProgressListenerStep();
            CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
            if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                Collection<CsmMethod> overridenMethods = CsmVirtualInfoQuery.getDefault().getOverridenMethods(method, true);
                if (overridenMethods.size() > 1) {
                    // check all overriden methods
                    for (CsmMethod csmMethod : overridenMethods) {
                        problem = checkIfModificationPossibleInFile(problem, csmMethod);
                        CsmFunction def = csmMethod.getDefinition();
                        if (def != null && !csmMethod.equals(def)) {
                            problem = checkIfModificationPossibleInFile(problem, def);
                        }
                    }
                    boolean fatal = (problem != null);
                    String msg = fatal ? fatalMessage : warnMessage;
                    problem = createProblem(problem, fatal, msg);
                }
            }
        } else {
            fireProgressListenerStep();
        }
        return problem;
    }

    private Problem checkIfModificationPossibleInFile(Problem problem, CsmObject csmObject) {
        CsmFile csmFile = null;
        if (CsmKindUtilities.isFile(csmObject)) {
            csmFile = (CsmFile) csmObject;
        } else if (CsmKindUtilities.isOffsetable(csmObject)) {
            csmFile = ((CsmOffsetable) csmObject).getContainingFile();
        }
        if (csmFile != null) {
            FileObject fo = CsmUtilities.getFileObject(csmFile);
            if (!CsmRefactoringUtils.isRefactorable(fo)) {
                problem = createProblem(problem, true, getCannotRename(fo));
            }
            // check that object is in opened project
            if (!CsmRefactoringUtils.isElementInOpenProject(fo)) {
                problem = new Problem(true, NbBundle.getMessage(CsmModificationRefactoringPlugin.class, "ERR_ProjectNotOpened"));
                return problem;
            }
        }
        return problem;
    }

    private String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(CsmModificationRefactoringPlugin.class, "ERR_CannotModifyInFile")).format(new Object[]{r.getNameExt()});
    }

    @Override
    protected final ModificationResult processFiles(Collection<CsmFile> files) {
        ModificationResult out = null;
        for (CsmFile csmFile : files) {
            if (isCancelled()) {
                // may be return what we already have?
                return null;
            }
            if (out == null) {
                out = new ModificationResult(csmFile.getProject());
            }
            processFile(csmFile, out);
            fireProgressListenerStep();
        }
        return out;
    }
    
    protected final void processFile(CsmFile csmFile, ModificationResult mr) {
        Collection<CsmObject> referencedObjects = getRefactoredObjects();
        assert referencedObjects != null && referencedObjects.size() > 0 : "method must be called for resolved element";
        FileObject fo = CsmUtilities.getFileObject(csmFile);
        Collection<CsmReference> refs = new LinkedHashSet<CsmReference>();
        for (CsmObject obj : referencedObjects) {
            Collection<CsmReference> curRefs = CsmReferenceRepository.getDefault().getReferences(obj, csmFile, CsmReferenceKind.ALL, null);
            refs.addAll(curRefs);
        }
        if (refs.size() > 0) {
            List<CsmReference> sortedRefs = new ArrayList<CsmReference>(refs);
            Collections.sort(sortedRefs, new Comparator<CsmReference>() {
                public int compare(CsmReference o1, CsmReference o2) {
                    return o1.getStartOffset() - o2.getStartOffset();
                }
            });
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
            processRefactoredReferences(sortedRefs, fo, ces, mr);
        }
    }

    protected abstract void processRefactoredReferences(List<CsmReference> sortedRefs, FileObject fo, CloneableEditorSupport ces, ModificationResult mr);

}
