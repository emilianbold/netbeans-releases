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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.tools.javac.util.Context;
import org.junit.Test;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;

import javax.lang.model.type.TypeKind;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.List;
import java.util.EnumSet;
import java.util.Collections;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
public class CommentsManipulationTest extends GeneratorTest {
//    private final String TEST_CONTENT

    public CommentsManipulationTest(String aName) {
        super(aName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CommentsManipulationTest.class);
        return suite;
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    /**
     * Simulates encapsulate field refactoring with copy javadoc feature.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testEncapsulateField() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String origin =
                "public class EncapsulateField {\n" +
                        "\n" +
                        "/** Level of encapsulation */\n" +
                        "public int encapsulate = 5;" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[5];" +
                "}\n";


        JavaSource src = getJavaSource(testFile);        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {                
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                TreeMaker make = workingCopy.getTreeMaker();
                Tree setter = createSetter(make, "Encapsulate");
                ClassTree toRewrite = make.insertClassMember(clazz, clazz.getMembers().size(), setter);

                VariableTree node = (VariableTree) extractOriginalNode(cut);
                VariableTree newVar = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), node.getName(), node.getType(), node.getInitializer());

                workingCopy.rewrite(clazz, toRewrite);
                workingCopy.rewrite(node, newVar);

            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
//        assertEquals(golden, res);
    }

    private Tree createSetter(TreeMaker make, String name) {
        VariableTree parameter = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "encapsulate", make.PrimitiveType(TypeKind.INT), null);
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                "set"+name, make.PrimitiveType(TypeKind.INT),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>singletonList(parameter),
                Collections.<ExpressionTree>emptyList(),                 
                "{;}",
                make.Literal(5));
    }

    private Tree extractOriginalNode(CompilationUnitTree cut) {
        List<? extends Tree> classes = cut.getTypeDecls();
        if (!classes.isEmpty()) {
            ClassTree clazz = (ClassTree) classes.get(0);
            List<? extends Tree> trees = clazz.getMembers();
            if (trees.size() == 2) {
                VariableTree tree = (VariableTree) trees.get(1);
                return tree;
            }
        }

        throw new IllegalStateException("There is no array declaration in expected place.");

    }
    
}
