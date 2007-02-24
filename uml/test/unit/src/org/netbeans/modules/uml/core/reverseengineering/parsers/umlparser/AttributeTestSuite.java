package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.attributetest.ArrayTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.attributetest.AssignValueTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.attributetest.AssignValueToFinalVariableTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.attributetest.DirectInitializationTestSuite;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.attributetest.SimpleVariableDeclarationTestSuite;

public class AttributeTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Attribute Tests");
		suite.addTest(SimpleVariableDeclarationTestSuite.suite());
		suite.addTest(AssignValueTestSuite.suite());
		suite.addTest(AssignValueToFinalVariableTestSuite.suite());
		suite.addTest(DirectInitializationTestSuite.suite());
		suite.addTest(ArrayTestSuite.suite());
		return suite;
	}
}
