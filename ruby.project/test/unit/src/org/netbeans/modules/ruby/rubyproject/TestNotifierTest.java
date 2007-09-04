/*
 * TestNotifierTest.java
 * JUnit based test
 *
 * Created on August 30, 2007, 8:36 AM
 */

package org.netbeans.modules.ruby.rubyproject;

import junit.framework.TestCase;

/**
 *
 * @author Tor Norbye
 */
public class TestNotifierTest extends TestCase {
    
    public TestNotifierTest(String testName) {
        super(testName);
    }

    public void testUnit() {
        TestNotifier notifier = new TestNotifier(false, false);
        
        assertTrue(notifier.recognizeLine("35 tests, 81 assertions, 0 failures, 1 errors"));
    }

    public void testRSpec() {
        TestNotifier notifier = new TestNotifier(false, false);
        
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 not implemented"));
        assertTrue(notifier.recognizeLine("1 example, 1 failure"));
    }

    public void testStripAnsiColors() {
        assertEquals("3 examples, 0 failures", TestNotifier.stripAnsiColors("\033[32m3 examples, 0 failures\033[0m"));
        assertEquals("3 examples, 0 failures", TestNotifier.stripAnsiColors("\033[1;35m3 examples, 0 failures\033[0m"));
    }
    
    public void testWindows() {        
        TestNotifier notifier = new TestNotifier(false, false);

        assertTrue(notifier.recognizeLine("35 tests, 81 assertions, 0 failures, 1 errors\r"));
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 not implemented\r"));
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
    }

    public void testAccumulate2() {
        TestNotifier notifier = new TestNotifier(true, false);
        notifier.processLine("35 tests, 81 assertions, 0 failures, 1 errors");
        notifier.processLine("10 tests, 1 assertions, 5 failures, 1 errors\r");
        notifier.processLine("1 tests, 0 assertions, 0 failures, 0 errors");
        assertEquals("46 tests, 82 assertions, 5 failures, 2 errors", notifier.getSummary());
    }

    public void testRSpec1() {  
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("5 examples, 3 failures, 5 not implemented");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("6 examples, 4 failures, 5 not implemented", notifier.getSummary());
    }

    public void testRSpec2() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures, 5 not implemented");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("1 example, 1 failure, 5 not implemented", notifier.getSummary());
    }

    public void testRSpec3() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures");
        notifier.processLine("0 examples, 0 failures");
        assertEquals("0 examples, 0 failures", notifier.getSummary());
    }

    public void testRSpec4AnsiColors() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("\033[1;35m2 examples, 0 failures, 5 not implemented\033[0m");
        notifier.processLine("\033[32m1 example, 1 failure\033[0m\r");
        assertEquals("3 examples, 1 failure, 5 not implemented", notifier.getSummary());
    }


    public void testCombined() {
        TestNotifier notifier = new TestNotifier(true, false);

        notifier.processLine("0 examples, 0 failures, 5 not implemented");
        notifier.processLine("1 example, 1 failure\r");
        assertEquals("1 example, 1 failure, 5 not implemented", notifier.getSummary());
        notifier.processLine("1 tests, 1 assertions, 1 failures, 1 errors");
        assertEquals("1 test, 1 assertion, 1 example, 2 failures, 1 error, 5 not implemented", notifier.getSummary());
    }
    
    public void testNoAccumulate() {
        TestNotifier notifier = new TestNotifier(false, false);
        notifier.processLine("35 tests, 81 assertions, 0 failures, 0 errors");
        notifier.processLine("10 tests, 1 assertions, 0 failures, 0 errors\r");
        assertEquals("0 failures", notifier.getSummary());
    }
}
