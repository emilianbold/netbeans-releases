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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.java.source.engine.ASTModel;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author Pavel Flaska
 */
public class ImportFormatTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ImportFormatTest */
    public ImportFormatTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ImportFormatTest("testFirstAddition"));
        suite.addTest(new ImportFormatTest("testAddFirstImport"));
        suite.addTest(new ImportFormatTest("testAddLastImport"));
        suite.addTest(new ImportFormatTest("testRemoveInnerImport"));
        suite.addTest(new ImportFormatTest("testRemoveFirstImport"));
        suite.addTest(new ImportFormatTest("testRemoveLastImport"));
        suite.addTest(new ImportFormatTest("testRemoveAllRemaning"));
        return suite;
    }
    
    protected void setUp() throws FileStateInvalidException, Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportClass1.java");
    }

    public void testFirstAddition() throws IOException, FileStateInvalidException {
        System.err.println("testFirstAddition");
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>();
                imports.add(make.Import(make.Identifier("java.util.List"), false));
                imports.add(make.Import(make.Identifier("java.util.Set"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testFirstAddition_ImportFormatTest.pass");
    }

    public void testAddFirstImport() throws IOException, FileStateInvalidException {
        System.err.println("testAddFirstImport");
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                imports.add(0, make.Import(make.Identifier("java.util.AbstractList"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testAddFirstImport_ImportFormatTest.pass");
    }
    

    public void testAddLastImport() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                imports.add(make.Import(make.Identifier("java.io.IOException"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testAddLastImport_ImportFormatTest.pass");
    }
    
    public void testRemoveInnerImport() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                imports.remove(1);
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testRemoveInnerImport_ImportFormatTest.pass");
    }
    
    public void testRemoveFirstImport() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                imports.remove(0);
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testRemoveFirstImport_ImportFormatTest.pass");
    }

    public void testRemoveLastImport() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                imports.remove(1);
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testRemoveLastImport_ImportFormatTest.pass");
    }
    
    public void testRemoveAllRemaning() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        Collections.EMPTY_LIST,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testRemoveAllRemaning_ImportFormatTest.pass");
    }

    
    static class T extends Transformer<Void, Object> {
            public Void visitImport(ImportTree node, Object p) {
                make.Identifier("java.util.List");
                System.err.println(getParent(node, model));
                return null;
            }
    }

    public void testAddSeveral() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = (List<ImportTree>) node.getImports();
                imports.add(make.Import(make.Identifier("java.util.List"), false));
                imports.add(make.Import(make.Identifier("java.util.Set"), false));
                imports.add(make.Import(make.Identifier("javax.swing.CellRendererPane"), false));
                imports.add(make.Import(make.Identifier("javax.swing.BorderFactory"), false));
                imports.add(make.Import(make.Identifier("javax.swing.ImageIcon"), false));
                imports.add(make.Import(make.Identifier("javax.swing.InputVerifier"), false));
                imports.add(make.Import(make.Identifier("javax.swing.GrayFilter"), false));
                imports.add(make.Import(make.Identifier("javax.swing.JFileChooser"), false));
                imports.add(make.Import(make.Identifier("javax.swing.AbstractAction"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testAddSeveral_ImportFormatTest.pass");
    }
    
    public void testRemoveInside() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                imports.remove(4);
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testRemoveInside_ImportFormatTest.pass");
    }
    
    public void testMoveFirst() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                ImportTree oneImport = imports.remove(0);
                imports.add(3, oneImport);
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testMoveFirst_ImportFormatTest.pass");
    }
    
    public void testMoveLast() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                ImportTree oneImport = imports.remove(7);
                imports.add(1, oneImport);
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testMoveLast_ImportFormatTest.pass");
    }
    
    public void testReplaceLine() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                ImportTree oneImport = imports.remove(4);
                imports.add(4, make.Import(make.Identifier("java.util.Collection"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testReplaceLine_ImportFormatTest.pass");
    }

    public void testSort() throws IOException, FileStateInvalidException {
        process(new Transformer<Void, Object>() {
            public Void visitCompilationUnit(CompilationUnitTree node, Object p) {
                List<ImportTree> imports = new ArrayList<ImportTree>(node.getImports());
                ImportTree oneImport = imports.remove(4);
                imports.add(4, make.Import(make.Identifier("java.util.Collection"), false));
                Collections.sort(imports, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        if (o1 == o2) {
                            return 0;
                        }
                        ImportTree i1 = (ImportTree) o1;
                        ImportTree i2 = (ImportTree) o2;

                        return i1.toString().compareTo(i2.toString());
                    }

                    public boolean equals(Object obj) {
                        return this == obj ? true : false;
                    }
                });
                CompilationUnitTree unit = make.CompilationUnit(
                        node.getPackageName(),
                        imports,
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
                changes.rewrite(node, unit);
                return null;
            }
        });
        assertFiles("testSort_ImportFormatTest.pass");
    }
    
    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/indent/ImportFormatTest/";
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/indent/imports/";
    }
    
    private static Tree getParent(Tree t, ASTModel model) {
        Tree root = model.getRoot();
        Tree[] tp = model.makePath(root, t);
        if (tp.length == 0)
            return null;           // tree not found
        if (tp.length == 1) {
            assert t == root;
            return t;
        }
        return tp[tp.length - 2];
    }

}