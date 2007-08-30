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
        TestNotifier notifier = new TestNotifier();
        
        assertTrue(notifier.recognizeLine("35 tests, 81 assertions, 0 failures, 1 errors"));
    }

    public void testRSpec() {
        TestNotifier notifier = new TestNotifier();
        
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 not implemented"));
        assertTrue(notifier.recognizeLine("1 example, 1 failure"));
    }
    
    public void testWindows() {        
        TestNotifier notifier = new TestNotifier();

        assertTrue(notifier.recognizeLine("35 tests, 81 assertions, 0 failures, 1 errors\r"));
        assertTrue(notifier.recognizeLine("5 examples, 3 failures, 5 not implemented\r"));
        assertTrue(notifier.recognizeLine("1 example, 1 failure\r"));
    }
    
    public void testNoFalseNegatives() {
        TestNotifier notifier = new TestNotifier();
        
        assertFalse(notifier.recognizeLine("1 for example, 1 failure"));
        assertFalse(notifier.recognizeLine("hello world"));
        assertFalse(notifier.recognizeLine("1"));
        assertFalse(notifier.recognizeLine("NoMethodError: You have a nil object when you didn't expect it!"));
        assertFalse(notifier.recognizeLine(".......E..........................."));
        assertFalse(notifier.recognizeLine("   C:/InstantRails/rails_apps/rfs/test/unit/rest_phone/phone_action/phone_action_subtypes_test.rb:182:in `test_event_response'"));
    }
}
