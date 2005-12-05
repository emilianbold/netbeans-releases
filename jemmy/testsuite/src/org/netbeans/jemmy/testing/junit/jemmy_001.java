package org.netbeans.jemmy.testing.junit;

import org.netbeans.jemmy.JemmyProperties;
public class jemmy_001 extends junit.framework.TestCase{
        public static junit.framework.TestSuite suite() {

            System.setProperty("jemmy.robot_dispatching", "on");
                    return(JUnitTest.suite("001"));
    }
}
