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
import com.sun.source.util.SourcePositions;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.gen.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.filesystems.FileUtil;

/**
 * Test different formating options
 * 
 * @author Dusan Balek
 */
public class FormatingTest extends GeneratorTest {
    
    /** Creates a new instance of FormatingTest */
    public FormatingTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FormatingTest.class);
//        suite.addTest(new FormatingTest("testClass"));
        return suite;
    }

    public void testClass() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, " ");
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final int[] counter = new int[] {0};
        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putInt("rightMargin", 30);
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                MethodTree method = maker.Method(maker.Modifiers(EnumSet.of(Modifier.PUBLIC)), "run", maker.Identifier("void"), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                List<Tree> impl = new ArrayList<Tree>();
                impl.add(maker.Identifier("Runnable"));
                impl.add(maker.Identifier("Serializable"));
                ClassTree clazz = maker.Class(maker.Modifiers(Collections.<Modifier>emptySet()), "Test" + counter[0]++, Collections.<TypeParameterTree>emptyList(), maker.Identifier("Integer"), impl, Collections.singletonList(method));
                if (counter[0] == 1)
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.CompilationUnit(maker.Identifier("hierbas.del.litoral"), Collections.<ImportTree>emptyList(), Collections.singletonList(clazz), workingCopy.getCompilationUnit().getSourceFile()));
                else
                    workingCopy.rewrite(workingCopy.getCompilationUnit(), maker.addCompUnitTypeDecl(workingCopy.getCompilationUnit(), clazz));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeClassDeclLeftBrace", true);

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("classDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentTopLevelClassMembers", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentTopLevelClassMembers", true);

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_IF_LONG.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineImplements", true);
        testSource.runModificationTask(task).commit();

        preferences.put("wrapExtendsImplementsKeyword", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        testSource.runModificationTask(task).commit();

        preferences.putBoolean("alignMultilineImplements", false);
        testSource.runModificationTask(task).commit();
        preferences.put("wrapExtendsImplementsList", CodeStyle.WrapStyle.WRAP_NEVER.name());
        preferences.putInt("rightMargin", 120);

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "class Test0 extends Integer implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test1 extends Integer implements Runnable, Serializable{\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test2 extends Integer implements Runnable, Serializable\n" +
            "{\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test3 extends Integer implements Runnable, Serializable\n" +
            "  {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "  }\n\n" +
            "class Test4 extends Integer implements Runnable, Serializable\n" +
            "    {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "    }\n\n" +
            "class Test5 extends Integer implements Runnable, Serializable {\n\n" +
            "public void run() {\n" +
            "}\n" +
            "}\n\n" +
            "class Test6 extends Integer\n" +
            "        implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test7\n" +
            "        extends Integer\n" +
            "        implements Runnable, Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test8\n" +
            "        extends Integer\n" +
            "        implements Runnable,\n" +
            "        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test9\n" +
            "        extends Integer\n" +
            "        implements Runnable,\n" +
            "                   Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test10 extends Integer implements Runnable,\n" +
            "                                        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n\n" +
            "class Test11 extends Integer implements Runnable,\n" +
            "        Serializable {\n\n" +
            "    public void run() {\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final int[] counter = new int[] {0};
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker maker = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = maker.Modifiers(Collections.<Modifier>emptySet());
                MethodTree method = maker.Method(mods, "test" + counter[0]++, maker.Identifier("int"), Collections.<TypeParameterTree>emptyList(), Collections.singletonList(maker.Variable(mods, "i", maker.Identifier("int"), null)), Collections.<ExpressionTree>emptyList(), "{return i;}", null);
                workingCopy.rewrite(clazz, maker.addClassMember(clazz, method));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeMethodDeclParen", true);
        preferences.putBoolean("spaceWithinMethodDeclParens", true);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeMethodDeclParen", false);
        preferences.putBoolean("spaceWithinMethodDeclParens", false);
        preferences.putBoolean("spaceBeforeMethodDeclLeftBrace", true);

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("methodDeclBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    int test0(int i) {\n" +
            "        return i;\n" +
            "    }\n\n" +
            "    int test1 ( int i ){\n" +
            "        return i;\n" +
            "    }\n\n" +
            "    int test2(int i)\n" +
            "    {\n" +
            "        return i;\n" +
            "    }\n\n" +
            "    int test3(int i)\n" +
            "      {\n" +
            "        return i;\n" +
            "      }\n\n" +
            "    int test4(int i)\n" +
            "        {\n" +
            "        return i;\n" +
            "        }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testFor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "for (int i = 0; i < 10; i++) System.out.println(\"TRUE\");";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        for( int i = 0; i < 10; i++ ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "        for (int i = 0; i < 10; i++)\n" +
            "            System.out.println(\"TRUE\");\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testForEach() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String[] args) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "for (String s : args) System.out.println(s);";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeForParen", false);
        preferences.putBoolean("spaceWithinForParens", true);
        preferences.putBoolean("spaceBeforeForLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeForParen", true);
        preferences.putBoolean("spaceWithinForParens", false);
        preferences.putBoolean("spaceBeforeForLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantForBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(String[] args) {\n" +
            "        for (String s : args) {\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "        for( String s : args ){\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "        for (String s : args)\n" +
            "        {\n" +
            "            System.out.println(s);\n" +
            "        }\n" +
            "        for (String s : args)\n" +
            "          {\n" +
            "            System.out.println(s);\n" +
            "          }\n" +
            "        for (String s : args)\n" +
            "            {\n" +
            "            System.out.println(s);\n" +
            "            }\n" +
            "        for (String s : args)\n" +
            "            System.out.println(s);\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testIf() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "if (a) System.out.println(\"A\") else if (b) System.out.println(\"B\") else System.out.println(\"NONE\");";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeIfParen", false);
        preferences.putBoolean("spaceWithinIfParens", true);
        preferences.putBoolean("spaceBeforeIfLeftBrace", false);
        preferences.putBoolean("spaceBeforeElse", false);
        preferences.putBoolean("spaceBeforeElseLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeIfParen", true);
        preferences.putBoolean("spaceWithinIfParens", false);
        preferences.putBoolean("spaceBeforeIfLeftBrace", true);
        preferences.putBoolean("spaceBeforeElse", true);
        preferences.putBoolean("spaceBeforeElseLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantIfBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        preferences.putBoolean("placeElseOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeElseOnNewLine", false);
        
        preferences.putBoolean("specialElseIf", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("specialElseIf", true);
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean a, boolean b) {\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else if (b) {\n" +
            "            System.out.println(\"B\");\n" +
            "        } else {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if( a ){\n" +
            "            System.out.println(\"A\");\n" +
            "        }else if( b ){\n" +
            "            System.out.println(\"B\");\n" +
            "        }else{\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if (a)\n" +
            "        {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else if (b)\n" +
            "        {\n" +
            "            System.out.println(\"B\");\n" +
            "        } else\n" +
            "        {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if (a)\n" +
            "          {\n" +
            "            System.out.println(\"A\");\n" +
            "          } else if (b)\n" +
            "          {\n" +
            "            System.out.println(\"B\");\n" +
            "          } else\n" +
            "          {\n" +
            "            System.out.println(\"NONE\");\n" +
            "          }\n" +
            "        if (a)\n" +
            "            {\n" +
            "            System.out.println(\"A\");\n" +
            "            } else if (b)\n" +
            "            {\n" +
            "            System.out.println(\"B\");\n" +
            "            } else\n" +
            "            {\n" +
            "            System.out.println(\"NONE\");\n" +
            "            }\n" +
            "        if (a)\n" +
            "            System.out.println(\"A\");\n" +
            "        else if (b)\n" +
            "            System.out.println(\"B\");\n" +
            "        else\n" +
            "            System.out.println(\"NONE\");\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        }\n" +
            "        else if (b) {\n" +
            "            System.out.println(\"B\");\n" +
            "        }\n" +
            "        else {\n" +
            "            System.out.println(\"NONE\");\n" +
            "        }\n" +
            "        if (a) {\n" +
            "            System.out.println(\"A\");\n" +
            "        } else {\n" +
            "            if (b) {\n" +
            "                System.out.println(\"B\");\n" +
            "            } else {\n" +
            "                System.out.println(\"NONE\");\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "while (b) System.out.println(\"TRUE\");";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeWhileLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "        while (b) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while( b ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while (b)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while (b)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "        while (b)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "        while (b)\n" +
            "            System.out.println(\"TRUE\");\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testSwitch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int i) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "switch (i) {case 0: System.out.println(i); break; default: System.out.println(\"DEFAULT\");}";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeSwitchParen", false);
        preferences.putBoolean("spaceWithinSwitchParens", true);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeSwitchParen", true);
        preferences.putBoolean("spaceWithinSwitchParens", false);
        preferences.putBoolean("spaceBeforeSwitchLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("indentCasesFromSwitch", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("indentCasesFromSwitch", true);

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int i) {\n" +
            "        switch (i) {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "        switch( i ){\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "        switch (i)\n" +
            "        {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "        switch (i)\n" +
            "          {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "          }\n" +
            "        switch (i)\n" +
            "            {\n" +
            "            case 0:\n" +
            "                System.out.println(i);\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"DEFAULT\");\n" +
            "            }\n" +
            "        switch (i) {\n" +
            "        case 0:\n" +
            "            System.out.println(i);\n" +
            "            break;\n" +
            "        default:\n" +
            "            System.out.println(\"DEFAULT\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testDoWhile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "do System.out.println(\"TRUE\"); while (b);\n";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeWhileParen", false);
        preferences.putBoolean("spaceWithinWhileParens", true);
        preferences.putBoolean("spaceBeforeDoLeftBrace", false);
        preferences.putBoolean("spaceBeforeWhile", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeWhileParen", true);
        preferences.putBoolean("spaceWithinWhileParens", false);
        preferences.putBoolean("spaceBeforeDoLeftBrace", true);
        preferences.putBoolean("spaceBeforeWhile", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.ELIMINATE.name());
        testSource.runModificationTask(task).commit();
        preferences.put("redundantDoWhileBraces", CodeStyle.BracesGenerationStyle.GENERATE.name());

        preferences.putBoolean("placeWhileOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeWhileOnNewLine", false);
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(boolean b) {\n" +
            "        do {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        } while (b);\n" +
            "        do{\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }while( b );\n" +
            "        do\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        } while (b);\n" +
            "        do\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          } while (b);\n" +
            "        do\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            } while (b);\n" +
            "        do\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        while (b);\n" +
            "        do {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        while (b);\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testSynchronized() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "synchronized (this) {System.out.println(\"TRUE\");}";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeSynchronizedParen", false);
        preferences.putBoolean("spaceWithinSynchronizedParens", true);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeSynchronizedParen", true);
        preferences.putBoolean("spaceWithinSynchronizedParens", false);
        preferences.putBoolean("spaceBeforeSynchronizedLeftBrace", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        synchronized (this) {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        synchronized( this ){\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        synchronized (this)\n" +
            "        {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "        }\n" +
            "        synchronized (this)\n" +
            "          {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "          }\n" +
            "        synchronized (this)\n" +
            "            {\n" +
            "            System.out.println(\"TRUE\");\n" +
            "            }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testTry() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "try {System.out.println(\"TEST\");} catch(Exception e) {System.out.println(\"CATCH\");} finally {System.out.println(\"FINALLY\");}";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceBeforeCatchParen", false);
        preferences.putBoolean("spaceWithinCatchParens", true);
        preferences.putBoolean("spaceBeforeTryLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", false);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", false);
        preferences.putBoolean("spaceBeforeCatch", false);
        preferences.putBoolean("spaceBeforeFinally", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceBeforeCatchParen", true);
        preferences.putBoolean("spaceWithinCatchParens", false);
        preferences.putBoolean("spaceBeforeTryLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatchLeftBrace", true);
        preferences.putBoolean("spaceBeforeFinallyLeftBrace", true);
        preferences.putBoolean("spaceBeforeCatch", true);
        preferences.putBoolean("spaceBeforeFinally", true);

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        testSource.runModificationTask(task).commit();

        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.NEW_LINE_INDENTED.name());
        testSource.runModificationTask(task).commit();
        preferences.put("otherBracePlacement", CodeStyle.BracePlacement.SAME_LINE.name());

        preferences.putBoolean("placeCatchOnNewLine", true);
        preferences.putBoolean("placeFinallyOnNewLine", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("placeCatchOnNewLine", false);
        preferences.putBoolean("placeFinallyOnNewLine", false);
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        } catch (Exception e) {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        } finally {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "        try{\n" +
            "            System.out.println(\"TEST\");\n" +
            "        }catch( Exception e ){\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        }finally{\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "        try\n" +
            "        {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        } catch (Exception e)\n" +
            "        {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        } finally\n" +
            "        {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "        try\n" +
            "          {\n" +
            "            System.out.println(\"TEST\");\n" +
            "          } catch (Exception e)\n" +
            "          {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "          } finally\n" +
            "          {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "          }\n" +
            "        try\n" +
            "            {\n" +
            "            System.out.println(\"TEST\");\n" +
            "            } catch (Exception e)\n" +
            "            {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "            } finally\n" +
            "            {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "            }\n" +
            "        try {\n" +
            "            System.out.println(\"TEST\");\n" +
            "        }\n" +
            "        catch (Exception e) {\n" +
            "            System.out.println(\"CATCH\");\n" +
            "        }\n" +
            "        finally {\n" +
            "            System.out.println(\"FINALLY\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testOperators() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int x, int y) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "for (int i = 0; i < x; i++) y += (y ^ 123) << 2;";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceWithinParens", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceWithinParens", false);

        preferences.putBoolean("spaceAroundUnaryOps", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundUnaryOps", false);

        preferences.putBoolean("spaceAroundBinaryOps", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundBinaryOps", true);

        preferences.putBoolean("spaceAroundAssignOps", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceAroundAssignOps", true);

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int x, int y) {\n" +
            "        for (int i = 0; i < x; i++) {\n" +
            "            y += (y ^ 123) << 2;\n" +
            "        }\n" +
            "        for (int i = 0; i < x; i++) {\n" +
            "            y += ( y ^ 123 ) << 2;\n" +
            "        }\n" +
            "        for (int i = 0; i < x; i ++ ) {\n" +
            "            y += (y ^ 123) << 2;\n" +
            "        }\n" +
            "        for (int i = 0; i<x; i++) {\n" +
            "            y += (y^123)<<2;\n" +
            "        }\n" +
            "        for (int i=0; i < x; i++) {\n" +
            "            y+=(y ^ 123) << 2;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testTypeCast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "if (cs instanceof String) {String s = (String)cs;}";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putBoolean("spaceWithinTypeCastParens", true);
        preferences.putBoolean("spaceAfterTypeCast", false);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("spaceWithinTypeCastParens", false);
        preferences.putBoolean("spaceAfterTypeCast", true);

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        if (cs instanceof String) {\n" +
            "            String s = (String) cs;\n" +
            "        }\n" +
            "        if (cs instanceof String) {\n" +
            "            String s = ( String )cs;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    public void testLabelled() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "    }\n" +
            "}\n"
            );
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        final String stmt = 
            "label: System.out.println();";
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                StatementTree statement = workingCopy.getTreeUtilities().parseStatement(stmt, new SourcePositions[1]);
                workingCopy.rewrite(block, workingCopy.getTreeMaker().addBlockStatement(block, statement));
            }            
            public void cancel() {
            }
        };
        testSource.runModificationTask(task).commit();

        Preferences preferences = FmtOptions.getPreferences(FmtOptions.getCurrentProfileId());
        preferences.putInt("labelIndent", 4);
        testSource.runModificationTask(task).commit();
        preferences.putInt("labelIndent", 0);

        preferences.putBoolean("absoluteLabelIndent", true);
        testSource.runModificationTask(task).commit();
        preferences.putBoolean("absoluteLabelIndent", false);

        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);

        String golden = 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui(CharSequence cs) {\n" +
            "        label:\n" +
            "        System.out.println();\n" +
            "        label:\n" +
            "            System.out.println();\n" +
            "label:  System.out.println();\n" +
            "    }\n" +
            "}\n";
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
