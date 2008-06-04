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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.net.URL;
import java_cup.runtime.Symbol;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.editor.lexer.PHPLexerUtils;
import org.netbeans.modules.php.editor.parser.ASTPHP5Symbols;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class ASTPHP5ParserTest extends ParserTestBase {
    
    public ASTPHP5ParserTest(String testName) {
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
    
//    public void testPHPDoc1() throws Exception {
//        String source = "<?php\n/**\n * PHP Template.\n */\necho \"ahoj\"\n?>";
//        System.out.println("-----------------start: ------------------");
//        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(source));
//        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
//        Program result = (Program)parser.parse().value;
//        
//        System.out.println((new PrintASTVisitor()).printTree(result));
//        System.out.println("-----------------end: ------------------\n\n\n");
//    }
    
    public void xtestPHPError1() throws Exception {
        String source = "<?php\npublic class User {\n  \n}\n?>";
        System.out.println("-----------------start: ------------------");
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(source));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        if (root != null){
            Program result = (Program)root.value;
        
            System.out.println((new PrintASTVisitor()).printTree(result));
        }
        System.out.println("-----------------end: ------------------\n\n\n");
    }
    
//    public void testParser01 () throws Exception {
//        File testFile = new File(getDataDir(), "testfiles/TextSearchQuery.php");
//        assertTrue(testFile.exists());
//        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new FileReader(testFile));
//        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
//        Symbol root = parser.parse();
//    }
//    
//    public void testParserUnfinishedPHPDoc () throws Exception {
//        File testFile = new File(getDataDir(), "testfiles/test01.php");
//        assertTrue(testFile.exists());
//        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new FileReader(testFile));
//        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
//        Symbol root = parser.parse();
//    }
    
    public void testNowdoc () throws Exception {
        performFileParserTest("nowdoc_000");
        performFileParserTest("nowdoc_001");
        performFileParserTest("nowdoc_002");
        performFileParserTest("nowdoc_003");
        performFileParserTest("nowdoc_004");
        performFileParserTest("nowdoc_005");
        performFileParserTest("nowdoc_006");
        performFileParserTest("nowdoc_007");
        performFileParserTest("nowdoc_008");
        performFileParserTest("nowdoc_009");
        performFileParserTest("nowdoc_010");
        performFileParserTest("nowdoc_011");
        performFileParserTest("nowdoc_012");
        performFileParserTest("nowdoc_013");
        performFileParserTest("nowdoc_014");
        performFileParserTest("nowdoc_015");
    }
    
}
