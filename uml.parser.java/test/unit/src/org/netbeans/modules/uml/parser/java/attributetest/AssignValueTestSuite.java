package org.netbeans.modules.uml.parser.java.attributetest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.attributetest.assignvaluetest.AssignValueWithinBlockTest;
import org.netbeans.modules.uml.parser.java.attributetest.assignvaluetest.AssignValueWithinMethodTest;

public class AssignValueTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Tests");
		suite.addTest(new TestSuite(AssignValueWithinBlockTest.class));
		suite.addTest(new TestSuite(AssignValueWithinMethodTest.class));
			
		return suite;
	}
}
