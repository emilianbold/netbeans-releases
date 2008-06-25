package org.netbeans.jemmy.testing.junit;
public class jemmy_003 extends junit.framework.TestCase{
        public static junit.framework.TestSuite suite() {
            System.setProperty("jemmy.robot_dispatching", "on");
            System.setProperty("jemmy.smooth_robot_dispatching", "on");
        return(JUnitTest.suite("003"));
    }
}
