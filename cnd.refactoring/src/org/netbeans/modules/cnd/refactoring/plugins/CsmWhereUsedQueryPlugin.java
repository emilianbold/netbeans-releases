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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository.Interrupter;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.cnd.refactoring.elements.CsmRefactoringElementImpl;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Actual implementation of Find Usages query search for C/C++
 * 
 * @todo Perform index lookups to determine the set of files to be checked!
 * 
 * @author Vladimir Voskresensky
 */
public class CsmWhereUsedQueryPlugin extends CsmRefactoringPlugin {
    private final WhereUsedQuery refactoring;
    private final CsmObject startReferenceObject;
    
    /** Creates a new instance of WhereUsedQuery */
    public CsmWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
        startReferenceObject = refactoring.getRefactoringSource().lookup(CsmObject.class);
    }
    
    public Problem prepare(final RefactoringElementsBag elements) {
        CsmUID referencedObjectUID = refactoring.getRefactoringSource().lookup(CsmUID.class);
        CsmObject referencedObject = referencedObjectUID == null ? null : (CsmObject) referencedObjectUID.getObject();
        if (referencedObject == null) {
            return null;
        }
        Collection<RefactoringElementImplementation> res = doPrepareElements(referencedObject);
        if (res != null) {
            elements.addAll(refactoring, res);
        }
        fireProgressListenerStop();
        return null;
    }

    /*package*/ Collection<RefactoringElementImplementation> doPrepareElements(CsmObject referencedObject) {
        Collection<RefactoringElementImplementation> res = null;
        referencedObject = CsmRefactoringUtils.convertToCsmObjectIfNeeded(referencedObject);
        if (referencedObject == null) {
            return null;
        }
        if (isFindUsages()) {
            if (CsmKindUtilities.isFile(referencedObject)) {
                fireProgressListenerStart(ProgressEvent.START, 2);
                res = processIncludeQuery((CsmFile)referencedObject);
            } else {
                Collection<CsmObject> referencedObjects = getObjectsForFindUsages(referencedObject);
                CsmFile startFile = CsmRefactoringUtils.getCsmFile(startReferenceObject);
                Set<CsmFile> files = new HashSet<CsmFile>();
                for (CsmObject csmObject : referencedObjects) {
                    files.addAll(getRelevantFiles(startFile, csmObject, refactoring));
                }
                fireProgressListenerStart(ProgressEvent.START, files.size() + 2);
                res = processObjectUsagesQuery(referencedObjects, files);
            }
        } else if (isFindDirectSubclassesOnly() || isFindSubclasses()) {
            assert CsmKindUtilities.isClass(referencedObject) : "must be class";
            fireProgressListenerStart(ProgressEvent.START, 2);
            res = processSubclassesQuery((CsmClass)referencedObject);
        } else if (isFindOverridingMethods()) {
            assert CsmKindUtilities.isMethod(referencedObject) : "must be method";
            fireProgressListenerStart(ProgressEvent.START, 2);
            res = processOverridenMethodsQuery((CsmMethod)referencedObject);
        }   
        fireProgressListenerStep();
        return res;
    }

    @Override
    public Problem preCheck() {
        CsmUID uid = refactoring.getRefactoringSource().lookup(CsmUID.class);    
        Problem invalidContext = new Problem(true, NbBundle.getMessage(CsmWhereUsedQueryPlugin.class, "MSG_InvalidObjectNothingToFind")); // NOI18N;
        if (uid == null) {
            CsmFile startFile = CsmRefactoringUtils.getCsmFile(startReferenceObject);
            if (startFile == null || !startFile.isValid()) {
                return invalidContext;
            }              
            return super.preCheck();
        }
        CsmObject referencedObject = (CsmObject) uid.getObject();
        if (!CsmBaseUtilities.isValid(referencedObject)) {
            return invalidContext;
        }
        return super.preCheck();
    }

    @Override
    public Problem fastCheckParameters() {
        CsmUID uid = refactoring.getRefactoringSource().lookup(CsmUID.class);    
        if (uid != null && CsmKindUtilities.isMethod((CsmObject)uid.getObject())) {
            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
        } else {
            return super.fastCheckParameters();
        }
    }
    
    
    //    //@Override
