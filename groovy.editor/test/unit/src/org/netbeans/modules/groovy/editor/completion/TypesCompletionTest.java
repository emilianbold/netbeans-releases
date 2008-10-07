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

package org.netbeans.modules.groovy.editor.completion;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schmidtm
 */
public class TypesCompletionTest extends GroovyTestBase {

    String TYPES_BASE = "testfiles/completion/types/";

    public TypesCompletionTest(String testName) {
        super(testName);
        Logger.getLogger(CodeCompleter.class.getName()).setLevel(Level.FINEST);
    }

    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    // Type completion


    // we don't get proper AST for this mini-class, disable it for now.
//    public void testTypeCompletion1() throws Exception {
//        checkCompletion(TYPES_BASE + "" + "TypeCompletion1.groovy", "class Bar { ^}", false);
//        // assertTrue(false);
//    }

    public void testTypeCompletion2() throws Exception {
        checkCompletion(TYPES_BASE + "" + "TypeCompletion2.groovy", "class Pre { Cl^ }", false);
        // assertTrue(false);
    }

    public void testTypeCompletion3() throws Exception {
        checkCompletion(TYPES_BASE + "" + "TypeCompletion3.groovy", "    Cl^ }", false);
        // assertTrue(false);
    }

    public void testTypeCompletion4() throws Exception {
        checkCompletion(TYPES_BASE + "" + "TypeCompletion4.groovy", "class Pre { Cl^", false);
        // assertTrue(false);
    }

    public void testTypeCompletion5() throws Exception {
        checkCompletion(TYPES_BASE + "" + "TypeCompletion5.groovy", "    No^", false);
        // assertTrue(false);
    }

    // test manually imported Types

    public void testManualImport1() throws Exception {
        checkCompletion(TYPES_BASE + "" + "ManualImport1.groovy", "println Sign^", false);
    }  

    public void testManualImport2() throws Exception {
        checkCompletion(TYPES_BASE + "" + "ManualImport2.groovy", "println Can^", false);
    }

    // testing the various default imports

    public void testDefaultImport1() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "FileRea^", false);
    }

    public void testDefaultImport2() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "ClassCastExc^", false);
    }

    public void testDefaultImport3() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "BigDec^", false);
    }

    public void testDefaultImport4() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "BigInte^", false);
    }

    public void testDefaultImport5() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "HttpU^", false);
    }

    public void testDefaultImport6() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "Scan^", false);
    }

    // make sure, we don't complete in comments.

    // not in block comments.

    public void testNotInComments1() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "Groovy def^", false);
    }

    // ... and also not in line comments.

    public void testNotInComments2() throws Exception {
        checkCompletion(TYPES_BASE + "" + "DefaultImport1.groovy", "java.lang.ClassCastException^", false);
    }



    // testing wildcard-imports

//    public void testWildCardImport1() throws Exception {
//        checkCompletion(TYPES_BASE + "" + "WildCardImport1.groovy", "new Mark^", false);
//    }


    // test for types defined in the very same file

    public void testSamePackage1() throws Exception {
        checkCompletion(TYPES_BASE + "" + "SamePackage1.groovy", "println TestSamePack^", false);
    }

    // test if interfaces and only interfaces are proposed:
    
    public void testInterfaceCompletion1() throws Exception {
        checkCompletion(TYPES_BASE + "" + "InterfaceCompletion1.groovy", "class SpecialGroovyClass implements ^Runnable, Serializable {", false);
    }

    public void testInterfaceCompletion2() throws Exception {
        checkCompletion(TYPES_BASE + "" + "InterfaceCompletion1.groovy", "class SpecialGroovyClass implements R^unnable, Serializable {", false);
    }

    public void testInterfaceCompletion3() throws Exception {
        checkCompletion(TYPES_BASE + "" + "InterfaceCompletion1.groovy", "class SpecialGroovyClass implements Runn^able, Serializable {", false);
    }

    public void testInterfaceCompletion4() throws Exception {
        checkCompletion(TYPES_BASE + "" + "InterfaceCompletion1.groovy", "class SpecialGroovyClass implements Runnable, Ser^ializable {", false);
    }

    public void testInterfaceCompletion5() throws Exception {
        checkCompletion(TYPES_BASE + "" + "InterfaceCompletion1.groovy", "class SpecialGroovyClass implements Runnable, Se^rializable {", false);
    }

    // FIXME this works in the IDE, but due to some isPackageValid magic
    // and perhaps due to index stuff this does not work in tests
//    public void testFqnTypeCompletion1() throws Exception {
//        checkCompletion(TYPES_BASE + "" + "FqnTypeCompletion1.groovy", "groovy.xml.^", false);
//    }
}
