/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.testrunner.ui;

import java.util.regex.Matcher;
import junit.framework.TestCase;
import org.netbeans.modules.gsf.testrunner.api.Trouble.ComparisonFailure;

/**
 * Unit test for the PyUnitHandler - adapted from the Test::Unit testcases by
 * Erno.
 * 
 * @author Erno Mononen
 * @author Tor Norbye
 */
public class PyUnitRecognizerTest extends TestCase {
    public void testTestStarted() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.TestStartedHandler();
        String output = "%TEST_STARTED% test_foo (TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("test_foo", matcher.group(1));
        assertEquals("TestFooBar", matcher.group(2));

        output = "%TEST_STARTED% test_foo (Foo::Bar::TestFooBar)";
        matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("test_foo", matcher.group(1));
        assertEquals("Foo::Bar::TestFooBar", matcher.group(2));
    }

    public void testTestFinished() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.TestFinishedHandler();
        String output = "%TEST_FINISHED% time=0.008765 test_foo (TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("0.008765", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestFooBar", matcher.group(3));

        output = "%TEST_FINISHED% time=0.008765 test_foo (FooModule::TestFooBar)";
        matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("0.008765", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("FooModule::TestFooBar", matcher.group(3));
    }

    public void testTestFinished2() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.TestFinishedHandler();
        String output = "%TEST_FINISHED% time=8.4e-05 test_foo (TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("8.4e-05", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestFooBar", matcher.group(3));
    }

    public void testTestFailed() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.TestFailedHandler();
        String output = "%TEST_FAILED% time=0.007233 testname=test_positive_price (ProductTest) message=<false> is not true. location=./test/unit/product_test.rb:69:in `test_positive_price'";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());

        assertEquals(5, matcher.groupCount());
        assertEquals("0.007233", matcher.group(1));
        assertEquals("test_positive_price", matcher.group(2));
        assertEquals("ProductTest", matcher.group(3));
        assertEquals("<false> is not true.", matcher.group(4));
        assertEquals("./test/unit/product_test.rb:69:in `test_positive_price'", matcher.group(5));

        String outputScientificNotation = "%TEST_FAILED% time=9.8e-07 testname=test_positive_price (ProductTest) message=<false> is not true. location=./test/unit/product_test.rb:69:in `test_positive_price'";
        matcher = handler.match(outputScientificNotation);
        assertTrue(matcher.matches());
        assertEquals("9.8e-07", matcher.group(1));

        // nested class name
        String outputNestedClass = "%TEST_FAILED% time=0.0060 testname=test_foo (TestSomething::TestNotExecuted) message=this test is not executed. location=/a/path/to/somewhere/test/test_something.rb:21:in `test_foo'";
        matcher = handler.match(outputNestedClass);
        assertTrue(matcher.matches());

        assertEquals(5, matcher.groupCount());
        assertEquals("0.0060", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestSomething::TestNotExecuted", matcher.group(3));
        assertEquals("this test is not executed.", matcher.group(4));
        assertEquals("/a/path/to/somewhere/test/test_something.rb:21:in `test_foo'", matcher.group(5));
    }


    /*
    FAen ta meg!
    %SUITE_STARTING% Other_TestCase
    Joda!
    %TEST_STARTED% test_probably_errs (other_test.Other_TestCase)
    %TEST_ERROR% time=0.000024 testname=test_probably_errs (other_test.Other_TestCase) message=integer division or modulo by zero location=run() in /System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/unittest.py:267%BR%test_probably_errs() in /Users/tor/NetBeansProjects/NewPythonProject44/src/other_test.py:27%BR%
    %TEST_STARTED% test_probably_fails (other_test.Other_TestCase)
    %TEST_FAILED% time=0.000028 testname=test_probably_fails (other_test.Other_TestCase) message=TODO: Write test location=run() in /System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/unittest.py:263%BR%test_probably_fails() in /Users/tor/NetBeansProjects/NewPythonProject44/src/other_test.py:24%BR%fail() in /System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/unittest.py:301%BR%
    %TEST_STARTED% test_should_succeed (other_test.Other_TestCase)
    %TEST_FINISHED% time=0.000013 test_should_succeed (other_test.Other_TestCase)
    %SUITE_FAILURES% 1
    %SUITE_ERRORS% 1
    %SUITE_FINISHED% time=0.0005


     */
    public void testTestError() {
        PyUnitHandlerFactory.TestErrorHandler handler = new PyUnitHandlerFactory.TestErrorHandler();


        String output = "%TEST_ERROR% time=0.000024 testname=test_probably_errs (other_test.Other_TestCase) message=integer division or modulo by zero location=run() in /System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/unittest.py:267%BR%test_probably_errs() in /Users/tor/NetBeansProjects/NewPythonProject44/src/other_test.py:27%BR%";

        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());

        assertEquals(5, matcher.groupCount());
        assertEquals("0.000024", matcher.group(1));
        assertEquals("test_probably_errs", matcher.group(2));
        assertEquals("other_test.Other_TestCase", matcher.group(3));
        assertEquals("integer division or modulo by zero", matcher.group(4));

        String[] stackTrace = PyUnitHandlerFactory.getStackTrace(matcher.group(4), matcher.group(5));
        assertEquals(3, stackTrace.length);
        assertEquals("integer division or modulo by zero", stackTrace[0]);
        assertEquals("test_probably_errs() in /Users/tor/NetBeansProjects/NewPythonProject44/src/other_test.py:27", stackTrace[2]);
        assertEquals("run() in /System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/unittest.py:267", stackTrace[1]);
    }

    public void testSuiteFinished() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.SuiteFinishedHandler();
        String output = "%SUITE_FINISHED% time=0.124";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());

        assertEquals(1, matcher.groupCount());
        assertEquals("0.124", matcher.group(1));
    }

    public void testSuiteFinished2() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.SuiteFinishedHandler();
        String output = "%SUITE_FINISHED% time=8.4e-05";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());

        assertEquals(1, matcher.groupCount());
        assertEquals("8.4e-05", matcher.group(1));
    }

    public void testSuiteStarted() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.SuiteStartedHandler();
        String output = "%SUITE_STARTED% 0 tests, 0 assertions, 0 failures, 0 errors";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
    }

    public void testSuiteStarting() throws InterruptedException {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.SuiteStartingHandler();
        String output = "%SUITE_STARTING% TestMe";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(1, matcher.groupCount());
        assertEquals("TestMe", matcher.group(1));

        output = "%SUITE_STARTING% MyModule::TestMe";
        matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(1, matcher.groupCount());
        assertEquals("MyModule::TestMe", matcher.group(1));
    }

    public void testSuiteErrorOutput() throws InterruptedException {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.SuiteErrorOutputHandler();
        String output = "%SUITE_ERROR_OUTPUT% error=undefined method `size' for UserHelperTest:Class";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(1, matcher.groupCount());
        assertEquals("undefined method `size' for UserHelperTest:Class", matcher.group(1));
    }

    public void testTestLogger() throws InterruptedException {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.TestLoggerHandler();
        String output = "%TEST_LOGGER% level=FINE msg=Loading 3 files took 12.345";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("FINE", matcher.group(1));
        assertEquals("Loading 3 files took 12.345", matcher.group(2));
    }

    public void testIssue143508TestStarted() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.TestStartedHandler();
        String output = "%TEST_STARTED% test_foo (FooTest)\\n";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("test_foo", matcher.group(1));
        assertEquals("FooTest", matcher.group(2));
    }

    public void testIssue143508TestFinished() {
        TestRecognizerHandler handler = new PyUnitHandlerFactory.TestFinishedHandler();
        String output = "%TEST_FINISHED% time=0.203 test_foo (FooTest)\\n";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("0.203", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("FooTest", matcher.group(3));
    }

    public void testExtractDiff1() {
        ComparisonFailure failure = PyUnitHandlerFactory.getComparisonFailure("'foo\nbar\nbaz' != 'foo\nbr\nbazz'");
        assertNotNull(failure);
        assertEquals("'foo\nbar\nbaz'", failure.getExpected());
        assertEquals("'foo\nbr\nbazz'", failure.getActual());
    }

    public void testExtractDiff2() {
        ComparisonFailure failure = PyUnitHandlerFactory.getComparisonFailure("Expected 265252859812191058636308480000000L but got 132626429906095529318154240000000L");
        assertNotNull(failure);
        assertEquals("265252859812191058636308480000000L", failure.getExpected());
        assertEquals("132626429906095529318154240000000L", failure.getActual());
    }
}
