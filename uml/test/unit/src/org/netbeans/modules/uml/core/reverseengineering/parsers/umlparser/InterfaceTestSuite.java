package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest.BasicInterfaceTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest.InterfaceBodyTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest.InterfaceContainsClassTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest.InterfaceContainsEnumTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest.MultipleInterfaceInheritanceTest;
import org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser.interfacetest.SingleInterfaceInheritanceTest;

public class InterfaceTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("UML Parser Interface Tests");
		suite.addTest(new TestSuite(BasicInterfaceTest.class));
		suite.addTest(new TestSuite(InterfaceBodyTest.class));
		suite.addTest(new TestSuite(InterfaceContainsClassTest.class));
		suite.addTest(new TestSuite(InterfaceContainsEnumTest.class));
		suite.addTest(new TestSuite(MultipleInterfaceInheritanceTest.class));
		suite.addTest(new TestSuite(SingleInterfaceInheritanceTest.class));
		return suite;
	}
}
