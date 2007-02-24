package org.netbeans.modules.uml.parser.java.attributetest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization.VariableInitializedInForLoopTest;
import org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization.VariableInitializedWithinBlockTest;
import org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization.VariableInitializedWithinClassTest;
import org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization.VariableInitializedWithinMethodTest;

public class DirectInitializationTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Class Tests");
		suite.addTest(new TestSuite(VariableInitializedWithinClassTest.class));
		suite.addTest(new TestSuite(VariableInitializedWithinMethodTest.class));
		suite.addTest(new TestSuite(VariableInitializedWithinBlockTest.class));
		suite.addTest(new TestSuite(VariableInitializedInForLoopTest.class));
		return suite;
	}
}
