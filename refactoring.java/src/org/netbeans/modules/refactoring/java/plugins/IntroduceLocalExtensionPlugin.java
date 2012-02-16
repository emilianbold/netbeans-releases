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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Ruijs
 */
public final class IntroduceLocalExtensionPlugin extends JavaRefactoringPlugin {
    private IntroduceLocalExtensionRefactoring refactoring;
    private TreePathHandle treePathHandle;

    public IntroduceLocalExtensionPlugin(IntroduceLocalExtensionRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }

    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        preCheckProblem = isElementAvail(treePathHandle, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        Element el = treePathHandle.resolveElement(info);
        if (!(el.getKind() == ElementKind.CLASS)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeParamsWrongType")); // NOI18N
            return preCheckProblem;
        }
        return preCheckProblem;
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        HashSet<FileObject> set = new HashSet<FileObject>();
        ClassIndex idx = cpInfo.getClassIndex();
        set.addAll(idx.getResources(treePathHandle.getElementHandle(), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        return set;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        final IntroduceLocalExtensionTransformer transformer = new IntroduceLocalExtensionTransformer(refactoring);
        TransformTask transform = new TransformTask(transformer, treePathHandle);
        Problem problem = createAndAddElements(a, transform, refactoringElements, refactoring);
        fireProgressListenerStop();
        return problem != null ? problem : transformer.getProblem();
    }
}
