package org.netbeans.modules.uml.parser.java.attributetest;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.attributetest.AssignValueToFinalVariabletest.FinalVariableInitializationWithinBlockTest;
import org.netbeans.modules.uml.parser.java.attributetest.AssignValueToFinalVariabletest.FinalVariableInitializationWithinMethodTest;
import org.netbeans.modules.uml.parser.java.attributetest.AssignValueToFinalVariabletest.FinalVariableDirectInitializationTest;
public class AssignValueToFinalVariableTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Tests");
		suite.addTest(new TestSuite(FinalVariableInitializationWithinBlockTest.class));
		suite.addTest(new TestSuite(FinalVariableInitializationWithinMethodTest.class));
                suite.addTest(new TestSuite(FinalVariableDirectInitializationTest.class));			
		return suite;
	}
}
