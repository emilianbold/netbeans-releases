/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.plugins;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class CsmModificationRefactoringPlugin extends CsmRefactoringPlugin {
    // the context object where refactoring starts

    private final CsmObject startReferenceObject;
    private final CsmContext editorContext;
    private final AbstractRefactoring refactoring;

    protected CsmModificationRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
        this.startReferenceObject = refactoring.getRefactoringSource().lookup(CsmObject.class);
        this.editorContext = refactoring.getRefactoringSource().lookup(CsmContext.class);
        assert startReferenceObject != null || editorContext != null: "no start reference or editor context";
    }

    protected final CsmObject getStartReferenceObject() {
        return startReferenceObject;
    }
    
    protected final CsmContext getEditorContext() {
        return editorContext;
    }

    public final Problem prepare(RefactoringElementsBag elements) {
        Problem out = null;
        try {
            Collection<CsmFile> files = getRefactoredFiles();
            fireProgressListenerStart(ProgressEvent.START, files.size());
            out = createAndAddElements(files, elements, refactoring);
        } finally {
            fireProgressListenerStop();
        }
        return out;
    }

    protected abstract Collection<CsmFile> getRefactoredFiles();

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
                Collection<CsmMethod> overridenMethods = CsmVirtualInfoQuery.getDefault().getOverriddenMethods(method, true);
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
            if (problem ==null && !CsmRefactoringUtils.isElementInOpenProject(csmFile)) {
                problem = new Problem(false, NbBundle.getMessage(CsmModificationRefactoringPlugin.class, "ERR_ProjectNotOpened"));
                return problem;
            }
        }
        return problem;
    }

    private String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(CsmModificationRefactoringPlugin.class, "ERR_CannotModifyInFile")).format(new Object[]{r.getNameExt()});
    }

    @Override
    protected final ModificationResult processFiles(Collection<CsmFile> files, AtomicReference<Problem> outProblem) {
        ModificationResult out = null;
        for (CsmFile csmFile : files) {
            if (isCancelled()) {
                // may be return what we already have?
                return null;
            }
            if (out == null) {
                out = new ModificationResult(csmFile.getProject());
            }
            processFile(csmFile, out, outProblem);
            fireProgressListenerStep();
        }
        return out;
    }

    protected abstract void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem);
}
