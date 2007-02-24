package org.netbeans.modules.uml.parser.java;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.modules.uml.parser.java.classtest.BasicClassTest;
import org.netbeans.modules.uml.parser.java.classtest.AnanymousClassTest;
import org.netbeans.modules.uml.parser.java.classtest.ClassBlockTest;
import org.netbeans.modules.uml.parser.java.classtest.ClassContainsInterfaceTest;
import org.netbeans.modules.uml.parser.java.classtest.GeneralizationImplementationClassTest;
import org.netbeans.modules.uml.parser.java.classtest.GeneralizationClassTest;
import org.netbeans.modules.uml.parser.java.classtest.ImplementationClassTest;
import org.netbeans.modules.uml.parser.java.classtest.MultipleClassTest;
import org.netbeans.modules.uml.parser.java.classtest.NestedClassTest;
import org.netbeans.modules.uml.parser.java.interfacetest.BasicInterfaceTest;
import org.netbeans.modules.uml.parser.java.interfacetest.InterfaceContainsClassTest;
import org.netbeans.modules.uml.parser.java.interfacetest.InterfaceContainsEnumTest;
import org.netbeans.modules.uml.parser.java.interfacetest.SingleInterfaceInheritanceTest;
import org.netbeans.modules.uml.parser.java.interfacetest.MultipleInterfaceInheritanceTest;
import org.netbeans.modules.uml.parser.java.interfacetest.InterfaceBodyTest;

public class InterfaceTestSuite {

	public static void main(String[] args) {
		TestRunner.run(suite());

	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Java Parser Interface Tests");
		suite.addTest(new TestSuite(BasicInterfaceTest.class));
		suite.addTest(new TestSuite(InterfaceContainsClassTest.class));
		suite.addTest(new TestSuite(InterfaceContainsEnumTest.class));
		suite.addTest(new TestSuite(SingleInterfaceInheritanceTest.class));
		suite
				.addTest(new TestSuite(
						MultipleInterfaceInheritanceTest.class));
		suite.addTest(new TestSuite(InterfaceBodyTest.class));
		return suite;
	}
}
