package org.netbeans.modules.uml.parser.java;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.attributetest.ArrayTestSuite;
import org.netbeans.modules.uml.parser.java.attributetest.AssignValueTestSuite;
import org.netbeans.modules.uml.parser.java.attributetest.AssignValueToFinalVariableTestSuite;
import org.netbeans.modules.uml.parser.java.attributetest.SimpleVariableDeclarationTestSuite;
import org.netbeans.modules.uml.parser.java.attributetest.DirectInitializationTestSuite;

public class AttributeTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Attribute Tests");
		suite.addTest(SimpleVariableDeclarationTestSuite.suite());
		suite.addTest(AssignValueTestSuite.suite());
		suite.addTest(AssignValueToFinalVariableTestSuite.suite());
		suite.addTest(DirectInitializationTestSuite.suite());
		suite.addTest(ArrayTestSuite.suite());
		return suite;
	}
}
