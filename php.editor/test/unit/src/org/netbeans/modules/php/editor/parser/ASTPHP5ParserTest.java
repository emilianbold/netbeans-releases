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

import java.io.StringReader;
import java_cup.runtime.Symbol;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.editor.parser.ASTPHP5Symbols;
import org.netbeans.modules.php.editor.parser.astnodes.Program;

/**
 *
 * @author Petr Pisl
 */
public class ASTPHP5ParserTest extends NbTestCase {
    
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
    
    public void testPHPError1() throws Exception {
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
    
}
