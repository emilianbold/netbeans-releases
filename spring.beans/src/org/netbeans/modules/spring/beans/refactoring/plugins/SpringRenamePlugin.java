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

package org.netbeans.modules.spring.beans.refactoring.plugins;

import java.io.IOException;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.beans.refactoring.Modifications;
import org.netbeans.modules.spring.beans.refactoring.Occurrences;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.netbeans.modules.spring.beans.refactoring.ModificationTransaction;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactoringElement;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings.RenamedClassName;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class SpringRenamePlugin implements RefactoringPlugin {

    private final RenameRefactoring refactoring;

    public SpringRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        FileObject fo = null;
        if (treePathHandle != null) {
            fo = treePathHandle.getFileObject();
        }
        if (fo == null) {
            fo = refactoring.getRefactoringSource().lookup(FileObject.class);
        }
        boolean recursive = true;
        if (fo == null) {
            NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            if (folder != null) {
                recursive = false;
                fo = folder.getFolder();
            }
        }
        if (fo == null) {
            return null;
        }
        SpringScope scope = SpringScope.getSpringScope(fo);
        if (scope == null) {
            return null;
        }
        try {
            if (treePathHandle != null) {
                RenamedClassName clazz = null;
                if (treePathHandle != null) {
                    JavaSource js = JavaSource.forFileObject(fo);
                    if (js != null) {
                        clazz = SpringRefactorings.getRenamedClassName(treePathHandle, js, refactoring.getNewName());
                    }
                }
                String oldBinaryName = clazz.getOldBinaryName();
                String newBinaryName = clazz.getNewBinaryName();
                if (oldBinaryName != null && newBinaryName != null) {
                    Modifications mods = new Modifications();
                    for (Occurrence occurrence : Occurrences.getJavaClassOccurrences(oldBinaryName, scope)) {
                        refactoringElements.add(refactoring, SpringRefactoringElement.createJavaElementRefModification(occurrence, mods, clazz.getOldSimpleName(), newBinaryName));
                    }
                    refactoringElements.registerTransaction(new ModificationTransaction(mods));
                }
            } else if (fo.isFolder()) {
                String oldPackageName = SpringRefactorings.getPackageName(fo);
                // If the rename is not recursive (e.g, "a.b.c" -> "x.b.c"), the new name is the whole package name.
                String newPackageName = recursive ? SpringRefactorings.getRenamedPackageName(fo, refactoring.getNewName()) : refactoring.getNewName();
                if (oldPackageName != null && newPackageName != null) {
                    Modifications mods = new Modifications();
                    for (Occurrence occurrence : Occurrences.getJavaPackageOccurrences(oldPackageName, recursive, scope)) {
                        refactoringElements.add(refactoring, SpringRefactoringElement.createJavaElementRefModification(occurrence, mods, null, newPackageName));
                    }
                    refactoringElements.registerTransaction(new ModificationTransaction(mods));
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
}
