/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author Ralph Ruijs
 */
public class CopyClassTest extends RefactoringTestBase {

    public CopyClassTest(String name) {
        super(name);
    }

    public void test179333() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/package-info.java", "package t;"),
                                 new File("u/A.java", "package u; public class A { }"));
                                 
        performCopyClass(src.getFileObject("t/package-info.java"), new URL(src.getURL(), "u/"));
        verifyContent(src,
                      new File("t/package-info.java", "package t;"),
                      new File("u/package-info.java", "package u;"),
                      new File("u/A.java", "package u; public class A { }"));
    }

    private void performCopyClass(FileObject source, URL target, Problem... expectedProblems) throws Exception {
        final SingleCopyRefactoring[] r = new SingleCopyRefactoring[1];
        
        r[0] = new SingleCopyRefactoring(Lookups.singleton(source));
        r[0].setTarget(Lookups.singleton(target));
        r[0].setNewName(source.getNameExt());

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        addAllProblems(problems, r[0].prepare(rs));
        addAllProblems(problems, rs.doRefactoring(true));

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

}

