/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.lexer;

import java.io.StringReader;
import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author petr
 */
public class PHPLexerTest extends TestCase {

    public PHPLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNoPHPg() {
        TokenSequence<?> ts = seqForText("<html>");
        next(ts, PHPTokenId.T_INLINE_HTML, "<html>");
    }

    public void testOpenTag() {
        TokenSequence<?> ts = seqForText("<?php ?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testOpenTag2() {
        TokenSequence<?> ts = seqForText("<?php \t ?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        next(ts, PHPTokenId.WHITESPACE, " \t ");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testLineComment1() {
        TokenSequence<?> ts = seqForText("<? // comment\n$a?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_LINE_COMMENT, "//");
        next(ts, PHPTokenId.PHP_LINE_COMMENT, " comment\n");
        next(ts, PHPTokenId.PHP_VARIABLE, "$a");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testLineComment2() {
        TokenSequence<?> ts = seqForText("<? # comment\n$a?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_LINE_COMMENT, "#");
        next(ts, PHPTokenId.PHP_LINE_COMMENT, " comment\n");
        next(ts, PHPTokenId.PHP_VARIABLE, "$a");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testLineComment3() {
        TokenSequence<?> ts = seqForText("<? // comment");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_LINE_COMMENT, "//");
        next(ts, PHPTokenId.PHP_LINE_COMMENT, " comment");
    }

    public void testPHPCommnet1() {
        TokenSequence<?> ts = seqForText("<?/*$a*/$b?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        next(ts, PHPTokenId.PHP_COMMENT, "$a");
        next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        next(ts, PHPTokenId.PHP_VARIABLE, "$b");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPCommnet2() {
        TokenSequence<?> ts = seqForText("<?/***\n**$a***\n****/$b?>");
       // printTokenSequence(ts, "testPHPComment2"); ts.moveStart();
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        next(ts, PHPTokenId.PHP_COMMENT, "**\n**$a***\n***");
        next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        next(ts, PHPTokenId.PHP_VARIABLE, "$b");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPCommnet3() {
        // test unfinished comment at the end of file
        TokenSequence<?> ts = seqForText("<?/*a**\n**$a***\n***hello\nword");
        //printTokenSequence(ts, "testPHPComment3"); ts.moveStart();
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        next(ts, PHPTokenId.PHP_COMMENT, "a**\n**$a***\n***hello\nword");
    }

    public void testPHPCommnet4() {
        TokenSequence<?> ts = seqForText("<?/*comment1*/echo/*comment2*/?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        next(ts, PHPTokenId.PHP_COMMENT, "comment1");
        next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        next(ts, PHPTokenId.PHP_ECHO, "echo");
        next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        next(ts, PHPTokenId.PHP_COMMENT, "comment2");
        next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPCommnet5() {
        TokenSequence<?> ts = seqForText("<?\n/*\nRevision 1.6  2007/01/07 18:41:01\n*/echo?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.WHITESPACE, "\n");
        next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        next(ts, PHPTokenId.PHP_COMMENT, "\nRevision 1.6  2007/01/07 18:41:01\n");
        next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        next(ts, PHPTokenId.PHP_ECHO, "echo");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor1() {
        TokenSequence<?> ts = seqForText("<?/**\n * Enter description here...\n * @access private\n * @var string $name\n */\nvar $name = \"ahoj\"\n?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Enter description here...\n * ");
        next(ts, PHPTokenId.PHPDOC_ACCESS, "@access");
        next(ts, PHPTokenId.PHPDOC_COMMENT, " private\n * ");
        next(ts, PHPTokenId.PHPDOC_VAR, "@var");
        next(ts, PHPTokenId.PHPDOC_COMMENT, " string $name\n ");
        next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        next(ts, PHPTokenId.WHITESPACE, "\n");
        next(ts, PHPTokenId.PHP_VAR, "var");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_VARIABLE, "$name");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_TOKEN, "=");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        next(ts, PHPTokenId.WHITESPACE, "\n");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor2() {
        TokenSequence<?> ts = seqForText("<?/**\n * Enter description here...\n * @ppp private\n * @var string $name\n */\nvar $name = \"ahoj\"\n?>");
        //printTokenSequence(ts, "testPHPDocumentor2"); ts.moveStart();
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Enter description here...\n * ");
        next(ts, PHPTokenId.PHPDOC_COMMENT, "@");
        next(ts, PHPTokenId.PHPDOC_COMMENT, "ppp private\n * ");
        next(ts, PHPTokenId.PHPDOC_VAR, "@var");
        next(ts, PHPTokenId.PHPDOC_COMMENT, " string $name\n ");
        next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        next(ts, PHPTokenId.WHITESPACE, "\n");
        next(ts, PHPTokenId.PHP_VAR, "var");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_VARIABLE, "$name");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_TOKEN, "=");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        next(ts, PHPTokenId.WHITESPACE, "\n");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor3() {
        TokenSequence<?> ts = seqForText("<?/**\n * Comment 1\n */\nvar $name = \"ahoj\"\n /**\n * Comment 2\n */\nvar $age = 10?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Comment 1\n ");
        next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        next(ts, PHPTokenId.WHITESPACE, "\n");
        next(ts, PHPTokenId.PHP_VAR, "var");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_VARIABLE, "$name");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_TOKEN, "=");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        next(ts, PHPTokenId.WHITESPACE, "\n ");
        next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Comment 2\n ");
        next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        next(ts, PHPTokenId.WHITESPACE, "\n");
        next(ts, PHPTokenId.PHP_VAR, "var");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_VARIABLE, "$age");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_TOKEN, "=");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_NUMBER, "10");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }
    
    public void testPHPDocumentor4() {
        TokenSequence<?> ts = seqForText("<?/**\n This File is free software; you can redistribute it and/or modify\n */?>");
        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        next(ts, PHPTokenId.PHPDOC_COMMENT, "\n This File is free software; you can redistribute it and/or modify\n ");
        next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }
    
    public void testShortOpenTag() {
        TokenSequence<?> ts = seqForText("<? echo \"ahoj\" ?>");

        next(ts, PHPTokenId.PHP_OPENTAG, "<?");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_ECHO, "echo");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testTokenLength () throws Exception {
        //TokenSequence<?> ts = seqForText("<?php\n      echo \"\n                while ($row) {\n                    $ip = $row['ip'];");
        TokenSequence<?> ts = seqForText("<?php echo \"$row['ip'];");
        //printTokenSequence(ts, "testTokenLength"); ts.moveStart();
        
        next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_ECHO, "echo");
        next(ts, PHPTokenId.WHITESPACE, " ");
        next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"");
        next(ts, PHPTokenId.PHP_VARIABLE, "$row");
        next(ts, PHPTokenId.PHP_TOKEN, "[");
        next(ts, PHPTokenId.PHP_TOKEN, "[");
        
//        next(ts, PHPTokenId.WHITESPACE, "\n      ");
//        next(ts, PHPTokenId.PHP_ECHO, "echo");
//        next(ts, PHPTokenId.WHITESPACE, " ");
//        next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"");
//        next(ts, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, "\n                while (");
//        next(ts, PHPTokenId.PHP_VARIABLE, "$row");
//        next(ts, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, ") {\n                    ");
//        next(ts, PHPTokenId.PHP_VARIABLE, "$ip");
//        next(ts, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, " = ");
//        next(ts, PHPTokenId.PHP_VARIABLE, "$row");
//        next(ts, PHPTokenId.PHP_TOKEN, "[");
//        next(ts, PHPTokenId.PHP_TOKEN, "[");
    }
    
    TokenSequence<?> seqForText(String text) {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, PHPTokenId.language());
        return hi.tokenSequence();
    }

    void next(TokenSequence<?> ts, PHPTokenId id, String fixedText) {
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, id, fixedText, -1);
    }
    
    /** This is used for debugging purposes
     * 
     * @param ts
     * @param name
     */
    private void printTokenSequence (TokenSequence<?> ts, String name) {
        System.out.println("--- " + name + " ---");
        while (ts.moveNext()) {
            System.out.println(ts.token().id()+"\t"+ts.token());
        }
        System.out.println("-----------------------");
    }
}
