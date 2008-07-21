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

import java.util.regex.Matcher;
import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class TestUnitRecognizerTest extends TestCase {
    
    public void testTestStarted() {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.TestStartedHandler();
        String output = "%TEST_STARTED% test_foo(TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("test_foo", matcher.group(1));
        assertEquals("TestFooBar", matcher.group(2));

        output = "%TEST_STARTED% test_foo(Foo::Bar::TestFooBar)";
        matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("test_foo", matcher.group(1));
        assertEquals("Foo::Bar::TestFooBar", matcher.group(2));
    }

    public void testTestFinished() {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.TestFinishedHandler();
        String output = "%TEST_FINISHED% time=0.008765 test_foo(TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("0.008765", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestFooBar", matcher.group(3));

        output = "%TEST_FINISHED% time=0.008765 test_foo(FooModule::TestFooBar)";
        matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("0.008765", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("FooModule::TestFooBar", matcher.group(3));
    }

    public void testTestFinished2() {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.TestFinishedHandler();
        String output = "%TEST_FINISHED% time=8.4e-05 test_foo(TestFooBar)";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(3, matcher.groupCount());
        assertEquals("8.4e-05", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestFooBar", matcher.group(3));
    }

    public void testTestFailed() {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.TestFailedHandler();
        String output = "%TEST_FAILED% time=0.007233 testname=test_positive_price(ProductTest) message=<false> is not true. location=./test/unit/product_test.rb:69:in `test_positive_price'";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        
        assertEquals(5, matcher.groupCount());
        assertEquals("0.007233", matcher.group(1));
        assertEquals("test_positive_price", matcher.group(2));
        assertEquals("ProductTest", matcher.group(3));
        assertEquals("<false> is not true.", matcher.group(4));
        assertEquals("./test/unit/product_test.rb:69:in `test_positive_price'", matcher.group(5));
        
        String outputScientificNotation = "%TEST_FAILED% time=9.8e-07 testname=test_positive_price(ProductTest) message=<false> is not true. location=./test/unit/product_test.rb:69:in `test_positive_price'";
        matcher = handler.match(outputScientificNotation);
        assertTrue(matcher.matches());
        assertEquals("9.8e-07", matcher.group(1));

        // nested class name
        String outputNestedClass = "%TEST_FAILED% time=0.0060 testname=test_foo(TestSomething::TestNotExecuted) message=this test is not executed. location=/a/path/to/somewhere/test/test_something.rb:21:in `test_foo'";
        matcher = handler.match(outputNestedClass);
        assertTrue(matcher.matches());

        assertEquals(5, matcher.groupCount());
        assertEquals("0.0060", matcher.group(1));
        assertEquals("test_foo", matcher.group(2));
        assertEquals("TestSomething::TestNotExecuted", matcher.group(3));
        assertEquals("this test is not executed.", matcher.group(4));
        assertEquals("/a/path/to/somewhere/test/test_something.rb:21:in `test_foo'", matcher.group(5));
    }

    public void testTestError() {
        TestUnitHandlerFactory.TestErrorHandler handler = new TestUnitHandlerFactory.TestErrorHandler();
        String output = "%TEST_ERROR% time=0.000883 testname=test_two_people_buying(DslUserStoriesTest) " +
                "message=StandardError: No fixture with name 'ruby_book' found for table 'products' " +
                "location=/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:894:in `products'%BR%" +
                "/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:888:in `map'%BR%" +
                "/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:888:in `products'%BR%" +
                "./test/integration/dsl_user_stories_test.rb:55:in `setup_without_fixtures'%BR%" +
                "/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:979:in `full_setup'%BR%" +
                "/usr/lib/ruby/1.8/test/unit/testcase.rb:77:in `setup'%BR%" +
                "/usr/lib/ruby/1.8/test/unit/testcase.rb:77:in `run'%BR%" +
                "/usr/lib/ruby/gems/1.8/gems/actionpack-2.0.2/lib/action_controller/integration.rb:547:in `run'%BR%" +
                "/usr/lib/ruby/1.8/test/unit/testsuite.rb:34:in `run'%BR%" +
                "/usr/lib/ruby/1.8/test/unit/testsuite.rb:33:in `each'%BR%" +
                "/usr/lib/ruby/1.8/test/unit/testsuite.rb:33:in `run'%BR%" +
                "/usr/lib/ruby/1.8/test/unit/ui/testrunnermediator.rb:46:in `run_suite'%BR%" +
                "/home/erno/work/elohopea/main-vara/ruby.testrunner/release/nb_test_mediator.rb:145:in `run_mediator'%BR%" +
                "/home/erno/work/elohopea/main-vara/ruby.testrunner/release/nb_test_mediator.rb:140:in `each'%BR%" +
                "/home/erno/work/elohopea/main-vara/ruby.testrunner/release/nb_test_mediator.rb:140:in `run_mediator'%BR%" +
                "/home/erno/work/elohopea/main-vara/ruby.testrunner/release/nb_test_mediator.rb:206";

        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        
        assertEquals(5, matcher.groupCount());
        assertEquals("0.000883", matcher.group(1));
        assertEquals("test_two_people_buying", matcher.group(2));
        assertEquals("DslUserStoriesTest", matcher.group(3));
        assertEquals("StandardError: No fixture with name 'ruby_book' found for table 'products'", matcher.group(4));
        assertEquals("StandardError: No fixture with name 'ruby_book' found for table 'products'", matcher.group(4));
        
        String[] stackTrace = TestUnitHandlerFactory.getStackTrace(matcher.group(4), matcher.group(5));
        assertEquals(13, stackTrace.length);
        assertEquals("StandardError: No fixture with name 'ruby_book' found for table 'products'", stackTrace[0]);
        assertEquals("/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:888:in `map'", stackTrace[2]);
        assertEquals("/usr/lib/ruby/gems/1.8/gems/actionpack-2.0.2/lib/action_controller/integration.rb:547:in `run'", stackTrace[8]);

        String outputScientificNotation = "%TEST_ERROR% time=1.2e-34 testname=test_two_people_buying(DslUserStoriesTest) " +
                "message=StandardError: No fixture with name 'ruby_book' found for table 'products' " +
                "location=/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:894:in `products'%BR%" +
                "/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:888:in `map'%BR%";

        matcher = handler.match(outputScientificNotation);
        assertTrue(matcher.matches());
        assertEquals("1.2e-34", matcher.group(1));

        String outputNestedClass = "%TEST_ERROR% time=1.2e-34 testname=test_two_people_buying(Some::Another::DslUserStoriesTest) " +
                "message=StandardError: No fixture with name 'ruby_book' found for table 'products' " +
                "location=/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:894:in `products'%BR%" +
                "/usr/lib/ruby/gems/1.8/gems/activerecord-2.0.2/lib/active_record/fixtures.rb:888:in `map'%BR%";

        matcher = handler.match(outputNestedClass);
        assertTrue(matcher.matches());
        assertEquals("Some::Another::DslUserStoriesTest", matcher.group(3));

    }
    
    public void testSuiteFinished() {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.SuiteFinishedHandler();
        String output = "%SUITE_FINISHED% time=0.124";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        
        assertEquals(1, matcher.groupCount());
        assertEquals("0.124", matcher.group(1));
    }
    
    public void testSuiteFinished2() {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.SuiteFinishedHandler();
        String output = "%SUITE_FINISHED% time=8.4e-05";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        
        assertEquals(1, matcher.groupCount());
        assertEquals("8.4e-05", matcher.group(1));
    }
    
    public void testSuiteStarted() {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.SuiteStartedHandler();
        String output = "%SUITE_STARTED% 0 tests, 0 assertions, 0 failures, 0 errors";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
    }
    
    public void testSuiteStarting() throws InterruptedException {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.SuiteStartingHandler();
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
        TestRecognizerHandler handler = new TestUnitHandlerFactory.SuiteErrorOutputHandler();
        String output = "%SUITE_ERROR_OUTPUT% error=undefined method `size' for UserHelperTest:Class";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(1, matcher.groupCount());
        assertEquals("undefined method `size' for UserHelperTest:Class", matcher.group(1));
    }

    public void testTestLogger() throws InterruptedException {
        TestRecognizerHandler handler = new TestUnitHandlerFactory.TestLoggerHandler();
        String output = "%TEST_LOGGER% level=FINE msg=Loading 3 files took 12.345";
        Matcher matcher = handler.match(output);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("FINE", matcher.group(1));
        assertEquals("Loading 3 files took 12.345", matcher.group(2));
    }

}
