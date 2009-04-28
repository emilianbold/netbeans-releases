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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.javadoc;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestSuite;

/**
 * XXX missing tests of unclosed javadoc cases
 * 
 * @author Jan Pokorsky
 */
public class JavadocCompletionUtilsTest extends JavadocTestSupport {

    public JavadocCompletionUtilsTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        System.setProperty("org.netbeans.modules.javadoc.completion.level", "0");
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(JavadocCompletionUtilsTest.class);
//        suite.addTest(new JavadocCompletionUtilsTest("testFindJavadoc"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsInlineTagStart"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsJavadocContext"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsJavadocContext_InEmptyJavadoc"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsLineBreak"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsLineBreak2"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsWhiteSpace"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsInvalidDocInstance"));
        return suite;
    }
    
    public void testIsJavadocContext() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link String} GUG. Second  sentence. <code>true {@link St</code>\n" +
                "  inside indent   * Second line sentence.\n" +
                "     * @param m1 m1 description\n" +
                "     * \n" +
                "     * @return return description\n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        Element methodEl = info.getTopLevelElements().get(0).getEnclosedElements().get(1);
        Doc javaDocFor = info.getElementUtilities().javaDocFor(methodEl);
        Tag[] firstSentenceTags = javaDocFor.firstSentenceTags();
        Tag[] tags = javaDocFor.tags();
        Tag[] inlineTags = javaDocFor.inlineTags();
        String commentText = javaDocFor.commentText();
        String rawCommentText = javaDocFor.getRawCommentText();
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, code.indexOf("HUH"));
        jdts.moveStart();
        jdts.moveNext();
        
        String what = "HUH";
        int offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = " sentence";
        offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "block comment";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "int m";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // test positions around '*'
        
        // empty content
        what = "     * \n";
        offset = code.indexOf(what) + what.length() - 1;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // '*' inside sentence is not considered as indent
        what = " inside indent   *";
        offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // test position inside indent
        what = "   * HUH";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what) + 1;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what) + 2;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what) + 3;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // last line of javadoc
        what = "*/";
        offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "*/";
        offset = code.indexOf(what) + 1;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "*/";
        offset = code.indexOf(what) + 2;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
    }
    
    public void testIsJavadocContext_InEmptyJavadoc() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /***/\n" +
                "    int f1;\n" +
                "    /** */\n" +
                "    int f2;\n" +
                "}\n";
        prepareTest(code);

        String what = "/***/";
        int offset = code.indexOf(what) + 3;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/** */";
        offset = code.indexOf(what) + 3;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
    }

    public void testFindJavadoc() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /***/\n" +
                "    int f1;\n" +
                "    /** */\n" +
                "    int f2;\n" +
                "}\n";
        prepareTest(code);
        
        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(1);
        Doc exp = info.getElementUtilities().javaDocFor(fieldEl);
        
        String what = "/***/";
        int offset = code.indexOf(what) + 3;
        
        Doc jdoc = JavadocCompletionUtils.findJavadoc(info, doc, offset);
        assertEquals("Wrong Doc instance", exp, jdoc);
    }

    public void testFindJavadoc_147533() throws Exception {
        String code =
                "package p;\n" +
                "class C {\n" +
                "    /**jd1*/\n" +
                "    /**/\n" +
                "    int f1;\n" +
                "    /**jd2*/\n" +
                "    /*a*/\n" +
                "    int f2;\n" +
                "}\n";
        prepareTest(code);

        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(1);
        Doc exp = info.getElementUtilities().javaDocFor(fieldEl);

        String what = "/**jd1*/";
        int offset = code.indexOf(what) + 3;

        Doc jdoc = JavadocCompletionUtils.findJavadoc(info, doc, offset);
        assertNull("Wrong Doc instance", jdoc);

        fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(2);
        exp = info.getElementUtilities().javaDocFor(fieldEl);
        what = "/**jd2*/";
        offset = code.indexOf(what) + 3;
        jdoc = JavadocCompletionUtils.findJavadoc(info, doc, offset);
        assertEquals("Wrong Doc instance", exp, jdoc);
    }
    
    public void testResolveOtherText() throws Exception {
        // XXX obsolete, write new one
//        String code = 
//                "package p;\n" +
//                "class C {\n" +
//                "    /**\n" +
//                "     * HUH {@link String} GUG. Second  sentence. <code>true {@link St</code>\n" +
//                "  inside indent   * Second line sentence.\n" +
//                " * \n" +
//                "     * @param m1 m1 description\n" +
//                "     * \n" +
//                "     * @return return description\n" +
//                "     */\n" +
//                "    int m(int m1) {\n" +
//                "        /* block comment */\n" +
//                "        return 0;\n" +
//                "    }\n" +
//                "}\n";
//        prepareTest(code);
//        
//        String what = "     * \n";
//        int offset = code.indexOf(what) + what.length() - 1;
//        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(doc, offset);
//        assertTrue(jdts.moveNext());
//        JavadocCompletionQuery.JavadocContext jdctx = new JavadocCompletionQuery.JavadocContext();
//        JavadocCompletionQuery.resolveOtherText(jdctx, jdts, offset);
//        assertFalse(what, jdctx.isInsideDesription);
//        
//        what = " * \n";
//        offset = code.indexOf(what) + what.length() - 1;
//        jdts = JavadocCompletionUtils.findJavadocTokenSequence(doc, offset);
//        assertTrue(jdts.moveNext());
//        jdctx = new JavadocCompletionQuery.JavadocContext();
//        JavadocCompletionQuery.resolveOtherText(jdctx, jdts, offset);
//        assertFalse(what, jdctx.isInsideDesription);
    }
    
    public void testIsLineBreak() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link String} GUG. Second  sentence. <code>true {@link St</code>\n" +
                "  \n" +
                "  * *\n" +
                "  * {*iii\n" +
                "  inside indent   * Second line sentence.\n" +
                " * \n" +
                "     * @param m1 m1 description\n" +
                "     * \n" +
                "     * @return return description{@link String } \n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "     * HUH";
        int offset = code.indexOf(what) + what.length() - 4;
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts.token()));
        offset += 1;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        // token is INDENT
        assertFalse(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts.token()));
        
        what = "  \n";
        offset = code.indexOf(what);
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts.token(), offset - jdts.offset()));
        
        what = "  * {*i";
        offset = code.indexOf(what) + what.length() - 3;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts.token()));
        assertTrue(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts.token(), offset - jdts.offset()));
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts.token(), offset - jdts.offset()));
    }
    
    public void testIsLineBreak2() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * {@code String}\n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "{@code";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        // test OTHER_TEXT('     * {|')
        assertTrue(jdts.moveNext());
        assertTrue(jdts.token().id() == JavadocTokenId.OTHER_TEXT);
        assertFalse(insertPointer(code, jdts.offset() + jdts.token().length()),
                JavadocCompletionUtils.isLineBreak(jdts.token()));
        // test OTHER_TEXT('     * |{')
        assertTrue(insertPointer(code, jdts.offset() + jdts.token().length() - 1),
                JavadocCompletionUtils.isLineBreak(jdts.token(), jdts.token().length() - 1));
    }
    
    public void testIsWhiteSpace() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link \t String} GUG. Second  sentence. \n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "     * HUH";
        int offset = code.indexOf(what) + what.length() - 4;
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpaceLast(jdts.token()));
        
        what = "Second  sentence.";
        offset = code.indexOf(what) + "Second".length();
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpace(jdts.token()));
        
        what = "\t String}";
        offset = code.indexOf(what);
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpace(jdts.token()));
    }
    
    public void testIsFirstWhiteSpaceAtFirstLine_131826() throws Exception {
        String code = 
                "/** * @author\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        
        prepareTest(code);
        
        String what = "* @author";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isFirstWhiteSpaceAtFirstLine(jdts.token()));
    }
    
    public void testIsInlineTagStart() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link \t String} GUG{@link String}.\n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "HUH {@link";
        int offset = code.indexOf(what) + 3;
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpaceFirst(jdts.token()));
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInlineTagStart(jdts.token()));
        
        what = "GUG{@link";
        offset = code.indexOf(what) + 3;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInlineTagStart(jdts.token()));
    }
    
    public void testIsInsideIndent() throws Exception {
        String code = 
                "/**\n" +
                " * line1\n" +
                " *   \n" +
                "   line3\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        prepareTest(code);
        
        String what = " * line1";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = " * line1";
        offset = code.indexOf(what) + 1;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = " * line1";
        offset = code.indexOf(what) + 2;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = "   line3";
        offset = code.indexOf(what);
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = "line3";
        offset = code.indexOf(what) + 1;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        // issue #128963
        what = " * ";
        offset = code.indexOf(what) + what.length();
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
    }
    
    public void testConcurrentModification_130709() throws Exception {
        String code = 
                "/**\n" +
                " * line1\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        prepareTest(code);
        
        String what = " * line1";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        
        doc.insertString(0, "\n", null);
        assertTrue(jdts.moveNext());
    }
    
    public void testFindJavadocTokenSequenceForElement() throws Exception {
        String code = 
                "/**\n" +
                " * line1\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        prepareTest(code);

        TypeElement clazzC = info.getTopLevelElements().iterator().next();
        assertNotNull(clazzC);

        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, clazzC);
        assertNotNull(jdts);
        assertTrue(jdts.moveNext());

        // test synthetic element #131157
        Element defConstructor = clazzC.getEnclosedElements().get(0);
        assertNotNull(defConstructor);
        assertTrue(info.getElementUtilities().isSynthetic(defConstructor));
        assertNull(JavadocCompletionUtils.findJavadocTokenSequence(info, defConstructor));
    }

    public void testIsInvalidDocInstance() throws Exception {
        String code =
                "package p;\n" +
                "class C {\n" +
                "    /***/\n" +
                "    int f1;\n" +
                "    /** \t */\n" +
                "    int f2;\n" +
                "    /**\t \n" +
                "     */\n" +
                "    int f3;\n" +
                "    /** javadoc\n" +
                "     */\n" +
                "    int f4;\n" +
                "    int f5;\n" +
                "    /**\n" +
                "     */\n" +
                "    int f_issue_159352;\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    int f7;\n" +
                "    /**\n" +
                "\n" +
                "     */\n" +
                "    int f8;\n" +
                "    /**  **/\n" +
                "    int f9_issue_139147;\n" +
                "    /**  ***/\n" +
                "    int f10_issue_139147;\n" +
                "    /**  * */\n" +
                "    int f11_issue_139147;\n" +
                "}\n";
        prepareTest(code);

        doIsInvalidJavadoc(1, null, null, null, false);
        doIsInvalidJavadoc(2, null, null, null, false);
        doIsTokenOfEmptyJavadoc(2, true);
        doIsInvalidJavadoc(3, null, null, null, false);
        doIsTokenOfEmptyJavadoc(3, false);
        doIsInvalidJavadoc(4, null, null, null, false);

        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(4);
        assertNotNull(fieldEl);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, fieldEl);
        assertNotNull(jdts);
        doIsInvalidJavadoc(5, null, null, jdts, true);

        // issue 159352
        doIsInvalidJavadoc(6, null, null, null, false);
        doIsTokenOfEmptyJavadoc(6, true);
        doIsInvalidJavadoc(7, null, null, null, false);
        doIsTokenOfEmptyJavadoc(7, false);
        doIsInvalidJavadoc(8, null, null, null, false);
        doIsTokenOfEmptyJavadoc(8, false);
        doIsInvalidJavadoc(9, null, null, null, false);
        doIsTokenOfEmptyJavadoc(9, true);
        doIsInvalidJavadoc(10, null, null, null, false);
        doIsTokenOfEmptyJavadoc(10, true);
        doIsInvalidJavadoc(11, null, null, null, false);
        doIsTokenOfEmptyJavadoc(11, false);
    }

    private void doIsInvalidJavadoc(int fieldIndex, Element fieldEl, Doc jdoc, TokenSequence<JavadocTokenId> jdts, boolean isInvalid) {
        fieldEl = fieldEl != null ? fieldEl : info.getTopLevelElements().get(0).getEnclosedElements().get(fieldIndex);
        assertNotNull(fieldEl);
        jdoc = jdoc != null ? jdoc : info.getElementUtilities().javaDocFor(fieldEl);
        assertNotNull(jdoc);
        jdts = jdts != null ? jdts : JavadocCompletionUtils.findJavadocTokenSequence(info, fieldEl);
        assertNotNull(jdts);
        assertEquals(fieldEl.getSimpleName().toString(), isInvalid, JavadocCompletionUtils.isInvalidDocInstance(jdoc, jdts));
    }

    private void doIsTokenOfEmptyJavadoc(int fieldIndex, boolean isEmpty) {
        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(fieldIndex);
        assertNotNull(fieldEl);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, fieldEl);
        assertNotNull(jdts);
        jdts.moveStart();
        if (jdts.moveNext()) {
            assertEquals(fieldEl.getSimpleName().toString(), isEmpty, JavadocCompletionUtils.isTokenOfEmptyJavadoc(jdts.token()));
        }
    }
    
}
