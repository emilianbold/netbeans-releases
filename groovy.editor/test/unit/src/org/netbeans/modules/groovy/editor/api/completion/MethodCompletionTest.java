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

package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author schmidtm
 */
import org.netbeans.modules.groovy.editor.api.completion.GroovyCompletionHandler;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schmidtm
 */
public class MethodCompletionTest extends GroovyTestBase {

    String TEST_BASE = "testfiles/completion/method/";

    public MethodCompletionTest(String testName) {
        super(testName);
        Logger.getLogger(GroovyCompletionHandler.class.getName()).setLevel(Level.FINEST);
    }

    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    public void testMethods1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods1.groovy", "        new URL(\"http://google.com\").getPr^", false);
    }

    public void testMethods2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods1.groovy", "        new URL(\"http://google.com\").getP^r", false);
    }

    public void testMethods3() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods1.groovy", "        new URL(\"http://google.com\").get^Pr", false);
    }

    public void testMethods4() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods1.groovy", "        new URL(\"http://google.com\").^getPr", false);
    }

    public void testMethods5() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods2.groovy", "        new Byte().^", false);
    }
    
    public void testCompletionInMethodCall1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods4.groovy", "        new File(\"something\").ea^", false);
    }

    public void testCompletionInMethodCall2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods5.groovy", "        new File(\"something\").c^", false);
    }

    public void testCompletionInMethodCall3() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods6.groovy", "if (new File(\"/\").is^) {", false);
    }

    public void testCompletionInArgument1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods7.groovy", "println new URL(\"http://google.com\").getT^", false);
    }

    public void testCompletionForLiteral1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods8.groovy", "1.d^", false);
    }

    public void testCompletionForLiteral2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods8.groovy", "1.0.d^", false);
    }

    public void testCompletionForLiteral3() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods8.groovy", "\"\".c^", false);
    }

    public void testCompletionInsideConstructor1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods9.groovy", "new File(\"/\").equals(new Date().a^", false);
    }

    public void testCompletionInsideConstructor2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods9.groovy", "new File(new Date().get^", false);
    }

    public void testCompletionInsideConstructor3() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods9.groovy", "if (new File(new Date().get^", false);
    }
    
    public void testCompletionInsideFor1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods9.groovy", "for(new Date().get^", false);
    }

    public void testCompletionNoPrefixString1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods10.groovy", "println \"Hello $name!\".^", false);
    }
    
    public void testCompletionNoPrefixString2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods11.groovy", "def name='Petr'.^", false);
    }    

    // FIXME this test randomly fails
    public void testCompletionInsideFor2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods9.groovy", "for (String other in [1:\"Alice\", 2:\"Bob\"].^", false);
    }

    public void testCompletionGeneratedAccessors1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods12.groovy", "        new Test().get^", false);
    }

    public void testCompletionGeneratedAccessors2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods12.groovy", "        new Test().set^", false);
    }

    public void testCompletionGroovyClass1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods13.groovy", "        new Test1().^", false);
    }

    public void testCompletionGroovyThis1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods14.groovy", "        this.get^", false);
    }
    
    public void testCompletionGroovySuper1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Methods15.groovy", "        super.^", false);
    }
}

