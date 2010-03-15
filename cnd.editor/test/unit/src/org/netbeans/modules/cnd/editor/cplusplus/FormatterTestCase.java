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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testReformatSimpleClass
 *
 * @author Alexander Simon
 */
public class FormatterTestCase extends EditorBase {

    public FormatterTestCase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void assertDocumentText(String msg, String expectedText) {
        super.assertDocumentText(msg, expectedText);
        reformat();
        super.assertDocumentText(msg+" (not stable)", expectedText);
    }

    // -------- Reformat tests -----------
    
    public void testReformatMultiLineSystemOutPrintln() {
        setDefaultsOptions();
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "    \"haf\");\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m()\n"
                + "{\n"
                + "    printf(\n"
                + "            \"haf\");\n"
                + "}\n"
                    );
    }

    public void testReformatMultiLineSystemOutPrintln2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "    \"haf\");\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\");\n"
                + "}\n"
                );
    }
    
    public void testReformatMultiLineSystemOutPrintln3() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        setLoadDocumentText(
                "void m() {\n"
                + "    printf(\n"
                + "    \"haf\");\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m() {\n"
                + "    printf(\n"
                + "           \"haf\");\n"
                + "}\n"
                );
    }

    public void testReformatMultiLineClassDeclaration() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
                "class C\n"
                + ": public Runnable {\n"
                + "int printf(int);\n"
                + "};\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "class C\n"
                + ": public Runnable\n"
                + "{\n"
                + "    int printf(int);\n"
                + "};\n"
                );
    }
    
    // tests for regressions
    
    /**
     * Tests reformatting of new on two lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id6065
     */
    public void testReformatNewOnTwoLines() {
        setDefaultsOptions();
        setLoadDocumentText(
                "javax::swing::JPanel* panel =\n" +
                "new java::swing::JPanel();");
        reformat();
        assertDocumentText("Incorrect new on two lines reformating",
                "javax::swing::JPanel* panel =\n" +
                "        new java::swing::JPanel();");
    }

    /**
     * Tests reformatting of ternary conditional operators on multiple lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=23508
     */
    public void testReformatTernaryConditionalOperator() {
        setDefaultsOptions();
        setLoadDocumentText(
                "void foo()\n"+
                "{\n"+
                "something = (someComplicatedExpression != null) ?\n" +
                "(aComplexCalculation) :\n" +
                "(anotherComplexCalculation);\n"+
                "}\n");
        reformat();
        assertDocumentText("Incorrect ternary conditional operator reformatting",
                "void foo()\n"+
                "{\n"+
                "    something = (someComplicatedExpression != null) ?\n" +
                "            (aComplexCalculation) :\n" +
                "            (anotherComplexCalculation);\n"+
                "}\n");
    }
    
    /**
     * Test reformatting of array initializer with newlines on
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=47069
     */
    public void testReformatArrayInitializerWithNewline() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int[] foo =  {1, 2, 3};\n" +
                "int[] foo2 =  {1,\n" +
                "2, 3};\n" +
                "int[] foo3 = {\n" +
                "1, 2, 3\n" +
                "};\n" +
                "\n");
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "int[] foo = {1, 2, 3};\n" +
                "int[] foo2 = {1,\n" +
                "    2, 3};\n" +
                "int[] foo3 = {\n" +
                "    1, 2, 3\n" +
                "};\n" +
                "\n");
    }

    /**
     * Test reformatting of array initializer with newlines on
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=47069
     */
    public void testReformatArrayInitializerWithNewline2() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int[][] foo4 =  {\n" +
                "{1, 2, 3},\n" +
                "{3,4,5},\n" +
                "{7,8,9}\n" +
                "};\n" +
                "\n");
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "int[][] foo4 = {\n" +
                "    {1, 2, 3},\n" +
                "    {3, 4, 5},\n" +
                "    {7, 8, 9}\n" +
                "};\n" +
                "\n");
    }
    
    /**
     * Test reformatting of newline braces to normal ones
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=48926
     */
    public void testReformatNewlineBracesToNormalOnes() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineCatch, true);
        setLoadDocumentText(
                "try\n" +
                "{\n" +
                "printf(\"test\");\n" +
                "}\n" +
                "catch (Exception e)\n" +
                "{\n" +
                "printf(\"exception\");\n" +
                "}");
        reformat();
        assertDocumentText("Incorrect try-catch reformatting",
                "try {\n" +
                "    printf(\"test\");\n" +
                "}\n"+
                "catch (Exception e) {\n" +
                "    printf(\"exception\");\n" +
                "}");
    }

    public void testReformatNewlineBracesToNormalOnes1() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineCatch, true);
        setLoadDocumentText(
                "try\n" +
                "{\n" +
                "printf(\"test\");\n" +
                "}\n" +
                "catch (Exception e)\n" +
                "{\n" +
                "printf(\"exception\");\n" +
                "}");
        reformat();
        assertDocumentText("Incorrect try-catch reformatting",
                "try {\n" +
                "    printf(\"test\");\n" +
                "}\n"+
                "catch (Exception e) {\n" +
                "    printf(\"exception\");\n" +
                "}");
    }
    
    public void testReformatNewlineBracesToNormalOnes2() {
        setDefaultsOptions();
        setLoadDocumentText(
                "	void testError(CuTest *tc){\n" +
                "		IndexReader* reader = NULL;\n" +
                "		try{\n" +
                "			RAMDirectory dir;\n" +
                "		}catch(CLuceneError& a){\n" +
                "			_CLDELETE(reader);\n" +
                "		}catch(...){\n" +
                "			CuAssert(tc,_T(\"Error did not catch properly\"),false);\n" +
                "		}\n" +
                "	}\n" +
                "\n");
        reformat();
        assertDocumentText("Incorrect try-catch reformatting",
                "void testError(CuTest *tc)\n" +
                "{\n" +
                "    IndexReader* reader = NULL;\n" +
                "    try {\n" +
                "        RAMDirectory dir;\n" +
                "    } catch (CLuceneError& a) {\n" +
                "        _CLDELETE(reader);\n" +
                "    } catch (...) {\n" +
                "        CuAssert(tc, _T(\"Error did not catch properly\"), false);\n" +
                "    }\n" +
                "}\n" +
                "\n");
    }

    public void testReformatNewlineBracesToNormalOnes3() {
        setDefaultsOptions();
            setDefaultsOptions();
        setLoadDocumentText(
                "try {\n" +
                "    printf(\"test\");\n" +
                "}\n" +
                "catch ( IllegalStateException illegalStateException  ) {\n" +
                "    illegalStateException.printStackTrace();\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect tabbed catch reformatting",
                "try {\n" +
                "    printf(\"test\");\n" +
                "} catch (IllegalStateException illegalStateException) {\n" +
                "    illegalStateException.printStackTrace();\n" +
                "}\n");
    }

    
    /**
     * Test reformatting of multiline constructors
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=49450
     */
    public void testReformatMultilineConstructor() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
                "class Test {\n" +
                "Test(int one,\n" +
                "int two,\n" +
                "int three,\n" +
                "int four) {\n" +
                "this.one = one;\n" +
                "}\n" +
                "};");
        reformat();
        assertDocumentText("Incorrect multiline constructor reformatting",
                "class Test {\n" +
                "    Test(int one,\n" +
                "            int two,\n" +
                "            int three,\n" +
                "            int four) {\n" +
                "        this.one = one;\n" +
                "    }\n" +
                "};");
    }

    /**
     * Test reformatting of multiline constructors
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=49450
     */
    public void testReformatMultilineConstructor2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
                "class Test {\n" +
                "Test(int one,\n" +
                "int two,\n" +
                "int three,\n" +
                "int four) {\n" +
                "this.one = one;\n" +
                "}\n" +
                "};");
        reformat();
        assertDocumentText("Incorrect multiline constructor reformatting",
                "class Test\n" +
                "{\n" +
                "    Test(int one,\n" +
                "            int two,\n" +
                "            int three,\n" +
                "            int four)\n" +
                "    {\n" +
                "        this.one = one;\n" +
                "    }\n" +
                "};");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets() {
        setDefaultsOptions();
        setLoadDocumentText(
                "if (count == 0)\n" +
                "return 0.0f;\n" +
                "else\n" +
                "return performanceSum / getCount();\n");
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (count == 0)\n" +
                "    return 0.0f;\n" +
                "else\n" +
                "    return performanceSum / getCount();\n");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets2() {
        setDefaultsOptions();
        setLoadDocumentText(
                "if (count == 0)\n" +
                "return 0.0f;\n" +
                "else  {\n" +
                "return performanceSum / getCount();\n"+
                "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (count == 0)\n" +
                "    return 0.0f;\n" +
                "else {\n" +
                "    return performanceSum / getCount();\n" +
                "}\n");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets3() {
        setDefaultsOptions();
        setLoadDocumentText(
                "if (true) if (true) if (true)\n" +
                "else return;\n" +
                "else return;\n" +
                "else return;\n");
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (true) if (true) if (true)\n" +
                "        else return;\n" +
                "    else return;\n" +
                "else return;\n");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets4() {
        setDefaultsOptions();
        setLoadDocumentText(
                "if (true)\n" +
                "    if (true)\n" +
                "    if (true)\n" +
                "else return;\n" +
                "else return;\n" +
                "else return;\n");
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (true)\n" +
                "    if (true)\n" +
                "        if (true)\n" +
                "        else return;\n" +
                "    else return;\n" +
                "else return;\n");
    }
    
    /**
     * Test reformatting of class
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=97544
     */
    public void testReformatSimpleClass() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "class C {\n" +
            "protected:\n" +
            "int i;\n" +
            "int foo();\n" +
            "private:\n" +
            "int j;\n" +
            "public:\n" +
            "int k;\n" +
            "};\n");
        reformat();
        assertDocumentText("Incorrect reformatting of simple class",
            "class C {\n" +
            "protected:\n" +
            "    int i;\n" +
            "    int foo();\n" +
            "private:\n" +
            "    int j;\n" +
            "public:\n" +
            "    int k;\n" +
            "};\n");
    }
    
    /**
     * Test reformatting of class
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=97544
     */
    public void testReformatSimpleClass2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "class C {\n" +
            "protected:\n" +
            "int i;\n" +
            "int foo();\n" +
            "private:\n" +
            "int j;\n" +
            "public:\n" +
            "int k;\n" +
            "};\n");
        reformat();
        assertDocumentText("Incorrect reformatting of simple class",
            "class C\n" +
            "{\n" +
            "protected:\n" +
            "    int i;\n" +
            "    int foo();\n" +
            "private:\n" +
            "    int j;\n" +
            "public:\n" +
            "    int k;\n" +
            "};\n");
    }

    /**
     * Test reformatting of For without braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=98475
     */
    public void testReformatForWithoutBraces() {
        setDefaultsOptions();
        setLoadDocumentText(
            "for (i = 0; i < MAXBUCKET; i++) {\n" +
	    "for (j = 0; j < MAXBUCKET; j++)\n" +
            "if (i != j) {\n" +
            "if (isempty()) {\n" +
            "pour(current, i, j);\n" +
            "insCnf(current);\n" +
            "}\n" +
            "}\n" +
            "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting of For without braces",
            "for (i = 0; i < MAXBUCKET; i++) {\n" +
	    "    for (j = 0; j < MAXBUCKET; j++)\n" +
            "        if (i != j) {\n" +
            "            if (isempty()) {\n" +
            "                pour(current, i, j);\n" +
            "                insCnf(current);\n" +
            "            }\n" +
            "        }\n" +
            "}\n");
    }

    /**
     * Test reformatting for preprocessors directives
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=100665
     */
    public void testReformatPreprocessorsDirectives() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
            "main() {\n" +
            "#define AAA 1\n" +
            "int aaa;\n" +
            "#define BBB 2\n" +
            "long bbb;\n" +
            "int ccc;\n" +
            "int ddd;\n" +
            "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting for preprocessors directives",
            "main() {\n" +
            "#define AAA 1\n" +
            "    int aaa;\n" +
            "#define BBB 2\n" +
            "    long bbb;\n" +
            "    int ccc;\n" +
            "    int ddd;\n" +
            "}\n");
    }

    /**
     * Test reformatting for preprocessors directives
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=100665
     */
    public void testReformatPreprocessorsDirectives2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "main() {\n" +
            "#define AAA 1\n" +
            "int aaa;\n" +
            "#define BBB 2\n" +
            "long bbb;\n" +
            "int ccc;\n" +
            "int ddd;\n" +
            "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting for preprocessors directives",
            "main()\n" +
            "{\n" +
            "#define AAA 1\n" +
            "    int aaa;\n" +
            "#define BBB 2\n" +
            "    long bbb;\n" +
            "    int ccc;\n" +
            "    int ddd;\n" +
            "}\n");
    }

    /**
     * Test reformatting of function arguments list
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=115628
     */
    public void testReformatFunctionArguments() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        setLoadDocumentText(
            "int foo(int z){\n" +
            "z += myfoo(a,\n" +
            "b,\n" +
            "c);\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect reformatting of function arguments list",
            "int foo(int z)\n" +
            "{\n" +
            "    z += myfoo(a,\n" +
            "               b,\n" +
            "               c);\n" +
            "}\n");
    }
    
    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatConstructorInitializer() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units) {\n" +
            "ComputeSupportMetric();\n" +
            "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting of constructor initializer",
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units) {\n" +
            "    ComputeSupportMetric();\n" +
            "}\n");
    }
    
    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatConstructorInitializer2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units) {\n" +
            "ComputeSupportMetric();\n" +
            "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting of constructor initializer",
            "Cpu::Cpu(int type, int architecture, int units) :\n" +
            "Module(\"CPU\", \"generic\", type, architecture, units)\n" +
            "{\n" +
            "    ComputeSupportMetric();\n" +
            "}\n");
    }

    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatMultilineMainDefinition() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
            "int\n" +
            "main(int argc, char** argv) {\n" +
            "return (EXIT_SUCCESS);\n" +
            "};\n");
        reformat();
        assertDocumentText("Incorrect reformatting of multi line main definition",
            "int\n" +
            "main(int argc, char** argv) {\n" +
            "    return (EXIT_SUCCESS);\n" +
            "};\n");
    }

    /**
     * Test reformatting of constructor initializer
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91173
     */
    public void testReformatMultilineMainDefinition2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int\n" +
            "main(int argc, char** argv) {\n" +
            "return (EXIT_SUCCESS);\n" +
            "};\n");
        reformat();
        assertDocumentText("Incorrect reformatting of multi line main definition",
            "int\n" +
            "main(int argc, char** argv)\n" +
            "{\n" +
            "    return (EXIT_SUCCESS);\n" +
            "};\n");
    }

    /**
     * Test reformatting of unbalanced braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
     */
    public void testReformatUnbalancedBraces() {
        setDefaultsOptions();
        setLoadDocumentText(
            "void foo() {\n" +
            "#if A\n" +
            "if (0) {\n" +
            "#else\n" +
            "if (1) {\n" +
            "#endif\n" +
            "}\n" +
            "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting of unbalanced braces",
            "void foo()\n" +
            "{\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "    }\n" +
            "}\n");
    }

    public void testIdentInnerEnum() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeClass, 1);
        setLoadDocumentText(
            "class NdbTransaction {\n" +
            "#ifndef D\n" +
            "friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "enum AbortOption {\n" +
            "#ifndef D\n" +
            "AbortOnError=::AbortOnError,\n" +
            "#endif\n" +
            "AO_IgnoreError=::AO_IgnoreError,\n" +
            "AO_SkipError\n" +
            "};\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("Incorrect identing of inner enum",
            "class NdbTransaction {\n" +
            "#ifndef D\n" +
            "    friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    enum AbortOption {\n" +
            "#ifndef D\n" +
            "        AbortOnError = ::AbortOnError,\n" +
            "#endif\n" +
            "        AO_IgnoreError = ::AO_IgnoreError,\n" +
            "        AO_SkipError\n" +
            "    };\n" +
            "};\n"
        );
    }

    public void testIdentInnerEnum2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeClass, 0);
        setLoadDocumentText(
            "class NdbTransaction {\n" +
            "#ifndef D\n" +
            "friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "enum AbortOption {\n" +
            "#ifndef D\n" +
            "AbortOnError=::AbortOnError,\n" +
            "#endif\n" +
            "AO_IgnoreError=::AO_IgnoreError,\n" +
            "AO_SkipError\n" +
            "};\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("Incorrect identing of inner enum",
            "class NdbTransaction\n" +
            "{\n" +
            "#ifndef D\n" +
            "    friend class Ndb;\n" +
            "#endif\n" +
            "\n" +
            "public:\n" +
            "    enum AbortOption\n" +
            "    {\n" +
            "#ifndef D\n" +
            "        AbortOnError = ::AbortOnError,\n" +
            "#endif\n" +
            "        AO_IgnoreError = ::AO_IgnoreError,\n" +
            "        AO_SkipError\n" +
            "    };\n" +
            "};\n"
        );
    }

    public void testTemplate() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U> {\n" +
            "public:\n" +
            "KeyTable2(ArrayPool<U>& pool) :\n" +
            "DLHashTable2<T, U>(pool) {\n" +
            "}\n" +
            "\n" +
            "bool find(Ptr<T>& ptr, const T& rec) const {\n" +
            "return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "}\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("Incorrect identing of template class",
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U> {\n" +
            "public:\n" +
            "    KeyTable2(ArrayPool<U>& pool) :\n" +
            "    DLHashTable2<T, U>(pool) {\n" +
            "    }\n" +
            "    bool find(Ptr<T>& ptr, const T& rec) const {\n" +
            "        return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "    }\n" +
            "};\n"
            );
    }

    public void testTemplate2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 1);
        setLoadDocumentText(
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U> {\n" +
            "public:\n" +
            "KeyTable2(ArrayPool<U>& pool) :\n" +
            "DLHashTable2<T, U>(pool) {\n" +
            "}\n" +
            "\n" +
            "bool find(Ptr<T>& ptr, const T& rec) const {\n" +
            "return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "}\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("Incorrect identing of template class",
            "template <class T, class U>\n" +
            "class KeyTable2 : public DLHashTable2<T, U>\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    KeyTable2(ArrayPool<U>& pool) :\n" +
            "    DLHashTable2<T, U>(pool)\n" +
            "    {\n" +
            "    }\n" +
            "\n" +
            "    bool find(Ptr<T>& ptr, const T& rec) const\n" +
            "    {\n" +
            "        return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "    }\n" +
            "};\n"
            );
    }
    
    public void testIdentPreprocessorElase() {
        setDefaultsOptions();
        setLoadDocumentText(
            "#if defined(USE_MB)\n" +
            "if (use_mb(cs)) {\n" +
            "result_state = IDENT_QUOTED;\n" +
            "}\n" +
            "#endif\n" +
            "{\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing of preprocessor else",
            "#if defined(USE_MB)\n" +
            "if (use_mb(cs)) {\n" +
            "    result_state = IDENT_QUOTED;\n" +
            "}\n" +
            "#endif\n" +
            "{\n" +
            "}\n"
        );
    }
    
    public void testIdentDefine() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "int\n" +
            "main() {\n" +
            "int z;\n" +
            "#define X \\\n" +
            "        a+\\\n" +
            "        b+ \\\n" +
            "        c \n" +
            "z++;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing of preprocessor else",
            "int\n" +
            "main()\n" +
            "{\n" +
            "    int z;\n" +
            "#define X \\\n" +
            "        a+\\\n" +
            "        b+ \\\n" +
            "        c \n" +
            "    z++;\n" +
            "}\n"
        );
    }

    public void testIdentMultyLineMain() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "long z;\n" +
            "int\n" +
            "main() {\n" +
            "short a;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline main",
            "long z;\n" +
            "int\n" +
            "main() {\n" +
            "    short a;\n" +
            "}\n"
        );
    }

    public void testIdentMultyLineMain2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "long z;\n" +
            "int\n" +
            "main() {\n" +
            "short a;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline main",
            "long z;\n" +
            "int\n" +
            "main()\n" +
            "{\n" +
            "    short a;\n" +
            "}\n"
        );
    }
    
    public void testIdentMultyConstructor() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
            "Log_event::Log_event(uint flags_arg, bool using_trans)\n" +
            "        :log_pos(0), temp_buf(0), exec_time(0), flags(flags_arg), thd(thd_arg)\n" +
            "        {\n" +
            "                server_id=thd->server_id;\n" +
            "        }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Log_event::Log_event(uint flags_arg, bool using_trans)\n" +
            ": log_pos(0), temp_buf(0), exec_time(0), flags(flags_arg), thd(thd_arg) {\n" +
            "    server_id = thd->server_id;\n" +
            "}\n"
        );
    }

    public void testIdentMultyConstructor2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceAfterColon, false);
        setLoadDocumentText(
            "Log_event::Log_event(const char* buf,\n" +
            "        const Format_description_log_event* description_event)\n" +
            "        :temp_buf(0), cache_stmt(0)\n" +
            "        {\n" +
            "                server_id=thd->server_id;\n" +
            "        }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Log_event::Log_event(const char* buf,\n" +
            "        const Format_description_log_event* description_event)\n" +
            ":temp_buf(0), cache_stmt(0)\n" +
            "{\n" +
            "    server_id = thd->server_id;\n" +
            "}\n"
        );
    }

    public void testIdentMultyConstructor3() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceAfterColon, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        setLoadDocumentText(
            "Log_event::Log_event(const char* buf,\n" +
            "        const Format_description_log_event* description_event)\n" +
            "        :temp_buf(0), cache_stmt(0)\n" +
            "        {\n" +
            "                server_id=thd->server_id;\n" +
            "        }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Log_event::Log_event(const char* buf,\n" +
            "                     const Format_description_log_event* description_event)\n" +
            ":temp_buf(0), cache_stmt(0)\n" +
            "{\n" +
            "    server_id = thd->server_id;\n" +
            "}\n"
        );
    }

    public void testIdentMultyConstructor4() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "public:\n" +
            "class IndexReaderCommitLockWith : \n" +
            "public CL_NS(store)::LuceneLockWith\n" +
            "{\n" +
            "private:\n" +
            "IndexReader* reader;\n" +
            "};\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    class IndexReaderCommitLockWith :\n" +
            "    public CL_NS(store)::LuceneLockWith\n" +
            "    {\n" +
            "    private:\n" +
            "        IndexReader* reader;\n" +
            "    };\n" +
            "};\n"
        );
    }
    

    public void testIdentDefineBrace() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "#define BRACE {\n" +
            "int main() {\n" +
            "if (a) {\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing define brace",
            "#define BRACE {\n" +
            "int main()\n" +
            "{\n" +
            "    if (a) {\n" +
            "    }\n" +
            "}\n"
        );
    }
    
    public void testIdentDefineBrace2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "#define BRACE }\n" +
            "int main() {\n" +
            "if (a) {\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing define brace",
            "#define BRACE }\n" +
            "int main()\n" +
            "{\n" +
            "    if (a) {\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testMacroDefineWithBrace() {
        setDefaultsOptions();
        setLoadDocumentText(
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
            );
        reformat();
            assertDocumentText("Incorrect formatting for macro define with brace",
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
        );
    }

    public void testMacroDefineWithBrace1() {
        setDefaultsOptions();
        setLoadDocumentText(
            "\n"+
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
            );
        reformat();
            assertDocumentText("Incorrect formatting for macro define with brace",
            "\n"+
            "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n"
        );
    };
    
    public void testMacroDefineWithBrace2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        setLoadDocumentText(
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with brace",
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
    }

    public void testMacroDefineWithBrace3() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        setLoadDocumentText(
                "\n"+
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with brace",
                "\n"+
                "#define SOME_IF(a, b) if ((a) > (b)) { /* do something */ }\n");
    }

    public void testMacroDefineWithParen() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "M(\"test\");\n" +
                "return 0;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testMacroDefineWithParen11() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "M(\"test\");\n" +
                "return 0;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main()\n" +
                "{\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testMacroDefineWithParen2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceBeforeMethodCallParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceBeforeMethodDeclParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main () {\n" +
                "    M (\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testMacroDefineWithParen21() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceBeforeMethodCallParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main() {\n" +
                "    M(\"test\");\n" +
                "    return 0;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "#include <stdio.h>\n" +
                "#define M(x) puts(#x)\n" +
                "int main()\n" +
                "{\n" +
                "    M (\"test\");\n" +
                "    return 0;\n" +
                "}\n");
    }

    public void testSwitchFormatting() {
        setDefaultsOptions();
        setLoadDocumentText(
                "switch (GetTypeID()) {\n" +
                "case FAST:\n" +
                "metric += 100;\n" +
                "break;\n" +
                "case ULTRA:\n" +
                "case SLOW:\n" +
                "metric += 200;\n" +
                "break;\n" +
                "default:\n" +
                "break;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "switch (GetTypeID()) {\n" +
                "    case FAST:\n" +
                "        metric += 100;\n" +
                "        break;\n" +
                "    case ULTRA:\n" +
                "    case SLOW:\n" +
                "        metric += 200;\n" +
                "        break;\n" +
                "    default:\n" +
                "        break;\n" +
                "}\n");
    }

    public void testSwitchFormatting2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        setLoadDocumentText(
                "switch (GetTypeID()) {\n" +
                "case FAST:\n" +
                "metric += 100;\n" +
                "break;\n" +
                "case ULTRA:\n" +
                "case SLOW:\n" +
                "metric += 200;\n" +
                "break;\n" +
                "default:\n" +
                "break;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "switch (GetTypeID()) {\n" +
                "case FAST:\n" +
                "    metric += 100;\n" +
                "    break;\n" +
                "case ULTRA:\n" +
                "case SLOW:\n" +
                "    metric += 200;\n" +
                "    break;\n" +
                "default:\n" +
                "    break;\n" +
                "}\n");
    }

    public void testSwitchFormatting3() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 4:\n" +
                "            if (true)return;\n" +
                "            else {\n" +
                "                break;\n" +
                "            }\n" +
                "            break;\n" +
                "        case 14:\n" +
                "        {\n" +
                "            i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "            return;\n" +
                "        default:\n" +
                "            break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "            case 1:\n" +
                "                return 1;\n" +
                "            case 2:\n" +
                "                break;\n" +
                "            case 4:\n" +
                "                i++;\n" +
                "            case 6:\n" +
                "                switch (i * 2) {\n" +
                "                    case 10:\n" +
                "                        if (true)return;\n" +
                "                        else {\n" +
                "                            break;\n" +
                "                        }\n" +
                "                    case 12:\n" +
                "                    {\n" +
                "                        break;\n" +
                "                    }\n" +
                "                }\n" +
                "            default:\n" +
                "                break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting3Half() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i)\n" +
                "      {\n" +
                "        case 1:\n" +
                "          return 1;\n" +
                "        case 4:\n" +
                "          if (true)return;\n" +
                "          else\n" +
                "            {\n" +
                "              break;\n" +
                "            }\n" +
                "          break;\n" +
                "        case 14:\n" +
                "          {\n" +
                "            i++;\n" +
                "          }\n" +
                "        case 6:\n" +
                "          return;\n" +
                "        default:\n" +
                "          break;\n" +
                "      }\n" +
                "    if (i != 8)\n" +
                "      switch (i)\n" +
                "        {\n" +
                "          case 1:\n" +
                "            return 1;\n" +
                "          case 2:\n" +
                "            break;\n" +
                "          case 4:\n" +
                "            i++;\n" +
                "          case 6:\n" +
                "            switch (i * 2)\n" +
                "              {\n" +
                "                case 10:\n" +
                "                  if (true)return;\n" +
                "                  else\n" +
                "                    {\n" +
                "                      break;\n" +
                "                    }\n" +
                "                case 12:\n" +
                "                  {\n" +
                "                    break;\n" +
                "                  }\n" +
                "              }\n" +
                "          default:\n" +
                "            break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting3HalfSQL() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 4:\n" +
                "            if (true)return;\n" +
                "            else\n" +
                "              {\n" +
                "                break;\n" +
                "              }\n" +
                "            break;\n" +
                "        case 14:\n" +
                "        {\n" +
                "            i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "            return;\n" +
                "        default:\n" +
                "            break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "      switch (i) {\n" +
                "          case 1:\n" +
                "              return 1;\n" +
                "          case 2:\n" +
                "              break;\n" +
                "          case 4:\n" +
                "              i++;\n" +
                "          case 6:\n" +
                "              switch (i * 2) {\n" +
                "                  case 10:\n" +
                "                      if (true)return;\n" +
                "                      else\n" +
                "                        {\n" +
                "                          break;\n" +
                "                        }\n" +
                "                  case 12:\n" +
                "                  {\n" +
                "                      break;\n" +
                "                  }\n" +
                "              }\n" +
                "          default:\n" +
                "              break;\n" +
                "      }\n" +
                "}\n");
    }

    public void testSwitchFormatting3SQL() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "    case 1:\n" +
                "        return 1;\n" +
                "    case 4:\n" +
                "        if (true)return;\n" +
                "        else\n" +
                "        {\n" +
                "            break;\n" +
                "        }\n" +
                "        break;\n" +
                "    case 14:\n" +
                "    {\n" +
                "        i++;\n" +
                "    }\n" +
                "    case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 2:\n" +
                "            break;\n" +
                "        case 4:\n" +
                "            i++;\n" +
                "        case 6:\n" +
                "            switch (i * 2) {\n" +
                "            case 10:\n" +
                "                if (true)return;\n" +
                "                else\n" +
                "                {\n" +
                "                    break;\n" +
                "                }\n" +
                "            case 12:\n" +
                "            {\n" +
                "                break;\n" +
                "            }\n" +
                "            }\n" +
                "        default:\n" +
                "            break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting4() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "        i++;\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   return;\n" +
                "       case 12:\n" +
                "                break;\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "    case 1:\n" +
                "        return 1;\n" +
                "    case 4:\n" +
                "        i++;\n" +
                "    case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "            return 1;\n" +
                "        case 2:\n" +
                "            break;\n" +
                "        case 4:\n" +
                "            i++;\n" +
                "        case 6:\n" +
                "            switch (i * 2) {\n" +
                "            case 10:\n" +
                "                return;\n" +
                "            case 12:\n" +
                "                break;\n" +
                "            }\n" +
                "        default:\n" +
                "            break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testSwitchFormatting4Half() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "    switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 4 :\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "        break;\n" +
                "        case 14 :\n" +
                "        {\n" +
                "        i++;\n" +
                "        }\n" +
                "        case 6:\n" +
                "        return;\n" +
                "    default:\n" +
                "        break;\n" +
                "    }\n" +
                "    if (i != 8)\n" +
                "        switch (i) {\n" +
                "        case 1:\n" +
                "        return 1;\n" +
                "        case 2:\n" +
                "        break;\n" +
                "        case 4 :\n" +
                "                i++;\n" +
                "           case 6:\n" +
                "               switch (i * 2) {\n" +
                "            case 10:\n" +
                "                   if (true)return;\n" +
                "                   else {break;}\n" +
                "       case 12:\n" +
                "                {\n" +
                "                break;\n" +
                "                }\n" +
                "        }\n" +
                "     default :\n" +
                "            break;\n" +
                "     }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting for macro define with paren",
                "int main(int i)\n" +
                "{\n" +
                "    switch (i)\n" +
                "      {\n" +
                "      case 1:\n" +
                "        return 1;\n" +
                "      case 4:\n" +
                "        if (true)return;\n" +
                "        else\n" +
                "          {\n" +
                "            break;\n" +
                "          }\n" +
                "        break;\n" +
                "      case 14:\n" +
                "        {\n" +
                "          i++;\n" +
                "        }\n" +
                "      case 6:\n" +
                "        return;\n" +
                "      default:\n" +
                "        break;\n" +
                "      }\n" +
                "    if (i != 8)\n" +
                "      switch (i)\n" +
                "        {\n" +
                "        case 1:\n" +
                "          return 1;\n" +
                "        case 2:\n" +
                "          break;\n" +
                "        case 4:\n" +
                "          i++;\n" +
                "        case 6:\n" +
                "          switch (i * 2)\n" +
                "            {\n" +
                "            case 10:\n" +
                "              if (true)return;\n" +
                "              else\n" +
                "                {\n" +
                "                  break;\n" +
                "                }\n" +
                "            case 12:\n" +
                "              {\n" +
                "                break;\n" +
                "              }\n" +
                "            }\n" +
                "        default:\n" +
                "          break;\n" +
                "        }\n" +
                "}\n");
    }

    public void testDoxyGenIdent() {
        setDefaultsOptions();
        setLoadDocumentText(
            "        /**\n" +
            "         * Class for accessing a compound stream.\n" +
            "         *\n" +
            "         * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            "         */\n" +
            "        class CompoundFileReader: public CL_NS(store)::Directory {\n" +
            "        }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing doc comment",
            "/**\n" +
            " * Class for accessing a compound stream.\n" +
            " *\n" +
            " * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            " */\n" +
            "class CompoundFileReader : public CL_NS(store)::Directory\n" +
            "{\n" +
            "}\n"
        );
    }

    public void testBlockCommentIdent() {
        setDefaultsOptions();
        setLoadDocumentText(
            "        /*\n" +
            "         * Class for accessing a compound stream.\n" +
            "         *\n" +
            "         * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            "         */\n" +
            "        class CompoundFileReader: public CL_NS(store)::Directory {\n" +
            "        }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing block comment",
            "/*\n" +
            " * Class for accessing a compound stream.\n" +
            " *\n" +
            " * @version $Id: CompoundFile.h,v 1.1.2.12 2005/11/02 12:44:22 ustramooner Exp $\n" +
            " */\n" +
            "class CompoundFileReader : public CL_NS(store)::Directory\n" +
            "{\n" +
            "}\n"
        );
    }

    public void testIdentElse() {
        setDefaultsOptions();
        setLoadDocumentText(
            "    void FieldsWriter::addDocument(Document* doc)\n" +
            "    {\n" +
            "         if (field->stringValue() == NULL) {\n" +
            "             Reader* r = field->readerValue();\n" +
            "         }    else\n" +
            "         fieldsStream->writeString(field->stringValue(), _tcslen(field->stringValue()));\n" +
            "    }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing eles without {}",
            "void FieldsWriter::addDocument(Document* doc)\n" +
            "{\n" +
            "    if (field->stringValue() == NULL) {\n" +
            "        Reader* r = field->readerValue();\n" +
            "    } else\n" +
            "        fieldsStream->writeString(field->stringValue(), _tcslen(field->stringValue()));\n" +
            "}\n"
        );
    }

    public void testIdentDoWhile() {
        setDefaultsOptions();
        setLoadDocumentText(
            " int foo()\n" +
            " {\n" +
            " do {\n" +
            " try {\n" +
            " op1().op2.op3().op4();\n" +
            " } catch (Throwable t) {\n" +
            " log();\n" +
            " }\n" +
            " }\n" +
            " while (this.number < 2 && number != 3);\n"+ 
            " }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing doWhile",
            "int foo()\n" +
            "{\n" +
            "    do {\n" +
            "        try {\n" +
            "            op1().op2.op3().op4();\n" +
            "        } catch (Throwable t) {\n" +
            "            log();\n" +
            "        }\n" +
            "    } while (this.number < 2 && number != 3);\n"+ 
            "}\n"
        );
    }

    public void testIdentInlineMethod() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "    		CL_NS(store)::Directory* getDirectory() { return directory; }\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "    CL_NS(store)::Directory* getDirectory()\n" +
            "    {\n" +
            "        return directory;\n" +
            "    }\n" +
            "};\n"
        );
    }

    public void testIdentInlineMethod2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "    		CL_NS(store)::Directory* getDirectory() { return directory; }\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "CL_NS(store)::Directory* getDirectory()\n" +
            "{\n" +
            "    return directory;\n" +
            "}\n"
        );
    }

    // end line comment should prevent move left brace on same line by design
    // RFE: move brace before end line comment in future
    public void testBraceBeforeLineComment() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "if (!line) // End of file\n" +
            "{\n" +
            "status.exit_status = 0;\n" +
            "break;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect formatting brace before line comment",
            "int foo()\n" +
            "{\n" +
            "    if (!line) // End of file\n" +
            "    {\n" +
            "        status.exit_status = 0;\n" +
            "        break;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testCaseIndentAftePreprocessor() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "            setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "            break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "            strmov(mysql_charsets_dir, argument);\n" +
            "            charsets_dir = mysql_charsets_dir;\n" +
            "            break;\n" +
            "        case OPT_DEFAULT_CHARSET:\n" +
            "            default_charset_used = 1;\n" +
            "            break;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testCaseIndentAftePreprocessor2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "#define Y\n" +
            "        {\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "}\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "    case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "    case OPT_CHARSETS_DIR:\n" +
            "#define Y\n" +
            "    {\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    }\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "    }\n" +
            "}\n"
        );
    }
    public void testCaseIndentAftePreprocessorHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid)\n" +
            "      {\n" +
            "#ifdef __NETWARE__\n" +
            "      case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "      case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "      case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "      }\n" +
            "}\n"
        );
    }

    public void testCaseIndentAftePreprocessorHalf2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceSwitch, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
            "int foo() {\n" +
            "     switch (optid) {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "        break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "        strmov(mysql_charsets_dir, argument);\n" +
            "        charsets_dir = mysql_charsets_dir;\n" +
            "        break;\n" +
            "    case OPT_DEFAULT_CHARSET:\n" +
            "        default_charset_used = 1;\n" +
            "        break;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "int foo()\n" +
            "{\n" +
            "    switch (optid)\n" +
            "      {\n" +
            "#ifdef __NETWARE__\n" +
            "        case OPT_AUTO_CLOSE:\n" +
            "          setscreenmode(SCR_AUTOCLOSE_ON_EXIT);\n" +
            "#define X\n" +
            "          break;\n" +
            "#endif\n" +
            "        case OPT_CHARSETS_DIR:\n" +
            "          strmov(mysql_charsets_dir, argument);\n" +
            "          charsets_dir = mysql_charsets_dir;\n" +
            "          break;\n" +
            "        case OPT_DEFAULT_CHARSET:\n" +
            "          default_charset_used = 1;\n" +
            "          break;\n" +
            "      }\n" +
            "}\n"
        );
    }

    public void testTypedefClassNameIndent() {
        setDefaultsOptions();
        setLoadDocumentText(
            "typedef struct st_line_buffer\n" +
            "{\n" +
            "File file;\n" +
            "char *buffer;\n" +
            "/* The buffer itself, grown as needed. */\n" +
            "}LINE_BUFFER;\n" 
            );
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "typedef struct st_line_buffer\n" +
            "{\n" +
            "    File file;\n" +
            "    char *buffer;\n" +
            "    /* The buffer itself, grown as needed. */\n" +
            "} LINE_BUFFER;\n" 
        );
    }

    public void testLabelIndent() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "end:\n" +
            "if (fd >= 0)\n" +
            "        my_close(fd, MYF(MY_WME));\n" +
            "    return error;\n" +
            "}\n" 
            );
        reformat();
        assertDocumentText("Incorrect label indent",
            "int foo()\n" +
            "{\n" +
            "end:\n" +
            "    if (fd >= 0)\n" +
            "        my_close(fd, MYF(MY_WME));\n" +
            "    return error;\n" +
            "}\n" 
        );
    }

    public void testIdentBlockAfterDirective() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int yyparse()\n" +
            "{\n" +
            "    yychar = - 1;\n" +
            "#if YYMAXDEPTH <= 0\n" +
            "    if (yymaxdepth <= 0) {\n" +
            "        if ((yymaxdepth = YYEXPAND(0)) <= 0) {\n" +
            "            yyerror(\"yacc initialization error\");\n" +
            "            YYABORT;\n" +
            "        }\n" +
            "    }\n" +
            "#endif\n" +
            " {\n" +
            "        register YYSTYPE *yy_pv;\n" +
            "        /* top of value stack */\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing coode block after directive",
            "int yyparse()\n" +
            "{\n" +
            "    yychar = -1;\n" +
            "#if YYMAXDEPTH <= 0\n" +
            "    if (yymaxdepth <= 0) {\n" +
            "        if ((yymaxdepth = YYEXPAND(0)) <= 0) {\n" +
            "            yyerror(\"yacc initialization error\");\n" +
            "            YYABORT;\n" +
            "        }\n" +
            "    }\n" +
            "#endif\n" +
            "    {\n" +
            "        register YYSTYPE *yy_pv;\n" +
            "        /* top of value stack */\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testMacroBeforePrepricessor() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        setLoadDocumentText(
            "int yyparse()\n" +
            "{\n" +
            "    switch (nchar) {\n" +
            "        /* split current window in two parts, horizontally */\n" +
            "    case 'S':\n" +
            "    case 's':\n" +
            "        CHECK_CMDWIN\n" +
            "#    ifdef FEAT_VISUAL\n" +
            "reset_VIsual_and_resel();\n" +
            "        /* stop Visual mode */\n" +
            "#    endif\n" +
            "    case 'W':\n" +
            "        CHECK_CMDWIN\n" +
            "if (lastwin == firstwin && Prenum != 1) /* just one window */\n" +
            "            beep_flush();\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing macro before preoprocessor",
            "int yyparse()\n" +
            "{\n" +
            "    switch (nchar) {\n" +
            "        /* split current window in two parts, horizontally */\n" +
            "    case 'S':\n" +
            "    case 's':\n" +
            "        CHECK_CMDWIN\n" +
            "#ifdef FEAT_VISUAL\n" +
            "                reset_VIsual_and_resel();\n" +
            "        /* stop Visual mode */\n" +
            "#endif\n" +
            "    case 'W':\n" +
            "        CHECK_CMDWIN\n" +
            "        if (lastwin == firstwin && Prenum != 1) /* just one window */\n" +
            "            beep_flush();\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentElseBeforePreprocessor() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        setLoadDocumentText(
            "int yyparse()\n" +
            "{\n" +
            "#ifdef X\n" +
            "    if (true) {\n" +
            "        if (oldwin->w_p_wfw)\n" +
            "            win_setwidth_win(oldwin->w_width + new_size, oldwin);\n" +
            "    } else\n" +
            "#    endif\n" +
            " {\n" +
            "        layout = FR_COL;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing else before preprocessor",
            "int yyparse()\n" +
            "{\n" +
            "#ifdef X\n" +
            "    if (true) {\n" +
            "        if (oldwin->w_p_wfw)\n" +
            "            win_setwidth_win(oldwin->w_width + new_size, oldwin);\n" +
            "    } else\n" +
            "#endif\n" +
            "    {\n" +
            "        layout = FR_COL;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentK_and_R_style() {
        setDefaultsOptions();
        setLoadDocumentText(
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "{\n" +
            "    int i;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing K&R declaration",
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "{\n" +
            "    int i;\n" +
            "}\n"
        );
    }

    public void testIdentK_and_R_style2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "extern \"C\" {\n" +
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "{\n" +
            "    int i;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "extern \"C\"\n" +
            "{\n" +
            "    static void\n" +
            "    win_init(newp, oldp)\n" +
            "    win_T *newp;\n" +
            "    win_T *oldp;\n" +
            "    {\n" +
            "        int i;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentInBlockComment() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "extern \"C\" {\n" +
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "           /*\n" +
            "             Preserve identation in block\n" +
            "               1.\n" +
            "               2.\n" +
            "\n" +
            "            */\n" +
            "{\n" +
            "/*\n" +
            "  Preserve identation in block\n" +
            "    1.\n" +
            "    2.\n" +
            "\n" +
            "*/\n" +
            "    int i;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing in block comment",
            "extern \"C\"\n" +
            "{\n" +
            "    static void\n" +
            "    win_init(newp, oldp)\n" +
            "    win_T *newp;\n" +
            "    win_T *oldp;\n" +
            "    /*\n" +
            "      Preserve identation in block\n" +
            "        1.\n" +
            "        2.\n" +
            "\n" +
            "     */\n" +
            "    {\n" +
            "        /*\n" +
            "          Preserve identation in block\n" +
            "            1.\n" +
            "            2.\n" +
            "\n" +
            "         */\n" +
            "        int i;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testIdentInBlockComment2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "extern \"C\" {\n" +
            "static void\n" +
            "win_init(newp, oldp)\n" +
            "win_T *newp;\n" +
            "win_T *oldp;\n" +
            "      /*\n" +
            "           * Preserve identation in block\n" +
            "          *   1.\n" +
            "       *   2.\n" +
            "*\n" +
            "   */\n" +
            "{\n" +
            "  /*\n" +
            "* Preserve identation in block\n" +
            "    *   1.\n" +
            " *   2.\n" +
            "*\n" +
            "*/\n" +
            "    int i;\n" +
            "}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing in block comment",
            "extern \"C\"\n" +
            "{\n" +
            "    static void\n" +
            "    win_init(newp, oldp)\n" +
            "    win_T *newp;\n" +
            "    win_T *oldp;\n" +
            "    /*\n" +
            "     * Preserve identation in block\n" +
            "     *   1.\n" +
            "     *   2.\n" +
            "     *\n" +
            "     */\n" +
            "    {\n" +
            "        /*\n" +
            "         * Preserve identation in block\n" +
            "         *   1.\n" +
            "         *   2.\n" +
            "         *\n" +
            "         */\n" +
            "        int i;\n" +
            "    }\n" +
            "}\n"
        );
    
    }

    public void testAddNewLineAfterSemocolon() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo(int i)\n" +
            "{\n" +
            "if(true) if(true) if(true) i--;\n" +
            "else i++;else i++; else i++;\n" +
            " if(true) while(i>0) i--;\n" +
            " if(true) return; else break;\n" +
            " if(true) return;\n" +
            " else {break;}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect adding new line after semocolon",
            "int foo(int i)\n" +
            "{\n" +
            "    if (true) if (true) if (true) i--;\n" +
            "            else i++;\n" +
            "        else i++;\n" +
            "    else i++;\n" +
            "    if (true) while (i > 0) i--;\n" +
            "    if (true) return;\n" +
            "    else break;\n" +
            "    if (true) return;\n" +
            "    else {\n" +
            "        break;\n" +
            "    }\n" +
            "}\n"
        );
    }

    public void testAddNewLineAfterSemocolon2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
            "int foo(int i)\n" +
            "{\n" +
            "if(true) if(true) if(true) i--;\n" +
            "else i++;else i++; else i++;\n" +
            " if(true) while(i>0) i--;\n" +
            " if(true) return; else break;\n" +
            " if(true) return;\n" +
            " else {break;}\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect adding new line after semocolon",
            "int foo(int i)\n" +
            "{\n" +
            "    if (true) if (true) if (true) i--;\n" +
            "        else i++;\n" +
            "      else i++;\n" +
            "    else i++;\n" +
            "    if (true) while (i > 0) i--;\n" +
            "    if (true) return;\n" +
            "    else break;\n" +
            "    if (true) return;\n" +
            "    else\n" +
            "      {\n" +
            "        break;\n" +
            "      }\n" +
            "}\n"
        );
    }

    public void testIdentFunctionDefinition() {
        setDefaultsOptions();
        setLoadDocumentText(
            "uchar *\n" +
            "        tokname(int n)\n" +
            "{\n" +
            "    static char buf[100];\n" +
            "    return printname[n - 257];\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing function definition",
            "uchar *\n" +
            "tokname(int n)\n" +
            "{\n" +
            "    static char buf[100];\n" +
            "    return printname[n - 257];\n" +
            "}\n"
        );
    }
    

    public void testIdentFunctionDefinition2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesBeforeMethods, 0);
        setLoadDocumentText(
            "namespace A\n" +
            "{\n" +
            "uchar *\n" +
            "        tokname(int n)\n" +
            "{\n" +
            "    static char buf[100];\n" +
            "    return printname[n - 257];\n" +
            "}\n"+
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing function definition",
            "namespace A\n" +
            "{\n" +
            "    uchar *\n" +
            "    tokname(int n)\n" +
            "    {\n" +
            "        static char buf[100];\n" +
            "        return printname[n - 257];\n" +
            "    }\n"+
            "}\n"
        );
    }
    
    public void testIdentElseAfterPreprocessor() {
        setDefaultsOptions();
        setLoadDocumentText(
            "getcmdline(int firstc)\n" +
            "{\n" +
            "    if (firstc == '/')\n" +
            "    {\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "	im_set_active(*b_im_ptr == B_IMODE_IM);\n" +
            "#endif\n" +
            "    }\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "    else if (p_imcmdline)\n" +
            "	im_set_active(TRUE);\n" +
            "#endif\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing else after preprocessor",
            "getcmdline(int firstc)\n" +
            "{\n" +
            "    if (firstc == '/') {\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "        im_set_active(*b_im_ptr == B_IMODE_IM);\n" +
            "#endif\n" +
            "    }\n" +
            "#ifdef USE_IM_CONTROL\n" +
            "    else if (p_imcmdline)\n" +
            "        im_set_active(TRUE);\n" +
            "#endif\n" +
            "}\n"
        );
    }

    public void testBlankLineBeforeMethod() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "}\n" +
                "/*\n" +
                "* Call this when vim starts up, whether or not the GUI is started\n" +
                " */\n" +
                "void\n" +
                "gui_prepare(argc)\n" +
                "    int *argc;\n" +
                "{\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect blank line before method",
                "int foo()\n" +
                "{\n" +
                "}\n" +
                "\n" +
                "/*\n" +
                " * Call this when vim starts up, whether or not the GUI is started\n" +
                " */\n" +
                "void\n" +
                "gui_prepare(argc)\n" +
                "int *argc;\n" +
                "{\n" +
                "}\n"
                );
    }

    public void testBlockCodeNewLine() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  bt.setFragmentType(t->getFragmentType());\n" +
                "  { NdbDictionary::Column bc(\"PK\");\n" +
                "    bt.addColumn(bc);\n" +
                "  }\n" +
                "  { NdbDictionary::Column bc(\"DIST\");\n" +
                "    bt.addColumn(bc);\n" +
                "  }\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect block code new line",
                "int foo()\n" +
                "{\n" +
                "    bt.setFragmentType(t->getFragmentType());\n" +
                "    {\n" +
                "        NdbDictionary::Column bc(\"PK\");\n" +
                "        bt.addColumn(bc);\n" +
                "    }\n" +
                "    {\n" +
                "        NdbDictionary::Column bc(\"DIST\");\n" +
                "        bt.addColumn(bc);\n" +
                "    }\n" +
                "}\n"
                );
    }

    public void testBlankLineAfterEndLineComment() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    if (0){\n" +
                "        DBUG_RETURN(3);\n" +
                "    }//if\n" +
                "}//Ndb::NDB_connect()\n" +
                "NdbTransaction *\n" +
                "Ndb::getConnectedNdbTransaction(Uint32 nodeId)\n" +
                "{\n" +
                "    return next;\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
        reformat();
        assertDocumentText("Incorrect blak line after end line comment",
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    if (0) {\n" +
                "        DBUG_RETURN(3);\n" +
                "    }//if\n" +
                "}//Ndb::NDB_connect()\n" +
                "\n" +
                "NdbTransaction *\n" +
                "Ndb::getConnectedNdbTransaction(Uint32 nodeId)\n" +
                "{\n" +
                "    return next;\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
    }
    
    
    public void testReformatCodeBlocks() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    DBUG_ENTER(\"Ndb::startTransaction\");\n" +
                "    if (theInitState == Initialised) {\n" +
                "        NdbTableImpl* impl;\n" +
                "        if (table != 0 && keyData != 0 && (impl = &NdbTableImpl::getImpl(*table))) {\n" +
                "            Uint32 hashValue; {\n" +
                "                Uint32 buf[4];\n" +
                "            }\n" +
                "            const Uint16 *nodes;\n" +
                "            Uint32 cnt = impl->get_nodes(hashValue, &nodes);\n" +
                "        } else {\n" +
                "            nodeId = 0;\n" +
                "        }//if\n" +
                "{\n" +
                "            NdbTransaction *trans = startTransactionLocal(0, nodeId);\n" +
                "        }\n" +
                "    } else {\n" +
                "        DBUG_RETURN(NULL);\n" +
                "    }//if\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
        reformat();
        assertDocumentText("Incorrect code block formatting",
                "int Ndb::NDB_connect(Uint32 tNode)\n" +
                "{\n" +
                "    DBUG_ENTER(\"Ndb::startTransaction\");\n" +
                "    if (theInitState == Initialised) {\n" +
                "        NdbTableImpl* impl;\n" +
                "        if (table != 0 && keyData != 0 && (impl = &NdbTableImpl::getImpl(*table))) {\n" +
                "            Uint32 hashValue;\n" +
                "            {\n" +
                "                Uint32 buf[4];\n" +
                "            }\n" +
                "            const Uint16 *nodes;\n" +
                "            Uint32 cnt = impl->get_nodes(hashValue, &nodes);\n" +
                "        } else {\n" +
                "            nodeId = 0;\n" +
                "        }//if\n" +
                "        {\n" +
                "            NdbTransaction *trans = startTransactionLocal(0, nodeId);\n" +
                "        }\n" +
                "    } else {\n" +
                "        DBUG_RETURN(NULL);\n" +
                "    }//if\n" +
                "}//Ndb::getConnectedNdbTransaction()\n"
                );
    }

    public void testSpaceBinaryOperator() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    bmove_upp(dst + rest+new_length, dst+tot_length, rest);\n" +
            "    if (len <= 0 ||| len >= (int)sizeof(buf) || buf[sizeof(buf)-1] != 0) return 0;\n" +
            "    lmask = (1U << state->lenbits)-1;\n" +
            "    len = BITS(4)+8;\n" +
            "    s->depth[node] = (uch)((s->depth[n] >= s->depth[m] ? s->depth[n] : s->depth[m])+1);\n" +
            "    for (i = 0; i<n; i++) return;\n" +
            "    match[1].end = match[0].end+s_length;\n" +
            "    return(0);\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect spaces in binary operators",
            "int foo()\n" +
            "{\n" +
            "    bmove_upp(dst + rest + new_length, dst + tot_length, rest);\n" +
            "    if (len <= 0 || len >= (int) sizeof (buf) || buf[sizeof (buf) - 1] != 0) return 0;\n" +
            "    lmask = (1U << state->lenbits) - 1;\n" +
            "    len = BITS(4) + 8;\n" +
            "    s->depth[node] = (uch) ((s->depth[n] >= s->depth[m] ? s->depth[n] : s->depth[m]) + 1);\n" +
            "    for (i = 0; i < n; i++) return;\n" +
            "    match[1].end = match[0].end + s_length;\n" +
            "    return (0);\n" +
            "}\n"
        );
    }

    public void testSpaceBinaryOperator2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    BOOST_CHECK(\n" +
            "            ((nc_result.begin()-str1.begin()) == 3) &&\n" +
            "            ((nc_result.end()-str1.begin()) == 6));\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect spaces in binary operators",
            "int foo()\n" +
            "{\n" +
            "    BOOST_CHECK(\n" +
            "            ((nc_result.begin() - str1.begin()) == 3) &&\n" +
            "            ((nc_result.end() - str1.begin()) == 6));\n" +
            "}\n"
        );
    }

    public void testSpaceTemplateSeparator() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    vector<string> tokens1;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect spaces before template separator",
            "int foo()\n" +
            "{\n" +
            "    vector<string> tokens1;\n" +
            "}\n"
        );
    }

    public void testSpaceCastOperator() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceWithinTypeCastParens, true);
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    if (m == NULL ||| *m == \'\\0\') m = (char*)ERR_MSG(s->z_err);\n" +
            "    hold += (unsigned long)(PUP(in)) << bits;\n" +
            "    state = (struct inflate_state FAR *)strm->state;\n" +
            "    if (strm->zalloc == (alloc_func)0) return;\n" +
            "    stream.zalloc = (alloc_func)0;\n" +
            "    put_short(s, (ush)len);\n" +
            "    put_short(s, (ush)~len);\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect spaces in cast operators",
            "int foo()\n" +
            "{\n" +
            "    if (m == NULL || *m == \'\\0\') m = ( char* ) ERR_MSG(s->z_err);\n" +
            "    hold += ( unsigned long ) (PUP(in)) << bits;\n" +
            "    state = ( struct inflate_state FAR * ) strm->state;\n" +
            "    if (strm->zalloc == ( alloc_func ) 0) return;\n" +
            "    stream.zalloc = ( alloc_func ) 0;\n" +
            "    put_short(s, ( ush ) len);\n" +
            "    put_short(s, ( ush ) ~len);\n" +
            "}\n"
        );
    }

    public void testNoSpaceBeforeUnaryOperator() {
        setDefaultsOptions();
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    if (s == NULL ||| s->mode != 'r') return - 1;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect no space before unary operator",
            "int foo()\n" +
            "{\n" +
            "    if (s == NULL || s->mode != 'r') return -1;\n" +
            "}\n"
        );
    }

    public void testNoEscapedSpaceSupport() {
        setDefaultsOptions();
        setLoadDocumentText(
                "static const char* _dbname = \"TEST_DB\";\n" +
                "static void usage()\n" +
                "{\n" +
                "  char desc[] = \n" +
                "    \"[<table> <index>]+\\n\"\\\n" +
                "    \"This program will drop index(es) in Ndb\\n\";\n" +
                "    ndb_std_print_version();\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect escaped space",
                "static const char* _dbname = \"TEST_DB\";\n" +
                "\n" +
                "static void usage()\n" +
                "{\n" +
                "    char desc[] =\n" +
                "            \"[<table> <index>]+\\n\"\\\n" +
                "    \"This program will drop index(es) in Ndb\\n\";\n" +
                "    ndb_std_print_version();\n" +
                "}\n"
                );
    }
 
    public void testIfDoWhile() {
        setDefaultsOptions();
        setLoadDocumentText(
                "void foo()\n" +
                "{\n" +
                "    if (len) do {\n" +
                "            DO1;\n" +
                "    } while (--len);\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect if-do-while indent",
                "void foo()\n" +
                "{\n" +
                "    if (len) do {\n" +
                "            DO1;\n" +
                "        } while (--len);\n" +
                "}\n"
                );
    }

    public void testIfIfDoWhile() {
        setDefaultsOptions();
        setLoadDocumentText(
                "void foo()\n" +
                "{\n" +
                "    if (len) if (true) do {\n" +
                "        DO1;\n" +
                "        } while (--len);\n" +
                "    else return;\n" +
                "    else return;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect if-if-do-while indent",
                "void foo()\n" +
                "{\n" +
                "    if (len) if (true) do {\n" +
                "                DO1;\n" +
                "            } while (--len);\n" +
                "        else return;\n" +
                "    else return;\n" +
                "}\n"
                );
    }

    public void testDoubleFunctionComment() {
        setDefaultsOptions();
        setLoadDocumentText(
                "void foo();\n" +
                "/* Stream status */\n" +
                "/* Data structure describing a single value and its code string. */\n" +
                "typedef struct ct_data_s\n" +
                "{\n" +
                "    ush code;\n" +
                "} FAR ct_data;\n"
                );
        reformat();
        assertDocumentText("Incorrect blank lines between block comments",
                "void foo();\n" +
                "/* Stream status */\n" +
                "\n" +
                "/* Data structure describing a single value and its code string. */\n" +
                "typedef struct ct_data_s\n" +
                "{\n" +
                "    ush code;\n" +
                "} FAR ct_data;\n"
                );
    }

    public void testArrayAsParameter() {
        setDefaultsOptions();
        setLoadDocumentText(
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "    ClassA() : cc({ \"A\", \"B\", \"C\", \"D\"}), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting of array as parameter",
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "\n" +
                "    ClassA() : cc({\"A\", \"B\", \"C\", \"D\"}), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n"
                );
    }

    public void testArrayAsParameter2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentNamespace, false);
        setLoadDocumentText(
                "namespace AC\n" +
                "{\n" +
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "ClassA() : cc({ \"A\", \"B\", \"C\", \"D\" }), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting of array as parameter",
                "namespace AC\n" +
                "{\n" +
                "\n" +
                "class ClassA : InterfaceA, InterfaceB, InterfaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "\n" +
                "    ClassA() : cc({\"A\", \"B\", \"C\", \"D\"}), number(2)\n" +
                "    {\n" +
                "    }\n" +
                "} FAR ct_data;\n" +
                "}\n"
                );
    }

    public void testIssue129747() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentNamespace, false);
        setLoadDocumentText(
                "enum CpuArch { OPTERON, INTEL, SPARC}; // CPU architecture\n"
                );
        reformat();
        assertDocumentText("Issue 129747",
                "enum CpuArch\n" +
                "{\n" +
                "    OPTERON, INTEL, SPARC\n" +
                "}; // CPU architecture\n"
                );
    }

    public void tesIssue129608() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentNamespace, false);
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "s = (teststruct_t)\n" +
                " {\n" +
                "    .a = 1,\n" +
                "            .b = 2,\n" +
                "            .c = 3,\n" +
                "};\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Issue 129608",
                "int foo()\n" +
                "{\n" +
                "    s = (teststruct_t){\n" +
                "        .a = 1,\n" +
                "        .b = 2,\n" +
                "        .c = 3,\n" +
                "    };\n" +
                "}\n"
                );
    }

    public void testReformatIfElseElse() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "  }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect indent if-else if-else",
                 "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "    }\n" +
                "}\n");
    }

    public void testHalfIndent() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "        text = 1;\n" +
                "    else if (strlen(text) == 0)\n" +
                "        text = 3;\n" +
                "    else\n" +
                "        number++;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "      text = 1;\n" +
                "    else if (strlen(text) == 0)\n" +
                "      text = 3;\n" +
                "    else\n" +
                "      number++;\n" +
                "}\n");
    }

    public void testHalfIndentFull() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "        text = 1;\n" +
                "    else if (strlen(text) == 0)\n" +
                "        text = 3;\n" +
                "    else\n" +
                "        number++;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "  if (text == NULL)\n" +
                "    text = 1;\n" +
                "  else if (strlen(text) == 0)\n" +
                "    text = 3;\n" +
                "  else\n" +
                "    number++;\n" +
                "}\n");
    }

    public void testHalfIndent2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "  }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "    if (text == NULL)\n" +
                "      {\n" +
                "        text = 1;\n" +
                "      }\n" +
                "    else if (strlen(text) == 0)\n" +
                "      {\n" +
                "        text = 3;\n" +
                "      }\n" +
                "    else\n" +
                "      {\n" +
                "        number++;\n" +
                "      }\n" +
                "}\n");
    }

    public void testHalfIndent2Full() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineElse, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int method()\n" +
                "{\n" +
                "    if (text == NULL) {\n" +
                "        text = 1;\n" +
                "    } else if (strlen(text) == 0) {\n" +
                "        text = 3;\n" +
                "    } else {\n" +
                "        number++;\n" +
                "  }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect block half indent",
                 "int method()\n" +
                "{\n" +
                "  if (text == NULL)\n" +
                "    {\n" +
                "      text = 1;\n" +
                "    }\n" +
                "  else if (strlen(text) == 0)\n" +
                "    {\n" +
                "      text = 3;\n" +
                "    }\n" +
                "  else\n" +
                "    {\n" +
                "      number++;\n" +
                "    }\n" +
                "}\n");
    }

    public void testDoWhileHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineWhile, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "  while (this.number < 2 &&\n" +
                "      number != 3)\n" +
                "    {\n" +
                "      method(12);\n" +
                "    }\n" +
                "  do\n" +
                "    {\n" +
                "      op1().op2.op3().op4();\n" +
                "    }\n" +
                "   while (this.number < 2 &&\n" +
                "   number != 3);\n" +
                "}\n");

        
        reformat();
        assertDocumentText("Incorrect formatting half do-while",
                "int main(int i)\n" +
                "{\n" +
                "  while (this.number < 2 &&\n" +
                "          number != 3)\n" +
                "    {\n" +
                "      method(12);\n" +
                "    }\n" +
                "  do\n" +
                "    {\n" +
                "      op1().op2.op3().op4();\n" +
                "    }\n" +
                "  while (this.number < 2 &&\n" +
                "          number != 3);\n" +
                "}\n");
    }

    public void testDoWhileHalf2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineWhile, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int foo() {\n" +
                "do {\n" +
                "    i++;\n" +
                "} while(true);\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting do-while half indent",
                "int foo()\n" +
                "{\n" +
                "  do\n" +
                "    {\n" +
                "      i++;\n" +
                "    }\n" +
                "  while (true);\n" +
                "}\n");
    }

    public void testDereferenceAfterIf() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int main(int i)\n" +
                "{\n" +
                "if (offset)\n" +
                "    *offset = layout->record_size/ BITS_PER_UNIT;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect space for dereference after if",
                "int main(int i)\n" +
                "{\n" +
                "    if (offset)\n" +
                "        *offset = layout->record_size / BITS_PER_UNIT;\n" +
                "}\n");
    }

    public void testTryCatchHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineCatch, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int foo() {\n" +
                "try {\n" +
                "    i++;\n" +
                "} catch (char e){\n" +
                "    i--;\n" +
                "} catch (char e)\n" +
                "    i--;\n" +
                "if (true)try\n" +
                "    i++;\n" +
                "catch (char e)\n" +
                "    i--;\n" +
                " catch (char e){\n" +
                "    i--;}\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting try-catch half indent",
                "int foo()\n" +
                "{\n" +
                "  try\n" +
                "    {\n" +
                "      i++;\n" +
                "    }\n" +
                "  catch (char e)\n" +
                "    {\n" +
                "      i--;\n" +
                "    }\n" +
                "  catch (char e)\n" +
                "    i--;\n" +
                "  if (true) try\n" +
                "      i++;\n" +
                "    catch (char e)\n" +
                "      i--;\n" +
                "    catch (char e)\n" +
                "      {\n" +
                "        i--;\n" +
                "      }\n" +
                "}\n");
    }

    public void testEndLineComments() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineCatch, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  if (strcmp (TREE_STRING_POINTER (id), \"default\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_DEFAULT;  // comment\n" +
                "  else if (strcmp (TREE_STRING_POINTER (id), \"hidden\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_HIDDEN;  \n" +
                "  else if (strcmp (TREE_STRING_POINTER (id), \"protected\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_PROTECTED;   /* comment */   \n" +
                "  else\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_PROTECTED;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect unexpected new line after semicolomn",
                "int foo()\n" +
                "{\n" +
                "  if (strcmp(TREE_STRING_POINTER(id), \"default\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_DEFAULT; // comment\n" +
                "  else if (strcmp(TREE_STRING_POINTER(id), \"hidden\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_HIDDEN;\n" +
                "  else if (strcmp(TREE_STRING_POINTER(id), \"protected\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_PROTECTED; /* comment */\n" +
                "  else\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_PROTECTED;\n" +
                "}\n");
    }

    public void testLabelIndentHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  start: while(true){\n" +
                "int i = 0;\n" +
                "goto start;\n" +
                "end:\n" +
                "if(true){\n" +
                "foo();\n" +
                "second:\n" +
                "foo();\n" +
                "}\n" +
                "}\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect label half indent",
                "int foo()\n" +
                "{\n" +
                "start:\n" +
                "  while (true)\n" +
                "    {\n" +
                "      int i = 0;\n" +
                "      goto start;\n" +
                "end:\n" +
                "      if (true)\n" +
                "        {\n" +
                "          foo();\n" +
                "second:\n" +
                "          foo();\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testLabelIndentHalf2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.absoluteLabelIndent, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  start: while(true){\n" +
                "int i = 0;\n" +
                "goto start;\n" +
                "end:\n" +
                "if(true){\n" +
                "foo();\n" +
                "second:\n" +
                "foo();\n" +
                "}\n" +
                "}\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect label half indent",
                "int foo()\n" +
                "{\n" +
                "start:\n" +
                "  while (true)\n" +
                "    {\n" +
                "      int i = 0;\n" +
                "      goto start;\n" +
                "    end:\n" +
                "      if (true)\n" +
                "        {\n" +
                "          foo();\n" +
                "        second:\n" +
                "          foo();\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testLabelStatementIndent() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.absoluteLabelIndent, false);
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  start: while(true){\n" +
                "int i = 0;\n" +
                "goto start;\n" +
                "end:\n" +
                "if(true){\n" +
                "foo();\n" +
                "second:\n" +
                "foo();\n" +
                "}\n" +
                "}\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect label indent",
                "int foo()\n" +
                "{\n" +
                "start:\n" +
                "    while (true) {\n" +
                "        int i = 0;\n" +
                "        goto start;\n" +
                "    end:\n" +
                "        if (true) {\n" +
                "            foo();\n" +
                "        second:\n" +
                "            foo();\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testOperatorEQformatting() {
        setDefaultsOptions();
        setLoadDocumentText(
                "class real_c_float\n" +
                "{\n" +
                "  const real_c_float & operator=(long l){ from_long(l);\n" +
                "    return *this;\n" +
                "  }\n" +
                "};\n");
        reformat();
        assertDocumentText("Incorrect operator = formatting",
                "class real_c_float\n" +
                "{\n" +
                "\n" +
                "    const real_c_float & operator=(long l)\n" +
                "    {\n" +
                "        from_long(l);\n" +
                "        return *this;\n" +
                "    }\n" +
                "};\n");
    }

    public void testDereferenceFormatting() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "for (DocumentFieldList* list = fieldList; list != NULL; list = list->next) {\n" +
                "TCHAR* tmp = list->field->toString();\n" +
                "}\n" +
                "CL_NS_STD(ostream)* infoStream;\n" +
                "directory->deleteFile( *itr );\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect * spacing",
                "int foo()\n" +
                "{\n" +
                "    for (DocumentFieldList* list = fieldList; list != NULL; list = list->next) {\n" +
                "        TCHAR* tmp = list->field->toString();\n" +
                "    }\n" +
                "    CL_NS_STD(ostream)* infoStream;\n" +
                "    directory->deleteFile(*itr);\n" +
                "}\n");
    }

    public void testNewStyleCastFormatting() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "const char* j = const_cast < const char*>(a);\n" +
                "A* c = dynamic_cast <A* > (b);\n" +
                "int i = reinterpret_cast< int > (a);\n" +
                "i = static_cast < int > (*a);\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect new style cast formating",
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "    const char* j = const_cast<const char*> (a);\n" +
                "    A* c = dynamic_cast<A*> (b);\n" +
                "    int i = reinterpret_cast<int> (a);\n" +
                "    i = static_cast<int> (*a);\n" +
                "}\n");
    }

    public void testNewStyleCastFormatting2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceWithinTypeCastParens, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceAfterTypeCast, false);
        setLoadDocumentText(
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "const char* j = const_cast < const char*>(a);\n" +
                "A* c = dynamic_cast <A* > (b);\n" +
                "int i = reinterpret_cast< int > (a);\n" +
                "i = static_cast < int > (*a);\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect new style cast formating",
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "    const char* j = const_cast < const char* >(a);\n" +
                "    A* c = dynamic_cast < A* >(b);\n" +
                "    int i = reinterpret_cast < int >(a);\n" +
                "    i = static_cast < int >(*a);\n" +
                "}\n");
    }

    public void testConcurrentSpacing() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "              for (cnt = 0; domain->successor[cnt] != NULL;++cnt);\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect cpace after ; and befor ++",
                "int foo(char* a, class B* b)\n" +
                "{\n" +
                "    for (cnt = 0; domain->successor[cnt] != NULL; ++cnt);\n" +
                "}\n");
    }

    public void testIZ130538() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceBeforeMethodCallParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceBeforeMethodDeclParen, true);
        setLoadDocumentText(
                "int foooooooo(char* a,\n" +
                " class B* b)\n" +
                "{\n" +
                "    foo(a,\n" +
                "   b);\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formating IZ#130538",
                "int foooooooo (char* a,\n" +
                "               class B* b)\n" +
                "{\n" +
                "    foo (a,\n" +
                "         b);\n" +
                "}\n");
    }

    //IZ#130544:Multiline alignment works wrongly with complex expressions
    //IZ#130690:IDE cann't align multi-line expression on '('
    public void testAlignOtherParen() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineParen, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineIfCondition, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        setLoadDocumentText(
            "int foo()\n" +
            "{\n" +
            "    v = (rup->ru_utime.tv_sec * 1000 + rup->ru_utime.tv_usec / 1000\n" +
            "     + rup->ru_stime.tv_sec * 1000 + rup->ru_stime.tv_usec / 1000);\n" +
            "    if ((inmode[j] == VOIDmode\n" +
            "            && (GET_MODE_SIZE (outmode[j]) > GET_MODE_SIZE (inmode[j])))\n" +
            "            ? outmode[j] : inmode[j]) a++;\n" +
            "  while ((opt = getopt_long(argc, argv, OPTION_STRING,\n" +
            "       options, NULL)) != -1)\n" +
            "    a++;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect spaces in binary operators",
            "int foo()\n" +
            "{\n" +
            "    v = (rup->ru_utime.tv_sec * 1000 + rup->ru_utime.tv_usec / 1000\n" +
            "         + rup->ru_stime.tv_sec * 1000 + rup->ru_stime.tv_usec / 1000);\n" +
            "    if ((inmode[j] == VOIDmode\n" +
            "         && (GET_MODE_SIZE(outmode[j]) > GET_MODE_SIZE(inmode[j])))\n" +
            "        ? outmode[j] : inmode[j]) a++;\n" +
            "    while ((opt = getopt_long(argc, argv, OPTION_STRING,\n" +
            "                              options, NULL)) != -1)\n" +
            "        a++;\n" +
            "}\n"
        );
    }

    //IZ#130525:Formatter should move the name of the function in column one
    public void testNewLineFunctionDefinitionName() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineFunctionDefinitionName, true);
        setLoadDocumentText(
            "static char *concat (char *s1, char *s2)\n" +
            "{\n" +
            "  int i;\n" +
            "   int j;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Formatter should move the name of the function in column one",
            "static char *\n" +
            "concat(char *s1, char *s2)\n" +
            "{\n" +
            "    int i;\n" +
            "    int j;\n" +
            "}\n"
            );
    }

    //IZ#130898:'Spaces around ternary operators' is not working
    public void testSpacesAroundTernary() {
        setDefaultsOptions();
        setLoadDocumentText(
            "static char *concat (char *s1, char *s2)\n" +
            "{\n" +
            "  int i=0;\n" +
            "  i=(i==1)?1:2;\n" +
            "  return (0);\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("\'Spaces around ternary operators\' is not working",
            "static char *concat(char *s1, char *s2)\n" +
            "{\n" +
            "    int i = 0;\n" +
            "    i = (i == 1) ? 1 : 2;\n" +
            "    return (0);\n" +
            "}\n"
            );
    }

    //IZ#130900:'Spaces around Operators|Unary Operators' doesn't work in some cases
    public void testSpaceAroundUnaryOperator() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceAroundUnaryOps, true);
        setLoadDocumentText(
            "int main(int argc, char** argv)\n" +
            "{\n" +
            "    int i = 0;\n" +
            "    i = -i;\n" +
            "    i = (-i);\n" +
            "    return (0);\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect spaces in unary operators",
            "int main(int argc, char** argv)\n" +
            "{\n" +
            "    int i = 0;\n" +
            "    i = - i;\n" +
            "    i = (- i);\n" +
            "    return (0);\n" +
            "}\n"
            );
    }

    //IZ#130901:'Blank Lines|After Class Header' text field works wrongly
    public void testNewLinesAterClassHeader() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }
    public void testNewLinesAterClassHeader2() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }

    public void testNewLinesAterClassHeader3() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }

    public void testNewLinesAterClassHeader4() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.blankLinesAfterClassHeader, 1);
        setLoadDocumentText(
            "class A\n" +
            "{\n" +
            "\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Blank Lines \'After Class Header\' text field works wrongly",
            "class A\n" +
            "{\n" +
            "\n" +
            "public:\n" +
            "\n" +
            "    A()\n" +
            "    {\n" +
            "    }\n" +
            "}\n"
            );
    }

    //IZ#130916:'Multiline Alignment|Array Initializer' checkbox works wrongly
    public void testMultilineArrayAlignment() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineArrayInit, true);
        setLoadDocumentText(
            "        int array[10] ={1, 2, 3, 4,\n" +
            "    5, 6, 7, 8, 9\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("\'Multiline Alignment|Array Initializer\' checkbox works wrongly",
            "int array[10] = {1, 2, 3, 4,\n" +
            "                 5, 6, 7, 8, 9};\n"
            );
    }

    //IZ#131038:GNU style: reformat works wrongly with destructors
    public void testGnuStuleNewLineName() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "locale::~locale() throw()\n" +
                "{ _M_impl->_M_remove_reference(); }\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "locale::~locale () throw ()\n" +
                "{\n" +
                "  _M_impl->_M_remove_reference ();\n" +
                "}\n"
                );
    }

    //IZ#131043:GNU style: reformat works wrongly with function names
    public void testGnuStuleNewLineName2() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "void\n" +
                "__num_base::_S_format_float(const ios_base& __io, char* __fptr, char __mod)\n" +
                "{\n" +
                "return;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "void\n" +
                "__num_base::_S_format_float (const ios_base& __io, char* __fptr, char __mod)\n" +
                "{\n" +
                "  return;\n" +
                "}\n"
                );
    }
    
    //IZ#131059:GNU style: Multiline alignment works wrongly
    public void testGnuStuleNewLineName3() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "int f(int a1, int a2,\n" +
                "      int a3) {\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "int\n" +
                "f (int a1, int a2,\n" +
                "   int a3) { }\n" 
                );
    }

    public void testGnuStuleNewLineName4() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "Db::Db (DbEnv *env, u_int32_t flags)\n" +
                ": imp_ (0)\n" +
                ", env_ (env)\n" +
                "{\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "Db::Db (DbEnv *env, u_int32_t flags)\n" +
                ": imp_ (0)\n" +
                ", env_ (env) { }\n" 
                );
    }

    public void testGnuStuleNewLineName5() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "tree decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "tree\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
    }

    public void testGnuStuleNewLineName6() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "B::tree A::\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "B::tree\n" +
                "A::decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n"
                );
    }
    
    //IZ#131158:"Spaces Within Parenthesis|Braces" checkbox works wrongly
    public void testSpaceWithinBraces() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceWithinBraces, true);
        setLoadDocumentText(
                "int a[] = {1,(1+2),(2+ 3)};\n" +
                "int b[] = {  1,(1+2),(2+ 3)  };\n" +
                "int c[] = {  1,(1+2),(2+ 3)  \n" +
                "};\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting array init",
                "int a[] = { 1, (1 + 2), (2 + 3) };\n" +
                "int b[] = { 1, (1 + 2), (2 + 3) };\n" +
                "int c[] = { 1, (1 + 2), (2 + 3) };\n"
                );
    }

    public void testSpaceWithinBraces2() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int a[] = {1,(1+2),(2+ 3)};\n" +
                "int b[] = {  1,(1+2),(2+ 3)  };\n" +
                "int c[] = {  1,(1+2),(2+ 3)  \n" +
                "};\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting array init",
                "int a[] = {1, (1 + 2), (2 + 3)};\n" +
                "int b[] = {1, (1 + 2), (2 + 3)};\n" +
                "int c[] = {1, (1 + 2), (2 + 3)};\n"
                );
    }

    public void testFunctionNameInNamespace() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "namespace {\n" +
                "void outCustomersList() {\n" +
                "return;\n" +
                "}\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect formatting GNU new line name",
                "namespace\n" +
                "{\n" +
                "\n" +
                "  void\n" +
                "  outCustomersList ()\n" +
                "  {\n" +
                "    return;\n" +
                "  }\n" +
                "}\n"
                );
    }

    // IZ#131286:Nondeterministic behavior of formatter
    public void testIZ131286() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int\n" +
                "foo() {\n" +
                "    s = (teststruct_t){\n" +
                "        .a = 1,\n" +
                "        .b = 2,\n" +
                "        .c = 3,\n" +
                "    };\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Nondeterministic behavior of formatter",
                "int\n" +
                "foo()\n" +
                "{\n" +
                "    s = (teststruct_t){\n" +
                "        .a = 1,\n" +
                "        .b = 2,\n" +
                "        .c = 3,\n" +
                "    };\n" +
                "}\n"
                );
    }
    
    // IZ#123656:Indenting behavior seems odd
    public void testIZ123656() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int\n" +
                "foo() {\n" +
                "a\n" +
                "b\n" +
                "i=0;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Indenting behavior seems odd",
                "int\n" +
                "foo()\n" +
                "{\n" +
                "    a\n" +
                "    b\n" +
                "    i = 0;\n" +
                "}\n"
                );
    }

    // IZ#123656:Indenting behavior seems odd
    public void testIZ123656_2() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int\n" +
                "foo() {\n" +
                "a()\n" +
                "b\n" +
                "i=0;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Indenting behavior seems odd",
                "int\n" +
                "foo()\n" +
                "{\n" +
                "    a()\n" +
                "    b\n" +
                "    i = 0;\n" +
                "}\n"
                );
    }

    // IZ#123656:Indenting behavior seems odd
    public void testIZ123656_3() {
        setDefaultsOptions();
        setLoadDocumentText(
            " C_MODE_START\n" +
            "#    include <decimal.h>\n" +
            "        C_MODE_END\n" +
            "\n" +
            "#    define DECIMAL_LONGLONG_DIGITS 22\n" +
            "\n" +
            "\n" +
            "        /* maximum length of buffer in our big digits (uint32) */\n" +
            "#    define DECIMAL_BUFF_LENGTH 9\n" +
            "        /*\n" +
            "        point on the border of our big digits))\n" +
            "*/\n" +
            "#    define DECIMAL_MAX_PRECISION ((DECIMAL_BUFF_LENGTH * 9) - 8*2)\n" +
            "\n"
            );
        reformat();
        assertDocumentText("Incorrect identing case after preprocessor",
            "C_MODE_START\n" +
            "#include <decimal.h>\n" +
            "C_MODE_END\n" +
            "\n" +
            "#define DECIMAL_LONGLONG_DIGITS 22\n" +
            "\n" +
            "\n" +
            "/* maximum length of buffer in our big digits (uint32) */\n" +
            "#define DECIMAL_BUFF_LENGTH 9\n" +
            "/*\n" +
            "point on the border of our big digits))\n" +
            " */\n" +
            "#define DECIMAL_MAX_PRECISION ((DECIMAL_BUFF_LENGTH * 9) - 8*2)\n" +
            "\n"
        );
    }

    public void testIdentMultyConstructor5() {
        setDefaultsOptions("MySQL");
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineParen, true);
        setLoadDocumentText(
            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" + 
            "				 ulong query_length, bool using_trans,\n" +
            "				 bool suppress_use)\n" +
              ":Log_event(thd_arg,\n" +
            "	     ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
            "	      || (suppress_use          ? LOG_EVENT_SUPPRESS_USE_F    : 0)),\n" +
            "	     using_trans),\n" +
            "   data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
            "   db(thd_arg->db), q_len((uint32) query_length),\n" +
            "   error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
            "              ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
            "               0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
            "   thread_id(thd_arg->thread_id),\n" +
            "   /* save the original thread id; we already know the server id */\n" +
            "   slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
            "   flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
            "   sql_mode(thd_arg->variables.sql_mode),\n" +
            "   auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
            "   auto_increment_offset(thd_arg->variables.auto_increment_offset)\n" +
            "{\n" +
            "    time_t end_time;\n" +
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" + 
            "                                 ulong query_length, bool using_trans,\n" +
            "                                 bool suppress_use)\n" +
            ": Log_event(thd_arg,\n" +
            "            ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
            "             | (suppress_use ? LOG_EVENT_SUPPRESS_USE_F : 0)),\n" +
            "            using_trans),\n" +
            "data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
            "db(thd_arg->db), q_len((uint32) query_length),\n" +
            "error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
            "           ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
            "            0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
            "thread_id(thd_arg->thread_id),\n" +
            "/* save the original thread id; we already know the server id */\n" +
            "slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
            "flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
            "sql_mode(thd_arg->variables.sql_mode),\n" +
            "auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
            "auto_increment_offset(thd_arg->variables.auto_increment_offset)\n" +
            "{\n" +
            "  time_t end_time;\n" +
            "}\n"
        );
    }

    // IZ#131379:GNU style: formatter works wrong with functions if it returns struct
    public void testIZ131379() {
        setDefaultsOptions("GNU");
        setLoadDocumentText(
                "tree\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n" +
                "\n" +
                "void\n" +
                "decl_shadowed_for_var_insert (tree from, tree to)\n" +
                "{\n" +
                "  return;\n" +
                "}\n" +
                "\n"
                );
        reformat();
        assertDocumentText("Indenting behavior seems odd",
                "tree\n" +
                "decl_shadowed_for_var_lookup (tree from)\n" +
                "{\n" +
                "  return NULL_TREE;\n" +
                "}\n" +
                "\n" +
                "void\n" +
                "decl_shadowed_for_var_insert (tree from, tree to)\n" +
                "{\n" +
                "  return;\n" +
                "}\n" +
                "\n"
                );
    }

    // IZ#130509:Formatter should ignore empty function body
    public void testIZ130509() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.ignoreEmptyFunctionBody, true);
        setLoadDocumentText(
                "int foo0() { \n" +
                "  }\n" +
                "int foo1() { } \n" +
                "int foo2()\n" +
                " { } \n" +
                "int foo3(){}\n" +
                "int foo4(){\n" +
                "}\n" +
                "int foo5() { //\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Formatter should ignore empty function body",
                "int foo0() { }\n" +
                "\n" +
                "int foo1() { }\n" +
                "\n" +
                "int foo2() { }\n" +
                "\n" +
                "int foo3() { }\n" +
                "\n" +
                "int foo4() { }\n" +
                "\n" +
                "int foo5() { //\n" +
                "}\n"
                );
    }

    // IZ#130509:NPE on formatting unbalanced braces
    // Correct test case when macro will be taken into account
    public void testIZ135015() {
        setDefaultsOptions();
        setLoadDocumentText(
                "#define FOR(n) for (int i = 0; i < n; i++) {\n" +
                "\n" +
                "int g() {\n" +
                "    FOR(2)\n" +
                "        foo();\n" +
                "    }\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#130509:NPE on formatting unbalanced braces",
                "#define FOR(n) for (int i = 0; i < n; i++) {\n" +
                "\n" +
                "int g()\n" +
                "{\n" +
                "    FOR(2)\n" +
                "    foo();\n" +
                "}\n" +
                "}\n"
                );
    }
    
    // IZ#135205:'Spaces Before Keywords|else' option works wrongly in some cases
    public void testIZ135205() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int main() {\n" +
                "    int i = 0;\n" +
                "    if (1) {\n" +
                "        i = 2;\n" +
                "    }else {\n" +
                "        i = 3;\n" +
                "    }\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#135205:'Spaces Before Keywords|else' option works wrongly in some cases",
                "int main()\n" +
                "{\n" +
                "    int i = 0;\n" +
                "    if (1) {\n" +
                "        i = 2;\n" +
                "    } else {\n" +
                "        i = 3;\n" +
                "    }\n" +
                "}\n"
                );
    }
    
    // IZ#131721:Comment moves on new line after reformat
    public void testIZ131721() {
        setDefaultsOptions();
        setLoadDocumentText(
                "char seek_scrbuf[SEEKBUFSIZE]; /* buffer for seeking */\n" +
                "int cf_debug; /* non-zero enables debug prints */\n" +
                "void *\n" +
                "cf_alloc(void *opaque, unsigned int items, unsigned int size)\n" +
                "{\n" +
                "    return (ptr);\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#131721:Comment moves on new line after reformat",
                "char seek_scrbuf[SEEKBUFSIZE]; /* buffer for seeking */\n" +
                "int cf_debug; /* non-zero enables debug prints */\n" +
                "\n" +
                "void *\n" +
                "cf_alloc(void *opaque, unsigned int items, unsigned int size)\n" +
                "{\n" +
                "    return (ptr);\n" +
                "}\n"
                );
    }

    public void testTypecast() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceWithinTypeCastParens, true);
        setLoadDocumentText(
                "int i = (int)'a';\n"+
                "void *\n" +
                "foo(void *ptr)\n" +
                "{\n" +
                "    ptr = *(long*)ptr +(int)ptr+ (struct A*)ptr;\n" +
                "    return(int)(ptr);\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Wrong type cast formatting",
                "int i = ( int ) 'a';\n"+
                "\n" +
                "void *\n" +
                "foo(void *ptr)\n" +
                "{\n" +
                "    ptr = *( long* ) ptr + ( int ) ptr + ( struct A* ) ptr;\n" +
                "    return ( int ) (ptr);\n" +
                "}\n"
                );
    }

    public void testReformatMultiLineAndSpacing() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceWithinMethodDeclParens, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.spaceWithinMethodCallParens, true);
        setLoadDocumentText(
                  "void m(int a,\n"
                + "int b) {\n"
                + "    printf(a, \n"
                + "    \"haf\");\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                  "void m( int a,\n" 
                + "        int b ) {\n"
                + "    printf( a,\n"
                + "            \"haf\" );\n"
                + "}\n"
                );
    }
    
    public void testQtExtension() {
        setDefaultsOptions();
        setLoadDocumentText(
                "#define Q_OBJECT\n" +
                "#define signals private\n" +
                "#define slots\n" +
                "\n" +
                "class PrettyPopupMenu\n" +
                "{\n" +
                "};\n" +
                "\n" +
                "class Menu : public PrettyPopupMenu\n" +
                "{\n" +
                "    Q_OBJECT\n" +
                "\n" +
                "signals:\n" +
                "    void test();\n" +
                "\n" +
                "public slots:\n" +
                "    void slotActivated(int index);\n" +
                "\n" +
                "private slots:\n" +
                "    void slotAboutToShow();\n" +
                "\n" +
                "private:\n" +
                "    Menu();\n" +
                "};\n"
                );
        reformat();
        assertDocumentText("Wrong QT formatting",
                "#define Q_OBJECT\n" +
                "#define signals private\n" +
                "#define slots\n" +
                "\n" +
                "class PrettyPopupMenu\n" +
                "{\n" +
                "};\n" +
                "\n" +
                "class Menu : public PrettyPopupMenu\n" +
                "{\n" +
                "    Q_OBJECT\n" +
                "\n" +
                "signals:\n" +
                "    void test();\n" +
                "\n" +
                "public slots:\n" +
                "    void slotActivated(int index);\n" +
                "\n" +
                "private slots:\n" +
                "    void slotAboutToShow();\n" +
                "\n" +
                "private:\n" +
                "    Menu();\n" +
                "};\n"
                );
    }

    public void testExpandToTab() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.expandTabToSpaces, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.tabSize, 4);
        setLoadDocumentText(
                "typedef struct pcihp {\n" +
                "\n" +
                " struct pcihp_slotinfo {\n" +
                "\t\tchar *name;\n" +
                "\t} slotinfo[10];\n" +
                "} pcihp_t;\n"
                );
        for(int i = 0; i < 2; i++){
        reformat();
        assertDocumentText("Incorrect tab formatting",
                "typedef struct pcihp {\n" +
                "\n" +
                "\tstruct pcihp_slotinfo {\n" +
                "\t\tchar *name;\n" +
                "\t} slotinfo[10];\n" +
                "} pcihp_t;\n");
        }
    }

    public void testExpandToTab2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.expandTabToSpaces, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.tabSize, 8);
        setLoadDocumentText(
                "typedef struct pcihp {\n" +
                "\n" +
                " struct pcihp_slotinfo {\n" +
                "\t\tchar *name;\n" +
                "\t} slotinfo[10];\n" +
                "} pcihp_t;\n"
                );
        reformat();
        assertDocumentText("Incorrect tab formatting",
                "typedef struct pcihp {\n" +
                "\n" +
                "    struct pcihp_slotinfo {\n" +
                "\tchar *name;\n" +
                "    } slotinfo[10];\n" +
                "} pcihp_t;\n");
    }

    public void testIZ145529() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass,
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "class Base {\n" +
                "\n" +
                "};\n"
                );
        reformat();
        assertDocumentText("Incorrect empty class formatting",
                "class Base {\n" +
                "};\n"
                );
    }

    public void testReformatConstructorInitializer3() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.constructorListContinuationIndent, 6);
        setLoadDocumentText(
            "class ClipCost {\n" +
            "public:\n" +
            "    ClipCost(OmFrameRate rate = omFrmRateInvalid)\n" +
            "      : m_type(Threshold::play1xThresh),\n" +
            "        m_ticksPerPane(getTicksPerPane(rate)),\n" +
            "        m_frameStart(0),\n" +
            "        m_thisFrame(~0)\n" +
            "    {\n" +
            "        // indentation should be like this\n" +
            "        for (uint i = 0; i < nCosts; i++)\n" +
            "            init(i);\n" +
            "\n" +
            "          // ide insists (strongly) on this indentation\n" +
            "          for (uint i = 0; i < nCosts; i++)\n" +
            "              init(i);\n" +
            "    }\n" +
            "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting of constructor initializer",
            "class ClipCost\n" +
            "{\n" +
            "public:\n" +
            "\n" +
            "    ClipCost(OmFrameRate rate = omFrmRateInvalid)\n" +
            "          : m_type(Threshold::play1xThresh),\n" +
            "          m_ticksPerPane(getTicksPerPane(rate)),\n" +
            "          m_frameStart(0),\n" +
            "          m_thisFrame(~0)\n" +
            "    {\n" +
            "        // indentation should be like this\n" +
            "        for (uint i = 0; i < nCosts; i++)\n" +
            "            init(i);\n" +
            "\n" +
            "        // ide insists (strongly) on this indentation\n" +
            "        for (uint i = 0; i < nCosts; i++)\n" +
            "            init(i);\n" +
            "    }\n" +
            "}\n");
    }

    public void testIZ144976() {
        setLoadDocumentText(
                "int Ar[] ={\n" +
                "1, 2, 3,\n" +
                " 4, 5 };\n"
                );
        reformat();
        assertDocumentText("Incorrect arry init formatting",
                "int Ar[] = {\n" +
                "    1, 2, 3,\n" +
                "    4, 5\n" +
                "};\n"
                );
    }

    public void testIZ144976_2() {
        setLoadDocumentText(
                "int Ar[] ={1, 2, 3,\n" +
                " 4, 5 };\n"
                );
        reformat();
        assertDocumentText("Incorrect arry init formatting",
                "int Ar[] = {1, 2, 3,\n" +
                "    4, 5};\n"
                );
    }

    public void testIZ144976_3() {
        setLoadDocumentText(
                "int Ar[] ={1, 2, 3,\n" +
                "4, 5 \n" +
                " };\n"
                );
        reformat();
        assertDocumentText("Incorrect arry init formatting",
                "int Ar[] = {1, 2, 3,\n" +
                "    4, 5};\n"
                );
    }

    // IZ#156015:'Format' works wrongly with 'while'
    public void testIZ156015() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "    int main() {\n" +
                "    \n" +
                "    do {\n" +
                "        int i;\n" +
                "    } while(0);\n" +
                "\n" +
                "    while(0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "    \n" +
                "    return (0);\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#156015:'Format' works wrongly with 'while'",
                "int main() {\n" +
                "\n" +
                "    do {\n" +
                "        int i;\n" +
                "    } while (0);\n" +
                "\n" +
                "    while (0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "\n" +
                "    return (0);\n" +
                "}\n"
                );
    }

    // IZ#156015:'Format' works wrongly with 'while'
    public void testIZ156015_2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "    int main() {\n" +
                "    \n" +
                "    if (0) do {\n" +
                "        int i;\n" +
                "    } while(0);\n" +
                "\n" +
                "    while(0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "    \n" +
                "    return (0);\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#156015:'Format' works wrongly with 'while'",
                "int main() {\n" +
                "\n" +
                "    if (0) do {\n" +
                "            int i;\n" +
                "        } while (0);\n" +
                "\n" +
                "    while (0) {\n" +
                "        int i;\n" +
                "    }\n" +
                "\n" +
                "    return (0);\n" +
                "}\n"
                );
    }

    public void testIZ170649() {
        setLoadDocumentText(
                "switch (value) {\n" +
                "  case Foo::BAR:\n" +
                "      cout << \"Bar!\" << endl;\n" +
                "    break;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ 170649: Wrong formatting in switch-case with namespace",
                "switch (value) {\n" +
                "    case Foo::BAR:\n" +
                "        cout << \"Bar!\" << endl;\n" +
                "        break;\n" +
                "}\n"
                );
    }

    // IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)
    public void testIZ166051_1() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "int main() {\n" +
                "    do {\n" +
                "        size_t i = 0; while (1) {\n" +
                "    }\n" +
                "    } while (1);\n" +
                "    return 0;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)",
                "int main() {\n" +
                "    do {\n" +
                "        size_t i = 0;\n" +
                "        while (1) {\n" +
                "        }\n" +
                "    } while (1);\n" +
                "    return 0;\n" +
                "}\n"
                );
    }

    // IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)
    public void testIZ166051_2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        setLoadDocumentText(
                "int main() {\n" +
                "  do{\n" +
                "    size_t i = 0;while(true){\n" +
                "  }\n" +
                "    size_t i = 0;\n" +
                "  }while(true);\n" +
                "    return 0;\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#166051:while blocks inside do..while are formatted incorrectly (Alt+Shift+F)",
                "int main() {\n" +
                "    do {\n" +
                "        size_t i = 0;\n" +
                "        while (true) {\n" +
                "        }\n" +
                "        size_t i = 0;\n" +
                "    } while (true);\n" +
                "    return 0;\n" +
                "}\n"
                );
    }

    // IZ#159334:Cannot format initialization list the way I want
    public void testIZ159334_1() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.NEW_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.constructorListContinuationIndent, 4);
        setLoadDocumentText(
                "MyClass::MyClass(int param1, int param2)\n" +
                "   : _var1(param1),\n" +
                "     _var2(param2)\n" +
                "{\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("IZ#159334:Cannot format initialization list the way I want",
                "MyClass::MyClass(int param1, int param2)\n" +
                "    : _var1(param1),\n" +
                "    _var2(param2)\n" +
                "{\n" +
                "}\n"
                );
    }

    // IZ#159334:Cannot format initialization list the way I want
    public void testIZ159334_2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putInt(EditorOptions.constructorListContinuationIndent, 4);
        setLoadDocumentText(
                "class Class\n" +
                "{\n" +
                "    int p, r;\n" +
                "public:\n" +
                "\n" +
                "    Class()\n" +
                "      : p(0),\n" +
                "      r(0) {\n" +
                "    }\n" +
                "    Class(const Class& orig);\n" +
                "    virtual ~Class();\n" +
                "private:\n" +
                "\n" +
                "};\n"
                );
        reformat();
        assertDocumentText("IZ#159334:Cannot format initialization list the way I want",
                "class Class\n" +
                "{\n" +
                "    int p, r;\n" +
                "public:\n" +
                "\n" +
                "    Class()\n" +
                "        : p(0),\n" +
                "        r(0) {\n" +
                "    }\n" +
                "    Class(const Class& orig);\n" +
                "    virtual ~Class();\n" +
                "private:\n" +
                "\n" +
                "};\n"
                );
    }
    
    //  Bug 180110 - Inconsistent C/C++ switch statement formatting
    public void testIZ180110() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration,
                CodeStyle.BracePlacement.SAME_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceSwitch,
                CodeStyle.BracePlacement.NEW_LINE.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.indentCasesFromSwitch, false);
        setLoadDocumentText(
                "int foo(){\n"
                + "    switch(value)\n"
                + "    {\n"
                + "     case MACRO(x):\n"
                + "      {\n"
                + "        break;\n"
                + "    }\n"
                + "    case MACRO_2:\n"
                + "     {\n"
                + "        break;\n"
                + "   }\n"
                + "    case (MACRO_3):\n"
                + "   {\n"
                + "    break;\n"
                + "  }\n"
                + "    }\n"
                + "}\n");
        reformat();
        assertDocumentText("Bug 180110 - Inconsistent C/C++ switch statement formatting",
                "int foo() {\n"
                + "    switch (value)\n"
                + "    {\n"
                + "    case MACRO(x):\n"
                + "    {\n"
                + "        break;\n"
                + "    }\n"
                + "    case MACRO_2:\n"
                + "    {\n"
                + "        break;\n"
                + "    }\n"
                + "    case (MACRO_3):\n"
                + "    {\n"
                + "        break;\n"
                + "    }\n"
                + "    }\n"
                + "}\n");
    }

    //  Bug 176820 -  Erroneous formatting of typecasting of reference
    public void testIZ176820() {
        setDefaultsOptions();
        setLoadDocumentText(
                "void m(char *a)\n" +
                "{\n"+
                "    int *i;\n" +
                "    i=(int *) &a;\n"+
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "void m(char *a)\n" +
                "{\n"+
                "    int *i;\n" +
                "    i = (int *) &a;\n"+
                "}\n"
                    );
    }
}
