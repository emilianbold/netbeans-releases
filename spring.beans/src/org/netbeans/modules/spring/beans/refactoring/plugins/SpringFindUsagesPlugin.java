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
package org.netbeans.modules.spring.beans.refactoring.plugins;

import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.beans.refactoring.Occurrences;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactoringElement;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * @author John Baker
 */

public class SpringFindUsagesPlugin implements RefactoringPlugin {
    private static final Logger LOGGER = Logger.getLogger(SpringFindUsagesPlugin.class.getName());

    private WhereUsedQuery springBeansWhereUsed;
    private TreePathHandle treePathHandle = null;    
    
    SpringFindUsagesPlugin(WhereUsedQuery query) {
        springBeansWhereUsed = query;
    }
   
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        if (isFindReferences()) {
            treePathHandle = springBeansWhereUsed.getRefactoringSource().lookup(TreePathHandle.class);
            if (treePathHandle != null && treePathHandle.getKind() == Kind.CLASS) {
                SpringScope scope = SpringScope.getSpringScope(treePathHandle.getFileObject());
                if (scope != null) {
                    fillElementsBag(springBeansWhereUsed, treePathHandle.getFileObject(), scope, refactoringElementsBag);
                }
            }
        }
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }
   
    public void cancelRequest() {
        // no-op here
    }

    public Problem preCheck() {
        return null;
    }
    
    private void fillElementsBag(final AbstractRefactoring refactoring, final FileObject fileObject, final SpringScope scope, final RefactoringElementsBag refactoringElementsBag) {
        JavaSource source = JavaSource.forFileObject(fileObject);
        try {
            source.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController compilationController) throws Exception {
                    compilationController.toPhase(JavaSource.Phase.RESOLVED);
                    TypeElement type = (TypeElement) treePathHandle.resolveElement(compilationController);
                    if (type != null) {
                        String className = ElementUtilities.getBinaryName(type);
                        for (Occurrences.Occurrence item : Occurrences.getJavaClassOccurrences(className, scope)) {
                            refactoringElementsBag.add(refactoring, SpringRefactoringElement.create(item));
                        }
                    }                  
                }
            }, false);
        } catch (IOException exception) {
            Exceptions.printStackTrace(exception);
        }
    }
    
    private boolean isFindReferences() {
        return springBeansWhereUsed.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);          
    }   
}
