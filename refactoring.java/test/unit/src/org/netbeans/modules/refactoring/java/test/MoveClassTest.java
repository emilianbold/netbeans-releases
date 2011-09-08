/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs
 */
public class MoveClassTest extends RefactoringTestBase {

    public MoveClassTest(String name) {
        super(name);
    }
    
    public void test185959() throws Exception { // #185959 - [Move] No warning on move-refactoring a package-private class with references [69cat]
        writeFilesAndWaitForScan(src,
                                 new File("t/package-info.java", "package t;"),
                                 new File("u/A.java", "package u; public class A { public void foo() { int d = B.c; } }"),
                                 new File("u/B.java", "package u; class B { public static int c = 5; }"));
        performCopyClass(src.getFileObject("u/B.java"), new URL(src.getURL(), "t/"), new Problem(false, "ERR_AccessesPackagePrivateFeature"));
        verifyContent(src,
                      new File("t/package-info.java", "package t;"),
                      new File("u/A.java", "package u; import t.B; public class A { public void foo() { int d = B.c; } }"),
                      new File("t/B.java", "package t; class B { public static int c = 5; }"));
        
        writeFilesAndWaitForScan(src,
                                 new File("t/package-info.java", "package t;"),
                                 new File("u/A.java", "package u; public class A { public void foo() { int d = B.c; } }"),
                                 new File("u/B.java", "package u; public class B { static int c = 5; }"));
        performCopyClass(src.getFileObject("u/B.java"), new URL(src.getURL(), "t/"), new Problem(false, "ERR_AccessesPackagePrivateFeature"));
        verifyContent(src,
                      new File("t/package-info.java", "package t;"),
                      new File("u/A.java", "package u; import t.B; public class A { public void foo() { int d = B.c; } }"),
                      new File("t/B.java", "package t; public class B { static int c = 5; }"));
        
        writeFilesAndWaitForScan(src,
                                 new File("t/package-info.java", "package t;"),
                                 new File("u/A.java", "package u; public class A { public void foo() { int d = B.c; } }"),
                                 new File("u/B.java", "package u; public class B { public static int c = 5; }"));
        performCopyClass(src.getFileObject("u/B.java"), new URL(src.getURL(), "t/"));
        verifyContent(src,
                      new File("t/package-info.java", "package t;"),
                      new File("u/A.java", "package u; import t.B; public class A { public void foo() { int d = B.c; } }"),
                      new File("t/B.java", "package t; public class B { public static int c = 5; }"));
        
        writeFilesAndWaitForScan(src,
                                 new File("t/package-info.java", "package t;"),
                                 new File("u/A.java", "package u; public class A { public void foo() { int d = B.c(); } }"),
                                 new File("u/B.java", "package u; class B { public static int c() { return 5; } }"));
        performCopyClass(src.getFileObject("u/A.java"), new URL(src.getURL(), "t/"), new Problem(false, "ERR_AccessesPackagePrivateFeature2"), new Problem(false, "ERR_AccessesPackagePrivateFeature2"));
        verifyContent(src,
                      new File("t/package-info.java", "package t;"),
                      new File("t/A.java", "package t; import u.B; public class A { public void foo() { int d = B.c(); } }"),
                      new File("u/B.java", "package u; class B { public static int c() { return 5; } }"));
        
        writeFilesAndWaitForScan(src,
                                 new File("t/package-info.java", "package t;"),
                                 new File("u/A.java", "package u; public class A { public void foo() { int d = B.c(); } }"),
                                 new File("u/B.java", "package u; public class B { static int c() { return 5 } }"));
        performCopyClass(src.getFileObject("u/A.java"), new URL(src.getURL(), "t/"), new Problem(false, "ERR_AccessesPackagePrivateFeature2"));
        verifyContent(src,
                      new File("t/package-info.java", "package t;"),
                      new File("t/A.java", "package t; import u.B; public class A { public void foo() { int d = B.c(); } }"),
                      new File("u/B.java", "package u; public class B { static int c() { return 5 } }"));
        
        writeFilesAndWaitForScan(src,
                                 new File("t/package-info.java", "package t;"),
                                 new File("u/A.java", "package u; public class A { public void foo() { int d = B.c(); } }"),
                                 new File("u/B.java", "package u; public class B { public static int c() { return 5 } }"));
        performCopyClass(src.getFileObject("u/A.java"), new URL(src.getURL(), "t/"));
        verifyContent(src,
                      new File("t/package-info.java", "package t;"),
                      new File("t/A.java", "package t; import u.B; public class A { public void foo() { int d = B.c(); } }"),
                      new File("u/B.java", "package u; public class B { public static int c() { return 5 } }"));
    }
    
    public void test121738() throws Exception { // #121738 - [Move] Fields are not accessible after move class
        writeFilesAndWaitForScan(src,
                new File("t/package-info.java", "package t;"),
                new File("u/C1.java", "package u; public class C1 { protected int p; }"),
                new File("u/C2.java", "package u; public class C2 { public void m(C1 c1) { c1.p=2; } }"));
        performCopyClass(src.getFileObject("u/C1.java"), new URL(src.getURL(), "t/"), new Problem(false, "ERR_AccessesPackagePrivateFeature"));
        verifyContent(src,
                new File("t/package-info.java", "package t;"),
                new File("t/C1.java", "package t; public class C1 { protected int p; }"),
                new File("u/C2.java", "package u; import t.C1; public class C2 { public void m(C1 c1) { c1.p=2; } }"));
    }

    private void performCopyClass(FileObject source, URL target, Problem... expectedProblems) throws Exception {
        final MoveRefactoring[] r = new MoveRefactoring[1];
        
        r[0] = new MoveRefactoring(Lookups.singleton(source));
        r[0].setTarget(Lookups.singleton(target));

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        addAllProblems(problems, r[0].prepare(rs));
        addAllProblems(problems, rs.doRefactoring(true));

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
}
