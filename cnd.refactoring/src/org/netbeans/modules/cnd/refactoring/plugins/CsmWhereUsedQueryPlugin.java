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
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.cnd.refactoring.elements.CsmRefactoringElementImpl;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.util.NbBundle;

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
        CsmFile startFile = getCsmFile(startReferenceObject);     
        Collection<CsmFile> files = getRelevantFiles(startFile, referencedObject, refactoring);
        fireProgressListenerStart(ProgressEvent.START, files.size());
        processQuery(referencedObject, elements, files);
        fireProgressListenerStop();
        return null;
    }

    @Override
    public Problem checkParameters() {
        return super.checkParameters();
    }

    @Override
    public Problem preCheck() {
        CsmUID uid = refactoring.getRefactoringSource().lookup(CsmUID.class);    
        Problem invalidContext = new Problem(true, NbBundle.getMessage(CsmWhereUsedQueryPlugin.class, "MSG_InvalidObjectNothingToFind")); // NOI18N;
        if (uid == null) {
            CsmFile startFile = getCsmFile(startReferenceObject);
            if (startFile == null || !startFile.isValid()) {
                return invalidContext;
            }              
            return super.preCheck();
        }
        CsmObject referencedObject = (CsmObject) uid.getObject();
        if (referencedObject == null) {
            return invalidContext;
        }
        if (referencedObject instanceof CsmValidable) {
            if (!((CsmValidable)referencedObject).isValid()) {
                return invalidContext;
            }
        }
        return super.preCheck();
    }

    @Override
    public Problem fastCheckParameters() {
        return super.fastCheckParameters();
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

    private void processQuery(final CsmObject csmObject, 
            final RefactoringElementsBag elements,
            final Collection<CsmFile> files) {
        if (isFindUsages()) {
            if (CsmKindUtilities.isFile(csmObject)) {
                Collection<CsmReference> refs = CsmIncludeHierarchyResolver.getDefault().getIncludes((CsmFile)csmObject);
                for (CsmReference csmReference : refs) {
                    elements.add(refactoring, CsmRefactoringElementImpl.create(csmReference, false));
                }      
            } else {
                CsmReferenceRepository xRef = CsmReferenceRepository.getDefault();
                for (CsmFile file : files) {
                    if (cancelRequest) {
                        break;
                    }
                    Collection<CsmReference> refs = xRef.getReferences(csmObject, file, true);
                    for (CsmReference csmReference : refs) {
                        elements.add(refactoring, CsmRefactoringElementImpl.create(csmReference, true));
                    }      
                    fireProgressListenerStep();
                }
            }
        } else if (isFindDirectSubclassesOnly() || isFindSubclasses()) {
            assert (CsmKindUtilities.isClass(csmObject));
            CsmClass referencedClass = (CsmClass)csmObject;
            boolean directSubtypesOnly = isFindDirectSubclassesOnly();
            Collection<CsmReference> refs = CsmTypeHierarchyResolver.getDefault().getSubTypes(referencedClass, directSubtypesOnly);
            for (CsmReference csmReference : refs) {
                elements.add(refactoring, CsmRefactoringElementImpl.create(csmReference, false));
            }             
        }
    }
}
