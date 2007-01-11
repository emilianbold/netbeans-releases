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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Testing method throws() section.
 *
 * @author Pavel Flaska
 */
public class MethodTest4 extends GeneratorTestMDRCompat {
    
    /** Need to be defined because of JUnit */
    public MethodTest4(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MethodTest4("testAddFirstThrows"));
        suite.addTest(new MethodTest4("testAddSecondThrows"));
        suite.addTest(new MethodTest4("testAddThirdThrows"));
        suite.addTest(new MethodTest4("testRemoveFirstThrows"));
        suite.addTest(new MethodTest4("testRemoveLastThrows"));
        suite.addTest(new MethodTest4("testRemoveAllThrows"));
/*        suite.addTest(new MethodTest4("testAnnotationAndThrows"));
        suite.addTest(new MethodTest4("testRemoveAnnAndAddThrows"));
        suite.addTest(new MethodTest4("testAddTypeParam"));*/
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodTest4.java");
    }

    public void testAddFirstThrows() throws IOException {
        System.err.println("testAddFirstThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("java.io.IOException"));
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddFirstThrows.pass");
    }

    public void testAddSecondThrows() throws IOException {
        System.err.println("testAddSecondThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("java.io.FileNotFoundException"));
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddSecondThrows.pass");
    }
    
    public void testAddThirdThrows() throws IOException {
        System.err.println("testAddThirdThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.insertMethodThrows(node, 0, make.Identifier("java.io.WriteAbortedException"));
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddThirdThrows.pass");
    }
    
    public void testRemoveFirstThrows() throws IOException {
        System.err.println("testRemoveFirstThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodThrows(node, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveFirstThrows.pass");
    }
    
    public void testRemoveLastThrows() throws IOException {
        System.err.println("testRemoveLastThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        // just to test the method
                        ExpressionTree lastThrows = node.getThrows().get(1);
                        MethodTree copy = make.removeMethodThrows(node, lastThrows);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveLastThrows.pass");
    }
    
    public void testRemoveAllThrows() throws IOException {
        System.err.println("testRemoveAllThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        // there will be nothing in throws section.
                        MethodTree copy = make.removeMethodThrows(node, 0);
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveAllThrows.pass");
    }
    
    public void testAnnotationAndThrows() throws IOException {
        System.err.println("testAnnotationAndThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        ModifiersTree newMods = make.addModifiersAnnotation(
                            node.getModifiers(), 
                            make.Annotation(
                                make.Identifier("Deprecated"),
                                Collections.<ExpressionTree>emptyList()
                            )
                        );
                        changes.rewrite(node.getModifiers(), newMods);
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("java.io.IOException"));
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAnnotationAndThrows.pass");
    }
    
    public void testRemoveAnnAndAddThrows() throws IOException {
        System.err.println("testRemoveAnnAndAddThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        ModifiersTree newMods = make.removeModifiersAnnotation(node.getModifiers(), 0);
                        changes.rewrite(node.getModifiers(), newMods);
                        MethodTree copy = make.insertMethodThrows(node, 0, make.Identifier("java.io.WriteAbortedException"));
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveAnnAndAddThrows.pass");
    }
    
    public void testAddTypeParam() throws IOException {
        System.err.println("testAddTypeParam");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodTypeParameter(
                            node, 
                            make.TypeParameter("T", Collections.<ExpressionTree>emptyList())
                        );
                        changes.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddTypeParam.pass");
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/MethodTest4/";
    }
    
}
