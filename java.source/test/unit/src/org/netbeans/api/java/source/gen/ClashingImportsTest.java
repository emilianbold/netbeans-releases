/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author Dusan Balek
 */
public class ClashingImportsTest extends GeneratorTest {

    /** Creates a new instance of ClashingImportsTest */
    public ClashingImportsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ClashingImportsTest.class);
        return suite;
    }
    
    public void testAddImport() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for(StatementTree st : body.getStatements())
                    stats.add(st);
                TypeElement e = workingCopy.getElements().getTypeElement("java.awt.List");
                ExpressionTree type = make.QualIdent(e);
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "awtList", type, null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport_ClashingImports.pass");
    }

    public void testAddClashingImport() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports2.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for(StatementTree st : body.getStatements())
                    stats.add(st);
                TypeElement e = workingCopy.getElements().getTypeElement("java.util.List");
                ExpressionTree type = make.QualIdent(e);
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list", type, null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddClashingImport_ClashingImports.pass");
    }

    public void testAddClashingImport2() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports3.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for(StatementTree st : body.getStatements())
                    stats.add(st);
                TypeElement e = workingCopy.getElements().getTypeElement("java.awt.List");
                ExpressionTree type = make.QualIdent(e);
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "awtList", type, null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddClashingImport2.pass");
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ClashingImportsTest/";
    }

    @Override
    void assertFiles(final String aGoldenFile) throws IOException, FileStateInvalidException {
        assertFile("File is not correctly generated.",
                getTestFile(),
                getFile(getGoldenDir(), getGoldenPckg() + aGoldenFile),
                getWorkDir(),
                new WhitespaceIgnoringDiff()
                );
    }
    
}
