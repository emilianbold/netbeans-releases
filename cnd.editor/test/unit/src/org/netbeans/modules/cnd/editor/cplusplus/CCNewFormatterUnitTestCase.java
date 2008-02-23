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

    private void setDefaultsOptions(){
        EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP));
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

//    public void testReformatMultiLineClassDeclaration() {
//        setDefaultsOptions();
//        setLoadDocumentText(
//                "public class C\n"
//                + ": public Runnable {\n"
//                + "int printf(int);\n"
//                + "};\n"
//                );
//        reformat();
//        assertDocumentText("Incorrect new-line indent",
//                "public class C\n"
//                + "        : public Runnable {\n"
//                + "    int printf(int);\n"
//                + "};\n"
//                );
//        
//    }
    
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
                "something = (someComplicatedExpression != null) ?\n" +
                "(aComplexCalculation) :\n" +
                "(anotherComplexCalculation);");
        reformat();
        assertDocumentText("Incorrect ternary conditional operator reformatting",
                "something = (someComplicatedExpression != null) ?\n" +
                "    (aComplexCalculation) :\n" +
                "    (anotherComplexCalculation);");
    }
    
    
    /**
     * Test reformatting of array initializer with newlines on
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=47069
     */
    public void testReformatArrayInitializerWithNewline() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int[] foo = new int[] {1, 2, 3};");
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "int[] foo = new int[] {1, 2, 3};");
    }
    
    /**
     * Test reformatting of newline braces to normal ones
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=48926
     */
    public void testReformatNewlineBracesToNormalOnes() {
        setDefaultsOptions();
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
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "try {\n" +
                "    printf(\"test\");\n" +
                "}\n"+
                "catch (Exception e) {\n" +
                "    printf(\"exception\");\n" +
                "}");
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
                "return performanceSum / getCount()");
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "if (count == 0)\n" +
                "    return 0.0f;\n" +
                "else\n" +
                "    return performanceSum / getCount()");
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

    //    /**
//     * Test reformatting of function arguments list
//     * @see http://www.netbeans.org/issues/show_bug.cgi?id=115628
//     */
//    public void testReformatFunctionArguments() {
//        setDefaultsOptions();
//        setLoadDocumentText(
//            "z = myfoo(a,\n" +
//            "b,\n" +
//            "c);\n");
//        reformat();
//        assertDocumentText("Incorrect reformatting of function arguments list",
//            "z = myfoo(a,\n" +
//            "          b,\n" +
//            "          c);\n");
//    }
    
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

//    /**
//     * Test reformatting of unbalanced braces
//     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
//     */
//    public void testReformatUnbalancedBraces() {
//        setDefaultsOptions();
//        setLoadDocumentText(
//            "void foo() {\n" +
//            "#if A\n" +
//            "if (0) {\n" +
//            "#else\n" +
//            "if (1) {\n" +
//            "#endif\n" +
//            "}\n" +
//            "}\n");
//        reformat();
//        assertDocumentText("Incorrect reformatting of unbalanced braces",
//            "void foo() {\n" +
//            "#if A\n" +
//            "    if (0) {\n" +
//            "#else\n" +
//            "    if (1) {\n" +
//            "#endif\n" +
//            "    }\n" +
//            "}\n");
//    }

    public void testIdentInnerEnum() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceClass, 
                CodeStyle.BracePlacement.SAME_LINE.name());
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
            "\n" +
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
            "\n" +
            "    bool find(Ptr<T>& ptr, const T& rec) const {\n" +
            "        return DLHashTable2<T, U>::find(ptr, rec);\n" +
            "    }\n" +
            "};\n"
            );
    }

    public void testTemplate2() {
        setDefaultsOptions();
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

    public void testIdentDefineBrace() {
        setDefaultsOptions();
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

//    public void testIdentMultyConstructor3() {
//        setDefaultsOptions();
//        setLoadDocumentText(
//            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" +
//            "        ulong query_length, bool using_trans,\n" +
//            "        bool suppress_use)\n" +
//            ":Log_event(thd_arg,\n" +
//            "        ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
//            "        & (suppress_use          ? LOG_EVENT_SUPPRESS_USE_F    : 0)),\n" +
//            "                using_trans),\n" +
//            "                data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
//            "                db(thd_arg->db), q_len((uint32) query_length),\n" +
//            "                error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
//            "                    ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
//            "                        0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
//            "                                thread_id(thd_arg->thread_id),\n" +
//            "                                /* save the original thread id; we already know the server id */\n" +
//            "                                slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
//            "                                flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
//            "                                sql_mode(thd_arg->variables.sql_mode),\n" +
//            "                                auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
//            "                                auto_increment_offset(thd_arg->variables.auto_increment_offset)\n" +
//            "                        {\n" +
//            "                            time_t end_time;\n" +
//            "                        }\n"
//            );
//        reformat();
//        assertDocumentText("Incorrect identing multyline constructor",
//            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" +
//            "        ulong query_length, bool using_trans,\n" +
//            "        bool suppress_use)\n" +
//            ":Log_event(thd_arg,\n" +
//            "        ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
//            "& (suppress_use          ? LOG_EVENT_SUPPRESS_USE_F    : 0)),\n" +
//            "        using_trans),\n" +
//            "        data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
//            "        db(thd_arg->db), q_len((uint32) query_length),\n" +
//            "        error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
//            "            ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
//            "                 0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
//            "        thread_id(thd_arg->thread_id),\n" +
//            "        /* save the original thread id; we already know the server id */\n" +
//            "        slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
//            "        flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
//            "        sql_mode(thd_arg->variables.sql_mode),\n" +
//            "        auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
//            "        auto_increment_offset(thd_arg->variables.auto_increment_offset) {\n" +
//            "    time_t end_time;\n" +
//            "}\n"
//        );
//    }
    
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
}
