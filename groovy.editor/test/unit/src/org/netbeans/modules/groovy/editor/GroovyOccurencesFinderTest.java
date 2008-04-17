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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.groovy.editor.parser.GroovyOccurrencesFinder;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.modules.gsf.api.OccurrencesFinder;

/**
 *
 * @author Martin Adamek
 */
public class GroovyOccurencesFinderTest extends GroovyTestBase {

    public GroovyOccurencesFinderTest(String testName) {
        super(testName);
    }

    @Override
    protected OccurrencesFinder getOccurrencesFinder() {
        return new GroovyOccurrencesFinder();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Logger.getLogger(PathFinderVisitor.class.getName()).setLevel(Level.FINEST);
    }

    // uncomment this to have logging from GroovyLexer
//    protected Level logLevel() {
//        // enabling logging
//        return Level.INFO;
//        // we are only interested in a single logger, so we set its level in setUp(),
//        // as returning Level.FINEST here would log from all loggers
//    }

    public void testParams() throws Exception {
        String caretLine = "        par^ams.each {";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testUnusedParams() throws Exception {
        String caretLine = "    private printParams(params, unus^edParam) {";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }

    public void testClassVariable() throws Exception {
        String caretLine = "    Map par^ams = [:]";
        checkOccurrences("testfiles/BookmarkController.groovy", caretLine, true);
    }
    
    /* now test some stuff from GroovyScopeTestcase.groovy */
    
    String TEST_FILE = "testfiles/GroovyScopeTestcase.groovy";
    
    private void doTest(String caretLine) throws Exception {
        checkOccurrences(TEST_FILE, caretLine, true);
    }
    
    public void testMethod1() throws Exception {
        doTest("new TestCase().met^hod1(1)");
    }  

    public void testClass2() throws Exception {
        doTest("        new Test^Case()");
    }    

    public void testLocalVar3() throws Exception {
        doTest("        int local^var1 = 3");
    }    

    public void testMemberVar4() throws Exception {
        doTest("    int member^var1 = 2");
    }
    
    public void testParameter5() throws Exception {
        doTest("        def localvar3 = membervar1 + par^am1 + localvar1 + localvar2");
    }
    
}
