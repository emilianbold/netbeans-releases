/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testReformatSimpleClass
 *
 * @author Alexander Simon
 */
public class CCNewFormatterUnitTestCase extends CCFormatterBaseUnitTestCase {

    public CCNewFormatterUnitTestCase(String testMethodName) {
        super(testMethodName);
    }

    /**
     * Perform reformatting of the whole document's text.
     */
    @Override
    protected void reformat() {
        Reformatter f = new Reformatter(getDocument(), CodeStyle.getDefault(getDocument()));
        try {
            f.reformat();
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
	}
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
            "}\n"
            );
        reformat();
        assertDocumentText("Incorrect spaces in binary operators",
            "int foo()\n" +
            "{\n" +
            "    bmove_upp(dst + rest + new_length, dst + tot_length, rest);\n" +
            "    if (len <= 0 || len >= (int) sizeof(buf) || buf[sizeof(buf) - 1] != 0) return 0;\n" +
            "    lmask = (1U << state->lenbits) - 1;\n" +
            "    len = BITS(4) + 8;\n" +
            "    s->depth[node] = (uch) ((s->depth[n] >= s->depth[m] ? s->depth[n] : s->depth[m]) + 1);\n" +
            "    for (i = 0; i < n; i++) return;\n" +
            "    match[1].end = match[0].end + s_length;\n" +
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
            "    put_short(s, ( ush )~len);\n" +
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
                "class ClassA : InterfaceA, InterfaceB, IntefaceC\n" +
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
                "class ClassA : InterfaceA, InterfaceB, IntefaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "\n" +
                "    ClassA() : cc({ \"A\", \"B\", \"C\", \"D\"}), number(2)\n" +
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
                "class ClassA : InterfaceA, InterfaceB, IntefaceC\n" +
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
                "class ClassA : InterfaceA, InterfaceB, IntefaceC\n" +
                "{\n" +
                "public:\n" +
                "    int number;\n" +
                "    char** cc;\n" +
                "\n" +
                "    ClassA() : cc({ \"A\", \"B\", \"C\", \"D\" }), number(2)\n" +
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
}
