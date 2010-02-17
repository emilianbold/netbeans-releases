/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;


/**
 * Rename refactoring for Java Card projects. It's involved in renaming of
 * Java classes and packages. If needed, it modifies the Java Card specific 
 * descriptors.
 */
public class JCRenameRefactoringPlugin implements RefactoringPlugin {

    /* This one is important creature - makes sure that cycles between 
     * plugins won't appear */
    private static ThreadLocal<Object> semafor = new ThreadLocal<Object>();
    private AbstractRefactoring refactoring;
    private ImportantFilesRenameRefactoring importantFilesRenameRefactoring;

    public JCRenameRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
        this.importantFilesRenameRefactoring = new ImportantFilesRenameRefactoring(refactoring);
    }

    public Problem preCheck() {
        Problem problem = null;

        if (semafor.get() == null) {
            semafor.set(new Object());
            problem = importantFilesRenameRefactoring.preCheck();
            semafor.set(null);
        }
        return problem;
    }

    public Problem checkParameters() {
        Problem problem = null;

        if (semafor.get() == null) {
            semafor.set(new Object());
            problem = importantFilesRenameRefactoring.checkParameters();
            semafor.set(null);
        }
        return problem;
    }

    public Problem fastCheckParameters() {
        Problem problem = null;

        if (semafor.get() == null) {
            semafor.set(new Object());
            problem = importantFilesRenameRefactoring.fastCheckParameters();
            semafor.set(null);
        }
        return problem;
    }

    public void cancelRequest() {
        if (semafor.get() == null) {
            semafor.set(new Object());
            importantFilesRenameRefactoring.cancelRequest();
            semafor.set(null);
        }
    }

    @SuppressWarnings("unchecked")
    public Problem prepare(RefactoringElementsBag elems) {
        Problem problem = null;

        if (semafor.get() == null) {
            problem = importantFilesRenameRefactoring.prepare(elems);
        }
        return problem;
    }

}
