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
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.util.*;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** Plugin that implements the core functionality of "inner to outer" refactoring.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class InnerToOuterRefactoringPlugin extends JavaRefactoringPlugin {
    /** Reference to the parent refactoring instance */
    private final InnerToOuterRefactoring refactoring;
    private TreePathHandle treePathHandle;
    
    
    /** Creates a new instance of InnerToOuterRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    InnerToOuterRefactoringPlugin(InnerToOuterRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        case PRECHECK:
            ClasspathInfo cpInfo = getClasspathInfo(refactoring);
            return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        default:
            return JavaSource.forFileObject(treePathHandle.getFileObject());
        }
    }
    
    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        // fire operation start on the registered progress listeners (4 steps)
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        Element el = treePathHandle.resolveElement(info);
        TreePathHandle sourceType = refactoring.getSourceType();
        
        // check whether the element is valid
        Problem result = isElementAvail(sourceType, info);
        if (result != null) {
            // fatal error -> don't continue with further checks
            return result;
        }
        result = JavaPluginUtils.isSourceElement(el, info);
        if (result != null) {
            return result;
        }
        
        
        //            // check whether the element is an unresolved class
        //            if (sourceType instanceof UnresolvedClass) {
        //                // fatal error -> return
        //                return new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
        //            }
        
        refactoring.setClassName(RetoucheUtils.getSimpleName(sourceType));
        
        // increase progress (step 1)
        fireProgressListenerStep();
        
        // #1 - check if the class is an inner class
        //            RefObject declCls = (RefObject) sourceType.refImmediateComposite();
        if (el instanceof TypeElement) {
            if (((TypeElement)el).getNestingKind() == NestingKind.ANONYMOUS) {
                // fatal error -> return
                preCheckProblem = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InnerToOuter_Anonymous")); // NOI18N
                return preCheckProblem;
            }
            if (!((TypeElement)el).getNestingKind().isNested()) {
                // fatal error -> return
                preCheckProblem = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InnerToOuter_MustBeInnerClass")); // NOI18N
                return preCheckProblem;
            }
        } else {
            preCheckProblem = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InnerToOuter_MustBeInnerClass")); // NOI18N
            return preCheckProblem;
        }
        
        // increase progress (step 2)
        fireProgressListenerStep();
        
        //            if (Modifier.isStatic(sourceType.getModifiers())) {
        //                fireProgressListenerStep();
        //            } else {
        //                // collect all features of the outer class
        //                Set featureSet = new HashSet(), innerFeatures = new HashSet();
        //                getFeatureSet((JavaClass) declCls, featureSet, new HashSet());
        //                getFeatureSet(sourceType, innerFeatures, new HashSet());
        //                featureSet.remove(sourceType);
        //
        //                // increase progress (step 3)
        //                fireProgressListenerStep();
        //
        //                // check if any features of the outer class are referenced
        //                // if so, create refactoring elements for them
        //                multipartIds = new ArrayList();
        //                Collection outerReferences = traverseForOuterReferences(sourceType, new ArrayList(), multipartIds, featureSet, innerFeatures, (JavaClass) declCls);
        //                if (!outerReferences.isEmpty()) {
        //                    this.outerReferences = outerReferences;
        refactoring.setReferenceName("outer"); // NOI18N
        //                }
        //            }
        
        fireProgressListenerStop();
        return preCheckProblem;
    }

    @Override
    public Problem checkParameters() {
        //TODO:
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        Problem result = null;
        
        String newName = refactoring.getClassName();
        
        if (!Utilities.isJavaIdentifier(newName)) {
            result = createProblem(result, true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InvalidIdentifier", newName)); // NOI18N
            return result;
        }
        
        FileObject primFile = refactoring.getSourceType().getFileObject();
        FileObject folder = primFile.getParent();
        FileObject[] children = folder.getChildren();
        for (FileObject child: children) {
            if (!child.isVirtual() && child.getName().equals(newName) && "java".equals(child.getExt())) { // NOI18N
                result = createProblem(result, true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_ClassClash", newName, folder.getName())); // NOI18N
                return result;
            }
        }

        return null;
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        HashSet<FileObject> set = new HashSet<FileObject>();
        ClassIndex idx = cpInfo.getClassIndex();
        set.addAll(idx.getResources(RetoucheUtils.getElementHandle(refactoring.getSourceType()), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        return set;
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        final InnerToOuterTransformer innerToOuter = new InnerToOuterTransformer(refactoring);
        TransformTask transform = new TransformTask(innerToOuter, refactoring.getSourceType());
        Problem problem = createAndAddElements(a, transform, refactoringElements, refactoring);
        fireProgressListenerStop();
        return problem != null ? problem : innerToOuter.getProblem();
    }
}
