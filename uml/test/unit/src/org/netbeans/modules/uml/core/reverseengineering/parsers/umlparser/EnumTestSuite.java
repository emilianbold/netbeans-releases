package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.enumtest.BasicEnumTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.enumtest.EnumWithLiteralAndAttributeTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.enumtest.EnumWithLiteralTest;

public class EnumTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Class Tests");
		suite.addTest(new TestSuite(BasicEnumTest.class));
		suite.addTest(new TestSuite(EnumWithLiteralAndAttributeTest.class));
		suite.addTest(new TestSuite(EnumWithLiteralTest.class));
		return suite;
	}
}
