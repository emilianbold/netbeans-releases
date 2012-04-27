/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.php.hints;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.php.GeneralPHP;

/**
 *
 * @author Vladimir Riha
 */
public class testHints extends GeneralPHP {

    static final String TEST_PHP_NAME = "PhpProject_hints_0001";

    public testHints(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testHints.class).addTest(
                "CreateApplication",
                "testImmutableVariable",
                "testImmutableVariableSimple",
                "testUnusedUse",
                "testClassExpr",
                "testBinaryNotationIncorrect",
                "testBinaryNotationCorrect",
                "testShortArraySyntax",
                "testPhp54RelatedHint",
                "testPhp53RelatedHint").enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    public void CreateApplication() {
        startTest();
        CreatePHPApplicationInternal(TEST_PHP_NAME);
        endTest();
    }

    public void CreatePHPFile() {
        startTest();
        SetAspTags(TEST_PHP_NAME, true);
        CreatePHPFile(TEST_PHP_NAME, "PHP File", null);
        endTest();
    }

    public void testClassExpr() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Class");
        startTest();
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n class Test{ \n public static function test(){\n");
        file.setCaretPosition("?>", true);
        TypeCode(file, "Bar::{'test'}();");
        new EventTool().waitNoEvent(1000);
        Object[] oo = file.getAnnotations();
        for (Object o : oo) {
            assertTrue("Error annotation showed", !EditorOperator.getAnnotationType(o).toString().endsWith("_err"));
        }
        endTest();
    }

    public void testBinaryNotationIncorrect() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n $wrongBinary=0b002;");
        new EventTool().waitNoEvent(1000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationShortDescription(o).toString().contains("Syntax error: unexpected: 2")) {
                numberOfErrors++;
            }
        }
        assertEquals("Incorrect number of error hints", 1, numberOfErrors);
        endTest();
    }

    public void testBinaryNotationCorrect() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.replace("0b002", "0b001");
        new EventTool().waitNoEvent(1000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationShortDescription(o).toString().contains("Syntax error: unexpected: 2")) {
                numberOfErrors++;
            }
        }
        assertEquals("Incorrect number of error hints", 0, numberOfErrors);
        endTest();
    }

    public void testShortArraySyntax() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n $arr = [0 => \"Foo\"];");
        new EventTool().waitNoEvent(1000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationShortDescription(o).toString().contains("error") && !EditorOperator.getAnnotationShortDescription(o).toString().contains("HTML error checking")) {
                numberOfErrors++;
            }
        }
        assertEquals("Incorrect number of error hints", 0, numberOfErrors);
        endTest();
    }

    public void testPhp54RelatedHint() {
        startTest();
        SetPhpVersion(TEST_PHP_NAME, 3);
        EditorOperator file = new EditorOperator("Class.php");
        new EventTool().waitNoEvent(1000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationType(o).toString().endsWith("_err")) {
                numberOfErrors++;
            }
        }
        assertEquals("Incorrect number of error hints", 2, numberOfErrors);
        endTest();
    }

    public void testPhp53RelatedHint() {
        startTest();
        EditorOperator file = new EditorOperator("Class.php");
        file.setCaretPosition("*/", false);
        TypeCode(file, "\n namespace test;");
        SetPhpVersion(TEST_PHP_NAME, 2);
        new EventTool().waitNoEvent(1000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationType(o).toString().endsWith("_err")) {
                numberOfErrors++;
            }
        }
        assertEquals("Incorrect number of error hints", 3, numberOfErrors);
        endTest();
    }

    public void testImmutableVariableSimple() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Immutable");
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n $foo=1;\n $foo=2;");
        new EventTool().waitNoEvent(2000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationShortDescription(o).toString().contains("Too many assignments")) {
                numberOfErrors++;
            }
            assertEquals("Incorrect number of Immutable hints", 2, numberOfErrors);
        }
        endTest();
    }

    public void testImmutableVariable() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "Immutable2");
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n for($i=0;$i<10;$i=$i+1){}");
        new EventTool().waitNoEvent(1000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationShortDescription(o).toString().contains("Too many assignments")) {
                numberOfErrors++;
            }
            assertEquals("Incorrect number of Immutable hints", 0, numberOfErrors);
        }
        endTest();
    }

    public void testUnusedUse() {
        EditorOperator file = CreatePHPFile(TEST_PHP_NAME, "PHP File", "UnusedUse");
        SetPhpVersion(TEST_PHP_NAME, 4);
        startTest();
        file.setCaretPosition("*/", false);
        new EventTool().waitNoEvent(1000);
        TypeCode(file, "\n use \\Foo\\Bar\\Baz;");
        new EventTool().waitNoEvent(2000);
        Object[] oo = file.getAnnotations();
        int numberOfErrors = 0;
        for (Object o : oo) {
            if (EditorOperator.getAnnotationShortDescription(o).toString().contains("Unused Use Statement")) {
                numberOfErrors++;
            }
            assertEquals("Incorrect number of Unused Use Statement hints", 1, numberOfErrors);
        }
        endTest();
    }
}
