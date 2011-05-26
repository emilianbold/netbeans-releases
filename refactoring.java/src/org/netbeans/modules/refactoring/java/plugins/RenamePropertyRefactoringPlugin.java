/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class RenamePropertyRefactoringPlugin extends JavaRefactoringPlugin {

    private RenameRefactoring refactoring;
    private TreePathHandle property;
    private TreePathHandle getter;
    private TreePathHandle setter;

    /** Creates a new instance of RenamePropertyRefactoringPlugin */
    public RenamePropertyRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        property = rename.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase phase) {
        return JavaSource.forFileObject(property.getFileObject());
    }

    @Override
    public Problem prepare(RefactoringElementsBag reb) {
        JavaRenameProperties renameProps = refactoring.getContext().lookup(JavaRenameProperties.class);
        if (renameProps==null) {
            return null;
        }
        if (!renameProps.isIsRenameGettersSetters()) {
            return null;
        }
        initHandles();
        fireProgressListenerStart(ProgressEvent.START, 2);
        Problem p = null;
        if (getter!=null) {
            RenameRefactoring renameGetter = new RenameRefactoring(Lookups.singleton(getter));
            renameGetter.setNewName(RetoucheUtils.getGetterName(refactoring.getNewName()));
            p = renameGetter.prepare(reb.getSession());
        }
        fireProgressListenerStep();

        if (setter!=null) {
            RenameRefactoring renameSetter = new RenameRefactoring(Lookups.singleton(setter));
            renameSetter.setNewName(RetoucheUtils.getSetterName(refactoring.getNewName()));
            chainProblems(p, renameSetter.prepare(reb.getSession()));
        }

        fireProgressListenerStop();
        return p;
    }

    private static Problem chainProblems(Problem p,Problem p1) {
        Problem problem;

        if (p==null) return p1;
        if (p1==null) return p;
        problem=p;
        while(problem.getNext()!=null) {
            problem=problem.getNext();
        }
        problem.setNext(p1);
        return p;
    }

    private void initHandles() {
        try {
            getJavaSource(Phase.PREPARE).runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController p) throws Exception {
                    p.toPhase(JavaSource.Phase.RESOLVED);
                    Element propertyElement = property.resolveElement(p);
                    for (ExecutableElement el:ElementFilter.methodsIn(propertyElement.getEnclosingElement().getEnclosedElements())) {
                        if (RetoucheUtils.isGetter(el, propertyElement)) {
                            getter = TreePathHandle.create(el, p);
                        } else if (RetoucheUtils.isSetter(el, propertyElement)) {
                            setter = TreePathHandle.create(el, p);
                        } 
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}