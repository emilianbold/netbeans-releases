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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
public class ImportAnalysisTest extends GeneratorTest {

    /** Creates a new instance of ClashingImportsTest */
    public ImportAnalysisTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ImportAnalysisTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports.java");
    }

    public void testAddImport1() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement e = elements.getTypeElement("java.util.List");
                        ExpressionTree type = make.QualIdent(e);
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", type, null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport1.pass");
    }

    public void testAddImport2() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.List");
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(types.erasure(list.asType())), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport2.pass");
    }
    
    public void testAddImport3() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.List");
                        TypeElement collection = elements.getTypeElement("java.util.Collection");
                        TypeMirror tm = types.getDeclaredType(list, types.erasure(collection.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport3.pass");
    }
    
    public void testAddImport4() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.List");
                        TypeElement collection = elements.getTypeElement("java.util.Collection");
                        TypeMirror tm = types.getDeclaredType(list, types.getWildcardType(types.erasure(collection.asType()), null));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport4.pass");
    }
    
    public void testAddImport5() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.List");
                        TypeElement collection = elements.getTypeElement("java.util.Collection");
                        TypeMirror tm = types.getDeclaredType(list, types.getWildcardType(null, types.erasure(collection.asType())));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport5.pass");
    }
    
    public void testAddImport6() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.Map.Entry");
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(types.erasure(list.asType())), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport6.pass");
    }
    
    public void testAddImport7() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.Map.Entry");
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.QualIdent(list), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport6.pass"); //the same as testAddImport6, so using only one golden file
    }
    
    public void testAddImport8() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.Map.Entry");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport8.pass");
    }
    
    public void testAddImport9() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.List");
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list1", make.QualIdent(list), null));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list2", make.QualIdent(list), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport9.pass");
    }
    
    public void testAddImport10() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.List");
                        TypeElement awtList = elements.getTypeElement("java.awt.List");
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list1", make.QualIdent(list), null));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list2", make.QualIdent(awtList), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport10.pass");
    }
    
    public void testAddImport11() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest1.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.Map.Entry");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport11.pass");
    }
    
    public void testAddImport12() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitClass(ClassTree node, Object p) {
                    TypeElement map = elements.getTypeElement("java.util.Map");
                    ClassTree nue = make.addClassImplementsClause(node, make.QualIdent(map));
                    
                    changes.rewrite(node, nue);
                    return super.visitClass(node, p);
                }
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.Map.Entry");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport12.pass");
    }
    
    public void testAddImport13() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest2.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.Map.Entry");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport13.pass");
    }
    
    public void testAddImport14() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest3.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.Map.Entry");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport14.pass");
    }
    
    public void testAddImport15() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest4.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.lang.Math");
                        ExecutableElement maxMethod = null;
                        VariableElement pi = null;
                        for (Element ee : list.getEnclosedElements()) {
                            if ("max".equals(ee.getSimpleName().toString())) {
                                maxMethod = (ExecutableElement) ee;
                            }
                            if ("PI".equals(ee.getSimpleName().toString())) {
                                pi = (VariableElement) ee;
                            }
                        }
                        
                        assertNotNull(maxMethod);
                        assertNotNull(pi);
                        
                        stats.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.QualIdent(maxMethod), Arrays.asList(make.QualIdent(pi), make.Literal(2)))));
                        
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport15.pass");
    }
    
    public void testAddImport16() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest5.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.lang.Math");
                        ExecutableElement maxMethod = null;
                        ExecutableElement minMethod = null;
                        VariableElement pi = null;
                        for (Element ee : list.getEnclosedElements()) {
                            if ("max".equals(ee.getSimpleName().toString())) {
                                maxMethod = (ExecutableElement) ee;
                            }
                            if ("min".equals(ee.getSimpleName().toString())) {
                                minMethod = (ExecutableElement) ee;
                            }
                            if ("PI".equals(ee.getSimpleName().toString())) {
                                pi = (VariableElement) ee;
                            }
                        }
                        
                        assertNotNull(maxMethod);
                        assertNotNull(minMethod);
                        assertNotNull(pi);
                        
                        stats.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.QualIdent(maxMethod), Arrays.asList(make.QualIdent(pi), make.Literal(2)))));
                        stats.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.QualIdent(minMethod), Arrays.asList(make.QualIdent(pi), make.Literal(2)))));
                        
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport16.pass");
    }
    
    public void testAddImport17() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest6.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.lang.String");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImport17.pass");
    }
    
    public void testAddImport18() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest6.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                    ExpressionTree pack = node.getPackageName();
                    PackageElement pe = elements.getPackageElement("org.netbeans.test");
                    ExpressionTree nuePack = make.QualIdent(pe);
                    
                    changes.rewrite(pack, nuePack);
                    return null;
                }
            }
        );
        assertFiles("testAddImport18.pass");
    }
    
    public void testAddImportOrder1() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest7.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("java.util.LinkedList");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImportOrder1.pass");
    }
    
    public void testAddImportSamePackage() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest7.java");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<StatementTree> stats = new ArrayList<StatementTree>();
                        for(StatementTree st : body.getStatements())
                            stats.add(st);
                        TypeElement list = elements.getTypeElement("org.netbeans.test.codegen.ImportsTest6");
                        TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                        stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.Type(tm), null));
                        changes.rewrite(body, make.Block(stats, false));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImportSamePackage.pass");
    }
    
    //XXX: test default package
    
    public void testImportAddedAfterThrows() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest7.java");
        process(
                new Transformer<Void, Object>() {
            public Void visitMethod(MethodTree node, Object p) {
                if ("<init>".contentEquals(node.getName())) {
                    TypeElement exc = elements.getTypeElement("javax.swing.text.BadLocationException");
                    MethodTree nueMethod = make.addMethodThrows(node, (ExpressionTree) make.Type(exc.asType()));
                    
                    changes.rewrite(node, nueMethod);
                }
                return null;
            }
        }
        );
        assertFiles("testImportAddedAfterThrows.pass");
    }
    
    public void testAddImportThroughMethod1() throws IOException {
        process(
            new MutableTransformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        WorkingCopy wc = getWorkingCopy();
                        int offset = (int) (wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), node) + 1);
                        TreePath context = wc.getTreeUtilities().pathFor(offset);
                        try {
                            assertEquals("List", SourceUtils.resolveImport(wc, context, "java.util.List"));
                            assertEquals("java.awt.List", SourceUtils.resolveImport(wc, context, "java.awt.List"));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImportThroughMethod1.pass");
    }
    
    public void testAddImportThroughMethod2() throws IOException {
        process(
            new MutableTransformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        WorkingCopy wc = getWorkingCopy();
                        int offset = (int) (wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), node) + 1);
                        TreePath context = wc.getTreeUtilities().pathFor(offset);
                        try {
                            assertEquals("List", SourceUtils.resolveImport(wc, context, "java.util.List"));
                            assertEquals("java.awt.List", SourceUtils.resolveImport(wc, context, "java.awt.List"));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImportThroughMethod2.pass");
    }
    
    public void testAddImportThroughMethod3() throws IOException {
        process(
            new MutableTransformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    if ("<init>".contentEquals(node.getName())) {
                        WorkingCopy wc = getWorkingCopy();
                        int offset = (int) (wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), node) + 1);
                        TreePath context = wc.getTreeUtilities().pathFor(offset);
                        try {
                            assertEquals("List", SourceUtils.resolveImport(wc, context, "java.util.List"));
                            assertEquals("Map", SourceUtils.resolveImport(wc, context, "java.util.Map"));
                            assertEquals("java.awt.List", SourceUtils.resolveImport(wc, context, "java.awt.List"));
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddImportThroughMethod3.pass");
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ImportAnalysisTest/";
    }

    void assertFiles(final String aGoldenFile) throws IOException, FileStateInvalidException {
        assertFile("File is not correctly generated.",
                getTestFile(),
                getFile(getGoldenDir(), getGoldenPckg() + aGoldenFile),
                getWorkDir(),
                new WhitespaceIgnoringDiff()
                );
    }
}