//    protected Problem fastCheckParameters(CompilationController info) {
//        if (searchHandle.getKind() == ElementKind.METHOD) {
//            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
//        } 
//        return null;
//    }
//    
//    //@Override
//    protected Problem checkParameters(CompilationController info) {
//        return null;
//    }
    
    private Problem checkParametersForMethod(boolean overriders, boolean usages) {
        if (!(usages || overriders)) {
            return new Problem(true, NbBundle.getMessage(CsmWhereUsedQueryPlugin.class, "MSG_NothingToFind"));
        } else {
            return null;
        }
    }

    private Collection<CsmObject> getObjectsForFindUsages(CsmObject referencedObject) {
        Collection<CsmObject> out = new LinkedHashSet<CsmObject>();
        if (isFindUsages()) {
            if (CsmKindUtilities.isMethod(referencedObject)) {
                CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
                if (isFindOverridingMethods() && CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                    out.addAll(CsmVirtualInfoQuery.getDefault().getOverridenMethods(method, isSearchFromBaseClass()));
                }
            } else if (CsmKindUtilities.isClass(referencedObject)) {
                // add all constructors
                out.addAll(CsmRefactoringUtils.getConstructors((CsmClass)referencedObject));
            }
            out.add(referencedObject);
        }
        return out;
    }
        
    private boolean isFindSubclasses() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }
    
    private boolean isFindUsages() {
        return refactoring.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
    }
    
    private boolean isFindDirectSubclassesOnly() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }
    
    private boolean isFindOverridingMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }

    private boolean isSearchFromBaseClass() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS);
    }

    private boolean isSearchInComments() {
        return refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS);
    }
    
    private Collection<RefactoringElementImplementation> processObjectUsagesQuery(
                                                            final Collection<CsmObject> csmObjects,            
                                                            final Collection<CsmFile> files) {
        assert isFindUsages() : "must be find usages mode";
        final CsmReferenceRepository xRef = CsmReferenceRepository.getDefault();
        final Collection<RefactoringElementImplementation> elements = new ConcurrentLinkedQueue<RefactoringElementImplementation>();
        //Set<CsmReferenceKind> kinds = isFindOverridingMethods() ? CsmReferenceKind.ALL : CsmReferenceKind.ANY_USAGE;
        final Set<CsmReferenceKind> kinds = CsmReferenceKind.ALL;
        final CsmObject[] objs = csmObjects.toArray(new CsmObject[csmObjects.size()]);
        final Interrupter interrupter = new Interrupter(){
            public boolean cancelled() {
                return isCancelled();
            }
        };
        RequestProcessor rp = new RequestProcessor("FindUsagesQuery", CndUtils.getNumberCndWorkerThreads() + 1); // NOI18N
        final CountDownLatch waitFinished = new CountDownLatch(files.size());
        for (final CsmFile file : files) {
            Runnable task = new Runnable() {
                public void run() {
                    try {
                        if (!isCancelled()) {
                            String oldName = Thread.currentThread().getName();
                            try {
                                Thread.currentThread().setName("FindUsagesQuery: Analyzing " + file.getAbsolutePath()); //NOI18N
                                Collection<CsmReference> refs = xRef.getReferences(objs, file, kinds, interrupter);
                                for (CsmReference csmReference : refs) {
                                    elements.add(CsmRefactoringElementImpl.create(csmReference, true));
                                }
                            } finally {
                                Thread.currentThread().setName(oldName);
                            }
                            synchronized (CsmWhereUsedQueryPlugin.this) {
                                fireProgressListenerStep();
                            }
                        }
                    } finally {
                        waitFinished.countDown();
                    }
                }
            };
            rp.post(task);
        }
        try {
            waitFinished.await();
        } catch (InterruptedException ex) {
        }
        return elements;
    }
    
    private Collection<RefactoringElementImplementation> processOverridenMethodsQuery(final CsmMethod csmMethod) {
        assert isFindOverridingMethods() : "must be search for overriden methods";
        Collection<RefactoringElementImplementation> elements = new LinkedHashSet<RefactoringElementImplementation>(1024);
        Collection<CsmMethod> overrides = CsmVirtualInfoQuery.getDefault().getOverridenMethods(csmMethod, isSearchFromBaseClass());        
        overrides.add(csmMethod);
        for (CsmMethod method : overrides) {
            CsmReference declRef = CsmReferenceSupport.createObjectReference(method);
            elements.add(CsmRefactoringElementImpl.create(declRef, false));
            // find defintion of method if needed
            if (!CsmKindUtilities.isFunctionDefinition(method)) {
                CsmFunctionDefinition def = method.getDefinition();
                if (def != null) {
                    CsmReference defRef = CsmReferenceSupport.createObjectReference(def);
                    elements.add(CsmRefactoringElementImpl.create(defRef, false));
                }
            }
        } 
        return elements;
    }

    protected final ModificationResult processFiles(Collection<CsmFile> files, AtomicReference<Problem> outProblem) {
        // where used query does not modify files
        return null;
    }

    private Collection<RefactoringElementImplementation> processIncludeQuery(final CsmFile csmFile) {
        assert isFindUsages() : "must be find usages";
        Collection<RefactoringElementImplementation> elements = new LinkedHashSet<RefactoringElementImplementation>(1024);
        Collection<CsmReference> refs = CsmIncludeHierarchyResolver.getDefault().getIncludes(csmFile);
        for (CsmReference csmReference : refs) {
            elements.add(CsmRefactoringElementImpl.create(csmReference, false));
        } 
        return elements;
    }
    
    private Collection<RefactoringElementImplementation> processSubclassesQuery(final CsmClass referencedClass) {
        assert isFindDirectSubclassesOnly() || isFindSubclasses() : "must be search of subclasses";
        Collection<RefactoringElementImplementation> elements = new LinkedHashSet<RefactoringElementImplementation>(1024);
        boolean directSubtypesOnly = isFindDirectSubclassesOnly();
        Collection<CsmReference> refs = CsmTypeHierarchyResolver.getDefault().getSubTypes(referencedClass, directSubtypesOnly);
        for (CsmReference csmReference : refs) {
            elements.add(CsmRefactoringElementImpl.create(csmReference, false));
        }         
        return elements;
    }     
}
