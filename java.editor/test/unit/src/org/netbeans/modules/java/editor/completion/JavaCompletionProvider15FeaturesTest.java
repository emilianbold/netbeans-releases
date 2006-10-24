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

package org.netbeans.modules.java.editor.completion;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionProvider15FeaturesTest extends CompletionTestBase {

    public JavaCompletionProvider15FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.5 generics tests -------------------------------------------------------

    public void testEmptyFileBeforeTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<", "empty.pass");
    }

    public void testBeforeTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<", "empty.pass");
    }
    
    public void testBeforeFirstTypeParam() throws Exception {
        performTest("Generics", 33, null, "empty.pass");
    }
    
    public void testEmptyFileTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X", "empty.pass");
    }
    
    public void testTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X", "empty.pass");
    }
    
    public void testOnFirstTypeParam() throws Exception {
        performTest("Generics", 34, null, "empty.pass");
    }

    public void testEmptyFileAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 32, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 35, null, "extendsKeyword.pass");
    }

    public void testEmptyFileTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 36, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 42, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends ", "javaLangContent.pass");
    }
    
    public void testAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends ", "javaLangContent.pass");
    }
    
    public void testAfterExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("Generics", 43, null, "javaLangContent.pass");
    }

    public void testEmptyFileAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 49, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterFirstTypeParam() throws Exception {
        performTest("Generics", 50, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingTypeParams() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypingTypeParams() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypeParams() throws Exception {
        performTest("Generics", 78, null, "extendsAndImplementsKeywords.pass");
    }

}
