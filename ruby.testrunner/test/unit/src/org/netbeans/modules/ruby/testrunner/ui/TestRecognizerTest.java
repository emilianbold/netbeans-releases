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

package org.netbeans.modules.ruby.testrunner.ui;

import org.netbeans.modules.ruby.testrunner.ui.TestRecognizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class TestRecognizerTest extends TestCase {
    
    public void testTestStarted() {
        TestRecognizer.TestHandler handler = new TestRecognizer.TestStartedHandler();
        String output = "%TEST_STARTED% test_foo(TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("test_foo", matcher.group(1));
        assertEquals("TestFooBar", matcher.group(2));
    }

    public void testTestFinished() {
        TestRecognizer.TestHandler handler = new TestRecognizer.TestFinishedHandler();
        String output = "%TEST_FINISHED% time=0.008765 test_foo(TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("0.008765", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestFooBar", matcher.group(3));
    }

    public void testTestFailed() {
        TestRecognizer.TestHandler handler = new TestRecognizer.TestFailedHandler();
        String output = "%TEST_FAILED% time=0.9981 Failure:test_foo(TestFooBar) [/a/path/to/somewhere/file.rb:17:]:  \"failed\"";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        
        assertEquals(4, matcher.groupCount());
        assertEquals("0.9981", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestFooBar", matcher.group(3));
    }
    
    public void testTestError() {
        TestRecognizer.TestHandler handler = new TestRecognizer.TestErrorHandler();
        String output = "%TEST_ERROR% time=0.01220 Error:test_foo(TestFooBar): RuntimeError: error ";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        
        assertEquals(4, matcher.groupCount());
        assertEquals("0.01220", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestFooBar", matcher.group(3));
    }
    
    public void testSuiteFinished() {
        TestRecognizer.TestHandler handler = new TestRecognizer.SuiteFinishedHandler();
        String output = "%SUITE_FINISHED% 0.124";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        
        assertEquals(1, matcher.groupCount());
        assertEquals("0.124", matcher.group(1));
    }
    
    public void testSuiteStarted() {
        TestRecognizer.TestHandler handler = new TestRecognizer.SuiteStartedHandler();
        String output = "%SUITE_STARTED% 0 tests, 0 assertions, 0 failures, 0 errors";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
    }
    
    public void testSuiteStarting() throws InterruptedException {
        TestRecognizer.TestHandler handler = new TestRecognizer.SuiteStartingHandler();
        String output = "%SUITE_STARTING% TestMe";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(1, matcher.groupCount());
        assertEquals("TestMe", matcher.group(1));
    }

}
