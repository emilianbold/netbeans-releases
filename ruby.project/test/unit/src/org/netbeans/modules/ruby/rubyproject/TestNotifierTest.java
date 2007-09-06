/*
 * TestNotifierTest.java
 * JUnit based test
 *
 * Created on August 30, 2007, 8:36 AM
 */

package org.netbeans.modules.ruby.rubyproject;

import org.netbeans.modules.ruby.RubyTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class TestNotifierTest extends RubyTestBase {
    
    public TestNotifierTest(String testName) {
        super(testName);
    }

    public void testUnit() {
        TestNotifier notifier = new TestNotifier(false, false);
        
        assertTrue(notifier.recognizeLine("35 tests, 81 assertions, 0 failures, 1 errors"));
    }

    public void testRSpec() {
        TestNotifier notifier = new TestNotifier(false, false);
        
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 not implemented")); // older rspec format
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 pending"));
        assertTrue(notifier.recognizeLine("1 example, 1 failure"));
    }

    public void testWindows() {        
        TestNotifier notifier = new TestNotifier(false, false);

        assertTrue(notifier.recognizeLine("35 tests, 81 assertions, 0 failures, 1 errors\r"));
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 not implemented\r"));
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 pending\r"));
        assertTrue(notifier.recognizeLine("1 example, 1 failure\r"));
    }
    
    public void testNoFalseNegatives() {
        TestNotifier notifier = new TestNotifier(false, false);
        
        assertFalse(notifier.recognizeLine("1 for example, 1 failure"));
        assertFalse(notifier.recognizeLine("hello world"));
        assertFalse(notifier.recognizeLine("1"));
        assertFalse(notifier.recognizeLine("NoMethodError: You have a nil object when you didn't expect it!"));
        assertFalse(notifier.recognizeLine(".......E..........................."));
        assertFalse(notifier.recognizeLine("   C:/InstantRails/rails_apps/rfs/test/unit/rest_phone/phone_action/phone_action_subtypes_test.rb:182:in `test_event_response'"));
    }
    
    public void testAccumulate1() {
        TestNotifier notifier = new TestNotifier(true, false);
        notifier.processLine("35 tests, 81 assertions, 0 failures, 0 errors");
        notifier.processLine("10 tests, 1 assertions, 0 failures, 0 errors\r");
        notifier.processLine("1 tests, 0 assertions, 0 failures, 0 errors");
        assertEquals("46 tests, 82 assertions, 0 failures, 0 errors", notifier.getSummary());
        assertTrue(!notifier.isError() && !notifier.isWarning());
    }

    public void testAccumulate2() {
        TestNotifier notifier = new TestNotifier(true, false);
        notifier.processLine("35 tests, 81 assertions, 0 failures, 1 errors");
        notifier.processLine("10 tests, 1 assertions, 5 failures, 1 errors\r");
        notifier.processLine("1 tests, 0 assertions, 0 failures, 0 errors");
        assertEquals("46 tests, 82 assertions, 5 failures, 2 errors", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRSpec1() {  
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("5 examples, 3 failures, 5 not implemented");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("6 examples, 4 failures, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRSpec1b() {  
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("5 examples, 3 failures, 5 pending");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("6 examples, 4 failures, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }
    
    public void testRSpec2() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures, 5 not implemented");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("1 example, 1 failure, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRSpec2b() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures, 5 pending");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("1 example, 1 failure, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRSpec3() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures");
        notifier.processLine("0 examples, 0 failures");
        assertEquals("0 examples, 0 failures", notifier.getSummary());
        assertTrue(!notifier.isError() && !notifier.isWarning());
    }

    public void testRSpec4AnsiColors() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("\033[1;35m2 examples, 0 failures, 5 not implemented\033[0m");
        notifier.processLine("\033[32m1 example, 1 failure\033[0m\r");
        assertEquals("3 examples, 1 failure, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRSpec4AnsiColors2() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("\033[1;35m2 examples, 0 failures, 5 pending\033[0m");
        notifier.processLine("\033[32m1 example, 1 failure\033[0m\r");
        assertEquals("3 examples, 1 failure, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRSpec5() {  
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("5 examples, 0 failures, 5 not implemented");
        notifier.processLine("1 example, 0 failures, 1 pending\r");
        assertEquals("6 examples, 0 failures, 6 pending", notifier.getSummary());
        assertTrue(notifier.isWarning() && !notifier.isError());
    }

    public void testCombined() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures, 5 not implemented");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("1 example, 1 failure, 5 pending", notifier.getSummary());
        notifier.processLine("1 tests, 1 assertions, 1 failures, 1 errors");
        assertEquals("1 test, 1 assertion, 1 example, 2 failures, 1 error, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }
    
    public void testCombined2() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures, 5 pending");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("1 example, 1 failure, 5 pending", notifier.getSummary());
        notifier.processLine("1 tests, 1 assertions, 1 failures, 1 errors");
        assertEquals("1 test, 1 assertion, 1 example, 2 failures, 1 error, 5 pending", notifier.getSummary());
        assertTrue(notifier.isError());
    }
    
    public void testNoAccumulate() {
        TestNotifier notifier = new TestNotifier(false, false);
        notifier.processLine("35 tests, 81 assertions, 0 failures, 0 errors");
        assertEquals("35 tests, 81 assertions, 0 failures, 0 errors", notifier.getSummary());
        assertTrue(!notifier.isError() && !notifier.isWarning());
        notifier.processLine("10 tests, 1 assertions, 0 failures, 0 errors\r");
        assertEquals("10 tests, 1 assertion, 0 failures, 0 errors", notifier.getSummary());
        assertTrue(!notifier.isError() && !notifier.isWarning());
        notifier.processLine("35 tests, 81 assertions, 0 failures, 1 errors");
        assertEquals("35 tests, 81 assertions, 0 failures, 1 error", notifier.getSummary());
        assertTrue(notifier.isError());
        notifier.processLine("10 tests, 1 assertions, 0 failures, 0 errors\r");
        assertEquals("10 tests, 1 assertion, 0 failures, 0 errors", notifier.getSummary());
        assertTrue(!notifier.isError() && !notifier.isWarning());
    }
    
    public void testRake() {
        TestNotifier notifier = new TestNotifier(true, false);
        notifier.processLine("Test failures");
        assertEquals("1 error", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRake2() {
        TestNotifier notifier = new TestNotifier(true, false);
        notifier.processLine("1 tests, 1 assertions, 1 failures, 1 errors");
        notifier.processLine("Test failures");
        assertEquals("1 test, 1 assertion, 1 failure, 2 errors", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRake2Windows() {
        TestNotifier notifier = new TestNotifier(true, false);
        notifier.processLine("1 tests, 1 assertions, 1 failures, 1 errors");
        notifier.processLine("Test failures\r");
        assertEquals("1 test, 1 assertion, 1 failure, 2 errors", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRake3() {
        TestNotifier notifier = new TestNotifier(false, false);
        notifier.processLine("Test failures");
        assertEquals("1 error", notifier.getSummary());
        assertTrue(notifier.isError());
    }

    public void testRake4() {
        TestNotifier notifier = new TestNotifier(true, false);
        notifier.processLine("No Test failures");
        // Make sure we don't pick up "Test failures" as just a substring
        assertEquals("0 failures", notifier.getSummary());
        assertTrue(!notifier.isError() && !notifier.isWarning());
    }

    public void testOutputLog1() throws Exception {
        TestNotifier notifier = new TestNotifier(true, false);
        FileObject fileObject = getTestFile("testfiles/testoutput.txt");
        String s = readFile(fileObject);
        String[] lines = s.split("\n");
        for (String line : lines) {
            notifier.processLine(line);
        }
        assertEquals("313 tests, 504 assertions, 1 failure, 0 errors", notifier.getSummary());
        assertTrue(notifier.isError());
    }
}
