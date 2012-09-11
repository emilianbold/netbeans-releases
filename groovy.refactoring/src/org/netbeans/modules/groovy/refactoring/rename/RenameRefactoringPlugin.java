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

package org.netbeans.modules.groovy.refactoring.rename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.groovy.refactoring.DiffElement;
import org.netbeans.modules.groovy.refactoring.GroovyRefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.FindUsagesElement;
import org.netbeans.modules.groovy.refactoring.findusages.FindUsagesPlugin;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;

/**
 * Implementation of the rename refactoring for groovy.
 *
 * @author Martin Janicek
 */
public class RenameRefactoringPlugin extends FindUsagesPlugin {


    public RenameRefactoringPlugin(FileObject fileObject, GroovyRefactoringElement element, RenameRefactoring renameRefactoring) {
        super(fileObject, element, renameRefactoring);
    }


    public RenameRefactoring getRefactoring() {
        return (RenameRefactoring) refactoring;
    }
    
    @Override
    protected void refactorResults(RefactoringElementsBag elementsBag, List<FindUsagesElement> usages) {
        final ModificationResult modificationResult = collectModifications(usages);

        elementsBag.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));
        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                elementsBag.add(refactoring, DiffElement.create(diff, fo, modificationResult));
            }
        }
    }

    private ModificationResult collectModifications(List<FindUsagesElement> usages) {
        final ModificationResult modificationResult = new ModificationResult();
        for (FindUsagesElement whereUsedElement : usages) {
            addModificationToResult(modificationResult, whereUsedElement);
        }
        return modificationResult;
    }

    private void addModificationToResult(ModificationResult modificationResult, FindUsagesElement whereUsedElement) {
        List<Difference> diffs = new ArrayList<Difference>();
        diffs.add(new Difference(
                Difference.Kind.CHANGE,
                whereUsedElement.getPosition().getBegin(),
                whereUsedElement.getPosition().getEnd(),
                whereUsedElement.getName(),
                getRefactoring().getNewName(),
                whereUsedElement.getDisplayText()));
        if (!diffs.isEmpty()) {
            modificationResult.addDifferences(whereUsedElement.getParentFile(), diffs);
        }

    }
}
