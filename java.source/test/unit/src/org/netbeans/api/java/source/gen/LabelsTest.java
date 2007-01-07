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

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import org.netbeans.junit.NbTestSuite;
import junit.textui.TestRunner;
import org.netbeans.jackpot.transform.Transformer;

/**
 *
 * @author Pavel Flaska
 */
public class LabelsTest extends GeneratorTest {
    
    /** Creates a new instance of LabelsTest */
    public LabelsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new LabelsTest("testIdentifiers"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "SetLabelTestClass.java");
        System.err.println(testFile.getAbsoluteFile().toString());
    }
    
    public void testIdentifiers() throws IOException {
        process(new LabelVisitor());
        assertFiles("testIdentifiers.pass");
    }
    
    class LabelVisitor<Void, Object> extends Transformer<Void, Object> {

        public Void visitMethod(MethodTree node, Object p) {
            System.err.println("visitMethod: " + node.getName());
            super.visitMethod(node, p);
            MethodTree copy = make.setLabel(node, node.getName() + "0");
            changes.rewrite(node, copy);
            return null;
        }

        public Void visitBreak(BreakTree node, Object p) {
            System.err.println("visitBreak: " + node.getLabel());
            super.visitBreak(node, p);
            BreakTree copy = make.setLabel(node, node.getLabel() + "0");
            changes.rewrite(node, copy);
            return null;
        }

        public Void visitContinue(ContinueTree node, Object p) {
            System.err.println("visitContinue: " + node.getLabel());
            super.visitContinue(node, p);
            ContinueTree copy = make.setLabel(node, node.getLabel() + "0");
            changes.rewrite(node, copy);
            return null;
        }

        public Void visitClass(ClassTree node, Object p) {
            System.err.println("visitClass: " + node.getSimpleName());
            super.visitClass(node, p);
            ClassTree copy = make.setLabel(node, node.getSimpleName() + "0");
            changes.rewrite(node, copy);
            return null;
        }

        public Void visitLabeledStatement(LabeledStatementTree node, Object p) {
            System.err.println("visitLabeledStatement: " + node.getLabel());
            super.visitLabeledStatement(node, p);
            LabeledStatementTree copy = make.setLabel(node, node.getLabel() + "0");
            changes.rewrite(node, copy);
            return null;
        }

        public Void visitMemberSelect(MemberSelectTree node, Object p) {
            System.err.println("visitMemberSelect: " + node.getIdentifier());
            super.visitMemberSelect(node, p);
            MemberSelectTree copy = make.setLabel(node, node.getIdentifier() + "0");
            changes.rewrite(node, copy);
            return null;
        }
        
        public Void visitIdentifier(IdentifierTree node, Object p) {
            System.err.println("visitIdentifier: " + node.getName());
            super.visitIdentifier(node, p);
            System.err.println("I: " + node);
            IdentifierTree copy = make.setLabel(node, node.getName() + "0");
            changes.rewrite(node, copy);
            return null;
        }

        public Void visitTypeParameter(TypeParameterTree node, Object p) {
            System.err.println("visitTypeParameter: " + node.getName());
            super.visitTypeParameter(node, p);
            TypeParameterTree copy = make.setLabel(node, node.getName() + "0");
            changes.rewrite(node, copy);
            return null;
        }

        public Void visitVariable(VariableTree node, Object p) {
            System.err.println("visitVariable: " + node.getName());
            super.visitVariable(node, p);
            VariableTree copy = make.setLabel(node, node.getName() + "0");
            changes.rewrite(node, copy);
            return null;
        }

    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/LabelsTest/";
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/labels/";
    }
    
}
