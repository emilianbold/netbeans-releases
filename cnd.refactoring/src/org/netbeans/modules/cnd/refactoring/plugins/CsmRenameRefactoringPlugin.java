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


import org.netbeans.modules.refactoring.api.Problem;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
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
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * The actual Renaming refactoring work for C/C++. The skeleton (name checks etc.) based
 * on the Java refactoring module by Jan Becicka, Martin Matula, Pavel Flaska and Daniel Prusa.
 * 
 * @author Jan Becicka
 * @author Martin Matula
 * @author Pavel Flaska
 * @author Daniel Prusa
 * @author Vladimir Voskresensky
 *
 * @todo Complete this. Most of the prechecks are not implemented - and the refactorings themselves need a lot of work.
 */
public class CsmRenameRefactoringPlugin extends CsmRefactoringPlugin {
    
    private final CsmObject startReferenceObject;
    private Collection overriddenByMethods = null; // methods that override the method to be renamed
    private Collection overridesMethods = null; // methods that are overridden by the method to be renamed
    private boolean doCheckName = true;
    private Collection<CsmObject> referencedObjects;
    private final RenameRefactoring refactoring;
    
    /** Creates a new instance of RenameRefactoring */
    public CsmRenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        startReferenceObject = refactoring.getRefactoringSource().lookup(CsmObject.class);     
        assert startReferenceObject != null : "no start reference";
    }
    
    private static final String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(CsmRenameRefactoringPlugin.class, "ERR_CannotRenameFile")).format(new Object[] {r.getNameExt()});
    }
    
    @Override
    public Problem fastCheckParameters() {
        Problem fastCheckProblem = null;
        String newName = refactoring.getNewName();
        String oldName = CsmRefactoringUtils.getSimpleText(startReferenceObject);
        
        if (oldName.equals(newName)) {
            fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged")); // NOI18N
            return fastCheckProblem;
        }
        
        if (!Utilities.isJavaIdentifier(newName)) {
            String s = getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }        
        return fastCheckProblem;
    }
    
    @Override
    public Problem checkParameters() {
        return fastCheckParameters();
    }
    
    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 5);
        if (this.referencedObjects == null) {
            initReferencedObjects(startReferenceObject);
            fireProgressListenerStep();
        }    
        preCheckProblem = isResovledElement(startReferenceObject);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        CsmObject directReferencedObject = CsmRefactoringUtils.getReferencedElement(startReferenceObject);
        // check read-only elements
        preCheckProblem = checkRenameInFile(preCheckProblem, directReferencedObject);
        fireProgressListenerStep();
        if (preCheckProblem != null) {
            return preCheckProblem;            
        }
        if (CsmKindUtilities.isMethod(directReferencedObject)) {
            fireProgressListenerStep();
            CsmMethod method = (CsmMethod)directReferencedObject;
            if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                Collection<CsmMethod> overridenMethods = CsmVirtualInfoQuery.getDefault().getOverridenMethods(method, true);
                if (overridenMethods.size() > 1) {
                    // check all overriden methods
                    for (CsmMethod csmMethod : overridenMethods) {
                        preCheckProblem = checkRenameInFile(preCheckProblem, csmMethod);
                        CsmFunction def = csmMethod.getDefinition();
                        if (def != null && !csmMethod.equals(def)) {
                            preCheckProblem = checkRenameInFile(preCheckProblem, def);
                        }
                    }
                    boolean fatal = (preCheckProblem != null);
                    String msg = fatal? getString("ERR_Overrides_Fatal") : getString("ERR_OverridesOrOverriden");
                    preCheckProblem = createProblem(preCheckProblem, fatal, msg);                    
                }
            }
        } else {
            fireProgressListenerStep();
        }
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        if (this.referencedObjects == null || this.referencedObjects.size() == 0) {
            return null;
        }
        Collection<CsmFile> files = new HashSet<CsmFile>();
        CsmFile startFile = getCsmFile(startReferenceObject);
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
    
    @Override
    protected ModificationResult processFiles(Collection<CsmFile> files) {
        ModificationResult out = null;
        for (CsmFile csmFile : files) {
            if (cancelRequest) {
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
    
    private static final String getString(String key) {
        return NbBundle.getMessage(CsmRenameRefactoringPlugin.class, key);
    }

    private Problem checkRenameInFile(Problem problem, CsmObject csmObject) {
        if (CsmKindUtilities.isOffsetable(csmObject)) {
            FileObject fo = CsmUtilities.getFileObject(((CsmOffsetable)csmObject).getContainingFile());
            if (fo != null && (FileUtil.getArchiveFile(fo)!= null || !fo.canWrite())) {
                problem = createProblem(problem, true, getCannotRename(fo));
            }            
        }
        return problem;
    }

    private void initReferencedObjects(CsmObject startReferenceObject) {
        CsmObject referencedObject = CsmRefactoringUtils.getReferencedElement(startReferenceObject);
        if (referencedObject != null) {
            this.referencedObjects = new LinkedHashSet<CsmObject>();
            if (CsmKindUtilities.isClass(referencedObject)) {
                // for class we need to add all needed elements
                this.referencedObjects.addAll(getRenamingClassObjects((CsmClass)referencedObject));
            } else if (CsmKindUtilities.isConstructor(referencedObject) || CsmKindUtilities.isDestructor(referencedObject)) {
                // for constructor/destructor we need to add all needed elements
                CsmFunction fun = (CsmFunction)referencedObject;
                CsmClass cls = CsmBaseUtilities.getFunctionClass(fun);
                if (cls != null) {
                    this.referencedObjects.addAll(getRenamingClassObjects(cls));
                }
            } else if (CsmKindUtilities.isMethod(referencedObject)) {
                CsmMethod method = (CsmMethod)referencedObject;
                this.referencedObjects.add(method);
                if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                    this.referencedObjects.addAll(CsmVirtualInfoQuery.getDefault().getOverridenMethods(method, true));
                    assert !this.referencedObjects.isEmpty() : "must be at least start object " + method;
                }
            } else {
                this.referencedObjects.add(referencedObject);
            }
        }
    }

    private Collection<? extends CsmObject> getRenamingClassObjects(CsmClass clazz) {
        Collection<CsmObject> out = new ArrayList<CsmObject>(5);
        if (clazz != null) {
            out.add(clazz);
            for (CsmMember member : clazz.getMembers()) {
                if (CsmKindUtilities.isConstructor(member)) {
                    out.add(member);
                } else if (CsmKindUtilities.isDestructor(member)) {
                    out.add(member);
                }
            }
        }
        return out;

    }
    
    private void processFile(CsmFile csmFile, ModificationResult mr) {
        assert this.referencedObjects != null && this.referencedObjects.size() > 0: "method must be called for resolved element";
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
            String newName = refactoring.getNewName();
            for (CsmReference ref : sortedRefs) {
                String oldName = ref.getText().toString();
                String descr = getDescription(ref, oldName);
                Difference diff = rename(ref, ces, oldName, newName, descr);
                assert diff != null;
                mr.addDifference(fo, diff);
            }
        }
    }
    
    private String getDescription(CsmReference ref, String targetName) {
        String out = NbBundle.getMessage(CsmRenameRefactoringPlugin.class, "UpdateRef", targetName);
        return out;
    }
    
    private Difference rename(CsmReference ref, CloneableEditorSupport ces,
            String oldName, String newName, String descr) {
        if (oldName == null) {
            oldName = ref.getText().toString();
        }
        if (newName == null) {
            newName = refactoring.getNewName();
        }
        assert oldName != null;
        assert newName != null;
        PositionRef startPos = ces.createPositionRef(ref.getStartOffset(), Bias.Forward);
        PositionRef endPos = ces.createPositionRef(ref.getEndOffset(), Bias.Backward);
        Difference diff = new Difference(Difference.Kind.CHANGE, ref, startPos, endPos, oldName, newName, descr);
        return diff;
    }
}
