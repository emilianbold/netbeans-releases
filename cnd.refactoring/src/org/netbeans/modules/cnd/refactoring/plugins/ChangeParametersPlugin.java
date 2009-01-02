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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.text.MessageFormat;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.api.ChangeParametersRefactoring;
import org.netbeans.modules.cnd.refactoring.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.refactoring.api.*;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers). Based on Java refactoring
 *
 * @author  Pavel Flaska
 * @author  Tomas Hurka
 * @author  Jan Becicka
 * @author  Vladimir Voskresensky
 */
public class ChangeParametersPlugin extends CsmModificationRefactoringPlugin {

    private ChangeParametersRefactoring refactoring;
    // objects affected by refactoring
    private Collection<CsmObject> referencedObjects;

    public ChangeParametersPlugin(ChangeParametersRefactoring refactoring) {
        super(refactoring);
        this.refactoring = refactoring;
    }

    @Override
    protected Collection<CsmObject> getRefactoredObjects() {
        return referencedObjects;
    }

    @Override
    public Problem fastCheckParameters() {
        ParameterInfo paramTable[] = refactoring.getParameterInfo();
        Problem p = null;
        for (int i = 0; i < paramTable.length; i++) {
            int origIndex = paramTable[i].getOriginalIndex();

            if (origIndex == -1) {
                // check parameter name
                CharSequence s;
                s = paramTable[i].getName();
                if ((s == null || s.length() < 1)) {
                    p = createProblem(p, true, newParMessage("ERR_parname")); // NOI18N
                } else {
                    if (!Utilities.isJavaIdentifier(s.toString())) {
                        p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_InvalidIdentifier", s)); // NOI18N
                    }
                }

                // check parameter type
                CharSequence t = paramTable[i].getType();
                if (t == null) {
                    p = createProblem(p, true, newParMessage("ERR_partype")); // NOI18N
                }
                // check the default value
                s = paramTable[i].getDefaultValue();
                if ((s == null || s.length() < 1)) {
                    p = createProblem(p, true, newParMessage("ERR_pardefv")); // NOI18N
                }
            }
            ParameterInfo in = paramTable[i];

            if (in.getType() != null && in.getType().toString().endsWith("...") && i != paramTable.length - 1) {//NOI18N
                p = createProblem(p, true, org.openide.util.NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_VarargsFinalPosition", new Object[]{}));
            }
        }
        return p;
    }

    private static String newParMessage(String par) {
        return new MessageFormat(getString("ERR_newpar")).format(new Object[]{getString(par)}); // NOI18N
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPlugin.class, key);
    }

    /**
     * Returns list of problems. For the change method signature, there are two
     * possible warnings - if the method is overriden or if it overrides
     * another method.
     *
     * @return  overrides or overriden problem or both
     */
    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 4);
        // check if resolved element
        preCheckProblem = isResovledElement(getStartReferenceObject());
        fireProgressListenerStep();
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        // check if valid element
        CsmObject directReferencedObject = CsmRefactoringUtils.getReferencedElement(getStartReferenceObject());
        // support only functions and not destructor
        if (!CsmKindUtilities.isFunction(directReferencedObject) || CsmKindUtilities.isDestructor(directReferencedObject)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeParamsWrongType"));
            return preCheckProblem;
        }
        // create additional objects to resolve
        if (this.referencedObjects == null) {
            initReferencedObjects();
            fireProgressListenerStep();
        }
        // check read-only elements
        preCheckProblem = checkIfModificationPossible(preCheckProblem, directReferencedObject, getString("ERR_Overrides_Fatal"), getString("ERR_OverridesOrOverriden"));
        fireProgressListenerStop();
        return preCheckProblem;
    }

    private void initReferencedObjects() {
        CsmObject referencedObject = CsmRefactoringUtils.getReferencedElement(getStartReferenceObject());
        if (referencedObject != null) {
            this.referencedObjects = new LinkedHashSet<CsmObject>();
            if (CsmKindUtilities.isMethod(referencedObject) && !CsmKindUtilities.isConstructor(referencedObject)) {
                CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
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

    @Override
    protected void processRefactoredReferences(List<CsmReference> sortedRefs, FileObject fo, CloneableEditorSupport ces, ModificationResult mr) {
        // not yet implemented
    }

}
