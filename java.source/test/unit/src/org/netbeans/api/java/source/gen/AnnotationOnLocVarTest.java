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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.VariableTree;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author Pavel Flaska
 */
public class AnnotationOnLocVarTest extends GeneratorTest {
    
    /** Creates a new instance of AnnotationAttributeValueTest */
    public AnnotationOnLocVarTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(AnnotationOnLocVarTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "AnnOnLocalVar.java");
    }

    public void testAddAnnToLocVar() throws IOException {
        System.err.println("testAddAnnToLocVar");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree body = node.getBody();
                        List<? extends StatementTree> statements = body.getStatements();
                        VariableTree statement = (VariableTree) statements.get(1);
                        // mods will be replaced by a new one
                        ModifiersTree mods = statement.getModifiers();
                        List<AnnotationTree> anns = new ArrayList<AnnotationTree>(1);
                        List<AssignmentTree> attribs = new ArrayList<AssignmentTree>(4);
                        attribs.add(make.Assignment(make.Identifier("id"), make.Literal(Integer.valueOf(666))));
                        attribs.add(make.Assignment(make.Identifier("synopsis"), make.Literal("fat")));
                        attribs.add(make.Assignment(make.Identifier("engineer"), make.Literal("PaF")));
                        attribs.add(make.Assignment(make.Identifier("date"), make.Literal("2005")));
                        anns.add(make.Annotation(make.Identifier("AnnotationType"), attribs));
                        model.setElement(statement, model.getElement(statement));
                        model.setType(statement, model.getType(statement));
                        changes.rewrite(mods, make.Modifiers(mods.getFlags(), anns));
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddAnnToLocVar_AnnotationOnLocVarTest.pass");
    }

    public void testAddLocVarWithAnn() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testAddLocVarWithAnn");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("<init>".contentEquals(node.getName())) {
                        BlockTree bt = node.getBody();
                        List<StatementTree> statements = new ArrayList<StatementTree>(bt.getStatements());
                        statements.remove(0); // remove super(), which is in class file, but not present in source
                        List<AnnotationTree> anns = new ArrayList<AnnotationTree>(1);
                        List<AssignmentTree> attribs = new ArrayList<AssignmentTree>(4);
                        attribs.add(make.Assignment(make.Identifier("id"), make.Literal(Integer.valueOf(777))));
                        attribs.add(make.Assignment(make.Identifier("synopsis"), make.Literal("thin")));
                        attribs.add(make.Assignment(make.Identifier("engineer"), make.Literal("Snoopy")));
                        attribs.add(make.Assignment(make.Identifier("date"), make.Literal("2001")));
                        anns.add(make.Annotation(make.Identifier("AnnotationType"), attribs));
                        statements.add(0, make.Variable(
                            make.Modifiers(Collections.singleton(Modifier.FINAL), anns),
                            "testVar",
                            make.Identifier("java.util.List"),
                            make.NewClass(
                                null,
                                Collections.EMPTY_LIST,
                                make.Identifier("java.util.ArrayList"),
                                Collections.singletonList(make.Literal(Integer.valueOf(3))),
                                null
                            )
                        ));
                        BlockTree njuBlock = make.Block(statements, false);
                        model.setPos(njuBlock, model.getPos(bt));
                        changes.rewrite(bt, njuBlock);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddLocVarWithAnn_AnnotationOnLocVarTest.pass");
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/AnnotationOnLocVarTest/";
    }
}
