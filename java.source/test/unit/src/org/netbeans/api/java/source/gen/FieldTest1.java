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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.lang.model.type.TypeKind;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test the field generator.
 *
 * @author  Pavel Flaska
 */
public class FieldTest1 extends GeneratorTestMDRCompat {
    
    public FieldTest1(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new FieldTest1("testFieldModifiers"));
        suite.addTest(new FieldTest1("testFieldType"));
        suite.addTest(new FieldTest1("testFieldName"));
        suite.addTest(new FieldTest1("testFieldInitialValueText"));
        suite.addTest(new FieldTest1("testFieldInitialValue"));
        suite.addTest(new FieldTest1("testFieldChangeInInitValue"));
        suite.addTest(new FieldTest1("testFieldInitialValueRemoval"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest1.java");
    }

    /**
     * Removes all the modififers from the field.
     */
    public void testFieldModifiers() throws IOException {
        System.err.println("testFieldModifiers");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("modifiersField".contentEquals(node.getName())) {
                        changes.rewrite(node.getModifiers(), make.Modifiers(Collections.EMPTY_SET));
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldModifiers.pass");
    }
    
    /**
     * Changes long type of the field to short type.
     */
    public void testFieldType() throws IOException {
        System.err.println("testFieldType");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("typeField".contentEquals(node.getName())) {
                        PrimitiveTypeTree pt = make.PrimitiveType(TypeKind.SHORT);
                        VariableTree vt = make.Variable(
                                node.getModifiers(),
                                node.getName(),
                                pt,
                                node.getInitializer()
                        );
                        model.setElement(vt, model.getElement(node));
                        model.setType(vt, model.getType(node));
                        model.setPos(vt, model.getPos(node));
                        //changes.rewrite(node.getType(), tree);
                        changes.rewrite(node, vt);
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldType.pass");
    }
    
    /**
     * Changes field name to thisIsTheNewName.
     */
    public void testFieldName() throws IOException {
        System.err.println("testFieldName");
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(3);
                workingCopy.rewrite(var, make.setLabel(var, "thisIsTheNewName"));
            }
            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertFiles("testFieldName.pass");
    }
    
    /**
     * Changes the initial value text.
     */
    public void testFieldInitialValueText() throws IOException {
        System.err.println("testFieldInitialValueText");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("initialValueTextTester".contentEquals(node.getName())) {
                        changes.rewrite(node.getInitializer(), make.Literal("This is a new text."));
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldInitialValueText.pass");
    }
    
    /**
     * Change whole initial value expression to literal 78.
     */
    public void testFieldInitialValue() throws IOException {
        System.err.println("testFieldInitialValue");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("initialValueChanger".contentEquals(node.getName())) {
                        changes.rewrite(node.getInitializer(), make.Literal(Long.valueOf(78)));
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldInitialValue.pass");
    }
    
    /**
     * Changes the initialization 'new String("NetBeers")' to 'new String()'.
     * It tests only change in expression, no expression swap.
     */
    public void testFieldChangeInInitValue() throws IOException {
        System.err.println("testFieldChangeInInitValue");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("initialValueReplacer".contentEquals(node.getName())) {
                        if (Tree.Kind.NEW_CLASS.equals(node.getInitializer().getKind())) {
                            NewClassTree nct = (NewClassTree) node.getInitializer();
                            NewClassTree njuNct = make.NewClass(
                                    nct.getEnclosingExpression(), 
                                    (List<ExpressionTree>) nct.getTypeArguments(),
                                    nct.getIdentifier(),
                                    Collections.singletonList(make.Literal("NetBeans")),
                                    nct.getClassBody()
                            );
                            changes.rewrite(nct, njuNct);
                        }
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldChangeInInitValue.pass");
    }

    /**
     * Removes the inital value from the field.
     */
    public void testFieldInitialValueRemoval() throws IOException {
        System.err.println("testFieldInitialValueRemoval");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("removeInitialValueField".contentEquals(node.getName())) {
                        VariableTree vt = make.Variable(
                                node.getModifiers(), 
                                node.getName(),
                                node.getType(),
                                null);
                        changes.rewrite(node, vt);
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldInitialValueRemoval.pass");
    }
                    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/FieldTest1/FieldTest1/";
    }
}
